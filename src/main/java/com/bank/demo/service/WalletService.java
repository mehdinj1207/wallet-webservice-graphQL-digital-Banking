package com.bank.demo.service;

import com.bank.demo.dto.AddWalletRequestDTO;
import com.bank.demo.entities.Currency;
import com.bank.demo.entities.Wallet;
import com.bank.demo.entities.WalletTransaction;
import com.bank.demo.enums.TransactionType;
import com.bank.demo.repositories.CurrencyRepository;
import com.bank.demo.repositories.WalletRepository;
import com.bank.demo.repositories.WalletTransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@Transactional
public class WalletService {
    private CurrencyRepository currencyRepository;
    private WalletRepository walletRepository;
    private WalletTransactionRepository walletTransactionRepository;

    public WalletService(CurrencyRepository currencyRepository, WalletRepository walletRepository, WalletTransactionRepository walletTransactionRepository) {
        this.currencyRepository = currencyRepository;
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
    }

    public void loadData()throws IOException{
        URI uri= new ClassPathResource("countries_currencies.CSV").getURI();
        Path path=Paths.get(uri);
        List<String> lines = Files.readAllLines(path);
        for (int i=1; i<lines.size() ; i++ ){
            String[] line=lines.get(i).split(",");
            Currency currency= Currency.builder()
                    .code(line[0])
                    .name(line[1])
                    .salePrice(Double.parseDouble((line[2])))
                    .purchasePrice(Double.parseDouble(line[3]))
                    .build();
            currencyRepository.save(currency);

        }
        Stream.of("AUD","USD","EUR","ARS").forEach(currencyCode->{
            Currency currency= currencyRepository.findById(currencyCode)
                    .orElseThrow(()->new RuntimeException(String.format("Currency %s not found", currencyCode)));
            Wallet wallet= new Wallet();
            wallet.setBalance(10000.0);
            wallet.setCurrency(currency);
            wallet.setCreatedAt(System.currentTimeMillis());
            wallet.setUserId("user1");
            wallet.setId(UUID.randomUUID().toString());
            walletRepository.save(wallet);
        });
        walletRepository.findAll().forEach(wallet -> {
            for (int i=0;i<10;i++) {
                WalletTransaction debitWalletTransaction = WalletTransaction.builder()
                        .amount(Math.random() * 1000)
                        .wallet(wallet)
                        .type(TransactionType.DEBIT)
                        .timestamp(System.currentTimeMillis())
                        .build();
                walletTransactionRepository.save(debitWalletTransaction);
                wallet.setBalance(wallet.getBalance()-debitWalletTransaction.getAmount());
                walletRepository.save(wallet);
                WalletTransaction creditWalletTransaction = WalletTransaction.builder()
                        .amount(Math.random() * 1000)
                        .wallet(wallet)
                        .type(TransactionType.CREDIT)
                        .timestamp(System.currentTimeMillis())
                        .build();
                walletTransactionRepository.save(creditWalletTransaction);
                wallet.setBalance(wallet.getBalance()+debitWalletTransaction.getAmount());
                walletRepository.save(wallet);
            }
        });
    }
    public Wallet save(AddWalletRequestDTO walletDTO){
        Currency currency=currencyRepository.findById(walletDTO.getCurrencyCode())
                .orElseThrow(()->new RuntimeException(String.format("Currency %s not found",walletDTO.getCurrencyCode())));
        Wallet wallet=Wallet.builder()
                .balance(walletDTO.getBalance())
                .id(UUID.randomUUID().toString())
                .createdAt(System.currentTimeMillis())
                .userId("user1")
                .currency(currency)
                .build();
        walletRepository.save(wallet);
        return wallet;
    }
    public List<WalletTransaction> walletTransfer(String sourceWalletId, String destinationWalletId, Double amount)
    {
        Wallet sourceWallet=walletRepository.findById(sourceWalletId)
                .orElseThrow(()->new RuntimeException("Wallet"+sourceWalletId+"Not found"));
        Wallet destinationWallet=walletRepository.findById(destinationWalletId)
                .orElseThrow(()->new RuntimeException("Wallet"+destinationWalletId+"Not found"));
        Double destinationAmount= amount*(destinationWallet.getCurrency().getSalePrice()/sourceWallet.getCurrency().getPurchasePrice());
        WalletTransaction sourceWalletTransaction= WalletTransaction.builder()
                .timestamp(System.currentTimeMillis())
                .type(TransactionType.DEBIT)
                .amount(amount)
                .currentSaleCurrencyPrice(sourceWallet.getCurrency().getSalePrice())
                .currentPurchaseCurrencyPrice(sourceWallet.getCurrency().getPurchasePrice())
                .wallet(sourceWallet)
                .build();
        walletTransactionRepository.save(sourceWalletTransaction);
        sourceWallet.setBalance(sourceWallet.getBalance()-amount);

        WalletTransaction destinationWalletTransaction= WalletTransaction.builder()
                .timestamp(System.currentTimeMillis())
                .type(TransactionType.CREDIT)
                .amount(destinationAmount)
                .currentSaleCurrencyPrice(destinationWallet.getCurrency().getSalePrice())
                .currentPurchaseCurrencyPrice(destinationWallet.getCurrency().getPurchasePrice())
                .wallet(destinationWallet)
                .build();
        walletTransactionRepository.save(destinationWalletTransaction);
        destinationWallet.setBalance(sourceWallet.getBalance()+destinationAmount);
        return Arrays.asList(sourceWalletTransaction, destinationWalletTransaction);
    }
}
