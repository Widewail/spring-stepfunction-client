package com.widewail.spring.stepfunctions;

import com.amazonaws.services.stepfunctions.model.ActivityDoesNotExistException;
import com.amazonaws.services.stepfunctions.model.InvalidArnException;
import com.amazonaws.services.stepfunctions.model.StateMachineDoesNotExistException;
import com.amazonaws.services.stepfunctions.model.TaskDoesNotExistException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class StepFunctionsService {

    private static final Logger log = LoggerFactory.getLogger(StepFunctionsService.class);

    private static final Set<Class<?>> FATAL_EXCEPTIONS = new HashSet<>(Arrays.asList(
            StateMachineDoesNotExistException.class,
            ActivityDoesNotExistException.class,
            InvalidArnException.class,
            TaskDoesNotExistException.class
    ));

    final StepFunctionsTemplate template;
    final TaskScheduler taskScheduler;

    public interface ActivityHandlerListener<INPUT, OUTPUT> {
        OUTPUT handleActivity(INPUT input, String taskToken) throws Throwable;
    }

    public static class AnnotationActivityHandlerListener implements ActivityHandlerListener {

        private final Method method;
        private final Object target;

        public AnnotationActivityHandlerListener(Object target, Method method) {
            this.target = target;
            this.method = method;
        }

        @Override
        public Object handleActivity(Object o, String taskToken) throws Throwable {
            try {
                switch (method.getParameterCount()) {
                    case 0:
                        return method.invoke(target);
                    case 1:
                        return method.invoke(target, o);
                    case 2:
                        return method.invoke(target, o, taskToken);
                    default:
                        throw new Exception("ActivityHandler method does not have a valid signature.");
                }
            } catch (InvocationTargetException t) {
                //unwrap from the reflective call
                throw t.getTargetException();
            }
        }
    }

    public <T> void addListener(String arn, Class<T> clazz, ActivityHandlerListener listener) {
        taskScheduler.scheduleWithFixedDelay(() -> {
            log.debug("Listening for activities on {}", arn);
            try {
                //potential race condition if we get back here before
                //a stop gets processed
                ActivityTask<T> at = template.waitForTask(arn, clazz);
                if (at != null) {
                    log.debug("Got activity for {}", arn);
                    Throwable activityHandlerError = null;
                    Object output = null;
                    try {
                        output = listener.handleActivity(at.getInput(), at.getTaskToken());
                        if (output instanceof ActivityResult) {
                            ActivityResult<?> result = (ActivityResult<?>) output;
                            if (result.isSuccess()) {
                                output = result.getPayload();
                            } else {
                                activityHandlerError = new RuntimeException(result.getFailureReason());
                            }
                        }
                    } catch (Throwable t) {
                        activityHandlerError = t;
                    }


                    if (activityHandlerError == null) {
                        log.debug("Sending activity success on {}", arn);
                        template.sendActivitySuccess(at.getTaskToken(), output);
                    } else {
                        log.error("Exception invoking activity handler", activityHandlerError);
                        log.debug("Sending activity failure for {} on {}", activityHandlerError.getClass().getSimpleName(), arn);
                        template.sendActivityFailure(at.getTaskToken(),
                                activityHandlerError.getMessage(),
                                activityHandlerError.getCause() != null ? activityHandlerError.getCause().getMessage() : null);
                    }
                }
            } catch (Throwable t) {
                //All kinds of exceptions can be thrown by the SDK and only a few
                //should actually stop the thread. The rest we can effectively
                //just treat as temporary situations (i.e. we shouldn't stop
                //trying to get new tasks)
                if (FATAL_EXCEPTIONS.contains(t.getClass())) {
                    log.error(arn + " listener stopped due to exception", t);
                    throw t;
                }
                if (log.isDebugEnabled())
                    log.warn(arn + " listener error", t);
            }
        }, 100L);
    }
}
