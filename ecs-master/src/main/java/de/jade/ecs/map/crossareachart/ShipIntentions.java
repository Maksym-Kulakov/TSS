package de.jade.ecs.map.crossareachart;

import de.jade.ecs.map.ConflictShips;
import de.jade.ecs.map.ShipAis;
import de.jade.ecs.map.geochart.AlterationsOfCourse;
import de.jade.ecs.map.geochart.DestinationsToEast;
import de.jade.ecs.map.geochart.DestinationsToSouth;
import de.jade.ecs.map.geochart.GeoTssAreas;
import org.jfree.chart.annotations.XYLineAnnotation;
import java.awt.*;
import java.util.Map;

public class ShipIntentions {
    public static AlterationsOfCourse findIntentionOfCourseAlteration(ShipAis shipAis) {
        if (shipAis.geoTssArea == GeoTssAreas.TO_EAST) {
            for (DestinationsToSouth port : DestinationsToSouth.values()) {
                if (port.name().equals(shipAis.destination)) {
                    return AlterationsOfCourse.STARBOARD;
                }
            }
        }
        if (shipAis.geoTssArea == GeoTssAreas.TO_SOUTH) {
            for (DestinationsToEast port : DestinationsToEast.values()) {
                if (port.name().equals(shipAis.destination)) {
                    return AlterationsOfCourse.PORT;
                }
            }
        }
        return null;
    }

    public static void assignedDirectionShipA(Map.Entry<Integer, ConflictShips> shipsPair) {
        if (!CrossAreaChartDraw.shipsToSouth.containsKey(shipsPair.getValue().shipA.mmsiNum)
                && !CrossAreaChartDraw.shipsToNorth.containsKey(shipsPair.getValue().shipA.mmsiNum)
                && !CrossAreaChartDraw.shipsToEast.containsKey(shipsPair.getValue().shipA.mmsiNum)) {
            switch (shipsPair.getValue().shipA.geoTssArea) {
                case TO_SOUTH:
                    CrossAreaChartDraw.shipsToSouth.put(shipsPair.getValue().shipA.mmsiNum,
                            ShipIntentions.findIntentionOfCourseAlteration(shipsPair.getValue().shipA));
                    break;
                case TO_NORTH:
                    CrossAreaChartDraw.shipsToNorth.put(shipsPair.getValue().shipA.mmsiNum,
                            ShipIntentions.findIntentionOfCourseAlteration(shipsPair.getValue().shipA));
                    break;
                case TO_EAST:
                    CrossAreaChartDraw.shipsToEast.put(shipsPair.getValue().shipA.mmsiNum,
                            ShipIntentions.findIntentionOfCourseAlteration(shipsPair.getValue().shipA));
                    break;
            }
        }
    }

    public static void assignedDirectionShipB(Map.Entry<Integer, ConflictShips> shipsPair) {
        if (!CrossAreaChartDraw.shipsToSouth.containsKey(shipsPair.getValue().shipB.mmsiNum)
                && !CrossAreaChartDraw.shipsToNorth.containsKey(shipsPair.getValue().shipB.mmsiNum)
                && !CrossAreaChartDraw.shipsToEast.containsKey(shipsPair.getValue().shipB.mmsiNum)) {
            switch (shipsPair.getValue().shipB.geoTssArea) {
                case TO_SOUTH:
                    CrossAreaChartDraw.shipsToSouth.put(shipsPair.getValue().shipB.mmsiNum,
                            ShipIntentions.findIntentionOfCourseAlteration(shipsPair.getValue().shipB));
                    break;
                case TO_NORTH:
                    CrossAreaChartDraw.shipsToNorth.put(shipsPair.getValue().shipB.mmsiNum,
                            ShipIntentions.findIntentionOfCourseAlteration(shipsPair.getValue().shipB));
                    break;
                case TO_EAST:
                    CrossAreaChartDraw.shipsToEast.put(shipsPair.getValue().shipB.mmsiNum,
                            ShipIntentions.findIntentionOfCourseAlteration(shipsPair.getValue().shipB));
                    break;
            }
        }
    }

    public static XYLineAnnotation[] getTurnLine(Map.Entry<Integer, ConflictShips> shipsPair,
                                               double xValueLine1, double yValueLine1,
                                               double xValueLine2, double yValueLine2,
                                               double[] xyCoordinatesEnds1, double[] xyCoordinatesEnds2) {
        XYLineAnnotation xyLineAnnotationTurnA = null;
        XYLineAnnotation xyLineAnnotationTurnB = null;
        if (CrossAreaChartDraw.shipsToSouth.containsKey(shipsPair.getValue().shipA.mmsiNum)
                && CrossAreaChartDraw.shipsToSouth.get(shipsPair.getValue().shipA.mmsiNum)
                == AlterationsOfCourse.PORT) {
            xyLineAnnotationTurnA = new XYLineAnnotation(xyCoordinatesEnds1[0] + 0.01, xyCoordinatesEnds1[1], xValueLine1 + 0.01, yValueLine1, new BasicStroke(1f), Color.red);
        }
        if (CrossAreaChartDraw.shipsToEast.containsKey(shipsPair.getValue().shipA.mmsiNum) && CrossAreaChartDraw.shipsToEast.get(shipsPair.getValue().shipA.mmsiNum)
                == AlterationsOfCourse.STARBOARD) {
            xyLineAnnotationTurnA = new XYLineAnnotation(xyCoordinatesEnds1[0], xyCoordinatesEnds1[1] - 0.01, xValueLine1, yValueLine1 - 0.01, new BasicStroke(1f), Color.green);
        }
        if (CrossAreaChartDraw.shipsToSouth.containsKey(shipsPair.getValue().shipB.mmsiNum) && CrossAreaChartDraw.shipsToSouth.get(shipsPair.getValue().shipB.mmsiNum)
                == AlterationsOfCourse.PORT) {
            xyLineAnnotationTurnB = new XYLineAnnotation(xyCoordinatesEnds2[0] + 0.01, xyCoordinatesEnds2[1], xValueLine2 + 0.01, yValueLine2, new BasicStroke(1f), Color.red);
        }
        if (CrossAreaChartDraw.shipsToEast.containsKey(shipsPair.getValue().shipB.mmsiNum) && CrossAreaChartDraw.shipsToEast.get(shipsPair.getValue().shipB.mmsiNum)
                == AlterationsOfCourse.STARBOARD) {
            xyLineAnnotationTurnB = new XYLineAnnotation(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1] - 0.01, xValueLine2, yValueLine2 - 0.01, new BasicStroke(1f), Color.green);
        }
        return new XYLineAnnotation[] {xyLineAnnotationTurnA, xyLineAnnotationTurnB};
    }
}
