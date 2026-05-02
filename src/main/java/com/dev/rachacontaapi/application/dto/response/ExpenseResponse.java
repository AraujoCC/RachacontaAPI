package com.dev.rachacontaapi.application.dto.response;

import com.dev.rachacontaapi.domain.enums.SplitType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ExpenseResponse(
        UUID id,
        String description,
        BigDecimal amount,
        SplitType splitType,
        UUID paidById,
        String paidByName,
        LocalDateTime createdAt
) {}
