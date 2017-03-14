package chord;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
 
public class Hash_SHA {
 
    /**
     * @param args
     * @throws NoSuchAlgorithmException 
     */
    public static int SHA1(String number) throws NoSuchAlgorithmException {
    	
         int i;
         byte[] hash_result = sha1(number);
         ByteBuffer b = ByteBuffer.allocate(40);
         b.putInt(0x00000000000000000000000000000000000003FF);
         byte[] mask=b.array();

         for(i=0;i<hash_result.length;i++){
             hash_result[i]=(byte) (hash_result[i] & mask[i]);  
         }
         
         ByteBuffer c = ByteBuffer.wrap(hash_result);
         int id=c.getInt();
         return id;
    }
     
    static byte[] sha1(String input) throws NoSuchAlgorithmException {
    	
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        return result;
    }

}