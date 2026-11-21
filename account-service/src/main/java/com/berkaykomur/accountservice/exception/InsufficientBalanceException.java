package com.berkaykomur.accountservice.exception;

public class InsufficientBalanceException extends BusinessException {
    public InsufficientBalanceException(String message) {
        super(message,"INSUFFICIENT_BALANCE");
    }
}
