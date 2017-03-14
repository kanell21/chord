package chord;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

public class MessageHandler extends Thread {
	
	private Message msg;
	
	private ChordNode node;
	
	private static final int BUFFER_SIZE = 176400; // 44100 x 16 x 2 / 8
	
	public MessageHandler(Message msg, ChordNode node) {
		this.msg = msg;
	    this.node = node;
	}
	
	public void run() {
	
	        Type type = msg.getType();
	        Message message = null;
	        int hashKey;
	        int msgId;
	        int msgTTL;
	        boolean foundQuery;
	        ReplicaElements repElems;
	        switch(type) {
	        
	            case PRED_SUCC_REQUEST:
                    System.out.println(node.id + "\t: Received Message of type PRED_SUCC_REQUEST");
                    message = new Message(msg.getReplyTo(), node.id, -1, Integer.toString(node.successor), -1, null, Type.PRED_SUCC_REPLY);
                    Messaging.SendMessage(message);
                    break;
                    
                case PRED_SUCC_REPLY:
                	System.out.println(node.id + "\t: Received Message of type PRED_SUCC_REPLY");
                    message = new Message(Integer.parseInt(msg.getBody()), node.id, node.id, null, -1, null, Type.PRED_SUCC_REQUEST);
                    Messaging.SendMessage(message);
                    break;

                case NEW_SUCC_INFORM:
                	System.out.println(node.id + "\t: Received Message of type NEW_SUCC_INFORM");
                	node.successor = Integer.parseInt(msg.getBody());
                    break;    
                
                case PRED_CHAIN_REQUEST:
                	System.out.println(node.id + "\t: Received Message of type PRED_CHAIN_REQUEST");
                	node.successor = msg.getReplyTo();
                	msg.getChain().clear();
                	msg.getChain().addAll(node.predChain);
                	msg.getChain().add(0, new ReplicaElements(node.id, node.hashtable));
                	while (msg.getChain().size() > node.K - 1) {
                		msg.getChain().remove(node.K - 1);
                	}
                	
                	msg.setTo(msg.getReplyTo());
                	msg.setFrom(node.id);
                	msg.setReplyTo(-1);
                	msg.setType(Type.PRED_CHAIN_REPLY);
                	
                	Messaging.SendMessage(msg);
                	break;
                
                case SUCC_CHAIN_REQUEST:
                	msgTTL = msg.getTTL();
                	System.out.println(node.id + "\t: Received Message of type SUCC_CHAIN_REQUEST with TTL " + msgTTL);
                	if (msgTTL == node.K - 1) {
                		node.predecessor = msg.getReplyTo();
                	}
                	node.predChain.add(node.K - 1 - msgTTL, msg.getChain().elementAt(0));
                	while (node.predChain.size() > node.K - 1) {
                		node.predChain.remove(node.K - 1);
                	}
                	
                	msg.setFrom(node.id);
                	msg.setTTL(msgTTL - 1);
                	if (msgTTL > 1 && node.successor != msg.getReplyTo() && node.successor != node.id)
                		msg.setTo(node.successor);
                	else {
                		msg.setTo(msg.getReplyTo());
                		msg.setType(Type.SUCC_CHAIN_REPLY);
                	}
                	Messaging.SendMessage(msg);
                	break;	
                
                case PRED_CHAIN_REPLY:
                	System.out.println(node.id + "\t: Received Message of type PRED_CHAIN_REPLY");
                	node.predChain.clear();
                	node.predChain = msg.getChain();
                	break;
                
                case SUCC_CHAIN_REPLY:
                	System.out.println(node.id + "\t: Received Message of type SUCC_CHAIN_REPLY");
                	break;
                
                case DEPART_PRED_CHAIN:
                	System.out.println(node.id + "\t: Received Message of type DEPART_PRED_CHAIN");
                	msgTTL = msg.getTTL();
                	if (msgTTL == node.K) {
                		msg.getHashtable().putAll(node.hashtable);
                		node.hashtable.putAll(msg.getHashtable());
                		node.predecessor = msg.getChain().firstElement().id;
                	}
                	else {
                		node.predChain.elementAt(node.K - msgTTL - 1).hashtable = msg.getHashtable();
                	}
                		
                	if (node.successor == msg.getReplyTo() && msgTTL > 1) {
                		node.predChain.remove(node.predChain.size() - 1);
                		Messaging.SendMessage(new Message(msg.getReplyTo(), node.id, -1, null, -1, null, Type.GRACEFUL_DEPARTURE));
                		break;
                	}
                	if (msgTTL == 1) {
                		Messaging.SendMessage(new Message(msg.getReplyTo(), node.id, -1, null, -1, null, Type.GRACEFUL_DEPARTURE));
                		break;
                	}
                	
                	node.predChain.remove(node.K - msgTTL);
                	if (msg.getChain().lastElement().id != node.id)
                		node.predChain.add(msg.getChain().lastElement());

                	msg.setTo(node.successor);
                	msg.setFrom(node.id);
                	msg.getChain().remove(msg.getChain().size() - 1);
                	msg.setTTL(msgTTL - 1);
                	Messaging.SendMessage(msg);
                	break;
                	
                case GRACEFUL_DEPARTURE:
                	System.out.println(node.id + "\t: Received Message of type GRACEFUL_DEPARTURE");
                    break;
                
                case RECEIVE_HASH_TABLE:
                	System.out.println(node.id + "\t: Received Message of type RECEIVE_HASH_TABLE");
                	Hashtable<Integer,String> temp = msg.getHashtable();
                	node.hashtable.putAll(temp);
                    break;
                         
                case REQUEST_HASH_TABLE:
                	System.out.println(node.id + "\t: Received Message of type REQUEST_HASH_TABLE");
                	Hashtable<Integer,String> distribute_hash = new Hashtable<Integer,String>();
                	for (Iterator<Integer> iter = node.hashtable.keySet().iterator(); iter.hasNext(); ) {
                	    int key = (int) iter.next();
                	    if (key <= msg.getReplyTo()) {
                	    	distribute_hash.put(key,(String) node.hashtable.get(key));
                	    	node.hashtable.remove(key);        	    	
                	    }
                	}
                	message = new Message(msg.getReplyTo(), node.id, -1, null, -1, distribute_hash, Type.RECEIVE_HASH_TABLE);
                	Messaging.SendMessage(message);
                    break;
                
                case INSERT:
                	System.out.println(node.id + "\t: Received Message of type INSERT");
                	hashKey = msg.getHashKey();
                	if ((node.predecessor < hashKey && (node.id >= hashKey || node.id == 0)) || node.id + hashKey == 0) {
                		synchronized(node.queue) {
	                		node.queue.add(this.getId());
                		}
	                	while (node.queue.firstElement() != this.getId());
                		node.hashtable.put(msg.getHashKey(), msg.getBody());
                		System.out.println(node.id + "\t: Inserted hashkey-value: " + hashKey + "-" + msg.getBody());
                		if (node.predChain.size() > 0)
                			Messaging.SendMessage(new Message(node.successor, node.id, node.id, msg.getBody(), hashKey, null, null, node.K - 1, Type.INSERT_REPLICAS_INFORM));
                		else {
                			synchronized(node.queue) {
                        		node.queue.removeElementAt(0);
                        	}
                		}
                	}
                	else {
                		msg.setFrom(node.id);
                		msg.setTo(node.successor);
                		Messaging.SendMessage(msg);
                	}
                	break;
                
                case INSERT_REPLICAS_INFORM:
                	System.out.println(node.id + "\t: Received Message of type INSERT_REPLICAS_INFORM");
                	msgId = msg.getReplyTo();
                	msgTTL = msg.getTTL();
                	for (Iterator<ReplicaElements> iter = node.predChain.iterator(); iter.hasNext(); ) {
                		repElems = iter.next();
                		if (repElems.id == msgId) {
                			repElems.hashtable.put(msg.getHashKey(), msg.getBody());
                        	System.out.println(node.id + "\t: Inserted hashkey-value: " + msg.getHashKey() + "-" + msg.getBody());
                			break;
                		}
                	}
                	if (msgTTL == 1 || msg.getReplyTo() == node.successor) {
                		Messaging.SendMessage(new Message(msg.getReplyTo(), node.id, -1, null, -1, null, Type.INSERT_REPLICAS_OK));
                		break;
                	}
                	msg.setTo(node.successor);
                	msg.setFrom(node.id);
                	msg.setTTL(msgTTL - 1);
                	Messaging.SendMessage(msg);
                	break;
                	
                case INSERT_REPLICAS_OK:
                	System.out.println(node.id + "\t: Received Message of type INSERT_REPLICAS_OK");
                	synchronized(node.queue) {
                		node.queue.removeElementAt(0);
                	}
                	break;
                	
                case DELETE:
                	System.out.println(node.id + "\t: Received Message of type DELETE");
                	hashKey = msg.getHashKey();
                	if (node.predecessor < hashKey && (node.id > hashKey || node.id == 0)) {
                		if (node.hashtable.containsKey(hashKey)) {
                			synchronized(node.queue) {
    	                		node.queue.add(this.getId());
                    		}
    	                	while (node.queue.firstElement() != this.getId());
                			node.hashtable.remove(hashKey);
                			System.out.println(node.id + "\t: Deleted hashkey: " + msg.getHashKey());
                			if (node.predChain.size() > 0)
                				Messaging.SendMessage(new Message(node.successor, node.id, node.id, null, hashKey, null, null, node.K - 1, Type.DELETE_REPLICAS_INFORM));
                		}
                		else
                			System.out.println(node.id + "\t: No hashkey "+ hashKey + " to delete");
                	}
                	else {
                		msg.setFrom(node.id);
                		msg.setTo(node.successor);
                		Messaging.SendMessage(msg);
                	}
                	break;
                
                case DELETE_REPLICAS_INFORM:
                	System.out.println(node.id + "\t: Received Message of type DELETE_REPLICAS_INFORM");
                	msgId = msg.getReplyTo();
                	msgTTL = msg.getTTL();
                	for (Iterator<ReplicaElements> iter = node.predChain.iterator(); iter.hasNext(); ) {
                		repElems = iter.next();
                		if (repElems.id == msgId) {
                			repElems.hashtable.remove(msg.getHashKey());
                        	System.out.println(node.id + "\t: Deleted hashkey: " + msg.getHashKey());
                			break;
                		}
                	}
                	if (msgTTL == 1 || msg.getReplyTo() == node.successor) {
                		Messaging.SendMessage(new Message(msg.getReplyTo(), node.id, -1, null, -1, null, Type.DELETE_REPLICAS_OK));
                		break;
                	}
                	msg.setTo(node.successor);
                	msg.setFrom(node.id);
                	msg.setTTL(msgTTL - 1);
                	Messaging.SendMessage(msg);
                	break;	
                	
                case DELETE_REPLICAS_OK:
                	System.out.println(node.id + "\t: Received Message of type DELETE_REPLICAS_OK");
                	synchronized(node.queue) {
                		node.queue.removeElementAt(0);
                	}
                	break;
                	
                case QUERY_SC:
                	System.out.println(node.id + "\t: Received Message of type QUERY_SC");
                	foundQuery = false;
                	hashKey = msg.getHashKey();
                	if (node.predecessor < hashKey && (node.id > hashKey || node.id == 0)) {
                		if (node.hashtable.containsKey(hashKey)) {
                			System.out.println(node.id + "\t: Returning hashkey-value: " + hashKey + "-" + node.hashtable.get(hashKey));
                    		message = new Message(msg.getReplyTo(), node.id, -1, node.hashtable.get(hashKey), hashKey, null, Type.RESPONSE);
                		}
                		else {
                			System.out.println(node.id + "\t: Nothing to return");
                    		message = new Message(msg.getReplyTo(), node.id, -1, null, -1, null, Type.RESPONSE);
                		}
                		Messaging.SendMessage(message);
                	}
                	else {
                		for (Iterator<ReplicaElements> iter = node.predChain.iterator(); iter.hasNext(); ) {
                			repElems = iter.next();
                			if ((repElems.id > hashKey || repElems.id == 0) & repElems.hashtable.containsKey(hashKey)) {
                				System.out.println(node.id + "\t: Returning hashkey-value: " + hashKey + "-" + repElems.hashtable.get(hashKey) + " from my replicas");
                        		message = new Message(msg.getReplyTo(), node.id, -1, repElems.hashtable.get(hashKey), hashKey, null, Type.RESPONSE);
                        		Messaging.SendMessage(message);
                        		foundQuery = true;
                        		break;
                			}
                				
                		}
                		if (!foundQuery) {
	                		msg.setFrom(node.id);
	                		msg.setTo(node.successor);
	                		Messaging.SendMessage(msg);
                		}
                	}
                	break;
                
                case QUERYALL_SC:
                	if (node.id > 0) {
                		System.out.println(node.id + "\t: Received Message of type QUERYALL_SC");
                		msg.getHashtable().putAll(node.hashtable);
                		msg.setTo(node.successor);
                		msg.setFrom(node.id);
                		Messaging.SendMessage(msg);
	        		}
                	else {
                		if (msg.getFrom() == -1) {
                			System.out.println(node.id + "\t: Received Message of type QUERYALL_SC");
                			msg.setHashtable(new Hashtable<Integer,String>());
                			msg.getHashtable().putAll(node.hashtable);
                			msg.setTo(node.successor);
                			msg.setFrom(node.id);
                			Messaging.SendMessage(msg);
                		}
                		else {
                			System.out.println(node.id + "\t: Received QUERYALL_SC response");
                			Hashtable<Integer,String> tmpHashtable = msg.getHashtable();
                			int key;
                			for (Iterator<Integer> iter = tmpHashtable.keySet().iterator(); iter.hasNext(); ) {
                				key = iter.next();
                				System.out.println(tmpHashtable.get(key));
                			}
                		}
                	}
                	break;
                	
                case QUERY:
                	System.out.println(node.id + "\t: Received Message of type QUERY");
                	hashKey = msg.getHashKey();
                	if (node.predecessor < hashKey && (node.id > hashKey || node.id == 0) && node.predChain.size() > 0) {
                		Messaging.SendMessage(new Message(node.successor, node.id, msg.getReplyTo(), null, hashKey, null, null, node.predChain.size(), Type.QUERY_TTL));
                	}
                	else if (node.predChain.size() == 0) {
                		if (node.hashtable.containsKey(hashKey)) {
                			System.out.println(node.id + "\t: Returning hashkey-value: " + hashKey + "-" + node.hashtable.get(hashKey));
                    		Messaging.SendMessage(new Message(msg.getReplyTo(), node.id, -1, node.hashtable.get(hashKey), hashKey, null, Type.RESPONSE));
                		}
                		else {
                			System.out.println(node.id + "\t: Nothing to return");
                    		message = new Message(msg.getReplyTo(), node.id, -1, null, -1, null, Type.RESPONSE);
                		}
                	}
                	else {
                		msg.setFrom(node.id);
                		msg.setTo(node.successor);
                		Messaging.SendMessage(msg);
                	}
                	break;
                	
                case QUERY_TTL:
                	System.out.println(node.id + "\t: Received Message of type QUERY_TTL");
                	hashKey = msg.getHashKey();
                	msgTTL = msg.getTTL();
                	if (msgTTL == 1) {
                		if (node.predChain.lastElement().hashtable.containsKey(hashKey)) {
                			System.out.println(node.id + "\t: Returning hashkey-value: " + hashKey + "-" + node.predChain.lastElement().hashtable.get(hashKey));
                    		Messaging.SendMessage(new Message(msg.getReplyTo(), node.id, -1, node.predChain.lastElement().hashtable.get(hashKey), hashKey, null, Type.RESPONSE));
                		}
                		else {
                			System.out.println(node.id + "\t: Nothing to return");
                    		message = new Message(msg.getReplyTo(), node.id, -1, null, -1, null, Type.RESPONSE);
                		}
                	}
                	else {
                		msg.setFrom(node.id);
                		msg.setTo(node.successor);
                		msg.setTTL(msgTTL - 1);
                		Messaging.SendMessage(msg);
                	}
                	break;
                
                case QUERYALL:
                	if (node.id > 0) {
                		System.out.println(node.id + "\t: Received Message of type QUERYALL");
                		msg.getHashtable().putAll(node.predChain.lastElement().hashtable);
                		msg.setTo(node.successor);
                		msg.setFrom(node.id);
                		Messaging.SendMessage(msg);
	        		}
                	else {
                		if (msg.getFrom() == -1) {
                			System.out.println(node.id + "\t: Received Message of type QUERYALL");
                			msg.setHashtable(new Hashtable<Integer,String>());
                			if (node.predChain.size() > 0)
                				msg.getHashtable().putAll(node.predChain.lastElement().hashtable);
                			else {
                				msg.getHashtable().putAll(node.hashtable);
                			}
                			msg.setTo(node.successor);
                			msg.setFrom(node.id);
                			Messaging.SendMessage(msg);
                		}
                		else {
                			System.out.println(node.id + "\t: Received QUERYALL response");
                			Hashtable<Integer,String> tmpHashtable = msg.getHashtable();
                			int key;
                			for (Iterator<Integer> iter = tmpHashtable.keySet().iterator(); iter.hasNext(); ) {
                				key = iter.next();
                				System.out.println(tmpHashtable.get(key));
                			}
                		}
                	}
                	break;

                case RESPONSE:
                	System.out.println(node.id + "\t: Received Message of type RESPONSE with hashkey-value " + msg.getHashKey() + "-" + msg.getBody());
                	if (msg.getHashKey() > -1) {
                		byte[]  buffer = new byte[BUFFER_SIZE];
                		try {
                		    AudioInputStream in = AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, AudioSystem.getAudioInputStream(new File("/home/dimosthenis/Downloads/key.mp3")));
                		    AudioFormat audioFormat = in.getFormat();
                	
                		    SourceDataLine line = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, audioFormat));
                	   
                			line.open(audioFormat);
                			line.start();
                			while (true) {
                			      int n = in.read(buffer, 0, buffer.length);
                			      if (n < 0) {
                			        break;
                			      }
                			      line.write(buffer, 0, n);
                			    }
                			    line.drain();
                			    line.close();
                			  
                		} catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
                			e.printStackTrace();
                		}
                	}
                	break;
                
                default:
                    System.out.println("Midweek days are so-so.");
                    break;
	        }
	        return;
	}

}
