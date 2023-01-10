package de.jade.ecs.map.crossareachart;

import de.jade.ecs.map.ConflictShips;
import de.jade.ecs.map.ShipAis;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HighlighterUtil {
    public static List<XYAnnotation> highLightPair() {
        List<XYAnnotation> xyAnnotationList = new ArrayList<>();
        for (Point2D.Double directPosition : CrossAreaChartDraw.directPositions) {
            Ellipse2D.Double cpaLocationShapeEnd1
                    = new Ellipse2D.Double(directPosition.getX() - 0.02, directPosition.getY() - 0.02, 0.04, 0.04);
            BasicStroke basicStroke1
                    = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
            XYShapeAnnotation xyPairAnnotation = new XYShapeAnnotation(cpaLocationShapeEnd1, basicStroke1, Color.BLACK, Color.ORANGE);
            xyAnnotationList.add(xyPairAnnotation);
        }
        return xyAnnotationList;
    }

    public static List<XYAnnotation> highLightManoeuver() {
        List<XYAnnotation> xyAnnotationList = new ArrayList<>();
        for (Point2D.Double directPosition : CrossAreaChartDraw.maneouveredPositions) {
            Ellipse2D.Double cpaLocationShapeEnd1
                    = new Ellipse2D.Double(directPosition.getX() - 0.02, directPosition.getY() - 0.02, 0.04, 0.04);
            BasicStroke basicStroke1
                    = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 12.0f);
            XYShapeAnnotation xyPairAnnotation = new XYShapeAnnotation(cpaLocationShapeEnd1, basicStroke1, Color.BLACK, Color.RED);
            xyAnnotationList.add(xyPairAnnotation);
        }
        return xyAnnotationList;
    }

    public static void madeTextAnnotationOfTcpaValue(double xValue, double yValue, double tcpaValue) {
        CrossAreaChartDraw.textAnnotation = new XYTextAnnotation(String.valueOf(Math.round(tcpaValue)),
                xValue + 0.24, yValue + 0.02);
        if (tcpaValue < 6) {
            CrossAreaChartDraw.textAnnotation.setPaint(Color.red);
        } else {
            CrossAreaChartDraw.textAnnotation.setPaint(Color.black);
        }
        CrossAreaChartDraw.textAnnotation.setFont(new Font("Tahoma", Font.BOLD, 10));
    }

    public static boolean isShipBowCrosser(Point2D.Double crossPoint,
                                           Map.Entry<Integer, ConflictShips> shipsPair,
                                           double xValueLine1, double yValueLine1) {
        if (CrossAreaChartDraw.shipsToEast.containsKey(shipsPair.getValue().shipA.mmsiNum)
                && crossPoint.getX() < xValueLine1) {
            return true;
        } else if (CrossAreaChartDraw.shipsToSouth.containsKey(shipsPair.getValue().shipA.mmsiNum)
                && crossPoint.getY() > yValueLine1) {
            return true;
        } else return CrossAreaChartDraw.shipsToNorth.containsKey(shipsPair.getValue().shipA.mmsiNum)
                && crossPoint.getY() < yValueLine1;
    }

    public static boolean isPeriodWithShipPsnBetweenBcrAndFuturePsn(Point2D.Double crossPoint,
                                                                  ShipAis shipAis,
                                                                  double[] xyCoordinatesInCross1) {
        if (CrossAreaChartDraw.shipsToEast.containsKey(shipAis.mmsiNum)
                && xyCoordinatesInCross1[0] > crossPoint.getX()) {
            return true;
        } else if (CrossAreaChartDraw.shipsToSouth.containsKey(shipAis.mmsiNum)
                && xyCoordinatesInCross1[1] < crossPoint.getY()) {
            return true;
        } else return CrossAreaChartDraw.shipsToNorth.containsKey(shipAis.mmsiNum)
                && xyCoordinatesInCross1[1] > crossPoint.getY();
    }
}
