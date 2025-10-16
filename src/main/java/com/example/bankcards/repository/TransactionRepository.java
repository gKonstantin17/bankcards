package com.example.bankcards.repository;

import com.example.bankcards.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByFromCardIdOrToCardId(Long fromCardId, Long toCardId, Pageable pageable);

    @Query("""
    SELECT DISTINCT t
    FROM Transaction t
    WHERE t.fromCard.id IN :fromCardIds
       OR t.toCard.id IN :toCardIds
""")
    Page<Transaction> findByFromCardIdInOrToCardIdIn(@Param("fromCardIds")List<Long> fromCardIds,
                                                     @Param("toCardIds") List<Long> toCardIds, Pageable pageable);

}
