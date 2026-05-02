package com.dev.rachacontaapi.application.dto.response;

import com.dev.rachacontaapi.domain.enums.GroupRole;
import java.util.UUID;

public record GroupMemberResponse(
        UUID userId,
        String name,
        String email,
        GroupRole role
) {}