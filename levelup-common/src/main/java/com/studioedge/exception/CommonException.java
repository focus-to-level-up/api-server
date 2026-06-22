package com.studioedge.exception;

public abstract class CommonException extends RuntimeException {
    private final int status;

    protected CommonException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
