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
    private static float szShift = 0;
    public static synchronized void set_zShift(float shift){ //solenoid z shift
        szShift = shift;
    }
    public static synchronized float get_zShift(){//solenoid z shift
        return szShift ;
    }
    
    private static float torXShift = 0;
    private static float torYShift = 0;
    private static float torZShift = 0;

    public static float getTorXShift() {
        return torXShift;
    }

    public static synchronized void setTorXShift(float torXShif) {
        Swimmer.torXShift = torXShif;
    }

    public static float getTorYShift() {
        return torYShift;
    }

    public static synchronized void setTorYShift(float torYShif) {
        Swimmer.torYShift = torYShif;
    }

    public static float getTorZShift() {
        return torZShift;
    }

    public static synchronized void setTorZShift(float torZShif) {
        Swimmer.torZShift = torZShif;
    }
    
    public static synchronized void setMagneticFieldsScales(double SolenoidScale, double TorusScale, double shift) {
        if (FieldsLoaded) {
            return;
        }
        if(TorusScale==0)
            TorusScale=0.000001;
        if(SolenoidScale==0)
            SolenoidScale=0.000001;
        MagneticFields.getInstance().getTorus().setScaleFactor(TorusScale);
        MagneticFields.getInstance().getSolenoid().setScaleFactor(SolenoidScale);
        MagneticFields.getInstance().setSolenoidShift(shift);
        setSolScale(SolenoidScale);
        setTorScale(TorusScale);
        //remove overlap for composite field
        //MagneticFields.getInstance().removeMapOverlap();
        FieldsLoaded = true;
        System.out.println("*** TRACKING ***** ****** ****** THE TORUS IS BEING SCALED BY " + (TorusScale * 100) + "  %   *******  ****** **** ");
        System.out.println("*** TRACKING ***** ****** ****** THE SOLENOID IS BEING SCALED BY " + (SolenoidScale * 100) + "  %   *******  ****** **** ");   
    }
    
    public static synchronized void setMagneticFieldsScales(double SolenoidScale, double TorusScale, 
            double Sx, double Sy, double Sz,
            double Tx, double Ty, double Tz) {
        if (FieldsLoaded) {
            return;
        }
        if(TorusScale==0)
            TorusScale=0.000001;
        if(SolenoidScale==0)
            SolenoidScale=0.000001;
        MagneticFields.getInstance().getTorus().setScaleFactor(TorusScale);
        MagneticFields.getInstance().getSolenoid().setScaleFactor(SolenoidScale);
        MagneticFields.getInstance().getTorus().setShiftX(Tx);
        MagneticFields.getInstance().getTorus().setShiftY(Ty);
        MagneticFields.getInstance().getTorus().setShiftZ(Tz);
        MagneticFields.getInstance().getSolenoid().setShiftX(Sx);
        MagneticFields.getInstance().getSolenoid().setShiftY(Sy);
        MagneticFields.getInstance().getSolenoid().setShiftZ(Sz);
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
