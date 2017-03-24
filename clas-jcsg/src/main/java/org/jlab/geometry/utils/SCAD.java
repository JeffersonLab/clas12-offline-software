/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geometry.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.paint.Color;
import org.jlab.detector.geant4.v2.Geant4Factory;
import org.jlab.detector.volume.Geant4Basic;

/**
 *
 * @author kenjo
 */
public class SCAD {

    private static final Map<String, Color> CLAS12COLORS;

    static {
        CLAS12COLORS = new HashMap<>();
        CLAS12COLORS.put("PCAL_Lead_Layer", Color.web("66ff33"));
        CLAS12COLORS.put("U-view_single_strip", Color.web("ff6633"));
        CLAS12COLORS.put("V-view_single_strip", Color.web("6600ff"));
        CLAS12COLORS.put("W-view_single_strip", Color.web("6600ff"));
        CLAS12COLORS.put("Stainless_Steel", Color.web("D4E3EE"));
        CLAS12COLORS.put("Last-a-Foam", Color.web("EED18C"));
        CLAS12COLORS.put("eclid1_s", Color.web("FCFFF0"));
        CLAS12COLORS.put("eclid2_s", Color.web("EED18C"));
        CLAS12COLORS.put("eclid3_s", Color.web("FCFFF0"));
        CLAS12COLORS.put("lead", Color.web("7CFC00"));
        CLAS12COLORS.put("U_strip", Color.web("ff6633"));
        CLAS12COLORS.put("V_strip", Color.web("6600ff"));
        CLAS12COLORS.put("W_strip", Color.web("6600ff"));
        CLAS12COLORS.put("panel1a_sector", Color.web("ff11aa"));
        CLAS12COLORS.put("panel1b_sector", Color.web("11ffaa"));
        CLAS12COLORS.put("panel2_sector", Color.web("ff11aa"));
        CLAS12COLORS.put("sc", Color.web("00ffff"));
        CLAS12COLORS.put("lg", Color.web("32CD32"));
    }

    public static String toSCAD(Geant4Factory factory, String dirname) throws IOException {
        StringBuilder stlout = new StringBuilder();
        File detdir = new File(dirname);
        detdir.mkdirs();
        for (Geant4Basic component : factory.getComponents()) {
            if(component.isAbstract()) continue;
            
            String compname = component.getName();
            String stlpath = detdir.getAbsolutePath() + "/" + compname + ".stl";
            component.toCSG().toStlFile(stlpath);

            Color col = Color.AZURE;
            for (Map.Entry<String, Color> entry : CLAS12COLORS.entrySet()) {
                if (compname.contains(entry.getKey())) {
                    col = entry.getValue();
                    break;
                }
            }

            stlout.append(String.format("color([%.4f, %.4f, %.4f]) import(\"%s\");\n",
                    col.getRed(), col.getGreen(), col.getBlue(), stlpath));

        }
        return stlout.toString();
    }

    public static void toSCAD(Geant4Factory factory, String dirname, String scadname) throws IOException {
        try (PrintWriter scadout = new PrintWriter(new FileOutputStream(scadname, false))) {
            scadout.println(toSCAD(factory, dirname));
        }
    }

    public static void toSCAD(List<Geant4Factory> factories, String dirname, String scadname) throws IOException {
        try (PrintWriter scadout = new PrintWriter(new FileOutputStream(scadname, false))) {
            for (Geant4Factory factory : factories) {
                scadout.println(toSCAD(factory, dirname));
            }
        }
    }
}
