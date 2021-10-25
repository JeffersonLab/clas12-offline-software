package org.jlab.rec.cvt.cluster;

import java.util.ArrayList;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.hit.FittedHit;
import org.jlab.rec.cvt.hit.Hit;
import org.jlab.rec.cvt.svt.SVTGeometry;
import java.util.Collections;
import org.jlab.clas.tracking.kalmanfilter.Surface;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Arc3D;
import org.jlab.geom.prim.Cylindrical3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Transformation3D;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.bmt.BMTConstants;
import org.jlab.rec.cvt.hit.Strip;
/**
 * A cluster in the BST consists of an array of hits that are grouped together
 * according to the algorithm of the ClusterFinder class
 *
 * @author ziegler
 *
 */
public class Cluster extends ArrayList<FittedHit> implements Comparable<Cluster> {

    private static final long serialVersionUID = 9153980362683755204L;

    private DetectorType _Detector;		//  The detector SVT or BMT
    private BMTType  _Type;                   //   The detector type  for BMT C or Z
    private int _Sector;      		//	sector[1...]
    private int _Layer;    	 		//	layer [1,...]
    private int _Id;			//	cluster Id
    private double _Centroid; 		// after LC (Lorentz Correction)
    private double _Centroid0; 		// before LC
    private double _CentroidValue;            // the value used in the KF
    private double _CentroidError;            // for BMT error or Z or phi
    private double _Resolution;               // cluster spatial resolution    
    private double _TotalEnergy;
    private double _Phi;  			// local LC phi and error for BMT-Z
    private double _PhiErr;
    private double _Phi0;  			// local uncorrected phi and error for BMT-Z 
    private double _PhiErr0;                                                        
    private double _Z;    			// local Z and correspondng error for BMT-C
    private double _ZErr;                       
    private Line3D _Line;                     // 3D line for SVT and BMT-Z
    private Arc3D  _Arc;                      // 3D Arc for BMT-C
    private Point3D _TrakInters;              //track intersection with the cluster
    private int AssociatedTrackID = -1;       // the track ID associated with that hit


    private int _MinStrip;			// the min strip number in the cluster
    private int _MaxStrip;			// the max strip number in the cluster
    private FittedHit _Seed;		// the seed: the strip with largest deposited energy

    private double _SeedResidual;               // residual is doca to seed strip from trk intersection with module plane
    private double _CentroidResidual;           // residual is doca to centroid of cluster to trk inters with module plane

    private Vector3D _l; //svt vector along cluster pseudo-strip direction or bmt vector along cluster pseudo-strip direction in the middle of the arc
    private Vector3D _s; //svt vector perpendicular to cluster pseudo-strip direction in the module plane or bmt vector perpendicular to cluster pseudo-strip in direction tangential to the cluster surface in the middle of the arc
    private Vector3D _n; //svt vector normal to the cluster module plane or bmt vector normal to the cluster surface in the middle of the arc
    

    public Cluster(DetectorType detector, BMTType type, int sector, int layer, int cid) {
        this._Detector = detector;
        this._Type = type;
        this._Sector = sector;
        this._Layer = layer;
        this._Id = cid;

    }

    /**
     *
     * @param hit the first hit in the list of hits composing the cluster
     * @param cid the id of the cluster
     * @return an array list of hits characterized by its sector, layer and id
     * number.
     */
    public Cluster newCluster(Hit hit, int cid) {
        return new Cluster(hit.get_Detector(), hit.get_Type(), hit.get_Sector(), hit.get_Layer(), cid);
    }

    public DetectorType get_Detector() {
        return _Detector;
    }

    public void set_Detector(DetectorType _Detector) {
        this._Detector = _Detector;
    }

    public BMTType get_Type() {
        return _Type;
    }

    public void set_Type(BMTType type) {
        this._Type = type;
    }

    /**
     *
     * @return the sector of the cluster
     */
    public int get_Sector() {
        return _Sector;
    }

