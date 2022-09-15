package com.example.bonusservicestub.entity.balance;

import lombok.Data;

import java.util.List;

@Data
public class CreditCardBalanceRequestData {
    private String systemID;

    private String channel;

    private String requestID;

    private String traceID;

    private String type;

    private List<String> virtualCardNumbers;
}
