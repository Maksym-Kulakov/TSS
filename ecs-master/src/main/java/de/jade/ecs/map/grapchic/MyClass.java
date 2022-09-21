package de.jade.ecs.map.grapchic;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;

public class MyClass extends JComponent{
	public MyClass () {
		
	}
	
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		Line2D.Double line = new Line2D.Double (0, 0, 1000, 800);
		g2d.draw(line);
	}
	
}