    /**
     *
     * @param _Sector sector of the cluster
     */
    public void set_Sector(int _Sector) {
        this._Sector = _Sector;
    }

    /**
     *
     * @return the layer of the cluster
     */
    public int get_Layer() {
        return _Layer;
    }

    /**
     *
     * @param _Layer the layer of the cluster
     */
    public void set_Layer(int _Layer) {
        this._Layer = _Layer;
    }

    /**
     *
     * @return the id of the cluster
     */
    public int get_Id() {
        return _Id;
    }

    /**
     *
     * @param _Id the id of the cluster
     */
    public void set_Id(int _Id) {
        this._Id = _Id;
    }

    /**
     *
     * @return region (1...4)
     */
    public int get_Region() {
        return (int) (this._Layer + 1) / 2;
    }

    /**
     *
     * @return superlayer 1 or 2 in region (1...4)
     */
    public int get_RegionSlayer() {
        return (this._Layer + 1) % 2 + 1;
    }

    /**
     * @return the _Radius
     */
    public double getRadius() {
        if(this.get_Detector()==DetectorType.BST)
            return 0;
        else {
            return this.get(0).get_Strip().get_Tile().baseArc().radius();
        }
    }

    public Line3D getAxis() {
        if(this.get_Detector()==DetectorType.BST)
            return new Line3D();
        else {
            return this.get(0).get_Strip().get_Tile().getAxis();
        }
    }

