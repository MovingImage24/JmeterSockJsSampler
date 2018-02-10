package orgMiJmeterSockjsSampler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

public class SockJsXhrSessionHandler extends AbstractWebSocketHandler {
	private String connectionHeader;
	private String subscribeHeaders;
	private long connectionTime;
	private long responseBufferTime;
	private ResponseMessage responseMessage;
	private boolean subscribed = false;
	private long messageCounter = 1;
	
	public SockJsXhrSessionHandler(String connectionHeader, String subscribeHeaders, long connectionTime, long responseBufferTime, ResponseMessage responseMessage) {
		this.connectionHeader = connectionHeader;
		this.subscribeHeaders = subscribeHeaders;
		this.connectionTime = connectionTime;
		this.responseBufferTime = responseBufferTime;
		this.responseMessage = responseMessage;		
	}
	
	@Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		this.responseMessage.addMessage(" Session id : " + session.getId());
		this.responseMessage.addMessage(" - Waiting for the server connection for " + this.connectionTime + " MILLISECONDS");
		this.responseMessage.addMessage(" - XHR-Streaming connection has been opened");
		
		SockJsStompTextMessageBuilder textMessageBuilder = SockJsStompTextMessageBuilder.create(StompCommand.CONNECT);
		String[] splitHeaders = connectionHeader.split("\n");
		for (int i = 0; i < splitHeaders.length; i++) {
			textMessageBuilder.headers(splitHeaders[i]);	
		}
		TextMessage connectMessage = textMessageBuilder.build();
      	  
		session.sendMessage(connectMessage);
		new Thread(new ConnectionTimer(session, this.connectionTime + this.responseBufferTime)).start(); 
    }
      
    @Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
	  	String messagePayload = message.getPayload().toString();
	  	
	  	final Matcher m = Pattern.compile("CONNECTED|MESSAGE").matcher(messagePayload);
	  	if (m.find()) {
	  		switch (m.group()) {
	      		case "CONNECTED": 
	      			this.responseMessage.addMessage(" - Connection established"); 
	      			this.responseMessage.addMessage(" - Leaving streaming connection open"); 
	      					      				      		
		      		SockJsStompTextMessageBuilder textMessageBuilder = SockJsStompTextMessageBuilder.create(StompCommand.SUBSCRIBE);
		      		String[] splitHeaders = subscribeHeaders.split("\n");
		    		for (int i = 0; i < splitHeaders.length; i++) {
		    			textMessageBuilder.headers(splitHeaders[i]);	
		    		}
		    		TextMessage subscribeMessage = textMessageBuilder.build();
		      		
		      		session.sendMessage(subscribeMessage);
		      		break;
	      		case "MESSAGE":
	      			if (!this.subscribed) {
	      				this.responseMessage.addMessage(" - Waiting for messages for " + this.responseBufferTime + " MILLISECONDS"); 
	      				this.subscribed = true;
	      			}
	      			
	      			String messageContent = this.getMessageContent(messagePayload);
	      			String headers = this.getMessageHeaders(messagePayload);
	      				      			
	      			StringBuilder sb = new StringBuilder();
	      				      			
	      			sb.append(" - Received message #" + this.messageCounter + " (" + messageContent.length() + " bytes)a[\"MESSAGE");
	      			
	      			String[] splitMessageHeaders = headers.split("\n");
	      	 		for (int i = 0; i < splitMessageHeaders.length; i++) {
	      	 			sb.append("\\n" + splitMessageHeaders[i]);
	      	 		}
	      			
	      			sb.append("\\n\\n" + messageContent.toString() + "\\u0000\\n\"];");
	      							
	      			this.responseMessage.addMessage(sb.toString());
	      			this.responseMessage.setMessageCounter(this.messageCounter);
	      			
	      			this.messageCounter++;
	      			break;
	  		}
	  	}  	
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		System.out.println("Exception Transport Error: " + exception.getMessage());
		
		String exceptionMessage = " - Received exception: " + exception.getMessage();
		
		this.responseMessage.addProblem(exceptionMessage);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		this.responseMessage.addMessage("Connection closed session id=" + session.getId());	
	}

	@Override
	public boolean supportsPartialMessages() {
		return false;
	}
	
	private String getMessageContent(String messagePayload) {
		final Matcher matcher = Pattern.compile(".*(\\{\".*\"\\}).*").matcher(messagePayload);
		matcher.find();
		String content = matcher.group(1);
		
		return content.trim();
	}
	
	private String getMessageHeaders(String messagePayload) {
		String headers = messagePayload.replaceFirst("MESSAGE", "");
		headers = headers.replaceFirst("\\{\".*\"\\}", "");
		
		return headers.trim();
	}
}
