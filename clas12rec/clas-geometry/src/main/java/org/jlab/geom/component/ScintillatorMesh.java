/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.component;

import org.jlab.geom.abs.AbstractComponent;
import org.jlab.geom.abs.MeshComponent;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;

/**
 *
 * @author gavalian
 */
public class ScintillatorMesh extends MeshComponent {

    public ScintillatorMesh(int id,double width, double length, double thickness) {
        super(id);
        this.init("box", width,length,thickness);
    }
    
    @Override
    public String getType() {
        return "Scintillator Mesh";
    }
    
}
