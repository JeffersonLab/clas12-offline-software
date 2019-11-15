/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2;

import eu.mihosoft.vrl.v3d.Vector3d;
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
    private double ministagger ;

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
        ministagger = cp.getDouble(dcdbpath + "ministagger/ministagger", 0);

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
    
    public double ministagger() {
        return ministagger;
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

final class Wire {

    private final int ireg;
    private final int isuper;
    private final int ilayer;
    private final int iwire;
    private final DCdatabase dbref = DCdatabase.getInstance();

    private final Vector3d midpoint;
    private final Vector3d center;
    private final Vector3d direction;
    private Vector3d leftend;
    private Vector3d rightend;

    public Wire translate(Vector3d vshift) {
        midpoint.add(vshift);
        center.add(vshift);
        leftend.add(vshift);
        rightend.add(vshift);

        return this;
    }

    public Wire rotateX(double rotX) {
        midpoint.rotateX(rotX);
        center.rotateX(rotX);
        direction.rotateX(rotX);
        leftend.rotateX(rotX);
        rightend.rotateX(rotX);

        return this;
    }

    public Wire rotateY(double rotY) {
        midpoint.rotateY(rotY);
        center.rotateY(rotY);
        direction.rotateY(rotY);
        leftend.rotateY(rotY);
        rightend.rotateY(rotY);

        return this;
    }

    public Wire rotateZ(double rotZ) {
        midpoint.rotateZ(rotZ);
        center.rotateZ(rotZ);
        direction.rotateZ(rotZ);
        leftend.rotateZ(rotZ);
        rightend.rotateZ(rotZ);

        return this;
    }

    private void findEnds() {
        // define vector from wire midpoint to chamber tip (z is wrong!!)
        Vector3d vnum = new Vector3d(0, dbref.xdist(ireg), 0);
        vnum.sub(midpoint);

        double copen = Math.cos(dbref.thopen(ireg) / 2.0);
        double sopen = Math.sin(dbref.thopen(ireg) / 2.0);

        // define unit vector normal to the sides of the chamber and pointing inside
        Vector3d rnorm = new Vector3d(copen, sopen, 0);
        Vector3d lnorm = new Vector3d(-copen, sopen, 0);

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
        double hh = (iwire-1 + ((double)(ilayer % 2)) / 2.0) * dw2;
        if(ireg==2 && isSensitiveWire(isuper, ilayer, iwire) && dbref.getMinistaggerStatus())
                hh += ((ilayer%2)*2-1)*dbref.ministagger();

        // ll: layer distance
        double tt = dbref.cellthickness(isuper) * dbref.wpdist(isuper);
        double ll = ilayer * tt;

        // wire x=0 coordinates in the lab
        double ym = y0mid + ll * stilt + hh * ctilt;
        double zm = z0mid + ll * ctilt - hh * stilt;

        // wire midpoint in the lab
        midpoint = new Vector3d(0, ym, zm);
        direction = new Vector3d(1, 0, 0);
        direction.rotateZ(dbref.thster(isuper));
        direction.rotateX(-dbref.thtilt(ireg));
        findEnds();
        center = leftend.plus(rightend).dividedBy(2.0);
    }

    private boolean isSensitiveWire(int isuper, int ilayer, int iwire) {
        return iwire>0 && iwire<=dbref.nsensewires() &&
                ilayer>0 && ilayer<=dbref.nsenselayers(isuper);
    }

    public Vector3d mid() {
        return new Vector3d(midpoint);
    }

    public Vector3d left() {
        return new Vector3d(leftend);
    }

    public Vector3d right() {
        return new Vector3d(rightend);
    }

    public Vector3d dir() {
        return new Vector3d(direction);
    }

    public Vector3d top() {
        if (leftend.y < rightend.y) {
            return new Vector3d(rightend);
        }
        return new Vector3d(leftend);
    }

    public Vector3d bottom() {
        if (leftend.y < rightend.y) {
            return new Vector3d(leftend);
        }
        return new Vector3d(rightend);
    }

    public double length() {
        return leftend.minus(rightend).magnitude();
    }

    public Vector3d center() {
        return new Vector3d(center);
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

    private final Wire[][][][] wires;
    private final Vector3d[][][] layerMids;
    private final Vector3d[][] regionMids;

    public static boolean MINISTAGGERON=true;
    public static boolean MINISTAGGEROFF=false;

    ///////////////////////////////////////////////////
    public DCGeant4Factory(ConstantProvider provider) {
        this(provider, MINISTAGGEROFF);
    }

    ///////////////////////////////////////////////////
    public DCGeant4Factory(ConstantProvider provider, boolean ministaggerStatus) {
        dbref.setMinistaggerStatus(ministaggerStatus);

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

        // define wire and layer points in tilted coordinate frame (z axis is perpendicular to the chamber, y is along the wire)
        wires = new Wire[dbref.nsectors()][dbref.nsuperlayers()][][];
        layerMids = new Vector3d[dbref.nsectors()][dbref.nsuperlayers()][];
        regionMids = new Vector3d[dbref.nsectors()][dbref.nregions()];

        for(int isec = 0; isec < dbref.nsectors(); isec++) {
            for(int iregion=0; iregion<dbref.nregions(); iregion++) {
                regionMids[isec][iregion] = getRegion(isec, iregion).getGlobalPosition()
				.add(dbref.getAlignmentShift(isec, iregion))
				.rotateZ(Math.toRadians(-isec * 60))
				.rotateY(-dbref.thtilt(iregion));
/*
                //define layerMid using wires (produce slight shift compared to GEMC volumes)
                regionMids[isec][iregion] = new Vector3d(layerMids[isec][iregion*2][dbref.nsenselayers(iregion*2)-1]);
                regionMids[isec][iregion] = regionMids[isec][iregion].plus(layerMids[isec][iregion*2+1][0]).dividedBy(2.0);
*/
            }

            for(int isuper=0; isuper<dbref.nsuperlayers(); isuper++) {
                layerMids[isec][isuper]  = new Vector3d[dbref.nsenselayers(isuper)];

                for(int ilayer=0; ilayer<dbref.nsenselayers(isuper); ilayer++) {
/*
                    //define layerMid using wires (produce slight shift compared to GEMC volumes)
                    Wire firstWire = new Wire(isuper, ilayer+1, 1);
                    Wire lastWire = new Wire(isuper, ilayer+1, dbref.nsensewires());
                    Vector3d firstMid = firstWire.mid().rotateZ(Math.toRadians(-90.0)).rotateY(-dbref.thtilt(isuper/2));
                    Vector3d lastMid = lastWire.mid().rotateZ(Math.toRadians(-90.0)).rotateY(-dbref.thtilt(isuper/2));
                    layerMids[isec][isuper][ilayer] = firstMid.plus(lastMid).dividedBy(2.0);
*/
                    layerMids[isec][isuper][ilayer] = getLayer(isec, isuper, ilayer).getGlobalPosition()
					.add(dbref.getAlignmentShift(isec, isuper/2))
					.rotateZ(Math.toRadians(- isec * 60))
					.rotateY(-dbref.thtilt(isuper/2));
                }
            }

            for(int isuper=0; isuper<dbref.nsuperlayers(); isuper++) {
                wires[isec][isuper]   = new Wire[dbref.nsenselayers(isuper)][dbref.nsensewires()];
                for(int ilayer=0; ilayer<dbref.nsenselayers(isuper); ilayer++) {
                    layerMids[isec][isuper][ilayer].add(regionMids[isec][isuper/2].times(-1.0));
                    layerMids[isec][isuper][ilayer].rotateZ(Math.toRadians(dbref.getAlignmentThetaZ(isec, isuper/2)));
                    layerMids[isec][isuper][ilayer].rotateX(Math.toRadians(dbref.getAlignmentThetaX(isec, isuper/2)));
                    layerMids[isec][isuper][ilayer].rotateY(Math.toRadians(dbref.getAlignmentThetaY(isec, isuper/2)));
                    layerMids[isec][isuper][ilayer].add(regionMids[isec][isuper/2]);

                    for(int iwire=0; iwire<dbref.nsensewires(); iwire++) {
                        wires[isec][isuper][ilayer][iwire] = new Wire(isuper, ilayer+1, iwire+1);

                        wires[isec][isuper][ilayer][iwire].rotateZ(Math.toRadians(-90.0 + isec * 60))
						.translate(dbref.getAlignmentShift(isec, isuper/2))
						.rotateZ(Math.toRadians(-isec * 60))
						.rotateY(-dbref.thtilt(isuper/2));

                        //dc alignment implementation
                        wires[isec][isuper][ilayer][iwire].translate(regionMids[isec][isuper/2].times(-1.0));
                        wires[isec][isuper][ilayer][iwire].rotateZ(Math.toRadians(dbref.getAlignmentThetaZ(isec, isuper/2)));
                        wires[isec][isuper][ilayer][iwire].rotateX(Math.toRadians(dbref.getAlignmentThetaX(isec, isuper/2)));
                        wires[isec][isuper][ilayer][iwire].rotateY(Math.toRadians(dbref.getAlignmentThetaY(isec, isuper/2)));
                        wires[isec][isuper][ilayer][iwire].translate(regionMids[isec][isuper/2]);
    //                    System.out.println((isuper+1) + " " + (ilayer+1) + " " + (iwire+1) + " " + wireLefts[isuper][ilayer][iwire] + " " + wireMids[isuper][ilayer][iwire] + " " + wireRights[isuper][ilayer][iwire]);
                   }
                }

            }
        }
    }

    public Vector3d getWireMidpoint(int isec, int isuper, int ilayer, int iwire) {
        return wires[isec][isuper][ilayer][iwire].mid();
    }

    public Vector3d getWireLeftend(int isec, int isuper, int ilayer, int iwire) {
        return wires[isec][isuper][ilayer][iwire].left();
    }

    public Vector3d getWireRightend(int isec, int isuper, int ilayer, int iwire) {
        return wires[isec][isuper][ilayer][iwire].right();
    }

    public Vector3d getRegionMidpoint(int isec, int iregion) {
        return regionMids[isec][iregion].clone();
    }

    public Vector3d getLayerMidpoint(int isec, int isuper, int ilayer) {
        return layerMids[isec][isuper][ilayer].clone();
    }

    public Vector3d getWireMidpoint(int isuper, int ilayer, int iwire) {
        return wires[0][isuper][ilayer][iwire].mid();
    }

    public Vector3d getWireLeftend(int isuper, int ilayer, int iwire) {
        return wires[0][isuper][ilayer][iwire].left();
    }

    public Vector3d getWireRightend(int isuper, int ilayer, int iwire) {
        return wires[0][isuper][ilayer][iwire].right();
    }

    public Vector3d getRegionMidpoint(int iregion) {
        return regionMids[0][iregion].clone();
    }

    public Vector3d getLayerMidpoint(int isuper, int ilayer) {
        return layerMids[0][isuper][ilayer].clone();
    }

    public Vector3d getWireDirection(int isuper, int ilayer, int iwire) {
        return wires[0][isuper][ilayer][iwire].dir();
    }

    private Geant4Basic getRegion(int isec, int ireg) {
        return motherVolume.getChildren().get(ireg*6+isec);
    }

    private Geant4Basic getLayer(int isec, int isuper, int ilayer) {
        return getRegion(isec, isuper/2).getChildren().get((isuper%2)*6 + ilayer);
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

        layerVolume.setPosition(lcent.x, lcent.y, lcent.z);

        return layerVolume;
    }

    /*
    public void printWires(){
        System.out.println("hello");
        for(int isup=0;isup<2;isup++)
        for(int il=0;il<8;il+=7)
            for(int iwire=0;iwire<nsgwires+1;iwire+=nsgwires/30){
        Wire regw = new Wire(isup,il,iwire);
        System.out.println("line("+regw.left()+", "+regw.right()+");");
            }
    }
    */
}