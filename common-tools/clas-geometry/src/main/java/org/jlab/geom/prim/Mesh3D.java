/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.prim;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.jlab.geom.Showable;

/**
 *
 * @author gavalian
 */
public class Mesh3D implements Transformable, Showable{
    
    private float[]  meshPoints = null;
    private int[]     meshFaces  = null;

    public Mesh3D(float[] points, int[] faces){
        this.set(points, faces);
    }
    
    public final void set(float[] points, int[] faces){
        this.meshPoints = new float[points.length];
        this.meshFaces  = new int[faces.length];
        System.arraycopy(points, 0, this.meshPoints, 0, points.length);
        System.arraycopy(faces, 0, this.meshFaces, 0, faces.length);
    }
    
    @Override
    public void translateXYZ(double dx, double dy, double dz) {
        for(int p = 0; p < this.meshPoints.length; p+=3){
            this.meshPoints[p]   += dx;
            this.meshPoints[p+1] += dy;
            this.meshPoints[p+2] += dz;
        }
    }

    @Override
    public void rotateX(double angle) {
        Point3D point = new Point3D();
        for(int p = 0; p < this.meshPoints.length; p+=3){
            point.set(meshPoints[p],meshPoints[p+1],meshPoints[p+2]);            
            point.rotateX(angle);
            meshPoints[p]   = (float) point.x();
            meshPoints[p+1] = (float) point.y();
            meshPoints[p+2] = (float) point.z();
        }
    }

    @Override
    public void rotateY(double angle) {
        Point3D point = new Point3D();
        for(int p = 0; p < this.meshPoints.length; p+=3){
            point.set(meshPoints[p],meshPoints[p+1],meshPoints[p+2]);            
            point.rotateY(angle);
            meshPoints[p]   = (float) point.x();
            meshPoints[p+1] = (float) point.y();
            meshPoints[p+2] = (float) point.z();
        }
    }

    @Override
    public void rotateZ(double angle) {
        Point3D point = new Point3D();
        for(int p = 0; p < this.meshPoints.length; p+=3){
            point.set(meshPoints[p],meshPoints[p+1],meshPoints[p+2]);            
            point.rotateZ(angle);
            meshPoints[p]   = (float) point.x();
            meshPoints[p+1] = (float) point.y();
            meshPoints[p+2] = (float) point.z();
        }
    }

    @Override
    public void show() {
        System.out.println("Mesh3D Object: points = " + meshPoints.length 
                + " faces = " + meshFaces.length);
        int npoints = meshPoints.length;
        int nfaces  = meshFaces.length;
        for(int p = 0 ; p < npoints; p+=3){
            System.out.println(String.format("\t p : %8.4f %8.4f %8.4f", 
                    meshPoints[p],meshPoints[p+1],meshPoints[p+2]));
        }
        for(int f = 0 ; f < nfaces; f+=3){
            System.out.println(String.format("\t p : %8d %8d %8d", 
                    meshFaces[f],meshFaces[f+1],meshFaces[f+2]));
        }
    }
    
    public int getNumPoints(){
        return this.meshPoints.length/3;
    }
    
    public void getPoint(int n, Point3D point){
        point.set(this.meshPoints[n*3],this.meshPoints[n*3+1],this.meshPoints[n*3+2]);
    }
    
    public int  getNumFaces(){
        return this.meshFaces.length/3;
    }
    public void getFace(int face, Triangle3D tri){
        int start  = face*3;
        
        int index1 = 3*meshFaces[start];
        int index2 = 3*meshFaces[start+1];
        int index3 = 3*meshFaces[start+2];        
        tri.set(
                meshPoints[index1],meshPoints[index1+1],meshPoints[index1+2],
                meshPoints[index2],meshPoints[index2+1],meshPoints[index2+2],
                meshPoints[index3],meshPoints[index3+1],meshPoints[index3+2]
                );
    }
    
    public void intersectionPath(Path3D path, List<Point3D> intersections){
        int nlines = path.getNumLines();
        for(int p = 0; p < nlines; p++){
            Line3D line = path.getLine(p);
            this.intersectionSegment(line, intersections);
        }
    }
    /**
     * Finds intersections of the given ray with this shape. 
     * Intersection points will be appended to the given list.
     *
     * @param line the ray
     * @param intersections the list of intersections
     * @return the number of intersections that were found
     */
    public int intersectionRay(final Line3D line, List<Point3D> intersections) {
        int count = 0;
        int nFaces = meshFaces.length/3;
        Triangle3D face = new Triangle3D();
        for (int f = 0; f < nFaces; f++){
            this.getFace(f, face);
            count += face.intersectionRay(line, intersections);
        }
        return count;
    }
    