    public Cylindrical3D getTile() {
        if(this.get_Detector()==DetectorType.BST)
            return null;
        else {
            return this.get(0).get_Strip().get_Tile();
        }
    }
    /**
     * sets energy-weighted parameters; these are the strip centroid
     * (energy-weighted) value, the energy-weighted phi for Z detectors and the
     * energy-weighted z for C detectors
     */
    public void calc_CentroidParams() {
        // instantiation of variables
        double totEn = 0.;			// cluster total energy
        double weightedStrp = 0;		// Lorentz-angle-corrected energy-weighted strip 
        double weightedStrp0 = 0;		// uncorrected energy-weighted strip 
        double weightedPhi = 0;			// Lorentz-angle-corrected energy-weighted phi of the strip 
        double weightedPhiErrSq = 0;            // Err^2 on Lorentz-angle-corrected energy-weighted phi of the strip 
        double weightedPhi0 = 0;		// Uncorrected energy-weighted phi of the strip 
        double weightedPhiErrSq0 = 0;           // Err^2 on uncorrected energy-weighted phi of the strip 
        double weightedZ = 0;			// Energy-weighted z of the strip
        double weightedZErrSq = 0;		  // Err^2 on  energy-weighted z of the strip
        double weightedX1 = 0;                  // SVT/BMT strip centroid positions of endpoints
        double weightedX2 = 0;                  // SVT/BMT strip centroid positions of endpoints
        double weightedXC = 0;                  // BMT strip centroid positions of strip midpoint
        double weightedX0 = 0;                  // BMT strip centroid positions of strip position with no LC
        double weightedY1 = 0;                  // SVT/BMT strip centroid positions of endpoints
        double weightedY2 = 0;                  // SVT/BMT strip centroid positions of endpoints
        double weightedYC = 0;                  // BMT strip centroid positions of strip midpoint
        double weightedY0 = 0;                  // BMT strip centroid positions of strip midpoint with no LC
        double weightedZ1 = 0;                  // SVT/BMT strip centroid positions of endpoints
        double weightedZ2 = 0;                  // SVT/BMT strip centroid positions of endpoints
        double weightedZC = 0;                  // BMT strip centroid positions of strip midpoint
        double weightedZ0 = 0;                  // BMT strip centroid positions of strip midpoint with no LC
        
        int nbhits = this.size();
        //sort for bmt detector
        //this.sort(Comparator.comparing(FittedHit.get_Strip()::get_Edep).thenComparing(FittedHit.get_Strip()::get_Edep));
        
        Collections.sort(this);
        if (nbhits != 0) {
            int min = 1000000;
            int max = -1;
            FittedHit seed = null;
           
            // looping over the number of hits in the cluster
            for (int i = 0; i < nbhits; i++) {
                FittedHit thehit = this.get(i); 
                
                int strpNb = -1;
                int strpNb0 = -1; //before LC

                //strip points:
                Point3D stEP1   = null;
                Point3D stEP2   = null;
                Point3D stCent  = null;
                Point3D stCent0 = null;
 
                // strip energy
                double strpEn = thehit.get_Strip().get_Edep();

                if (this.get_Detector()==DetectorType.BST) {
                   // for the SVT the analysis only uses the centroid
                    strpNb  = thehit.get_Strip().get_Strip();
                    stEP1   = thehit.get_Strip().get_Line().origin();
                    stEP2   = thehit.get_Strip().get_Line().end();
                    stCent  = thehit.get_Strip().get_Line().midpoint();
                    stCent0 = thehit.get_Strip().get_Line().midpoint();                
               }
                else if (this.get_Detector()==DetectorType.BMT) { 
                    
                    if(thehit.newClustering && nbhits>BMTConstants.MAXCLUSSIZE && i>BMTConstants.MAXCLUSSIZE-1) 
                        continue;

                    // for the BMT the analysis distinguishes between C and Z type detectors
                    if (this.get_Type()==BMTType.C) { // C-detectors
                        //strpEn = Math.sqrt(thehit.get_Strip().get_Edep());
                        strpNb  = thehit.get_Strip().get_Strip();
                        stEP1   = thehit.get_Strip().get_Arc().origin();
                        stEP2   = thehit.get_Strip().get_Arc().end();
                        stCent  = thehit.get_Strip().get_Arc().center();
                        stCent0 = thehit.get_Strip().get_Arc().center();
                        // for C detector the Z of the centroid is calculated
                        weightedZ += strpEn * thehit.get_Strip().get_Z();
                        weightedZErrSq += (thehit.get_Strip().get_ZErr()) * (thehit.get_Strip().get_ZErr());
                    }
                    if (this.get_Type()==BMTType.Z) { // Z-detectors
                        // for Z detectors Lorentz-correction is applied to the strip
                        strpNb = thehit.get_Strip().get_LCStrip();
                        strpNb0 = thehit.get_Strip().get_Strip();
                        stEP1  = thehit.get_Strip().get_Line().origin();
                        stEP2  = thehit.get_Strip().get_Line().end();
                        // RDV: should remove stuff that is not used or necessary from cluster strips and so on
                        // for C detectors the phi of the centroid is calculated for the uncorrected and the Lorentz-angle-corrected centroid
                        stCent  = new Point3D(Math.cos(thehit.get_Strip().get_Phi()),Math.sin(thehit.get_Strip().get_Phi()),0);
                        stCent0 = new Point3D(Math.cos(thehit.get_Strip().get_Phi0()),Math.sin(thehit.get_Strip().get_Phi0()),0);
                        weightedPhiErrSq  += (thehit.get_Strip().get_PhiErr())  * (thehit.get_Strip().get_PhiErr());
                        weightedPhiErrSq0 += (thehit.get_Strip().get_PhiErr0()) * (thehit.get_Strip().get_PhiErr0());
                        
                    }
                    
                }

                totEn += strpEn;
                weightedX1 += strpEn * stEP1.x();
                weightedY1 += strpEn * stEP1.y();
                weightedZ1 += strpEn * stEP1.z();
                weightedX2 += strpEn * stEP2.x();
                weightedY2 += strpEn * stEP2.y();
                weightedZ2 += strpEn * stEP2.z();
                weightedXC += strpEn * stCent.x();
                weightedYC += strpEn * stCent.y();
                weightedZC += strpEn * stCent.z();
                weightedX0 += strpEn * stCent0.x();
                weightedY0 += strpEn * stCent0.y();
                weightedZ0 += strpEn * stCent0.z();
                weightedStrp += strpEn * (double) strpNb;
                weightedStrp0 += strpEn * (double) strpNb0;
                
                // getting the max and min strip number in the cluster
                if (strpNb <= min) {
                    min = strpNb;
                }
                if (strpNb >= max) {
                    max = strpNb;
                }
                // getting the seed strip which is defined as the strip with the largest deposited energy
                if (seed==null || strpEn >= seed.get_Strip().get_Edep()) {
                    seed = thehit;
                }

            }
            if (totEn == 0) {
                System.err.println(" Cluster energy is null .... exit "+this._Detector+" "+this._Type);
                
                return;
            }

            this.set_MinStrip(min);
            this.set_MaxStrip(max);
            this.set_Seed(seed);
            // calculates the centroid values and associated errors
            weightedStrp  /= totEn;
            weightedStrp0 /= totEn;
            weightedX1 /= totEn;
            weightedY1 /= totEn;
            weightedZ1 /= totEn;
            weightedX2 /= totEn;
            weightedY2 /= totEn;
            weightedZ2 /= totEn;
            weightedXC /= totEn;
            weightedYC /= totEn;
            weightedZC /= totEn;
            weightedX0 /= totEn;
            weightedY0 /= totEn;
            weightedZ0 /= totEn;
            weightedZ /= totEn;
            weightedPhi  = Math.atan2(weightedYC, weightedXC);
            weightedPhi0 = Math.atan2(weightedY0, weightedX0);
            this.set_Centroid(weightedStrp);
            this.set_TotalEnergy(totEn);
            
            //setting final variables, including the ones used for alignment
            //-----------------------------------
            if (this.get_Detector()==DetectorType.BST) { //SVT
                
                this.setLine(new Line3D(weightedX1, weightedY1, weightedZ1, weightedX2, weightedY2, weightedZ2));
                Vector3D l = new Vector3D(this.getLine().direction().asUnit());
                Vector3D n = this.get(0).get_Strip().get_Normal();
                Vector3D s = l.cross(n).asUnit();
                                
                this.setL(l);
                this.setS(s);
                this.setN(n);
            }
            else if (this.get_Detector()==DetectorType.BMT) { //BMT 
                // for the BMT the analysis distinguishes between C and Z type detectors
                if (this.get_Type()==BMTType.C) { // C-detectors
                    Point3D  origin = new Point3D(weightedX1, weightedY1, weightedZ1);
                    Point3D  center = new Point3D(weightedXC, weightedYC, weightedZC);
                    Vector3D normal = this.get(0).get_Strip().get_Arc().normal();
                    double   theta  = this.get(0).get_Strip().get_Arc().theta();
                    this.set_Arc(new Arc3D(origin,center,normal,theta));

                    Vector3D s = this.get_Arc().normal();
                    Vector3D n = this.get_Arc().bisect();
                    Vector3D l = s.cross(n).asUnit();
                    
                    this.setL(l);
                    this.setS(s);
                    this.setN(n);
                    
                    this.set_Z(weightedZ);
                    this.set_ZErr(Math.sqrt(weightedZErrSq));
                    this.set_CentroidValue(weightedZ);
                    this.set_CentroidError(Math.sqrt(weightedZErrSq));
                    this.set_Resolution(Math.sqrt(weightedZErrSq));
                }
                if (this.get_Type()==BMTType.Z) { // Z-detectors
            
                    this.set_Centroid0(weightedStrp0);
                    this.set_Phi(weightedPhi);
                    this.set_PhiErr(Math.sqrt(weightedPhiErrSq));
                    this.set_Phi0(weightedPhi0);
                    this.set_PhiErr0(Math.sqrt(weightedPhiErrSq0));
                    this.set_CentroidValue(weightedPhi);
                    this.set_CentroidError(Math.sqrt(weightedPhiErrSq));
                    this.set_Resolution(this.getTile().baseArc().radius()*Math.sqrt(weightedPhiErrSq));
                    
                    // for Z detectors Lorentz-correction is applied to the strip
                    this.setLine(new Line3D(weightedX1, weightedY1, weightedZ1, weightedX2, weightedY2, weightedZ2));
                    
                    Vector3D l = this.getLine().direction().asUnit();                                    
                    Vector3D n = this.getAxis().distance(this.getLine().midpoint()).direction().asUnit();
                    Vector3D s = l.cross(n).asUnit();
                   
                    this.setL(l);
                    this.setS(s);
                    this.setN(n);
                   
                }
            }
        }
    }

        
    public double get_Centroid() {
        return _Centroid;
    }

