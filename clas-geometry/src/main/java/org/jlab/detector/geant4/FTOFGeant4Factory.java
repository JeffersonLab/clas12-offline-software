/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.geant.Geant4Basic;

/**
 *
 * @author gavalian, kenjo
 */
public final class FTOFGeant4Factory {

    private final Geant4Basic motherVolume = new Geant4Basic("fc", "Box", 0);

    private final HashMap<String, String> properties = new HashMap<>();

    private final String[] stringLayers = new String[]{
        "/geometry/ftof/panel1a",
        "/geometry/ftof/panel1b",
        "/geometry/ftof/panel2"};

    private final String[] gemcLayerNames = new String[]{
        "1a", "1b", "2"
    };

    public FTOFGeant4Factory() {

    }

    public FTOFGeant4Factory(ConstantProvider provider) {
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
        //panelVolume.setRotation("xyz", Math.toRadians(-90) - thtilt, 0.0, Math.toRadians(-30.0 - sector * 60.0));
        panelVolume.setRotation("zxy", Math.toRadians(-30.0 - sector * 60.0), Math.toRadians(-90) - thtilt, 0.0);
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

    public Geant4Basic getMother() {
        return motherVolume;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        for(Geant4Basic layerVolume : motherVolume.getChildren()) {
            str.append(layerVolume.gemcString());
            str.append(System.getProperty("line.separator"));

            for(Geant4Basic paddleVolume : layerVolume.getChildren()) {
                str.append(paddleVolume.gemcString());
                str.append(System.getProperty("line.separator"));
            }
        }

        return str.toString();
    }

    public String getProperty(String name) {
        return properties.containsKey(name) ? properties.get(name) : "none";
    }
}
