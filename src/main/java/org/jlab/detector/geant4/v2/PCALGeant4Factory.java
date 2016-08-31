/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2;

import org.jlab.detector.volume.G4Trap;
import org.jlab.detector.volume.G4World;
import org.jlab.detector.volume.Geant4Basic;
import org.jlab.geom.base.ConstantProvider;

/**
 *
 * @author kenjo
 */
public final class PCALGeant4Factory extends Geant4Factory {

    private final double microgap = 0.1;
    private final double extrathickness = 0.5;
    private final double virtualzero = 0.00000001;

    private final int nsectors, nviews, nlayers, nsteel, nfoam;
    private final double dsteel, dfoam, dlead, dstrip, umax, wmax, thview, uheight, wheight, walpha;

    public PCALGeant4Factory(ConstantProvider cp) {
        motherVolume = new G4World("fc");

        nsectors = cp.getInteger("/geometry/pcal/pcal/nsectors", 0);
        nlayers = cp.getInteger("/geometry/pcal/pcal/nlayers", 0);
        nviews = cp.getInteger("/geometry/pcal/pcal/nviews", 0);
        nsteel = cp.getInteger("/geometry/pcal/pcal/nsteel", 0);
        nfoam = cp.getInteger("/geometry/pcal/pcal/nfoam", 0);

        dsteel = cp.getDouble("/geometry/pcal/pcal/steel_thick", 0);
        dfoam = cp.getDouble("/geometry/pcal/pcal/foam_thick", 0);
        dlead = cp.getDouble("/geometry/pcal/pcal/lead_thick", 0);
        dstrip = cp.getDouble("/geometry/pcal/pcal/strip_thick", 0);

        umax = cp.getDouble("/geometry/pcal/Uview/max_length", 0);
        wmax = cp.getDouble("/geometry/pcal/Wview/max_length", 0);

        thview = Math.toRadians(cp.getDouble("/geometry/pcal/pcal/view_angle", 0));
        uheight = umax * Math.tan(thview) / 2.0;
        wheight = wmax * Math.sin(2 * thview);
        walpha = Math.atan((0.5 - 2.0 * Math.pow(Math.cos(thview), 2.0)) * wmax / wheight);

        for (int isec = 1; isec <= 1; isec++) {
            PCALSector sectorVolume = new PCALSector(isec);
            sectorVolume.setMother(motherVolume);
        }
    }

    private Layer getULayer(int ilayer, int isector) {
        Layer uLayer = new ULayer(ilayer, isector);
        uLayer.setName("U-scintillator_" + (ilayer * 3 + 1) + "_s" + isector);
        return uLayer;
    }

    private Layer getVLayer(int ilayer, int isector) {
        Layer vLayer = new WLayer(ilayer, isector);
        vLayer.setName("V-scintillator_" + (ilayer * 3 + 2) + "_s" + isector);
        vLayer.rotate("xyz", 0, Math.toRadians(180), thview);
        return vLayer;
    }

    private Layer getWLayer(int ilayer, int isector) {
        Layer wLayer = new WLayer(ilayer, isector);
        wLayer.setName("W-scintillator_" + (ilayer * 3 + 3) + "_s" + isector);
        wLayer.rotate("xyz", 0, 0, thview);
        return wLayer;
    }

    private class Layer extends G4Trap {

        private final double thickness;

        protected Layer(String name, double pDz, double pDy, double pDx1, double pDx2, double pAlpha) {
            super(name, pDz, 0, 0, pDy, pDx1, pDx2, pAlpha, pDy, pDx1, pDx2, pAlpha);
            this.thickness = 2.0 * pDz;
        }

        private Layer(String name, double thickness) {
            //G4Trap dimensions for U layer, same for all lead, stell, foam layers
            //used for creation of all passive layers
            this(name, thickness / 2.0, uheight / 2.0, virtualzero, umax / 2.0, 0);
        }

        public double shiftZ(double dz) {
            translate(0, 0, dz + thickness / 2.0);
            return dz + thickness;
        }
    }

    private final class ULayer extends Layer {
        private ULayer(int ilayer, int isector) {
            super("U-scintillator_" + (ilayer * 3 + 1) + "_s" + isector, dstrip);
        }
    }

    private final class WLayer extends Layer {
        private WLayer(int ilayer, int isector) {
            super("", dstrip / 2.0, wheight / 2.0, virtualzero, wmax / 2.0, -walpha);
        }
    }

    private final class PCALSector {

        private final G4Trap sectorVolume;
        //G4Trap dimensions for sector volume (mother volume)
        private final double hsector = uheight + 2.0 * extrathickness;
        private final double dsector = nsteel * dsteel + nfoam * dfoam
                + nviews * nlayers * dstrip
                + (nviews * nlayers - 1) * dlead
                + (2 * nviews * nlayers + nsteel + nfoam) * microgap;
        private double layerPos;
        private final int isector;

        public PCALSector(int isector) {
            sectorVolume = new G4Trap("pcal_s" + isector, dsector / 2.0, 0, 0, hsector / 2.0, virtualzero, umax / 2.0, 0, hsector / 2.0, virtualzero, umax / 2.0, 0);
            this.isector = isector;
            layerPos = -dsector / 2.0 + microgap;

            makeWindow("Front");

            int ilead = 1;
            for (int ilayer = 0; ilayer < nlayers; ilayer++) {
                for (Layer lVol : new Layer[]{
                    getULayer(ilayer, isector),
                    getVLayer(ilayer, isector),
                    getWLayer(ilayer, isector)}) {

                    lVol.setMother(sectorVolume);
                    layerPos = lVol.shiftZ(layerPos) + microgap;
/*
                    if (ilead < 15) {
                        Layer leadVol = new Layer("PCAL_Lead_Layer_" + (ilead++) + "_s" + isector, dlead);
                        leadVol.setMother(sectorVolume);
                        layerPos = lVol.shiftZ(layerPos) + microgap;
                    }
*/
                }
                makeWindow("Back");
            }
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
