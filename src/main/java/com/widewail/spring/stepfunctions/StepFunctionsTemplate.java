package com.widewail.spring.stepfunctions;

import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.model.GetActivityTaskRequest;
import com.amazonaws.services.stepfunctions.model.GetActivityTaskResult;
import com.amazonaws.services.stepfunctions.model.SendTaskFailureRequest;
import com.amazonaws.services.stepfunctions.model.SendTaskHeartbeatRequest;
import com.amazonaws.services.stepfunctions.model.SendTaskSuccessRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * @author Adam Burnett
 */
@Slf4j
public class StepFunctionsTemplate {

    private final AWSStepFunctions client;

    private final ObjectMapper objectMapper;

    private final WorkerNameProvider workerNameProvider;

    //per the docs (http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/stepfunctions/AWSStepFunctions.html#getActivityTask-com.amazonaws.services.stepfunctions.model.GetActivityTaskRequest-)
    private final int requestTimeout = 65000;

    public StepFunctionsTemplate(AWSStepFunctions client, ObjectMapper objectMapper) {
        this(client, objectMapper, new LocalWorkerNameProvider());
    }

    public StepFunctionsTemplate(AWSStepFunctions client, ObjectMapper objectMapper, WorkerNameProvider workerNameProvider) {
        this.client = client;
        this.objectMapper = objectMapper;
        this.workerNameProvider = workerNameProvider;
    }

    /**
     * Checks it a task is available right away and returns if none is.
     *
     * @param activityArn
     * @param dataType    - Object type to map, String for raw JSON or null for no input data
     * @param <T>
     * @return null if no task is available.
     */
    public <T> ActivityTask<T> fetchTask(String activityArn, Class<T> dataType) {
        return waitForTask(activityArn, dataType, 1);
    }

    /**
     * Waits for a task to become available. If no task comes in the specified
     * request timeout then a null object is returned.
     *
     * @param activityArn
     * @param dataType    - Object type to map, String for raw JSON or null for no input data
     * @param <T>
     * @return
     */
    public <T> ActivityTask<T> waitForTask(String activityArn, Class<T> dataType) {
        return this.waitForTask(activityArn, dataType, requestTimeout);
    }

    private <T> ActivityTask<T> waitForTask(String activityArn, Class<T> dataType, int timeout) {

        GetActivityTaskResult getActivityTaskResult = client.getActivityTask(
                new GetActivityTaskRequest()
                        .withActivityArn(activityArn)
                        .withWorkerName(workerNameProvider.getWorkerName())
                        .withSdkClientExecutionTimeout(timeout)
                        .withSdkRequestTimeout(timeout));

        if (getActivityTaskResult == null || getActivityTaskResult.getTaskToken() == null)
            return null;

        T input = null;
        if (dataType == String.class) {
            //caller just wants raw json
            input = (T) getActivityTaskResult.getInput();
        } else if (dataType != null) {
            try {
                input = objectMapper.readValue(getActivityTaskResult.getInput(), dataType);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return new ActivityTask<T>(input, getActivityTaskResult.getTaskToken());
    }


    public void sendActivitySuccess(String token, Object output) {
        try {
            String json = objectMapper.writeValueAsString(output);
            SendTaskSuccessRequest req = new SendTaskSuccessRequest()
                    .withTaskToken(token)
                    .withOutput(json);
            client.sendTaskSuccess(req);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void sendActivityFailure(String token, String error, String cause) {
        SendTaskFailureRequest req = new SendTaskFailureRequest()
                .withTaskToken(token)
                .withError(error == null ? "Unknown error." : error)
                .withCause(cause == null ? "Unknown cause." : cause);
        client.sendTaskFailure(req);
    }

    public void sendActivityHeartbeat(String token) {
        client.sendTaskHeartbeat(new SendTaskHeartbeatRequest()
                .withTaskToken(token));
    }

}
