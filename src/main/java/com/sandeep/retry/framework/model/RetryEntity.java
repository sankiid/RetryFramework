package com.sandeep.retry.framework.model;

public class RetryEntity<T> {

	private T		entityData;
	private String	uniqueKey;
	private String	externalAppRef;
	private Integer	timeInterval;
	private Integer	maxRetryCount;

	public T getRequest() {
		return entityData;
	}

	public void setRequest(T request) {
		this.entityData = request;
	}

	public String getUniqueKey() {
		return uniqueKey;
	}

	public void setUniqueKey(String uniqueKey) {
		this.uniqueKey = uniqueKey;
	}

	public String getExternalUniqueId() {
		return externalAppRef;
	}

	public void setExternalUniqueId(String externalUniqueId) {
		this.externalAppRef = externalUniqueId;
	}

	public String getExternalApplicationRef() {
		return externalAppRef;
	}

	public void setExternalApplicationRef(String externalApplicationRef) {
		this.externalAppRef = externalApplicationRef;
	}

	public Integer getTimeInterval() {
		return timeInterval;
	}

	public void setTimeInterval(Integer timeInterval) {
		this.timeInterval = timeInterval;
	}

	public Integer getMaxRetryCount() {
		return maxRetryCount;
	}

	public void setMaxRetryCount(Integer maxRetryCount) {
		this.maxRetryCount = maxRetryCount;
	}

	@Override
	public String toString() {
		return "Request [request=" + entityData + ", uniqueKey=" + uniqueKey + ", externalApplicationRef=" + externalAppRef + ", timeInterval="
				+ timeInterval + ", maxRetryCount=" + maxRetryCount + "]";
	}

}
