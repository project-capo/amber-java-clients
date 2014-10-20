import pl.edu.agh.amber.common.AmberClient;
import pl.edu.agh.amber.roboclaw.MotorsCurrentSpeed;
import pl.edu.agh.amber.roboclaw.RoboclawProxy;

import java.io.IOException;

/**
 * RoboClaw proxy example. Sends motors command and gets current motor's speed.
 *
 * @author Michał Konarski <konarski@student.agh.edu.pl>
 */
public class RoboclawExample {

    public static void main(String[] args) {
        (new RoboclawExample()).runDemo();
    }

    public void runDemo() {

        AmberClient client;
        try {
            client = new AmberClient("127.0.0.1", 26233);

        } catch (IOException e) {
            System.out.println("Unable to connect to robot: " + e);
            return;
        }

        RoboclawProxy roboclawProxy = new RoboclawProxy(client, 0);

        final int speed = 100;
        try {
            roboclawProxy.sendMotorsCommand(speed, speed, speed, speed);

            Thread.sleep(5000);

            MotorsCurrentSpeed mcs = roboclawProxy.getCurrentMotorsSpeed();
            mcs.waitAvailable();

            System.out.println(String.format(
                    "Motors current speed: fl: %d, fr: %d, rl: %d, rr: %d",
                    mcs.getFrontLeftSpeed(), mcs.getFrontRightSpeed(),
                    mcs.getRearLeftSpeed(), mcs.getRearRightSpeed()));

        } catch (IOException e) {
            System.out.println("Error in sending a command: " + e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.terminate();
        }
    }
}