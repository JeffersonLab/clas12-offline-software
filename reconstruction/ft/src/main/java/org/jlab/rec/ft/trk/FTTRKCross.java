/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.ft.trk;

import org.jlab.detector.base.DetectorLayer;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.ft.FTConstants;

/**
 * The crosses are objects used to find tracks and are characterized by a 3-D point and a direction unit vector.
 * @author ziegler
 * @author devita
 *
 */
//public class FTTRKCross extends ArrayList<FTTRKCluster> implements Comparable<FTTRKCross> {
public class FTTRKCross implements Comparable<FTTRKCross> {
	/**
	 * serial id
	 */
	private static final long serialVersionUID = 5317526429163382619L;

	private int _Sector;      							//	    sector[1]
	private int _Region;    		 					//	    region [1,2]
	private int _Id;							        //	    cross Id [0,1]
        private int _trkId;                                                             // cross index for tracking
        
	// point parameters:
	private Point3D _Point;
	private Point3D _PointErr;

	private Vector3D _Dir;
	private Vector3D _DirErr;

        private FTTRKCluster _clus1;
	private FTTRKCluster _clus2;
        
        private float _Energy;
        private float _Time;
        
        
        
        /**
	 * 
	 * @param sector the sector (1)
	 * @param region the region (1-2)
	 * @param rid the cross ID (if there are only 2 crosses in the event, the ID corresponds to the region index)
	 */
	public FTTRKCross(int sector, int region, int rid) {
		this._Sector = sector;
		this._Region = region;
		this._Id = rid;      // sequential number
                this._trkId = rid; 
	}
	
	/**
	 * 
	 * @return the sector of the cross
	 */
	public int get_Sector() {
		return _Sector;
	}

	/**
	 * Sets the sector
	 * @param _Sector  the sector of the cross
	 */
	public void set_Sector(int sector) {
		this._Sector = sector;
	}

	/**
	 * 
	 * @return the region of the cross
	 */
	public int get_Region() {
            // return the number of supermodule (top or bottom)
	    return _Region;
	}

	/**
	 * Sets the region
	 * @param _Region  the region of the cross
	 */
	public void set_Region(int region) {
		this._Region = region;
	}

	/**
	 * 
	 * @return the id of the cross
	 */
	public int get_Id() {
		return _Id;
	}

	/**
	 * Sets the cross ID
	 * @param _Id  the id of the cross
	 */
	public void set_Id(int id) {
		this._Id = id;
	}

	public int get_trkId() {
		return _trkId;
	}
        
	public void set_trkId(int trkid) {
		this._trkId = trkid;
	}
        
        

	/**
	 * 
	 * @return a 3-D point characterizing the position of the cross in the tilted coordinate system.
	 */
	public Point3D get_Point() {
		return _Point;
	}

	/**
	 * Sets the cross 3-D point 
	 * @param _Point  a 3-D point characterizing the position of the cross in the tilted coordinate system.
	 */
	public void set_Point(Point3D point) {
		this._Point = point;
	}

	/**
	 * 
	 * @return a 3-dimensional error on the 3-D point characterizing the position of the cross in the tilted coordinate system.
	 */
	public Point3D get_PointErr() {
		return _PointErr;
	}

	/**
	 * Sets a 3-dimensional error on the 3-D point 
	 * @param _PointErr a 3-dimensional error on the 3-D point characterizing the position of the cross in the tilted coordinate system.
	 */
	public void set_PointErr(Point3D pointErr) {
		this._PointErr = pointErr;
	}

	/**
	 * 
	 * @return the cross unit direction vector
	 */
	public Vector3D get_Dir() {
		return _Dir;
	}

	/**
	 * Sets the cross unit direction vector
	 * @param _Dir the cross unit direction vector
	 */
	public void set_Dir(Vector3D dir) {
		this._Dir = dir;
	}

	/**
	 * 
	 * @return the cross unit direction vector
	 */
	public Vector3D get_DirErr() {
		return _DirErr;
	}

	/**
	 * Sets the cross unit direction vector
	 * @param _DirErr the cross unit direction vector
	 */
	public void set_DirErr(Vector3D dirErr) {
		this._DirErr = dirErr;
	}

