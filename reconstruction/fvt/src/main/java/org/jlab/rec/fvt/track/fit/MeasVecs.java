package org.jlab.rec.fvt.track.fit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.jlab.rec.fvt.fmt.Constants;

import org.jlab.rec.fvt.track.Track;
import org.jlab.rec.fvt.track.fit.StateVecs.StateVec;

import org.apache.commons.math3.special.Erf;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.dc.trajectory.DCSwimmer;
import org.jlab.rec.fvt.fmt.cluster.Cluster;

public class MeasVecs {

    public List<MeasVec> measurements ;

    public class MeasVec implements Comparable<MeasVec> {

        public double z = Double.NaN; 
        public double centroid; 
        public double error;
        public int layer;
        public int k;
        public int size;

        MeasVec() {
        }

        @Override
        public int compareTo(MeasVec arg) {
            int CompLay = this.layer < arg.layer ? -1 : this.layer == arg.layer ? 0 : 1;
            return CompLay;
        }

    }
    Random random = new Random();
    public void setMeasVecs(DataEvent event, DCSwimmer swim) {
        random = new Random();
        if (event.hasBank("TimeBasedTrkg::TBTracks") == false) {
          // System.err.println(" no tracks");
            return ;
        }

        DataBank bankMC = event.getBank("MC::Particle");
	
            
        double x0 = bankMC.getFloat("vx", 0)/10.;
        double y0 = bankMC.getFloat("vy", 0)/10.;
        double z0 = bankMC.getFloat("vz", 0)/10.;
        double px0 = bankMC.getFloat("px", 0);
        double py0 = bankMC.getFloat("py", 0);
        double pz0 = bankMC.getFloat("pz", 0);
        int q =1;
        swim.SetSwimParameters(x0, y0, z0, px0, py0, pz0, q);
        
        measurements = new ArrayList<MeasVec>();
        
        MeasVec meas0 = new MeasVec();
        meas0.centroid = 0;
        meas0.error = 1;
        meas0.layer = 0;
        meas0.z = 0;
        measurements.add(meas0);
        for(int l = 0; l<6; l++) {
                
            MeasVec meas    = new MeasVec();
            //meas.centroid   = measurementsArray[l].get_Centroid();
            //meas.centroid   = (double)measurementsArray[l].get_SeedStrip();
            //double err      = 1 ;
            double sigma = 0.3;
            double simErr = sigma*random.nextGaussian();
            
            double err      = (double) Constants.FVT_Pitch/Math.sqrt(12.); 
            
            //meas.error      = Constants.FVT_Pitch*Constants.FVT_Pitch;
            meas.error      = sigma*sigma*Constants.FVT_Pitch*Constants.FVT_Pitch;
            meas.layer      = l+1;
            meas.z          = Constants.FVT_Zlayer[l]+Constants.hDrift/2;

            meas.size = 1;
            double ValAtPlane[] = swim.SwimToPlaneLab(meas.z);
            meas.centroid   =ValAtPlane[1]*Math.cos(Constants.FVT_Alpha[l])- ValAtPlane[0]*Math.sin(Constants.FVT_Alpha[l]);
            //System.out.println(" true centroid "+meas.centroid);
            meas.centroid=(Constants.FVT_Pitch*simErr  +meas.centroid) ;     //System.out.println(" smeared centroid "+meas.centroid); 
            measurements.add(meas);
            
        }   
    }
    public void setMeasVecs(Track trkcand) {

        measurements = new ArrayList<MeasVec>();
        
        Cluster[] measurementsArray;
        measurementsArray = new Cluster[6] ;
        
	for (int i = 0; i < trkcand.get_Clusters().size(); i++) {
            if(trkcand.get_Clusters().get(i).size()<=10) {
                measurementsArray[trkcand.get_Clusters().get(i).get_Layer()-1] = trkcand.get_Clusters().get(i);
                //System.out.println(" using cluster "+trkcand.get_Clusters().get(i).printInfo());
            }
            
            //for(int j =0; j< trkcand.get_Clusters().get(i).size(); j++)
            //    System.out.println(" layer "+trkcand.get_Clusters().get(i).get_Layer()+" strip "+trkcand.get_Clusters().get(i).get(j).get_Strip()+" E "+trkcand.get_Clusters().get(i).get(j).get_Edep());
        }	
	
        MeasVec meas0 = new MeasVec();
        meas0.centroid = 0;
        meas0.error = 1;
        meas0.layer = 0;
        meas0.z = 0;
        measurements.add(meas0);
        for(int l = 0; l<6; l++) {
            if(measurementsArray[l]!=null) {
                
                MeasVec meas    = new MeasVec();
                meas.centroid   = measurementsArray[l].get_Centroid();
                //meas.centroid   = (double)measurementsArray[l].get_SeedStrip();
                //double err      = Constants.FVT_Pitch/ (double) measurementsArray[l].size();
                double err      = (double) measurementsArray[l].size()*Constants.FVT_Pitch/Math.sqrt(12.);
                meas.error      = err*err ;
                meas.layer      = measurementsArray[l].get_Layer();
                meas.z          = Constants.FVT_Zlayer[measurementsArray[l].get_Layer()-1]+0.5*Constants.hDrift;

                meas.size = measurementsArray[l].size();
                
                measurements.add(meas);
            } else {
                MeasVec meas    = new MeasVec();
                meas.centroid   = 0;
                //meas.centroid   = (double)trkcand.get_Clusters().get(i).get_SeedStrip();
                //double err      = measurementsArray[l].get_CentroidError()/Constants.FVT_SigmaS ;
                double err      = (double) Double.POSITIVE_INFINITY;
                meas.error      = err*err;
                meas.layer      = l+1;
                meas.z          = Constants.FVT_Zlayer[l] +0.5*Constants.hDrift;

                meas.size = 0;
                
                measurements.add(meas);
            }
        }
      
        Collections.sort(measurements);
        for (int i = 0; i < measurements.size(); i++) {
            measurements.get(i).k = i;
            //System.out.println(i+") meas "+i+" layer "+measurements.get(i).layer+" cent "+measurements.get(i).centroid
            //		+" err "+measurements.get(i).error+ " z "+measurements.get(i).z);
        }
    }

    
    public double h(StateVec stateVec) {
        if (stateVec == null) {
            return 0;
        }
        if (this.measurements.get(stateVec.k) == null) {
            return 0;
        }
        
        int layer = this.measurements.get(stateVec.k).layer;
        
        //return this.getCentroidEstimate(layer, stateVec.x, stateVec.y);
        //return (double) this.getClosestStrip(stateVec.x, stateVec.y, layer);
        return stateVec.y*Math.cos(Constants.FVT_Alpha[layer-1])-stateVec.x*Math.sin(Constants.FVT_Alpha[layer-1]);
       //return stateVec.transportTroughDriftGap(0.99, this);
    }

