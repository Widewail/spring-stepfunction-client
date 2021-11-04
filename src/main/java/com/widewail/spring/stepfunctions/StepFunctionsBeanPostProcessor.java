package com.widewail.spring.stepfunctions;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringValueResolver;

import java.lang.reflect.Method;

/**
 * Inspects beans for @ActivityHandler annotations, resolves any Spring property placeholders
 * and registers listeners with the StepFunctionsService.
 *
 */
@RequiredArgsConstructor
public class StepFunctionsBeanPostProcessor implements BeanPostProcessor, Ordered, EmbeddedValueResolverAware {

    private static final Logger log = LoggerFactory.getLogger(StepFunctionsBeanPostProcessor.class);

    private final StepFunctionsService stepFunctionsService;

    private StringValueResolver valueResolver;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithMethods(bean.getClass(), method -> {
            log.info("Registering ActivityHandler method {}::{}", bean.getClass(), method.getName());

            ActivityHandler annotation = method.getAnnotation(ActivityHandler.class);
            String arn = valueResolver.resolveStringValue(annotation.arn());
            if (arn == null || arn.isBlank()){
                return;
            }
            validateActivityArn(arn);
            validateHandlerMethod(method);

            Class inputType = null;
            if(method.getParameterCount() > 0){
                inputType = method.getParameterTypes()[0];
            }
            stepFunctionsService.addListener(arn, inputType, new StepFunctionsService.AnnotationActivityHandlerListener(bean, method));
        }, method -> method.getAnnotation(ActivityHandler.class) != null);

        return bean;
    }

    private void validateActivityArn(String arn) {
        if(!arn.startsWith("arn"))
            throw new BeanDefinitionValidationException(ActivityHandler.class.getSimpleName() + " must contain a valid arn.");
    }

    private void validateHandlerMethod(Method method) {
        if(method.getParameterCount() > 2)
            throw new BeanDefinitionValidationException(ActivityHandler.class.getSimpleName() + " method " + method.getName() + " must have 0, 1 or 2 parameters.");

        //TODO: should just create a @TaskArn method parameter annotation so we don't have to enforce a magical method signature.
        if(method.getParameterCount() == 2 && !method.getParameterTypes()[1].equals(String.class))
            throw new BeanDefinitionValidationException(ActivityHandler.class.getSimpleName() + " method " + method.getName() + " must have signature (Object,String)");
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.valueResolver = resolver;
    }
}
