package com.example.bonusservicestub.config;

import com.example.bonusservicestub.listener.BonusHistoryListener;
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
public class JmsHistoryRequestConfiguration {

    @Value("${ru.bpc.svat.mobilebank.messaging.bonus.history.cf.jndi}")
    private String connectionFactoryJndi;

    @Value("${ru.bpc.svat.mobilebank.history.messaging.queue.request.jndi}")
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
        log.info("ru.bpc.svat.mobilebank.messaging.topic.jndi = {}", queueJndi);
        log.info("ru.bpc.svat.mobilebank.messaging.jmsserver.url = {}", jmsServerUrl);
    }

    @Bean
    public JndiTemplate historyJndiTemplate() {
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
    public JndiObjectFactoryBean queueHistoryConnectionFactory() {
        JndiObjectFactoryBean queueHistoryConnectionFactory = new JndiObjectFactoryBean();
        queueHistoryConnectionFactory.setJndiTemplate(historyJndiTemplate());
        queueHistoryConnectionFactory.setJndiName(connectionFactoryJndi);
        return queueHistoryConnectionFactory;
    }

    @Bean
    public JndiDestinationResolver jmsHistoryDestinationResolver() {
        JndiDestinationResolver destResolver = new JndiDestinationResolver();
        destResolver.setJndiTemplate(historyJndiTemplate());
        destResolver.setCache(true);
        return destResolver;
    }

    @Bean
    public JndiObjectFactoryBean jmsHistoryQueue() {
        JndiObjectFactoryBean jmsQueue = new JndiObjectFactoryBean();
        jmsQueue.setJndiTemplate(historyJndiTemplate());
        jmsQueue.setJndiName(queueJndi);
        return jmsQueue;
    }

    @Bean
    public MessageListener queueHistoryListener() {
        return new BonusHistoryListener();
    }

    @Bean
    @Qualifier("jmsHistoryTaskExecutor")
    public TaskExecutor taskHistoryExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(5);
        return executor;
    }

    @Bean
    public DefaultMessageListenerContainer messageHistoryListener(@Qualifier("jmsHistoryTaskExecutor") TaskExecutor taskExecutor) {
        DefaultMessageListenerContainer listener = new DefaultMessageListenerContainer();
        listener.setTaskExecutor(taskExecutor);
        listener.setConnectionFactory((ConnectionFactory) queueHistoryConnectionFactory().getObject());
        listener.setDestination((Destination) jmsHistoryQueue().getObject());
        listener.setMessageListener(queueHistoryListener());

        return listener;
    }

}
