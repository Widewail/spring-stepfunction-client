package com.widewail.spring.stepfunctions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfiguration.class})
public class ActivityHandlerTest {

    public static class TestHandlerClass {

        @ActivityHandler(arn = "arn:one")
        public String doIt(String o) throws Exception{
            return o;
        }

        @ActivityHandler(arn = "arn:zero")
        public String zeroArgs() throws Exception {
            return "hi";
        }

        @ActivityHandler(arn = "arn:two")
        public Map<String,String> twoArgs(Map<String, String> o, String taskToken) throws Exception {
            assertThat(o).isNotNull();
            assertThat(taskToken).isEqualTo("arn:two-token");
            return o;
        }

        @ActivityHandler(arn = "arn:throw")
        public void kaboom() throws Exception {
            throw new Exception("kaboom", new Exception("just be cause"));
        }

        @ActivityHandler(arn = "arn:activityResultSuccess")
        public ActivityResult<String> activityResultSuccess() throws Exception {
            return ActivityResult.success("foo");
        }

        @ActivityHandler(arn = "arn:activityResultFail")
        public ActivityResult<String> activityResultError() throws Exception {
            return ActivityResult.fail("boo");
        }

        @ActivityHandler(arn = "arn:activityResultHeartbeat")
        public ActivityResult<String> activityResultHeartbeat() throws Exception {
            return ActivityResult.heartbeat();
        }

    }

    @Autowired
    StepFunctionsTemplate stepFunctionsTemplate;

    @Autowired
    TestHandlerClass testHandlerClass;

    @BeforeEach
    public void setup() {
        Mockito.reset(stepFunctionsTemplate);
    }

    private <T> CountDownLatch waitForTaskCompletion(String arn, T input) {
        final CountDownLatch latch = new CountDownLatch(1);
        doAnswer(a -> {
            latch.countDown();
            return null;
        }).when(stepFunctionsTemplate).sendActivitySuccess(eq(arn), any());
        doAnswer(a -> {
            latch.countDown();
            return null;
        }).when(stepFunctionsTemplate).sendActivityFailure(eq(arn), anyString(), anyString());

        when(stepFunctionsTemplate.waitForTask(eq(arn), any()))
                .thenReturn(new ActivityTask<>(input, arn + "-token"))
                .thenAnswer(a -> {
                    Thread.sleep(30000l);
                    return null;
                });
        return latch;
    }

    @Test
    public void testSuccessfulActivity() throws Exception {
        CountDownLatch latch = waitForTaskCompletion("arn:zero", "");
        latch.await(1, TimeUnit.SECONDS);

        verify(stepFunctionsTemplate, atLeastOnce()).sendActivitySuccess(eq("arn:zero-token"), eq("hi"));
    }

    @Test
    public void testActivityHandlerThrowsException() throws InterruptedException {
        CountDownLatch latch = waitForTaskCompletion("arn:throw", "");
        latch.await(1, TimeUnit.SECONDS);

        verify(stepFunctionsTemplate, atLeastOnce()).sendActivityFailure(eq("arn:throw-token"), eq("kaboom"), eq("just be cause"));
    }

    @Test
    public void testOneArgHandler() throws Exception {
        CountDownLatch latch = waitForTaskCompletion("arn:one", "one");
        latch.await(1, TimeUnit.SECONDS);

        verify(stepFunctionsTemplate, atLeastOnce()).sendActivitySuccess(eq("arn:one-token"), eq("one"));
    }

    @Test
    public void testTwoArgHandler() throws Exception {
        CountDownLatch latch = waitForTaskCompletion("arn:two", Map.of("a", "b"));
        latch.await(1, TimeUnit.SECONDS);

        verify(stepFunctionsTemplate, atLeastOnce()).sendActivitySuccess(
                eq("arn:two-token"),
                any(Map.class)
            );
    }

    @Test
    public void testActivityResultSuccess() throws Exception {
        CountDownLatch latch = waitForTaskCompletion("arn:activityResultSuccess", Map.of("a", "b"));
        latch.await(1, TimeUnit.SECONDS);

        verify(stepFunctionsTemplate, atLeastOnce()).sendActivitySuccess(
                eq("arn:activityResultSuccess-token"),
                eq("foo")
        );
    }

    @Test
    public void testActivityResultFailure() throws Exception {
        CountDownLatch latch = waitForTaskCompletion("arn:activityResultFail", Map.of("a", "b"));
        latch.await(1, TimeUnit.SECONDS);

        verify(stepFunctionsTemplate, atLeastOnce()).sendActivityFailure(
                eq("arn:activityResultFail-token"),
                eq("boo"),
                eq(null)
        );
    }

    @Test
    public void testActivityResultHeartbeat() throws Exception {
        CountDownLatch latch = waitForTaskCompletion("arn:activityResultHeartbeat", Map.of("a", "b"));
        latch.await(1, TimeUnit.SECONDS);

        verify(stepFunctionsTemplate, atLeastOnce()).sendActivityHeartbeat(
                eq("arn:activityResultHeartbeat-token")
        );
    }

//
//    @Test
//    public void testTwoArgHandler() throws InterruptedException {
//        StepFunctionsTemplateTest.TestObject o = new StepFunctionsTemplateTest.TestObject();
//        stepFunctions.setNextActivity(o, "arn:two");
//
//        //wait for the actiity to get processed
//        assertThat(stepFunctions.getRequestLatch().await(3, TimeUnit.SECONDS)).isTrue();
//
//        SendTaskSuccessRequest success = stepFunctions.getLastSuccess();
//        assertThat(success.getOutput()).contains("arn:two");
//    }
}
