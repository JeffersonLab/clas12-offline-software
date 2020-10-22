package org.jlab.rec.cvt.cluster;

import java.util.ArrayList;
import org.jlab.geom.prim.Point3D;

import org.jlab.rec.cvt.hit.FittedHit;
import org.jlab.rec.cvt.hit.Hit;

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
    public void calc_CentroidParams(org.jlab.rec.cvt.bmt.Geometry geo) {
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
        double weightedX1 = 0;                  // SVT strip centroid positions of endpoints
        double weightedX2 = 0;                  // SVT strip centroid positions of endpoints
        double weightedY1 = 0;                  // SVT strip centroid positions of endpoints
        double weightedY2 = 0;                  // SVT strip centroid positions of endpoints
        double weightedZ1 = 0;                  // SVT strip centroid positions of endpoints
        double weightedZ2 = 0;                  // SVT strip centroid positions of endpoints
        
        int nbhits = this.size();

        if (nbhits != 0) {
            int min = 1000000;
            int max = -1;
            int seed = -1;
            double Emax = -1;
            // looping over the number of hits in the cluster
            for (int i = 0; i < nbhits; i++) {
                FittedHit thehit = this.get(i);
                // gets the energy value of the strip
                double strpEn = thehit.get_Strip().get_Edep();
                
                int strpNb = -1;
                int strpNb0 = -1; //before LC
                if (this.get_Detector()==0) {
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
                    // for the BMT the analysis distinguishes between C and Z type detectors
                    if (this.get_DetectorType()==0) { // C-detectors
                        strpNb = thehit.get_Strip().get_Strip();
                        // for C detector the Z of the centroid is calculated
                        weightedZ += strpEn * thehit.get_Strip().get_Z();
                        weightedZErrSq += (thehit.get_Strip().get_ZErr()) * (thehit.get_Strip().get_ZErr());
                    }
                    if (this.get_DetectorType()==1) { // Z-detectors
                        // for Z detectors Larentz-correction is applied to the strip
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
            //phiErrCent = Math.sqrt(weightedPhiErrSq);
            //phiErrCent0 = Math.sqrt(weightedPhiErrSq0);
            //zErrCent = Math.sqrt(weightedZErrSq);
        }

        _TotalEnergy = totEn;
        _Centroid = stripNumCent;
        if (this.get_DetectorType() == 1) {
            set_Centroid0(stripNumCent0);
            _Phi = phiCent;
            _PhiErr = phiErrCent;

            set_Phi0(phiCent0);
            set_PhiErr0(phiErrCent0);
        }
        if (this.get_DetectorType() == 0) {
            _Z = zCent;
            _ZErr = zErrCent;
        }

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

    private Point3D _EndPoint1;
    private Point3D _EndPoint2;
    
    
}
