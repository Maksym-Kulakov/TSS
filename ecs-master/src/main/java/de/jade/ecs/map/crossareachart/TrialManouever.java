package de.jade.ecs.map.crossareachart;

import de.jade.ecs.map.ApplicationCPA;
import de.jade.ecs.map.ConflictShips;
import de.jade.ecs.map.ShipAis;
import de.jade.ecs.map.Track;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.referencing.GeodeticCalculator;
import org.opengis.geometry.DirectPosition;
import org.scheduler.agent.state.ShipState;

import java.awt.geom.Point2D;
import java.util.Map;

public class TrialManouever {
    public static GeodeticCalculator geoCalc = GeodeticCalculator.create(CommonCRS.WGS84.geographic());

    public static void getSafeCpa(String manouever, double cpa) {
        for (Map.Entry<Integer, ConflictShips> shipsPair
                : CrossAreaChartDraw.shipsPairInConflict.entrySet()) {
            if (CrossAreaChartDraw.shipsToEast.containsKey(shipsPair.getValue().shipA.mmsiNum)
                    && CrossAreaChartDraw.shipsToSouth.containsKey(shipsPair.getValue().shipB.mmsiNum)) {
                getSafeConflict(shipsPair.getKey(), shipsPair.getValue().shipA, shipsPair.getValue().shipB, manouever, cpa);
            } else if (CrossAreaChartDraw.shipsToEast.containsKey(shipsPair.getValue().shipB.mmsiNum)
                    && CrossAreaChartDraw.shipsToSouth.containsKey(shipsPair.getValue().shipA.mmsiNum)) {
                getSafeConflict(shipsPair.getKey(), shipsPair.getValue().shipB, shipsPair.getValue().shipA, manouever, cpa);
            } else if (CrossAreaChartDraw.shipsToNorth.containsKey(shipsPair.getValue().shipA.mmsiNum)
                    && CrossAreaChartDraw.shipsToEast.containsKey(shipsPair.getValue().shipB.mmsiNum)) {
                getSafeConflict(shipsPair.getKey(), shipsPair.getValue().shipA, shipsPair.getValue().shipB, manouever, cpa);
            } else if (CrossAreaChartDraw.shipsToNorth.containsKey(shipsPair.getValue().shipB.mmsiNum)
                    && CrossAreaChartDraw.shipsToEast.containsKey(shipsPair.getValue().shipA.mmsiNum)) {
                getSafeConflict(shipsPair.getKey(), shipsPair.getValue().shipB, shipsPair.getValue().shipA, manouever, cpa);
            } else {
                ConflictShips conflictShips = new ConflictShips(shipsPair.getValue().cpaValue, shipsPair.getValue().tcpaValue,
                        shipsPair.getValue().cpaLocation, new ShipAis(shipsPair.getValue().shipA.mmsiNum, shipsPair.getValue().shipA.latitude,
                        shipsPair.getValue().shipA.longitude, shipsPair.getValue().shipA.hdg, shipsPair.getValue().shipA.speed, shipsPair.getValue().shipA.destination),
                        new ShipAis(shipsPair.getValue().shipB.mmsiNum, shipsPair.getValue().shipB.latitude, shipsPair.getValue().shipB.longitude,
                                shipsPair.getValue().shipB.hdg, shipsPair.getValue().shipB.speed, shipsPair.getValue().shipB.destination),
                        shipsPair.getValue().position1Future, shipsPair.getValue().position2Future);
                CrossAreaChartDraw.trialShipsPairInConflict.put(shipsPair.getKey(), conflictShips);
            }
            for (Map.Entry<Integer, ConflictShips> pair : CrossAreaChartDraw.trialShipsPairInConflict.entrySet()) {
                for (Map.Entry<Integer, ShipAis> shipMan : CrossAreaChartDraw.shipsManoeuvered.entrySet()) {
                    if (pair.getValue().shipA.mmsiNum.equals(shipMan.getKey())) {
                        pair.getValue().shipA = shipMan.getValue();
                        pair.getValue().shipA.positionFuture = shipMan.getValue().positionFuture;
                    }
                    if (pair.getValue().shipB.mmsiNum.equals(shipMan.getKey()) &&
                            pair.getValue().shipB != shipMan.getValue()) {
                        pair.getValue().shipB = shipMan.getValue();
                        pair.getValue().shipB.positionFuture = shipMan.getValue().positionFuture;
                    }
                }
            }
        }
    }

