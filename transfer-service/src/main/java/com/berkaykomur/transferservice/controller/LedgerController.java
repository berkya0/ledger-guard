package com.berkaykomur.transferservice.controller;

import com.berkaykomur.transferservice.dto.AccountLedgerSummaryResponse;
import com.berkaykomur.transferservice.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ledgers")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<AccountLedgerSummaryResponse> getAccountLedger(@PathVariable UUID accountId) {
        return ResponseEntity.ok(ledgerService.getAccountLedgerSummary(accountId));
    }
}