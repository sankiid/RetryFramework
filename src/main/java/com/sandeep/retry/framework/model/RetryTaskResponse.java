package com.sandeep.retry.framework.model;

public class RetryTaskResponse {

	private long		id;
	private boolean	status;

	public RetryTaskResponse(long id, boolean status) {
		this.id = id;
		this.status = status;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @return the status
	 */
	public boolean isStatus() {
		return status;
	}

}
