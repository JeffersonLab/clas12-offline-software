package org.jlab.rec.ftof;

public final class CCDBConstants {

	public CCDBConstants() {
		// TODO Auto-generated constructor stub
	}

	// Instantiating the constants arrays
	private static double[][][] YOFF = new double[6][3][62];
	private static double[][][] LAMBDAL = new double[6][3][62];
	private static double[][][] LAMBDAR = new double[6][3][62];
	private static double[][][] LAMBDALU = new double[6][3][62];
	private static double[][][] LAMBDARU = new double[6][3][62];
	private static double[][][] EFFVELL = new double[6][3][62];
	private static double[][][] EFFVELR = new double[6][3][62];
	private static double[][][] EFFVELLU = new double[6][3][62];
	private static double[][][] EFFVELRU = new double[6][3][62];
	private static double[][][] TW0L = new double[6][3][62];
	private static double[][][] TW1L = new double[6][3][62];
	private static double[][][] TW2L = new double[6][3][62];
	private static double[][][] TW0R = new double[6][3][62];
	private static double[][][] TW1R = new double[6][3][62];
	private static double[][][] TW2R = new double[6][3][62];
	private static double[][][] LR = new double[6][3][62];
	private static double[][][] PADDLE2PADDLE = new double[6][3][62];
	private static int[][][] STATUSL = new int[6][3][62];
	private static int[][][] STATUSR = new int[6][3][62];
	private static double[][][] MIPL = new double[6][3][62];
	private static double[][][] MIPR = new double[6][3][62];
	private static double[][][] MIPLU = new double[6][3][62];
	private static double[][][] MIPRU = new double[6][3][62];

	public static synchronized double[][][] getYOFF() {
		return YOFF;
	}

	public static synchronized void setYOFF(double[][][] yOFF) {
		YOFF = yOFF;
	}

	public static synchronized double[][][] getLAMBDAL() {
		return LAMBDAL;
	}

	public static synchronized void setLAMBDAL(double[][][] lAMBDAL) {
		LAMBDAL = lAMBDAL;
	}

	public static synchronized double[][][] getLAMBDAR() {
		return LAMBDAR;
	}

	public static synchronized void setLAMBDAR(double[][][] lAMBDAR) {
		LAMBDAR = lAMBDAR;
	}

	public static synchronized double[][][] getLAMBDALU() {
		return LAMBDALU;
	}

	public static synchronized void setLAMBDALU(double[][][] lAMBDALU) {
		LAMBDALU = lAMBDALU;
	}

	public static synchronized double[][][] getLAMBDARU() {
		return LAMBDARU;
	}

	public static synchronized void setLAMBDARU(double[][][] lAMBDARU) {
		LAMBDARU = lAMBDARU;
	}

	public static synchronized double[][][] getEFFVELL() {
		return EFFVELL;
	}

	public static synchronized void setEFFVELL(double[][][] eFFVELL) {
		EFFVELL = eFFVELL;
	}

	public static synchronized double[][][] getEFFVELR() {
		return EFFVELR;
	}

	public static synchronized void setEFFVELR(double[][][] eFFVELR) {
		EFFVELR = eFFVELR;
	}

	public static synchronized double[][][] getEFFVELLU() {
		return EFFVELLU;
	}

	public static synchronized void setEFFVELLU(double[][][] eFFVELLU) {
		EFFVELLU = eFFVELLU;
	}

	public static synchronized double[][][] getEFFVELRU() {
		return EFFVELRU;
	}

	public static synchronized void setEFFVELRU(double[][][] eFFVELRU) {
		EFFVELRU = eFFVELRU;
	}

	public static synchronized double[][][] getTW0L() {
		return TW0L;
	}

	public static synchronized void setTW0L(double[][][] tW0L) {
		TW0L = tW0L;
	}

	public static synchronized double[][][] getTW1L() {
		return TW1L;
	}

	public static synchronized void setTW1L(double[][][] tW1L) {
		TW1L = tW1L;
	}

	public static synchronized double[][][] getTW2L() {
		return TW2L;
	}

	public static synchronized void setTW2L(double[][][] tW2L) {
		TW2L = tW2L;
	}

	public static synchronized double[][][] getTW0R() {
		return TW0R;
	}

	public static synchronized void setTW0R(double[][][] tW0R) {
		TW0R = tW0R;
	}

	public static synchronized double[][][] getTW1R() {
		return TW1R;
	}

	public static synchronized void setTW1R(double[][][] tW1R) {
		TW1R = tW1R;
	}

	public static synchronized double[][][] getTW2R() {
		return TW2R;
	}

	public static synchronized void setTW2R(double[][][] tW2R) {
		TW2R = tW2R;
	}

	public static synchronized double[][][] getLR() {
		return LR;
	}

	public static synchronized void setLR(double[][][] lR) {
		LR = lR;
	}

	public static synchronized double[][][] getPADDLE2PADDLE() {
		return PADDLE2PADDLE;
	}

	public static synchronized void setPADDLE2PADDLE(double[][][] pADDLE2PADDLE) {
		PADDLE2PADDLE = pADDLE2PADDLE;
	}

	public static synchronized int[][][] getSTATUSL() {
		return STATUSL;
	}

	public static synchronized void setSTATUSL(int[][][] sTATUSL) {
		STATUSL = sTATUSL;
	}

	public static synchronized int[][][] getSTATUSR() {
		return STATUSR;
	}

	public static synchronized void setSTATUSR(int[][][] sTATUSR) {
		STATUSR = sTATUSR;
	}

	public static synchronized double[][][] getMIPL() {
		return MIPL;
	}

	public static synchronized void setMIPL(double[][][] mIPL) {
		MIPL = mIPL;
	}

	public static synchronized double[][][] getMIPR() {
		return MIPR;
	}

	public static synchronized void setMIPR(double[][][] mIPR) {
		MIPR = mIPR;
	}

	public static synchronized double[][][] getMIPLU() {
		return MIPLU;
	}

	public static synchronized void setMIPLU(double[][][] mIPLU) {
		MIPLU = mIPLU;
	}

	public static synchronized double[][][] getMIPRU() {
		return MIPRU;
	}

	public static synchronized void setMIPRU(double[][][] mIPRU) {
		MIPRU = mIPRU;
	}

}
