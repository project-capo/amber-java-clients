package pl.edu.agh.amber.ninedof;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import pl.edu.agh.amber.common.AmberClient;
import pl.edu.agh.amber.common.AmberProxy;
import pl.edu.agh.amber.common.CyclicDataListener;
import pl.edu.agh.amber.common.FutureObject;
import pl.edu.agh.amber.common.proto.CommonProto.DriverHdr;
import pl.edu.agh.amber.common.proto.CommonProto.DriverMsg;
import pl.edu.agh.amber.ninedof.proto.NinedofProto;
import pl.edu.agh.amber.ninedof.proto.NinedofProto.DataRequest;
import pl.edu.agh.amber.ninedof.proto.NinedofProto.SensorData;
import pl.edu.agh.amber.ninedof.proto.NinedofProto.SubscribeAction;

import com.google.protobuf.ExtensionRegistry;

public class NinedofProxy extends AmberProxy {

	private final static int DEVICE_TYPE = 1;

	private Map<Integer, FutureObject> futureObjectsMap = new ConcurrentHashMap<Integer, FutureObject>();
	private CyclicDataListener<NinedofData> ninedofDataListener;
	private final ReentrantLock listenerLock = new ReentrantLock();

	private int synNum = 100;
	private final ExtensionRegistry extensionRegistry;

	public NinedofProxy(AmberClient amberClient, int deviceID) {
		super(DEVICE_TYPE, deviceID, amberClient, Logger
				.getLogger("NinedofProxy"));

		extensionRegistry = ExtensionRegistry.newInstance();
		NinedofProto.registerAllExtensions(extensionRegistry);
	}

	synchronized private int getNextSynNum() {
		return synNum++;
	}

	private void fillStructure(NinedofData ninedofData, DriverMsg message) {
		SensorData sensorData = message.getExtension(NinedofProto.sensorData);

		ninedofData.setAccel(new NinedofData.AxesData(sensorData.getAccel()
				.getXAxis(), sensorData.getAccel().getYAxis(), sensorData
				.getAccel().getZAxis()));

		ninedofData.setGyro(new NinedofData.AxesData(sensorData.getGyro()
				.getXAxis(), sensorData.getGyro().getYAxis(), sensorData
				.getGyro().getZAxis()));

		ninedofData.setMagnet(new NinedofData.AxesData(sensorData.getMagnet()
				.getXAxis(), sensorData.getMagnet().getYAxis(), sensorData
				.getMagnet().getZAxis()));
	}

	@Override
	public void handleDataMsg(DriverHdr header, DriverMsg message) {

		if (!message.hasAckNum() || message.getAckNum() == 0) {

			NinedofData ninedofData = new NinedofData();
			fillStructure(ninedofData, message);
			ninedofData.setAvailable();

			synchronized (listenerLock) {
				if (ninedofDataListener != null) {
					ninedofDataListener.handle(ninedofData);
				}
			}

		} else {
			int ackNum = message.getAckNum();

			// TODO: automatically removing abandoned futureObjects
			if (futureObjectsMap.containsKey(ackNum)) {
				NinedofData ninedofData = (NinedofData) futureObjectsMap
						.remove(ackNum);

				fillStructure(ninedofData, message);
				ninedofData.setAvailable();
			}
		}
	}

	public void registerNinedofDataListener(int freq, boolean accel,
			boolean gyro, boolean magnet,
			CyclicDataListener<NinedofData> listener) throws IOException {

		DriverMsg driverMsg = buildSubscribeActionMsg(freq, accel, gyro, magnet);

		synchronized (listenerLock) {
			ninedofDataListener = listener;
		}

		amberClient.sendMessage(buildHeader(), driverMsg);
	}

	public void unregisterDataListener() throws IOException {
		DriverMsg driverMsg = buildSubscribeActionMsg(0, false, false, false);

		synchronized (listenerLock) {
			ninedofDataListener = null;
		}

		amberClient.sendMessage(buildHeader(), driverMsg);
	}

	private DriverMsg buildSubscribeActionMsg(int freq, boolean accel,
			boolean gyro, boolean magnet) {

		SubscribeAction.Builder subscribeActionBuilder = SubscribeAction
				.newBuilder();
		subscribeActionBuilder.setFreq(freq);
		subscribeActionBuilder.setAccel(accel);
		subscribeActionBuilder.setGyro(gyro);
		subscribeActionBuilder.setMagnet(magnet);

		DriverMsg.Builder driverMsgBuilder = DriverMsg.newBuilder();
		driverMsgBuilder.setType(DriverMsg.MsgType.DATA);
		driverMsgBuilder.setExtension(NinedofProto.subscribeAction,
				subscribeActionBuilder.build());

		return driverMsgBuilder.build();
	}

	private DriverMsg buildDataRequestMsg(int synNum, boolean accel,
			boolean gyro, boolean magnet) {

		DataRequest.Builder dataRequestBuilder = DataRequest.newBuilder();
		dataRequestBuilder.setAccel(accel);
		dataRequestBuilder.setGyro(gyro);
		dataRequestBuilder.setMagnet(magnet);

		DriverMsg.Builder driverMsgBuilder = DriverMsg.newBuilder();
		driverMsgBuilder.setType(DriverMsg.MsgType.DATA);
		driverMsgBuilder.setExtension(NinedofProto.dataRequest,
				dataRequestBuilder.build());

		driverMsgBuilder.setSynNum(synNum);

		return driverMsgBuilder.build();
	}

	public NinedofData getAxesData(boolean accel, boolean gyro, boolean magnet)
			throws IOException {
		int synNum = getNextSynNum();

		DriverMsg dataRequestMsg = buildDataRequestMsg(synNum, accel, gyro,
				magnet);

		NinedofData ninedofData = new NinedofData();
		futureObjectsMap.put(synNum, ninedofData);

		amberClient.sendMessage(buildHeader(), dataRequestMsg);

		return ninedofData;
	}

	@Override
	public ExtensionRegistry getExtensionRegistry() {
		return extensionRegistry;
	}

}
