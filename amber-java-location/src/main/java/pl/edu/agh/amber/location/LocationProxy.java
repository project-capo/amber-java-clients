package pl.edu.agh.amber.location;

import com.google.protobuf.ExtensionRegistry;

import pl.edu.agh.amber.common.AmberClient;
import pl.edu.agh.amber.common.AmberProxy;
import pl.edu.agh.amber.common.CyclicDataListener;
import pl.edu.agh.amber.common.proto.CommonProto.DriverMsg;
import pl.edu.agh.amber.common.FutureObject;
import pl.edu.agh.amber.common.proto.CommonProto;
import pl.edu.agh.amber.location.proto.LocationProto;
import pl.edu.agh.amber.location.proto.LocationProto.Location;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class LocationProxy extends AmberProxy {

	/* Magic-constant */
	private final static int DEVICE_TYPE = 6;

	private Map<Integer, FutureObject> futureObjectsMap = new ConcurrentHashMap<Integer, FutureObject>();
	private CyclicDataListener<String> stringListener;
	private final ReentrantLock listenerLock = new ReentrantLock();

	private int synNum = 100;
	private final ExtensionRegistry extensionRegistry;

	public LocationProxy(AmberClient amberClient, int deviceID) {
		super(DEVICE_TYPE, deviceID, amberClient, Logger
				.getLogger("LocationProxy"));

		logger.info("Starting and registering LocationProxy.");

		extensionRegistry = ExtensionRegistry.newInstance();
		LocationProto.registerAllExtensions(extensionRegistry);
	}

	public LocationCurrent getCurrentLocation() throws IOException {
		logger.fine("Getting current location.");

		int synNum = getNextSynNum();

		DriverMsg currentLocationRequestMsg = buildCurrentLocationRequestMsg(synNum);

		LocationCurrent locationCurrent = new LocationCurrent();
		futureObjectsMap.put(synNum, locationCurrent);

		amberClient.sendMessage(buildHeader(), currentLocationRequestMsg);

		return locationCurrent;
	}

	@Override
	public void handleDataMsg(CommonProto.DriverHdr header,
			CommonProto.DriverMsg message) {
		logger.fine("Handling data message");

		if (message.hasAckNum() && message.getAckNum() != 0) 
		{
			int ackNum = message.getAckNum();

			// TODO: automatically removing abandoned futureObjects
			if (futureObjectsMap.containsKey(ackNum)) {
				FutureObject futureObject = futureObjectsMap.remove(ackNum);

				if (futureObject != null) {
					if (futureObject instanceof LocationCurrent) {
						fillLocationCurrent((LocationCurrent) futureObject,
								message);
					}
				}
			}
		}
	}

	@Override
	public ExtensionRegistry getExtensionRegistry() {
		return extensionRegistry;
	}

	synchronized private int getNextSynNum() {
		return synNum++;
	}

	private void fillLocationCurrent(LocationCurrent lc, DriverMsg message) {
		Location inputLc = message.getExtension(LocationProto.currentLocation);

		lc.setX(inputLc.getX());
		lc.setY(inputLc.getY());
		lc.setAngle(inputLc.getAlfa());
		lc.setP(inputLc.getP());
		lc.setTimeStamp(inputLc.getTimeStamp());

		lc.setAvailable();
	}

	private DriverMsg buildCurrentLocationRequestMsg(int synNum) {
		DriverMsg.Builder driverMsgBuilder = DriverMsg.newBuilder();

		driverMsgBuilder.setType(DriverMsg.MsgType.DATA);
		driverMsgBuilder.setExtension(LocationProto.getLocation, true);
		driverMsgBuilder.setSynNum(synNum);

		return driverMsgBuilder.build();
	}
	
	public void UploadMap(String sMap) throws IOException {
		logger.fine("Upload map to location driver.");

		int synNum = getNextSynNum();

		DriverMsg uploadMapMsg = buildUploadMapMsg(synNum,sMap);

		amberClient.sendMessage(buildHeader(), uploadMapMsg);
	}
	
	private DriverMsg buildUploadMapMsg(int synNum,String sMap) {
		DriverMsg.Builder driverMsgBuilder = DriverMsg.newBuilder();

		driverMsgBuilder.setType(DriverMsg.MsgType.DATA);
		driverMsgBuilder.setExtension(LocationProto.uploadMap, sMap);
		driverMsgBuilder.setSynNum(synNum);

		return driverMsgBuilder.build();
	}

}
