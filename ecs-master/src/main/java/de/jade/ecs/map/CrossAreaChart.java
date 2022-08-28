package de.jade.ecs.map;

import java.awt.geom.Point2D;
import java.util.HashMap;

public class CrossAreaChart {
    public static final int MIN_CPA_TO_GET_ON_CHART = 3;
    public static HashMap<Integer, ConflictShips> shipsConflictsInCrossAreaSouth = new HashMap<>();

    public static Point2D.Double point1 = new Point2D.Double(53.95167, 7.615);
    public static Point2D.Double point2 = new Point2D.Double(53.9045, 7.6515);
    public static Point2D.Double point3 = new Point2D.Double(53.9697, 7.7448);
    public static Point2D.Double point4 = new Point2D.Double(53.9208, 7.7681);
    public static Point2D.Double[] crossAreaSouth = {point1, point2, point3, point4};


    public static void getShipsConflictsInCrossAreaSouth() {
        ApplicationCPA.shipsConflicts.entrySet().stream()
                .filter(cs -> cs.getValue().cpaValue < MIN_CPA_TO_GET_ON_CHART
                && BoundaryArea.insideArea(new Point2D.Double(cs.getValue().cpaLocation.getCoordinate()[0],
                cs.getValue().cpaLocation.getCoordinate()[1]), crossAreaSouth))
                .forEach(cs -> shipsConflictsInCrossAreaSouth.put(cs.getKey(), cs.getValue()));
    }
}
