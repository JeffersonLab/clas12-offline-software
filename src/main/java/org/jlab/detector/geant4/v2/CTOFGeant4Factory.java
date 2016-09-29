/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.STL;
import java.io.IOException;
import java.io.InputStream;
import org.jlab.detector.volume.G4World;

/**
 *
 * @author kenjo
 */
public final class CTOFGeant4Factory extends Geant4Factory {

    public CTOFGeant4Factory() throws IOException {
        motherVolume = new G4World("fc");

//        System.out.println(getClass().getClassLoader().getResourceAsStream("ctof/cad/test.txt"));
        InputStream configStream = getClass().getClassLoader().getResourceAsStream("ctof/cad/test.txt");
        System.out.println(configStream);

//String filepath = getClass().getClassLoader().getResourceAsStream("ctof/cad/sc48.stl");
//        CSG detector = STL.file(java.nio.file.Paths.get(filepath));
//        detector.toStlFile("/home/kenjo/1.stl");
        IOUtils.toString(configStream);
        //IOUtils.
    }

    public static void main(String[] args) throws IOException {
        CTOFGeant4Factory ctof = new CTOFGeant4Factory();
    }
}
