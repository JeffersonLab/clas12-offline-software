/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2;

import eu.mihosoft.vrl.v3d.Vector3d;
import org.jlab.detector.units.SystemOfUnits.Length;
import org.jlab.detector.volume.G4Trap;
import org.jlab.detector.volume.G4World;
import org.jlab.detector.volume.Geant4Basic;
import org.jlab.geom.base.ConstantProvider;

/**
 *
 * @author kenjo
 */
public final class ECGeant4Factory extends Geant4Factory {

    private final double microgap = 0.0001;
    private final double virtualzero = 1e-6;

    private final int nsectors, nviews, nlayers;
    private final int nustrips, nwstrips, nvstrips;
    private final double dlead, dstrip, dwrap, dalum;
    private final double wUstrip, dwUstrip, wVstrip, dwVstrip, wWstrip, dwWstrip;
    private final double thtilt, thview, walpha;
    private final double dist2tgt, dist2cnt, shiftcnt;

    public ECGeant4Factory(ConstantProvider cp) {
        motherVolume = new G4World("fc");

        nsectors = cp.getInteger("/geometry/ec/ec/nsectors", 0);
        nlayers = cp.getInteger("/geometry/ec/ec/nlayers", 0);
        nviews = cp.getInteger("/geometry/ec/ec/nviews", 0);

        nustrips = cp.getInteger("/geometry/ec/uview/nstrips", 0);
        nvstrips = cp.getInteger("/geometry/ec/vview/nstrips", 0);
        nwstrips = cp.getInteger("/geometry/ec/wview/nstrips", 0);

        dlead = cp.getDouble("/geometry/ec/ec/lead_thick", 0) * Length.mm;
        dstrip = cp.getDouble("/geometry/ec/ec/strip_thick", 0) * Length.mm;
        dwrap = cp.getDouble("/geometry/ec/ec/wrapper_thick", 0) * Length.mm;
        dalum = cp.getDouble("/geometry/ec/ec/alum_thick", 0) * Length.mm;

        dist2tgt = (cp.getDouble("/geometry/ec/ec/dist2tgt", 0)) * Length.mm;
        dist2cnt = cp.getDouble("/geometry/ec/ec/dist2cnt", 0) * Length.mm;
        shiftcnt = cp.getDouble("/geometry/ec/ec/a1", 0) * Length.mm;

        wUstrip = cp.getDouble("/geometry/ec/uview/a5", 0) * Length.mm;
        dwUstrip = cp.getDouble("/geometry/ec/uview/a6", 0) * Length.mm;

        wVstrip = cp.getDouble("/geometry/ec/vview/a5", 0) * Length.mm;
        dwVstrip = cp.getDouble("/geometry/ec/vview/a6", 0) * Length.mm;

        wWstrip = cp.getDouble("/geometry/ec/wview/a5", 0) * Length.mm;
        dwWstrip = cp.getDouble("/geometry/ec/wview/a6", 0) * Length.mm;

        thview = Math.toRadians(cp.getDouble("/geometry/ec/ec/view_angle", 0));
        thtilt = Math.toRadians(cp.getDouble("/geometry/ec/ec/thtilt", 0));
        walpha = Math.atan((Math.pow(Math.tan(thview), 2.0) - 3) / 4 / Math.tan(thview));

        for (int sec = 1; sec <= nsectors; sec++) {
            ECSector sectorVolume = new ECSector(sec);
            sectorVolume.setMother(motherVolume);
        }
    }

    private Layer getULayer(int iuvw, int isector) {
        int ilayer = iuvw * 3 + 1;
        double height = nustrips * (wUstrip + (ilayer - 1) * dwUstrip) + 2.0 * virtualzero;
        double halfbase = height / Math.tan(thview);

        Layer uLayer = new Layer("U-scintillator_" + ilayer + "_s" + isector + "_view_1_stack_" + ((ilayer < 16) ? 1 : 2), dstrip,
                height / 2.0, virtualzero, halfbase, 0);
        uLayer.populateUstrips(ilayer, isector);
        return uLayer;
    }

