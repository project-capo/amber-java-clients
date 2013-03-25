package pl.edu.agh.amber.roboclaw;

import java.io.IOException;
import java.util.logging.Logger;

import pl.edu.agh.amber.common.AmberClient;
import pl.edu.agh.amber.common.AmberProxy;
import pl.edu.agh.amber.common.proto.CommonProto.DriverHdr;
import pl.edu.agh.amber.common.proto.CommonProto.DriverMsg;
import pl.edu.agh.amber.roboclaw.proto.RoboclawProto;
import pl.edu.agh.amber.roboclaw.proto.RoboclawProto.MotorsQuadCommand;

import com.google.protobuf.ExtensionRegistry;


public class RoboclawProxy extends AmberProxy {

	private final static int DEVICE_TYPE = 2;
	
	private int synNum = 100;	
	private final ExtensionRegistry extensionRegistry;
	
	public RoboclawProxy(AmberClient amberClient, int deviceID) {
		super(DEVICE_TYPE, deviceID, amberClient, Logger.getLogger("RoboclawProxy"));
		
		extensionRegistry = ExtensionRegistry.newInstance();
		RoboclawProto.registerAllExtensions(extensionRegistry);	
	}
	
	synchronized private int getNextSynNum() {
		return synNum++;
	}
	

	@Override
	public ExtensionRegistry getExtensionRegistry() {
		return extensionRegistry;
	}

	@Override
	public void handleDataMsg(DriverHdr header, DriverMsg message) {
		
		
	}
	
	private MotorsQuadCommand buildMotorsCommand(MotorsCommand mc) {
		
		if (mc == null) {
			return null;
		}
		
		MotorsQuadCommand.Builder commandBuilder = MotorsQuadCommand.newBuilder();

		commandBuilder.setFrontLeftSpeed(mc.getFrontLeftSpeed());
		commandBuilder.setFrontRightSpeed(mc.getFrontRightSpeed());
		commandBuilder.setRearLeftSpeed(mc.getRearLeftSpeed());
		commandBuilder.setRearRightSpeed(mc.getRearRightSpeed());

		return commandBuilder.build();
	}
	
	public void sendMotorsCommand(MotorsCommand mc) throws IOException {

		if (mc == null) {
			throw new IllegalArgumentException("MotorsCommand cannot be null");
		}
		
		DriverMsg.Builder driverMsgBuilder = DriverMsg.newBuilder();
		driverMsgBuilder.setType(DriverMsg.MsgType.DATA);
		
		MotorsQuadCommand motorsCommand;
		
		motorsCommand = buildMotorsCommand(mc);
		if (motorsCommand == null) {
			throw new RuntimeException("Error in serializing MotorsCommand");
		}
		
		driverMsgBuilder.setExtension(RoboclawProto.motorsCommand, motorsCommand);
		driverMsgBuilder.setSynNum(getNextSynNum());
		
		amberClient.sendMessage(buildHeader(), driverMsgBuilder.build());
	}

}
