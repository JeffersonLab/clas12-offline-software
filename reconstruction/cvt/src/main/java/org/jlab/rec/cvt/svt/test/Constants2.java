/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.cvt.svt.test;

/**
 *
 * @author ziegler
 */
public class Constants2 {
    
    private static int NREGIONS; // number of regions
    private static int[] NSECTORS; // number of sectors in a region
    private static int NFIDUCIALS; // number of survey fiducials on a sector module
    private static int NMODULES; // number of modules in a sector
    private static int NSENSORS; // number of sensors in a module
    private static int NSTRIPS; // number of strips in a layer
    private static int NPADS; // number of pads on High Voltage Rail
    private static double STRIPOFFSETWID; // offset of first intermediate sensor strip from edge of active zone
    private static double READOUTPITCH; // distance between start of strips along front of hybrid sensor
    private static double STEREOANGLE; // total angle swept by sensor strips
    private static int[] STATUS; // whether a region is used in Reconstruction
    //
    // position and orientation of layers
    private static double PHI0;
    private static double SECTOR0;
    private static double[] REFRADIUS; // outer side of U (inner) module
    private static double LAYERPOSFAC; // location of strip layer within sensor volume
    private static double[] Z0ACTIVE; // Cu edge of hybrid sensor's active volume
    // fiducials
    private static double[] SUPPORTRADIUS; // from MechEng drawings, to inner side of wide copper part
    private static double FIDCUX;
    private static double FIDPKX;
    private static double FIDORIGINZ;
    private static double FIDCUZ;
    private static double FIDPKZ0;
    private static double FIDPKZ1;
    //
    // dimensions of sensors
    private static double ACTIVESENWID;
    private static double PHYSSENWID;
    private static double DEADZNWID;
    //
    private static double SILICONTHK;
    //
    private static double PHYSSENLEN;
    private static double ACTIVESENLEN;
    private static double DEADZNLEN;
    private static double MICROGAPLEN; // spacing between sensors
    //
    // dimensions of passive materials
    private static int NMATERIALS;
    private static double PASSIVETHK;
    //
    // calculated on load()
    private static int NLAYERS; // total number of layers in a sector
    private static int NTOTALSECTORS; // total number of sectors for all regions
    private static int NTOTALFIDUCIALS; // total number of fiducials for all sectors and regions
    private static double[][] LAYERRADIUS; // radius to strip planes
    private static double LAYERGAPTHK; // distance between pairs of layers within a sector
    private static double MODULELEN;    // || DZ |  AZ  | DZ |MG| DZ |  AZ  | DZ |MG| DZ |  AZ  || DZ ||
    private static double STRIPLENMAX;  //      ||  AZ  | DZ |MG| DZ |  AZ  | DZ |MG| DZ |  AZ  ||
    private static double MODULEWID; // || DZ | AZ | DZ ||
    private static double SECTORLEN;

    public static synchronized int getNREGIONS() {
        return NREGIONS;
    }

    public static synchronized void setNREGIONS(int aNREGIONS) {
        NREGIONS = aNREGIONS;
    }

    public static synchronized int[] getNSECTORS() {
        return NSECTORS;
    }

    public static synchronized void setNSECTORS(int[] aNSECTORS) {
        NSECTORS = aNSECTORS;
    }

    public static synchronized int getNFIDUCIALS() {
        return NFIDUCIALS;
    }

    public static synchronized void setNFIDUCIALS(int aNFIDUCIALS) {
        NFIDUCIALS = aNFIDUCIALS;
    }

    public static synchronized int getNMODULES() {
        return NMODULES;
    }

    public static synchronized void setNMODULES(int aNMODULES) {
        NMODULES = aNMODULES;
    }

    public static synchronized int getNSENSORS() {
        return NSENSORS;
    }

    public static synchronized void setNSENSORS(int aNSENSORS) {
        NSENSORS = aNSENSORS;
    }

    public static synchronized int getNSTRIPS() {
        return NSTRIPS;
    }

    public static synchronized void setNSTRIPS(int aNSTRIPS) {
        NSTRIPS = aNSTRIPS;
    }

