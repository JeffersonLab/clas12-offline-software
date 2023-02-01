/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jlab.rec.dc.cross;

import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.dc.trajectory.TrackVec;

/**
 *
 * @author ziegler
 */
public class MCCross extends Cross {
    
    public MCCross(int sector, int region, int rid, DataEvent event,
            Swim swim,DCGeant4Factory DcDetector) {
        super(sector, region, rid);
        if(pars==null)
            pars = mcTrackPars(event);
        TrackVec tv = new TrackVec();
        double[] xpars= tv.TransformToTiltSectorFrame(sector, pars[0],pars[1],pars[2]);
        double[] ppars= tv.TransformToTiltSectorFrame(sector, pars[3],pars[4],pars[5]);
        swim.SetSwimParameters(xpars[0],xpars[1],xpars[2],ppars[0],ppars[1],ppars[2], (int)pars[6]);
        
        z = DcDetector.getRegionMidpoint(this.get_Region() - 1).z;
        double spars[] = swim.SwimToPlaneTiltSecSys(sector, z);
        this.set_Point(new Point3D(spars[0],spars[1],spars[2]));
        this.set_Dir(new Vector3D(spars[3],spars[4],spars[5]).asUnit().toPoint3D());
    }
    private double[] pars;
    private double z;
    public static double[] mcTrackPars(DataEvent event) {
        double[] value = new double[7];
        if (event.hasBank("MC::Particle") == false) {
            return value;
        }
        DataBank bank = event.getBank("MC::Particle");
        
        // fills the arrays corresponding to the variables
        if(bank!=null) {
            value[0] = (double) bank.getFloat("vx", 0);
            value[1] = (double) bank.getFloat("vy", 0);
            value[2] = (double) bank.getFloat("vz", 0);
            value[3] = (double) bank.getFloat("px", 0);
            value[4] = (double) bank.getFloat("py", 0);
            value[5] = (double) bank.getFloat("pz", 0);
            value[6] = getCharge(bank.getInt("pid", 0));
            
        }
        return value;
    }
    
    
    private static double getCharge(int pid) {
       if((int)(pid/100) ==0 ) {
           return -Math.signum(pid);
       } else {
           return Math.signum(pid);
       }
    }
}
