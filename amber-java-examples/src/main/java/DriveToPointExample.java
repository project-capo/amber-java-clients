import pl.edu.agh.amber.common.AmberClient;
import pl.edu.agh.amber.drivetopoint.DriveToPointProxy;
import pl.edu.agh.amber.drivetopoint.Location;
import pl.edu.agh.amber.drivetopoint.Point;
import pl.edu.agh.amber.drivetopoint.Result;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Drive to point proxy example.
 *
 * @author Pawel Suder <pawel@suder.info>
 */
public class DriveToPointExample {

    public static void main(String[] args) {
        (new DriveToPointExample()).runDemo();
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

        DriveToPointProxy driveToPointProxy = new DriveToPointProxy(client, 0);

        try {
            List<Point> targets = Arrays.asList(
                    new Point(2.44725, 4.22125, 0.25),
                    new Point(1.46706, 4.14285, 0.25),
                    new Point(0.67389, 3.76964, 0.25),
                    new Point(0.47339, 2.96781, 0.25));
            driveToPointProxy.setTargets(targets);

            while (true) {
                Result<List<Point>> resultNextTargets = driveToPointProxy.getNextTargets();
                Result<List<Point>> resultVisitedTargets = driveToPointProxy.getVisitedTargets();
                List<Point> nextTargets = resultNextTargets.getResult();
                List<Point> visitedTargets = resultVisitedTargets.getResult();
                System.out.println(String.format("next targets: %s, visited targets: %s", nextTargets.toString(), visitedTargets.toString()));
                Thread.sleep(1000);
            }

        } catch (IOException e) {
            System.out.println("Error in sending a command: " + e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.terminate();
        }
    }
}
