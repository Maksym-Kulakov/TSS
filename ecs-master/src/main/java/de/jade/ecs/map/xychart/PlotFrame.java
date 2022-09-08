package de.jade.ecs.map.xychart;

import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author tarik
 */
public class PlotFrame extends javax.swing.JFrame {
    public static void main(String[] argv) {
        PlotFrame plotFrame = new PlotFrame();
        plotFrame.setVisible(true);
    }


    /**
     * Creates new form PlotFrame
     */
    public PlotFrame() {
        this.setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JFreeChart chart = createChart();
        ChartPanel panel = new ChartPanel(chart);

        this.add(panel);
        pack();
    }

    private JFreeChart createChart() {
        // Create dataset  
        XYDataset dataset = createDataset();

        // Create chart  
        JFreeChart chart = ChartFactory.createScatterPlot(
                "Boys VS Girls weight comparison chart",
                "X-Axis", "Y-Axis", dataset);

        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
        renderer.setDefaultShapesFilled(false);
        renderer.setUseFillPaint(false);

        return chart;
    }

    private XYDataset createDataset() {
        XYSeriesCollection dataset = new XYSeriesCollection();

        //Boys (Age,weight) series  
        XYSeries series1 = new XYSeries("Boys");
        series1.add(1, 72.9);
        series1.add(2, 81.6);
        series1.add(3, 88.9);
        series1.add(4, 96);
        series1.add(5, 102.1);
        series1.add(6, 108.5);
        series1.add(7, 113.9);
        series1.add(8, 119.3);
        series1.add(9, 123.8);
        series1.add(10, 124.4);

        dataset.addSeries(series1);

        //Girls (Age,weight) series  
        XYSeries series2 = new XYSeries("Girls");
        series2.add(1, 72.5);
        series2.add(2, 80.1);
        series2.add(3, 87.2);
        series2.add(4, 94.5);
        series2.add(5, 101.4);
        series2.add(6, 107.4);
        series2.add(7, 112.8);
        series2.add(8, 118.2);
        series2.add(9, 122.9);
        series2.add(10, 123.4);

        dataset.addSeries(series2);

        return dataset;
    }
}
