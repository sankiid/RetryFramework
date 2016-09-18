package com.sandeep.retry.framework.dao;

import java.util.List;
import java.util.Map;
/**
 * 
 * @author sandeep
 *
 */
public interface IRetryDao {

	<T> void save(final RetryEntity<T> requestList, final String source, final String destination, final Class<T> klass);

	<T> void getRetryableTask(String source, String destination, Class<T> klass, BlockingQueue<RetryTask<T>> queue);

	void updateTaskResponse(RetryTaskResponse response);

}
