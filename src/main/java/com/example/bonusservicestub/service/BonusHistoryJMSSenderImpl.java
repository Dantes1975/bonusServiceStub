package com.example.bonusservicestub.service;

import com.example.bonusservicestub.entity.*;
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

    @Value("${bonus.history.page.total}")
    private Integer pageTotal;

    @Value("${bonus.history.page.number}")
    private Integer pageNum;

    @Value("${bonus.history.response.maskNum}")
    private String maskNum;

    @Value("${bonus.history.response.transDateFirst}")
    private String transDateFirst;

    @Value("${bonus.history.response.transDateSecond}")
    private String transDateSecond;

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
            final String message = processBonusHistoryMessage();
            textMessage.setText(message);
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
            final String message = processBonusHistoryMessage();
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

    private String processBonusHistoryMessage() {
        final FastDateFormat dateFormatter = FastDateFormat.getInstance("yyyy-MM-dd");
        BonusHistory history = new BonusHistory();
        history.setStatus("success");
        history.setActualTimestamp(actualTimestamp);
        BonusHistoryResponseData data = new BonusHistoryResponseData();
        data.setPageNum(pageNum);
        data.setPagesTotal(pageTotal);
        List<BonusHistoryPoint> points = new ArrayList<>();
        final String startDate =  dateFormatter.format(new Date());
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.MONTH, -1);
        Date checkTime = instance.getTime();
        final String startDate1 =  dateFormatter.format(checkTime);
        Calendar instance1 = Calendar.getInstance();
        instance1.add(Calendar.DAY_OF_MONTH, -15);
        Date checkTime1 = instance1.getTime();
        final String startDate2 =  dateFormatter.format(checkTime1);
        Calendar instance2 = Calendar.getInstance();
        instance2.add(Calendar.DAY_OF_MONTH, -25);
        Date checkTime2 = instance2.getTime();
        final String startDate3 =  dateFormatter.format(checkTime2);


        BonusHistoryPoint point = new BonusHistoryPoint("100", "extend", startDate, maskNum, new ArrayList<PointDetail>());
        BonusHistoryPoint point4 = new BonusHistoryPoint("100", "lessen", startDate, maskNum, new ArrayList<PointDetail>());
        BonusHistoryPoint point5 = new BonusHistoryPoint("100", "extend", startDate, maskNum, new ArrayList<PointDetail>());
        BonusHistoryPoint point1 = new BonusHistoryPoint("150", "lessen", startDate1, maskNum, new ArrayList<PointDetail>());
        BonusHistoryPoint point2 = new BonusHistoryPoint("150", "lessen", startDate2, maskNum, new ArrayList<PointDetail>());
        BonusHistoryPoint point3 = new BonusHistoryPoint("200", "extend", startDate3, maskNum, new ArrayList<PointDetail>());


        point.getDetailList().add(new PointDetail("4829", "TEST DBO 1", "730061", transDateFirst, "991002798839", "991002798839"));
        point1.getDetailList().add(new PointDetail("4830", "TEST DBO 1", "730062", transDateSecond, "991002798840", "991002798839"));
        points.add(point);
        points.add(point1);
        //points.add(point2);
        //points.add(point3);
        //points.add(point4);
        //points.add(point5);
        data.setPointsList(points);
        history.setData(data);


        if (isError) {
            history.setStatus("error");
            List<BonusError> errors = new ArrayList<>();
            BonusError error = new BonusError();
            error.setCode(errorCode);
            error.setMessage(errorMessage);
            error.setSystemId(errorSystemId);
            errors.add(error);
            history.setErrors(errors);
        }

        return convertHistoryResponseToString(history);
    }

    private String convertHistoryResponseToString(BonusHistory response) {
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
