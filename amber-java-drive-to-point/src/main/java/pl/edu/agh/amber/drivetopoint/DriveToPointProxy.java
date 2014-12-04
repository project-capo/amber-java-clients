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

    private void fillTarget(Result<Point> result, CommonProto.DriverMsg message) {
        DriveToPointProto.Targets targets = message.getExtension(DriveToPointProto.targets);

        Point point = null;
        if (targets.getLongitudesCount() > 0 && targets.getLatitudesCount() > 0 && targets.getRadiusesCount() > 0) {
            point = new Point(targets.getLongitudes(0), targets.getLatitudes(0), targets.getRadiuses(0));
        }

        result.setResult(point);
        result.setAvailable();
    }

    private void fillTargets(Result<List<Point>> result, CommonProto.DriverMsg message) {
        DriveToPointProto.Targets targets = message.getExtension(DriveToPointProto.targets);
        List<Point> points = new LinkedList<Point>();

        List<Double> longitudes = targets.getLongitudesList();
        List<Double> latitudes = targets.getLatitudesList();
        List<Double> radiuses = targets.getRadiusesList();

        Iterator<Double> longitudesIterator = longitudes.iterator();
        Iterator<Double> latitudesIterator = latitudes.iterator();
        Iterator<Double> radiusesIterator = radiuses.iterator();

        while (longitudesIterator.hasNext() && latitudesIterator.hasNext() && radiusesIterator.hasNext()) {
            points.add(new Point(longitudesIterator.next(), latitudesIterator.next(), radiusesIterator.next()));
        }

        result.setResult(points);
        result.setAvailable();
    }
}
