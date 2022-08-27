package de.jade.ecs.map;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Application {
	public static void main(String[] args) {
		try {
			ChartViewer chartViewer = new ChartViewer();
			chartViewer.show();
			ExecutorService executor = Executors.newFixedThreadPool(3);
			TimeClient aisServerClient = new TimeClient(chartViewer);
			executor.execute(aisServerClient);
			ApplicationCPA applicationCPA = new ApplicationCPA();
			executor.execute(applicationCPA);

			while(true){
				chartViewer.getJXMapViewer().updateUI();
				Thread.sleep(1000l);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
