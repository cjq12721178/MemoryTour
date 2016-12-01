package com.cjq.tool.memorytour.exception;

/**
 * Created by KAT on 2016/11/8.
 */
public class NullMemoryPatternException extends RuntimeException {

    private static final long serialVersionUID = -6081617134204174982L;

    public NullMemoryPatternException() {
        super();
    }

    public NullMemoryPatternException(String detailMessage) {
        super(detailMessage);
    }
}
