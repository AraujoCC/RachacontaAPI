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
import com.dev.rachacontaapi.web.exception.BusinessException;
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

    @Transactional(readOnly = true)
    public List<GroupResponse> listMyGroups() {
        User currentUser = getCurrentUser();
        return groupMemberRepository.findByUserId(currentUser.getId())
                .stream()
                .map(gm -> toResponse(gm.getGroup()))
                .toList();
    }

    @Transactional(readOnly = true)
    public GroupResponse findById(UUID groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo não encontrado"));
        return toResponse(group);
    }

    @Transactional(readOnly = true)
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
        User currentUser = getCurrentUser();
        GroupMember currentMember = groupMemberRepository.findByGroupIdAndUserId(groupId, currentUser.getId()).orElseThrow(() ->
        new BusinessException("Usuário não pertence ao grupo"));

        if (currentMember.getRole() != GroupRole.ADMIN) {
            throw new BusinessException("Apenas administradores podem adicionar membros");
        }

        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new BusinessException("Usuário já é membro do grupo");
        }

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException("Grupo não encontrado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

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