    public int getClosestStrip(double x, double y, int layer) {
        int closestStrip = 0;
        if(Math.sqrt(x*x+y*y)<Constants.FVT_Rmax && Math.sqrt(x*x+y*y)>Constants.FVT_Beamhole) {
	
            double x_loc =  x*Math.cos(Constants.FVT_Alpha[layer-1])+ y*Math.sin(Constants.FVT_Alpha[layer-1]);
            double y_loc =  y*Math.cos(Constants.FVT_Alpha[layer-1])- x*Math.sin(Constants.FVT_Alpha[layer-1]);

            if(y_loc>-(Constants.FVT_Halfstrips*Constants.FVT_Pitch/2.) && y_loc < (Constants.FVT_Halfstrips*Constants.FVT_Pitch/2.)){ 
              if (x_loc<=0) closestStrip = (int) (Math.floor(((Constants.FVT_Halfstrips*Constants.FVT_Pitch/2.)-y_loc)/Constants.FVT_Pitch) + 1 );
              if (x_loc>0) closestStrip =  (int) ((Math.floor((y_loc+(Constants.FVT_Halfstrips*Constants.FVT_Pitch/2.))/Constants.FVT_Pitch) + 1 ) + Constants.FVT_Halfstrips +0.5*( Constants.FVT_Nstrips-2.*Constants.FVT_Halfstrips));
            }
            else if(y_loc <= -(Constants.FVT_Halfstrips*Constants.FVT_Pitch/2.) && y_loc > -Constants.FVT_Rmax){ 
              closestStrip =  (int) (Math.floor(((Constants.FVT_Halfstrips*Constants.FVT_Pitch/2.)-y_loc)/Constants.FVT_Pitch) +1 ); 
            }
            else if(y_loc >= (Constants.FVT_Halfstrips*Constants.FVT_Pitch/2.) && y_loc < Constants.FVT_Rmax){ 
              closestStrip = (int) (Math.floor((y_loc+(Constants.FVT_Halfstrips*Constants.FVT_Pitch/2.))/Constants.FVT_Pitch) + 1 + Constants.FVT_Halfstrips+0.5*( Constants.FVT_Nstrips-2.*Constants.FVT_Halfstrips));  
            }
        } 
        return closestStrip;
    }
    
