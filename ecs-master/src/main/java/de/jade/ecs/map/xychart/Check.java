package de.jade.ecs.map.xychart;

import de.jade.ecs.map.Track;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.referencing.GeodeticCalculator;
import org.opengis.geometry.DirectPosition;
import org.scheduler.agent.state.ShipState;

import java.awt.geom.Point2D;

public class Check {
    public static void main(String[] args) {
        GeodeticCalculator geoCalc = GeodeticCalculator.create(CommonCRS.WGS84.geographic());
        ShipState shipStateA = new ShipState(12345678,
                new Point2D.Double(10, 10), 90, 6); //8, 54 // 8.16, 54
        ShipState shipStateB = new ShipState(87654321,
                new Point2D.Double(10.2, 10.2), 270, 6); //8.16, 53.9 | 0.45507,-0.29586

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

        /** calc location of cpa (center) **/
        geoCalc.setStartGeographicPoint(position1Future.getCoordinate()[0], position1Future.getCoordinate()[1]);
        geoCalc.setStartingAzimuth(startingAzimuth);
        geoCalc.setGeodesicDistance(cpaDistance/2);
        DirectPosition cpaCenterLocation = geoCalc.getEndPoint();

        System.out.println("DCPA: " + cpaDistance + " | HeadingA: "
                + shipStateA.getHeading_current_deg() + " | HeadingB: "
                + shipStateB.getHeading_current_deg()
                + " | cpaCenter: " + cpaCenterLocation);

    }
}
