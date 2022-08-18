package de.jade.ecs.map.plotchart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.Random;


public class DynamicPlotAddition extends JFrame {

    private static final String title = "Dynamic Point Addition";
    private final Random rand = new Random();
    private XYSeries series = new XYSeries("Added");

    public DynamicPlotAddition(String s) {
        super(s);
        final ChartPanel chartPanel = createDemoPanel();
        this.add(chartPanel, BorderLayout.CENTER);
        Runnable runner = new Runnable(){

            @Override
            public void run() {
                int total = 1000;
                int iter = 0;
                while ( iter++ < total ){
                    SwingUtilities.invokeLater(new Runnable(){

                        @Override
                        public void run() {
                            series.add(rand.nextGaussian(), rand.nextGaussian());
                        }

                    });

                    try{Thread.sleep(100);}catch(Exception e){}
                }
            }

        };
        new Thread(runner).start();
    }

    private ChartPanel createDemoPanel() {
        JFreeChart jfreechart = ChartFactory.createScatterPlot(
            title, "X", "Y", createDataset(),
            PlotOrientation.VERTICAL, true, true, false);
        XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
        xyPlot.setDomainCrosshairVisible(true);
        xyPlot.setRangeCrosshairVisible(true);
        XYItemRenderer renderer = xyPlot.getRenderer();
        renderer.setSeriesPaint(0, Color.blue);
        NumberAxis domain = (NumberAxis) xyPlot.getDomainAxis();
        domain.setVerticalTickLabels(true);
        return new ChartPanel(jfreechart);
    }

    private XYDataset createDataset() {
        XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
        xySeriesCollection.addSeries(series);
        return xySeriesCollection;
    }

    public static void main(String args[]) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                DynamicPlotAddition demo = new DynamicPlotAddition(title);
                demo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                demo.pack();
                demo.setLocationRelativeTo(null);
                demo.setVisible(true);
            }
        });
    }
}
