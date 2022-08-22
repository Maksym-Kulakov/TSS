package de.jade.ecs.map;

import de.jade.ecs.map.riskassessment.TcpaCalculation;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.referencing.GeodeticCalculator;
import org.opengis.geometry.DirectPosition;
import org.scheduler.Scheduler;
import org.scheduler.SchedulerConfiguration;
import org.scheduler.agent.Agent;
import org.scheduler.agent.behaviour.LinearDrivingBehaviour;
import org.scheduler.agent.behaviour.NmeaPosReportUdpOutputBehaviour;
import org.scheduler.agent.state.ShipState;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class ApplicationCPA implements Runnable {
	public static HashMap<Integer, Double> shipsSituations = new HashMap<>();
	TcpaCalculation tcpaCalculation;
	ShipAis shipA;
	ShipAis shipB;

	public ApplicationCPA(TcpaCalculation tcpaCalculation) {
		this.tcpaCalculation = tcpaCalculation;
	}

	@Override
	public void run() {

		while (true) {
			System.out.println("## Ship Simulation ##");
			do {
				shipA = tcpaCalculation.getShip(ShipAis.shipsInsideAreaToEastAisHashMap);
				shipB = tcpaCalculation.getShip(ShipAis.shipsInsideAreaInWeserAisHashMap);
			}
			while (shipA == null || shipB == null);

			if (shipsSituations.containsKey(shipA.getMmsi() + shipB.getMmsi())) {
				continue;
			}

			System.out.println("crossing situation 1");
//				double freq = 1;
//				double simulationSpeed = 100;
//				Scheduler scheduler = new Scheduler(new SchedulerConfiguration(freq, simulationSpeed));

			/** Ship A **/
			ShipState shipStateA = new ShipState(shipA.getMmsi(), new Point2D.Double(shipA.latitude, shipA.longitude), shipA.hdg, shipA.speed); //8, 54 // 8.16, 54
//				Agent agentA = new Agent(scheduler, new LinearDrivingBehaviour(shipStateA), new NmeaPosReportUdpOutputBehaviour(shipStateA, 2947));
//				scheduler.registerAgent(agentA);

			/** Ship B **/
			ShipState shipStateB = new ShipState(shipB.getMmsi(), new Point2D.Double(shipB.latitude, shipB.longitude), shipB.hdg, shipB.speed); //8.16, 53.9 | 0.45507,-0.29586
//				Agent agentB = new Agent(scheduler, new LinearDrivingBehaviour(shipStateB), new NmeaPosReportUdpOutputBehaviour(shipStateB, 2947));
//				scheduler.registerAgent(agentB);

			//	scheduler.registerAgent(new Agent(scheduler, new LinearDrivingBehaviour(shipStateB),new NmeaPosReportUdpOutputBehaviour(shipStateB, 2947)));

//				scheduler.start();

			GeodeticCalculator geoCalc = GeodeticCalculator.create(CommonCRS.WGS84.geographic());

//				while (true) {
//
//					try {
//						TimeUnit.SECONDS.sleep(1);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
			/** wgs-84 to polar **/
			Point2D position1 = shipStateA.getPoint();
			geoCalc.setStartGeographicPoint(position1.getY(), position1.getX());

			Point2D position2 = shipStateB.getPoint();
			geoCalc.setEndGeographicPoint(position2.getY(), position2.getX());

			double distance = geoCalc.getGeodesicDistance();
			double azi = geoCalc.getStartingAzimuth();
			azi = (azi + 540 + 180) % 360;
			azi = Math.toRadians(azi);


			/** polar to cartesian **/
			double x = distance * Math.cos(azi);
			double y = distance * Math.sin(azi);

			/** create tracks **/
			double speed_kn1 = shipStateA.getSpeed_current_kn();
			double speed_kn2 = shipStateB.getSpeed_current_kn();
			Track track1 = new Track(0, 0, speed_kn1 * 1.852 / 3.6, shipStateA.getHeading_current_deg());
			Track track2 = new Track(x, y, speed_kn2 * 1.852 / 3.6, shipStateB.getHeading_current_deg());

			/** calculate CPA **/
			double cpaTime = Track.cpaTime(track1, track2);

			System.out.print("TCPA: " + cpaTime + ", ");

			/** calc CPA-P1 **/
			geoCalc.setStartGeographicPoint(position1.getY(), position1.getX());
			if (Math.signum(cpaTime) == -1) {
				geoCalc.setStartingAzimuth((shipStateA.getHeading_current_deg() + 180) % 360);
			} else {
				geoCalc.setStartingAzimuth(shipStateA.getHeading_current_deg());
			}
			geoCalc.setGeodesicDistance(speed_kn1 * 1.852 / 3.6 * Math.abs(cpaTime));
			DirectPosition position1Future = geoCalc.getEndPoint();

			/** calc CPA-P2 **/
			geoCalc.setStartGeographicPoint(position2.getY(), position2.getX());
			if (Math.signum(cpaTime) == -1) {
				geoCalc.setStartingAzimuth((shipStateB.getHeading_current_deg() + 180) % 360);
			} else {
				geoCalc.setStartingAzimuth(shipStateB.getHeading_current_deg());
			}
			geoCalc.setGeodesicDistance(speed_kn2 * 1.852 / 3.6 * Math.abs(cpaTime));
			DirectPosition position2Future = geoCalc.getEndPoint();

			/** calc distance between CPA-P1 & CPA-P2 **/
			geoCalc.setStartPoint(position1Future);
			geoCalc.setEndPoint(position2Future);
			double cpaDistance = geoCalc.getGeodesicDistance();

			System.out.println("DCPA: " + cpaDistance + " | HeadingA: " + shipStateA.getHeading_current_deg() + " | HeadingB: " + shipStateB.getHeading_current_deg());

			shipsSituations.put(shipA.getMmsi() + shipB.getMmsi(), cpaTime);

//			if(cpaDistance < 3072 && cpaTime < 120 && cpaTime > 0) {
//				/** set new heading ranging from 0 to 360 **/
//				double newHeading = (shipStateA.getHeading_current_deg()-1)%360;
//				if(newHeading < 0) newHeading = newHeading+360;
//				shipStateA.setHeading_commanded_deg(newHeading);
//			}

//				}
		}
	}
}
