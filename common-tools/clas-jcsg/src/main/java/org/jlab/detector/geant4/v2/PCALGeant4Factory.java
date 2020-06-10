/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2;

import eu.mihosoft.vrl.v3d.Vector3d;
import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import static org.jlab.detector.hits.DetId.PCALID;
import org.jlab.detector.units.SystemOfUnits.Length;
import org.jlab.detector.volume.G4Trap;
import org.jlab.detector.volume.G4World;
import org.jlab.detector.volume.Geant4Basic;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.prim.Plane3D;

/**
 *
 * @author kenjo
 */
public final class PCALGeant4Factory extends Geant4Factory {

    private final double microgap = 0.0001;
    private final double virtualzero = 1e-6;

    private final int nsectors, nviews, nlayers, nsteel, nfoam;
    private final int nustrips, nwstrips;
    private final double dsteel, dfoam, dlead, dstrip, dwrap, wstrip;
    private final double umax, wmax, uheight, wheight;
    private final double thtilt, thview, walpha, dist2tgt, yhigh;
    
    private final double[] align_deltaX = new double[6];
    private final double[] align_deltaY = new double[6];
    private final double[] align_deltaZ = new double[6];
    private final double[] align_rotX = new double[6];
    private final double[] align_rotY = new double[6];
    private final double[] align_rotZ = new double[6];


    List<PCALSector> sectorVolumes = new ArrayList<>();

    public PCALGeant4Factory(ConstantProvider cp) {
        motherVolume = new G4World("fc");

        nsectors = cp.getInteger("/geometry/pcal/pcal/nsectors", 0);
        nlayers = cp.getInteger("/geometry/pcal/pcal/nlayers", 0);
        nviews = cp.getInteger("/geometry/pcal/pcal/nviews", 0);
        nsteel = cp.getInteger("/geometry/pcal/pcal/nsteel", 0);
        nfoam = cp.getInteger("/geometry/pcal/pcal/nfoam", 0);

        nustrips = cp.getInteger("/geometry/pcal/Uview/nstrips", 0);
        nwstrips = cp.getInteger("/geometry/pcal/Wview/nstrips", 0);

        dsteel = cp.getDouble("/geometry/pcal/pcal/steel_thick", 0) * Length.mm;
        dfoam = cp.getDouble("/geometry/pcal/pcal/foam_thick", 0) * Length.mm;
        dlead = cp.getDouble("/geometry/pcal/pcal/lead_thick", 0) * Length.mm;
        dwrap = cp.getDouble("/geometry/pcal/pcal/wrapper_thick", 0) * Length.mm;
        dstrip = cp.getDouble("/geometry/pcal/pcal/strip_thick", 0) * Length.mm;
        wstrip = cp.getDouble("/geometry/pcal/pcal/strip_width", 0) * Length.mm;

        dist2tgt = cp.getDouble("/geometry/pcal/pcal/dist2tgt", 0) * Length.mm;
        yhigh = cp.getDouble("/geometry/pcal/pcal/yhigh", 0) * Length.mm;

        umax = cp.getDouble("/geometry/pcal/Uview/max_length", 0) * Length.mm;
        wmax = cp.getDouble("/geometry/pcal/Wview/max_length", 0) * Length.mm;

        thview = Math.toRadians(cp.getDouble("/geometry/pcal/pcal/view_angle", 0));
        thtilt = Math.toRadians(cp.getDouble("/geometry/pcal/pcal/thtilt", 0));

        int alignrows = cp.length("/geometry/pcal/alignment/sector");
        for(int irow = 0; irow< alignrows; irow++) {
            int isector = cp.getInteger("/geometry/pcal/alignment/sector",irow)-1;
            
            align_deltaX[isector] = cp.getDouble("/geometry/pcal/alignment/deltaX",irow);
            align_deltaY[isector] = cp.getDouble("/geometry/pcal/alignment/deltaY",irow);
            align_deltaZ[isector] = cp.getDouble("/geometry/pcal/alignment/deltaZ",irow);
            align_rotX[isector]   = cp.getDouble("/geometry/pcal/alignment/rotX",irow);
            align_rotY[isector]   = cp.getDouble("/geometry/pcal/alignment/rotY",irow);
            align_rotZ[isector]   = cp.getDouble("/geometry/pcal/alignment/rotZ",irow);
        }

        uheight = umax * Math.tan(thview) / 2.0;
        wheight = wmax * Math.sin(2 * thview);
        walpha = Math.atan((0.5 - 2.0 * Math.pow(Math.cos(thview), 2.0)) * wmax / wheight);

        for (int sec = 1; sec <= nsectors; sec++) {
            PCALSector sectorVolume = new PCALSector(sec);
            sectorVolume.setMother(motherVolume);
            sectorVolumes.add(sectorVolume);
        }
    }

