package de.jade.ecs.map;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class ShipAis {
    int mmsiNum;
    double latitude;
    double longitude;
    public static HashMap<Integer, ShipAis> shipsAisHashMap = new HashMap<>();
    public ArrayList<Painter<JXMapViewer>> paintersList;

    public ShipAis() {
    }

    public ShipAis(int mmsiNum, double latitude, double longitude) {
        this.mmsiNum = mmsiNum;
        this.latitude = latitude;
        this.longitude = longitude;
        addShipToAisHashMap(this);
    }

    public void addShipToAisHashMap(ShipAis ship) {
        if (shipsAisHashMap.containsKey(ship.mmsiNum)) {
            shipsAisHashMap.replace(ship.mmsiNum, shipsAisHashMap.get(mmsiNum), ship);
        }
        shipsAisHashMap.put(ship.mmsiNum, ship);
    }

    public void addShipsOnMap(ChartViewer chartViewerObj, HashMap<Integer, ShipAis> values) {
        for (Entry<Integer, ShipAis> value : values.entrySet()) {
            chartViewerObj.addShipPainter(new GeoPosition(value.getValue().latitude, value.getValue().longitude));
        }
    }
}



