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

	private static final Logger					LOGGER				= LoggerFactory.getLogger(RetryHandler.class);
	private static final int					CONSUMER_POOL_SIZE	= 10;
	@Autowired
	private IRetryDao							retryDao;

	public volatile static boolean				shutdown			= false;

	private volatile String						source;
	private volatile String						destination;
	private volatile Class<T>					klass;
	private AtomicBoolean						registered			= new AtomicBoolean(false);

	private ScheduledExecutorService			producerPool;
	private ExecutorService						consumerPool;
	private BlockingQueue<RetryTask<T>>			taskQueue			= null;
	private BlockingQueue<RetryTaskResponse>	taskResponseQueue	= null;

	/**
	 * Register Retry Service before using it. The data in execute method is
	 * available based on this service.
	 * 
	 * @param source
	 * @param destination
	 * @param klass
	 * @param taskProducePerSeconds
	 * @param nextWaitTimeInSeconds
	 */
	public void registerService(String source, String destination, Class<T> klass) {
		if (!registered.getAndSet(true)) {
			this.source = source;
			this.destination = destination;
			this.klass = klass;
			initialize();
		}
	}

	public void initialize() {
		initializeThreadPool();
		initializeQueues();
		consumerRetryTaskResponse();
		produceRetryTask();
	}

	private void initializeThreadPool() {
		producerPool = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
			private AtomicInteger threadId = new AtomicInteger(1);

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "retry-producer-" + klass.getName() + "-" + threadId.getAndIncrement());
			}
		});

		consumerPool = Executors.newFixedThreadPool(CONSUMER_POOL_SIZE, new ThreadFactory() {
			private AtomicInteger threadId = new AtomicInteger(1);

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "retry-consumer-" + klass.getName() + "-" + threadId.getAndIncrement());
			}
		});
	}

	private void initializeQueues() {
		taskQueue = new LinkedBlockingQueue<>(10000);
		taskResponseQueue = new LinkedBlockingQueue<>();
	}

	private void consumerRetryTaskResponse() {
		for (int i = 0; i < CONSUMER_POOL_SIZE; ++i) {
			consumerPool.execute(new Runnable() {
				@Override
				public void run() {
					LOGGER.debug("starting retry events comsumer thread.");
					while (!shutdown) {
						try {
							RetryTaskResponse response = taskResponseQueue.take();
							retryDao.updateTaskResponse(response);
						} catch (Throwable e) {
							System.gc();
							LOGGER.error("Error in execution ", e);
						}
					}
				}
			});
		}
	}

	private void produceRetryTask() {
		execute(taskQueue, taskResponseQueue);
		producerPool.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					retryDao.getRetryableTask(source, destination, klass, taskQueue);
				} catch (Throwable t) {
					System.gc();
					LOGGER.error("Error in execution ", t);
				}
			}
		}, 10, 10, TimeUnit.SECONDS);
	}

	/**
	 * 
	 * @param requestList
	 */
	public void process(final RetryEntity<T> retryEntity) {
		if (retryEntity != null) {
			retryDao.save(retryEntity, source, destination, klass);
		}
	}

	@PreDestroy
	public void shutdownRetryHandler() {
		shutdown = true;
		if (producerPool != null) {
			try {
				producerPool.awaitTermination(5000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			producerPool.shutdown();
		}
		if (consumerPool != null) {
			try {
				consumerPool.awaitTermination(20000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			consumerPool.shutdown();
		}
	}

	public abstract void execute(BlockingQueue<RetryTask<T>> taskQueue, BlockingQueue<RetryTaskResponse> taskResponseQueue);

}
