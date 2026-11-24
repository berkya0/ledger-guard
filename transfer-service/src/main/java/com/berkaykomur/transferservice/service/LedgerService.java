package com.berkaykomur.transferservice.service;

import com.berkaykomur.transferservice.dto.AccountCreatedEvent;
import com.berkaykomur.transferservice.dto.AccountLedgerSummaryResponse;
import com.berkaykomur.transferservice.dto.LedgerEntryResponse;
import com.berkaykomur.transferservice.entity.AccountLedger;
import com.berkaykomur.transferservice.entity.EntryType;
import com.berkaykomur.transferservice.repository.AccountLedgerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerService {

    private final AccountLedgerRepository accountLedgerRepository;

    @Transactional
    public void recordInitialBalance(AccountCreatedEvent event) {
        if (event.initialBalance() == null || event.initialBalance().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        log.info("Recording initial balance of {} for accountId={}", event.initialBalance(), event.accountId());
        if (accountLedgerRepository.existsByAccountIdAndTransferIdIsNull(event.accountId())) {
            log.warn("Initial balance already recorded for accountId={}. Skipping duplicate event.", event.accountId());
            return;
        }
        AccountLedger initialEntry = AccountLedger.builder()
                .accountId(event.accountId())
                .transferId(null)
                .amount(event.initialBalance())
                .entryType(EntryType.CREDIT)
                .build();

        accountLedgerRepository.save(initialEntry);
        log.info("Recorded initial balance of {} for accountId={}", event.initialBalance(), event.accountId());
    }

    @Transactional(readOnly = true)
    public AccountLedgerSummaryResponse getAccountLedgerSummary(UUID accountId, Pageable pageable) {
        BigDecimal netBalance = accountLedgerRepository.calculateNetBalanceByAccountId(accountId);
        Page<LedgerEntryResponse> entryPage = accountLedgerRepository.findAllByAccountId(accountId, pageable)
                .map(entry -> new LedgerEntryResponse(
                        entry.getId(),
                        entry.getAccountId(),
                        entry.getTransferId(),
                        entry.getAmount(),
                        entry.getEntryType(),
                        entry.getCreatedAt()
                ));

        return new AccountLedgerSummaryResponse(accountId, netBalance,entryPage.getContent().size() ,new PagedModel<>(entryPage));
    }
}