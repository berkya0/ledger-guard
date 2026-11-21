package com.berkaykomur.accountservice.exception;

public class SameAccountTransferException extends BusinessException {
    public SameAccountTransferException(String message) {
        super(message, "SAME_ACCOUNT_TRANSFER");
    }
}
