package com.example.bonusservicestub.entity.balance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditCardPrivate {
    private int countFailPIN;
    private String expDate;
    private String expFullDate;
}
