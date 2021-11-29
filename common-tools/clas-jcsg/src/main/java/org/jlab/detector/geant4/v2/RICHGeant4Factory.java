//*
To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package org.jlab.detector.geant4.v2;

import eu.mihosoft.vrl.v3d.Vector3d;

import java.util.ArrayList;
import java.util.List;

import org.jlab.detector.units.SystemOfUnits.Length;
import org.jlab.detector.volume.G4Stl;
import org.jlab.detector.volume.G4World;
import org.jlab.geom.prim.Point3D;


import org.jlab.detector.volume.G4Box;

/**
 * Building the RICH PMTs
 * @author Goodwill, kenjo
 */
public final class RICHGeant4Factory extends Geant4Factory {
    
    //to be  stored in database, from hashmap of perl script for gemc, all dimensions in mm
    private final int PMT_rows = 23, sector = 4;
    
    private final double RICHpanel_y = 1706.145 * Length.mm,
    RICHpanel_z = 5705.13 * Length.mm,
    RICHpanel_y_offset = 284.31 * Length.mm;
    
    private final double RICH_thtilt = 25;
    
    private final double MAPMT_dx = 26 * Length.mm,
    MAPMT_dy = 26 * Length.mm, //dimensions of PMT
    MAPMT_dz = 13.5 * Length.mm;
    
    private final double MAPMTgap = 1 * Length.mm,
    MAPMTFirstRow_y = -1931.23 * Length.mm, //Position of PMT in box
    MAPMTFirstRow_z = 474.10 * Length.mm;
    
    private final double MAPMTWall_thickness = 1.0 * Length.mm;
    private final double MAPMTWindow_thickness = 1.5 * Length.mm;
    private final double MAPMTPhotocathode_side = 49 * Length.mm,
    MAPMTPhotocathode_thickness = 0.1 * Length.mm;
    private final double MAPMTSocket_thickness = 2.7 * Length.mm;
    
    private final double offset = 0.5 * Length.mm;
    
    // list containing the pmts
    private List<G4Box> pmts = new ArrayList<G4Box>();
    private List<G4Box> photocatodes = new ArrayList<G4Box>();
    private List<G4Stl> stlvolumes = new ArrayList<G4Stl>();
    
    // Optical properties
    private double n_w=1.3;
    private double n_aerogel=1.5;
    
    
    public RICHGeant4Factory() {
        motherVolume = new G4World("fc");
        ClassLoader cloader = getClass().getClassLoader();
        G4Stl gasVolume = new G4Stl("RICH_s4", cloader.getResourceAsStream("rich/cad/RICH_s4.stl"), Length.mm / Length.cm);
        gasVolume.setMother(motherVolume);
        stlvolumes.add(gasVolume);
        
        ArrayList<String> stl_names = StlNames();
        for( int i =0; i <stl_names.size(); i++ ) {
            String name = stl_names.get(i);
            G4Stl component = new G4Stl(String.format("%s", name),
                                        cloader.getResourceAsStream(String.format("rich/cad/%s.stl", name)),
                                        Length.mm / Length.cm);
            component.setMother(gasVolume);
            stlvolumes.add(component);
            
        }
        
        //place the PMTs in the trapezoidal box
        for (int irow = 0; irow < PMT_rows; irow++) {
            //define number of PMTs in this row
            int nPMTInARow = 6 + irow;
            
            //define the y position of the row
            double MAPMT_ylocal = MAPMTFirstRow_y + (MAPMTgap + 2.0 * MAPMT_dy) * irow;
            
            for (int ipmt = 0; ipmt < nPMTInARow; ipmt++) {
                //define the x position of the volume
                double MAPMT_xlocal = (nPMTInARow - 1) * (MAPMT_dx + MAPMTgap / 2) - (MAPMT_dx * 2 + MAPMTgap) * ipmt;
                
                //build the PMT
                G4Box PMT = buildMAPMT(String.format("MAPMT_%d_%d", irow, ipmt));
                PMT.setMother(gasVolume);
                
                PMT.translate(MAPMT_xlocal, MAPMT_ylocal, MAPMTFirstRow_z);
                
                PMT.rotate("xzy", Math.toRadians(RICH_thtilt), Math.toRadians(90.0 - (sector - 1) * 60.0), 0);
                Vector3d position = new Vector3d(0, RICHpanel_y + RICHpanel_y_offset, RICHpanel_z);
                PMT.translate(position.rotateZ(-Math.toRadians(90.0 - (sector - 1) * 60.0)));
                pmts.add(PMT);
                
                
                
            }
        }
        
    }
    
    
    // Function to import as stl volumes all stl files:
    