    private Layer getULayer(int ilayer, int isector) {
        Layer uLayer = new Layer("U-view-scintillator_" + (ilayer * 3 + 1) + "_s" + isector, dstrip + 2.0 * dwrap);
        uLayer.populateUstrips(ilayer, isector);
        for(int istrip=0; istrip<uLayer.scipaddles.size(); istrip++) {
            G4Trap strip = (G4Trap) uLayer.scipaddles.get(istrip);
            strip.setId(PCALID, isector, (ilayer * 3 + 1), istrip+1);
        }
        return uLayer;
    }

    private Layer getVLayer(int ilayer, int isector) {
        Layer vLayer = new Layer("V-view-scintillator_" + (ilayer * 3 + 2) + "_s" + isector, dstrip + 2.0 * dwrap,
                wheight / 2.0, wmax / 2.0, virtualzero, -walpha);
        vLayer.layerVol.rotate("zyx", thview, Math.toRadians(180), 0);

        Vector3d uTopRight = new Vector3d(umax / 2.0, uheight / 2.0, 0);
        Vector3d shiftVec = uTopRight.minus(vLayer.layerVol.getLocalTransform().transform(
                new Vector3d(-wmax * (Math.pow(Math.cos(thview), 2.0) + 0.25), -wheight / 2.0, 0)));
        vLayer.layerVol.translate(shiftVec);
        vLayer.populateWstrips(ilayer, isector);
        for(int istrip=0; istrip<vLayer.scipaddles.size(); istrip++) {
            G4Trap strip = (G4Trap) vLayer.scipaddles.get(istrip);
            strip.setId(PCALID, isector, (ilayer * 3 + 2), istrip+1);
        }
        return vLayer;
    }

    private Layer getWLayer(int ilayer, int isector) {
        Layer wLayer = new Layer("W-view-scintillator_" + (ilayer * 3 + 3) + "_s" + isector, dstrip + 2.0 * dwrap,
                wheight / 2.0, wmax / 2.0, virtualzero, -walpha);
        wLayer.layerVol.rotate("xyz", 0, 0, thview);

        Vector3d uTopLeft = new Vector3d(-umax / 2.0, uheight / 2.0, 0);
        Vector3d shiftVec = uTopLeft.minus(wLayer.layerVol.getLocalTransform().transform(
                new Vector3d(-wmax * (Math.pow(Math.cos(thview), 2.0) + 0.25), -wheight / 2.0, 0)));
        wLayer.layerVol.translate(shiftVec);
        wLayer.populateWstrips(ilayer, isector);
        for(int istrip=0; istrip<wLayer.scipaddles.size(); istrip++) {
            G4Trap strip = (G4Trap) wLayer.scipaddles.get(istrip);
            strip.setId(PCALID, isector, (ilayer * 3 + 3), istrip+1);
        }
        return wLayer;
    }

    private class Layer {

        final G4Trap layerVol;
        final double thickness;
        List<Geant4Basic> scipaddles = new ArrayList<>();

        protected Layer(String name, double thickness, double pDy, double pDx1, double pDx2, double pAlpha) {
            layerVol = new G4Trap(name, thickness / 2.0, 0, 0,
                    pDy, pDx1 + virtualzero, pDx2 + virtualzero, pAlpha,
                    pDy, pDx1 + virtualzero, pDx2 + virtualzero, pAlpha);
            this.thickness = thickness;
        }

        private Layer(String name, double thickness) {
            //G4Trap dimensions for U layer, same for all lead, stell, foam layers
            //used for creation of all passive layers
            this(name, thickness, uheight / 2.0, 0, umax / 2.0, 0);
        }

        public double shiftZ(double dz) {
            layerVol.translate(0, 0, dz + thickness / 2.0);
            return dz + thickness;
        }

        public void setMother(Geant4Basic motherVol) {
            layerVol.setMother(motherVol);
        }

