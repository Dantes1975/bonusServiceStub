package com.example.bonusservicestub.listener;

import com.example.bonusservicestub.entity.BonusDetailsRequest;
import com.example.bonusservicestub.entity.BonusHistoryRequest;
import com.example.bonusservicestub.service.BonusHistoryJMSSender;
import com.example.bonusservicestub.service.BonusJMSSender;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.TextMessage;

import static lombok.AccessLevel.PRIVATE;

@Slf4j
public class BonusHistoryListener implements MessageListener {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${answerDelay}")
    private Long answerDelay;

    @Autowired
    @Getter(PRIVATE) private BonusHistoryJMSSender bonusJMSSender;

    @PostConstruct
    public void init() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.registerSubtypes(new NamedType(BonusHistoryRequest.class, "BonusHistoryRequest"));
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                final String objectMessage = textMessage.getText();
                final String jmsCorrelationID = textMessage.getJMSCorrelationID();
                final String transactionID = textMessage.getStringProperty("X_TransactionID");
                final Queue responseQueue = (Queue) textMessage.getJMSReplyTo();
                final Long timeToLive = textMessage.getJMSExpiration();
                log.info("Bonus history request with JMSCorrelationID={}, TTL ={} and ReplyTo={}", jmsCorrelationID, timeToLive, textMessage.getJMSReplyTo().toString());
                BonusHistoryRequest bonusHistoryRequest = objectMapper.readValue(objectMessage, BonusHistoryRequest.class);
                    log.info("Bonus history request with text={}", objectMessage);
                    if (answerDelay > 0) {
                        try {
                            Thread.sleep(answerDelay);
                        } catch (InterruptedException e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                    bonusJMSSender.sendBonusHistory(bonusHistoryRequest, jmsCorrelationID, timeToLive, transactionID, responseQueue);
                    //bonusJMSSender.sendAdditionalBonusHistory(transactionID, timeToLive, responseQueue);
            }
        } catch (Exception e) {
            log.error("Exception during updating setting. Message {}", e.getMessage());
        }
    }

}
