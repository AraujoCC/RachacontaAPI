package com.dev.rachacontaapi.infrastructure.repository;

import com.dev.rachacontaapi.domain.model.GroupMember;
import com.dev.rachacontaapi.domain.enums.GroupRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {

    List<GroupMember> findByGroupId(UUID groupId);

    Optional<GroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId);

    List<GroupMember> findByUserId(UUID userId);

    boolean existsByGroupIdAndUserId(UUID groupId, UUID userId);
}
