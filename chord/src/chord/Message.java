package chord;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;

public class Message implements Serializable {

	private static final long serialVersionUID = 1L;
	private int to;
	private int from;
	private int replyTo;
	private String body;
	private int hashKey;
	private Hashtable<Integer,String> hashtable;
	private Vector<ReplicaElements> chain;
	private int ttl;
	private Type type;
	private boolean timer;

	public Message() {}

	
	public Message(int to, int from, int replyTo, String body, int hashKey, Hashtable<Integer,String> hashtable, Type type, boolean timer) {

		this.to = to;
		this.from = from;
		this.replyTo = replyTo;
		this.body = body;
		this.hashKey = hashKey;
		this.hashtable = hashtable;
		this.type = type;
		this.timer = timer;
	}
	
	public Message(int to, int from, int replyTo, String body, int hashKey, Hashtable<Integer,String> hashtable, Vector<ReplicaElements> chain, Type type, boolean timer) {

		this.to = to;
		this.from = from;
		this.replyTo = replyTo;
		this.body = body;
		this.hashKey = hashKey;
		this.hashtable = hashtable;
		this.chain = chain;
		this.type = type;
		this.timer = timer;
	}
	
	public Message(int to, int from, int replyTo, String body, int hashKey, Hashtable<Integer,String> hashtable, Vector<ReplicaElements> chain, int ttl, Type type, boolean timer) {

		this.to = to;
		this.from = from;
		this.replyTo = replyTo;
		this.body = body;
		this.hashKey = hashKey;
		this.hashtable = hashtable;
		this.chain = chain;
		this.ttl = ttl;
		this.type = type;
		this.timer = timer;
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
	public void setHashKey(int hashKey) {
		this.hashKey = hashKey;
	}
	public void setHashtable(Hashtable<Integer,String> hashtable) {
		this.hashtable = hashtable;
	}
	public void setChain(Vector<ReplicaElements> chain) {
		this.chain = chain;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public void setTTL(int ttl) {
		this.ttl = ttl;
	}
	public void setTimer(boolean timer) {
		this.timer = timer;
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
	public int getHashKey() {
		return this.hashKey;
	}
	public Hashtable<Integer,String> getHashtable() {
		return this.hashtable;
	}
	public Vector<ReplicaElements> getChain() {
		return this.chain;
	}
	public Type getType() {
		return this.type;
	}
	public int getTTL() {
		return this.ttl;
	}
	public boolean getTimer() {
		return this.timer;
	}
}
