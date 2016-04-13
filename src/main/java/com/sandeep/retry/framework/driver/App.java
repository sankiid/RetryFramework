package com.sandeep.retry.framework.driver;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.sandeep.retry.framework.handler.RetryHandler;

public class App {

	public static void main(String[] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		RetryHandler<RetryEntity> employeeInfoService = context.getBean(EmployeeInfoService.class);

		List<RetryEntity> list = new ArrayList<>();
		RetryEntity entity1 = new RetryEntity();
		entity1.setEmail("abc@xyz.com");
		entity1.setEmpCode(243);
		list.add(entity1);

		employeeInfoService.process(list, "ABC", "EMP-INFO-FETCHER", 10, 60, RetryEntity.class);

		list = new ArrayList<>();
		RetryEntity entity2 = new RetryEntity();
		entity2.setEmail("hgasdh@xyz.com");
		entity2.setEmpCode(23647);
		list.add(entity2);
		employeeInfoService.process(list, "ABC", "EMP-INFO-FETCHER", 10, 60, RetryEntity.class);
	}
}
