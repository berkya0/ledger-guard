package com.berkaykomur.transferservice.controller;

import com.berkaykomur.transferservice.dto.AccountLedgerSummaryResponse;
import com.berkaykomur.transferservice.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ledgers")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<AccountLedgerSummaryResponse> getAccountLedger(@PathVariable UUID accountId,
                                                                         @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ledgerService.getAccountLedgerSummary(accountId, pageable));
    }
}