package com.teamreports.weeklyreport.exception;

/** Thrown when an action is attempted against a report in an incompatible status. */
public class InvalidReportStateException extends RuntimeException {
    public InvalidReportStateException(String message) {
        super(message);
    }
}
