package chord;

import java.io.Serializable;

public class Message implements Serializable{


	private static final long serialVersionUID = 1L;
	private int to;
	private int from;
	private int replyTo;
	private String body;
	private int hashId;
	private Type type;
	
	public Message() {}
	public Message(int to, int from, int replyTo, String body, int hashId, Type type) {
		this.to = to;
		this.from = from;
		this.replyTo = replyTo;
		this.body = body;
		this.hashId = hashId;
		this.type = type;
	}
	
	public void setTo(int to) {
		this.to = to;
	}
	public void setFrom(int from) {
		this.from = from;
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
	public void setType(Type type) {
		this.type = type;
	}
	public int getTo() {
		return this.to;
	}
	public int getFrom() {
		return this.from;
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
	public Type getType() {
		return this.type;
	}
	
}
