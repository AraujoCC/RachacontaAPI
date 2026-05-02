package com.dev.rachacontaapi.infrastructure.repository;

import com.dev.rachacontaapi.domain.model.Settlement;
import com.dev.rachacontaapi.domain.enums.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface SettlementRepository extends JpaRepository<Settlement, UUID> {

    List<Settlement> findByGroupId(UUID groupId);

    List<Settlement> findByGroupIdAndStatus(UUID groupId, SettlementStatus status);

    // Quanto cada usuário pagou no total dentro de um grupo
    // (usado pelo algoritmo de liquidação)
    @Query("""
        SELECT e.paidBy.id, SUM(e.amount)
        FROM Expense e
        WHERE e.group.id = :groupId
        GROUP BY e.paidBy.id
    """)
    List<Object[]> findTotalPaidPerUserInGroup(@Param("groupId") UUID groupId);
}
