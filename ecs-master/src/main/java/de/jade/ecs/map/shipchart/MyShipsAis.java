package de.jade.ecs.map.shipchart;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;

public class MyShipsAis extends DefaultWaypoint {
	
	private String name;
	private JButton button;
	
	
	
	private void initButton() {
		button = new ShipAis();
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				 System.out.println("Click" + name);
				
			}
			
		}
				
				);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public JButton getButton() {
		return button;
	}
	public void setButton(JButton button) {
		this.button = button;
	}
	
	public MyShipsAis(String name, GeoPosition coord) {
		super(coord);
		this.name = name;
		initButton();
	}
	
	public MyShipsAis() { 
	
	}

	
	
	
}