    public static synchronized int getNPADS() {
        return NPADS;
    }

    public static synchronized void setNPADS(int aNPADS) {
        NPADS = aNPADS;
    }

    public static synchronized double getSTRIPOFFSETWID() {
        return STRIPOFFSETWID;
    }

    public static synchronized void setSTRIPOFFSETWID(double aSTRIPOFFSETWID) {
        STRIPOFFSETWID = aSTRIPOFFSETWID;
    }

    public static synchronized double getREADOUTPITCH() {
        return READOUTPITCH;
    }

    public static synchronized void setREADOUTPITCH(double aREADOUTPITCH) {
        READOUTPITCH = aREADOUTPITCH;
    }

    public static synchronized double getSTEREOANGLE() {
        return STEREOANGLE;
    }

    public static synchronized void setSTEREOANGLE(double aSTEREOANGLE) {
        STEREOANGLE = aSTEREOANGLE;
    }

    public static synchronized int[] getSTATUS() {
        return STATUS;
    }

    public static synchronized void setSTATUS(int[] aSTATUS) {
        STATUS = aSTATUS;
    }

    public static synchronized double getPHI0() {
        return PHI0;
    }

    public static synchronized void setPHI0(double aPHI0) {
        PHI0 = aPHI0;
    }

    public static synchronized double getSECTOR0() {
        return SECTOR0;
    }

    public static synchronized void setSECTOR0(double aSECTOR0) {
        SECTOR0 = aSECTOR0;
    }

    public static synchronized double[] getREFRADIUS() {
        return REFRADIUS;
    }

    public static synchronized void setREFRADIUS(double[] aREFRADIUS) {
        REFRADIUS = aREFRADIUS;
    }

    public static synchronized double getLAYERPOSFAC() {
        return LAYERPOSFAC;
    }

    public static synchronized void setLAYERPOSFAC(double aLAYERPOSFAC) {
        LAYERPOSFAC = aLAYERPOSFAC;
    }

    public static synchronized double[] getZ0ACTIVE() {
        return Z0ACTIVE;
    }

    public static synchronized void setZ0ACTIVE(double[] aZ0ACTIVE) {
        Z0ACTIVE = aZ0ACTIVE;
    }

    public static synchronized double[] getSUPPORTRADIUS() {
        return SUPPORTRADIUS;
    }

    public static synchronized void setSUPPORTRADIUS(double[] aSUPPORTRADIUS) {
        SUPPORTRADIUS = aSUPPORTRADIUS;
    }

    public static synchronized double getFIDCUX() {
        return FIDCUX;
    }

    public static synchronized void setFIDCUX(double aFIDCUX) {
        FIDCUX = aFIDCUX;
    }

    public static synchronized double getFIDPKX() {
        return FIDPKX;
    }

    public static synchronized void setFIDPKX(double aFIDPKX) {
        FIDPKX = aFIDPKX;
    }

    public static synchronized double getFIDORIGINZ() {
        return FIDORIGINZ;
    }

    public static synchronized void setFIDORIGINZ(double aFIDORIGINZ) {
        FIDORIGINZ = aFIDORIGINZ;
    }

    public static synchronized double getFIDCUZ() {
        return FIDCUZ;
    }

    public static synchronized void setFIDCUZ(double aFIDCUZ) {
        FIDCUZ = aFIDCUZ;
    }

    public static synchronized double getFIDPKZ0() {
        return FIDPKZ0;
    }

    public static synchronized void setFIDPKZ0(double aFIDPKZ0) {
        FIDPKZ0 = aFIDPKZ0;
    }

    public static synchronized double getFIDPKZ1() {
        return FIDPKZ1;
    }

    public static synchronized void setFIDPKZ1(double aFIDPKZ1) {
        FIDPKZ1 = aFIDPKZ1;
    }

    public static synchronized double getACTIVESENWID() {
        return ACTIVESENWID;
    }

    public static synchronized void setACTIVESENWID(double aACTIVESENWID) {
        ACTIVESENWID = aACTIVESENWID;
    }

    public static synchronized double getPHYSSENWID() {
        return PHYSSENWID;
    }

