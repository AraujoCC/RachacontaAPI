package com.dev.rachacontaapi.infrastructure.repository;

import com.dev.rachacontaapi.domain.model.ExpenseSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, UUID> {

    List<ExpenseSplit> findByExpenseId(UUID expenseId);

    // Quanto cada usuário deve no total dentro de um grupo
    // (usado pelo algoritmo de liquidação)
    @Query("""
        SELECT es.user.id, SUM(es.amountOwed)
        FROM ExpenseSplit es
        WHERE es.expense.group.id = :groupId
        GROUP BY es.user.id
    """)
    List<Object[]> findTotalOwedPerUserInGroup(@Param("groupId") UUID groupId);
}
