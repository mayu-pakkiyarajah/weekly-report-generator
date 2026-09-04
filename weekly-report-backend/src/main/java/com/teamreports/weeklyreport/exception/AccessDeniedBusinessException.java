package com.teamreports.weeklyreport.exception;

/** Thrown for business-level authorization failures (e.g. accessing another user's report). */
public class AccessDeniedBusinessException extends RuntimeException {
    public AccessDeniedBusinessException(String message) {
        super(message);
    }
}
