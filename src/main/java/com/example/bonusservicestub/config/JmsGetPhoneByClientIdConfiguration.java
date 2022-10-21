package com.example.bonusservicestub.config;

import com.example.bonusservicestub.listener.GetPhoneByClientIdListener;
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
public class JmsGetPhoneByClientIdConfiguration {

    @Value("${ru.bpc.svat.mobilebank.messaging.bonus.history.cf.jndi}")
    private String connectionFactoryJndi;

    @Value("${ru.bpc.svat.mobilebank.phones.by.client.id.queue.request.jndi}")
    private String queueJndi;

    @Value("${ru.bpc.svat.mobilebank.messaging.jmsserver.url}")
    private String jmsServerUrl;

    @Value("${ru.bpc.svat.mobilebank.messaging.weblogic.user}")
    private String weblogicUser;

    @Value("${ru.bpc.svat.mobilebank.messaging.weblogic.password}")
    private String weblogicPassword;

    @PostConstruct
    protected void inform() {
        log.info("ru.bpc.svat.mobilebank.phones.by.client.id.cf.jndi = {}", connectionFactoryJndi);
        log.info("ru.bpc.svat.mobilebank.phones.by.client.id.queue.jndi = {}", queueJndi);
        log.info("ru.bpc.svat.mobilebank.phones.by.client.id.jmsserver.url = {}", jmsServerUrl);
    }

    @Bean
    public JndiTemplate phoneByClientIdJndiTemplate() {
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
    public JndiObjectFactoryBean phoneByClientIdConnectionFactory() {
        JndiObjectFactoryBean phoneByClientIdConnectionFactory = new JndiObjectFactoryBean();
        phoneByClientIdConnectionFactory.setJndiTemplate(phoneByClientIdJndiTemplate());
        phoneByClientIdConnectionFactory.setJndiName(connectionFactoryJndi);
        return phoneByClientIdConnectionFactory;
    }

    @Bean
    public JndiDestinationResolver jmsPhoneByClientIdDestinationResolver() {
        JndiDestinationResolver phoneByClientIdDestResolver = new JndiDestinationResolver();
        phoneByClientIdDestResolver.setJndiTemplate(phoneByClientIdJndiTemplate());
        phoneByClientIdDestResolver.setCache(true);
        return phoneByClientIdDestResolver;
    }

    @Bean
    public JndiObjectFactoryBean jmsPhoneByClientIdQueue() {
        JndiObjectFactoryBean jmsQueue = new JndiObjectFactoryBean();
        jmsQueue.setJndiTemplate(phoneByClientIdJndiTemplate());
        jmsQueue.setJndiName(queueJndi);
        return jmsQueue;
    }

    @Bean
    public MessageListener queuePhoneByClientIdListener() {
        return new GetPhoneByClientIdListener();
    }

    @Bean
    @Qualifier("jmsPhoneByClientIdTaskExecutor")
    public TaskExecutor taskPhoneByClientIdExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(5);
        return executor;
    }

    @Bean
    public DefaultMessageListenerContainer messagePhoneByClientIdListener(@Qualifier("jmsPhoneByClientIdTaskExecutor") TaskExecutor taskExecutor) {
        DefaultMessageListenerContainer listener = new DefaultMessageListenerContainer();
        listener.setTaskExecutor(taskExecutor);
        listener.setConnectionFactory((ConnectionFactory) phoneByClientIdConnectionFactory().getObject());
        listener.setDestination((Destination) jmsPhoneByClientIdQueue().getObject());
        listener.setMessageListener(queuePhoneByClientIdListener());

        return listener;
    }

}
