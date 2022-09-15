package com.example.bonusservicestub.service.bic;

import com.example.bonusservicestub.service.bic.BankBicSender;
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
import javax.naming.NamingException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;

@Service
@Slf4j
public class BankBicSenderImpl implements BankBicSender {

    @Value("${ru.bpc.svat.mobilebank.bankBic.cf.jndi}")
    private String connectionFactoryJndi;

    @Value("${ru.bpc.svat.mobilebank.bankBic.queue.jndi}")
    private String queueJndi;

    @Value("${ru.bpc.svat.mobilebank.bankBic.jmsserver.url}")
    private String jmsServerUrl;

    @Value("${ru.bpc.svat.mobilebank.bankBic.weblogic.user}")
    private String weblogicUser;

    @Value("${ru.bpc.svat.mobilebank.bankBic.weblogic.password}")
    private String weblogicPassword;

    @Value("${ru.bpc.svat.mobilebank.bankBic.filePath}")
    private String filePath;

    @Value("${ru.bpc.svat.mobilebank.bankBic.enabled}")
    private boolean isBankBicEnabled;

    @Value("${ru.bpc.svat.mobilebank.bankBic.object.message}")
    private boolean isObjectMessage;

    @Value("${ru.bpc.svat.mobilebank.bankBic.text.message}")
    private boolean isTextMessage;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Queue queue;
    private QueueConnectionFactory factory;
    private QueueConnection connection;

    @PostConstruct
    @SneakyThrows
    protected void init() {
        if (isBankBicEnabled) {
            initContext();
            log.info("bank.bic.cf.jndi = {}", connectionFactoryJndi);
            log.info("bank.bic.topic.jndi = {}", queueJndi);
            log.info("bank.bic.jmsserver.url = {}", jmsServerUrl);
        }
    }

    private void initContext() throws NamingException {
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
    public void sendBankBic() {
        try {
            QueueSession session = getConnection().createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            final QueueSender sender = session.createSender(queue);
            final String correlationID = generateCorrelationId();
            if (isObjectMessage) {
                final ObjectMessage objectMessage = session.createObjectMessage();
                File file = new File(filePath);
                objectMessage.setObject(file);
                objectMessage.setJMSCorrelationID(correlationID);
                sender.send(objectMessage);
                log.info("Send bank BIC object message with JMSCorrelationID ={} to queue={}", correlationID, queue.getQueueName());
                isTextMessage = false;
            }

            if (isTextMessage) {
                final TextMessage textMessage = session.createTextMessage();
                final String message = getMessageFromFile(filePath);
                textMessage.setJMSCorrelationID(correlationID);
                textMessage.setText(message);
                sender.send(textMessage);
                log.info("Send bank BIC with JMSCorrelationID ={} to queue={}", correlationID, queue.getQueueName());
            }

        } catch (Exception e) {
            log.error("Error during sending bank BIC message", e);
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

    private String getMessageFromFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        InputStream inputStream = Files.newInputStream(path);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
}
