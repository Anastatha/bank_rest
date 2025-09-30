package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardCryptoUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardCryptoUtil cryptoUtil;

    public CardService(CardRepository cardRepository,
                       UserRepository userRepository,
                       CardCryptoUtil cryptoUtil) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.cryptoUtil = cryptoUtil;
    }

    public Card createCardForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String plainNumber = generateCardNumber();
        while (cardRepository.existsByNumber(plainNumber)) {
            plainNumber = generateCardNumber();
        }

        Card card = new Card();
        card.setNumber(cryptoUtil.encrypt(plainNumber));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);
        card.setUser(user);
        card.setExpiryDate(LocalDate.now().plusYears(3).withDayOfMonth(1).plusMonths(1).minusDays(1));

        return cardRepository.save(card);
    }

    private String generateCardNumber() {
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(rnd.nextInt(10));
        }
        return sb.toString();
    }

    public Page<Card> findCardsByUserId(Long userId, Pageable pageable) {
        return cardRepository.findByUserId(userId, pageable);
    }

    public Page<Card> findCardsByUserIdAndNumber(Long userId, String partialNumber, Pageable pageable) {
        return cardRepository.findByUserIdAndNumberContaining(userId, partialNumber, pageable);
    }

    public Card getCardByIdForUser(Long cardId, Long userId) {
        return cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new NotFoundException("Card not found or access denied"));
    }

    public void deleteCard(Long id) {
        cardRepository.deleteById(id);
    }

    public Card requestBlockCard(Long userId, Long cardId) {
        Card card = cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new NotFoundException("Card not found or not yours"));

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalArgumentException("Only ACTIVE cards can be requested for block");
        }

        card.setBlockRequested(true);
        return cardRepository.save(card);
    }

    public Card approveBlockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found"));

        if (!card.isBlockRequested()) {
            throw new IllegalArgumentException("No block request for this card");
        }

        card.setStatus(CardStatus.BLOCKED);
        card.setBlockRequested(false);
        return cardRepository.save(card);
    }

    public String getMaskedNumber(Card card) {
        String plain = cryptoUtil.decrypt(card.getNumber());
        return CardCryptoUtil.mask(plain);
    }

    public Page<Card> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable);
    }

    public Card depositToCard(Long userId, Long cardId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительной");
        }

        Card card = cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new NotFoundException("Карта не найдена или не принадлежит пользователю"));

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalArgumentException("На карту можно класть деньги только если она активна");
        }

        card.setBalance(card.getBalance().add(amount));
        return cardRepository.save(card);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void checkAndUpdateExpiredCards() {
        LocalDate today = LocalDate.now();
        List<Card> expiredCards = cardRepository.findByStatusAndExpiryDateBefore(CardStatus.ACTIVE, today);

        for (Card card : expiredCards) {
            card.setStatus(CardStatus.EXPIRED);
            cardRepository.save(card);
        }
    }
}
