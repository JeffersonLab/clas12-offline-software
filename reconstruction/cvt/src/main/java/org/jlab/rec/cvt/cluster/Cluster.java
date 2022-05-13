package org.jlab.rec.cvt.cluster;

import java.util.ArrayList;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.hit.Hit;
import java.util.Collections;
import org.jlab.clas.tracking.kalmanfilter.AKFitter.HitOnTrack;
import org.jlab.clas.tracking.kalmanfilter.Surface;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Arc3D;
import org.jlab.geom.prim.Cylindrical3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Transformation3D;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.hit.Strip;
/**
 * A cluster in the BST consists of an array of hits that are grouped together
 * according to the algorithm of the ClusterFinder class
 *
 * @author ziegler
 *
 */
public class Cluster extends ArrayList<Hit> implements Comparable<Cluster> {

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
    private double _Time;
    private double _Phi;  			// local LC phi and error for BMT-Z
    private double _PhiErr;
    private double _Phi0;  			// local uncorrected phi and error for BMT-Z 
    private double _PhiErr0;                    // RDV could be removed                                    
    private double _Z;    			// local Z and correspondng error for BMT-C
    private double _ZErr;                       
    private Line3D _Line;                     // 3D line for SVT and BMT-Z
    private Arc3D  _Arc;                      // 3D Arc for BMT-C
    private Point3D _TrakInters;              //track intersection with the cluster
    private int AssociatedTrackID = -1;       // the track ID associated with that hit


    private int _MinStrip;			// the min strip number in the cluster
    private int _MaxStrip;			// the max strip number in the cluster
    private Hit _Seed;		                // the seed: the strip with largest deposited energy

    private double _SeedResidual;               // residual is doca to seed strip from trk intersection with module plane
    private double _CentroidResidual;           // residual is doca to centroid of cluster to trk inters with module plane

