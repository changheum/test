package com.samsungds.gpt.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.web.socket.WebSocketSession;

public class SessionMap {

	private static final Map<String, WebSocketSession> sessionMap = new HashMap<>();
	
	private SessionMap() {
	}
	
	public static Map<String, WebSocketSession> getInstance() {
		return sessionMap;
	}
	
}
