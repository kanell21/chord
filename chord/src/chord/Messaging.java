package chord;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Messaging {

    public static int PORT = 49152;
    public static String IPV4 = "127.0.0.1";
	
	public static void SendMessage(Message message) {
    	
    	Socket echoSocket;
		try {
			echoSocket = new Socket(IPV4, PORT + message.getTo());
	    	ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(echoSocket.getOutputStream()));
			oos.writeObject(message);
			oos.flush();
//	        System.out.println(message.getFrom() + "\t: Message sent to port-node " + message.getTo() + " : " + message.getBody()); 
	        echoSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    } 
}
