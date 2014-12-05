package pl.edu.agh.amber.drivetopoint;

public class Point {
    public final double x, y, r;

    public Point(double x, double y, double r) {
        this.x = x;
        this.y = y;
        this.r = r;
    }

    public String toString() {
        return String.format("target: %f, %f, %f", x, y, r);
    }
}
