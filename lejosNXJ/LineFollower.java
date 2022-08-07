import lejos.nxt.*;

/**
 * This program will have the LejosNXT robot follow a line
 * by determining whether it is on the line (black) or off
 * the line (white) using Proportional control.
 * 
 * My car will follow the line on the left side.
 * 
 * @author shill
 */

public class LineFollower {	
	public static void main(String[] args) {
		//Declarations for each motor, the black and white sensor...
		MotorPort A = MotorPort.A;
		MotorPort C = MotorPort.C;
		BlackWhiteSensor bws = new BlackWhiteSensor(SensorPort.S3);
		//And each variable/constant in the Proportional control equation.
		int x;
		int y;
		int z;
		int Kp = 1;
		int speed = 65;
		//Calibrate what is black, what is white, then calc. the median
		bws.calibrate();
		y = bws.getThresholdValue();
		
		while(Button.LEFT.isUp()) {
			//Get the light value from the sensor
			x = bws.light();
			//Calculate the z value from the Proportional control equation.
			z = (int) (Kp * (y - x));
			//If the z value is within 1/10th of the threshold,
			if (Math.abs(z) < y/10) {
				//The motors will both go forward
				A.controlMotor(speed, 1);
				C.controlMotor(speed, 1);
			}
			//If the z value is less than 1/10th of the threshold,
			else if (z < 0) {
				//Pivot turn right (away from black)
				A.controlMotor(speed - z, 1);
				//It's minus z here because z is a negative number.
				C.controlMotor(0, 1);
			}
			//If the z value is less than 1/10th of the threshold,
			else if (z > 0) {
				//Pivot turn left (away from white)
				A.controlMotor(0, 1);
				C.controlMotor(speed + z, 1);
			}
		}
	}

}
