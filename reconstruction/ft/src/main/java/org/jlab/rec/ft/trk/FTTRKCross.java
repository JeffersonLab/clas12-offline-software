/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.ft.trk;

import java.util.ArrayList;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

/**
 * The crosses are objects used to find tracks and are characterized by a 3-D point and a direction unit vector.
 * @author ziegler
 * @author devita
 *
 */
public class FTTRKCross extends ArrayList<FTTRKCluster> implements Comparable<FTTRKCross> {

	/**
	 * serial id
	 */
	private static final long serialVersionUID = 5317526429163382618L;

	/**
	 * 
	 * @param sector the sector (1)
	 * @param region the region (1...4)
	 * @param rid the cross ID (if there are only 2 crosses in the event, the ID corresponds to the region index)
	 */
	public FTTRKCross(int sector, int region, int rid) {
		this._Sector = sector;
		this._Region = region;
		this._Id = rid;
	}
	
	private int _Sector;      							//	    sector[1...6]
	private int _Region;    		 					//	    region [1,...3]
	private int _Id;									//		cross Id

	// point parameters:
	private Point3D _Point;
	private Point3D _PointErr;

	private Vector3D _Dir;
	private Vector3D _DirErr;
	
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
	public void set_Sector(int _Sector) {
		this._Sector = _Sector;
	}

	/**
	 * 
	 * @return the region of the cross
	 */
	public int get_Region() {
		return _Region;
	}

	/**
	 * Sets the region
	 * @param _Region  the region of the cross
	 */
	public void set_Region(int _Region) {
		this._Region = _Region;
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
	public void set_Id(int _Id) {
		this._Id = _Id;
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
	public void set_Point(Point3D _Point) {
		this._Point = _Point;
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
	public void set_PointErr(Point3D _PointErr) {
		this._PointErr = _PointErr;
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
	public void set_Dir(Vector3D _Dir) {
		this._Dir = _Dir;
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
	public void set_DirErr(Vector3D _DirErr) {
		this._DirErr = _DirErr;
	}

	/**
	 * 
	 * @return serialVersionUID
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
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

	private FTTRKCluster _clus1;
	private FTTRKCluster _clus2;
	
	/**
	 * Set the first cluster (corresponding to the first superlayer in a region)
	 * @param seg1 the Cluster (in the first superlayer) which is used to make a cross
	 */
	public void set_Cluster1(FTTRKCluster seg1) {
		this._clus1 = seg1;
	}

	/**
	 * Set the second Cluster (corresponding to the second superlayer in a region)
	 * @param seg2 the Cluster (in the second superlayer) which is used to make a cross
	 */
	public void set_Cluster2(FTTRKCluster seg2) {
		this._clus2 = seg2;
	}

	/**
	 * 
	 * @return he Cluster (in the first superlayer) which is used to make a cross
	 */
	public FTTRKCluster get_Cluster1() {
		return _clus1;
	}
	
	/**
	 * 
	 * @return the Cluster (in the second superlayer) which is used to make a cross
	 */
	public FTTRKCluster get_Cluster2() {
		return _clus2;
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
                
		this.set_Point(l_in.distance(l_out).midpoint());		
		
	}

	
	
	public void set_AssociatedElementsIDs() {
		
		for(FTTRKCluster cluster : this) {
			cluster.set_AssociatedCrossID(this._Id);
			
			for(FTTRKHit hit : cluster) {
				hit.set_ClusterIndex(cluster.get_Id());
				hit.set_CrossIndex(this._Id);
				
			}
		}
		
	}
	
	/**
	 * 
	 * @return the track info.
	 */
	public String printInfo() {
		String s = "fmt cross: ID "+this.get_Id()+" Sector "+this.get_Sector()+" Region "+this.get_Region()
				+" Point "+this.get_Point().toString();
		return s;
	}

	
    
}
