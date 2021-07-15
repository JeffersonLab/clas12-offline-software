package org.jlab.rec.cvt.cluster;

import java.util.ArrayList;
import org.jlab.clas.tracking.kalmanfilter.helical.StateVecs;
import org.jlab.geom.prim.Cylindrical3D;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

import org.jlab.rec.cvt.hit.FittedHit;
import org.jlab.rec.cvt.hit.Hit;

import java.util.Collections;
import org.jlab.rec.cvt.bmt.Constants;
/**
 * A cluster in the BST consists of an array of hits that are grouped together
 * according to the algorithm of the ClusterFinder class
 *
 * @author ziegler
 *
 */
public class Cluster extends ArrayList<FittedHit> implements Comparable<Cluster> {

    private static final long serialVersionUID = 9153980362683755204L;

    private int _Detector;							//              The detector SVT or BMT
    private int _DetectorType;                                                  //              The detector type  for BMT C or Z
    private int _Sector;      							//	        sector[1...]
    private int _Layer;    	 						//	        layer [1,...]
    private int _Id;								//		cluster Id
    private double _Centroid; 							// 		after LC (Lorentz Correction)
    private double _CentroidError;
    private double _Error;                                              //              strip resolution    
    private double _Centroid0; 							// 		before LC
    private double _TotalEnergy;
    private double _Phi;  							// 		for Z-detectors
    private double _PhiErr;
    private double _Phi0;  							// 		for Z-detectors before LC
    private double _PhiErr0;
    private double _Z;    							// 		for C-detectors
    private double _ZErr;
    private Point3D _TrakInters; //track intersection with the cluster

    //added variables for alignment
    private double _x1; //cluster first end point x
    private double _y1; //svt cluster first end point y
    private double _z1; //svt cluster first end point z
    private double _x2; //svt cluster second end point x
    private double _y2; //svt cluster second end point y
    private double _z2; //svt cluster second end point z
    private double _ox; //bmt cluster arc origin x
    private double _oy; //bmt cluster arc origin y
    private double _oz; //bmt cluster arc origin z
    private double _cx; //bmt cluster arc center x
    private double _cy; //bmt cluster arc center y
    private double _cz; //bmt cluster arc center z
    private double _theta; //bmt cluster arc theta
    private Line3D _cyl; // bmt axis
    private Vector3D _l; //svt vector along cluster pseudo-strip direction or bmt vector along cluster pseudo-strip direction in the middle of the arc
    private Vector3D _s; //svt vector perpendicular to cluster pseudo-strip direction in the module plane or bmt vector perpendicular to cluster pseudo-strip in direction tangential to the cluster surface in the middle of the arc
    private Vector3D _n; //svt vector normal to the cluster module plane or bmt vector normal to the cluster surface in the middle of the arc
    

    public Cluster(int detector, int detectortype, int sector, int layer, int cid) {
        this._Detector = detector;
        this._DetectorType = detectortype;
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
        return new Cluster(hit.get_Detector(), hit.get_DetectorType(), hit.get_Sector(), hit.get_Layer(), cid);
    }

    public int get_Detector() {
        return _Detector;
    }

    public void set_Detector(int _Detector) {
        this._Detector = _Detector;
    }

    public int get_DetectorType() {
        return _DetectorType;
    }

