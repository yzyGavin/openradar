package de.knewcleus.openradar.radardata.fgatc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import de.knewcleus.fgfs.Units;
import de.knewcleus.openradar.radardata.RadarDataProvider;

public class FGATCEndpoint extends RadarDataProvider implements Runnable {
	protected final static Logger logger=Logger.getLogger("de.knewcleus.openradar.radardata.fgatc");
	protected final static int receiveBufferLength=1024;
	
	protected final DatagramSocket datagramSocket;
	
	protected final Map<TargetIdentifier, TargetStatus> targetMap = new HashMap<TargetIdentifier, TargetStatus>();
	
	/**
	 * Time for a single antenna rotation in milliseconds.
	 * 
	 * At most one radar data packet per aircraft is sent per antenna rotation.
	 */
	protected static final int antennaRotationTimeMsecs=1000;
	
	/**
	 * Timeout for removal of stale targets in milliseconds.
	 */
	protected static final int staleTargetTimeoutMsecs=4 * antennaRotationTimeMsecs;

	public FGATCEndpoint() throws IOException {
		datagramSocket=new DatagramSocket();
		datagramSocket.setSoTimeout(antennaRotationTimeMsecs/2);
	}
	
	public FGATCEndpoint(int port) throws IOException {
		datagramSocket=new DatagramSocket(port);
		datagramSocket.setSoTimeout(antennaRotationTimeMsecs/2);
	}
	
	@Override
	public void run() {
		while (!Thread.interrupted()) {
			try {
				processPacket();
			} catch (SocketTimeoutException e) {
				// Do nothing, this is expected
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sendRadarPackets();
			removeStaleTargets();
		}
	}
	
	protected void processPacket() throws IOException {
		byte[] buf=new byte[receiveBufferLength];
		final DatagramPacket datagramPacket=new DatagramPacket(buf,buf.length);
		datagramSocket.receive(datagramPacket);
		
		final TargetIdentifier targetIdentifier=new TargetIdentifier(datagramPacket.getAddress(),datagramPacket.getPort());

		String dataLine=new String(buf, Charset.forName("US-ASCII"));
		dataLine=dataLine.trim();

		logger.finest("Received packet '"+dataLine+"' from "+targetIdentifier);

		final PositionPacket newPacket = parsePacket(dataLine);
		
		if (newPacket==null) {
			logger.warning("Invalid packet '"+dataLine+"' from "+targetIdentifier+", ignoring");
			return;
		}
		
		final TargetStatus status;
		if (!targetMap.containsKey(targetIdentifier)) {
			status = new TargetStatus(targetIdentifier);
			targetMap.put(targetIdentifier, status);
		} else {
			status = targetMap.get(targetIdentifier);
		}
		
		status.update(newPacket);
	}
	
	protected void sendRadarPackets() {
		for (TargetStatus status: targetMap.values()) {
			if (status.getLastAntennaRotationTime() + antennaRotationTimeMsecs >= status.getLastPacketTime()) {
				/* Was not seen for at least one rotation */
				continue;
			}
			/* at least one antenna rotation since the last update, so now we provide the radar data packet */
			publishRadarDataPacket(new RadarDataPacket(status));
			status.setLastAntennaRotationTime(status.getLastPacketTime());
		}
	}
	
	protected void removeStaleTargets() {
		final Iterator<TargetStatus> targetIterator;
		final long earliestStalePacketTime = System.currentTimeMillis() - staleTargetTimeoutMsecs;
		targetIterator = targetMap.values().iterator();
		while (targetIterator.hasNext()) {
			final TargetStatus target = targetIterator.next();
			if (target.getLastPacketTime() < earliestStalePacketTime) { 
				targetIterator.remove();
			}
		}
	}
	
	protected PositionPacket parsePacket(String dataLine) {
		boolean hadPositionTime = false, hadLongitude = false, hadLatitude = false;
		boolean hadSSRActive = false, hadSSRCode = false;
		boolean hadEncoderActive = false, hadEncoderAlt = false;
		
		float positionTime=0.0f;
		double longitude=0.0, latitude=0.0;
		boolean ssrActive = false, encoderActive = false;
		String ssrCode = "0000";
		float encoderAltitude = 0.0f;
		boolean specialPurposeIndicator = false;
		
		for (String element: dataLine.split("\\s+")) {
			int eqIndex=element.indexOf('=');
			if (eqIndex==-1) {
				return null;
			}
			String name=element.substring(0, eqIndex).trim();
			String value=element.substring(eqIndex+1).trim();
			
			if (eqIndex==-1) {
				name=element;
				value="1";
			} else {
				name=element.substring(0, eqIndex).trim();
				value=element.substring(eqIndex+1).trim();
			}
			
			if (name.equals("TIME")) {
				positionTime=Float.parseFloat(value)*(float)Units.SEC;
				hadPositionTime = true;
			} else if (name.equals("LON")) {
				longitude=Double.parseDouble(value)*Units.DEG;
				hadLongitude = true;
			} else if (name.equals("LAT")) {
				latitude=Double.parseDouble(value)*Units.DEG;
				hadLatitude = true;
			} else if (name.equals("SSR_SRV")) {
				ssrActive=(Integer.parseInt(value)!=0);
				hadSSRActive = true;
			} else if (name.equals("SSR_CODE")) {
				ssrCode=value;
				hadSSRCode = true;
			} else if (name.equals("SSR_SPI")) {
				specialPurposeIndicator=(Integer.parseInt(value)!=0);
			} else if (name.equals("ENC_SRV")) {
				encoderActive=(Integer.parseInt(value)!=0);
				hadEncoderActive = true;
			} else if (name.equals("ENC_ALT")) {
				encoderAltitude=Float.parseFloat(value)*(float)Units.FT;
				hadEncoderAlt = true;
			}
		}
		
		if (!hadPositionTime || !hadLongitude || !hadLatitude || !hadSSRActive || !hadEncoderActive) {
			return null;
		}
		
		if (ssrActive && !hadSSRCode) {
			return null;
		}
		
		if (encoderActive && !hadEncoderAlt ) {
			return null;
		}
		
		if (!encoderActive) {
			encoderAltitude = 0.0f;
		}
		
		if (!ssrActive) {
			ssrCode = "0000";
		}
		return new PositionPacket(positionTime, longitude, latitude,
				ssrActive, ssrCode, encoderActive, encoderAltitude, specialPurposeIndicator);
	}
}