package com.example.bonusservicestub.listener;

import com.example.bonusservicestub.entity.bonus.BonusDetailsRequest;
import com.example.bonusservicestub.entity.bonus.BonusHistoryRequest;
import com.example.bonusservicestub.service.bonus.BonusJMSSender;
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
import javax.jms.TextMessage;

import static lombok.AccessLevel.PRIVATE;

@Slf4j
public class BonusMessageListener implements MessageListener {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${answerDelay}")
    private Long answerDelay;

    @Autowired
    @Getter(PRIVATE) private BonusJMSSender bonusJMSSender;

    @PostConstruct
    public void init() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.registerSubtypes(new NamedType(BonusDetailsRequest.class, "BonusDetailsRequest"));
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
                final Long timeToLive = textMessage.getJMSExpiration();
                    log.info("Bonus details request with JMSCorrelationID={}, TTL ={}", jmsCorrelationID, timeToLive);
                    BonusDetailsRequest bonusDetailsRequest = objectMapper.readValue(objectMessage, BonusDetailsRequest.class);
                    log.info("Bonus details request with text={}", objectMessage);
                    if (answerDelay > 0) {
                        try {
                            Thread.sleep(answerDelay);
                        } catch (InterruptedException e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                    bonusJMSSender.sendBonusDetails(bonusDetailsRequest, jmsCorrelationID, timeToLive, transactionID);
                    bonusJMSSender.sendAdditionalBonusDetails(transactionID, timeToLive);

            }
        } catch (Exception e) {
            log.error("Exception during updating setting", e);
        }
    }

}
