package com.example.bonusservicestub.service.phone;

import com.example.bonusservicestub.entity.phone.cardByPhone.CardAdditionPhoneDetailsRequest;
import com.example.bonusservicestub.utils.StubUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Hashtable;

@Service
@Slf4j
public class CardAdditionPhoneSenderImpl implements CardAdditionPhoneSender {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Value("${ru.bpc.svat.mobilebank.messaging.bonus.history.cf.jndi}")
    private String connectionFactoryJndi;

    @Value("${ru.bpc.svat.mobilebank.products.by.phone.queue.response.jndi}")
    private String queueJndi;

    @Value("${ru.bpc.svat.mobilebank.messaging.jmsserver.url}")
    private String jmsServerUrl;

    @Value("${ru.bpc.svat.mobilebank.messaging.weblogic.user}")
    private String weblogicUser;

    @Value("${ru.bpc.svat.mobilebank.messaging.weblogic.password}")
    private String weblogicPassword;

    @Value("${ru.bpc.svat.mobilebank.get.products.by.phone.filePath}")
    private String filePath;

    @Value("${get.products.by.phone.response.enabled}")
    private Boolean isSendEnabled;

    private Queue queue;
    private QueueConnectionFactory factory;
    private QueueConnection connection;

    @PostConstruct
    @SneakyThrows
    protected void init() {
        final Hashtable<String, String> environment = new Hashtable<>();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
        environment.put(Context.PROVIDER_URL, "t3://" + jmsServerUrl);
        if (StringUtils.isNoneBlank(weblogicUser)) {
            environment.put(Context.SECURITY_PRINCIPAL, weblogicUser);
        }
        if (StringUtils.isNoneBlank(weblogicPassword)) {
            environment.put(Context.SECURITY_CREDENTIALS, weblogicPassword);
        }
        Context context = new InitialContext(environment);
        this.factory = (QueueConnectionFactory)context.lookup(connectionFactoryJndi);;
        this.queue = (Queue)context.lookup(queueJndi);
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    @Override
    public void sendCardAdditionPhoneDetailsResponse(CardAdditionPhoneDetailsRequest request,
                                                     String correlationID,
                                                     Long timeToLive,
                                                     String transactionID) {
        try {
            QueueSession session = getConnection().createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            final QueueSender sender = session.createSender(queue);
            final TextMessage textMessage = session.createTextMessage();
            textMessage.setJMSCorrelationID(correlationID);
            textMessage.setStringProperty("X_TransactionID", transactionID);
            final String message = StubUtil.getMessageFromFile(filePath);
            textMessage.setText(message);
            if (!isSendEnabled) {
                log.info("Sending GetProductsByPhone response disabled");
                return;
            }
            sender.send(textMessage, DeliveryMode.PERSISTENT, 4, timeToLive);
            log.info("Send GetProductsByPhone response with JMSCorrelationID ={} and text={}", correlationID, message);
        } catch (Exception e) {
            log.error("Error during send message", e);
        }
    }

    @PreDestroy
    protected void preDestroy() {
        if(connection != null) {
            try {
                connection.close();
            } catch (JMSException e) {
                log.error("Connection close exception: {}", e.getMessage(), e);
            }
        }
    }

    private QueueConnection getConnection() throws JMSException {
        if(connection == null) {
            connection = factory.createQueueConnection();
        }
        return connection;
    }

}
