package com.sandeep.retry.framework.dao;

import java.util.List;
import java.util.Map;
/**
 * 
 * @author sandeep
 *
 */
public interface IRetryDao {

	<T> void save(final List<T> obj, final String source, final String destination, final int maxRetryCount, final int timeInterval, final Class<T> klass);
	
	<T> Map<Integer, T> getData(String destination, final Class<T> klass);
	
	void updateData(Map<Integer, Boolean> executeResponse);
	
}
