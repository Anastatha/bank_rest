package com.example.bankcards.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfers")
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "from_card_id", nullable = false)
    private Card fromCard;

    @ManyToOne()
    @JoinColumn(name = "to_card_id", nullable = false)
    private Card toCard;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime transferDate = LocalDateTime.now();

    // геттеры и сеттеры

    public Long getId() {
        return id;
    }

    public Transfer setId(Long id) {
        this.id = id;
        return this;
    }

    public Card getFromCard() {
        return fromCard;
    }

    public Transfer setFromCard(Card fromCard) {
        this.fromCard = fromCard;
        return this;
    }

    public Card getToCard() {
        return toCard;
    }

    public Transfer setToCard(Card toCard) {
        this.toCard = toCard;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Transfer setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public LocalDateTime getTransferDate() {
        return transferDate;
    }

    public Transfer setTransferDate(LocalDateTime transferDate) {
        this.transferDate = transferDate;
        return this;
    }
}