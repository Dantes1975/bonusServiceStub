package com.example.bonusservicestub.service;

import com.example.bonusservicestub.entity.RequestGuid;
import com.example.bonusservicestub.entity.RequestGuidData;
import com.example.bonusservicestub.entity.RequestGuidMeta;
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
import java.util.UUID;

@Service
@Slf4j
public class CardJMSSenderImpl implements CardJMSSender {

    @Value("${ru.bpc.svat.mobilebank.messaging.p2p.cf.jndi}")
    private String connectionFactoryJndi;

    @Value("${ru.bpc.svat.mobilebank.p2p.messaging.queue.response.jndi}")
    private String queueJndi;

    @Value("${ru.bpc.svat.mobilebank.messaging.jmsserver.url}")
    private String jmsServerUrl;

    @Value("${ru.bpc.svat.mobilebank.messaging.weblogic.user}")
    private String weblogicUser;

    @Value("${ru.bpc.svat.mobilebank.messaging.weblogic.password}")
    private String weblogicPassword;

    @Value("${requestGuid.systeId}")
    private String guidSystemId;

    @Value("${requestGuid.channel}")
    private String guidChannel;

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
    public void sendRequestGuid(String guid) {
        try {
            RequestGuidMeta meta = new RequestGuidMeta();
            RequestGuidData data = new RequestGuidData();
            if (StringUtils.isNotEmpty(guidSystemId)) {
                meta.setSystemId(guidSystemId);
            }

            if (StringUtils.isNotEmpty(guidChannel)) {
                meta.setChannel(guidChannel);
            }

            data.setRequestGuid(guid);
            RequestGuid requestGuid = new RequestGuid();
            requestGuid.setMeta(meta);
            requestGuid.setData(data);
            QueueSession session = getConnection().createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            final QueueSender sender = session.createSender(queue);
            final TextMessage textMessage = session.createTextMessage();
            final String message = convertRequestGuidToString(requestGuid);
            final String correlationId = generateMessageId();
            final String transactionId =  generateTransactionId();
            textMessage.setText(message);
            textMessage.setJMSCorrelationID(correlationId);
            textMessage.setStringProperty("X_TransactionID", transactionId);
            //sender.send(textMessage, DeliveryMode.PERSISTENT, 4, 10000);
            sender.send(textMessage);
            log.info("P2p card parameters request: JMSCorrelationID={}, X_TransactionID={}, text={} sent to queue={}",
                    correlationId, transactionId, message, queueJndi);
        } catch (Exception e) {
            log.error("Error during send message", e);
        }
    }

    @Override
    public void sendAdditionalRequestGuid() {
        try {
            RequestGuidMeta meta = new RequestGuidMeta();
            RequestGuidData data = new RequestGuidData();
            if (StringUtils.isNotEmpty(guidSystemId)) {
                meta.setSystemId(guidSystemId);
            }

            if (StringUtils.isNotEmpty(guidChannel)) {
                meta.setChannel(guidChannel);
            }

            String guid = UUID.randomUUID().toString();

            data.setRequestGuid(guid);
            RequestGuid requestGuid = new RequestGuid();
            requestGuid.setMeta(meta);
            requestGuid.setData(data);
            QueueSession session = getConnection().createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            final QueueSender sender = session.createSender(queue);
            final TextMessage textMessage = session.createTextMessage();
            final String message = convertRequestGuidToString(requestGuid);
            final String correlationId = generateMessageId();
            final String transactionId =  generateTransactionId();
            textMessage.setText(message);
            textMessage.setJMSCorrelationID(correlationId);
            textMessage.setStringProperty("X_TransactionID", transactionId);
            //sender.send(textMessage, DeliveryMode.PERSISTENT, 4, 10000);
            sender.send(textMessage);
            log.info("P2p card parameters additional request: JMSCorrelationID={}, X_TransactionID={}, text={} sent to queue={}",
                    correlationId, transactionId, message, queueJndi);
        } catch (Exception e) {
            log.error("Error during send additional message", e);
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

    private String convertRequestGuidToString(RequestGuid requestGuid) {
        try {
            return OBJECT_MAPPER.writeValueAsString(requestGuid);
        } catch (Exception e) {
            log.error("Error while convert response to string");
        }
        return StringUtils.EMPTY;
    }

    private String generateMessageId() {
        return StringUtils.leftPad(java.util.UUID.randomUUID().toString().replace("-", ""), 48, "0");
    }

    private String generateTransactionId() {
        return UUID.randomUUID().toString();
    }

}
