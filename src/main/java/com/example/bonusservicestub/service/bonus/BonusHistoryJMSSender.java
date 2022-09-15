package com.example.bonusservicestub.service.bonus;

import com.example.bonusservicestub.entity.bonus.BonusHistoryRequest;
import org.springframework.stereotype.Service;

@Service
public interface BonusHistoryJMSSender {
    void sendBonusHistory(BonusHistoryRequest request,
                          String correlationID,
                          Long timeToLive,
                          String transactionID);

    void sendAdditionalBonusHistory(String transactionID, Long timeToLive);
}
