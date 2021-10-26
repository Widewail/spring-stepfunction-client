package com.widewail.spring.stepfunctions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Add to methods to process Step Function activity tasks.
 *
 * Usage:
 *
 * {code}
 * <pre>
 * * @ActivityHandler(arn = "arn:...")
 * public void handleActivity(Map m)
 *</pre>
 * {code}
 *
 * Or if you want to also receive the ARN of the activity as a method parameter
 * add a second parameter of type string. The ordering of the method args must
 * match this example. This should one day be extracted out to another annotation
 * that can be put on a method param.
 *
 * {code}
 * <pre>
 * * @ActivityHandler(arn = "arn:...")
 * public void handleActivity(Map m, String arn)
 *</pre>
 * {code}
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ActivityHandler {
    String arn() default "";
}
