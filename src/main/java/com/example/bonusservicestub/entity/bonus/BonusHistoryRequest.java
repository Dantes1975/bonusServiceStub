package com.example.bonusservicestub.entity.bonus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BonusHistoryRequest implements BonusRequest {

    private BonusHistoryData data;


    @Override
    public String getBoId() {
        return data.getMemberId();
    }
}
