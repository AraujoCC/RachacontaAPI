package com.dev.rachacontaapi.application.service;

import com.dev.rachacontaapi.application.dto.request.CreateGroupRequest;
import com.dev.rachacontaapi.application.dto.response.GroupMemberResponse;
import com.dev.rachacontaapi.application.dto.response.GroupResponse;
import com.dev.rachacontaapi.domain.enums.GroupRole;
import com.dev.rachacontaapi.domain.model.Group;
import com.dev.rachacontaapi.domain.model.GroupMember;
import com.dev.rachacontaapi.domain.model.User;
import com.dev.rachacontaapi.infrastructure.repository.GroupMemberRepository;
import com.dev.rachacontaapi.infrastructure.repository.GroupRepository;
import com.dev.rachacontaapi.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public GroupResponse create(CreateGroupRequest request) {
        User currentUser = getCurrentUser();

        Group group = Group.builder()
                .name(request.name())
                .description(request.description())
                .build();

        groupRepository.save(group);

        // Criador entra como ADMIN automaticamente
        GroupMember member = GroupMember.builder()
                .group(group)
                .user(currentUser)
                .role(GroupRole.ADMIN)
                .build();

        groupMemberRepository.save(member);

        return toResponse(group);
    }

    public List<GroupResponse> listMyGroups() {
        User currentUser = getCurrentUser();
        return groupMemberRepository.findByGroupId(currentUser.getId())
                .stream()
                .map(gm -> toResponse(gm.getGroup()))
                .toList();
    }

    public GroupResponse findById(UUID groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo não encontrado"));
        return toResponse(group);
    }

    public List<GroupMemberResponse> listMembers(UUID groupId) {
        return groupMemberRepository.findByGroupId(groupId)
                .stream()
                .map(gm -> new GroupMemberResponse(
                        gm.getUser().getId(),
                        gm.getUser().getName(),
                        gm.getUser().getEmail(),
                        gm.getRole()
                ))
                .toList();
    }

    @Transactional
    public void addMember(UUID groupId, UUID userId) {
        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new IllegalArgumentException("Usuário já é membro do grupo");
        }

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo não encontrado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        GroupMember member = GroupMember.builder()
                .group(group)
                .user(user)
                .role(GroupRole.MEMBER)
                .build();

        groupMemberRepository.save(member);
    }

    // Helpers
    private GroupResponse toResponse(Group group) {
        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getCreatedAt()
        );
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
    }
}