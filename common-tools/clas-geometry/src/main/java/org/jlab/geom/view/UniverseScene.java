/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jlab.geom.prim.Line3D;

/**
 *
 * @author gavalian
 */
public class UniverseScene extends JPanel {
    
    public static int SCENE_XY = 1;
    public static int SCENE_XZ = 2;
    public static int SCENE_YZ = 3;
    
    private int SCENE_TYPE = UniverseScene.SCENE_XY;
    
    private UniverseCoordinateSystem  system = new UniverseCoordinateSystem();
    List<ShapeObject>  objectStore = new ArrayList<ShapeObject>();
    
    
    public UniverseScene(){
        
    }
    
    public UniverseScene(double x0, double y0, double x1, double y1){
        system.setWorld(x0, y0, x1, y1);
    }
    
    @Override
    public void paint(Graphics g){ 
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        int w = this.getSize().width;
        int h = this.getSize().height;
        this.system.setCanvas(w, h);
        
        g2d.setColor(new Color(255,250,240));
        g2d.fillRect(0, 0, w, h);
        
        g2d.setColor(Color.red);
        g2d.drawString(this.system.getWorld().toString(), 6, 12);
        g2d.drawString(this.system.getCanvas().toString(), 6, 28);
        
        g2d.setColor(Color.black);
        
        for(ShapeObject object : objectStore){
            object.draw(g2d, system);
        }
    }
    
    public void addLine(double x0, double y0, double x1, double y1){
        ShapePath path = new ShapePath();
        path.getPath().addPoint(x0, y0, 0);
        path.getPath().addPoint(x1, y1, 0);
        this.objectStore.add(path);
    }
    
    public void addPoint(double x, double y){
        ShapePoint point = new ShapePoint(x,y,0.0);
        this.objectStore.add(point);
    }
    public static void main(String[] args){
        JFrame frame =  new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        UniverseScene scene = new UniverseScene(-500,-500,500.0,500.0);
        scene.addLine(-200, -200, 200, 200);
        scene.addLine(-100, 100, 100, -100);
        scene.addPoint(-100, 100);
        
        frame.add(scene);
        frame.setSize(600, 600);
        frame.setVisible(true);
    }
}
