package chord;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ChordNode extends Thread {
	
	public Hashtable<Integer, String> myTable = new Hashtable<Integer, String>();
	public ServerSocket Server;
    public int id;
    public int number;
    public int predecessor;
    public int successor;
    public int PORT = 49152;
    public String IPV4 = "127.0.0.1";
    
    private class PredSucc {
    	int predecessor;
    	int successor;
    }
    
    public ChordNode(int id, int number) {
        this.id = id;
        this.number = number;
    }
    
    
    
    @Override
    public void run(){
        
        try {
            Server = new ServerSocket(PORT + this.id);
            System.out.println(this.id + "\t: Started my Server using port " + Server.getLocalPort());
            PredSucc predsucc;
            if (this.id > 0)
            	predsucc = FindPredSucc();
            else {
            	predsucc = new PredSucc();
            	predsucc.predecessor = 0;
            	predsucc.successor = 0;
            }
            this.predecessor = predsucc.predecessor;
            this.successor = predsucc.successor;
            
            if (this.predecessor != this.id) {
            	Message succInform = new Message(this.predecessor, this.id, -1, Integer.toString(this.id), -1, Type.NEW_SUCC_INFORM);
            	SendMessage(succInform);
            }
            	
            if (this.successor != this.id) {
            	Message predInform = new Message(this.successor, this.id, -1, Integer.toString(this.id), -1, Type.NEW_PRED_INFORM);
            	SendMessage(predInform);
            }
            
            Message message;
            
            for(;;){
            	
            	message = ReceiveMessage();
            	MessageHandler messageHandler = new MessageHandler(message, this);
    	        messageHandler.start();
            }
            
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ChordNode.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Message ReceiveMessage() throws ClassNotFoundException {
    	
    	Message message = null;
    	try {
    		
	    	Socket incoming = Server.accept();
	    	ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(incoming.getInputStream()));
	    	message = (Message) ois.readObject();
	        
    	} catch (IOException e) {
			e.printStackTrace();
		}  
    	return message;
    }
    
	public void SendMessage(Message message) {
    	
    	Socket echoSocket;
		try {
			echoSocket = new Socket(IPV4, PORT + message.getTo());
	    	ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(echoSocket.getOutputStream()));
			oos.writeObject(message);
			oos.flush();
	        System.out.println(this.id + "\t: Message sent to port-node " + message.getTo() + " : " + message.getBody()); 
	        echoSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    } 

    
    public PredSucc FindPredSucc() {
    	
    	PredSucc predsucc = new PredSucc();
    	predsucc.predecessor = 0;
    	predsucc.successor = 0;
    	Message message = new Message(0, this.id, this.id, null, -1, Type.PRED_SUCC_REQUEST);
    	SendMessage(message);
    	while(true) {

    		try {
				message = ReceiveMessage();
		        System.out.println(this.id + "\t: Message received from port-node " + message.getFrom() + " : " + message.getBody()); 
				predsucc.successor = Integer.parseInt(message.getBody());		
	    		if(predsucc.successor > this.id || predsucc.successor == 0)
	    			break;
	    		else {
	    			predsucc.predecessor = predsucc.successor;
	    			MessageHandler messageHandler = new MessageHandler(message, this);
					messageHandler.start();
	    		}
	    		
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}	
    	}
    	return predsucc;
    }
    
}