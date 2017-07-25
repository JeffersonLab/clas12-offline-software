/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.volume;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.jlab.geometry.prim.StlPrim;

/**
 *
 * @author kenjo
 */
public class G4Stl extends Geant4Basic {

    public G4Stl(String name, String filename, double scaleFactor) throws FileNotFoundException {
        this(name, new FileInputStream(new File(filename)), scaleFactor);
    }

    public G4Stl(String name, String filename) throws FileNotFoundException {
        this(name, new FileInputStream(new File(filename)), 1);
    }

    public G4Stl(String name, InputStream stlstream, double scaleFactor) {
        super(new StlPrim(stlstream, scaleFactor));
        setName(name);
        setType("Stl");
    }

    public G4Stl(String name, InputStream stlstream) {
        this(name, stlstream, 1);
    }
}
