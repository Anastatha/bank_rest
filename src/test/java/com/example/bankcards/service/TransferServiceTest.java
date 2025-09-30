package com.example.bankcards.service;

import com.example.bankcards.dto.transfer.TransferInput;
import com.example.bankcards.dto.transfer.TransferResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ForbiddenException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private TransferService transferService;

    private User testUser;
    private Card fromCard;
    private Card toCard;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);

        fromCard = new Card();
        fromCard.setId(10L);
        fromCard.setUser(testUser);
        fromCard.setBalance(new BigDecimal("1000"));
        fromCard.setStatus(CardStatus.ACTIVE);

        toCard = new Card();
        toCard.setId(20L);
        toCard.setUser(testUser);
        toCard.setBalance(new BigDecimal("500"));
        toCard.setStatus(CardStatus.ACTIVE);
    }

    @Test
    void transferBetweenOwnCards_ShouldTransferMoney() {
        TransferInput input = new TransferInput(fromCard.getId(), toCard.getId(), new BigDecimal("200"));

        when(cardRepository.findByIdForUpdate(fromCard.getId())).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdForUpdate(toCard.getId())).thenReturn(Optional.of(toCard));

        Transfer saved = new Transfer();
        saved.setId(100L);
        saved.setFromCard(fromCard);
        saved.setToCard(toCard);
        saved.setAmount(new BigDecimal("200"));
        saved.setTransferDate(LocalDateTime.now());
        when(transferRepository.save(any(Transfer.class))).thenReturn(saved);

        TransferResponse response = transferService.transferBetweenOwnCards(testUser.getId(), input);

        assertThat(response.amount()).isEqualByComparingTo("200");
        assertThat(fromCard.getBalance()).isEqualByComparingTo("800");
        assertThat(toCard.getBalance()).isEqualByComparingTo("700");

        verify(cardRepository, times(1)).save(fromCard);
        verify(cardRepository, times(1)).save(toCard);
        verify(transferRepository, times(1)).save(any(Transfer.class));
    }

    @Test
    void transferBetweenOwnCards_ShouldThrow_WhenCardNotFound() {
        TransferInput input = new TransferInput(99L, toCard.getId(), new BigDecimal("100"));
        when(cardRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.transferBetweenOwnCards(testUser.getId(), input))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void transferBetweenOwnCards_ShouldThrow_WhenCardsBelongDifferentUser() {
        User anotherUser = new User();
        anotherUser.setId(2L);
        toCard.setUser(anotherUser);

        TransferInput input = new TransferInput(fromCard.getId(), toCard.getId(), new BigDecimal("100"));

        when(cardRepository.findByIdForUpdate(fromCard.getId())).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdForUpdate(toCard.getId())).thenReturn(Optional.of(toCard));

        assertThatThrownBy(() -> transferService.transferBetweenOwnCards(testUser.getId(), input))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void transferBetweenOwnCards_ShouldThrow_WhenInsufficientFunds() {
        TransferInput input = new TransferInput(fromCard.getId(), toCard.getId(), new BigDecimal("5000"));

        when(cardRepository.findByIdForUpdate(fromCard.getId())).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdForUpdate(toCard.getId())).thenReturn(Optional.of(toCard));

        assertThatThrownBy(() -> transferService.transferBetweenOwnCards(testUser.getId(), input))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void transferBetweenOwnCards_ShouldThrow_WhenCardNotActive() {
        fromCard.setStatus(CardStatus.BLOCKED);

        TransferInput input = new TransferInput(fromCard.getId(), toCard.getId(), new BigDecimal("100"));

        when(cardRepository.findByIdForUpdate(fromCard.getId())).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdForUpdate(toCard.getId())).thenReturn(Optional.of(toCard));

        assertThatThrownBy(() -> transferService.transferBetweenOwnCards(testUser.getId(), input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Обе карты должны быть ACTIVE");
    }

    @Test
    void getTransfersByUser_ShouldReturnTransfers() {
        Transfer tr = new Transfer();
        tr.setId(1L);
        tr.setFromCard(fromCard);
        tr.setToCard(toCard);
        tr.setAmount(new BigDecimal("100"));
        tr.setTransferDate(LocalDateTime.now());

        Page<Transfer> page = new PageImpl<>(List.of(tr));
        when(transferRepository.findByFromCard_User_IdOrToCard_User_Id(eq(1L), eq(1L), any(Pageable.class)))
                .thenReturn(page);

        Page<TransferResponse> result = transferService.getTransfersByUser(1L, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).amount()).isEqualByComparingTo("100");
    }

    @Test
    void getTransfersByCard_ShouldReturnCombinedTransfers() {
        Transfer tr1 = new Transfer();
        tr1.setId(1L);
        tr1.setFromCard(fromCard);
        tr1.setToCard(toCard);
        tr1.setAmount(new BigDecimal("100"));
        tr1.setTransferDate(LocalDateTime.now().minusDays(1));

        Transfer tr2 = new Transfer();
        tr2.setId(2L);
        tr2.setFromCard(toCard);
        tr2.setToCard(fromCard);
        tr2.setAmount(new BigDecimal("200"));
        tr2.setTransferDate(LocalDateTime.now());

        when(transferRepository.findByFromCard_Id(eq(fromCard.getId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(tr1)));
        when(transferRepository.findByToCard_Id(eq(fromCard.getId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(tr2)));

        Page<TransferResponse> result = transferService.getTransfersByCard(fromCard.getId(), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).amount()).isEqualByComparingTo("200");
        assertThat(result.getContent().get(1).amount()).isEqualByComparingTo("100");
    }
}
