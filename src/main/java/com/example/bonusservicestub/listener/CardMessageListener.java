package com.example.bonusservicestub.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Slf4j
public class CardMessageListener implements MessageListener {

    @Value("${ru.bpc.svat.mobilebank.p2p.messaging.queue.request.jndi}")
    private String queueJndi;

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                final String objectMessage = textMessage.getText();
                final String jmsCorrelationID = textMessage.getJMSCorrelationID();

                    final String transactionID = textMessage.getStringProperty("X_TransactionID");
                    log.info("P2p card parameters response: JMSCorrelationID={}, X_TransactionID={}, text={}, recieved from queue={}",
                            jmsCorrelationID, transactionID, objectMessage, queueJndi);

            }
        } catch (Exception e) {
            log.error("Exception during updating setting. Message {}", e.getMessage());
        }
    }

}
