package com.example.bonusservicestub.entity;

import lombok.Data;

import java.util.List;

@Data
public class BonusHistoryResponse {
    private List<BonusHistory> pointsList;
}