    private Vector3D _l; //svt vector along cluster pseudo-strip direction or bmt vector along cluster pseudo-strip direction in the middle of the arc
    private Vector3D _s; //svt vector perpendicular to cluster pseudo-strip direction in the module plane or bmt vector perpendicular to cluster pseudo-strip in direction tangential to the cluster surface in the middle of the arc
    private Vector3D _n; //svt vector normal to the cluster module plane or bmt vector normal to the cluster surface in the middle of the arc
    public boolean flagForExclusion = false;
    

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
        return new Cluster(hit.getDetector(), hit.getType(), hit.getSector(), hit.getLayer(), cid);
    }

    public DetectorType getDetector() {
        return _Detector;
    }

    public void setDetector(DetectorType _Detector) {
        this._Detector = _Detector;
    }

    public BMTType getType() {
        return _Type;
    }

    public void setType(BMTType type) {
        this._Type = type;
    }

    /**
     *
     * @return the sector of the cluster
     */
    public int getSector() {
        return _Sector;
    }

    /**
     *
     * @param _Sector sector of the cluster
     */
    public void setSector(int _Sector) {
        this._Sector = _Sector;
    }

    /**
     *
     * @return the layer of the cluster
     */
    public int getLayer() {
        return _Layer;
    }

    /**
     *
     * @param _Layer the layer of the cluster
     */
    public void setLayer(int _Layer) {
        this._Layer = _Layer;
    }

    /**
     *
     * @return the id of the cluster
     */
    public int getId() {
        return _Id;
    }

    /**
     *
     * @param _Id the id of the cluster
     */
    public void setId(int _Id) {
        this._Id = _Id;
    }

    /**
     *
     * @return region (1...4)
     */
    public int getRegion() {
        return (int) (this._Layer + 1) / 2;
    }

    /**
     *
     * @return superlayer 1 or 2 in region (1...4)
     */
    public int getRegionSlayer() {
        return (this._Layer + 1) % 2 + 1;
    }

    /**
     * @return the _Radius
     */
    public double getRadius() {
        if(this.getDetector()==DetectorType.BST)
            return 0;
        else {
            return this.get(0).getStrip().getTile().baseArc().radius();
        }
    }

    public Line3D getAxis() {
        if(this.getDetector()==DetectorType.BST)
            return new Line3D();
        else {
            return this.get(0).getStrip().getTile().getAxis();
        }
    }

    public Cylindrical3D getTile() {
        if(this.getDetector()==DetectorType.BST)
            return null;
        else {
            return this.get(0).getStrip().getTile();
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
        double aveTime = 0.;			// cluster average time
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
        //this.sort(Comparator.comparing(FittedHit.getStrip()::getEdep).thenComparing(FittedHit.getStrip()::getEdep));
        
        Collections.sort(this);
        if (nbhits != 0) {
            int min = 1000000;
            int max = -1;
            Hit seed = null;
            int totHits = 0;
            
            // looping over the number of hits in the cluster
            for (int i = 0; i < nbhits; i++) {
                Hit thehit = this.get(i); 
                
                int strpNb = -1;
                int strpNb0 = -1; //before LC

                //strip points:
                Point3D stEP1   = null;
                Point3D stEP2   = null;
                Point3D stCent  = null;
                Point3D stCent0 = null;
 
                // strip energy
                double strpEn = thehit.getStrip().getEdep();

                // strip time
                double strpTime = thehit.getStrip().getTime();
                
                if (this.getDetector()==DetectorType.BST) {
                   // for the SVT the analysis only uses the centroid
                    strpNb  = thehit.getStrip().getStrip();
                    stEP1   = thehit.getStrip().getLine().origin();
                    stEP2   = thehit.getStrip().getLine().end();
                    stCent  = thehit.getStrip().getLine().midpoint();
                    stCent0 = thehit.getStrip().getLine().midpoint();                
               }
                else if (this.getDetector()==DetectorType.BMT) { 
                    
//                    if(thehit.newClustering && nbhits>BMTConstants.MAXCLUSSIZE && i>BMTConstants.MAXCLUSSIZE-1) 
//                        continue;

                    // for the BMT the analysis distinguishes between C and Z type detectors
                    if (this.getType()==BMTType.C) { // C-detectors
                        //strpEn = Math.sqrt(thehit.getStrip().getEdep());
                        strpNb  = thehit.getStrip().getStrip();
                        stEP1   = thehit.getStrip().getArc().origin();
                        stEP2   = thehit.getStrip().getArc().end();
                        stCent  = thehit.getStrip().getArc().center();
                        stCent0 = thehit.getStrip().getArc().center();
                        // for C detector the Z of the centroid is calculated
                        weightedZ += strpEn * thehit.getStrip().getZ();
                        weightedZErrSq += (thehit.getStrip().getZErr()) * (thehit.getStrip().getZErr());
                    }
                    if (this.getType()==BMTType.Z) { // Z-detectors
                        // for Z detectors Lorentz-correction is applied to the strip
                        strpNb = thehit.getStrip().getLCStrip();
                        strpNb0 = thehit.getStrip().getStrip();
                        stEP1  = thehit.getStrip().getLine().origin();
                        stEP2  = thehit.getStrip().getLine().end();
                        // RDV: should remove stuff that is not used or necessary from cluster strips and so on
                        // for Z detectors the phi of the centroid is calculated for the uncorrected and the Lorentz-angle-corrected centroid
                        stCent  = new Point3D(Math.cos(thehit.getStrip().getPhi()),Math.sin(thehit.getStrip().getPhi()),0);
                        stCent0 = new Point3D(Math.cos(thehit.getStrip().getPhi0()),Math.sin(thehit.getStrip().getPhi0()),0);
                        weightedPhiErrSq  += (thehit.getStrip().getPhiErr())  * (thehit.getStrip().getPhiErr());
                        weightedPhiErrSq0 += (thehit.getStrip().getPhiErr0()) * (thehit.getStrip().getPhiErr0());
                        
                    }
                    
                }
                
                totHits++;
                totEn += strpEn;
                aveTime += strpTime;
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
                // getting the seed strip which is defined as the first in the cluster according to the chosen ordering
                if (seed==null) {
                    seed = thehit;
                }

            }
            if (totEn == 0) {
                System.err.println(" Cluster energy is null .... exit "+this._Detector+" "+this._Type);
                
                return;
            }

            this.setMinStrip(min);
            this.setMaxStrip(max);
            this.setSeed(seed);
            // calculates the centroid values and associated errors
            aveTime /= totHits;
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
            this.setCentroid(weightedStrp);
            this.setTotalEnergy(totEn);
            this.setTime(aveTime);
            this.setPhi(weightedPhi);
            this.setPhi0(weightedPhi0);
                    
            //setting final variables, including the ones used for alignment
            //-----------------------------------
            if (this.getDetector()==DetectorType.BST) { //SVT
                
                this.setLine(new Line3D(weightedX1, weightedY1, weightedZ1, weightedX2, weightedY2, weightedZ2));
                Vector3D l = new Vector3D(this.getLine().direction().asUnit());
                Vector3D n = this.get(0).getStrip().getNormal();
                Vector3D s = l.cross(n).asUnit();
                                
                this.setL(l);
                this.setS(s);
                this.setN(n);
            }
            else if (this.getDetector()==DetectorType.BMT) { //BMT 
                // for the BMT the analysis distinguishes between C and Z type detectors
                if (this.getType()==BMTType.C) { // C-detectors
                    Point3D  origin = new Point3D(weightedX1, weightedY1, weightedZ1);
                    Point3D  center = new Point3D(weightedXC, weightedYC, weightedZC);
                    Vector3D normal = this.get(0).getStrip().getArc().normal();
                    double   theta  = this.get(0).getStrip().getArc().theta();
                    this.setArc(new Arc3D(origin,center,normal,theta));

                    Vector3D s = this.getArc().normal();
                    Vector3D n = this.getArc().bisect();
                    Vector3D l = s.cross(n).asUnit();
                    
                    this.setL(l);
                    this.setS(s);
                    this.setN(n);
                    
                    this.setZ(weightedZ);
                    this.setZErr(Math.sqrt(weightedZErrSq));
                    this.setCentroidValue(weightedZ);
                    this.setCentroidError(Math.sqrt(weightedZErrSq));
                    this.setResolution(Math.sqrt(weightedZErrSq));
                }
                if (this.getType()==BMTType.Z) { // Z-detectors
            
                    this.setCentroid0(weightedStrp0);
                    this.setPhiErr(Math.sqrt(weightedPhiErrSq));
                    this.setPhiErr0(Math.sqrt(weightedPhiErrSq0));
                    this.setCentroidValue(weightedPhi);
                    this.setCentroidError(Math.sqrt(weightedPhiErrSq));
                    this.setResolution(this.getTile().baseArc().radius()*Math.sqrt(weightedPhiErrSq));
                    
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

        
    public double getCentroid() {
        return _Centroid;
    }

    public void setCentroid(double _Centroid) {
        this._Centroid = _Centroid;
    }
    
    public double getResolution() {
        return _Resolution;
    }

    public void setResolution(double E) {
        this._Resolution = E;
    }
    public double getCentroid0() {
        return _Centroid0;
    }

    public void setCentroid0(double _Centroid0) {
        this._Centroid0 = _Centroid0;
    }

    public double getPhi() {
        return _Phi;
    }

    public void setPhi(double _Phi) {
        this._Phi = _Phi;
    }

    public double getPhi0() {
        return _Phi0;
    }

    public void setPhi0(double _Phi0) {
        this._Phi0 = _Phi0;
    }

    public double getPhiErr() {
        return _PhiErr;
    }

    public void setPhiErr(double _PhiErr) {
        this._PhiErr = _PhiErr;
    }

    public double getPhiErr0() {
        return _PhiErr0;
    }

    public void setPhiErr0(double _PhiErr0) {
        this._PhiErr0 = _PhiErr0;
    }

    public double getZ() {
        return _Z;
    }

    public void setZ(double _Z) {
        this._Z = _Z;
    }

    public double getZErr() {
        return _ZErr;
    }

    public void setZErr(double _ZErr) {
        this._ZErr = _ZErr;
    }

    public void setCentroidValue(double _CentroidValue) {
        this._CentroidValue = _CentroidValue;
    }

    public double getCentroidValue() {
        return this._CentroidValue;
    }

    public double getCentroidError() {
        return this._CentroidError;
    }

    public void setCentroidError(double _CentroidError) {
        this._CentroidError = _CentroidError;
    }
    
    /**
     * @return the _Arc
     */
    public Arc3D getArc() {
        return _Arc;
    }

    /**
     * @param _Arc the _Arc to set
     */
    public void setArc(Arc3D _Arc) {
        this._Arc = _Arc;
    }

    public Point3D origin() {
        if(this.getDetector()==DetectorType.BST)
            return this.getLine().origin();
        else {
            if(this.getType()==BMTType.C)
                return this.getArc().origin();
            else
                return this.getLine().origin();
        }
    }

    public Point3D end() {
        if(this.getDetector()==DetectorType.BST)
            return this.getLine().end();
        else {
            if(this.getType()==BMTType.C)
                return this.getArc().end();
            else
                return this.getLine().end();
        }
    }

    public Point3D center() {
        if(this.getDetector()==DetectorType.BST)
            return this.getLine().midpoint();
        else {
            if(this.getType()==BMTType.C)
                return this.getArc().point(this.getArc().theta()/2);
            else
                return this.getLine().midpoint();
        }
    }

    public double theta() {
        if(this.getDetector()==DetectorType.BST)
            return 0;
        else {
            if(this.getType()==BMTType.C)
                return this.getArc().theta();
            else
                return 0;
        }
    }

    public Transformation3D toLocal() {
        return this.get(0).getStrip().toLocal();
    }
    
    public Transformation3D toGlobal() {
        return this.get(0).getStrip().toGlobal();
    }
    
    public double residual(Point3D traj) {
        double value = 0;
        if(this.getDetector()==DetectorType.BST) {
            Line3D dist = this.getLine().distance(traj);
            double side = -Math.signum(this.getLine().direction().cross(dist.direction()).dot(this.getN()));
            value = dist.length()*side;
        }
        else {
            Point3D local = new Point3D(traj);
            this.toLocal().apply(local);
            if(this.getType()==BMTType.C)                
                value = local.z()-this.getCentroidValue();
            else {
                value = local.toVector3D().phi()-this.getCentroidValue();
                if(Math.abs(value)>Math.PI) value-=Math.signum(value)*2*Math.PI;
            }
        }     
        return value;
    }
    
    public void setCentroidResidual(Point3D traj) {
        this.setCentroidResidual(this.residual(traj));
    }
    
    public void setSeedResidual(Point3D traj) {
        this.setSeedResidual(this.getSeed().residual(traj));
    }
    
    public double getTotalEnergy() {
        return _TotalEnergy;
    }

    public void setTotalEnergy(double _TotalEnergy) {
        this._TotalEnergy = _TotalEnergy;
    }

    public double getTime() {
        return _Time;
    }

    public void setTime(double _Time) {
        this._Time = _Time;
    }
    
    public int getMinStrip() {
        return _MinStrip;
    }

    public void setMinStrip(int _MinStrip) {
        this._MinStrip = _MinStrip;
    }

    public int getMaxStrip() {
        return _MaxStrip;
    }

    public void setMaxStrip(int _MaxStrip) {
        this._MaxStrip = _MaxStrip;
    }

    public Strip getSeedStrip() {
        return _Seed.getStrip();
    }

    public Hit getSeed() {
        return _Seed;
    }

    public void setSeed(Hit _Seed) {
        this._Seed = _Seed;
    }

    public double getSeedResidual() {
        return _SeedResidual;
    }

    public void setSeedResidual(double _SeedResidual) {
        this._SeedResidual = _SeedResidual;
    }

    public double getCentroidResidual() {
        return _CentroidResidual;
    }

    public void setCentroidResidual(double _CentroidResidual) {
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

    public Surface measurement() {
        Surface surface = null;
        
        if(this.getDetector()==DetectorType.BST) {
            Point3D endPt1 = this.getLine().origin();
            Point3D endPt2 = this.getLine().end();
//            org.jlab.clas.tracking.objects.Strip strp = new org.jlab.clas.tracking.objects.Strip(this.getId(), this.getCentroid(), 
//                                                                                                 endPt1.x(), endPt1.y(), endPt1.z(),
//                                                                                                 endPt2.x(), endPt2.y(), endPt2.z());
            Plane3D plane = new Plane3D(endPt1, this.getN());
            surface = Constants.getInstance().SVTGEOMETRY.getSurface(this.getLayer(), this.getSector(), this.getId(), 
                                                       this.getCentroid(), this.getLine());
            surface.hemisphere = Math.signum(this.center().y());
            surface.setError(this.getResolution()); 
        }
        else {
            if(this.getType()==BMTType.C) {
                surface = Constants.getInstance().BMTGEOMETRY.getSurfaceC(this.getLayer(), this.getSector(), this.getId(), 
                                                            this.getCentroid(), this.getCentroidValue());
            }
            else {
                Point3D point = new Point3D(this.getLine().midpoint());
                this.toLocal().apply(point);
                surface = Constants.getInstance().BMTGEOMETRY.getSurfaceZ(this.getLayer(), this.getSector(), this.getId(), 
                                                            this.getCentroid(), point.x(), point.y(), this.getCentroidValue());           
            }
            surface.setError(this.getCentroidError());
        }
        return surface;
    }

    /**
     *
     * @param Z z-coordinate of a point in the local coordinate system of a
     * module
     * @return the average resolution for a group of strips in a cluster in the
     * SVT
     *
     */
    public double getResolutionAlongZ(double Z) {

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
            double rstrp = Constants.getInstance().SVTGEOMETRY.getSingleStripResolution(this.get(i).getLayer(), this.get(i).getStrip().getStrip(), Z);
            res += rstrp * rstrp;
        }
        return Math.sqrt(res);
    }

    public int getAssociatedTrackID() {
        return AssociatedTrackID;
    }

    public void setAssociatedTrackID(int associatedTrackID) {
        AssociatedTrackID = associatedTrackID;
    }

    @Override
    public int compareTo(Cluster arg) {
            
        //sort by phi of strip implant of first strip in the cluster, then by layer, then by seed strip number
        double this_phi = this.getPhi0(); 
        double arg_phi  = arg.getPhi0();

        int CompPhi = this_phi < arg_phi ? -1 : this_phi == arg_phi ? 0 : 1;
        int CompLay = this._Layer < arg._Layer ? -1 : this._Layer == arg._Layer ? 0 : 1;
        int CompId = this.getSeedStrip().getStrip()< arg.getSeedStrip().getStrip() ? -1 : this.getSeedStrip().getStrip() == arg.getSeedStrip().getStrip() ? 0 : 1;

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

    public void update(int trackId, HitOnTrack traj) {
        
        Point3D  trackPos = new Point3D(traj.x, traj.y, traj.z);
        Vector3D trackDir = new Vector3D(traj.px, traj.py, traj.pz).asUnit();
                
        this.setAssociatedTrackID(trackId);
        this.setCentroidResidual(traj.residual);
        this.setSeedResidual(trackPos); 
        this.setTrakInters(trackPos);

        
        if(this.getDetector()==DetectorType.BMT && this.getType()==BMTType.C) {  
            this.setS(this.getAxis().direction().asUnit());
            this.setN(this.getAxis().distance(trackPos).direction().asUnit());
            this.setL(this.getS().cross(this.getN()).asUnit());
        }
        if(this.getDetector()==DetectorType.BMT && this.getType()==BMTType.Z) {  
            this.setCentroidResidual(traj.residual*this.getTile().baseArc().radius());    
        }
        
        for (Hit hit : this) {
            hit.setAssociatedTrackID(trackId);
            double doca1 = hit.residual(trackPos);
            hit.setdocaToTrk(doca1);  
            if(this.getDetector()==DetectorType.BST) {
                Point3D local = Constants.getInstance().SVTGEOMETRY.toLocal(this.getLayer(), this.getSector(), trackPos);
                double sigma1 = Constants.getInstance().SVTGEOMETRY.getSingleStripResolution(this.getLayer(), hit.getStrip().getStrip(), local.z());
                hit.setstripResolutionAtDoca(sigma1);
            }
            if(traj.isUsed) hit.setTrkgStatus(1);
        }
          
    }

    @Override
    public String toString() {
        String str = String.format("Cluster id=%d %s %s layer=%d sector=%d centroid=%.3f value=%.3f error=%.3f resolution=%.3f phi=%.3f phi0=%.3f size=%d", 
                                    this.getId(), this.getDetector(), this.getType(), this.getLayer(), this.getSector(), this.getCentroid(),
                                    this.getCentroidValue(), this.getCentroidError(), this.getResolution(), this.getPhi(), this.getPhi0(), this.size());
        return str;
    }


}
