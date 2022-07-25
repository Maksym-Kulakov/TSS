package de.jade.ecs.map.shipchart;

import org.jxmapviewer.beans.AbstractBean;
import org.jxmapviewer.viewer.GeoPosition;

public class DefaultShip extends AbstractBean implements ShipInter {
    private GeoPosition position;

    public DefaultShip()
    {
        this(new GeoPosition(0, 0));
    }

    public DefaultShip(double latitude, double longitude)
    {
        this(new GeoPosition(latitude, longitude));
    }

    public DefaultShip(GeoPosition coord)
    {
        this.position = coord;
    }

    @Override
    public GeoPosition getPosition() {
        return position;
    }

    public void setPosition(GeoPosition coordinate)
    {
        GeoPosition old = getPosition();
        this.position = coordinate;
        firePropertyChange("position", old, getPosition());
    }
}
