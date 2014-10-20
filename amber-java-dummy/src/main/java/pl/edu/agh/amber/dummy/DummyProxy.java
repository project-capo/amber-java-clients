package pl.edu.agh.amber.dummy;

import com.google.protobuf.ExtensionRegistry;
import pl.edu.agh.amber.common.AmberClient;
import pl.edu.agh.amber.common.AmberProxy;
import pl.edu.agh.amber.common.CyclicDataListener;
import pl.edu.agh.amber.common.FutureObject;
import pl.edu.agh.amber.common.proto.CommonProto;
import pl.edu.agh.amber.dummy.proto.DummyProto;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class DummyProxy extends AmberProxy {

    /* Magic-constant */
    private final static int DEVICE_TYPE = 5;

    private Map<Integer, FutureObject> futureObjectsMap = new ConcurrentHashMap<Integer, FutureObject>();
    private CyclicDataListener<String> stringListener;
    private final ReentrantLock listenerLock = new ReentrantLock();

    private int synNum = 100;
    private final ExtensionRegistry extensionRegistry;

    public DummyProxy(AmberClient amberClient, int deviceID) {
        super(DEVICE_TYPE, deviceID, amberClient, Logger.getLogger("DummyProxy"));

        logger.info("Starting and registering DummyProxy.");

        extensionRegistry = ExtensionRegistry.newInstance();
        DummyProto.registerAllExtensions(extensionRegistry);
    }

    public void subscribe(CyclicDataListener<String> listener) throws IOException {
        logger.fine(String.format("Registering listener"));

        CommonProto.DriverMsg driverMsg = buildSubscribeActionMsg();

        synchronized (listenerLock) {
            stringListener = listener;
        }

        amberClient.sendMessage(buildHeader(), driverMsg);
    }

    private CommonProto.DriverMsg buildSubscribeActionMsg() {
        CommonProto.DriverMsg.Builder driverMsgBuilder = CommonProto.DriverMsg.newBuilder();
        driverMsgBuilder.setType(CommonProto.DriverMsg.MsgType.SUBSCRIBE);
        return driverMsgBuilder.build();
    }

    public void unsubscribe() throws IOException {
        CommonProto.DriverMsg driverMsg = buildUnsubscribeActionMsg();

        synchronized (listenerLock) {
            stringListener = null;
        }

        amberClient.sendMessage(buildHeader(), driverMsg);
    }

    private CommonProto.DriverMsg buildUnsubscribeActionMsg() {
        CommonProto.DriverMsg.Builder driverMsgBuilder = CommonProto.DriverMsg.newBuilder();
        driverMsgBuilder.setType(CommonProto.DriverMsg.MsgType.UNSUBSCRIBE);
        return driverMsgBuilder.build();
    }

    @Override
    public void handleDataMsg(CommonProto.DriverHdr header, CommonProto.DriverMsg message) {
        logger.fine("Handling data message");

        if (!message.hasAckNum() || message.getAckNum() == 0) {
            String response = message.getExtension(DummyProto.message);
            if (response != null) {
                synchronized (listenerLock) {
                    if (stringListener != null) {
                        stringListener.handle(response);
                    }
                }
            }

        } else {
            int ackNum = message.getAckNum();

            // TODO: automatically removing abandoned futureObjects
            if (futureObjectsMap.containsKey(ackNum)) {
                FutureObject futureObject = futureObjectsMap.remove(ackNum);

                if (futureObject != null) {
                    if (futureObject instanceof Status) {
                        fillStatus((Status) futureObject, message);
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

    public void setEnable(boolean value) throws IOException {
        logger.fine("Set enable to " + value);

        CommonProto.DriverMsg msg = buildSetEnableRequestMsg(value);
        amberClient.sendMessage(buildHeader(), msg);
    }

    private CommonProto.DriverMsg buildSetEnableRequestMsg(boolean value) {
        CommonProto.DriverMsg.Builder driverMsgBuilder = CommonProto.DriverMsg.newBuilder();

        driverMsgBuilder.setType(CommonProto.DriverMsg.MsgType.DATA);
        driverMsgBuilder.setExtension(DummyProto.enable, value);

        return driverMsgBuilder.build();
    }

    public void setMessage(String message) throws IOException {
        logger.fine("Set message to " + message);

        CommonProto.DriverMsg msg = buildSetMessageRequestMsg(message);
        amberClient.sendMessage(buildHeader(), msg);
    }

    private CommonProto.DriverMsg buildSetMessageRequestMsg(String message) {
        CommonProto.DriverMsg.Builder driverMsgBuilder = CommonProto.DriverMsg.newBuilder();

        driverMsgBuilder.setType(CommonProto.DriverMsg.MsgType.DATA);
        driverMsgBuilder.setExtension(DummyProto.message, message);

        return driverMsgBuilder.build();
    }

    public Status getStatus() throws IOException {
        logger.fine("Get status");

        int synNum = getNextSynNum();

        CommonProto.DriverMsg msg = buildGetStatusRequestMsg(synNum);

        Status status = new Status();
        futureObjectsMap.put(synNum, status);

        amberClient.sendMessage(buildHeader(), msg);

        return status;
    }

    private CommonProto.DriverMsg buildGetStatusRequestMsg(int synNum) {
        CommonProto.DriverMsg.Builder driverMsgBuilder = CommonProto.DriverMsg.newBuilder();

        driverMsgBuilder.setType(CommonProto.DriverMsg.MsgType.DATA);
        driverMsgBuilder.setExtension(DummyProto.getStatus, true);

        driverMsgBuilder.setSynNum(synNum);

        return driverMsgBuilder.build();
    }

    private void fillStatus(Status status, CommonProto.DriverMsg message) {
        status.setEnable(message.getExtension(DummyProto.enable));
        status.setMessage(message.getExtension(DummyProto.message));

        status.setAvailable();
    }
}
