package chord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;

public class Master {

	private static Hashtable<Integer,ChordNode> chord;
	public int PORT = 49152;
	public String IPV4 = "127.0.0.1";
	public static int K = 5;
	public static String MODE = "seq_con";
	
	public Master() throws NoSuchAlgorithmException {
		
		chord = new Hashtable<Integer,ChordNode>();
		ChordNode chordNode = new ChordNode(0, 0, K);
		chord.put(0, chordNode);
		chordNode.start();
	}
	
	public int InsertNode(int num) throws NoSuchAlgorithmException {
		
		int id = Hash_SHA.SHA1(Integer.toString(num));
		if(chord.containsKey(id)){
			if(chord.get(id) != null)
				return -1;
		}
		ChordNode chordNode = new ChordNode(id, num, K);
		chord.put(id, chordNode);
		chordNode.start();
		return 1;
	}
	
	public int RemoveNode(int num) throws NoSuchAlgorithmException, InterruptedException, IOException {
		if (chord.size() < 2)
			return -1;
		int id = Hash_SHA.SHA1(Integer.toString(num));
		ChordNode chordNode = chord.get(id);
		System.out.println("master\t: Removing node " + id);
		chordNode.alive = false;
		chordNode.Server.close();
		chord.remove(id);
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
	
	public void InsertKeyValue(String key, String value) throws NoSuchAlgorithmException {
		
		Random rnd = new Random();
		rnd.setSeed(System.currentTimeMillis());
		int random_key = Math.abs(rnd.nextInt() % chord.size());
		int hash_key = Hash_SHA.SHA1(key);
		int random_node;
		int counter = 0;
		for (Iterator<Integer> iter = chord.keySet().iterator(); iter.hasNext(); ) {
			random_node = iter.next();
			if(random_key == counter) {
				Message message = new Message(random_node, -1, -1, value, hash_key, null, Type.INSERT);
				Messaging.SendMessage(message);
				break;
			}
			counter++;
    	}	
	}
	
	public void DeleteKey(String key) throws NoSuchAlgorithmException {
		
		Random rnd = new Random();
		rnd.setSeed(System.currentTimeMillis());
		int random_key = Math.abs(rnd.nextInt() % chord.size());
		int hash_key = Hash_SHA.SHA1(key);
		int random_node;
		int counter = 0;
		for (Iterator<Integer> iter = chord.keySet().iterator(); iter.hasNext(); ) {
			random_node = iter.next();
			if(random_key == counter) {
				Message message = new Message(random_node, -1, -1, null, hash_key, null, Type.DELETE);
				Messaging.SendMessage(message);
				break;
			}
			counter++;
    	}
	}
	
	public void Query(String key) throws NoSuchAlgorithmException {

		Message message;
		if (key.equals("*")) {
			if (MODE.equals("seq_con"))
				message = new Message(0, -1, 0, null, -1, null, Type.QUERYALL_SC);
			else
				message = new Message(0, -1, 0, null, -1, null, Type.QUERYALL);
			Messaging.SendMessage(message);
			return;
		}
		
		Random rnd = new Random();
		rnd.setSeed(System.currentTimeMillis());
		int random_key = Math.abs(rnd.nextInt() % chord.size());
		int hash_key = Hash_SHA.SHA1(key);
		int random_node;
		int counter = 0;
		for (Iterator<Integer> iter = chord.keySet().iterator(); iter.hasNext(); ) {
			random_node = iter.next();
			if(random_key == counter) {
				if (MODE.equals("seq_con"))
					message = new Message(random_node, -1, random_node, null, hash_key, null, Type.QUERY_SC);
				else 
					message = new Message(random_node, -1, random_node, null, hash_key, null, Type.QUERY);
				Messaging.SendMessage(message);
				break;
			}
			counter++;
    	}
	}
	
	public void TKanel() {
		int i, j;
		for (Iterator<Integer> iter = chord.keySet().iterator(); iter.hasNext(); ) {
			i = iter.next();
			System.out.println();
			for (int k = chord.get(i).predChain.size() - 1; k >= 0; k--) {
				j = chord.get(i).predChain.elementAt(k).id;
				System.out.print(j + "\t-->\t");
			}
			System.out.println("** " + i + " **\n");
		}
	}
	
	public void Fileread() throws FileNotFoundException, IOException, NoSuchAlgorithmException {
		File file = new File("/home/dimosthenis/Downloads/insert.txt");
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
		    String line;
		    String[] elems;
		    while ((line = br.readLine()) != null) {
		       elems = line.split(", ");
		       InsertKeyValue(elems[0], elems[1]);
		    }
		}
		
	}

}
