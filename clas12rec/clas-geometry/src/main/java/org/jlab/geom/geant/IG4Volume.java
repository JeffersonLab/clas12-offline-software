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

/**
 *
 * @author gavalian
 */
public interface IG4Volume {
    
    String   getName();
    String   getParent();
    String   gemcString();
    Shape3D  getShape();
    GemcDetector  getDetector();
    G4Material getMaterial();
    void       setMaterial(G4Material mat);
    
    List<Point3D> getPoints();
    
    int[]         getFaces();
    float[]       getMeshPoints();
    int[]         getMeshFaces();
}
