amber-java-clients
==================

[![Build Status](https://travis-ci.org/project-capo/amber-java-clients.svg?branch=master)](https://travis-ci.org/project-capo/amber-java-clients)

This repository contains library used to managing drivers located on robot controlled by Amber mediator.

Supported devices
-----------------

* Hitec by `amber-java-hitec` - servo motor used in robot arm or 3D laser range scanner
* Hokuyo by `amber-java-hokuyo` - laser range scanner
* Location by `amber-java-location` - relative robots location  
* 9DOF by `amber-java-ninedof` - sensor stick with accelerometer, magnetometer and gyro
* Roboclaw by `amber-java-roboclaw` - motor controllers

Requirements
------------

* `jdk7` with `maven`
* `protobuf` and `protoc` from `protobuf-compiler`

How to deploy
-------------

* Clone this project.
* `mvn install` inside project.
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
            <groupId>pl.edu.agh.amber.drivetopoint</groupId>
            <artifactId>amber-java-drive-to-point</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>pl.edu.agh.amber.hokuyo</groupId>
            <artifactId>amber-java-hokuyo</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>pl.edu.agh.amber.hokuyo-scanner</groupId>
            <artifactId>amber-java-hokuyo-scanner</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>pl.edu.agh.amber.location</groupId>
            <artifactId>amber-java-location</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>pl.edu.agh.amber.maestro</groupId>
            <artifactId>amber-java-maestro</artifactId>
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
 * [Hitec client](https://github.com/project-capo/amber-java-clients/tree/mvn-repo/pl/edu/agh/amber/hitec/amber-java-hitec/1.0-SNAPSHOT "Hitec client") for hitec servometer
 * [Hokuyo client](https://github.com/project-capo/amber-java-clients/tree/mvn-repo/pl/edu/agh/amber/hokuyo/amber-java-hokuyo/1.0-SNAPSHOT "Hokuyo client") for laser range finder
  * [Hokuyo replacement client](https://github.com/project-capo/amber-java-clients/tree/mvn-repo/pl/edu/agh/amber/hokuyo-scanner/amber-java-hokuyo-scanner/1.0-SNAPSHOT "Hokuyo replacement client") used with *Hitec* instead of standard *Hokuyo* client
 * [Location client](https://github.com/project-capo/amber-java-clients/tree/mvn-repo/pl/edu/agh/amber/location/amber-java-location/1.0-SNAPSHOT "Location client") for location
 * [Ninedof client](https://github.com/project-capo/amber-java-clients/tree/mvn-repo/pl/edu/agh/amber/ninedof/amber-java-ninedof/1.0-SNAPSHOT "Ninedof client") for accelerometer, magnetometer and gyro sensors
 * [Roboclaw client](https://github.com/project-capo/amber-java-clients/tree/mvn-repo/pl/edu/agh/amber/roboclaw/amber-java-roboclaw/1.0-SNAPSHOT "Roboclaw client") for motor controllers

How to contribute
-----------------

Clone this repo, setup your environment, using maven. Next, change what you want and make pull request.

Examples
========

You can find it [here](https://github.com/dev-amber/amber-java-clients/tree/master/amber-java-examples/src/main/java).

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
