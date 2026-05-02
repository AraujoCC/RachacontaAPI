package com.dev.rachacontaapi.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record GroupResponse(
        UUID id,
        String name,
        String description,
        LocalDateTime createdAt
) {}