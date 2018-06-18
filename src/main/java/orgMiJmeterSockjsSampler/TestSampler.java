package orgMiJmeterSockjsSampler;

import java.net.URI;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.tomcat.websocket.Constants;
import org.apache.tomcat.websocket.WsWebSocketContainer;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import javax.net.ssl.TrustManager;

public class TestSampler {
	
	private String transport = "websocket";
    private String host = "https://xxx.com";
    private String path = "/xxx/";
    private long connectionTime = 500;
    private long responseBufferTime = 10000;
    private String connectionHeadersLogin = "login:xxx";
    private String connectionHeadersPasscode = "passcode:xxx";
    private String connectionHeadersHost = "host:xxx";
    private String connectionHeadersAcceptVersion = "accept-version:1.1,1.0";
    private String connectionHeadersHeartbeat = "heart-beat:0,0";
    private String subscribeHeadersId = "id:sub-x";
    private String subscribeHeadersDestination = "destination:/exchange/xxx/xxx";
		
	/*
    private String transport = "websocket";
    private String host = "https://wbc-service.movingimage.com";
    private String path = "/stomp";
    private long connectionTime = 500;
    private long responseBufferTime = 10000;
    private String connectionHeadersLogin = "login:wbc-consumer-prod";
    private String connectionHeadersPasscode = "passcode:wbc-consumer";
    private String connectionHeadersHost = "host:webcast_prod";
    private String connectionHeadersAcceptVersion = "accept-version:1.1,1.0";
    private String connectionHeadersHeartbeat = "heart-beat:0,0";
    private String subscribeHeadersId = "id:sub-0";
    private String subscribeHeadersDestination = "destination:/exchange/question-consumer/wbc_59dba188340925066669f7fa";
    */
	
	public static void main(String[] args)
    {
		TestSampler test = new TestSampler();
		ResponseMessage responseMessage = new ResponseMessage();
		
		try {
			test.createWebsocketConnection(responseMessage);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	// Websocket test
	public void createWebsocketConnection(ResponseMessage responseMessage) throws Exception {
		StandardWebSocketClient simpleWebSocketClient = new StandardWebSocketClient();
		
		// set up a TrustManager that trusts everything
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, new TrustManager[] { new BlindTrustManager() }, null);
		Map<String, Object> userProperties = new HashMap<>();
		userProperties.put(Constants.SSL_CONTEXT_PROPERTY, sslContext);
		simpleWebSocketClient.setUserProperties(userProperties);
			
		List<Transport> transports = new ArrayList<>(1);
     	transports.add(new WebSocketTransport(simpleWebSocketClient));
 				
 		SockJsClient sockJsClient = new SockJsClient(transports);
 		WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
 		stompClient.setMessageConverter(new StringMessageConverter());
 		
 		URI stompUrlEndpoint = new URI(this.host + this.path);
 		StompSessionHandler sessionHandler = new SockJsWebsocketStompSessionHandler(
			this.getSubscribeHeaders(), 
			this.connectionTime, 
			this.responseBufferTime,
			responseMessage
		);
 		 		
 		WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
 		StompHeaders connectHeaders = new StompHeaders();
 		String connectionHeadersString = this.getConnectionsHeaders();
 		String[] splitHeaders = connectionHeadersString.split("\n");

 		for (int i = 0; i < splitHeaders.length; i++) {
 			int key = 0;
 			int value = 1;
 			String[] headerParameter = splitHeaders[i].split(":");
 			
 			connectHeaders.add(headerParameter[key], headerParameter[value]);			
 		}
 		
 		String startMessage = "\n[Execution Flow]"
 						    + "\n - Opening new connection"
 							+ "\n - Using response message pattern \"a[\"CONNECTED\""
 							+ "\n - Using response message pattern \"a[\"MESSAGE\\nsubscription:sub-0\""
 							+ "\n - Using disconnect pattern \"\"";

 		responseMessage.addMessage(startMessage);
 		
 		System.out.println(startMessage);
 		stompClient.connect(stompUrlEndpoint.toString(), handshakeHeaders, connectHeaders, sessionHandler, new Object[0]);
 		 	
 		// wait some time till killing the stomp connection
 		Thread.sleep(this.connectionTime + this.responseBufferTime);
 		stompClient.stop();
 		
 		String messageVariables = "\n[Variables]"
 								+ "\n" + " - Message count: " + responseMessage.getMessageCounter();
 		
 		responseMessage.addMessage(messageVariables);
 	
 		String messageProblems = "\n[Problems]"
								+ "\n" + responseMessage.getProblems();
 		
 		System.out.println(messageProblems);

 		responseMessage.addMessage(messageProblems);
 	}
	
	private String getSubscribeHeaders() {
    	return String.format(
			"%s\n%s", 
			this.subscribeHeadersId,
			this.subscribeHeadersDestination
		);
    }
    
    private String getConnectionsHeaders() {
    	return String.format(
			"%s\n%s\n%s\n%s\n%s", 
			this.connectionHeadersLogin,
			this.connectionHeadersPasscode,
			this.connectionHeadersHost, 
			this.connectionHeadersAcceptVersion,
			this.connectionHeadersHeartbeat			
		);
    }

}
