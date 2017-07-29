package com.ljaer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StringUtils;

import com.ljaer.dao.CodeTestDao;
import com.ljaer.po.CodeTest;

@RunWith(SpringJUnit4ClassRunner.class)  
@ContextConfiguration({"classpath:applicationContext.xml"}) 
public class CodeJunitTest {
	
	@Resource
	CodeTestDao codeTestDao;
	
	/**
	 * 使用存储过程
	 */
	@Test
	public void codeTest1(){
		Map<String, Object> map = new HashMap<>();
	        map.put("pre","PCH");
	        map.put("preLen", 3);
	        map.put("num", 8);
	        map.put("noLen", 4);
	        codeTestDao.getCode(map);
	        
	        // 返回生成的产品编码
	        String newCode = (String) map.get("newCode");
	        
	        System.out.println("生成的编码为："+newCode);
	}
	

	//代码同步，防止高并发
	private final static ReentrantLock lock;
	static{
		lock = new ReentrantLock();
	}
	
	/**
	 * 不使用存储过程
	 */
	@Test
	public void codeTest2(){
		Map<String, Object> map = new HashMap<String, Object>();

		String pre = "PCH";
		Integer preLen = 3;
		Integer num = 4;
		Integer noLen = 4;
		
		Date curr = new Date();
		SimpleDateFormat formatter = null;

		switch (num) {
		case 4:
			formatter = new SimpleDateFormat("yyyy");
			break;
		case 6:
			formatter = new SimpleDateFormat("yyyyMM");
			break;
		case 8:
			formatter = new SimpleDateFormat("yyyyMMdd");
			break;
		case 12:
			formatter = new SimpleDateFormat("yyyyMMddHHmm");
			break;
		case 14:
			formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			break;

		default:
			break;
		}

		String currentDate = formatter.format(curr);

		map.put("pre", pre);
		map.put("preLen", preLen);
		map.put("num", num);
		map.put("noLen", noLen);
		map.put("currentDate", currentDate);

		lock.lock();

		String newCode = "";
		String newNo = "";

		try {

			String oldCode = codeTestDao.getOldCode(map);

			if (StringUtils.isEmpty(oldCode)) {
				newNo = "1";
			} else {
				Integer maxNo = Integer.parseInt(oldCode.substring(oldCode.length() - noLen));
				newNo = String.valueOf(maxNo + 1);
			}

			int length = newNo.length();
			
			if(length > noLen){
				System.out.println("新流水号位数超限");
			}
			
			for (int i = 0; i < noLen - length; i++) {
				newNo = "0".concat(newNo);
			}

			newCode = pre + currentDate + newNo;

			CodeTest codeTest = new CodeTest();
			codeTest.setCode(newCode);

			codeTestDao.saveSelective(codeTest);

		} finally {
			lock.unlock();
		}

		System.out.println("生成的编码为："+newCode);
	}
	

}
