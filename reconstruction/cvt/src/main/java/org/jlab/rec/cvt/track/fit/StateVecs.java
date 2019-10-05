package org.jlab.rec.cvt.track.fit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.track.Seed;
import org.jlab.rec.cvt.trajectory.Helix;

import Jama.Matrix;
import org.jlab.clas.swimtools.Swim;

/***********
 * 
 * @author mdefurne
 *
 */

public class StateVecs {

    final static double speedLight = org.jlab.rec.cvt.Constants.LIGHTVEL;

    public List<B> bfieldPoints = new ArrayList<B>();
    //Raw predictions
    public Map<Integer, StateVec> trackTraj = new HashMap<Integer, StateVec>();
    public Map<Integer, CovMat> trackCov = new HashMap<Integer, CovMat>();
    //Filtered predictions
    public Map<Integer, StateVec> trackTrajFilt = new HashMap<Integer, StateVec>();
    public Map<Integer, CovMat> trackCovFilt = new HashMap<Integer, CovMat>();
    //Smoothed post-predictions
    public Map<Integer, StateVec> trackTrajSmooth = new HashMap<Integer, StateVec>();
    public Map<Integer, CovMat> trackCovSmooth = new HashMap<Integer, CovMat>();
    
    //Transport Matrix between step k-1 and step k
    public Map<Integer, Matrix> trackTransport = new HashMap<Integer, Matrix>();
    public Map<Integer, Matrix> MeasurementDerivative = new HashMap<Integer, Matrix>();

    public StateVec StateVec;
    public CovMat CovMat;
    public Matrix F;

    public List<Double> X0;
    public List<Double> Y0;
    public List<Double> Z0; // reference points

    public List<Integer> Layer;
    public List<Integer> Sector;

   public void getStateVecPosAtModule(int k, StateVec kVec, org.jlab.rec.cvt.svt.Geometry svt_geo, org.jlab.rec.cvt.bmt.Geometry bmt_geo, int type, Swim swimmer) {
      
        Helix kHelix = setSvPars(kVec, null);
        double csPoint=0;
        Vector3D InitialPos=kHelix.getHelixPoint(csPoint);
        kVec.x=InitialPos.x();
        kVec.y=InitialPos.y();
        kVec.z=InitialPos.z();
        Vector3D temp;
        
    	if (k>0) {        
    		if (Layer.get(k)>6) csPoint=bmt_geo.getRefinedIntersection(kHelix, Layer.get(k)-6, Sector.get(k));
    		else csPoint=svt_geo.getRefinedIntersection(kHelix, Layer.get(k), Sector.get(k));
    			
    		temp=kHelix.getHelixPoint(csPoint);	
    		Vector3D labpos=new Vector3D(temp.x(), temp.y(), temp.z());
    		Vector3D detpos;
    		if (type>0) {
    			detpos=bmt_geo.LabToDetFrame(Layer.get(k)-6, Sector.get(k), labpos);
    		}
    		else {
    			detpos=svt_geo.Point_LabToDetFrame(Layer.get(k), Sector.get(k), labpos);
    		}
    		kVec.xdet=detpos.x();
    		kVec.ydet=detpos.y();
    		kVec.zdet=detpos.z();
    	}
    	else {
    		csPoint=kHelix.getCurvAbsForPCAToPoint(new Vector3D(org.jlab.rec.cvt.Constants.getXb(),org.jlab.rec.cvt.Constants.getYb(),0));
    		temp=kHelix.getHelixPoint(csPoint);
    		kVec.x=temp.x();
    		kVec.y=temp.y();
    		kVec.z=temp.z();
    	}
		
        kVec.phi=Math.atan2(temp.y()-kHelix.ycen(), temp.x()-kHelix.xcen());
        Vector3D tempDir=kHelix.getHelixDir(csPoint);
        kVec.dirx=tempDir.x();
        kVec.diry=tempDir.y();
        kVec.dirz=tempDir.z();
        kVec.alpha = new B(k, kVec.x, kVec.y, kVec.z, swimmer).alpha;
                
    }

    public void getStateVecAtModule(int k, StateVec kVec, org.jlab.rec.cvt.svt.Geometry sgeo, 
            org.jlab.rec.cvt.bmt.Geometry bgeo, int type, Swim swimmer) {

        StateVec newVec = kVec;
        this.getStateVecPosAtModule(k, newVec, sgeo, bgeo, type, swimmer);
        
        // new state: 
        kVec = newVec;
    }

