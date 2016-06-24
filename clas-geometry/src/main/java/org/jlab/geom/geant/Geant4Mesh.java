/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.geant;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;

/**
 *
 * @author gavalian
 */
public class Geant4Mesh {
    
    String meshName = "G4Mesh";
    MeshView volumeMesh = null;
    
    public Geant4Mesh(){
        
    }
    
    
    public final void createMesh(Geant4Basic volume){
        this.meshName = volume.getName();
        this.volumeMesh = Geant4Mesh.makeMesh(volume);
    }
    /**
     * Creates a JavaFX Mesh object from given volume, they type string
     * determines which function should be called.
     * @param volume
     * @return 
     */
    public static MeshView makeMesh(Geant4Basic volume){        
        if(volume.getType().compareTo("box")==0)
            return Geant4Mesh.makeMeshBox(volume);
        return null;
    }
    
    public static List<MeshView> getMesh(Geant4Basic volume){
        List<MeshView> meshes = new ArrayList<MeshView>();
        Transformation3D  vT  = new Transformation3D();

        for(Geant4Basic item : volume.getChildren()){
            vT.clear();

            vT.append(item.rotation());
            vT.append(item.translation());
            vT.append(volume.rotation());
            vT.append(volume.translation());            
            System.out.println("[creating mesh] --> name : " + item.getName());
            //vT.show();
            if(item.getType().compareTo("box")==0){
                MeshView  mesh = Geant4Mesh.makeMeshBox(item, vT);                
                meshes.add(mesh);
            }
            if(item.getType().compareTo("trap")==0){
                MeshView  mesh = Geant4Mesh.makeMeshTrap(item, vT);                
                meshes.add(mesh);
            }
            if(item.getType().compareTo("trd")==0){
                MeshView  mesh = Geant4Mesh.makeMeshTrd(item, vT);                
                meshes.add(mesh);
            }
        }
        return meshes;
    }
    public static MeshView makeMeshTrd(Geant4Basic volume, Transformation3D tr){
        double[] p = volume.getParameters();
        float[] points = new float[]{
            (float) -p[0], (float) -p[2], (float) -p[4],
            (float) -p[0], (float)  p[2], (float) -p[4],
            (float) -p[0], (float)  p[2], (float)  p[4],
            (float) -p[0], (float) -p[2], (float)  p[4],
            
            (float)  p[1], (float) -p[3], (float) -p[4],
            (float)  p[1], (float)  p[3], (float) -p[4],
            (float)  p[1], (float)  p[3], (float)  p[4],
            (float)  p[1], (float) -p[3], (float)  p[4]                     
        };
        int[]  faces = new int[]{
            0,0,  3,0, 2,0,
            2,0,  1,0, 0,0,
            4,0,  5,0, 6,0,
            6,0,  7,0, 4,0,
            1,0,  2,0, 6,0,
            6,0,  5,0, 1,0,
            0,0,  1,0, 5,0,
            5,0,  4,0, 0,0,
            0,0,  4,0, 7,0,
            7,0,  3,0, 0,0,
            2,0,  3,0, 7,0,
            7,0,  6,0, 2,0 
        };
        Point3D  point = new Point3D();
        for(int loop = 0; loop < 8; loop++){
            int index = loop*3;
            point.set(points[index], points[index+1], points[index+2]);
            tr.apply(point);
            points[index]   = (float) point.x();
            points[index+1] = (float) point.y();
            points[index+2] = (float) point.z();
        }
        /*
        System.out.println(" TRD = " );
        for(int i = 0; i < points.length; i++){
            System.out.print("  " + points[i]);
            if((i+1)%3==0) System.out.println();
        }*/
        TriangleMesh boxMesh = new TriangleMesh();
        //System.out.println("CREATING MESH");
        boxMesh.getTexCoords().addAll(0, 0);
        boxMesh.getPoints().addAll(points);
        boxMesh.getFaces().addAll(faces);
        return new MeshView(boxMesh);
    }
    
