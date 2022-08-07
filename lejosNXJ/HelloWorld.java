import lejos.nxt.*;

public class HelloWorld {
	public static void main(String[] args) throws Exception {
		LCD.clear();
		LCD.drawString("Hello World", 3, 4);
		Thread.sleep(2000);
		LCD.refresh();
	}
}