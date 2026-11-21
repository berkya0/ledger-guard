package com.berkaykomur.transferservice.exception;

public class SameAccountTransferException extends BaseException {
    public SameAccountTransferException(String message) {
        super(message, "SAME_ACCOUNT_TRANSFER");
    }
}
