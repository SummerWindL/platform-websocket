package com.platform.websocket.manager;

import com.platform.websocket.service.IWebSocketService;
import com.platform.websocket.service.WebSocketHandler;
import com.platform.websocket.spring.WebSocketSpringContextHolder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zx Websocket 管理器
 */
@Slf4j
@Component
@Data
public class PlatformWebsocketManager {

    public static final int WEBSOCKET_MSG_SEND_SUCCESS = 0;   //  Websocket消息发送成功
    public static final int WEBSOCKET_SESSION_OFFLINE = -1;   //  Websocket客户端离线

    private static IWebSocketService zxWebSocketService = null;

    private static WebSocketHandler zxWebSocketHandler = null;
    /**
     * 专用于Websocket Session管理
     * key = sessionId , value = Websocket session
     */
    private static ConcurrentHashMap<String, WebSocketSession> wsSessionManager = new ConcurrentHashMap<String, WebSocketSession>();

    /**
     * 专用于Websocket Session 和 竹信业务用户（医生和患者）token的管理
     * key = token , value = sessionId
     */
    private static ConcurrentHashMap<String, String> wsSessionTokenManager = new ConcurrentHashMap<String, String>();

    public static SimpMessagingTemplate simpMessagingTemplate;

    /**
     * 新增Websocket连接会话
     * @param sessionId
     * @param webSocketSession
     */
    public static synchronized void addWsSession(String sessionId, WebSocketSession webSocketSession) {
        if (wsSessionManager.containsKey(sessionId))
        {
            log.info("先关闭已有的Websocket连接会话，sessionId={}", sessionId);
            PlatformWebsocketManager.closeWsSession(sessionId);
        }

        log.info("新增Websocket连接会话，sessionId={}", sessionId);
        wsSessionManager.put(sessionId, webSocketSession);
    }

    /**
     * 获取指定Websocket连接会话
     * @param sessionId
     * @return
     */
    public static synchronized WebSocketSession getWsSession(String sessionId) {
        if (!wsSessionManager.containsKey(sessionId))
        {
            log.info("指定的Websocket连接会话不存在，sessionId={}", sessionId);
            return null;
        }

        WebSocketSession webSocketSession = wsSessionManager.get(sessionId);
        //  TODO 这里需要判断 webSocketSession 是否状态正常
        if (!webSocketSession.isOpen())
        {
            log.error("指定的Websocket连接会话状态异常，关闭重连，sessionId={}", sessionId);
            PlatformWebsocketManager.closeWsSession(sessionId);
            return null;
        }

        log.info("获取Websocket连接会话 session-id={}", webSocketSession.getId());
        return webSocketSession;
    }

    /**
     * 获取指定token对应的Websocket连接会话
     * 根据 token
     * @param token
     * @return
     */
    public static synchronized WebSocketSession getWsSessionByToken(String token) {
        if (!wsSessionTokenManager.containsKey(token))
        {
            log.info("指定的Websocket连接会话不存在，token={}", token);
            return null;
        }

        String sessionId = wsSessionTokenManager.get(token);
        return PlatformWebsocketManager.getWsSession(sessionId);
    }

