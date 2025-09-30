package com.example.bankcards.service;

import com.example.bankcards.dto.transfer.TransferInput;
import com.example.bankcards.dto.transfer.TransferResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.exception.ForbiddenException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TransferService {

    private final TransferRepository transferRepository;
    private final CardRepository cardRepository;

    public TransferService(TransferRepository transferRepository,
                           CardRepository cardRepository) {
        this.transferRepository = transferRepository;
        this.cardRepository = cardRepository;
    }

    @Transactional
    public TransferResponse transferBetweenOwnCards(Long userId, TransferInput request) {
        BigDecimal amount = request.amount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма должна быть больше нуля");
        }

        Card from = cardRepository.findByIdForUpdate(request.fromCardId())
                .orElseThrow(() -> new NotFoundException("Карта,от куда переводить, не найдена"));
        Card to = cardRepository.findByIdForUpdate(request.toCardId())
                .orElseThrow(() -> new NotFoundException("Карта, куда переводить, не найдена"));

        if (!from.getUser().getId().equals(userId) || !to.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Карты не принадлежат одному и тому же пользователю");
        }

        if (from.getStatus() != CardStatus.ACTIVE || to.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalArgumentException("Обе карты должны быть ACTIVE");
        }

        if (from.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Недостаточно средств");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        cardRepository.save(from);
        cardRepository.save(to);

        Transfer tr = new Transfer();
        tr.setFromCard(from);
        tr.setToCard(to);
        tr.setAmount(amount);
        tr.setTransferDate(LocalDateTime.now());

        Transfer saved = transferRepository.save(tr);

        return new TransferResponse(saved.getId(), from.getId(), to.getId(), amount, saved.getTransferDate());
    }

    public Page<TransferResponse> getTransfersByUser(Long userId, Pageable pageable) {
        return transferRepository.findByFromCard_User_IdOrToCard_User_Id(userId, userId, pageable)
                .map(t -> new TransferResponse(t.getId(),
                        t.getFromCard().getId(),
                        t.getToCard().getId(),
                        t.getAmount(),
                        t.getTransferDate()));
    }

    public Page<TransferResponse> getTransfersByCard(Long cardId, Pageable pageable) {
        List<Transfer> combined = new ArrayList<>();
        combined.addAll(transferRepository.findByFromCard_Id(cardId, Pageable.unpaged()).getContent());
        combined.addAll(transferRepository.findByToCard_Id(cardId, Pageable.unpaged()).getContent());

        combined.sort(Comparator.comparing(Transfer::getTransferDate).reversed());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), combined.size());
        List<TransferResponse> content = new ArrayList<>();
        if (start <= end) {
            for (Transfer t : combined.subList(start, end)) {
                content.add(new TransferResponse(t.getId(),
                        t.getFromCard().getId(),
                        t.getToCard().getId(),
                        t.getAmount(),
                        t.getTransferDate()));
            }
        }

        return new PageImpl<>(content, pageable, combined.size());
    }
}
