package de.jade.ecs.map;

import de.jade.ecs.map.shipchart.ShipInter;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class ShipAis implements ShipInter {
    int mmsiNum;
    double latitude;
    double longitude;
    public static HashMap<Integer, ShipAis> shipsAisHashMap = new HashMap<>();
    GeoPosition geoPosition;



    public ShipAis() {
    }

    public ShipAis(int mmsiNum, double latitude, double longitude) {
        this.mmsiNum = mmsiNum;
        this.latitude = latitude;
        this.longitude = longitude;
        geoPosition = new GeoPosition(latitude, longitude);
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
            chartViewerObj.addShipPainter();
        }
    }

    @Override
    public GeoPosition getPosition() {
        return geoPosition;
    }
}



