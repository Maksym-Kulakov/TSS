package de.jade.ecs.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.event.MouseInputListener;
import de.jade.ecs.map.grapchic.MyClass;
import de.jade.ecs.map.shipchart.ShipInter;
import de.jade.ecs.map.shipchart.ShipPainter;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import org.jxmapviewer.viewer.wms.WMSTileFactory;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import de.jade.ecs.util.WMSServiceImagePNG;
import render.ChartContext;
import s57.S57map;
import s57.S57map.Feature;
import s57.S57map.GeomIterator;
import s57.S57map.Pflag;
import s57.S57map.Snode;
import s57.S57obj.Obj;
import symbols.Symbols;

/**
 * ChartViewer based on JXMapviewer2
 * 
 * @author chris
 *
 */
public class ChartViewer implements ChartContext {

	private JXMapViewer mapViewer;
	public ArrayList<Painter<JXMapViewer>> paintersList;
	public ArrayList<Painter<JXMapViewer>> shipsList;
	private CompoundPainter<JXMapViewer> painter;
	private MyClass myline;

	private JFrame jFrame = null;

	public JXMapViewer getJXMapViewer() {
		return this.mapViewer;
	}

	/**
	 * Ctor
	 */
	public ChartViewer() {
		mapViewer = new JXMapViewer();

		
		// https://seamlessrnc.nauticalcharts.noaa.gov/arcgis/rest/services/encdirect
//		WMSService wms = new WMSService("https://seamlessrnc.nauticalcharts.noaa.gov/arcgis/services/RNC/NOAA_RNC/MapServer/WmsServer?", "1");//"OSM-WMS");
//		WMSTileFactory tileFactory = new WMSTileFactory(wms);
		
		WMSServiceImagePNG wms = new WMSServiceImagePNG("https://wms.sevencs.com/?SERVICE=WMS&CSBOOL=183&CSVALUE=10,5,15,10,1,2,1,,,,1&", "ENC");//"OSM-WMS");
		WMSTileFactory tileFactory = new WMSTileFactory(wms);
		tileFactory.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Safari/537.36");
		
		
		// Create a TileFactoryInfo for OpenStreetMap
//		TileFactoryInfo info = new OSMTileFactoryInfo();
//		DefaultTileFactory tileFactory = new DefaultTileFactory(info);
		mapViewer.setTileFactory(tileFactory);

		// Use 8 threads in parallel to load the tiles
		tileFactory.setThreadPoolSize(8);

		// Set the focus
		GeoPosition nordsee = new GeoPosition(54, 8);

		paintersList = new ArrayList<Painter<JXMapViewer>>();
		shipsList = new ArrayList<Painter<JXMapViewer>>();

		mapViewer.setZoom(8);
		mapViewer.setAddressLocation(nordsee);

		// Add interactions
		MouseInputListener mia = new PanMouseInputListener(mapViewer);
		mapViewer.addMouseListener(mia);
		mapViewer.addMouseMotionListener(mia);
		mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));

		addShipPainter();

	}

	public void show() {
		// Display the viewer in a JFrame
		if (jFrame == null) {
			jFrame = new JFrame("ChartViewer");
			jFrame.getContentPane().add(mapViewer);
			jFrame.setSize(1000, 800);
			jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			jFrame.setVisible(true);
		} else {
			if (!jFrame.isVisible()) {
				jFrame.setVisible(true);
			}
		}
	}

	public void addSeachart(S57map s57map) {
		SeachartPainter seaPainter = new SeachartPainter(s57map, this);
		paintersList.add(seaPainter);
		painter = new CompoundPainter<JXMapViewer>(paintersList);
		mapViewer.setOverlayPainter(painter);
		mapViewer.updateUI();
	}


	/**
	 * removes the given WaypointPainter from WaypointPainters List
	 * 
	 * @param painter
	 */
	public void removePointPainter(WaypointPainter<Waypoint> painter) {
		paintersList.remove(painter);
	}

	/**
	 * adds a new WaypointPainter with the given Point to the WaypointPainters List
	 * for other geometries use a LinePainter
	 *
	 * @param point
	 * @return
	 */
	public WaypointPainter<Waypoint> addPointPainter(Point point) {
		GeoPosition geoPosition = new GeoPosition(point.getY(), point.getX());
		Set<Waypoint> waypoints = new HashSet<Waypoint>(Arrays.asList(new DefaultWaypoint(geoPosition)));
		WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<Waypoint>();
		waypointPainter.setWaypoints(waypoints);

		paintersList.add(waypointPainter);
		painter = new CompoundPainter<JXMapViewer>(paintersList);
		mapViewer.setOverlayPainter(painter);
		mapViewer.updateUI();

		return waypointPainter;
	}

