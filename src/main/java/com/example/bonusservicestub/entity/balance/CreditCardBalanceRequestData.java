package com.example.bonusservicestub.entity.balance;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreditCardBalanceRequestData {
    private String systemID;

    private String channel;

    private String requestID;

    private String traceID;

    private String type;

    private List<String> virtualCardNumbers;
}
