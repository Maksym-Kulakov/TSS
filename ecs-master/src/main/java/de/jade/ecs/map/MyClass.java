package de.jade.ecs.map;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

import javax.swing.JComponent;

public class MyClass extends JComponent{
	public MyClass () {
		
	}
	
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		Line2D.Double line = new Line2D.Double (0, 0, 1000, 800);
		g2d.draw(line);
	}
	
}
