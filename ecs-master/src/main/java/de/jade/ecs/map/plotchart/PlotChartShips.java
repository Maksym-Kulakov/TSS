package de.jade.ecs.map.plotchart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.UIUtils;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PlotChartShips extends ApplicationFrame implements ActionListener {
    private final TimeSeries series;
    private final TimeSeries secondSeries;
    private final TimeSeries thirdSeries;
    private double lastValue = 100.0;
    private double lastValue2 = 100.0;
    private double lastValue3 = 100.0;


    public PlotChartShips(final String title) {
        super(title);
        this.series = new TimeSeries("Random Data 1");
        this.secondSeries = new TimeSeries("Random Data 2");
        this.thirdSeries = new TimeSeries("Random Data 3");

        JFreeChart chart = createChart();

        final JPanel content = new JPanel(new BorderLayout());
        final ChartPanel chartPanel = new ChartPanel(chart);
        content.add(chartPanel);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 500));
        setContentPane(content);
        Timer timer = new Timer((1000), this);
        timer.start();

    }

    private XYDataset createDataset(TimeSeries series) {
        return new TimeSeriesCollection(series);
    }

    private void firstTimeSeries(final XYPlot plot) {
        final ValueAxis xaxis = plot.getDomainAxis();
        xaxis.setAutoRange(true);

        // Domain axis would show data of 60 seconds for a time
        xaxis.setFixedAutoRange(60000.0); // 60 seconds
        xaxis.setVerticalTickLabels(true);

        final ValueAxis yaxis = plot.getRangeAxis();
        yaxis.setRange(0.0, 300.0);

        final XYItemRenderer renderer = plot.getRenderer();
        renderer.setSeriesPaint(0, Color.RED);

        final NumberAxis yAxis1 = (NumberAxis) plot.getRangeAxis();
        yAxis1.setTickLabelPaint(Color.RED);
    }

    private void secondTimeSeries(final XYPlot plot) {
        final XYDataset secondDataset = this.createDataset(secondSeries);
        plot.setDataset(1, secondDataset); // the second dataset (datasets are zero-based numbering)
        plot.mapDatasetToDomainAxis(1, 0); // same axis, different dataset
        plot.mapDatasetToRangeAxis(1, 0); // same axis, different dataset

        final XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setSeriesPaint(0, Color.BLUE);
        plot.setRenderer(1, renderer);
    }

    private void thirdTimeSeries(final XYPlot plot) {
        final XYDataset thirdDataset = this.createDataset(thirdSeries);
        plot.setDataset(2, thirdDataset); // the third dataset (datasets are zero-based numbering)
        plot.mapDatasetToDomainAxis(2, 0); // same axis, different dataset
        plot.mapDatasetToRangeAxis(2, 0); // same axis, different dataset

        final XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setSeriesPaint(0, Color.GREEN);
        plot.setRenderer(2, renderer);
    }

    private JFreeChart createChart() {
        final XYDataset dataset = this.createDataset(series);
        final JFreeChart result = ChartFactory.createTimeSeriesChart("Dynamic Line And TimeSeries Chart", "Time", "Value", dataset, true, true, false);

        final XYPlot plot = result.getXYPlot();
        plot.setBackgroundPaint(new Color(0xffffe0));
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // first time series
        this.firstTimeSeries(plot);

        // second time series
        this.secondTimeSeries(plot);

        // third time series
        this.thirdTimeSeries(plot);

        return result;
    }

    private double generateValue(final double lastValue, final double factor) {
        final double result = lastValue * factor;
        if (result <= 0.0 || result >= 300.0) {
            return 100.00;
        }
        return result;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final double factor = 0.9 + 0.2 * Math.random();
        lastValue = this.generateValue(lastValue, factor);
        series.add(new Millisecond(), lastValue);

        final double factor2 = 0.9 + 0.2 * Math.random();
        lastValue2 = this.generateValue(lastValue2, factor2);
        secondSeries.add(new Millisecond(), lastValue2);

        final double factor3 = 0.9 + 0.2 * Math.random();
        lastValue3 = this.generateValue(lastValue3, factor3);
        thirdSeries.add(new Millisecond(), lastValue3);
    }

    public static void main(String[] args) {
        final PlotChartShips plotChartShips = new PlotChartShips("Dynamic Ships Data");
        plotChartShips.pack();
        UIUtils.centerFrameOnScreen(plotChartShips);
        plotChartShips.setVisible(true);
    }
}
