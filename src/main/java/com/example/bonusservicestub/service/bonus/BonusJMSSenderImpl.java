package com.example.bonusservicestub.service.bonus;

import com.example.bonusservicestub.entity.bonus.BonusDetailsRequest;
import com.example.bonusservicestub.entity.bonus.BonusDetailsResponse;
import com.example.bonusservicestub.entity.bonus.BonusDetailsResponseData;
import com.example.bonusservicestub.entity.bonus.BonusError;
import com.example.bonusservicestub.service.bonus.BonusJMSSender;
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
import javax.jms.Queue;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.*;

@Service
@Slf4j
public class BonusJMSSenderImpl implements BonusJMSSender {
    @Value("${ru.bpc.svat.mobilebank.messaging.bonus.details.jndi}")
    private String connectionFactoryJndi;

    @Value("${ru.bpc.svat.mobilebank.messaging.queue.response.jndi}")
    private String queueJndi;

    @Value("${ru.bpc.svat.mobilebank.messaging.jmsserver.url}")
    private String jmsServerUrl;

    @Value("${ru.bpc.svat.mobilebank.messaging.weblogic.user}")
    private String weblogicUser;

    @Value("${ru.bpc.svat.mobilebank.messaging.weblogic.password}")
    private String weblogicPassword;

    @Value("${bonus.details.endCardDate}")
    private String endCardDate;

    @Value("${bonus.details.summPoints}")
    private String summPoints;

    @Value("${bonus.response.actual.timestamp}")
    private String actualTimestamp;

    @Value("${bonus.response.error.message}")
    private String errorMessage;

    @Value("${bonus.response.error.code}")
    private String errorCode;

    @Value("${bonus.response.error.system.id}")
    private String errorSystemId;

    @Value("${bonus.response.error}")
    private Boolean isError;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
    public void sendBonusDetails(BonusDetailsRequest request,
                                 String correlationID,
                                 Long timeToLive,
                                 String transactionID) {
        try {
            QueueSession session = getConnection().createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            final QueueSender sender = session.createSender(queue);
            final TextMessage textMessage = session.createTextMessage();
            textMessage.setJMSCorrelationID(correlationID);
            textMessage.setStringProperty("X_TransactionID", transactionID);
            final String message = processBonusDetailsMessage();
            textMessage.setText(message);
            sender.send(textMessage, DeliveryMode.PERSISTENT, 4, timeToLive);
            log.info("Send bonus details response with JMSCorrelationID ={} and text={} to queue={}", correlationID, message, queue.getQueueName());
        } catch (Exception e) {
            log.error("Error during send message", e);
        }
    }

    @Override
    public void sendAdditionalBonusDetails(String transactionID, Long timeToLive) {
        try {
            QueueSession session = getConnection().createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            final QueueSender sender = session.createSender(queue);
            final TextMessage textMessage = session.createTextMessage();
            final String correlationID = generateCorrelationId();
            textMessage.setJMSCorrelationID(correlationID);
            textMessage.setStringProperty("X_TransactionID", transactionID);
            final String message = processBonusDetailsMessage();
            textMessage.setText(message);
            sender.send(textMessage, DeliveryMode.PERSISTENT, 4, timeToLive);
            log.info("Send bonus details response with JMSCorrelationID ={} and text={} to queue={}", correlationID, message, queue.getQueueName());
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

    private String processBonusDetailsMessage() {
        BonusDetailsResponseData data = new BonusDetailsResponseData();
        data.setSummPoints(summPoints);
        data.setExpDate(endCardDate);
        BonusDetailsResponse response = new BonusDetailsResponse();
        response.setActualTimestamp(actualTimestamp);
        response.setStatus("success");
        response.setData(data);

        if (isError) {
            response.setStatus("error");
            List<BonusError> errors = new ArrayList<>();
            BonusError error = new BonusError();
            error.setCode(errorCode);
            error.setMessage(errorMessage);
            error.setSystemId(errorSystemId);
            errors.add(error);
            response.setErrors(errors);
        }

        return convertDetailsResponseToString(response);
    }

    private String convertDetailsResponseToString(BonusDetailsResponse response) {
        try {
            return OBJECT_MAPPER.writeValueAsString(response);
        } catch (Exception e) {
            log.error("Error while convert response to string");
        }
        return StringUtils.EMPTY;
    }

    private String generateCorrelationId() {
        return StringUtils.leftPad(java.util.UUID.randomUUID().toString().replace("-", ""), 48, "0");
    }

}
