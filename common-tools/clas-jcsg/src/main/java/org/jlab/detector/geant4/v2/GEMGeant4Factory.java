/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2;

import eu.mihosoft.vrl.v3d.Vector3d;
import java.util.HashMap;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.detector.units.SystemOfUnits.Length;
import org.jlab.detector.volume.G4Trap;
import org.jlab.detector.volume.G4World;
import org.jlab.detector.volume.Geant4Basic;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geometry.prim.Line3d;

/**
 *
 * @author kenjo
 */
final class GEMdatabase {

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

    
    private final String dcdbpath = "/geometry/dc/";
    private static GEMdatabase instance = null;

    private GEMdatabase() {
    }

    public static GEMdatabase getInstance() {
        if (instance == null) {
            instance = new GEMdatabase();
        }
        return instance;
    }

    public void connect(ConstantProvider cp) {

        nguardwires = cp.getInteger(dcdbpath + "layer/nguardwires", 0);
        nsensewires = cp.getInteger(dcdbpath + "layer/nsensewires", 0);
        
        for (int ireg = 0; ireg < nRegions; ireg++) {
            dist2tgt[ireg] = cp.getDouble(dcdbpath + "region/dist2tgt", ireg)*Length.cm;
            xdist[ireg] = cp.getDouble(dcdbpath + "region/xdist", ireg)*Length.cm;
            frontgap[ireg] = cp.getDouble(dcdbpath + "region/frontgap", ireg)*Length.cm;
            midgap[ireg] = cp.getDouble(dcdbpath + "region/midgap", ireg)*Length.cm;
            backgap[ireg] = cp.getDouble(dcdbpath + "region/backgap", ireg)*Length.cm;
            thopen[ireg] = Math.toRadians(cp.getDouble(dcdbpath + "region/thopen", ireg));
            thtilt[ireg] = Math.toRadians(cp.getDouble(dcdbpath + "region/thtilt", ireg));
        }
        for (int isuper = 0; isuper < nSupers; isuper++) {
            thmin[isuper] = Math.toRadians(cp.getDouble(dcdbpath + "superlayer/thmin", isuper));
            thster[isuper] = Math.toRadians(cp.getDouble(dcdbpath + "superlayer/thster", isuper));
            wpdist[isuper] = cp.getDouble(dcdbpath + "superlayer/wpdist", isuper)*Length.cm;
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

final class Strip extends Line3d {

    public Strip(Vector3d origin, Vector3d end) {
        super(origin, end);
    }


    public Vector3d dir() {
        Vector3d dir = this.end().minus(this.origin()).normalized();
        return dir;
    }

    public Vector3d top() {
        if (this.origin().y < this.end().y) {
            return new Vector3d(this.end());
        }
        return new Vector3d(this.origin());
    }

    public Vector3d bottom() {
        if (this.origin().y < this.end().y) {
            return new Vector3d(this.origin());
        }
        return new Vector3d(this.end());
    }

    public double length() {
        Vector3d length = new Vector3d(this.end());
        return length.minus(this.origin()).magnitude();
    }

    public Vector3d center() {
        Vector3d center = this.origin().plus(this.end()).dividedBy(2.0);
        return center;
    }
}

///////////////////////////////////////////////////
public final class GEMGeant4Factory extends Geant4Factory {

    GEMdatabase dbref = GEMdatabase.getInstance();

    private final HashMap<String, String> properties = new HashMap<>();
    private int nsgwires;

    private final double y_enlargement = 3.65;
    private final double z_enlargement = -2.96;
    private final double microgap = 0.01;

    ///////////////////////////////////////////////////
    public GEMGeant4Factory(ConstantProvider provider) {
        motherVolume = new G4World("fc");

        dbref.connect(provider);
        nsgwires = dbref.nsensewires() + dbref.nguardwires();

        for (int iregion = 0; iregion < 3; iregion++) {
            for (int isector = 0; isector < 6; isector++) {
                Geant4Basic regionVolume = createRegion(isector, iregion);
                regionVolume.setMother(motherVolume);
            }
        }
    }


    /**
     * 
     * @param sector sector 1...6
     * @param super  superlayer index 0...5
     * @param layer  layer 1...6
     * @param wire   wire 1...112
     */
    private Strip getStrip(int sector, int isuper, int layer, int wire) {
        int ireg    = isuper / 2;

        // calculate first-wire distance from target
        double w2tgt = dbref.dist2tgt(ireg);
        if (isuper % 2 > 0) {
            w2tgt += dbref.superwidth(isuper - 1) + dbref.midgap(ireg);
        }
        w2tgt /= Math.cos(dbref.thtilt(ireg) - dbref.thmin(isuper));

        // y0 and z0 in the lab for the first wire of the layer
        double y0mid = w2tgt * Math.sin(dbref.thmin(isuper));
        double z0mid = w2tgt * Math.cos(dbref.thmin(isuper));

        double cster = Math.cos(dbref.thster(isuper));
        double ctilt = Math.cos(dbref.thtilt(ireg));
        double stilt = Math.sin(dbref.thtilt(ireg));

        double dw = 4 * Math.cos(Math.toRadians(30)) * dbref.wpdist(isuper);
        double dw2 = dw / cster;

        // hh: wire distance in the wire plane
        double hh = (wire-1 + ((double)(layer % 2)) / 2.0) * dw2;
 
        // ll: layer distance
        double tt = dbref.cellthickness(isuper) * dbref.wpdist(isuper);
        double ll = layer * tt;

        // wire x=0 coordinates in the lab
        double ym = y0mid + ll * stilt + hh * ctilt;
        double zm = z0mid + ll * ctilt - hh * stilt;

        // wire midpoint in the lab
        Vector3d midpoint = new Vector3d(0, ym, zm);
        Vector3d direction = new Vector3d(1, 0, 0);
        direction.rotateZ(dbref.thster(isuper));
        direction.rotateX(-dbref.thtilt(ireg));

        Vector3d vnum = new Vector3d(0, dbref.xdist(ireg), 0);
        vnum.sub(midpoint);

        double copen = Math.cos(dbref.thopen(ireg) / 2.0);
        double sopen = Math.sin(dbref.thopen(ireg) / 2.0);

        // define unit vector normal to the sides of the chamber and pointing inside
        Vector3d rnorm = new Vector3d(copen, sopen, 0);
        Vector3d lnorm = new Vector3d(-copen, sopen, 0);

        double wlenl = vnum.dot(lnorm) / direction.dot(lnorm);
        Vector3d leftend = direction.times(wlenl).add(midpoint);

        double wlenr = vnum.dot(rnorm) / direction.dot(rnorm);
        Vector3d rightend = direction.times(wlenr).add(midpoint);
        
        return new Strip(leftend,rightend);
    }
    
    
    ///////////////////////////////////////////////////
    public Geant4Basic createRegion(int isector, int iregion) {
        Strip regw0 = this.getStrip(isector+1, iregion * 2, 0, 0);
        Strip regw1 = this.getStrip(isector+1, iregion * 2 + 1, 7, nsgwires - 1);

        double dx_shift = y_enlargement * Math.tan(Math.toRadians(29.5));

        double reg_dz = (dbref.frontgap(iregion) + dbref.backgap(iregion) + dbref.midgap(iregion) + dbref.superwidth(iregion * 2) + dbref.superwidth(iregion * 2 + 1)) / 2.0 + z_enlargement;
        double reg_dx0 = Math.abs(regw0.bottom().x) - dx_shift + 1.0;
        double reg_dx1 = Math.abs(regw1.top().x) + dx_shift + 1.0;
        double reg_dy = regw1.top().minus(regw0.bottom()).y / Math.cos(dbref.thtilt(iregion)) / 2.0 + y_enlargement + 1.0;
        double reg_skew = 0.0;
        double reg_thtilt = dbref.thtilt(iregion);

        Vector3d vcenter = regw1.top().plus(regw0.bottom()).dividedBy(2.0);
        vcenter.x = 0;
        Vector3d reg_position0 = new Vector3d(vcenter.x, vcenter.y, vcenter.z);
        vcenter.rotateZ(-Math.toRadians(90 - isector * 60));

        Geant4Basic regionVolume = new G4Trap("region" + (iregion + 1) + "_s" + (isector + 1),
                reg_dz, -reg_thtilt, Math.toRadians(90.0),
                reg_dy, reg_dx0, reg_dx1, 0.0,
                reg_dy, reg_dx0, reg_dx1, 0.0);
        regionVolume.rotate("yxz", 0.0, reg_thtilt, Math.toRadians(90.0 - isector * 60.0));
        regionVolume.translate(vcenter.x, vcenter.y, vcenter.z);
        regionVolume.setId(isector + 1, iregion + 1, 0, 0);

        for (int isup = 0; isup < 2; isup++) {
            int isuper = iregion * 2 + isup;
            int nsglayers = dbref.nsenselayers(isuper) + dbref.nguardlayers(isuper);
            for (int ilayer = 1; ilayer < nsglayers - 1; ilayer++) {
                Geant4Basic layerVolume = this.createLayer(isuper, ilayer);
                layerVolume.setName("sl" + (isuper + 1) + "_layer" + ilayer + "_s" + (isector + 1));

                Vector3d lcenter = layerVolume.getLocalPosition();
                Vector3d lshift = lcenter.minus(reg_position0);
                lshift.rotateX(reg_thtilt);

                layerVolume.rotate("zxy", -dbref.thster(isuper), 0.0, 0.0);

                layerVolume.setPosition(lshift.x, lshift.y, lshift.z);
                layerVolume.setMother(regionVolume);
                layerVolume.setId(isector + 1, iregion + 1, isuper + 1, ilayer);
            }
        }

        return regionVolume;
    }

    ///////////////////////////////////////////////////
    public Geant4Basic createLayer(int isuper, int ilayer) {
        Strip lw0 = this.getStrip(1, isuper, ilayer, 0);
        Strip lw1 = this.getStrip(1, isuper, ilayer, nsgwires - 1);

        Vector3d midline = lw1.center().minus(lw0.center());
        double lay_dy = Math.sqrt(Math.pow(midline.magnitude(), 2.0) - Math.pow(midline.dot(lw0.dir()), 2.0)) / 2.0;
        double lay_dx0 = lw0.length() / 2.0;
        double lay_dx1 = lw1.length() / 2.0;
        double lay_dz = dbref.cellthickness(isuper) * dbref.wpdist(isuper) / 2.0 - microgap;
        double lay_skew = lw0.center().minus(lw1.center()).angle(lw1.dir()) - Math.toRadians(90.0);
        
        Vector3d lcent = lw0.center().plus(lw1.center()).dividedBy(2.0);
        G4Trap layerVolume = new G4Trap("sl" + (isuper + 1) + "_layer" + ilayer,
                lay_dz, -dbref.thtilt(isuper / 2), Math.toRadians(90.0),
                lay_dy, lay_dx0, lay_dx1, lay_skew,
                lay_dy, lay_dx0, lay_dx1, lay_skew);

        layerVolume.setPosition(lcent.x, lcent.y, lcent.z);

        return layerVolume;
    }

    public static void main(String[] args) {
        ConstantProvider cp = GeometryFactory.getConstants(DetectorType.DC, 11, "default");
        GEMGeant4Factory factory = new GEMGeant4Factory(cp);
            
        for(Geant4Basic volume : factory.getAllVolumes()) {
            System.out.println(volume.gemcString());
        }
    }
}