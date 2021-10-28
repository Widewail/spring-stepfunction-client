# AWS Step Function Client for Spring

This library provides an easy way to write step function activity workers in a Spring
based Java app.

## Usage

Enable the client by adding `@EnableStepFunctionClient` to you application configuration.

```java

import com.widewail.spring.stepfunctions.EnableStepFunctionClient;

@SpringBootApplication
@EnableStepFunctionClient
public class Application {

}
```

## Configuration

AWS Step Functions allows a worker name to be specified when consuming a task. By default the worker name
will be the hostname of the machine consuming the task. If you would like to use the EC2 instance ID instead
of the hostname you can create an `@EC2WorkerNameProvider` bean instead. Both naming strategies allow for
a prefix to be added to the worker name. This is controlable via the `stepfunctions.client.workerName` property.

```properties
stepfunctions.client.workerName=worker-app
```

### Other configuration

If you need to customize the default `ObjectMapper` instance simply provide one of your own in the application context.

## Handling activities

Define your activity handlers using the `@ActivityHandler` annotation. The input to the handler method will be
the input to the activity. The method should then return the input to the next activity as its return value or
throw an exception to fail the task. If the argument to a method is an object then the payload to the task will
be deserialized accordingly. A `String` argument type will always receive the raw payload.

```java

import com.widewail.spring.stepfunctions.ActivityHandler;

@ActivityHandler(arn = "arn:task1")
public String appendStuff(String input) {
    return input + "-stuff";
}
```

Handler methods can have the following signatures

`R handle()` - no args returning the output from the task.

`R handle(T)` - Receive the input from the task. Return the output from the task.

`R handle(T, String)` - receive the input from the task and the task token. Return the output from the task.

```java

import com.widewail.spring.stepfunctions.ActivityHandler;

@ActivityHandler(arn = "arn:task2")
public String appendStuff(MyObject input, String taskToken) {
    //...
}
```

Handlers may also return a value of type `ActivityResult` to control the success/failure of a task.

```java

import com.widewail.spring.stepfunctions.ActivityHandler;

@ActivityHandler(arn = "arn:task2")
public ActivityResult<String> doTheThing(String input) {
    //...
    if(success) {
        return ActivityResult.success("return payload");
    } else {
        return ActivityResult.fail("error message");
    }
}
```