    public static MeshView makeMeshTrap(Geant4Basic volume, Transformation3D tr){  
        float[] p = Geant4Mesh.getPointsTrap(volume.getParameters());
        int[]  faces = new int[]{
            0,0,  3,0, 2,0,
            2,0,  1,0, 0,0,
            4,0,  5,0, 6,0,
            6,0,  7,0, 4,0,
            1,0,  2,0, 6,0,
            6,0,  5,0, 1,0,
            0,0,  1,0, 5,0,
            5,0,  4,0, 0,0,
            0,0,  4,0, 7,0,
            7,0,  3,0, 0,0,
            2,0,  3,0, 7,0,
            7,0,  6,0, 2,0 
        };
        Point3D  point = new Point3D();
        for(int loop = 0; loop < 8; loop++){
            int index = loop*3;
            point.set(p[index], p[index+1], p[index+2]);
            tr.apply(point);
            p[index]   = (float) point.x();
            p[index+1] = (float) point.y();
            p[index+2] = (float) point.z();
        }
        
        TriangleMesh boxMesh = new TriangleMesh();
        //System.out.println("CREATING MESH");
        boxMesh.getTexCoords().addAll(0, 0);
        boxMesh.getPoints().addAll(p);
        boxMesh.getFaces().addAll(faces);
        return new MeshView(boxMesh);
    }
    /**
     * Creates a JavaFX Mesh for a BOX object, it can be viewed in the JavaFX scene.
     * @param volume
     * @return 
     */
    public static MeshView makeMeshBox(Geant4Basic volume){        
        double[] p = volume.getParameters();
        double[] tr = volume.getPosition();        
        
        float[] points = new float[]{
            (float) -p[0], (float) -p[1], (float) -p[2],
            (float) -p[0], (float)  p[1], (float) -p[2],
            (float) -p[0], (float)  p[1], (float)  p[2],
            (float) -p[0], (float) -p[1], (float)  p[2],
            
            (float)  p[0], (float) -p[1], (float) -p[2],
            (float)  p[0], (float)  p[1], (float) -p[2],
            (float)  p[0], (float)  p[1], (float)  p[2],
            (float)  p[0], (float) -p[1], (float)  p[2]                     
        };
        
        int[]  faces = new int[]{
            0,0,  3,0, 2,0,
            2,0,  1,0, 0,0,
            4,0,  5,0, 6,0,
            6,0,  7,0, 4,0,
            1,0,  2,0, 6,0,
            6,0,  5,0, 1,0,
            0,0,  1,0, 5,0,
            5,0,  4,0, 0,0,
            0,0,  4,0, 7,0,
            7,0,  3,0, 0,0,
            2,0,  3,0, 7,0,
            7,0,  6,0, 2,0                                  
        };
        
        TriangleMesh boxMesh = new TriangleMesh();
        //System.out.println("CREATING MESH");
        boxMesh.getTexCoords().addAll(0, 0);
        boxMesh.getPoints().addAll(points);
        boxMesh.getFaces().addAll(faces);
        return new MeshView(boxMesh);
    }
    /**
     * Creates a JavaFX Mesh for a BOX object, it can be viewed in the JavaFX scene.
     * @param volume
     * @return 
     */
    public static MeshView makeMeshBox(Geant4Basic volume,Transformation3D trans){
        
        double[] p = volume.getParameters();
        double[] tr = volume.getPosition();        
        Point3D  point = new Point3D();
        float[]  points = new float[8*3];
        
        point.set(-p[0], -p[1], -p[2]);
        trans.apply(point);
        points[0] = (float) point.x();
        points[1] = (float) point.y();
        points[2] = (float) point.z();
        point.set(-p[0],  p[1], -p[2]);
        trans.apply(point);
        points[3] = (float) point.x();
        points[4] = (float) point.y();
        points[5] = (float) point.z();
        point.set(-p[0],  p[1],  p[2]);
        trans.apply(point);
        points[6] = (float) point.x();
        points[7] = (float) point.y();
        points[8] = (float) point.z();
        point.set(-p[0], -p[1],  p[2]);
        trans.apply(point);
        points[9] = (float) point.x();
        points[10] = (float) point.y();
        points[11] = (float) point.z();
        
        point.set(p[0], -p[1], -p[2]);
        trans.apply(point);
        points[12] = (float) point.x();
        points[13] = (float) point.y();
        points[14] = (float) point.z();
        point.set(p[0],  p[1], -p[2]);
        trans.apply(point);
        points[15] = (float) point.x();
        points[16] = (float) point.y();
        points[17] = (float) point.z();
        point.set(p[0],  p[1],  p[2]);
        trans.apply(point);
        points[18] = (float) point.x();
        points[19] = (float) point.y();
        points[20] = (float) point.z();
        point.set(p[0], -p[1],  p[2]);
        trans.apply(point);
        points[21] = (float) point.x();
        points[22] = (float) point.y();
        points[23] = (float) point.z();
        /*
        float[] points = new float[]{
            (float) -p[0], (float) -p[1], (float) -p[2],
            (float) -p[0], (float)  p[1], (float) -p[2],
            (float) -p[0], (float)  p[1], (float)  p[2],
            (float) -p[0], (float) -p[1], (float)  p[2],
            
            (float)  p[0], (float) -p[1], (float) -p[2],
            (float)  p[0], (float)  p[1], (float) -p[2],
            (float)  p[0], (float)  p[1], (float)  p[2],
            (float)  p[0], (float) -p[1], (float)  p[2]                     
        };*/
        
        int[]  faces = new int[]{
            0,0,  3,0, 2,0,
            2,0,  1,0, 0,0,
            4,0,  5,0, 6,0,
            6,0,  7,0, 4,0,
            1,0,  2,0, 6,0,
            6,0,  5,0, 1,0,
            0,0,  1,0, 5,0,
            5,0,  4,0, 0,0,
            0,0,  4,0, 7,0,
            7,0,  3,0, 0,0,
            2,0,  3,0, 7,0,
            7,0,  6,0, 2,0                                  
        };
        
        TriangleMesh boxMesh = new TriangleMesh();
        //System.out.println("CREATING MESH");
        boxMesh.getTexCoords().addAll(0, 0);
        boxMesh.getPoints().addAll(points);
        boxMesh.getFaces().addAll(faces);
        return new MeshView(boxMesh);
    }
    
    
    public static float[]   getPointsBox(int dx, int dy, int dz){
        float[] p = new float[]{
            (float) -dx, (float) -dy, (float) -dz,
            (float) -dx, (float)  dy, (float) -dz,
            (float) -dx, (float)  dy, (float)  dz,
            (float) -dx, (float) -dy, (float)  dz,
            
            (float)  dx, (float) -dy, (float) -dz,
            (float)  dx, (float)  dy, (float) -dz,
            (float)  dx, (float)  dy, (float)  dz,
            (float)  dx, (float) -dy, (float)  dz
        };
        return p;
    }
    
