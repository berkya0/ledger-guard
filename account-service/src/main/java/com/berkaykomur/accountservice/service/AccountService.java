package com.berkaykomur.accountservice.service;

import com.berkaykomur.accountservice.dto.AccountRequest;
import com.berkaykomur.accountservice.dto.AccountResponse;
import com.berkaykomur.accountservice.dto.TransferRequest;
import com.berkaykomur.accountservice.entity.Account;
import com.berkaykomur.accountservice.exception.AccountNotFoundException;
import com.berkaykomur.accountservice.mapper.AccountMapper;
import com.berkaykomur.accountservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountMapper accountMapper;
    private final AccountRepository accountRepository;

    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        log.info("Creating account for owner: {}", request.ownerName());
        Account newAccount =accountMapper.toEntity(request);
        Account savedAccount = accountRepository.save(newAccount);
        return accountMapper.toResponse(savedAccount);
    }

    public AccountResponse getAccountById(UUID accountId) {
        log.info("Fetching account with ID: {}", accountId);
        return accountRepository.findById(accountId)
                .map(accountMapper::toResponse)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + accountId));
    }

    @Transactional(readOnly = true)
    public Page<AccountResponse> getAllAccounts(Pageable pageable) {
        log.info("Fetching accounts - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return accountRepository.findAll(pageable)
                .map(accountMapper::toResponse);
    }

    public void deleteAccount(UUID accountId) {
        log.info("Deleting account with ID: {}", accountId);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + accountId));
        accountRepository.delete(account);
    }


    @Transactional
    public AccountResponse applyTransfer(TransferRequest request) {
        log.info("Applying transfer from {} to {} of amount {}", request.senderAccountId(), request.receiverAccountId(), request.amount());
        Account senderAccount = accountRepository.findById(request.senderAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Sender account not found with ID: " + request.senderAccountId()));
        Account receiverAccount = accountRepository.findById(request.receiverAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Receiver account not found with ID: " + request.receiverAccountId()));

        senderAccount.debit(request.amount());
        receiverAccount.credit(request.amount());

        accountRepository.save(senderAccount);
        accountRepository.save(receiverAccount);

        return accountMapper.toResponse(senderAccount);
    }

}
