import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.robotics.navigation.*;

public class PennyTriangle {
	public static void main(String[] args) throws Exception {
		LCD.clear();
		LCD.drawString("Here it goes:", 1, 2);
		LCD.drawString("Press ESCAPE", 1, 3);
		LCD.drawString("When ready", 1, 4);
		LCD.refresh();
		
		Button.ESCAPE.waitForPressAndRelease();
		
		DifferentialPilot sam = new DifferentialPilot(2.204f, 6.25f, Motor.A, Motor.C);
		
		sam.setTravelSpeed(8.0f);
		sam.setRotateSpeed(12.0f);
		sam.travel(48.0f);
		sam.rotate(-135.0f);
		sam.travel(67.9f);
		sam.rotate(-135.0f);
		sam.travel(48.0f);
		sam.stop();
	}
}
