/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.swimtools;
import cnuphys.magfield.MagneticFields;

import java.util.HashMap;
/**
 *
 * @author ziegler, heddle
 */


public class Swimmer {
    
    private static HashMap<Thread, ProbeCollection> swimmers = new HashMap<>();
    
    public static ProbeCollection getProbeCollection(Thread thr){
        return swimmers.get(thr);
    }
    public static void put(Thread thr, ProbeCollection PC) {
        swimmers.put(thr, PC);
    }
    
    public static synchronized void setMagneticFieldsScales(double SolenoidScale, double TorusScale, double shift) {
        if (FieldsLoaded) {
            return;
        }
        if(TorusScale==0)
            TorusScale=0.000001;
        MagneticFields.getInstance().getTorus().setScaleFactor(TorusScale);
        MagneticFields.getInstance().getSolenoid().setScaleFactor(SolenoidScale);
        MagneticFields.getInstance().setSolenoidShift(shift);
        setSolScale(SolenoidScale);
        setTorScale(TorusScale);
        //remove overlap for composite field
        //MagneticFields.getInstance().removeMapOverlap();
        FieldsLoaded = true;
        System.out.println(" TRACKING ***** ****** ****** THE TORUS IS BEING SCALED BY " + (TorusScale * 100) + "  %   *******  ****** **** ");
        System.out.println(" TRACKING ***** ****** ****** THE SOLENOID IS BEING SCALED BY " + (SolenoidScale * 100) + "  %   *******  ****** **** ");   
    }

    private static double SOLSCALE = -1;
    private static double TORSCALE = -1;
    
    public static synchronized void setSolScale(double s) {
        SOLSCALE = s;
    }
    public static synchronized void setTorScale(double s) {
        TORSCALE = s;
    }
    public static double getSolScale() {
        return SOLSCALE;
    }
    public static double getTorScale() {
        return TORSCALE;
    }
    static boolean FieldsLoaded = false;
    
}