    public static synchronized void setPHYSSENWID(double aPHYSSENWID) {
        PHYSSENWID = aPHYSSENWID;
    }

    public static synchronized double getDEADZNWID() {
        return DEADZNWID;
    }

    public static synchronized void setDEADZNWID(double aDEADZNWID) {
        DEADZNWID = aDEADZNWID;
    }

    public static synchronized double getSILICONTHK() {
        return SILICONTHK;
    }

    public static synchronized void setSILICONTHK(double aSILICONTHK) {
        SILICONTHK = aSILICONTHK;
    }

    public static synchronized double getPHYSSENLEN() {
        return PHYSSENLEN;
    }

    public static synchronized void setPHYSSENLEN(double aPHYSSENLEN) {
        PHYSSENLEN = aPHYSSENLEN;
    }

    public static synchronized double getACTIVESENLEN() {
        return ACTIVESENLEN;
    }

    public static synchronized void setACTIVESENLEN(double aACTIVESENLEN) {
        ACTIVESENLEN = aACTIVESENLEN;
    }

    public static synchronized double getDEADZNLEN() {
        return DEADZNLEN;
    }

    public static synchronized void setDEADZNLEN(double aDEADZNLEN) {
        DEADZNLEN = aDEADZNLEN;
    }

    public static synchronized double getMICROGAPLEN() {
        return MICROGAPLEN;
    }

    public static synchronized void setMICROGAPLEN(double aMICROGAPLEN) {
        MICROGAPLEN = aMICROGAPLEN;
    }

    public static synchronized int getNMATERIALS() {
        return NMATERIALS;
    }

    public static synchronized void setNMATERIALS(int aNMATERIALS) {
        NMATERIALS = aNMATERIALS;
    }

    public static synchronized double getPASSIVETHK() {
        return PASSIVETHK;
    }

    public static synchronized void setPASSIVETHK(double aPASSIVETHK) {
        PASSIVETHK = aPASSIVETHK;
    }

    public static synchronized int getNLAYERS() {
        return NLAYERS;
    }

    public static synchronized void setNLAYERS(int aNLAYERS) {
        NLAYERS = aNLAYERS;
    }

    public static synchronized int getNTOTALSECTORS() {
        return NTOTALSECTORS;
    }

    public static synchronized void setNTOTALSECTORS(int aNTOTALSECTORS) {
        NTOTALSECTORS = aNTOTALSECTORS;
    }

    public static synchronized int getNTOTALFIDUCIALS() {
        return NTOTALFIDUCIALS;
    }

    public static synchronized void setNTOTALFIDUCIALS(int aNTOTALFIDUCIALS) {
        NTOTALFIDUCIALS = aNTOTALFIDUCIALS;
    }

    public static synchronized double[][] getLAYERRADIUS() {
        return LAYERRADIUS;
    }

    public static synchronized void setLAYERRADIUS(double[][] aLAYERRADIUS) {
        LAYERRADIUS = aLAYERRADIUS;
    }

    public static synchronized double getLAYERGAPTHK() {
        return LAYERGAPTHK;
    }

    public static synchronized void setLAYERGAPTHK(double aLAYERGAPTHK) {
        LAYERGAPTHK = aLAYERGAPTHK;
    }

    public static synchronized double getMODULELEN() {
        return MODULELEN;
    }

    public static synchronized void setMODULELEN(double aMODULELEN) {
        MODULELEN = aMODULELEN;
    }

    public static synchronized double getSTRIPLENMAX() {
        return STRIPLENMAX;
    }

    public static synchronized void setSTRIPLENMAX(double aSTRIPLENMAX) {
        STRIPLENMAX = aSTRIPLENMAX;
    }

    public static synchronized double getMODULEWID() {
        return MODULEWID;
    }

    public static synchronized void setMODULEWID(double aMODULEWID) {
        MODULEWID = aMODULEWID;
    }

    public static synchronized double getSECTORLEN() {
        return SECTORLEN;
    }

    public static synchronized void setSECTORLEN(double aSECTORLEN) {
        SECTORLEN = aSECTORLEN;
    }
}
