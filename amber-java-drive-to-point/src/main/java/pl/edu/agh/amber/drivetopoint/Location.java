package pl.edu.agh.amber.drivetopoint;

public class Location {

    private double X;
    private double Y;
    private double angle;
    private double P;
    private double timeStamp;

    public Location(double x, double y, double angle, double p, double timeStamp) {
        this.X = x;
        this.Y = y;
        this.angle = angle;
        this.P = p;
        this.timeStamp = timeStamp;
    }

    public double getX() {
        return X;
    }

    public double getY() {
        return Y;
    }

    public double getAngle() {
        return angle;
    }

    public double getP() {
        return P;
    }

    public double getTimeStamp() {
        return timeStamp;
    }
}
