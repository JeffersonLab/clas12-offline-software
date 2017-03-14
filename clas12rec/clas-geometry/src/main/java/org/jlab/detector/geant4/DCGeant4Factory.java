/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import org.jlab.geom.geant.Geant4Basic;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.prim.Vector3D;

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
            dist2tgt[ireg] = cp.getDouble(dcdbpath + "region/dist2tgt", ireg);
            xdist[ireg] = cp.getDouble(dcdbpath + "region/xdist", ireg);
            frontgap[ireg] = cp.getDouble(dcdbpath + "region/frontgap", ireg);
            midgap[ireg] = cp.getDouble(dcdbpath + "region/midgap", ireg);
            backgap[ireg] = cp.getDouble(dcdbpath + "region/backgap", ireg);
            thopen[ireg] = cp.getDouble(dcdbpath + "region/thopen", ireg);
            thtilt[ireg] = cp.getDouble(dcdbpath + "region/thtilt", ireg);
        }
        for (int isuper = 0; isuper < nSupers; isuper++) {
            thmin[isuper] = cp.getDouble(dcdbpath + "superlayer/thmin", isuper);
            thster[isuper] = cp.getDouble(dcdbpath + "superlayer/thster", isuper);
            wpdist[isuper] = cp.getDouble(dcdbpath + "superlayer/wpdist", isuper);
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

    private double y0mid, z0mid;
    private double length;

    private Vector3D midpoint;
    private Vector3D center;
    private Vector3D direction;
    private Vector3D leftend;
    private Vector3D rightend;
    private Vector3D topend;
    private Vector3D bottomend;

    private double w2tgt;

    private void findEnds() {
        Vector3D vnum = new Vector3D(0, dbref.xdist(ireg), 0);
        vnum.sub(midpoint);

        double copen = Math.cos(Math.toRadians(dbref.thopen(ireg) / 2.0));
        double sopen = Math.sin(Math.toRadians(dbref.thopen(ireg) / 2.0));

        Vector3D lnorm = new Vector3D(copen, sopen, 0);
        Vector3D rnorm = new Vector3D(-copen, sopen, 0);

        double wlenl = vnum.dot(lnorm) / direction.dot(lnorm);
        leftend = direction.multiply(wlenl).add(midpoint);

        double wlenr = vnum.dot(rnorm) / direction.dot(rnorm);
        rightend = direction.multiply(wlenr).add(midpoint);
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
        w2tgt /= Math.cos(Math.toRadians(dbref.thtilt(ireg) - dbref.thmin(isuper)));

        y0mid = w2tgt * Math.sin(Math.toRadians(dbref.thmin(isuper)));
        z0mid = w2tgt * Math.cos(Math.toRadians(dbref.thmin(isuper)));

        double cster = Math.cos(Math.toRadians(dbref.thster(isuper)));
        double ctilt = Math.cos(Math.toRadians(dbref.thtilt(ireg)));
        double stilt = Math.sin(Math.toRadians(dbref.thtilt(ireg)));

        double dw = 4 * Math.cos(Math.toRadians(30)) * dbref.wpdist(isuper);
        double dw2 = dw / cster;

        double hh = (iwire + (ilayer % 2) / 2.0) * dw2;
        double tt = dbref.cellthickness(isuper) * dbref.wpdist(isuper);
        double ll = ilayer * tt;

        double ym = y0mid + ll * stilt + hh * ctilt;
        double zm = z0mid + ll * ctilt - hh * stilt;

        midpoint = new Vector3D(0, ym, zm);
        direction = new Vector3D(1, 0, 0);
        direction.rotateZ(-Math.toRadians(dbref.thster(isuper)));
        direction.rotateX(-Math.toRadians(dbref.thtilt(ireg)));

        findEnds();

        if (leftend.y() < rightend.y()) {
            topend = rightend;
            bottomend = leftend;
        } else {
            topend = leftend;
            bottomend = rightend;
        }

        length = leftend.clone().sub(rightend).r();
        center = leftend.clone().add(rightend).divide(2.0);
    }

    public Vector3D mid() {
        return midpoint;
    }

    public Vector3D left() {
        return leftend;
    }

    public Vector3D right() {
        return rightend;
    }

    public Vector3D dir() {
        return direction;
    }

    public Vector3D top() {
        return topend;
    }

    public Vector3D bottom() {
        return bottomend;
    }

    public double length() {
        return length;
    }

    public Vector3D center() {
        return center;
    }
}

///////////////////////////////////////////////////
public class DCGeant4Factory {

    DCdatabase dbref = DCdatabase.getInstance();
    private Geant4Basic motherVolume = new Geant4Basic("fc", "Box", 0);
    private HashMap<String, String> properties = new HashMap<String, String>();

    private int nsgwires;

