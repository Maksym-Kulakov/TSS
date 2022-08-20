package de.jade.ecs.map.riskassessment;

import de.jade.ecs.map.ShipAis;

import java.util.HashMap;

public class TcpaCalculation {
    public ShipAis shipA;
    public ShipAis shipB;

    ShipAis getShip(HashMap<Integer, ShipAis> shipAisHashMap) {
        if (!shipAisHashMap.values().isEmpty()) {
            return shipAisHashMap.values().stream().findFirst().get();
        }
        return null;
    }

    public void calculateTcpa() {
        ShipAis shipA = getShip(ShipAis.shipsInsideAreaToNorthAisHashMap);
        ShipAis shipB = getShip(ShipAis.shipsInsideAreaInWeserAisHashMap);
        if (shipA != null && shipB != null) {
            System.out.println("SHIPS SPEEDS !!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println(shipA.getSpeed());
            System.out.println(shipB.getSpeed());
        }
    }
}
