package com.example.bonusservicestub.entity.bonus;

import com.example.bonusservicestub.entity.bonus.BonusHistory;
import lombok.Data;

import java.util.List;

@Data
public class BonusHistoryResponse {
    private List<BonusHistory> pointsList;
}
