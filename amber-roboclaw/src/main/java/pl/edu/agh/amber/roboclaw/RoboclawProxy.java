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
	
	public void sendMotorsCommand(int frontLeftSpeed, int frontRightSpeed, int rearLeftSpeed, int rearRightSpeed) throws IOException {
		DriverMsg.Builder driverMsgBuilder = DriverMsg.newBuilder();
		driverMsgBuilder.setType(DriverMsg.MsgType.DATA);
		
		MotorsQuadCommand.Builder commandBuilder = MotorsQuadCommand.newBuilder();

		commandBuilder.setFrontLeftSpeed(frontLeftSpeed);
		commandBuilder.setFrontRightSpeed(frontRightSpeed);
		commandBuilder.setRearLeftSpeed(rearLeftSpeed);
		commandBuilder.setRearRightSpeed(rearRightSpeed);

		MotorsQuadCommand motorsQuadCommand = commandBuilder.build();
		driverMsgBuilder.setExtension(RoboclawProto.motorsCommand, motorsQuadCommand);
		driverMsgBuilder.setSynNum(getNextSynNum());
		
		amberClient.sendMessage(buildHeader(), driverMsgBuilder.build());
	}

}
