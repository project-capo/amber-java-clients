package pl.edu.agh.amber.location;

import pl.edu.agh.amber.common.FutureObject;

public class LocationCurrent extends FutureObject {

	private double X;
	private double Y;
	private double angle;
	private double P;
	private double timeStamp;

	public LocationCurrent() {

	}

	public LocationCurrent(double x, double y, double angle, double p,
			double timeStamp) {
		this.X = x;
		this.Y = y;
		this.angle = angle;
		this.P = p;
		this.timeStamp = timeStamp;
	}

	public double getX() throws Exception {
		return X;
	}

	public void setX(double x) {
		this.X = x;
	}

	public double getY() throws Exception {
		return Y;
	}

	public void setY(double y) {
		this.Y = y;
	}

	public double getAngle() throws Exception {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public double getP() throws Exception {
		return P;
	}

	public void setP(double p) {
		this.P = p;
	}

	public double getTimeStamp() throws Exception {
		return timeStamp;
	}

	public void setTimeStamp(double timeStamp) {
		this.timeStamp = timeStamp;
	}
}
