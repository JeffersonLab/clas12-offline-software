/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2;

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

    private final class ULayer extends G4Trap {
        private final static double H=10, DU=1, UMAX=10;
        
        public ULayer(String name, double thickness) {
            super(name, DU, 0, 0, H, virtualzero, UMAX, 0, H, virtualzero, UMAX, 0);
        }
    }

    private final double microgap = 0.1;
    private final double extrathickness = 0.5;
    private final double virtualzero = 0.00000001;
    private double layerZpos;
    
    private final int nsectors, nviews, nlayers, nsteel, nfoam;
    private final double dsteel;

    public PCALGeant4Factory(ConstantProvider cp) {
        motherVolume = new G4World("fc");

        nsectors = cp.getInteger("/geometry/pcal/pcal/nsectors", 0);
        nlayers = cp.getInteger("/geometry/pcal/pcal/nlayers", 0);
        nviews = cp.getInteger("/geometry/pcal/pcal/nviews", 0);
        nsteel = cp.getInteger("/geometry/pcal/pcal/nsteel", 0);
        nfoam = cp.getInteger("/geometry/pcal/pcal/nfoam", 0);

        for (int isec = 0; isec < nsectors; isec++) {
            Geant4Basic sectorVolume = createSector(cp, isec);
        }
    }

    public Geant4Basic createSector(ConstantProvider cp, int isec) {

        double dsteel = cp.getDouble("/geometry/pcal/pcal/steel_thick", 0);
        double dfoam = cp.getDouble("/geometry/pcal/pcal/foam_thick", 0);
        double dlead = cp.getDouble("/geometry/pcal/pcal/lead_thick", 0);
        double dstrip = cp.getDouble("/geometry/pcal/pcal/strip_thick", 0);

        double umax = cp.getDouble("/geometry/pcal/Uview/max_length", 0);
        double thview = cp.getDouble("/geometry/pcal/pcal/view_angle", 0);

        double dsector = nsteel * dsteel + nfoam * dfoam;
        dsector += nviews * nlayers * dstrip;
        dsector += (nviews * nlayers - 1) * dlead;
        dsector += (2 * nviews * nlayers + nsteel + nfoam) * microgap;
        double hsector = umax * Math.tan(Math.toRadians(thview)) / 2.0;

        //layer_params - G4Trap dimensions for U layer, same for all lead, stell, foam layers
        //used for creation of all passive layers
        double layer_params[] = {0.0, 0, 0,
            hsector / 2, virtualzero, umax / 2, 0,
            hsector / 2, virtualzero, umax / 2, 0};

        //params - G4Trap dimensions for sector volume (mother volume)
        double params[] = {dsector / 2.0, 0, 0,
            hsector / 2 + extrathickness, virtualzero, umax / 2, 0,
            hsector / 2 + extrathickness, virtualzero, umax / 2, 0};
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

    /*

    public Geant4Basic createLayer(String volname, double params[], double thickness, Geant4Basic motherVol) {
        Geant4Basic sectorVolume = new Geant4Basic("pcal_s" + (isec + 1), "G4Trap", params);
        sectorVolume.setParUnits("mm", "deg", "deg", "mm", "mm", "mm", "deg", "mm", "mm", "mm", "deg");
        sectorVolume.setPosition(0, 0, isec * 20);
        layerVolume.setMother(motherVol);
    }

    public Geant4Basic createPanel(ConstantProvider cp, int sector, int layer) {
        //Geant4Basic  mother = new Geant4Basic();

        double motherGap = 4.0;

        double thtilt = Math.toRadians(cp.getDouble(stringLayers[layer - 1] + "/panel/thtilt", 0));
        double thmin = Math.toRadians(cp.getDouble(stringLayers[layer - 1] + "/panel/thmin", 0));
        double dist2edge = cp.getDouble(stringLayers[layer - 1] + "/panel/dist2edge", 0);

        List<Geant4Basic> paddles = this.createLayer(cp, layer);

        double panel_width = (paddles.get(paddles.size() - 1).getPosition()[2] - paddles.get(0).getPosition()[2])
                + 2 * paddles.get(0).getParameters()[2] + 2 * motherGap;

        double panel_mother_dx1 = paddles.get(0).getParameters()[0] + motherGap;
        double panel_mother_dx2 = paddles.get(paddles.size() - 1).getParameters()[0]
                + (paddles.get(paddles.size() - 1).getParameters()[0] - paddles.get(paddles.size() - 2).getParameters()[0])
                + motherGap;

        double panel_mother_dy = paddles.get(0).getParameters()[1] + motherGap;
        double panel_mother_dz = panel_width / 2.0;

        double[] params = new double[5];
        params[0] = panel_mother_dx1;
        params[1] = panel_mother_dx2;
        params[2] = panel_mother_dy;
        params[3] = panel_mother_dy;
        params[4] = panel_mother_dz;

        Geant4Basic panelVolume = new Geant4Basic("ftof_p" + gemcLayerNames[layer - 1] + "_s" + sector, "Trd", params);
        panelVolume.setId(sector, layer, 0);

        double panel_pos_xy = dist2edge * Math.sin(thmin) + panel_width / 2 * Math.cos(thtilt);
        double panel_pos_x = panel_pos_xy * Math.cos(Math.toRadians(sector * 60 - 60));
        double panel_pos_y = panel_pos_xy * Math.sin(Math.toRadians(sector * 60 - 60));
        double panel_pos_z = dist2edge * Math.cos(thmin) - panel_width / 2 * Math.sin(thtilt);

        panelVolume.setPosition(panel_pos_x, panel_pos_y, panel_pos_z);

        //panelVolume.setRotation("xzy", thtilt/3, Math.toRadians(-30.0 - 1 * 60.0), 0.0);
        panelVolume.setRotation("xyz", Math.toRadians(-90) - thtilt, 0.0, Math.toRadians(-30.0 - sector * 60.0));
        //panelVolume.setRotation("zxy", Math.toRadians(-30.0 - sector * 60.0), Math.toRadians(-90) - thtilt, 0.0);
        for (int ipaddle = 0; ipaddle < paddles.size(); ipaddle++) {
            paddles.get(ipaddle).setName("panel" + gemcLayerNames[layer - 1] + "_sector" + sector + "_paddle_" + (ipaddle + 1));
            paddles.get(ipaddle).setId(sector, layer, ipaddle + 1);

            paddles.get(ipaddle).setMother(panelVolume);
        }
        return panelVolume;
    }

    public List<Geant4Basic> createLayer(ConstantProvider cp, int layer) {

        int numPaddles = cp.length(stringLayers[layer - 1] + "/paddles/paddle");
        double paddlewidth = cp.getDouble(stringLayers[layer - 1] + "/panel/paddlewidth", 0);
        double paddlethickness = cp.getDouble(stringLayers[layer - 1] + "/panel/paddlethickness", 0);
        double gap = cp.getDouble(stringLayers[layer - 1] + "/panel/gap", 0);
        double wrapperthickness = cp.getDouble(stringLayers[layer - 1] + "/panel/wrapperthickness", 0);
        double thtilt = Math.toRadians(cp.getDouble(stringLayers[layer - 1] + "/panel/thtilt", 0));
        double thmin = Math.toRadians(cp.getDouble(stringLayers[layer - 1] + "/panel/thmin", 0));

        String paddleLengthStr = stringLayers[layer - 1] + "/paddles/Length";

        //List<Geant4Basic>  mother = new ArrayList<Geant4Basic>();
        List<Geant4Basic> paddleVolumes = new ArrayList<>();

        for (int ipaddle = 0; ipaddle < numPaddles; ipaddle++) {
            double paddlelength = cp.getDouble(paddleLengthStr, ipaddle);
            String vname = String.format("sci_S%d_L%d_C%d", 0, layer, ipaddle + 1);
            Geant4Basic volume = new Geant4Basic(vname, "Box",
                    paddlelength / 2., paddlethickness / 2., paddlewidth / 2.0);

            double zoffset = (ipaddle - numPaddles / 2. + 0.5) * (paddlewidth + gap + 2 * wrapperthickness);
            volume.setPosition(0.0, 0.0, zoffset);
            volume.setRotation("xyz", 0, 0, 0);
            paddleVolumes.add(volume);
        }
        return paddleVolumes;
    }
     */
}
