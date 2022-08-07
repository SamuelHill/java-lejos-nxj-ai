/* File: SwarmSlave.java
 * Author: Seth Greenstein
 * Edited: Samuel Hill
 * Date: 11/18/2013
 * Purpose: Searches for a colored swatch in conjunction with other robots
 */
import lejos.nxt.Button;
import lejos.nxt.ColorSensor;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;
import lejos.util.TextMenu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.StringTokenizer;

import lejos.nxt.LCD;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;


public class SwarmSlave
{
	DifferentialPilot pilot;
	Navigator nav;
	String inData; //received message data
	Network master; //master's network thread
	
	//all units are centimeters
	float STEP = 10; //distance between passes
	final float START_X = 0; //initial x position
	final float START_Y = 0; //initial y position
	float xBound; //maximum x value to go to
	float yBound; //maximum y value to go to
		
	public static void main(String [] args) throws Exception
    {
		SwarmSlave swarm = new SwarmSlave();
		swarm.start();
    }
	
	public void start()
	{
		final float WHEELBASE = 16.0f; //distance from the center of one wheel to the center of the other, in cm
		final float WHEEL_DIAM = 5.9f; //diameter of each of the two drive wheels, in cm
		//set up pilot and navigator
		pilot = new DifferentialPilot(WHEEL_DIAM, WHEELBASE, Motor.A, Motor.C);
		pilot.setTravelSpeed(20);
		pilot.setRotateSpeed(35);
		nav = new Navigator(pilot);
		//initialize message-receiving variables
		inData = "No data";
		//define area to search
		STEP = 10; //go in columns 10 cm apart
		xBound = START_X + 81; //go 81 cm to the east of the start
		yBound = START_Y + 225; //go 225 cm north of the start
		//determine which color is the target
		int targetColor = colorMenu();
		master = new Network(this);
		master.start();
		Behavior b1 = new Search(); //searches for swatch (lowest priority)
		Behavior b2 = new TouchObstacle(SensorPort.S4); //backs up and turns to avoid obstacle
		Behavior b3 = new OffWhite(SensorPort.S1); //backs up and turns to get back onto white
		Behavior b4 = new ReceiveMessage(SensorPort.S4); //receives and acts on a message
		Behavior b5 = new FindSwatch(SensorPort.S1, targetColor); //sends a message about the swatch location
		Behavior b6 = new Quit(); //stops when escape button is pressed (highest priority)
		Behavior [] bArray = {b1, b2, b3, b4, b5, b6};
		Arbitrator arby = new Arbitrator(bArray);
		arby.start();
	}
	
	/**
	 * Sets message to inData Variable 
	 * @author sgreenstein
	 * 
	 */
	public void onMessage(String message)
	{
		//get message from slave network thread
		inData = message; //the message
	}
	
	/**
	 * Activated by default. Drives forward 
	 * @author sgreenstein
	 *
	 */
	public class Search implements Behavior
	{
		java.util.Random rand;
		public Search()
		{
			//create random number generator
			rand = new java.util.Random();
			//set initial position
			nav.getPoseProvider().setPose(new Pose(START_X, START_Y, 90));
			//set waypoints to search up and down this robot's section
			for(float i = START_X; i <= xBound; i+=STEP*2)
			{
				if(i != START_X)
					nav.addWaypoint(i, START_Y);
				nav.addWaypoint(i, yBound);
				nav.addWaypoint(i + STEP, yBound);
				nav.addWaypoint(i + STEP, START_Y);
			}
		}
		public boolean takeControl()
		{
			//always tries to take control
			return true;
		}
		public void suppress()
		{
		}
		public void action()
		{
			//if it's done following the preset path,
			//something probably went wrong. Go somewhere random
			if(nav.pathCompleted())
			{
				pilot.forward();
			}
			nav.followPath();
		}
	}
	
	/**
	 * Activated to receive messages from other robots
	 * @author sgreenstein
	 *
	 */
	public class ReceiveMessage implements Behavior
	{
		TouchSensor ts;
		public ReceiveMessage(SensorPort port1)
		{
			ts = new TouchSensor(port1);
		}
		public boolean takeControl()
		{
				return !inData.equals("No data");
		}
		public void suppress()
		{
		}
		public void action()
		{
			if(!inData.equals("No data"))
			{
				//stop searching
				nav.clearPath();
				//parse method into two floats
				StringTokenizer tok = new StringTokenizer(inData,",");
				//go to specified location
				LCD.clear();
				LCD.drawString(inData, 1, 1);;
				LCD.refresh();
				nav.addWaypoint(Float.parseFloat(tok.nextToken()), Float.parseFloat(tok.nextToken()));
				nav.followPath(); //go to the point specified by the message
				while(!nav.pathCompleted())
				{
					if(ts.isPressed())
						System.exit(0);
				}
				LCD.drawString("Goal reached", 1, 5);
				System.exit(0);
			}
		}
	}
	
