package de.jade.ecs.map;

import de.jade.ecs.map.geochart.AlterationsOfCourse;
import de.jade.ecs.map.geochart.DestinationsToEast;
import de.jade.ecs.map.geochart.DestinationsToSouth;
import de.jade.ecs.map.geochart.GeoTssAreas;
import de.jade.ecs.map.shipchart.TssArea;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.util.ShapeUtilities;
import org.jxmapviewer.viewer.GeoPosition;
import org.opengis.geometry.DirectPosition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CrossAreaChartDraw extends ApplicationFrame implements Runnable {
    public static Point2D.Double centerPoint = new Point2D.Double(53.93652, 7.696533);
    private static final String title = "South Cross Area";
    private XYSeries chartSouth = new XYSeries("Conflict Ships");
    private static List<ConflictShips> shipsPairInConflict = new ArrayList<>();
    XYPlot xyPlot;
    XYTextAnnotation textAnnotation;

    public CrossAreaChartDraw(String s) {
        super(s);
//        update();
        final ChartPanel chartPanel = createChartPanel();
        this.add(chartPanel, BorderLayout.CENTER);
        JPanel control = new JPanel();
        control.add(new JButton(new AbstractAction("Add Conflicts") {
            @Override
            public void actionPerformed(ActionEvent e) {
                chartSouth.clear();
                xyPlot.clearAnnotations();
                update();
            }
        }));
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
     /*               if (shipsPairInConflict.get(shipsPairInConflict.size() - col - 1).cpaValue < 0.2) {
                        return ShapeUtilities.createDiagonalCross(9, 6);
                    }*/
                    Ellipse2D.Double shapeMark
                            = new Ellipse2D.Double(-(float) 0.25 * 115 / 2, -(float) 0.25 * 115 / 2,
                            (float) 0.25 * 115, (float) 0.25 * 115);

                    return shapeMark;
 /*                   Ellipse2D.Double shapeMark
                            = new Ellipse2D.Double(-(float) cpaValue / 2, -(float) cpaValue / 2,
                            (float) cpaValue, (float) cpaValue);
                    return shapeMark;*/
                } else {
                    return super.getItemShape(row, col);
                }
            }
        });
        XYLineAndShapeRenderer renderer
                = (XYLineAndShapeRenderer) jfreechart.getXYPlot().getRenderer();
//        renderer.setDefaultShapesFilled(true);
        renderer.setUseFillPaint(true);
