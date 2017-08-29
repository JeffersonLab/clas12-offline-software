/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.volume;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Vector3d;
import org.jlab.geometry.prim.Trd;
import org.jlab.detector.units.SystemOfUnits.Length;
import org.jlab.geometry.prim.Line3d;
import org.jlab.geometry.prim.Ray3d;

/**
 *
 * @author kenjo
 */
public class G4Trd extends Geant4Basic {

    private Line3d localAxisX, localAxisY, localAxisZ;

    public G4Trd(String name, double pdx1, double pdx2, double pdy1, double pdy2, double pdz) {
        super(new Trd(pdx1, pdx2, pdy1, pdy2, pdz));
        setName(name);
        setType("Trd");
        setDimensions(Length.value(pdx1), Length.value(pdx2), Length.value(pdy1), Length.value(pdy2), Length.value(pdz));

        CSG volCSG = volumeSolid.toCSG();

        localAxisX = new Line3d(volCSG.getIntersections(new Ray3d(new Vector3d(0, 0, 0), new Vector3d(1, 0, 0))).get(0),
                volCSG.getIntersections(new Ray3d(new Vector3d(0, 0, 0), new Vector3d(-1, 0, 0))).get(0));
        localAxisY = new Line3d(volCSG.getIntersections(new Ray3d(new Vector3d(0, 0, 0), new Vector3d(0, 1, 0))).get(0),
                volCSG.getIntersections(new Ray3d(new Vector3d(0, 0, 0), new Vector3d(0, -1, 0))).get(0));
        localAxisZ = new Line3d(volCSG.getIntersections(new Ray3d(new Vector3d(0, 0, 0), new Vector3d(0, 0, 1))).get(0),
                volCSG.getIntersections(new Ray3d(new Vector3d(0, 0, 0), new Vector3d(0, 0, -1))).get(0));
    }

    @Override
    public Line3d getLineX() {
        return new Line3d(localAxisX).transformed(getGlobalTransform());
    }

    @Override
    public Line3d getLineY() {
        return new Line3d(localAxisY).transformed(getGlobalTransform());
    }

    @Override
    public Line3d getLineZ() {
        return new Line3d(localAxisZ).transformed(getGlobalTransform());
    }
}
