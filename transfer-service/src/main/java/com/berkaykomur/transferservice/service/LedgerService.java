package com.berkaykomur.transferservice.service;

import com.berkaykomur.transferservice.dto.AccountLedgerSummaryResponse;
import com.berkaykomur.transferservice.dto.LedgerEntryResponse;
import com.berkaykomur.transferservice.repository.AccountLedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final AccountLedgerRepository accountLedgerRepository;

    @Transactional(readOnly = true)
    public AccountLedgerSummaryResponse getAccountLedgerSummary(UUID accountId) {
        BigDecimal netBalance = accountLedgerRepository.calculateNetBalanceByAccountId(accountId);
        List<LedgerEntryResponse> entries = accountLedgerRepository.findAllByAccountIdOrderByCreatedAtAsc(accountId)
                .stream()
                .map(entry -> new LedgerEntryResponse(
                        entry.getId(),
                        entry.getAccountId(),
                        entry.getTransferId(),
                        entry.getAmount(),
                        entry.getEntryType(),
                        entry.getCreatedAt()
                ))
                .toList();

        return new AccountLedgerSummaryResponse(accountId, netBalance, entries.size(), entries);
    }
}