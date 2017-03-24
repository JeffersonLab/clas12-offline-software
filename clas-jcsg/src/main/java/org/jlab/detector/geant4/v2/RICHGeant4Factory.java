/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2;

import eu.mihosoft.vrl.v3d.Vector3d;
import java.io.InputStream;
import org.jlab.detector.units.SystemOfUnits.Length;
import org.jlab.detector.volume.G4Stl;
import org.jlab.detector.volume.G4World;
import org.jlab.detector.volume.G4Box;
import org.jlab.detector.volume.G4Trap;

/**
 *
 * @author Goodwill
 */
public final class RICHGeant4Factory extends Geant4Factory {
    //to be  stored in database, from hashmap of perl script for gemc, all dimensions in mm
    private final int PMT_rows = 23, sector = 4;
    private int nPMTs = 0;
    
    private final double RichBox_x = 0 * Length.mm,
                         RichBox_y = 1706.145* Length.mm,
                         RichBox_z = 5705.13* Length.mm,
                         RichBox_the = 25,    // position of box
                         RichBox_y_offset = 284.31* Length.mm,

                         PMTCase_dx = 26* Length.mm, 
                         PMTCase_dy = 26* Length.mm,       //dimensions of PMT
                         PMTCase_dz = 13.5* Length.mm,
                         
                         PMTSeparation = 1* Length.mm,
                         PMTFirstRow_y = -1931.23* Length.mm,  //Position of PMT in box
                         PMTFirstRow_z = 474.10* Length.mm;
                            
    private double PMTCase_x =0 * Length.mm,
                   PMTCase_y = PMTFirstRow_y;
                   
    
    private final double PMTCase_z =PMTFirstRow_z;

    public RICHGeant4Factory() {
            motherVolume = new G4World("fc");
            //import the 5 mesh files
            
            ClassLoader cloader = getClass().getClassLoader();
            for (String name : new String[]{"AerogelTiles", "Aluminum", "CFRP","Glass","TedlarWrapping"}) {
                G4Stl component = new G4Stl(String.format("%s", name),
                        cloader.getResourceAsStream(String.format("rich/cad/%s.stl",name)));
                
                component.scale(Length.mm/Length.cm);
                component.setMother(motherVolume);
            }
            
            //calculate the position of the rich box where PMTs will be placed              
            Vector3d position = RichBoxPos.getpos(sector, RichBox_x, RichBox_y, RichBox_y_offset, RichBox_z);
            //calculate the rotation of the box
            Vector3d rotation = RichBoxPos.getrot(sector,RichBox_the);
            
            //place the PMTs in the trapezoidal box
            for(int irow=0; irow<PMT_rows ; irow++){
                
		//define the y position of the volume
                PMTCase_y=PMTFirstRow_y;
                
                PMTCase_y += (PMTSeparation+2.0*PMTCase_dy)*irow;
                //System.out.println(PMTCase_y);
                
                //define number of PMTs in this row
		int nPMTInARow = 6 + irow;
                        
		//define the xoffset for this row
		double PMTOffset_x = (nPMTInARow -1)*(PMTCase_dx+PMTSeparation/2);
			
                for(int ipmt=0; ipmt < nPMTInARow; ipmt++){
                    //increment count of PMT
                    nPMTs+=1;
                    //define the x position of the volume
                    PMTCase_x = PMTOffset_x - (2*PMTCase_dx +PMTSeparation)*ipmt;
                    //build the PMT
                    G4Box PMT = PMTBuilder.buildPMTVolume(nPMTs,irow,PMTCase_dx, PMTCase_dy, PMTCase_dz);
                    PMT.setMother(motherVolume);
                    PMT.translate(PMTCase_x, PMTCase_y, PMTCase_z);
                    PMT.rotate("xzy", rotation.x, rotation.y, rotation.z);
                    PMT.translate(position.x,position.y, position.z);
                }
            }
        
    }
    
    static private class RichBoxPos{
        
        static Vector3d getpos(int sector, double RichBox_x, double RichBox_y, double RichBox_y_offset, double RichBox_z){
        //calculate position of the box
            double phi = (sector-1)*60;
            double RichBox_y_real = RichBox_y + RichBox_y_offset;
            double r = Math.sqrt(RichBox_x * RichBox_x + RichBox_y_real*RichBox_y_real);
            double x= r*Math.cos(Math.toRadians(phi));
            double y = r*Math.sin(Math.toRadians(phi));
            double z = RichBox_z;
            
            Vector3d position = new Vector3d(x,y,z);
            return position;
        }
        
