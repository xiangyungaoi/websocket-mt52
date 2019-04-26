package com.zwxt.websocket2mt5.util.mq;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class Send2Mq {


    public static void sendObject2Mq(String info, String exchange, String routingkey, RabbitTemplate rabbitTemplate){
            rabbitTemplate.convertAndSend(exchange, routingkey, info, new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    //设置消息的属性message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT)可以让消息持久化,Springboot默认就是持久化了，可以省略不写
                    message.getMessageProperties().setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN); //持久化消息并且以String的方式保存
                    return message;
                }
            });

    }
}
