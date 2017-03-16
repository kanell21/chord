package chord;

import java.awt.Canvas;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

public class Master {

	public static Hashtable<Integer,ChordNode> chord;
	public static int PORT = 49152;
	public static String IPV4 = "127.0.0.1";
	public static int K = 3;
	public static String MODE;
	private Canvas canvas;
	
	public Master(Canvas canvas) throws NoSuchAlgorithmException {
		
		MODE = "lin";
		chord = new Hashtable<Integer,ChordNode>();
		ChordNode chordNode = new ChordNode(0, 0, K, MODE);
		chord.put(0, chordNode);
		chordNode.start();
		this.canvas = canvas;
	}
	
	public int NodeJoin(int num) throws NoSuchAlgorithmException {
		
		int id = Hash_SHA.SHA1(Integer.toString(num));
		if(chord.containsKey(id)){
			if(chord.get(id) != null)
				return -1;
		}
		if (!available(PORT + id))
			return -1;
		ChordNode chordNode = new ChordNode(id, num, K, MODE);
		chord.put(id, chordNode);
		chordNode.start();
		canvas.repaint();
		return 1;
	}
	
	public int NodeDepart(int num) throws NoSuchAlgorithmException, InterruptedException, IOException {
		
		int id = Hash_SHA.SHA1(Integer.toString(num));
		if (chord.size() < 2 || !chord.containsKey(id))
			return -1;
		ChordNode chordNode = chord.get(id);
		System.out.println("master\t: Removing node " + id);
		chordNode.alive = false;
		chordNode.Server.close();
		chord.remove(id);
		canvas.repaint();
		return 1;
	}
	
	public void CheckAlive(int num) throws NoSuchAlgorithmException {
		
		int id = Hash_SHA.SHA1(Integer.toString(num));
		
		if(chord.containsKey(id)){
			if(chord.get(id).alive == true)
				System.out.println("master\t: Node "+ num + " is alive. It's id is "+ id);
			else {
				System.out.println("master\t: Node "+ num + " is dead. It's id was "+ id);
				chord.remove(id);
			}
		}
		else
			System.out.println("master\t: Node "+ num + " is dead. It's id was "+ id);
		return;
	}
	
	public void InsertKeyValue(String key, String value, boolean timer) throws NoSuchAlgorithmException {
		
		Random rnd = new Random();
		rnd.setSeed(System.currentTimeMillis());
		int random_key = Math.abs(rnd.nextInt() % chord.size());
		int hash_key = Hash_SHA.SHA1(key);
		int random_node;
		int counter = 0;
		for (Iterator<Integer> iter = chord.keySet().iterator(); iter.hasNext(); ) {
			random_node = iter.next();
			if(random_key == counter) {
				Message message = new Message(random_node, -1, random_node, value, hash_key, null, Type.INSERT, timer);
				Messaging.SendMessage(message);
				break;
			}
			counter++;
    	}
		return;
	}
	
	public void DeleteKey(String key, boolean timer) throws NoSuchAlgorithmException {
		
		Random rnd = new Random();
		rnd.setSeed(System.currentTimeMillis());
		int random_key = Math.abs(rnd.nextInt() % chord.size());
		int hash_key = Hash_SHA.SHA1(key);
		int random_node;
		int counter = 0;
		for (Iterator<Integer> iter = chord.keySet().iterator(); iter.hasNext(); ) {
			random_node = iter.next();
			if(random_key == counter) {
				Message message = new Message(random_node, -1, random_node, null, hash_key, null, Type.DELETE, timer);
				Messaging.SendMessage(message);
				break;
			}
			counter++;
    	}
		return;
	}
	
	public void Query(String key, boolean timer/*, int qid*/) throws NoSuchAlgorithmException {

		Message message;
		Random r = new Random();
		int random_key = r.nextInt(chord.size());
		int hash_key = Hash_SHA.SHA1(key);
		int random_node;
		int counter = 0;
		for (Iterator<Integer> iter = chord.keySet().iterator(); iter.hasNext(); ) {
			random_node = iter.next();
			if(random_key == counter) {
				if (MODE.equals("ev_con") && key.equals("*"))
					message = new Message(random_node, -1, random_node, null/*Integer.toString(qid)*/, -1, new Hashtable<Integer,String>(), Type.QUERYALL_EC, timer);
				else if (!MODE.equals("ev_con") && key.equals("*"))
					message = new Message(random_node, -1, random_node, null/*Integer.toString(qid)*/, -1, new Hashtable<Integer,String>(), Type.QUERYALL, timer);
				else if (MODE.equals("ev_con"))
					message = new Message(random_node, -1, random_node, null/*Integer.toString(qid)*/, hash_key, null, Type.QUERY_EC, timer);
				else
					message = new Message(random_node, -1, random_node, null/*Integer.toString(qid)*/, hash_key, null, Type.QUERY, timer);
				Messaging.SendMessage(message);
				break;
			}
			counter++;
    	}
		return;
	}
	
	public void TKanel() {
		
		int i, j;
		System.out.println("Replica chains:");
		for (Iterator<Integer> iter = chord.keySet().iterator(); iter.hasNext(); ) {
			i = iter.next();
			for (int k = chord.get(i).predChain.size() - 1; k >= 0; k--) {
				j = chord.get(i).predChain.elementAt(k).id;
				System.out.print(j + "  ->  ");
			}
			System.out.println("** " + i + " **\n");
		}
		return;
	}
	
	public void Fileread(String path) throws FileNotFoundException, IOException, NoSuchAlgorithmException {
		
		Vector<String> filelines = new Vector<String>();
		String line;
		BufferedReader reader = new BufferedReader(new FileReader(path));

		while ((line = reader.readLine()) != null) {
			filelines.add(line);
		}
		reader.close();

		String[] elems;
		System.out.println(filelines.size() + "-line file");
		Timer timer = new Timer(filelines.size());
		timer.start();
		
		reader = new BufferedReader(new FileReader(path));
		//int i = 1;
		while((line = reader.readLine()) != null) {
		    elems = line.split(", ");
		    if (elems.length == 1)
		    	Query(elems[0], true/*, i++*/);
		    else if (elems[0].equals("query"))
			    Query(elems[1], true/*, i++*/);
		    else if (elems[0].equals("insert"))
			    InsertKeyValue(elems[1], elems[2], true);
		    else if (elems[0].equals("delete"))
		 	    DeleteKey(elems[1], true);
		    else 
			    InsertKeyValue(elems[0], elems[1], true);
			try {
				Thread.sleep(4);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return;
	}
	
	public static boolean available(int port) {
	    if (port < PORT || port > PORT + 1024) {
	        throw new IllegalArgumentException("Invalid start port: " + port);
	    }

	    ServerSocket ss = null;
	    DatagramSocket ds = null;
	    try {
	        ss = new ServerSocket(port);
	        ss.setReuseAddress(true);
	        ds = new DatagramSocket(port);
	        ds.setReuseAddress(true);
	        return true;
	    } catch (IOException e) {
	    } finally {
	        if (ds != null) {
	            ds.close();
	        }
	        if (ss != null) {
	            try {
	                ss.close();
	            } catch (IOException e) {
	                /* should not be thrown */
	            }
	        }
	    }
	    return false;
	}

}