    /**
     * Finds intersections of the given line segment with this shape. 
     * Intersection points will be appended to the given list.
     * 
     * @param line the line segment
     * @param intersections the list of intersections
     * @return the number of intersections that were found
     */
    public int intersectionSegment(final Line3D line, List<Point3D> intersections) {
        int count = 0;
        int nFaces = meshFaces.length/3;
        Triangle3D face = new Triangle3D();
        for (int f = 0; f < nFaces; f++){
            this.getFace(f, face);
            count += face.intersectionSegment(line, intersections);
        }
        return count;
    }
    
    /**
     * Returns true if the infinite line intersects this shape.
     * @param line the infinite line
     * @return true if the line intersects the shape
     */
    public boolean hasIntersection(Line3D line) {
        List<Point3D> list = new ArrayList();
        int count = 0;
        int nFaces = meshFaces.length/3;
        Triangle3D face = new Triangle3D();
        for (int f = 0; f < nFaces; f++){
            this.getFace(f, face);
            if( face.intersection(line, list)>0)return true;
        }
        return false;
    }
    
    /**
     * Returns true if the ray intersects this shape.
     * @param line the ray
     * @return true if the line intersects the shape
     */
    public boolean hasIntersectionRay(Line3D line) {
        List<Point3D> list = new ArrayList();
        /*for (Face3D face : faces)
            if (face.intersectionRay(line, list) > 0)
                return true;*/
        return false;
    }
    
    /**
     * Returns true if the line segment intersects this shape.
     * @param line the line segment
     * @return true if the line intersects the shape
     */
    public boolean hasIntersectionSegment(Line3D line) {
        List<Point3D> list = new ArrayList();
        /*
        for (Face3D face : faces)
            if (face.intersectionSegment(line, list) > 0)
                return true;*/
        return false;
    }
    
    public Point3D  getCenterX(int order){
        Point3D  point  = new Point3D();
        Point3D  center = new Point3D();
        int offset = order*4;
        for(int p = 0; p < 4; p++){
            this.getPoint(p+offset, point);
            center.set(center.x()+point.x(),
                    center.y()+point.y(),center.z()+point.z());
        }
        center.set(center.x()*0.25, center.y()*0.25, center.z()*0.25);
        return center;
    }
    
    
    public Point3D  getCenterY(int order){
        Point3D  point  = new Point3D();
        Point3D  center = new Point3D();
        int offset = order*4;
        if(order==0){
            this.getPoint(0, point);
            center.set(center.x()+point.x(),
                    center.y()+point.y(),center.z()+point.z());
            this.getPoint(3, point);
            center.set(center.x()+point.x(),
                    center.y()+point.y(),center.z()+point.z());
            this.getPoint(4, point);
            center.set(center.x()+point.x(),
                    center.y()+point.y(),center.z()+point.z());
            this.getPoint(7, point);
            center.set(center.x()+point.x(),
                    center.y()+point.y(),center.z()+point.z());
        } else {
            this.getPoint(1, point);
            center.set(center.x()+point.x(),
                    center.y()+point.y(),center.z()+point.z());
            this.getPoint(2, point);
            center.set(center.x()+point.x(),
                    center.y()+point.y(),center.z()+point.z());
            this.getPoint(5, point);
            center.set(center.x()+point.x(),
                    center.y()+point.y(),center.z()+point.z());
            this.getPoint(6, point);
            center.set(center.x()+point.x(),
                    center.y()+point.y(),center.z()+point.z());
        }
        center.set(center.x()*0.25, center.y()*0.25, center.z()*0.25);
        return center;
    }
    
