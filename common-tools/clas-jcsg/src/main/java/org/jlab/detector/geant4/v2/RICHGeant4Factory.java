/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2;

import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Vector3d;
import eu.mihosoft.vrl.v3d.Vertex;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.units.SystemOfUnits.Length;
import org.jlab.detector.volume.G4Stl;
import org.jlab.detector.volume.G4World;
import org.jlab.detector.volume.G4Box;

/**
 * Building the RICH PMTs
 * @author gangel, Goodwill, kenjo
 */
public final class RICHGeant4Factory extends Geant4Factory {
    // list containing the volumes that can be obtained with a getter method
    private List<G4Box> pmts = new ArrayList<G4Box>();
    private List<G4Box> photocatodes = new ArrayList<G4Box>();
    private List<G4Box> windows = new ArrayList<G4Box>();
    private List<G4Stl> aerogel_201=new ArrayList<G4Stl>();
    private List<G4Stl> aerogel_202=new ArrayList<G4Stl>();
    private List<G4Stl> aerogel_203=new ArrayList<G4Stl>();
    private List<G4Stl> aerogel_204=new ArrayList<G4Stl>();
    private List<G4Stl> mirror_301 = new ArrayList<G4Stl>();
    private List<G4Stl> mirror_302 = new ArrayList<G4Stl>();
    private List<G4Stl> stlvolumes = new ArrayList<G4Stl>();
    
    private int StlNr = 0;
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
    
    
    
    // Optical properties (as a test), they will be implemented for database purposes
    //   private double n_w=1.3;
    //  private double n_aerogel=1.05;
    
    public RICHGeant4Factory() {

        int debugMode = 0;
        
        motherVolume = new G4World("fc");
        //import the 5 mesh files
        int stlN=0;
        
        if(debugMode>=1)System.out.format("#####RICHFactory Geant4 v2 \n");
        ClassLoader cloader = getClass().getClassLoader();
        if(debugMode>=1)System.out.format("   --> Rich Sector 4 Mother volume\n");
        G4Stl gasVolume = new G4Stl("RICH_s4", cloader.getResourceAsStream("rich/cad/RICH_s4.stl"), Length.mm / Length.cm);
        gasVolume.setMother(motherVolume);
        stlvolumes.add(gasVolume);
        for (String name : new String[]{"Aluminum", "CFRP", "TedlarWrapping","MirrorSupport"}) {
            if(debugMode>=1)System.out.format("   --> %s\n",name);
            G4Stl component = new G4Stl(String.format("%s", name),
                                        cloader.getResourceAsStream(String.format("rich/cad/%s.stl", name)),
                                        Length.mm / Length.cm);
            if(debugMode>=1)System.out.println("Resource read correctly");
            component.setMother(gasVolume);
            stlvolumes.add(component);
            stlN++;
        }
        this.StlNr=stlN;
        
        // STL READER:
        // Read an Stl from folder Layer, with component from 1 up to component max.
        // Give 0 if the Layer is not optical
        // Give 1 if it is otpical but not reflective (ex. aerogel)
        // Give 2 if it is mirror
        Read_Stl(gasVolume,201,16);
        Read_Stl(gasVolume,202,22);
        Read_Stl(gasVolume,203,32);
        Read_Stl(gasVolume,204,32); 
        // Mirrors
        Read_Stl(gasVolume,301,7); //Planar Mirrors
        Read_Stl(gasVolume,302,10); //Spherical Mirrors
        
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
        windows.add(Window);
        
        //photocathode
        G4Box Photocathode = new G4Box("Photocathode_" + mapmtName, MAPMTPhotocathode_side / 2, MAPMTPhotocathode_side / 2, MAPMTPhotocathode_thickness / 2);
        Photocathode.translate(0, 0, -MAPMT_dz + MAPMTWindow_thickness + MAPMTPhotocathode_thickness / 2);
        Photocathode.setMother(MAPMTVolume);
        Photocathode.makeSensitive();
        
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
        
        return    pmts.get(i-1);
        
        // Object[] pmtArray = pmts.toArray();
        //return (G4Box) pmtArray[i];
    }
    
    /**
     * @author: gangel
     * @param i the nr of the PMT (starting from 1)
     * @return: the Photocatodes volumes inside the PMT
     */
    public G4Box GetPhotocatode(int i)
    {
        return    photocatodes.get(i-1);
    }
    
    
    /**
     * @author: gangel
     * @param i the nr of the PMT (starting from 1 )
     * @return: the Windows volumes inside the PMT
     */
    public G4Box GetWindow(int i)
    {
        return windows.get(i-1);
    }
    
