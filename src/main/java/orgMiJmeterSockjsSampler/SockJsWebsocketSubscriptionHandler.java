package orgMiJmeterSockjsSampler;

import java.lang.reflect.Type;
import java.util.Map;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;

public class SockJsWebsocketSubscriptionHandler implements StompFrameHandler
{
	private long messageCounter = 1;
	private long responseBufferTime;
	private ResponseMessage responseMessage;
	
	public SockJsWebsocketSubscriptionHandler(ResponseMessage responseMessage, long responseBufferTime) {
		this.responseMessage = responseMessage;
		this.responseBufferTime = responseBufferTime;		
	
		String subscribeMessage = " - Leaving streaming connection open"
								+ "\n - Waiting for messages for " + this.responseBufferTime + " MILLISECONDS";
		this.responseMessage.addMessage(subscribeMessage);	
	}
	
	@Override
	public Type getPayloadType(StompHeaders headers) {
		return String.class;
	}

	@Override
	public void handleFrame(StompHeaders headers, Object payload) {
		String message = "MESSAGE\\n" + headers.toString() + payload.toString();
		
		
		StringBuilder sb = new StringBuilder();
		Map<String, String> map = headers.toSingleValueMap();
		
		sb.append(" - Received message #" + this.messageCounter + " (" + message.length() + " bytes)a[\"MESSAGE");
				
		for (Map.Entry<String, String> entry : map.entrySet()) {
			sb.append("\\n" + entry.getKey() + ":" + entry.getValue());
		}
		
		sb.append("\\n\\n" + payload.toString() + "\\u0000\\n\"];");
						
		this.responseMessage.addMessage(sb.toString());
		this.responseMessage.setMessageCounter(this.messageCounter);
		this.messageCounter++;
	}
}
