/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.geom.gui;

import java.awt.Dimension;
import javax.swing.JFrame;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.detector.ec.ECFactory;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Point3D;

/**
 *
 * @author gavalian
 */
public class GeometryFrame extends JFrame {
//    private double xmin = -100;
//    private double xmax =  100;
//    private double ymin = -100;
//    private double ymax =  100;
    GeometryPanel panel = null;
    public GeometryFrame(int xsize, int ysize,double xm, double ym){
        initUI(xsize,ysize,xm,ym);
    }
    
    public void addLineXY(Line3D line){
        panel.addLineXY(line);
        panel.repaint();
    }
    
    public void addLineXZ(Line3D line){
        panel.addLineXZ(line);
        panel.repaint();
    }
    
    public void addLineYZ(Line3D line){
        panel.addLineYZ(line);
        panel.repaint();
    }
    
    private void initUI(int xsize, int ysize, double xm, double ym){
        setTitle("Geometry Frame");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel = new GeometryPanel(xsize,ysize,xm,ym);
        panel.setSize(xsize,ysize);
        add(panel);
        setSize(xsize,ysize);
        panel.repaint();
        this.setPreferredSize(new Dimension(xsize,ysize));
        setLocationRelativeTo(null); 
        this.pack();
    }
    
    public static void main(String[] args){
        GeometryFrame frame = new GeometryFrame(800,800,600,600);
        frame.setVisible(true);
        System.err.println("adding lines");
        //ConstantProvider data = DataBaseLoader.
        //ECFactory factory = new ECFactory();
        //ECLayer   layerU = factory
//        Transformation3D xform = new Transformation3D();
//        xform.translateXYZ(230, 0, 0);
//        xform.rotateZ(Math.toRadians(-90));
//        xform.rotateX(Math.toRadians(-90));
//        
//        Detector detector = (new ECFactory()).createDetectorCLAS(DataBaseLoader.getCalorimeterConstants());
//        
//        for (int superlayerId=0; superlayerId<1; superlayerId++) {
//            Superlayer superlayer = detector.getSector(0).getSuperlayer(superlayerId);
//            for (int layerId=0; layerId<3; layerId++) {
//                Layer layer = superlayer.getLayer(layerId);
//                Shape3D boundary = layer.getBoundary();
//                for (int f=0; f<boundary.size(); f++) {
//                    Face3D face = boundary.face(f);
//                    for (int p=0; p<3; p++) {
//                        Line3D line = new Line3D(face.point(p), face.point((p+1)%3));
//                        line.translateXYZ(-150, 0, 0);
//                        frame.addLineXY(line);
//                    }
//                }
//            }
//        }
//        
//        
//        Path3D path = new Path3D();
//        for (int i=0; i<1000; i++) {
//            path.generateRandom(0, 0, 0, 0, 90, 0, 360, 1000, 2);
//            
//            List<DetectorHit> hits = detector.getHits(path);
//            System.out.println(hits.size());
//        }
        
//        Path3D path = new Path3D();
//        path.addPoint(0, 0, 100);
//        path.addPoint(0, 1, -100);
//        
//        ScintillatorPaddle paddle = new ScintillatorPaddle(0, 10, 10, 10);
//        
////        for (int f=paddle.g)
////        for (int p=0; p<3; p++) {
////            Line3D line = new Line3D(face.point(p), face.point((p+1)%3));
////            frame.addLineXY(line);
////        }
//        Point3D pt = new Point3D(9, 9, 9);
////        boolean b = paddle.getLayerIntersection(path.getLine(0), pt);
//        
////        boolean b = face.intersection(path.getLine(0), pt);
//        System.out.println(path);
//        System.out.println(path.getLine(0));
////        System.out.println(face);
//        System.out.println(b+" "+pt);
        
        
        
        frame.repaint();
    }
}
