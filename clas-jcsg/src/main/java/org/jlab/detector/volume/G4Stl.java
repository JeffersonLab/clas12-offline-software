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

    public G4Stl(String name, String filename) throws FileNotFoundException {
        this(name, new FileInputStream(new File(filename)));
    }

    public G4Stl(String name, InputStream stlstream) {
        super(new StlPrim(stlstream));
        setName(name);
        setType("Stl");
    }
}
