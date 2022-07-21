package de.jade.ecs.map;

import dk.dma.enav.model.geometry.CoordinateSystem;
import dk.dma.enav.model.geometry.Element;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class ShipAis implements Element {
    int mmsiNum;
    double latitude;
    double longitude;
    static HashMap<Integer, ShipAis> shipsAisHashMap = new HashMap<>();

    public ShipAis(int mmsiNum, double latitude, double longitude) {
        this.mmsiNum = mmsiNum;
        this.latitude = latitude;
        this.longitude = longitude;
        addShipToAisHashMap(this);
    }

    public void addShipToAisHashMap (ShipAis ship) {
        if (shipsAisHashMap.containsKey(ship.mmsiNum)) {
            shipsAisHashMap.replace(ship.mmsiNum, shipsAisHashMap.get(mmsiNum), ship);
        }
        shipsAisHashMap.put(ship.mmsiNum, ship);
    }

    public static void addShipsOnMap(HashMap<Integer, ShipAis> values) throws InterruptedException {
        ChartViewer chartViewer = new ChartViewer();
        for (Entry<Integer, ShipAis> value : values.entrySet()) {
           chartViewer.addPointPainter(new GeoPosition(value.getValue().latitude, value.getValue().longitude));
           TimeUnit.MILLISECONDS.sleep(5);
        }
    }



    @Override
    public double distanceTo(Element position, CoordinateSystem system) {
        return 0;
    }

    @Override
    public double geodesicDistanceTo(Element other) {
        return 0;
    }

    @Override
    public double rhumbLineDistanceTo(Element other) {
        return 0;
    }
}
