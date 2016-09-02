/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2;

import eu.mihosoft.vrl.v3d.Vector3d;
import org.jlab.detector.volume.G4Trap;
import org.jlab.detector.volume.G4World;
import org.jlab.detector.volume.Geant4Basic;
import org.jlab.geom.base.ConstantProvider;

/**
 *
 * @author kenjo
 */
public final class ECGeant4Factory extends Geant4Factory {

    private final double microgap = 0.1;
    private final double virtualzero = 1e-8;

    private final int nsectors, nviews, nlayers;
    private final int nustrips, nwstrips;
    private final double dlead, dstrip, dwrap;
    private final double wUstrip, dwUstrip, wVstrip, dwVstrip, wWstrip, dwWstrip;
    private final double thtilt, thview, walpha;
    private final double dist2tgt, dist2cnt, shiftcnt;

    public ECGeant4Factory(ConstantProvider cp) {
        motherVolume = new G4World("fc");

        nsectors = cp.getInteger("/geometry/ec/ec/nsectors", 0);
        nlayers = cp.getInteger("/geometry/ec/ec/nlayers", 0);
        nviews = cp.getInteger("/geometry/ec/ec/nviews", 0);

        nustrips = cp.getInteger("/geometry/pcal/Uview/nstrips", 0);
        nwstrips = cp.getInteger("/geometry/pcal/Wview/nstrips", 0);

        dlead = cp.getDouble("/geometry/ec/ec/lead_thick", 0);
        dstrip = cp.getDouble("/geometry/ec/ec/strip_thick", 0);
        dwrap = cp.getDouble("/geometry/ec/ec/wrapper_thick", 0);

        dist2tgt = cp.getDouble("/geometry/ec/ec/dist2tgt", 0);
        dist2cnt = cp.getDouble("/geometry/ec/ec/dist2cnt", 0);
        shiftcnt = cp.getDouble("/geometry/ec/ec/a1", 0);

        wUstrip = cp.getDouble("/geometry/pcal/uview/a5", 0);
        dwUstrip = cp.getDouble("/geometry/pcal/uview/a6", 0);

        wVstrip = cp.getDouble("/geometry/pcal/vview/a5", 0);
        dwVstrip = cp.getDouble("/geometry/pcal/vview/a6", 0);

        wWstrip = cp.getDouble("/geometry/pcal/wview/a5", 0);
        dwWstrip = cp.getDouble("/geometry/pcal/wview/a6", 0);

        thview = Math.toRadians(cp.getDouble("/geometry/ec/ec/view_angle", 0));
        thtilt = Math.toRadians(cp.getDouble("/geometry/ec/ec/thtilt", 0));
        walpha = Math.atan((Math.pow(Math.tan(thview), 2.0) - 3) / 4 / Math.tan(thview));

        for (int isec = 1; isec <= nsectors; isec++) {
            PCALSector sectorVolume = new PCALSector(isec);
            sectorVolume.setMother(motherVolume);
        }
    }

    private final class ULayer {
        private Ulayer(int ilayer, int isector) {
            super("U-scintillator_" + (ilayer * 3 + 1) + "_s" + isector, dstrip);
            uLayer.populateUstrips(ilayer, isector);
        }
    }

    private final class VLayer {
        private VLayer(int ilayer, int isector) {
            int globlayer = ilayer*3+2;
            super("V-scintillator_" + globlayer + "_s" + isector+"_stack_"+((globlayer>15)+1), dstrip,
                    wheight / 2.0, wmax / 2.0, virtualzero, -walpha);
            vLayer.layerVol.rotate("zyx", thview, Math.toRadians(180), 0);

            Vector3d uTopRight = new Vector3d(umax / 2.0, uheight / 2.0, 0);
            Vector3d shiftVec = uTopRight.minus(vLayer.layerVol.getLocalTransform().transform(
                    new Vector3d(-wmax * (Math.pow(Math.cos(thview), 2.0) + 0.25), -wheight / 2.0, 0)));
            vLayer.layerVol.translate(shiftVec);
            //vLayer.populateWstrips(ilayer, isector);
        }
    }

    private final class WLayer {

        private WLayer(int ilayer, int isector) {
            super("W-scintillator_" + (ilayer * 3 + 3) + "_s" + isector, dstrip,
                    wheight / 2.0, wmax / 2.0, virtualzero, -walpha);
            wLayer.layerVol.rotate("xyz", 0, 0, thview);

            Vector3d uTopLeft = new Vector3d(-umax / 2.0, uheight / 2.0, 0);
            Vector3d shiftVec = uTopLeft.minus(wLayer.layerVol.getLocalTransform().transform(
                    new Vector3d(-wmax * (Math.pow(Math.cos(thview), 2.0) + 0.25), -wheight / 2.0, 0)));
            wLayer.layerVol.translate(shiftVec);
            //wLayer.populateWstrips(ilayer, isector);
        }
    }

    private class Layer {

        final G4Trap layerVol;
        final double thickness;

        protected Layer(String name, double thickness, double pDy, double pDx1, double pDx2, double pAlpha) {
            layerVol = new G4Trap(name, thickness / 2.0, 0, 0, pDy, pDx1, pDx2, pAlpha, pDy, pDx1, pDx2, pAlpha);
            this.thickness = thickness;
        }

        private Layer(String name, double thickness) {
            //G4Trap dimensions for U layer, same for all lead, stell, foam layers
            //used for creation of all passive layers
            this(name, thickness, uheight / 2.0, virtualzero, umax / 2.0, 0);
        }

