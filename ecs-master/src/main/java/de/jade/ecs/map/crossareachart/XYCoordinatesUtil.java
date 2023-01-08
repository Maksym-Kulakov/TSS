package de.jade.ecs.map.crossareachart;

import de.jade.ecs.map.ApplicationCPA;
import de.jade.ecs.map.ShipAis;
import org.opengis.geometry.DirectPosition;

public class XYCoordinatesUtil {
    public static double[] getXYCoordinates(DirectPosition directPosition) {
        double horizontalValue;
        double verticalValue;
        double lat_cpaLocation = directPosition.getCoordinate()[0];
        double long_cpaLocation = directPosition.getCoordinate()[1];
        ApplicationCPA.geoCalc.setStartGeographicPoint(CrossAreaChartDraw.centerPoint.x,
                CrossAreaChartDraw.centerPoint.y);
        ApplicationCPA.geoCalc.setEndGeographicPoint(lat_cpaLocation, long_cpaLocation);
        double startingAzimuth = ApplicationCPA.geoCalc.getStartingAzimuth();
        if (startingAzimuth < 0) {
            startingAzimuth = 360 + startingAzimuth;
        }
        double correctedAzimuth = startingAzimuth + 14.0;
        double angle = correctedAzimuth % 90;
        double geodesicDistance = ApplicationCPA.geoCalc.getGeodesicDistance();
        if (correctedAzimuth > 0 && correctedAzimuth <= 90
                || correctedAzimuth > 180 && correctedAzimuth <= 270) {
            horizontalValue = geodesicDistance / 1852 * Math.abs(Math.sin(angle * Math.PI / 180));
            if (correctedAzimuth > 180) {
                horizontalValue = 0 - horizontalValue;
            }
            verticalValue = geodesicDistance / 1852 * Math.abs(Math.cos(angle * Math.PI / 180));
            if (correctedAzimuth > 90 && correctedAzimuth < 270) {
                verticalValue = 0 - verticalValue;
            }
        } else {
            horizontalValue = geodesicDistance / 1852 * Math.abs(Math.cos(angle * Math.PI / 180));
            if (correctedAzimuth > 180) {
                horizontalValue = 0 - horizontalValue;
            }
            verticalValue = geodesicDistance / 1852 * Math.abs(Math.sin(angle * Math.PI / 180));
            if (correctedAzimuth > 90 && correctedAzimuth < 270) {
                verticalValue = 0 - verticalValue;
            }
        }
        return new double[]{horizontalValue, verticalValue};
    }

