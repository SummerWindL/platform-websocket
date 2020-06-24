package com.platform.websocket.service;

/**
 * @program: middle-server
 * @description:
 * @author: fuyl
 * @create: 2020-06-23 20:43
 **/
public interface IWebSocketService {
    void invokeCloseSocket(String... token);
}
