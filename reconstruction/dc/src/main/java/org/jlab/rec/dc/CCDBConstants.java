package org.jlab.rec.dc;

public class CCDBConstants {

    public CCDBConstants() {
        // TODO Auto-generated constructor stub
    }
    //Instantiate the constants arrays
    // T2D
    private static double[][] DELTANM = new double[6][6];
    private static double[][] V0 = new double[6][6];					    // staturated drift velocity in cm/ns
    private static double[][] DELT_BFIELD_COEFFICIENT = new double[6][6]; //coefficient of the bfield part of the increase in time

    private static double[] DMAXSUPERLAYER = new double[6];
    private static double[][] TMAXSUPERLAYER = new double[6][6];

    private static double DELTATIME_BFIELD_PAR1[][] = new double[6][6];
    private static double DELTATIME_BFIELD_PAR2[][] = new double[6][6];
    private static double DELTATIME_BFIELD_PAR3[][] = new double[6][6];
    private static double DELTATIME_BFIELD_PAR4[][] = new double[6][6];

    private static double DISTBETA[][] = new double[6][6];

    //RMS
    // Instantiating the constants arrays
    private static double[][] PAR1 = new double[6][6];
    private static double[][] PAR2 = new double[6][6];
    private static double[][] PAR3 = new double[6][6];
    private static double[][] PAR4 = new double[6][6];
    private static double[][] SCAL = new double[6][6];

    private static int[][][][] STATUS = new int[6][6][6][112];

    //T0s
    private static double[][][][] T0 = new double[6][6][7][6]; //nSec*nSL*nSlots*nCables
    private static double[][][][] T0ERR = new double[6][6][7][6]; //nSec*nSL*nSlots*nCables

    public static synchronized double[][] getDELTANM() {
        return DELTANM;
    }

    public static synchronized void setDELTANM(double[][] dELTANM) {
        DELTANM = dELTANM;
    }

    public static synchronized double[][] getV0() {
        return V0;
    }

    public static synchronized void setV0(double[][] v0) {
        V0 = v0;
    }

    public static synchronized double[][] getDELT_BFIELD_COEFFICIENT() {
        return DELT_BFIELD_COEFFICIENT;
    }

    public static synchronized void setDELT_BFIELD_COEFFICIENT(
            double[][] dELT_BFIELD_COEFFICIENT) {
        DELT_BFIELD_COEFFICIENT = dELT_BFIELD_COEFFICIENT;
    }

    public static synchronized double[] getDMAXSUPERLAYER() {
        return DMAXSUPERLAYER;
    }

    public static synchronized void setDMAXSUPERLAYER(double[] dMAXSUPERLAYER) {
        DMAXSUPERLAYER = dMAXSUPERLAYER;
    }

    public static synchronized double[][] getTMAXSUPERLAYER() {
        return TMAXSUPERLAYER;
    }

    public static synchronized void setTMAXSUPERLAYER(double[][] tMAXSUPERLAYER) {
        TMAXSUPERLAYER = tMAXSUPERLAYER;
    }

    public static synchronized double[][] getDELTATIME_BFIELD_PAR1() {
        return DELTATIME_BFIELD_PAR1;
    }

    public static synchronized void setDELTATIME_BFIELD_PAR1(
            double[][] dELTATIME_BFIELD_PAR1) {
        DELTATIME_BFIELD_PAR1 = dELTATIME_BFIELD_PAR1;
    }

    public static synchronized double[][] getDELTATIME_BFIELD_PAR2() {
        return DELTATIME_BFIELD_PAR2;
    }

    public static synchronized void setDELTATIME_BFIELD_PAR2(
            double[][] dELTATIME_BFIELD_PAR2) {
        DELTATIME_BFIELD_PAR2 = dELTATIME_BFIELD_PAR2;
    }

    public static synchronized double[][] getDELTATIME_BFIELD_PAR3() {
        return DELTATIME_BFIELD_PAR3;
    }

    public static synchronized void setDELTATIME_BFIELD_PAR3(
            double[][] dELTATIME_BFIELD_PAR3) {
        DELTATIME_BFIELD_PAR3 = dELTATIME_BFIELD_PAR3;
    }

    public static synchronized double[][] getDELTATIME_BFIELD_PAR4() {
        return DELTATIME_BFIELD_PAR4;
    }

    public static synchronized void setDELTATIME_BFIELD_PAR4(
            double[][] dELTATIME_BFIELD_PAR4) {
        DELTATIME_BFIELD_PAR4 = dELTATIME_BFIELD_PAR4;
    }

    public static synchronized double[][] getDISTBETA() {
        return DISTBETA;
    }

    public static synchronized void setDISTBETA(double[][] dISTBETA) {
        DISTBETA = dISTBETA;
    }

    public static synchronized double[][] getPAR1() {
        return PAR1;
    }

    public static synchronized void setPAR1(double[][] pAR1) {
        PAR1 = pAR1;
    }

    public static synchronized double[][] getPAR2() {
        return PAR2;
    }

    public static synchronized void setPAR2(double[][] pAR2) {
        PAR2 = pAR2;
    }

    public static synchronized double[][] getPAR3() {
        return PAR3;
    }

    public static synchronized void setPAR3(double[][] pAR3) {
        PAR3 = pAR3;
    }

    public static synchronized double[][] getPAR4() {
        return PAR4;
    }

    public static synchronized void setPAR4(double[][] pAR4) {
        PAR4 = pAR4;
    }

    public static synchronized double[][] getSCAL() {
        return SCAL;
    }

    public static synchronized void setSCAL(double[][] sCAL) {
        SCAL = sCAL;
    }

    public static synchronized int[][][][] getSTATUS() {
        return STATUS;
    }

    public static synchronized void setSTATUS(int[][][][] sTATUS) {
        STATUS = sTATUS;
    }

    public static synchronized double[][][][] getT0() {
        return T0;
    }

    public static synchronized void setT0(double[][][][] t0) {
        T0 = t0;
    }

    public static synchronized double[][][][] getT0ERR() {
        return T0ERR;
    }

    public static synchronized void setT0ERR(double[][][][] t0err) {
        T0ERR = t0err;
    }

}
