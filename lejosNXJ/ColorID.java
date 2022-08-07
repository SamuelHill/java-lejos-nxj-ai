import lejos.nxt.*;
import lejos.robotics.navigation.DifferentialPilot;

/**
 * This program will have the LejosNXT robot move forward, back,
 * and turn 4 times to determine the color of each square on the edges
 * of the starting square.
 * 
 * @author shill
 */

public class ColorID {
	public static void main(String[] args) {
		//Declarations
		DifferentialPilot sam = new DifferentialPilot(2.204f, 6.25f, Motor.A, Motor.C);
		sam.setTravelSpeed(5.0f);
		sam.setRotateSpeed(20.0f);
		ColorSensor cs = new ColorSensor(SensorPort.S4);
		
		LCD.clear();
		LCD.drawString("Press ESCAPE", 0, 0);
		LCD.drawString("to start...", 0, 1);
		//Wait for the ESCAPE key pressed
		Button.ESCAPE.waitForPressAndRelease();
		LCD.clear();
		
		if (Button.LEFT.isUp()) {
			for(int i = 0; i < 4; i++){
				sam.travel(4.0f);
				String colorName = colorNamer(cs);
				LCD.drawString(colorName, 0, i);
				sam.travel(-4.0f);
				sam.rotate(92.0f);
			}
		}
		
		LCD.drawString("Press ESCAPE", 0, 4);
		LCD.drawString("to end...", 0, 5);
		//Wait for the ESCAPE key pressed
		Button.ESCAPE.waitForPressAndRelease();
		LCD.clear();
	}
	
	public static String colorNamer(ColorSensor cs) {
		String colors[] = {"None", "Red", "Green", "Blue", "Yellow", "Megenta", "Orange",
			"White", "Black", "Pink", "Grey", "Light Grey", "Dark Grey", "Cyan"};
		ColorSensor.Color vals = cs.getColor();
		int enumColor = vals.getColor();
		if (enumColor == 2) {
			int rawR = cs.getRawColor().getRed();
			int rawG = cs.getRawColor().getGreen();
			if (rawR > rawG) {
				enumColor = 8;
			}
		}
		return colors[enumColor + 1];
	}
}
