package com.berkaykomur.accountservice.repository;

import com.berkaykomur.accountservice.entity.ProcessedTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedTransferRepository extends JpaRepository<ProcessedTransfer, UUID> {
}