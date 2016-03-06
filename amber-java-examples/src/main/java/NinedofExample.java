import pl.edu.agh.amber.common.AmberClient;
import pl.edu.agh.amber.common.CyclicDataListener;
import pl.edu.agh.amber.ninedof.NinedofData;
import pl.edu.agh.amber.ninedof.NinedofProxy;

import java.io.IOException;
import java.util.Scanner;

/**
 * 9DOF sensor proxy example. Receives data cyclically and synchronously.
 *
 * @author Michał Konarski <konarski@student.agh.edu.pl>
 */
public class NinedofExample {

    public static void main(String[] args) {
        (new NinedofExample()).runDemo();
    }

    public void runDemo() {
        Scanner keyboard = new Scanner(System.in);
        System.out.print("IP (default: 127.0.0.1): ");
        String hostname = keyboard.nextLine();

        if ("".equals(hostname)) {
            hostname = "127.0.0.1";
        }

        AmberClient client;
        try {
            client = new AmberClient(hostname, 26233);

        } catch (IOException e) {
            System.out.println("Unable to connect to robot: " + e);
            return;
        }

        NinedofProxy ninedofProxy = new NinedofProxy(client, 0);

        try {

            // Synchronous receiving
            for (int i = 0; i < 10; i++) {
                NinedofData ninedofData = ninedofProxy.getAxesData(true, true, true);
                ninedofData.waitAvailable();

                System.out.println(String.format(
                        "accel: %d %d %d, gyro: %d %d %d, magnet: %d %d %d",
                        ninedofData.getAccel().xAxis,
                        ninedofData.getAccel().yAxis,
                        ninedofData.getAccel().zAxis,
                        ninedofData.getGyro().xAxis,
                        ninedofData.getGyro().yAxis,
                        ninedofData.getGyro().zAxis,
                        ninedofData.getMagnet().xAxis,
                        ninedofData.getMagnet().yAxis,
                        ninedofData.getMagnet().zAxis));

                Thread.sleep(10);
            }

            System.out.println("Now registering cyclic data listener...");
            Thread.sleep(1000);

            // Asynchronous receiving (with listener)
            ninedofProxy.registerNinedofDataListener(10, true, true, true,
                    new CyclicDataListener<NinedofData>() {

                        @Override
                        public void handle(NinedofData ninedofData) {
                            try {
                                System.out.println(String
                                        .format("accel: %d %d %d, gyro: %d %d %d, magnet: %d %d %d",
                                                ninedofData.getAccel().xAxis,
                                                ninedofData.getAccel().yAxis,
                                                ninedofData.getAccel().zAxis,
                                                ninedofData.getGyro().xAxis,
                                                ninedofData.getGyro().yAxis,
                                                ninedofData.getGyro().zAxis,
                                                ninedofData.getMagnet().xAxis,
                                                ninedofData.getMagnet().yAxis,
                                                ninedofData.getMagnet().zAxis));
                            } catch (Exception e) {
                                System.err.println("Exception occurred: " + e);
                            }
                        }
                    });

            Thread.sleep(10 * 1000);

        } catch (IOException e) {
            System.out.println("Error in sending a command: " + e);
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        } catch (Exception e) {
            System.err.println("Exception occurred: " + e);
        } finally {
            client.terminate();
        }
    }

}
