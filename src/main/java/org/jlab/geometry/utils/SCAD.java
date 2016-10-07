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
import org.jlab.detector.geant4.v2.Geant4Factory;
import org.jlab.detector.volume.Geant4Basic;

/**
 *
 * @author kenjo
 */
public class SCAD {

    private static final Map<String, int[]> CLAS12COLORS;

    static {
        CLAS12COLORS = new HashMap<>();
        CLAS12COLORS.put("PCAL_Lead_Layer", new int[]{0x66, 0xff, 0x33});
        CLAS12COLORS.put("U-view_single_strip", new int[]{0xff, 0x66, 0x33});
        CLAS12COLORS.put("V-view_single_strip", new int[]{0x66, 0x00, 0xff});
        CLAS12COLORS.put("W-view_single_strip", new int[]{0x66, 0x00, 0xff});
        CLAS12COLORS.put("Stainless_Steel", new int[]{0xD4, 0xE3, 0xEE});
        CLAS12COLORS.put("Last-a-Foam", new int[]{0xEE, 0xD1, 0x8C});
        CLAS12COLORS.put("eclid1_s", new int[]{0xFC, 0xFF, 0xF0});
        CLAS12COLORS.put("eclid2_s", new int[]{0xEE, 0xD1, 0x8C});
        CLAS12COLORS.put("eclid3_s", new int[]{0xFC, 0xFF, 0xF0});
        CLAS12COLORS.put("lead", new int[]{0x7C, 0xFC, 0x00});
        CLAS12COLORS.put("U_strip", new int[]{0xff, 0x66, 0x33});
        CLAS12COLORS.put("V_strip", new int[]{0x66, 0x00, 0xff});
        CLAS12COLORS.put("W_strip", new int[]{0x66, 0x00, 0xff});
        CLAS12COLORS.put("panel1a_sector", new int[]{0xff, 0x11, 0xaa});
        CLAS12COLORS.put("panel1b_sector", new int[]{0x11, 0xff, 0xaa});
        CLAS12COLORS.put("panel2_sector", new int[]{0xff, 0x11, 0xaa});
        CLAS12COLORS.put("sc", new int[]{0x00, 0xff, 0xff});
        CLAS12COLORS.put("lg", new int[]{0x32, 0xCD, 0x32});
    }

    public static String toSCAD(Geant4Factory factory, String dirname) throws IOException {
        StringBuilder stlout = new StringBuilder();
        File detdir = new File(dirname);
        detdir.mkdirs();
        for (Geant4Basic component : factory.getComponents()) {
            String compname = component.getName();
            String stlpath = detdir.getAbsolutePath() + "/" + compname + ".stl";
            component.toCSG().toStlFile(stlpath);

            for (Map.Entry<String, int[]> entry : CLAS12COLORS.entrySet()) {
                if (compname.contains(entry.getKey())) {
                    stlout.append(String.format("color([%d/255, %d/255, %d/255]) import(\"%s\");\n",
                            entry.getValue()[0], entry.getValue()[1], entry.getValue()[2], stlpath));
                    break;
                }
            }
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
