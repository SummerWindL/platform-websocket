package com.platform.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @program: platform-websocket
 * @description:
 * @author: fuyl
 * @create: 2020-06-19 17:20
 **/
@SpringBootApplication
@EnableScheduling
public class PlatformWebsocketApplication {
    public static void main(String[] args) {
        SpringApplication.run(PlatformWebsocketApplication.class);
    }
}
