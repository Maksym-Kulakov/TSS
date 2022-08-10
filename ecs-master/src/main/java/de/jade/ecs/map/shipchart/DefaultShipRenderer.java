package de.jade.ecs.map.shipchart;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Objects;
import javax.imageio.ImageIO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jxmapviewer.JXMapViewer;

public class DefaultShipRenderer implements ShipRenderer<ShipInter> {

    private static final Log log = LogFactory.getLog(DefaultShipRenderer.class);

    private BufferedImage img = null;
    private BufferedImage imgnew;

    public static HashMap<Integer, Double> shipsCogHashMap = new HashMap<>();
    public static HashMap<Integer, BufferedImage> shipsImgHashMap = new HashMap<>();

    public DefaultShipRenderer() {
        try {
            img = ImageIO.read(Objects.requireNonNull(DefaultShipRenderer.class.getResource("/images/ownship.png")));
            imgnew = img;
        } catch (Exception ex) {
            log.warn("couldn't read standard_waypoint.png", ex);
        }
    }

    @Override
    public void paintShip(Graphics2D g, JXMapViewer map, ShipInter s) {


        if (imgnew == null)
            return;

        Point2D point = map.getTileFactory().geoToPixel(s.getPosition(), map.getZoom());


        if (shipsCogHashMap.get(s.getMmsi()) == null || !s.getCog().equals(shipsCogHashMap.get(s.getMmsi()))) {
            int x = (int) point.getX() - imgnew.getWidth() / 2;
            int y = (int) point.getY() - imgnew.getHeight() / 2;
            imgnew = ShipImage.rotateImage(img, s.getCog());
            shipsCogHashMap.put(s.getMmsi(), s.getCog());
            shipsImgHashMap.put(s.getMmsi(), imgnew);
            g.drawImage(imgnew, x, y, null);
        } else {
            int x = (int) point.getX() - shipsImgHashMap.get(s.getMmsi()).getWidth() / 2;
            int y = (int) point.getY() - shipsImgHashMap.get(s.getMmsi()).getHeight() / 2;
            g.drawImage(shipsImgHashMap.get(s.getMmsi()), x, y, null);
        }
    }
}
