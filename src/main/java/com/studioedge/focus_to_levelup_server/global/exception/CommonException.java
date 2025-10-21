package com.studioedge.focus_to_levelup_server.global.exception;

public abstract class CommonException extends RuntimeException {

    protected CommonException() {
    }

    protected CommonException(Exception e) {
        super(e);
    }

    protected CommonException(String message) {
        super(message);
    }

    protected CommonException(Exception e, String msg) {
        super(msg, e);
    }
}