    /**
     * @author: gangel
     * @param i the STL volume nr (as follows):
     * 0: OpticalGasVolume -1: Aluminum -2: CFRP -3: TedlarWrapping -4: MirrorSupport
     * @return: the Stl volume
     */
    public G4Stl GetStl(int i)
    {
        System.out.println(" GETSTL IS OBSOLETE. DO NOT USE IT, the volumes are incorrect");
        return  stlvolumes.get(i);
    }
    
    public int GetStlNR()
    {
        return this.StlNr;
    }
    
    //i is the number of the stl
    //
    public Vector3d GetNormal_Stl(int i, Vector3d hitV )
    {
        
        Vector3d[] Vector = new Vector3d[3];
        int k=0;
        //to modify
        for ( Polygon comp:    this.GetStl(i).toCSG().getPolygons() )
        {
            if(comp.contains(hitV))  // search between all the components of the mesh the one containing the vector
            {
                for (Vertex vert: comp.vertices ) //creating a list of vertices of components of the hitten track
                {
                    Vector[k] = vert.pos;
                    k++;
                }
            }
        }
        // using k=0,1,2, I am sure I am going to take the entrance vertexes
        Vector3d Surf1= new Vector3d(Vector[1].minus(Vector[0]));
        Vector3d Surf2= new Vector3d(Vector[2].minus(Vector[0]));
        Vector3d SurfN = new Vector3d(Surf1.cross(Surf2));
        return SurfN;
    }
    
    // read from file an Stl using the RICH layer, component convention
    private void Read_Stl(G4Stl Mother, int Layernr , int componentnr ){

        int debugMode = 0;
        
        ClassLoader cloader = getClass().getClassLoader();
        for ( int _ii = 1 ; _ii<= componentnr; _ii++)
        {
            String Layers = String.format("Layer_%d",Layernr);
            String name = String.format("%s_component_%d",Layers,_ii);
            String Path = String.format("%d",Layernr);
            // String Path = String.format("%s/%d",Layers,Layernr);
            if(debugMode>=1 && _ii==componentnr)System.out.format(" Loaded up to %s \n",String.format("rich/cad/%s_%d.stl",Path,_ii));
            G4Stl component = new G4Stl(String.format("%s", name),
                                        cloader.getResourceAsStream(String.format("rich/cad/%s_%d.stl",Path,_ii)),
                                        Length.mm / Length.cm);
            component.setMother(Mother);
            if(Layernr == 201){
                //component.makeOptical();
                aerogel_201.add(component);
            }
            else  if(Layernr == 202){
                //component.makeOptical();
                aerogel_202.add(component);
            }
            else if(Layernr == 203){
                //  component.makeOptical();
                aerogel_203.add(component);
            }
            else  if(Layernr == 204){
                // component.makeOptical();
                aerogel_204.add(component);
            }
            else  if(Layernr == 301){
                // component.makeReflective();
                mirror_301.add(component);
            }
            else  if(Layernr == 302){
                // component.makeReflective();
                mirror_302.add(component);
            }
        }
        return;
    }
    /**
     * @author: gangel
     * @param   layer nr, component nr
     * Layer can be (Aerogel) 201,202,203,204,  (Planar Mirror) 301, (Spherical Mirror)302
     * @return: the G4Stl file
     */
    public G4Stl getStlComponent(int Layer_nr , int Component_nr){
        if (Layer_nr == 201) return aerogel_201.get(Component_nr);
        else if (Layer_nr == 202) return aerogel_202.get(Component_nr);
        else if (Layer_nr == 203) return aerogel_203.get(Component_nr);
        else if (Layer_nr == 204) return aerogel_204.get(Component_nr);
        else if (Layer_nr == 301) return mirror_301.get(Component_nr);
        else if (Layer_nr == 302) return mirror_302.get(Component_nr);
        return null;
    }
    
    public int getStlNumber(int Layer_nr){
        //System.out.format("getStlNumber Chiesto layer %d\n",Layer_nr);
        if (Layer_nr == 201) return 16;
        else if (Layer_nr == 202) return 22;
        else if (Layer_nr == 203) return 32;
        else if (Layer_nr == 204) return 32;
        else if (Layer_nr == 301) return 7;
        else if (Layer_nr == 302) return 10;
        return 0;
    }
}

