package de.jade.ecs.map;


import org.opengis.geometry.DirectPosition;

import java.awt.geom.Point2D;

public class ConflictShips {
    Double cpaValue;
    Double tcpaValue;
    DirectPosition cpaLocation;
    ShipAis shipA;
    ShipAis shipB;

    public ConflictShips(Double cpaValue, Double tcpaValue, DirectPosition cpaLocation, ShipAis shipA, ShipAis shipB) {
        this.cpaValue = cpaValue;
        this.tcpaValue = tcpaValue;
        this.cpaLocation = cpaLocation;
        this.shipA = shipA;
        this.shipB = shipB;
    }
}
