package com.bank.demo.web;

import com.bank.demo.dto.AddWalletRequestDTO;
import com.bank.demo.entities.Currency;
import com.bank.demo.entities.Wallet;
import com.bank.demo.entities.WalletTransaction;
import com.bank.demo.repositories.CurrencyRepository;
import com.bank.demo.repositories.WalletRepository;
import com.bank.demo.service.WalletService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class WalletGraphQLController {
    private WalletRepository walletRepository;
    private CurrencyRepository currencyRepository;
    private WalletService walletService;

    public WalletGraphQLController(WalletRepository walletRepository, CurrencyRepository currencyRepository, WalletService walletService) {
        this.walletRepository = walletRepository;
        this.currencyRepository = currencyRepository;
        this.walletService = walletService;
    }
    @QueryMapping
    public List<Wallet> userWallets(){
        return walletRepository.findAll();
    }
    @QueryMapping
    public Wallet walletById(@Argument String id){
        return walletRepository.findById(id).
                orElseThrow(()->new RuntimeException(String.format("Wallet %s not found", id)));
    }
    @QueryMapping
    public List<Currency> currencies (){
        return currencyRepository.findAll();
    }
    @MutationMapping
    public Wallet addWallet(@Argument AddWalletRequestDTO walletDTO){
        return walletService.save(walletDTO);
    }
    @MutationMapping
    public List<WalletTransaction> walletTransfer(@Argument String sourceWalletID,
                                                  @Argument String destinationWalletID,
                                                  @Argument Double amount ){
        return walletService.walletTransfer(sourceWalletID,destinationWalletID,amount);
    }
}
