package org.jlab.rec.cvt.track.fit;

import java.util.ArrayList;


import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.track.Seed;
import org.jlab.rec.cvt.track.Track;
import org.jlab.rec.cvt.track.fit.StateVecs.B;
import org.jlab.rec.cvt.track.fit.StateVecs.StateVec;
import org.jlab.rec.cvt.trajectory.Helix;

import Jama.Matrix;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.base.DetectorType;

/*********
 * 
 * @author mdefurne
 *
 */

public class KFitter {

    public boolean setFitFailed = false;
    public boolean RunningAlgo = true;

    StateVecs sv = new StateVecs();
    MeasVecs mv = new MeasVecs();
    
    public Helix KFHelix;
    
    public int totNumIter =1;// 5;
    double newChisq = Double.POSITIVE_INFINITY;
    
    public double chi2 = 0;
    public int NDF = 0;
    
    //public ArrayList<HitOnTrack> TrjPoints = new ArrayList<HitOnTrack>();
    public ArrayList<StateVec> TrjPoints = new ArrayList<StateVec>();
    
    public KFitter(Seed trk, org.jlab.rec.cvt.svt.Geometry geo, Swim swimmer) {
        this.init(trk, geo, null, swimmer);
    }
    
    public KFitter(Seed trk, org.jlab.rec.cvt.svt.Geometry geo, DataEvent event, Swim swimmer) {
        this.init(trk, geo, event, swimmer);
    }
    
    public void init(Seed trk, org.jlab.rec.cvt.svt.Geometry geo, DataEvent event, Swim swimmer) {
        //Helix helix = trk.get_Helix();
        mv.setMeasVecs(trk, geo);
        if (sv.Layer != null) {
            sv.Layer.clear();
        } else {
            sv.Layer = new ArrayList<Integer>();
        }
        if (sv.Sector != null) {
            sv.Sector.clear();
        } else {
            sv.Sector = new ArrayList<Integer>();
        }
        if (sv.X0 != null) {
            sv.X0.clear();
        } else {
            sv.X0 = new ArrayList<Double>();
        }
        if (sv.Y0 != null) {
            sv.Y0.clear();
        } else {
            sv.Y0 = new ArrayList<Double>();
        }
        if (sv.Z0 != null) {
            sv.Z0.clear();
        } else {
            sv.Z0 = new ArrayList<Double>();
        }
        //take first plane along beam line with n = y-dir;
        sv.Layer.add(0);
        sv.Sector.add(0);
       
        sv.X0.add((double) 0.0);
        sv.Y0.add((double) 0.0);
        sv.Z0.add((double) 0.0);
        for (int i = 1; i < mv.measurements.size(); i++) {
            sv.Layer.add(mv.measurements.get(i).layer);
            sv.Sector.add(mv.measurements.get(i).sector);
            
            Point3D ref = new Point3D(0, 0, 0);
            sv.X0.add(ref.x());
            sv.Y0.add(ref.y());
            sv.Z0.add(ref.z());
        }
        sv.initAtBeam(trk, this, swimmer);
       
    }

