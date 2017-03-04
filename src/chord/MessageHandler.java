package chord;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MessageHandler extends Thread{
	
	private Message msg;
	
	private ChordNode node;
	
	public MessageHandler(Message msg,ChordNode node){
		this.msg=msg;
	    this.node=node;
	}
	
	
	public void run(){
	
	        Type type;
	        type=msg.getType();
	        Message message;
	        
	        switch(type){
	            case PRED_SUCC_REQUEST:
                    System.out.println(node.id + "\t: Received Message of type PRED_SUCC_REQUEST");
                    message = new Message(msg.getReplyTo(), node.id, -1, Integer.toString(node.successor), -1, Type.PRED_SUCC_REPLY);
                    SendMessage(message);
                    break;
                    
                case PRED_SUCC_REPLY:
                	System.out.println(node.id + "\t: Received Message of type PRED_SUCC_REPLY");
                    message = new Message(Integer.parseInt(msg.getBody()), node.id, node.id, null, -1, Type.PRED_SUCC_REQUEST);
                    SendMessage(message);
                    break;
                         
                case NEW_PRED_INFORM:
                	System.out.println(node.id + "\t: Received Message of type NEW_PRED_INFORM");
                	node.predecessor = Integer.parseInt(msg.getBody());
                    break;
                
                case NEW_SUCC_INFORM:
                	System.out.println(node.id + "\t: Received Message of type NEW_SUCC_INFORM");
                	node.successor = Integer.parseInt(msg.getBody());
                    break;    
                    
                default:
                    System.out.println("Midweek days are so-so.");
                    break;

	        }	        
	}     
	        
	private void SendMessage(Message message) {
    	
    	Socket echoSocket;
		try {
			echoSocket = new Socket(node.IPV4, node.PORT + message.getTo());
	    	ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(echoSocket.getOutputStream()));
			oos.writeObject(message);
			oos.flush();
	        System.out.println(node.id + "\t: Message sent to port-node " + message.getTo() + " : " + message.getBody()); 
	        echoSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }        

}
