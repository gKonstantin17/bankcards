package com.example.bankcards.dto;

import jakarta.validation.constraints.*;
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
public class CardCreateDto {

    @NotBlank(message = "Card holder name is required")
    @Size(min = 3, max = 100)
    private String cardHolder;

    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be in the future")
    private LocalDate expiryDate;

    @DecimalMin(value = "0.0", inclusive = true, message = "Initial balance must be positive")
    private BigDecimal initialBalance = BigDecimal.ZERO;

    @NotNull(message = "Owner ID is required")
    private Long ownerId;
}