        public double shiftZ(double dz) {
            layerVol.translate(0, 0, dz + thickness / 2.0);
            return dz + thickness;
        }

        public void setMother(Geant4Basic motherVol) {
            layerVol.setMother(motherVol);
        }

        public void populateUstrips(int ilayer, int isector) {
            double w0 = uheight - wstrip * nustrips;
            double hbtm = dwrap;
            double htop = w0 - dwrap;

            for (int istrip = 0; istrip <= nustrips; istrip++) {
                double uwidth = (istrip == 0) ? w0 : wstrip;
                double lhalfbtm = hbtm / Math.tan(thview);
                double lhalftop = htop / Math.tan(thview);

                G4Trap stripVol = new G4Trap(layerVol.getName().charAt(0) + "_single_" + (ilayer + 1) + "_" + istrip + "_s" + isector,
                        dstrip / 2.0 - dwrap, 0, 0,
                        uwidth / 2.0 - dwrap, lhalfbtm, lhalftop, 0,
                        uwidth / 2.0 - dwrap, lhalfbtm, lhalftop, 0);

                stripVol.setMother(layerVol);
                stripVol.translate(0, (-uheight + hbtm + htop) / 2.0, 0);

                hbtm = htop + 2.0 * dwrap;
                htop += wstrip;
            }
        }

        public void populateWstrips(int ilayer, int isector) {
            double w0 = wheight - wstrip * nwstrips;
            double htop = dwrap;
            double hbtm = w0 - dwrap;

            for (int istrip = 0; istrip <= nwstrips; istrip++) {
                double wwidth = (istrip == 0) ? w0 : wstrip;
                double lbtm = hbtm / Math.sin(2.0 * thview);
                double ltop = htop / Math.sin(2.0 * thview);

                G4Trap stripVol = new G4Trap(layerVol.getName().charAt(0) + "_single_" + (ilayer + 1) + "_" + istrip + "_s" + isector,
                        dstrip / 2.0 - dwrap, 0, 0,
                        wwidth / 2.0 - dwrap, lbtm / 2.0, ltop / 2.0, -walpha,
                        wwidth / 2.0 - dwrap, lbtm / 2.0, ltop / 2.0, -walpha);

                double ystrip = (wheight - hbtm - htop) / 2.0;
                double xstrip = ystrip * Math.tan(-walpha);
                stripVol.translate(xstrip, ystrip, 0);
                stripVol.setMother(layerVol);

                htop = hbtm + 2.0 * dwrap;
                hbtm += wstrip;
            }
        }
    }

    private final class PCALSector {

        private final double extrathickness = 0.5;
        //G4Trap dimensions for sector volume (mother volume)
        private final double dsector = nsteel * dsteel + nfoam * dfoam
                + nviews * nlayers * dstrip
                + (nviews * nlayers - 1) * dlead
                + (2 * nviews * nlayers + nsteel + nfoam) * microgap;
        private final double dist2midplane = dist2tgt
                + (nviews * nlayers * dstrip
                + (nviews * nlayers - 1) * dlead
                + (2 * nviews * nlayers - 2) * microgap) / 2.0;
        private final double hshift = uheight / 2.0 - yhigh;

        private double layerPos;
        private final int isector;
        private final G4Trap sectorVolume;

        public PCALSector(int isector) {
            sectorVolume = new G4Trap("pcal_s" + isector, dsector / 2.0 + extrathickness, 0, 0,
                    uheight / 2.0 + extrathickness, virtualzero, umax / 2.0 + extrathickness, 0,
                    uheight / 2.0 + extrathickness, virtualzero, umax / 2.0 + extrathickness, 0);

            double secphi = Math.toRadians(90 - (isector - 1) * 60);
            sectorVolume.rotate("yxz", 0, thtilt, secphi);

            Vector3d secPos = new Vector3d(0, 0, dist2midplane)
                    .rotateX(-thtilt)
                    .add(0, -hshift * Math.cos(thtilt), hshift * Math.sin(thtilt))
                    .rotateZ(-secphi);
            sectorVolume.translate(secPos);

            this.isector = isector;
            layerPos = -dsector / 2.0 + microgap;

            makeWindow("Front");
            int ilead = 1;
            for (int ilayer = 0; ilayer < nlayers; ilayer++) {
                for (Layer uvwVol : new Layer[]{
                    getULayer(ilayer, isector),
                    getVLayer(ilayer, isector),
                    getWLayer(ilayer, isector)}) {

                    uvwVol.setMother(sectorVolume);
                    layerPos = uvwVol.shiftZ(layerPos) + microgap;

                    if (ilead < 15) {
                        Layer leadVol = new Layer("PCAL_Lead_Layer_" + (ilead++) + "_s" + isector, dlead);
                        leadVol.setMother(sectorVolume);
                        layerPos = leadVol.shiftZ(layerPos) + microgap;
                    }

                }
            }
            makeWindow("Back");
        }

        public void makeWindow(String winname) {
            for (Layer layerVol : new Layer[]{
                new Layer("Stainless_Steel_" + winname + "_1_s" + isector, dsteel),
                new Layer("Last-a-Foam_" + winname + "_s" + isector, dfoam),
                new Layer("Stainless_Steel_" + winname + "_2_s" + isector, dsteel)}) {

                layerVol.setMother(sectorVolume);
                layerPos = layerVol.shiftZ(layerPos) + microgap;
            }
        }

        public void setMother(Geant4Basic motherVolume) {
            sectorVolume.setMother(motherVolume);
        }
    }

}
