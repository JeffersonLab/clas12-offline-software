package org.jlab.rec.rich;

import java.util.ArrayList;
import org.jlab.geom.prim.Point3D;

public class RICHCluster extends ArrayList<RICHHit> {

    /**
     * A cluster in the RICH consists of an array of anodes in one PMT
     */

    private int clusid;    // cluster ID
    private int signal;    // pointer to the derived RICH signal 
    
    // constructor
    // ----------------
    public RICHCluster(int cluid) {
    // ----------------
          this.set_id(cluid);
    }

    // ----------------
    public int get_id() { return this.clusid; }
    // ----------------

    // ----------------
    public void set_id(int cluid) { this.clusid = cluid; }
    // ----------------

    // ----------------
    public int get_signal() { return this.signal; }
    // ----------------

    // ----------------
    public void set_signal(int signal) { this.signal = signal; }
    // ----------------


    // ----------------
    public int get_size() {
    // ----------------
          // return number of anodes in cluster
          return this.size();
    }

    // ----------------
    public double get_charge() {
    // ----------------
          // return measured charge
          double clusterEnergy = 0;
          for(int i=0; i<this.size(); i++) {
              clusterEnergy += this.get(i).get_duration();
          }
          return clusterEnergy;
    }


   // ----------------
    public int get_iMax() {
    // ----------------
    // return anode with maximum charge

        int    imax      = 0;
        double maxcharge = 0;
        for(int i=0; i<this.size(); i++) {
            if(this.get(i).get_duration()>maxcharge){
                imax=i;
                maxcharge=this.get(i).get_duration();
            }
        }
        return imax;
    }


    // ----------------
    //public int get_MaxAnode() {
    // ----------------
    // return anode with maximum charge

      /*  int    imax      = 0;
        double maxcharge = 0;
        for(int i=0; i<this.size(); i++) {
            if(this.get(i).get_duration()>maxcharge){
                imax=i;
                maxcharge=this.get(i).get_duration();
            }
        }
        return this.get(imax).get_anode();
    }


    // ----------------
    public int get_MaxPMT() {
    // ----------------
    // return anode with maximum charge

        int    imax      = 0;
        double maxcharge = 0;
        for(int i=0; i<this.size(); i++) {
            if(this.get(i).get_duration()>maxcharge){
                imax=i;
                maxcharge=this.get(i).get_duration();
            }
        }
        return this.get(imax).get_pmt();
    }*/


    // ----------------
    public double get_time() {
    // ----------------
          return this.get(0).get_Time();
    }

    // ----------------
    public double get_rawtime() {
    // ----------------
          return this.get(0).get_rawtime();
    }

  
    // ----------------
    public int get_glx() {
    // ----------------
        // returns glx coordinate of first hit
        return this.get(0).get_glx();
    }

    // ----------------
      public int get_gly() {
    // ----------------
        // returns gly coordinate of first hit
        return this.get(0).get_gly();
    }

    // ----------------
    public double get_x() {
    // ----------------
        // returns x coordinate of first hit
        return this.get(0).get_x();
    }

    // ----------------
      public double get_y() {
    // ----------------
        // returns y coordinate of first hit
        return this.get(0).get_y();
    }

    // ----------------
     public double get_z() {
    // ----------------
        // returns z coordinate of first hit

        if(RICHConstants.COSMIC_RUN==0){
            return this.get(0).get_z();

      }else{
            if(this.get(0).get_tile()<139){
                return 0;
            }else{
                return  RICHConstants.COSMIC_TRACKING_Z;
            }
        }
     }

    // ----------------
    public double get_wtime() {
    // ----------------
        // returns charge weighted time 
        double clusterEnergy  = this.get_charge();
        double clusterTime    = 0;
        for(int i=0; i<this.size(); i++) {
            RICHHit hit = this.get(i);
            clusterTime += hit.get_duration()*hit.get_Time();              
        }
        clusterTime /= clusterEnergy;
        return clusterTime;
      }
        

    // ----------------
    public Point3D getCentroid() {
    // ----------------
        // returns charge weighted centroid
        double clusc = this.get_charge();
        double wtot       = 0;
        double clusx   = 0;
        double clusy   = 0;
        double clusz   = 0;

        if(RICHConstants.COSMIC_RUN==1 && this.get(0).get_tile()>138){
            clusz   = RICHConstants.COSMIC_TRACKING_Z;
        }

        for(int i=0; i<this.size(); i++) {
            RICHHit hit = this.get(i);
            double wi = hit.get_duration();
            wtot     += wi;
            clusx += wi*hit.get_x();
            clusy += wi*hit.get_y();
            clusz += wi*hit.get_z();
        }

	if(wtot>0){
            clusx /= wtot;
            clusy /= wtot;
            clusz /= wtot;
	}

        Point3D centroid  = new Point3D(clusx,clusy,clusz);
        return centroid;            

    }    


    // ----------------
    public double get_wx() {
    // ----------------
        // returns X coordinate of weigthed centroid
        return this.getCentroid().x();
    }

    // ----------------
    public double get_wy() {
    // ----------------
        // returns Y coordinate of weigthed centroid
        return this.getCentroid().y();           
    }
       
    // ----------------
    public double get_wz() {
    // ----------------
        // returns Z coordinate of weigthed centroid
        return this.getCentroid().z();           
    }

