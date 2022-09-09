package de.jade.ecs.map.grapchic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.geom.Ellipse2D;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class AnnotationTest {

    private static final BasicStroke stroke = new BasicStroke(2.0f);
    private static final int N = 16;
    private static final int S = 8;

    public static void main(String[] args) {
        EventQueue.invokeLater(new AnnotationTest()::display);
    }

    private void display() {
        XYDataset data = createDataset();
        JFreeChart chart = ChartFactory.createXYLineChart("ArcTest", "X", "Y",
            data, PlotOrientation.VERTICAL, true, true, false);
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer
            = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setDefaultShapesVisible(true);
        for (int i = 0; i < N; i++) {
            double x = data.getXValue(0, i) - S / 2;
            double y = data.getYValue(0, i) - S / 2;
            Ellipse2D.Double ellipse = new Ellipse2D.Double(x, y, S, S);
            Color color = Color.getHSBColor((float) i / N, 1, 1);
            renderer.addAnnotation(new XYShapeAnnotation(ellipse, stroke, color));
        }
        ChartFrame frame = new ChartFrame("Test", chart);
        frame.pack();
        frame.setVisible(true);
    }

    private static XYDataset createDataset() {
        XYSeriesCollection result = new XYSeriesCollection();
        XYSeries series = new XYSeries("ArcTest");
        for (int i = 0; i < N; i++) {
            series.add(i * S, i * S);
        }
        result.addSeries(series);
        return result;
    }
}
