package com.cjq.tool.memorytour.exception;

/**
 * Created by KAT on 2016/11/17.
 */
public class IllegalStageException extends RuntimeException {
    private static final long serialVersionUID = 4736399342531086333L;

    public IllegalStageException() {
        super();
    }

    public IllegalStageException(String detailMessage) {
        super(detailMessage);
    }

    public IllegalStageException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public IllegalStageException(Throwable throwable) {
        super(throwable);
    }
}
