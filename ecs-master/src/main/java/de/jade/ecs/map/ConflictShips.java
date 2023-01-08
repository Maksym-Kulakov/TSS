package de.jade.ecs.map;


import org.opengis.geometry.DirectPosition;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class ConflictShips implements Cloneable {
    public Double cpaValue;
    public Double tcpaValue;
    public DirectPosition cpaLocation;
    public ShipAis shipA;
    public ShipAis shipB;
    public DirectPosition position1Future;
    public DirectPosition position2Future;
    public boolean draw = false;
    public boolean shipAGiveWay = false;
    public boolean shipBGiveWay = false;

    public ConflictShips(Double cpaValue, Double tcpaValue,
                         DirectPosition cpaLocation, ShipAis shipA,
                         ShipAis shipB, DirectPosition position1Future, DirectPosition position2Future) {
        this.cpaValue = cpaValue;
        this.tcpaValue = tcpaValue;
        this.cpaLocation = cpaLocation;
        this.shipA = shipA;
        this.shipB = shipB;
        this.position1Future = position1Future;
        this.position2Future = position2Future;
    }

    Stack<String> strings = new Stack<>();

    @Override
    public ConflictShips clone() throws CloneNotSupportedException {
        ConflictShips newConflictShips = (ConflictShips) super.clone();
        newConflictShips.shipA = shipA.clone();
        newConflictShips.shipB = shipB.clone();
        return newConflictShips;
    }
}