    public StateVec newStateVecAtModule(int k, StateVec kVec, org.jlab.rec.cvt.svt.Geometry sgeo, 
            org.jlab.rec.cvt.bmt.Geometry bgeo, int type, Swim swimmer) {

        StateVec newVec = kVec;
        
       this.getStateVecPosAtModule(k, newVec, sgeo, bgeo, type, swimmer);
      
         // new state: 
        return newVec;
    }
    
    public void new_transport(int i, int f, StateVec iVec, CovMat icovMat, 
            org.jlab.rec.cvt.svt.Geometry sgeo, org.jlab.rec.cvt.bmt.Geometry bgeo, int type, 
            Swim swimmer) { // s = signed step-size... s=0 at dca... ALWAYS!!!
    
    	double Xc, Yc; //Coordinate of the Helix center
    	double Ri=0; //Radius at s=0
    	double Rf=0; //Radius we want to reach
    	double d=Double.POSITIVE_INFINITY;//Distance left to run before reaching f-th layer.
    	Vector3D Poslab=new Vector3D(); //To transport step-by-step the state Vec
    	Vector3D Posdet=new Vector3D(); //To transport step-by-step the state Vec
    	double step=Math.signum(f-i)*org.jlab.rec.cvt.Constants.KFitterStepsize;//mm
    	int count=0; //Needed to avoid loopers in the transportation.
    	int Max_count=100;
    	
    	StateVec sv_i = new StateVec(f);
    	sv_i.Duplicate(iVec);
    	StateVec sv_f = new StateVec(f);
    	sv_f.Duplicate(iVec) ;
    	sv_i.k=f;
    	sv_f.k=f;
    	B Bf = new B(i, sv_i.x, sv_i.y, sv_i.z, swimmer);	
    	sv_i.alpha=Bf.alpha;
    	
        // transport stateVec...
        if (Layer.get(f)>6) Rf=bgeo.getRadius(Layer.get(f)-6);
        	
        if (Layer.get(f)<7&&f>0) Rf=sgeo.getRadius(Layer.get(f));
                
        CovMat fCov = new CovMat(f);
        fCov.covMat=icovMat.covMat;
                           
        double phi_i = 0;
        double phi_f = 0;
        
        boolean AtModule=false;
        boolean AtClosest=false;
       
        double[][] Id = new double[][]{
            {1, 0, 0, 0, 0},
            {0, 1, 0, 0, 0},
            {0, 0, 1, 0, 0},
            {0, 0, 0, 1, 0},
            {0, 0, 0, 0, 1}
        };
        Matrix fTransport = new Matrix(Id);
        
        while (((!AtModule&&f>0)||(!AtClosest&&f==0))&&count<Max_count) {
        	
        	Helix iHelix=setSvPars(sv_i, fCov.covMat);// Helix definition and equations valid only if xref=0 and yref=0;
        	
            if (f>0) {
            	if (Layer.get(f)>6) Posdet=bgeo.LabToDetFrame(Layer.get(f)-6, Sector.get(f), new Vector3D(sv_i.x, sv_i.y, sv_i.z));
                if (Layer.get(f)<7) Posdet=sgeo.Point_LabToDetFrame(Layer.get(f), Sector.get(f), new Vector3D(sv_i.x, sv_i.y, sv_i.z));
            	Ri=Math.sqrt(Posdet.x()*Posdet.x()+Posdet.y()*Posdet.y());
            	d=Math.abs(Rf-Ri);
            }
            else {
            	if (Math.sqrt((sv_i.x-org.jlab.rec.cvt.Constants.getXb())*(sv_i.x-org.jlab.rec.cvt.Constants.getXb())+(sv_i.y-org.jlab.rec.cvt.Constants.getYb())*(sv_i.y-org.jlab.rec.cvt.Constants.getYb()))>d) AtClosest=true;
            	d=Math.sqrt((sv_i.x-org.jlab.rec.cvt.Constants.getXb())*(sv_i.x-org.jlab.rec.cvt.Constants.getXb())+(sv_i.y-org.jlab.rec.cvt.Constants.getYb())*(sv_i.y-org.jlab.rec.cvt.Constants.getYb()));
            	 
            }
            Xc = iHelix.xcen();
            Yc = iHelix.ycen();
            
        	if ((f==0&&!AtClosest)||(d>Math.abs(step*Math.cos(iHelix.get_dip()))&&f>0)) {
        		Poslab=iHelix.getHelixPoint(step);
        		sv_f.x=Poslab.x();
        		sv_f.y=Poslab.y();
        		sv_f.z=Poslab.z();
        				
        		if (f==0) sv_f.pathlength=sv_i.pathlength+Math.abs(step);
        		
        	}
        	else {
        		if (f>0) {
        			this.getStateVecAtModule(f, sv_f, sgeo, bgeo, type, swimmer);
        			AtModule=true;
        		}
        		else {
        			double cs=iHelix.getCurvAbsForPCAToPoint(new Vector3D(org.jlab.rec.cvt.Constants.getXb(),org.jlab.rec.cvt.Constants.getYb(),0));
        			Poslab=iHelix.getHelixPoint(cs);
        			sv_f.x=Poslab.x();
            		sv_f.y=Poslab.y();
            		sv_f.z=Poslab.z();
            		sv_f.pathlength=sv_i.pathlength-cs;// The convention is supposed to be cs>0 implies we walk alongside the particle. Now, we are supposed to have performed a step too far (we have passed the PCA... 
            		//which means that the pathlength is too long.)
            	}
        	}
        	
        	//Angle of pivot point
        	phi_i=sv_i.getPhiRef(Xc, Yc);
        	
            //Angle of new pivot point for f-state vector... chosen to be the position of state vector f
            sv_f.refx=sv_f.x;
            sv_f.refy=sv_f.y;
            sv_f.refz=sv_f.z;
            //Except when swimming back to vertex... we want to take as reference the position of the beam
            if (f==0&&AtClosest) {
            	sv_f.refx=org.jlab.rec.cvt.Constants.getXb();
            	sv_f.refy=org.jlab.rec.cvt.Constants.getYb();
            	sv_f.refz=0;
            }
            
        	phi_f = sv_f.getPhiRef(Xc, Yc);
           
            double delta= phi_f-phi_i;
            
            //Difference must be kept between -pi and pi
            if (delta>Math.PI) delta=delta-2*Math.PI;
            if (delta<-Math.PI) delta=delta+2*Math.PI;
            
            sv_f.phi0 = sv_i.phi0+delta;
            
            sv_f.d_rho = (Xc - sv_f.refx) * Math.cos(phi_f) + (Yc - sv_f.refy) * Math.sin(phi_f) - sv_i.alpha / sv_i.kappa;
            
           
            sv_f.kappa = sv_i.kappa;

            sv_f.dz = sv_i.refz - sv_f.refz + sv_i.dz - (sv_i.alpha / sv_i.kappa) * delta * sv_i.tanL;
            
            sv_f.tanL = sv_i.tanL;
           
            // now transport covMat...
             double dphi0_prm_del_drho = -1. / (sv_f.d_rho + sv_i.alpha / sv_i.kappa) * Math.sin(delta);
             double dphi0_prm_del_phi0 = (sv_i.d_rho + sv_i.alpha / sv_i.kappa) / (sv_f.d_rho + sv_i.alpha / sv_i.kappa) * Math.cos(delta);
             double dphi0_prm_del_kappa = (sv_i.alpha / (sv_i.kappa * sv_i.kappa)) / (sv_f.d_rho + sv_i.alpha / sv_i.kappa) * Math.sin(delta);
             double dphi0_prm_del_dz = 0;
             double dphi0_prm_del_tanL = 0;

             double drho_prm_del_drho = Math.cos(delta);
             double drho_prm_del_phi0 = (sv_i.d_rho + sv_i.alpha / sv_i.kappa) * Math.sin(delta);
             double drho_prm_del_kappa = (sv_i.alpha / (sv_i.kappa * sv_i.kappa)) * (1 - Math.cos(delta));
             double drho_prm_del_dz = 0;
             double drho_prm_del_tanL = 0;

             double dkappa_prm_del_drho = 0;
             double dkappa_prm_del_phi0 = 0;
             double dkappa_prm_del_dkappa = 1;
             double dkappa_prm_del_dz = 0;
             double dkappa_prm_del_tanL = 0;

             double dz_prm_del_drho = ((sv_i.alpha / sv_i.kappa) / (sv_f.dz + sv_i.alpha / sv_i.kappa)) * sv_i.tanL * Math.sin(delta);
             double dz_prm_del_phi0 = (sv_i.alpha / sv_i.kappa) * sv_i.tanL * (1 - Math.cos(delta) * (sv_i.dz + sv_i.alpha / sv_i.kappa) / (sv_f.dz + sv_i.alpha / sv_i.kappa));
             double dz_prm_del_kappa = (sv_i.alpha / (sv_i.kappa * sv_i.kappa)) * sv_i.tanL * (delta - Math.sin(delta) * (sv_i.alpha / sv_i.kappa) / (sv_f.dz + sv_i.alpha / sv_i.kappa));
             double dz_prm_del_dz = 1;
             double dz_prm_del_tanL = -sv_i.alpha * delta / sv_i.kappa;

             double dtanL_prm_del_drho = 0;
             double dtanL_prm_del_phi0 = 0;
             double dtanL_prm_del_dkappa = 0;
             double dtanL_prm_del_dz = 0;
             double dtanL_prm_del_tanL = 1;
            
             double[][] FMat = new double[][]{
                 {drho_prm_del_drho, drho_prm_del_phi0, drho_prm_del_kappa, drho_prm_del_dz, drho_prm_del_tanL},
                 {dphi0_prm_del_drho, dphi0_prm_del_phi0, dphi0_prm_del_kappa, dphi0_prm_del_dz, dphi0_prm_del_tanL},
                 {dkappa_prm_del_drho, dkappa_prm_del_phi0, dkappa_prm_del_dkappa, dkappa_prm_del_dz, dkappa_prm_del_tanL},
                 {dz_prm_del_drho, dz_prm_del_phi0, dz_prm_del_kappa, dz_prm_del_dz, dz_prm_del_tanL},
                 {dtanL_prm_del_drho, dtanL_prm_del_phi0, dtanL_prm_del_dkappa, dtanL_prm_del_dz, dtanL_prm_del_tanL}
             };
             
             Matrix F = new Matrix(FMat);
             Matrix FT = F.transpose();
             Matrix Cpropagated = F.times(fCov.covMat).times(FT);
          
             //Store the transport information
             Matrix fTransStep= F.times(fTransport);
             fTransport=fTransStep;
             
             fCov.covMat = Cpropagated;
            
             if (Cpropagated != null&&AtModule&&i<f) {//Add the noise only at module... and only going forward... Going backward only to get the best estimate at Vertex
                 fCov.covMat = fCov.covMat.plus(this.Q(iVec, f - i));
             } 
            
             //We will move the pivot point to sv_f... we need to compute the phi0 of the point wrt to the new Helix center
             Bf = new B(f, sv_f.x, sv_f.y, sv_f.z, swimmer);
             sv_f.alpha=Bf.alpha;
                         
             sv_i.Duplicate(sv_f);
                      
             count++;
        }
        sv_f.layer=Layer.get(f);
        sv_f.sector=Sector.get(f);
        if (f!=0) {
        	CovMat fCovFilt = new CovMat(f);
        	fCovFilt.covMat=fCov.covMat;
        	StateVec sv_fFilt = new StateVec(f);
        	sv_fFilt.Duplicate(sv_f) ;
        	this.trackTraj.put(f, sv_f);
        	this.trackTrajFilt.put(f, sv_fFilt);
        	this.trackCov.put(f, fCov);
        	this.trackCovFilt.put(f, fCovFilt);
        	this.trackTransport.put(f, fTransport);
        }      
        if (f==0) {
        	this.trackTrajSmooth.put(f, sv_f);
        	this.trackCovSmooth.put(f, fCov);
        }
    }

