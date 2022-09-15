package com.example.bonusservicestub.service.balance;

import com.example.bonusservicestub.entity.balance.CreditCardBalanceRequest;
import org.springframework.stereotype.Service;

@Service
public interface CardBalanceJMSSender {
    void sendCardBalance(CreditCardBalanceRequest request,
                         String correlationID,
                         Long timeToLive);
}