        public void populateUstrips(int ilayer, int isector) {
            final int ndoubles = 16;
            double w0 = uheight - wstrip * nustrips;
            double hshort = dwrap;

            for (int istrip = 0; istrip <= nustrips; istrip++) {
                double uwidth = (istrip == 0) ? w0 : wstrip;
                double hlong = hshort + uwidth - 2.0 * dwrap;
                double lhalfbtm = hshort / Math.tan(thview);
                double lhalftop = hlong / Math.tan(thview);

                G4Trap stripVol = new PCALstrip(layerVol.getName().charAt(0) + "-view_single_strip_" + (ilayer + 1) + "_" + istrip + "_s" + isector,
                        dstrip / 2.0, 0, 0,
                        uwidth / 2.0 - dwrap, lhalfbtm, lhalftop, 0,
                        uwidth / 2.0 - dwrap, lhalfbtm, lhalftop, 0);
                if (istrip > 0 && istrip <= (nustrips - ndoubles * 2)) {
                    stripVol.makeSensitive();
                    scipaddles.add(stripVol);
                }
                stripVol.setMother(layerVol);
                stripVol.translate(0, (-uheight + hshort + hlong) / 2.0, 0);
                hshort += uwidth;
            }

            hshort = w0 + (nustrips - ndoubles * 2) * wstrip + dwrap;
            for (int idouble = 1; idouble <= ndoubles; idouble++) {
                double hlong = hshort + 2.0 * wstrip - 2.0 * dwrap;
                double lhalfbtm = hshort / Math.tan(thview);
                double lhalftop = hlong / Math.tan(thview);

                G4Trap stripVol = new PCALstrip(layerVol.getName().charAt(0) + "-view_double_strip_" + (ilayer + 1) + "_" + idouble + "_s" + isector,
                        dstrip / 2.0, 0, 0,
                        wstrip - dwrap, lhalfbtm, lhalftop, 0,
                        wstrip - dwrap, lhalfbtm, lhalftop, 0);
                stripVol.makeAbstract();
                stripVol.makeSensitive();
                scipaddles.add(stripVol);
                stripVol.setMother(layerVol);
                stripVol.translate(0, (-uheight + hshort + hlong) / 2.0, 0);
                hshort += 2.0 * wstrip;
            }

        }

        public void populateWstrips(int ilayer, int isector) {
            final int ndoubles = 15;
            double w0 = wheight - wstrip * nwstrips;

            double hshort = w0 + dwrap;
            for (int idouble = 1; idouble <= ndoubles; idouble++) {
                double hlong = hshort + 2.0 * wstrip - 2.0 * dwrap;
                double lbtm = hlong / Math.sin(2.0 * thview);
                double ltop = hshort / Math.sin(2.0 * thview);

                G4Trap stripVol = new PCALstrip(layerVol.getName().charAt(0) + "-view_double_strip_" + (ilayer + 1) + "_" + idouble + "_s" + isector,
                        dstrip / 2.0, 0, 0,
                        wstrip - dwrap, lbtm / 2.0, ltop / 2.0, -walpha,
                        wstrip - dwrap, lbtm / 2.0, ltop / 2.0, -walpha);
                
                stripVol.makeAbstract();
                stripVol.makeSensitive();
                scipaddles.add(stripVol);
                double ystrip = (wheight - hlong - hshort) / 2.0;
                double xstrip = ystrip * Math.tan(-walpha);
                stripVol.translate(xstrip, ystrip, 0);
                stripVol.setMother(layerVol);
                hshort += 2*wstrip;
            }

            hshort = dwrap;
            for (int istrip = 0; istrip <= nwstrips; istrip++) {
                double wwidth = (istrip == 0) ? w0 : wstrip;
                double hlong = hshort + wwidth - 2.0 * dwrap;
                double lbtm = hlong / Math.sin(2.0 * thview);
                double ltop = hshort / Math.sin(2.0 * thview);

                G4Trap stripVol = new PCALstrip(layerVol.getName().charAt(0) + "-view_single_strip_" + (ilayer + 1) + "_" + istrip + "_s" + isector,
                        dstrip / 2.0, 0, 0,
                        wwidth / 2.0 - dwrap, lbtm / 2.0, ltop / 2.0, -walpha,
                        wwidth / 2.0 - dwrap, lbtm / 2.0, ltop / 2.0, -walpha);
                
                if (istrip > ndoubles * 2) {
                    stripVol.makeSensitive();
                    scipaddles.add(stripVol);
                }
                double ystrip = (wheight - hlong - hshort) / 2.0;
                double xstrip = ystrip * Math.tan(-walpha);
                stripVol.translate(xstrip, ystrip, 0);
                stripVol.setMother(layerVol);
                hshort += wwidth;
            }

        }
    }

    private final class PCALSector {

        private final double extrathickness = 0.05;
        //G4Trap dimensions for sector volume (mother volume)
        private final double dsector = nsteel * dsteel + nfoam * dfoam
                + nviews * nlayers * (dstrip + 2.0 * dwrap)
                + (nviews * nlayers - 1) * dlead
                + (2 * nviews * nlayers + nsteel + nfoam) * microgap;
        private final double dist2midplane = dist2tgt
                + (nviews * nlayers * (dstrip + 2.0 * dwrap)
                + (nviews * nlayers - 1) * dlead
                + (2 * nviews * nlayers - 2) * microgap) / 2.0;
        private final double hshift = uheight / 2.0 - yhigh;

        private double layerPos;
        private final int isector;
        private final G4Trap sectorVolume;
        List<Layer> layerVolumes = new ArrayList<>();

