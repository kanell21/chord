package chord;

//import java.io.File;
//import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
/*
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
*/
public class MessageHandler extends Thread {
	
	private Message msg;
	
	private ChordNode node;
	
//	private static final int BUFFER_SIZE = 176400; // 44100 x 16 x 2 / 8
	
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
//                  System.out.println(node.id + "\t: Received Message of type PRED_SUCC_REQUEST");
                    message = new Message(msg.getReplyTo(), node.id, -1, Integer.toString(node.successor), -1, null, Type.PRED_SUCC_REPLY, msg.getTimer());
                    Messaging.SendMessage(message);
                    break;
                    
                case PRED_SUCC_REPLY:
//                	System.out.println(node.id + "\t: Received Message of type PRED_SUCC_REPLY");
                    message = new Message(Integer.parseInt(msg.getBody()), node.id, node.id, null, -1, null, Type.PRED_SUCC_REQUEST, msg.getTimer());
                    Messaging.SendMessage(message);
                    break;

                case NEW_SUCC_INFORM:
//                	System.out.println(node.id + "\t: Received Message of type NEW_SUCC_INFORM");
                	node.successor = Integer.parseInt(msg.getBody());
                    break;    
                
                case PRED_CHAIN_REQUEST:
//                	System.out.println(node.id + "\t: Received Message of type PRED_CHAIN_REQUEST");
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
//                	System.out.println(node.id + "\t: Received Message of type SUCC_CHAIN_REQUEST with TTL " + msgTTL);
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
//                	System.out.println(node.id + "\t: Received Message of type PRED_CHAIN_REPLY");
                	node.predChain.clear();
                	node.predChain = msg.getChain();
                	break;
                
                case SUCC_CHAIN_REPLY:
//                	System.out.println(node.id + "\t: Received Message of type SUCC_CHAIN_REPLY");
                	break;
                