    private ArrayList StlNames()
    {
        ArrayList<String> names = new ArrayList<String>();
        // CFRP
        names.add("CFRP");
        names.add("MirrorSupport");
        // Aluminium
        names.add("Al_Base");
        names.add("Al_BottomFrame");
        names.add("Al_TopFrame");
        names.add("Al_TopLeft");
        names.add("Al_TopRight");
        // Tedlar Wrapping : Horizontal components
        names.add("Tedlar_H1");
        names.add("Tedlar_H2");
        names.add("Tedlar_H3");
        names.add("Tedlar_H4");
        names.add("Tedlar_H5");
        names.add("Tedlar_H6");
        names.add("Tedlar_H7");
        names.add("Tedlar_H8");
        names.add("Tedlar_H9");
        names.add("Tedlar_H10");
        names.add("Tedlar_H11");
        names.add("Tedlar_H12");
        names.add("Tedlar_H13");
        names.add("Tedlar_H14");
        names.add("Tedlar_H15");
        names.add("Tedlar_H16");
        names.add("Tedlar_H17");
        names.add("Tedlar_H18");
        names.add("Tedlar_H19");
        names.add("Tedlar_H20");
        names.add("Tedlar_H21");
        names.add("Tedlar_H22");
        names.add("Tedlar_H23");
        names.add("Tedlar_H24");
        names.add("Tedlar_H25");
        names.add("Tedlar_H26");
        names.add("Tedlar_H27");
        names.add("Tedlar_H28");
        names.add("Tedlar_H29");
        names.add("Tedlar_H30");
        names.add("Tedlar_H31");
        names.add("Tedlar_H32");
        names.add("Tedlar_H33");
        names.add("Tedlar_H34");
        names.add("Tedlar_H35");
        names.add("Tedlar_H36");
        names.add("Tedlar_H37");
        names.add("Tedlar_H38");
        names.add("Tedlar_H39");
        names.add("Tedlar_H40");
        names.add("Tedlar_H41");
        names.add("Tedlar_H42");
        names.add("Tedlar_H43");
        names.add("Tedlar_H44");
        names.add("Tedlar_H45");
        names.add("Tedlar_H46");
        names.add("Tedlar_H47");
        // Tedlar Wrapping : Vertical components
        names.add("Tedlar_V1");
        names.add("Tedlar_V2");
        names.add("Tedlar_V3");
        names.add("Tedlar_V4");
        names.add("Tedlar_V5");
        names.add("Tedlar_V6");
        names.add("Tedlar_V7");
        names.add("Tedlar_V8");
        names.add("Tedlar_V9");
        names.add("Tedlar_V10");
        names.add("Tedlar_V11");
        names.add("Tedlar_V12");
        names.add("Tedlar_V13");
        names.add("Tedlar_V14");
        names.add("Tedlar_V15");
        names.add("Tedlar_V16");
        names.add("Tedlar_V17");
        names.add("Tedlar_V18");
        names.add("Tedlar_V19");
        names.add("Tedlar_V20");
        names.add("Tedlar_V21");
        names.add("Tedlar_V22");
        names.add("Tedlar_V23");
        // Aerogel Tiles
        names.add("Layer_201_component_1");
        names.add("Layer_201_component_2");
        names.add("Layer_201_component_3");
        names.add("Layer_201_component_4");
        names.add("Layer_201_component_5");
        names.add("Layer_201_component_6");
        names.add("Layer_201_component_7");
        names.add("Layer_201_component_8");
        names.add("Layer_201_component_9");
        names.add("Layer_201_component_10");
        names.add("Layer_201_component_11");
        names.add("Layer_201_component_12");
        names.add("Layer_201_component_13");
        names.add("Layer_201_component_14");
        names.add("Layer_201_component_15");
        names.add("Layer_201_component_16");
        names.add("Layer_202_component_1");
        names.add("Layer_202_component_2");
        names.add("Layer_202_component_3");
        names.add("Layer_202_component_4");
        names.add("Layer_202_component_5");
        names.add("Layer_202_component_6");
        names.add("Layer_202_component_7");
        names.add("Layer_202_component_8");
        names.add("Layer_202_component_9");
        names.add("Layer_202_component_10");
        names.add("Layer_202_component_11");
        names.add("Layer_202_component_12");
        names.add("Layer_202_component_13");
        names.add("Layer_202_component_14");
        names.add("Layer_202_component_15");
        names.add("Layer_202_component_16");
        names.add("Layer_202_component_17");
        names.add("Layer_202_component_18");
        names.add("Layer_202_component_19");
        names.add("Layer_202_component_20");
        names.add("Layer_202_component_21");
        names.add("Layer_202_component_22");
        names.add("Layer_203_component_1");
        names.add("Layer_203_component_2");
        names.add("Layer_203_component_3");
        names.add("Layer_203_component_4");
        names.add("Layer_203_component_5");
        names.add("Layer_203_component_6");
        names.add("Layer_203_component_7");
        names.add("Layer_203_component_8");
        names.add("Layer_203_component_9");
        names.add("Layer_203_component_10");
        names.add("Layer_203_component_11");
        names.add("Layer_203_component_12");
        names.add("Layer_203_component_13");
        names.add("Layer_203_component_14");
        names.add("Layer_203_component_15");
        names.add("Layer_203_component_16");
        names.add("Layer_203_component_17");
        names.add("Layer_203_component_18");
        names.add("Layer_203_component_19");
        names.add("Layer_203_component_20");
        names.add("Layer_203_component_21");
        names.add("Layer_203_component_22");
        names.add("Layer_203_component_23");
        names.add("Layer_203_component_24");
        names.add("Layer_203_component_25");
        names.add("Layer_203_component_26");
        names.add("Layer_203_component_27");
        names.add("Layer_203_component_28");
        names.add("Layer_203_component_29");
        names.add("Layer_203_component_30");
        names.add("Layer_203_component_31");
        names.add("Layer_203_component_32");
        names.add("Layer_204_component_1");
        names.add("Layer_204_component_2");
        names.add("Layer_204_component_3");
        names.add("Layer_204_component_4");
        names.add("Layer_204_component_5");
        names.add("Layer_204_component_6");
        names.add("Layer_204_component_7");
        names.add("Layer_204_component_8");
        names.add("Layer_204_component_9");
        names.add("Layer_204_component_10");
        names.add("Layer_204_component_11");
        names.add("Layer_204_component_12");
        names.add("Layer_204_component_13");
        names.add("Layer_204_component_14");
        names.add("Layer_204_component_15");
        names.add("Layer_204_component_16");
        names.add("Layer_204_component_17");
        names.add("Layer_204_component_18");
        names.add("Layer_204_component_19");
        names.add("Layer_204_component_20");
        names.add("Layer_204_component_21");
        names.add("Layer_204_component_22");
        names.add("Layer_204_component_23");
        names.add("Layer_204_component_24");
        names.add("Layer_204_component_25");
        names.add("Layer_204_component_26");
        names.add("Layer_204_component_27");
        names.add("Layer_204_component_28");
        names.add("Layer_204_component_29");
        names.add("Layer_204_component_30");
        names.add("Layer_204_component_31");
        names.add("Layer_204_component_32");
        // Planar Mirrors
        names.add("Layer_301_component_1");
        names.add("Layer_301_component_2");
        names.add("Layer_301_component_3");
        names.add("Layer_301_component_4");
        names.add("Layer_301_component_5");
        names.add("Layer_301_component_6");
        names.add("Layer_301_component_7");
        return names;
    }
    
