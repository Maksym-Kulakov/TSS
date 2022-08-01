package de.jade.ecs.map.shipchart;

import org.jxmapviewer.JXMapViewer;

import java.awt.*;
// S - ship type
public interface ShipRenderer<S> {
    void paintShip(Graphics2D g, JXMapViewer map, S ShipInter);
}
