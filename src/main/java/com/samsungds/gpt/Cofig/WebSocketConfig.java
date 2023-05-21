package com.samsungds.gpt.Cofig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.samsungds.gpt.service.SessionMap;
import com.samsungds.gpt.websocket.GptWebSocketHandler;


@Configuration
@EnableWebSocket// 웹소켓 활성화
public class WebSocketConfig implements WebSocketConfigurer{

	@Autowired
	private GptWebSocketHandler devLogWebSocketHandler;
	
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		// WebSocketHandler를 추가
		registry.addHandler(devLogWebSocketHandler, "/chating");
	}
	
}