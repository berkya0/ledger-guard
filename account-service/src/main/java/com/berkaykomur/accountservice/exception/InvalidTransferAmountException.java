package com.berkaykomur.accountservice.exception;

public class InvalidTransferAmountException extends BaseException {
    public InvalidTransferAmountException(String message) {
        super(message,"INVALID_TRANSFER_AMOUNT");
    }
}