    public double getWeightEstimate(int strip, int layer, double x, double y) {
        double sigmaDrift = 0.01;
        double strip_y = Constants.FVT_stripsYlocref[strip-1];
        double strip_x = Constants.FVT_stripsXlocref[strip-1];
     
        
        double strip_length = Constants.FVT_stripslength[strip-1];
        double sigma = sigmaDrift*Constants.hDrift;
        double wght=(Erf.erf((strip_y+Constants.FVT_Pitch/2.-y)/sigma/Math.sqrt(2))-Erf.erf((strip_y-Constants.FVT_Pitch/2.-y)/sigma/Math.sqrt(2)))*(Erf.erf((strip_x+strip_length/2.-x)/sigma/Math.sqrt(2))-Erf.erf((strip_x-strip_length/2.-x)/sigma/Math.sqrt(2)))/2./2.;
        if (wght<0) wght=-wght;
        return wght;
    }
    
    public double getCentroidEstimate(int layer, double x, double y) {
        if(this.getClosestStrip(x, y, layer)>1) {
            return Constants.FVT_stripsYlocref[this.getClosestStrip(x, y, layer)-1];
        } else {
            return y*Math.cos(Constants.FVT_Alpha[layer-1])- x*Math.sin(Constants.FVT_Alpha[layer-1]);

        }
    
    }
    public double[] H(StateVec stateVec, StateVecs sv) {
        /*
       // double Zk = this.measurements.get(stateVec.k).z;
        StateVec SVplus = null;// = new StateVec(stateVec.k);
        StateVec SVminus = null;// = new StateVec(stateVec.k);

        double delta_d_x = 7.449126e-03;
        SVplus = this.reset(SVplus, stateVec, sv);
        SVminus = this.reset(SVminus, stateVec, sv);

        SVplus.x = stateVec.x + delta_d_x / 2.; 
        SVminus.x = stateVec.x - delta_d_x / 2.;

        double delta_m_dx = (h(SVplus) - h(SVminus)) / delta_d_x;
        
        double delta_d_y = 9.019044e-02;
        SVplus = this.reset(SVplus, stateVec, sv);
        SVminus = this.reset(SVminus, stateVec, sv);

        SVplus.y = stateVec.y + delta_d_y / 2.; 
        SVminus.y = stateVec.y - delta_d_y / 2.;

        double delta_m_dy = (h(SVplus) - h(SVminus)) / delta_d_y;
        //int layer = this.measurements.get(stateVec.k).layer;
        //delta_m_dy = Math.cos(Constants.FVT_Alpha[layer-1]);
        //delta_m_dx = - Math.sin(Constants.FVT_Alpha[layer-1]);
        //System.out.println("* delta_m_dx "+delta_m_dx+" delta_m_dy "+delta_m_dy);
        
        double delta_d_tx = 7.159996e-04;
        SVplus = this.reset(SVplus, stateVec, sv);
        SVminus = this.reset(SVminus, stateVec, sv);

        SVplus.tx = stateVec.tx + delta_d_tx / 2.;
        SVminus.tx = stateVec.tx - delta_d_tx / 2.;

        double delta_m_dtx = (h(SVplus) - h(SVminus)) / delta_d_tx;

        double delta_d_ty = 7.169160e-04;
        SVplus = this.reset(SVplus, stateVec, sv);
        SVminus = this.reset(SVminus, stateVec, sv);

        SVplus.ty = stateVec.ty + delta_d_ty / 2.;
        SVminus.ty = stateVec.ty - delta_d_ty / 2.;

        double delta_m_dty = (h(SVplus) - h(SVminus)) / delta_d_ty;
        
        double delta_d_Q = 4.809686e-04;
        SVplus = this.reset(SVplus, stateVec, sv);
        SVminus = this.reset(SVminus, stateVec, sv);

        SVplus.Q = stateVec.Q + delta_d_Q / 2.;
        SVminus.Q = stateVec.Q - delta_d_Q / 2.;

        double delta_m_dQ = (h(SVplus) - h(SVminus)) / delta_d_Q;
       
        
        double[] H = new double[]{delta_m_dx, delta_m_dy, delta_m_dtx, delta_m_dty, delta_m_dQ};
        */
        int layer = this.measurements.get(stateVec.k).layer;
        
        //return this.getCentroidEstimate(layer, stateVec.x, stateVec.y);
        //return (double) this.getClosestStrip(stateVec.x, stateVec.y, layer);
        double[] H = new double[]{-Math.sin(Constants.FVT_Alpha[layer-1]), Math.cos(Constants.FVT_Alpha[layer-1]), 0, 0, 0};
        //for(int i = 0; i<H.length; i++)
        //    System.out.println("H["+i+"] = "+H[i]);
        
        return H;
        
    }

    private StateVec reset(StateVec SVplus, StateVec stateVec, StateVecs sv) {
        SVplus = sv.new StateVec(stateVec.k);
        SVplus.x = stateVec.x;
        SVplus.y = stateVec.y;
        SVplus.z = stateVec.z;
        SVplus.tx = stateVec.tx;
        SVplus.ty = stateVec.ty;
        SVplus.Q = stateVec.Q;

        return SVplus;
    }

}
