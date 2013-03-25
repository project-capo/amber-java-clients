import java.io.IOException;

import pl.edu.agh.amber.common.AmberClient;
import pl.edu.agh.amber.roboclaw.RoboclawProxy;

public class RoboclawExample {
	
	public static void main(String[] args) {
		(new RoboclawExample()).runDemo();
	}
	
	public void runDemo() {

		AmberClient client;
		try {
			client = new AmberClient("192.168.1.50", 26233);
		} catch (IOException e) {
			System.out.println("Unable to connect to robot: " + e);
			return;
		}
		
		RoboclawProxy roboclawProxy = new RoboclawProxy(client, 0);
				
		try {
			roboclawProxy.sendMotorsCommand(1000, 1000, 2000, 2000);
			
			Thread.sleep(1000);
			
		} catch (IOException e) {
			System.out.println("Error in sending a command: " + e);
		} catch (InterruptedException e) {
			
		} finally {
			client.terminate();
		}
	}
}