                case DEPART_PRED_CHAIN:
//                	System.out.println(node.id + "\t: Received Message of type DEPART_PRED_CHAIN");
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
                		Messaging.SendMessage(new Message(msg.getReplyTo(), node.id, -1, null, -1, null, Type.GRACEFUL_DEPARTURE, msg.getTimer()));
                		break;
                	}
                	if (msgTTL == 1) {
                		Messaging.SendMessage(new Message(msg.getReplyTo(), node.id, -1, null, -1, null, Type.GRACEFUL_DEPARTURE, msg.getTimer()));
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
//                	System.out.println(node.id + "\t: Received Message of type RECEIVE_HASH_TABLE");
                	Hashtable<Integer,String> temp = msg.getHashtable();
                	node.hashtable.putAll(temp);
                    break;
                         
                case REQUEST_HASH_TABLE:
//                	System.out.println(node.id + "\t: Received Message of type REQUEST_HASH_TABLE");
                	Hashtable<Integer,String> distribute_hash = new Hashtable<Integer,String>();
                	Hashtable<Integer,String> tmp_hashtable = new Hashtable<Integer,String>();
                	tmp_hashtable.putAll(node.hashtable);
                	Iterator<Integer> itr = tmp_hashtable.keySet().iterator();
	                for ( ; itr.hasNext(); ) {
	                    int key = (int) itr.next();
	                    if (key <= msg.getReplyTo()) {
	                    	distribute_hash.put(key,(String) tmp_hashtable.get(key)); 
	                    	itr.remove();
	                    }
                	}
	                node.hashtable.putAll(tmp_hashtable);

                	message = new Message(msg.getReplyTo(), node.id, -1, null, -1, distribute_hash, Type.RECEIVE_HASH_TABLE, msg.getTimer());
                	Messaging.SendMessage(message);
                    break;
                
                case INSERT:
//                	System.out.println(node.id + "\t: Received Message of type INSERT");
                	hashKey = msg.getHashKey();
                	if ((node.predecessor < hashKey && (node.id >= hashKey || node.id == 0)) || node.id + hashKey == 0) {
                		synchronized(node.queue) {
	                		node.queue.add(this.getId());
                		}
	                	while (node.queue.firstElement() != this.getId());
                		node.hashtable.put(msg.getHashKey(), msg.getBody());
//                		System.out.println(node.id + "\t: Inserted hashkey-value: " + hashKey + "-" + msg.getBody());
                		
                		if (node.MODE.equals("ev_con") && msg.getTimer())
                			Messaging.SendMessage(new Message(1024, -1, -1, null, -1, null, Type.TIMER, false));
                		
                		if (node.predChain.size() > 0)
                			Messaging.SendMessage(new Message(node.successor, node.id, node.id, msg.getBody(), hashKey, null, null, node.K - 1, Type.INSERT_REPLICAS_INFORM, msg.getTimer()));
                		else {
                			msg.setTo(msg.getReplyTo());
                			msg.setType(Type.INSERT_REPLICAS_OK);
                			Messaging.SendMessage(msg);
                		}
                	}
                	else {
                		msg.setFrom(node.id);
                		msg.setTo(node.successor);
                		Messaging.SendMessage(msg);
                	}
                	break;
                
                case INSERT_REPLICAS_INFORM:
//                	System.out.println(node.id + "\t: Received Message of type INSERT_REPLICAS_INFORM");
                	msgId = msg.getReplyTo();
                	msgTTL = msg.getTTL();
                	for (Iterator<ReplicaElements> iter = node.predChain.iterator(); iter.hasNext(); ) {
                		repElems = iter.next();
                		if (repElems.id == msgId) {
                			repElems.hashtable.put(msg.getHashKey(), msg.getBody());
//                        	System.out.println(node.id + "\t: Inserted hashkey-value: " + msg.getHashKey() + "-" + msg.getBody());
                			break;
                		}
                	}
                	if (msgTTL == 1 || msg.getReplyTo() == node.successor) {
                		Messaging.SendMessage(new Message(msg.getReplyTo(), node.id, -1, msg.getBody(), msg.getHashKey(), null, Type.INSERT_REPLICAS_OK, msg.getTimer()));
                		break;
                	}
                	msg.setTo(node.successor);
                	msg.setFrom(node.id);
                	msg.setTTL(msgTTL - 1);
                	Messaging.SendMessage(msg);
                	break;
                	
                case INSERT_REPLICAS_OK:
//                	System.out.println(node.id + "\t: Received Message of type INSERT_REPLICAS_OK");
                	System.out.println(node.id + "\t: " + msg.getHashKey() + "-" + msg.getBody() + " was successfully inserted.");
                	synchronized(node.queue) {
                		node.queue.removeElementAt(0);
                	}
                	if (!node.MODE.equals("ev_con") && msg.getTimer())
            			Messaging.SendMessage(new Message(1024, -1, -1, null, -1, null, Type.TIMER, false));
                		
                	break;
                	
                case DELETE:
//                	System.out.println(node.id + "\t: Received Message of type DELETE");
                	hashKey = msg.getHashKey();
                	if (node.predecessor < hashKey && (node.id > hashKey || node.id == 0) || (node.id == 0 && hashKey ==0)) {
                		if (node.hashtable.containsKey(hashKey)) {
                			synchronized(node.queue) {
    	                		node.queue.add(this.getId());
                    		}
                			
                			if (node.MODE.equals("ev_con") && msg.getTimer())
                    			Messaging.SendMessage(new Message(1024, -1, -1, null, -1, null, Type.TIMER, false));
                			
    	                	while (node.queue.firstElement() != this.getId());
                			node.hashtable.remove(hashKey);
//                			System.out.println(node.id + "\t: Deleted hashkey: " + msg.getHashKey());
                			if (node.predChain.size() > 0)
                				Messaging.SendMessage(new Message(node.successor, node.id, node.id, null, hashKey, null, null, node.K - 1, Type.DELETE_REPLICAS_INFORM, msg.getTimer()));
                			else
                				Messaging.SendMessage(new Message(msg.getReplyTo(), node.id, -1, null, -1, null, Type.DELETE_REPLICAS_OK, msg.getTimer()));
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
//                	System.out.println(node.id + "\t: Received Message of type DELETE_REPLICAS_INFORM");
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
                		msg.setTo(msg.getReplyTo());
                		msg.setType(Type.DELETE_REPLICAS_OK);
                		Messaging.SendMessage(msg);
                		break;
                	}
                	msg.setTo(node.successor);
                	msg.setFrom(node.id);
                	msg.setTTL(msgTTL - 1);
                	Messaging.SendMessage(msg);
                	break;	
                	
                case DELETE_REPLICAS_OK:
//                	System.out.println(node.id + "\t: Received Message of type DELETE_REPLICAS_OK");
                	System.out.println(node.id + "\t: " + msg.getHashKey() + " was completely removed.");
                	synchronized(node.queue) {
                		node.queue.removeElementAt(0);
                	}
                	if (!node.MODE.equals("ev_con") && msg.getTimer())
            			Messaging.SendMessage(new Message(1024, -1, -1, null, -1, null, Type.TIMER, false));
                	break;
                	
                case QUERY_EC:
//                	System.out.println(node.id + "\t: Received Message of type QUERY_SC");
                	foundQuery = false;
                	hashKey = msg.getHashKey();
                	if (node.predecessor < hashKey && (node.id > hashKey || node.id == 0) || (node.id == 0 && hashKey ==0)) {
                		if (node.hashtable.containsKey(hashKey)) {
//                			System.out.println(node.id + "\t: Returning hashkey-value: " + hashKey + "-" + node.hashtable.get(hashKey));
                    		message = new Message(msg.getReplyTo(), node.id, -1, node.hashtable.get(hashKey), hashKey, null, Type.RESPONSE, msg.getTimer());
                		}
                		else {
//                			System.out.println(node.id + "\t: Nothing to return");
                    		message = new Message(msg.getReplyTo(), node.id, -1, null, -1, null, Type.RESPONSE, msg.getTimer());
                		}
                		Messaging.SendMessage(message);
                	}
                	else {
                		for (Iterator<ReplicaElements> iter = node.predChain.iterator(); iter.hasNext(); ) {
                			repElems = iter.next();
                			if ((repElems.id > hashKey || repElems.id == 0) & repElems.hashtable.containsKey(hashKey)) {
//               				System.out.println(node.id + "\t: Returning hashkey-value: " + hashKey + "-" + repElems.hashtable.get(hashKey) + " from my replicas");
                        		message = new Message(msg.getReplyTo(), node.id, -1, repElems.hashtable.get(hashKey), hashKey, null, Type.RESPONSE, msg.getTimer());
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
                
                case QUERYALL_EC:
                	if (msg.getReplyTo() != node.id || msg.getFrom() == -1) {
//                		System.out.println(node.id + "\t: Received Message of type QUERYALL_SC");
                		msg.getHashtable().putAll(node.hashtable);
                		for (int i = 0; i < node.predChain.size() - 1; i++) {
                			msg.getHashtable().putAll(node.predChain.elementAt(i).hashtable);
                		}
                		if (node.predChain.size() > 0)
                			msg.setTo(node.predChain.lastElement().id);
                		else
                			msg.setTo(node.predecessor);
                		msg.setFrom(node.id);
                		Messaging.SendMessage(msg);
	        		}
                	else {
//                		System.out.println(node.id + "\t: Received QUERYALL_SC response");
                		if(msg.getTimer())
                			Messaging.SendMessage(new Message(1024, -1, -1, null, -1, null, Type.TIMER, false));
                		Hashtable<Integer,String> tmpHashtable = msg.getHashtable();
                		int key, i = 0;
                		for (Iterator<Integer> iter = tmpHashtable.keySet().iterator(); iter.hasNext(); ) {
                			key = iter.next();
                			System.out.print(key + "-" + tmpHashtable.get(key) + " ");
                			if (++i % 8 == 0)
                				System.out.println();
                		}                		
                		System.out.println();
                	}
                	break;
                	
                case QUERY:
//                	System.out.println(node.id + "\t: Received Message of type QUERY");
                	hashKey = msg.getHashKey();
                	if ((node.predecessor < hashKey && (node.id > hashKey || node.id == 0) || (node.id == 0 && hashKey ==0)) && node.predChain.size() > 0) {
                		Messaging.SendMessage(new Message(node.successor, node.id, msg.getReplyTo(), null, hashKey, null, null, node.predChain.size(), Type.QUERY_TTL, msg.getTimer()));
                	}
                	else if ((node.predecessor < hashKey && (node.id > hashKey || node.id == 0) || (node.id == 0 && hashKey ==0))) {
                		if (node.hashtable.containsKey(hashKey)) {
//                			System.out.println(node.id + "\t: Returning hashkey-value: " + hashKey + "-" + node.hashtable.get(hashKey));
                    		Messaging.SendMessage(new Message(msg.getReplyTo(), node.id, -1, node.hashtable.get(hashKey), hashKey, null, Type.RESPONSE, msg.getTimer()));
                		}
                		else {
//                			System.out.println(node.id + "\t: Nothing to return");
                			Messaging.SendMessage(new Message(msg.getReplyTo(), node.id, -1, null, -1, null, Type.RESPONSE, msg.getTimer()));
                		}
                	}
                	else {
                		msg.setFrom(node.id);
                		msg.setTo(node.successor);
                		Messaging.SendMessage(msg);
                	}
                	break;
                	
                case QUERY_TTL:
//                	System.out.println(node.id + "\t: Received Message of type QUERY_TTL");
                	hashKey = msg.getHashKey();
                	msgTTL = msg.getTTL();
                	if (msgTTL == 1) {
                		if (node.predChain.lastElement().hashtable.containsKey(hashKey)) {
//                			System.out.println(node.id + "\t: Returning hashkey-value: " + hashKey + "-" + node.predChain.lastElement().hashtable.get(hashKey));
                    		Messaging.SendMessage(new Message(msg.getReplyTo(), node.id, -1, node.predChain.lastElement().hashtable.get(hashKey), hashKey, null, Type.RESPONSE, msg.getTimer()));
                		}
                		else {
//                			System.out.println(node.id + "\t: Nothing to return for hashkey " + hashKey);
                			Messaging.SendMessage(new Message(msg.getReplyTo(), node.id, -1, null, -1, null, Type.RESPONSE, msg.getTimer()));
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
                	if (msg.getReplyTo() != node.id || msg.getFrom() == -1) {
//                		System.out.println(node.id + "\t: Received Message of type QUERYALL");
                		if (node.predChain.size() > 0)
                			msg.getHashtable().putAll(node.predChain.lastElement().hashtable);
                		else 
                			msg.getHashtable().putAll(node.hashtable);
                		msg.setTo(node.successor);
                		msg.setFrom(node.id);
                		Messaging.SendMessage(msg);
	        		}
                	else {
//                		System.out.println(node.id + "\t: Received QUERYALL response");
                		if (msg.getTimer())
                			Messaging.SendMessage(new Message(1024, -1, -1, null, -1, null, Type.TIMER, false));
                		Hashtable<Integer,String> tmpHashtable = msg.getHashtable();
                		int key, i = 0;
                		for (Iterator<Integer> iter = tmpHashtable.keySet().iterator(); iter.hasNext(); ) {
                			key = iter.next();
                			System.out.print(key + "-" + tmpHashtable.get(key) + " ");
                			if (++i % 8 == 0)
                				System.out.println();
                		}
                		System.out.println();
                		
                	}
                	break;
              	
                case RESPONSE:
//                	System.out.println(node.id + "\t: Received Message of type RESPONSE with hashkey-value " + msg.getHashKey() + "-" + msg.getBody());
                	if (msg.getTimer())
                		Messaging.SendMessage(new Message(1024, -1, -1, null, -1, null, Type.TIMER, false));
                	System.out.println(node.id + "\t: " + msg.getHashKey() + "-" + msg.getBody());
                	break;
                
                default:
                    System.out.println("Bad message Type, nothing to handle.");
                    break;
	        }
	        return;
	}

}
