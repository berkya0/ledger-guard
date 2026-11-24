package com.berkaykomur.transferservice.repository;

import com.berkaykomur.transferservice.entity.AccountLedger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface AccountLedgerRepository extends JpaRepository<AccountLedger, UUID> {

    Page<AccountLedger> findAllByAccountId(UUID accountId, Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(
                CASE WHEN l.entryType = 'CREDIT' THEN l.amount ELSE -l.amount END
            ), 0)
            FROM AccountLedger l
            WHERE l.accountId = :accountId
            """)
    BigDecimal calculateNetBalanceByAccountId(@Param("accountId") UUID accountId);

    boolean existsByAccountIdAndTransferIdIsNull(UUID accountId);
}