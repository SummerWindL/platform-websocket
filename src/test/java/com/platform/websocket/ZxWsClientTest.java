package com.platform.websocket;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ZxWsClientTest {

    private SimpleDateFormat dateFormat = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");

    //连接数
    public static int connectNum = 0;
    //连接成功数
    public static int successNum = 0;
    //连接失败数
    public static int errorNum = 0;

    /**
     * 测试websocket最大连接数
     * @throws InterruptedException
     */
    @Test
    public void testConnect() throws InterruptedException {
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //每次3秒打印一次连接结果
                    log.debug(dateFormat.format(System.currentTimeMillis()) +
                            "  连接数：{}，成功数：{}，失败数：{}",
                            connectNum ,
                            successNum ,
                            errorNum);
                }
            }
        }.start();
        List<WebSocketStompClient> list = new ArrayList<>();
        log.debug("开始时间：{}", dateFormat.format(System.currentTimeMillis()));
        while (true) {
            //连接失败超过10次，停止测试
            if(errorNum > 10){
                break;
            }
            list.add(newConnect(++connectNum));
            Thread.sleep(10);
        }
    }

    /**
     * 创建websocket连接
     * @param i
     * @return
     */
    private WebSocketStompClient newConnect(int i) {
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        WebSocketClient socketClient = new SockJsClient(transports);
        WebSocketStompClient stompClient = new WebSocketStompClient(socketClient);

        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.afterPropertiesSet();
        stompClient.setTaskScheduler(taskScheduler);

        String clientToken = String.valueOf(i);
        WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("token", clientToken);

//        String url = "http://localhost:8076/demo-zx-websocket-midserver?token=" + clientToken;
        String url = "http://192.168.9.54:8089/demo-zx-websocket-midserver?token=" + clientToken;
//        String url = "http://192.168.9.201:8089/demo-zx-websocket-midserver?token=" + clientToken;
        stompClient.connect(url, handshakeHeaders, connectHeaders, new TestConnectHandler());
        return stompClient;
    }

    private static synchronized void addSuccessNum() {
        successNum++;
    }

    private static synchronized void addErrorNum() {
        errorNum++;
    }

    private static class TestConnectHandler extends StompSessionHandlerAdapter {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            addSuccessNum();
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            addErrorNum();
        }
    }

}