    private Layer getVLayer(int iuvw, int isector) {
        int ilayer = iuvw * 3 + 2;
        double height = nvstrips * (wVstrip + (ilayer - 2) * dwVstrip) + 2.0 * virtualzero;
        double lenbtm = height / Math.sin(2.0 * thview);
        Layer vLayer = new Layer("V-scintillator_" + ilayer + "_s" + isector + "_view_2_stack_" + ((ilayer < 16) ? 1 : 2), dstrip,
                height / 2.0, lenbtm / 2.0, virtualzero, -walpha);
        vLayer.layerVol.rotate("zyx", thview, Math.toRadians(180), 0);

        vLayer.populateVstrips(ilayer, isector);
        vLayer.align(ilayer);

        return vLayer;
    }

    private Layer getWLayer(int iuvw, int isector) {
        int ilayer = iuvw * 3 + 3;
        double height = nwstrips * (wWstrip + (ilayer - 3) * dwWstrip) + 2.0 * virtualzero;
        double lenbtm = height / Math.sin(2.0 * thview);
        Layer wLayer = new Layer("W-scintillator_" + ilayer + "_s" + isector + "_view_3_stack_" + ((ilayer < 16) ? 1 : 2), dstrip,
                height / 2.0, lenbtm / 2.0, virtualzero, -walpha);
        wLayer.layerVol.rotate("zyx", thview, 0, 0);
        wLayer.populateWstrips(ilayer, isector);
        wLayer.align(ilayer);

        return wLayer;
    }

    private final class Layer {

        final G4Trap layerVol;
        final double thickness;

        public Layer(String name, double thickness, int ilayer) {
            double height = getLeadHeight(ilayer) + 2.0 * virtualzero;
            double halfbase = height / Math.tan(thview);
            layerVol = new G4Trap(name, thickness / 2.0, 0, 0,
                    height / 2.0, virtualzero, halfbase, 0,
                    height / 2.0, virtualzero, halfbase, 0);
            this.thickness = thickness;
        }

        public Layer(String name, double thickness, double pDy, double pDx1, double pDx2, double pAlpha) {
            layerVol = new G4Trap(name, thickness / 2.0, 0, 0,
                    pDy, pDx1, pDx2, pAlpha,
                    pDy, pDx1, pDx2, pAlpha);
            this.thickness = thickness;
        }

        public final double getLayerHeight(int ilayer) {
            switch ((ilayer - 1) % 3) {
                case 1:
                    return (wVstrip + (ilayer - 2) * dwVstrip) * nvstrips;
                case 2:
                    return (wWstrip + (ilayer - 3) * dwWstrip) * nwstrips;
            }
            return (wUstrip + (ilayer - 1) * dwUstrip) * nustrips;
        }

        public final double getLeadHeight(int ilayer) {
            double width = getLayerHeight(ilayer);
            if ((ilayer - 1) % 3 == 0) {
                return width;
            }
            return width / Math.sin(2 * thview) * Math.sin(thview);
        }

        public void align(int ilayer) {
            double height = getLayerHeight(ilayer);
            double uheight = getLeadHeight(ilayer);
            Vector3d uVertex = new Vector3d(0, -uheight / 2.0, 0);
            Vector3d trivertex = new Vector3d(height * (0.5 / Math.sin(2.0 * thview) + 0.125 * (Math.tan(thview) * Math.tan(thview) - 3) / Math.tan(thview)), -height / 2.0, 0);
            layerVol.translate(uVertex.minus(layerVol.getLocalTransform().transform(trivertex)));
        }

        public double shiftZ(double dx, double dy, double dz) {
            layerVol.translate(dx, dy, dz + thickness / 2.0);
            return dz + thickness;
        }