    public static float[]  getPointsTrap(double... pars){
        if(pars.length!=11){
            System.out.println("[Geatn4::getPointsTrap] --> parameter length is wrong ["
            + pars.length + "]  must be 11" );
            return null;
        }
        double pDz    = pars[ 0];
        double pTheta = pars[ 1];
        double pPhi   = pars[ 2];
        double pDy1   = pars[ 3];
        double pDx1   = pars[ 4];
        double pDx2   = pars[ 5];
        double pAlp1  = pars[ 6];
        double pDy2   = pars[ 7];
        double pDx3   = pars[ 8];
        double pDx4   = pars[ 9];
        double pAlp2  = pars[10];

        
        double rt = pDz*Math.sin(pTheta);
        double rx = rt*Math.sin(pPhi);
        double ry = rt*Math.cos(pPhi);
        //---------------------------
        // PB describes the point of the bottom parallel face of the trapesoid
        float[] pB = new float[]{
            (float) (-pDx1 - pDy1*Math.tan(pAlp1)+rx), (float) (-pDy1 + ry), (float) -pDz,
            (float) ( pDx1 - pDy1*Math.tan(pAlp1)+rx), (float) (-pDy1 + ry), (float) -pDz,
            (float) ( pDx2 + pDy1*Math.tan(pAlp1)+rx), (float) ( pDy1 + ry), (float) -pDz,
            (float) (-pDx2 + pDy1*Math.tan(pAlp1)+rx), (float) ( pDy1 + ry), (float) -pDz,            
        };
        
        float[] pT = new float[]{
            (float) (-pDx3 - pDy2*Math.tan(pAlp2) - rx), (float) (-pDy2 - ry), (float) pDz,
            (float) ( pDx3 - pDy2*Math.tan(pAlp2) - rx), (float) (-pDy2 - ry), (float) pDz,
            (float) ( pDx4 + pDy2*Math.tan(pAlp2) - rx), (float) ( pDy2 - ry), (float) pDz,
            (float) (-pDx4 + pDy2*Math.tan(pAlp2) - rx), (float) ( pDy2 - ry), (float) pDz,
        };
        
        float[] p = new float[8*3];
        for(int loop = 0; loop < pB.length; loop++) p[loop] = pB[loop];
        for(int loop = 0; loop < pT.length; loop++) p[loop+pB.length] = pT[loop];
        
        return p;
    }    
    
    public static void main(String[] args){
        Geant4Basic  box = new Geant4Basic("box_1","box",20,20,120);
        MeshView view = Geant4Mesh.makeMesh(box);
    }
}