    //function that generates the PMT with all the volumes in it
    
    private G4Box buildMAPMT(String mapmtName) {
        
        
        G4Box MAPMTVolume = new G4Box(mapmtName, MAPMT_dx, MAPMT_dy, MAPMT_dz);
        
        //Aluminum walls of MAPMT
        G4Box AluminumLeft = new G4Box("AlLeft_" + mapmtName, MAPMTWall_thickness / 2, MAPMT_dy - MAPMTWall_thickness, MAPMT_dz);
        AluminumLeft.translate(MAPMT_dx - MAPMTWall_thickness / 2, 0, 0);
        AluminumLeft.setMother(MAPMTVolume);
        
        G4Box AluminumRight = new G4Box("AlRight_" + mapmtName, MAPMTWall_thickness / 2, MAPMT_dy - MAPMTWall_thickness, MAPMT_dz);
        AluminumRight.translate(-MAPMT_dx + MAPMTWall_thickness / 2, 0, 0);
        AluminumRight.setMother(MAPMTVolume);
        
        G4Box AluminumTop = new G4Box("AlTop_" + mapmtName, MAPMT_dx, MAPMTWall_thickness / 2, MAPMT_dz);
        AluminumTop.translate(0, MAPMT_dy - MAPMTWall_thickness / 2, 0);
        AluminumTop.setMother(MAPMTVolume);
        
        G4Box AluminumBottom = new G4Box("AlBottom_" + mapmtName, MAPMT_dx, MAPMTWall_thickness / 2, MAPMT_dz);
        AluminumBottom.translate(0, -MAPMT_dy + MAPMTWall_thickness / 2, 0);
        AluminumBottom.setMother(MAPMTVolume);
        
        //window
        G4Box Window = new G4Box("Window_" + mapmtName, MAPMT_dx - MAPMTWall_thickness, MAPMT_dy - MAPMTWall_thickness, MAPMTWindow_thickness / 2);
        Window.translate(0, 0, -MAPMT_dz + MAPMTWindow_thickness / 2);
        Window.setMother(MAPMTVolume);
        //Test indexof refraction
        Window.setIndexRefraction(n_w);
        
        //photocathode
        G4Box Photocathode = new G4Box("Photocathode_" + mapmtName, MAPMTPhotocathode_side / 2, MAPMTPhotocathode_side / 2, MAPMTPhotocathode_thickness / 2);
        Photocathode.translate(0, 0, -MAPMT_dz + MAPMTWindow_thickness + MAPMTPhotocathode_thickness / 2);
        Photocathode.setMother(MAPMTVolume);
        // add the photocatodes to the list
        Photocathode.setIndexRefraction(n_w);//test to remove
        photocatodes.add(Photocathode);
        
        //socket
        G4Box Socket = new G4Box("Socket_" + mapmtName, MAPMT_dx - MAPMTWall_thickness, MAPMT_dy - MAPMTWall_thickness, MAPMTSocket_thickness / 2);
        Socket.translate(0, 0, MAPMT_dz - MAPMTSocket_thickness / 2);
        Socket.setMother(MAPMTVolume);
        
        return MAPMTVolume;
    }
    
    /**
     * @author: gangel
     * @param i the nr of the PMT
     * @return: PMT volume as a G4Box
     */
    public G4Box GetPMT(int i)
    {
        
        return    pmts.get(i-1);
        
        // Object[] pmtArray = pmts.toArray();
        //return (G4Box) pmtArray[i];
    }
    
    /**
     * @author: gangel
     * @param i the nr of the PMT
     * @return: the Photocatodes volumes inside the PMT
     */
    public G4Box GetPhotocatode(int i)
    {
        
        return    photocatodes.get(i-1);
        
        
    }
    
    /**
     * @author: gangel
     * @param i the STL volume
     * 0 OpticalGasVolume - 1 AerogelTiles,2 Aluminum,3 CFRP,4 Glass,5 TedlarWrapping
     * @return: the Photocatodes volumes inside the PMT
     */
    public G4Stl GetStl(int i)
    {
        return  stlvolumes.get(i-1);
    }
}
