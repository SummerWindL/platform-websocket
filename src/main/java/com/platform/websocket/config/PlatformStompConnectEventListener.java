package com.platform.websocket.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;

/**
 * STOMP监听类
 * 用于获取 spring-boot 的 websocket SimpMessagingTemplate ，并注入到 竹信专用Websocket管理器
 */
@Component
@Slf4j
public class PlatformStompConnectEventListener implements ApplicationListener<SessionConnectEvent> {

//    @Autowired
//    private SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void onApplicationEvent(SessionConnectEvent event) {

/*
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());

        //login get from browser
        String token = sha.getNativeHeader("token").get(0);
        String sessionId = sha.getSessionId();

        log.info("onApplicationEvent token = {} , session-id = {}", token , sessionId);
*/

//         TODO 改由 ZxWebsocketBrokerConfig 实现
//        PlatformWebsocketManager.simpMessagingTemplate = this.simpMessagingTemplate ;
    }
}
