package com.berkaykomur.accountservice.exception;

public class AccountNotFoundException extends BaseException {
    public AccountNotFoundException(String message) {
        super(message, "ACCOUNT_NOT_FOUND");
    }
}