	/**
	 * Activated when the robot runs off the white
	 * @author sgreenstein
	 *
	 */
	public class OffWhite implements Behavior
	{
		java.util.Random rand; //for random wandering after pattern completed
		ColorSensor cs;
		public OffWhite(SensorPort port)
		{
			cs = new ColorSensor(port);
			//create random number generator
			rand = new java.util.Random();
		}
		public boolean takeControl()
		{
			//takes control if on carpet (black)
			return(cs.getColor().getColor() == 7);
		}
		public void suppress()
		{

		}
		public void action()
		{
			//stop, back up, turn
			nav.stop();
			Pose pose = nav.getPoseProvider().getPose();
			pilot.travel(-10);
			//trick nav into thinking we didn't back up
			nav.getPoseProvider().setPose(pose);
			pilot.rotate(rand.nextInt(270) + 45);
		}
	}
	
	/**
	 * Activated when the robot hits a wall or another robot
	 * @author sgreenstein
	 *
	 */
	public class TouchObstacle implements Behavior
	{
		java.util.Random rand; //for random wandering after pattern completed
		TouchSensor ts1;
		TouchSensor ts2;
		public TouchObstacle(SensorPort port1)
		{
			ts1 = new TouchSensor(port1);
			ts2 = new TouchSensor(port1);
			//create random number generator
			rand = new java.util.Random();
		}
		public TouchObstacle(SensorPort port1, SensorPort port2)
		{
			ts1 = new TouchSensor(port1);
			ts2 = new TouchSensor(port2);
			//create random number generator
			rand = new java.util.Random();
		}
		public boolean takeControl()
		{
			//takes control if a touch sensor is activated
			return(ts1.isPressed() || ts2.isPressed());
		}
		public void suppress()
		{

		}
		public void action()
		{
			//stop, back up, turn
			nav.stop();
			pilot.travel(-10);
			pilot.rotate(rand.nextInt(270) + 45);
		}
	}
	
	/**
	 * Activated when the target swatch is found
	 * @author sgreenstein
	 *
	 */
	public class FindSwatch implements Behavior
	{
		ColorSensor cs;
		int targetColor;
		public FindSwatch(SensorPort port, int targCol)
		{
			cs = new ColorSensor(port);
			targetColor = targCol;
		}
		public boolean takeControl()
		{
			//takes control if correct swatch is found
			return(cs.getColor().getColor() == targetColor);
		}
		public void suppress()
		{

		}
		public void action()
		{
			//stop
			nav.stop();
			nav.clearPath();
			//send message to slaves
			master.sendData(Math.round(nav.getPoseProvider().getPose().getX()) + "," + Math.round(nav.getPoseProvider().getPose().getY()));
		}
	}
	
	
	/**
	 * Quits if escape button is pressed
	 * @author sgreenstein
	 *
	 */
	public class Quit implements Behavior
	{
		public boolean takeControl()
		{
			//takes control if escape button is pressed
			return Button.ESCAPE.isDown();
		}
		public void suppress()
		{
			//cannot be suppressed
		}
		public void action()
		{
			nav.stop();
			System.exit(0); //quit
		}
	}
	  
	  /**
	   * 
	   * @return color: int representing target color
	   */
		public int colorMenu()
		{
			String colors[] = {"red", "green", "blue", "yellow"};
			TextMenu colorMenu = new TextMenu(colors, 1, "Color to find:");
			int color = colorMenu.select();
			LCD.clear();
			return color;
		}
		
		public class Network extends Thread
		{
			SwarmSlave swarm;
			private BufferedReader in;
			private OutputStreamWriter out;
			private BTConnection btc;
			private boolean isConnected;
			
			public Network(SwarmSlave sw)
			{
				swarm = sw;
				connect();
			}
			
			public void run()
			{
			    in = new BufferedReader(new InputStreamReader(btc.openInputStream()));
			    out = new OutputStreamWriter(btc.openOutputStream());
			    isConnected = true;
				while(true)
				{
					try {
						String message = in.readLine();
						LCD.drawString(message, 3, 3);
						LCD.refresh();
						swarm.onMessage(message);
					} catch (IOException e) {
						LCD.clear();
						LCD.drawString("Receive failed", 0, 0);
						LCD.refresh();
					}
				}
			}
			
			/**
			   * Send a string
			   * @param output
			   */
			  public void sendData(String output){
				  if(isConnected){
					  try {
						out.write(output + "\n");
					} catch (IOException e) {
						LCD.clear();
						LCD.drawString("Send failed", 0, 0);
						LCD.refresh();
					}
				  }
			  }
			  
			  /**
			   * Try to receive a string
			   * @return
			   */
			  public String receiveData(){
					while(true)
					{
						try {
							String message = in.readLine();
							swarm.onMessage(message);
						} catch (IOException e) {
							LCD.clear();
							LCD.drawString("Receive failed", 0, 0);
							LCD.refresh();
						}
					}
			  }
			  
			  /**
			   * Stop the connection
			   */
			  public void stopConnection(){
			     try {
			    	 in.close();
			    	 out.close();
			        btc.close();
			     } catch (IOException e) {
			        LCD.clear(0);
			        LCD.drawString("failed to close", 0, 0);
			     }
			  }

			  /**
			   * Wait for other NXT to connect.
			   */
			  public void connect(){
			     LCD.drawString("Waiting...", 0, 0);
			     btc = Bluetooth.waitForConnection();
			     LCD.drawString("Connected  ",0,0);
			  }
		}
}