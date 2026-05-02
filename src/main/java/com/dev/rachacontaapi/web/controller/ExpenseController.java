package com.dev.rachacontaapi.web.controller;

import com.dev.rachacontaapi.application.dto.request.CreateExpenseRequest;
import com.dev.rachacontaapi.application.dto.response.ExpenseResponse;
import com.dev.rachacontaapi.application.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{groupId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseResponse> create(@PathVariable UUID groupId,
                                                  @Valid @RequestBody CreateExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(expenseService.create(groupId, request));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> listByGroup(@PathVariable UUID groupId) {
        return ResponseEntity.ok(expenseService.listByGroup(groupId));
    }
}
