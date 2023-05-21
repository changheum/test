package com.samsungds.gpt.Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samsungds.gpt.service.SessionMap;
import com.samsungds.gpt.service.UserMap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@RestController
public class GptRestController {

	Map<String, WebSocketSession> sessionMap = SessionMap.getInstance();
//	Map<String, WebSocketSession> sessionMap = new HashMap<>(); //웹소켓 세션을 담아둘 맵
	
	Map<String, String> userMap = UserMap.getInstance();
//	Map<String, String> userMap = new HashMap<>();	//사용자
	
	@RequestMapping(value = "/test", method = RequestMethod.GET)
	public String requestRestAPI(Model model) throws Exception {

		String urlString = "https://chatgpt-public.openai.azure.com/openai/deployments/gpt-35-turbo/chat/completions?api-version=2023-03-15-preview";
		
		String line = null;
		InputStream in = null;
		BufferedReader reader = null; 
		HttpsURLConnection httpsConn = null;
		
		String Answer="";
		
		try {
			// Get HTTPS URL connection
			URL url = new URL(urlString);
			httpsConn = (HttpsURLConnection) url.openConnection();
			
			// Set Hostname verification
			httpsConn.setHostnameVerifier(new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					// Ignore host name verification. It always returns true.
					return true;
				}
			});
			
			// Input setting
			httpsConn.setDoInput(true);
			// Output setting
			//httpsConn.setDoOutput(true);
			// Caches setting
			httpsConn.setUseCaches(false);
			// Read Timeout Setting
			httpsConn.setReadTimeout(5000);
			// Connection Timeout setting
			httpsConn.setConnectTimeout(5000);
			// Method Setting(GET/POST)
			httpsConn.setRequestMethod("POST");
			// Header Setting
			httpsConn.setRequestProperty("Content-Type","application/json");
			httpsConn.setRequestProperty("api-key","0d1ca2f8c99441f790f65789e1ae7a64");
			
			httpsConn.setDoOutput(true);
			
			String ParamData = "{\r\n"
					+ "  \"messages\": [\r\n"
					+ "    {\r\n"
					+ "      \"role\": \"system\",\r\n"
					+ "      \"content\": \"You are an AI assistant that helps people find information.\"\r\n"
					+ "    },\r\n"
					+ "    {\r\n"
					+ "      \"role\": \"user\",\r\n"
					+ "      \"content\": \"hi\"\r\n"
					+ "    },\r\n"
					+ "    {\r\n"
					+ "      \"role\": \"assistant\",\r\n"
					+ "      \"content\": \"Hello! How can I assist you today?\"\r\n"
					+ "    },\r\n"
					+ "    {\r\n"
					+ "      \"role\": \"user\",\r\n"
					+ "      \"content\": \"nice\"\r\n"
					+ "    },\r\n"
					+ "    {\r\n"
					+ "      \"role\": \"assistant\",\r\n"
					+ "      \"content\": \"Thank you! Is there anything you need help with?\"\r\n"
					+ "    }\r\n"
					+ "  ],\r\n"
					+ "  \"temperature\": 0.7,\r\n"
					+ "  \"top_p\": 0.95,\r\n"
					+ "  \"frequency_penalty\": 0,\r\n"
					+ "  \"presence_penalty\": 0,\r\n"
					+ "  \"max_tokens\": 800,\r\n"
					+ "  \"stop\": null\r\n"
					+ "}";
			
			System.out.println(ParamData);
			
			try (OutputStream os = httpsConn.getOutputStream()){
				byte request_data[] = ParamData.getBytes("utf-8");
				os.write(request_data);
				os.close();
			}
			
			
			int responseCode = httpsConn.getResponseCode();
			System.out.println("응답코드 : " + responseCode);
			System.out.println("응답메세지 : " + httpsConn.getResponseMessage());
			
			// SSL setting
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, null, null); // No validation for now
			httpsConn.setSSLSocketFactory(context.getSocketFactory());
			
			// Connect to host
			httpsConn.connect();
			httpsConn.setInstanceFollowRedirects(true);
			
			// Print response from host
			if (responseCode == HttpsURLConnection.HTTP_OK) { // 정상 호출 200
				in = httpsConn.getInputStream();
			} else { // 에러 발생
				in = httpsConn.getErrorStream();
			}
			reader = new BufferedReader(new InputStreamReader(in));
			while ((line = reader.readLine()) != null) {
				Answer = line;
				System.out.println(line);
			}
			
			reader.close();
			
			for(String key : sessionMap.keySet()) {
				WebSocketSession wss = sessionMap.get(key);
				
				if(userMap.get(wss.getId()) == null) {
					userMap.put(wss.getId(), "Server");
				}
				
				//클라이언트에게 메시지 전달
				wss.sendMessage(new TextMessage(Answer));
			}
			
		} catch (UnknownHostException e) {
			System.out.println("UnknownHostException : " + e);
		} catch (MalformedURLException e) {
			System.out.println(urlString + " is not a URL I understand");
        } catch (IOException e) {
        	System.out.println("IOException :" + e);
        } catch (Exception e) {
        	System.out.println("error : " + e);
        } finally {
            if (reader != null) {
            	reader.close();
            }
            if (httpsConn != null) {
                httpsConn.disconnect(); 
            }
        }
		return Answer;
	}
}