package de.jade.ecs.map;

import de.jade.ecs.map.geochart.AlterationsOfCourse;
import de.jade.ecs.map.geochart.DestinationsToEast;
import de.jade.ecs.map.geochart.DestinationsToSouth;
import de.jade.ecs.map.geochart.GeoTssAreas;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.opengis.geometry.DirectPosition;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CrossAreaChartDraw extends ApplicationFrame implements Runnable {
    public static Point2D.Double centerPoint = new Point2D.Double(53.93652, 7.696533);
    private static final String title = "South Cross Area";
    private XYSeries chartSouth = new XYSeries("Conflict Ships");
    private static List<ConflictShips> shipsPairInConflict = new ArrayList<>();
    XYPlot xyPlot;
    XYTextAnnotation textAnnotation;

    public CrossAreaChartDraw(String s) {
        super(s);
        final ChartPanel chartPanel = createChartPanel();
        this.add(chartPanel, BorderLayout.CENTER);

        JPanel control = new JPanel();

        SpinnerModel spinnerModelCpa = new SpinnerNumberModel(0.5, 0.1, 10, 0.1);
        JSpinner jSpinnerCpa = new JSpinner(spinnerModelCpa);

        SpinnerModel spinnerModelTcpa = new SpinnerNumberModel(10, 0.1, 30, 1);
        JSpinner jSpinnerTcpa = new JSpinner(spinnerModelTcpa);

        control.add(new JButton(new AbstractAction("in BWCR psn") {
            @Override
            public void actionPerformed(ActionEvent e) {
                chartSouth.clear();
                xyPlot.clearAnnotations();

                Double valueFilterCpa = Double.valueOf(jSpinnerCpa.getValue().toString());
                Double valueFilterTcpa = Double.valueOf(jSpinnerTcpa.getValue().toString());

                updateBCR(valueFilterCpa, valueFilterTcpa);
            }
        }));

        control.add(new JButton(new AbstractAction("in CPA psn") {
            @Override
            public void actionPerformed(ActionEvent e) {
                chartSouth.clear();
                xyPlot.clearAnnotations();

                Double valueFilterCpa = Double.valueOf(jSpinnerCpa.getValue().toString());
                Double valueFilterTcpa = Double.valueOf(jSpinnerTcpa.getValue().toString());

                update(valueFilterCpa, valueFilterTcpa);
            }
        }));

        JTextArea jTextAreaCpa = new JTextArea("cpa <");
        control.add(jTextAreaCpa);

        control.add(jSpinnerCpa, BorderLayout.SOUTH);

        JTextArea jTextAreaNm= new JTextArea("nm");
        control.add(jTextAreaNm);

        JTextArea jTextAreaTcpa = new JTextArea("tcpa <");
        control.add(jTextAreaTcpa);

        control.add(jSpinnerTcpa, BorderLayout.SOUTH);

        JTextArea jTextAreaMin= new JTextArea("min");
        control.add(jTextAreaMin);

        this.add(control, BorderLayout.SOUTH);
    }


    private ChartPanel createChartPanel() {
        JFreeChart jfreechart = ChartFactory.createScatterPlot(
                title, "X", "Y", createChartData(),
                PlotOrientation.VERTICAL, true, true, false);
        xyPlot = (XYPlot) jfreechart.getPlot();
        xyPlot.setRenderer(new XYLineAndShapeRenderer(false, true) {
            @Override
            public Shape getItemShape(int row, int col) {
                if (!shipsPairInConflict.isEmpty()) {
                    double cpaValue = shipsPairInConflict.get(shipsPairInConflict.size() - col - 1).cpaValue * 115;

                    Ellipse2D.Double shapeMark
                            = new Ellipse2D.Double(-(float) 0.25 * 115 / 2, -(float) 0.25 * 115 / 2,
                            (float) 0.25 * 115, (float) 0.25 * 115);

                    return shapeMark;
                } else {
                    return super.getItemShape(row, col);
                }
            }
        });
        XYLineAndShapeRenderer renderer
                = (XYLineAndShapeRenderer) jfreechart.getXYPlot().getRenderer();
        renderer.setUseFillPaint(true);

        NumberAxis domain = (NumberAxis) xyPlot.getDomainAxis();
        domain.setRange(-2.6, 2.6);
        domain.setTickUnit(new NumberTickUnit(1));
        NumberAxis range = (NumberAxis) xyPlot.getRangeAxis();
        range.setRange(-1.6, 1.6);
        range.setTickUnit(new NumberTickUnit(1));
        return new ChartPanel(jfreechart) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(640, 480);
            }
        };
    }

    private XYDataset createChartData() {
        XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
        xySeriesCollection.addSeries(chartSouth);
        return xySeriesCollection;
    }

    private Shape createCircle(double size) {
        return new Ellipse2D.Double(-size / 2, -size / 2, size, size);
    }

    private double[] getXYCoordinates(DirectPosition directPosition) {
        double horizontalValue;
        double verticalValue;
        double lat_cpaLocation = directPosition.getCoordinate()[0];
        double long_cpaLocation = directPosition.getCoordinate()[1];
        ApplicationCPA.geoCalc.setStartGeographicPoint(centerPoint.x, centerPoint.y);
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

    private double[] getProlongedPointCoordinates(double xEnd, double yEnd,
                                                  ShipAis shipAis, double hdg, double additionalDist) {
        double xValueProlong = 0;
        double yValueProlong = 0;
        hdg =  hdg + 14;
        double angle = 0;
        if (shipAis.geoTssArea == GeoTssAreas.TO_EAST) {
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
        } else if (shipAis.geoTssArea == GeoTssAreas.TO_SOUTH) {
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
        } else if (shipAis.geoTssArea == GeoTssAreas.TO_NORTH) {
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

    private double[] getXYCoordinatesEnds(double xCpaLocation, double yCpaLocation,
                                          ShipAis shipAis, double hdg) {
        double xValueEnd = 0;
        double yValueEnd = 0;
        hdg = hdg + 14;
        double angle = 0;

        if (shipAis.geoTssArea == GeoTssAreas.TO_EAST) {
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
        } else if (shipAis.geoTssArea == GeoTssAreas.TO_SOUTH) {
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
        } else if (shipAis.geoTssArea == GeoTssAreas.TO_NORTH) {
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

    private void update(Double valueCpaLimit, Double valueTCpaLimit) {
        double xValue = 0;
        double yValue = 0;
        Set<DirectPosition> directPositions = new HashSet<>();
        for (ConflictShips shipsPair : CrossAreaChart.shipsConflictsInCrossAreaSouth.values()) {
            //conflicts indication filtering by cpa value
            if (shipsPair.cpaValue < valueCpaLimit && shipsPair.tcpaValue < valueTCpaLimit) {
                shipsPairInConflict.add(shipsPair);

                double[] xyCoordinates = getXYCoordinates(shipsPair.cpaLocation);
                xValue = xyCoordinates[0];
                yValue = xyCoordinates[1];

                //cpa value in circle
                System.out.println("shipsConflicts updated");
                textAnnotation = new XYTextAnnotation(String.valueOf(Math.round(shipsPair.tcpaValue)),
                        xValue + 0.24, yValue + 0.02);
                if (shipsPair.tcpaValue < 6) {
                    textAnnotation.setPaint(Color.red);
                } else {
                    textAnnotation.setPaint(Color.black);
                }
                textAnnotation.setFont(new Font("Tahoma", Font.BOLD, 10));


                //lines of ships` paths
                double[] xyCoordinatesLine1 = getXYCoordinates(shipsPair.position1Future);
                double xValueLine1 = xyCoordinatesLine1[0];
                double yValueLine1 = xyCoordinatesLine1[1];
                directPositions.add(shipsPair.position1Future);

                double[] xyCoordinatesLine2 = getXYCoordinates(shipsPair.position2Future);
                double xValueLine2 = xyCoordinatesLine2[0];
                double yValueLine2 = xyCoordinatesLine2[1];
                directPositions.add(shipsPair.position2Future);

                double[] xyCoordinatesEnds1
                        = getXYCoordinatesEnds(xValueLine1, yValueLine1, shipsPair.shipA, shipsPair.shipA.hdg);
                double[] xyCoordinatesEnds2
                        = getXYCoordinatesEnds(xValueLine2, yValueLine2, shipsPair.shipB, shipsPair.shipB.hdg);

                //to cacl coord of prolonged point
                int distanceOfProlong = 4;

                double[] xyCoordinatesProlonged1 = getProlongedPointCoordinates(xyCoordinatesEnds1[0], xyCoordinatesEnds1[1],
                        shipsPair.shipA, shipsPair.shipA.hdg, distanceOfProlong);

                double[] xyCoordinatesProlonged2 = getProlongedPointCoordinates(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1],
                        shipsPair.shipB, shipsPair.shipB.hdg, distanceOfProlong);

  /*              Ellipse2D.Double prolongPoint1
                        = new Ellipse2D.Double(xyCoordinatesProlonged1[0] - 0.015, xyCoordinatesProlonged1[1] - 0.015, 0.03, 0.03);
                BasicStroke basicStrokeProlongPoint1
                        = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);

                xyPlot.addAnnotation(new XYShapeAnnotation(prolongPoint1, basicStrokeProlongPoint1, Color.RED, Color.RED));

                Ellipse2D.Double prolongPoint2
                        = new Ellipse2D.Double(xyCoordinatesProlonged2[0] - 0.015, xyCoordinatesProlonged2[1] - 0.015, 0.03, 0.03);
                BasicStroke basicStrokeProlongPoint2
                        = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);

                xyPlot.addAnnotation(new XYShapeAnnotation(prolongPoint2, basicStrokeProlongPoint2, Color.RED, Color.RED));*/

                Line2D.Double line1 = new Line2D.Double(new Point2D.Double(xyCoordinatesEnds1[0], xyCoordinatesEnds1[1]),
                        new Point2D.Double(xyCoordinatesProlonged1[0], xyCoordinatesProlonged1[1]));
                Line2D.Double line2 = new Line2D.Double(new Point2D.Double(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1]),
                        new Point2D.Double(xyCoordinatesProlonged2[0], xyCoordinatesProlonged2[1]));

                Point2D.Double crossPoint = Intersection.intersection(line1, line2);

          /*      Point2D.Double crossPoint = Intersection.lineLineIntersection(
                        new Point2D.Double(xyCoordinatesEnds1[0], xyCoordinatesEnds1[0])
                        , new Point2D.Double(xyCoordinatesProlonged1[0], xyCoordinatesProlonged1[1]),
                        new Point2D.Double(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1]),
                        new Point2D.Double(xyCoordinatesProlonged2[0], xyCoordinatesProlonged2[1]));*/





                Ellipse2D.Double crossPointMark
                        = new Ellipse2D.Double(crossPoint.getX()- 0.015, crossPoint.getY() - 0.015, 0.03, 0.03);
                BasicStroke basicStrokeCrossPointMark
                        = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);

                xyPlot.addAnnotation(new XYShapeAnnotation(crossPointMark, basicStrokeCrossPointMark, Color.YELLOW, Color.YELLOW));


/*                double newPointX_1 = xyCoordinatesEnds1[0] + 2 * Math.sin((shipsPair.shipA.hdg + 14) * Math.PI / 180);
                double newPointY_1 = xyCoordinatesEnds1[1] + 2 * Math.round(Math.cos(Math.toRadians(shipsPair.shipA.hdg + 14)));

                double newPointX_2 = xyCoordinatesEnds2[0] + 2 * Math.sin((shipsPair.shipB.hdg + 14) * Math.PI / 180);
                double newPointY_2 = xyCoordinatesEnds2[1] + 2 * Math.round(Math.cos(Math.toRadians(shipsPair.shipA.hdg + 14)));

                Ellipse2D.Double crossPoint1
                        = new Ellipse2D.Double(newPointX_1 - 0.15, newPointY_1 - 0.15, 0.03, 0.03);
                BasicStroke basicStrokeCrossPoint1
                        = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);

                xyPlot.addAnnotation(new XYShapeAnnotation(crossPoint1, basicStrokeCrossPoint1, Color.RED, Color.RED));*/



   /*             Line2D.Double line1 = new Line2D.Double(new Point2D.Double(xValueLine1, yValueLine1),
                        new Point2D.Double(newPointX_1, newPointY_1));
                Line2D.Double line2 = new Line2D.Double(new Point2D.Double(xValueLine1, yValueLine1),
                        new Point2D.Double(newPointX_2, newPointY_2));*/

       /*         Point2D.Double crossPointLines = Intersection.lineLineIntersection(new Point2D.Double(xValueLine1, yValueLine1), new Point2D.Double(newPointX_1, newPointY_1),
                        new Point2D.Double(xValueLine2, yValueLine2), new Point2D.Double(newPointX_2, newPointY_2));

                Ellipse2D.Double crossPoint
                        = new Ellipse2D.Double(crossPointLines.getX() - 0.15, crossPointLines.getY() - 0.15, 0.03, 0.03);
                BasicStroke basicStrokeCrossPoint
                        = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);

                xyPlot.addAnnotation(new XYShapeAnnotation(crossPoint, basicStrokeCrossPoint, Color.BLUE, Color.BLUE));*/


                XYLineAnnotation xyLineAnnotation1 = new XYLineAnnotation(xyCoordinatesEnds1[0], xyCoordinatesEnds1[1], xValueLine1, yValueLine1, new BasicStroke(2f), Color.white);
                XYLineAnnotation xyLineAnnotation2 = new XYLineAnnotation(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1], xValueLine2, yValueLine2, new BasicStroke(2f), Color.white);


                xyPlot.addAnnotation(xyLineAnnotation1);
                xyPlot.addAnnotation(xyLineAnnotation2);


                //green and red line to indicate ships port intentions
                XYLineAnnotation xyLineAnnotationTurnA = null;
                XYLineAnnotation xyLineAnnotationTurnB = null;

                if (findIntentionOfCourseAlteration(shipsPair.shipA) == AlterationsOfCourse.PORT
                        && shipsPair.shipA.geoTssArea == GeoTssAreas.TO_SOUTH) {
                    xyLineAnnotationTurnA = new XYLineAnnotation(xyCoordinatesEnds1[0] + 0.01, xyCoordinatesEnds1[1], xValueLine1 + 0.01, yValueLine1, new BasicStroke(1f), Color.red);
                }
                if (findIntentionOfCourseAlteration(shipsPair.shipA) == AlterationsOfCourse.STARBOARD
                        && shipsPair.shipA.geoTssArea == GeoTssAreas.TO_EAST) {
                    xyLineAnnotationTurnA = new XYLineAnnotation(xyCoordinatesEnds1[0], xyCoordinatesEnds1[1] - 0.01, xValueLine1, yValueLine1 - 0.01, new BasicStroke(1f), Color.green);
                }
                if (findIntentionOfCourseAlteration(shipsPair.shipB) == AlterationsOfCourse.PORT
                        && shipsPair.shipB.geoTssArea == GeoTssAreas.TO_SOUTH) {
                    xyLineAnnotationTurnB = new XYLineAnnotation(xyCoordinatesEnds2[0] + 0.01, xyCoordinatesEnds2[1], xValueLine2 + 0.01, yValueLine2, new BasicStroke(1f), Color.red);
                }
                if (findIntentionOfCourseAlteration(shipsPair.shipB) == AlterationsOfCourse.STARBOARD
                        && shipsPair.shipB.geoTssArea == GeoTssAreas.TO_EAST) {
                    xyLineAnnotationTurnB = new XYLineAnnotation(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1] - 0.01, xValueLine2, yValueLine2 - 0.01, new BasicStroke(1f), Color.green);
                }
                if (xyLineAnnotationTurnA != null) {
                    xyPlot.addAnnotation(xyLineAnnotationTurnA);
                }
                if (xyLineAnnotationTurnB != null) {
                    xyPlot.addAnnotation(xyLineAnnotationTurnB);
                }

                //circle

                xyPlot.addAnnotation(textAnnotation);

                drawTcpaSmall(xyPlot, xValue, yValue, shipsPair.tcpaValue);

            }
            for (DirectPosition directPosition : directPositions) {
                double[] xyCoordinatesLine1 = getXYCoordinates(directPosition);
                double xValueLine1 = xyCoordinatesLine1[0];
                double yValueLine1 = xyCoordinatesLine1[1];
                Ellipse2D.Double cpaLocationShapeEnd1
                        = new Ellipse2D.Double(xyCoordinatesLine1[0] - 0.015, xyCoordinatesLine1[1] - 0.015, 0.03, 0.03);
                BasicStroke basicStroke1
                        = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                xyPlot.addAnnotation(new XYShapeAnnotation(cpaLocationShapeEnd1, basicStroke1, Color.BLUE, Color.BLUE));
            }
        }


    }

    private void updateBCR(Double valueCpaLimit, Double valueTCpaLimit) {
        double xValue = 0;
        double yValue = 0;
        Set<Point2D.Double> directPositions = new HashSet<>();
        for (ConflictShips shipsPair : CrossAreaChart.shipsConflictsInCrossAreaSouth.values()) {
            //conflicts indication filtering by cpa value
            if (shipsPair.cpaValue < valueCpaLimit && shipsPair.tcpaValue < valueTCpaLimit) {
                shipsPairInConflict.add(shipsPair);

                double[] xyCoordinates = getXYCoordinates(shipsPair.cpaLocation);
                xValue = xyCoordinates[0];
                yValue = xyCoordinates[1];

                //cpa value in circle
                System.out.println("shipsConflicts updated");
                textAnnotation = new XYTextAnnotation(String.valueOf(Math.round(shipsPair.tcpaValue)),
                        xValue + 0.24, yValue + 0.02);
                if (shipsPair.tcpaValue < 6) {
                    textAnnotation.setPaint(Color.red);
                } else {
                    textAnnotation.setPaint(Color.black);
                }
                textAnnotation.setFont(new Font("Tahoma", Font.BOLD, 10));


                //lines of ships` paths
                double[] xyCoordinatesLine1 = getXYCoordinates(shipsPair.position1Future);
                double xValueLine1 = xyCoordinatesLine1[0];
                double yValueLine1 = xyCoordinatesLine1[1];
                 ;

                double[] xyCoordinatesLine2 = getXYCoordinates(shipsPair.position2Future);
                double xValueLine2 = xyCoordinatesLine2[0];
                double yValueLine2 = xyCoordinatesLine2[1];


                double[] xyCoordinatesEnds1
                        = getXYCoordinatesEnds(xValueLine1, yValueLine1, shipsPair.shipA, shipsPair.shipA.hdg);
                double[] xyCoordinatesEnds2
                        = getXYCoordinatesEnds(xValueLine2, yValueLine2, shipsPair.shipB, shipsPair.shipB.hdg);

                //to cacl coord of prolonged point
                int distanceOfProlong = 4;

                double[] xyCoordinatesProlonged1 = getProlongedPointCoordinates(xyCoordinatesEnds1[0], xyCoordinatesEnds1[1],
                        shipsPair.shipA, shipsPair.shipA.hdg, distanceOfProlong);

                double[] xyCoordinatesProlonged2 = getProlongedPointCoordinates(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1],
                        shipsPair.shipB, shipsPair.shipB.hdg, distanceOfProlong);

 /*               Ellipse2D.Double prolongPoint1
                        = new Ellipse2D.Double(xyCoordinatesProlonged1[0] - 0.015, xyCoordinatesProlonged1[1] - 0.015, 0.03, 0.03);
                BasicStroke basicStrokeProlongPoint1
                        = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);

                xyPlot.addAnnotation(new XYShapeAnnotation(prolongPoint1, basicStrokeProlongPoint1, Color.RED, Color.RED));

                Ellipse2D.Double prolongPoint2
                        = new Ellipse2D.Double(xyCoordinatesProlonged2[0] - 0.015, xyCoordinatesProlonged2[1] - 0.015, 0.03, 0.03);
                BasicStroke basicStrokeProlongPoint2
                        = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);

                xyPlot.addAnnotation(new XYShapeAnnotation(prolongPoint2, basicStrokeProlongPoint2, Color.RED, Color.RED));*/

                Line2D.Double line1 = new Line2D.Double(new Point2D.Double(xyCoordinatesEnds1[0], xyCoordinatesEnds1[1]),
                        new Point2D.Double(xyCoordinatesProlonged1[0], xyCoordinatesProlonged1[1]));
                Line2D.Double line2 = new Line2D.Double(new Point2D.Double(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1]),
                        new Point2D.Double(xyCoordinatesProlonged2[0], xyCoordinatesProlonged2[1]));


                Point2D.Double crossPoint = Intersection.intersection(line1, line2);



                Line2D.Double segment1 = new Line2D.Double(new Point2D.Double(xyCoordinatesEnds1[0], xyCoordinatesEnds1[1]),
                        new Point2D.Double(xValueLine1, yValueLine1));
                Line2D.Double segment2 = new Line2D.Double(new Point2D.Double(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1]),
                        new Point2D.Double(xyCoordinatesProlonged2[0], xyCoordinatesProlonged2[1]));




                XYLineAnnotation xyLineAnnotation1 = null;
                XYLineAnnotation xyLineAnnotation2 = null;

                boolean checkIfPointInSegment = false;
                if (shipsPair.shipA.geoTssArea == GeoTssAreas.TO_EAST
                        && crossPoint.getX() < xValueLine1) {
                    checkIfPointInSegment = true;
                } else if (shipsPair.shipA.geoTssArea == GeoTssAreas.TO_SOUTH
                        && crossPoint.getY() > yValueLine1) {
                    checkIfPointInSegment = true;
                } else if (shipsPair.shipA.geoTssArea == GeoTssAreas.TO_NORTH
                        && crossPoint.getY() < yValueLine1) {
                    checkIfPointInSegment = true;
                }

                if (checkIfPointInSegment) {
                    double distFmBcrToEnd1 = Math.sqrt((yValueLine1 - crossPoint.getY()) * (yValueLine1 - crossPoint.getY())
                        + (xValueLine1 - crossPoint.getX()) * (xValueLine1 - crossPoint.getX()));
                    double distFmBcrToEnd2 = distFmBcrToEnd1 * shipsPair.shipB.speed / shipsPair.shipA.speed;

                    double distOfLine2 = Math.sqrt((yValueLine2 - xyCoordinatesEnds2[1]) * (yValueLine2 - xyCoordinatesEnds2[1])
                            + (xValueLine2 - xyCoordinatesEnds2[0]) * (xValueLine2 - xyCoordinatesEnds2[0]));

                    double[] otherBwrPoint = getProlongedPointCoordinates(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1], shipsPair.shipB, shipsPair.shipB.hdg, distOfLine2 - distFmBcrToEnd2);

                    xyLineAnnotation1
                            = new XYLineAnnotation(xyCoordinatesEnds1[0], xyCoordinatesEnds1[1],
                            crossPoint.getX(), crossPoint.getY(), new BasicStroke(2f), Color.white);
                    xyLineAnnotation2
                            = new XYLineAnnotation(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1],
                            otherBwrPoint[0], otherBwrPoint[1], new BasicStroke(2f), Color.white);

                    directPositions.add(crossPoint);
                    directPositions.add(new Point2D.Double(otherBwrPoint[0], otherBwrPoint[1]));
                } else {
                    double distFmBcrToEnd2 = Math.sqrt((yValueLine2 - crossPoint.getY()) * (yValueLine2 - crossPoint.getY())
                            + (xValueLine2 - crossPoint.getX()) * (xValueLine2 - crossPoint.getX()));
                    double distFmBcrToEnd1 = distFmBcrToEnd2 * shipsPair.shipA.speed / shipsPair.shipB.speed;

                    double distOfLine1 = Math.sqrt((yValueLine1 - xyCoordinatesEnds1[1]) * (yValueLine1 - xyCoordinatesEnds1[1])
                            + (xValueLine1 - xyCoordinatesEnds1[0]) * (xValueLine1 - xyCoordinatesEnds1[0]));

                    double[] otherBwrPoint = getProlongedPointCoordinates(xyCoordinatesEnds1[0], xyCoordinatesEnds1[1], shipsPair.shipA, shipsPair.shipA.hdg, distOfLine1 - distFmBcrToEnd1);

                    xyLineAnnotation1
                            = new XYLineAnnotation(xyCoordinatesEnds1[0], xyCoordinatesEnds1[1],
                            otherBwrPoint[0], otherBwrPoint[1], new BasicStroke(2f), Color.white);
                    xyLineAnnotation2
                            = new XYLineAnnotation(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1],
                            crossPoint.getX(), crossPoint.getY(), new BasicStroke(2f), Color.white);

                    directPositions.add(crossPoint);
                    directPositions.add(new Point2D.Double(otherBwrPoint[0], otherBwrPoint[1]));
                }

          /*      Point2D.Double crossPoint = Intersection.lineLineIntersection(
                        new Point2D.Double(xyCoordinatesEnds1[0], xyCoordinatesEnds1[0])
                        , new Point2D.Double(xyCoordinatesProlonged1[0], xyCoordinatesProlonged1[1]),
                        new Point2D.Double(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1]),
                        new Point2D.Double(xyCoordinatesProlonged2[0], xyCoordinatesProlonged2[1]));*/





      /*          Ellipse2D.Double crossPointMark
                        = new Ellipse2D.Double(crossPoint.getX()- 0.015, crossPoint.getY() - 0.015, 0.03, 0.03);
                BasicStroke basicStrokeCrossPointMark
                        = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);

                xyPlot.addAnnotation(new XYShapeAnnotation(crossPointMark, basicStrokeCrossPointMark, Color.YELLOW, Color.YELLOW));*/


