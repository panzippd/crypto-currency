package com.crypto.currency.common.exception;

import com.google.common.base.Preconditions;

/**
 * @author Panzi
 * @Description customized exception
 * @date 2022/4/29 17:25
 */
public class BusinessException extends RuntimeException {
    private String code = "9999";

    private String desc = "The System is busy";

    public BusinessException() {

    }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(Throwable throwable) {

        super(throwable);
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static void throwIfUnchecked(Throwable throwable) {

        Preconditions.checkNotNull(throwable);
        throw new BusinessException(throwable);
    }

    public static void throwIfMessage(String message) {

        Preconditions.checkNotNull(message);
        throw new BusinessException(message);
    }

    public static void throwIfUnkown() {

        throw new BusinessException();
    }
}
