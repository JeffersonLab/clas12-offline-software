/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.view;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jlab.geom.prim.Camera3D;
import org.jlab.geom.prim.Line3D;

/**
 *
 * @author gavalian
 */
public class GeometryView extends JPanel implements MouseMotionListener {
    
    private ArrayList<Line3D>  canvasLines = new ArrayList<Line3D>();
    private Camera3D           camera      = new Camera3D();
    private Color      backgroundGradientUp   = new Color(25,42,62);
    private Color      backgroundGradientDown = new Color(27,63,79); 
    
    private List<Line3D>  axisX               = new ArrayList<Line3D>();
    private List<Line3D>  axisY               = new ArrayList<Line3D>();
    private List<Line3D>  axisZ               = new ArrayList<Line3D>();
    
    private int        mouseDraggedPositionX  = 0;
    private int        mouseDraggedPositionY  = 0;
    
    public GeometryView(){
        super();
        
        
        this.setSize(500, 500);
        this.addMouseMotionListener(this);
        
        this.initAxis(-50, 150,20);
        //this.camera.translateXYZ(0, 0, -100);
    }
    
    public void initAxis(double axisMin, double axisMax, double divisions){
        axisX.clear();
        axisY.clear();
        axisZ.clear();
        axisX.add(new Line3D( axisMin,0.0,0.0,axisMax,0.0,0.0));        
        
        axisY.add(new Line3D( 0.0,axisMin,0.0,0.0,axisMax,0.0));
        axisZ.add(new Line3D( 0.0,0.0,axisMin,0.0,0.0,axisMax));
        
        for(double loop = 0; loop < axisMax; loop += divisions){
            axisX.add(new Line3D(loop, -5,0, loop , 5,0));
            axisY.add(new Line3D(-5,loop,0, 5, loop , 0));
            axisZ.add(new Line3D( -5,0,loop , 5,0,loop));
        }
    }
    
    public Camera3D getCamera(){
        return this.camera;
    }
    
    @Override
    public void paint(Graphics g){
        try {        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        

        
        int w = this.getSize().width;
        int h = this.getSize().height;
        
        Color  background = new Color(25,42,62);
        g2d.setColor(background);
        g2d.fillRect(0, 0, w, h);
        
        /*
        GradientPaint paint = new GradientPaint(0,50,this.backgroundGradientDown, 
                0,100,this.backgroundGradientUp);
        g2d.setPaint(paint);
        g2d.fill(new Rectangle2D.Double(0,0,w,h));
        */
        this.camera.setCanvasSize(w, h);
        
        this.drawLines(g2d, new Color(255,0,0), axisX);
        this.drawLines(g2d, new Color(0,255,0), axisY);
        this.drawLines(g2d, new Color(0,0,255), axisZ);
        g2d.setColor(Color.WHITE);
        for(Line3D line : this.canvasLines){
            //System.out.println("---------------------------> ");
            //System.out.println(" ORIGINAL LINE ");
            //System.out.println(line);
            Line3D  cline = this.camera.getLine(line);
            //System.out.println(" CAMERA  LINE ");
            //System.out.println(cline);
            
            g2d.drawLine(
                    this.camera.getCanvasX(cline.origin()),
                    this.camera.getCanvasY(cline.origin()),
                    this.camera.getCanvasX(cline.end()),
                    this.camera.getCanvasY(cline.end())
                    );
        }
        } catch(Exception e){
            
        }
    }
    
    public void drawLines(Graphics2D g2d, Color color, List<Line3D> lines){
        try {
            g2d.setColor(color);
            for(Line3D line : lines){
                Line3D  cline = this.camera.getLine(line);
                System.out.println(" DRAWING AXIS LINES ");
                g2d.drawLine(
                        this.camera.getCanvasX(cline.origin()),
                        this.camera.getCanvasY(cline.origin()),
                        this.camera.getCanvasX(cline.end()),
                        this.camera.getCanvasY(cline.end())
                );
            }
        } catch (Exception e) {
            
        }
    }
    
    public void addLines(List<Line3D> lines){
        for(Line3D line : lines) this.canvasLines.add(line);
    }
    
    public void addLine(Line3D line){
        this.canvasLines.add(line);
        this.repaint();
    }
    
    public static void main(String[] args){
        JFrame  frame = new JFrame();
        frame.setSize(600, 600);
        GeometryView view = new GeometryView();
        frame.add(view);
        frame.setVisible(true);
        
        for(int sector = 0; sector < 2; sector++){
            for(int loop = 0; loop < 30; loop++){
                Box3D  box = new Box3D(20 + 5*loop ,10,10);
                box.translateXYZ(0, 60 + 10*loop, 350);
                //box.rotateZ(Math.toRadians(60*sector));
                box.rotateZ(Math.toRadians(60.0*sector));
                view.addLines(box.getLines());
            }
        }
        
        Box3D  box2 = new Box3D(80,80,80);

        view.addLines(box2.getLines());
        
        /*
        view.addLine(new Line3D(100.0,-100,0.0, 100.0,100,0.0));
        view.addLine(new Line3D(100.0, 100,0.0,-100.0,100,0.0));
        view.addLine(new Line3D(-100.0, 100,0.0,-100.0,-100,0.0));
        view.addLine(new Line3D(-100.0, -100,0.0,100.0,-100,0.0));
        
        view.addLine(new Line3D(100.0,-100, 200.0, 100.0,100,200.0));
        view.addLine(new Line3D(100.0, 100, 200.0,-100.0,100,200.0));
        view.addLine(new Line3D(-100.0, 100, 200.0,-100.0,-100,200.0));
        view.addLine(new Line3D(-100.0, -100, 200.0,100.0,-100,200.0));
        
        view.addLine(new Line3D(100.0,-100, 200.0, 100.0,-100,0.0));
        view.addLine(new Line3D(100.0, 100, 200.0, 100.0,100,0.0));
        view.addLine(new Line3D(-100.0, 100, 200.0,-100.0,100,0.0));
        view.addLine(new Line3D(-100.0, -100, 200.0,-100.0,-100,0.0));
        */
        //view.getCamera().rotateZ(Math.toRadians(0.05));
        /*
        for(int loop = 0; loop < 20; loop++){
            double y = -20 + 10 * loop;
            view.addLine(new Line3D(-100.0,y,0.0,100.0,y,0.0));
        }*/
        
        
    }

    public void mouseDragged(MouseEvent e) {
        //System.out.println("Mouse Dragged " + e.getXOnScreen() + " " + e.getYOnScreen()
        //+ "  " );
        int xDragged = e.getXOnScreen() - this.mouseDraggedPositionX;
        int yDragged = e.getYOnScreen() - this.mouseDraggedPositionY;
        
        //System.out.println("DRAGGED : " + xDragged + " : " + yDragged);
        
        if(Math.abs(xDragged)<10&&Math.abs(yDragged)<10){
            double angle = Math.atan2(yDragged, xDragged);
            angle = angle/25.0;
            //this.camera.rotateX(Math.toRadians(xDragged));
            //this.camera.rotateY(Math.toRadians(yDragged));
            if(xDragged>yDragged){
                this.camera.rotateY(angle);
            } else {
                this.camera.rotateX(angle);
            }
            this.repaint();
        }
        
        this.mouseDraggedPositionX = e.getXOnScreen();
        this.mouseDraggedPositionY = e.getYOnScreen();
    }

    public void mouseMoved(MouseEvent e) {
        //System.out.println("Mouse Moved");
    }
}
