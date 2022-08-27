package de.jade.ecs.map;

import de.jade.ecs.map.shipchart.PairHash;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.referencing.GeodeticCalculator;
import org.opengis.geometry.DirectPosition;
import org.scheduler.agent.state.ShipState;
import java.awt.geom.Point2D;
import java.util.HashMap;

public class ApplicationCPA implements Runnable {
	public static HashMap<Integer, ConflictShips> shipsConflicts = new HashMap<>();
	GeodeticCalculator geoCalc = GeodeticCalculator.create(CommonCRS.WGS84.geographic());
	int count;

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			createConflictSituationsForTwoAreas(ShipAis.shipsInsideAreaToNorthAisHashMap,
					ShipAis.shipsInsideAreaToSouthAisHashMap);
			createConflictSituationsForTwoAreas(ShipAis.shipsInsideAreaToEastAisHashMap,
					ShipAis.shipsInsideAreaToNorthAisHashMap);
			createConflictSituationsForTwoAreas(ShipAis.shipsInsideAreaToEastAisHashMap,
					ShipAis.shipsInsideAreaToSouthAisHashMap);
			count++;
		}
	}

	private void createConflictSituationsForTwoAreas(HashMap<Integer, ShipAis> firstArea,
									  HashMap<Integer, ShipAis> secondArea) {
		firstArea.values().forEach(shipAis -> {
			secondArea.values().forEach(shipAIS2 -> {
				doConflictSituations(shipAis, shipAIS2);
			});
		});
	}


	private void doConflictSituations(ShipAis shipAis, ShipAis shipAIS2) {
		ShipState shipStateA = new ShipState(shipAis.getMmsi(),
				new Point2D.Double(shipAis.latitude, shipAis.longitude), shipAis.hdg, shipAis.speed); //8, 54 // 8.16, 54
		ShipState shipStateB = new ShipState(shipAIS2.getMmsi(),
				new Point2D.Double(shipAIS2.latitude, shipAIS2.longitude), shipAIS2.hdg, shipAIS2.speed); //8.16, 53.9 | 0.45507,-0.29586

		Point2D position1 = shipStateA.getPoint();
		geoCalc.setStartGeographicPoint(position1.getY(), position1.getX());

		Point2D position2 = shipStateB.getPoint();
		geoCalc.setEndGeographicPoint(position2.getX(), position2.getY());

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

		/** calculate TCPA **/
		double cpaTime = Track.cpaTime(track1, track2);

		System.out.print("TCPA: " + cpaTime + ", ");

		/** calc CPA-P1 **/
		geoCalc.setStartGeographicPoint(position1.getX(), position1.getY());
		if (Math.signum(cpaTime) == -1) {
			geoCalc.setStartingAzimuth((shipStateA.getHeading_current_deg() + 180) % 360);
		} else {
			geoCalc.setStartingAzimuth(shipStateA.getHeading_current_deg());
		}
		geoCalc.setGeodesicDistance(speed_kn1 * 1.852 / 3.6 * Math.abs(cpaTime));
		DirectPosition position1Future = geoCalc.getEndPoint();

		/** calc CPA-P2 **/
		geoCalc.setStartGeographicPoint(position2.getX(), position2.getY());
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
		double startingAzimuth = geoCalc.getStartingAzimuth();

		/** calc cpa center psn **/
		geoCalc.setStartGeographicPoint(position1Future.getCoordinate()[0], position1Future.getCoordinate()[1]);
		geoCalc.setStartingAzimuth(startingAzimuth);
		geoCalc.setGeodesicDistance(cpaDistance/2);
		DirectPosition cpaCenterPsn = geoCalc.getEndPoint();

		System.out.println("DCPA: " + cpaDistance + " | HeadingA: "
				+ shipStateA.getHeading_current_deg() + " | HeadingB: "
				+ shipStateB.getHeading_current_deg()
				+ " | cpaCenter: " + cpaCenterPsn);

		Integer key = new PairHash(shipAis.getMmsi(), shipAIS2.getMmsi()).hashCode();
		ConflictShips conflictShips
				= new ConflictShips(cpaDistance, cpaTime, cpaCenterPsn, shipAis, shipAIS2);
		shipsConflicts.put(key, conflictShips);
	}
}
