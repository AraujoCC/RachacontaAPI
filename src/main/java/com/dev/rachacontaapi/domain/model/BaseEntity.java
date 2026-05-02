package com.dev.rachacontaapi.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @CreationTimestamp  // ← Hibernate preenche automaticamente na inserção
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}