    private double get_t_ov_X0(double radius) {
        double value = org.jlab.detector.geant4.v2.SVT.SVTConstants.SILICONTHK / org.jlab.rec.cvt.Constants.SILICONRADLEN;
        if(radius>=org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[0]) 
            value = org.jlab.rec.cvt.bmt.Constants.get_T_OVER_X0()[this.getBMTLayer(radius)-1];
        return value;
    }
    
    private double detMat_Z_ov_A_timesThickn(double radius) {    
        double value = 0;
        if(radius>=org.jlab.detector.geant4.v2.SVT.SVTConstants.LAYERRADIUS[0][0]&& radius<org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[0])
            value = org.jlab.rec.cvt.Constants.detMatZ_ov_A_timesThickn;
        if(radius>=org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[0] && this.getBMTLayer(radius)>0)
            value = org.jlab.rec.cvt.bmt.Constants.getEFF_Z_OVER_A()[this.getBMTLayer(radius)-1];
        return value;
    }
    private int getBMTLayer(double radius) {
        int layer = 0;
        if(radius>=org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[0] && radius<org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[0])
            layer=1;
        if(radius>=org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[0] && radius<org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[1])
            layer=2;
        if(radius>=org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[1] && radius<org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[1])
            layer=3;
        if(radius>=org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[1] && radius<org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[2])
            layer=4;
        if(radius>=org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[2] && radius<org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[2])
           layer=5;
        if(radius>=org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[2])
           layer=6;
       
        return layer;
    }
    private double[] ELoss_hypo(StateVec iVec, int dir) {
        double[] Eloss = new double[3]; //Eloss for pion, kaon, proton hypotheses

        if (dir < 0  || Math.sqrt(iVec.refx*iVec.refx+iVec.refy*iVec.refy)<org.jlab.detector.geant4.v2.SVT.SVTConstants.LAYERRADIUS[0][0]) {
            return Eloss;
        }

        Vector3D trkDir = this.P(iVec.k);
        trkDir.unit();
        double cosEntranceAngle = trkDir.z();
       // System.out.println(" cosTrk "+Math.toDegrees(Math.acos(trkDir.z()))+" at state "+iVec.k+" dir "+dir);
        double pt = Math.abs(1. / iVec.kappa);
        double pz = pt * iVec.tanL;
        double p = Math.sqrt(pt * pt + pz * pz);

        for (int hyp = 2; hyp < 5; hyp++) {

            double mass = MassHypothesis(hyp); // assume given mass hypothesis
            double beta = p / Math.sqrt(p * p + mass * mass); // use particle momentum
            double gamma = 1. / Math.sqrt(1 - beta * beta);

            double s = MassHypothesis(1) / mass;

            double Wmax = 2. * mass * beta * beta * gamma * gamma / (1. + 2. * s * gamma + s * s);
            double I = 0.000000172;

            double logterm = 2. * mass * beta * beta * gamma * gamma * Wmax / (I * I);

            double delta = 0.;
            double dEdx = 0.00001535 * this.detMat_Z_ov_A_timesThickn(Math.sqrt(iVec.refx*iVec.refx+iVec.refy*iVec.refy)) * (Math.log(logterm) - 2 * beta * beta - delta) / (beta * beta); //in GeV/mm
            //System.out.println(" mass hy "+hyp+" Mat at "+Math.sqrt(iVec.x*iVec.x+iVec.y*iVec.y)+"Z/A*t "+this.detMat_Z_ov_A_timesThickn(Math.sqrt(iVec.x*iVec.x+iVec.y*iVec.y))+" dEdx "+dEdx);
            Eloss[hyp - 2] = dir * Math.abs(dEdx / cosEntranceAngle);
        }
        return Eloss;
    }

