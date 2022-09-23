package de.jade.ecs.map;

import de.jade.ecs.map.shipchart.TssArea;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
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
                            = new Ellipse2D.Double(-(float) cpaValue / 2, -(float) cpaValue / 2,
                            (float) cpaValue, (float) cpaValue);
                    return shapeMark;
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
        return new ChartPanel(jfreechart){
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

    private Shape createCircle(double size){
        return new Ellipse2D.Double(-size/2,-size/2,size,size);
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
                                          GeoPosition geoPosition, double hdg) {
        double xValueEnd = 0;
        double yValueEnd = 0;
        hdg = hdg + 14;
        double angle = 0;
        Point2D.Double geoPoint = new Point2D.Double(geoPosition.getLatitude(),
                geoPosition.getLongitude());
        if (BoundaryArea.insideArea(geoPoint, TssArea.trafficLineToEast)) {
            xValueEnd = - 2.6;
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
        } else if (BoundaryArea.insideArea(geoPoint, TssArea.trafficLineToSouth)) {
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
        } else if (BoundaryArea.insideArea(geoPoint, TssArea.trafficLineToNorth)) {
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
            chartSouth.add(new XYDataItem(xValue, yValue));
            System.out.println("shipsConflicts updated");
            textAnnotation = new XYTextAnnotation(String.valueOf(Math.round(shipsPair.tcpaValue)),
                    xValue + shipsPair.cpaValue, yValue + shipsPair.cpaValue);
            if (shipsPair.tcpaValue < 6.0) {
                textAnnotation.setPaint(Color.red);
            } else {
                textAnnotation.setPaint(Color.black);
            }
            textAnnotation.setFont(new Font("Tahoma", Font.BOLD, 10));
            xyPlot.addAnnotation(textAnnotation);

            double[] xyCoordinatesLine1 = getXYCoordinates(shipsPair.position1Future);
            double xValueLine1 = xyCoordinatesLine1[0];
            double yValueLine1 = xyCoordinatesLine1[1];

            double[] xyCoordinatesLine2 = getXYCoordinates(shipsPair.position2Future);
            double xValueLine2 = xyCoordinatesLine2[0];
            double yValueLine2 = xyCoordinatesLine2[1];

            double[] xyCoordinatesEnds1 = getXYCoordinatesEnds(xValueLine1, yValueLine1, shipsPair.shipA.geoPosition, shipsPair.shipA.hdg);
            double[] xyCoordinatesEnds2 = getXYCoordinatesEnds(xValueLine2, yValueLine2, shipsPair.shipB.geoPosition, shipsPair.shipB.hdg);

            XYLineAnnotation xyLineAnnotation1 = new XYLineAnnotation(xyCoordinatesEnds1[0],xyCoordinatesEnds1[1],xValueLine1,yValueLine1);
            XYLineAnnotation xyLineAnnotation2 = new XYLineAnnotation(xyCoordinatesEnds2[0],xyCoordinatesEnds2[1],xValueLine2,yValueLine2);

            xyPlot.addAnnotation(textAnnotation);
            xyPlot.addAnnotation(xyLineAnnotation1);
            xyPlot.addAnnotation(xyLineAnnotation2);
        }
    }

    public void run() {
        EventQueue.invokeLater(() -> {
            CrossAreaChartDraw chart = new CrossAreaChartDraw("shipsConflicts");
            chart.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            chart.pack();
            chart.setLocationRelativeTo(null);
            chart.setVisible(true);
        });
    }
}
