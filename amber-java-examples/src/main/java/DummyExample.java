import pl.edu.agh.amber.common.AmberClient;
import pl.edu.agh.amber.common.CyclicDataListener;
import pl.edu.agh.amber.dummy.DummyProxy;
import pl.edu.agh.amber.dummy.Status;

import java.io.IOException;

/**
 * Dummy sensor proxy example.
 *
 * @author Paweł Suder <psuder@student.agh.edu.pl>
 */
public class DummyExample {

    public static void main(String[] args) {
        (new DummyExample()).runDemo();
    }

    public void runDemo() {
        AmberClient client;
        try {
            client = new AmberClient("127.0.0.1", 26233);

        } catch (IOException e) {
            System.out.println("Unable to connect to robot: " + e);
            return;
        }

        DummyProxy dummyProxy = new DummyProxy(client, 0);

        try {

            // Synchronous receiving
            dummyProxy.setEnable(true);
            dummyProxy.setMessage("Message");

            Status status = dummyProxy.getStatus();
            System.err.println(status);

            System.out.println("Now registering cyclic data listener...");
            Thread.sleep(1000);

            // Asynchronous receiving (with listener)
            dummyProxy.subscribe(new CyclicDataListener<String>() {
                @Override
                public void handle(String data) {
                    System.err.println(data);
                }
            });
            Thread.sleep(10000);

        } catch (IOException e) {
            System.out.println("Error in sending a command: " + e);
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.terminate();
        }
    }
}