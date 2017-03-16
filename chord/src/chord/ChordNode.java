package chord;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Vector;

public class ChordNode extends Thread {
	
	public Hashtable<Integer,String> hashtable;
	public ServerSocket Server;
    public int id;
    public int number;
    public int predecessor;
    public int successor;
    public int PORT = 49152;
    public static String IPV4 = "127.0.0.1";
    public String MODE;
    public boolean alive = true;
    public Vector<ReplicaElements> predChain;
    public int K;
    public Vector<Long> queue;
    
    public ChordNode(int id, int number, int K, String mode) {
        this.id = id;
        this.number = number;
        this.K = K;
        this.MODE = mode;
    }
    
  
    @Override
    public void run(){
    	
        try {
            Initialization();
            Message message;
            for(;;){
            	message = ReceiveMessage();
            	
            	if (!this.alive) {
            		message = new Message(this.successor, this.id, this.id, Integer.toString(this.predecessor), -1, this.hashtable, this.predChain, this.K, Type.DEPART_PRED_CHAIN, false);
                	Messaging.SendMessage(message);
                	Server = new ServerSocket(PORT + this.id);
                	message = ReceiveMessage();
            		Messaging.SendMessage(new Message(this.predecessor, this.id, -1, Integer.toString(this.successor), -1, null, Type.NEW_SUCC_INFORM, false));
        	        Server.close();
            		return;
            	}
            	
            	MessageHandler messageHandler = new MessageHandler(message, this);
            	messageHandler.start();
            }
            
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
        	e.printStackTrace();
        }
    }
    
    public Message ReceiveMessage() throws ClassNotFoundException {
    	
    	Message message = null;
    	try {
    		
	    	Socket incoming = Server.accept();
	    	ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(incoming.getInputStream()));
	    	message = (Message) ois.readObject();
	    	
	        
    	} catch (IOException e) {
    		//e.printStackTrace();
		}  
    	return message;
    }
    
    public void FindPredSucc() {
    	
    	int predecessor = 0;
    	int successor = 0;
    	Message inMessage = new Message(0, this.id, this.id, null, -1, null, Type.PRED_SUCC_REQUEST, false);
    	Message outMessage;
    	Messaging.SendMessage(inMessage);
    	while (true) {
    		try {
				inMessage = ReceiveMessage();
//		        System.out.println(this.id + "\t: Message received from port-node " + inMessage.getFrom() + " : " + inMessage.getBody()); 
				successor = Integer.parseInt(inMessage.getBody());		
	    		if (successor > this.id || successor == 0) {
	    			break;
	    		}
	    		else {
	    			predecessor = successor;
	    			outMessage = new Message(Integer.parseInt(inMessage.getBody()), this.id, this.id, null, -1, null, Type.PRED_SUCC_REQUEST, false);
	    			Messaging.SendMessage(outMessage);
	    		}
	    		
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}	
    	}
    	this.predecessor = predecessor;
    	this.successor = successor;
    	return;
    }
    
    public void FindMyHashtable() throws InterruptedException {
    	
    	Message msg = new Message(this.successor, this.id, this.id, null, -1, null, Type.REQUEST_HASH_TABLE, false);
    	Messaging.SendMessage(msg);
    	try {
			msg = ReceiveMessage();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
    	MessageHandler msgHandler = new MessageHandler(msg, this);
    	msgHandler.start();
    	msgHandler.join();
    	
    	return;
    }
   

    public void Initialization() throws IOException, InterruptedException {
    	
    	queue = new Vector<Long>();
    	hashtable = new Hashtable<Integer,String>();
    	predChain = new Vector<ReplicaElements>();
    	Server = new ServerSocket(PORT + this.id);
        System.out.println(this.id + "\t: Started my Server using port " + Server.getLocalPort());

        this.predecessor = 0;
    	this.successor = 0;

    	if (this.id == 0)
    		return;

        FindPredSucc();

        FindMyHashtable();
        
        Message predChainMsg = new Message(this.predecessor, this.id, this.id, null, -1, new Hashtable<Integer,String>(), new Vector<ReplicaElements>(), this.K - 1, Type.PRED_CHAIN_REQUEST, false);
        Messaging.SendMessage(predChainMsg);
        Vector<ReplicaElements> tmpChain = new Vector<ReplicaElements>();
        tmpChain.add(new ReplicaElements(this.id, this.hashtable));
        Message succChainMsg = new Message(this.successor, this.id, this.id, null, -1, new Hashtable<Integer,String>(), tmpChain, this.K, Type.SUCC_CHAIN_REQUEST, false);
        Messaging.SendMessage(succChainMsg);
        
        return;
    }
}