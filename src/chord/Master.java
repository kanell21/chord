package chord;

import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;

public class Master {

	private static Hashtable<Integer,Boolean> chord;
	
	public Master() throws NoSuchAlgorithmException {
		
		chord = new Hashtable<Integer,Boolean>();
		chord.put(0, true);
		ChordNode chordNode = new ChordNode(0, 0);
		chordNode.start();
	}
	
	public int InsertNode(int num) throws NoSuchAlgorithmException {
		
		int id = Hash_SHA.SHA1(Integer.toString(num));
		if(chord.containsKey(id)){
			if(chord.get(id))
				return -1;
		}
		chord.put(id, true);
		ChordNode chordNode = new ChordNode(id, num);
		chordNode.start();
		return 1;
	}
	
	public void RemoveNode(int num) throws NoSuchAlgorithmException {
		
		int id = Hash_SHA.SHA1(Integer.toString(num));
		chord.put(id, false);
		return;
	}
	
	public void CheckAlive(int num) throws NoSuchAlgorithmException {
		
		int id = Hash_SHA.SHA1(Integer.toString(num));
		
		if(chord.containsKey(id)){
			if(chord.get(id))
				System.out.println("Node "+ num + " is alive. It's id is "+ id);
			else
				System.out.println("Node "+ num + " is dead. It's id was "+ id);
		}
		else
			System.out.println("Node "+ num + " is dead. It's id was "+ id);
		return;
	}
	
}
