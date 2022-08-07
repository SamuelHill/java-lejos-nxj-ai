import lejos.nxt.*;
import lejos.robotics.navigation.DifferentialPilot;

/**
 * This program will have the LejosNXT robot move across the board of colors
 * using the hill-climber local search algorithm.
 * 
 * @author Samuel Hill
 */

public class HillClimber {
	DifferentialPilot sam = new DifferentialPilot(2.204f, 6.25f, Motor.A, Motor.C);
	ColorSensor cs = new ColorSensor(SensorPort.S4);
	private double left = 91.0f;
	private double right = -91.0f;
	private double flip = 182.0f;
	private double fwd = 8.0f;
	
	public static void main(String[] args) {
		new HillClimber().go();
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
			
			if (curVal < heuristic) {
				LCD.clear();
				LCD.drawString("Failed Run...", 0, 0);
				LCD.drawString("Press ESCAPE", 0, 1);
				LCD.drawString("to exit...", 0, 2);
				Button.ESCAPE.waitForPressAndRelease();
				LCD.clear();
				break;
			}
			
			if (direction == 0) {
				sam.travel(fwd);
				curVal = heuristic;
			}
			else if (direction == 1) {
				sam.rotate(left);
				sam.travel(fwd);
				sam.rotate(right);
				curVal = heuristic;
			}
			else if (direction == 2) {
				sam.rotate(flip);
				sam.travel(fwd);
				curVal = heuristic;
			}
			else if (direction == 3) {
				sam.rotate(right);
				sam.travel(fwd);
				sam.rotate(left);
				curVal = heuristic;
			}
			
			if (curVal == 0) {
				LCD.clear();
				LCD.drawString("GOAL!!!", 0, 0);
				LCD.drawString("Press ESCAPE", 0, 1);
				LCD.drawString("to exit...", 0, 2);
				Button.ESCAPE.waitForPressAndRelease();
				LCD.clear();
				break;
			}
		}
	}
	
	public int[] neighborhoodPatrol() {
		int[] neighbors = new int[4];
		int[] directAndHeuristic = new int[2];
		for(int i = 0; i < 4; i++){
			sam.travel(4.0f);
			neighbors[i] = colorGrabber();
			sam.travel(-4.0f);
			sam.rotate(left);
		}
		directAndHeuristic = bestNeighbor(neighbors);
		return directAndHeuristic;
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
		for (int i = 0; i < 4; i++) {
			if (neighbors[i] < best) {
				best = neighbors[i];
				direction = i;
			}
		}
		int[] directAndHeuristic = {direction, neighbors[direction]};
		return directAndHeuristic;
	}
}