    public void set_Centroid(double _Centroid) {
        this._Centroid = _Centroid;
    }
    
    public double get_Resolution() {
        return _Resolution;
    }

    public void set_Resolution(double E) {
        this._Resolution = E;
    }
    public double get_Centroid0() {
        return _Centroid0;
    }

    public void set_Centroid0(double _Centroid0) {
        this._Centroid0 = _Centroid0;
    }

    public double get_Phi() {
        return _Phi;
    }

    public void set_Phi(double _Phi) {
        this._Phi = _Phi;
    }

    public double get_Phi0() {
        return _Phi0;
    }

    public void set_Phi0(double _Phi0) {
        this._Phi0 = _Phi0;
    }

    public double get_PhiErr() {
        return _PhiErr;
    }

    public void set_PhiErr(double _PhiErr) {
        this._PhiErr = _PhiErr;
    }

    public double get_PhiErr0() {
        return _PhiErr0;
    }

    public void set_PhiErr0(double _PhiErr0) {
        this._PhiErr0 = _PhiErr0;
    }

    public double get_Z() {
        return _Z;
    }

    public void set_Z(double _Z) {
        this._Z = _Z;
    }

    public double get_ZErr() {
        return _ZErr;
    }

    public void set_ZErr(double _ZErr) {
        this._ZErr = _ZErr;
    }

