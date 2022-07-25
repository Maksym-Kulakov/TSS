package de.jade.ecs.map;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.sun.jdi.IntegerType;
import org.apache.commons.collections4.MultiMap;
import org.jxmapviewer.viewer.GeoPosition;
import s57.S57dec;
import s57.S57map;

import javax.xml.crypto.Data;

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
			ShipAis shipAisObject = new ShipAis();
			ChartViewer chartViewer = new ChartViewer();
			chartViewer.show();

			while (true) {
				ShipAis shipAis = new ShipAis(aisServerClient.mmsi, aisServerClient.shiplat, aisServerClient.shiplong);
				shipAisObject.addShipsOnMap(chartViewer, ShipAis.shipsAisHashMap);
				System.out.println(ShipAis.shipsAisHashMap.get(211141000)); //to check that Map updates
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
