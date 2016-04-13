package com.sandeep.retry.framework.driver;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sandeep.retry.framework.handler.RetryHandler;

@Service
public class EmployeeInfoService extends RetryHandler<RetryEntity> {

	@Override
	public Map<Integer, Boolean> execute(Map<Integer, RetryEntity> data) {
		/*
		 * execute some logic on the give data and send back the true/false on
		 * that logic
		 */
		Map<Integer, Boolean> map = new HashMap<>();
		for (Integer key : data.keySet()) {
			map.put(key, Boolean.FALSE);
		}
		return map;
	}

}