    private Matrix Q(StateVec iVec, int dir) {

        Matrix Q = new Matrix(new double[][]{
            {0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0}
        });

        // if (iVec.k % 2 == 1 && dir > 0) {
        if (dir >0 && Math.sqrt(iVec.refx*iVec.refx+iVec.refy*iVec.refy)>org.jlab.detector.geant4.v2.SVT.SVTConstants.LAYERRADIUS[0][0]) {
        	double cosEntranceAngle = Math.abs(this.P(iVec.k).z());

            double pt = Math.abs(1. / iVec.kappa);
            double pz = pt * iVec.tanL;
            double p = Math.sqrt(pt * pt + pz * pz);

            //double t_ov_X0 = 2. * 0.32 / Constants.SILICONRADLEN; //path length in radiation length units = t/X0 [true path length/ X0] ; Si radiation length = 9.36 cm
            double t_ov_X0 = this.get_t_ov_X0(Math.sqrt(iVec.xdet*iVec.xdet+iVec.ydet*iVec.ydet)); //System.out.println(Math.log(t_ov_X0)/9.+" rad "+Math.sqrt(iVec.x*iVec.x+iVec.y*iVec.y)+" t/x0 "+t_ov_X0);
            double mass = MassHypothesis(2);   // assume given mass hypothesis (2=pion)
            double beta = p / Math.sqrt(p * p + mass * mass); // use particle momentum
            double pathLength = t_ov_X0 / cosEntranceAngle;
//0.0136?

            double sctRMS = (0.00141 / (beta * p)) * Math.sqrt(pathLength) * (1 + Math.log10(pathLength)/9.); // Highland-Lynch-Dahl formula
            
            Q = new Matrix(new double[][]{
                {0, 0, 0, 0, 0},
                {0, sctRMS*sctRMS * (1 + iVec.tanL * iVec.tanL), 0, 0, 0},
                {0, 0, sctRMS*sctRMS * (iVec.kappa * iVec.kappa * iVec.tanL * iVec.tanL), 0, sctRMS*sctRMS * (iVec.kappa * iVec.tanL * (1 + iVec.tanL * iVec.tanL))},
                {0, 0, 0, 0, 0},
                {0, 0, sctRMS*sctRMS * (iVec.kappa * iVec.tanL * (1 + iVec.tanL * iVec.tanL)), 0, sctRMS*sctRMS * (1 + iVec.tanL * iVec.tanL) * (1 + iVec.tanL * iVec.tanL)}
            });
        }

        return Q;

    }

