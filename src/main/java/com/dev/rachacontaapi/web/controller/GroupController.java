package com.dev.rachacontaapi.web.controller;

import com.dev.rachacontaapi.application.dto.request.CreateGroupRequest;
import com.dev.rachacontaapi.application.dto.response.GroupMemberResponse;
import com.dev.rachacontaapi.application.dto.response.GroupResponse;
import com.dev.rachacontaapi.application.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupResponse> create(@Valid @RequestBody CreateGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> listMyGroups() {
        return ResponseEntity.ok(groupService.listMyGroups());
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> findById(@PathVariable UUID groupId) {
        return ResponseEntity.ok(groupService.findById(groupId));
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberResponse>> listMembers(@PathVariable UUID groupId) {
        return ResponseEntity.ok(groupService.listMembers(groupId));
    }

    @PostMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Void> addMember(@PathVariable UUID groupId,
                                          @PathVariable UUID userId) {
        groupService.addMember(groupId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}