package com.widewail.spring.stepfunctions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.widewail.spring.stepfunctions.configuration.SchedulerConfiguration;
import com.widewail.spring.stepfunctions.configuration.StepFunctionsConfig;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.TaskScheduler;

@Configuration
@Import(SchedulerConfiguration.class)
public class TestConfiguration {

    @Bean
    public StepFunctionsTemplate stepFunctions(){
        return Mockito.mock(StepFunctionsTemplate.class);
    }

    @Bean
    public ActivityHandlerTest.TestHandlerClass testHandlerClass(){
        return new ActivityHandlerTest.TestHandlerClass();
    }

    @Bean
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

//    public static class MockAWSStepFunctions implements AWSStepFunctions {
//
//        ObjectMapper objectMapper = new ObjectMapper();
//
//        CountDownLatch requestLatch = new CountDownLatch(1);
//
//        volatile Map<String, GetActivityTaskResult> nextActivityResult = new ConcurrentHashMap<>();
//        SendTaskFailureRequest lastFailure;
//        SendTaskHeartbeatRequest lastHeartbeat;
//        SendTaskSuccessRequest lastSuccess;
//
//        public void reset(){
//            nextActivityResult.clear();
//            lastFailure = null;
//            lastSuccess = null;
//            lastHeartbeat = null;
//            requestLatch = new CountDownLatch(1);
//        }
//
//        @Override
//        public SendTaskFailureResult sendTaskFailure(SendTaskFailureRequest sendTaskFailureRequest) {
//            this.lastFailure = sendTaskFailureRequest;
//            requestLatch.countDown();
//            return null;
//        }
//
//        @Override
//        public SendTaskHeartbeatResult sendTaskHeartbeat(SendTaskHeartbeatRequest sendTaskHeartbeatRequest) {
//            this.lastHeartbeat = sendTaskHeartbeatRequest;
//            requestLatch.countDown();
//            return null;
//        }
//
//        @Override
//        public SendTaskSuccessResult sendTaskSuccess(SendTaskSuccessRequest sendTaskSuccessRequest) {
//            this.lastSuccess = sendTaskSuccessRequest;
//            requestLatch.countDown();
//            return null;
//        }
//
//        @Override
//        public GetActivityTaskResult getActivityTask(GetActivityTaskRequest getActivityTaskRequest) {
//            while(!nextActivityResult.containsKey(getActivityTaskRequest.getActivityArn())){
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                }
//            }
//            //not the most thread safe but...tests
//            GetActivityTaskResult retval = nextActivityResult.get(getActivityTaskRequest.getActivityArn());
//            nextActivityResult.remove(getActivityTaskRequest.getActivityArn());
//            return retval;
//        }
//
////        public GetActivityTaskResult getNextActivityResult() {
////            return nextActivityResult;
////        }
//
//        public void setNextActivity(Object input, String arn){
//            try {
//                String json = objectMapper.writeValueAsString(input);
//                GetActivityTaskResult res = new GetActivityTaskResult()
//                        .withInput(json)
//                        .withTaskToken("task_token");
//                nextActivityResult.put(arn, res);
//            } catch (JsonProcessingException e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        public SendTaskFailureRequest getLastFailure() {
//            return lastFailure;
//        }
//
//        public SendTaskHeartbeatRequest getLastHeartbeat() {
//            return lastHeartbeat;
//        }
//
//        public SendTaskSuccessRequest getLastSuccess() {
//            return lastSuccess;
//        }
//
//        public CountDownLatch getRequestLatch() {
//            return requestLatch;
//        }
//
//        @Override
//        public void setEndpoint(String endpoint) {
//
//        }
//
//        @Override
//        public void setRegion(Region region) {
//
//        }
//
//        @Override
//        public CreateActivityResult createActivity(CreateActivityRequest createActivityRequest) {
//            return null;
//        }
//
//        @Override
//        public CreateStateMachineResult createStateMachine(CreateStateMachineRequest createStateMachineRequest) {
//            return null;
//        }
//
//        @Override
//        public DeleteActivityResult deleteActivity(DeleteActivityRequest deleteActivityRequest) {
//            return null;
//        }
//
//        @Override
//        public DeleteStateMachineResult deleteStateMachine(DeleteStateMachineRequest deleteStateMachineRequest) {
//            return null;
//        }
//
//        @Override
//        public DescribeActivityResult describeActivity(DescribeActivityRequest describeActivityRequest) {
//            return null;
//        }
//
//        @Override
//        public DescribeExecutionResult describeExecution(DescribeExecutionRequest describeExecutionRequest) {
//            return null;
//        }
//
//        @Override
//        public DescribeStateMachineResult describeStateMachine(DescribeStateMachineRequest describeStateMachineRequest) {
//            return null;
//        }
//
//        @Override
//        public DescribeStateMachineForExecutionResult describeStateMachineForExecution(DescribeStateMachineForExecutionRequest describeStateMachineForExecutionRequest) {
//            return null;
//        }
//
//        @Override
//        public GetExecutionHistoryResult getExecutionHistory(GetExecutionHistoryRequest getExecutionHistoryRequest) {
//            return null;
//        }
//
//        @Override
//        public ListActivitiesResult listActivities(ListActivitiesRequest listActivitiesRequest) {
//            return null;
//        }
//
//        @Override
//        public ListExecutionsResult listExecutions(ListExecutionsRequest listExecutionsRequest) {
//            return null;
//        }
//
//        @Override
//        public ListStateMachinesResult listStateMachines(ListStateMachinesRequest listStateMachinesRequest) {
//            return null;
//        }
//
//        @Override
//        public ListTagsForResourceResult listTagsForResource(ListTagsForResourceRequest listTagsForResourceRequest) {
//            return null;
//        }
//
//        @Override
//        public StartExecutionResult startExecution(StartExecutionRequest startExecutionRequest) {
//            return null;
//        }
//
//        @Override
//        public StartSyncExecutionResult startSyncExecution(StartSyncExecutionRequest startSyncExecutionRequest) {
//            return null;
//        }
//
//        @Override
//        public StopExecutionResult stopExecution(StopExecutionRequest stopExecutionRequest) {
//            return null;
//        }
//
//        @Override
//        public TagResourceResult tagResource(TagResourceRequest tagResourceRequest) {
//            return null;
//        }
//
//        @Override
//        public UntagResourceResult untagResource(UntagResourceRequest untagResourceRequest) {
//            return null;
//        }
//
//        @Override
//        public UpdateStateMachineResult updateStateMachine(UpdateStateMachineRequest updateStateMachineRequest) {
//            return null;
//        }
//
//        @Override
//        public void shutdown() {
//
//        }
//
//        @Override
//        public ResponseMetadata getCachedResponseMetadata(AmazonWebServiceRequest request) {
//            return null;
//        }
//    }
}
