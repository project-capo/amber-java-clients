package pl.edu.agh.amber.hokuyo;

import java.util.Arrays;
import java.util.List;

public class AngleRange {

    private double[] angles;

    private int fromIndex;

    private int toIndex;

    public AngleRange(List<Double> originalAngles, double fromAngle, double toAngle, double clockwiseAngleRotation) {
        if (angles != null) {
            return;
        }

        double couterclockwiseRotation = 360.0 - clockwiseAngleRotation;

        angles = new double[originalAngles.size()];
        int index = 0;
        for (Double value : originalAngles) {
            angles[index++] = (value + couterclockwiseRotation) % 360;
        }

        fromIndex = Arrays.binarySearch(angles, fromAngle);
        toIndex = Arrays.binarySearch(angles, toAngle);
    }

    public int getFromIndex() {
        return fromIndex;
    }

    public int getToIndex() {
        return toIndex;
    }

    public double getAngle(int relativeIndex) {
        return angles[fromIndex + relativeIndex];
    }

}
