package com.example.bonusservicestub.entity;

import lombok.Data;

import java.util.List;

@Data
public class BonusHistoryResponseData {
    private int pageNum;
    private int pagesTotal;
    private List<BonusHistoryPoint> pointsList;
}