    public class StateVec {

        public int k;
        public int layer;
        public int sector;
        public int DetectorType;
        public int clusID;
        //Pivot point coordinates for Helix parameters
        public double refx;
        public double refy;
        public double refz;
        //Lab coordinates for state vector
        public double x;
        public double y;
        public double z;
        //Detector coordinates for state vector
        public double xdet;
        public double ydet;
        public double zdet;
        //Direction of helix in lab coordinate
        public double dirx;
        public double diry;
        public double dirz;
        //Helix parameters
        public double kappa;
        public double d_rho;
        public double phi0;
        public double phi;
        public double tanL;
        public double dz;
        public double alpha;
        public double residual;
        public double excl_residual;
        public double pathlength;
        //Track angles at module or tile
        public double angle; // Angle of the direction wrt to norm
        public double RTheta; // Angle of the projected direction in er/etheta with normal vector er
        public double RZ; // Angle of the projected direction in er/ez with norm
                        
        public StateVec(int k) {
            this.k = k;
        }
        
        private double[] _ELoss = new double[3];

        public double[] get_ELoss() {
            return _ELoss;
        }

        public void set_ELoss(double[] _ELoss) {
            this._ELoss = _ELoss;
        }
        
        public double get_Radius() {
        	return this.alpha/this.kappa;
        }
        
