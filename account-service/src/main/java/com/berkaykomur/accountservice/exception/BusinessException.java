package com.berkaykomur.accountservice.exception;

public class BusinessException extends BaseException {
    public BusinessException(String message,String errorCode) {
        super(errorCode, message);
    }
}
