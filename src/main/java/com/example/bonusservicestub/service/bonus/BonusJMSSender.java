package com.example.bonusservicestub.service.bonus;

import com.example.bonusservicestub.entity.bonus.BonusDetailsRequest;
import org.springframework.stereotype.Service;

@Service
public interface BonusJMSSender {
    void sendBonusDetails(BonusDetailsRequest request,
                          String correlationID,
                          Long timeToLive,
                          String transactionID);

    void sendAdditionalBonusDetails(String transactionID, Long timeToLive);
}
