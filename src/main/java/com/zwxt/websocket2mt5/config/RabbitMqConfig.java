package com.zwxt.websocket2mt5.config;

import lombok.Data;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 *Broker:它提供一种传输服务,它的角色就是维护一条从生产者到消费者的路线，保证数据能按照指定的方式进行传输,
 *Exchange：消息交换机,它指定消息按什么规则,路由到哪个队列。
 *Queue:消息的载体,每个消息都会被投到一个或多个队列。
 *Binding:绑定，它的作用就是把exchange和queue按照路由规则绑定起来.
 *Routing Key:路由关键字,exchange根据这个关键字进行消息投递。
 *vhost:虚拟主机,一个broker里可以有多个vhost，用作不同用户的权限分离。
 *Producer:消息生产者,就是投递消息的程序.
 *Consumer:消息消费者,就是接受消息的程序.
 *Channel:消息通道,在客户端的每个连接里,可建立多个channel.
 */
@Configuration
@ConfigurationProperties(prefix = "spring.rabbitmq")
@Data
public class RabbitMqConfig {
    private String host;
    private int port;
    private String userName;
    private String password;

    //消息交换机
    public static final String EXCHANGE_QUEUE_PARAMETER = "exchange2QueueParameter";
    public static final String EXCHANGE_QUEUE_MESSAGE = "exchange2QueueMessage";
    //队列
    public static final String QUEUE_PARAMETER = "QueueParameter";
    public static final String QUEUE_MESSAGE = "QueueMessage";
    //路由关键字,exchange根据这个关键字进行消息投递
    public static final String ROUTINGKEY_QUEUE_PARAMETER = "QueueParameter";
    public static final String ROUTINGKEY_QUEUE_MESSAGE = "QueueMessage";


    @Bean
    public ConnectionFactory connectionFactory(){
        CachingConnectionFactory factory = new CachingConnectionFactory(); //CachingConnectionFactory RabbitMQ的缓存连接池
        factory.setHost(host);
        factory.setPort(port);
        factory.setVirtualHost("/");
        factory.setUsername(userName);
        factory.setPassword(password);
        factory.setPublisherConfirms(true);
        return factory;
    }


    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    //必须是prototype类型
    public RabbitTemplate rabbitTemplate(){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        return rabbitTemplate;
    }




}
