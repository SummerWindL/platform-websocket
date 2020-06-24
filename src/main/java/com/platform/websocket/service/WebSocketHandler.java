package com.platform.websocket.service;

import org.springframework.stereotype.Component;

/**
 * @program: middle-server
 * @description:
 * @author: fuyl
 * @create: 2020-06-24 10:55
 **/
@Component
public class WebSocketHandler {
    public void handler(IWebSocketService iWebSocketService ,String ...token){
        iWebSocketService.invokeCloseSocket(token);
    }
}
