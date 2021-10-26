package com.widewail.spring.stepfunctions;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;

import java.net.Inet4Address;

@RequiredArgsConstructor
public class LocalWorkerNameProvider implements WorkerNameProvider {

    @Value("${stepfunctions.client.workerName:}")
    private String workerNamePrefix;

    @Override
    @SneakyThrows
    public String getWorkerName() {
        String hostname = Inet4Address.getLocalHost().getHostName();
        if (workerNamePrefix == null || workerNamePrefix.isBlank()) {
            return hostname;
        }
        return workerNamePrefix + "-" + hostname;
    }
}
