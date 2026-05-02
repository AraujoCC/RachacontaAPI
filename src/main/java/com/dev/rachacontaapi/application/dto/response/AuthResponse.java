package com.dev.rachacontaapi.application.dto.response;

import java.util.UUID;

public record AuthResponse(
        UUID id,
        String token,
        String name,
        String email
) {}
