package com.example.bonusservicestub.entity.balance;

import lombok.Data;

@Data
public class CreditCardBalanceError {
    private String systemId;
    private String cardId;
    private String code;
    private String message;
}