        static Vector3d getrot(int sector, double RichBox_the){
            double tilt = RichBox_the;
            double zrot = -(sector -1)*60 + 90;
            
            Vector3d rotation = new Vector3d(Math.toRadians(tilt),Math.toRadians(zrot),0);
            return rotation;
        }
        
        
            
    }

    static private class PMTBuilder {
        //function that generates the PMT with all the volumes in it 
        static G4Box buildPMTVolume(int nPMTs, int irow, double PMTCase_dx, double PMTCase_dy, double PMTCase_dz) {
            
            G4Box PMTVolume = new G4Box(String.format("PMTRow_%d_n%d", irow,nPMTs), PMTCase_dx, PMTCase_dy, PMTCase_dz);
            
            double PMTCase_width = 1.0 * Length.mm;
            
            //Aluminum
            G4Box AluminumLeft = new G4Box(String.format("AlLeft_%d_n%d", irow, nPMTs), PMTCase_width/2,PMTCase_dy-PMTCase_width,PMTCase_dz);
            AluminumLeft.translate(PMTCase_dx-PMTCase_width/2,0,0);
            AluminumLeft.setMother(PMTVolume);
            
            G4Box AluminumRight = new G4Box(String.format("AlRight_%d_n%d", irow, nPMTs), PMTCase_width/2,PMTCase_dy-PMTCase_width,PMTCase_dz);
            AluminumRight.translate(-PMTCase_dx+PMTCase_width/2,0,0);
            AluminumRight.setMother(PMTVolume);
            
            G4Box AluminumTop = new G4Box(String.format("AlTop_%d_n%d", irow, nPMTs),PMTCase_dx, PMTCase_width/2,PMTCase_dz);
            AluminumTop.translate(0,PMTCase_dy-PMTCase_width/2,0);
            AluminumTop.setMother(PMTVolume);
            
            G4Box AluminumBottom = new G4Box(String.format("AlBottom_%d_n%d", irow, nPMTs),PMTCase_dx, PMTCase_width/2,PMTCase_dz);
            AluminumBottom.translate(0,-PMTCase_dy+PMTCase_width/2,0);
            AluminumBottom.setMother(PMTVolume);
           
            
            //window
            double PMTWindow_dz = 0.75 * Length.mm,
                   PMTWindow_dx = PMTCase_dx - PMTCase_width,
                   PMTWindow_dy = PMTCase_dy - PMTCase_width; 
            double PMTWindow_z = -PMTCase_dz + PMTWindow_dz;
            G4Box Window = new G4Box(String.format("Window_%d_n%d", irow, nPMTs), PMTWindow_dx, PMTWindow_dy, PMTWindow_dz);
            Window.translate(0,0,PMTWindow_z);
            Window.setMother(PMTVolume);
            
            
            //photocathode
            double PMTPhotocathode_dx = 24.5 * Length.mm,
                   PMTPhotocathode_dy = 24.5 * Length.mm,
                   PMTPhotocathode_dz = 0.5 * Length.mm;
            double PMTPhotocathode_z = -PMTCase_dz + 2*PMTWindow_dz + PMTPhotocathode_dz;
            G4Box Photocathode = new G4Box(String.format("Photocathode_%d_n%d", irow, nPMTs),PMTPhotocathode_dx,PMTPhotocathode_dy,PMTPhotocathode_dz);
            Photocathode.translate(0,0,PMTPhotocathode_z);
            Photocathode.setMother(PMTVolume);
            
            //socket
            double PMTSocket_dx = PMTCase_dx - PMTCase_width,
                    PMTSocket_dy = PMTCase_dy - PMTCase_width,
                    PMTSocket_dz = 1.35 * Length.mm;
            double PMTSocketZShift = 1.0 * Length.mm;
            double PMTSocket_z = PMTCase_dz - PMTSocket_dz;
            G4Box Socket = new G4Box(String.format("Socket_%d_n%d", irow, nPMTs),PMTSocket_dx,PMTSocket_dy,PMTSocket_dz);
            Socket.translate(0,0,PMTSocket_z);
            Socket.setMother(PMTVolume);
            
            return PMTVolume;   
        }
    }
    
}


