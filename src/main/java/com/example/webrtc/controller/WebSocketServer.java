package com.example.webrtc.controller;

import com.example.webrtc.SocketMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Controller
@ServerEndpoint(value = "/websocket/{userId}")
@Component
public class WebSocketServer {
    //用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<WebSocketServer>();
    private static Map<String,Session> userMap = new HashMap<String, Session>();
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    private String userId;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        this.session = session;
        this.userId = userId;
        userMap.put(session.getId(), session);
        webSocketSet.add(this);     //加入set中
        System.out.println("有新連接加入！線上人數為: " + webSocketSet.size());
        this.session.getAsyncRemote().sendText("WebSocket Connected: " + userId + "-->Channel: " + session.getId());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);  //从set中删除
        System.out.println("有一连接关闭！当前在线人数为" + webSocketSet.size());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") String userId) {
        System.out.println("From client ----> " + userId + ": " + message);
        //私聊
        ObjectMapper objectMapper = new ObjectMapper();
        SocketMessage socketMessage;
        try {
            socketMessage = objectMapper.readValue(message, SocketMessage.class);
            if (socketMessage.getType() == 1) {
                socketMessage.setSender(session.getId());
                Session senderSession = userMap.get(socketMessage.getSender());
                Session receiverSession = userMap.get(socketMessage.getReceiver());

                if(receiverSession != null) {
                    senderSession.getAsyncRemote().sendText(userId + ": " + socketMessage.getMessage());
                    receiverSession.getAsyncRemote().sendText(userId + ": " + socketMessage.getMessage());
                } else {
                    senderSession.getAsyncRemote().sendText("System info: user is offline or wrong channel ID");
                }
            } else {
                //群聊
                broadcast(userId+": "+socketMessage.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
        error.printStackTrace();
    }

    /**
     * 群发自定义消息
     */
    public void broadcast(String message) {
        for (WebSocketServer item : webSocketSet) {
            //同步异步说明参考：http://blog.csdn.net/who_is_xiaoming/article/details/53287691
            //this.session.getBasicRemote().sendText(message);
            item.session.getAsyncRemote().sendText(message);//异步发送消息.
        }
    }
}


