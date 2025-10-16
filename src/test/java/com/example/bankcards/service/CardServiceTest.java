package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateDto;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BusinessException;
import com.example.bankcards.exception.CardBlockedException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardMaskingUtil;
import com.example.bankcards.util.CardNumberGenerator;
import com.example.bankcards.util.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserService userService;

    @Mock
    private EncryptionUtil encryptionUtil;

    @Mock
    private CardMaskingUtil cardMaskingUtil;

    @Mock
    private CardNumberGenerator cardNumberGenerator;

    @InjectMocks
    private CardService cardService;

    private User testUser;
    private Card testCard;
    private CardCreateDto cardCreateDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        testCard = Card.builder()
                .id(1L)
                .cardNumber("encrypted123")
                .cardHolder("TEST USER")
                .expiryDate(LocalDate.now().plusYears(3))
                .cvv("encrypted456")
                .status(Card.CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .owner(testUser)
                .build();

        cardCreateDto = CardCreateDto.builder()
                .cardHolder("Test User")
                .expiryDate(LocalDate.now().plusYears(3))
                .initialBalance(BigDecimal.valueOf(500))
                .ownerId(1L)
                .build();
    }

    @Test
    void getCardById_Success() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(encryptionUtil.decrypt(anyString())).thenReturn("4000001234567890");
        when(cardMaskingUtil.maskCardNumber(anyString())).thenReturn("**** **** **** 7890");

        // Act
        CardDto result = cardService.getCardById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testCard.getId(), result.getId());
        verify(cardRepository, times(1)).findById(1L);
    }

    @Test
    void getCardById_NotFound() {
        // Arrange
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> cardService.getCardById(999L));
    }

    @Test
    void getUserCards_Success() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(List.of(testCard));

        when(cardRepository.findByOwnerId(1L, pageRequest)).thenReturn(cardPage);
        when(encryptionUtil.decrypt(anyString())).thenReturn("4000001234567890");
        when(cardMaskingUtil.maskCardNumber(anyString())).thenReturn("**** **** **** 7890");

        // Act
        Page<CardDto> result = cardService.getUserCards(1L, pageRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(cardRepository, times(1)).findByOwnerId(1L, pageRequest);
    }

    @Test
    void blockCard_Success() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(encryptionUtil.decrypt(anyString())).thenReturn("4000001234567890");
        when(cardMaskingUtil.maskCardNumber(anyString())).thenReturn("**** **** **** 7890");

        // Act
        CardDto result = cardService.blockCard(1L);

        // Assert
        assertNotNull(result);
        assertEquals(Card.CardStatus.BLOCKED, testCard.getStatus());
        verify(cardRepository, times(1)).save(testCard);
    }

    @Test
    void blockCard_AlreadyBlocked() {
        // Arrange
        testCard.setStatus(Card.CardStatus.BLOCKED);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // Act & Assert
        assertThrows(BusinessException.class, () -> cardService.blockCard(1L));
    }

    @Test
    void unblockCard_Success() {
        // Arrange
        testCard.setStatus(Card.CardStatus.BLOCKED);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(encryptionUtil.decrypt(anyString())).thenReturn("4000001234567890");
        when(cardMaskingUtil.maskCardNumber(anyString())).thenReturn("**** **** **** 7890");

        // Act
        CardDto result = cardService.unblockCard(1L);

        // Assert
        assertNotNull(result);
        assertEquals(Card.CardStatus.ACTIVE, testCard.getStatus());
        verify(cardRepository, times(1)).save(testCard);
    }

    @Test
    void deleteCard_WithPositiveBalance_ThrowsException() {
        // Arrange
        testCard.setBalance(BigDecimal.valueOf(100));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // Act & Assert
        assertThrows(BusinessException.class, () -> cardService.deleteCard(1L));
        verify(cardRepository, never()).delete(any(Card.class));
    }

    @Test
    void validateCardForTransaction_BlockedCard_ThrowsException() {
        // Arrange
        testCard.setStatus(Card.CardStatus.BLOCKED);

        // Act & Assert
        assertThrows(CardBlockedException.class, () -> cardService.validateCardForTransaction(testCard));
    }

    @Test
    void validateCardForTransaction_ExpiredCard_ThrowsException() {
        // Arrange
        testCard.setStatus(Card.CardStatus.EXPIRED);

        // Act & Assert
        assertThrows(CardBlockedException.class, () -> cardService.validateCardForTransaction(testCard));
    }
}
