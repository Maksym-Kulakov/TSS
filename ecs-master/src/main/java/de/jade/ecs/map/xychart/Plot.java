package de.jade.ecs.map.xychart;/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;


public class Plot extends JPanel {
    VPanel vpanel;
    long initial_time;
    double diff;
    Calendar cl;
    JFrame frame;
    int maxAge;
    JTabbedPane tabbedPane;

    public Plot(int maxage) {
        super(new GridLayout(1,1));
        frame = new JFrame("Roll Pitch Heading");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800,600);
        frame.setLocation(100, 100);
        maxAge=maxage;
        createGUI(maxAge);
        frame.add(this);

        frame.setVisible(true);
        
        maxAge=maxage;

    }
    public void createGUI(int maxAge) {
        tabbedPane = new JTabbedPane();
        
        
      vpanel = new VPanel(maxAge);
                vpanel.GUI();
               
                tabbedPane.add("Vn Ve Vz Plot",vpanel.chartPanel);
                 cl=java.util.Calendar.getInstance();
                initial_time = cl.getTimeInMillis();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        add(tabbedPane);

    }

class DataGenerator extends Timer implements ActionListener
    {
        double t=0;
        DataGenerator(int interval)
        {
            super(interval, null);
            addActionListener(this);
        }
        public void actionPerformed(ActionEvent event) {
            try
            {
                   t=t+0.05;
                   
                   diff=java.util.Calendar.getInstance().getTimeInMillis()-initial_time;

                   vpanel.Vx.add(diff/1000,Math.sin(t));
                   vpanel.Vy.add(diff/1000, 1);
                   vpanel.Vz.add(diff/1000,Math.sin(t-Math.PI/4));
        } catch (Exception ex) {System.out.println("1 "+ex);}
        }
    }
 public static void main(String[] args) {
        try
        {
            Plot panel = new Plot(5);
            panel.new DataGenerator(200).start();
        } catch (Exception ex) {}
    }
}
