package de.jade.ecs.map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PolarPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;

public class CrossAreaChartDraw extends JFrame implements Runnable {
    public static Point2D.Double centerPoint = new Point2D.Double(53.93652, 7.696533);
//    private static final int N = 16;
    private static final String title = "South Cross Area";
    private XYSeries chartSouth = new XYSeries("Conflict Ships");
/*    //horizontal axe
    static final double x1 = 53.9045;
    static final double y1 = 7.6515;
    static final double x2 = 53.9208;
    static final double y2 = 7.7681;
    static final double xx = x2 - x1;
    static final double yy = y2 - y1;
    //vertical axe
    static final double x1v = 53.95167;
    static final double y1v = 7.615;
    static final double x2v = 53.9045;
    static final double y2v = 7.6515;
    static final double xxv = x2v - x1v;
    static final double yyv = y2v - y1v;*/

/*    {
        Vector2D vectorLowerBorder = Vector2D.create(new CoordinateXY(53.89917, 7.616008),
                new CoordinateXY(53.9208, 7.7681));
    }*/

    public CrossAreaChartDraw(String s) {
        super(s);
        update();
        final ChartPanel chartPanel = createChartPanel();
        this.add(chartPanel, BorderLayout.CENTER);
        JPanel control = new JPanel();
        control.add(new JButton(new AbstractAction("Add Conflicts") {
            @Override
            public void actionPerformed(ActionEvent e) {
                chartSouth.clear();
                update();
            }
        }));
        this.add(control, BorderLayout.SOUTH);
    }

    private ChartPanel createChartPanel() {
        JFreeChart jfreechart = ChartFactory.createScatterPlot(
                title, "X", "Y", createChartData(),
                PlotOrientation.VERTICAL, true, true, false);
        XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
        XYItemRenderer renderer = xyPlot.getRenderer();
        NumberAxis domain = (NumberAxis) xyPlot.getDomainAxis();
        domain.setRange(-2.5, 2.5);
        domain.setTickUnit(new NumberTickUnit(1));
        NumberAxis range = (NumberAxis) xyPlot.getRangeAxis();
        range.setRange(-1.5, 1.5);
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

    private void update() {
//        chartSouth.add(new XYDataItem(0,0));
        for (ConflictShips ships : CrossAreaChart.shipsConflictsInCrossAreaSouth.values()) {
            double x_cpaLocation = ships.cpaLocation.getCoordinate()[0];
            double y_cpaLocation = ships.cpaLocation.getCoordinate()[1];
            ApplicationCPA.geoCalc.setStartGeographicPoint(centerPoint.x, centerPoint.y);
            ApplicationCPA.geoCalc.setEndGeographicPoint(x_cpaLocation, y_cpaLocation);
            double startingAzimuth = ApplicationCPA.geoCalc.getStartingAzimuth();
            if (startingAzimuth < 0) {
                startingAzimuth = 360 + startingAzimuth;
            }
            double correctedAzimuth = startingAzimuth + 24.0;
            double angle = correctedAzimuth % 90;
            double geodesicDistance = ApplicationCPA.geoCalc.getGeodesicDistance();
            double horizontalValue = geodesicDistance / 1852 * Math.abs(Math.sin(angle));
            if (correctedAzimuth > 180) {
                horizontalValue = 0 - horizontalValue;
            }
            double verticalValue = geodesicDistance / 1852 * Math.abs(Math.cos(angle));
            if (correctedAzimuth > 90 && correctedAzimuth < 180) {
                verticalValue = 0 - verticalValue;
            }
            chartSouth.add(new XYDataItem(horizontalValue,verticalValue));
            System.out.println("ee");
        }
    }



/*    private void update() {
        for (ConflictShips ships : CrossAreaChart.shipsConflictsInCrossAreaSouth.values()) {
            double x3 = ships.cpaLocation.getCoordinate()[0];
            double y3 = ships.cpaLocation.getCoordinate()[1];
            Double chartY = ((xx * (x3 - x1)) + (yy * (y3 - y1))) / ((xx * xx) + (yy * yy));
            Double chartX = ((xxv * (x3 - x1v)) + (yyv * (y3 - y1v))) / ((xxv * xxv) + (yyv * yyv));
            chartSouth.add(new XYDataItem(chartX,chartY));
        }
    }*/

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