    public void set_CentroidValue(double _CentroidValue) {
        this._CentroidValue = _CentroidValue;
    }

    public double get_CentroidValue() {
        return this._CentroidValue;
    }

    public double get_CentroidError() {
        return this._CentroidError;
    }

    public void set_CentroidError(double _CentroidError) {
        this._CentroidError = _CentroidError;
    }
    
    /**
     * @return the _Arc
     */
    public Arc3D get_Arc() {
        return _Arc;
    }

    /**
     * @param _Arc the _Arc to set
     */
    public void set_Arc(Arc3D _Arc) {
        this._Arc = _Arc;
    }

    public Point3D origin() {
        if(this.get_Detector()==DetectorType.BST)
            return this.getLine().origin();
        else {
            if(this.get_Type()==BMTType.C)
                return this.get_Arc().origin();
            else
                return this.getLine().origin();
        }
    }

    public Point3D end() {
        if(this.get_Detector()==DetectorType.BST)
            return this.getLine().end();
        else {
            if(this.get_Type()==BMTType.C)
                return this.get_Arc().end();
            else
                return this.getLine().end();
        }
    }

    public Point3D center() {
        if(this.get_Detector()==DetectorType.BST)
            return this.getLine().midpoint();
        else {
            if(this.get_Type()==BMTType.C)
                return this.get_Arc().point(this.get_Arc().theta()/2);
            else
                return this.getLine().midpoint();
        }
    }

    public double theta() {
        if(this.get_Detector()==DetectorType.BST)
            return 0;
        else {
            if(this.get_Type()==BMTType.C)
                return this.get_Arc().theta();
            else
                return 0;
        }
    }

    public Transformation3D toLocal() {
        return this.get(0).get_Strip().toLocal();
    }
    
    public Transformation3D toGlobal() {
        return this.get(0).get_Strip().toGlobal();
    }
    
