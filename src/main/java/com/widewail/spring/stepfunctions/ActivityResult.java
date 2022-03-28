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
        SUCCESS, FAIL, CANCEL, HEARTBEAT
    }

    private final T payload;
    private final String failureReason;
    private final ActivityResultOutcome outcome;

    /**
     * Return an activity result representing an error. This method is intended
     * to report unexpected failure modes which may require engineering effort to
     * correct.
     * @param reason the error message
     * @return an activity result representing an error
     * @see #cancel(String) Use cancel to cancel execution without logging an
     * error.
     */
    public static <T> ActivityResult<T> fail(String reason) {
        return new ActivityResult<>(null, reason, ActivityResultOutcome.FAIL);
    }

    /**
     * Return an activity result representing a non-error cancellation. This
     * method is intended to report failure modes which are expected to
     * occur, such as operator error.
     * @param reason the error message
     * @return an activity result representing a non-error cancellation condition
     * @see #fail(String) Use fail for error conditions.
     */
    public static <T> ActivityResult<T> cancel(String reason) {
        return new ActivityResult<>(null, reason, ActivityResultOutcome.CANCEL);
    }

    public static <T> ActivityResult<T> success(T payload) {
        return new ActivityResult<>(payload, null, ActivityResultOutcome.SUCCESS);
    }

    public static <T> ActivityResult<T> heartbeat() {
        return new ActivityResult<>(null, null, ActivityResultOutcome.HEARTBEAT);
    }
}
