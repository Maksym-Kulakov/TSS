package de.jade.ecs.map.shipchart;

import de.jade.ecs.map.ShipAis;
import de.jade.ecs.map.shipchart.ShipInter;
import de.jade.ecs.map.shipchart.ShipRenderer;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.AbstractPainter;
import org.jxmapviewer.viewer.DefaultWaypointRenderer;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointRenderer;

import java.awt.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ShipPainter<S extends ShipInter> extends AbstractPainter<JXMapViewer> {

    private ShipRenderer<? super S> renderer = new DefaultShipRenderer();
    private Set<S> ships = new HashSet<S>();

    public ShipPainter()
    {
        setAntialiasing(true);
        setCacheable(false);
    }

    public void setRenderer(ShipRenderer<S> r)
    {
        this.renderer = r;
    }

    public Set<S> getShips()
    {
        return Collections.unmodifiableSet(ships);
    }

    @Override
    protected void doPaint(Graphics2D g, JXMapViewer map, int width, int height)
    {
        if (renderer == null)
        {
            return;
        }
        Rectangle viewportBounds = map.getViewportBounds();
        g.translate(-viewportBounds.getX(), -viewportBounds.getY());

        for (ShipAis s : ShipAis.shipsAisHashMap.values())
        {
            renderer.paintShip(g, map, (S)s);
        }
        g.translate(viewportBounds.getX(), viewportBounds.getY());
    }
}