/*                double newPointX_1 = xyCoordinatesEnds1[0] + 2 * Math.sin((shipsPair.shipA.hdg + 14) * Math.PI / 180);
                double newPointY_1 = xyCoordinatesEnds1[1] + 2 * Math.round(Math.cos(Math.toRadians(shipsPair.shipA.hdg + 14)));

                double newPointX_2 = xyCoordinatesEnds2[0] + 2 * Math.sin((shipsPair.shipB.hdg + 14) * Math.PI / 180);
                double newPointY_2 = xyCoordinatesEnds2[1] + 2 * Math.round(Math.cos(Math.toRadians(shipsPair.shipA.hdg + 14)));

                Ellipse2D.Double crossPoint1
                        = new Ellipse2D.Double(newPointX_1 - 0.15, newPointY_1 - 0.15, 0.03, 0.03);
                BasicStroke basicStrokeCrossPoint1
                        = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);

                xyPlot.addAnnotation(new XYShapeAnnotation(crossPoint1, basicStrokeCrossPoint1, Color.RED, Color.RED));*/



   /*             Line2D.Double line1 = new Line2D.Double(new Point2D.Double(xValueLine1, yValueLine1),
                        new Point2D.Double(newPointX_1, newPointY_1));
                Line2D.Double line2 = new Line2D.Double(new Point2D.Double(xValueLine1, yValueLine1),
                        new Point2D.Double(newPointX_2, newPointY_2));*/

       /*         Point2D.Double crossPointLines = Intersection.lineLineIntersection(new Point2D.Double(xValueLine1, yValueLine1), new Point2D.Double(newPointX_1, newPointY_1),
                        new Point2D.Double(xValueLine2, yValueLine2), new Point2D.Double(newPointX_2, newPointY_2));

                Ellipse2D.Double crossPoint
                        = new Ellipse2D.Double(crossPointLines.getX() - 0.15, crossPointLines.getY() - 0.15, 0.03, 0.03);
                BasicStroke basicStrokeCrossPoint
                        = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);

                xyPlot.addAnnotation(new XYShapeAnnotation(crossPoint, basicStrokeCrossPoint, Color.BLUE, Color.BLUE));*/






                xyPlot.addAnnotation(xyLineAnnotation1);
                xyPlot.addAnnotation(xyLineAnnotation2);


                //green and red line to indicate ships port intentions
                XYLineAnnotation xyLineAnnotationTurnA = null;
                XYLineAnnotation xyLineAnnotationTurnB = null;

                if (findIntentionOfCourseAlteration(shipsPair.shipA) == AlterationsOfCourse.PORT
                        && shipsPair.shipA.geoTssArea == GeoTssAreas.TO_SOUTH) {
                    xyLineAnnotationTurnA = new XYLineAnnotation(xyCoordinatesEnds1[0] + 0.01, xyCoordinatesEnds1[1], xValueLine1 + 0.01, yValueLine1, new BasicStroke(1f), Color.red);
                }
                if (findIntentionOfCourseAlteration(shipsPair.shipA) == AlterationsOfCourse.STARBOARD
                        && shipsPair.shipA.geoTssArea == GeoTssAreas.TO_EAST) {
                    xyLineAnnotationTurnA = new XYLineAnnotation(xyCoordinatesEnds1[0], xyCoordinatesEnds1[1] - 0.01, xValueLine1, yValueLine1 - 0.01, new BasicStroke(1f), Color.green);
                }
                if (findIntentionOfCourseAlteration(shipsPair.shipB) == AlterationsOfCourse.PORT
                        && shipsPair.shipB.geoTssArea == GeoTssAreas.TO_SOUTH) {
                    xyLineAnnotationTurnB = new XYLineAnnotation(xyCoordinatesEnds2[0] + 0.01, xyCoordinatesEnds2[1], xValueLine2 + 0.01, yValueLine2, new BasicStroke(1f), Color.red);
                }
                if (findIntentionOfCourseAlteration(shipsPair.shipB) == AlterationsOfCourse.STARBOARD
                        && shipsPair.shipB.geoTssArea == GeoTssAreas.TO_EAST) {
                    xyLineAnnotationTurnB = new XYLineAnnotation(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1] - 0.01, xValueLine2, yValueLine2 - 0.01, new BasicStroke(1f), Color.green);
                }
                if (xyLineAnnotationTurnA != null) {
                    xyPlot.addAnnotation(xyLineAnnotationTurnA);
                }
                if (xyLineAnnotationTurnB != null) {
                    xyPlot.addAnnotation(xyLineAnnotationTurnB);
                }

                //circle

                xyPlot.addAnnotation(textAnnotation);

                drawTcpaSmall(xyPlot, xValue, yValue, shipsPair.tcpaValue);

            }
            for (Point2D.Double directPosition : directPositions) {
                Ellipse2D.Double cpaLocationShapeEnd1
                        = new Ellipse2D.Double(directPosition.getX() - 0.015, directPosition.getY() - 0.015, 0.03, 0.03);
                BasicStroke basicStroke1
                        = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                xyPlot.addAnnotation(new XYShapeAnnotation(cpaLocationShapeEnd1, basicStroke1, Color.BLUE, Color.BLUE));
            }
        }


    }

    private void drawTcpaSmall(XYPlot xyPlot, double xValue, double yValue, double tcpaValue) {
        Ellipse2D.Double cpaLocationShape1
                = new Ellipse2D.Double(xValue + 0.1645, yValue - 0.062, 0.15, 0.15);
        BasicStroke basicStroke
                = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
        xyPlot.addAnnotation(new XYShapeAnnotation(cpaLocationShape1, basicStroke, Color.GRAY, null));
    }

    private void drawTcpa(XYPlot xyPlot, double xValue, double yValue, double tcpaValue) {
        double diam = 0.04;
        double ref = 0.02;
        double dist = 0.1075;
        if (tcpaValue > 3) {
            Ellipse2D.Double cpaLocationShape1
                    = new Ellipse2D.Double(xValue - ref, yValue + dist + ref, diam, diam);
            BasicStroke basicStroke
                    = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
            if (tcpaValue < 6) {
                xyPlot.addAnnotation(new XYShapeAnnotation(cpaLocationShape1, basicStroke, Color.RED, Color.RED));
            } else {
                xyPlot.addAnnotation(new XYShapeAnnotation(cpaLocationShape1, basicStroke, Color.GRAY, Color.GRAY));
            }
        }
        if (tcpaValue >= 6) {
            Ellipse2D.Double cpaLocationShape2
                    = new Ellipse2D.Double(xValue - ref + dist, yValue + dist - ref, diam, diam);
            BasicStroke basicStroke
                    = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
            if (tcpaValue < 9) {
                xyPlot.addAnnotation(new XYShapeAnnotation(cpaLocationShape2, basicStroke, Color.BLUE, Color.BLUE));
            } else {
                xyPlot.addAnnotation(new XYShapeAnnotation(cpaLocationShape2, basicStroke, Color.GRAY, Color.GRAY));
            }
        }
        if (tcpaValue >= 9) {
            Ellipse2D.Double cpaLocationShape3
                    = new Ellipse2D.Double(xValue  + dist + ref, yValue - ref, diam, diam);
            BasicStroke basicStroke
                    = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
            if (tcpaValue < 12) {
                xyPlot.addAnnotation(new XYShapeAnnotation(cpaLocationShape3, basicStroke, Color.BLUE, Color.BLUE));
            } else {
                xyPlot.addAnnotation(new XYShapeAnnotation(cpaLocationShape3, basicStroke, Color.GRAY, Color.GRAY));
            }
        }
        if (tcpaValue >= 12) {
            Ellipse2D.Double cpaLocationShape4
                    = new Ellipse2D.Double(xValue - ref + dist, yValue - dist - ref, diam, diam);
            BasicStroke basicStroke
                    = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
            if (tcpaValue < 15) {
                xyPlot.addAnnotation(new XYShapeAnnotation(cpaLocationShape4, basicStroke, Color.BLUE, Color.BLUE));
            } else {
                xyPlot.addAnnotation(new XYShapeAnnotation(cpaLocationShape4, basicStroke, Color.GRAY, Color.GRAY));
            }
        }
        if (tcpaValue >= 15) {
            Ellipse2D.Double cpaLocationShape5
                    = new Ellipse2D.Double(xValue - ref, yValue - dist - ref - ref - ref, diam, diam);
            BasicStroke basicStroke
                    = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
            if (tcpaValue < 18) {
                xyPlot.addAnnotation(new XYShapeAnnotation(cpaLocationShape5, basicStroke, Color.BLUE, Color.BLUE));
            } else {
                xyPlot.addAnnotation(new XYShapeAnnotation(cpaLocationShape5, basicStroke, Color.GRAY, Color.GRAY));
            }
        }
        if (tcpaValue >= 18) {
            Ellipse2D.Double cpaLocationShape6
                    = new Ellipse2D.Double(xValue - ref - dist, yValue - dist - ref, diam, diam);
            BasicStroke basicStroke
                    = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
            if (tcpaValue < 21) {
                xyPlot.addAnnotation(new XYShapeAnnotation(cpaLocationShape6, basicStroke, Color.BLUE, Color.BLUE));
            } else {
                xyPlot.addAnnotation(new XYShapeAnnotation(cpaLocationShape6, basicStroke, Color.GRAY, Color.GRAY));
            }
        }
        if (tcpaValue >= 21) {
            Ellipse2D.Double cpaLocationShape7
                    = new Ellipse2D.Double(xValue  - dist - ref - ref -ref, yValue - ref, diam, diam);
            BasicStroke basicStroke
                    = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
            if (tcpaValue < 24) {
                xyPlot.addAnnotation(new XYShapeAnnotation(cpaLocationShape7, basicStroke, Color.BLUE, Color.BLUE));
            } else {
                xyPlot.addAnnotation(new XYShapeAnnotation(cpaLocationShape7, basicStroke, Color.GRAY, Color.GRAY));
            }
        }
        if (tcpaValue >= 24) {
            Ellipse2D.Double cpaLocationShape8
                   = new Ellipse2D.Double(xValue - ref - dist, yValue + dist - ref, diam, diam);
            BasicStroke basicStroke
                    = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
            if (tcpaValue < 27) {
                xyPlot.addAnnotation(new XYShapeAnnotation(cpaLocationShape8, basicStroke, Color.BLUE, Color.BLUE));
            } else {
                xyPlot.addAnnotation(new XYShapeAnnotation(cpaLocationShape8, basicStroke, Color.GRAY, Color.GRAY));
            }
        }
    }

    private AlterationsOfCourse findIntentionOfCourseAlteration(ShipAis shipAis) {
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




        public void run () {
            EventQueue.invokeLater(() -> {
                CrossAreaChartDraw chart = new CrossAreaChartDraw("shipsConflicts");
                chart.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                chart.pack();
                chart.setLocationRelativeTo(null);
                chart.setVisible(true);
            });
        }
    }

