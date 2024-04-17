package com.bank.demo.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Wallet {
    @Id
    private String id;
    private Double balance;
    private Long createdAt;
    private String userId;
    @ManyToOne
    private Currency currency;
    @OneToMany(mappedBy="wallet")
    private List<WalletTransaction> walletTransactions;

}
