package com.dev.rachacontaapi.application.dto.response;

public record AuthResponse(
        String token,
        String name,
        String email
) {}
