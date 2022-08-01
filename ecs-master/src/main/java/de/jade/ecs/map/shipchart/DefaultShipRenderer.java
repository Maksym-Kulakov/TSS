package de.jade.ecs.map.shipchart;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Objects;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.DefaultWaypointRenderer;
import org.jxmapviewer.viewer.Waypoint;


public class DefaultShipRenderer implements ShipRenderer<ShipInter> {

    private static final Log log = LogFactory.getLog(DefaultShipRenderer.class);

    private BufferedImage img = null;
    private BufferedImage imgnew;

    public DefaultShipRenderer()
    {
        try
        {
            img = ImageIO.read(Objects.requireNonNull(DefaultShipRenderer.class.getResource("/images/ownship.png")));
            imgnew = img;
        }
        catch (Exception ex)
        {
            log.warn("couldn't read standard_waypoint.png", ex);
        }
    }

    @Override
    public void paintShip(Graphics2D g, JXMapViewer map, ShipInter s)
    {
        if (imgnew == null)
            return;



        Point2D point = map.getTileFactory().geoToPixel(s.getPosition(), map.getZoom());

        int x = (int)point.getX() -imgnew.getWidth() / 2;
        int y = (int)point.getY() -imgnew.getHeight();

        imgnew = ShipImage.rotateImage(img, s.getCog());

        /*
        double radian = Math.toRadians(s.getCog());
        double sin = Math.abs(Math.sin(radian));
        double cos = Math.abs(Math.cos(radian));
        int width = imgnew.getWidth();
        int height = imgnew.getHeight();
        int nWidth = (int) Math.floor((double) width * cos + (double) height * sin);
        int nHeight = (int) Math.floor((double) height * cos + (double) width * sin);
        g.translate((nWidth - width) / 2, (nHeight - height) / 2);
        g.rotate(radian, (double) (width / 2), (double) (height / 2));

         */

        g.drawImage(imgnew, x, y, null);
    }
}
