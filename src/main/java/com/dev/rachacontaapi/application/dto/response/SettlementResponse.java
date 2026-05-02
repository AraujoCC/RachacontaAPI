package com.dev.rachacontaapi.application.dto.response;

import com.dev.rachacontaapi.domain.enums.SettlementStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record SettlementResponse(
        UUID id,
        UUID payerId,
        String payerName,
        UUID receiverId,
        String receiverName,
        BigDecimal amount,
        SettlementStatus status
) {}
