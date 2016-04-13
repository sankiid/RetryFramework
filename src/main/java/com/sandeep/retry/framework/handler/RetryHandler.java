package com.sandeep.retry.framework.handler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import com.sandeep.retry.framework.dao.IRetryDao;
import com.sandeep.retry.framework.service.IRetryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Sandeep
 *
 */
public abstract class RetryHandler<T> {
	private static final Logger LOGGER = LoggerFactory.getLogger(RetryHandler.class);

	public static boolean			initialize	= false;
	public volatile static boolean	shutdown	= false;
	public static final Object		lock		= new Object();
	public static final long		TIMEOUT		= 30 * 1000;

	@Autowired
	private IRetryService  	retryService;
	private ExecutorService executor;

	@PostConstruct
	public void init() {
		executor = Executors.newFixedThreadPool(1);
	}

	@SuppressWarnings("null")
	public void process(List<T> obj,  String source, final String destination, int maxRetryCount, int timeInterval,
			final Class<T> klass) {
		if (obj != null || !obj.isEmpty()) {
			retryService.save(obj, source, destination, maxRetryCount, timeInterval, klass);
		}
		if (!initialize) {
			initialize = true;
			executor.execute(new Runnable() {
				@Override
				public void run() {
					while (!shutdown) {
						synchronized (lock) {
							try {
								lock.wait(TIMEOUT);
							} catch (InterruptedException e) {
								LOGGER.error("Lock Error::", e);
							}
						}
						Map<Integer, T> data = retryService.getData(destination, klass);
						if (data == null || data.isEmpty()) {
							continue;
						}
						Map<Integer, Boolean> executeResponse = execute(data);
						retryService.updateData(executeResponse);
					}
				}
			});
		}
	}

	public void shutdownRetryHandler() {
		shutdown = true;
		if (executor != null) {
			try {
				executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			executor.shutdown();
		}
	}

	public abstract Map<Integer, Boolean> execute(Map<Integer, T> data);

}
