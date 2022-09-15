package com.example.bonusservicestub.entity.bonus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BonusHistoryPoint {
    private String value;
    private String operationType;
    private String changeLoyaltyDate;
    private String maskNum;
    private List<PointDetail> detailList;
}
