package com.ticket_booking.common;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {

	@Bean 
	Executor taskExecutor() {
		return new TaskExecutorAdapter(
				Executors.newVirtualThreadPerTaskExecutor()
				);
	}
}