        public PCALSector(int sector) {
            sectorVolume = new G4Trap("pcal_s" + sector, dsector / 2.0 + extrathickness, 0, 0,
                    uheight / 2.0 + extrathickness, virtualzero, umax / 2.0 + extrathickness, 0,
                    uheight / 2.0 + extrathickness, virtualzero, umax / 2.0 + extrathickness, 0);

            double secphi = Math.toRadians(90 - (sector - 1) * 60);
            sectorVolume.rotate("yxz", 0, thtilt, secphi);

            // apply shifts from alignment table (ROTATIONS TO BE ADDED!!)
            // alignment table offsets are defined in standard tilted-sector-coordinate frame
            // sector before placement is "sector-2.5", i.e sectorX=-tcsY and sectorY=tcsX
            Vector3d secPos = new Vector3d(0, 0, dist2midplane + align_deltaZ[sector-1])
                    .rotateX(-thtilt)
                    .add(-align_deltaY[sector-1], -(hshift-align_deltaX[sector-1]) * Math.cos(thtilt), (hshift-align_deltaX[sector-1]) * Math.sin(thtilt))
                    .rotateZ(-secphi);
            sectorVolume.translate(secPos);

            this.isector = sector;
            layerPos = -dsector / 2.0 + microgap;

            makeWindow("Front");
            int ilead = 1;
            for (int ilayer = 0; ilayer < nlayers; ilayer++) {
                for (Layer uvwVol : new Layer[]{
                    getULayer(ilayer, sector),
                    getVLayer(ilayer, sector),
                    getWLayer(ilayer, sector)}) {

                    uvwVol.setMother(sectorVolume);
                    layerPos = uvwVol.shiftZ(layerPos) + microgap;
                    layerVolumes.add(uvwVol);

                    if (ilead < 15) {
                        Layer leadVol = new Layer("PCAL_Lead_Layer_" + (ilead++) + "_s" + sector, dlead);
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

    private class PCALstrip extends G4Trap {

        final int iview;

        PCALstrip(String name, double pDz, double pTheta, double pPhi,
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

    public int getNumberOfPaddles(int ilayer) {
        ilayer--;
        PCALSector secVol = sectorVolumes.get(0);
        if (ilayer < 0 || ilayer >= secVol.layerVolumes.size()) {
            System.err.println(String.format("Layer %d  doesn't exist", ilayer + 1));
            throw new IndexOutOfBoundsException();
        }

        Layer lVol = secVol.layerVolumes.get(ilayer);
        return lVol.scipaddles.size();
    }

    public G4Trap getPaddle(int isector, int ilayer, int ipaddle) {
        isector--;
        ilayer--;
        ipaddle--;

        if (isector < 0 || isector >= sectorVolumes.size()) {
            System.err.println(String.format("Sector %d  doesn't exist", isector + 1));
            throw new IndexOutOfBoundsException();
        }

        PCALSector secVol = sectorVolumes.get(isector);
        if (ilayer < 0 || ilayer >= secVol.layerVolumes.size()) {
            System.err.println(String.format("Layer %d  doesn't exist", ilayer + 1));
            throw new IndexOutOfBoundsException();
        }

        Layer lVol = secVol.layerVolumes.get(ilayer);
        if (ipaddle < 0 || ipaddle >= lVol.scipaddles.size()) {
            System.err.println(String.format("Paddle %d  doesn't exist", ipaddle + 1));
            throw new IndexOutOfBoundsException();
        }

        return (G4Trap) lVol.scipaddles.get(ipaddle);
    }

    public Plane3D getFrontalFace(int sector) {
        if (sector < 1 || sector > sectorVolumes.size()) {
            System.err.println(String.format("Sector %d  doesn't exist", sector));
            throw new IndexOutOfBoundsException();
        }
        Layer lVol = sectorVolumes.get(sector - 1).layerVolumes.get(0);
        Geant4Basic layerVol = lVol.layerVol;
        G4Trap padl = (G4Trap) lVol.scipaddles.get(0);
        Vector3d point = new Vector3d(padl.getVertex(0));
        Vector3d normal = new Vector3d(layerVol.getLineZ().diff().normalized());
//        System.out.println("color(\"red\") translate(" + point + ") sphere(2, $fn=100);");
//        System.out.println("line3d(" + point + ", "+point.plus(normal.times(20))+");");
        return new Plane3D(point.x, point.y, point.z, normal.x, normal.y, normal.z);
    }
    
    
        public static void main(String[] args) {
        ConstantProvider cp = GeometryFactory.getConstants(DetectorType.ECAL, 11, "default");
        PCALGeant4Factory factory = new PCALGeant4Factory(cp);
            
        for (int sector = 1; sector <= 6; sector++) {
            System.out.println(factory.getFrontalFace(sector).point().toString() + " " + factory.getPaddle(sector, 1, 1).getGlobalPosition().toString());
        }
    }

}
