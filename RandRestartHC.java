import lejos.nxt.*;
import lejos.robotics.navigation.DifferentialPilot;
import java.util.Random;

/**
 * This program will have the LejosNXT robot move across the board of colors
 * using the random restart hill-climber search algorithm, as well as an aspect
 * of basic Tabu (the robot will never go backwards). This works well under the
 * assumption that the goal state is across the board and not somewhere else.
 * 
 * @author Samuel Hill
 */

public class RandRestartHC {
	DifferentialPilot sam = new DifferentialPilot(2.204f, 6.25f, Motor.A, Motor.C);
	ColorSensor cs = new ColorSensor(SensorPort.S4);
	private double left = 91.6f;
	private double right = -91.6f;
	private double fwd = 8.0f;
	Random generator = new Random();
	
	public static void main(String[] args) {
		new RandRestartHC().go();
	}
	
	public void go() {
		sam.setTravelSpeed(5.0f);
		sam.setRotateSpeed(20.0f);
		int curVal = 10; // Initialized to White
		
		LCD.clear();
		LCD.drawString("Press ESCAPE", 0, 0);
		LCD.drawString("to start...", 0, 1);
		Button.ESCAPE.waitForPressAndRelease();
		LCD.clear();
		
		while (true) {
			int directAndHeuristic[] = neighborhoodPatrol();
			int direction = directAndHeuristic[0];
			int heuristic = directAndHeuristic[1];
			
			if (curVal < heuristic) { // If a run fails
				Sound.beep();
				// Reset the heuristic and go a random direction.
				heuristic = 10;
				direction = generator.nextInt(2);
			}
			
			if (direction == 0) { // East
				sam.rotate(right);
				sam.travel(fwd);
				sam.rotate(left);
				curVal = heuristic;
			}
			else if (direction == 1) { // North
				sam.travel(fwd);
				curVal = heuristic;
			}
			else if (direction == 2) { // West
				sam.rotate(left);
				sam.travel(fwd);
				sam.rotate(right);
				curVal = heuristic;
			}
			
			if (curVal == 0) { // Goal!
				LCD.clear();
				LCD.drawString("GOAL!!!", 0, 0);
				LCD.drawString("Press ESCAPE", 0, 1);
				LCD.drawString("to exit...", 0, 2);
				Sound.beepSequenceUp();
				Button.ESCAPE.waitForPressAndRelease();
				LCD.clear();
				break;
			}
		}
	}
	
	public int[] neighborhoodPatrol() {
		int[] neighbors = new int[3];
		// Neighbors are in the order 0 - E, 1 - N, 2 - W.
		sam.rotate(right);
		for(int i = 0; i < 3; i++){
			sam.travel(4.0f);
			neighbors[i] = colorGrabber();
			sam.travel(-4.0f);
			if (i != 2) {
				sam.rotate(left);
			}
		}
		sam.rotate(right);
		// South is never checked because it will always be a higher heuristic.
		return bestNeighbor(neighbors);
	}
	
	public int colorGrabber() {
		// Get the current color as a Color object
		ColorSensor.Color vals = cs.getColor();
		// Get the enumerated color value (-1 - 12)
		int enumColor = vals.getColor();
		// If the enum value is 2 (Blue)...
		if (enumColor == 2) {
			// Get the raw red and green values
			int rawR = cs.getRawColor().getRed();
			int rawG = cs.getRawColor().getGreen();
			// If red is greater than the green
			if (rawR > rawG) {
				// Change the color to pink.
				enumColor = 8;
			}
		}
		// Convert the enum value to the heuristic value
		int heuristicValue = heuristicConverter(enumColor);
		// Return the heuristic value
		return heuristicValue;
	}
	
	public int heuristicConverter(int enumColor) {
		int heuristicValue = 0;
		// Convert the enum value to the given heuristic values
		switch (enumColor) {
			case 6: heuristicValue = 10;
				break;
			case 8: heuristicValue = 9;
				break;
			case 7: heuristicValue = 8;
				break;
			case 3: heuristicValue = 6;
				break;
			case 2: heuristicValue = 5;
				break;
			case 1: heuristicValue = 4;
				break;
			case 0: heuristicValue = 0;
				break;
		}
		return heuristicValue;
	}
	
	public int[] bestNeighbor(int neighbors[]) {
		int best = neighbors[0];
		int direction = 0;
		// Loop through the neighbors and find the lowest heuristic value
		for (int i = 0; i < 3; i++) {
			if (neighbors[i] < best) {
				best = neighbors[i];
				direction = i;
			}
		}
		int[] directAndHeuristic = {direction, neighbors[direction]};
		return directAndHeuristic;
	}
}