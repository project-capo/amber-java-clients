import pl.edu.agh.amber.common.AmberClient;
import pl.edu.agh.amber.location.LocationProxy;
import pl.edu.agh.amber.location.LocationCurrent;

import java.io.IOException;

/**
 * Location proxy example.
 * 
 * @author szsz <szsz@agh.edu.pl>
 */
public class LocationExample {

	public static void main(String[] args) {
		(new LocationExample()).runDemo();
	}

	public void runDemo() {

		AmberClient client;
		try {
			client = new AmberClient("127.0.0.1", 26233);

		} catch (IOException e) {
			System.out.println("Unable to connect to robot: " + e);
			return;
		}

		LocationProxy locationProxy = new LocationProxy(client, 0);

		try {

			LocationCurrent lok = locationProxy.getCurrentLocation();
			lok.waitAvailable();

			System.out.println(String.format("Current location: X: %e, Y: %e, Alfa: %e, P: %e, TimeStamp: %e",
									lok.getX(), lok.getY(), lok.getAngle(),
									lok.getP(), lok.getTimeStamp()));

		} catch (IOException e) {
			System.out.println("Error in sending a command: " + e);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			client.terminate();
		}
	}
}