    public Point3D  getCenterZ(int order){
        Point3D  point  = new Point3D();
        Point3D  center = new Point3D();
        int offset = order*4;
        if(order==0){
            this.getPoint(0, point);
            center.set(center.x()+point.x(),
                    center.y()+point.y(),center.z()+point.z());
            this.getPoint(1, point);
            center.set(center.x()+point.x(),
                    center.y()+point.y(),center.z()+point.z());
            this.getPoint(4, point);
            center.set(center.x()+point.x(),
                    center.y()+point.y(),center.z()+point.z());
            this.getPoint(5, point);
            center.set(center.x()+point.x(),
                    center.y()+point.y(),center.z()+point.z());
        } else {
            this.getPoint(2, point);
            center.set(center.x()+point.x(),
                    center.y()+point.y(),center.z()+point.z());
            this.getPoint(3, point);
            center.set(center.x()+point.x(),
                    center.y()+point.y(),center.z()+point.z());
            this.getPoint(6, point);
            center.set(center.x()+point.x(),
                    center.y()+point.y(),center.z()+point.z());
            this.getPoint(7, point);
            center.set(center.x()+point.x(),
                    center.y()+point.y(),center.z()+point.z());
        }
        center.set(center.x()*0.25, center.y()*0.25, center.z()*0.25);
        return center;
    }
    
    public Line3D getLineX(){
        return new Line3D(this.getCenterX(0),this.getCenterX(1));
    }
    
    public Line3D getLineY(){
        return new Line3D(this.getCenterY(0),this.getCenterY(1));
    }
    
    public Line3D getLineZ(){
        return new Line3D(this.getCenterZ(0),this.getCenterZ(1));
    }
    
    public static Mesh3D box(float dx, float dy, float dz){
        float[] p = new float[]{
            -dx, -dy,  -dz,
            -dx,  dy,  -dz,
            -dx,  dy,   dz,
            -dx, -dy,   dz,
            
            dx,  -dy,  -dz,
            dx,   dy,  -dz,
            dx,   dy,   dz,
            dx,  -dy,   dz
        };
        
        int[]    f = new int[]{
            0,  3, 2,
            2,  1, 0,
            4,  5, 6,
            6,  7, 4,
            1,  2, 6,
            6,  5, 1,
            0,  1, 5,
            5,  4, 0,
            0,  4, 7,
            7,  3, 0,
            2,  3, 7,
            7,  6, 2
        };
        Mesh3D  mesh = new Mesh3D(p,f);
        return  mesh;
    }
    
    public MeshView  getMeshView(){
        
        float[] points = new float[this.meshPoints.length];
        System.arraycopy(this.meshPoints, 0, points, 0, points.length);
        int[] faces = new int[this.meshFaces.length*2];
        // Mesh view requires also to have texture mapping coordinates
        // so the faces array is twice as long, 0 are inserted.
        
        for(int f = 0; f < this.meshFaces.length; f++){
            faces[f*2]   = this.meshFaces[f];
            //System.out.println("f = " + f + "  length = " + this.meshFaces.length);
            faces[f*2+1] = 0;
        }
        /*
        for(int p = 0; p < faces.length; p++){
            System.out.print("  " + faces[p]);
            if((p+1)%6==0) System.out.println();
        }
        for(int p = 0; p < this.meshFaces.length; p++){
            System.out.print("  " + meshFaces[p]);
            if((p+1)%3==0) System.out.println();
        }*/
        TriangleMesh boxMesh = new TriangleMesh();
        //System.out.println("CREATING MESH");
        boxMesh.getTexCoords().addAll(0, 0);
        boxMesh.getPoints().addAll(points);
        boxMesh.getFaces().addAll(faces);
        return new MeshView(boxMesh);
    }
    /**
     * Main program for tests
     * @param args 
     */
    public static void main(String[] args){
        Mesh3D box = Mesh3D.box(10, 20, 60);
        
        Line3D  lineX = box.getLineX();
        Line3D  lineY = box.getLineY();
        Line3D  lineZ = box.getLineZ();
        lineX.show();
        lineY.show();
        lineZ.show();
        box.translateXYZ(100, 0.0, 0.0);        
        box.show();
        Line3D  line = new Line3D();        
        List<Point3D>  intersects = new ArrayList<Point3D>();
        
        for(int p = 0; p < 100; p++){
            double r = 600;
            double theta = Math.toRadians(90.0);//Math.toRadians(Math.random()*180.0);
            double phi   = Math.toRadians(Math.random()*360.0-180.0);
            line.set(0.0,0.0,0.0, 
                    Math.sin(theta)*Math.cos(phi)*r,
                    Math.sin(theta)*Math.sin(phi)*r,
                    Math.cos(theta)*r
                    );
            intersects.clear();
            box.intersectionRay(line, intersects);
            System.out.println("theta/phi = " + Math.toDegrees(theta) + "  "
                    +  Math.toDegrees(phi) + " intersects = " + intersects.size());
           
        }
    }
    
}
