package org.jlab.rec.dc.cluster;

import java.util.Collections;
import java.util.List;

import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.dc.GeometryLoader;
import org.jlab.rec.dc.hit.FittedHit;

import trackfitter.fitter.LineFitPars;
import trackfitter.fitter.LineFitter;

public class ClusterFitter {

	public ClusterFitter() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Fits a cluster to a line
	 *
	 */
	private LineFitPars  FitPars;
	private double[][] FitArray ;
	private String CoordinateSystem; // LC= local, TSC = tilted Sector
	
	public void SetFitArray(FittedCluster clus, String system){
		
		Collections.sort(clus);
		
		double[][] fitArray = new double[4][clus.size()];
		double[] x = new double[clus.size()];
		double[] y = new double[clus.size()];
		double[] ex = new double[clus.size()];
		double[] ey = new double[clus.size()];
		
		for(int i = 0; i < clus.size(); i++) {
			if(system.equals("LC")) {
				CoordinateSystem="LC"; // local coordinate grid Delta_z = 1
				x[i] = clus.get(i).get_lX();
				ex[i] = 0;
				y[i] = clus.get(i).get_lY();
				ey[i] = 1;
			} 
			if(system.equals("TSC")) {
				CoordinateSystem="TSC"; // local tilted coordinate system Delta_z ~ cell size
				x[i] = clus.get(i).get_Z();
				ex[i] = 0;
				y[i] = clus.get(i).get_X();
				ey[i]= clus.get(i).get_DocaErr(); 
			}
			
			
		}
		fitArray[0] = x;
		fitArray[1] = ex;
		fitArray[2] = y;
		fitArray[3] = ey;
		
		this.FitArray = fitArray;
	}
	
	public void Fit(FittedCluster clus, boolean SaveFitPars) {
		
		if(FitArray!=null) {
		
			LineFitter linefit = new LineFitter();
			boolean linefitstatusOK = linefit.fitStatus(FitArray[0], FitArray[2], FitArray[1], FitArray[3], FitArray[0].length);
			
			if(linefitstatusOK)		
		      //  Get the results of the fits
				FitPars = linefit.getFit();
			if(SaveFitPars)
				this.SetClusterFitParameters(clus);
		} else {
			//System.err.println("Cluster Fit array not set!!!");
		}
	}
	
	public void SetClusterFitParameters(FittedCluster clus) {
		if(FitPars!=null ) {
			
			clus.set_clusterLineFitSlope(FitPars.slope()); 
	        clus.set_clusterLineFitSlopeErr(FitPars.slopeErr());
	        clus.set_clusterLineFitIntercept(FitPars.intercept());
	        clus.set_clusterLineFitInterceptErr(FitPars.interceptErr());
	        clus.set_fitProb(FitPars.getProb());
	        clus.set_Chisq(FitPars.chisq());
			
		} else {
			//System.err.println("Cluster Fit Params not set!!!");
		}
	}
	
	public void SetSegmentLineParameters(double x0, FittedCluster clus) {
		
		if(FitPars!=null ) {
        
        	Point3D pointOnLine = get_PointOnLine(x0, FitPars.slope(), FitPars.intercept());
        	Point3D dirOfLine = get_DirOnLine(FitPars.slope(), FitPars.intercept());       
        	   
	        Point3D pointOnLineErr = get_PointOnLine(x0, FitPars.slopeErr(), FitPars.interceptErr());
	        Point3D dirOfLineErr = get_DirOnLine(FitPars.slopeErr(), FitPars.interceptErr());     

        	clus.set_clusLine(new Line3D(pointOnLine, dirOfLine));
	        clus.set_clusLineErr(new Line3D(pointOnLineErr, dirOfLineErr));	        
	        
	        clus.set_clusterLineFitSlopeMP(FitPars.slope());
	        clus.set_clusterLineFitSlopeErrMP(FitPars.slopeErr());
	        clus.set_clusterLineFitInterceptMP(FitPars.intercept());
	        clus.set_clusterLineFitInterceptErrMP(FitPars.interceptErr());	        
	        
		} else {
			//System.err.println("Cluster Fit Params not set!!!");
			
		}
	}
	
