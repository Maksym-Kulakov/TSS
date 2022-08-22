package de.jade.ecs.map;

import de.jade.ecs.map.riskassessment.TcpaCalculation;
import de.jade.ecs.map.shipchart.ShipInter;
import de.jade.ecs.map.shipchart.TssArea;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map.Entry;

public class ShipAis implements ShipInter {
    Integer mmsiNum;
    double latitude;
    double longitude;
    double hdg;
    double speed;
    public static HashMap<Integer, ShipAis> shipsAisHashMap = new HashMap<>();
    GeoPosition geoPosition;
    public static HashMap<Integer, ShipAis> shipsInsideAreaToEastAisHashMap = new HashMap<>();
    public static HashMap<Integer, ShipAis> shipsInsideAreaToSouthAisHashMap = new HashMap<>();
    public static HashMap<Integer, ShipAis> shipsInsideAreaToNorthAisHashMap = new HashMap<>();
    public static HashMap<Integer, ShipAis> shipsInsideAreaInWeserAisHashMap = new HashMap<>();
    //    public static DynamicData data = new DynamicData("Ship Ais");
    TcpaCalculation tcpaCalculation = new TcpaCalculation();

    public ShipAis() {
    }

    public ShipAis(int mmsiNum, double latitude, double longitude, double hdg, double speed) {
        this.hdg = hdg;
        this.mmsiNum = mmsiNum;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        geoPosition = new GeoPosition(latitude, longitude);
        addShipToAisHashMap(this);
        addShipsInsideAreaAisHashMapToEast(this);
        addShipsInsideAreaAisHashMapToSouth(this);
        addShipsInsideAreaAisHashMapToNorth(this);
        addShipsInsideAreaAisHashMapInWeser(this);
    }

    public void addShipToAisHashMap(ShipAis ship) {
        shipsAisHashMap.put(ship.mmsiNum, ship);
    }

    public void addShipsInsideAreaAisHashMapToEast(ShipAis ship) {
        Point2D.Double geoPoint = new Point2D.Double(ship.latitude, ship.longitude);
        if (BoundaryArea.insideArea(geoPoint, TssArea.trafficLineToEast)) {
            shipsInsideAreaToEastAisHashMap.put(ship.mmsiNum, ship);
        }
    }

    public void addShipsInsideAreaAisHashMapToSouth(ShipAis ship) {
        Point2D.Double geoPoint = new Point2D.Double(ship.latitude, ship.longitude);
        if (BoundaryArea.insideArea(geoPoint, TssArea.trafficLineToSouth)) {
            shipsInsideAreaToSouthAisHashMap.put(ship.mmsiNum, ship);
        }
    }

    public void addShipsInsideAreaAisHashMapToNorth(ShipAis ship) {
        Point2D.Double geoPoint = new Point2D.Double(ship.latitude, ship.longitude);
        if (BoundaryArea.insideArea(geoPoint, TssArea.trafficLineToNorth)) {
            shipsInsideAreaToNorthAisHashMap.put(ship.mmsiNum, ship);
        }
    }

    public void addShipsInsideAreaAisHashMapInWeser(ShipAis ship) {
        Point2D.Double geoPoint = new Point2D.Double(ship.latitude, ship.longitude);
        if (BoundaryArea.insideArea(geoPoint, TssArea.inWeser)) {
            shipsInsideAreaInWeserAisHashMap.put(ship.mmsiNum, ship);
        }
    }

    public void addShipsOnMap(ChartViewer chartViewerObj, HashMap<Integer, ShipAis> values) {
        for (Entry<Integer, ShipAis> value : values.entrySet()) {
            chartViewerObj.addShipPainter();
        }
    }

    public double getSpeed() {
        return speed;
    }

    @Override
    public GeoPosition getPosition() {
        return geoPosition;
    }

    @Override
    public Double getHdg() {
        return hdg;
    }

    @Override
    public Integer getMmsi() {
        return mmsiNum;
    }
}



