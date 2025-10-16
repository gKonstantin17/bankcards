package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    Page<Card> findByOwnerId(Long ownerId, Pageable pageable);
    List<Card> findByOwnerId(Long ownerId);

    Page<Card> findByOwnerIdAndStatus(Long ownerId, Card.CardStatus status, Pageable pageable);

    Optional<Card> findByCardNumber(String cardNumber);

    @Query("SELECT c FROM Card c WHERE c.owner.id = :ownerId AND c.id = :cardId")
    Optional<Card> findByIdAndOwnerId(@Param("cardId") Long cardId, @Param("ownerId") Long ownerId);

    boolean existsByCardNumber(String cardNumber);
}
