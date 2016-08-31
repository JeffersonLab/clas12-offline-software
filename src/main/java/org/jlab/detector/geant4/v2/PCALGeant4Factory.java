/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2;

import eu.mihosoft.vrl.v3d.Vector3d;
import org.jlab.detector.volume.G4Box;
import org.jlab.detector.volume.G4Trap;
import org.jlab.detector.volume.G4World;
import org.jlab.detector.volume.Geant4Basic;
import org.jlab.geom.base.ConstantProvider;

/**
 *
 * @author kenjo
 */
public final class PCALGeant4Factory extends Geant4Factory {

    private class Layer extends G4Trap {
        private final double thickness;
        
        public Layer(String name, double thickness) {
            super(name, thickness/2.0, 0, 0, uheight/2.0, virtualzero, umax/2.0, 0, uheight/2.0, virtualzero, umax/2.0, 0);
            this.thickness = thickness;
        }
        
        public double shiftZ(double dz){
            translate(0,0,dz+thickness/2.0);
            return dz+thickness;
        }
    }

    private final class ULayer extends Layer {

        public ULayer(String name, double thickness) {
            super(name, thickness);
        }
    }

    private enum 
    private class PCALSector {

        private final G4Trap sectorVolume;
        private final double hsector = uheight + 2.0 * extrathickness;
        private final double dsector = nsteel * dsteel + nfoam * dfoam
                + nviews * nlayers * dstrip
                + (nviews * nlayers - 1) * dlead
                + (2 * nviews * nlayers + nsteel + nfoam) * microgap;

        public PCALSector(int isector) {
            sectorVolume = new G4Trap("pcal_s"+isector, dsector / 2.0, 0, 0, hsector / 2.0, virtualzero, umax / 2.0, 0, hsector / 2.0, virtualzero, umax / 2.0, 0);
            
            double layerPos = -dsector / 2.0 + microgap;
            
            Layer steelVol1 = new Layer("Stainless_Steel_Front_1_s"+isector, dsteel);
            steelVol1.setMother(sectorVolume);
            layerPos = steelVol1.shiftZ(layerPos) + microgap;
            
            Layer foamVol1 = new Layer("Last-a-Foam_Front_s"+isector, dfoam);
            foamVol1.setMother(sectorVolume);
            layerPos = foamVol1.shiftZ(layerPos) + microgap;
            
            Layer steelVol2 = new Layer("Stainless_Steel_Front_2_s"+isector, dsteel);
            steelVol2.setMother(sectorVolume);
            layerPos = steelVol2.shiftZ(layerPos) + microgap;
            

        }

        public void setMother(Geant4Basic motherVolume) {
            sectorVolume.setMother(motherVolume);
        }
    }

    private final double microgap = 0.1;
    private final double extrathickness = 0.5;
    private final double virtualzero = 0.00000001;
    private double layerZpos;

    private final int nsectors, nviews, nlayers, nsteel, nfoam;
    private final double dsteel, dfoam, dlead, dstrip, umax, thview, uheight;

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
        thview = cp.getDouble("/geometry/pcal/pcal/view_angle", 0);
        uheight = umax * Math.tan(Math.toRadians(thview)) / 2.0;

        for (int isec = 0; isec < nsectors; isec++) {
            Geant4Basic sectorVolume = createSector(cp, isec);
        }
    }

    public Geant4Basic createSector(ConstantProvider cp, int isec) {

        //layer_params - G4Trap dimensions for U layer, same for all lead, stell, foam layers
        //used for creation of all passive layers
        double layer_params[] = {0.0, 0, 0,
            hsector / 2, virtualzero, umax / 2, 0,
            hsector / 2, virtualzero, umax / 2, 0};

        //params - G4Trap dimensions for sector volume (mother volume)
        Geant4Basic sectorVolume = new G4Box("pcal_s" + (isec + 1), 1, 1, 1);
        sectorVolume.setMother(motherVolume);

        layerZpos = microgap;

//        createLayer("", layer_params, dsteel, sectorVolume);

        /*
        for (int ilayer = 0; ilayer < nlayers; ilayer++) {
            for (int iview = 0; iview < nviews; iview++) {
                Geant4Basic layerVolume = createLayer(cp, isec, ilayer, iview);
                layerVolume.setMother(sectorVolume);
            }
        }

         */
        return new G4Box("asdf", 1, 1, 1);
    }

}
