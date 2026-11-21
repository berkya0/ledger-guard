package com.berkaykomur.transferservice.exception;

public class TransferNotFoundException extends BaseException {
    public TransferNotFoundException(String message) {
        super(message, "TRANSFER_NOT_FOUND");
    }
}
