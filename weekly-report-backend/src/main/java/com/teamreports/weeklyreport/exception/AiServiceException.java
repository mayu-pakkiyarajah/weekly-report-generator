package com.teamreports.weeklyreport.exception;

/** Thrown when the upstream Anthropic API call fails or returns an unexpected shape. */
public class AiServiceException extends RuntimeException {
    public AiServiceException(String message) {
        super(message);
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