	/**
	 * 
	 * @return serialVersionUID
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

        public void set_Energy(float energy){
            this._Energy = energy;
        }
        
        public void set_Time(float time){
            this._Time = time;
        }
        
        public float get_Energy(){
            return this._Energy;
        }
        
        public float get_Time(){
            return this._Time;
        }
        
	/**
	 * Sorts crosses by azimuth angle values
	 */
	@Override
	public int compareTo(FTTRKCross arg) {
		
		if(this.get_Point().toVector3D().phi()<arg.get_Point().toVector3D().phi()) {
			return 1;
		} else {
			return -1;
		}
	}
	
	/**
	 * Set the first cluster (corresponding to the first layer in a region)
	 * @param seg1 the Cluster (in the first layer) which is used to make a cross
	 */
	public void set_Cluster1(FTTRKCluster seg1) {
		this._clus1 = seg1;
	}

	/**
	 * Set the second Cluster (corresponding to the second layer in a region)
	 * @param seg2 the Cluster (in the second layer) which is used to make a cross
	 */
	public void set_Cluster2(FTTRKCluster seg2) {
		this._clus2 = seg2;
	}

	/**
	 * 
	 * @return he Cluster (in the first layer) which is used to make a cross
	 */
	public FTTRKCluster get_Cluster1() {
		return _clus1;
	}
	
	/**
	 * 
	 * @return the Cluster (in the second layer) which is used to make a cross
	 */
	public FTTRKCluster get_Cluster2() {
		return _clus2;
	}

        public int getDetector() {
		return (_Region-1);
	}
        
        
	/**
	 * Sets the cross parameters: the position and direction unit vector
	 */
	public void set_CrossParams() {
		
		FTTRKCluster inlayerclus = this.get_Cluster1();
		FTTRKCluster outlayerclus = this.get_Cluster2();
		
		// Getting the cross position from the calculated centroid segment line positions 
		//----------------------------------------------------
		double x0_inner = inlayerclus.get_StripSegment().origin().x();
		double x1_inner = inlayerclus.get_StripSegment().end().x();
		double x0_outer = outlayerclus.get_StripSegment().origin().x();
		double x1_outer = outlayerclus.get_StripSegment().end().x();
		double y0_inner = inlayerclus.get_StripSegment().origin().y();
		double y1_inner = inlayerclus.get_StripSegment().end().y();
		double y0_outer = outlayerclus.get_StripSegment().origin().y();
		double y1_outer = outlayerclus.get_StripSegment().end().y();
		double z0_inner = inlayerclus.get_StripSegment().origin().z();		
		double z0_outer = outlayerclus.get_StripSegment().origin().z();
 
		Line3D l_in  = new Line3D(x0_inner, y0_inner, z0_inner, x1_inner, y1_inner, z0_inner);
                Line3D l_out = new Line3D(x0_outer, y0_outer, z0_outer, x1_outer, y1_outer, z0_outer);
                
                Point3D mid = l_in.distance(l_out).midpoint();
                double d = l_in.distance(mid).length();
                int region = inlayerclus.get_Region(); // 1-2
                double distanceBwLayers = FTTRKConstantsLoader.Zlayer[2*region-1] - FTTRKConstantsLoader.Zlayer[2*region-2];
                // check if the point belongs to the crossed segments 
                // set as tolerance the maximum uncertainty of the two centroids
                double maxTolerance = 0;
                double tol1 = inlayerclus.get_CentroidError(); 
                double tol2 = outlayerclus.get_CentroidError();
                double err1 = Math.abs(tol1*Math.cos(FTTRKConstantsLoader.Alpha[inlayerclus.get_Layer()-1]));
                double err2 = Math.abs(tol2*Math.cos(FTTRKConstantsLoader.Alpha[outlayerclus.get_Layer()-1]));
                if(err1>=err2){
                    maxTolerance = err1;
                }else{
                    maxTolerance = err2;
                }
                boolean isPointInSegment1 = false;
                // first one, inner
                if(x0_inner<=x1_inner){
                    if(mid.x()>=x0_inner-tol1 && mid.x()<=x1_inner+tol1){
                        if(y0_inner<=y1_inner){
                            if(mid.y()>=y0_inner-tol1 && mid.y()<=y1_inner+tol1) isPointInSegment1 = true;
                        }else{
                            if(mid.y()>=y1_inner-tol1 && mid.y()<=y0_inner+tol1) isPointInSegment1 = true;
                        }
                    }
                }else{
                    if(mid.x()<=x0_inner+tol1 && mid.x()>=x1_inner-tol1){
                        if(y0_inner<=y1_inner){
                            if(mid.y()>=y0_inner-tol1 && mid.y()<=y1_inner+tol1) isPointInSegment1 = true;
                        }else{
                            if(mid.y()>=y1_inner-tol1 && mid.y()<=y0_inner+tol1) isPointInSegment1 = true;
                        }
                    }
                }
                // second one, outer
                boolean isPointInSegment2 = false;
                if(x0_outer<=x1_outer){
                    if(mid.x()>=x0_outer-tol2 && mid.x()<=x1_outer+tol2){
                        if(y0_outer<=y1_outer){
                            if(mid.y()>=y0_outer-tol2 && mid.y()<=y1_outer+tol2) isPointInSegment2 = true;
                        }else{
                            if(mid.y()>=y1_outer-tol2 && mid.y()<=y0_outer+tol2) isPointInSegment2 = true;
                        }
                    }
                }else{
                    if(mid.x()<=x0_outer+tol2 && mid.x()>=x1_outer-tol2){
                        if(y0_outer<=y1_outer){
                            if(mid.y()>=y0_outer-tol2 && mid.y()<=y1_outer+tol2) isPointInSegment2 = true;
                        }else{
                            if(mid.y()>=y1_outer-tol2 && mid.y()<=y0_outer+tol2) isPointInSegment2 = true;
                        }
                    }
                }
                boolean isPointInSegments = isPointInSegment1&&isPointInSegment2;
                
                boolean isCluster1EnergyAboveTHR = (inlayerclus.get_TotalEnergy() > FTConstants.TRK_MIN_CLUS_ENERGY);
                boolean isCluster2EnergyAboveTHR = (outlayerclus.get_TotalEnergy() > FTConstants.TRK_MIN_CLUS_ENERGY);
                int regionDet1 = DetectorLayer.FTTRK_MODULE1;  // region of detector1: 1 
                if(d<distanceBwLayers && isPointInSegments && isCluster1EnergyAboveTHR && isCluster2EnergyAboveTHR){
                   this.set_Point(mid);
                   double thickness = 0; 
                   if(region==regionDet1){
                       thickness = (FTTRKConstantsLoader.Zlayer[DetectorLayer.FTTRK_LAYER2-1]-FTTRKConstantsLoader.Zlayer[DetectorLayer.FTTRK_LAYER1-1])/2.;
                   }else{
                       thickness = (FTTRKConstantsLoader.Zlayer[DetectorLayer.FTTRK_LAYER4-1]-FTTRKConstantsLoader.Zlayer[DetectorLayer.FTTRK_LAYER3-1])/2.;
                   }
                   Point3D error = new Point3D(err1, err2, thickness);
                   this.set_PointErr(error); 
                   this.set_AssociatedElementsIDs();  // associates cross number to the clusters
                }else{
                   Point3D error = new Point3D(-999., -999., -999); 
                   this.set_Point(error);
                   this.set_PointErr(error);
                }
                
                this.evaluate_EnergyAndTime();
                 
	}

