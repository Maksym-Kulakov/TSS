package de.jade.ecs.map.plotchart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;

public class JFreeChartLineChartExample extends JFrame {

   private static final long serialVersionUID = 1L;

   public JFreeChartLineChartExample(String applicationTitle, String chartTitle) {
        super(applicationTitle);

        // based on the dataset we create the chart
        JFreeChart pieChart = ChartFactory.createXYLineChart(chartTitle, "Category", "Score", createDataset(),PlotOrientation.VERTICAL, true, true, false);

        // Adding chart into a chart panel
        ChartPanel chartPanel = new ChartPanel(pieChart);
      
        // settind default size
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
      
        // add to contentPane
        setContentPane(chartPanel);
    }
  
   private XYDataset createDataset() {
     
      final XYSeries firefox = new XYSeries("Firefox");
      firefox.add(1.0, 1.0);
      firefox.add(2.0, 3.0);
      firefox.add(3.0, 4.0);
     
      final XYSeries chrome = new XYSeries("Chrome");
      chrome.add(1.0, 4.0);
      chrome.add(2.0, 6.0);
      chrome.add(3.0, 5.0);
    
     
      final XYSeries iexplorer = new XYSeries("InternetExplorer");
      iexplorer.add(3.0, 4.0);
      iexplorer.add(4.0, 5.0);
      iexplorer.add(5.0, 4.0);
     
     
      final XYSeriesCollection dataset = new XYSeriesCollection();
      dataset.addSeries(firefox);
      dataset.addSeries(chrome);
      dataset.addSeries(iexplorer);
     
      return dataset;
     
  }

   public static void main(String[] args) {
      JFreeChartLineChartExample chart = new JFreeChartLineChartExample("Browser Usage Statistics", "Which Browser are you using?");
      chart.pack();
      chart.setVisible(true);
   }
}
