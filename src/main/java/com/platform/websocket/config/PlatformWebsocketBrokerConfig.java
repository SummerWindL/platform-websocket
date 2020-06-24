package com.platform.websocket.config;

import com.platform.websocket.manager.PlatformWebsocketManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.config.SimpleBrokerRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.WebSocketMessageBrokerStats;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

/**
 * Configuration to enable websocket 
 *
 */
@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class PlatformWebsocketBrokerConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private PlatformWsConfig platformWsConfig;
    @Autowired
    private PlatformWebsocketDecoratorFactory webSocketDecoratorFactory;
    @Autowired
    private PlatformPrincipalHandshakeHandler principalHandshakeHandler;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        //  客户端接收（订阅）消息的地址
        SimpleBrokerRegistration simpleBrokerRegistration = config.enableSimpleBroker("/");

        //  客户端提交消息的地址
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(this.platformWsConfig.getEndpoint())  //  websocket 地址，需从spring-boot配置文件读取
                .setAllowedOrigins("*")             //  允许js跨域访问
                .setHandshakeHandler(principalHandshakeHandler)     //  处理客户端请求的 token
                .withSockJS();      //  兼容老浏览器不支持 websocket 的情况
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.addDecoratorFactory(webSocketDecoratorFactory);    //  激活 Websocket Session 管理
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new PlatformChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String token = accessor.getNativeHeader("token").get(0) ; // access authentication header(s)
                    String sessionId = accessor.getSessionId();
                    log.info("configureClientInboundChannel -> token = {}", token);
                    log.info("configureClientInboundChannel -> getSessionId = {}", sessionId);

                    WebSocketSession webSocketSession = PlatformWebsocketManager.getWsSession(sessionId);
                    webSocketSession.getAttributes().put("token", token);     //  缓存 token 和 Websocket session-id 的关系

                    PlatformWebsocketManager.addToken(token, sessionId);
                }
                return message;
            }
        });
    }

    /**
     * 专用于处理Websocket通道参数配置
     */
//    @PostConstruct
    public void init() {
    }

    @Bean
    public BeanPostProcessor beanPostProcessor() {
        return new BeanPostProcessor() {

            @Autowired
            private PlatformWsConfig platformWsConfig;

            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof WebSocketMessageBrokerStats) {
                    WebSocketMessageBrokerStats webSocketMessageBrokerStats = (WebSocketMessageBrokerStats) bean;
                    webSocketMessageBrokerStats.setLoggingPeriod(this.platformWsConfig.getWebSocketMessageBrokerStatsPeriod() * 1000); // 自定义SpringBoot打印Websocket session统计数据，时间间隔，单位：秒
                }
                return bean;
            }

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                return bean;
            }
        };
    }
}