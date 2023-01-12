package com.example.bonusservicestub.listener;

import com.example.bonusservicestub.entity.phone.cardByPhone.CardAdditionPhoneDetailsRequest;
import com.example.bonusservicestub.service.phone.CardAdditionPhoneSender;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import static lombok.AccessLevel.PRIVATE;

@Slf4j
public class CardAdditionPhoneListener implements MessageListener {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    @Getter(PRIVATE) private CardAdditionPhoneSender sender;

    @PostConstruct
    public void init() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
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
                log.info("GetProductsByPhone request: JMSCorrelationID={}, X_TransactionID={}, text={}",
                        jmsCorrelationID, transactionID, objectMessage);
                CardAdditionPhoneDetailsRequest request = objectMapper.readValue(objectMessage, CardAdditionPhoneDetailsRequest.class);
                sender.sendCardAdditionPhoneDetailsResponse(request, jmsCorrelationID, timeToLive, transactionID);
            }
        } catch (Exception e) {
            log.error("Exception during getting GetPhoneByClientId request. Message {}", e.getMessage());
        }
    }
}
