package swordfishsync.exceptions;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(CustomAsyncExceptionHandler.class);

	@Override
	public void handleUncaughtException(Throwable throwable, Method method, Object... obj) {
		log.error("Exception occurred in acync method for Class [" + method.getClass().getName() + "] and method [" + method.getName() + "]", throwable);
		for (Object param : obj) {
			log.error("Parameter value - " + param);
		}
	}

}
