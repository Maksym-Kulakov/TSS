package de.jade.ecs.map;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.sun.jdi.IntegerType;
import de.jade.ecs.map.riskassessment.TcpaCalculation;
import org.apache.commons.collections4.MultiMap;
import org.jxmapviewer.viewer.GeoPosition;
import s57.S57dec;
import s57.S57map;

import javax.xml.crypto.Data;

public class Application {
	public static void main(String[] args) {
		try {
		//	FileInputStream in = new FileInputStream("D:\\U37IL137\\U37IL137.000");
//			S57map s57map = new S57map(true);
//			S57dec.decodeChart(in, s57map);
//			ArrayList<Feature> list = s57map.features.get(Obj.SOUNDG);
			ShipAis shipAisObject = new ShipAis();
			ChartViewer chartViewer = new ChartViewer();
			chartViewer.show();
			ExecutorService executor = Executors.newFixedThreadPool(3);
			TimeClient aisServerClient = new TimeClient(chartViewer);
			executor.execute(aisServerClient);
			TcpaCalculation tcpaCalculation = new TcpaCalculation();
			ApplicationCPA applicationCPA = new ApplicationCPA(tcpaCalculation);
			executor.execute(applicationCPA);

			while(true){
				chartViewer.getJXMapViewer().updateUI();
				Thread.sleep(1000l);
//				ShipAis.shipsInsideAreaAisHashMap.values().stream()
//						.map(ShipAis::getMmsi).forEach(System.out::println);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
