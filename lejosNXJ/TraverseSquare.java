import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;

public class TraverseSquare {
	public static void main(String[] args) throws Exception {
		LCD.clear();
		LCD.drawString("Here it goes:", 1, 2);
		LCD.drawString("Press ESCAPE", 1, 3);
		LCD.drawString("When ready", 1, 4);
		LCD.refresh();
		
		//Wait for the ESCAPE key pressed
		
		Button.ESCAPE.waitForPressAndRelease();
		
		for(int i = 0; i < 4; i++) {
			forward(3000);
			pointTurn(820);
		}
		stop();
	}
	
	public static void forward(int length) {
		Motor.A.forward();
		Motor.C.forward();
		try { Thread.sleep(length); }
		catch(Exception e) {};
	}

	public static void pointTurn(int length) {
		Motor.A.forward();
		Motor.C.backward();
		try { Thread.sleep(length); }
		catch(Exception e) {};
	}
	
	public static void stop() {
		Motor.A.stop();
		Motor.C.stop();
	}
}
