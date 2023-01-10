package de.jade.ecs.map.crossareachart;

import de.jade.ecs.map.ApplicationCPA;
import de.jade.ecs.map.BoundaryArea;
import de.jade.ecs.map.ConflictShips;
import de.jade.ecs.map.CrossAreaChart;
import de.jade.ecs.map.Intersection;
import de.jade.ecs.map.Manoueveres;
import de.jade.ecs.map.ShipAis;
import de.jade.ecs.map.Track;
import de.jade.ecs.map.geochart.AlterationsOfCourse;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CrossAreaChartDraw extends ApplicationFrame implements Runnable {
    public static Point2D.Double centerPoint = new Point2D.Double(53.93652, 7.696533);
    private static final String title = "South Cross Area";
    private XYSeries chartSouth = new XYSeries("Conflict Ships");
    public static Map<Integer, ConflictShips> shipsPairInConflict = new HashMap<>();
    public static Map<Integer, AlterationsOfCourse> shipsToSouth = new HashMap<>();
    public static Map<Integer, AlterationsOfCourse> shipsToEast = new HashMap<>();
    public static Map<Integer, AlterationsOfCourse> shipsToNorth = new HashMap<>();
    public static Map<Integer, ShipAis> shipsManoeuvered = new HashMap<>();
    public static final Map<Integer, ConflictShips> trialShipsPairInConflict = new HashMap<>();
    public XYPlot xyPlot;
    public static XYTextAnnotation textAnnotation;
    public static Set<Point2D.Double> directPositions;
    public List<XYAnnotation> xyAnnotationList;
    public List<XYAnnotation> trialXyAnnotationList;
    int account = 0;
    boolean safeCpaPushed = false;
    public JTextField jTextFieldHdgManoeuver = new JTextField(null, "120", 3);

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

        SpinnerModel spinnerModelShipSpeed = new SpinnerNumberModel(10, 0.1, 30, 1);
        JSpinner jSpinnerShipSpeed = new JSpinner(spinnerModelShipSpeed);

        SpinnerModel spinnerModelTrial = new SpinnerListModel(Manoueveres.values());
        JSpinner jSpinnerShipTrial = new JSpinner(spinnerModelTrial);

        SpinnerModel spinnerModelAllowCpa = new SpinnerNumberModel(0.4, 0.1, 1, 0.1);
        JSpinner jSpinnerShipAllowCpa = new JSpinner(spinnerModelAllowCpa);


        controlNorth.add(new JButton(new AbstractAction("SafeCpa") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String valueOfManoeuver = jSpinnerShipTrial.getValue().toString();
                double valueFilterCpa = Double.parseDouble(jSpinnerShipAllowCpa.getValue().toString());
                try {
                    TrialManouever.getSafeCpa(valueOfManoeuver, valueFilterCpa);
                } catch (CloneNotSupportedException ex) {
                    ex.printStackTrace();
                }
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
                jTextFieldHdgManoeuver.setText("123");
                xyAnnotationList = HighlighterUtil.highLightPair();
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

        JTextArea jTextAreaNm = new JTextArea("nm");
        controlNorth.add(jTextAreaNm);

        JTextArea jTextAreaTcpa = new JTextArea("tcpa <");
        controlNorth.add(jTextAreaTcpa);

        controlNorth.add(jSpinnerTcpa, BorderLayout.SOUTH);

        JTextArea jTextAreaMin = new JTextArea("min");
        controlNorth.add(jTextAreaMin);

        this.add(controlNorth, BorderLayout.NORTH);

        JTextArea jTextAreaTrial = new JTextArea("Trial");
        controlSouth.add(jTextAreaTrial);

        controlSouth.add(jSpinnerShipTrial, BorderLayout.SOUTH);

        JTextArea jTextAreaAllowCpa = new JTextArea("minCPA");
        controlSouth.add(jTextAreaAllowCpa);

        controlSouth.add(jSpinnerShipAllowCpa, BorderLayout.SOUTH);

        JTextArea jTextAreaShip = new JTextArea("Ship №:");
        controlSouth.add(jTextAreaShip);

        controlSouth.add(jSpinnerShip, BorderLayout.SOUTH);

        JTextArea jTextAreaShipHdg = new JTextArea("HDG:");
        controlSouth.add(jTextAreaShipHdg);

        controlSouth.add(jTextFieldHdgManoeuver, BorderLayout.SOUTH);

        JTextArea jTextAreaShipDegr = new JTextArea("degr");
        controlSouth.add(jTextAreaShipDegr);

        JTextArea jTextAreaShipSpeed = new JTextArea("SPD:");
        controlSouth.add(jTextAreaShipSpeed);

        controlSouth.add(jSpinnerShipSpeed, BorderLayout.SOUTH);

        JTextArea jTextAreaShipKn = new JTextArea("kn");
        controlSouth.add(jTextAreaShipKn);

        this.add(controlSouth, BorderLayout.SOUTH);
    }

    private ChartPanel createChartPanel() {
        JFreeChart jfreechart = ChartFactory.createScatterPlot(
                title, "X", "Y", createChartData(),
                PlotOrientation.VERTICAL, true, true, false);
        xyPlot = (XYPlot) jfreechart.getPlot();
        xyPlot.setRenderer(new XYLineAndShapeRenderer(false, true) {
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

    //ships in CPA position
    private void update(Double valueCpaLimit, Double valueTCpaLimit) {
        double xValue = 0;
        double yValue = 0;
        directPositions = new LinkedHashSet<>();

        for (Map.Entry<Integer, ConflictShips> shipsPair
                : CrossAreaChart.shipsConflictsInCrossAreaSouth.entrySet()) {
            //conflicts indication filtering by cpa value
            if (shipsPair.getValue().cpaValue < valueCpaLimit && shipsPair.getValue().tcpaValue < valueTCpaLimit
                    && shipsPair.getValue().tcpaValue > 0) {
                    shipsPairInConflict.put(shipsPair.getKey(), shipsPair.getValue());

                //to add ships to AreaDirections
                ShipIntentions.assignedDirectionShipA(shipsPair);
                ShipIntentions.assignedDirectionShipB(shipsPair);

                //to get X and Y of CPA Location for chart
                double[] xyCoordinates = XYCoordinatesUtil.getXYCoordinates(shipsPair.getValue().cpaLocation);
                xValue = xyCoordinates[0];
                yValue = xyCoordinates[1];

                //tcpa value in circle
                HighlighterUtil.madeTextAnnotationOfTcpaValue(xValue, yValue, shipsPair.getValue().tcpaValue);

                //to get X and Y of FuturePsn of shipA for chart
                double[] xyCoordinatesLine1 = XYCoordinatesUtil.getXYCoordinates(shipsPair.getValue().position1Future);
                double xValueLine1 = xyCoordinatesLine1[0];
                double yValueLine1 = xyCoordinatesLine1[1];
                directPositions.add(new Point2D.Double(xValueLine1, yValueLine1));

                //to get X and Y of FuturePsn of shipB for chart
                double[] xyCoordinatesLine2 = XYCoordinatesUtil.getXYCoordinates(shipsPair.getValue().position2Future);
                double xValueLine2 = xyCoordinatesLine2[0];
                double yValueLine2 = xyCoordinatesLine2[1];
                directPositions.add(new Point2D.Double(xValueLine2, yValueLine2));

                //to get X and Y of EndLinePsn of shipA for chart
                double[] xyCoordinatesEnds1
                        = XYCoordinatesUtil.getXYCoordinatesEnds(xValueLine1, yValueLine1, shipsPair.getValue().shipA, shipsPair.getValue().shipA.hdg);

                //to get X and Y of EndLinePsn of shipB for chart
                double[] xyCoordinatesEnds2
                        = XYCoordinatesUtil.getXYCoordinatesEnds(xValueLine2, yValueLine2, shipsPair.getValue().shipB, shipsPair.getValue().shipB.hdg);

                //check if vessel in cross area
                boolean insideAreaShipA = BoundaryArea.insideArea(new Point2D.Double(shipsPair.getValue().shipA.geoPosition.getLatitude(),
                        shipsPair.getValue().shipA.geoPosition.getLongitude()), CrossAreaChart.crossAreaSouth);

                boolean insideAreaShipB = BoundaryArea.insideArea(new Point2D.Double(shipsPair.getValue().shipB.geoPosition.getLatitude(),
                        shipsPair.getValue().shipB.geoPosition.getLongitude()), CrossAreaChart.crossAreaSouth);

                //make line of path
                XYLineAnnotation xyLineAnnotation1;
                //line building depends on if ship already in Cross Area (to replace EndPsn)
                if (insideAreaShipA) {
                    //to get X & Y Chart coordinates for shipA psn
                    double[] xyCoordinatesInCross1 =
                            XYCoordinatesUtil.getXYCoordinates(new DirectPosition2D(shipsPair.getValue().shipA.geoPosition.getLatitude(),
                                    shipsPair.getValue().shipA.geoPosition.getLongitude()));

                    //make Annotation line of path for ShipA, when inside Cross Area
                    xyLineAnnotation1 = new XYLineAnnotation(xyCoordinatesInCross1[0], xyCoordinatesInCross1[1],
                            xValueLine1, yValueLine1, new BasicStroke(2f), Color.white);

                    Ellipse2D.Double shipLocationInCross1
                            = new Ellipse2D.Double(xyCoordinatesInCross1[0] - 0.025, xyCoordinatesInCross1[1] - 0.025, 0.05, 0.05);
                    BasicStroke basicStroke1
                            = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                    xyPlot.addAnnotation(new XYShapeAnnotation(shipLocationInCross1, basicStroke1, Color.DARK_GRAY, Color.DARK_GRAY));
                    //updating EndPsn as ShipPsn in cross area for fwg TurnLines
                    xyCoordinatesEnds1 = xyCoordinatesInCross1;
                } else {
                    //make Annotation line of path for ShipA, when OUTside Cross Area
                    xyLineAnnotation1 = new XYLineAnnotation(xyCoordinatesEnds1[0], xyCoordinatesEnds1[1],
                            xValueLine1, yValueLine1, new BasicStroke(2f), Color.white);
                }

                //line building depends on if ship already in Cross Area (to replace EndPsn)
                XYLineAnnotation xyLineAnnotation2;
                if (insideAreaShipB) {
                    //to get X & Y Chart coordinates for shipB psn
                    double[] xyCoordinatesInCross2 =
                            XYCoordinatesUtil.getXYCoordinates(new DirectPosition2D(shipsPair.getValue().shipB.geoPosition.getLatitude(),
                                    shipsPair.getValue().shipB.geoPosition.getLongitude()));

                    //make Annotation line of path for ShipB, when inside Cross Area
                    xyLineAnnotation2 = new XYLineAnnotation(xyCoordinatesInCross2[0], xyCoordinatesInCross2[1],
                            xValueLine2, yValueLine2, new BasicStroke(2f), Color.white);

                    Ellipse2D.Double shipLocationInCross2
                            = new Ellipse2D.Double(xyCoordinatesInCross2[0] - 0.025, xyCoordinatesInCross2[1] - 0.025, 0.05, 0.05);
                    BasicStroke basicStroke2
                            = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                    xyPlot.addAnnotation(new XYShapeAnnotation(shipLocationInCross2, basicStroke2, Color.DARK_GRAY, Color.DARK_GRAY));
                    //updating EndPsn as ShipPsn in cross area for fwg TurnLines
                    xyCoordinatesEnds2 = xyCoordinatesInCross2;
                } else {
                    //make Annotation line of path for ShipB, when OUTside Cross Area
                    xyLineAnnotation2 = new XYLineAnnotation(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1],
                            xValueLine2, yValueLine2, new BasicStroke(2f), Color.white);
                }

                //DRAW lines of both Ships paths
                xyPlot.addAnnotation(xyLineAnnotation1);
                xyPlot.addAnnotation(xyLineAnnotation2);

                //DRAW green and red line to indicate ships port intentions
                XYLineAnnotation[] turnLines = ShipIntentions.getTurnLine(shipsPair, xValueLine1, yValueLine1,
                        xValueLine2, yValueLine2, xyCoordinatesEnds1, xyCoordinatesEnds2);
                if (turnLines[0] != null) {
                    xyPlot.addAnnotation(turnLines[0]);
                }
                if (turnLines[1] != null) {
                    xyPlot.addAnnotation(turnLines[1]);
                }

                //DRAW tcpa value in circle
                xyPlot.addAnnotation(textAnnotation);

                //DRAW circle for tcpa
                drawTcpaSmall(xyPlot, xValue, yValue, shipsPair.getValue().tcpaValue);

                //for period after Closing Situation, when ship is only one Point
            } else if (shipsPair.getValue().cpaValue < valueCpaLimit && shipsPair.getValue().tcpaValue < valueTCpaLimit
                    && shipsPair.getValue().tcpaValue < 0) {
                double[] xyCoordinatesInCross1 =
                        XYCoordinatesUtil.getXYCoordinates(new DirectPosition2D(shipsPair.getValue().shipA.geoPosition.getLatitude(),
                                shipsPair.getValue().shipA.geoPosition.getLongitude()));

                Ellipse2D.Double shipLocationInCross1
                        = new Ellipse2D.Double(xyCoordinatesInCross1[0] - 0.025, xyCoordinatesInCross1[1] - 0.025, 0.05, 0.05);
                BasicStroke basicStroke1
                        = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                //DRAW ship A as point only
                xyPlot.addAnnotation(new XYShapeAnnotation(shipLocationInCross1, basicStroke1, Color.DARK_GRAY, Color.DARK_GRAY));

                double[] xyCoordinatesInCross2 =
                        XYCoordinatesUtil.getXYCoordinates(new DirectPosition2D(shipsPair.getValue().shipB.geoPosition.getLatitude(),
                                shipsPair.getValue().shipB.geoPosition.getLongitude()));

                Ellipse2D.Double shipLocationInCross2
                        = new Ellipse2D.Double(xyCoordinatesInCross2[0] - 0.025, xyCoordinatesInCross2[1] - 0.025, 0.05, 0.05);
                BasicStroke basicStroke2
                        = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                //DRAW ship B as point only
                xyPlot.addAnnotation(new XYShapeAnnotation(shipLocationInCross2, basicStroke2, Color.DARK_GRAY, Color.DARK_GRAY));
            }

            //DRAW all points of ships for FuturePositions
            for (Point2D.Double directPosition : directPositions) {
                Ellipse2D.Double cpaLocationShapeEnd1
                        = new Ellipse2D.Double(directPosition.getX() - 0.015, directPosition.getY() - 0.015, 0.03, 0.03);
                BasicStroke basicStroke1
                        = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                xyPlot.addAnnotation(new XYShapeAnnotation(cpaLocationShapeEnd1, basicStroke1, Color.BLUE, Color.BLUE));
            }
        }
    }

    //ships in BCR position
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

                //to get X and Y of CPA Location for chart
                double[] xyCoordinates = XYCoordinatesUtil.getXYCoordinates(shipsPair.getValue().cpaLocation);
                xValue = xyCoordinates[0];
                yValue = xyCoordinates[1];

                //to add ships to AreaDirections
                ShipIntentions.assignedDirectionShipA(shipsPair);
                ShipIntentions.assignedDirectionShipB(shipsPair);

                //to get X & Y of FuturePsn of shipA for chart
                double[] xyCoordinatesLine1 = XYCoordinatesUtil.getXYCoordinates(shipsPair.getValue().position1Future);
                double xValueLine1 = xyCoordinatesLine1[0];
                double yValueLine1 = xyCoordinatesLine1[1];

                //to get X & Y of FuturePsn of shipB for chart
                double[] xyCoordinatesLine2 = XYCoordinatesUtil.getXYCoordinates(shipsPair.getValue().position2Future);
                double xValueLine2 = xyCoordinatesLine2[0];
                double yValueLine2 = xyCoordinatesLine2[1];

                //to get X & Y of EndLinePsn of shipA for chart
                double[] xyCoordinatesEnds1
                        = XYCoordinatesUtil.getXYCoordinatesEnds(xValueLine1, yValueLine1,
                        shipsPair.getValue().shipA, shipsPair.getValue().shipA.hdg);
                //to get X & Y of EndLinePsn of shipA for chart
                double[] xyCoordinatesEnds2
                        = XYCoordinatesUtil.getXYCoordinatesEnds(xValueLine2, yValueLine2,
                        shipsPair.getValue().shipB, shipsPair.getValue().shipB.hdg);

                //to calculate coordinates of prolonged point
                int distanceOfProlong = 4;

                //get Chart coordinates of prolonged path of shipA
                double[] xyCoordinatesProlonged1 = XYCoordinatesUtil.getProlongedPointCoordinates(xyCoordinatesEnds1[0], xyCoordinatesEnds1[1],
                        shipsPair.getValue().shipA, shipsPair.getValue().shipA.hdg, distanceOfProlong);

                //get Chart coordinates of prolonged path of shipB
                double[] xyCoordinatesProlonged2 = XYCoordinatesUtil.getProlongedPointCoordinates(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1],
                        shipsPair.getValue().shipB, shipsPair.getValue().shipB.hdg, distanceOfProlong);

                //to build prolonged line for shipA
                Line2D.Double line1 = new Line2D.Double(new Point2D.Double(xyCoordinatesEnds1[0], xyCoordinatesEnds1[1]),
                        new Point2D.Double(xyCoordinatesProlonged1[0], xyCoordinatesProlonged1[1]));
                //to build prolonged line for shipB
                Line2D.Double line2 = new Line2D.Double(new Point2D.Double(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1]),
                        new Point2D.Double(xyCoordinatesProlonged2[0], xyCoordinatesProlonged2[1]));

                //to get Chart coordinates of Crossed line
                Point2D.Double crossPoint = Intersection.intersection(line1, line2);

                if (crossPoint == null) {
                    continue;
                }

                //check if crossed Point located on path of shipA (check if this ship will be Bow-crosser)
                boolean checkIfPointInSegmentA = HighlighterUtil.isShipBowCrosser(crossPoint,
                        shipsPair, xValueLine1, yValueLine1);

                //flags for method isPeriodWithShipPsnBetweenBcrAndFuturePsn()
                boolean checkIfPointOutA = false;
                boolean checkIfPointOutB = false;

                //check if vessel in cross area
                boolean insideAreaShipA = BoundaryArea.insideArea(new Point2D.Double(shipsPair.getValue().shipA.geoPosition.getLatitude(),
                        shipsPair.getValue().shipA.geoPosition.getLongitude()), CrossAreaChart.crossAreaSouth);
                boolean insideAreaShipB = BoundaryArea.insideArea(new Point2D.Double(shipsPair.getValue().shipB.geoPosition.getLatitude(),
                        shipsPair.getValue().shipB.geoPosition.getLongitude()), CrossAreaChart.crossAreaSouth);

                XYLineAnnotation xyLineAnnotation1 = null;
                XYLineAnnotation xyLineAnnotation2 = null;

        //if shipA is Bow-Crosser
                if (checkIfPointInSegmentA) {
                    //calc dist for shipA from cross point to future position
                    double distFmBcrToEnd1 = Math.sqrt((yValueLine1 - crossPoint.getY()) * (yValueLine1 - crossPoint.getY())
                            + (xValueLine1 - crossPoint.getX()) * (xValueLine1 - crossPoint.getX()));
                    //calc dist for shipB from future position to new "future position"
                    double distFmBcrToEnd2 = distFmBcrToEnd1 * shipsPair.getValue().shipB.speed / shipsPair.getValue().shipA.speed;
                    //calc new tcpa value
                    double newTcpa = shipsPair.getValue().tcpaValue - distFmBcrToEnd1 / shipsPair.getValue().shipA.speed * 60;
                    HighlighterUtil.madeTextAnnotationOfTcpaValue(xValue, yValue, newTcpa);

                    //calc for shipB distance value from EndPsn to old future position
                    double distOfLine2 = Math.sqrt((yValueLine2 - xyCoordinatesEnds2[1]) * (yValueLine2 - xyCoordinatesEnds2[1])
                            + (xValueLine2 - xyCoordinatesEnds2[0]) * (xValueLine2 - xyCoordinatesEnds2[0]));

                    //find for shipB Chart coordinates for new "future position"
                    double[] otherBwrPoint = XYCoordinatesUtil.getProlongedPointCoordinates(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1],
                            shipsPair.getValue().shipB, shipsPair.getValue().shipB.hdg, distOfLine2 - distFmBcrToEnd2);

                    //ShipA: line building depends on if ship already in Cross Area (to replace EndPsn)
                    if (insideAreaShipA) {
                        //to get X & Y Chart coordinates for shipA psn
                        double[] xyCoordinatesInCross1 =
                                XYCoordinatesUtil.getXYCoordinates(new DirectPosition2D(shipsPair.getValue().shipA.geoPosition.getLatitude(),
                                        shipsPair.getValue().shipA.geoPosition.getLongitude()));
                        //make Annotation line of path for ShipA, when inside Cross Area
                        xyLineAnnotation1 = new XYLineAnnotation(xyCoordinatesInCross1[0], xyCoordinatesInCross1[1],
                                crossPoint.getX(), crossPoint.getY(), new BasicStroke(2f), Color.white);

                        //DRAW points of BCR point for shipA and newFuturePsn for shipB
                        Ellipse2D.Double shipLocationInCross1
                                = new Ellipse2D.Double(xyCoordinatesInCross1[0] - 0.025, xyCoordinatesInCross1[1] - 0.025, 0.05, 0.05);
                        BasicStroke basicStroke1
                                = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                        xyPlot.addAnnotation(new XYShapeAnnotation(shipLocationInCross1, basicStroke1, Color.DARK_GRAY, Color.DARK_GRAY));
                        //updating EndPsn as ShipPsn in cross area for fwg TurnLines
                        xyCoordinatesEnds1 = xyCoordinatesInCross1;

                        //catch moment when shipA will be between cross point and future position (tcpa still > 0)
                        //with using appr flag
                        checkIfPointOutA = HighlighterUtil.isPeriodWithShipPsnBetweenBcrAndFuturePsn(crossPoint,
                                shipsPair.getValue().shipA, xyCoordinatesInCross1);
                    } else {
                        //make Annotation line of path for ShipA, when OUTside Cross Area
                        xyLineAnnotation1 = new XYLineAnnotation(xyCoordinatesEnds1[0], xyCoordinatesEnds1[1],
                                crossPoint.getX(), crossPoint.getY(), new BasicStroke(2f), Color.white);
                    }
                    //ShipB: line building depends on if ship already in Cross Area (to replace EndPsn)
                    if (insideAreaShipB) {
                        //to get X & Y Chart coordinates for shipB psn
                        double[] xyCoordinatesInCross2 =
                                XYCoordinatesUtil.getXYCoordinates(new DirectPosition2D(shipsPair.getValue().shipB.geoPosition.getLatitude(),
                                        shipsPair.getValue().shipB.geoPosition.getLongitude()));

                        //make Annotation line of path for ShipB, when inside Cross Area
                        xyLineAnnotation2 = new XYLineAnnotation(xyCoordinatesInCross2[0], xyCoordinatesInCross2[1],
                                otherBwrPoint[0], otherBwrPoint[1], new BasicStroke(2f), Color.white);

                        Ellipse2D.Double shipLocationInCross2
                                = new Ellipse2D.Double(xyCoordinatesInCross2[0] - 0.025, xyCoordinatesInCross2[1] - 0.025, 0.05, 0.05);
                        BasicStroke basicStroke2
                                = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                        xyPlot.addAnnotation(new XYShapeAnnotation(shipLocationInCross2, basicStroke2, Color.DARK_GRAY, Color.DARK_GRAY));
                        //updating EndPsn as ShipPsn in cross area for fwg TurnLines
                        xyCoordinatesEnds2 = xyCoordinatesInCross2;

                        //catch moment when shipA will be between cross point and future position (tcpa still > 0)
                        //with using appr flag
                        checkIfPointOutB = HighlighterUtil.isPeriodWithShipPsnBetweenBcrAndFuturePsn(
                                new Point2D.Double(otherBwrPoint[0], otherBwrPoint[1]),
                                shipsPair.getValue().shipB, xyCoordinatesInCross2);
                    } else {
                        xyLineAnnotation2 = new XYLineAnnotation(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1],
                                otherBwrPoint[0], otherBwrPoint[1], new BasicStroke(2f), Color.white);
                    }

                    //check that both ships still before final BCR positions
                    if (!checkIfPointOutA && !checkIfPointOutB) {
                        directPositions.add(crossPoint);
                        directPositions.add(new Point2D.Double(otherBwrPoint[0], otherBwrPoint[1]));

                        //DRAW green and red line to indicate ships port intentions
                        XYLineAnnotation[] turnLines = ShipIntentions.getTurnLine(shipsPair, crossPoint.getX(), crossPoint.getY(),
                                otherBwrPoint[0], otherBwrPoint[1], xyCoordinatesEnds1, xyCoordinatesEnds2);
                        if (turnLines[0] != null) {
                            xyPlot.addAnnotation(turnLines[0]);
                        }
                        if (turnLines[1] != null) {
                            xyPlot.addAnnotation(turnLines[1]);
                        }
                    }
        //if ShipB is Bow-Crosser
                } else {
                    //calc dist for shipB from cross point to future position
                    double distFmBcrToEnd2 = Math.sqrt((yValueLine2 - crossPoint.getY()) * (yValueLine2 - crossPoint.getY())
                            + (xValueLine2 - crossPoint.getX()) * (xValueLine2 - crossPoint.getX()));
                    //calc dist for shipA from future position to new "future position"
                    double distFmBcrToEnd1 = distFmBcrToEnd2 * shipsPair.getValue().shipA.speed / shipsPair.getValue().shipB.speed;
                    //calc new tcpa value
                    double newTcpa = shipsPair.getValue().tcpaValue - distFmBcrToEnd1 / shipsPair.getValue().shipA.speed * 60;
                    HighlighterUtil.madeTextAnnotationOfTcpaValue(xValue, yValue, newTcpa);

                    //calc for shipA distance value from EndPsn to old future position
                    double distOfLine1 = Math.sqrt((yValueLine1 - xyCoordinatesEnds1[1]) * (yValueLine1 - xyCoordinatesEnds1[1])
                            + (xValueLine1 - xyCoordinatesEnds1[0]) * (xValueLine1 - xyCoordinatesEnds1[0]));

                    //find for shipA Chart coordinates for new "future position"
                    double[] otherBwrPoint = XYCoordinatesUtil.getProlongedPointCoordinates(xyCoordinatesEnds1[0], xyCoordinatesEnds1[1], shipsPair.getValue().shipA, shipsPair.getValue().shipA.hdg, distOfLine1 - distFmBcrToEnd1);

                    //ShipA: line building depends on if ship already in Cross Area (to replace EndPsn)
                    if (insideAreaShipA) {
                        //to get X & Y Chart coordinates for shipA psn
                        double[] xyCoordinatesInCross1 =
                                XYCoordinatesUtil.getXYCoordinates(new DirectPosition2D(shipsPair.getValue().shipA.geoPosition.getLatitude(),
                                        shipsPair.getValue().shipA.geoPosition.getLongitude()));
                        //make Annotation line of path for ShipA, when inside Cross Area
                        xyLineAnnotation1 = new XYLineAnnotation(xyCoordinatesInCross1[0], xyCoordinatesInCross1[1],
                                otherBwrPoint[0], otherBwrPoint[1], new BasicStroke(2f), Color.white);

                        //DRAW points of BCR point for shipB and newFuturePsn for shipA
                        Ellipse2D.Double shipLocationInCross1
                                = new Ellipse2D.Double(xyCoordinatesInCross1[0] - 0.025, xyCoordinatesInCross1[1] - 0.025, 0.05, 0.05);
                        BasicStroke basicStroke1
                                = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                        xyPlot.addAnnotation(new XYShapeAnnotation(shipLocationInCross1, basicStroke1, Color.DARK_GRAY, Color.DARK_GRAY));
                        //updating EndPsn as ShipPsn in cross area for fwg TurnLines
                        xyCoordinatesEnds1 = xyCoordinatesInCross1;

                        //catch moment when shipA will be between cross point and future position (tcpa still > 0)
                        //with using appr flag
                        checkIfPointOutA = HighlighterUtil.isPeriodWithShipPsnBetweenBcrAndFuturePsn(
                                new Point2D.Double(otherBwrPoint[0], otherBwrPoint[1]),
                                shipsPair.getValue().shipA, xyCoordinatesInCross1);
                    } else {
                        //make Annotation line of path for ShipA, when OUTside Cross Area
                        xyLineAnnotation1 = new XYLineAnnotation(xyCoordinatesEnds1[0], xyCoordinatesEnds1[1],
                                otherBwrPoint[0], otherBwrPoint[1], new BasicStroke(2f), Color.white);
                    }
                    //ShipB: line building depends on if ship already in Cross Area (to replace EndPsn)
                    if (insideAreaShipB) {
                        //to get X & Y Chart coordinates for shipB psn
                        double[] xyCoordinatesInCross2 =
                                XYCoordinatesUtil.getXYCoordinates(new DirectPosition2D(shipsPair.getValue().shipB.geoPosition.getLatitude(),
                                        shipsPair.getValue().shipB.geoPosition.getLongitude()));
                        //make Annotation line of path for ShipB, when inside Cross Area
                        xyLineAnnotation2 = new XYLineAnnotation(xyCoordinatesInCross2[0], xyCoordinatesInCross2[1],
                                crossPoint.getX(), crossPoint.getY(), new BasicStroke(2f), Color.white);

                        Ellipse2D.Double shipLocationInCross2
                                = new Ellipse2D.Double(xyCoordinatesInCross2[0] - 0.025, xyCoordinatesInCross2[1] - 0.025, 0.05, 0.05);
                        BasicStroke basicStroke2
                                = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                        xyPlot.addAnnotation(new XYShapeAnnotation(shipLocationInCross2, basicStroke2, Color.DARK_GRAY, Color.DARK_GRAY));
                        //updating EndPsn as ShipPsn in cross area for fwg TurnLines
                        xyCoordinatesEnds2 = xyCoordinatesInCross2;

                        //catch moment when shipA will be between cross point and future position (tcpa still > 0)
                        //with using appr flag
                        checkIfPointOutB = HighlighterUtil.isPeriodWithShipPsnBetweenBcrAndFuturePsn(
                                crossPoint, shipsPair.getValue().shipB, xyCoordinatesInCross2);
                    } else {
                        //make Annotation line of path for ShipB, when OUTside Cross Area
                        xyLineAnnotation2 = new XYLineAnnotation(xyCoordinatesEnds2[0], xyCoordinatesEnds2[1],
                                crossPoint.getX(), crossPoint.getY(), new BasicStroke(2f), Color.white);
                    }

                    //check that both ships still before final BCR positions and then DRAW TURN LINES
                    if (!checkIfPointOutA && !checkIfPointOutB) {

                        directPositions.add(crossPoint);
                        directPositions.add(new Point2D.Double(otherBwrPoint[0], otherBwrPoint[1]));

                        //DRAW green and red line to indicate ships port intentions
                        XYLineAnnotation[] turnLines = ShipIntentions.getTurnLine(shipsPair, otherBwrPoint[0], otherBwrPoint[1],
                                crossPoint.getX(), crossPoint.getY(), xyCoordinatesEnds1, xyCoordinatesEnds2);
                        if (turnLines[0] != null) {
                            xyPlot.addAnnotation(turnLines[0]);
                        }
                        if (turnLines[1] != null) {
                            xyPlot.addAnnotation(turnLines[1]);
                        }
                    }
                }

        //check that both ships still before final BCR positions and then DRAW lines
                if (!checkIfPointOutA && !checkIfPointOutB) {
                    xyPlot.addAnnotation(xyLineAnnotation1);
                    xyPlot.addAnnotation(xyLineAnnotation2);
                }

                //DRAW tcpa value in circle
                xyPlot.addAnnotation(textAnnotation);
                //DRAW circle for tcpa
                drawTcpaSmall(xyPlot, xValue, yValue, shipsPair.getValue().tcpaValue);
                //for period after Closing Situation, when ship is only one Point
            } else if (shipsPair.getValue().cpaValue < valueCpaLimit && shipsPair.getValue().tcpaValue < valueTCpaLimit
                    && shipsPair.getValue().tcpaValue < 0) {
                double[] xyCoordinatesInCross1 =
                        XYCoordinatesUtil.getXYCoordinates(new DirectPosition2D(shipsPair.getValue().shipA.geoPosition.getLatitude(),
                                shipsPair.getValue().shipA.geoPosition.getLongitude()));

                Ellipse2D.Double shipLocationInCross1
                        = new Ellipse2D.Double(xyCoordinatesInCross1[0] - 0.025, xyCoordinatesInCross1[1] - 0.025, 0.05, 0.05);
                BasicStroke basicStroke1
                        = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                //DRAW ship A as point only
                xyPlot.addAnnotation(new XYShapeAnnotation(shipLocationInCross1, basicStroke1, Color.DARK_GRAY, Color.DARK_GRAY));

                double[] xyCoordinatesInCross2 =
                        XYCoordinatesUtil.getXYCoordinates(new DirectPosition2D(shipsPair.getValue().shipB.geoPosition.getLatitude(),
                                shipsPair.getValue().shipB.geoPosition.getLongitude()));

                Ellipse2D.Double shipLocationInCross2
                        = new Ellipse2D.Double(xyCoordinatesInCross2[0] - 0.025, xyCoordinatesInCross2[1] - 0.025, 0.05, 0.05);
                BasicStroke basicStroke2
                        = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                //DRAW ship B as point only
                xyPlot.addAnnotation(new XYShapeAnnotation(shipLocationInCross2, basicStroke2, Color.DARK_GRAY, Color.DARK_GRAY));
            }

            //DRAW all points of ships for FuturePositions
            for (Point2D.Double directPosition : directPositions) {
                Ellipse2D.Double cpaLocationShapeEnd1
                        = new Ellipse2D.Double(directPosition.getX() - 0.015, directPosition.getY() - 0.015, 0.03, 0.03);
                BasicStroke basicStroke1
                        = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                xyPlot.addAnnotation(new XYShapeAnnotation(cpaLocationShapeEnd1, basicStroke1, Color.BLUE, Color.BLUE));
            }
        }
    }

    //DRAW tcpa circle
    private void drawTcpaSmall(XYPlot xyPlot, double xValue, double yValue, double tcpaValue) {
        Ellipse2D.Double cpaLocationShape1
                = new Ellipse2D.Double(xValue + 0.1645, yValue - 0.062, 0.15, 0.15);
        BasicStroke basicStroke
                = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
        xyPlot.addAnnotation(new XYShapeAnnotation(cpaLocationShape1, basicStroke, Color.GRAY, null));
    }

    //to DRAW trial paths of ships
    private void updateTrial(double cpa) {
        double xValue = 0;
        double yValue = 0;
        for (ConflictShips shipsPair : trialShipsPairInConflict.values()) {
/*            if (shipsPair.cpaValue < cpa) {
                continue;
            }*/
            if (!shipsPair.draw) {
                continue;
            }
            double[] xyCoordinates = XYCoordinatesUtil.getXYCoordinates(shipsPair.cpaLocation);
            xValue = xyCoordinates[0];
            yValue = xyCoordinates[1];

            //lines of ships` paths
            double[] xyCoordinatesLine1 = XYCoordinatesUtil.getXYCoordinates(shipsPair.position1Future);
            double xValueLine1 = xyCoordinatesLine1[0];
            double yValueLine1 = xyCoordinatesLine1[1];

            double[] xyCoordinatesLine2 = XYCoordinatesUtil.getXYCoordinates(shipsPair.position2Future);
            double xValueLine2 = xyCoordinatesLine2[0];
            double yValueLine2 = xyCoordinatesLine2[1];

            double[] xyCoordinatesEnds1
                    = XYCoordinatesUtil.getXYCoordinatesEnds(xValueLine1, yValueLine1, shipsPair.shipA, shipsPair.shipA.hdg);
            double[] xyCoordinatesEnds2
                    = XYCoordinatesUtil.getXYCoordinatesEnds(xValueLine2, yValueLine2, shipsPair.shipB, shipsPair.shipB.hdg);

            //check if vessel in cross area
            boolean insideAreaShipA = BoundaryArea.insideArea(new Point2D.Double(shipsPair.shipA.geoPosition.getLatitude(),
                    shipsPair.shipA.geoPosition.getLongitude()), CrossAreaChart.crossAreaSouth);

            boolean insideAreaShipB = BoundaryArea.insideArea(new Point2D.Double(shipsPair.shipB.geoPosition.getLatitude(),
                    shipsPair.shipB.geoPosition.getLongitude()), CrossAreaChart.crossAreaSouth);

            XYLineAnnotation xyLineAnnotation1;
            if (insideAreaShipA) {
                double[] xyCoordinatesInCross1 =
                        XYCoordinatesUtil.getXYCoordinates(new DirectPosition2D(shipsPair.shipA.geoPosition.getLatitude(),
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
                        XYCoordinatesUtil.getXYCoordinates(new DirectPosition2D(shipsPair.shipB.geoPosition.getLatitude(),
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

