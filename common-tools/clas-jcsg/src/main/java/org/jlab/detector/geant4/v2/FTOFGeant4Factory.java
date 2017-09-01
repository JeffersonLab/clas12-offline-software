/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2;

import eu.mihosoft.vrl.v3d.Vector3d;
import org.jlab.detector.volume.Geant4Basic;
import org.jlab.detector.volume.G4Trd;
import org.jlab.detector.volume.G4Box;
import java.util.ArrayList;
import java.util.List;
import static org.jlab.detector.hits.DetId.FTOFID;
import static org.jlab.detector.units.SystemOfUnits.Length;
import org.jlab.detector.volume.G4World;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geometry.prim.Line3d;

/**
 *
 * @author gavalian, kenjo
 */
public final class FTOFGeant4Factory extends Geant4Factory {

    private final double motherGap = 4.0 * Length.cm;
    private final double pbthickness = 0.005 * Length.in;
    private final double microgap = 0.001;

    private final String[] stringLayers = new String[]{
        "/geometry/ftof/panel1a",
        "/geometry/ftof/panel1b",
        "/geometry/ftof/panel2"};

    private final String[] gemcLayerNames = new String[]{
        "1a", "1b", "2"
    };

    public FTOFGeant4Factory(ConstantProvider provider) {
        motherVolume = new G4World("fc");

        for (int sector = 1; sector <= 6; sector++) {
            for (int layer = 1; layer <= 3; layer++) {
                Geant4Basic layerVolume = createPanel(provider, sector, layer);
                layerVolume.setMother(motherVolume);
            }
        }
        properties.put("email", "carman@jlab.org, jguerra@jlab.org");
        properties.put("author", "carman, guerra");
        properties.put("date", "06/03/13");
    }

    private Geant4Basic createPanel(ConstantProvider cp, int sector, int layer) {
        double thtilt = Math.toRadians(cp.getDouble(stringLayers[layer - 1] + "/panel/thtilt", 0));
        double thmin = Math.toRadians(cp.getDouble(stringLayers[layer - 1] + "/panel/thmin", 0));
        double dist2edge = cp.getDouble(stringLayers[layer - 1] + "/panel/dist2edge", 0) * Length.cm;

        List<G4Box> paddles = this.createLayer(cp, layer);

        double panel_mother_dx1 = paddles.get(0).getXHalfLength();
        double panel_mother_dx2 = paddles.get(paddles.size() - 1).getXHalfLength()
                + (paddles.get(paddles.size() - 1).getXHalfLength() - paddles.get(paddles.size() - 2).getXHalfLength());

        double panel_mother_dy = paddles.get(0).getYHalfLength();
        double panel_width = (paddles.get(paddles.size() - 1).getLocalPosition().z - paddles.get(0).getLocalPosition().z)
                + 2 * paddles.get(0).getZHalfLength();
        double panel_mother_dz = panel_width / 2.0;

        G4Trd panelVolume = new G4Trd("ftof_p" + gemcLayerNames[layer - 1] + "_s" + sector,
                panel_mother_dx1 + motherGap, panel_mother_dx2 + motherGap,
                panel_mother_dy + motherGap, panel_mother_dy + motherGap,
                panel_mother_dz + motherGap);
        panelVolume.setId(FTOFID, sector, layer, 0);

        double panel_pos_xy = dist2edge * Math.sin(thmin) + (panel_width + 2.0 * motherGap) / 2 * Math.cos(thtilt);
        double panel_pos_x = panel_pos_xy * Math.cos(Math.toRadians(sector * 60 - 60));
        double panel_pos_y = panel_pos_xy * Math.sin(Math.toRadians(sector * 60 - 60));
        double panel_pos_z = dist2edge * Math.cos(thmin) - (panel_width + 2.0 * motherGap) / 2 * Math.sin(thtilt);

        panelVolume.rotate("xyz", Math.toRadians(-90) - thtilt, 0.0, Math.toRadians(-30.0 - sector * 60.0));
        panelVolume.translate(panel_pos_x, panel_pos_y, panel_pos_z);

        for (int ipaddle = 0; ipaddle < paddles.size(); ipaddle++) {
            paddles.get(ipaddle).setName("panel" + gemcLayerNames[layer - 1] + "_sector" + sector + "_paddle_" + (ipaddle + 1));
            paddles.get(ipaddle).setId(FTOFID, sector, layer, ipaddle + 1);
            paddles.get(ipaddle).setMother(panelVolume);
        }

        if (layer == 2) {
            G4Trd pbShield = new G4Trd("ftof_shield_sector" + sector,
                    panel_mother_dx1, panel_mother_dx2, pbthickness / 2.0, pbthickness / 2.0, panel_mother_dz);
            pbShield.translate(0.0, panel_mother_dy + microgap + pbthickness / 2.0, 0.0);
            pbShield.setMother(panelVolume);
        }

        return panelVolume;
    }

