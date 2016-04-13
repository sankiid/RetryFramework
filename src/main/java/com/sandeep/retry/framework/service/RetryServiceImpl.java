package com.sandeep.retry.framework.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.sandeep.retry.framework.dao.IRetryDao;

public class RetryServiceImpl implements IRetryService {

	@Autowired
	private IRetryDao retryDao;

	@Override
	public <T> void save(List<T> obj, String source, String destination, int maxRetryCount, int timeInterval,
			Class<T> klass) {
		retryDao.save(obj, source, destination, maxRetryCount, timeInterval, klass);
	}

	@Override
	public <T> Map<Integer, T> getData(String destination, Class<T> klass) {
		return retryDao.getData(destination, klass);
	}

	@Override
	public void updateData(Map<Integer, Boolean> executeResponse) {
		retryDao.updateData(executeResponse);
	}
}
