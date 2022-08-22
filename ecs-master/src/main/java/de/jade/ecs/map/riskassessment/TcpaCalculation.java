package de.jade.ecs.map.riskassessment;

import de.jade.ecs.map.ShipAis;

import java.util.HashMap;
import java.util.Map;

public class TcpaCalculation {
    public static HashMap<Integer, ShipAis> shipsGot = new HashMap<>();

    public ShipAis getShip(HashMap<Integer, ShipAis> shipAisHashMap) {
        if (!shipAisHashMap.values().isEmpty()) {
            for (Map.Entry<Integer, ShipAis> ship : shipAisHashMap.entrySet()) {
                if (!shipsGot.containsKey(ship.getKey())) {
                    shipsGot.put(ship.getKey(), ship.getValue());
                    return ship.getValue();
                }
            }
            shipsGot.clear();
            getShip(shipAisHashMap);
        }
        return null;
    }
}
