/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.abs;

import java.util.ArrayList;
import java.util.List;
import org.jlab.geom.base.Component;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Mesh3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Shape3D;
import org.jlab.geom.prim.Transformation3D;

/**
 *
 * @author gavalian
 */
public class MeshComponent implements Component {
    private final int compnentID;
    private Mesh3D    componentMesh;
    
    public MeshComponent(int id){
        this.compnentID = id;
    }
    
    @Override
    public int getComponentId() {
        return this.compnentID;
    }

    @Override
    public int getNumVolumePoints() {
        return this.componentMesh.getNumPoints();
    }

    @Override
    public Point3D getVolumePoint(int p) {
        Point3D pnt = new Point3D();
        this.componentMesh.getPoint(p, pnt);
        return pnt;
    }

    @Override
    public int getNumVolumeEdges() {
        return 0;
    }

    @Override
    public Line3D getVolumeEdge(int e) {
        return new Line3D();
    }

    @Override
    public Shape3D getVolumeShape() {
        return new Shape3D();
    }

    @Override
    public List<Line3D> getVolumeCrossSection(Transformation3D transformation) {
        return new ArrayList<Line3D>();
    }

    @Override
    public boolean getVolumeIntersection(Line3D line, Point3D inIntersect, Point3D outIntersect) {
        return false;
    }

    @Override
    public Point3D getMidpoint() {
        return new Point3D();
    }

    @Override
    public double getLength() {
        return 0.0;
    }
    
    public Line3D getLineX(){
        return this.componentMesh.getLineX();
    }
    
    public Line3D getLineY(){
        return this.componentMesh.getLineY();
    }
    
    public Line3D getLineZ(){
        return this.componentMesh.getLineZ();
    }
    
    @Override
    public String getType() {
        return "mesh";
    }

    @Override
    public void show() {
        System.out.println("Mesh3D component");
    }

    @Override
    public void translateXYZ(double dx, double dy, double dz) {
        this.componentMesh.translateXYZ(dx, dy, dz);
    }

    @Override
    public void rotateX(double angle) {
        this.componentMesh.rotateX(angle);
    }

    @Override
    public void rotateY(double angle) {
        this.componentMesh.rotateY(angle);
    }

    @Override
    public void rotateZ(double angle) {
        this.componentMesh.rotateZ(angle);
    }
    
    
    public Mesh3D  getVolumeMesh(){
        return this.componentMesh;
    }
    
    public void init(String type, double... pars){
        if(type.compareTo("box")==0){
            this.componentMesh = Mesh3D.box((float) pars[0], (float) pars[1], 
                    (float) pars[2]);
        }
    }
}