    public double residual(Point3D traj) {
        double value = 0;
        if(this.get_Detector()==DetectorType.BST) {
            Line3D dist = this.getLine().distance(traj);
            double side = -Math.signum(this.getLine().direction().cross(dist.direction()).dot(this.getN()));
            value = dist.length()*side;
        }
        else {
            Point3D local = new Point3D(traj);
            this.toLocal().apply(local);
            if(this.get_Type()==BMTType.C)                
                value = local.z()-this.get_CentroidValue();
            else {
                value = local.toVector3D().phi()-this.get_CentroidValue();
                if(Math.abs(value)>2*Math.PI) value-=Math.signum(value)*2*Math.PI;
            }
        }     
        return value;
    }
    
    public void set_CentroidResidual(Point3D traj) {
        this.set_CentroidResidual(this.residual(traj));
    }
    
    public void set_SeedResidual(Point3D traj) {
        this.set_SeedResidual(this.get_Seed().residual(traj));
    }
    
    public double get_TotalEnergy() {
        return _TotalEnergy;
    }

    public void set_TotalEnergy(double _TotalEnergy) {
        this._TotalEnergy = _TotalEnergy;
    }
    
    public int get_MinStrip() {
        return _MinStrip;
    }

    public void set_MinStrip(int _MinStrip) {
        this._MinStrip = _MinStrip;
    }

    public int get_MaxStrip() {
        return _MaxStrip;
    }

    public void set_MaxStrip(int _MaxStrip) {
        this._MaxStrip = _MaxStrip;
    }

    public Strip get_SeedStrip() {
        return _Seed.get_Strip();
    }

    public FittedHit get_Seed() {
        return _Seed;
    }

    public void set_Seed(FittedHit _Seed) {
        this._Seed = _Seed;
    }

    public double get_SeedResidual() {
        return _SeedResidual;
    }

    public void set_SeedResidual(double _SeedResidual) {
        this._SeedResidual = _SeedResidual;
    }

    public double get_CentroidResidual() {
        return _CentroidResidual;
    }

    public void set_CentroidResidual(double _CentroidResidual) {
        this._CentroidResidual = _CentroidResidual;
    }

    /**
     * @return the _TrakInters
     */
    public Point3D getTrakInters() {
        return _TrakInters;
    }

    /**
     * @param _TrakInters the _TrakInters to set
     */
    public void setTrakInters(Point3D _TrakInters) {
        this._TrakInters = _TrakInters;
    }

    public Surface measurement(int layerID) {
        Surface surface = null;
        if(this.get_Detector()==DetectorType.BST) {
            Point3D endPt1 = this.getLine().origin();
            Point3D endPt2 = this.getLine().end();
            org.jlab.clas.tracking.objects.Strip strp = new org.jlab.clas.tracking.objects.Strip(this.get_Id(), this.get_Centroid(), 
                                                                                                 endPt1.x(), endPt1.y(), endPt1.z(),
                                                                                                 endPt2.x(), endPt2.y(), endPt2.z());
            Plane3D plane = new Plane3D(endPt1, this.getN());
            Line3D module = this.get(0).get_Strip().get_Module();
            surface = new Surface(plane, strp, module.origin(), module.end(), Constants.SWIMACCURACYSVT);
            surface.hemisphere = Math.signum(this.center().y());
            surface.setLayer(layerID);
            surface.setSector(this.get_Sector());
            surface.setError(this.get_Resolution()*this.get_Resolution()); 
            surface.setl_over_X0(this.get(0).get_Strip().getToverX0());
        }
        else {
            if(this.get_Type()==BMTType.C) {
                org.jlab.clas.tracking.objects.Strip strp = new org.jlab.clas.tracking.objects.Strip(this.get_Id(), this.get_Centroid(), this.get_CentroidValue());
                surface = new Surface(this.get(0).get_Strip().get_Tile(), strp, Constants.SWIMACCURACYBMT);
                double error = this.get_CentroidError();
                surface.setError(error*error);
            }
            else {
                Point3D point = new Point3D(this.getLine().midpoint());
                this.toLocal().apply(point);
                org.jlab.clas.tracking.objects.Strip strp = new org.jlab.clas.tracking.objects.Strip(this.get_Id(), this.get_Centroid(), point.x(), point.y(), this.get_CentroidValue());  
                surface = new Surface(this.getTile(), strp, Constants.SWIMACCURACYBMT);
                double error = this.get_CentroidError();///this.getTile().baseArc().radius();
                surface.setError(error*error);
            
            }
            surface.setTransformation(this.toGlobal()); 
            surface.setLayer(layerID);
            surface.setSector(this.get_Sector());
            surface.setl_over_X0(this.get(0).get_Strip().getToverX0());
        }
        return surface;
    }
        
