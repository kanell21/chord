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
	
	
	private Hashtable<String, String> myTable = new Hashtable<String, String>();
	private ServerSocket Server;
    public int id;
    public int number;
    private int predecessor;
    private int successor;
    private static int PORT = 49152;
    private String IPV4 = "127.0.0.1";
    
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
            
            if (this.predecessor != this.id)
            	// send my id
            if (this.successor != this.id)
            	// send my id
            
            System.out.println(this.id + "\t: my predecessor is " + this.predecessor + ", my successor is " + this.successor);
            Message message;
            for(;;){

            	message = ReceiveMessage();
    	        if(message.getType() == 0) { // find predecessor - successor
    	        	
    	        }
    	        else if(message.getType() == 1) { // i am your successor 
    	        	
    	        }  
    	        else if(message.getType() == 2) { //  i am your predecessor
    	        	
    	        }
            	
            }
            
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ChordNode.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void SendMessage(Message message) {
    	
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
    
    private Message ReceiveMessage() throws ClassNotFoundException {
    	
    	Message message = null;
    	try {
    		
	    	Socket incoming = Server.accept();
	    	ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(incoming.getInputStream()));
	    	message = (Message) ois.readObject();
	        System.out.println(this.id + "\t: Message received: " + message.getBody());
	        
    	} catch (IOException e) {
			e.printStackTrace();
		}  
    	return message;
    }
    
    private PredSucc FindPredSucc() {
    	
    	PredSucc predsucc = new PredSucc();
    	predsucc.predecessor = 0;
    	predsucc.successor = 0;
    	Message message = new Message();
    	message.setTo(0);
    	message.setType(0); // 0 = find predecessor - successor
    	while(true) {
    		SendMessage(message);
    		try {
				message = ReceiveMessage();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
    		predsucc.successor = Integer.parseInt(message.getBody());
    		if(predsucc.successor > this.id || predsucc.successor == 0)
    			break;
    		else
    			predsucc.predecessor = predsucc.successor;
    	}
    	return predsucc;
    }

}