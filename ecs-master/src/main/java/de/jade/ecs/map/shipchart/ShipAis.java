package de.jade.ecs.map.shipchart;

import java.awt.Cursor;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class ShipAis extends JButton {
		
	public ShipAis() {
		setContentAreaFilled(false);
		setIcon(new ImageIcon(getClass().getResource("/pic.png")));
		setCursor(new Cursor(Cursor.HAND_CURSOR));
		setSize(new Dimension(24, 24));
	}
}