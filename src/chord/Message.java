package chord;

public class Message {
	private int to;
	private int replyTo;
	private String body;
	private int hashId;
	private int type;
	
	public Message() {}
	public Message(int to, int replyTo, String body, int hashId, int type) {
		this.to = to;
		this.replyTo = replyTo;
		this.body = body;
		this.hashId = hashId;
		this.type = type;
	}
	
	public void setTo(int to) {
		this.to = to;
	}
	public void setReplyTo(int replyTo) {
		this.replyTo = replyTo;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public void setHashId(int hashId) {
		this.hashId = hashId;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getTo() {
		return this.to;
	}
	public int getReplyTo() {
		return this.replyTo;
	}
	public String getBody() {
		return this.body;
	}
	public int getHashId() {
		return this.hashId;
	}
	public int getType() {
		return this.type;
	}
	
}