//        renderer.setDrawOutlines(true);

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

    private double[] getXYCoordinatesEnds(double xCpaLocation, double yCpaLocation,
                                          ShipAis shipAis, double hdg) {
        double xValueEnd = 0;
        double yValueEnd = 0;
        hdg = hdg + 14;
        double angle = 0;
 /*       Point2D.Double geoPoint = new Point2D.Double(geoPosition.getLatitude(),
                geoPosition.getLongitude());*/
        if (shipAis.geoTssArea == GeoTssAreas.TO_EAST) {
            xValueEnd = -2.6;
            if (hdg == 90) {
                yValueEnd = yCpaLocation;
            } else if (hdg < 90) {
                double correction = (2.6 - xCpaLocation) / Math.abs(Math.tan(angle * Math.PI / 180));
                yValueEnd = yCpaLocation - correction;
            } else if (hdg > 90) {
                angle = 180 - hdg;
                double correction = (2.6 - xCpaLocation) / Math.abs(Math.tan(angle * Math.PI / 180));
                yValueEnd = yCpaLocation + correction;
            }
        } else if (shipAis.geoTssArea == GeoTssAreas.TO_SOUTH) {
            yValueEnd = 1.6;
            if (hdg == 360) {
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
            } else if (hdg > 0) {
                angle = 90 - hdg;
                double correction = (1.6 - yCpaLocation) / Math.abs(Math.tan(angle * Math.PI / 180));
                xValueEnd = xCpaLocation - correction;
            } else if (hdg < 360) {
                angle = hdg - 270;
                double correction = Math.abs(-1.6 - yCpaLocation) / Math.abs(Math.tan(angle * Math.PI / 180));
                xValueEnd = xCpaLocation + correction;
            }
        }
        return new double[]{xValueEnd, yValueEnd};
    }

    private void update() {
        double xValue = 0;
        double yValue = 0;
        for (ConflictShips shipsPair : CrossAreaChart.shipsConflictsInCrossAreaSouth.values()) {
            shipsPairInConflict.add(shipsPair);
            double[] xyCoordinates = getXYCoordinates(shipsPair.cpaLocation);
            xValue = xyCoordinates[0];
            yValue = xyCoordinates[1];

            System.out.println("shipsConflicts updated");
            textAnnotation = new XYTextAnnotation(String.valueOf((double) Math.round(shipsPair.cpaValue * 100) / 100),
                    xValue, yValue);
            if (shipsPair.tcpaValue < 6.0) {
                textAnnotation.setPaint(Color.red);
            } else {
                textAnnotation.setPaint(Color.black);
            }
            textAnnotation.setFont(new Font("Tahoma", Font.BOLD, 10));


            double[] xyCoordinatesLine1 = getXYCoordinates(shipsPair.position1Future);
            double xValueLine1 = xyCoordinatesLine1[0];
            double yValueLine1 = xyCoordinatesLine1[1];

            double[] xyCoordinatesLine2 = getXYCoordinates(shipsPair.position2Future);
            double xValueLine2 = xyCoordinatesLine2[0];
            double yValueLine2 = xyCoordinatesLine2[1];

            double[] xyCoordinatesEnds1 = getXYCoordinatesEnds(xValueLine1, yValueLine1, shipsPair.shipA, shipsPair.shipA.hdg);
            double[] xyCoordinatesEnds2 = getXYCoordinatesEnds(xValueLine2, yValueLine2, shipsPair.shipB, shipsPair.shipB.hdg);

            XYLineAnnotation xyLineAnnotation1 = new XYLineAnnotation(xyCoordinatesEnds1[0], xyCoordinatesEnds1[1], xValueLine1, yValueLine1, new BasicStroke(2f), Color.white);
            XYLineAnnotation xyLineAnnotation2 = new XYLineAnnotation(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1], xValueLine2, yValueLine2, new BasicStroke(2f), Color.white);

         /*   xyPlot.addAnnotation(textAnnotation);*/
            xyPlot.addAnnotation(xyLineAnnotation1);
            xyPlot.addAnnotation(xyLineAnnotation2);


            XYLineAnnotation xyLineAnnotationTurnA = null;
            XYLineAnnotation xyLineAnnotationTurnB = null;

            if (findIntentionOfCourseAlteration(shipsPair.shipA) == AlterationsOfCourse.PORT
                    && shipsPair.shipA.geoTssArea == GeoTssAreas.TO_SOUTH) {
                xyLineAnnotationTurnA = new XYLineAnnotation(xyCoordinatesEnds1[0] + 0.01, xyCoordinatesEnds1[1], xValueLine1  + 0.01, yValueLine1, new BasicStroke(1f), Color.red);
            }
            if (findIntentionOfCourseAlteration(shipsPair.shipA) == AlterationsOfCourse.STARBOARD
                    && shipsPair.shipA.geoTssArea == GeoTssAreas.TO_EAST) {
                xyLineAnnotationTurnA = new XYLineAnnotation(xyCoordinatesEnds1[0], xyCoordinatesEnds1[1]  - 0.01, xValueLine1, yValueLine1  - 0.01, new BasicStroke(1f), Color.green);
            }
            if (findIntentionOfCourseAlteration(shipsPair.shipB) == AlterationsOfCourse.PORT
                    && shipsPair.shipB.geoTssArea == GeoTssAreas.TO_SOUTH) {
                xyLineAnnotationTurnB = new XYLineAnnotation(xyCoordinatesEnds2[0] + 0.01, xyCoordinatesEnds2[1], xValueLine2  + 0.01, yValueLine2, new BasicStroke(1f), Color.red);
            }
            if (findIntentionOfCourseAlteration(shipsPair.shipB) == AlterationsOfCourse.STARBOARD
                    && shipsPair.shipB.geoTssArea == GeoTssAreas.TO_EAST) {
                xyLineAnnotationTurnB = new XYLineAnnotation(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1]  - 0.01, xValueLine2, yValueLine2  - 0.01, new BasicStroke(1f), Color.green);
            }
            xyPlot.addAnnotation(xyLineAnnotationTurnA);
            xyPlot.addAnnotation(xyLineAnnotationTurnB);

            /*chartSouth.add(new XYDataItem(xValue, yValue));*/

            Ellipse2D.Double cpaLocationShape = new Ellipse2D.Double(xValue - 0.15, yValue - 0.15, 0.3, 0.3);
            BasicStroke basicStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);

            xyPlot.addAnnotation(new XYShapeAnnotation(cpaLocationShape, basicStroke, Color.WHITE, Color.RED));

            xyPlot.addAnnotation(textAnnotation);

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

