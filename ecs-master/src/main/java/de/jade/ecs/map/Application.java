package de.jade.ecs.map;

import de.jade.ecs.map.xychart.DynamicSimulation;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Application {
	public static void main(String[] args) {
		try {
			ChartViewer chartViewer = new ChartViewer();
			chartViewer.show();
			ExecutorService executor = Executors.newFixedThreadPool(5);
			TimeClient aisServerClient = new TimeClient(chartViewer);
			executor.execute(aisServerClient);
			ApplicationCPA applicationCPA = new ApplicationCPA();
			executor.execute(applicationCPA);
			DynamicSimulation applicationCPADynamic = new DynamicSimulation();
			executor.execute(applicationCPADynamic);
			CrossAreaChartDraw crossAreaChartDraw = new CrossAreaChartDraw("crossArea");
			executor.execute(crossAreaChartDraw);

			while(true){
				chartViewer.getJXMapViewer().updateUI();
				Thread.sleep(1000l);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
