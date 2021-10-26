package com.widewail.spring.stepfunctions.configuration;

import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.AWSStepFunctionsClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.widewail.spring.stepfunctions.LocalWorkerNameProvider;
import com.widewail.spring.stepfunctions.StepFunctionsBeanPostProcessor;
import com.widewail.spring.stepfunctions.StepFunctionsService;
import com.widewail.spring.stepfunctions.StepFunctionsTemplate;
import com.widewail.spring.stepfunctions.WorkerNameProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;

public class StepFunctionsConfig {

    @Bean
    @ConditionalOnMissingBean(AWSStepFunctions.class)
    public AWSStepFunctions stepFunctions(){
        return AWSStepFunctionsClientBuilder.defaultClient();
    }

    @Bean
    @ConditionalOnMissingBean(StepFunctionsTemplate.class)
    public StepFunctionsTemplate stepFunctionTemplate(AWSStepFunctions client, ObjectMapper mapper, WorkerNameProvider workerNameProvider){
        return new StepFunctionsTemplate(client, mapper, workerNameProvider);
    }

    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

    @Bean
    public StepFunctionsBeanPostProcessor beanPostProcessor(StepFunctionsService stepFunctionsService) {
        return new StepFunctionsBeanPostProcessor(stepFunctionsService);
    }

    @Bean
    public StepFunctionsService stepFunctionsService(StepFunctionsTemplate stepFunctionsTemplate, TaskScheduler taskScheduler) {
        return new StepFunctionsService(stepFunctionsTemplate, taskScheduler);
    }

    @Bean
    @ConditionalOnMissingBean(WorkerNameProvider.class)
    public WorkerNameProvider instanceIdProvider(){
        return new LocalWorkerNameProvider();
    }
}
