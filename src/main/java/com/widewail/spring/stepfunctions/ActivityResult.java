package com.widewail.spring.stepfunctions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * May be optionally returned from an `@ActivityHandler` to indicate
 * the success or failure of an activity. This can be used as an alternative
 * to throwing an exception on activity failure.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ActivityResult<T> {
    public enum ActivityResultOutcome {
        SUCCESS, FAIL, HEARTBEAT
    }

    private final T payload;
    private final String failureReason;
    private final ActivityResultOutcome outcome;

    public static <T> ActivityResult<T> fail(String reason) {
        return new ActivityResult<>(null, reason, ActivityResultOutcome.FAIL);
    }

    public static <T> ActivityResult<T> success(T payload) {
        return new ActivityResult<>(payload, null, ActivityResultOutcome.SUCCESS);
    }

    public static <T> ActivityResult<T> heartbeat() {
        return new ActivityResult<>(null, null, ActivityResultOutcome.HEARTBEAT);
    }
}
