package com.dev.rachacontaapi.infrastructure.repository;

import com.dev.rachacontaapi.domain.model.Settlement;
import com.dev.rachacontaapi.domain.enums.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SettlementRepository extends JpaRepository<Settlement, UUID> {

    List<Settlement> findByGroupId(UUID groupId);

    List<Settlement> findByGroupIdAndStatus(UUID groupId, SettlementStatus status);
}
