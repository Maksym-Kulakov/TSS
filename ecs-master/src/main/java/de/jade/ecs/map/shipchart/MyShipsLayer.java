package de.jade.ecs.map.shipchart;
 
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.VirtualEarthTileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.WaypointPainter;

 
public class MyShipsLayer extends JFrame implements ActionListener{
	
	
	
	JXMapViewer mymapViewer;
 	
	int i;
	
	private final Set<MyShipsAis> ships = new HashSet<>();
	
	private JButton cmdAdd;
	

	
	public MyShipsLayer (int i) {
		mymapViewer = new JXMapViewer();
		this.i = i;
		init(i);
		
		cmdAdd = new JButton("add Mark");
		cmdAdd.setBounds(2, 2, 10, 10);

		JInternalFrame internalFrame = new JInternalFrame("New JInternalFrame");
		add(internalFrame, BorderLayout.WEST);
	 	internalFrame.setVisible(true);
	 	internalFrame.add(cmdAdd);
	 	cmdAdd.addActionListener(this);
	 	
		getContentPane().add(mymapViewer);
		getComponents();
		
	}
	
	private void init(int i) {
		TileFactoryInfo info;
		if (i == 1) {
		info = new OSMTileFactoryInfo();
		} else if (i == 2) {	 	
		info = new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.SATELLITE);
		} else if (i == 3) {	 	
		info = new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.HYBRID);
		} else {	 	
		info = new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.MAP);
		}
		DefaultTileFactory tileFactory = new DefaultTileFactory(info);
		mymapViewer.setTileFactory(tileFactory);
		GeoPosition geo = new GeoPosition(51.4688936, 7.2183372);
		mymapViewer.setAddressLocation(geo);
		mymapViewer.setZoom(2);
		
		MouseInputListener mymouse = new PanMouseInputListener(mymapViewer);
		mymapViewer.addMouseListener(mymouse);
		mymapViewer.addMouseMotionListener(mymouse);
		mymapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCenter(mymapViewer));
		
	}
	
	
		
	
		private void initMark() {
			WaypointPainter<MyShipsAis> wp = new MyShipsReader();
			wp.setWaypoints(ships);
			mymapViewer.setOverlayPainter(wp);
			for (MyShipsAis x : ships) {
				mymapViewer.add(x.getButton());
			}
		}
	
//		private void clearMark() {
//			for (MyMark x : marks) {
//				mymapViewer.remove(x.getButton());
//			}
//			marks.clear();
//			initMark();
//		}
//	
//		
//		private void cmdAddActionPerformed(ActionEvent e) {
//			
//		}

//		private void cmdClearActionPerformed(ActionEvent e) {
//			clearMark();
//		}
	
    public static void main( String[] args ) {
    	
    	JFrame myframe = new MyShipsLayer(1);
    	myframe.setSize(1000, 800);
    	myframe.setVisible(true);
    	
    		

	 
		}

	@Override
	public void actionPerformed(ActionEvent e) {
		ships.add(new MyShipsAis("Test 001", new GeoPosition(51.470187914814495, 7.217131991814141)));
		initMark();
		
	}
	}