    public void runFitter(org.jlab.rec.cvt.svt.Geometry sgeo, org.jlab.rec.cvt.bmt.Geometry bgeo, Swim swimmer) {
        this.NDF = -5; 
        this.chi2= Double.POSITIVE_INFINITY;
        
        if (!org.jlab.rec.cvt.Constants.FromTargetToCTOF) {
        	sv.initAtLastMeas(this, mv, sgeo, bgeo, swimmer);
        	RunningAlgo=this.filter(mv.measurements.get(mv.measurements.size()-1).k, sgeo, bgeo, swimmer);
        }
        
        if (mv.measurements.size()-6<org.jlab.rec.cvt.Constants.NDF_Min) RunningAlgo=false;
        	
        //Possible to do several iterations if wanted
        for (int it = 0; it < totNumIter; it++) {
        	            
            if (org.jlab.rec.cvt.Constants.FromTargetToCTOF) {
            	for (int k = 0; k < sv.X0.size() - 1; k++) {
            		if (!RunningAlgo) break;
            		sv.new_transport(k, k + 1, sv.trackTrajFilt.get(k), sv.trackCovFilt.get(k), sgeo, bgeo, mv.measurements.get(k + 1).type, swimmer);
            		RunningAlgo=this.filter(k + 1, sgeo, bgeo, swimmer);
            	}
            
            	if (!RunningAlgo) break;
            	sv.trackTrajFilt.get(sv.X0.size() - 1).pathlength=0; 
            	//During the smoothing, we compute the pathlength... Warning: Once we are at the vertex and knows the pathlength, 
            	//we will change it following the convention 0 at dca to beam.
            	sv.trackTrajSmooth.put(sv.X0.size() -1,sv.trackTrajFilt.get(sv.X0.size() - 1));
            	sv.trackCovSmooth.put(sv.X0.size() -1,sv.trackCovFilt.get(sv.X0.size() - 1));
                        
            	for (int k = sv.X0.size() - 2; k > 0; k--) {
            		if (!RunningAlgo) break;
            		RunningAlgo=this.smoother(k+1, k, sgeo, bgeo, swimmer);
            	}
            	
            }
            else {
            	for (int k = sv.X0.size()-1; k > 1; k--) {
            		if (!RunningAlgo) break;
            		sv.new_transport(k, k - 1, sv.trackTrajFilt.get(k), sv.trackCovFilt.get(k), sgeo, bgeo, mv.measurements.get(k - 1).type, swimmer);
            		RunningAlgo=this.filter(k - 1, sgeo, bgeo, swimmer);
            	}
            
            	if (!RunningAlgo) break;
            	sv.trackTrajFilt.get(mv.measurements.get(mv.measurements.size()-1).k).pathlength=0; 
            	//During the smoothing, we compute the pathlength... Warning: Once we are at the vertex and knows the pathlength, 
            	//we will change it following the convention 0 at dca to beam.
            	sv.trackTrajSmooth.put(1,sv.trackTrajFilt.get(1));
            	sv.trackCovSmooth.put(1,sv.trackCovFilt.get(1));
                        
            	for (int k = 2; k < sv.X0.size(); k++) {
            		if (!RunningAlgo) break;
            		RunningAlgo=this.smoother(k-1,k, sgeo, bgeo, swimmer);
            	}
            }
                   
                                 
        }
        // If filetring failed
        if (!RunningAlgo) {
    		this.chi2=Double.NaN;
    		this.NDF=-1;
    	}
        //If filtering and smoothing done properly, then we have an Helix and a trajectory
        if (RunningAlgo) {
        	//To extrapolate to "Vertex"
            sv.new_transport(1, 0, sv.trackTrajSmooth.get(1), sv.trackCovSmooth.get(1), sgeo, bgeo, mv.measurements.get(0).type, swimmer);
            this.chi2=this.getResiduals_Chi2(sgeo, bgeo, swimmer);
        	KFHelix = sv.setSvPars(sv.trackTrajSmooth.get(0),sv.trackCovSmooth.get(0).covMat);
        	this.setTrajectory(sgeo,bgeo);
        }
        
    }
    
    public void setTrajectory(org.jlab.rec.cvt.svt.Geometry sgeo, org.jlab.rec.cvt.bmt.Geometry bgeo) {
    	TrjPoints.clear();
    	double[] angle=new double[3];
        for (int k = 1; k < sv.trackTrajSmooth.size(); k++) {
        	if (sv.trackTrajSmooth.get(k).layer<7) {
        		sv.trackTrajSmooth.get(k).DetectorType=DetectorType.BST.getDetectorId();
        		angle=sgeo.ComputeAngles(sv.trackTrajSmooth.get(k).layer, sv.trackTrajSmooth.get(k).sector,
        				new Vector3D(sv.trackTrajSmooth.get(k).dirx, sv.trackTrajSmooth.get(k).diry,sv.trackTrajSmooth.get(k).dirz));
        	}
        	if (sv.trackTrajSmooth.get(k).layer>6) {
        		sv.trackTrajSmooth.get(k).DetectorType=DetectorType.BMT.getDetectorId();
        		angle=bgeo.ComputeAngles(new Vector3D(sv.trackTrajSmooth.get(k).xdet, sv.trackTrajSmooth.get(k).ydet,sv.trackTrajSmooth.get(k).zdet),
        				new Vector3D(sv.trackTrajSmooth.get(k).dirx, sv.trackTrajSmooth.get(k).diry,sv.trackTrajSmooth.get(k).dirz));
        		
        	}
        	sv.trackTrajSmooth.get(k).angle=angle[0];
        	sv.trackTrajSmooth.get(k).RTheta=angle[1];
        	sv.trackTrajSmooth.get(k).RZ=angle[2];
        	TrjPoints.add(sv.trackTrajSmooth.get(k));
        }
    }

