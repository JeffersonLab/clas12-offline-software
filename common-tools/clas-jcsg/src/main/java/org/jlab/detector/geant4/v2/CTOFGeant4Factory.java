/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2;

import eu.mihosoft.vrl.v3d.Vector3d;
import java.io.InputStream;
import static org.jlab.detector.hits.DetId.CTOFID;
import org.jlab.detector.units.SystemOfUnits.Length;
import org.jlab.detector.volume.G4Stl;
import org.jlab.detector.volume.G4World;
import org.jlab.detector.volume.Geant4Basic;
import org.jlab.geometry.prim.Line3d;

/**
 *
 * @author kenjo
 */
public final class CTOFGeant4Factory extends Geant4Factory {

    private final int npaddles = 48;

    public CTOFGeant4Factory() {
        motherVolume = new G4World("fc");

        ClassLoader cloader = getClass().getClassLoader();

        for (String name : new String[]{"sc", "lgd"}) {
            for (int iscint = 1; iscint <= npaddles; iscint++) {
                CTOFpaddle component = new CTOFpaddle(String.format("%s%02d", name, iscint),
                        cloader.getResourceAsStream(String.format("ctof/cad/%s%02d.stl", name, iscint)), iscint);
                component.scale(Length.mm/Length.cm);

                component.rotate("zyx", 0, Math.toRadians(180), 0);
                component.translate(0, 0, 127.327);
                component.setMother(motherVolume);

                if (name.equals("sc")) {
                    component.makeSensitive();
                    component.setId(CTOFID, iscint);
                }
            }
        }
    }

    public Geant4Basic getPaddle(int ipaddle) {
        if (ipaddle < 1 || ipaddle > npaddles) {
            System.err.println("ERROR!!!");
            System.err.println("CTOF Paddle #" + ipaddle + " doesn't exist");
            System.exit(111);
        }
        return motherVolume.getChildren().get(ipaddle - 1);
    }

    private class CTOFpaddle extends G4Stl {

        private final int padnum;
        private Line3d centerline;
        private final double angle0 = -3.75, dangle = 7.5;
        private final double zmin = -54.18, zmax = 36.26;

        CTOFpaddle(String name, InputStream stlstream, int padnum) {
            super(name, stlstream);
            this.padnum = padnum;
        }

        @Override
        public Line3d getLineZ() {
            Vector3d cent = new Vector3d(0, -26.62, 0);
            cent.rotateZ(Math.toRadians(angle0 + (padnum - 1) * dangle));
            return new Line3d(new Vector3d(cent.x, cent.y, zmin), new Vector3d(cent.x, cent.y, zmax));
        }
    }

    public static void main(String[] args) {
        CTOFGeant4Factory factory = new CTOFGeant4Factory();

        for (int ipad = 15; ipad <= 19; ipad++) {
            Geant4Basic pad = factory.getPaddle(ipad);
            Line3d line = pad.getLineZ();
            System.out.println(line);
        }
    }
}
