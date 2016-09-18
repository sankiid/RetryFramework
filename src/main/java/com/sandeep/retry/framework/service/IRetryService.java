package com.sandeep.retry.framework.service;

import java.util.List;
import java.util.Map;

public interface IRetryService {

	<T> void save(List<T> obj, String source, String destination, int maxRetryCount, int timeInterval, Class<T> klass);

	<T> Map<Integer, T> getData(String destination, Class<T> klass);

	void updateData(Map<Integer, Boolean> executeResponse);

}
