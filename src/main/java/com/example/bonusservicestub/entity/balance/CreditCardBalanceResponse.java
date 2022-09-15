package com.example.bonusservicestub.entity.balance;

import lombok.Data;

import java.util.List;

@Data
public class CreditCardBalanceResponse {
    private String status;

    private String actualTimestamp;

    private CreditCardBalanceResponseData data;

    private List<CreditCardBalanceError> errors;
}
