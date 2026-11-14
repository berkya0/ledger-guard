package com.berkaykomur.accountservice.exception;

public class AccountAlreadyDeactivatedException extends BaseException {
    public AccountAlreadyDeactivatedException(String message) {
        super(message, "ACCOUNT_ALREADY_DEACTIVATED");
    }
}
