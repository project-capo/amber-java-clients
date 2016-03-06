import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import pl.edu.agh.amber.common.AmberClient;
import pl.edu.agh.amber.roboclaw.RoboclawProxy;

public class MultipleRobotsExample {
	
	// set which robots should be controlled
	private static final int[] ROBOTS = {1, 2, 3, 4, 5};
	
	private static final int PORT = 26233;
	
	Map<Integer, AmberClient> clients = new HashMap<>();
	Map<Integer, RoboclawProxy> proxies = new HashMap<>();

	public static void main(String[] args) {
		(new MultipleRobotsExample()).runDemo();
	}

	private void terminateClients() {
		for (AmberClient client: clients.values()) {
			client.terminate();
		}
	}
	
	private void initClientsAndProxies() {
		for (int robotId: ROBOTS) { 
			int lastOctet = 200 + robotId;
			
			AmberClient client;
			try {
				client = new AmberClient("192.168.2." + lastOctet, PORT);
				clients.put(robotId, client);
				
				RoboclawProxy roboclawProxy = new RoboclawProxy(client, 0);
				proxies.put(robotId, roboclawProxy);
				
			} catch (IOException e) {
				System.err.println("IOException in client " + robotId + ", terminating all.");
				terminateClients();
				System.exit(1);
			}
		}
	}
	
	private void setSpeedForAll(int frontLeftSpeed, int frontRightSpeed, int rearLeftSpeed, int rearRightSpeed) {
		
		for (Entry<Integer, RoboclawProxy> entry: proxies.entrySet()) {
			
			int robotId = entry.getKey();
			RoboclawProxy roboclawProxy = entry.getValue();
			
			try {
				roboclawProxy.sendMotorsCommand(frontLeftSpeed, frontRightSpeed, rearLeftSpeed, rearRightSpeed);
			} catch (IOException e) {
				System.err.println("IOException in proxy: " + robotId);
			}
		}
	}
	
	public void runDemo() {

		initClientsAndProxies();

		setSpeedForAll(1000, 1000, 1000, 1000);
		
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
		}
		
		terminateClients();
	}
}