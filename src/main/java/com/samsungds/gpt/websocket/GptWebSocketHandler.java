package com.samsungds.gpt.websocket;

// https://fvor001.tistory.com/123

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.samsungds.gpt.service.SessionMap;
import com.samsungds.gpt.service.UserMap;

@Component
public class GptWebSocketHandler extends TextWebSocketHandler{
	
	Map<String, WebSocketSession> sessionMap = SessionMap.getInstance();
//	Map<String, WebSocketSession> sessionMap = new HashMap<>(); //웹소켓 세션을 담아둘 맵
	
	Map<String, String> userMap = UserMap.getInstance();
//	Map<String, String> userMap = new HashMap<>();	//사용자
	
	/* 클라이언트로부터 메시지 수신시 동작 */
	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		String msg = message.getPayload();
		System.out.println("===============Message=================");
		System.out.println(msg);
		System.out.println("===============Message=================");
		
		JSONObject obj = jsonToObjectParser(msg);
		//로그인된 Member (afterConnectionEstablished 메소드에서 session을 저장함)
		for(String key : sessionMap.keySet()) {
			WebSocketSession wss = sessionMap.get(key);
			
			if(userMap.get(wss.getId()) == null) {
				userMap.put(wss.getId(), (String)obj.get("userName"));
			}
			
			//클라이언트에게 메시지 전달
			wss.sendMessage(new TextMessage(obj.toJSONString()));
		}
	}
	
	/* 클라이언트가 소켓 연결시 동작 */
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		System.out.println(session.getId()+" 연결되었습니다.");
		super.afterConnectionEstablished(session);
		sessionMap.put(session.getId(), session);
		
		JSONObject obj = new JSONObject();
		obj.put("type", "getId");
		obj.put("sessionId", session.getId());
        
        //클라이언트에게 메세지 전달
		session.sendMessage(new TextMessage(obj.toJSONString()));
	}
	
	/* 클라이언트가 소켓 종료시 동작 */
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		System.out.println(session.getId()+" 연결이 종료되었습니다.");
		super.afterConnectionClosed(session, status);
		sessionMap.remove(session.getId());
		
		String userName = userMap.get(session.getId());
		for(String key : sessionMap.keySet()) {
			WebSocketSession wss = sessionMap.get(key);
			
			if(wss == session) continue;

			JSONObject obj = new JSONObject();
			obj.put("type", "close");
			obj.put("userName", userName);
			
			wss.sendMessage(new TextMessage(obj.toJSONString()));
		}
		userMap.remove(session.getId());
	}
	
	/**
	 * JSON 형태의 문자열을 JSONObejct로 파싱
	 */
	private static JSONObject jsonToObjectParser(String jsonStr) throws Exception{
		JSONParser parser = new JSONParser();
		JSONObject obj = null;
		obj = (JSONObject) parser.parse(jsonStr);
		return obj;
	}
}