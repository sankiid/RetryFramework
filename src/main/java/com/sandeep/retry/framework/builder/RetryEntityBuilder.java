package com.sandeep.retry.framework.builder;

import com.sandeep.retry.framework.model.RetryEntity;

public class RetryEntityBuilder<T> {

	private RetryEntity<T> entity;

	public RetryEntityBuilder(T obj, Integer maxRetryCount, Integer timeInterval) {
		entity = new RetryEntity<T>();
		entity.setRequest(obj);
		entity.setMaxRetryCount(maxRetryCount);
		entity.setTimeInterval(timeInterval);
	}

	public RetryEntityBuilder<T> withUniqueKey(String uniqueKey) {
		entity.setUniqueKey(uniqueKey);
		return this;
	}

	public RetryEntityBuilder<T> withExternalApplicationReference(String externalApplicationReference) {
		entity.setExternalUniqueId(externalApplicationReference);
		return this;
	}

	public RetryEntity<T> build() {
		return entity;
	}
}
