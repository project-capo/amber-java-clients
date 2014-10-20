amber-java-clients
==================

[![Build Status](https://travis-ci.org/project-capo/amber-java-clients.svg?branch=master)](https://travis-ci.org/project-capo/amber-java-clients)

This repository contains library used to managing drivers located on robot controlled by Amber mediator.

Supported devices
-----------------

* Hokuyo by `amber-java-hokuyo` - laser range scanner
* 9DOF by `amber-java-ninedof` - sensor stick with accelerometer, magnetometer and gyro
* Roboclaw by `amber-java-roboclaw` - motor controllers

Requirements
------------

* `jdk7` with `maven`
* `protobuf` and `protoc` from `protobuf-compiler`

How to deploy
-------------

* Clone this project.
* Import project to your favorite IDE.

If you want to use packages, run `mvn install` ar `mvn package` inside project.

If project cannot be build in IDE due to import errors, check if `target/generated-sources/java` is selected as *source* in every module (if exists).

How to use (maven)
------------------

Simply. Add following lines to your projects `pom.xml`:

    <repositories>
        <repository>
            <id>amber-java-clients-mvn-repo</id>
            <url>https://github.com/project-capo/amber-java-clients/raw/mvn-repo</url>
        </repository>
    </repositories>

Next, add following selected dependencies:

    <dependencies>
        <dependency>
            <groupId>pl.edu.agh.amber.common</groupId>
            <artifactId>amber-java-common</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>pl.edu.agh.amber.hokuyo</groupId>
            <artifactId>amber-java-hokuyo</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>pl.edu.agh.amber.ninedof</groupId>
            <artifactId>amber-java-ninedof</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>pl.edu.agh.amber.roboclaw</groupId>
            <artifactId>amber-java-roboclaw</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
    </dependencies>

How to use (jar)
----------------

You can download jars from maven repository. You **need** to use *common part* with other jars. Find the **latest** version in places:

 * [Common part](https://github.com/project-capo/amber-java-clients/tree/mvn-repo/pl/edu/agh/amber/common/amber-java-common/1.0-SNAPSHOT "Common part") *required*
 * [Hokuyo client](https://github.com/project-capo/amber-java-clients/tree/mvn-repo/pl/edu/agh/amber/hokuyo/amber-java-hokuyo/1.0-SNAPSHOT "Hokuyo client") for laser range finder
 * [Ninedof client](https://github.com/project-capo/amber-java-clients/tree/mvn-repo/pl/edu/agh/amber/ninedof/amber-java-ninedof/1.0-SNAPSHOT "Ninedof client") for accelerometer, magnetometer and gyro sensors
 * [Roboclaw client](https://github.com/project-capo/amber-java-clients/tree/mvn-repo/pl/edu/agh/amber/roboclaw/amber-java-roboclaw/1.0-SNAPSHOT "Roboclaw client") for motor controllers

Examples
========

Motor controllers Roboclaw
--------------------------

Motor speed values is in unit `mm/s`.

Example code:

    AmberClient client = new AmberClient("192.168.1.50", 26233);
    RoboclawProxy roboclawProxy = new RoboclawProxy(client, 0);
    
    for (int i = 1; i <= 10; i++) {
        roboclawProxy.sendMotorsCommand(100 * i, 100 * i, 100 * i, 100 * i);
        
        Thread.sleep(500);
    }
    
    MotorsCurrentSpeed mcs = roboclawProxy.getCurrentMotorsSpeed();
    mcs.waitAvailable();
    
    System.out.println(String.format("Motors current speed: fl: %d, fr: %d, rl: %d, rr: %d",
        mcs.getFrontLeftSpeed(), mcs.getFrontRightSpeed(), mcs.getRearLeftSpeed(), mcs.getRearRightSpeed()));
    
    roboclawProxy.stopMotors();
    client.terminate();

9DOF sensors
------------

Values are in units:

* accelerometer - `mG`
* gyro - `°/min`
* magnetometer - `mGs`

Example code:

    AmberClient client = new AmberClient("192.168.1.50", 26233);
    NinedofProxy ninedofProxy = new NinedofProxy(client, 0);
    
    for (int i = 0; i < 10; i++) {
        NinedofData ninedofData = ninedofProxy.getAxesData(true, true, true);
        ninedofData.waitAvailable();
        
        System.out.println(ninedofData.getAccel().xAxis);
        
        Thread.sleep(10);
    }
    
    ninedofProxy.registerNinedofDataListener(10, true, true, true, new CyclicDataListener<NinedofData>() {
    
        @Override
        public void handle(NinedofData ninedofData) {
            System.out.println(ninedofData.getAccel().xAxis);
        }
    });
    
    client.terminate();