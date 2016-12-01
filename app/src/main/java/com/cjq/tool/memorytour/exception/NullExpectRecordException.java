package com.cjq.tool.memorytour.exception;

/**
 * Created by KAT on 2016/11/24.
 */
public class NullExpectRecordException extends RuntimeException {
    private static final long serialVersionUID = -3870476742965021477L;

    public NullExpectRecordException() {
        super();
    }

    public NullExpectRecordException(String detailMessage) {
        super(detailMessage);
    }

    public NullExpectRecordException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public NullExpectRecordException(Throwable throwable) {
        super(throwable);
    }
}