        public void Duplicate(StateVec ToCopy) {
        	this.k=ToCopy.k;
        	this.layer=ToCopy.layer;
        	this.sector=ToCopy.sector;
        	this.clusID=ToCopy.clusID;
        	this.refx=ToCopy.refx;
        	this.refy=ToCopy.refy;
        	this.refz=ToCopy.refz;
        	this.x=ToCopy.x;
        	this.y=ToCopy.y;
        	this.z=ToCopy.z;
        	this.xdet=ToCopy.xdet;
        	this.ydet=ToCopy.ydet;
        	this.zdet=ToCopy.zdet;
        	this.dirx=ToCopy.dirx;
        	this.diry=ToCopy.diry;
        	this.dirz=ToCopy.dirz;
        	this.kappa=ToCopy.kappa;
        	this.d_rho=ToCopy.d_rho;
        	this.phi0=ToCopy.phi0;
        	this.tanL=ToCopy.tanL;
        	this.dz=ToCopy.dz;
        	this.alpha=ToCopy.alpha;
        	this.phi=ToCopy.phi;
        	this.residual=ToCopy.residual;
        	this.excl_residual=ToCopy.excl_residual;
        	this.pathlength=ToCopy.pathlength;
        	this.angle=ToCopy.angle;
        	this.RZ=ToCopy.RZ;
        	this.RTheta=ToCopy.RTheta;
        	return ;
        }
        
        public double getPhiRef(double Xc, double Yc) {
        	if (this.alpha / this.kappa < 0) {
                return Math.atan2(-Yc + this.refy, -Xc + this.refx);
            }
        	else return Math.atan2(Yc - this.refy, Xc - this.refx);
        }

    }

    public class CovMat {

        final int k;
        public Matrix covMat;

        CovMat(int k) {
            this.k = k;
        }

    }

    private double shift = org.jlab.rec.cvt.Constants.getZoffset();
    public class B {

        final int k;
        double x;
        double y;
        double z;
        Swim swimmer;
        
        public double Bx;
        public double By;
        public double Bz;

        public double alpha;

        float b[] = new float[3];
        B(int k, double x, double y, double z, Swim swimmer) {
        	
            this.k = k;
            this.x = x;
            this.y = y;
            this.z = z;

            swimmer.BfieldLab(x / 10, y / 10, z / 10 + shift, b);
            this.Bx = b[0];
            this.By = b[1];
            this.Bz = b[2];
            this.alpha = 1. / (StateVecs.speedLight * b[2]);
         
        }
    }

