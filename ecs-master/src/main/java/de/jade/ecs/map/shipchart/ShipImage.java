package de.jade.ecs.map.shipchart;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ShipImage {

    public static BufferedImage rotateImage(BufferedImage buffImage, double angle) {
        double radian = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(radian));
        double cos = Math.abs(Math.cos(radian));
        int width = buffImage.getWidth();
        int height = buffImage.getHeight();
        int nWidth = (int) Math.floor((double) width * cos + (double) height * sin);
        int nHeight = (int) Math.floor((double) height * cos + (double) width * sin);
        BufferedImage rotatedImage = new BufferedImage(nWidth, nHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = rotatedImage.createGraphics();
        graphics.translate((nWidth - width) / 2, (nHeight - height) / 2);
        graphics.rotate(radian, (double) (width / 2), (double) (height / 2));
        graphics.drawRenderedImage(buffImage, null);
        graphics.dispose();
        return rotatedImage;
    }
}
