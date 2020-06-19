package com.platform.websocket.demo;

import com.alibaba.fastjson.JSONObject;
import com.platform.websocket.manager.PlatformWebsocketManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

/**
 * Reference: https://docs.spring.io/spring/docs/current/spring-framework-reference/html/websocket.html
 * 仅供Websocket通道调用测试专用，无业务用途
 */
@Controller
public class DemoSendToUserControllerTest {

	private long sendCount = 0 ;     // 自启动以来，累计总发送topic的数量

	@Autowired
	PlatformWebsocketManager platformWebsocketManager;

//    @Scheduled(fixedDelay=5000)
	@Scheduled(fixedDelayString ="${middle.server.websocket.test-topic-scheduled-fixed-delay}")
    public void priceAutoConvert() throws Exception {

		this.sendCount++;

		//	模拟给指定 ssid 的患者发送消息	Topic
		JSONObject jsonObjectTopic = new JSONObject ();
		jsonObjectTopic.put("token", "ssid123");
		jsonObjectTopic.put("测试", "中文");
		jsonObjectTopic.put("flag", "topic");
		jsonObjectTopic.put("sendCount", sendCount);


		//	模拟给指定 ssid 的患者发送消息	Queue
		JSONObject jsonObjectQueue = new JSONObject ();
		jsonObjectQueue.put("token", "ssid123");
		jsonObjectQueue.put("测试", "中文");
		jsonObjectQueue.put("flag", "queue");
		jsonObjectTopic.put("sendCount", sendCount);

		PlatformWebsocketManager.sendTopicToSs("ssid123","/cmd123", jsonObjectTopic.toJSONString());
//		PlatformWebsocketManager.sendQueueToSs("ssid123","/cmd123", jsonObjectQueue.toJSONString());

    }
}
