package de.jade.ecs.map.shipchart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jxmapviewer.JXMapViewer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Objects;

public class DefaultShipRenderer implements ShipRenderer<ShipInter> {

    private static final Log log = LogFactory.getLog(DefaultShipRenderer.class);

    private BufferedImage img = null;
    private BufferedImage imgnew;

    public static HashMap<Integer, Double> shipsCogHashMap = new HashMap<>();
    public static HashMap<Integer, BufferedImage> shipsImgHashMap = new HashMap<>();
    public static HashMap<Double, BufferedImage> shipsHdgimg = new HashMap<>();

    public DefaultShipRenderer() {
        try {
            img = ImageIO.read(Objects.requireNonNull(DefaultShipRenderer.class.getResource("/images/ownship.png")));
            imgnew = img;

            for(float i = 0.0f; i<=3600f; i+=1.0){
                shipsHdgimg.put((double)i/10, ShipImage.rotateImage(img, i/10));
            }
        } catch (Exception ex) {
            log.warn("couldn't read standard_waypoint.png", ex);
        }
    }

    @Override
    public void paintShip(Graphics2D g, JXMapViewer map, ShipInter s) {


        if (imgnew == null)
            return;

        Point2D point = map.getTileFactory().geoToPixel(s.getPosition(), map.getZoom());
        int x = (int) point.getX() - shipsHdgimg.get(s.getHdg()).getWidth() / 2;
        int y = (int) point.getY() - shipsHdgimg.get(s.getHdg()).getHeight() / 2;
        g.drawImage(shipsHdgimg.get(s.getHdg()), x, y, null);
//
//        if (shipsCogHashMap.get(s.getMmsi()) == null || !s.getCog().equals(shipsCogHashMap.get(s.getMmsi()))) {
//            int x = (int) point.getX() - imgnew.getWidth() / 2;
//            int y = (int) point.getY() - imgnew.getHeight() / 2;
//            imgnew = ShipImage.rotateImage(img, s.getCog());
//            shipsCogHashMap.put(s.getMmsi(), s.getCog());
//            shipsImgHashMap.put(s.getMmsi(), imgnew);
//            g.drawImage(imgnew, x, y, null);
//        } else {
//            int x = (int) point.getX() - shipsImgHashMap.get(s.getMmsi()).getWidth() / 2;
//            int y = (int) point.getY() - shipsImgHashMap.get(s.getMmsi()).getHeight() / 2;
//            g.drawImage(shipsImgHashMap.get(s.getMmsi()), x, y, null);
//        }
    }
}
