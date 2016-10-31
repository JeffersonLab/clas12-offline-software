/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2;

import eu.mihosoft.vrl.v3d.Vector3d;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import org.jlab.detector.units.SystemOfUnits.Length;
import org.jlab.detector.volume.G4Trap;
import org.jlab.detector.volume.G4World;
import org.jlab.detector.volume.Geant4Basic;
import org.jlab.geom.base.ConstantProvider;

/**
 *
 * @author kenjo
 */
final class DCdatabase {

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

    private int nsensewires;
    private int nguardwires;

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

}

final class Wire {

    private final int ireg;
    private final int isuper;
    private final int ilayer;
    private final int iwire;
    private final DCdatabase dbref = DCdatabase.getInstance();

    private final double y0mid, z0mid;
    private final double length;

    private final Vector3d midpoint;
    private final Vector3d center;
    private final Vector3d direction;
    private Vector3d leftend;
    private Vector3d rightend;
    private final Vector3d topend;
    private final Vector3d bottomend;

    private double w2tgt;

    private void findEnds() {
        Vector3d vnum = new Vector3d(0, dbref.xdist(ireg), 0);
        vnum.set(vnum.minus(midpoint));

        double copen = Math.cos(dbref.thopen(ireg) / 2.0);
        double sopen = Math.sin(dbref.thopen(ireg) / 2.0);

        Vector3d lnorm = new Vector3d(copen, sopen, 0);
        Vector3d rnorm = new Vector3d(-copen, sopen, 0);

        double wlenl = vnum.dot(lnorm) / direction.dot(lnorm);
        leftend = direction.times(wlenl).add(midpoint);

        double wlenr = vnum.dot(rnorm) / direction.dot(rnorm);
        rightend = direction.times(wlenr).add(midpoint);
    }

    public Wire(int isuper, int ilayer, int iwire) {
        this.isuper = isuper;
        this.ilayer = ilayer;
        this.iwire = iwire;
        this.ireg = isuper / 2;

        w2tgt = dbref.dist2tgt(ireg);
        if (isuper % 2 > 0) {
            w2tgt += dbref.superwidth(isuper - 1) + dbref.midgap(ireg);
        }
        w2tgt /= Math.cos(dbref.thtilt(ireg) - dbref.thmin(isuper));

        y0mid = w2tgt * Math.sin(dbref.thmin(isuper));
        z0mid = w2tgt * Math.cos(dbref.thmin(isuper));

        double cster = Math.cos(dbref.thster(isuper));
        double ctilt = Math.cos(dbref.thtilt(ireg));
        double stilt = Math.sin(dbref.thtilt(ireg));

        double dw = 4 * Math.cos(Math.toRadians(30)) * dbref.wpdist(isuper);
        double dw2 = dw / cster;

        double hh = (iwire + (ilayer % 2) / 2.0) * dw2;
        double tt = dbref.cellthickness(isuper) * dbref.wpdist(isuper);
        double ll = ilayer * tt;

        double ym = y0mid + ll * stilt + hh * ctilt;
        double zm = z0mid + ll * ctilt - hh * stilt;

        midpoint = new Vector3d(0, ym, zm);
        direction = new Vector3d(1, 0, 0);
        direction.rotateZ(-dbref.thster(isuper));
        direction.rotateX(-dbref.thtilt(ireg));

        findEnds();

        if (leftend.y < rightend.y) {
            topend = rightend;
            bottomend = leftend;
        } else {
            topend = leftend;
            bottomend = rightend;
        }

        length = leftend.minus(rightend).magnitude();
        center = leftend.plus(rightend).dividedBy(2.0);
    }

    public Vector3d mid() {
        return midpoint;
    }

    public Vector3d left() {
        return leftend;
    }

    public Vector3d right() {
        return rightend;
    }

    public Vector3d dir() {
        return direction;
    }

    public Vector3d top() {
        return topend;
    }

    public Vector3d bottom() {
        return bottomend;
    }

    public double length() {
        return length;
    }

    public Vector3d center() {
        return center;
    }
}

///////////////////////////////////////////////////
public final class DCGeant4Factory extends Geant4Factory {

    DCdatabase dbref = DCdatabase.getInstance();

    private final HashMap<String, String> properties = new HashMap<>();
    private int nsgwires;

    private final double y_enlargement = 3.65;
    private final double z_enlargement = -2.96;
    private final double microgap = 0.01;
    
    ///////////////////////////////////////////////////
    public DCGeant4Factory(ConstantProvider provider) {
        motherVolume = new G4World("fc");

        dbref.connect(provider);
        nsgwires = dbref.nsensewires() + dbref.nguardwires();

        for (int iregion = 0; iregion < 3; iregion++) {
            for (int isector = 0; isector < 6; isector++) {
                Geant4Basic regionVolume = createRegion(isector, iregion);
                regionVolume.setMother(motherVolume);
            }
        }

        properties.put("email", "mestayer@jlab.org");
        properties.put("author", "mestayer");
        properties.put("date", "05/08/16");
    }

    ///////////////////////////////////////////////////
    public Geant4Basic createRegion(int isector, int iregion) {
        Wire regw0 = new Wire(iregion * 2, 0, 0);
        Wire regw1 = new Wire(iregion * 2 + 1, 7, nsgwires - 1);

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
                lshift = lshift.minus(lcenter);

                System.err.println(lcenter);
                layerVolume.rotate("zxy", dbref.thster(isuper), 0.0, 0.0);
                layerVolume.translate(lshift.x, lshift.y, lshift.z);
                layerVolume.setMother(regionVolume);
                layerVolume.setId(isector + 1, iregion + 1, isuper + 1, ilayer);
            }
        }

        return regionVolume;
    }

    ///////////////////////////////////////////////////
    public Geant4Basic createLayer(int isuper, int ilayer) {
        Wire lw0 = new Wire(isuper, ilayer, 0);
        Wire lw1 = new Wire(isuper, ilayer, nsgwires - 1);

        Vector3d midline = lw1.mid().minus(lw0.mid());
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
        layerVolume.translate(lcent.x, lcent.y, lcent.z);
        System.err.println(lcent);

        return layerVolume;
    }
}

/*
for (int ipaddle = 0; ipaddle < paddles.size(); ipaddle++) {
	paddles.get(ipaddle).setName("panel" + gemcLayerNames[layer - 1] + "_sector" + sector + "_paddle_" + (ipaddle + 1));
	paddles.get(ipaddle).setMother(panelVolume);
}
 */


















