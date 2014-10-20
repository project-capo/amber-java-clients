import pl.edu.agh.amber.common.AmberClient;
import pl.edu.agh.amber.common.CyclicDataListener;
import pl.edu.agh.amber.hokuyo.HokuyoProxy;
import pl.edu.agh.amber.hokuyo.Scan;

import java.io.IOException;

/**
 * Hokuyo sensor proxy example.
 *
 * @author Paweł Suder <psuder@student.agh.edu.pl>
 */
public class HokuyoExample {

    public static void main(String[] args) {
        (new HokuyoExample()).runDemo();
    }

    public void runDemo() {
        AmberClient client;
        try {
            client = new AmberClient("127.0.0.1", 26233);

        } catch (IOException e) {
            System.out.println("Unable to connect to robot: " + e);
            return;
        }

        HokuyoProxy hokuyoProxy = new HokuyoProxy(client, 0);

        try {

            // Synchronous receiving
            Scan singleScan = hokuyoProxy.getSingleScan();
            System.err.println(singleScan.getPoints());

            System.out.println("Now registering cyclic data listener...");
            Thread.sleep(1000);

            // Asynchronous receiving (with listener)
            hokuyoProxy.registerMultiScanListener(new CyclicDataListener<Scan>() {
                @Override
                public void handle(Scan data) {
                    try {
                        System.out.println(data.getPoints());
                    } catch (Exception e) {
                        System.err.println("Exception occurred: " + e);
                    }
                }
            });

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