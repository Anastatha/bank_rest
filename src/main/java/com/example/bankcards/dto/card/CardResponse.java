package com.example.bankcards.dto.card;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardResponse(
        Long id,
        String maskedNumber,
        CardStatus status,
        BigDecimal balance,
        LocalDate expiryDate,
        boolean blockRequested,
        String userUsername
) {
    public static CardResponse fromEntity(Card card, String maskedNumber) {
        return new CardResponse(
                card.getId(),
                maskedNumber,
                card.getStatus(),
                card.getBalance(),
                card.getExpiryDate(),
                card.isBlockRequested(),
                card.getUser().getUsername()
        );
    }
}
