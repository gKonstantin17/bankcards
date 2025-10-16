package com.example.bankcards.service;

import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BusinessException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private CardService cardService;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransferService transferService;

    private User testUser;
    private Card sourceCard;
    private Card destinationCard;
    private TransferDto transferDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        sourceCard = Card.builder()
                .id(1L)
                .cardNumber("encrypted123")
                .cardHolder("TEST USER")
                .expiryDate(LocalDate.now().plusYears(3))
                .status(Card.CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .owner(testUser)
                .build();

        destinationCard = Card.builder()
                .id(2L)
                .cardNumber("encrypted456")
                .cardHolder("TEST USER")
                .expiryDate(LocalDate.now().plusYears(3))
                .status(Card.CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(500))
                .owner(testUser)
                .build();

        transferDto = TransferDto.builder()
                .fromCardId(1L)
                .toCardId(2L)
                .amount(BigDecimal.valueOf(100))
                .description("Test transfer")
                .build();
    }

    @Test
    void transferBetweenOwnCards_Success() {
        // Arrange
        when(cardService.getCardByIdInternal(1L)).thenReturn(sourceCard);
        when(cardService.getCardByIdInternal(2L)).thenReturn(destinationCard);
        doNothing().when(cardService).validateCardForTransaction(any(Card.class));
        when(cardService.saveCard(any(Card.class))).thenAnswer(i -> i.getArguments()[0]);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> {
            Transaction t = (Transaction) i.getArguments()[0];
            t.setId(1L);
            return t;
        });

        // Act
        Transaction result = transferService.transferBetweenOwnCards(transferDto, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(Transaction.TransactionStatus.SUCCESS, result.getStatus());
        assertEquals(BigDecimal.valueOf(900), sourceCard.getBalance());
        assertEquals(BigDecimal.valueOf(600), destinationCard.getBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void transferBetweenOwnCards_SameCard_ThrowsException() {
        // Arrange
        transferDto.setToCardId(1L); // Same as fromCardId

        // Act & Assert
        assertThrows(BusinessException.class,
                () -> transferService.transferBetweenOwnCards(transferDto, 1L));
    }

    @Test
    void transferBetweenOwnCards_NegativeAmount_ThrowsException() {
        // Arrange
        transferDto.setAmount(BigDecimal.valueOf(-100));

        // Act & Assert
        assertThrows(BusinessException.class,
                () -> transferService.transferBetweenOwnCards(transferDto, 1L));
    }

    @Test
    void transferBetweenOwnCards_InsufficientFunds_ThrowsException() {
        // Arrange
        transferDto.setAmount(BigDecimal.valueOf(2000));
        when(cardService.getCardByIdInternal(1L)).thenReturn(sourceCard);
        when(cardService.getCardByIdInternal(2L)).thenReturn(destinationCard);
        doNothing().when(cardService).validateCardForTransaction(any(Card.class));

        // Act & Assert
        assertThrows(InsufficientFundsException.class,
                () -> transferService.transferBetweenOwnCards(transferDto, 1L));
    }

    @Test
    void transferBetweenOwnCards_CardNotOwnedByUser_ThrowsException() {
        // Arrange
        User anotherUser = User.builder().id(2L).username("another").build();
        destinationCard.setOwner(anotherUser);

        when(cardService.getCardByIdInternal(1L)).thenReturn(sourceCard);
        when(cardService.getCardByIdInternal(2L)).thenReturn(destinationCard);

        // Act & Assert
        assertThrows(BusinessException.class,
                () -> transferService.transferBetweenOwnCards(transferDto, 1L));
    }
}
