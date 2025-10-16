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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

    private final CardRepository cardRepository;
    private final UserService userService;
    private final EncryptionUtil encryptionUtil;
    private final CardMaskingUtil cardMaskingUtil;
    private final CardNumberGenerator cardNumberGenerator;

    @Transactional
    public CardDto createCard(CardCreateDto createDto) {
        log.info("Creating new card for user: {}", createDto.getOwnerId());

        User owner = userService.getUserById(createDto.getOwnerId());

        String cardNumber = cardNumberGenerator.generateCardNumber();
        String cvv = cardNumberGenerator.generateCVV();

        // Проверяем уникальность номера карты
        while (cardRepository.existsByCardNumber(encryptionUtil.encrypt(cardNumber))) {
            cardNumber = cardNumberGenerator.generateCardNumber();
        }

        Card card = Card.builder()
                .cardNumber(encryptionUtil.encrypt(cardNumber))
                .cardHolder(createDto.getCardHolder().toUpperCase())
                .expiryDate(createDto.getExpiryDate())
                .cvv(encryptionUtil.encrypt(cvv))
                .status(Card.CardStatus.ACTIVE)
                .balance(createDto.getInitialBalance() != null ? createDto.getInitialBalance() : BigDecimal.ZERO)
                .owner(owner)
                .build();

        Card savedCard = cardRepository.save(card);
        log.info("Card created successfully with id: {}", savedCard.getId());

        return mapToDto(savedCard, cardNumber);
    }

    @Transactional
    public CardDto getCardById(Long id) {
        Card card = findCardById(id);
        card.updateStatus();
        return mapToDto(card);
    }

    @Transactional
    public Page<CardDto> getUserCards(Long userId, Pageable pageable) {
        return cardRepository.findByOwnerId(userId, pageable)
                .map(card -> {
                    card.updateStatus();
                    return mapToDto(card);
                });
    }

    public List<CardDto> getUserCards(Long userId) {
        return cardRepository.findByOwnerId(userId)
                .stream()
                .map(card -> {
                    card.updateStatus();
                    return mapToDto(card);
                })
                .toList();
    }

    @Transactional
    public Page<CardDto> getUserCardsByStatus(Long userId, Card.CardStatus status, Pageable pageable) {
        return cardRepository.findByOwnerIdAndStatus(userId, status, pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<CardDto> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable)
                .map(card -> {
                    card.updateStatus();
                    return mapToDto(card);
                });
    }

    @Transactional
    public CardDto blockCard(Long cardId) {
        log.info("Blocking card with id: {}", cardId);

        Card card = findCardById(cardId);

        if (card.getStatus() == Card.CardStatus.BLOCKED) {
            throw new BusinessException("Card is already blocked");
        }

        card.setStatus(Card.CardStatus.BLOCKED);
        Card savedCard = cardRepository.save(card);

        return mapToDto(savedCard);
    }

    @Transactional
    public CardDto unblockCard(Long cardId) {
        log.info("Unblocking card with id: {}", cardId);

        Card card = findCardById(cardId);

        if (card.getStatus() != Card.CardStatus.BLOCKED) {
            throw new BusinessException("Card is not blocked");
        }

        if (card.isExpired()) {
            throw new BusinessException("Cannot unblock expired card");
        }

        card.setStatus(Card.CardStatus.ACTIVE);
        Card savedCard = cardRepository.save(card);

        return mapToDto(savedCard);
    }

    @Transactional
    public void deleteCard(Long cardId) {
        log.info("Deleting card with id: {}", cardId);
        Card card = findCardById(cardId);

        if (card.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("Cannot delete card with positive balance");
        }

        cardRepository.delete(card);
    }

    @Transactional
    public BigDecimal getCardBalance(Long cardId, Long userId) {
        Card card = cardRepository.findByIdAndOwnerId(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found or doesn't belong to user"));
        return card.getBalance();
    }

    @Transactional
    public CardDto requestCardBlock(Long cardId, Long userId) {
        log.info("User {} requesting block for card {}", userId, cardId);

        Card card = cardRepository.findByIdAndOwnerId(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found or doesn't belong to user"));

        if (card.getStatus() == Card.CardStatus.BLOCKED) {
            throw new BusinessException("Card is already blocked");
        }

        card.setStatus(Card.CardStatus.BLOCKED);
        Card savedCard = cardRepository.save(card);

        return mapToDto(savedCard);
    }

    private Card findCardById(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + id));
    }

    void validateCardForTransaction(Card card) {
        if (card.getStatus() == Card.CardStatus.BLOCKED) {
            throw new CardBlockedException("Card is blocked");
        }

        if (card.getStatus() == Card.CardStatus.EXPIRED || card.isExpired()) {
            throw new CardBlockedException("Card is expired");
        }
    }

    Card getCardByIdInternal(Long id) {
        return findCardById(id);
    }

    Card saveCard(Card card) {
        return cardRepository.save(card);
    }

    private CardDto mapToDto(Card card) {
        String decryptedNumber = encryptionUtil.decrypt(card.getCardNumber());
        return mapToDto(card, decryptedNumber);
    }

    private CardDto mapToDto(Card card, String decryptedNumber) {
        return CardDto.builder()
                .id(card.getId())
                .maskedCardNumber(cardMaskingUtil.maskCardNumber(decryptedNumber))
                .cardHolder(card.getCardHolder())
                .expiryDate(card.getExpiryDate())
                .status(card.getStatus())
                .balance(card.getBalance())
                .ownerId(card.getOwner().getId())
                .ownerUsername(card.getOwner().getUsername())
                .build();
    }
}
