package com.example.bonusservicestub.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BonusHistory {
    private String status;
    private String actualTimestamp;
    private BonusHistoryResponseData data;
    private List<BonusError> errors;
}
