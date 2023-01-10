package de.jade.ecs.map.crossareachart;

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
    public static GeodeticCalculator geoCalc
            = GeodeticCalculator.create(CommonCRS.WGS84.geographic());

    public static void getSafeCpa(String manouever, double cpa) throws CloneNotSupportedException {
        //cloning map of ships to new - trialShipsPairInConflict
        for (Map.Entry<Integer, ConflictShips> shipsPair
                : CrossAreaChartDraw.shipsPairInConflict.entrySet()) {
                CrossAreaChartDraw.trialShipsPairInConflict.put(shipsPair.getKey(),
                        shipsPair.getValue().clone());
        }

        for (Map.Entry<Integer, ConflictShips> shipsPair : CrossAreaChartDraw.trialShipsPairInConflict.entrySet()) {
            if (shipsPair.getValue().cpaValue < cpa) {
                getSafeConflictShips(shipsPair, manouever, cpa);
            }
        }
    }

    private static void getSafeConflictShips(Map.Entry<Integer, ConflictShips> shipsPair,
                                             String manouever, double cpa) throws CloneNotSupportedException {
        //determine which ship should give way
        ShipIntentions.performShipsPriority();
        ShipAis shipStandOn;
        ShipAis shipGiveWay;
        if (shipsPair.getValue().shipAGiveWay) {
            shipGiveWay = shipsPair.getValue().shipA.clone();
            shipStandOn = shipsPair.getValue().shipB;
        } else if (shipsPair.getValue().shipBGiveWay) {
            shipGiveWay = shipsPair.getValue().shipB.clone();
            shipStandOn = shipsPair.getValue().shipA;
        } else {
            return;
        }

        while (shipsPair.getValue().cpaValue < cpa) {
            ShipState shipStateA = new ShipState(shipStandOn.getMmsi(),
                    new Point2D.Double(shipStandOn.latitude, shipStandOn.longitude),
                    shipStandOn.hdg, shipStandOn.speed);
            ShipState shipStateB = new ShipState(shipGiveWay.getMmsi(),
                    new Point2D.Double(shipGiveWay.latitude, shipGiveWay.longitude),
                    shipGiveWay.hdg, shipGiveWay.speed);

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
            shipsPair.getValue().tcpaValue = cpaTime / 60;

            /** calc CPA-P1 **/
            geoCalc.setStartGeographicPoint(position1.getX(), position1.getY());
            if (Math.signum(cpaTime) == -1) {
                geoCalc.setStartingAzimuth((shipStateA.getHeading_current_deg() + 180) % 360);
            } else {
                geoCalc.setStartingAzimuth(shipStateA.getHeading_current_deg());
            }
            geoCalc.setGeodesicDistance(speed_kn1 * 1.852 / 3.6 * Math.abs(cpaTime));
            DirectPosition position1F = geoCalc.getEndPoint();

            /** calc CPA-P2 **/
            geoCalc.setStartGeographicPoint(position2.getX(), position2.getY());
            if (Math.signum(cpaTime) == -1) {
                geoCalc.setStartingAzimuth((shipStateB.getHeading_current_deg() + 180) % 360);
            } else {
                geoCalc.setStartingAzimuth(shipStateB.getHeading_current_deg());
            }
            geoCalc.setGeodesicDistance(speed_kn2 * 1.852 / 3.6 * Math.abs(cpaTime));
            DirectPosition position2F = geoCalc.getEndPoint();

            /** calc distance between CPA-P1 & CPA-P2 **/
            geoCalc.setStartPoint(position1F);
            geoCalc.setEndPoint(position2F);
            double cpaDistance = geoCalc.getGeodesicDistance();
            double startingAzimuth = geoCalc.getStartingAzimuth();
            shipsPair.getValue().cpaValue = cpaDistance / 1852;
            geoCalc.setStartGeographicPoint(position1F.getCoordinate()[0],
                    position1F.getCoordinate()[1]);
            geoCalc.setStartingAzimuth(startingAzimuth);
            geoCalc.setGeodesicDistance(cpaDistance / 2);
            shipsPair.getValue().cpaLocation = geoCalc.getEndPoint();

            if (shipsPair.getValue().shipAGiveWay) {
                shipsPair.getValue().position2Future = position1F;
                shipsPair.getValue().position1Future = position2F;
            }
            if (shipsPair.getValue().shipBGiveWay) {
                shipsPair.getValue().position2Future = position2F;
                shipsPair.getValue().position1Future = position1F;
            }

            if (shipsPair.getValue().cpaValue > cpa) {
                break;
            }

            if (manouever.equals("HDG")) {
                shipGiveWay.hdg++;
            }
            if (manouever.equals("SPD")) {
                shipGiveWay.speed -= 0.5;
            }
            if (manouever.equals("MIX")) {
                shipGiveWay.hdg++;
                shipGiveWay.speed -= 0.5;
            }
            shipsPair.getValue().draw = true;
        }

        if (shipsPair.getValue().shipAGiveWay) {
            shipsPair.getValue().shipA = shipGiveWay;
        }
        if (shipsPair.getValue().shipBGiveWay) {
            shipsPair.getValue().shipB = shipGiveWay;
        }

        if (shipsPair.getValue().draw) {
            CrossAreaChartDraw.shipsManoeuvered.put(shipsPair.getValue().shipB.mmsiNum,
                    shipsPair.getValue().shipB);
        }
    }
}
