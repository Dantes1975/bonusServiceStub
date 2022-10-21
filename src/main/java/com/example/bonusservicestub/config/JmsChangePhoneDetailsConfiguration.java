package com.example.bonusservicestub.config;

import com.example.bonusservicestub.listener.ChangePhoneDetailsListener;
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
public class JmsChangePhoneDetailsConfiguration {

    @Value("${ru.bpc.svat.mobilebank.messaging.bonus.history.cf.jndi}")
    private String connectionFactoryJndi;

    @Value("${ru.bpc.svat.mobilebank.change.phone.details.queue.request.jndi}")
    private String queueJndi;

    @Value("${ru.bpc.svat.mobilebank.messaging.jmsserver.url}")
    private String jmsServerUrl;

    @Value("${ru.bpc.svat.mobilebank.messaging.weblogic.user}")
    private String weblogicUser;

    @Value("${ru.bpc.svat.mobilebank.messaging.weblogic.password}")
    private String weblogicPassword;

    @PostConstruct
    protected void inform() {
        log.info("change.phone.details.cf.jndi = {}", connectionFactoryJndi);
        log.info("change.phone.details.queue.jndi = {}", queueJndi);
        log.info("change.phone.details.jmsserver.url = {}", jmsServerUrl);
    }

    @Bean
    public JndiTemplate changePhoneDetailsJndiTemplate() {
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
    public JndiObjectFactoryBean changePhoneDetailsConnectionFactory() {
        JndiObjectFactoryBean changePhoneDetailsConnectionFactory = new JndiObjectFactoryBean();
        changePhoneDetailsConnectionFactory.setJndiTemplate(changePhoneDetailsJndiTemplate());
        changePhoneDetailsConnectionFactory.setJndiName(connectionFactoryJndi);
        return changePhoneDetailsConnectionFactory;
    }

    @Bean
    public JndiDestinationResolver jmsChangePhoneDetailsDestinationResolver() {
        JndiDestinationResolver changePhoneDetailsDestResolver = new JndiDestinationResolver();
        changePhoneDetailsDestResolver.setJndiTemplate(changePhoneDetailsJndiTemplate());
        changePhoneDetailsDestResolver.setCache(true);
        return changePhoneDetailsDestResolver;
    }

    @Bean
    public JndiObjectFactoryBean jmsChangePhoneDetailsQueue() {
        JndiObjectFactoryBean jmsQueue = new JndiObjectFactoryBean();
        jmsQueue.setJndiTemplate(changePhoneDetailsJndiTemplate());
        jmsQueue.setJndiName(queueJndi);
        return jmsQueue;
    }

    @Bean
    public MessageListener queueChangePhoneDetailsListener() {
        return new ChangePhoneDetailsListener();
    }

    @Bean
    @Qualifier("jmsChangePhoneDetailsTaskExecutor")
    public TaskExecutor taskChangePhoneDetailsExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(5);
        return executor;
    }

    @Bean
    public DefaultMessageListenerContainer messageChangePhoneDetailsListener(@Qualifier("jmsChangePhoneDetailsTaskExecutor") TaskExecutor taskExecutor) {
        DefaultMessageListenerContainer listener = new DefaultMessageListenerContainer();
        listener.setTaskExecutor(taskExecutor);
        listener.setConnectionFactory((ConnectionFactory) changePhoneDetailsConnectionFactory().getObject());
        listener.setDestination((Destination) jmsChangePhoneDetailsQueue().getObject());
        listener.setMessageListener(queueChangePhoneDetailsListener());

        return listener;
    }

}
