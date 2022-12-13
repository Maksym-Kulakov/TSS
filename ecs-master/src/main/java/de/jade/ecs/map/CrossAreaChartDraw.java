package de.jade.ecs.map;

import de.jade.ecs.map.geochart.AlterationsOfCourse;
import de.jade.ecs.map.geochart.DestinationsToEast;
import de.jade.ecs.map.geochart.DestinationsToSouth;
import de.jade.ecs.map.geochart.GeoTssAreas;
import de.jade.ecs.map.shipchart.PairHash;
import org.apache.sis.geometry.DirectPosition2D;
import org.apache.sis.referencing.GeodeticCalculator;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYAnnotation;
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
import org.scheduler.agent.state.ShipState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CrossAreaChartDraw extends ApplicationFrame implements Runnable {
    public static Point2D.Double centerPoint = new Point2D.Double(53.93652, 7.696533);
    private static final String title = "South Cross Area";
    private XYSeries chartSouth = new XYSeries("Conflict Ships");
    public static Map<Integer, ConflictShips> shipsPairInConflict = new HashMap<>();

    private static Map<Integer, AlterationsOfCourse> shipsToSouth = new HashMap<>();
    private static Map<Integer, AlterationsOfCourse> shipsToEast = new HashMap<>();
    private static Map<Integer, AlterationsOfCourse> shipsToNorth = new HashMap<>();

    public static Map<Integer, ShipAis> shipsManoeuvered = new HashMap<>();



    XYPlot xyPlot;
    XYTextAnnotation textAnnotation;
    Set<Point2D.Double> directPositions;
    List<XYAnnotation> xyAnnotationList;
    List<XYAnnotation> trialXyAnnotationList;
    int account = 0;
    boolean safeCpaPushed = false;

    public CrossAreaChartDraw(String s) {
        super(s);
        final ChartPanel chartPanel = createChartPanel();
        this.add(chartPanel, BorderLayout.CENTER);

        JPanel controlNorth = new JPanel();
        JPanel controlSouth = new JPanel();

        SpinnerModel spinnerModelCpa = new SpinnerNumberModel(1, 0.1, 10, 0.1);
        JSpinner jSpinnerCpa = new JSpinner(spinnerModelCpa);

        SpinnerModel spinnerModelTcpa = new SpinnerNumberModel(30, 0.1, 30, 1);
        JSpinner jSpinnerTcpa = new JSpinner(spinnerModelTcpa);

        SpinnerModel spinnerModelShip = new SpinnerNumberModel(1, 1, 3, 1);
        JSpinner jSpinnerShip = new JSpinner(spinnerModelShip);

        SpinnerModel spinnerModelShipHdg = new SpinnerNumberModel(0, 0, 359, 1);
        JSpinner jSpinnerShipHdg = new JSpinner(spinnerModelShipHdg);

        SpinnerModel spinnerModelShipSpeed = new SpinnerNumberModel(10, 0.1, 30, 1);
        JSpinner jSpinnerShipSpeed = new JSpinner(spinnerModelShipSpeed);

        SpinnerModel spinnerModelTrial = new SpinnerListModel(Manoueveres.values());
        JSpinner jSpinnerShipTrial = new JSpinner(spinnerModelTrial);

        SpinnerModel spinnerModelAllowCpa = new SpinnerNumberModel(0.4, 0.1, 1, 0.1);
        JSpinner jSpinnerShipAllowCpa = new JSpinner(spinnerModelAllowCpa);


        controlNorth.add(new JButton(new AbstractAction("SafeCpa") {
            @Override
            public void actionPerformed(ActionEvent e) {
            /*    for (XYAnnotation xyAnnotation : trialXyAnnotationList) {
                    xyPlot.removeAnnotation(xyAnnotation);
                }*/
                String valueOfManoeuver = jSpinnerShipTrial.getValue().toString();
                double valueFilterCpa = Double.parseDouble(jSpinnerShipAllowCpa.getValue().toString());
                getSafeCpa(valueOfManoeuver, valueFilterCpa);
                updateTrial(valueFilterCpa);
                safeCpaPushed = true;
            }
        }));


        controlNorth.add(new JButton(new AbstractAction("in BWCR psn") {
            @Override
            public void actionPerformed(ActionEvent e) {
                chartSouth.clear();
                xyPlot.clearAnnotations();

                Double valueFilterCpa = Double.valueOf(jSpinnerCpa.getValue().toString());
                Double valueFilterTcpa = Double.valueOf(jSpinnerTcpa.getValue().toString());

                updateBCR(valueFilterCpa, valueFilterTcpa);
                account = 0;
                safeCpaPushed = false;
            }
        }));

        controlNorth.add(new JButton(new AbstractAction("in CPA psn") {
            @Override
            public void actionPerformed(ActionEvent e) {
                chartSouth.clear();
                xyPlot.clearAnnotations();

                Double valueFilterCpa = Double.valueOf(jSpinnerCpa.getValue().toString());
                Double valueFilterTcpa = Double.valueOf(jSpinnerTcpa.getValue().toString());

                update(valueFilterCpa, valueFilterTcpa);
                account = 0;
                safeCpaPushed = false;
            }
        }));



        controlNorth.add(new JButton(new AbstractAction("pair") {
            @Override
            public void actionPerformed(ActionEvent e) {
                xyAnnotationList = highLightPair();
                for (XYAnnotation xyAnnotationToRemove : xyAnnotationList) {
                    xyPlot.removeAnnotation(xyAnnotationToRemove);
                }
                xyPlot.addAnnotation(xyAnnotationList.get(account));
                xyPlot.addAnnotation(xyAnnotationList.get(account + 1));
                account += 2;
                if (xyAnnotationList.size() == account) {
                    account = 0;
                }
            }
        }));



        JTextArea jTextAreaCpa = new JTextArea("cpa <");
        controlNorth.add(jTextAreaCpa);

        controlNorth.add(jSpinnerCpa, BorderLayout.SOUTH);

        JTextArea jTextAreaNm= new JTextArea("nm");
        controlNorth.add(jTextAreaNm);

        JTextArea jTextAreaTcpa = new JTextArea("tcpa <");
        controlNorth.add(jTextAreaTcpa);

        controlNorth.add(jSpinnerTcpa, BorderLayout.SOUTH);

        JTextArea jTextAreaMin= new JTextArea("min");
        controlNorth.add(jTextAreaMin);

        this.add(controlNorth, BorderLayout.NORTH);

        JTextArea jTextAreaTrial= new JTextArea("Trial");
        controlSouth.add(jTextAreaTrial);

        controlSouth.add(jSpinnerShipTrial, BorderLayout.SOUTH);

        JTextArea jTextAreaAllowCpa= new JTextArea("minCPA");
        controlSouth.add(jTextAreaAllowCpa);

        controlSouth.add(jSpinnerShipAllowCpa, BorderLayout.SOUTH);

        JTextArea jTextAreaShip= new JTextArea("Ship №:");
        controlSouth.add(jTextAreaShip);

        controlSouth.add(jSpinnerShip, BorderLayout.SOUTH);

        JTextArea jTextAreaShipHdg= new JTextArea("HDG:");
        controlSouth.add(jTextAreaShipHdg);

        controlSouth.add(jSpinnerShipHdg, BorderLayout.SOUTH);

        JTextArea jTextAreaShipDegr= new JTextArea("degr");
        controlSouth.add(jTextAreaShipDegr);

        JTextArea jTextAreaShipSpeed= new JTextArea("SPD:");
        controlSouth.add(jTextAreaShipSpeed);

        controlSouth.add(jSpinnerShipSpeed, BorderLayout.SOUTH);

        JTextArea jTextAreaShipKn= new JTextArea("kn");
        controlSouth.add(jTextAreaShipKn);

        this.add(controlSouth, BorderLayout.SOUTH);
    }

    private List<XYAnnotation> highLightPair() {
        List<XYAnnotation> xyAnnotationList = new ArrayList<>();
        for (Point2D.Double directPosition : directPositions) {
            Ellipse2D.Double cpaLocationShapeEnd1
                    = new Ellipse2D.Double(directPosition.getX() - 0.02, directPosition.getY() - 0.02, 0.04, 0.04);
            BasicStroke basicStroke1
                    = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
            XYShapeAnnotation xyPairAnnotation = new XYShapeAnnotation(cpaLocationShapeEnd1, basicStroke1, Color.BLACK, Color.ORANGE);
           xyAnnotationList.add(xyPairAnnotation);
        }
        return xyAnnotationList;
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
        if (shipsToEast.containsKey(shipAis.mmsiNum)) {
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
        } else if (shipsToSouth.containsKey(shipAis.mmsiNum)) {
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
        } else if (shipsToNorth.containsKey(shipAis.mmsiNum)) {
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

        if (shipsToEast.containsKey(shipAis.mmsiNum)) {
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
        } else if (shipsToSouth.containsKey(shipAis.mmsiNum)) {
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
        } else if (shipsToNorth.containsKey(shipAis.mmsiNum)) {
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
        directPositions = new LinkedHashSet<>();

        Map<Integer, ConflictShips> activeMap = new HashMap<>();

        if (safeCpaPushed) {
            activeMap = trialShipsPairInConflict;
        } else {
            activeMap = CrossAreaChart.shipsConflictsInCrossAreaSouth;
        }

        for (Map.Entry<Integer, ConflictShips> shipsPair
                : activeMap.entrySet()) {
            //conflicts indication filtering by cpa value
            if (shipsPair.getValue().cpaValue < valueCpaLimit && shipsPair.getValue().tcpaValue < valueTCpaLimit
                    && shipsPair.getValue().tcpaValue > 0) {
                if (!safeCpaPushed) {
                    shipsPairInConflict.put(shipsPair.getKey(), shipsPair.getValue());
                }

                if (!shipsToSouth.containsKey(shipsPair.getValue().shipA.mmsiNum)
                && !shipsToNorth.containsKey(shipsPair.getValue().shipA.mmsiNum)
                && !shipsToEast.containsKey(shipsPair.getValue().shipA.mmsiNum)) {
                    switch (shipsPair.getValue().shipA.geoTssArea) {
                        case TO_SOUTH:
                            shipsToSouth.put(shipsPair.getValue().shipA.mmsiNum, findIntentionOfCourseAlteration(shipsPair.getValue().shipA));
                            break;
                        case TO_NORTH:
                            shipsToNorth.put(shipsPair.getValue().shipA.mmsiNum, findIntentionOfCourseAlteration(shipsPair.getValue().shipA));
                            break;
                        case TO_EAST:
                            shipsToEast.put(shipsPair.getValue().shipA.mmsiNum, findIntentionOfCourseAlteration(shipsPair.getValue().shipA));
                            break;
                    }
                }

                if (!shipsToSouth.containsKey(shipsPair.getValue().shipB.mmsiNum)
                        && !shipsToNorth.containsKey(shipsPair.getValue().shipB.mmsiNum)
                        && !shipsToEast.containsKey(shipsPair.getValue().shipB.mmsiNum)) {
                    switch (shipsPair.getValue().shipB.geoTssArea) {
                        case TO_SOUTH:
                            shipsToSouth.put(shipsPair.getValue().shipB.mmsiNum, findIntentionOfCourseAlteration(shipsPair.getValue().shipB));
                            break;
                        case TO_NORTH:
                            shipsToNorth.put(shipsPair.getValue().shipB.mmsiNum, findIntentionOfCourseAlteration(shipsPair.getValue().shipB));
                            break;
                        case TO_EAST:
                            shipsToEast.put(shipsPair.getValue().shipB.mmsiNum, findIntentionOfCourseAlteration(shipsPair.getValue().shipB));
                            break;
                    }
                }


                double[] xyCoordinates = getXYCoordinates(shipsPair.getValue().cpaLocation);
                xValue = xyCoordinates[0];
                yValue = xyCoordinates[1];

                //cpa value in circle
           /*     System.out.println("shipsConflicts updated");*/
                textAnnotation = new XYTextAnnotation(String.valueOf(Math.round(shipsPair.getValue().tcpaValue)),
                        xValue + 0.24, yValue + 0.02);
                if (shipsPair.getValue().tcpaValue < 6) {
                    textAnnotation.setPaint(Color.red);
                } else {
                    textAnnotation.setPaint(Color.black);
                }
                textAnnotation.setFont(new Font("Tahoma", Font.BOLD, 10));

                double[] xyCoordinatesLine1 = getXYCoordinates(shipsPair.getValue().position1Future);
                double xValueLine1 = xyCoordinatesLine1[0];
                double yValueLine1 = xyCoordinatesLine1[1];
                directPositions.add(new Point2D.Double(xValueLine1, yValueLine1));

                double[] xyCoordinatesLine2 = getXYCoordinates(shipsPair.getValue().position2Future);
                double xValueLine2 = xyCoordinatesLine2[0];
                double yValueLine2 = xyCoordinatesLine2[1];
                directPositions.add(new Point2D.Double(xValueLine2, yValueLine2));


                //lines of ships` paths


                double[] xyCoordinatesEnds1
                        = getXYCoordinatesEnds(xValueLine1, yValueLine1, shipsPair.getValue().shipA, shipsPair.getValue().shipA.hdg);
                double[] xyCoordinatesEnds2
                        = getXYCoordinatesEnds(xValueLine2, yValueLine2, shipsPair.getValue().shipB, shipsPair.getValue().shipB.hdg);

                //to cacl coord of prolonged point
                int distanceOfProlong = 4;

                double[] xyCoordinatesProlonged1 = getProlongedPointCoordinates(xyCoordinatesEnds1[0], xyCoordinatesEnds1[1],
                        shipsPair.getValue().shipA, shipsPair.getValue().shipA.hdg, distanceOfProlong);

                double[] xyCoordinatesProlonged2 = getProlongedPointCoordinates(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1],
                        shipsPair.getValue().shipB, shipsPair.getValue().shipB.hdg, distanceOfProlong);

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

//                xyPlot.addAnnotation(new XYShapeAnnotation(crossPointMark, basicStrokeCrossPointMark, Color.YELLOW, Color.YELLOW));


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

                //check if vessel in cross area
                boolean insideAreaShipA = BoundaryArea.insideArea(new Point2D.Double(shipsPair.getValue().shipA.geoPosition.getLatitude(),
                        shipsPair.getValue().shipA.geoPosition.getLongitude()), CrossAreaChart.crossAreaSouth);

                boolean insideAreaShipB = BoundaryArea.insideArea(new Point2D.Double(shipsPair.getValue().shipB.geoPosition.getLatitude(),
                        shipsPair.getValue().shipB.geoPosition.getLongitude()), CrossAreaChart.crossAreaSouth);

                XYLineAnnotation xyLineAnnotation1;
                if (insideAreaShipA) {
                    double[] xyCoordinatesInCross1 =
                            getXYCoordinates(new DirectPosition2D(shipsPair.getValue().shipA.geoPosition.getLatitude(),
                                    shipsPair.getValue().shipA.geoPosition.getLongitude()));

                    xyLineAnnotation1 = new XYLineAnnotation(xyCoordinatesInCross1[0], xyCoordinatesInCross1[1],
                            xValueLine1, yValueLine1, new BasicStroke(2f), Color.white);

                    Ellipse2D.Double shipLocationInCross1
                            = new Ellipse2D.Double(xyCoordinatesInCross1[0] - 0.025, xyCoordinatesInCross1[1] - 0.025, 0.05, 0.05);
                    BasicStroke basicStroke1
                            = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                    xyPlot.addAnnotation(new XYShapeAnnotation(shipLocationInCross1, basicStroke1, Color.DARK_GRAY, Color.DARK_GRAY));
                    xyCoordinatesEnds1 = xyCoordinatesInCross1;
                } else {
                    xyLineAnnotation1 = new XYLineAnnotation(xyCoordinatesEnds1[0], xyCoordinatesEnds1[1],
                                    xValueLine1, yValueLine1, new BasicStroke(2f), Color.white);
                }

                XYLineAnnotation xyLineAnnotation2;
                if (insideAreaShipB) {
                    double[] xyCoordinatesInCross2 =
                            getXYCoordinates(new DirectPosition2D(shipsPair.getValue().shipB.geoPosition.getLatitude(),
                                    shipsPair.getValue().shipB.geoPosition.getLongitude()));

                    xyLineAnnotation2 = new XYLineAnnotation(xyCoordinatesInCross2[0], xyCoordinatesInCross2[1],
                            xValueLine2, yValueLine2, new BasicStroke(2f), Color.white);

                    Ellipse2D.Double shipLocationInCross2
                            = new Ellipse2D.Double(xyCoordinatesInCross2[0] - 0.025, xyCoordinatesInCross2[1] - 0.025, 0.05, 0.05);
                    BasicStroke basicStroke2
                            = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                    xyPlot.addAnnotation(new XYShapeAnnotation(shipLocationInCross2, basicStroke2, Color.DARK_GRAY, Color.DARK_GRAY));
                    xyCoordinatesEnds2 = xyCoordinatesInCross2;
                } else {
                    xyLineAnnotation2 = new XYLineAnnotation(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1],
                                    xValueLine2, yValueLine2, new BasicStroke(2f), Color.white);
                }

                xyPlot.addAnnotation(xyLineAnnotation1);
                xyPlot.addAnnotation(xyLineAnnotation2);


                //green and red line to indicate ships port intentions
                XYLineAnnotation xyLineAnnotationTurnA = null;
                XYLineAnnotation xyLineAnnotationTurnB = null;

                if (shipsToSouth.containsKey(shipsPair.getValue().shipA.mmsiNum) && shipsToSouth.get(shipsPair.getValue().shipA.mmsiNum)
                 == AlterationsOfCourse.PORT) {
                    xyLineAnnotationTurnA = new XYLineAnnotation(xyCoordinatesEnds1[0] + 0.01, xyCoordinatesEnds1[1], xValueLine1 + 0.01, yValueLine1, new BasicStroke(1f), Color.red);
                }
                if (shipsToEast.containsKey(shipsPair.getValue().shipA.mmsiNum) && shipsToEast.get(shipsPair.getValue().shipA.mmsiNum)
                        == AlterationsOfCourse.STARBOARD) {
                    xyLineAnnotationTurnA = new XYLineAnnotation(xyCoordinatesEnds1[0], xyCoordinatesEnds1[1] - 0.01, xValueLine1, yValueLine1 - 0.01, new BasicStroke(1f), Color.green);
                }
                if (shipsToSouth.containsKey(shipsPair.getValue().shipB.mmsiNum) && shipsToSouth.get(shipsPair.getValue().shipB.mmsiNum)
                        == AlterationsOfCourse.PORT) {
                    xyLineAnnotationTurnB = new XYLineAnnotation(xyCoordinatesEnds2[0] + 0.01, xyCoordinatesEnds2[1], xValueLine2 + 0.01, yValueLine2, new BasicStroke(1f), Color.red);
                }
                if (shipsToEast.containsKey(shipsPair.getValue().shipB.mmsiNum) && shipsToEast.get(shipsPair.getValue().shipB.mmsiNum)
                        == AlterationsOfCourse.STARBOARD) {
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

                drawTcpaSmall(xyPlot, xValue, yValue, shipsPair.getValue().tcpaValue);

            } else if (shipsPair.getValue().cpaValue < valueCpaLimit && shipsPair.getValue().tcpaValue < valueTCpaLimit
                    && shipsPair.getValue().tcpaValue < 0) {
                double[] xyCoordinatesInCross1 =
                        getXYCoordinates(new DirectPosition2D(shipsPair.getValue().shipA.geoPosition.getLatitude(),
                                shipsPair.getValue().shipA.geoPosition.getLongitude()));

                Ellipse2D.Double shipLocationInCross1
                        = new Ellipse2D.Double(xyCoordinatesInCross1[0] - 0.025, xyCoordinatesInCross1[1] - 0.025, 0.05, 0.05);
                BasicStroke basicStroke1
                        = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                xyPlot.addAnnotation(new XYShapeAnnotation(shipLocationInCross1, basicStroke1, Color.DARK_GRAY, Color.DARK_GRAY));

                double[] xyCoordinatesInCross2 =
                        getXYCoordinates(new DirectPosition2D(shipsPair.getValue().shipB.geoPosition.getLatitude(),
                                shipsPair.getValue().shipB.geoPosition.getLongitude()));

                Ellipse2D.Double shipLocationInCross2
                        = new Ellipse2D.Double(xyCoordinatesInCross2[0] - 0.025, xyCoordinatesInCross2[1] - 0.025, 0.05, 0.05);
                BasicStroke basicStroke2
                        = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                xyPlot.addAnnotation(new XYShapeAnnotation(shipLocationInCross2, basicStroke2, Color.DARK_GRAY, Color.DARK_GRAY));
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

    private void updateBCR(Double valueCpaLimit, Double valueTCpaLimit) {
        double xValue = 0;
        double yValue = 0;
        directPositions = new LinkedHashSet<>();

        for (Map.Entry<Integer, ConflictShips> shipsPair
                : CrossAreaChart.shipsConflictsInCrossAreaSouth.entrySet()) {
            //conflicts indication filtering by cpa value
            if (shipsPair.getValue().cpaValue < valueCpaLimit && shipsPair.getValue().tcpaValue < valueTCpaLimit
            && shipsPair.getValue().tcpaValue > 0) {
                shipsPairInConflict.put(shipsPair.getKey(), shipsPair.getValue());

                double[] xyCoordinates = getXYCoordinates(shipsPair.getValue().cpaLocation);
                xValue = xyCoordinates[0];
                yValue = xyCoordinates[1];

                //cpa value in circle
                System.out.println("shipsConflicts updated");
                textAnnotation = new XYTextAnnotation(String.valueOf(Math.round(shipsPair.getValue().tcpaValue)),
                        xValue + 0.24, yValue + 0.02);
                if (shipsPair.getValue().tcpaValue < 6) {
                    textAnnotation.setPaint(Color.red);
                } else {
                    textAnnotation.setPaint(Color.black);
                }
                textAnnotation.setFont(new Font("Tahoma", Font.BOLD, 10));

                if (!shipsToSouth.containsKey(shipsPair.getValue().shipA.mmsiNum)
                        && !shipsToNorth.containsKey(shipsPair.getValue().shipA.mmsiNum)
                        && !shipsToEast.containsKey(shipsPair.getValue().shipA.mmsiNum)) {
                    switch (shipsPair.getValue().shipA.geoTssArea) {
                        case TO_SOUTH:
                            shipsToSouth.put(shipsPair.getValue().shipA.mmsiNum, findIntentionOfCourseAlteration(shipsPair.getValue().shipA));
                            break;
                        case TO_NORTH:
                            shipsToNorth.put(shipsPair.getValue().shipA.mmsiNum, findIntentionOfCourseAlteration(shipsPair.getValue().shipA));
                            break;
                        case TO_EAST:
                            shipsToEast.put(shipsPair.getValue().shipA.mmsiNum, findIntentionOfCourseAlteration(shipsPair.getValue().shipA));
                            break;
                    }
                }


                if (!shipsToSouth.containsKey(shipsPair.getValue().shipB.mmsiNum)
                        && !shipsToNorth.containsKey(shipsPair.getValue().shipB.mmsiNum)
                        && !shipsToEast.containsKey(shipsPair.getValue().shipB.mmsiNum)) {
                    switch (shipsPair.getValue().shipB.geoTssArea) {
                        case TO_SOUTH:
                            shipsToSouth.put(shipsPair.getValue().shipB.mmsiNum, findIntentionOfCourseAlteration(shipsPair.getValue().shipB));
                            break;
                        case TO_NORTH:
                            shipsToNorth.put(shipsPair.getValue().shipB.mmsiNum, findIntentionOfCourseAlteration(shipsPair.getValue().shipB));
                            break;
                        case TO_EAST:
                            shipsToEast.put(shipsPair.getValue().shipB.mmsiNum, findIntentionOfCourseAlteration(shipsPair.getValue().shipB));
                            break;
                    }
                }

                boolean insideAreaShipA = BoundaryArea.insideArea(new Point2D.Double(shipsPair.getValue().shipA.geoPosition.getLatitude(),
                        shipsPair.getValue().shipA.geoPosition.getLongitude()), CrossAreaChart.crossAreaSouth);

                boolean insideAreaShipB = BoundaryArea.insideArea(new Point2D.Double(shipsPair.getValue().shipB.geoPosition.getLatitude(),
                        shipsPair.getValue().shipB.geoPosition.getLongitude()), CrossAreaChart.crossAreaSouth);


                //lines of ships` paths
                double[] xyCoordinatesLine1 = getXYCoordinates(shipsPair.getValue().position1Future);
                double xValueLine1 = xyCoordinatesLine1[0];
                double yValueLine1 = xyCoordinatesLine1[1];
                 ;

                double[] xyCoordinatesLine2 = getXYCoordinates(shipsPair.getValue().position2Future);
                double xValueLine2 = xyCoordinatesLine2[0];
                double yValueLine2 = xyCoordinatesLine2[1];


                double[] xyCoordinatesEnds1
                        = getXYCoordinatesEnds(xValueLine1, yValueLine1, shipsPair.getValue().shipA, shipsPair.getValue().shipA.hdg);
                double[] xyCoordinatesEnds2
                        = getXYCoordinatesEnds(xValueLine2, yValueLine2, shipsPair.getValue().shipB, shipsPair.getValue().shipB.hdg);

                //to cacl coord of prolonged point
                int distanceOfProlong = 4;

                double[] xyCoordinatesProlonged1 = getProlongedPointCoordinates(xyCoordinatesEnds1[0], xyCoordinatesEnds1[1],
                        shipsPair.getValue().shipA, shipsPair.getValue().shipA.hdg, distanceOfProlong);

                double[] xyCoordinatesProlonged2 = getProlongedPointCoordinates(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1],
                        shipsPair.getValue().shipB, shipsPair.getValue().shipB.hdg, distanceOfProlong);

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

                boolean checkIfPointInSegmentA = false;
                if (shipsPair.getValue().shipA.geoTssArea == GeoTssAreas.TO_EAST
                        && crossPoint.getX() < xValueLine1) {
                    checkIfPointInSegmentA = true;
                } else if (shipsPair.getValue().shipA.geoTssArea == GeoTssAreas.TO_SOUTH
                        && crossPoint.getY() > yValueLine1) {
                    checkIfPointInSegmentA = true;
                } else if (shipsPair.getValue().shipA.geoTssArea == GeoTssAreas.TO_NORTH
                        && crossPoint.getY() < yValueLine1) {
                    checkIfPointInSegmentA = true;
                }

                boolean checkIfPointOutA = false;
                boolean checkIfPointOutB = false;

                if (checkIfPointInSegmentA) {
                    double distFmBcrToEnd1 = Math.sqrt((yValueLine1 - crossPoint.getY()) * (yValueLine1 - crossPoint.getY())
                        + (xValueLine1 - crossPoint.getX()) * (xValueLine1 - crossPoint.getX()));
                    double distFmBcrToEnd2 = distFmBcrToEnd1 * shipsPair.getValue().shipB.speed / shipsPair.getValue().shipA.speed;

                    double distOfLine2 = Math.sqrt((yValueLine2 - xyCoordinatesEnds2[1]) * (yValueLine2 - xyCoordinatesEnds2[1])
                            + (xValueLine2 - xyCoordinatesEnds2[0]) * (xValueLine2 - xyCoordinatesEnds2[0]));

                    double[] otherBwrPoint = getProlongedPointCoordinates(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1], shipsPair.getValue().shipB, shipsPair.getValue().shipB.hdg, distOfLine2 - distFmBcrToEnd2);

                    if (insideAreaShipA) {
                        double[] xyCoordinatesInCross1 =
                                getXYCoordinates(new DirectPosition2D(shipsPair.getValue().shipA.geoPosition.getLatitude(),
                                        shipsPair.getValue().shipA.geoPosition.getLongitude()));

                        xyLineAnnotation1 = new XYLineAnnotation(xyCoordinatesInCross1[0], xyCoordinatesInCross1[1],
                                crossPoint.getX(), crossPoint.getY(), new BasicStroke(2f), Color.white);

                        Ellipse2D.Double shipLocationInCross1
                                = new Ellipse2D.Double(xyCoordinatesInCross1[0] - 0.025, xyCoordinatesInCross1[1] - 0.025, 0.05, 0.05);
                        BasicStroke basicStroke1
                                = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                        xyPlot.addAnnotation(new XYShapeAnnotation(shipLocationInCross1, basicStroke1, Color.DARK_GRAY, Color.DARK_GRAY));
                        xyCoordinatesEnds1 = xyCoordinatesInCross1;

                        if (shipsToEast.containsKey(shipsPair.getValue().shipA.mmsiNum)
                                && xyCoordinatesInCross1[0] > crossPoint.getX()) {
                            checkIfPointOutA = true;
                        } else if (shipsToSouth.containsKey(shipsPair.getValue().shipA.mmsiNum)
                                && xyCoordinatesInCross1[1] < crossPoint.getY()) {
                            checkIfPointOutA = true;
                        } else if (shipsToNorth.containsKey(shipsPair.getValue().shipA.mmsiNum)
                                && xyCoordinatesInCross1[1] > crossPoint.getY()) {
                            checkIfPointOutA = true;
                        }
                    } else {
                        xyLineAnnotation1 = new XYLineAnnotation(xyCoordinatesEnds1[0], xyCoordinatesEnds1[1],
                                crossPoint.getX(), crossPoint.getY(), new BasicStroke(2f), Color.white);
                    }


                    if (insideAreaShipB) {
                        double[] xyCoordinatesInCross2 =
                                getXYCoordinates(new DirectPosition2D(shipsPair.getValue().shipB.geoPosition.getLatitude(),
                                        shipsPair.getValue().shipB.geoPosition.getLongitude()));

                        xyLineAnnotation2 = new XYLineAnnotation(xyCoordinatesInCross2[0], xyCoordinatesInCross2[1],
                                otherBwrPoint[0], otherBwrPoint[1], new BasicStroke(2f), Color.white);

                        Ellipse2D.Double shipLocationInCross2
                                = new Ellipse2D.Double(xyCoordinatesInCross2[0] - 0.025, xyCoordinatesInCross2[1] - 0.025, 0.05, 0.05);
                        BasicStroke basicStroke2
                                = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                        xyPlot.addAnnotation(new XYShapeAnnotation(shipLocationInCross2, basicStroke2, Color.DARK_GRAY, Color.DARK_GRAY));
                        xyCoordinatesEnds2 = xyCoordinatesInCross2;

                        if (shipsToEast.containsKey(shipsPair.getValue().shipB.mmsiNum)
                                && xyCoordinatesInCross2[0] > otherBwrPoint[0]) {
                            checkIfPointOutB = true;
                        } else if (shipsToSouth.containsKey(shipsPair.getValue().shipB.mmsiNum)
                                && xyCoordinatesInCross2[1] < otherBwrPoint[1]) {
                            checkIfPointOutB = true;
                        } else if (shipsToNorth.containsKey(shipsPair.getValue().shipB.mmsiNum)
                                && xyCoordinatesInCross2[1] > otherBwrPoint[1]) {
                            checkIfPointOutB = true;
                        }


                    } else {
                        xyLineAnnotation2 = new XYLineAnnotation(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1],
                                otherBwrPoint[0], otherBwrPoint[1], new BasicStroke(2f), Color.white);
                    }

                    if (!checkIfPointOutA && !checkIfPointOutB) {
                        directPositions.add(crossPoint);
                        directPositions.add(new Point2D.Double(otherBwrPoint[0], otherBwrPoint[1]));



                    //green and red line to indicate ships port intentions
                    XYLineAnnotation xyLineAnnotationTurnA = null;
                    XYLineAnnotation xyLineAnnotationTurnB = null;

                    if (shipsToSouth.containsKey(shipsPair.getValue().shipA.mmsiNum) && shipsToSouth.get(shipsPair.getValue().shipA.mmsiNum)
                            == AlterationsOfCourse.PORT) {
                        xyLineAnnotationTurnA = new XYLineAnnotation(xyCoordinatesEnds1[0] + 0.01, xyCoordinatesEnds1[1], crossPoint.getX() + 0.01, crossPoint.getY(), new BasicStroke(1f), Color.red);
                    }
                    if (shipsToEast.containsKey(shipsPair.getValue().shipA.mmsiNum) && shipsToEast.get(shipsPair.getValue().shipA.mmsiNum)
                            == AlterationsOfCourse.STARBOARD) {
                        xyLineAnnotationTurnA = new XYLineAnnotation(xyCoordinatesEnds1[0], xyCoordinatesEnds1[1] - 0.01, crossPoint.getX(), crossPoint.getY() - 0.01, new BasicStroke(1f), Color.green);
                    }
                    if (shipsToSouth.containsKey(shipsPair.getValue().shipB.mmsiNum) && shipsToSouth.get(shipsPair.getValue().shipB.mmsiNum)
                            == AlterationsOfCourse.PORT) {
                        xyLineAnnotationTurnB = new XYLineAnnotation(xyCoordinatesEnds2[0] + 0.01, xyCoordinatesEnds2[1], otherBwrPoint[0] + 0.01, otherBwrPoint[1], new BasicStroke(1f), Color.red);
                    }
                    if (shipsToEast.containsKey(shipsPair.getValue().shipB.mmsiNum) && shipsToEast.get(shipsPair.getValue().shipB.mmsiNum)
                            == AlterationsOfCourse.STARBOARD) {
                        xyLineAnnotationTurnB = new XYLineAnnotation(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1] - 0.01, otherBwrPoint[0], otherBwrPoint[1] - 0.01, new BasicStroke(1f), Color.green);
                    }
                    if (xyLineAnnotationTurnA != null) {
                        xyPlot.addAnnotation(xyLineAnnotationTurnA);
                    }
                    if (xyLineAnnotationTurnB != null) {
                        xyPlot.addAnnotation(xyLineAnnotationTurnB);
                    }

                    }



                } else {
                    double distFmBcrToEnd2 = Math.sqrt((yValueLine2 - crossPoint.getY()) * (yValueLine2 - crossPoint.getY())
                            + (xValueLine2 - crossPoint.getX()) * (xValueLine2 - crossPoint.getX()));
                    double distFmBcrToEnd1 = distFmBcrToEnd2 * shipsPair.getValue().shipA.speed / shipsPair.getValue().shipB.speed;

                    double distOfLine1 = Math.sqrt((yValueLine1 - xyCoordinatesEnds1[1]) * (yValueLine1 - xyCoordinatesEnds1[1])
                            + (xValueLine1 - xyCoordinatesEnds1[0]) * (xValueLine1 - xyCoordinatesEnds1[0]));

                    double[] otherBwrPoint = getProlongedPointCoordinates(xyCoordinatesEnds1[0], xyCoordinatesEnds1[1], shipsPair.getValue().shipA, shipsPair.getValue().shipA.hdg, distOfLine1 - distFmBcrToEnd1);


                    if (insideAreaShipA) {
                        double[] xyCoordinatesInCross1 =
                                getXYCoordinates(new DirectPosition2D(shipsPair.getValue().shipA.geoPosition.getLatitude(),
                                        shipsPair.getValue().shipA.geoPosition.getLongitude()));

                        xyLineAnnotation1 = new XYLineAnnotation(xyCoordinatesInCross1[0], xyCoordinatesInCross1[1],
                                otherBwrPoint[0], otherBwrPoint[1], new BasicStroke(2f), Color.white);

                        Ellipse2D.Double shipLocationInCross1
                                = new Ellipse2D.Double(xyCoordinatesInCross1[0] - 0.025, xyCoordinatesInCross1[1] - 0.025, 0.05, 0.05);
                        BasicStroke basicStroke1
                                = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                        xyPlot.addAnnotation(new XYShapeAnnotation(shipLocationInCross1, basicStroke1, Color.DARK_GRAY, Color.DARK_GRAY));
                        xyCoordinatesEnds1 = xyCoordinatesInCross1;

                        if (shipsToEast.containsKey(shipsPair.getValue().shipA.mmsiNum)
                                && xyCoordinatesInCross1[0] > otherBwrPoint[0]) {
                            checkIfPointOutA = true;
                        } else if (shipsToSouth.containsKey(shipsPair.getValue().shipA.mmsiNum)
                                && xyCoordinatesInCross1[1] < otherBwrPoint[1]) {
                            checkIfPointOutA = true;
                        } else if (shipsToNorth.containsKey(shipsPair.getValue().shipA.mmsiNum)
                                && xyCoordinatesInCross1[1] > otherBwrPoint[1]) {
                            checkIfPointOutA = true;
                        }

                    } else {
                        xyLineAnnotation1 = new XYLineAnnotation(xyCoordinatesEnds1[0], xyCoordinatesEnds1[1],
                                otherBwrPoint[0], otherBwrPoint[1], new BasicStroke(2f), Color.white);
                    }


                    if (insideAreaShipB) {
                        double[] xyCoordinatesInCross2 =
                                getXYCoordinates(new DirectPosition2D(shipsPair.getValue().shipB.geoPosition.getLatitude(),
                                        shipsPair.getValue().shipB.geoPosition.getLongitude()));

                        xyLineAnnotation2 = new XYLineAnnotation(xyCoordinatesInCross2[0], xyCoordinatesInCross2[1],
                                crossPoint.getX(), crossPoint.getY(), new BasicStroke(2f), Color.white);

                        Ellipse2D.Double shipLocationInCross2
                                = new Ellipse2D.Double(xyCoordinatesInCross2[0] - 0.025, xyCoordinatesInCross2[1] - 0.025, 0.05, 0.05);
                        BasicStroke basicStroke2
                                = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                        xyPlot.addAnnotation(new XYShapeAnnotation(shipLocationInCross2, basicStroke2, Color.DARK_GRAY, Color.DARK_GRAY));
                        xyCoordinatesEnds2 = xyCoordinatesInCross2;

                        if (shipsToEast.containsKey(shipsPair.getValue().shipB.mmsiNum)
                                && xyCoordinatesInCross2[0] > crossPoint.getX()) {
                            checkIfPointOutB = true;
                        } else if (shipsToSouth.containsKey(shipsPair.getValue().shipB.mmsiNum)
                                && xyCoordinatesInCross2[1] < crossPoint.getY()) {
                            checkIfPointOutB = true;
                        } else if (shipsToNorth.containsKey(shipsPair.getValue().shipB.mmsiNum)
                                && xyCoordinatesInCross2[1] > crossPoint.getY()) {
                            checkIfPointOutB = true;
                        }

                    } else {
                        xyLineAnnotation2 = new XYLineAnnotation(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1],
                                crossPoint.getX(), crossPoint.getY(), new BasicStroke(2f), Color.white);
                    }



                    if (!checkIfPointOutA && !checkIfPointOutB) {

                    directPositions.add(crossPoint);
                    directPositions.add(new Point2D.Double(otherBwrPoint[0], otherBwrPoint[1]));

                    //green and red line to indicate ships port intentions
                    XYLineAnnotation xyLineAnnotationTurnA = null;
                    XYLineAnnotation xyLineAnnotationTurnB = null;

                    if (shipsToSouth.containsKey(shipsPair.getValue().shipA.mmsiNum) && shipsToSouth.get(shipsPair.getValue().shipA.mmsiNum)
                            == AlterationsOfCourse.PORT) {
                        xyLineAnnotationTurnA = new XYLineAnnotation(xyCoordinatesEnds1[0] + 0.01, xyCoordinatesEnds1[1], otherBwrPoint[0] + 0.01, otherBwrPoint[1], new BasicStroke(1f), Color.red);
                    }
                    if (shipsToEast.containsKey(shipsPair.getValue().shipA.mmsiNum) && shipsToEast.get(shipsPair.getValue().shipA.mmsiNum)
                            == AlterationsOfCourse.STARBOARD) {
                        xyLineAnnotationTurnA = new XYLineAnnotation(xyCoordinatesEnds1[0], xyCoordinatesEnds1[1] - 0.01, otherBwrPoint[0], otherBwrPoint[1] - 0.01, new BasicStroke(1f), Color.green);
                    }
                    if (shipsToSouth.containsKey(shipsPair.getValue().shipB.mmsiNum) && shipsToSouth.get(shipsPair.getValue().shipB.mmsiNum)
                            == AlterationsOfCourse.PORT) {
                        xyLineAnnotationTurnB = new XYLineAnnotation(xyCoordinatesEnds2[0] + 0.01, xyCoordinatesEnds2[1], crossPoint.getX() + 0.01, crossPoint.getY(), new BasicStroke(1f), Color.red);
                    }
                    if (shipsToEast.containsKey(shipsPair.getValue().shipB.mmsiNum) && shipsToEast.get(shipsPair.getValue().shipB.mmsiNum)
                            == AlterationsOfCourse.STARBOARD) {
                        xyLineAnnotationTurnB = new XYLineAnnotation(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1] - 0.01, crossPoint.getX(), crossPoint.getY() - 0.01, new BasicStroke(1f), Color.green);
                    }
                    if (xyLineAnnotationTurnA != null) {
                        xyPlot.addAnnotation(xyLineAnnotationTurnA);
                    }
                    if (xyLineAnnotationTurnB != null) {
                        xyPlot.addAnnotation(xyLineAnnotationTurnB);
                    }
                    }
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




                if (!checkIfPointOutA && !checkIfPointOutB) {

                    xyPlot.addAnnotation(xyLineAnnotation1);
                    xyPlot.addAnnotation(xyLineAnnotation2);

                }


                //circle

                xyPlot.addAnnotation(textAnnotation);

                drawTcpaSmall(xyPlot, xValue, yValue, shipsPair.getValue().tcpaValue);

            } else if (shipsPair.getValue().cpaValue < valueCpaLimit && shipsPair.getValue().tcpaValue < valueTCpaLimit
                    && shipsPair.getValue().tcpaValue < 0) {
                double[] xyCoordinatesInCross1 =
                        getXYCoordinates(new DirectPosition2D(shipsPair.getValue().shipA.geoPosition.getLatitude(),
                                shipsPair.getValue().shipA.geoPosition.getLongitude()));

                Ellipse2D.Double shipLocationInCross1
                        = new Ellipse2D.Double(xyCoordinatesInCross1[0] - 0.025, xyCoordinatesInCross1[1] - 0.025, 0.05, 0.05);
                BasicStroke basicStroke1
                        = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                xyPlot.addAnnotation(new XYShapeAnnotation(shipLocationInCross1, basicStroke1, Color.DARK_GRAY, Color.DARK_GRAY));

                double[] xyCoordinatesInCross2 =
                        getXYCoordinates(new DirectPosition2D(shipsPair.getValue().shipB.geoPosition.getLatitude(),
                                shipsPair.getValue().shipB.geoPosition.getLongitude()));

                Ellipse2D.Double shipLocationInCross2
                        = new Ellipse2D.Double(xyCoordinatesInCross2[0] - 0.025, xyCoordinatesInCross2[1] - 0.025, 0.05, 0.05);
                BasicStroke basicStroke2
                        = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                xyPlot.addAnnotation(new XYShapeAnnotation(shipLocationInCross2, basicStroke2, Color.DARK_GRAY, Color.DARK_GRAY));
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

    private static final Map<Integer, ConflictShips> trialShipsPairInConflict = new HashMap<>();

    void getSafeCpa(String manouever, double cpa) {



        for (Map.Entry<Integer, ConflictShips> shipsPair
                : shipsPairInConflict.entrySet()) {
                if (shipsToEast.containsKey(shipsPair.getValue().shipA.mmsiNum)
                        && shipsToSouth.containsKey(shipsPair.getValue().shipB.mmsiNum)) {
                    getSafeConflict(shipsPair.getKey(), shipsPair.getValue().shipA, shipsPair.getValue().shipB, manouever, cpa);
                } else if (shipsToEast.containsKey(shipsPair.getValue().shipB.mmsiNum)
                        && shipsToSouth.containsKey(shipsPair.getValue().shipA.mmsiNum)) {
                    getSafeConflict(shipsPair.getKey(), shipsPair.getValue().shipB, shipsPair.getValue().shipA, manouever, cpa);
                } else if (shipsToNorth.containsKey(shipsPair.getValue().shipA.mmsiNum)
                        && shipsToEast.containsKey(shipsPair.getValue().shipB.mmsiNum)) {
                    getSafeConflict(shipsPair.getKey(), shipsPair.getValue().shipA, shipsPair.getValue().shipB, manouever, cpa);
                } else if (shipsToNorth.containsKey(shipsPair.getValue().shipB.mmsiNum)
                        && shipsToEast.containsKey(shipsPair.getValue().shipA.mmsiNum)) {
                    getSafeConflict(shipsPair.getKey(), shipsPair.getValue().shipB, shipsPair.getValue().shipA, manouever, cpa);
                } else {
                    ConflictShips conflictShips = new ConflictShips(shipsPair.getValue().cpaValue, shipsPair.getValue().tcpaValue,
                            shipsPair.getValue().cpaLocation, new ShipAis(shipsPair.getValue().shipA.mmsiNum, shipsPair.getValue().shipA.latitude,
                            shipsPair.getValue().shipA.longitude, shipsPair.getValue().shipA.hdg, shipsPair.getValue().shipA.speed, shipsPair.getValue().shipA.destination),
                            new ShipAis(shipsPair.getValue().shipB.mmsiNum, shipsPair.getValue().shipB.latitude, shipsPair.getValue().shipB.longitude,
                                    shipsPair.getValue().shipB.hdg, shipsPair.getValue().shipB.speed, shipsPair.getValue().shipB.destination),
                            shipsPair.getValue().position1Future, shipsPair.getValue().position2Future);
                    trialShipsPairInConflict.put(shipsPair.getKey(), conflictShips);
                }
            for (Map.Entry<Integer, ConflictShips> pair : trialShipsPairInConflict.entrySet()) {
                for (Map.Entry<Integer, ShipAis> shipMan : shipsManoeuvered.entrySet()) {
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

    private ConflictShips getSafeConflict(int key, ShipAis shipA, ShipAis shipB, String manouever, double cpa) {
        GeodeticCalculator geoCalc = ApplicationCPA.geoCalc;
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
        trialShipsPairInConflict.put(key, conflictShips);
        if (conflictShips.draw) {
            shipsManoeuvered.put(conflictShips.shipB.mmsiNum, conflictShips.shipB);
        }
        return conflictShips;
    }

    List<Integer> mmsilist = new ArrayList<>();

    private void updateTrial(double cpa) {
        double xValue = 0;
        double yValue = 0;
        for (ConflictShips shipsPair : trialShipsPairInConflict.values()) {
                if (shipsPair.cpaValue < cpa) {
                    continue;
                }
                if (!shipsPair.draw) {
                    continue;
                }
                double[] xyCoordinates = getXYCoordinates(shipsPair.cpaLocation);
                xValue = xyCoordinates[0];
                yValue = xyCoordinates[1];

                //lines of ships` paths
                double[] xyCoordinatesLine1 = getXYCoordinates(shipsPair.shipA.positionFuture);
                double xValueLine1 = xyCoordinatesLine1[0];
                double yValueLine1 = xyCoordinatesLine1[1];

                double[] xyCoordinatesLine2 = getXYCoordinates(shipsPair.shipB.positionFuture);
                double xValueLine2 = xyCoordinatesLine2[0];
                double yValueLine2 = xyCoordinatesLine2[1];

                double[] xyCoordinatesEnds1
                        = getXYCoordinatesEnds(xValueLine1, yValueLine1, shipsPair.shipA, shipsPair.shipA.hdg);
                double[] xyCoordinatesEnds2
                        = getXYCoordinatesEnds(xValueLine2, yValueLine2, shipsPair.shipB, shipsPair.shipB.hdg);

                //check if vessel in cross area
                boolean insideAreaShipA = BoundaryArea.insideArea(new Point2D.Double(shipsPair.shipA.geoPosition.getLatitude(),
                        shipsPair.shipA.geoPosition.getLongitude()), CrossAreaChart.crossAreaSouth);

                boolean insideAreaShipB = BoundaryArea.insideArea(new Point2D.Double(shipsPair.shipB.geoPosition.getLatitude(),
                        shipsPair.shipB.geoPosition.getLongitude()), CrossAreaChart.crossAreaSouth);

                XYLineAnnotation xyLineAnnotation1;
                if (insideAreaShipA) {
                    double[] xyCoordinatesInCross1 =
                            getXYCoordinates(new DirectPosition2D(shipsPair.shipA.geoPosition.getLatitude(),
                                    shipsPair.shipA.geoPosition.getLongitude()));

                    xyLineAnnotation1 = new XYLineAnnotation(xyCoordinatesInCross1[0], xyCoordinatesInCross1[1],
                            xValueLine1, yValueLine1, new BasicStroke(2f), Color.YELLOW);

                    Ellipse2D.Double shipLocationInCross1
                            = new Ellipse2D.Double(xyCoordinatesInCross1[0] - 0.025, xyCoordinatesInCross1[1] - 0.025, 0.05, 0.05);
                    BasicStroke basicStroke1
                            = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                    xyPlot.addAnnotation(new XYShapeAnnotation(shipLocationInCross1, basicStroke1, Color.DARK_GRAY, Color.DARK_GRAY));
                } else {
                    xyLineAnnotation1 = new XYLineAnnotation(xyCoordinatesEnds1[0], xyCoordinatesEnds1[1],
                            xValueLine1, yValueLine1, new BasicStroke(2f), Color.YELLOW);
                }
                XYLineAnnotation xyLineAnnotation2;
                if (insideAreaShipB) {
                    double[] xyCoordinatesInCross2 =
                            getXYCoordinates(new DirectPosition2D(shipsPair.shipB.geoPosition.getLatitude(),
                                    shipsPair.shipB.geoPosition.getLongitude()));

                    xyLineAnnotation2 = new XYLineAnnotation(xyCoordinatesInCross2[0], xyCoordinatesInCross2[1],
                            xValueLine2, yValueLine2, new BasicStroke(2f), Color.YELLOW);

                    Ellipse2D.Double shipLocationInCross2
                            = new Ellipse2D.Double(xyCoordinatesInCross2[0] - 0.025, xyCoordinatesInCross2[1] - 0.025, 0.05, 0.05);
                    BasicStroke basicStroke2
                            = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                    xyPlot.addAnnotation(new XYShapeAnnotation(shipLocationInCross2, basicStroke2, Color.DARK_GRAY, Color.DARK_GRAY));
                } else {
                    xyLineAnnotation2 = new XYLineAnnotation(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1],
                            xValueLine2, yValueLine2, new BasicStroke(2f), Color.YELLOW);
                }
     /*       trialXyAnnotationList.add(xyLineAnnotation1);
            trialXyAnnotationList.add(xyLineAnnotation2);*/
            xyPlot.addAnnotation(xyLineAnnotation1);
            xyPlot.addAnnotation(xyLineAnnotation2);
        }
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

