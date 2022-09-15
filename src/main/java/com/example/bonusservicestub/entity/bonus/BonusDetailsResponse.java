package com.example.bonusservicestub.entity.bonus;

import lombok.Data;

import java.util.List;

@Data
public class BonusDetailsResponse {
    private String status;
    private String actualTimestamp;
    private BonusDetailsResponseData data;
    private List<BonusError> errors;
}
