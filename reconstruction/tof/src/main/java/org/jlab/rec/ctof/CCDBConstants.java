package org.jlab.rec.ctof;

public class CCDBConstants {

    public CCDBConstants() {
        // TODO Auto-generated constructor stub
    }

    // Instantiating the constants arrays
    private static double[][][] YOFF = new double[1][1][48];
    private static double[][][] LAMBDAU = new double[1][1][48];
    private static double[][][] LAMBDAD = new double[1][1][48];
    private static double[][][] LAMBDAUU = new double[1][1][48];
    private static double[][][] LAMBDADU = new double[1][1][48];
    private static double[][][] EFFVELU = new double[1][1][48];
    private static double[][][] EFFVELD = new double[1][1][48];
    private static double[][][] EFFVELUU = new double[1][1][48];
    private static double[][][] EFFVELDU = new double[1][1][48];
    private static double[][][] TW0U = new double[1][1][48];
    private static double[][][] TW1U = new double[1][1][48];
    private static double[][][] TW2U = new double[1][1][48];
    private static double[][][] TW0D = new double[1][1][48];
    private static double[][][] TW1D = new double[1][1][48];
    private static double[][][] TW2D = new double[1][1][48];
    private static double[][][] UD = new double[1][1][48];
    private static double[][][] PADDLE2PADDLE = new double[1][1][48];
    private static double[][][] RFPAD = new double[1][1][48];
    private static int[][][] STATUSU = new int[1][1][48];
    private static int[][][] STATUSD = new int[1][1][48];

    public static synchronized double[][][] getYOFF() {
        return YOFF;
    }

    public static synchronized void setYOFF(double[][][] yOFF) {
        YOFF = yOFF;
    }

    public static synchronized double[][][] getLAMBDAU() {
        return LAMBDAU;
    }

    public static synchronized void setLAMBDAU(double[][][] lAMBDAU) {
        LAMBDAU = lAMBDAU;
    }

    public static synchronized double[][][] getLAMBDAD() {
        return LAMBDAD;
    }

    public static synchronized void setLAMBDAD(double[][][] lAMBDAD) {
        LAMBDAD = lAMBDAD;
    }

    public static synchronized double[][][] getLAMBDAUU() {
        return LAMBDAUU;
    }

    public static synchronized void setLAMBDAUU(double[][][] lAMBDAUU) {
        LAMBDAUU = lAMBDAUU;
    }

    public static synchronized double[][][] getLAMBDADU() {
        return LAMBDADU;
    }

    public static synchronized void setLAMBDADU(double[][][] lAMBDADU) {
        LAMBDADU = lAMBDADU;
    }

    public static synchronized double[][][] getEFFVELU() {
        return EFFVELU;
    }

    public static synchronized void setEFFVELU(double[][][] eFFVELU) {
        EFFVELU = eFFVELU;
    }

    public static synchronized double[][][] getEFFVELD() {
        return EFFVELD;
    }

    public static synchronized void setEFFVELD(double[][][] eFFVELD) {
        EFFVELD = eFFVELD;
    }

    public static synchronized double[][][] getEFFVELUU() {
        return EFFVELUU;
    }

    public static synchronized void setEFFVELUU(double[][][] eFFVELUU) {
        EFFVELUU = eFFVELUU;
    }

    public static synchronized double[][][] getEFFVELDU() {
        return EFFVELDU;
    }

    public static synchronized void setEFFVELDU(double[][][] eFFVELDU) {
        EFFVELDU = eFFVELDU;
    }

    public static synchronized double[][][] getTW0U() {
        return TW0U;
    }

    public static synchronized void setTW0U(double[][][] tW0U) {
        TW0U = tW0U;
    }

    public static synchronized double[][][] getTW1U() {
        return TW1U;
    }

    public static synchronized void setTW1U(double[][][] tW1U) {
        TW1U = tW1U;
    }

    public static synchronized double[][][] getTW2U() {
        return TW2U;
    }

    public static synchronized void setTW2U(double[][][] tW2U) {
        TW2U = tW2U;
    }

    public static synchronized double[][][] getTW0D() {
        return TW0D;
    }

    public static synchronized void setTW0D(double[][][] tW0D) {
        TW0D = tW0D;
    }

    public static synchronized double[][][] getTW1D() {
        return TW1D;
    }

    public static synchronized void setTW1D(double[][][] tW1D) {
        TW1D = tW1D;
    }

    public static synchronized double[][][] getTW2D() {
        return TW2D;
    }

    public static synchronized void setTW2D(double[][][] tW2D) {
        TW2D = tW2D;
    }

    public static synchronized double[][][] getUD() {
        return UD;
    }

    public static synchronized void setUD(double[][][] uD) {
        UD = uD;
    }

    public static synchronized double[][][] getPADDLE2PADDLE() {
        return PADDLE2PADDLE;
    }

    public static synchronized void setPADDLE2PADDLE(double[][][] pADDLE2PADDLE) {
        PADDLE2PADDLE = pADDLE2PADDLE;
    }

    public static synchronized double[][][] getRFPAD() {
        return RFPAD;
    }

    public static synchronized void setRFPAD(double[][][] rFPAD) {
        RFPAD = rFPAD;
    }

    public static synchronized int[][][] getSTATUSU() {
        return STATUSU;
    }

    public static synchronized void setSTATUSU(int[][][] sTATUSU) {
        STATUSU = sTATUSU;
    }

    public static synchronized int[][][] getSTATUSD() {
        return STATUSD;
    }

    public static synchronized void setSTATUSD(int[][][] sTATUSD) {
        STATUSD = sTATUSD;
    }

}
