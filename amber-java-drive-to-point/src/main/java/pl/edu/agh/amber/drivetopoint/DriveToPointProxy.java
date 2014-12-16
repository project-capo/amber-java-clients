package pl.edu.agh.amber.drivetopoint;

import com.google.protobuf.ExtensionRegistry;
import pl.edu.agh.amber.common.AmberClient;
import pl.edu.agh.amber.common.AmberProxy;
import pl.edu.agh.amber.common.FutureObject;
import pl.edu.agh.amber.common.proto.CommonProto;
import pl.edu.agh.amber.drivetopoint.proto.DriveToPointProto;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Proxy used to connect to Drive to point driver.
 *
 * @author Pawel Suder <pawel@suder.info>
 */
public class DriveToPointProxy extends AmberProxy {

    /* Magic-constant */
    private final static int DEVICE_TYPE = 8;

    private int synNum = 100;
    private final ExtensionRegistry extensionRegistry;

    private Map<Integer, FutureObject> futureObjectsMap = new ConcurrentHashMap<Integer, FutureObject>();

    public DriveToPointProxy(AmberClient amberClient, int deviceID) {
        super(DEVICE_TYPE, deviceID, amberClient, Logger.getLogger("DriveToPointProxy"));

        logger.info("Starting and registering DriveToPointProxy.");

        extensionRegistry = ExtensionRegistry.newInstance();
        DriveToPointProto.registerAllExtensions(extensionRegistry);
    }