    private List<G4Box> createLayer(ConstantProvider cp, int layer) {

        int numPaddles = cp.length(stringLayers[layer - 1] + "/paddles/paddle");
        double paddlewidth = cp.getDouble(stringLayers[layer - 1] + "/panel/paddlewidth", 0);
        double paddlethickness = cp.getDouble(stringLayers[layer - 1] + "/panel/paddlethickness", 0);
        double gap = cp.getDouble(stringLayers[layer - 1] + "/panel/gap", 0);
        double wrapperthickness = cp.getDouble(stringLayers[layer - 1] + "/panel/wrapperthickness", 0);
        double thtilt = Math.toRadians(cp.getDouble(stringLayers[layer - 1] + "/panel/thtilt", 0));
        double thmin = Math.toRadians(cp.getDouble(stringLayers[layer - 1] + "/panel/thmin", 0));

        String paddleLengthStr = stringLayers[layer - 1] + "/paddles/Length";

        List<G4Box> paddleVolumes = new ArrayList<>();

        for (int ipaddle = 0; ipaddle < numPaddles; ipaddle++) {
            double paddlelength = cp.getDouble(paddleLengthStr, ipaddle);
            String vname = String.format("sci_S%d_L%d_C%d", 0, layer, ipaddle + 1);
            G4Box volume = new G4Box(vname, paddlelength / 2. * Length.cm, paddlethickness / 2. * Length.cm, paddlewidth / 2.0 * Length.cm);
            volume.makeSensitive();

            double zoffset = (ipaddle - numPaddles / 2. + 0.5) * (paddlewidth + gap + 2 * wrapperthickness);
            volume.translate(0.0, 0.0, zoffset * Length.cm);

            paddleVolumes.add(volume);
        }
        return paddleVolumes;
    }

    public G4Box getComponent(int sector, int layer, int paddle) {
        int ivolume = (sector - 1) * 3 + layer - 1;

        if (sector >= 1 && sector <= 6
                && layer >= 1 && layer <= 3) {

            List<Geant4Basic> panel = motherVolume.getChildren().get(ivolume).getChildren();
            int npaddles = panel.size();

            if (paddle >= 1 && paddle <= npaddles) {
                return (G4Box) panel.get(paddle - 1);
            }
        }

        System.err.println("ERROR!!!");
        System.err.println("Component: sector: " + sector + ", layer: " + layer + ", paddle: " + paddle + " doesn't exist");
        throw new IndexOutOfBoundsException();
    }

    public Plane3D getFrontalFace(int sector, int layer) {
        if (sector < 1 || sector > 6
                || layer < 1 || layer > 3) {
            System.err.println("ERROR!!!");
            System.err.println("Component: sector: " + sector + ", layer: " + layer + " doesn't exist");
            throw new IndexOutOfBoundsException();
        }

        int ivolume = (sector - 1) * 3 + layer - 1;

        Geant4Basic panel = motherVolume.getChildren().get(ivolume);
        G4Box padl = (G4Box) panel.getChildren().get(1);
        Vector3d point = new Vector3d(padl.getVertex(0));
        Vector3d normal = new Vector3d(panel.getLineY().diff().normalized());

        return new Plane3D(point.x, point.y, point.z, normal.x, normal.y, normal.z);
    }

    public G4World getMother() {
        return motherVolume;
    }
}
