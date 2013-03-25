package pl.edu.agh.amber.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.collections.keyvalue.MultiKey;

import pl.edu.agh.amber.common.proto.CommonProto.DriverHdr;
import pl.edu.agh.amber.common.proto.CommonProto.DriverMsg;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

public class AmberClient implements Runnable {

	private final DatagramSocket socket;
	private final InetAddress address;
	private final int port;

	private final static int RECEIVING_BUFFER_SIZE = 4096;

	private Map<MultiKey, AmberProxy> proxyMap = new HashMap<MultiKey, AmberProxy>();
	private Thread receivingThread;

	private static Logger logger = Logger.getLogger("AmberClient");

	public void registerClient(int deviceType, int deviceID, AmberProxy proxy) {
		proxyMap.put(new MultiKey(deviceType, deviceID), proxy);
	}

	public AmberClient(String hostname, int port) throws IOException {

		this.socket = new DatagramSocket();
		this.address = InetAddress.getByName(hostname);
		this.port = port;

		receivingThread = new Thread(this);
		receivingThread.start();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				terminate();
			}
		});
	}

	@Override
	public void run() {
		messageReceivingLoop();
	}

	private void messageReceivingLoop() {
		DatagramPacket packet = new DatagramPacket(
				new byte[RECEIVING_BUFFER_SIZE], RECEIVING_BUFFER_SIZE);
		AmberProxy clientProxy = null;

		while (true) {
			try {
				socket.receive(packet);

				byte[] packetBytes = packet.getData();

				int headerLen = (packetBytes[0] << 8) | packetBytes[1];
				ByteString headerByteString = ByteString.copyFrom(
						packet.getData(), 2, headerLen);
				DriverHdr header = DriverHdr.parseFrom(headerByteString);

				int messageLen = (packetBytes[2 + headerLen] << 8)
						| packetBytes[2 + headerLen + 1];
				ByteString messageByteString = ByteString.copyFrom(
						packet.getData(), 2 + headerLen + 2, messageLen);
				DriverMsg message;

				if (!header.hasDeviceType() || !header.hasDeviceID()
						|| header.getDeviceType() == 0) {
					message = DriverMsg.parseFrom(messageByteString);
					handleMessageFromMediator(header, message);

				} else {
					clientProxy = proxyMap.get(new MultiKey(header
							.getDeviceType(), header.getDeviceID()));

					if (clientProxy == null) {
						logger.fine(String.format(
								"Client proxy with given device type (%d) and ID (%d) not found, "
										+ "ignoring message.",
								header.getDeviceType(), header.getDeviceID()));
						continue;
					}

					message = DriverMsg.parseFrom(messageByteString,
							clientProxy.getExtensionRegistry());
					handleMessageFromDriver(header, message, clientProxy);
				}

			} catch (InvalidProtocolBufferException ex) {
				logger.fine("Error in parsing the message, ignoring.");

			} catch (IOException e) {

				if (socket.isClosed()) {
					logger.info("Socket closed, exiting.");
					return;
				}

				logger.fine("Error in receiving packet: " + e);
			}
		}
	}

	private void handleMessageFromMediator(DriverHdr header, DriverMsg message) {

		switch (message.getType()) {
		case DATA:
			logger.fine("DATA message came, but device details not set, ignoring.");
			break;

		case PING:
			logger.fine("PING message came, handling.");
			handlePingMessage(header, message);
			break;

		case PONG:
			logger.fine("PONG message came, handling.");
			handlePongMessage(header, message);
			break;

		case DRIVER_DIED:
			logger.fine("DRIVER_DIED message came, but device details not set, ignoring.");
			break;

		default:
			logger.fine(String.format("Unexpected message came: %s, ignoring.",
					message.getType().toString()));
		}

	}

	private void handleMessageFromDriver(DriverHdr header, DriverMsg message,
			AmberProxy clientProxy) {

		switch (message.getType()) {
		case DATA:
			logger.fine(String.format(
					"DATA message came for (%d: %d), handling.",
					clientProxy.deviceType, clientProxy.deviceID));
			clientProxy.handleDataMsg(header, message);
			break;

		case PING:
			logger.fine(String.format(
					"PING message came for (%d: %d), handling.",
					clientProxy.deviceType, clientProxy.deviceID));
			clientProxy.handlePingMessage(header, message);
			break;

		case PONG:
			logger.fine(String.format(
					"PONG message came for (%d: %d), handling.",
					clientProxy.deviceType, clientProxy.deviceID));
			clientProxy.handlePongMessage(header, message);
			break;

		case DRIVER_DIED:
			logger.fine(String.format(
					"DRIVER_DIED message came dor (%d: %d), handling.",
					clientProxy.deviceType, clientProxy.deviceID));
			clientProxy.handleDriverDiedMessage(header, message);
			break;

		default:
			logger.fine(String.format(
					"Unexpected message came %s for (%d: %d), ignoring.",
					message.getType().toString(), clientProxy.deviceType,
					clientProxy.deviceID));
		}

	}

	synchronized public void sendMessage(DriverHdr header, DriverMsg message)
			throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		int len;

		// Header length
		len = header.getSerializedSize();
		outputStream.write((byte) (len >> 8) & 0xff);
		outputStream.write((byte) (len & 0xff));

		// Header
		outputStream.write(header.toByteArray());

		// Message length
		len = message.getSerializedSize();
		outputStream.write((byte) (len >> 8) & 0xff);
		outputStream.write((byte) (len & 0xff));

		// Message
		outputStream.write(message.toByteArray());

		logger.fine(String.format("Sending an UDP packet for (%d: %d).",
				header.getDeviceType(), header.getDeviceID()));

		DatagramPacket packet = new DatagramPacket(outputStream.toByteArray(),
				outputStream.size(), address, port);
		socket.send(packet);
	}

	private void handlePingMessage(DriverHdr header, DriverMsg message) {

	}

	private void handlePongMessage(DriverHdr header, DriverMsg message) {

	}

	public void terminate() {
		terminateProxies();
		socket.close();
		receivingThread.interrupt();
	}

	public void terminateProxies() {
		for (AmberProxy proxy : proxyMap.values()) {
			proxy.terminateProxy();
		}
	}

}
