package de.jade.ecs.map.grapchic;

import javax.swing.*;
import java.awt.*;

public class ShapeTest extends JFrame{
     public ShapeTest(){
          setSize(400,400);
          setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          setLocationRelativeTo(null);
          setVisible(true);
     }

     public static void main(String a[]){
         new ShapeTest();
     }

     public void paint(Graphics g){
          g.drawOval(40, 40, 60, 60); //FOR CIRCLE
     }
}
