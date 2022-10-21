package com.example.bonusservicestub.config;

import com.example.bonusservicestub.listener.CardAdditionPhoneListener;
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
public class JmsCardAdditionPhoneConfiguration {

    @Value("${ru.bpc.svat.mobilebank.messaging.bonus.history.cf.jndi}")
    private String connectionFactoryJndi;

    @Value("${ru.bpc.svat.mobilebank.products.by.phone.queue.request.jndi}")
    private String queueJndi;

    @Value("${ru.bpc.svat.mobilebank.messaging.jmsserver.url}")
    private String jmsServerUrl;

    @Value("${ru.bpc.svat.mobilebank.messaging.weblogic.user}")
    private String weblogicUser;

    @Value("${ru.bpc.svat.mobilebank.messaging.weblogic.password}")
    private String weblogicPassword;

    @PostConstruct
    protected void inform() {
        log.info("product.by.phone.cf.jndi = {}", connectionFactoryJndi);
        log.info("product.by.phone.queue.jndi = {}", queueJndi);
        log.info("product.by.phone.jmsserver.url = {}", jmsServerUrl);
    }

    @Bean
    public JndiTemplate cardAdditionPhoneJndiTemplate() {
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
    public JndiObjectFactoryBean cardAdditionPhoneConnectionFactory() {
        JndiObjectFactoryBean cardAdditionPhoneConnectionFactory = new JndiObjectFactoryBean();
        cardAdditionPhoneConnectionFactory.setJndiTemplate(cardAdditionPhoneJndiTemplate());
        cardAdditionPhoneConnectionFactory.setJndiName(connectionFactoryJndi);
        return cardAdditionPhoneConnectionFactory;
    }

    @Bean
    public JndiDestinationResolver jmsCardAdditionPhoneDestinationResolver() {
        JndiDestinationResolver cardAdditionPhoneDestResolver = new JndiDestinationResolver();
        cardAdditionPhoneDestResolver.setJndiTemplate(cardAdditionPhoneJndiTemplate());
        cardAdditionPhoneDestResolver.setCache(true);
        return cardAdditionPhoneDestResolver;
    }

    @Bean
    public JndiObjectFactoryBean jmsCardAdditionPhoneQueue() {
        JndiObjectFactoryBean jmsQueue = new JndiObjectFactoryBean();
        jmsQueue.setJndiTemplate(cardAdditionPhoneJndiTemplate());
        jmsQueue.setJndiName(queueJndi);
        return jmsQueue;
    }

    @Bean
    public MessageListener queueCardAdditionPhoneListener() {
        return new CardAdditionPhoneListener();
    }

    @Bean
    @Qualifier("jmsCardAdditionPhoneTaskExecutor")
    public TaskExecutor taskCardAdditionPhoneExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(5);
        return executor;
    }

    @Bean
    public DefaultMessageListenerContainer messageCardAdditionPhoneListener(@Qualifier("jmsCardAdditionPhoneTaskExecutor") TaskExecutor taskExecutor) {
        DefaultMessageListenerContainer listener = new DefaultMessageListenerContainer();
        listener.setTaskExecutor(taskExecutor);
        listener.setConnectionFactory((ConnectionFactory) cardAdditionPhoneConnectionFactory().getObject());
        listener.setDestination((Destination) jmsCardAdditionPhoneQueue().getObject());
        listener.setMessageListener(queueCardAdditionPhoneListener());

        return listener;
    }

}
