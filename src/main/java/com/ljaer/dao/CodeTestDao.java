package com.ljaer.dao;

import java.util.Map;

import com.ljaer.po.CodeTest;

public interface CodeTestDao {
	void getCode(Map<String, Object> map);

	String getOldCode(Map<String, Object> map);

	void saveSelective(CodeTest codeTest); 
}