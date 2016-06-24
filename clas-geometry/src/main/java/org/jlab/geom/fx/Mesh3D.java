/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.fx;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Triangle3D;

/**
 *
 * @author gavalian
 */
public class Mesh3D {
    
    
    MeshView        meshView = new MeshView();
    Triangle3D       triFace = new Triangle3D();
    
    
    public Mesh3D(){
        
    }
    
    
    public void initSphere(){
        
    }
    
    public void initTrap(float g4trd_dx1, float g4trd_dx2, float g4trd_dy1, 
            float g4trd_dy2, float g4trd_dz){
        
        TriangleMesh mesh = new TriangleMesh();
        
        mesh.getPoints().clear();
        mesh.getFaces().clear();
        
        mesh.getPoints().addAll(
         -g4trd_dx1, -g4trd_dy1, -g4trd_dz,
          g4trd_dx1, -g4trd_dy1, -g4trd_dz,
          g4trd_dx1,  g4trd_dy1, -g4trd_dz,
         -g4trd_dx1,  g4trd_dy1, -g4trd_dz,
         -g4trd_dx2, -g4trd_dy2,  g4trd_dz,
          g4trd_dx2, -g4trd_dy2,  g4trd_dz,
          g4trd_dx2,  g4trd_dy2,  g4trd_dz,
         -g4trd_dx2,  g4trd_dy2,  g4trd_dz
        );
        
        mesh.getFaces().addAll(
                0,0, 1,0, 2,0,
                2,0, 3,0, 0,0,
                0,0, 4,0, 5,0,
                0,0, 5,0, 1,0,

                1,0, 5,0, 6,0,
                1,0, 6,0, 2,0,
                2,0, 6,0, 7,0,
                2,0, 7,0, 3,0,

                3,0, 7,0, 4,0,
                3,0, 4,0, 0,0,
                4,0, 5,0, 6,0,
                4,0, 6,0, 7,0
        );
        //meshView.getMaterial().
    }
    
    public List<Point3D>  getIntersection(Line3D line){
        
        List<Point3D>  ipoints = new ArrayList<Point3D>();
        List<Point3D>  lp      = new ArrayList<Point3D>();
        
        TriangleMesh mesh = (TriangleMesh) this.meshView.getMesh();
        int npoints = mesh.getPoints().size()/3;
        int nfaces  = mesh.getFaces().size()/2/3;
        int sindex  = 0;
        for(int loop = 0; loop < nfaces; loop++){
            sindex = loop*6*3;
            this.triFace.set(
                    mesh.getFaces().get(sindex), 
                    mesh.getFaces().get(sindex+2),
                    mesh.getFaces().get(sindex+4),
                    
                    mesh.getFaces().get(sindex+6), 
                    mesh.getFaces().get(sindex+8),
                    mesh.getFaces().get(sindex+10),
                    
                    mesh.getFaces().get(sindex+12), 
                    mesh.getFaces().get(sindex+14),
                    mesh.getFaces().get(sindex+16)
            );
            this.triFace.intersectionSegment(line, lp);
            ipoints.addAll(lp);
        }
        return ipoints;
    }
    
    public List<Point3D>  getIntersection(Path3D  path){
        List<Point3D>  ipoints = new ArrayList<Point3D>();
        int nlines = path.getNumLines();
        for(int loop = 0; loop < nlines; loop++){
            List<Point3D>  p = this.getIntersection(path.getLine(loop));
            ipoints.addAll(p);
        }
        return ipoints;
    }
    
    public MeshView getMesh(){
        return this.meshView;
    }
    
    
}
