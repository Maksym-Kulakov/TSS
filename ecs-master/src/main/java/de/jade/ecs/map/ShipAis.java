package de.jade.ecs.map;

import de.jade.ecs.map.shipchart.ShipInter;
import de.jade.ecs.map.shipchart.TssArea;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Map.Entry;
import java.util.HashMap;

public class ShipAis implements ShipInter {
    Integer mmsiNum;
    double latitude;
    double longitude;
    double cog;
    public static HashMap<Integer, ShipAis> shipsAisHashMap = new HashMap<>();
    GeoPosition geoPosition;
    public static HashMap<Integer, ShipAis> shipsInsideAreaAisHashMap = new HashMap<>();



    public ShipAis() {
    }

    public ShipAis(int mmsiNum, double latitude, double longitude, double cog) {
        this.cog = cog;
        this.mmsiNum = mmsiNum;
        this.latitude = latitude;
        this.longitude = longitude;
        geoPosition = new GeoPosition(latitude, longitude);
        addShipToAisHashMap(this);
        addShipsInsideAreaAisHashMap(this);
    }

    public void addShipToAisHashMap(ShipAis ship) {
//        if (shipsAisHashMap.containsKey(ship.mmsiNum)) {
//            shipsAisHashMap.replace(ship.mmsiNum, shipsAisHashMap.get(mmsiNum), ship);
//        }
        shipsAisHashMap.put(ship.mmsiNum, ship);
    }

    public void addShipsInsideAreaAisHashMap(ShipAis ship) {
        Point2D.Double geoPoint = new Point2D.Double(ship.latitude, ship.longitude);
        if (BoundaryArea.insideArea(geoPoint, TssArea.polygon)) {
            shipsInsideAreaAisHashMap.put(ship.mmsiNum, ship);
        }
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

    @Override
    public Double getCog() {
        return cog;
    }

    @Override
    public Integer getMmsi() {
        return mmsiNum;
    }
}



