package com.berkaykomur.accountservice.exception;

public class InsufficientBalanceException extends BaseException {
    public InsufficientBalanceException(String message) {
        super(message,"INSUFFICIENT_BALANCE");
    }
}