        public void populateUstrips(int ilayer, int isector) {
            double uwidth = wUstrip + (ilayer - 1) * dwUstrip;
            double uheight = getLayerHeight(ilayer);

            double hshort = virtualzero;
            for (int istrip = 1; istrip <= nustrips; istrip++) {
                double hlong = hshort + uwidth;
                double lshort = hshort / Math.tan(thview);
                double llong = hlong / Math.tan(thview);
                G4Trap stripVol = new ECstrip(layerVol.getName().charAt(0) + "_strip_" + ilayer
                        + "_" + istrip + "_s" + isector + "_stack_" + ((ilayer < 16) ? 1 : 2),
                        dstrip / 2.0, 0, 0,
                        uwidth / 2.0, lshort, llong, 0,
                        uwidth / 2.0, lshort, llong, 0);

                stripVol.makeSensitive();
                stripVol.setMother(layerVol);
                stripVol.translate(0, (-uheight + hshort + hlong) / 2.0, 0);

                hshort += uwidth;
            }
        }

        public void populateVstrips(int ilayer, int isector) {
            populateVWstrips(ilayer, isector, nvstrips, wVstrip + (ilayer - 2) * dwVstrip);
        }

        public void populateWstrips(int ilayer, int isector) {
            populateVWstrips(ilayer, isector, nwstrips, wWstrip + (ilayer - 3) * dwWstrip);
        }

        public void populateVWstrips(int ilayer, int isector, int nstrips, double stripwidth) {
            double height = getLayerHeight(ilayer);

            double hshort = virtualzero;
            for (int istrip = 1; istrip <= nstrips; istrip++) {
                double hlong = hshort + stripwidth;
                double lshort = hshort / Math.sin(2.0 * thview);
                double llong = hlong / Math.sin(2.0 * thview);

                G4Trap stripVol = new ECstrip(layerVol.getName().charAt(0) + "_strip_" + ilayer
                        + "_" + istrip + "_s" + isector + "_stack_" + ((ilayer < 16) ? 1 : 2),
                        dstrip / 2.0, 0, 0,
                        stripwidth / 2.0, llong / 2.0, lshort / 2.0, -walpha,
                        stripwidth / 2.0, llong / 2.0, lshort / 2.0, -walpha);

                double ystrip = (height - hlong - hshort) / 2.0;
                double xstrip = ystrip * Math.tan(-walpha);
                stripVol.translate(xstrip, ystrip, 0);
                stripVol.makeSensitive();
                stripVol.setMother(layerVol);

                hshort += stripwidth;
            }
        }
    }

    private final class ECSector {

        private final double dfoam = 5.08, dsteel = 0.15875;

        private final double extrathickness = 0.05;
        //G4Trap dimensions for sector volume (mother volume)
        private final double dsector = dfoam + dsteel * 2.0 + nviews * nlayers * dstrip
                + (nviews * nlayers - 1) * dlead
                + (2 * nviews * nlayers + 3) * microgap;
        private final double dist2midplane = dist2tgt
                + (nviews * nlayers * dstrip
                + (nviews * nlayers - 1) * dlead
                + (2 * nviews * nlayers - 2) * microgap) / 2.0;
        ;

        private double layerPos;
        private final G4Trap sectorVolume;

