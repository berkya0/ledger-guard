package com.berkaykomur.accountservice.service;

import com.berkaykomur.accountservice.dto.AccountRequest;
import com.berkaykomur.accountservice.dto.AccountResponse;
import com.berkaykomur.accountservice.dto.TransferRequest;
import com.berkaykomur.accountservice.entity.Account;
import com.berkaykomur.accountservice.entity.ProcessedTransfer;
import com.berkaykomur.accountservice.exception.AccountAlreadyDeactivatedException;
import com.berkaykomur.accountservice.exception.AccountNotFoundException;
import com.berkaykomur.accountservice.exception.SameAccountTransferException;
import com.berkaykomur.accountservice.mapper.AccountMapper;
import com.berkaykomur.accountservice.repository.AccountRepository;
import com.berkaykomur.accountservice.repository.ProcessedTransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountMapper accountMapper;
    private final AccountRepository accountRepository;
    private final ProcessedTransferRepository processedTransferRepository;

    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        log.info("Creating account for owner: {}", request.ownerName());
        Account newAccount =accountMapper.toEntity(request);
        Account savedAccount = accountRepository.save(newAccount);
        return accountMapper.toResponse(savedAccount);
    }

    public AccountResponse getAccountById(UUID accountId) {
        log.info("Fetching account with ID: {}", accountId);
        return accountRepository.findByIdAndIsActiveTrue(accountId)
                .map(accountMapper::toResponse)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + accountId));
    }

    @Transactional(readOnly = true)
    public Page<AccountResponse> getAllAccounts(Pageable pageable) {
        log.info("Fetching accounts - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return accountRepository.findAllByIsActiveTrue(pageable)
                .map(accountMapper::toResponse);
    }

    @Transactional
    public void deleteAccount(UUID accountId) {
        log.info("Deactivating account with ID: {}", accountId);

        Account account = accountRepository.findByIdAndIsActiveTrue(accountId)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found with ID: " + accountId));

        if (!account.isActive()) {
            throw new AccountAlreadyDeactivatedException(
                    "Account is already deactivated: " + accountId);
        }

        account.setActive(false);
        accountRepository.save(account);
    }


    @Transactional
    public AccountResponse applyTransfer(TransferRequest request) {
        if (processedTransferRepository.existsById(request.transferId())) { //tekrar eden kayıtları önleme stratejisi
            log.warn("Transfer with ID {} has already been processed. Skipping duplicate event.", request.transferId());
            Account existingSender = accountRepository.findById(request.senderAccountId())
                    .orElseThrow(() -> new AccountNotFoundException("Sender account not found with ID: " + request.senderAccountId()));
            return accountMapper.toResponse(existingSender);
        }

        if (request.senderAccountId().equals(request.receiverAccountId())) {
            throw new SameAccountTransferException("Sender and receiver account IDs cannot be the same.");
        }

        log.info("Applying transfer {} from {} to {} of amount {}",
                request.transferId(), request.senderAccountId(), request.receiverAccountId(), request.amount());

        Account senderAccount = accountRepository.findByIdAndIsActiveTrue(request.senderAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Sender account not found with ID: " + request.senderAccountId()));
        Account receiverAccount = accountRepository.findByIdAndIsActiveTrue(request.receiverAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Receiver account not found with ID: " + request.receiverAccountId()));

        senderAccount.debit(request.amount());
        receiverAccount.credit(request.amount());

        accountRepository.save(senderAccount);
        accountRepository.save(receiverAccount);

        processedTransferRepository.save(new ProcessedTransfer(request.transferId(), Instant.now()));

        return accountMapper.toResponse(senderAccount);
    }


}
