package com.dev.rachacontaapi.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGroupRequest(

        @NotBlank(message = "Nome do grupo é obrigatório")
        @Size(max = 100)
        String name,

        @Size(max = 255)
        String description
) {}
