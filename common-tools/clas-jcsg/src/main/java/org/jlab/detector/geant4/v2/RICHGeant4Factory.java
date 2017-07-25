/*
 * To change this license header, choose License Headers in Project Properties.
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
    

    public RICHGeant4Factory() {
    	
        motherVolume = new G4World("fc");
        //import the 5 mesh files

        ClassLoader cloader = getClass().getClassLoader();
        G4Stl gasVolume = new G4Stl("OpticalGasVolume", cloader.getResourceAsStream("rich/cad/OpticalGasVolume.stl"), Length.mm / Length.cm);
        gasVolume.setMother(motherVolume);

        for (String name : new String[]{"AerogelTiles", "Aluminum", "CFRP", "Glass", "TedlarWrapping"}) {
            G4Stl component = new G4Stl(String.format("%s", name),
                    cloader.getResourceAsStream(String.format("rich/cad/%s.stl", name)),
                    Length.mm / Length.cm);

            component.setMother(gasVolume);
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

        //photocathode
        G4Box Photocathode = new G4Box("Photocathode_" + mapmtName, MAPMTPhotocathode_side / 2, MAPMTPhotocathode_side / 2, MAPMTPhotocathode_thickness / 2);
        Photocathode.translate(0, 0, -MAPMT_dz + MAPMTWindow_thickness + MAPMTPhotocathode_thickness / 2);
        Photocathode.setMother(MAPMTVolume);
      // add the photocatodes to the list
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
    	
    return	pmts.get(i-1);
    
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
    	
    return	photocatodes.get(i-1);
    
   
    }
}
