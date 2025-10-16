package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.exception.BusinessException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.repository.TransactionRepository;
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
public class TransferService {

    private final CardService cardService;
    private final TransactionRepository transactionRepository;

    @Transactional
    public Transaction transferBetweenOwnCards(TransferDto transferDto, Long userId) {
        log.info("Processing transfer from card {} to card {} for user {}",
                transferDto.getFromCardId(), transferDto.getToCardId(), userId);

        // Проверки
        if (transferDto.getFromCardId().equals(transferDto.getToCardId())) {
            throw new BusinessException("Cannot transfer to the same card");
        }

        if (transferDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Transfer amount must be positive");
        }

        // Получаем карты
        Card fromCard = cardService.getCardByIdInternal(transferDto.getFromCardId());
        Card toCard = cardService.getCardByIdInternal(transferDto.getToCardId());

        // Проверяем, что обе карты принадлежат пользователю
        if (!fromCard.getOwner().getId().equals(userId) || !toCard.getOwner().getId().equals(userId)) {
            throw new BusinessException("You can only transfer between your own cards");
        }

        // Валидация карт
        cardService.validateCardForTransaction(fromCard);
        cardService.validateCardForTransaction(toCard);

        // Проверка баланса
        if (fromCard.getBalance().compareTo(transferDto.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds on source card");
        }

        // Выполняем перевод
        try {
            fromCard.setBalance(fromCard.getBalance().subtract(transferDto.getAmount()));
            toCard.setBalance(toCard.getBalance().add(transferDto.getAmount()));

            cardService.saveCard(fromCard);
            cardService.saveCard(toCard);

            Transaction transaction = Transaction.builder()
                    .fromCard(fromCard)
                    .toCard(toCard)
                    .amount(transferDto.getAmount())
                    .status(Transaction.TransactionStatus.SUCCESS)
                    .description(transferDto.getDescription())
                    .build();

            Transaction savedTransaction = transactionRepository.save(transaction);
            log.info("Transfer completed successfully. Transaction id: {}", savedTransaction.getId());

            return savedTransaction;

        } catch (Exception e) {
            log.error("Transfer failed", e);

            Transaction failedTransaction = Transaction.builder()
                    .fromCard(fromCard)
                    .toCard(toCard)
                    .amount(transferDto.getAmount())
                    .status(Transaction.TransactionStatus.FAILED)
                    .description("Transfer failed: " + e.getMessage())
                    .build();

            transactionRepository.save(failedTransaction);
            throw new BusinessException("Transfer failed: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Page<Transaction> getUserTransactions(Long userId, Pageable pageable) {
        List<CardDto> userCards = cardService.getUserCards(userId);

        if (userCards.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> cardIds = userCards.stream()
                .map(CardDto::getId)
                .toList();

        return transactionRepository.findByFromCardIdInOrToCardIdIn(cardIds, cardIds, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Transaction> getCardTransactions(Long cardId, Pageable pageable) {
        return transactionRepository.findByFromCardIdOrToCardId(cardId, cardId, pageable);
    }
}