    public void Rinit(Swim swimmer) {
        Helix helix = sv.setSvPars(sv.trackTrajSmooth.get(0),sv.trackCovSmooth.get(0).covMat);

        sv.trackTraj.get(0).refx = sv.trackTrajSmooth.get(0).refx;
        sv.trackTraj.get(0).refy = sv.trackTrajSmooth.get(0).refy;
        sv.trackTraj.get(0).refz = sv.trackTrajSmooth.get(0).refz;
       
        B Bf = sv.new B(0, 0, 0, 0, swimmer);
        sv.trackTraj.get(0).alpha = Bf.alpha;
        sv.trackTraj.get(0).kappa = Bf.alpha * helix.get_curvature();
       
        sv.trackTraj.get(0).phi0 = helix.get_phi_at_dca();
        sv.trackTraj.get(0).dz = helix.get_Z0();
        sv.trackTraj.get(0).tanL = helix.get_tandip();
        sv.trackTraj.get(0).d_rho = helix.get_dca();
        sv.trackTraj.get(0).phi = 0;

        sv.trackCov.get(0).covMat = sv.trackCovSmooth.get(0).covMat;
    }
    
    public Track OutputTrack(Seed trk, org.jlab.rec.cvt.svt.Geometry sgeo, org.jlab.rec.cvt.bmt.Geometry bgeo, Swim swimmer) {
   
        Track cand ;
        if (RunningAlgo) {
        	cand=new Track(KFHelix, swimmer);
        	
        	//Reassign correctly the pathlength in correct ordering
        	double Total_pathlength=sv.trackTrajSmooth.get(0).pathlength;
        	for (int i=0; i<sv.trackTrajSmooth.size();i++) {sv.trackTrajSmooth.get(i).pathlength=Total_pathlength-sv.trackTrajSmooth.get(i).pathlength;}
        
        	if(cand.get_Pt()<0.05)
        		this.setFitFailed = true;
      
        
        	cand.addAll(trk.get_Crosses());
        	cand.setTrajectory(TrjPoints);
        }
        else {
        	cand = new Track(null, swimmer);
        }
        cand.setNDF(NDF);
        cand.setChi2(chi2);
        return cand;
    }

    private double getResiduals_Chi2(org.jlab.rec.cvt.svt.Geometry sgeo, org.jlab.rec.cvt.bmt.Geometry bgeo, Swim swimmer) {
        //get the measurement
        double m = 0;
        //get the projector state... excl-suffix indicates that we have excluded from KF the site K
        double h = 0;
        double h_excl = 0;
        double excl_diff=0;
        double diff = 0;
        double chi2 =0;
        
        for(int k = 1; k< sv.X0.size(); k++) {
        	if (mv.measurements.get(k).FilteredOn) {
        		m=0;
        		h=0;
        		diff=mv.Residual(sv.trackTrajSmooth.get(k), sgeo);
        		excl_diff=0;
        		h_excl=0;
        		sv.trackTrajSmooth.get(k).clusID=mv.measurements.get(k).ID;
        		//SVT 
        		if (mv.measurements.get(k).type == 0) {
            	
        			double V = sgeo.getSingleStripResolution(mv.measurements.get(k).layer, (int) mv.measurements.get(k).centroid, 
        					sgeo.transformToFrame(mv.measurements.get(k).sector, mv.measurements.get(k).layer,sv.trackTrajSmooth.get(k).xdet, sv.trackTrajSmooth.get(k).ydet, sv.trackTrajSmooth.get(k).zdet , "local", "").z());
                
        			mv.measurements.get(k).error = V * V;
        		}
           
        		if (org.jlab.rec.cvt.Constants.ExcludingSite) excl_diff=mv.Residual(this.getStateVecExcludingSite(k,diff, sgeo, bgeo, swimmer),sgeo);//Important... V must be updated for SVT before calling getStateVecExcludingSite
                             
        		//Increment the chi2
        		chi2 += diff * diff / mv.measurements.get(k).error;
           
        		//Associate residual infos to the state vectors
        		sv.trackTrajSmooth.get(k).excl_residual=excl_diff;//m-this.ExcludingSite(k,diff,mv.measurements.get(k).error);
        		sv.trackTrajSmooth.get(k).residual=diff;
            
        		//Prefer to give a spatial residual for BMT Z
        		if (mv.measurements.get(k).type == 1) {
        			sv.trackTrajSmooth.get(k).residual=bgeo.getRadius(mv.measurements.get(k).layer-6)*diff;  
        			sv.trackTrajSmooth.get(k).excl_residual=bgeo.getRadius(mv.measurements.get(k).layer-6)*excl_diff;  
        		}
        	}
        }  
       return chi2;

    }
    
