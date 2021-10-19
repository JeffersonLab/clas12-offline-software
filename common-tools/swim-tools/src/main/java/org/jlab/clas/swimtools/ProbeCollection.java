/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.swimtools;

import cnuphys.magfield.CompositeProbe;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.RotatedCompositeProbe;
import cnuphys.swimZ.SwimZ;

/**
 *
 * @author ziegler, heddle
 */
public class ProbeCollection {
    
    public final cnuphys.swimZ.SwimZ RCF_z;  //  rotated composite field - for swimming to fixed z 
    public final cnuphys.swimZ.SwimZ CF_z;   //  composite field - for swimming to fixed z 
    public final cnuphys.swim.Swimmer RCF;   //  rotated composite field 
    public final cnuphys.swim.Swimmer CF;    //  composite field 
    public final cnuphys.adaptiveSwim.AdaptiveSwimmer AS; // adaptive swimmer
    //Probes:
    public final RotatedCompositeProbe RCP;
    public final CompositeProbe CP; 
    /**
     * Gets rotated composite and composite fields, get corresponding probes
     */
    public ProbeCollection() {
        RCP =   new RotatedCompositeProbe(MagneticFields.getInstance().getRotatedCompositeField());
        CP  =   new CompositeProbe(MagneticFields.getInstance().getCompositeField());
        
        RCF_z   =   new SwimZ(MagneticFields.getInstance().getRotatedCompositeField());
        CF_z    =   new SwimZ(MagneticFields.getInstance().getCompositeField());
        RCF     =   new cnuphys.swim.Swimmer(MagneticFields.getInstance().getRotatedCompositeField());
        CF      =   new cnuphys.swim.Swimmer(MagneticFields.getInstance().getCompositeField());
        AS      =   new cnuphys.adaptiveSwim.AdaptiveSwimmer(MagneticFields.getInstance().getCompositeField());
    }
}
