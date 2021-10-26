package com.widewail.spring.stepfunctions;

import com.widewail.spring.stepfunctions.configuration.SchedulerConfiguration;
import com.widewail.spring.stepfunctions.configuration.StepFunctionsConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Automatically creates StepFunctions helper classes and enables
 * annotation support for activity processing methods.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({StepFunctionsConfig.class, SchedulerConfiguration.class})
public @interface EnableStepFunctionClient {
}
