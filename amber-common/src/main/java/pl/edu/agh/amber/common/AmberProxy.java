package pl.edu.agh.amber.common;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.edu.agh.amber.common.proto.CommonProto.DriverHdr;
import pl.edu.agh.amber.common.proto.CommonProto.DriverMsg;

import com.google.protobuf.ExtensionRegistry;

public abstract class AmberProxy {

	protected final AmberClient amberClient;
	protected final int deviceType;
	protected final int deviceID;
	
	protected Logger logger;
	
	public AmberProxy(int deviceType, int deviceID, AmberClient amberClient, Logger logger) {
		this.deviceType = deviceType;
		this.deviceID = deviceID;
		this.amberClient = amberClient;
		this.logger = logger;
		
		logger.setLevel(Level.INFO);
	
		amberClient.registerClient(deviceType, deviceID, this);
	}
	
	public abstract void handleDataMsg(DriverHdr header, DriverMsg message);
	public void handlePingMessage(DriverHdr header, DriverMsg message) {};
	public void handlePongMessage(DriverHdr header, DriverMsg message) {};
	public void handleDriverDiedMessage(DriverHdr header, DriverMsg message) {};
	
	public abstract ExtensionRegistry getExtensionRegistry();
	
	protected DriverHdr buildHeader() {
		DriverHdr.Builder driverHdrBuilder = DriverHdr.newBuilder();
		driverHdrBuilder.setDeviceType(deviceType); 
		driverHdrBuilder.setDeviceID(deviceID);
		
		return driverHdrBuilder.build();
	}
	
	public void terminateProxy() {
		logger.info("Terminating proxy.");

		DriverMsg.Builder driverMsgBuilder = DriverMsg.newBuilder();
		driverMsgBuilder.setType(DriverMsg.MsgType.CLIENT_DIED);
	
		try {
			amberClient.sendMessage(buildHeader(), driverMsgBuilder.build());
		} catch (IOException e) {
			logger.warning("Error in sending terminate message");
		}
	}
	
}
