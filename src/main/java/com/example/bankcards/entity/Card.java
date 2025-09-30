package com.example.bankcards.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @ManyToOne()
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "fromCard")
    private List<Transfer> outgoingTransfers;

    @OneToMany(mappedBy = "toCard")
    private List<Transfer> incomingTransfers;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    private boolean blockRequested = false;

    public Long getId() {
        return id;
    }

    public Card setId(Long id) {
        this.id = id;
        return this;
    }

    public String getNumber() {
        return number;
    }

    public Card setNumber(String number) {
        this.number = number;
        return this;
    }

    public CardStatus getStatus() {
        return status;
    }

    public Card setStatus(CardStatus status) {
        this.status = status;
        return this;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Card setBalance(BigDecimal balance) {
        this.balance = balance;
        return this;
    }

    public User getUser() {
        return user;
    }

    public Card setUser(User user) {
        this.user = user;
        return this;
    }

    public List<Transfer> getOutgoingTransfers() {
        return outgoingTransfers;
    }

    public Card setOutgoingTransfers(List<Transfer> outgoingTransfers) {
        this.outgoingTransfers = outgoingTransfers;
        return this;
    }

    public List<Transfer> getIncomingTransfers() {
        return incomingTransfers;
    }

    public Card setIncomingTransfers(List<Transfer> incomingTransfers) {
        this.incomingTransfers = incomingTransfers;
        return this;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public Card setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
        return this;
    }

    public boolean isBlockRequested() {
        return blockRequested;
    }

    public Card setBlockRequested(boolean blockRequested) {
        this.blockRequested = blockRequested;
        return this;
    }
}