    @Override
    public void handleDataMsg(CommonProto.DriverHdr header, CommonProto.DriverMsg message) {
        logger.fine("Handling data message");

        if (message.hasAckNum() && message.getAckNum() != 0) {
            int ackNum = message.getAckNum();

            // TODO: automatically removing abandoned futureObjects
            if (futureObjectsMap.containsKey(ackNum)) {
                FutureObject futureObject = futureObjectsMap.remove(ackNum);

                if (futureObject != null) {
                    if (futureObject instanceof Result) {
                        if (message.getExtension(DriveToPointProto.getNextTarget) ||
                                message.getExtension(DriveToPointProto.getVisitedTarget)) {
                            fillTarget((Result<Point>) futureObject, message);

                        } else if (message.getExtension(DriveToPointProto.getNextTargets) ||
                                message.getExtension(DriveToPointProto.getVisitedTargets)) {
                            fillTargets((Result<List<Point>>) futureObject, message);

                        } else if (message.getExtension(DriveToPointProto.getConfiguration)) {
                            fillConfiguration((Result<Configuration>) futureObject, message);
                        }
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

    public void setTargets(List<Point> targets) throws IOException {
        logger.fine("Set targets");

        CommonProto.DriverMsg msg = buildSetTargetsRequestMsg(targets);
        amberClient.sendMessage(buildHeader(), msg);
    }

    public CommonProto.DriverMsg buildSetTargetsRequestMsg(List<Point> targets) {
        CommonProto.DriverMsg.Builder driverMsgBuilder = CommonProto.DriverMsg.newBuilder();

        driverMsgBuilder.setType(CommonProto.DriverMsg.MsgType.DATA);
        driverMsgBuilder.setExtension(DriveToPointProto.setTargets, true);

        DriveToPointProto.Targets.Builder targetsBuilder = DriveToPointProto.Targets.newBuilder();

        List<Double> longitudes = new LinkedList<Double>();
        for (Point target : targets) {
            longitudes.add(target.x);
        }

        List<Double> latitudes = new LinkedList<Double>();
        for (Point target : targets) {
            latitudes.add(target.y);
        }

        List<Double> radiuses = new LinkedList<Double>();
        for (Point target : targets) {
            radiuses.add(target.r);
        }

        targetsBuilder.addAllLongitudes(longitudes);
        targetsBuilder.addAllLatitudes(latitudes);
        targetsBuilder.addAllRadiuses(radiuses);

        DriveToPointProto.Targets generatedTargets = targetsBuilder.build();
        driverMsgBuilder.setExtension(DriveToPointProto.targets, generatedTargets);

        return driverMsgBuilder.build();
    }

    public Result<Point> getNextTarget() throws IOException {
        logger.fine("Get next target");

        int synNum = getNextSynNum();

        CommonProto.DriverMsg msg = buildGetNextTargetRequestMsg(synNum);

        Result<Point> status = new Result<Point>();
        futureObjectsMap.put(synNum, status);

        amberClient.sendMessage(buildHeader(), msg);

        return status;
    }

    private CommonProto.DriverMsg buildGetNextTargetRequestMsg(int synNum) {
        CommonProto.DriverMsg.Builder driverMsgBuilder = CommonProto.DriverMsg.newBuilder();

        driverMsgBuilder.setType(CommonProto.DriverMsg.MsgType.DATA);
        driverMsgBuilder.setExtension(DriveToPointProto.getNextTarget, true);

        driverMsgBuilder.setSynNum(synNum);

        return driverMsgBuilder.build();
    }

    public Result<List<Point>> getNextTargets() throws IOException {
        logger.fine("Get next targets");

        int synNum = getNextSynNum();

        CommonProto.DriverMsg msg = buildGetNextTargetsRequestMsg(synNum);

        Result<List<Point>> status = new Result<List<Point>>();
        futureObjectsMap.put(synNum, status);

        amberClient.sendMessage(buildHeader(), msg);

        return status;
    }

    private CommonProto.DriverMsg buildGetNextTargetsRequestMsg(int synNum) {
        CommonProto.DriverMsg.Builder driverMsgBuilder = CommonProto.DriverMsg.newBuilder();

        driverMsgBuilder.setType(CommonProto.DriverMsg.MsgType.DATA);
        driverMsgBuilder.setExtension(DriveToPointProto.getNextTargets, true);

        driverMsgBuilder.setSynNum(synNum);

        return driverMsgBuilder.build();
    }

    public Result<Point> getVisitedTarget() throws IOException {
        logger.fine("Get visited target");

        int synNum = getNextSynNum();

        CommonProto.DriverMsg msg = buildGetVisitedTargetRequestMsg(synNum);

        Result<Point> status = new Result<Point>();
        futureObjectsMap.put(synNum, status);

        amberClient.sendMessage(buildHeader(), msg);

        return status;
    }

    private CommonProto.DriverMsg buildGetVisitedTargetRequestMsg(int synNum) {
        CommonProto.DriverMsg.Builder driverMsgBuilder = CommonProto.DriverMsg.newBuilder();

        driverMsgBuilder.setType(CommonProto.DriverMsg.MsgType.DATA);
        driverMsgBuilder.setExtension(DriveToPointProto.getVisitedTarget, true);

        driverMsgBuilder.setSynNum(synNum);

        return driverMsgBuilder.build();
    }

    public Result<List<Point>> getVisitedTargets() throws IOException {
        logger.fine("Get visited targets");

        int synNum = getNextSynNum();

        CommonProto.DriverMsg msg = buildGetVisitedTargetsRequestMsg(synNum);

        Result<List<Point>> status = new Result<List<Point>>();
        futureObjectsMap.put(synNum, status);

        amberClient.sendMessage(buildHeader(), msg);

        return status;
    }

    private CommonProto.DriverMsg buildGetVisitedTargetsRequestMsg(int synNum) {
        CommonProto.DriverMsg.Builder driverMsgBuilder = CommonProto.DriverMsg.newBuilder();

        driverMsgBuilder.setType(CommonProto.DriverMsg.MsgType.DATA);
        driverMsgBuilder.setExtension(DriveToPointProto.getVisitedTargets, true);

        driverMsgBuilder.setSynNum(synNum);

        return driverMsgBuilder.build();
    }

    public Result<Configuration> getConfiguration() throws IOException {
        logger.fine("Get configuration");

        int synNum = getNextSynNum();

        CommonProto.DriverMsg msg = buildGetConfiguration(synNum);

        Result<Configuration> configuration = new Result<Configuration>();
        futureObjectsMap.put(synNum, configuration);

        amberClient.sendMessage(buildHeader(), msg);

        return configuration;
    }

    private CommonProto.DriverMsg buildGetConfiguration(int synNum) {
        CommonProto.DriverMsg.Builder driverMsgBuilder = CommonProto.DriverMsg.newBuilder();

        driverMsgBuilder.setType(CommonProto.DriverMsg.MsgType.DATA);
        driverMsgBuilder.setExtension(DriveToPointProto.getConfiguration, true);

        driverMsgBuilder.setSynNum(synNum);

        return driverMsgBuilder.build();
    }

    private void fillTarget(Result<Point> result, CommonProto.DriverMsg message) {
        DriveToPointProto.Targets targetsMessage = message.getExtension(DriveToPointProto.targets);
        DriveToPointProto.Location locationMessage = message.getExtension(DriveToPointProto.location);

        Point point = null;
        if (targetsMessage.getLongitudesCount() > 0 && targetsMessage.getLatitudesCount() > 0 && targetsMessage.getRadiusesCount() > 0) {
            point = new Point(targetsMessage.getLongitudes(0), targetsMessage.getLatitudes(0), targetsMessage.getRadiuses(0));
        }
        Location location = new Location(locationMessage.getX(), locationMessage.getY(), locationMessage.getAlfa(),
                locationMessage.getP(), locationMessage.getTimeStamp());

        result.setResult(point);
        result.setLocation(location);
        result.setAvailable();
    }

    private void fillTargets(Result<List<Point>> result, CommonProto.DriverMsg message) {
        DriveToPointProto.Targets targetsMessage = message.getExtension(DriveToPointProto.targets);
        List<Point> points = new LinkedList<Point>();
        DriveToPointProto.Location locationMessage = message.getExtension(DriveToPointProto.location);

        List<Double> longitudes = targetsMessage.getLongitudesList();
        List<Double> latitudes = targetsMessage.getLatitudesList();
        List<Double> radiuses = targetsMessage.getRadiusesList();

        Iterator<Double> longitudesIterator = longitudes.iterator();
        Iterator<Double> latitudesIterator = latitudes.iterator();
        Iterator<Double> radiusesIterator = radiuses.iterator();

        while (longitudesIterator.hasNext() && latitudesIterator.hasNext() && radiusesIterator.hasNext()) {
            points.add(new Point(longitudesIterator.next(), latitudesIterator.next(), radiusesIterator.next()));
        }
        Location location = new Location(locationMessage.getX(), locationMessage.getY(), locationMessage.getAlfa(),
                locationMessage.getP(), locationMessage.getTimeStamp());

        result.setResult(points);
        result.setLocation(location);
        result.setAvailable();
    }

    private void fillConfiguration(Result<Configuration> result, CommonProto.DriverMsg message) {
        DriveToPointProto.Configuration configurationMessage = message.getExtension(DriveToPointProto.configuration);
        Configuration configuration = new Configuration(configurationMessage.getMaxSpeed());

        result.setResult(configuration);
        result.setAvailable();
    }
}
