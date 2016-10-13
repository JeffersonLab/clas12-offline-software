/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.volume;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.jlab.geometry.prim.StlPrim;

/**
 *
 * @author kenjo
 */
public class G4Stl extends Geant4Basic {

    public G4Stl(String name, String filename) throws FileNotFoundException {
        this(name, new FileInputStream(new File(filename)));
    }

    public G4Stl(String name, InputStream stlstream) {
        super(new StlPrim(stlstream));
        setName(name);
        setType("Stl");
    }

    /*
    @Override
    public void afterCSGtransformation() {
        boundVol = volumeCSG.getBounds().toCSG();
    }
*/
    
    /*
    public Line3d getLineX() {
        Line3d xline = new Line3d(new Vector3d(-getXHalfLength(), 0, 0), new Vector3d(getXHalfLength(), 0, 0));
        return xline.transformed(getGlobalTransform());
    }

    public Line3d getLineY() {
        Line3d yline = new Line3d(new Vector3d(0, -getYHalfLength(), 0), new Vector3d(0, getYHalfLength(), 0));
        return yline.transformed(getGlobalTransform());
    }

    public Line3d getLineZ() {
        Line3d zline = new Line3d(new Vector3d(0, 0, -getZHalfLength()), new Vector3d(0, 0, getZHalfLength()));
        return zline.transformed(getGlobalTransform());
    }
     */
}
