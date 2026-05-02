package com.dev.rachacontaapi.application.dto.request;

public record AuthResponse(
        String token,
        String name,
        String email
) {}
