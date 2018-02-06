package orgMiJmeterSockjsSampler;

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.socket.WebSocketSession;

/**
 * ConnectionTimer will run a timer and after the timer comes to an end 
 * close the Websocket connection
 *
 * @author Frank Mohr
 *
 */
public class ConnectionTimer implements Runnable {
    private static Log logger = LogFactory.getLog(ConnectionTimer.class);
	// in milliseconds
    private long totalTime;
	private WebSocketSession webSocketSession = null;
	
    public ConnectionTimer(WebSocketSession session, long totalTime) {
		this.webSocketSession = session;
		this.totalTime = totalTime;
    }
    
    @Override
    public void run() {
		try {
			Thread.sleep(totalTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			webSocketSession.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}