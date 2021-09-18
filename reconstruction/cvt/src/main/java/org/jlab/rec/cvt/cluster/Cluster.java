package org.jlab.rec.cvt.cluster;

import java.util.ArrayList;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.hit.FittedHit;
import org.jlab.rec.cvt.hit.Hit;
import org.jlab.rec.cvt.svt.SVTGeometry;
import java.util.Collections;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Arc3D;
import org.jlab.geom.prim.Transformation3D;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.bmt.Constants;
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

    private DetectorType _Detector;							//              The detector SVT or BMT
    private BMTType  _Type;                                                  //              The detector type  for BMT C or Z
    private int _Sector;      							//	        sector[1...]
    private int _Layer;    	 						//	        layer [1,...]
    private int _Id;								//		cluster Id
    private double _Centroid; 							// 		after LC (Lorentz Correction)
    private double _CentroidError;
    private double _Error;                                                      //              strip resolution    
    private double _Centroid0; 							// 		before LC
    private double _TotalEnergy;
    private double _Phi;  							// 		for Z-detectors
    private double _PhiErr;
    private double _Phi0;  							// 		for Z-detectors before LC
    private double _PhiErr0;                                                        //      local Z
    private double _Z;    							// 		for C-detectors
    private double _ZErr;
    private Line3D _Line;
    private Arc3D  _Arc;
    private Point3D _TrakInters; //track intersection with the cluster

    private int _MinStrip;			// the min strip number in the cluster
    private int _MaxStrip;			// the max strip number in the cluster
    private FittedHit _Seed;		// the seed: the strip with largest deposited energy

    private double _SeedResidual;               // residual is doca to seed strip from trk intersection with module plane
    private double _CentroidResidual;           // residual is doca to centroid of cluster to trk inters with module plane

    //added variables for alignment
