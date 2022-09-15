package com.example.bonusservicestub.entity.balance;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreditCardBalanceValue {
    private BigDecimal amount;
    private CreditCardBalanceCurrency currency;
}
