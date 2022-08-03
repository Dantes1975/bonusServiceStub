package com.example.bonusservicestub.config;

import com.example.bonusservicestub.listener.BonusMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
public class JmsRequestConfiguration {
    @Value("${ru.bpc.svat.mobilebank.messaging.bonus.details.jndi}")
    private String connectionFactoryJndi;

    @Value("${ru.bpc.svat.mobilebank.messaging.queue.request.jndi}")
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
    public JndiTemplate jndiTemplate() {
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
    @Primary
    public JndiObjectFactoryBean queueConnectionFactory() {
        JndiObjectFactoryBean queueConnectionFactory = new JndiObjectFactoryBean();
        queueConnectionFactory.setJndiTemplate(jndiTemplate());
        queueConnectionFactory.setJndiName(connectionFactoryJndi);
        return queueConnectionFactory;
    }

    @Bean
    public JndiDestinationResolver jmsDestinationResolver() {
        JndiDestinationResolver destResolver = new JndiDestinationResolver();
        destResolver.setJndiTemplate(jndiTemplate());
        destResolver.setCache(true);
        return destResolver;
    }

    @Bean
    public JndiObjectFactoryBean jmsQueue() {
        JndiObjectFactoryBean jmsQueue = new JndiObjectFactoryBean();
        jmsQueue.setJndiTemplate(jndiTemplate());
        jmsQueue.setJndiName(queueJndi);
        return jmsQueue;
    }

    @Bean
    public MessageListener queueListener() {
        return new BonusMessageListener();
    }

    @Bean
    @Qualifier("jmsTaskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(5);
        return executor;
    }

    @Bean
    public DefaultMessageListenerContainer messageListener(@Qualifier("jmsTaskExecutor") TaskExecutor taskExecutor) {
        DefaultMessageListenerContainer listener = new DefaultMessageListenerContainer();
        listener.setTaskExecutor(taskExecutor);
        listener.setConnectionFactory((ConnectionFactory) queueConnectionFactory().getObject());
        listener.setDestination((Destination) jmsQueue().getObject());
        listener.setMessageListener(queueListener());

        return listener;
    }
}
