package de.jade.ecs.map;

import org.apache.sis.geometry.DirectPosition2D;
import org.opengis.geometry.DirectPosition;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class CrossAreaChart {
    public static final int MIN_CPA_TO_GET_ON_CHART = 3;
    public static HashMap<Integer, ConflictShips> shipsConflictsInCrossAreaSouth = new LinkedHashMap<>();

    static {
        DirectPosition cpaLocation1 = new DirectPosition2D(53.91895, 7.694732);
        DirectPosition cpaLocation2 = new DirectPosition2D(53.95917, 7.71458);
        ShipAis shipAis1 = new ShipAis();
        ShipAis shipAis2 = new ShipAis();
        ConflictShips conflictShips1
                = new ConflictShips(0.2, 5.0, cpaLocation1, shipAis1, shipAis2);
        ConflictShips conflictShips2
                = new ConflictShips(0.5, 5.0, cpaLocation2, shipAis1, shipAis2);
        shipsConflictsInCrossAreaSouth.put(1, conflictShips1);
        shipsConflictsInCrossAreaSouth.put(2, conflictShips2);
    }

    public void addHardCodedConflict() {
        ShipAis shipAis1 = new ShipAis();
        ShipAis shipAis2 = new ShipAis();
        DirectPosition cpaLocation3 = new DirectPosition2D(53.94025, 7.625657);
        ConflictShips conflictShips3
                = new ConflictShips(0.0, 5.0, cpaLocation3, shipAis1, shipAis2);
        shipsConflictsInCrossAreaSouth.put(3, conflictShips3);
    }


    public static Point2D.Double point1 = new Point2D.Double(53.92279, 7.778727);
    public static Point2D.Double point2 = new Point2D.Double(53.97126, 7.75936);
    public static Point2D.Double point3 = new Point2D.Double(53.95075, 7.611302);
    public static Point2D.Double point4 = new Point2D.Double(53.90158, 7.631205);
    public static Point2D.Double[] crossAreaSouth = {point1, point2, point3, point4};

    public static void getShipsConflictsInCrossAreaSouth() {
        ApplicationCPA.shipsConflicts.entrySet().stream()
                .filter(cs -> cs.getValue().cpaValue < MIN_CPA_TO_GET_ON_CHART
                && BoundaryArea.insideArea(new Point2D.Double(cs.getValue().cpaLocation.getCoordinate()[0],
                cs.getValue().cpaLocation.getCoordinate()[1]), crossAreaSouth))
                .forEach(cs -> shipsConflictsInCrossAreaSouth.put(cs.getKey(), cs.getValue()));
    }
}