	public void evaluate_EnergyAndTime(){
            FTTRKCluster cl1 = this._clus1; 
            FTTRKCluster cl2 = this._clus2;
            double meanEnergy = -9999.;
            double meanTime = -9999.;
            double timeCross1 = 0., timeCross2 = 0.;
            meanEnergy = Math.sqrt(cl1.get_TotalEnergy() * cl2.get_TotalEnergy());
            this.set_Energy((float)meanEnergy);
            
            // to determine the time associated with the cross, the times of each hit forming each cluster must be retrieved
            // loop on strips of each cluster
            int nHits1 = cl1.size();
            for(int i=0; i<nHits1; i++){
                timeCross1 += cl1.get(i).get_Time();
            }
            int nHits2 = cl2.size();
            for(int i=0; i<nHits2; i++){
                timeCross2 += cl2.get(i).get_Time();
            }
            // meantime value over all the strips
            if(nHits1 != 0){timeCross1 /= nHits1;}else{timeCross1 = 9999.;};
            if(nHits2 != 0){timeCross2 /= nHits2;}else{timeCross2 = 9999.;};
            meanTime = (timeCross1+timeCross2)/2.; 
            this.set_Time((float)meanTime);
        }
        
        
	
	public void set_AssociatedElementsIDs() {
            this._clus1.set_AssociatedCrossID(this._Id);
            this._clus2.set_AssociatedCrossID(this._Id);
            
	}
	
	/**
	 * 
	 * @return the track info.
	 */
	public String printInfo() {
		String s = "FT cross: ID "+this.get_Id()+" Sector "+this.get_Sector()+" Region "+this.get_Region()
				+" Point "+this.get_Point().toString();
		return s;
	}

	
    
}
