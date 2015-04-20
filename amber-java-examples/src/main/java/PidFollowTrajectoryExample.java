import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import pl.edu.agh.amber.common.AmberClient;
import pl.edu.agh.amber.pidfollowtrajectory.Configuration;
import pl.edu.agh.amber.pidfollowtrajectory.PidFollowTrajectoryProxy;
import pl.edu.agh.amber.pidfollowtrajectory.Point;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class PidFollowTrajectoryExample {
    private static final String PATH_FILE = "pid_paths/trasa_mniejszy_prostokat.csv";

    public static void main(String[] args) throws IOException {
        (new PidFollowTrajectoryExample()).runDemo();
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

        PidFollowTrajectoryProxy pidFollowTrajectoryProxy = new PidFollowTrajectoryProxy(client, 0);

        try {
            Configuration configuration = new Configuration(0.6, 0.95, 0.05, 0.5, 0.35, 8, false, 6.0, 1000);

            List<Point> targets = readTargetsFromCSV(PATH_FILE);
            pidFollowTrajectoryProxy.setTargets(targets, configuration);
            pidFollowTrajectoryProxy.startExecution();
            keyboard.nextLine();
            pidFollowTrajectoryProxy.stopExecution();
        } catch (IOException e) {
            System.out.println("Error in sending a command: " + e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.terminate();
        }
    }

    private static List<Point> readTargetsFromCSV(String filename) throws IOException {
        List<Point> points = new LinkedList<Point>();

        URL pathFileURL = ClassLoader.getSystemResource(filename);
        CSVParser parser = CSVParser.parse(pathFileURL, Charset.forName("UTF-8"), CSVFormat.DEFAULT.withCommentMarker('#'));
        for (CSVRecord record : parser.getRecords()){
            points.add(new Point(Double.parseDouble(record.get(0)), Double.parseDouble(record.get(1)), 0));
        }

        return points;
    }

    private static List<Point> defaultPath(){
        return Arrays.asList(
                new Point(0.431469, 4.735101, 0),
                new Point(0.489452, 3.861686, 0),
                new Point(0.455830, 3.078262, 0),
                new Point(0.449974, 2.342394, 0),
                new Point(0.439255, 1.393628, 0),
                new Point(0.928279, 1.298266, 0),
                new Point(1.392740, 1.278608, 0),
                new Point(1.479275, 0.963965, 0),
                new Point(1.594501, 2.573211, 0),
                new Point(1.582556, 3.509170, 0),
                new Point(1.050480, 3.594206, 0),
                new Point(0.469069, 3.662022, 0)
        );
    }
}
