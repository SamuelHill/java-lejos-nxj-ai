import lejos.nxt.*;
import lejos.robotics.subsumption.*;
import lejos.robotics.navigation.DifferentialPilot;
import java.util.Random;

public class ObstacleCourse {
	static DifferentialPilot sam = new DifferentialPilot(6.0f, 10.75f, Motor.A, Motor.C);
	static UltrasonicSensor eeik = new UltrasonicSensor(SensorPort.S1);
	static TouchSensor feels = new TouchSensor(SensorPort.S3);
	static ColorSensor eyes = new ColorSensor(SensorPort.S4);
	public static void main(String[] args) {
		sam.setTravelSpeed(10.0f);
		sam.setRotateSpeed(25.0f);
		Behavior b1 = new DriveForward();
		Behavior b2 = new ObstacleChecker();
		Behavior b3 = new GoalFinder();
		Behavior b4 = new EscapeSequence();
		Behavior [] bArray = {b1, b2, b3, b4};
		Arbitrator arby = new Arbitrator(bArray);
		arby.start();
	}
}

class DriveForward implements Behavior {
	private boolean suppressed = false;
	
	public boolean takeControl() {
		return true;
	}
	
	public void suppress() {
		suppressed = true;
	}
	
	public void action() {
		suppressed = false;
		ObstacleCourse.sam.forward();
		while (!suppressed) {
			Thread.yield( );
		}
		ObstacleCourse.sam.stop();
	}
}

class ObstacleChecker implements Behavior {
	private boolean suppressed = false;
	Random generator = new Random();
	
	public boolean takeControl() {
		int dist = ObstacleCourse.eeik.getDistance();
		int color = ObstacleCourse.eyes.getColor().getColor();
		return ObstacleCourse.feels.isPressed() || (dist < 8) || (color != ColorSensor.WHITE);
	}
	
	public void suppress() {
		suppressed = true;
	}
	
	public void action() {
		suppressed = false;
		int direction = generator.nextInt(2);
		ObstacleCourse.sam.stop();
		ObstacleCourse.sam.travel(-6.0f);
		if (direction == 0) {
			ObstacleCourse.sam.rotate(90.0f);
		}
		if (direction == 1) {
			ObstacleCourse.sam.rotate(-90.0f);
		}
		while(ObstacleCourse.sam.isMoving() && !suppressed) {
			Thread.yield();
		}
		ObstacleCourse.sam.stop();
	}
}

class GoalFinder implements Behavior {
	private boolean suppressed = false;
	
	public boolean takeControl() {
		int color = ObstacleCourse.eyes.getColor().getColor();
		return color == 3;
	}
	
	public void suppress() {
		suppressed = true;
	}
	
	public void action() {
		suppressed = false;
		Sound.beepSequenceUp();
		System.exit(0);
	}
}

class EscapeSequence implements Behavior {
	private boolean suppressed = false;
	
	public boolean takeControl() {
		return Button.ESCAPE.isDown();
	}
	
	public void suppress() {
		suppressed = true;
	}
	
	public void action() {
		suppressed = false;
		Sound.beepSequence();
		System.exit(0);
	}
}