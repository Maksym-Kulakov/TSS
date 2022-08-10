package de.jade.ecs.map.shipchart;

import org.jxmapviewer.viewer.GeoPosition;

public interface ShipInter {
    GeoPosition getPosition();
    Double getCog();
    Integer getMmsi();
}
