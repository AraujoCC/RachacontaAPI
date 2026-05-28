package com.dev.rachacontaapi.application.service;

import com.dev.rachacontaapi.application.dto.request.CreateExpenseRequest;
import com.dev.rachacontaapi.application.dto.response.ExpenseResponse;
import com.dev.rachacontaapi.domain.enums.SplitType;
import com.dev.rachacontaapi.domain.model.Expense;
import com.dev.rachacontaapi.domain.model.ExpenseSplit;
import com.dev.rachacontaapi.domain.model.Group;
import com.dev.rachacontaapi.domain.model.User;
import com.dev.rachacontaapi.infrastructure.repository.*;
import com.dev.rachacontaapi.web.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dev.rachacontaapi.infrastructure.security.AuthenticatedUserResolver;
import com.dev.rachacontaapi.application.dto.request.ExpenseSplitRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    @Transactional
    public ExpenseResponse create(UUID groupId, CreateExpenseRequest request) {
        User currentUser = authenticatedUserResolver.getCurrentUser();

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException("Grupo não encontrado"));

        Expense expense = Expense.builder()
                .group(group)
                .paidBy(currentUser)
                .description(request.description())
                .amount(request.amount())
                .splitType(request.splitType())
                .build();

        expenseRepository.save(expense);

        if (request.splitType() == SplitType.EQUAL) {
            createEqualSplits(expense, groupId);
        } else {
            createCustomSplits(expense, request);
        }

        return toResponse(expense);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> listByGroup(UUID groupId) {
        return expenseRepository.findByGroupId(groupId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Divide igualmente entre todos os membros do grupo
    private void createEqualSplits(Expense expense, UUID groupId) {
        List<User> members = groupMemberRepository.findByGroupId(groupId)
                .stream()
                .map(gm -> gm.getUser())
                .toList();

        BigDecimal share = expense.getAmount()
                .divide(BigDecimal.valueOf(members.size()), 2, RoundingMode.HALF_UP);

        members.forEach(member -> {
            ExpenseSplit split = ExpenseSplit.builder()
                    .expense(expense)
                    .user(member)
                    .amountOwed(share)
                    .build();
            expenseSplitRepository.save(split);
        });
    }

    // Usa os valores customizados enviados na requisição
    private void createCustomSplits(Expense expense, CreateExpenseRequest request) {
        if (request.splits() == null || request.splits().isEmpty()) {
            throw new BusinessException("Splits customizados são obrigatórios para CUSTOM");
        }

        BigDecimal totalSplits = request.splits().stream()
                .map(ExpenseSplitRequest::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalSplits.compareTo(expense.getAmount()) != 0) {
            throw new BusinessException("A soma das divisões não bate com o valor total da despesa");
        }

        request.splits().forEach(splitRequest -> {
            User user = userRepository.findById(splitRequest.userId())
                    .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

            ExpenseSplit split = ExpenseSplit.builder()
                    .expense(expense)
                    .user(user)
                    .amountOwed(splitRequest.amount())
                    .build();

            expenseSplitRepository.save(split);
        });
    }

    private ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getSplitType(),
                expense.getPaidBy().getId(),
                expense.getPaidBy().getName(),
                expense.getCreatedAt()
        );
    }
}