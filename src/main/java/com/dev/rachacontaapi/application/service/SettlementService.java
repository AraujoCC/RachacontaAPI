package com.dev.rachacontaapi.application.service;

import com.dev.rachacontaapi.infrastructure.repository.ExpenseRepository;
import com.dev.rachacontaapi.infrastructure.repository.ExpenseSplitRepository;

import com.dev.rachacontaapi.application.dto.response.SettlementResponse;
import com.dev.rachacontaapi.domain.enums.SettlementStatus;
import com.dev.rachacontaapi.domain.model.Group;
import com.dev.rachacontaapi.domain.model.Settlement;
import com.dev.rachacontaapi.domain.model.User;
import com.dev.rachacontaapi.infrastructure.repository.GroupRepository;
import com.dev.rachacontaapi.infrastructure.repository.SettlementRepository;
import com.dev.rachacontaapi.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final ExpenseRepository expenseRepository; // ← adiciona
    private final ExpenseSplitRepository expenseSplitRepository; // ← adiciona
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    // Calcula e persiste as liquidações com mínimo de transferências
    @Transactional
    public List<SettlementResponse> calculate(UUID groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo não encontrado"));

        // limpa settlements pendentes antigos
        settlementRepository.deleteByGroupIdAndStatus(groupId, SettlementStatus.PENDING);

        // 1. Busca total pago e total devido por usuário
        Map<UUID, BigDecimal> totalPaid = toMap(
                expenseRepository.findTotalPaidPerUserInGroup(groupId)); // ← expenseRepository

        Map<UUID, BigDecimal> totalOwed = toMap(
                expenseSplitRepository.findTotalOwedPerUserInGroup(groupId)); // ← expenseSplitRepository

        // 2. Calcula saldo líquido: positivo = credor, negativo = devedor
        Map<UUID, BigDecimal> balance = new HashMap<>();
        Set<UUID> allUsers = new HashSet<>();
        allUsers.addAll(totalPaid.keySet());
        allUsers.addAll(totalOwed.keySet());

        for (UUID userId : allUsers) {
            BigDecimal paid = totalPaid.getOrDefault(userId, BigDecimal.ZERO);
            BigDecimal owed = totalOwed.getOrDefault(userId, BigDecimal.ZERO);
            balance.put(userId, paid.subtract(owed));
        }

        // 3. Separa credores (saldo > 0) e devedores (saldo < 0)
        // Usa PriorityQueue para sempre pegar o maior valor primeiro (greedy)
        PriorityQueue<Map.Entry<UUID, BigDecimal>> creditors = new PriorityQueue<>(
                (a, b) -> b.getValue().compareTo(a.getValue()));

        PriorityQueue<Map.Entry<UUID, BigDecimal>> debtors = new PriorityQueue<>(
                (a, b) -> a.getValue().compareTo(b.getValue()));

        for (Map.Entry<UUID, BigDecimal> entry : balance.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                creditors.offer(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()));
            } else if (entry.getValue().compareTo(BigDecimal.ZERO) < 0) {
                debtors.offer(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()));
            }
        }

        // 4. Algoritmo greedy: maior devedor paga para maior credor
        List<Settlement> settlements = new ArrayList<>();

        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            Map.Entry<UUID, BigDecimal> creditor = creditors.poll();
            Map.Entry<UUID, BigDecimal> debtor = debtors.poll();

            BigDecimal creditAmount = creditor.getValue();
            BigDecimal debtAmount = debtor.getValue().abs();

            // Transfere o menor valor absoluto entre os dois
            BigDecimal transferAmount = creditAmount.min(debtAmount)
                    .setScale(2, RoundingMode.HALF_UP);

            User payer = userRepository.getReferenceById(debtor.getKey());
            User receiver = userRepository.getReferenceById(creditor.getKey());

            Settlement settlement = Settlement.builder()
                    .group(group)
                    .payer(payer)
                    .receiver(receiver)
                    .amount(transferAmount)
                    .status(SettlementStatus.PENDING)
                    .build();

            settlements.add(settlement);

            // Reinsere quem ainda tem saldo restante
            BigDecimal remainingCredit = creditAmount.subtract(transferAmount);
            BigDecimal remainingDebt = debtAmount.subtract(transferAmount);

            if (remainingCredit.compareTo(BigDecimal.ZERO) > 0) {
                creditors.offer(new AbstractMap.SimpleEntry<>(creditor.getKey(), remainingCredit));
            }
            if (remainingDebt.compareTo(BigDecimal.ZERO) > 0) {
                debtors.offer(new AbstractMap.SimpleEntry<>(debtor.getKey(), remainingDebt.negate()));
            }
        }

        settlementRepository.saveAll(settlements);
        return settlements.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SettlementResponse> listByGroup(UUID groupId) {
        return settlementRepository.findByGroupId(groupId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SettlementResponse confirm(UUID settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("Liquidação não encontrada"));

        settlement.setStatus(SettlementStatus.CONFIRMED);
        settlementRepository.save(settlement);

        return toResponse(settlement);
    }

    // Converte List<Object[]> do JPQL para Map<UUID, BigDecimal>
    private Map<UUID, BigDecimal> toMap(List<Object[]> rows) {
        Map<UUID, BigDecimal> map = new HashMap<>();
        for (Object[] row : rows) {
            UUID userId = (UUID) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            map.put(userId, amount);
        }
        return map;
    }

    private SettlementResponse toResponse(Settlement s) {
        return new SettlementResponse(
                s.getId(),
                s.getPayer().getId(),
                s.getPayer().getName(),
                s.getReceiver().getId(),
                s.getReceiver().getName(),
                s.getAmount(),
                s.getStatus());
    }
}