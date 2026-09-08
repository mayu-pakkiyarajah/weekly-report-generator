package com.teamreports.weeklyreport.exception;

/** Thrown when the AI assistant is called but no API key / not enabled. */
public class AiUnavailableException extends RuntimeException {
    public AiUnavailableException(String message) {
        super(message);
    }
}
