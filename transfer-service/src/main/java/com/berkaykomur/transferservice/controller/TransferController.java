package com.berkaykomur.transferservice.controller;

import com.berkaykomur.transferservice.dto.CreateTransferRequest;
import com.berkaykomur.transferservice.dto.TransferResponse;
import com.berkaykomur.transferservice.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<TransferResponse> initiateTransfer(@Valid @RequestBody CreateTransferRequest request) {
        TransferResponse response = transferService.initiateTransfer(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response); //asenkronlar için yani işlem tamamen bitmediyse Accepted
    }

    @GetMapping("/{transferId}")
    public ResponseEntity<TransferResponse> getTransferById(@PathVariable UUID transferId) {
        return ResponseEntity.ok(transferService.getTransferById(transferId));
    }
}