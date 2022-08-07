import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;

public class Wander {
	public static void main(String[] args) throws Exception {
		LCD.clear();
		LCD.drawString("Here it goes:", 1, 2);
		LCD.drawString("Press ESCAPE", 1, 3);
		LCD.drawString("When ready", 1, 4);
		LCD.refresh();
		
		//Wait for the ESCAPE key pressed
		
		Button.ESCAPE.waitForPressAndRelease();
		TouchSensor t = new TouchSensor(SensorPort.S1);
		
		while(Button.LEFT.isUp()) {
			forward();
			if (t.isPressed()) {
				reverse(2000);
				pointTurn(820);
			}
		}
		stop();
	}
	
	public static void forward() {
		Motor.A.forward();
		Motor.C.forward();
	}
	
	public static void reverse(int length) {
		Motor.A.backward();
		Motor.C.backward();
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
