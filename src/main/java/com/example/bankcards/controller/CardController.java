package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreateDto;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.security.JwtUtil;
import com.example.bankcards.service.CardService;
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

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Cards", description = "Card management endpoints")
public class CardController {

    private final CardService cardService;
    private final JwtUtil jwtUtil;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new card (Admin only)")
    public ResponseEntity<CardDto> createCard(@Valid @RequestBody CardCreateDto createDto) {
        CardDto card = cardService.createCard(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get card by ID")
    public ResponseEntity<CardDto> getCard(@PathVariable Long id) {
        CardDto card = cardService.getCardById(id);
        return ResponseEntity.ok(card);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get user cards")
    public ResponseEntity<Page<CardDto>> getUserCards(
            @PathVariable Long userId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<CardDto> cards = cardService.getUserCards(userId, pageable);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/my-cards")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get my cards")
    public ResponseEntity<Page<CardDto>> getMyCards(
            Authentication authentication,
            @PageableDefault(size = 10) Pageable pageable) {
        Long userId = jwtUtil.extractUserId(authentication);
        Page<CardDto> cards = cardService.getUserCards(userId, pageable);
        return ResponseEntity.ok(cards);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all cards (Admin only)", description = "Admin only: Get all cards in the system")
    public ResponseEntity<Page<CardDto>> getAllCards(@PageableDefault(size = 10) Pageable pageable) {
        Page<CardDto> cards = cardService.getAllCards(pageable);
        return ResponseEntity.ok(cards);
    }

    @PutMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Block card (Admin only)", description = "Admin only: Block a card")
    public ResponseEntity<CardDto> blockCard(@PathVariable Long id) {
        CardDto card = cardService.blockCard(id);
        return ResponseEntity.ok(card);
    }

    @PutMapping("/{id}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Unblock card (Admin only)", description = "Admin only: Unblock a card")
    public ResponseEntity<CardDto> unblockCard(@PathVariable Long id) {
        CardDto card = cardService.unblockCard(id);
        return ResponseEntity.ok(card);
    }

    @PostMapping("/{id}/request-block")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Request card block (from user)")
    public ResponseEntity<CardDto> requestBlockCard(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = jwtUtil.extractUserId(authentication);
        CardDto card = cardService.requestCardBlock(id, userId);
        return ResponseEntity.ok(card);
    }

    @GetMapping("/{id}/balance")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get card balance")
    public ResponseEntity<BigDecimal> getCardBalance(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = jwtUtil.extractUserId(authentication);
        BigDecimal balance = cardService.getCardBalance(id, userId);
        return ResponseEntity.ok(balance);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete card (Admin only)")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }


}