    //public String massHypo = "pion";
    public double MassHypothesis(int H) {
        double piMass = 0.13957018;
        double KMass = 0.493677;
        double muMass = 0.105658369;
        double eMass = 0.000510998;
        double pMass = 0.938272029;
        double value = piMass; //default
        if (H == 4) {
            value = pMass;
        }
        if (H == 1) {
            value = eMass;
        }
        if (H == 2) {
            value = piMass;
        }
        if (H == 3) {
            value = KMass;
        }
        if (H == 0) {
            value = muMass;
        }
        return value;
    }

    public Vector3D P(int kf) {
        if (this.trackTraj.get(kf) != null) {
         
            double px = Math.signum(1 / this.trackTraj.get(kf).kappa) * Math.cos(this.trackTraj.get(kf).phi0 + this.trackTraj.get(kf).phi);
            double py = Math.signum(1 / this.trackTraj.get(kf).kappa) * Math.sin(this.trackTraj.get(kf).phi0 + this.trackTraj.get(kf).phi);
            double pz = Math.signum(1 / this.trackTraj.get(kf).kappa) * this.trackTraj.get(kf).tanL;
           
            return new Vector3D(px, py, pz);
        } else {
            return new Vector3D(0, 0, 0);
        }

    }

      
    public Helix setSvPars(StateVec sv, Matrix Cov) {
    	//Track parameters are defined at Xb Yb (this is why h_omega = kappa/this.trackTraj.get(0).alpha)
        double h_phi0 = sv.phi0;
        double kappa = sv.kappa;
        double h_omega = kappa/sv.alpha;
        double h_dca = sv.d_rho;
        double h_dz = sv.dz;
        double h_tandip = sv.tanL;
    	        
        Helix trkHelix = new Helix(h_dca, h_phi0, h_omega, h_dz, h_tandip, new Vector3D(sv.refx,sv.refy,sv.refz), Cov);
       // System.out.println("x "+x+" y "+y+" z "+z+" p "+p_unc+" pt "+Math.sqrt(px*px+py*py) +" theta "+Math.toDegrees(Math.acos(pz/Math.sqrt(px*px+py*py+pz*pz)))+" phi "+Math.toDegrees(Math.atan2(py, px))+" q "+q);

        return trkHelix;
    }

    public void initAtBeam(Seed trk, KFitter kf, Swim swimmer) {
        //init stateVec
    	
        StateVec initSV = new StateVec(0);
        initSV.refx = 0;
        initSV.refy = 0;
        initSV.refz = 0;
        initSV.layer=0;
        initSV.sector=0;
      
        B Bf = new B(0, trk.get_Helix().getHelixPoint(0).x(),trk.get_Helix().getHelixPoint(0).y() ,trk.get_Helix().getHelixPoint(0).z() , swimmer);
        initSV.alpha = Bf.alpha;
        initSV.kappa = Bf.alpha * trk.get_Helix().get_curvature();
        initSV.phi0 = trk.get_Helix().get_phi_at_dca();
        initSV.dz = trk.get_Helix().get_Z0();
        initSV.tanL = trk.get_Helix().get_tandip();
        initSV.d_rho = trk.get_Helix().get_dca();
        initSV.phi = 0;
        
        //If Pt>5 GeV from helical fit, we initialize it at 5 GeV for Kalman filter
        if (Math.abs(1/initSV.kappa)>5)  initSV.kappa=Math.signum(initSV.kappa)*0.2;
       
        this.trackTraj.put(0, initSV);
        this.trackTrajFilt.put(0, initSV);
        //init covMat
        //TODO:Absolute values are used to avoid negative term on diagonal of the cov matrix from helical fitter
        Matrix fitCovMat = trk.get_Helix().get_covmatrix();
        double cov_d02 = Math.abs(fitCovMat.get(0, 0));
        double cov_d0phi0 = fitCovMat.get(0, 1);
        double cov_d0rho = Bf.alpha * fitCovMat.get(0, 2);
        double cov_phi02 = Math.abs(fitCovMat.get(1, 1));
        double cov_phi0rho = Bf.alpha * fitCovMat.get(1, 2);
        double cov_rho2 = Bf.alpha * Bf.alpha * Math.abs(fitCovMat.get(2, 2));
        double cov_z02 = Math.abs(fitCovMat.get(3, 3));
        double cov_z0tandip = fitCovMat.get(3, 4);
        double cov_tandip2 = Math.abs(fitCovMat.get(4, 4));

        double components[][] = new double[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                components[i][j] = 0;
            }
        }
      