        public ECSector(int sector) {
            double height = (wUstrip + 39 * dwUstrip) * nustrips + 2 * 39 * shiftcnt;
            double halfbase = height / Math.tan(thview);

            sectorVolume = new G4Trap("ec_s" + sector, dsector / 2.0 + extrathickness, 0, 0,
                    height / 2.0 + extrathickness, virtualzero, halfbase + extrathickness, 0,
                    height / 2.0 + extrathickness, virtualzero, halfbase + extrathickness, 0);

            double secphi = Math.toRadians(90 - (sector - 1) * 60);
            sectorVolume.rotate("yxz", 0, thtilt, secphi);

            Vector3d secPos = new Vector3d(0, 0, dist2midplane)
                    .rotateX(-thtilt)
                    .add(0, -dist2cnt * Math.cos(thtilt), dist2cnt * Math.sin(thtilt))
                    .rotateZ(-secphi);
            sectorVolume.translate(secPos);

            layerPos = -dsector / 2.0 + microgap;
            int ilayer = 1;

            Layer steelVol1 = new Layer("eclid1_s" + sector, dsteel, ilayer);
            steelVol1.layerVol.setMother(sectorVolume);
            layerPos = steelVol1.shiftZ(0, 0, layerPos) + microgap;
            Layer alumVol = new Layer("eclid2_s" + sector, dfoam, ilayer);
            alumVol.layerVol.setMother(sectorVolume);
            layerPos = alumVol.shiftZ(0, 0, layerPos) + microgap;
            Layer steelVol2 = new Layer("eclid3_s" + sector, dsteel, ilayer);
            steelVol2.layerVol.setMother(sectorVolume);
            layerPos = steelVol2.shiftZ(0, 0, layerPos) + microgap;

            for (int iuvw = 0; iuvw < nlayers; iuvw++) {
                for (Layer uvwVol : new Layer[]{
                    getULayer(iuvw, sector),
                    getVLayer(iuvw, sector),
                    getWLayer(iuvw, sector)}) {

                    if (ilayer > 1) {
                        Layer leadVol = new Layer(String.format("lead_%d_s%d_view_%d_stack_%d",
                                ilayer, sector, (ilayer - 1) % 3 + 1, (ilayer > 15) ? 2 : 1), dlead, ilayer);
                        leadVol.layerVol.setMother(sectorVolume);
                        layerPos = leadVol.shiftZ(0, (ilayer - 1) * shiftcnt, layerPos) + microgap;
                    }

                    uvwVol.layerVol.setMother(sectorVolume);
                    layerPos = uvwVol.shiftZ(0, (ilayer - 1) * shiftcnt, layerPos) + microgap;

                    ilayer++;
                }
            }
        }

        public void setMother(Geant4Basic motherVolume) {
            sectorVolume.setMother(motherVolume);
        }
    }

    private class ECstrip extends G4Trap {

        final int iview;

        ECstrip(String name, double pDz, double pTheta, double pPhi,
                double pDy1, double pDx1, double pDx2, double pAlp1,
                double pDy2, double pDx3, double pDx4, double pAlp2) {
            super(name, pDz, pTheta, pPhi, pDy1, pDx1, pDx2, pAlp1, pDy2, pDx3, pDx4, pAlp2);

            iview = name.charAt(0) - 'U';
        }

        @Override
        public Vector3d getVertex(int ivertex) {
            int ipol = ivertex / 4;
            if (iview == 1) {
                ipol = 1 - ipol;
            }

            int[][][] ivert = {
                {{0, 3, 1, 2}, {0, 1, 3, 2}},
                {{1, 2, 0, 3}, {3, 2, 0, 1}},
                {{2, 1, 3, 0}, {2, 3, 1, 0}}
            };
            return volumeCSG.getPolygons().get(4 + ipol).vertices.get(ivert[iview][ipol][ivertex % 4]).pos;
        }
    }

    public int getNumberOfSectors() {
        return nsectors;
    }

    public int getNumberOfLayers() {
        return nviews * nlayers;
    }

    public int getNumberOfPaddles() {
        return nustrips;
    }

    public G4Trap getPaddle(int isector, int ilayer, int ipaddle) {
        int iview = (ilayer - 1) % 3;
        int[] npaddles = {nustrips, nvstrips, nwstrips};
        if (isector < 1 || isector > nsectors || ilayer < 1 || ilayer > nlayers*nviews || ipaddle < 1 || ipaddle > npaddles[iview]) {
            System.err.println(String.format("Paddle #%d in sector %d, layer %d doesn't exist", ipaddle, isector, ilayer));
            throw new IndexOutOfBoundsException();
        }

        return (G4Trap) motherVolume.getChildren().get(isector - 1)
                .getChildren().get(3 + (ilayer - 1) * 2)
                .getChildren().get(ipaddle - 1);
    }

}