    /**
     * 关闭Websocket连接会话
     * @param sessionId
     */
    public static synchronized void closeWsSession(String sessionId) {
        if (!wsSessionManager.containsKey(sessionId))
        {
            log.info("指定的Websocket连接会话不存在，sessionId={}", sessionId);
            return ;
        }

        WebSocketSession webSocketSession = wsSessionManager.get(sessionId);
        String token = (String)webSocketSession.getAttributes().get("token");
        if(zxWebSocketHandler == null){
            zxWebSocketHandler = WebSocketSpringContextHolder.getBean("webSocketHandler");
        }
        if(zxWebSocketService == null){
            //这个地方其他模块实现
            zxWebSocketService =  WebSocketSpringContextHolder.getBean("webSocketMonitorServiceImpl");
        }
        zxWebSocketHandler.handler(zxWebSocketService,token);

        if (null != token) {
            wsSessionTokenManager.remove(token);
        }
        //  TODO 这里需要判断 webSocketSession 是否状态正常
        if (webSocketSession.isOpen())
        {
            try {
                webSocketSession.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        log.info("关闭Websocket连接会话，sessionId={}", sessionId);
        wsSessionManager.remove(sessionId);
    }

    /**
     * 新增Websocket连接会话 和 token
     * @param token
     * @param sessionId
     */
    public static synchronized void addToken(String token, String sessionId) {
        if (wsSessionTokenManager.containsKey(token))
        {
            log.info("先关闭已有的Websocket连接会话，token={}", token);
            WebSocketSession webSocketSession = PlatformWebsocketManager.getWsSessionByToken(token);
            if (null != webSocketSession){
                PlatformWebsocketManager.closeWsSession(webSocketSession.getId());
            }
        }

        log.info("新增Websocket连接会话，token={}，session-id={}", token, sessionId);
        wsSessionTokenManager.put(token, sessionId);
    }

    /*********************************************
     *  发送消息部分
     *  V1版，2020.6.10，只支持发送文本JSON字符串
     ********************************************/
    /**
     * 专用于发送订阅消息 Topic
     * @param token
     * @param destination
     * @param jsonMsg
     * @return
     */
    private static synchronized int sendTopic(String token, String destination, String jsonMsg){

        //  判断Websocket客户端是否在线
        WebSocketSession webSocketSession = PlatformWebsocketManager.getWsSessionByToken(token);
        if (null == webSocketSession)
            return PlatformWebsocketManager.WEBSOCKET_SESSION_OFFLINE ;

        simpMessagingTemplate.convertAndSend(destination, jsonMsg);

/*
        try{
            webSocketSession.sendMessage(new WebSocketMessage (){
                @Override
                public Object getPayload() {
                    return "hello";
                }

                @Override
                public int getPayloadLength() {
                    return "hello".length();
                }

                @Override
                public boolean isLast() {
                    return true;
                }
            });
        }catch (IOException e) {
            e.printStackTrace();
            return PlatformWebsocketManager.WEBSOCKET_SESSION_OFFLINE ;
        }
*/

        return PlatformWebsocketManager.WEBSOCKET_MSG_SEND_SUCCESS ;
    }

    /**
     * 专用于发送队列消息 Queue
     * @param token
     * @param destination
     * @param jsonMsg
     * @return
     */
    private static synchronized int sendQueue(String token, String destination, String jsonMsg){

        //  判断Websocket客户端是否在线
        WebSocketSession webSocketSession = PlatformWebsocketManager.getWsSessionByToken(token);
        if (null == webSocketSession)
            return PlatformWebsocketManager.WEBSOCKET_SESSION_OFFLINE ;

        simpMessagingTemplate.convertAndSendToUser(token, destination, jsonMsg);

        return PlatformWebsocketManager.WEBSOCKET_MSG_SEND_SUCCESS ;
    }

    /**
     * 专用于医生给患者发送消息
     * @param ssToken
     * @param cmdno
     * @param jsonMsg
     * @return
     */
    public static synchronized int sendTopicToSs(String ssToken, String cmdno, String jsonMsg) {
        return PlatformWebsocketManager.sendTopic(ssToken, cmdno, jsonMsg);
    }

    public static synchronized int sendQueueToSs(String ssToken, String cmdno, String jsonMsg) {
        return PlatformWebsocketManager.sendQueue(ssToken, cmdno, jsonMsg);
    }

    /**
     * 专用于患者给医生发送消息
     * @param doctorToken
     * @param cmdno
     * @param jsonMsg
     * @return
     */
    public static synchronized int sendTopicToDoctor(String doctorToken, String cmdno, String jsonMsg) {
        return PlatformWebsocketManager.sendTopic(doctorToken, cmdno, jsonMsg);
    }

    public static synchronized int sendQueueToDoctor(String doctorToken, String cmdno, String jsonMsg) {
        return PlatformWebsocketManager.sendQueue(doctorToken, cmdno, jsonMsg);
    }
}