    ///////////////////////////////////////////////////
    public DCGeant4Factory(ConstantProvider provider) {
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

        double y_enlargement = 3.65;
        double z_enlargement = -2.96;
        double dx_shift = y_enlargement*Math.tan(Math.toRadians(29.5));
        
        double reg_dz = (dbref.frontgap(iregion) + dbref.backgap(iregion) + dbref.midgap(iregion) + dbref.superwidth(iregion * 2) + dbref.superwidth(iregion * 2 + 1)) / 2.0 + z_enlargement;
        double reg_dx0 = Math.abs(regw0.bottom().x()) - dx_shift + 1.0;
        double reg_dx1 = Math.abs(regw1.top().x()) + dx_shift + 1.0;
        double reg_dy = regw1.top().clone().sub(regw0.bottom()).y() / Math.cos(Math.toRadians(dbref.thtilt(iregion))) / 2.0 + y_enlargement + 1.0;
        double reg_skew = 0.0;
        double reg_thtilt = dbref.thtilt(iregion);

        Vector3D vcenter = regw1.top().clone().add(regw0.bottom()).divide(2.0);
        vcenter.setX(0);
        Vector3D reg_position0 = new Vector3D(vcenter);
        vcenter.rotateZ(-Math.toRadians(90 - isector * 60));

        double[] params = {reg_dz, -reg_thtilt, 90.0, reg_dy, reg_dx0, reg_dx1, 0.0, reg_dy, reg_dx0, reg_dx1, 0.0};
        Geant4Basic regionVolume = new Geant4Basic("region" + (iregion + 1) + "_s" + (isector + 1), "G4Trap", params);
        regionVolume.setParUnits("cm", "deg", "deg", "cm", "cm", "cm", "deg", "cm", "cm", "cm", "deg");
        regionVolume.setPosition(vcenter.x(), vcenter.y(), vcenter.z());
        regionVolume.setRotation("yxz", 0.0, Math.toRadians(reg_thtilt), Math.toRadians(90.0 - isector * 60.0));
        regionVolume.setId(isector+1, iregion+1, 0, 0);

        for (int isup = 0; isup < 2; isup++) {
            int isuper = iregion * 2 + isup;
            int nsglayers = dbref.nsenselayers(isuper) + dbref.nguardlayers(isuper);
            for (int ilayer = 1; ilayer < nsglayers - 1; ilayer++) {
                Geant4Basic layerVolume = this.createLayer(isuper, ilayer);
                layerVolume.setMother(regionVolume);
                layerVolume.setId(isector+1, iregion+1, isuper+1, ilayer);

                Vector3D lcenter = new Vector3D(layerVolume.getPosition());
                Vector3D lshift = lcenter.clone().sub(reg_position0);
                lshift.rotateX(Math.toRadians(reg_thtilt));

                layerVolume.setName("sl" + (isuper + 1) + "_layer" + ilayer + "_s" + (isector + 1));
                layerVolume.setPosition(lshift.x(), lshift.y(), lshift.z());
                layerVolume.setRotation("zxy", Math.toRadians(dbref.thster(isuper)), 0.0, 0.0);
            }
        }

        return regionVolume;
    }

    ///////////////////////////////////////////////////
    public Geant4Basic createLayer(int isuper, int ilayer) {
        Wire lw0 = new Wire(isuper, ilayer, 0);
        Wire lw1 = new Wire(isuper, ilayer, nsgwires - 1);

        double microgap = 0.01;
        
        Vector3D midline = lw1.mid().clone().sub(lw0.mid());
        double lay_dy = Math.sqrt(Math.pow(midline.r(), 2.0) - Math.pow(midline.dot(lw0.dir()), 2.0)) / 2.0;
        double lay_dx0 = lw0.length() / 2.0;
        double lay_dx1 = lw1.length() / 2.0;
        double lay_dz = dbref.cellthickness(isuper) * dbref.wpdist(isuper) / 2.0 - microgap;
        double lay_skew = Math.toDegrees(lw0.center().clone().sub(lw1.center()).angle(lw1.dir())) - 90;

        Vector3D lcent = lw0.center().clone().add(lw1.center()).divide(2.0);

        double[] params = {lay_dz, -dbref.thtilt(isuper / 2), 90.0, lay_dy, lay_dx0, lay_dx1, lay_skew, lay_dy, lay_dx0, lay_dx1, lay_skew};
        Geant4Basic layerVolume = new Geant4Basic("sl" + (isuper + 1) + "_layer" + ilayer, "G4Trap", params);
        layerVolume.setParUnits("cm", "deg", "deg", "cm", "cm", "cm", "deg", "cm", "cm", "cm", "deg");
        layerVolume.setPosition(lcent.x(), lcent.y(), lcent.z());

        return layerVolume;
    }

    public String toString() {
        StringBuilder str = new StringBuilder();

        for (Geant4Basic regionVolume : motherVolume.getChildren()) {
            str.append(regionVolume.gemcString());
            str.append(System.getProperty("line.separator"));

            for (Geant4Basic layerVolume : regionVolume.getChildren()) {
                str.append(layerVolume.gemcString());
                str.append(System.getProperty("line.separator"));
            }

        }

        return str.toString();
    }

}

/*
for (int ipaddle = 0; ipaddle < paddles.size(); ipaddle++) {
	paddles.get(ipaddle).setName("panel" + gemcLayerNames[layer - 1] + "_sector" + sector + "_paddle_" + (ipaddle + 1));
	paddles.get(ipaddle).setMother(panelVolume);
}
 */
