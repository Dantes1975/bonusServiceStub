package com.example.bonusservicestub.config;

import com.example.bonusservicestub.listener.CardBalanceListener;
import com.example.bonusservicestub.listener.CardMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.PostConstruct;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageListener;
import javax.naming.Context;
import java.util.Properties;

@Configuration
@Slf4j
public class JmsCardBalanceConfiguration {

    @Value("${ru.bpc.svat.mobilebank.messaging.p2p.cf.jndi}")
    private String connectionFactoryJndi;

    @Value("${ru.bpc.svat.mobilebank.card.balance.queue.request.jndi}")
    private String queueJndi;

    @Value("${ru.bpc.svat.mobilebank.messaging.jmsserver.url}")
    private String jmsServerUrl;

    @Value("${ru.bpc.svat.mobilebank.messaging.weblogic.user}")
    private String weblogicUser;

    @Value("${ru.bpc.svat.mobilebank.messaging.weblogic.password}")
    private String weblogicPassword;

    @PostConstruct
    protected void inform() {
        log.info("ru.bpc.svat.mobilebank.messaging.cf.jndi = {}", connectionFactoryJndi);
        log.info("rru.bpc.svat.mobilebank.p2p.messaging.queue.request.jndi = {}", queueJndi);
        log.info("ru.bpc.svat.mobilebank.messaging.jmsserver.url = {}", jmsServerUrl);
    }

    @Bean
    public JndiTemplate cardBalanceJndiTemplate() {
        final JndiTemplate jndiTemplate = new JndiTemplate();
        final Properties environment = new Properties();
        environment.setProperty(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
        environment.setProperty(Context.PROVIDER_URL, "t3://" + jmsServerUrl);
        if(StringUtils.isNoneBlank(weblogicUser)) {
            environment.put(Context.SECURITY_PRINCIPAL, weblogicUser);
        }
        if(StringUtils.isNoneBlank(weblogicPassword)) {
            environment.put(Context.SECURITY_CREDENTIALS, weblogicPassword);
        }
        jndiTemplate.setEnvironment(environment);
        return jndiTemplate;
    }

    @Bean
    public JndiObjectFactoryBean queueCardBalanceConnectionFactory() {
        JndiObjectFactoryBean queueBalanceConnectionFactory = new JndiObjectFactoryBean();
        queueBalanceConnectionFactory.setJndiTemplate(cardBalanceJndiTemplate());
        queueBalanceConnectionFactory.setJndiName(connectionFactoryJndi);
        return queueBalanceConnectionFactory;
    }

    @Bean
    public JndiDestinationResolver jmsCardBalanceDestinationResolver() {
        JndiDestinationResolver balanceDestResolver = new JndiDestinationResolver();
        balanceDestResolver.setJndiTemplate(cardBalanceJndiTemplate());
        balanceDestResolver.setCache(true);
        return balanceDestResolver;
    }

    @Bean
    public JndiObjectFactoryBean jmsCardBalanceQueue() {
        JndiObjectFactoryBean jmsBalanceQueue = new JndiObjectFactoryBean();
        jmsBalanceQueue.setJndiTemplate(cardBalanceJndiTemplate());
        jmsBalanceQueue.setJndiName(queueJndi);
        return jmsBalanceQueue;
    }

    @Bean
    public MessageListener queueCardBalanceListener() {
        return new CardBalanceListener();
    }

    @Bean
    @Qualifier("jmsCardBalanceTaskExecutor")
    public TaskExecutor taskCardBalanceExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(5);
        return executor;
    }

    @Bean
    public DefaultMessageListenerContainer messageCardBalanceListener(@Qualifier("jmsCardBalanceTaskExecutor") TaskExecutor taskExecutor) {
        DefaultMessageListenerContainer balanceListener = new DefaultMessageListenerContainer();
        balanceListener.setTaskExecutor(taskExecutor);
        balanceListener.setConnectionFactory((ConnectionFactory) queueCardBalanceConnectionFactory().getObject());
        balanceListener.setDestination((Destination) jmsCardBalanceQueue().getObject());
        balanceListener.setMessageListener(queueCardBalanceListener());

        return balanceListener;
    }

}
