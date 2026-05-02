package com.dev.rachacontaapi.web.controller;

import com.dev.rachacontaapi.application.dto.response.SettlementResponse;
import com.dev.rachacontaapi.application.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{groupId}/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping("/calculate")
    public ResponseEntity<List<SettlementResponse>> calculate(@PathVariable UUID groupId) {
        return ResponseEntity.ok(settlementService.calculate(groupId));
    }

    @GetMapping
    public ResponseEntity<List<SettlementResponse>> listByGroup(@PathVariable UUID groupId) {
        return ResponseEntity.ok(settlementService.listByGroup(groupId));
    }

    @PatchMapping("/{settlementId}/confirm")
    public ResponseEntity<SettlementResponse> confirm(@PathVariable UUID groupId,
                                                      @PathVariable UUID settlementId) {
        return ResponseEntity.ok(settlementService.confirm(settlementId));
    }
}
