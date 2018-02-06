package orgMiJmeterSockjsSampler;

public class ResponseMessage {
	
	private String message = "";
	private long messageCounter = 0;
	private String problems = "";

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public void addMessage(String message) {
		this.message = (this.message != "") ? this.message + "\n" + message : message;
	}

	public long getMessageCounter() {
		return messageCounter;
	}

	public void setMessageCounter(long messageCounter) {
		this.messageCounter = messageCounter;
	}

	public String getProblems() {
		return problems;
	}

	public void addProblem(String problem) {
		this.problems = (this.problems != "") ? this.problems + "\n" + problem : problem;
	}
	
	public void setProblems(String problems) {
		this.problems = problems;
	}
}
