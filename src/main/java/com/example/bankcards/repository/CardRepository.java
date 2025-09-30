package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    Page<Card> findByUserId(Long userId, Pageable pageable);
    Page<Card> findByUserIdAndNumberContaining(Long userId, String partialNumber, Pageable pageable);
    boolean existsByNumber(String number);
    Optional<Card> findByIdAndUserId(Long id, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Card c where c.id = :id")
    Optional<Card> findByIdForUpdate(@Param("id") Long id);

    @Modifying
    @Query("update Card c set c.balance = c.balance + :amount where c.id = :id")
    int updateBalance(@Param("id") Long id, @Param("amount") BigDecimal amount);

    List<Card> findByStatusAndExpiryDateBefore(CardStatus status, LocalDate expiryDate);

    List<Card> findByUserIdAndStatusAndExpiryDateBefore(Long userId, CardStatus status, LocalDate expiryDate);

    List<Card> findByStatus(CardStatus status);

}
