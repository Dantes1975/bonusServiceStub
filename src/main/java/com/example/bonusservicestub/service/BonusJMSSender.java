package com.example.bonusservicestub.service;

import com.example.bonusservicestub.entity.BonusDetailsRequest;
import org.springframework.stereotype.Service;

import javax.jms.Queue;

@Service
public interface BonusJMSSender {
    void sendBonusDetails(BonusDetailsRequest request,
                          String correlationID,
                          Long timeToLive,
                          String transactionID,
                          Queue responseQueue);

    void sendAdditionalBonusDetails(String transactionID, Long timeToLive, Queue responseQueue);
}