    public static ConflictShips getSafeConflict(int key, ShipAis shipA, ShipAis shipB, String manouever, double cpa) {
        double cpaDistanceNm = 0;
        double cpaTimeMin = 0;
        DirectPosition position1Future = null;
        DirectPosition position2Future = null;
        DirectPosition cpaCenterPsn = null;
        int flag = 0;
        while (cpaDistanceNm < cpa) {
            ShipState shipStateA = new ShipState(shipA.getMmsi(),
                    new Point2D.Double(shipA.latitude, shipA.longitude), shipA.hdg, shipA.speed); //8, 54 // 8.16, 54
            ShipState shipStateB = new ShipState(shipB.getMmsi(),
                    new Point2D.Double(shipB.latitude, shipB.longitude), shipB.hdg, shipB.speed); //8.16, 53.9 | 0.45507,-0.29586

            Point2D position1 = shipStateA.getPoint();
            geoCalc.setStartGeographicPoint(position1.getX(), position1.getY());

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
            cpaTimeMin = cpaTime / 60;

            /** calc CPA-P1 **/
            geoCalc.setStartGeographicPoint(position1.getX(), position1.getY());
            if (Math.signum(cpaTime) == -1) {
                geoCalc.setStartingAzimuth((shipStateA.getHeading_current_deg() + 180) % 360);
            } else {
                geoCalc.setStartingAzimuth(shipStateA.getHeading_current_deg());
            }
            geoCalc.setGeodesicDistance(speed_kn1 * 1.852 / 3.6 * Math.abs(cpaTime));
            position1Future = geoCalc.getEndPoint();

            /** calc CPA-P2 **/
            geoCalc.setStartGeographicPoint(position2.getX(), position2.getY());
            if (Math.signum(cpaTime) == -1) {
                geoCalc.setStartingAzimuth((shipStateB.getHeading_current_deg() + 180) % 360);
            } else {
                geoCalc.setStartingAzimuth(shipStateB.getHeading_current_deg());
            }
            geoCalc.setGeodesicDistance(speed_kn2 * 1.852 / 3.6 * Math.abs(cpaTime));
            position2Future = geoCalc.getEndPoint();

            /** calc distance between CPA-P1 & CPA-P2 **/
            geoCalc.setStartPoint(position1Future);
            geoCalc.setEndPoint(position2Future);
            double cpaDistance = geoCalc.getGeodesicDistance();
            double startingAzimuth = geoCalc.getStartingAzimuth();
            cpaDistanceNm = cpaDistance / 1852;
            geoCalc.setStartGeographicPoint(position1Future.getCoordinate()[0], position1Future.getCoordinate()[1]);
            geoCalc.setStartingAzimuth(startingAzimuth);
            geoCalc.setGeodesicDistance(cpaDistance / 2);
            cpaCenterPsn = geoCalc.getEndPoint();

            if (cpaDistanceNm > cpa) {
                break;
            }

            if (manouever.equals("HDG")) {
                shipB.hdg++;
            }
            if (manouever.equals("SPD")) {
                shipB.speed -= 0.5;
            }
            if (manouever.equals("MIX")) {
                shipB.hdg++;
                shipB.speed -= 0.5;
            }
            flag++;
        }

   /*     shipA.positionFuture = position1Future;
        shipB.positionFuture = position2Future;
        CrossAreaChart.shipsConflictsInCrossAreaSouth.get(key).position1Future = position1Future;
        CrossAreaChart.shipsConflictsInCrossAreaSouth.get(key).position2Future = position2Future;
        CrossAreaChart.shipsConflictsInCrossAreaSouth.get(key).cpaValue = cpaDistanceNm;*/
        ConflictShips conflictShips = new ConflictShips(cpaDistanceNm, cpaTimeMin,
                cpaCenterPsn, new ShipAis(shipA.mmsiNum, shipA.latitude, shipA.longitude, shipA.hdg, shipA.speed, shipA.destination),
                new ShipAis(shipB.mmsiNum, shipB.latitude, shipB.longitude, shipB.hdg, shipB.speed, shipB.destination),
                position1Future, position2Future);
        if (flag > 2) {
            conflictShips.draw = true;
        }
        conflictShips.shipA.positionFuture = position1Future;
        conflictShips.shipB.positionFuture = position2Future;
        CrossAreaChartDraw.trialShipsPairInConflict.put(key, conflictShips);
        if (conflictShips.draw) {
            CrossAreaChartDraw.shipsManoeuvered.put(conflictShips.shipB.mmsiNum, conflictShips.shipB);
        }
        return conflictShips;
    }
}