    /**
     *
     */
    public void printInfo() {
        String s = " cluster: Detector " + this.get_Detector().getName() +"  Detector Type " + this.get_Type().getName() + " ID " + this.get_Id() + " Sector " + this.get_Sector() 
                + " Layer " + this.get_Layer() + " Radius " + this.getRadius()+ " Size " + this.size() +" centroid "+this.get_Centroid() + this.size() +" centroidValue "+this.get_CentroidValue();
        System.out.println(s);
    }

    /**
     *
     * @param Z z-coordinate of a point in the local coordinate system of a
     * module
     * @return the average resolution for a group of strips in a cluster in the
     * SVT
     *
     */
    public double get_ResolutionAlongZ(double Z, SVTGeometry geo) {

        // returns the total resolution for a group of strips in a cluster
        // the single strip resolution varies at each point along the strip as a function of Z (due to the graded angle of the strips) and 
        // is smallest at the pitch implant at which is is simply Pitch/sqrt(12)
        int nbhits = this.size();
        if (nbhits == 0) {
            return 0;
        }

        // average
        double res = 0;

        for (int i = 0; i < nbhits; i++) {
            double rstrp = geo.getSingleStripResolution(this.get(i).get_Layer(), this.get(i).get_Strip().get_Strip(), Z);
            res += rstrp * rstrp;
        }
        return Math.sqrt(res);
    }

    public int get_AssociatedTrackID() {
        return AssociatedTrackID;
    }

    public void set_AssociatedTrackID(int associatedTrackID) {
        AssociatedTrackID = associatedTrackID;
    }

    @Override
    public int compareTo(Cluster arg) {
            
        //sort by phi of strip implant of first strip in the cluster, then by layer, then by seed strip number
        double this_phi = PhiInRange(this.get(0).get_Strip().get_Line().origin().toVector3D().phi());
        double arg_phi = PhiInRange(arg.get(0).get_Strip().get_Line().origin().toVector3D().phi());

        int CompPhi = this_phi < arg_phi ? -1 : this_phi == arg_phi ? 0 : 1;
        int CompLay = this._Layer < arg._Layer ? -1 : this._Layer == arg._Layer ? 0 : 1;
        int CompId = this.get_SeedStrip().get_Strip()< arg.get_SeedStrip().get_Strip() ? -1 : this.get_SeedStrip().get_Strip() == arg.get_SeedStrip().get_Strip() ? 0 : 1;

        int return_val1 = ((CompLay == 0) ? CompId : CompLay);
        int return_val = ((CompPhi == 0) ? return_val1 : CompPhi);

        return return_val;
        
        
    }

    private double PhiInRange(double phi) {
        if (phi < 0) {
            phi += Math.PI * 2;
        }
        return phi;
    }

    public Line3D getLine() {
        return _Line;
    }

    public void setLine(Line3D _Line) {
        this._Line = _Line;
    }

    /**
     * @return the _l
     */
    public Vector3D getL() {
        return _l;
    }

    /**
     * @param _l the _l to set
     */
    public void setL(Vector3D _l) {
        this._l = _l;
    }

    /**
     * @return the _s
     */
    public Vector3D getS() {
        return _s;
    }

    /**
     * @param _s the _s to set
     */
    public void setS(Vector3D _s) {
        this._s = _s;
    }

    /**
     * @return the _n
     */
    public Vector3D getN() {
        return _n;
    }

    /**
     * @param _n the _n to set
     */
    public void setN(Vector3D _n) {
        this._n = _n;
    }

}
