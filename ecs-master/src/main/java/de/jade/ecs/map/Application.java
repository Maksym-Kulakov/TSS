package de.jade.ecs.map;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.sun.jdi.IntegerType;
import org.apache.commons.collections4.MultiMap;
import s57.S57dec;
import s57.S57map;

import javax.xml.crypto.Data;

public class Application {
	public static void main(String[] args) {

		ExecutorService executor = Executors.newFixedThreadPool(100);
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
				ShipAis shipAis = new ShipAis(aisServerClient.mmsi, aisServerClient.shiplat, aisServerClient.shiplong);
		 		ShipAis.addShipsOnMap(ShipAis.shipsAisHashMap);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
