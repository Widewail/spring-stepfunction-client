package com.widewail.spring.stepfunctions.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

public class SchedulerConfiguration {

    @Bean(name = "stepfunctionsClientTaskScheduler")
    public TaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(30);
        threadPoolTaskScheduler.setThreadNamePrefix("sfn-client-poller-");
        threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);
//        threadPoolTaskScheduler.setAwaitTerminationMillis(25000L);
        threadPoolTaskScheduler.initialize();
        return threadPoolTaskScheduler;
    }
}
