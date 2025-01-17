package org.scheduler.util;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/** Draw 2D Objects
 * 
 * @author chris
 *
 */
public class Draw2DObjects extends JFrame {

		private static final long serialVersionUID = 1L;
		
		ArrayList<Shape> list = new ArrayList<>();
		
		public Draw2DObjects(ArrayList<Shape> list) {
			this.list = list;
			add("Center", new MyCanvas());
			
			setSize(1000, 1000);
		    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		    setVisible(true);
		}
		
		class MyCanvas extends Canvas {

			private static final long serialVersionUID = 1L;

			public void paint(Graphics graphics) {
		      Graphics2D g = (Graphics2D) graphics;
		      list.forEach( shape -> g.draw(shape));

		     
		    }
		  }
	}
