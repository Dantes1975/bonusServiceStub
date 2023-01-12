package com.example.bonusservicestub.service.phone;

import com.example.bonusservicestub.entity.phone.changeOper.*;
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
public class ChangePhoneDetailsSenderImpl implements ChangePhoneDetailsSender {

    private static final String ACTION_SET = "set";
    private static final String ACTION_CHANGE = "change";
    private static final String ACTION_CHANGE_CLIENT_TYPE = "changeClientType";
    private static final String ACTION_DELETE = "delete";


    @Value("${ru.bpc.svat.mobilebank.messaging.bonus.history.cf.jndi}")
    private String connectionFactoryJndi;

    @Value("${ru.bpc.svat.mobilebank.change.phone.details.queue.response.jndi}")
    private String queueJndi;

    @Value("${ru.bpc.svat.mobilebank.messaging.jmsserver.url}")
    private String jmsServerUrl;

    @Value("${ru.bpc.svat.mobilebank.messaging.weblogic.user}")
    private String weblogicUser;

    @Value("${ru.bpc.svat.mobilebank.messaging.weblogic.password}")
    private String weblogicPassword;

    @Value("${ru.bpc.svat.mobilebank.phone.set.filePath}")
    private String filePathSet;

    @Value("${ru.bpc.svat.mobilebank.phone.change.filePath}")
    private String filePathChange;

    @Value("${ru.bpc.svat.mobilebank.phone.change.client.type.filePath}")
    private String filePathChangeClientType;

    @Value("${ru.bpc.svat.mobilebank.phone.delete.filePath}")
    private String filePathDelete;

    @Value("${change.phone.details.response.enabled}")
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
    public void sendChangePhoneDetailsResponse(ChangePhoneDetailsRequest request,
                                               String correlationID,
                                               Long timeToLive,
                                               String transactionID) {
        try {
            final String action = request.getData().getSvfe().getAction();
            QueueSession session = getConnection().createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            final QueueSender sender = session.createSender(queue);
            final TextMessage textMessage = session.createTextMessage();
            textMessage.setJMSCorrelationID(correlationID);
            textMessage.setStringProperty("X_TransactionID", transactionID);
            String message = null;
            
            if (ACTION_SET.equalsIgnoreCase(action)) {
                message = StubUtil.getMessageFromFile(filePathSet);
            }

            if (ACTION_CHANGE.equalsIgnoreCase(action)) {
                message = StubUtil.getMessageFromFile(filePathChange);
            }

            if (ACTION_CHANGE_CLIENT_TYPE.equalsIgnoreCase(action)) {
                message = StubUtil.getMessageFromFile(filePathChangeClientType);
            }

            if (ACTION_DELETE.equalsIgnoreCase(action)) {
                message = StubUtil.getMessageFromFile(filePathDelete);
            }

            textMessage.setText(message);
            if (!isSendEnabled) {
                log.info("Sending ChangePhoneDetails response disabled by setting");
                return;
            }
            sender.send(textMessage, DeliveryMode.PERSISTENT, 4, timeToLive);
            log.info("Send ChangePhoneDetails response with JMSCorrelationID ={} and text={} to queue={}", correlationID, message, queue.getQueueName());
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
