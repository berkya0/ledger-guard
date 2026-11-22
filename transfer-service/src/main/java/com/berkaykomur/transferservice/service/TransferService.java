package com.berkaykomur.transferservice.service;

import com.berkaykomur.transferservice.dto.CreateTransferRequest;
import com.berkaykomur.transferservice.dto.TransferInitiatedEvent;
import com.berkaykomur.transferservice.dto.TransferResponse;
import com.berkaykomur.transferservice.dto.TransferResultEvent;
import com.berkaykomur.transferservice.entity.AccountLedger;
import com.berkaykomur.transferservice.entity.EntryType;
import com.berkaykomur.transferservice.entity.Transfer;
import com.berkaykomur.transferservice.entity.TransferStatus;
import com.berkaykomur.transferservice.exception.SameAccountTransferException;
import com.berkaykomur.transferservice.exception.TransferNotFoundException;
import com.berkaykomur.transferservice.repository.AccountLedgerRepository;
import com.berkaykomur.transferservice.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;
    private final ApplicationEventPublisher eventPublisher;

    // Sınıf bağımlılıklarına ekle:
    private final AccountLedgerRepository accountLedgerRepository;

    @Transactional
    public TransferResponse initiateTransfer(CreateTransferRequest request) {
        if (request.senderAccountId().equals(request.receiverAccountId())) {
            throw new SameAccountTransferException("Sender and receiver account IDs cannot be the same.");
        }

        Transfer transfer = Transfer.builder()
                .senderAccountId(request.senderAccountId())
                .receiverAccountId(request.receiverAccountId())
                .amount(request.amount())
                .status(TransferStatus.INITIATED)
                .build();

        Transfer savedTransfer = transferRepository.save(transfer);
        log.info("Transfer record saved with status INITIATED: transferId={}", savedTransfer.getId());

        recordTransferEntries(
                savedTransfer.getId(),
                savedTransfer.getSenderAccountId(),
                savedTransfer.getReceiverAccountId(),
                savedTransfer.getAmount()
        );

        TransferInitiatedEvent event = new TransferInitiatedEvent(
                savedTransfer.getId(),
                savedTransfer.getSenderAccountId(),
                savedTransfer.getReceiverAccountId(),
                savedTransfer.getAmount()
        );

        eventPublisher.publishEvent(event);

        return mapToResponse(savedTransfer);
    }

    @Transactional
    public void handleTransferResult(TransferResultEvent event) {
        log.info("Handling transfer result: transferId={}", event.transferId());
        Transfer transfer = transferRepository.findByIdAndIsActiveTrue(event.transferId())
                .orElseThrow(() -> new TransferNotFoundException("Transfer not found with ID: " + event.transferId()));

        if (transfer.getStatus() != TransferStatus.INITIATED) {
            log.warn("Transfer {} already processed with status {}. Skipping duplicate result event.",
                    transfer.getId(), transfer.getStatus());
            return;
        }

        if (event.success()) {
            transfer.setStatus(TransferStatus.COMPLETED);
            log.info("Transfer {} updated to COMPLETED", transfer.getId());
        } else {
            transfer.setStatus(TransferStatus.FAILED);
            transfer.setFailureReason(event.failureReason());

            recordTransferEntries(
                    transfer.getId(),
                    transfer.getReceiverAccountId(),
                    transfer.getSenderAccountId(),
                    transfer.getAmount()
            );
            log.warn("Transfer {} updated to FAILED and compensating ledger entries recorded. Reason: {}",
                    transfer.getId(), event.failureReason());
        }

        transferRepository.saveAndFlush(transfer);
    }

    private void recordTransferEntries(UUID transferId, UUID debitAccountId, UUID creditAccountId, BigDecimal amount) {
        AccountLedger debitEntry = AccountLedger.builder()
                .accountId(debitAccountId)
                .transferId(transferId)
                .amount(amount)
                .entryType(EntryType.DEBIT)
                .build();

        AccountLedger creditEntry = AccountLedger.builder()
                .accountId(creditAccountId)
                .transferId(transferId)
                .amount(amount)
                .entryType(EntryType.CREDIT)
                .build();

        accountLedgerRepository.saveAll(List.of(debitEntry, creditEntry));
    }

    @Transactional(readOnly = true)
    public TransferResponse getTransferById(UUID transferId) {
        Transfer transfer = transferRepository.findByIdAndIsActiveTrue(transferId)
                .orElseThrow(() -> new TransferNotFoundException("Transfer not found with ID: " + transferId));

        return mapToResponse(transfer);
    }

    private TransferResponse mapToResponse(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getSenderAccountId(),
                transfer.getReceiverAccountId(),
                transfer.getAmount(),
                transfer.getStatus(),
                transfer.getFailureReason(),
                transfer.getCreatedAt()
        );
    }
}