//    private double _x1; //cluster first end point x
//    private double _y1; //svt cluster first end point y
//    private double _z1; //svt cluster first end point z
//    private double _x2; //svt cluster second end point x
//    private double _y2; //svt cluster second end point y
//    private double _z2; //svt cluster second end point z
//    private double _ox; //bmt cluster arc origin x
//    private double _oy; //bmt cluster arc origin y
//    private double _oz; //bmt cluster arc origin z
//    private double _cx; //bmt cluster arc center x
//    private double _cy; //bmt cluster arc center y
//    private double _cz; //bmt cluster arc center z
//    private double _theta; //bmt cluster arc theta
//    private Line3D _cyl; // bmt axis
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
            if(this.get_Type()==BMTType.C)
                return this.get_Arc().radius();
            else
                return this.get(0).get_Strip().get_Axis().distance(this.getLine()).length();
        }
    }

    public Line3D getAxis() {
        if(this.get_Detector()==DetectorType.BST || true)
            return new Line3D();
        else {
            return this.get(0).get_Strip().get_Axis();
        }
    }
    /**
     * sets energy-weighted parameters; these are the strip centroid
     * (energy-weighted) value, the energy-weighted phi for Z detectors and the
     * energy-weighted z for C detectors
     * @param sgeo
     * @param geo
     */
    public void calc_CentroidParams(SVTGeometry sgeo, BMTGeometry geo) {
        // instantiation of variables
        double stripNumCent = 0;		// cluster Lorentz-angle-corrected energy-weighted strip = centroid
        double stripNumCent0 = 0;		// cluster uncorrected energy-weighted strip = centroid
        double phiCent = 0;			// cluster Lorentz-angle-corrected energy-weighted phi
        double phiErrCent = 0;			// cluster Lorentz-angle-corrected energy-weighted phi error
        double phiCent0 = 0;			// cluster uncorrected energy-weighted phi
        double phiErrCent0 = 0;			// cluster uncorrected energy-weighted phi error
        double zCent = 0;			// cluster energy-weighted z
        double zErrCent = 0;			// cluster energy-weighted z error
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
        double weightedXC = 0;                  // BMT strip centroid positions of arc center
        double weightedY1 = 0;                  // SVT/BMT strip centroid positions of endpoints
        double weightedY2 = 0;                  // SVT/BMT strip centroid positions of endpoints
        double weightedYC = 0;                  // BMT strip centroid positions of arc center
        double weightedZ1 = 0;                  // SVT/BMT strip centroid positions of endpoints
        double weightedZ2 = 0;                  // SVT/BMT strip centroid positions of endpoints
        double weightedZC = 0;                  // BMT strip centroid positions of arc center
        
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
                Point3D stEP1  = null;
                Point3D stEP2  = null;
                Point3D stCent = null;
                // strip energy
                double strpEn = thehit.get_Strip().get_Edep();

                if (this.get_Detector()==DetectorType.BST) {
                   // for the SVT the analysis only uses the centroid
                    strpNb = thehit.get_Strip().get_Strip();
                    stEP1  = thehit.get_Strip().get_Line().origin();
                    stEP2  = thehit.get_Strip().get_Line().end();
                    stCent = thehit.get_Strip().get_Line().midpoint();
                
               }
                else if (this.get_Detector()==DetectorType.BMT) { 
                    
                    if(thehit.newClustering && nbhits>Constants.MAXCLUSSIZE && i>Constants.MAXCLUSSIZE-1) 
                        continue;

                    // for the BMT the analysis distinguishes between C and Z type detectors
                    if (this.get_Type()==BMTType.C) { // C-detectors
                        //strpEn = Math.sqrt(thehit.get_Strip().get_Edep());
                        strpNb = thehit.get_Strip().get_Strip();
                        stEP1  = thehit.get_Strip().get_Arc().origin();
                        stEP2  = thehit.get_Strip().get_Arc().end();
                        stCent = thehit.get_Strip().get_Arc().center();
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
                        stCent = thehit.get_Strip().get_Line().midpoint();
                        // for C detectors the phi of the centroid is calculated for the uncorrected and the Lorentz-angle-corrected centroid
                        weightedPhi += strpEn * thehit.get_Strip().get_Phi();
                        weightedPhiErrSq += (thehit.get_Strip().get_PhiErr()) * (thehit.get_Strip().get_PhiErr());
                        weightedPhi0 += strpEn * thehit.get_Strip().get_Phi0();
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
            stripNumCent  = weightedStrp / totEn;
            stripNumCent0 = weightedStrp0 / totEn;

            //setting final variables, including the ones used for alignment
            //-----------------------------------
            if (this.get_Detector()==DetectorType.BST) { //SVT
                
                this.setLine(new Line3D(weightedX1/totEn, weightedY1/totEn, weightedZ1/totEn, weightedX2/totEn, weightedY2/totEn, weightedZ2/totEn));
                Vector3D l = new Vector3D(this.getLine().direction().asUnit());
                
//                //Vector3D s = sgeo.getPlaneModuleOrigin(this.get_Sector(), this.get_Layer()).
//                //        vectorTo(sgeo.getPlaneModuleEnd(this.get_Sector(), this.get_Layer())).asUnit();
//                double[][] Xi = sgeo.getStripEndPoints(1, (this.get_Layer() - 1) % 2);
//                double[][] Xf = sgeo.getStripEndPoints(100, (this.get_Layer() - 1) % 2);
//                Point3D EPi = sgeo.transformToFrame(this.get_Sector(), this.get_Layer(), Xi[0][0], 0, Xi[0][1], "lab", "");
//                Point3D EPf = sgeo.transformToFrame(this.get_Sector(), this.get_Layer(), Xf[0][0], 0, Xf[0][1], "lab", "");
//                
//                Vector3D se = EPi.vectorTo(EPf).asUnit(); // in direction of increasing strips
                Vector3D n = sgeo.findBSTPlaneNormal(this.get_Sector(), this.get_Layer());
                Vector3D s = l.cross(n).asUnit();
                
                this.setL(l);
                this.setS(s);
                this.setN(n);
            }
            else if (this.get_Detector()==DetectorType.BMT) { //BMT 
                // for the BMT the analysis distinguishes between C and Z type detectors
                if (this.get_Type()==BMTType.C) { // C-detectors
                    Point3D  origin = new Point3D(weightedX1/totEn, weightedY1/totEn, weightedZ1/totEn);
                    Point3D  center = new Point3D(weightedXC/totEn, weightedYC/totEn, weightedZC/totEn);
                    Vector3D normal = this.get(0).get_Strip().get_Arc().normal();
                    double   theta  = this.get(0).get_Strip().get_Arc().theta();
                    this.set_Arc(new Arc3D(origin,center,normal,theta));

                    Vector3D s = this.get_Arc().normal();
                    Vector3D n = this.get_Arc().bisect();
                    Vector3D l = s.cross(n).asUnit();
                    
                    this.setL(l);
                    this.setS(s);
                    this.setN(n);
                    
                    zCent    = weightedZ/totEn;
                    zErrCent = Math.sqrt(weightedZErrSq);
                    
                    _Z = zCent;
                    _ZErr = zErrCent;
                    this.set_Error(zErrCent);
                }
                if (this.get_Type()==BMTType.Z) { // Z-detectors
            
                    //phiCent = geo.LorentzAngleCorr(phiCent0,this.get_Layer());
                    phiCent = weightedPhi / totEn;
                    phiCent0 = weightedPhi0 / totEn;
                    //zCent = weightedZ / totEn;
                    phiErrCent = Math.sqrt(weightedPhiErrSq);
                    phiErrCent0 = Math.sqrt(weightedPhiErrSq0);
                
                    set_Centroid0(stripNumCent0);
                    _Phi = phiCent;
                    _PhiErr = phiErrCent;
                    this.set_Error(geo.getRadiusMidDrift(_Layer)*phiErrCent);
                    set_Phi0(phiCent0);
                    set_PhiErr0(phiErrCent0);
                    
                    // for Z detectors Lorentz-correction is applied to the strip
                    this.setLine(new Line3D(weightedX1/totEn, weightedY1/totEn, weightedZ1/totEn, weightedX2/totEn, weightedY2/totEn, weightedZ2/totEn));
                    
                    Vector3D l = new Vector3D(this.getLine().direction().asUnit());
                                    
                    Vector3D n = geo.getAxis(_Layer, _Sector).distance(_Line).direction().asUnit();
                    Vector3D s = l.cross(n).asUnit();
                   
                    this.setL(l);
                    this.setS(s);
                    this.setN(n);
                   
                }
            }
        }

        _TotalEnergy = totEn;
        _Centroid = stripNumCent;
    }

//    
//    public Vector3D getNFromTraj(double x, double y, double z, Line3D cln) {
//        Point3D trk = new Point3D(x,y,z);
//        Point3D Or = cln.distance(new Point3D(x,y,z)).origin();
//        Vector3D n = Or.vectorTo(trk).asUnit();
//        return n;
//    }
        
    public double get_Centroid() {
        return _Centroid;
    }

    public void set_Centroid(double _Centroid) {
        this._Centroid = _Centroid;
    }
    
    public double get_CentroidError() {
        return _CentroidError;
    }

    public void set_CentroidError(double _CentroidE) {
        this._CentroidError = _CentroidE;
    }
    public double get_Error() {
        return _Error;
    }

    public void set_Error(double E) {
        this._Error = E;
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
                return this.get_Arc().center();
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
        if(this.get_Detector()==DetectorType.BST)
            ;
        else {
            Point3D local = new Point3D(traj);
            this.toLocal().apply(local);
            if(this.get_Type()==BMTType.C)                
                value = local.z()-this.get_Z();
            else
                ;
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

    /**
     *
     * @return cluster info. about location and number of hits contained in it
     */
    public void printInfo() {
        String s = " cluster: Detector " + this.get_Detector().getName() +"  Detector Type " + this.get_Type().getName() + " ID " + this.get_Id() + " Sector " + this.get_Sector() 
                + " Layer " + this.get_Layer() + " Radius " + this.getRadius()+ " Size " + this.size() +" centroid "+this.get_Centroid();
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

    private int AssociatedTrackID = -1; // the track ID associated with that hit

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