    private StateVec getStateVecExcludingSite(int k, double diff, org.jlab.rec.cvt.svt.Geometry sgeo, org.jlab.rec.cvt.bmt.Geometry bgeo, Swim swimmer) {
		    	
    	StateVec dummy=sv.new StateVec(k);
    	dummy.Duplicate(sv.trackTrajSmooth.get(k));
    	double V = mv.measurements.get(k).error;
        double[] H = new double[5];
        H = mv.H(dummy, sv, sgeo, bgeo, mv.measurements.get(k).type, swimmer);
        
        double[] K_star=new double[5];
        double coeff=0;
        for (int i=0;i<5;i++) {
        	K_star[i]=0;
        	for (int j=0;j<5;j++) {
        		K_star[i]+=sv.trackCovSmooth.get(k).covMat.get(i, j)*H[j];
        	}
        	coeff+=K_star[i]*H[i];
        }
        
        coeff=coeff-V;
        coeff=1/coeff;
        
        dummy.d_rho=dummy.d_rho+coeff*K_star[0]*diff;
        dummy.phi0=dummy.phi0+coeff*K_star[1]*diff;
        dummy.kappa=dummy.kappa+coeff*K_star[2]*diff;
        dummy.dz=dummy.dz+coeff*K_star[3]*diff;
        dummy.tanL=dummy.tanL+coeff*K_star[4]*diff;
        
        sv.getStateVecAtModule(dummy.k, dummy, sgeo, bgeo, mv.measurements.get(k).type, swimmer);
        
        return dummy;
	}

	
	private boolean filter(int k, org.jlab.rec.cvt.svt.Geometry sgeo, org.jlab.rec.cvt.bmt.Geometry bgeo, Swim swimmer) {

        if (sv.trackTraj.get(k) != null && sv.trackCov.get(k).covMat != null) {
        	
            double[] K = new double[5];
            double V = mv.measurements.get(k).error;

            //get the measurement
            double m = 0;
            //get the projector state
            double h = 0;
            //Difference m-h
            double delta = mv.Residual(sv.trackTraj.get(k), sgeo);
            if (mv.measurements.get(k).layer>3||(mv.measurements.get(k).layer<4&&delta<1)||(this.NDF<1)) {
            	if (mv.measurements.get(k).type == 0) {
            	                
            		V = sgeo.getSingleStripResolution(mv.measurements.get(k).layer, (int) mv.measurements.get(k).centroid, 
                		sgeo.transformToFrame(mv.measurements.get(k).sector, mv.measurements.get(k).layer,sv.trackTraj.get(k).xdet, sv.trackTraj.get(k).ydet, sv.trackTraj.get(k).zdet , "local", "").z());
            		V = V * V;
               
            	}
           
            	//get the projector Matrix
            	double[] H = new double[5];
            	H = mv.H(sv.trackTraj.get(k), sv, sgeo, bgeo, mv.measurements.get(k).type, swimmer);

            	double[][] HTGH = new double[][]{
            		{H[0] * H[0] / V, H[0] * H[1] / V, H[0] * H[2] / V, H[0] * H[3] / V, H[0] * H[4] / V},
            		{H[1] * H[0] / V, H[1] * H[1] / V, H[1] * H[2] / V, H[1] * H[3] / V, H[1] * H[4] / V},
            		{H[2] * H[0] / V, H[2] * H[1] / V, H[2] * H[2] / V, H[2] * H[3] / V, H[2] * H[4] / V},
            		{H[3] * H[0] / V, H[3] * H[1] / V, H[3] * H[2] / V, H[3] * H[3] / V, H[3] * H[4] / V},
            		{H[4] * H[0] / V, H[4] * H[1] / V, H[4] * H[2] / V, H[4] * H[3] / V, H[4] * H[4] / V}
            	};
            
            	Matrix Ci = null;
            	//this.printMatrix(new Matrix(HTGH));System.err.println("-------------------------------\n");
            	if (this.isNonsingular(sv.trackCov.get(k).covMat) == false) {
            		//System.err.println("Covariance Matrix is non-invertible - quit filter!");
            		return false;
            	}
            	try {
            		Ci = sv.trackCov.get(k).covMat.inverse();
            	} catch (Exception e) {
            		return false;
            	}
           
            	Matrix Ca = null;
            	try {
            		sv.MeasurementDerivative.put(k, new Matrix(HTGH));
            		Ca = Ci.plus(sv.MeasurementDerivative.get(k));
            	} catch (Exception e) {
            		return false;
            	}
            	if (Ca != null && this.isNonsingular(Ca) == false) {
            		//System.err.println("Covariance Matrix Ca is non-invertible - quit filter!");
            		return false;
            	}
            	if (Ca != null && this.isNonsingular(Ca) == true) {
            		if (Ca.inverse() != null) {
            			Matrix CaInv = Ca.inverse();
            			sv.trackCovFilt.get(k).covMat = CaInv;
            			//this.printMatrix(sv.trackCov.get(k).covMat);
            			//System.err.println("Error: e");
            		} else {
            			return false;
            		}
            	} else {
            		return false;
            	}
            
            	for (int j = 0; j < 5; j++) {
            		// the gain matrix
            		K[j] = 0;
            		for (int i = 0; i < 5; i++) {
            			K[j] += H[i] * sv.trackCovFilt.get(k).covMat.get(j, i) / V; //The multiplication with V is already taken into account in line 313
            		}
            	}
            
            	double drho_filt = sv.trackTraj.get(k).d_rho;
            	double phi0_filt = sv.trackTraj.get(k).phi0;
            	double kappa_filt = sv.trackTraj.get(k).kappa;
            	double dz_filt = sv.trackTraj.get(k).dz;
            	double tanL_filt = sv.trackTraj.get(k).tanL;
            
            	drho_filt += K[0] * delta;
            	phi0_filt += K[1] * delta;
            	kappa_filt += K[2] * delta;
            	dz_filt += K[3] * delta;
            	tanL_filt += K[4] * delta;
                     
            	//System.out.println(sv.Layer.get(k)+" "+delta+" "+H[0]+" "+H[1]+" "+H[2]+" "+H[3]+" "+H[4]);
            	sv.trackTrajFilt.get(k).d_rho = drho_filt;
            	sv.trackTrajFilt.get(k).phi0 = phi0_filt;
            	sv.trackTrajFilt.get(k).kappa = kappa_filt;
            	sv.trackTrajFilt.get(k).dz = dz_filt;
            	sv.trackTrajFilt.get(k).tanL = tanL_filt;
            

            	sv.getStateVecAtModule(k, sv.trackTrajFilt.get(k), sgeo, bgeo, mv.measurements.get(k).type, swimmer);
            	mv.measurements.get(k).FilteredOn=true;
            	this.NDF++;
            }
            
            return true;
          }
        return false;
    }

