package com.example.bonusservicestub.service;

import com.example.bonusservicestub.entity.BonusHistoryRequest;
import org.springframework.stereotype.Service;

import javax.jms.Queue;

@Service
public interface BonusHistoryJMSSender {
    void sendBonusHistory(BonusHistoryRequest request,
                          String correlationID,
                          Long timeToLive,
                          String transactionID,
                          Queue responseQueue);

    void sendAdditionalBonusHistory(String transactionID, Long timeToLive, Queue responseQueue);
}
