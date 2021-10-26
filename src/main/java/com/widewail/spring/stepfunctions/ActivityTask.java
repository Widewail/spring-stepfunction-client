package com.widewail.spring.stepfunctions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ActivityTask<T> {

    private final T input;
    private final String taskToken;

}
