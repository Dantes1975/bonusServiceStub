package com.example.bonusservicestub.service.bonus;

import com.example.bonusservicestub.entity.bonus.*;
import com.example.bonusservicestub.service.bonus.BonusHistoryJMSSender;
import com.example.bonusservicestub.utils.StubUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
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
public class BonusHistoryJMSSenderImpl implements BonusHistoryJMSSender {

    @Value("${ru.bpc.svat.mobilebank.messaging.bonus.history.cf.jndi}")
    private String connectionFactoryJndi;

    @Value("${ru.bpc.svat.mobilebank.history.messaging.queue.response.jndi}")
    private String queueJndi;

    @Value("${ru.bpc.svat.mobilebank.messaging.jmsserver.url}")
    private String jmsServerUrl;

    @Value("${ru.bpc.svat.mobilebank.messaging.weblogic.user}")
    private String weblogicUser;

    @Value("${ru.bpc.svat.mobilebank.messaging.weblogic.password}")
    private String weblogicPassword;

    @Value("${ru.bpc.svat.mobilebank.bonus.history.filePath}")
    private String filePath;

    @Value("${bonus.history.response.enabled}")
    private Boolean isSendEnabled;

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
    public void sendBonusHistory(BonusHistoryRequest request,
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
                log.info("Sending bonus history response disabled");
                return;
            }
            sender.send(textMessage, DeliveryMode.PERSISTENT, 4, timeToLive);
            log.info("Send bonus history response with JMSCorrelationID ={} and text={} to queue={}", correlationID, message, queue.getQueueName());
        } catch (Exception e) {
            log.error("Error during send message", e);
        }

    }

    @Override
    public void sendAdditionalBonusHistory(String transactionID, Long timeToLive) {
        try {
            QueueSession session = getConnection().createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            final QueueSender sender = session.createSender(queue);
            final TextMessage textMessage = session.createTextMessage();
            final String correlationID = generateCorrelationId();
            textMessage.setJMSCorrelationID(correlationID);
            textMessage.setStringProperty("X_TransactionID", transactionID);
            final String message = StubUtil.getMessageFromFile(filePath);
            textMessage.setText(message);
            sender.send(textMessage, DeliveryMode.PERSISTENT, 4, timeToLive);
            log.info("Send bonus history response with JMSCorrelationID ={} and text={} to queue={}", correlationID, message, queue.getQueueName());
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

    private String generateCorrelationId() {
        return StringUtils.leftPad(java.util.UUID.randomUUID().toString().replace("-", ""), 48, "0");
    }

}
