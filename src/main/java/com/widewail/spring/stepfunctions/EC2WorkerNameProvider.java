package com.widewail.spring.stepfunctions;

import com.amazonaws.util.EC2MetadataUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

public class EC2WorkerNameProvider implements WorkerNameProvider{

    @Value("${stepfunctions.client.workerName:}")
    private String workerNamePrefix;

    @Override
    public String getWorkerName() {
        String instanceId = EC2MetadataUtils.getInstanceId();
        if (workerNamePrefix == null || workerNamePrefix.isBlank())
            return instanceId;
        return workerNamePrefix + "-" + instanceId;
    }
}
