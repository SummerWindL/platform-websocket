package com.platform.websocket.config;

import com.platform.websocket.manager.PlatformWebsocketManager;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import javax.annotation.PostConstruct;

/**
 * @program: middle-server
 * @description:
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "middle.server.websocket")
public class PlatformWsConfig {

    private String endpoint = "/demo-zx-websocket";
    private int webSocketMessageBrokerStatsPeriod = 30;     //  默认打印SpringBoot Websocket Session统计数据的时间间隔，单位：秒

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    /**
     * 专用于处理竹信Websocket通道参数配置
     */
    @PostConstruct
    public void init() {
        PlatformWebsocketManager.simpMessagingTemplate = this.simpMessagingTemplate ;
    }

}
