package com.sandeep.retry.framework.model;

public class RetryTask<T> {

	private long	id;
	private T		task;

	public RetryTask(long id, T task) {
		this.id = id;
		this.task = task;
	}

	public long getId() {
		return this.id;
	}

	public T getTask() {
		return this.task;
	}

}
