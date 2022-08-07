import lejos.nxt.*;
import lejos.robotics.navigation.DifferentialPilot;

/**
 * This program will have the LejosNXT robot navigate through a maze with
 * the "right hand rule". (Follow the right wall)
 * 
 * @author Samuel Hill
 */

public class MazeNavigator {
	// Definitions of motors (in cm), touch sensor, and ultra-sonic sensor.
	DifferentialPilot sam = new DifferentialPilot(6.0f, 10.0f, Motor.A, Motor.C);
	TouchSensor feels = new TouchSensor(SensorPort.S1);
	UltrasonicSensor eyes = new UltrasonicSensor(SensorPort.S4);
	
	public static void main(String[] args) {
		// Call to the "intelligence" of my program
		new MazeNavigator().go();
	}
	
	public void go() {
		// Set travel and rotation speeds
		sam.setTravelSpeed(12.0f);
		sam.setRotateSpeed(30.0f);
		
		// Start the maze navigation by pressing ESCAPE
		LCD.clear();
		LCD.drawString("Press ESCAPE", 0, 0);
		LCD.drawString("to start...", 0, 1);
		Button.ESCAPE.waitForPressAndRelease();
		LCD.clear();
		
		// While not pressing the LEFT button...
		while(Button.LEFT.isUp()) {
			// Go forward
			sam.forward();
			// If (while going forward) the touch sensor is pressed...
			if (feels.isPressed()) {
				// Stop, backup, and turn left.
				sam.stop();
				sam.travel(-10.0f);
				sam.rotate(91.0f);
			}
			// Else (if the touch sensor isn't pressed)...
			else {
				// Check the distance from the wall with the U-S sensor...
				int dist = eyes.getDistance();
				// If that distance is greater than 20 (to far from the wall)...
				if (dist > 20) {
					// Stop, move forward (to get away from the corner), turn right,
					// and move forward again (to back onto the wall).
					sam.stop();
					sam.travel(10.0f);
					sam.rotate(-91.0f);
					sam.travel(20.0f);
				}
			}
		}
	}
}