    /*
    // ----------------
    public double getX2() {
    // ----------------
        double clusEnergy = this.getEnergy();
        double wtot       = 0;
        double clusterXX   = 0;
        for(int i=0; i<this.size(); i++) {
            RICHHit hit = this.get(i);
            // the moments: this are calculated in a second loop because log weighting requires clusEnergy to be known
            // double wi = hit_in_clus.get_Edep();    // de-comment for arithmetic weighting
            double wi = Math.max(0., (3.45+Math.log(hit.get_Edep()/clusEnergy)));
            wtot     += wi;
            clusterXX += wi*hit.get_Dx()*hit.get_Dx();
        }
        clusterXX /= wtot;
        return clusterXX;
    }


    // ----------------
      public double getY2() {
    // ----------------
            double clusEnergy = this.getEnergy();
            double wtot       = 0;
            double clusterYY   = 0;
            for(int i=0; i<this.size(); i++) {
                RICHHit hit = this.get(i);
                // the moments: this are calculated in a second loop because log weighting requires clusEnergy to be known
//                        double wi = hit_in_clus.get_Edep();    // de-comment for arithmetic weighting
                double wi = Math.max(0., (3.45+Math.log(hit.get_Edep()/clusEnergy)));
                wtot     += wi;
                clusterYY += wi*hit.get_Dy()*hit.get_Dy();
            }
            clusterYY /= wtot;
            return clusterYY;
    }


    // ----------------
    public double getWidthX() {
    // ----------------
            double sigmaX = Math.sqrt(this.getX2() - Math.pow(this.getX(),2.)); 
            return sigmaX;
    }


    // ----------------
    public double getWidthY() {
    // ----------------
            double sigmaY = Math.sqrt(this.getY2() - Math.pow(this.getY(),2.)); 
            return sigmaY;
      }


    // ----------------
    public double getRadius() {
    // ----------------
            double radius = Math.sqrt(this.getX2() - Math.pow(this.getX(),2.) + this.getY2() - Math.pow(this.getY(),2.));
            return radius;
      }

      public double getTheta() {
            double theta = Math.toDegrees(Math.atan(Math.sqrt(Math.pow(this.getX(),2.)+Math.pow(this.getY(),2.))/this.getZ()));
            return theta;
      }

      public double getPhi() {
            double phi = Math.toDegrees(Math.atan2(this.getY(),this.getX()));
            return phi;
      }
      */


    // ----------------
    public boolean isgoodCluster() {
    // ----------------
        if(Math.abs(this.get_time() - RICHConstants.EVENT_TIME) < RICHConstants.CLUSTER_TIME_WINDOW &&
            this.get_size() >= RICHConstants.CLUSTER_MIN_SIZE  && 
            this.get_charge() >= RICHConstants.CLUSTER_MIN_CHARGE) {
            return true;
        } else {
            return false;
        }

    }      
      

    // ----------------
    public void merge(RICHCluster clu) {
    // ----------------

        for(int i=0; i<clu.size(); i++){
            RICHHit rhit = clu.get(i);
            rhit.set_cluster(this.get_id());
            int already = 0;
            for (int j=0; j<this.size(); j++){ 
                if(rhit.get_id() == this.get(j).get_id())already=1;
            }
            if(already==0)this.add(rhit);
        }

    }

    // ----------------
    public boolean containsHit(RICHHit hit) {
    // ----------------
        // checks if the hit belongs to any nonet around its already associated hits

        boolean addFlag = false;
        if(this.get(0).get_pmt()!=hit.get_pmt())return addFlag;

        for(int j = 0; j< this.size(); j++) {
            double tDiff = Math.abs(hit.get_Time() - this.get(j).get_Time());
            int xDiff = Math.abs(hit.get_idx()  - this.get(j).get_idx());
            int yDiff = Math.abs(hit.get_idy()  - this.get(j).get_idy());
            if(tDiff <= RICHConstants.CLUSTER_TIME_WINDOW && xDiff <= 1 && yDiff <= 1 && (xDiff + yDiff) >0) addFlag = true;
        }
        return addFlag;
    }


    // ----------------
    public int compareTo(RICHCluster ocluster) {
    // ----------------
        //System.out.println(" --> comp "+this.get_channel()+" "+this.get_charge()+" "+ocluster.get_channel()+" "+ocluster.get_charge());
        if(this.get_charge() == ocluster.get_charge())return 0;
        if(this.get_charge() > ocluster.get_charge()){
            return 1;
        }else{
            return -1;
        }
    }

       
    // ----------------
    public void showCluster() {
    // ----------------
        System.out.format("Cluster ID %3d  PMT %4d  Siz %4d  Ch %7.1f  T %7.1f  raw %7.1f  wT %7.1f  glxy %4d %4d  XYZ %7.2f %7.2f %7.2f  wXYZ %7.2f %7.2f %7.2f \n",
            this.clusid,  
            this.get(0).get_pmt(),
            this.get_size(),
            this.get_charge(),
            this.get_time(), this.get_rawtime(), this.get_wtime(),
            this.get_glx(), this.get_gly(),
            this.get_x(), this.get_y(), this.get_z(),
            this.get_wx(), this.get_wy(), this.get_wz());
            for(int j = 0; j< this.size(); j++) {
                System.out.format("  --> hit # %3d  ID %3d  idxy %3d %3d  dur %4d \n",
                j, this.get(j).get_id(), 
                this.get(j).get_idx(), this.get(j).get_idy(), this.get(j).get_duration());
            }
        }
    
}
