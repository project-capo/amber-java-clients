package pl.edu.agh.amber.roboclaw;

public class MotorsCommand {

	private int frontLeftSpeed = 0;
	private int frontRightSpeed = 0;
	private int rearLeftSpeed = 0;
	private int rearRightSpeed = 0;
	
	public MotorsCommand(int frontLeftSpeed, int frontRightSpeed, int rearLeftSpeed, int rearRightSpeed) {
		this.frontLeftSpeed = frontLeftSpeed;
		this.frontRightSpeed = frontRightSpeed;
		this.rearLeftSpeed = rearLeftSpeed;
		this.rearRightSpeed = rearRightSpeed;
	}
	
	public MotorsCommand() {
		
	}

	public int getFrontLeftSpeed() {
		return frontLeftSpeed;
	}

	public void setFrontLeftSpeed(int frontLeftSpeed) {
		this.frontLeftSpeed = frontLeftSpeed;
	}

	public int getFrontRightSpeed() {
		return frontRightSpeed;
	}

	public void setFrontRightSpeed(int frontRightSpeed) {
		this.frontRightSpeed = frontRightSpeed;
	}

	public int getRearLeftSpeed() {
		return rearLeftSpeed;
	}

	public void setRearLeftSpeed(int rearLeftSpeed) {
		this.rearLeftSpeed = rearLeftSpeed;
	}

	public int getRearRightSpeed() {
		return rearRightSpeed;
	}

	public void setRearRightSpeed(int rearRightSpeed) {
		this.rearRightSpeed = rearRightSpeed;
	}
	
	public void setAllSpeeds(int frontLeftSpeed, int frontRightSpeed, int rearLeftSpeed, int rearRightSpeed) {
		this.frontLeftSpeed = frontLeftSpeed;
		this.frontRightSpeed = frontRightSpeed;
		this.rearLeftSpeed = rearLeftSpeed;
		this.rearRightSpeed = rearRightSpeed;
	}
	
}