    public void set_DetectorType(int _DetectorType) {
        this._DetectorType = _DetectorType;
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
     * sets energy-weighted parameters; these are the strip centroid
     * (energy-weighted) value, the energy-weighted phi for Z detectors and the
     * energy-weighted z for C detectors
     */
    public void calc_CentroidParams(org.jlab.rec.cvt.svt.Geometry sgeo, 
            org.jlab.rec.cvt.bmt.BMTGeometry geo) {
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
        double weightedZErrSq = 0;		// Err^2 on  energy-weighted z of the strip
        double weightedX1 = 0;                  // SVT/BMT strip centroid positions of endpoints
        double weightedX2 = 0;                  // SVT/BMT strip centroid positions of endpoints
        double weightedXC = 0;                  // BMT strip centroid positions of arc center
        double weightedY1 = 0;                  // SVT/BMT strip centroid positions of endpoints
        double weightedY2 = 0;                  // SVT/BMT strip centroid positions of endpoints
        double weightedYC = 0;                  // BMT strip centroid positions of arc center
        double weightedZ1 = 0;                  // SVT/BMT strip centroid positions of endpoints
        double weightedZ2 = 0;                  // SVT/BMT strip centroid positions of endpoints
        double weightedZC = 0;                  // BMT strip centroid positions of arc center
        
        /*
        this.set_ImplantPoint(arcLine.origin());
            this.set_MidPoint(arcLine.center());
            this.set_EndPoint(arcLine.end());
            this.set_StripDir(arcLine.normal());
        */
        int nbhits = this.size();
        //sort for bmt detector
        //this.sort(Comparator.comparing(FittedHit.get_Strip()::get_Edep).thenComparing(FittedHit.get_Strip()::get_Edep));
        Collections.sort(this);
        if (nbhits != 0) {
            int min = 1000000;
            int max = -1;
            int seed = -1;
            double Emax = -1;
           
            // looping over the number of hits in the cluster
            for (int i = 0; i < nbhits; i++) {
                FittedHit thehit = this.get(i);
                // gets the energy value of the strip
                double strpEn = -1;
                int strpNb = -1;
                int strpNb0 = -1; //before LC
                if (this.get_Detector()==0) {
                    strpEn = thehit.get_Strip().get_Edep();
                    // for the SVT the analysis only uses the centroid
                    strpNb = thehit.get_Strip().get_Strip();
                    Point3D stEP1 = thehit.get_Strip().get_ImplantPoint();
                    Point3D stEP2 = thehit.get_Strip().get_EndPoint();
                    weightedX1 += strpEn * stEP1.x();
                    weightedY1 += strpEn * stEP1.y();
                    weightedZ1 += strpEn * stEP1.z();
                    weightedX2 += strpEn * stEP2.x();
                    weightedY2 += strpEn * stEP2.y();
                    weightedZ2 += strpEn * stEP2.z();
                }
                if (this.get_Detector()==1) { 
                    if(Constants.newClustering) {
                        strpEn = Math.sqrt(thehit.get_Strip().get_Edep());
                    } else {
                        //strpEn = thehit.get_Strip().get_Edep();
                        strpEn = 1;
                    }
                    if(Constants.newClustering && nbhits>2 && i>1) 
                        continue;
                    
                    //end points:
                    Point3D stEP1 = thehit.get_Strip().get_ImplantPoint();
                    Point3D stEP2 = thehit.get_Strip().get_EndPoint();
                    Point3D stCent = thehit.get_Strip().get_MidPoint();
                    weightedX1 += strpEn * stEP1.x();
                    weightedY1 += strpEn * stEP1.y();
                    weightedZ1 += strpEn * stEP1.z();
                    weightedX2 += strpEn * stEP2.x();
                    weightedY2 += strpEn * stEP2.y();
                    weightedZ2 += strpEn * stEP2.z();
                    weightedXC += strpEn * stCent.x();
                    weightedYC += strpEn * stCent.y();
                    weightedZC += strpEn * stCent.z();

                    // for the BMT the analysis distinguishes between C and Z type detectors
                    if (this.get_DetectorType()==0) { // C-detectors
                        //strpEn = Math.sqrt(thehit.get_Strip().get_Edep());
                        strpNb = thehit.get_Strip().get_Strip();
                        // for C detector the Z of the centroid is calculated
                        weightedZ += strpEn * thehit.get_Strip().get_Z();
                        weightedZErrSq += (thehit.get_Strip().get_ZErr()) * (thehit.get_Strip().get_ZErr());
                        
                    }
                    if (this.get_DetectorType()==1) { // Z-detectors
                        // for Z detectors Lorentz-correction is applied to the strip
                        strpNb = thehit.get_Strip().get_LCStrip();
                        strpNb0 = thehit.get_Strip().get_Strip();
                        // for C detectors the phi of the centroid is calculated for the uncorrected and the Lorentz-angle-corrected centroid
                        weightedPhi += strpEn * thehit.get_Strip().get_Phi();
                        weightedPhiErrSq += (thehit.get_Strip().get_PhiErr()) * (thehit.get_Strip().get_PhiErr());
                        weightedPhi0 += strpEn * thehit.get_Strip().get_Phi0();
                        weightedPhiErrSq0 += (thehit.get_Strip().get_PhiErr0()) * (thehit.get_Strip().get_PhiErr0());
                        
                    }
                    
                }

                totEn += strpEn;
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
                if (strpEn >= Emax) {
                    Emax = strpEn;
                    seed = strpNb;
                    if (this.get_DetectorType()==1) {
                        seed = strpNb0;
                    }
                }

            }
            if (totEn == 0) {
                System.err.println(" Cluster energy is null .... exit "+this._Detector+" "+this._DetectorType);
                
                return;
            }

            this.set_MinStrip(min);
            this.set_MaxStrip(max);
            this.set_SeedStrip(seed);
            this.set_SeedEnergy(Emax);
            // calculates the centroid values and associated errors
            stripNumCent = weightedStrp / totEn;
            stripNumCent0 = weightedStrp0 / totEn;
            phiCent = weightedPhi / totEn;
            //phiCent = geo.LorentzAngleCorr(phiCent0,this.get_Layer());
            phiCent0 = weightedPhi0 / totEn;
            zCent = weightedZ / totEn;
            phiErrCent = Math.sqrt(weightedPhiErrSq);
            phiErrCent0 = Math.sqrt(weightedPhiErrSq0);
            zErrCent = Math.sqrt(weightedZErrSq);
            this.setEndPoint1(new Point3D(weightedX1/totEn, weightedY1/totEn, weightedZ1/totEn));
            this.setEndPoint2(new Point3D(weightedX2/totEn, weightedY2/totEn, weightedZ2/totEn));
            //setting variables used for alignment
            //-----------------------------------
            if (this.get_Detector()==0) { //SVT
                this.setX1(weightedX1/totEn);
                this.setY1(weightedY1/totEn);
                this.setZ1(weightedZ1/totEn);
                this.setX2(weightedX2/totEn);
                this.setY2(weightedY2/totEn);
                this.setZ2(weightedZ2/totEn);
                
                Vector3D l = new Vector3D(weightedX2/totEn-weightedX1/totEn,
                                          weightedY2/totEn-weightedY1/totEn, 
                                          weightedZ2/totEn-weightedZ1/totEn).asUnit();
                
                //Vector3D s = sgeo.getPlaneModuleOrigin(this.get_Sector(), this.get_Layer()).
                //        vectorTo(sgeo.getPlaneModuleEnd(this.get_Sector(), this.get_Layer())).asUnit();
                double[][] Xi = sgeo.getStripEndPoints(1, (this.get_Layer() - 1) % 2);
                double[][] Xf = sgeo.getStripEndPoints(100, (this.get_Layer() - 1) % 2);
                Point3D EPi = sgeo.transformToFrame(this.get_Sector(), this.get_Layer(), Xi[0][0], 0, Xi[0][1], "lab", "");
                Point3D EPf = sgeo.transformToFrame(this.get_Sector(), this.get_Layer(), Xf[0][0], 0, Xf[0][1], "lab", "");
                
                Vector3D se = EPi.vectorTo(EPf).asUnit(); // in direction of increasing strips
                Vector3D n = sgeo.findBSTPlaneNormal(this.get_Sector(), this.get_Layer());
                Vector3D s = l.cross(n).asUnit();
                
                this.setL(l);
                this.setS(s);
                this.setN(n);
            }
            if (this.get_Detector()==1) { //BMT 
                Cylindrical3D cyl = geo.getCylinder(this.get_Layer(), this.get_Sector()); 
                this.setCylAxis(geo.getAxis(this.get_Layer(), this.get_Sector()));
                // for the BMT the analysis distinguishes between C and Z type detectors
                if (this.get_DetectorType()==0) { // C-detectors
                    this.setOx(weightedX1/totEn);
                    this.setOy(weightedY1/totEn);
                    this.setOz(weightedZ1/totEn);
                    this.setCx(weightedXC/totEn);
                    this.setCy(weightedYC/totEn);
                    this.setCz(weightedZC/totEn);
//                    Vector3D C2P1 = new Vector3D(weightedX1/totEn-weightedXC/totEn,
//                                                weightedY1/totEn-weightedYC/totEn, 
//                                                weightedZ1/totEn-weightedZC/totEn).asUnit();
//                    Vector3D C2P2 = new Vector3D(weightedX2/totEn-weightedXC/totEn,
//                                                weightedY2/totEn-weightedYC/totEn, 
//                                                weightedZ2/totEn-weightedZC/totEn).asUnit();
                    
                    double theta = Math.acos(this.getN(weightedX1/totEn, weightedY1/totEn, weightedZ1/totEn, this.getCylAxis())
                            .dot(this.getN(weightedX2/totEn, weightedY2/totEn, weightedZ2/totEn, this.getCylAxis())));
                    this.setTheta(theta);
                    
                    //Vector3D s = C2P1.cross(C2P2).asUnit();
                    Vector3D s = this.getCylAxis().direction().asUnit();
                    Vector3D n = cyl.baseArc().normal();
                    Vector3D l = s.cross(n).asUnit();
                    
                    this.setL(l);
                    this.setS(s);
                    this.setN(n);
                    
                }
                if (this.get_DetectorType()==1) { // Z-detectors
                    // for Z detectors Lorentz-correction is applied to the strip
                    this.setX1(weightedX1/totEn);
                    this.setY1(weightedY1/totEn);
                    this.setZ1(weightedZ1/totEn);
                    this.setX2(weightedX2/totEn);
                    this.setY2(weightedY2/totEn);
                    this.setZ2(weightedZ2/totEn);
                    
                    Vector3D l = this.getCylAxis().direction().asUnit();
                    //this.getCylAxis().setOrigin(this.getCylAxis().origin().x(), this.getCylAxis().origin().y(), weightedZ1/totEn);
                    //this.getCylAxis().setEnd(this.getCylAxis().end().x(), this.getCylAxis().end().y(), weightedZ2/totEn);
                    Vector3D n = this.getN(weightedX1/totEn, weightedY1/totEn, weightedZ1/totEn, this.getCylAxis());
                    //Vector3D n = cyl.baseArc().center().vectorTo(new Point3D((weightedX2/totEn+weightedX1/totEn)/2,
                    //                            (weightedY2/totEn+weightedY1/totEn)/2, 
                    //                            (weightedZ2/totEn+weightedZ1/totEn)/2)).asUnit();
                    
                    
                    Vector3D s = l.cross(n).asUnit();
                   
                    this.setL(l);
                    this.setS(s);
                    this.setN(n);
                   //System.out.println("bmtz"+l.toString()+" "+s.toString()+" "+n.toString()+" \n"+s.cross(l).toString()+" "+s.cross(n).toString());
                   
                }
            }
           
            //phiErrCent = Math.sqrt(weightedPhiErrSq);
            //phiErrCent0 = Math.sqrt(weightedPhiErrSq0);
            //zErrCent = Math.sqrt(weightedZErrSq);
            //System.out.println("end Points "+this.getEndPoint1().toString()+this.getEndPoint2().toString()+" for"); this.printInfo();
        }

        _TotalEnergy = totEn;
        _Centroid = stripNumCent;
        if (this.get_DetectorType() == 1) {
            set_Centroid0(stripNumCent0);
            _Phi = phiCent;
            _PhiErr = phiErrCent;
           // this.set_Error(geo.getRadius(_Layer)*phiErrCent);
            set_Phi0(phiCent0);
            set_PhiErr0(phiErrCent0);
        }
        if (this.get_DetectorType() == 0) {
            _Z = zCent;
            _ZErr = zErrCent;
            this.set_Error(zErrCent);
        }

    }

    
    public Vector3D getN(double x, double y, double z, Line3D cln) {
        Point3D trk = new Point3D(x,y,z);
        Point3D Or = cln.distance(new Point3D(x,y,z)).origin();
        Vector3D n = Or.vectorTo(trk).asUnit();
        
        return n;
    }
        
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

    public double get_TotalEnergy() {
        return _TotalEnergy;
    }

    public void set_TotalEnergy(double _TotalEnergy) {
        this._TotalEnergy = _TotalEnergy;
    }

    private int _MinStrip;			// the min strip number in the cluster
    private int _MaxStrip;			// the max strip number in the cluster
    private int _SeedStrip;			// the seed: the strip with largest deposited energy
    private double _SeedEnergy;                 // the deposited energy of the seed

    private double _SeedResidual;               // residual is doca to seed strip from trk intersection with module plane
    private double _CentroidResidual;           // residual is doca to centroid of cluster to trk inters with module plane
    
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

    public int get_SeedStrip() {
        return _SeedStrip;
    }

    public void set_SeedStrip(int _SeedStrip) {
        this._SeedStrip = _SeedStrip;
    }

    public double get_SeedEnergy() {
        return _SeedEnergy;
    }

    public void set_SeedEnergy(double _SeedEnergy) {
        this._SeedEnergy = _SeedEnergy;
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
        String s = " cluster: Detector " + this.get_Detector() +"  Detector Type " + this.get_DetectorType() + " ID " + this.get_Id() + " Sector " + this.get_Sector() + " Layer " + this.get_Layer() + " Size " + this.size() +" centroid "+this.get_Centroid();
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
    public double get_ResolutionAlongZ(double Z, org.jlab.rec.cvt.svt.Geometry geo) {

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
        double this_phi = PhiInRange(this.get(0).get_Strip().get_ImplantPoint().toVector3D().phi());
        double arg_phi = PhiInRange(arg.get(0).get_Strip().get_ImplantPoint().toVector3D().phi());

        int CompPhi = this_phi < arg_phi ? -1 : this_phi == arg_phi ? 0 : 1;
        int CompLay = this._Layer < arg._Layer ? -1 : this._Layer == arg._Layer ? 0 : 1;
        int CompId = this._SeedStrip < arg._SeedStrip ? -1 : this._SeedStrip == arg._SeedStrip ? 0 : 1;

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

    /**
     * @return the _EndPoint1
     */
    public Point3D getEndPoint1() {
        return _EndPoint1;
    }

    /**
     * @param _EndPoint1 the _EndPoint1 to set
     */
    public void setEndPoint1(Point3D _EndPoint1) {
        this._EndPoint1 = _EndPoint1;
    }

    /**
     * @return the _EndPoint2
     */
    public Point3D getEndPoint2() {
        return _EndPoint2;
    }

    /**
     * @param _EndPoint2 the _EndPoint2 to set
     */
    public void setEndPoint2(Point3D _EndPoint2) {
        this._EndPoint2 = _EndPoint2;
    }

    /**
     * @return the _x1
     */
    public double getX1() {
        return _x1;
    }

    /**
     * @param _x1 the _x1 to set
     */
    public void setX1(double _x1) {
        this._x1 = _x1;
    }

    /**
     * @return the _y1
     */
    public double getY1() {
        return _y1;
    }

    /**
     * @param _y1 the _y1 to set
     */
    public void setY1(double _y1) {
        this._y1 = _y1;
    }

    /**
     * @return the _z1
     */
    public double getZ1() {
        return _z1;
    }

    /**
     * @param _z1 the _z1 to set
     */
    public void setZ1(double _z1) {
        this._z1 = _z1;
    }

    /**
     * @return the _x2
     */
    public double getX2() {
        return _x2;
    }

    /**
     * @param _x2 the _x2 to set
     */
    public void setX2(double _x2) {
        this._x2 = _x2;
    }

    /**
     * @return the _y2
     */
    public double getY2() {
        return _y2;
    }

    /**
     * @param _y2 the _y2 to set
     */
    public void setY2(double _y2) {
        this._y2 = _y2;
    }

    /**
     * @return the _z2
     */
    public double getZ2() {
        return _z2;
    }

    /**
     * @param _z2 the _z2 to set
     */
    public void setZ2(double _z2) {
        this._z2 = _z2;
    }

    /**
     * @return the _ox
     */
    public double getOx() {
        return _ox;
    }

    /**
     * @param _ox the _ox to set
     */
    public void setOx(double _ox) {
        this._ox = _ox;
    }

    /**
     * @return the _oy
     */
    public double getOy() {
        return _oy;
    }

    /**
     * @param _oy the _oy to set
     */
    public void setOy(double _oy) {
        this._oy = _oy;
    }

    /**
     * @return the _oz
     */
    public double getOz() {
        return _oz;
    }

    /**
     * @param _oz the _oz to set
     */
    public void setOz(double _oz) {
        this._oz = _oz;
    }

    /**
     * @return the _cx
     */
    public double getCx() {
        return _cx;
    }

    /**
     * @param _cx the _cx to set
     */
    public void setCx(double _cx) {
        this._cx = _cx;
    }

    /**
     * @return the _cy
     */
    public double getCy() {
        return _cy;
    }

    /**
     * @param _cy the _cy to set
     */
    public void setCy(double _cy) {
        this._cy = _cy;
    }

    /**
     * @return the _cz
     */
    public double getCz() {
        return _cz;
    }

    /**
     * @param _cz the _cz to set
     */
    public void setCz(double _cz) {
        this._cz = _cz;
    }

    /**
     * @return the _theta
     */
    public double getTheta() {
        return _theta;
    }

    /**
     * @param _theta the _theta to set
     */
    public void setTheta(double _theta) {
        this._theta = _theta;
    }

    /**
     * @return the _cyl
     */
    public Line3D getCylAxis() {
        return _cyl;
    }

    /**
     * @param _cyl the _cyl to set
     */
    public void setCylAxis(Line3D _cyl) {
        this._cyl = _cyl;
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


    private Point3D _EndPoint1;
    private Point3D _EndPoint2;
    
    
}
