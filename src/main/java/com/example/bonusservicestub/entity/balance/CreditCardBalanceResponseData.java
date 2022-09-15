package com.example.bonusservicestub.entity.balance;

import lombok.Data;

import java.util.List;

@Data
public class CreditCardBalanceResponseData {
    private List<CreditCard> cards;
}