    private boolean smoother(int i, int f, org.jlab.rec.cvt.svt.Geometry sgeo, org.jlab.rec.cvt.bmt.Geometry bgeo, Swim swimmer) {
    	 if (this.isNonsingular(sv.trackCov.get(i).covMat) == false) {
             //System.err.println("Covariance Matrix is non-invertible - quit filter!");
             return false;
         }
         try {
        	 StateVecs.StateVec sv_Smooth=sv.new StateVec(f);
        	 StateVecs.CovMat covMat_Smooth=sv.new CovMat(f);
        	 sv_Smooth.Duplicate(sv.trackTrajFilt.get(f)) ;
        	 sv.trackTrajSmooth.put(f,sv_Smooth);
        	 sv.trackCovSmooth.put(f,covMat_Smooth);
        	 
        	
        	 Matrix Ak=sv.trackCovFilt.get(f).covMat.times(sv.trackTransport.get(i).transpose()).times(sv.trackCov.get(i).covMat.inverse());
    	
        	 double[] diff=new double [5];
        	 diff[0]=sv.trackTrajSmooth.get(i).d_rho-sv.trackTraj.get(i).d_rho;
        	 diff[1]=sv.trackTrajSmooth.get(i).phi0-sv.trackTraj.get(i).phi0;
        	 diff[2]=sv.trackTrajSmooth.get(i).kappa-sv.trackTraj.get(i).kappa;
        	 diff[3]=sv.trackTrajSmooth.get(i).dz-sv.trackTraj.get(i).dz;
        	 diff[4]=sv.trackTrajSmooth.get(i).tanL-sv.trackTraj.get(i).tanL;
    	
        	 double[] SmoothCorrection=new double [5];
        	 for (int j = 0; j < 5; j++) {
        		 // the gain matrix
        		 SmoothCorrection[j] = 0;
        		 for (int ii = 0; ii < 5; ii++) {
        			 SmoothCorrection[j] += diff[ii] * Ak.get(j, ii);
        		 }
        	 }
    	
        	 
    	
        	 sv.trackTrajSmooth.get(f).d_rho+=SmoothCorrection[0];
        	 sv.trackTrajSmooth.get(f).phi0+=SmoothCorrection[1];
        	 sv.trackTrajSmooth.get(f).kappa+=SmoothCorrection[2];
        	 sv.trackTrajSmooth.get(f).dz+=SmoothCorrection[3];
        	 sv.trackTrajSmooth.get(f).tanL+=SmoothCorrection[4];
    	
        	 sv.getStateVecAtModule(f, sv.trackTrajSmooth.get(f), sgeo, bgeo, mv.measurements.get(f).type, swimmer);
        	 sv.trackCovSmooth.get(f).covMat=sv.trackCovFilt.get(f).covMat.plus(Ak.times(sv.trackCovSmooth.get(i).covMat.minus(sv.trackCov.get(i).covMat)).times(Ak.transpose()));
        	   	 
        	 double deltaPhi=sv.trackTrajSmooth.get(i).phi-sv.trackTrajSmooth.get(f).phi;
        	 if (deltaPhi>Math.PI) deltaPhi=deltaPhi-2*Math.PI;
        	 if (deltaPhi<-Math.PI) deltaPhi=deltaPhi+2*Math.PI;
        	 	sv.trackTrajSmooth.get(f).pathlength=sv.trackTrajSmooth.get(i).pathlength
        	 			+Math.abs(sv.trackTrajSmooth.get(f).get_Radius()*Math.sqrt(1+sv.trackTrajSmooth.get(f).tanL*sv.trackTrajSmooth.get(f).tanL)*deltaPhi);
    	       	 	
         } catch (Exception e) {
             return false;
         }
         return true;
    }
    
    /**
     * prints the matrix -- used for debugging
     *
     * @param C matrix
     */
    public void printMatrix(Matrix C) {
    	System.out.println("////////////////");
        for (int k = 0; k < 5; k++) {
            System.out.println(C.get(k, 0)+"	"+C.get(k, 1)+"	"+C.get(k, 2)+"	"+C.get(k, 3)+"	"+C.get(k, 4));
        }
    }

    private boolean isNonsingular(Matrix mat) {
        double matDet = mat.det();
        if (Math.abs(matDet) < 1.e-30) {
            return false;
        } else {
            return true;
        }
      

    }

}
