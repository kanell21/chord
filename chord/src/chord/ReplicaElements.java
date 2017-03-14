package chord;

import java.io.Serializable;
import java.util.Hashtable;

public class ReplicaElements implements Serializable {

	private static final long serialVersionUID = 3680761781049059774L;
	public int id;
	public Hashtable<Integer,String> hashtable;
	
	public ReplicaElements(int id, Hashtable<Integer,String> hashtable) {
		this.id = id;
		this.hashtable = hashtable;
	}
}
