package com.zwxt.websocket2mt5.service;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.*;
import com.zwxt.websocket2mt5.config.RabbitMqConfig;
import com.zwxt.websocket2mt5.util.mq.Send2Mq;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket是类似客户端服务端的形式(采用ws协议)，
 * 那么这里的WebSocketServer其实就相当于一个ws协议的Controller
 * websocket建立一次连接就会new一个WebSocketToApp2对象,所以这里是多例的
 */
@Data
@Component
@ServerEndpoint(value = "/websocket/c")
public class WebSocketToApp2 {
    static Logger log = LoggerFactory.getLogger(WebSocketToApp2.class);
    // 静态变量，用来记录当前在线连接数.应该把它设计成线程安全的.
    private static int onlineCount = 0;
    // concurrent包的线程安全Set,用来存放每个客户端对应的WebSocketToApp2
    private static ConcurrentHashMap<String, WebSocketToApp2> webSocketToApp2S = new ConcurrentHashMap<>();
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    //用来存放请求传递过来的参数,判断当前拿到的mq消息要不要消费
    private  String uId;
    @Autowired
    private static RabbitTemplate rabbitTemplate;
    @Autowired
    public WebSocketToApp2(RabbitTemplate rabbitTemplate){
        this.rabbitTemplate = rabbitTemplate;
    }
    public WebSocketToApp2() { }

    /**连接建立成功调用的方法
     * @param session hhh
     *
     */
    @OnOpen
    public void onOpent(Session session){
        this.session = session;
        /*this.sid = sid;*/
        /*webSocketToApp2S.put(sid,this);//this是每次请求的时候，创建的WebSocketToApp2对象*/
        addOnlineCount();//连接数量+1
        log.info("前端连接后台WebSocket服务成功");
        try {
            sendMessage("连接成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    * 连接关闭调用的方法*/
    @OnClose
    public void onClose(){
        webSocketToApp2S.remove(uId);//将当前连接的websocket对象从集合中删除，让连接数正确
        subOnlineCount();           //在线数减1
        log.info("有一连接关闭！当前连接数为" + getOnlineCount());
    }

    /**收到客户端消息后调用的方法
     * @param message 客户端发送过来的消息
     * @param session
     */
    @OnMessage
    public void onMessage(String message, Session session){
        Map map = (Map) JSON.parseObject(message);
        uId = (String) map.get("reqid");
        log.info("收到来着用户:" + uId + "的请求参数:" + message);
        webSocketToApp2S.put(uId,this);

        Send2Mq.sendObject2Mq(message, RabbitMqConfig.EXCHANGE_QUEUE_PARAMETER,
                RabbitMqConfig.ROUTINGKEY_QUEUE_PARAMETER, rabbitTemplate);
        System.out.println("发送完成");
    }

    @OnError
    public void onError(Session session, Throwable error){
        log.error("发生错误");
        error.printStackTrace();
    }

    /**
     *  从mq获取到的消息
     *  需要解决的问题,拿到的消息要不要消费的问题:因为mq中的消息是多个客户端发送请求得到数据存到在mq中的，
     *            这时候监听拿到的消息可能是其他客户端的数据.要判断数据是否是当前客户端的数据，是就消费，不是将消息放回去
     * @param msg :解码后的消息
     *
     *
     */
    @RabbitListener(queues = RabbitMqConfig.QUEUE_MESSAGE )
    public void getMessageFromMqToApp(String msg, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag){
        try {
         /*   Map mt5ResultMap = (Map) JSON.parseObject(msg);
            //从mq中取出消息,返回给前端'
             String uIdFromMt5 = (String) mt5ResultMap.get("reqid");
            log.info("从mq中接受到的数据:" + msg);
            webSocketToApp2S.get(uIdFromMt5).sendMessage(msg);*/
            channel.basicAck(tag, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**服务器向客户端主动推送消息
     * @param message 消息
     */
    public void sendMessage(String message) throws IOException {
        if (this.session != null){
            this.session.getBasicRemote().sendText(message);
        }

    }
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketToApp2.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketToApp2.onlineCount--;
    }
}

