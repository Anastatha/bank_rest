package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardCryptoUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CardServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CardCryptoUtil cryptoUtil;

    @InjectMocks
    private CardService cardService;

    private User testUser;
    private Card testCard;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setId(1L);

        testCard = new Card();
        testCard.setId(1L);
        testCard.setUser(testUser);
        testCard.setNumber("encrypted");
        testCard.setBalance(BigDecimal.ZERO);
        testCard.setStatus(CardStatus.ACTIVE);
        testCard.setExpiryDate(LocalDate.now().plusYears(1));
    }

    @Test
    void createCardForUser_ShouldCreateCard() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cardRepository.existsByNumber(anyString())).thenReturn(false);
        when(cryptoUtil.encrypt(anyString())).thenReturn("encrypted");
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        Card card = cardService.createCardForUser(1L);

        assertThat(card.getUser()).isEqualTo(testUser);
        assertThat(card.getBalance()).isEqualTo(BigDecimal.ZERO);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void createCardForUser_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.createCardForUser(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findCardsByUserId_ShouldReturnPage() {
        when(cardRepository.findByUserId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testCard)));

        Page<Card> page = cardService.findCardsByUserId(1L, Pageable.unpaged());

        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void findCardsByUserIdAndNumber_ShouldReturnPage() {
        when(cardRepository.findByUserIdAndNumberContaining(eq(1L), eq("1234"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testCard)));

        Page<Card> page = cardService.findCardsByUserIdAndNumber(1L, "1234", Pageable.unpaged());

        assertThat(page.getContent()).contains(testCard);
    }

    @Test
    void getCardByIdForUser_ShouldReturnCard() {
        when(cardRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testCard));

        Card card = cardService.getCardByIdForUser(1L, 1L);

        assertThat(card).isEqualTo(testCard);
    }

    @Test
    void getCardByIdForUser_ShouldThrow_WhenNotFound() {
        when(cardRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCardByIdForUser(1L, 1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteCard_ShouldInvokeRepository() {
        cardService.deleteCard(1L);

        verify(cardRepository).deleteById(1L);
    }

    @Test
    void requestBlockCard_ShouldSetBlockRequested() {
        when(cardRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        Card card = cardService.requestBlockCard(1L, 1L);

        assertThat(card.isBlockRequested()).isTrue();
    }

    @Test
    void requestBlockCard_ShouldThrow_WhenNotActive() {
        testCard.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testCard));

        assertThatThrownBy(() -> cardService.requestBlockCard(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void approveBlockCard_ShouldBlockCard() {
        testCard.setBlockRequested(true);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        Card card = cardService.approveBlockCard(1L);

        assertThat(card.getStatus()).isEqualTo(CardStatus.BLOCKED);
        assertThat(card.isBlockRequested()).isFalse();
    }

    @Test
    void approveBlockCard_ShouldThrow_WhenNoRequest() {
        testCard.setBlockRequested(false);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        assertThatThrownBy(() -> cardService.approveBlockCard(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getMaskedNumber_ShouldReturnMasked() {
        when(cryptoUtil.decrypt("encrypted")).thenReturn("1234567812345678");

        String masked = cardService.getMaskedNumber(testCard);

        assertThat(masked).isEqualTo("**** **** **** 5678");
    }

    @Test
    void getAllCards_ShouldReturnPage() {
        when(cardRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(testCard)));

        Page<Card> page = cardService.getAllCards(Pageable.unpaged());

        assertThat(page.getContent()).contains(testCard);
    }

    @Test
    void depositToCard_ShouldIncreaseBalance() {
        when(cardRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        Card updated = cardService.depositToCard(1L, 1L, BigDecimal.valueOf(100));

        assertThat(updated.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void depositToCard_ShouldThrow_WhenNegativeAmount() {
        assertThatThrownBy(() -> cardService.depositToCard(1L, 1L, BigDecimal.valueOf(-10)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void checkAndUpdateExpiredCards_ShouldUpdateExpired() {
        Card expired = new Card();
        expired.setId(2L);
        expired.setStatus(CardStatus.ACTIVE);
        expired.setExpiryDate(LocalDate.now().minusDays(1));

        when(cardRepository.findByStatusAndExpiryDateBefore(eq(CardStatus.ACTIVE), any(LocalDate.class)))
                .thenReturn(List.of(expired));

        cardService.checkAndUpdateExpiredCards();

        assertThat(expired.getStatus()).isEqualTo(CardStatus.EXPIRED);
        verify(cardRepository).save(expired);
    }
}
