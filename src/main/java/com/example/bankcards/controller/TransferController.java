package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.security.JwtUtil;
import com.example.bankcards.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Transfers", description = "Money transfer endpoints")
public class TransferController {

    private final TransferService transferService;
    private final JwtUtil jwtUtil;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Transfer between own cards")
    public ResponseEntity<Transaction> transferMoney(
            @Valid @RequestBody TransferDto transferDto,
            Authentication authentication) {
        Long userId = jwtUtil.extractUserId(authentication);
        Transaction transaction = transferService.transferBetweenOwnCards(transferDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @GetMapping("/my-transactions")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get my transactions")
    public ResponseEntity<Page<Transaction>> getMyTransactions(
            Authentication authentication,
            @PageableDefault(size = 10) Pageable pageable) {
        Long userId = jwtUtil.extractUserId(authentication);
        Page<Transaction> transactions = transferService.getUserTransactions(userId, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/card/{cardId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get card transactions")
    public ResponseEntity<Page<Transaction>> getCardTransactions(
            @PathVariable Long cardId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<Transaction> transactions = transferService.getCardTransactions(cardId, pageable);
        return ResponseEntity.ok(transactions);
    }
}
