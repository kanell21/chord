package chord;

import java.io.IOException;
import java.net.ServerSocket;
import java.text.DecimalFormat;

public class Timer extends Thread {
	
	private int operations;
	private long sTime;
	private long time;
	
	@Override
	public void run(){
		try {
			ServerSocket Server = new ServerSocket(50176);
			sTime = System.currentTimeMillis();
			for (int i = 0; i < operations; i++) {
				Server.accept();
			}
			time = System.currentTimeMillis() - sTime;
			DecimalFormat df = new DecimalFormat("#.##");
			System.out.println("Time\t= " + time + " ms\nThroughput\t= " + df.format((double)((1000 * (double)operations) / (double)time)) + " ops/s");
			Server.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Timer(int operations) {
		this.operations = operations;
	}
	
}
