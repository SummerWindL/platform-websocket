package com.platform.websocket.config;

import com.platform.websocket.manager.PlatformWebsocketManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

/**
 * 服务端和客户端在进行握手挥手时会被执行
 */
@Component
@Slf4j
public class PlatformWebsocketDecoratorFactory implements WebSocketHandlerDecoratorFactory {
    @Override
    public WebSocketHandler decorate(WebSocketHandler handler) {
        return new WebSocketHandlerDecorator(handler) {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                log.info("有人连接啦  sessionId = {}", session.getId());

                /*
                    第一版，直接用 session-id 作为 ssid
                Principal principal = session.getPrincipal();
                if (principal != null) {
                    log.info("key = {} 存入", principal.getName());
                    // 身份校验成功，缓存socket连接
                    PlatformWebsocketManager.add(principal.getName(), session);
                }
                 */
                PlatformWebsocketManager.addWsSession(session.getId(), session);   //  TODO 可以考虑再优雅优化一下

                super.afterConnectionEstablished(session);
            }

            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
                log.info("有人退出连接啦  sessionId = {}", session.getId());
                log.info("原因 closeStatus = {}", closeStatus.getReason());

                /*
                    第一版，直接用 session-id 作为 ssid
                Principal principal = session.getPrincipal();
                if (principal != null) {
                    // 身份校验成功，移除socket连接
                    PlatformWebsocketManager.close(principal.getName());
                }
                 */
                PlatformWebsocketManager.closeWsSession(session.getId());      //  TODO 可以考虑再优雅优化一下

                super.afterConnectionClosed(session, closeStatus);
            }
        };
    }
}
