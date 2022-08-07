import lejos.nxt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.bluetooth.RemoteDevice;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;
import lejos.robotics.subsumption.*;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.util.TextMenu;

public class ColorSwarm {
	DifferentialPilot sam = new DifferentialPilot(6.0f, 10.75f, Motor.A, Motor.C);
	OdometryPoseProvider gps = new OdometryPoseProvider(sam);
	Navigator nav = new Navigator(sam);
	Pose pose;
	TouchSensor feels = new TouchSensor(SensorPort.S3);
	ColorSensor eyes = new ColorSensor(SensorPort.S4);
	
	DataInputStream dis;
	DataOutputStream dos;
	BTConnection btc;
	boolean isConnected;
	String name;
	
	float STEP; //distance between passes
	float X_MIN; //minimum x value to go to
	float Y_MIN; //minimum y value to go to
	float X_MAX; //maximum x value to go to
	float Y_MAX; //maximum y value to go to
	float START_X; //initial x position
	float START_Y; //initial y position
	
	public static void main(String[] args) {
		ColorSwarm swarm = new ColorSwarm();
		swarm.go();
	}
	
	public void go() {
		sam.addMoveListener(gps);
		sam.setTravelSpeed(10.0f);
		sam.setRotateSpeed(25.0f);
		
		STEP = 10;
		X_MAX = 120;
		Y_MAX = 220;
		START_X = 0;
		START_Y = 0;
		nav.getPoseProvider().setPose(new Pose(START_X, START_Y, 90));
		for(float i = X_MIN; i <= X_MAX; i+=STEP*2) {
			nav.addWaypoint(i, Y_MIN);
			nav.addWaypoint(i, Y_MAX);
			nav.addWaypoint(i + STEP, Y_MAX);
			nav.addWaypoint(i + STEP, Y_MIN);
		}
		
		connect();
        int targetColor = Integer.parseInt(recieveData());
		
		Behavior b1 = new Search(); //drives forward (lowest priority)
		Behavior b2 = new ReceiveMessage();
		Behavior b3 = new FindSwatch(SensorPort.S3, targetColor);
		Behavior b4 = new Quit(); //stops when escape button is pressed (highest priority)
		Behavior [] bArray = {b1, b2, b3, b4};
		Arbitrator arby = new Arbitrator(bArray);
		arby.start();
	}
	
	public int colorMenu() {
		String colors[] = {"red", "yellow", "green", "orange"};
        TextMenu colorMenu = new TextMenu(colors, 1, "Color to find:");
        int color = colorMenu.select();
        LCD.clear();
        return color;
	}
	
//////////////////////////////////////
// Communication methods are below  //
//////////////////////////////////////

	public boolean getIsConnected() {
		return isConnected;
	}
	  
	// Send a string
	public void sendData(String output) {
		try {
			dos.writeUTF(output);
			dos.flush();
		}
		catch (IOException ioe) {
			LCD.clear(0);
			LCD.drawString("failed to send", 0, 0);
			isConnected = false;
			stopConnection();
			reConnect();
		}
	}
	  
	// Try to receive a string
	public String recieveData() {
		String s = " ";
		try {
			s = dis.readUTF();
			return s;
		}
		catch (IOException e) {
			LCD.clear(0);
			LCD.drawString("failed to receive", 0, 0);
			isConnected = false;
			stopConnection();
			reConnect();
			return "0";
		}
	}
	  
	// Stop the connection
	public void stopConnection() {
		try {
			dos.close();
			dis.close();
			btc.close();
		}
		catch (IOException e) {
			LCD.clear(0);
			LCD.drawString("failed to close", 0, 0);
		}
	}
	  
	// Reconnect as master or slave depending on the initial connecting was setup.
	private void reConnect() {
		if(name != null) {
			connect(name);
		}
		else {
			connect();
		}
	}
	
	// Connect to other NXT with "name". The other NXT must be waiting for incoming connection, and be pre-paired.
	public void connect(String aName) {
		name = aName;
		LCD.drawString("Connecting...", 0, 0);
		LCD.refresh();
		RemoteDevice btrd = Bluetooth.getKnownDevice(name);
		if (btrd == null) {
			LCD.clear();
			LCD.drawString("No such device", 0, 0);
			LCD.refresh();
			try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
		}
		btc = Bluetooth.connect(btrd);
		if (btc == null) {
			LCD.clear();
			LCD.drawString("Connect fail", 0, 0);
			LCD.refresh();
			try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
		}
		else {
			LCD.clear();
			LCD.drawString("Connected", 0, 0);
			LCD.refresh();
			dis = btc.openDataInputStream();
			dos = btc.openDataOutputStream();
		}
	}
	
	// Wait for other NXT to connect.
	public void connect() {
		LCD.drawString("Waiting...", 0, 0);
		btc = Bluetooth.waitForConnection();
		LCD.drawString("Connected  ",0,0);
		isConnected = true;
		dis = btc.openDataInputStream();
		dos = btc.openDataOutputStream();
	}
	
////////////////////////////////////////
// Subsumption architecture is below  //
////////////////////////////////////////
	
	/**
	* Activated by default. Drives forward 
	* @author sgreenstein
	*/
	public class Search implements Behavior {
		public Search() {
			//set initial position
			nav.getPoseProvider().setPose(new Pose(START_X, START_Y, 90));
			for(float i = X_MIN; i <= X_MAX; i+=STEP*2) {
				nav.addWaypoint(i, Y_MIN);
				nav.addWaypoint(i, Y_MAX);
				nav.addWaypoint(i + STEP, Y_MAX);
				nav.addWaypoint(i + STEP, Y_MIN);
			}
		}
		public boolean takeControl() {
			//always tries to take control
			return true;
		}
		public void suppress() {
			
		}
		public void action() {
			nav.followPath();
			LCD.drawString("X: " + nav.getWaypoint().x, 1, 1);
			LCD.drawString("Y: " + nav.getWaypoint().y, 2, 2);
		}
	}
	 
	/**
	* Activated when a message is received from another robot
	* @author sgreenstein
	*/
	public class ReceiveMessage implements Behavior {
		public boolean takeControl() {
			return false;
		}
		public void suppress() {
			
		}
		public void action() {
			//TODO: send message to others
			//TODO: go to location specified by message
		}
	}

	/**
	* Activated when the target swatch is found
	* @author sgreenstein
	*/
	public class FindSwatch implements Behavior {
		ColorSensor cs;
		int targetColor;
		public FindSwatch(SensorPort port, int targCol) {
			cs = new ColorSensor(port);
			targetColor = targCol;
		}
		public boolean takeControl() {
			return(cs.getColor().getColor() == targetColor);
		}
		public void suppress() {
			
		}
		public void action() {
			nav.stop();
			nav.clearPath();
			//TODO: send message
		}
	}

	/**
	* Quits if escape button is pressed
	* @author sgreenstein
	*/
	public class Quit implements Behavior {
		public boolean takeControl() {
			//takes control if escape button is presses
			return Button.ESCAPE.isDown();
		}
		public void suppress() {
			//cannot be suppressed
		}
		public void action() {
			//TODO: stop moving
			System.exit(0); //quit
		}
	}
}