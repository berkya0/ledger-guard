package com.berkaykomur.accountservice.service;

import com.berkaykomur.accountservice.dto.AccountRequest;
import com.berkaykomur.accountservice.dto.AccountResponse;
import com.berkaykomur.accountservice.dto.TransferRequest;
import com.berkaykomur.accountservice.entity.Account;
import com.berkaykomur.accountservice.entity.ProcessedTransfer;
import com.berkaykomur.accountservice.exception.AccountAlreadyDeactivatedException;
import com.berkaykomur.accountservice.exception.AccountNotFoundException;
import com.berkaykomur.accountservice.exception.InsufficientBalanceException;
import com.berkaykomur.accountservice.exception.SameAccountTransferException;
import com.berkaykomur.accountservice.mapper.AccountMapper;
import com.berkaykomur.accountservice.repository.AccountRepository;
import com.berkaykomur.accountservice.repository.ProcessedTransferRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ProcessedTransferRepository processedTransferRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccount_mapsSavesAndReturnsAccount() {
        AccountRequest request = new AccountRequest("Alice");
        Account account = account("Alice", BigDecimal.ZERO);
        Account savedAccount = account("Alice", BigDecimal.ZERO);
        AccountResponse response = response(savedAccount);

        when(accountMapper.toEntity(request)).thenReturn(account);
        when(accountRepository.save(account)).thenReturn(savedAccount);
        when(accountMapper.toResponse(savedAccount)).thenReturn(response);

        assertThat(accountService.createAccount(request)).isSameAs(response);

        verify(accountMapper).toEntity(request);
        verify(accountRepository).save(account);
        verify(accountMapper).toResponse(savedAccount);
    }

    @Test
    void getAccountById_returnsMappedAccountWhenFound() {
        UUID accountId = UUID.randomUUID();
        Account account = account(accountId, "Alice", BigDecimal.TEN);
        AccountResponse response = response(account);

        when(accountRepository.findByIdAndIsActiveTrue(accountId)).thenReturn(Optional.of(account));
        when(accountMapper.toResponse(account)).thenReturn(response);

        assertThat(accountService.getAccountById(accountId)).isSameAs(response);
        verify(accountRepository).findByIdAndIsActiveTrue(accountId);
        verify(accountMapper).toResponse(account);
    }

    @Test
    void getAccountById_throwsWhenAccountDoesNotExist() {
        UUID accountId = UUID.randomUUID();
        when(accountRepository.findByIdAndIsActiveTrue(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccountById(accountId))
                .isInstanceOf(AccountNotFoundException.class);

        verify(accountMapper, never()).toResponse(any(Account.class));
    }

    @Test
    void getAllAccounts_mapsEveryAccountAndPreservesPageMetadata() {
        PageRequest pageable = PageRequest.of(1, 2);
        Account first = account(UUID.randomUUID(), "Alice", BigDecimal.TEN);
        Account second = account(UUID.randomUUID(), "Bob", BigDecimal.ONE);
        AccountResponse firstResponse = response(first);
        AccountResponse secondResponse = response(second);
        Page<Account> accounts = new PageImpl<>(List.of(first, second), pageable, 5);

        when(accountRepository.findAllByIsActiveTrue(pageable)).thenReturn(accounts);
        when(accountMapper.toResponse(first)).thenReturn(firstResponse);
        when(accountMapper.toResponse(second)).thenReturn(secondResponse);

        Page<AccountResponse> result = accountService.getAllAccounts(pageable);

        assertThat(result.getContent()).containsExactly(firstResponse, secondResponse);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(5);
    }

    @Test
    void deleteAccount_deactivatesExistingAccount() {
        UUID accountId = UUID.randomUUID();
        Account account = account(accountId, "Alice", BigDecimal.ZERO);
        account.setActive(true);
        when(accountRepository.findByIdAndIsActiveTrue(accountId)).thenReturn(Optional.of(account));

        accountService.deleteAccount(accountId);

        assertThat(account.isActive()).isFalse();
        verify(accountRepository).save(account);
        verify(accountRepository, never()).delete(any(Account.class));
    }

    @Test
    void deleteAccount_throwsAndDoesNotDeleteWhenAccountDoesNotExist() {
        UUID accountId = UUID.randomUUID();
        when(accountRepository.findByIdAndIsActiveTrue(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.deleteAccount(accountId))
                .isInstanceOf(AccountNotFoundException.class);

        verify(accountRepository, never()).save(any(Account.class));
        verify(accountRepository, never()).delete(any(Account.class));
    }

    @Test
    void deleteAccount_throwsWhenAccountIsAlreadyDeactivated() {
        UUID accountId = UUID.randomUUID();
        Account account = account(accountId, "Alice", BigDecimal.ZERO);
        account.setActive(false);
        when(accountRepository.findByIdAndIsActiveTrue(accountId)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.deleteAccount(accountId))
                .isInstanceOf(AccountAlreadyDeactivatedException.class);

        assertThat(account.isActive()).isFalse();
        verify(accountRepository, never()).save(any(Account.class));
        verify(accountRepository, never()).delete(any(Account.class));
    }

    @Test
    void applyTransfer_debitsCreditsAndPersistsTransfer() {
        UUID transferId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("25.50");
        TransferRequest request = new TransferRequest(transferId, senderId, receiverId, amount);
        Account sender = account(senderId, "Alice", new BigDecimal("100.00"));
        Account receiver = account(receiverId, "Bob", new BigDecimal("10.00"));

        when(processedTransferRepository.existsById(transferId)).thenReturn(false);
        when(accountRepository.findByIdAndIsActiveTrue(senderId)).thenReturn(Optional.of(sender));
        when(accountRepository.findByIdAndIsActiveTrue(receiverId)).thenReturn(Optional.of(receiver));

        accountService.applyTransfer(request);

        assertThat(sender.getBalance()).isEqualByComparingTo("74.50");
        assertThat(receiver.getBalance()).isEqualByComparingTo("35.50");
        verify(accountRepository).save(sender);
        verify(accountRepository).save(receiver);
        ArgumentCaptor<ProcessedTransfer> transferCaptor = ArgumentCaptor.forClass(ProcessedTransfer.class);
        verify(processedTransferRepository).save(transferCaptor.capture());
        assertThat(transferCaptor.getValue().getTransferId()).isEqualTo(transferId);
        assertThat(transferCaptor.getValue().getProcessedAt()).isNotNull();
    }

    @Test
    void applyTransfer_duplicateTransferSkipsApplyingAgain() {
        UUID transferId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        TransferRequest request = new TransferRequest(transferId, senderId, receiverId, BigDecimal.TEN);

        when(processedTransferRepository.existsById(transferId)).thenReturn(true);

        accountService.applyTransfer(request);

        verify(accountRepository, never()).findByIdAndIsActiveTrue(senderId);
        verify(accountRepository, never()).findByIdAndIsActiveTrue(receiverId);

        verify(accountRepository, never()).save(any(Account.class));
        verify(processedTransferRepository, never()).save(any(ProcessedTransfer.class));
    }

    @Test
    void applyTransfer_duplicateTransferSkipsWhenAccountsDoNotExist() {
        UUID transferId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        TransferRequest request = new TransferRequest(
                transferId, senderId, UUID.randomUUID(), BigDecimal.TEN);
        when(processedTransferRepository.existsById(transferId)).thenReturn(true);

        accountService.applyTransfer(request);

        verify(accountMapper, never()).toResponse(any(Account.class));
        verify(accountRepository, never()).findByIdAndIsActiveTrue(any(UUID.class));
        verify(accountRepository, never()).save(any(Account.class));
        verify(processedTransferRepository, never()).save(any(ProcessedTransfer.class));

    }

    @Test
    void applyTransfer_rejectsTransferBetweenSameAccount() {
        UUID accountId = UUID.randomUUID();
        TransferRequest request = new TransferRequest(
                UUID.randomUUID(), accountId, accountId, BigDecimal.TEN);
        when(processedTransferRepository.existsById(request.transferId())).thenReturn(false);

        assertThatThrownBy(() -> accountService.applyTransfer(request))
                .isInstanceOf(SameAccountTransferException.class);

        verify(accountRepository, never()).findByIdAndIsActiveTrue(any(UUID.class));
        verify(accountRepository, never()).save(any(Account.class));
        verify(processedTransferRepository, never()).save(any(ProcessedTransfer.class));
    }

    @Test
    void applyTransfer_throwsWhenSenderDoesNotExist() {
        UUID senderId = UUID.randomUUID();
        TransferRequest request = new TransferRequest(
                UUID.randomUUID(), senderId, UUID.randomUUID(), BigDecimal.TEN);
        when(processedTransferRepository.existsById(request.transferId())).thenReturn(false);
        when(accountRepository.findByIdAndIsActiveTrue(senderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.applyTransfer(request))
                .isInstanceOf(AccountNotFoundException.class);

        verify(accountRepository, never()).save(any(Account.class));
        verify(processedTransferRepository, never()).save(any(ProcessedTransfer.class));
    }

    @Test
    void applyTransfer_throwsWhenReceiverDoesNotExist() {
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        Account sender = account(senderId, "Alice", BigDecimal.TEN);
        TransferRequest request = new TransferRequest(
                UUID.randomUUID(), senderId, receiverId, BigDecimal.ONE);
        when(processedTransferRepository.existsById(request.transferId())).thenReturn(false);
        when(accountRepository.findByIdAndIsActiveTrue(senderId)).thenReturn(Optional.of(sender));
        when(accountRepository.findByIdAndIsActiveTrue(receiverId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.applyTransfer(request))
                .isInstanceOf(AccountNotFoundException.class);

        verify(accountRepository, never()).save(any(Account.class));
        verify(processedTransferRepository, never()).save(any(ProcessedTransfer.class));
    }
    @Test
    void applyTransfer_throwsAndDoesNotSaveWhenSenderHasInsufficientBalance() {
        UUID transferId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        Account sender = account(senderId, "Alice", BigDecimal.ONE);
        Account receiver = account(receiverId, "Bob", BigDecimal.TEN);
        TransferRequest request = new TransferRequest(transferId, senderId, receiverId, BigDecimal.TEN);

        when(processedTransferRepository.existsById(transferId)).thenReturn(false);
        when(accountRepository.findByIdAndIsActiveTrue(senderId)).thenReturn(Optional.of(sender));
        when(accountRepository.findByIdAndIsActiveTrue(receiverId)).thenReturn(Optional.of(receiver));

        assertThatThrownBy(() -> accountService.applyTransfer(request))
                .isInstanceOf(InsufficientBalanceException.class);

        assertThat(receiver.getBalance()).isEqualByComparingTo(BigDecimal.TEN);
        verify(accountRepository, never()).save(any(Account.class));
        verify(processedTransferRepository, never()).save(any(ProcessedTransfer.class));
    }

    private static Account account(String ownerName, BigDecimal balance) {
        return account(UUID.randomUUID(), ownerName, balance);
    }

    private static Account account(UUID id, String ownerName, BigDecimal balance) {
        return Account.builder()
                .id(id)
                .ownerName(ownerName)
                .balance(balance)
                .build();
    }

    private static AccountResponse response(Account account) {
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        return new AccountResponse(
                account.getId(), account.getOwnerName(), account.getBalance(), createdAt, createdAt);
    }
}
