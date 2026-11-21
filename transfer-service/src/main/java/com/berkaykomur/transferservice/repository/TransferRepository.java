package com.berkaykomur.transferservice.repository;

import com.berkaykomur.transferservice.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, UUID> {

    Optional<Transfer> findByIdAndIsActiveTrue(UUID uuid);
}
