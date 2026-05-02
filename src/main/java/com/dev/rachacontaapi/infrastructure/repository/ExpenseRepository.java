package com.dev.rachacontaapi.infrastructure.repository;

import com.dev.rachacontaapi.domain.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    List<Expense> findByGroupId(UUID groupId);

    @Query("""
        SELECT e.paidBy.id, SUM(e.amount)
        FROM Expense e
        WHERE e.group.id = :groupId
        GROUP BY e.paidBy.id
    """)
    List<Object[]> findTotalPaidPerUserInGroup(@Param("groupId") UUID groupId);
}
