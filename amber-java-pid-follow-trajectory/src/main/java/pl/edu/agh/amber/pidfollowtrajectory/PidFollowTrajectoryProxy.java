package pl.edu.agh.amber.pidfollowtrajectory;

import com.google.protobuf.ExtensionRegistry;
import pl.edu.agh.amber.common.AmberClient;
import pl.edu.agh.amber.common.AmberProxy;
import pl.edu.agh.amber.common.FutureObject;
import pl.edu.agh.amber.common.proto.CommonProto;
import pl.edu.agh.amber.pidfollowtrajectory.proto.PidFollowTrajectoryProto;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class PidFollowTrajectoryProxy extends AmberProxy {

    /* Magic-constant */
    private final static int DEVICE_TYPE = 10;

    private int synNum = 100;
    private final ExtensionRegistry extensionRegistry;

    private Map<Integer, FutureObject> futureObjectsMap = new ConcurrentHashMap<Integer, FutureObject>();

    public PidFollowTrajectoryProxy(AmberClient amberClient, int deviceID) {
        super(DEVICE_TYPE, deviceID, amberClient, Logger.getLogger("PidFollowTrajectoryProxy"));

        logger.info("Starting and registering PidFollowTrajectoryProxy.");

        extensionRegistry = ExtensionRegistry.newInstance();
        PidFollowTrajectoryProto.registerAllExtensions(extensionRegistry);
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
                        if (message.getExtension(PidFollowTrajectoryProto.getNextTargets) ||
                                message.getExtension(PidFollowTrajectoryProto.getVisitedTargets)) {
                            fillTargets((Result<List<Point>>) futureObject, message);

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

    public void setTargets(List<Point> targets, Configuration configuration) throws IOException {
        logger.fine("Set targets");

        CommonProto.DriverMsg msg = buildSetTargetsRequestMsg(targets, configuration);
        amberClient.sendMessage(buildHeader(), msg);
    }

    public CommonProto.DriverMsg buildSetTargetsRequestMsg(List<Point> targets, Configuration configuration) {
        CommonProto.DriverMsg.Builder driverMsgBuilder = CommonProto.DriverMsg.newBuilder();

        driverMsgBuilder.setType(CommonProto.DriverMsg.MsgType.DATA);
        driverMsgBuilder.setExtension(PidFollowTrajectoryProto.setTargets, true);

        PidFollowTrajectoryProto.Targets.Builder targetsBuilder = PidFollowTrajectoryProto.Targets.newBuilder();

        List<Double> longitudes = new LinkedList<Double>();
        for (Point target : targets) {
            longitudes.add(target.getY());
        }

        List<Double> latitudes = new LinkedList<Double>();
        for (Point target : targets) {
            latitudes.add(target.getX());
        }

        List<Long> times = new LinkedList<Long>();
        for (Point target : targets) {
            times.add(target.getTime());
        }

        targetsBuilder.addAllLongitudes(longitudes);
        targetsBuilder.addAllLatitudes(latitudes);
        targetsBuilder.addAllTimes(times);

        PidFollowTrajectoryProto.Targets generatedTargets = targetsBuilder.build();
        driverMsgBuilder.setExtension(PidFollowTrajectoryProto.targets, generatedTargets);

        PidFollowTrajectoryProto.Configuration generatedConfiguration = PidFollowTrajectoryProto.Configuration.newBuilder()
                .setLoopSleepTime(configuration.getLoopSleepTime())
                .setMaxCentricAcceleration(configuration.getCentreAcceleration())
                .setMaxLinearVelocity(configuration.getMaxLinearVelocity())
                .setMaxLookahead(configuration.getLookahead())
                .setScale(configuration.getScale())
                .setWeighLinear(configuration.getiTrack())
                .setWeightAngular(configuration.getiAlpha())
                .build();
        driverMsgBuilder.setExtension(PidFollowTrajectoryProto.configuration, generatedConfiguration);

        return driverMsgBuilder.build();
    }

    public void stopExecution() throws IOException {
        logger.fine("Stopping execution");

        int synNum = getNextSynNum();

        CommonProto.DriverMsg msg = buildStopExecutionRequest(synNum);

        amberClient.sendMessage(buildHeader(), msg);
    }

    public void startExecution() throws IOException {
        logger.fine("Starting execution");

        int synNum = getNextSynNum();

        CommonProto.DriverMsg msg = buildStartExecutionRequest(synNum);

        amberClient.sendMessage(buildHeader(), msg);
    }

    public void stepExecution() throws IOException {
        logger.fine("Step execution");

        int synNum = getNextSynNum();

        CommonProto.DriverMsg msg = buildStepExecutionRequest(synNum);

        amberClient.sendMessage(buildHeader(), msg);
    }

    private CommonProto.DriverMsg buildStopExecutionRequest(int synNum) {
        CommonProto.DriverMsg.Builder driverMsgBuilder = CommonProto.DriverMsg.newBuilder();

        driverMsgBuilder.setType(CommonProto.DriverMsg.MsgType.DATA);
        driverMsgBuilder.setExtension(PidFollowTrajectoryProto.stopExecution, true);

        driverMsgBuilder.setSynNum(synNum);

        return driverMsgBuilder.build();
    }

    private CommonProto.DriverMsg buildStartExecutionRequest(int synNum) {
        CommonProto.DriverMsg.Builder driverMsgBuilder = CommonProto.DriverMsg.newBuilder();

        driverMsgBuilder.setType(CommonProto.DriverMsg.MsgType.DATA);
        driverMsgBuilder.setExtension(PidFollowTrajectoryProto.startExecution, true);

        driverMsgBuilder.setSynNum(synNum);

        return driverMsgBuilder.build();
    }

    private CommonProto.DriverMsg buildStepExecutionRequest(int synNum) {
        CommonProto.DriverMsg.Builder driverMsgBuilder = CommonProto.DriverMsg.newBuilder();

        driverMsgBuilder.setType(CommonProto.DriverMsg.MsgType.DATA);
        driverMsgBuilder.setExtension(PidFollowTrajectoryProto.stepExecution, true);

        driverMsgBuilder.setSynNum(synNum);

        return driverMsgBuilder.build();
    }

    public Result<List<Point>> getNextTargets() throws IOException {
        logger.fine("Get next targets");

        int synNum = getNextSynNum();

        CommonProto.DriverMsg msg = buildGetNextTargetsRequestMsg(synNum);

        Result<List<Point>> result = new Result<List<Point>>();
        futureObjectsMap.put(synNum, result);

        amberClient.sendMessage(buildHeader(), msg);

        return result;
    }

    private CommonProto.DriverMsg buildGetNextTargetsRequestMsg(int synNum) {
        CommonProto.DriverMsg.Builder driverMsgBuilder = CommonProto.DriverMsg.newBuilder();

        driverMsgBuilder.setType(CommonProto.DriverMsg.MsgType.DATA);
        driverMsgBuilder.setExtension(PidFollowTrajectoryProto.getNextTargets, true);

        driverMsgBuilder.setSynNum(synNum);

        return driverMsgBuilder.build();
    }

    public Result<List<Point>> getVisitedTargets() throws IOException {
        logger.fine("Get visited targets");

        int synNum = getNextSynNum();

        CommonProto.DriverMsg msg = buildGetVisitedTargetsRequestMsg(synNum);

        Result<List<Point>> result = new Result<List<Point>>();
        futureObjectsMap.put(synNum, result);

        amberClient.sendMessage(buildHeader(), msg);

        return result;
    }

    private CommonProto.DriverMsg buildGetVisitedTargetsRequestMsg(int synNum) {
        CommonProto.DriverMsg.Builder driverMsgBuilder = CommonProto.DriverMsg.newBuilder();

        driverMsgBuilder.setType(CommonProto.DriverMsg.MsgType.DATA);
        driverMsgBuilder.setExtension(PidFollowTrajectoryProto.getVisitedTargets, true);

        driverMsgBuilder.setSynNum(synNum);

        return driverMsgBuilder.build();
    }


    private void fillTargets(Result<List<Point>> result, CommonProto.DriverMsg message) {
        PidFollowTrajectoryProto.Targets targetsMessage = message.getExtension(PidFollowTrajectoryProto.targets);
        List<Point> points = new LinkedList<Point>();

        List<Double> longitudes = targetsMessage.getLongitudesList();
        List<Double> latitudes = targetsMessage.getLatitudesList();
        List<Long> times = targetsMessage.getTimesList();

        Iterator<Double> longitudesIterator = longitudes.iterator();
        Iterator<Double> latitudesIterator = latitudes.iterator();
        Iterator<Long> timesIterator = times.iterator();

        while (longitudesIterator.hasNext() && latitudesIterator.hasNext() && timesIterator.hasNext()) {
            points.add(new Point(latitudesIterator.next(), longitudesIterator.next(), timesIterator.next()));
        }

        result.setResult(points);
        result.setAvailable();
    }
}
