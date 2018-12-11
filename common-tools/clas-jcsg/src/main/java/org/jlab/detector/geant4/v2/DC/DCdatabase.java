/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2.DC;

import eu.mihosoft.vrl.v3d.Vector3d;
import org.jlab.detector.units.SystemOfUnits;
import org.jlab.geom.base.ConstantProvider;

/**
 *
 * @author kenjo
 */
public final class DCdatabase {

    private final int nSectors = 6;
    private final int nRegions = 3;
    private final int nSupers = 6;

    private final double dist2tgt[] = new double[nRegions];
    private final double xdist[] = new double[nRegions];
    private final double frontgap[] = new double[nRegions];
    private final double midgap[] = new double[nRegions];
    private final double backgap[] = new double[nRegions];
    private final double thopen[] = new double[nRegions];
    private final double thtilt[] = new double[nRegions];

    private final double thmin[] = new double[nSupers];
    private final double thster[] = new double[nSupers];
    private final double wpdist[] = new double[nSupers];
    private final double cellthickness[] = new double[nSupers];
    private final int nsenselayers[] = new int[nSupers];
    private final int nguardlayers[] = new int[nSupers];
    private final int nfieldlayers[] = new int[nSupers];
    private final double superwidth[] = new double[nSupers];

    private final double align_dx[][] = new double[nSectors][nRegions];
    private final double align_dy[][] = new double[nSectors][nRegions];
    private final double align_dz[][] = new double[nSectors][nRegions];

    private final double align_dthetax[][] = new double[nSectors][nRegions];
    private final double align_dthetay[][] = new double[nSectors][nRegions];
    private final double align_dthetaz[][] = new double[nSectors][nRegions];

    private int nsensewires;
    private int nguardwires;

    private boolean ministaggerStatus = false;

    private final String dcdbpath = "/geometry/dc/";
    private static DCdatabase instance = null;

    private DCdatabase() {
    }

    public static DCdatabase getInstance() {
        if (instance == null) {
            instance = new DCdatabase();
        }
        return instance;
    }

    public void connect(ConstantProvider cp) {

        nguardwires = cp.getInteger(dcdbpath + "layer/nguardwires", 0);
        nsensewires = cp.getInteger(dcdbpath + "layer/nsensewires", 0);

        for (int ireg = 0; ireg < nRegions; ireg++) {
            dist2tgt[ireg] = cp.getDouble(dcdbpath + "region/dist2tgt", ireg)*SystemOfUnits.Length.cm;
            xdist[ireg] = cp.getDouble(dcdbpath + "region/xdist", ireg)*SystemOfUnits.Length.cm;
            frontgap[ireg] = cp.getDouble(dcdbpath + "region/frontgap", ireg)*SystemOfUnits.Length.cm;
            midgap[ireg] = cp.getDouble(dcdbpath + "region/midgap", ireg)*SystemOfUnits.Length.cm;
            backgap[ireg] = cp.getDouble(dcdbpath + "region/backgap", ireg)*SystemOfUnits.Length.cm;
            thopen[ireg] = Math.toRadians(cp.getDouble(dcdbpath + "region/thopen", ireg));
            thtilt[ireg] = Math.toRadians(cp.getDouble(dcdbpath + "region/thtilt", ireg));
        }
        for (int isuper = 0; isuper < nSupers; isuper++) {
            thmin[isuper] = Math.toRadians(cp.getDouble(dcdbpath + "superlayer/thmin", isuper));
            thster[isuper] = Math.toRadians(cp.getDouble(dcdbpath + "superlayer/thster", isuper));
            wpdist[isuper] = cp.getDouble(dcdbpath + "superlayer/wpdist", isuper)*SystemOfUnits.Length.cm;
            cellthickness[isuper] = cp.getDouble(dcdbpath + "superlayer/cellthickness", isuper);
            nsenselayers[isuper] = cp.getInteger(dcdbpath + "superlayer/nsenselayers", isuper);
            nguardlayers[isuper] = cp.getInteger(dcdbpath + "superlayer/nguardlayers", isuper);
            nfieldlayers[isuper] = cp.getInteger(dcdbpath + "superlayer/nfieldlayers", isuper);

            superwidth[isuper] = wpdist[isuper] * (nsenselayers[isuper] + nguardlayers[isuper] - 1) * cellthickness[isuper];
        }

	   int alignrows = cp.length(dcdbpath+"alignment/dx");
	   for(int irow = 0; irow< alignrows; irow++) {
		  int isec = cp.getInteger(dcdbpath + "alignment/sector",irow)-1;
		  int ireg = cp.getInteger(dcdbpath + "alignment/region",irow)-1;

		  align_dx[isec][ireg]=cp.getDouble(dcdbpath + "alignment/dx",irow);
		  align_dy[isec][ireg]=cp.getDouble(dcdbpath + "alignment/dy",irow);
		  align_dz[isec][ireg]=cp.getDouble(dcdbpath + "alignment/dz",irow);

		  align_dthetax[isec][ireg]=cp.getDouble(dcdbpath + "alignment/dtheta_x",irow);
  		  align_dthetay[isec][ireg]=cp.getDouble(dcdbpath + "alignment/dtheta_y",irow);
  		  align_dthetaz[isec][ireg]=cp.getDouble(dcdbpath + "alignment/dtheta_z",irow);
	   }
    }

    public double dist2tgt(int ireg) {
        return dist2tgt[ireg];
    }

    public double xdist(int ireg) {
        return xdist[ireg];
    }

    public double frontgap(int ireg) {
        return frontgap[ireg];
    }

    public double midgap(int ireg) {
        return midgap[ireg];
    }

    public double backgap(int ireg) {
        return backgap[ireg];
    }

    public double thopen(int ireg) {
        return thopen[ireg];
    }

    public double thtilt(int ireg) {
        return thtilt[ireg];
    }

    public double thmin(int isuper) {
        return thmin[isuper];
    }

    public double thster(int isuper) {
        return thster[isuper];
    }

    public double wpdist(int isuper) {
        return wpdist[isuper];
    }

    public double cellthickness(int isuper) {
        return cellthickness[isuper];
    }

    public int nsenselayers(int isuper) {
        return nsenselayers[isuper];
    }

    public int nguardlayers(int isuper) {
        return nguardlayers[isuper];
    }

    public int nfieldlayers(int isuper) {
        return nfieldlayers[isuper];
    }

    public double superwidth(int isuper) {
        return superwidth[isuper];
    }

    public int nsensewires() {
        return nsensewires;
    }

    public int nguardwires() {
        return nguardwires;
    }

    public int nsuperlayers() {
        return nSupers;
    }

    public int nregions() {
        return nRegions;
    }

    public int nsectors() {
        return nSectors;
    }

    public void setMinistaggerStatus(boolean ministaggerStatus) {
        this.ministaggerStatus = ministaggerStatus;
    }

    public boolean getMinistaggerStatus(){
        return ministaggerStatus;
    }

    public double getAlignmentThetaX(int isec, int ireg) {
        return align_dthetax[isec][ireg];
    }

    public double getAlignmentThetaY(int isec, int ireg) {
        return align_dthetay[isec][ireg];
    }

    public double getAlignmentThetaZ(int isec, int ireg) {
        return align_dthetaz[isec][ireg];
    }

    public Vector3d getAlignmentShift(int isec, int ireg) {
        return new Vector3d(align_dx[isec][ireg], align_dy[isec][ireg], align_dz[isec][ireg]);
    }
}
