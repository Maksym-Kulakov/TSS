package de.jade.ecs.map;

import java.awt.geom.Point2D;

public class BoundaryArea {
    public static boolean insideArea(Point2D.Double p, Point2D.Double[] polygon) {
        int intersections = 0;
        Point2D.Double prev = polygon[polygon.length - 1];
        for (Point2D.Double next : polygon) {
            if ((prev.y <= p.y && p.y < next.y) || (prev.y >= p.y && p.y > next.y)) {
                double dy = next.y - prev.y;
                double dx = next.x - prev.x;
                double x = (p.y - prev.y) / dy * dx + prev.x;
                if (x > p.x) {
                    intersections++;
                }
            }
            prev = next;
        }
        return intersections % 2 == 1;
    }
}
