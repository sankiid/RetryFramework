package com.sandeep.retry.framework.annotation;

import java.util.concurrent.TimeUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component("retryAspect")
public class RetryAspect {
	private static final Logger LOGGER = LoggerFactory.getLogger(RetryAspect.class);

	@Around("execution(* *(..)) && @annotation(retry)")
	public Object retryAroundAdvice(ProceedingJoinPoint proceedingJoinPoint, Retry retry) throws Throwable {
		Signature signature = proceedingJoinPoint.getSignature();
		Object object = null;
		int attempts = retry.attempts();
		long delay = retry.delay();
		TimeUnit unit = retry.unit();
		long delayInMilliSec = unit.toMillis(delay);
		boolean attemptSuccess = false;
		while (!attemptSuccess && attempts-- > 0) {
			try {
				LOGGER.info("Entering into {}.{}", signature.getDeclaringTypeName(), signature.getName());
				object = proceedingJoinPoint.proceed();
				LOGGER.info("Exiting from {}.{}", signature.getDeclaringTypeName(), signature.getName());
				attemptSuccess = true;
			} catch (Throwable t) {
				LOGGER.error("attempt got failed ", t.getMessage());
				synchronized (proceedingJoinPoint) {
					try {
						proceedingJoinPoint.wait(delayInMilliSec);
					} catch (InterruptedException e) {
					}
				}
				LOGGER.warn("re-attempt {}.{}", signature.getDeclaringTypeName(), signature.getName());
			}
		}
		if (!attemptSuccess) {
			LOGGER.warn("all attempt failed to process {}.{}", signature.getDeclaringTypeName(), signature.getName());
		}
		return object;
	}
}
