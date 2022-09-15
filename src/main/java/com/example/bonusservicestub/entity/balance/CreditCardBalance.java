package com.example.bonusservicestub.entity.balance;

import lombok.Data;

@Data
public class CreditCardBalance {
    private String type;
    public CreditCardBalanceValue value;
}
