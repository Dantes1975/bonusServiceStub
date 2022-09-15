package com.example.bonusservicestub.listener;

import com.example.bonusservicestub.entity.balance.CreditCardBalanceRequest;
import com.example.bonusservicestub.service.balance.CardBalanceJMSSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import static lombok.AccessLevel.PRIVATE;

@Slf4j
public class CardBalanceListener implements MessageListener {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    @Getter(PRIVATE) private CardBalanceJMSSender sender;


    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                final String objectMessage = textMessage.getText();
                final String jmsCorrelationID = textMessage.getJMSCorrelationID();
                final Long timeToLive = textMessage.getJMSExpiration();
                log.info("Card balance request with JMSCorrelationID={}, TTL ={}", jmsCorrelationID, timeToLive);
                CreditCardBalanceRequest request = objectMapper.readValue(objectMessage, CreditCardBalanceRequest.class);
                log.info("Card balance request with text={}", objectMessage);
                sender.sendCardBalance(request, jmsCorrelationID, timeToLive);

            }
        } catch (Exception e) {
            log.error("Exception during recieving card balance request", e);
        }
    }

}