        components[0][0] = org.jlab.rec.cvt.Constants.unc_d0*org.jlab.rec.cvt.Constants.unc_d0;
        components[0][1] = cov_d0phi0/Math.sqrt(cov_d02*cov_phi02)*org.jlab.rec.cvt.Constants.unc_d0*org.jlab.rec.cvt.Constants.unc_phi0;
        components[1][0] = cov_d0phi0/Math.sqrt(cov_d02*cov_phi02)*org.jlab.rec.cvt.Constants.unc_d0*org.jlab.rec.cvt.Constants.unc_phi0;
        components[1][1] = org.jlab.rec.cvt.Constants.unc_phi0*org.jlab.rec.cvt.Constants.unc_phi0;
        components[2][0] = cov_d0rho/Math.sqrt(cov_d02*cov_rho2)*org.jlab.rec.cvt.Constants.unc_d0*org.jlab.rec.cvt.Constants.unc_kappa;
        components[0][2] = cov_d0rho/Math.sqrt(cov_d02*cov_rho2)*org.jlab.rec.cvt.Constants.unc_d0*org.jlab.rec.cvt.Constants.unc_kappa;
        components[2][1] = cov_phi0rho/Math.sqrt(cov_phi02*cov_rho2)*org.jlab.rec.cvt.Constants.unc_phi0*org.jlab.rec.cvt.Constants.unc_kappa;
        components[1][2] = cov_phi0rho/Math.sqrt(cov_phi02*cov_rho2)*org.jlab.rec.cvt.Constants.unc_phi0*org.jlab.rec.cvt.Constants.unc_kappa;
        components[2][2] = org.jlab.rec.cvt.Constants.unc_kappa*org.jlab.rec.cvt.Constants.unc_kappa;
        components[3][3] = org.jlab.rec.cvt.Constants.unc_z0*org.jlab.rec.cvt.Constants.unc_z0;
        components[3][4] = cov_z0tandip/Math.sqrt(cov_tandip2*cov_z02)*org.jlab.rec.cvt.Constants.unc_z0*org.jlab.rec.cvt.Constants.unc_tanL;
        components[4][3] = cov_z0tandip/Math.sqrt(cov_tandip2*cov_z02)*org.jlab.rec.cvt.Constants.unc_z0*org.jlab.rec.cvt.Constants.unc_tanL;
        components[4][4] = org.jlab.rec.cvt.Constants.unc_tanL*org.jlab.rec.cvt.Constants.unc_tanL;
       
        Matrix initCMatrix = new Matrix(components);

        CovMat initCM = new CovMat(0);
        initCM.covMat = initCMatrix;
        this.trackCovFilt.put(0, initCM);
        this.trackCov.put(0, initCM);
    }
    
    public void initAtLastMeas(KFitter kf, MeasVecs mv, org.jlab.rec.cvt.svt.Geometry sgeo, org.jlab.rec.cvt.bmt.Geometry bgeo, Swim swimmer) {
        //Try to have a guess of position at last layer
    	
    	this.new_transport(0, mv.measurements.get(mv.measurements.size()-1).k, this.trackTrajFilt.get(0) , this.trackCovFilt.get(0) , sgeo, bgeo, mv.measurements.get(mv.measurements.size()-1).type, swimmer);
    
    	//Remove the 0-object
        this.trackTraj.remove(0);
        this.trackTrajFilt.remove(0);
        this.trackCov.remove(0);
        this.trackCovFilt.remove(0);     
        
    }

    public void printMatrix(Matrix C) {
        for (int k = 0; k < 5; k++) {
            System.out.println(C.get(k, 0) + "	" + C.get(k, 1) + "	" + C.get(k, 2) + "	" + C.get(k, 3) + "	" + C.get(k, 4));
        }
    }

    public void printlnStateVec(StateVec S) {
        System.out.println(S.k + ") drho " + S.d_rho + " phi0 " + S.phi0 + " kappa " + S.kappa + " dz " + S.dz + " tanL " + S.tanL + " phi " + S.phi + " x " + S.refx + " y " + S.refy + " z " + S.refz + " alpha " + S.alpha);
    }
	
}
