package com.widewail.spring.stepfunctions;

import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.model.GetActivityTaskRequest;
import com.amazonaws.services.stepfunctions.model.GetActivityTaskResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.UncheckedIOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StepFunctionsTemplateTest {

    @Mock
    AWSStepFunctions client;

    @Mock
    WorkerNameProvider workerNameProvider;

    ObjectMapper om = new ObjectMapper();

    StepFunctionsTemplate fixture;

    @BeforeEach
    public void setup() {
        when(workerNameProvider.getWorkerName()).thenReturn("i-bogus");
        fixture = new StepFunctionsTemplate(client, om, workerNameProvider);
    }

    public static class TestObject {
        String foo;

        public String getFoo() {
            return foo;
        }

        public void setFoo(String foo) {
            this.foo = foo;
        }
    }

    @Test
    public void testGetTask() {
        when(client.getActivityTask(any())).thenReturn(
                new GetActivityTaskResult()
                        .withInput("{ \"foo\": \"bar\"}")
                        .withTaskToken("abc123"));

        ActivityTask<TestObject> activityTask = fixture.waitForTask("arn", TestObject.class);

        ArgumentCaptor<GetActivityTaskRequest> captor = ArgumentCaptor.forClass(GetActivityTaskRequest.class);
        verify(client).getActivityTask(captor.capture());
        assertThat(captor.getValue().getActivityArn()).isEqualTo("arn");
        assertThat(activityTask.getInput().getFoo()).isEqualTo("bar");
        assertThat(activityTask.getTaskToken()).isEqualTo("abc123");
    }

    @Test()
    public void testThrowOnInvalidInputJson(){
        when(client.getActivityTask(any())).thenReturn(
                new GetActivityTaskResult()
                        .withInput("{ \"boogers\": \"bar\"}")
                        .withTaskToken("abc123"));

        assertThatThrownBy(() -> fixture.waitForTask("arn", TestObject.class))
                .isInstanceOf(UncheckedIOException.class);
    }

    @Test
    public void testPollingTimeout(){
        when(client.getActivityTask(any()))
                .thenReturn(new GetActivityTaskResult())
                .thenReturn(new GetActivityTaskResult().withInput("{ \"foo\": \"bar\"}").withTaskToken("abc123"));

        fixture.waitForTask("arn", TestObject.class);
        fixture.waitForTask("arn", TestObject.class);

        verify(client, times(2)).getActivityTask(any());
    }

    @ParameterizedTest
    @CsvSource({"test,java.lang.String","{\"key\":\"value\"},java.util.Map"})
    public void testFetchedTasks(String input, String typeName) throws Exception {
        when(client.getActivityTask(any())).thenReturn(new GetActivityTaskResult().withInput(input).withTaskToken("test"));

        Class type = Class.forName(typeName);
        ActivityTask<?> activityTask = fixture.waitForTask("", type);

        assertThat(activityTask.getTaskToken()).isEqualTo("test");
        assertThat(activityTask.getInput()).isInstanceOf(type);
    }


}
