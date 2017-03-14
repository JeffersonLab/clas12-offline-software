/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.geant;

import java.util.List;
import org.jlab.geom.gemc.GemcDetector;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Shape3D;
import org.jlab.geom.prim.Transformation3D;
import org.jlab.geom.prim.Transformation3D.Transform;

/**
 *
 * @author gavalian
 */
public class G4BaseVolume implements IG4Volume  {

    String volumeName = "";
    String volumeParentName = "";
    GemcDetector   detector = new GemcDetector();
    Transformation3D   translation = new Transformation3D();
    Transformation3D   rotation    = new Transformation3D();
    
    
    public void addTranslation(double dx, double dy, double dz){
        translation.clear();
        translation.translateXYZ(dx, dy, dz);
        this.detector.put("pos", String.format("%.5f %.5f %.5f", dx,dy,dz));
    }
        
    
    public void setDescription(String desc){
        this.detector.put("description", desc);        
    }
    
    public void addRoration(String coord1, Double angle1,
            String coord2, Double angle2,
            String coord3, Double angle3
            ){
        StringBuilder order = new StringBuilder();
        order.append("ordered: ");
        order.append(coord1);
        order.append(coord2);
        order.append(coord3);
        order.append(String.format("  %.2f*deg %.2f*deg %.2f*deg", angle1,angle2,angle3));
        this.getDetector().put("rotation", order.toString());
    }
    
    public void addRotation(
            Transform tr1, Transform tr2, Transform tr3){
        StringBuilder order = new StringBuilder();
        order.append(tr1.getName().charAt(1));
        order.append(tr2.getName().charAt(1));
        order.append(tr3.getName().charAt(1));
        
        rotation.clear();
        rotation.append(tr1);
        rotation.append(tr2);
        rotation.append(tr3);
        
        //this.detector.put("", String.format("ordered: %s %.3f*deg %.3f*deg %.3f*deg", order,
        //        tr1.));
    }
    
    public String getName() {
        return this.volumeName;
    }

    public String getParent() {
        return this.volumeParentName;
    }

    public String gemcString() {
       return this.detector.toString();
    }

    public Shape3D getShape() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public G4Material getMaterial() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setMaterial(G4Material mat) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public List<Point3D> getPoints() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int[] getFaces() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public float[] getMeshPoints() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int[] getMeshFaces() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public GemcDetector getDetector() {
        return this.detector;
    }
    
}
