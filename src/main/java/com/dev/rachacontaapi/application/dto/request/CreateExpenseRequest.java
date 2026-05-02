package com.dev.rachacontaapi.application.dto.request;

import com.dev.rachacontaapi.domain.enums.SplitType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateExpenseRequest(

        @NotBlank(message = "Descrição é obrigatória")
        @Size(max = 150)
        String description,

        @NotNull(message = "Valor é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
        BigDecimal amount,

        @NotNull(message = "Tipo de divisão é obrigatório")
        SplitType splitType,

        // Obrigatório apenas quando splitType = CUSTOM
        List<ExpenseSplitRequest> splits
) {}
