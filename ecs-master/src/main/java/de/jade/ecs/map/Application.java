package de.jade.ecs.map;

import java.io.FileInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import s57.S57dec;
import s57.S57map;

public class Application {
	public static void main(String[] args) {

		ExecutorService executor = Executors.newFixedThreadPool(10);
		TimeClient aisServerClient = new TimeClient();
		executor.execute(aisServerClient);
		
		try {
			FileInputStream in = new FileInputStream("D:\\U37IL137\\U37IL137.000");
			S57map s57map = new S57map(true);
			S57dec.decodeChart(in, s57map);
//			ArrayList<Feature> list = s57map.features.get(Obj.SOUNDG);
			ChartViewer chartViewer = new ChartViewer();
			chartViewer.show();
			
			while (true) {
			chartViewer.addPointPainter(aisServerClient.shiplat, aisServerClient.shiplong);	
			}
			
 // 		chartViewer.addSeachart(s57map);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
// AIS input
		
		
		
		
		
	}
}