// ShipPainter
	public ShipPainter<ShipInter> addShipPainter() {
//		Set<ShipInter> ships = new HashSet<ShipInter>(Arrays.asList(new DefaultShip(geoPosition)));
		ShipPainter<ShipInter> shipPainter = new ShipPainter<ShipInter>();
	//	shipPainter.setShips(ships);

		shipsList.add(shipPainter);
		painter = new CompoundPainter<JXMapViewer>(shipsList);
		mapViewer.setOverlayPainter(painter);
		mapViewer.updateUI();

		return shipPainter;
	}

	/**
	 * removes the given Linepainter from the paintersList
	 * 
	 * @param painter
	 */
	public void removeLinePainter(LinePainter painter) {
		paintersList.remove(painter);
	}

	/**
	 * adds a the LinePainter for the given Geometry to the paintersList and returns
	 * it
	 * 
	 * @param geo
	 * @return
	 */
	public LinePainter addLinePainter(Geometry geo) {
		List<GeoPosition> track = new ArrayList<>();
		Arrays.asList(geo.getCoordinates()).forEach(c -> track.add(new GeoPosition(c.getY(), c.getX())));
		LinePainter linePainter = new LinePainter(track);

		paintersList.add(linePainter);
		painter = new CompoundPainter<JXMapViewer>(paintersList);
		mapViewer.setOverlayPainter(painter);
		mapViewer.updateUI();

		return linePainter;
	}

	/**
	 * removes the given PolygonPainter from the paintersList
	 * 
	 * @param painter
	 */
	public void removePolygonPainter(PolygonPainter painter) {
		paintersList.remove(painter);
	}

	/**
	 * adds a the {@link PolygonPainter} for the given Geometry to the paintersList
	 * and returns it
	 * 
	 * @param geo
	 * @return
	 */
	public PolygonPainter addPolygonPainter(Geometry geo) {
		List<GeoPosition> track = new ArrayList<>();
		Arrays.asList(geo.getCoordinates()).forEach(c -> track.add(new GeoPosition(c.getY(), c.getX())));
		PolygonPainter polygonPainter = new PolygonPainter(track);

		paintersList.add(polygonPainter);
		painter = new CompoundPainter<JXMapViewer>(paintersList);
		mapViewer.setOverlayPainter(painter);
		mapViewer.updateUI();

		return polygonPainter;
	}

	/**
	 * removes the given Painter from the paintersList
	 * 
	 * @param painter
	 */
	public void removePainter(Painter<JXMapViewer> painter) {
		paintersList.remove(painter);
		painter = new CompoundPainter<JXMapViewer>(paintersList);
		mapViewer.setOverlayPainter(painter);
		mapViewer.updateUI();
	}

	/**
	 * adds a the {@link Painter} to the paintersList and returns it
	 * 
	 * @param geo
	 * @return
	 */
	public Painter<JXMapViewer> addPainter(Painter<JXMapViewer> painter) {
		paintersList.add(painter);
		painter = new CompoundPainter<JXMapViewer>(paintersList);
		mapViewer.setOverlayPainter(painter);
		mapViewer.updateUI();
		return painter;
	}

	/**
	 * LinePainter
	 * 
	 * @author Martin Steiger
	 *
	 */
	public class LinePainter implements Painter<JXMapViewer> {
		private Color color = Color.RED;
		private boolean antiAlias = true;

		private List<GeoPosition> track;

		/**
		 * @param track the track
		 */
		public LinePainter(List<GeoPosition> track) {
			// copy the list so that changes in the
			// original list do not have an effect here
			this.track = Collections.synchronizedList(new ArrayList<GeoPosition>(track));
		}
		
		
		/**
		 * 
		 * @return - the track list
		 */
		public List<GeoPosition> getTrack() {
			return track;
		}


		@Override
		public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
			g = (Graphics2D) g.create();

			// convert from viewport to world bitmap
			Rectangle rect = map.getViewportBounds();
			g.translate(-rect.x, -rect.y);

			if (antiAlias)
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			// do the drawing
			g.setColor(Color.BLACK);
			g.setStroke(new BasicStroke(4));

			drawRoute(g, map);

			// do the drawing again
			g.setColor(color);
			g.setStroke(new BasicStroke(2));

			drawRoute(g, map);

			g.dispose();
		}

		/**
		 * @param g   the graphics object
		 * @param map the map
		 */
		private void drawRoute(Graphics2D g, JXMapViewer map) {
			int lastX = 0;
			int lastY = 0;

			boolean first = true;

			synchronized (track) {
				for (GeoPosition gp : track) {
					// convert geo-coordinate to world bitmap pixel
					Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());

					if (first) {
						first = false;
					} else {
						g.drawLine(lastX, lastY, (int) pt.getX(), (int) pt.getY());
					}

					lastX = (int) pt.getX();
					lastY = (int) pt.getY();
				}
			}
		}
	}

	@Override
	public Point2D getPoint(Snode coord) {
		Point2D point = mapViewer.getTileFactory()
				.geoToPixel(new GeoPosition(Math.toDegrees(coord.lat), Math.toDegrees(coord.lon)), mapViewer.getZoom());
		return point;
	}

	@Override
	public double mile(Feature feature) {
		return 1852;
	}

	@Override
	public boolean clip() {
		return false;
	}

	@Override
	public Color background(S57map map) {
		if (map.features.containsKey(Obj.COALNE)) {
			for (Feature feature : map.features.get(Obj.COALNE)) {
				if (feature.geom.prim == Pflag.POINT) {
					break;
				}
				GeomIterator git = map.new GeomIterator(feature.geom);
				git.nextComp();
				while (git.hasEdge()) {
					git.nextEdge();
					while (git.hasNode()) {
						Snode node = git.next();
						if (node == null)
							continue;
						if ((node.lat >= map.bounds.minlat) && (node.lat <= map.bounds.maxlat)
								&& (node.lon >= map.bounds.minlon) && (node.lon <= map.bounds.maxlon)) {
							return Symbols.Bwater;
						}
					}
				}
			}
			return Symbols.Yland;
		} else {
			if (map.features.containsKey(Obj.ROADWY) || map.features.containsKey(Obj.RAILWY)
					|| map.features.containsKey(Obj.LAKARE) || map.features.containsKey(Obj.RIVERS)
					|| map.features.containsKey(Obj.CANALS)) {
				return Symbols.Yland;
			} else {
				return Symbols.Bwater;
			}
		}
	}

	@Override
	public RuleSet ruleset() {
		return RuleSet.ALL;
	}
}