    public static double[] getProlongedPointCoordinates(double xEnd, double yEnd,
                                                  ShipAis shipAis, double hdg, double additionalDist) {
        double xValueProlong = 0;
        double yValueProlong = 0;
        hdg =  hdg + 14;
        double angle = 0;
        if (CrossAreaChartDraw.shipsToEast.containsKey(shipAis.mmsiNum)) {
            if (hdg == 90) {
                yValueProlong = yEnd;
                xValueProlong = xEnd + additionalDist;
            } else if (hdg < 90) {
                angle = hdg;
                double correctionX = additionalDist * Math.sin(Math.toRadians(angle));
                double correctionY = additionalDist * Math.cos(Math.toRadians(angle));
                xValueProlong = xEnd + correctionX;
                yValueProlong = yEnd + correctionY;
            } else if (hdg > 90) {
                angle = 180 - hdg;
                double correctionX = additionalDist * Math.sin(Math.toRadians(angle));
                double correctionY = additionalDist * Math.cos(Math.toRadians(angle));
                xValueProlong = xEnd + correctionX;
                yValueProlong = yEnd - correctionY;
            }
        } else if (CrossAreaChartDraw.shipsToSouth.containsKey(shipAis.mmsiNum)) {
            if (hdg == 180) {
                yValueProlong = yEnd - additionalDist;
                xValueProlong = xEnd;
            } else if (hdg < 180) {
                angle = hdg - 90;
                double correctionX = additionalDist * Math.cos(Math.toRadians(angle));
                double correctionY = additionalDist * Math.sin(Math.toRadians(angle));
                xValueProlong = xEnd + correctionX;
                yValueProlong = yEnd - correctionY;
            } else if (hdg > 180) {
                angle = 270 - hdg;
                double correctionX = additionalDist * Math.cos(Math.toRadians(angle));
                double correctionY = additionalDist * Math.sin(Math.toRadians(angle));
                xValueProlong = xEnd - correctionX;
                yValueProlong = yEnd - correctionY;
            }
        } else if (CrossAreaChartDraw.shipsToNorth.containsKey(shipAis.mmsiNum)) {
            yValueProlong = -1.6;
            if (hdg == 0) {
                yValueProlong = yEnd + additionalDist;
                xValueProlong = xEnd;
            } else if (hdg > 0 && hdg < 90) {
                angle = 90 - hdg;
                double correctionX = additionalDist * Math.cos(Math.toRadians(angle));
                double correctionY = additionalDist * Math.sin(Math.toRadians(angle));
                xValueProlong = xEnd + correctionX;
                yValueProlong = yEnd + correctionY;
            } else if (hdg < 360 && hdg > 270) {
                angle = hdg - 270;
                double correctionX = additionalDist * Math.cos(Math.toRadians(angle));
                double correctionY = additionalDist * Math.sin(Math.toRadians(angle));
                xValueProlong = xEnd - correctionX;
                yValueProlong = yEnd + correctionY;
            }
        }
        return new double[]{xValueProlong, yValueProlong};
    }

    public static double[] getXYCoordinatesEnds(double xCpaLocation, double yCpaLocation,
                                          ShipAis shipAis, double hdg) {
        double xValueEnd = 0;
        double yValueEnd = 0;
        hdg = hdg + 14;
        double angle = 0;

        if (CrossAreaChartDraw.shipsToEast.containsKey(shipAis.mmsiNum)) {
            xValueEnd = -2.6;
            if (hdg == 90) {
                yValueEnd = yCpaLocation;
            } else if (hdg < 90) {
                angle = hdg;
                double correction = (2.6 + xCpaLocation) / Math.abs(Math.tan(angle * Math.PI / 180));
                yValueEnd = yCpaLocation - correction;
            } else if (hdg > 90) {
                angle = 180 - hdg;
                double correction = (2.6 + xCpaLocation) / Math.abs(Math.tan(angle * Math.PI / 180));
                yValueEnd = yCpaLocation + correction;
            }
        } else if (CrossAreaChartDraw.shipsToSouth.containsKey(shipAis.mmsiNum)) {
            yValueEnd = 1.6;
            if (hdg == 180) {
                xValueEnd = xCpaLocation;
            } else if (hdg < 180) {
                angle = hdg - 90;
                double correction = (1.6 - yCpaLocation) / Math.abs(Math.tan(angle * Math.PI / 180));
                xValueEnd = xCpaLocation - correction;
            } else if (hdg > 180) {
                angle = 270 - hdg;
                double correction = (1.6 - yCpaLocation) / Math.abs(Math.tan(angle * Math.PI / 180));
                xValueEnd = xCpaLocation + correction;
            }
        } else if (CrossAreaChartDraw.shipsToNorth.containsKey(shipAis.mmsiNum)) {
            yValueEnd = -1.6;
            if (hdg == 0) {
                xValueEnd = xCpaLocation;
            } else if (hdg > 0 && hdg < 90) {
                angle = 90 - hdg;
                double correction = (1.6 - yCpaLocation) / Math.abs(Math.tan(angle * Math.PI / 180));
                xValueEnd = xCpaLocation - correction;
            } else if (hdg < 360 && hdg > 270) {
                angle = hdg - 270;
                double correction = Math.abs(-1.6 - yCpaLocation) / Math.abs(Math.tan(angle * Math.PI / 180));
                xValueEnd = xCpaLocation + correction;
            }
        }
        return new double[]{xValueEnd, yValueEnd};
    }
}
