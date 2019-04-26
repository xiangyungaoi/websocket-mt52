package com.zwxt.websocket2mt5.controller;

import com.rabbitmq.client.Channel;
import com.zwxt.websocket2mt5.config.RabbitMqConfig;
import lombok.Data;
import org.java_websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Data
@Component
public class WebSocketClientToMt5 {
    static Logger log = LoggerFactory.getLogger(WebSocketClientToMt5.class);
    @Autowired
    WebSocketClient webSocketClient;
    @Autowired
    public  RabbitTemplate rabbitTemplate;
  /*  @Autowired
    public WebSocketClientToMt5(RabbitTemplate rabbitTemplate){
        this.rabbitTemplate = rabbitTemplate;
    }
    public WebSocketClientToMt5() { }*/


    /**从mq中获取到前端传递的参数,并想mt5发送websocket请求
     * @param msg  从mq中获取到的消息
     * @param channel 与mq连接的信道
     * @param tag  交付标签，用来确定 手动确认消息
     */
    @RabbitListener(queues = RabbitMqConfig.QUEUE_PARAMETER)
    public void getParameterFromMq(String msg, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag){
        try {
            log.info("从mq的请求参数队列中接受到的请求参数:" + msg);
            //使用websocket想mt5发送消息
            webSocketClient.send(msg);
            //手动应答
            channel.basicAck(tag, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }












}
