package com.example.bonusservicestub.service.balance;

import com.example.bonusservicestub.entity.balance.*;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

@Service
@Slf4j
public class CardBalanceJMSSenderImpl implements CardBalanceJMSSender {

    @Value("${ru.bpc.svat.mobilebank.card.balance.cf.jndi}")
    private String connectionFactoryJndi;

    @Value("${ru.bpc.svat.mobilebank.card.balance.queue.response.jndi}")
    private String queueJndi;

    @Value("${ru.bpc.svat.mobilebank.messaging.jmsserver.url}")
    private String jmsServerUrl;

    @Value("${ru.bpc.svat.mobilebank.messaging.weblogic.user}")
    private String weblogicUser;

    @Value("${ru.bpc.svat.mobilebank.messaging.weblogic.password}")
    private String weblogicPassword;

    @Value("${bonus.response.error}")
    private Boolean isError;

    @Value("${bonus.response.error.message}")
    private String errorMessage;

    @Value("${bonus.response.error.code}")
    private String errorCode;

    @Value("${bonus.response.error.system.id}")
    private String errorSystemId;

    @Value("${card.balance.actual.timestamp}")
    private String actualTimestamp;

    @Value("${card.mainAmount.balance.enable}")
    private Boolean mainAmountEnable;

    @Value("${card.mainAmount.balance.amount}")
    private BigDecimal mainAmount;

    @Value("${card.mainAmount.balance.currency}")
    private String mainCurrency;

    @Value("${card.ownFunds.balance.enable}")
    private Boolean ownFundsEnable;

    @Value("${card.ownFunds.balance.amount}")
    private BigDecimal ownFundsAmount;

    @Value("${card.ownFunds.balance.currency}")
    private String ownFundsCurrency;

    @Value("${card.creditAmount.balance.enable}")
    private Boolean creditAmountEnable;

    @Value("${card.creditAmount.balance.amount}")
    private BigDecimal creditAmount;

    @Value("${card.creditAmount.balance.currency}")
    private String creditCurrency;

    @Value("${card.balance.maskNum}")
    private String mackNum;

    @Value("${card.balance.response.enabled}")
    private Boolean isSendEnabled;

    private static final String MAIN_AMOUNT = "mainAmount";
    private static final String CREDIT_AMOUNT = "creditAmount";
    private static final String OWN_FUNDS = "ownFunds";
    private static final String ERROR_STATUS = "error";
    private static final String SUCCESS_STATUS = "success";
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
    public void sendCardBalance(CreditCardBalanceRequest request, String correlationID, Long timeToLive) {
        try {
            QueueSession session = getConnection().createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            final QueueSender sender = session.createSender(queue);
            final TextMessage textMessage = session.createTextMessage();
            textMessage.setJMSCorrelationID(correlationID);
            final String message = processCardBalanceMessage(request);
            textMessage.setText(message);
            if (!isSendEnabled) {
                log.info("Sending card balance response disabled");
                return;
            }
            sender.send(textMessage, DeliveryMode.PERSISTENT, 4, timeToLive);
            log.info("Send card balance response with JMSCorrelationID ={} and text={} to queue={}", correlationID, message, queue.getQueueName());
        } catch (Exception e) {
            log.error("Error during send message", e);
        }

    }

    private String processCardBalanceMessage(CreditCardBalanceRequest request) {
        String virtualCardNumber = request.getData().getVirtualCardNumbers().get(0);
        CreditCardBalanceResponse response = new CreditCardBalanceResponse();
        CreditCardBalanceResponseData data = new CreditCardBalanceResponseData();

        CreditCard creditCard = new CreditCard();
        creditCard.setBase(generateCreditCardBase(virtualCardNumber));
        creditCard.setDetail(new CardInfoDetails("2022-01-21", "2021-02-28", false, "1234456"));
        data.setCards(Collections.singletonList(creditCard));
        response.setActualTimestamp(actualTimestamp);
        response.setStatus(SUCCESS_STATUS);
        response.setData(data);


            List<CreditCardBalanceError> errors = new ArrayList<>();
            CreditCardBalanceError error = new CreditCardBalanceError();
            error.setCode(errorCode);
            error.setMessage(errorMessage);
            error.setSystemId(errorSystemId);
            errors.add(error);
            response.setErrors(errors);




        return  convertBalanceResponseToString(response);
    }

    private String convertBalanceResponseToString(CreditCardBalanceResponse response) {
        try {
            return OBJECT_MAPPER.writeValueAsString(response);
        } catch (Exception e) {
            log.error("Error while convert response to string");
        }
        return StringUtils.EMPTY;
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

    private CreditCardBalance generateMainAmountBalance() {
        CreditCardBalance balance = new CreditCardBalance();
        balance.setType(MAIN_AMOUNT);
        CreditCardBalanceValue value = new CreditCardBalanceValue();
        value.setAmount(mainAmount);
        value.setCurrency(new CreditCardBalanceCurrency("810", mainCurrency));
        balance.setValue(value);
        return balance;
    }

    private CreditCardBalance generateOwnFundsBalance() {
        CreditCardBalance balance = new CreditCardBalance();
        balance.setType(OWN_FUNDS);
        CreditCardBalanceValue value = new CreditCardBalanceValue();
        value.setAmount(ownFundsAmount);
        value.setCurrency(new CreditCardBalanceCurrency("810", ownFundsCurrency));
        balance.setValue(value);
        return balance;
    }

    private CreditCardBalance generateCreditAmountBalance() {
        CreditCardBalance balance = new CreditCardBalance();
        balance.setType(CREDIT_AMOUNT);
        CreditCardBalanceValue value = new CreditCardBalanceValue();
        value.setAmount(creditAmount);
        value.setCurrency(new CreditCardBalanceCurrency("810", creditCurrency));
        balance.setValue(value);
        return balance;
    }

    private CreditCardBase generateCreditCardBase(String virtualCardNumber) {
        CreditCardBase base = new CreditCardBase();
        base.setVirtualNum(virtualCardNumber);
        base.setMaskNum(mackNum);
        base.setState(new CreditCardState("ACTIVE", "Действует"));
        base.setOriginalState(new CreditCardState("24", "DO NOT HONOR"));
        base.setCardPrivate(new CreditCardPrivate(0, "09/27", "2021-02-28"));
        List<CreditCardBalance> balances = new ArrayList<>();
        if (mainAmountEnable) {
            balances.add(generateMainAmountBalance());
        }
        if (ownFundsEnable) {
            balances.add(generateOwnFundsBalance());
        }
        if (creditAmountEnable) {
            balances.add(generateCreditAmountBalance());
        }
        base.setBalance(balances);
        return base;
    }

}