	public void SetResidualDerivedParams(FittedCluster clus, boolean calcTimeResidual, boolean resetLRAmbig) {
		
		if(FitPars==null || FitArray==null)
			return;

		int[][] statusArray = new int[3][6]; // 6 = nb of layers, 3 number of criteria
		int[] nHitsInLyr = new int[6];
		int[] nLRresolvedInLyr = new int[6];
		int[] nPassingResAccCut = new int[6];

		for(int i = 0; i < clus.size(); i++) {
			nHitsInLyr[clus.get(i).get_Layer()-1]++;
			
	    	double residual = (FitArray[2][i]-FitPars.slope()*FitArray[0][i]-FitPars.intercept());
	    	clus.get(i).set_Residual(residual); 
	    	//clus.get(i).set_ClusFitDoca(FitPars.slope()*FitArray[0][i]+FitPars.intercept());
	    	double xWire = GeometryLoader.dcDetector.getSector(0).getSuperlayer(clus.get(i).get_Superlayer()-1).getLayer(clus.get(i).get_Layer()-1).getComponent(clus.get(i).get_Wire()-1).getMidpoint().x();
	    	double zWire = GeometryLoader.dcDetector.getSector(0).getSuperlayer(clus.get(i).get_Superlayer()-1).getLayer(clus.get(i).get_Layer()-1).getComponent(clus.get(i).get_Wire()-1).getMidpoint().z();
	    	
	    	
	    	Line3D FitLine = new Line3D();
	    	Point3D pointOnTrk = new Point3D(FitArray[0][0], FitPars.slope()*FitArray[0][0]+FitPars.intercept(), 0);
	    	Vector3D trkDir = new Vector3D(1, FitPars.slope(), 0);
	    	trkDir.unit();
	    	FitLine.set(pointOnTrk, trkDir);
	    	Point3D Wire = new Point3D(zWire, xWire,0);
	    	
	    	
	    	//double trkDocaMP = -xWire + (FitPars.slope()*FitArray[0][i]+FitPars.intercept());
	    	double trkDocaMP = FitLine.distance(Wire).length();
	    	double trkDoca = trkDocaMP*Math.cos(Math.toRadians(6.));
	    	
	    	clus.get(i).set_ClusFitDoca(trkDoca);

	    	
	    	//
	    	if(Math.abs(residual)<0.350) // less than the average resolution
	    		nPassingResAccCut[clus.get(i).get_Layer()-1]++;
	    	
	    	if(clus.get(i).get_LeftRightAmb()==0) {
		    	if(residual<0) {
					clus.get(i).set_LeftRightAmb(1);
				}
				if(residual>0) {
					clus.get(i).set_LeftRightAmb(-1);
				}
	    	}
			if(resetLRAmbig) {
				if( (CoordinateSystem.equals("LC") && Math.abs(residual)<0.01 ) ||( CoordinateSystem.equals("LTS") &&
						 clus.get(i).get_Doca()/clus.get(i).get_CellSize() < 0.4)  ) { //  DOCA require to be larger than 40% of cell size for hit-based tracking LR assignment 
						clus.get(i).set_LeftRightAmb(0);
					}
			}
			if(clus.get(i).get_LeftRightAmb()!=0)
				nLRresolvedInLyr[clus.get(i).get_Layer()-1]++;
			
	    	if(calcTimeResidual==true) {		        	
	    		double timeResidual = Math.abs(FitPars.slope()*FitArray[0][i]+FitPars.intercept()) - Math.abs(FitArray[2][i]);    		
	    		clus.get(i).set_TimeResidual(timeResidual);
	    	}
	    }
		for(int i =0; i<6; i++) {
			statusArray[0][i]=nHitsInLyr[i];
			if(nHitsInLyr[i]>0) {
				statusArray[1][i]=nLRresolvedInLyr[i]/nHitsInLyr[i];
				statusArray[2][i]=nPassingResAccCut[i]/nHitsInLyr[i];
			}
		}

		clus.set_Status(statusArray);
	}
	
	public FittedCluster BestClusterSelector(List<FittedCluster>clusters, String system) {
		//init
		FittedCluster BestCluster = null;
		double bestChisq = 999999999.;
	//	double bestClusx0=0;
		
		for(FittedCluster clusCand : clusters) {
			SetFitArray(clusCand,  system); // set the array of measurements according to the system used in the analysis
			// do the fit and get the chisq
			Fit(clusCand, true) ;
			if(FitPars==null)
				continue;
			double chisq = FitPars.chisq(); 

			if(chisq<bestChisq) {
				bestChisq = chisq;
				BestCluster = clusCand;
		//		bestClusx0 = FitArray[0][0];
			}
		}
		//SetSegmentLineParameters(bestClusx0, BestCluster) ;
	
		return BestCluster;
		
	}
	
	/**
	 * 
	 * @param the_slope the cluster fitted-line slope
	 * @param the_interc the fitted-line intercept
	 * @return the cluster fitted-line unit direction vector
	 */
	private Point3D get_DirOnLine(double the_slope, double the_interc) {
		return new Point3D(the_slope/Math.sqrt(1.+the_slope*the_slope),0,1./Math.sqrt(1.+the_slope*the_slope));
	}
	
	/**
	 * 
	 * @param d the point z coordinate
	 * @param the_slope the cluster fitted-line slope
	 * @param the_interc the fitted-line intercept
	 * @return  point (the_slope*d+the_interc,0,d) on the fitted cluster line
	 */
	private Point3D get_PointOnLine(double d, double the_slope,
			double the_interc) {
		
		return new Point3D(the_slope*d+the_interc,0,d);
	}
	
	
}
