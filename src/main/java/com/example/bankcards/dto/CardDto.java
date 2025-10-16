package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardDto {
    private Long id;
    private String maskedCardNumber;
    private String cardHolder;
    private LocalDate expiryDate;
    private Card.CardStatus status;
    private BigDecimal balance;
    private Long ownerId;
    private String ownerUsername;
}
