package com.samsungds.gpt.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.web.socket.WebSocketSession;

public class UserMap {
	
	private static final Map<String, String> userMap = new HashMap<>();

	private UserMap() {
	}
	
	public static Map<String, String> getInstance() {
		return userMap;
	}
}
