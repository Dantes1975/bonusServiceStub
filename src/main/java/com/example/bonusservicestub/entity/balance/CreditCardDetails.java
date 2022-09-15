package com.example.bonusservicestub.entity.balance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditCardDetails {
    private boolean is3DSecure;
    private String startDate;
    private String activateDate;
    private String priorityPassNum;
}
