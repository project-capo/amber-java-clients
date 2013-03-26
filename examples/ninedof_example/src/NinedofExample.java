

import java.io.IOException;

import pl.edu.agh.amber.common.AmberClient;
import pl.edu.agh.amber.common.CyclicDataListener;
import pl.edu.agh.amber.ninedof.NinedofData;
import pl.edu.agh.amber.ninedof.NinedofProxy;

public class NinedofExample implements CyclicDataListener<NinedofData> {

	public static void main(String[] args) {
		(new NinedofExample()).runDemo();		
	}
	
	public void runDemo() {
		long before, after;
		
		AmberClient client;
		try {
			client = new AmberClient("192.168.1.50", 26233);
			Thread.sleep(1000);
			NinedofProxy ninedofProxy = new NinedofProxy(client, 0);
			
			for (int i = 0; i < 5; i++) {
				before = System.nanoTime();
				
				NinedofData data = ninedofProxy.getAxesData(true, true, true);
				
				data.waitAvailable();
				
				after = System.nanoTime();
				
				System.out.println(String.format("accel x: %s, y: %s, z: %s", 
						data.getAccel().xAxis, data.getAccel().yAxis, data.getAccel().zAxis));
				
				System.out.println(String.format("gyro x: %s, y: %s, z: %s", 
						data.getGyro().xAxis, data.getGyro().yAxis, data.getGyro().zAxis));
				
				System.out.println(String.format("magnet x: %s, y: %s, z: %s", 
						data.getMagnet().xAxis, data.getMagnet().yAxis, data.getMagnet().zAxis));	
			
				System.out.println("time: " + (after - before)/1000 + "us");
			}			
			
			ninedofProxy.registerNinedofDataListener(1000, true, true, true, this);
			
			Thread.sleep(10000);
			
			ninedofProxy.unregisterDataListener();
			
		} catch (IOException e) {
			System.err.println("Connection error: " + e);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	@Override
	public void handle(NinedofData data) {
		System.out.println("Got new cyclic message.");
		
		try {
			System.out.println(String.format("accel x: %s, y: %s, z: %s", 
					data.getAccel().xAxis, data.getAccel().yAxis, data.getAccel().zAxis));
			
			System.out.println(String.format("gyro x: %s, y: %s, z: %s", 
					data.getGyro().xAxis, data.getGyro().yAxis, data.getGyro().zAxis));
			
			System.out.println(String.format("magnet x: %s, y: %s, z: %s", 
					data.getMagnet().xAxis, data.getMagnet().yAxis, data.getMagnet().zAxis));	
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
