package com.ticket_booking.common.configs;

import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration(proxyBeanMethods = false)
@EnableAsync
public class AsyncConfig {

	@Bean 
	TaskExecutor taskExecutor(ContextPropagatingTaskDecorator taskDecorator) {
		var adapter = new TaskExecutorAdapter(
	            Executors.newVirtualThreadPerTaskExecutor()
	    );
		
		adapter.setTaskDecorator(taskDecorator);
		
		return adapter;
	}
	
	@Bean
    ContextPropagatingTaskDecorator contextPropagatingTaskDecorator() {
        return new ContextPropagatingTaskDecorator();
    }
}
