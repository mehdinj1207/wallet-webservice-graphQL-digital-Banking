package com.bank.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class AddWalletRequestDTO {
    private Double balance;
    private String currencyCode;
}
