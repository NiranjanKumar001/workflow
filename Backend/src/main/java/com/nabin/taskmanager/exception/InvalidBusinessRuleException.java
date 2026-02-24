package com.nabin.taskmanager.exception;

public class InvalidBusinessRuleException extends RuntimeException {

    public InvalidBusinessRuleException(String message) {
        super(message);
    }
}