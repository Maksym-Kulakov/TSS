package de.jade.ecs.map.shipchart;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.DefaultWaypointRenderer;
import org.jxmapviewer.viewer.Waypoint;


public class DefaultShipRenderer implements ShipRenderer<ShipInter> {

    private static final Log log = LogFactory.getLog(DefaultShipRenderer.class);

    private BufferedImage img = null;

    public DefaultShipRenderer()
    {
        try
        {
            img = ImageIO.read(DefaultShipRenderer.class.getResource("/images/standard_waypoint.png"));
        }
        catch (Exception ex)
        {
            log.warn("couldn't read standard_waypoint.png", ex);
        }
    }

    @Override
    public void paintShip(Graphics2D g, JXMapViewer map, ShipInter s)
    {
        if (img == null)
            return;

        Point2D point = map.getTileFactory().geoToPixel(s.getPosition(), map.getZoom());

        int x = (int)point.getX() -img.getWidth() / 2;
        int y = (int)point.getY() -img.getHeight();

        g.drawImage(img, x, y, null);
    }
}
