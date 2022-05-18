package org.jlab.rec.dc.hit;

import eu.mihosoft.vrl.v3d.Vector3d;
import org.jlab.clas.clas.math.FastMath;
import org.jlab.clas.swimtools.Swimmer;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.geom.prim.Line3D;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.timetodistance.TimeToDistanceEstimator;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.dc.trajectory.StateVec;
import org.jlab.utils.groups.IndexedTable;
/**
 * A hit that was used in a fitted cluster. It extends the Hit class and
 * contains local and sector coordinate information at the MidPlane. An estimate
 * for the Left-right Ambiguity is assigned based on the linear fit to the wire
 * position residual.
 *
 * @author ziegler
 *
 */
public class FittedHit extends Hit implements Comparable<Hit> {
    
    private double _X;              	// X at Z in local coord. system
    private double _XMP;            	// X at the MidPlane in sector coord. system
    private double _Z;              	// Z in the sector coord. system
    private double _lX;			// X in local coordinate system used in hit-based fit to cluster line
    private double _lY;			// Y in local coordinate system used in hit-based fit to cluster line
    private double _Residual;		// cluster line  to the wire position resid
    private double _TimeResidual = 0;	// cluster line  to the wire position time-resid
    private int _LeftRightAmb;		// Left-Right Ambiguity value	-1 --> y-fit <0 --> to the left of the wire ==> y = y-_leftRight*TimeToDist

    private int _QualityFac;
    private int _TrkgStatus = -1;	//  TrkgStatusFlag factor (-1: no fit; 0: hit-based trking fit; 1: time-based trking fit)
    private double _ClusFitDoca = -1;
    private double _TrkFitDoca = -1;
    private double _TimeToDistance = 0;
    private double _Beta = 1.0;
   
    private StateVec _AssociatedStateVec;
    private double _Doca;							//         Reconstructed doca, for now it is using the linear parametrization that is in  gemc 
    //private double _DocaErr;      						//	   Error on doca
    private double _B;								// 	   B-field at hit location
    private int _Id;
    public int _lr;
    private int _AssociatedClusterID = -1;
    public boolean RemoveFlag = false;
    private int _AssociatedHBTrackID = -1;
    private int _AssociatedTBTrackID = -1;
    
    public int betaFlag = 0; //0 = OK; -1 = negative; 1 = less than lower cut (0.15); 2 = greater than 1.15
    
    // intersection of cross direction line with the hit wire (TCS)
    private Point3D CrossDirIntersWire;
    private double _SignalPropagAlongWire;
    private double _SignalPropagTimeAlongWire;
    private double _SignalTimeOfFlight;
    private double _T0;
    private double _tFlight;
    private double _tProp;
    private double _tStart;   // The event start time
    private double _Time;     //Time = TDC - tFlight - tProp - T0 - TStart
    /**
     * identifying outoftimehits;
     */
    private boolean _OutOfTimeFlag;
    /**
     *
     * @param sector (1...6)
     * @param superlayer (1...6)
     * @param layer (1...6)
     * @param wire (1...112)
     * @param TDC
     * @param id
     */
    public FittedHit(int sector, int superlayer, int layer, int wire,
            int TDC, int id) {
        super(sector, superlayer, layer, wire, TDC, id);
        
        this.set_lX(layer);
        this.set_lY(layer, wire);
    }

    /**
     * 
     * @return B at location along wire
     */
    public double getB() {
        return _B;
    }
    /**
     * 
     * @param _B B field intensity in T
     */
    public void setB(double _B) {
        this._B = _B;
    }

    /**
     *
     * @return the ID
     */
    @Override
    public int get_Id() {
        return _Id;
    }

    /**
     * Sets the hit ID. The ID corresponds to the hit index in the EvIO column.
     *
     * @param _Id
     */
    @Override
    public void set_Id(int _Id) {
        this._Id = _Id;
    }										//		Hit Id
    
    /**
     * 
     * @return calc doca in cm 
     */
    public double get_Doca() {
        return _Doca;
    }
    /**
     * 
     * @param _Doca doca in cm
     */
    public void set_Doca(double _Doca) {
        this._Doca = _Doca;
    }

    
    /**
     *
     * @return the local hit x-position in the local superlayer coordinate
     * system; used in cluster-finding algo. to fit the hit-based wire positions
     */
    public double get_lX() {
        return _lX;
    }

    /**
     *
     * @param layerValue layer number from 1 to 6
     */
    public final void set_lX(double layerValue) {
        this._lX = layerValue;
    }

    /**
     *
     * @return the local hit y-position in the local superlayer coordinate
     * system; used in cluster-finding algo. to fit the hit-based wire positions
     */
    public double get_lY() {
        return _lY;
    }

    /**
     *
     * @param layer layer number from 1 to 6
     * @param wire wire number from 1 to 112 sets the center of the cell as a
     * function of wire number in the local superlayer coordinate system.
     */
    public final void set_lY(int layer, int wire) {
        double y = this.calcLocY(layer, wire);
        this._lY = y;
    }

    /**
     *
     * @param event
     * @param B
     * @param constants0
     * @param constants1
     * @param tde
     * @return The approximate uncertainty on the hit position using the inverse
     * of the gemc smearing function
     */
    public double get_PosErr(DataEvent event, double B, IndexedTable constants0, IndexedTable constants1, TimeToDistanceEstimator tde) {

        double err = Constants.CELLRESOL; // default

        if (this._TrkgStatus != -1) {
            if (this.get_TimeToDistance() == 0) // if the time-to-dist is not set ... set it
            {
                set_TimeToDistance(event, 0.0, B, constants1, tde);
            }

            double x = this.get_Doca() / this.get_CellSize();
            if(event.hasBank("MC::Particle") ||
                    event.getBank("RUN::config").getInt("run", 0) < 100 ) { // for MC use functional form put in simulation
                
                double p1 = constants0.getDoubleValue("parameter1", this.get_Sector(),this.get_Superlayer(),0);
                double p2 = constants0.getDoubleValue("parameter2", this.get_Sector(),this.get_Superlayer(),0);
                double p3 = constants0.getDoubleValue("parameter3", this.get_Sector(),this.get_Superlayer(),0);
                double p4 = constants0.getDoubleValue("parameter4", this.get_Sector(),this.get_Superlayer(),0);
                double scale = constants0.getDoubleValue("scale", this.get_Sector(),this.get_Superlayer(),0);

                err = (p1 + p2 / ((p3 + x) * (p3 + x)) + p4 * Math.pow(x, 8)) * scale * 0.1; //gives a reasonable approximation to the measured CLAS resolution (in cm! --> scale by 0.1 )
            } else {
                // Mac's new function... to test
                err = 0.06 - 0.14 * Math.pow(x,1.5) + 0.18 * Math.pow(x,2.5);
            }
        }
        
        return err;
    }

    /**
     *
     * @return the time residual |trkDoca| - |Doca| from the fit to the wire positions
     * in the superlayer
     */
    public double get_TimeResidual() {
        return _TimeResidual;
    }

    /**
     *
     * @param _TimeResidual the residual |fit| - |y| from the fit to the hit
     * positions in the superlayer
     */
    public void set_TimeResidual(double _TimeResidual) {
        this._TimeResidual = _TimeResidual;
    }

    /**
     *
     * @return the residual from the fit to the wire positions in the superlayer
     */
    public double get_Residual() {
        return _Residual;
    }

    /**
     *
     * @param _Residual the residual from the fit to the hit positions in the
     * superlayer
     */
    public void set_Residual(double _Residual) {
        this._Residual = _Residual;
    }

    /**
     *
     * @return an integer representative of the estimate of the left-right
     * ambiguity obtained from pattern recognition. -1(+1): the track went to
     * the left(right) of the wire; 0: the left-right ambiguity could not be
     * resolved.
     */
    public int get_LeftRightAmb() {
        return _LeftRightAmb;
    }

    /**
     *
     * @param leftRightAmb an integer representative of the estimate of the
     * left-right ambiguity obtained from pattern recognition. -1(+1): the track
     * went to the left(right) of the wire; 0: the left-right ambiguity could
     * not be resolved.
     */
    public void set_LeftRightAmb(int leftRightAmb) {
        this._LeftRightAmb = leftRightAmb;
    }

    /**
     *
     * @return a quality factor representative of the quality of the fit to the
     * hit
     */
    public int get_QualityFac() {
        return _QualityFac;
    }

    /**
     *
     * @param _QualityFac is a quality factor representative of the quality of
     * the fit to the hit
     */
    public void set_QualityFac(int _QualityFac) {
        this._QualityFac |= 1 << (_QualityFac-1);
    }

    /**
     *
     * @return an integer representative of the stage of the pattern recognition
     * and subsequent KF fit for that hit. -1: the hit has not yet been fit and
     * is the input of hit-based tracking; 0: the hit has been successfully
     * involved in hit-based tracking and has a well-defined time-to-distance
     * value; 1: the hit has been successfully involved in track fitting.
     */
    public int get_TrkgStatus() {
        return _TrkgStatus;
    }

    /**
     *
     * @param trkgStatus is an integer representative of the stage of the
     * pattern recognition and subsequent KF fit for that hit. -1: the hit has
     * not yet been fit and is the input of hit-based tracking; 0: the hit has
     * been successfully involved in hit-based tracking and has a well-defined
     * time-to-distance value; 1: the hit has been successfully involved in
     * track fitting.
     */
    public void set_TrkgStatus(int trkgStatus) {
        _TrkgStatus = trkgStatus;
    }

    /**
     *
     * @return the calculated distance (in cm) from the time (in ns)
     */
    public double get_TimeToDistance() {
        return _TimeToDistance;
    }

    /**
     *
     * @param alpha the local angle of the track
     * @return the reduced angle in radians between the range of 0 and 30 deg.
     */
    double reducedAngle(double alpha) {
        double ralpha = 0;

        ralpha = Math.abs(alpha);

        while (ralpha > Math.PI / 3.) {
            ralpha -= Math.PI / 3.;
        }
        if (ralpha > Math.PI / 6.) {
            ralpha = Math.PI / 3. - ralpha;
        }

        return ralpha;
    }
    /**
     * 
     * @return state vector associated with the hit
     */
    public StateVec getAssociatedStateVec() {
        return _AssociatedStateVec;
    }
    /**
     * 
     * @param _AssociatedStateVec state vector (x,y,tx,ty,q/p) associated with the hit
     */
    public void setAssociatedStateVec(StateVec _AssociatedStateVec) {
        this._AssociatedStateVec = _AssociatedStateVec;
    }
    /**
     * sets the calculated distance (in cm) from the time (in ns)
     * @param event
     * @param trkAngle
     * @param B
     * @param tab
     * @param tde
     */
    public void set_TimeToDistance(DataEvent event, double trkAngle, double B, IndexedTable tab,TimeToDistanceEstimator tde) {     
        
        double distance = 0;
        int slIdx = this.get_Superlayer() - 1;
        int secIdx = this.get_Sector() - 1;
        if (_TrkgStatus != -1 && this.get_Time() > 0) {
            
            //local angle correction
            double theta0 = Math.acos(1-0.02*B);
            double alpha = Math.atan(trkAngle);
            
            // correct alpha with theta0, the angle corresponding to the isochrone lines twist due to the electric field
            //if(event.hasBank("MC::Particle")==false)
            //    alpha-=Swimmer.getTorScale()*theta0;
            alpha-=Swimmer.getTorScale()*theta0;
            
            this.setAlpha(Math.toDegrees(alpha));
            //reduce the corrected angle 
            double ralpha = this.reducedAngle(alpha);
            double beta = this.get_Beta0to1(); 
            double x = this.get_ClusFitDoca();
           
            double deltatime_beta = 0;
            double deltatime_beta2 = 0;
            
            if (x != -1) {
                if(Constants.getInstance().useUSETIMETBETA()==true) {
                    deltatime_beta = calcDeltaTimeBetaTFCN(this.get_Time(), tab, beta);
                    deltatime_beta2 = calcDeltaTimeBeta(this.get_Time(), tab, beta);
                } else {
                    deltatime_beta = calcDeltaTimeBeta(x, tab, beta);
                }
            }
            if(event.hasBank("MC::Particle")==false) {
                distance = tde.interpolateOnGrid(B, Math.toDegrees(ralpha), 
                        this.getCorrectedTime(this.get_Time(), deltatime_beta+deltatime_beta2), 
                        secIdx, slIdx) ;
            } else {
                distance = tde.interpolateOnGrid(B, Math.toDegrees(ralpha), 
                        this.getCorrectedTime(this.get_Time(), 0), 
                        secIdx, slIdx) ;
            }
            //deltatime_beta = calcDeltaTimeBeta(distance, tab, beta);
            //deltatime_beta = calcDeltaTimeBeta(distance, this.get_Superlayer(), beta);
            this.set_DeltaTimeBeta(deltatime_beta);
            //distance = tde.interpolateOnGrid(B, Math.toDegrees(ralpha), this.getCorrectedTime(this.get_Time(), deltatime_beta), secIdx, slIdx) ;
            
        }
     
        this.set_Doca(distance);
        this._TimeToDistance = distance;
    }
    public double getCorrectedTime(double t, double dbt) {
        double correctedTime = t -dbt;
        if(correctedTime<=0)
            correctedTime=0.01; // fixes edge effects ... to be improved
        return correctedTime;
    }
    
    public double calcDeltaTimeBeta(double x, IndexedTable tab, double beta){
        return (Math.sqrt(x * x + (tab.getDoubleValue("distbeta", this.get_Sector(), 
                this.get_Superlayer(),0) * beta * beta) * 
                (tab.getDoubleValue("distbeta", this.get_Sector(), 
                        this.get_Superlayer(),0) * beta * beta)) - x) / Constants.V0AVERAGED;
    }
    
    public double calcDeltaTimeBetaTFCN(double t,IndexedTable tab, double beta){
        double delt = tab.getDoubleValue("c3", this.get_Sector(), 
                        this.get_Superlayer(),0);
        
        //see [CLAS-Note 96-008]
        double tBeta = (0.5 *delt*delt*delt*t)/(delt*delt*delt+t*t*t);
        return tBeta*beta*beta;
    }
    
    
    /**
     * 
     * @return doca to cluster fit line (cm)
     */
    public double get_ClusFitDoca() {
        return _ClusFitDoca;
    }
    /**
     * 
     * @param _ClusFitDoca doca to cluster fit line (cm)
     */
    public void set_ClusFitDoca(double _ClusFitDoca) {
        this._ClusFitDoca = _ClusFitDoca;
    }
    
    /**
     * 
     * @return doca to track trajectory at hit layer plane (cm)
     */
    public double get_TrkFitDoca() {
        return _TrkFitDoca;
    }
    /**
     * 
     * @param _TrkFitDoca doca to track trajectory at hit layer plane (cm)
     */
    public void set_TrkFitDoca(double _TrkFitDoca) {
        this._TrkFitDoca = _TrkFitDoca;
    }
    
    /**
     * 
     * @param cellSize the cell size in cm
     */
    public void fix_TimeToDistance(double cellSize) {
        this._TimeToDistance = cellSize;
    }

    /**
     *
     * @return the hit x-position at the mid-plane (y=0) in the tilted sector
     * coordinate system
     */
    public double get_X() {
        return _X;
    }

    /**
     *
     * @param _X is the hit x-position at the mid-plane (y=0) in the tilted
     * sector coordinate system
     */
    public void set_X(double _X) {
        this._X = _X;
    }

    public double get_XWire() {
        return _XMP;
    }

    public void set_XWire(double _XMP) {
        this._XMP = _XMP;
    }

    /**
     *
     * @return the hit z-position at the mid-plane (y=0) in the tilted sector
     * coordinate system
     */
    public double get_Z() {
        return _Z;
    }

    /**
     *
     * @param _Z is the hit z-position at the mid-plane (y=0) in the tilted
     * sector coordinate system
     */
    public void set_Z(double _Z) {
        this._Z = _Z;
    }

    
    /**
     * A method to update the hit position information after the fit to the
     * local coord.sys.wire positions
     * @param DcDetector
     */
    public void updateHitPosition(DCGeant4Factory DcDetector) {
        if(this.get_Z()==0)
            this.calc_GeomCorr(DcDetector, 0); 
        this.set_X(this.get_XWire());
    }

    /**
     * A method to update the hit position information after the fit to the wire
     * positions employing hit-based tracking algorithms has been performed.
     * @param event
     * @param trkAngle
     * @param B
     * @param tab
     * @param DcDetector
     * @param tde
     */
    public void updateHitPositionWithTime(DataEvent event, double trkAngle, double B, 
            IndexedTable tab, DCGeant4Factory DcDetector, TimeToDistanceEstimator tde) {
        if (this.get_Time() > 0) {
            this.set_TimeToDistance(event, trkAngle, B, tab, tde);
        }
        if(this.get_Z()==0)
            this.calc_GeomCorr(DcDetector, 0); 
        double x = this.get_XWire();
        //this.set_X(x+this.get_LeftRightAmb()*this.get_TimeToDistance());
        double MPCorr = 1;
        double cosTkAng = 1./Math.sqrt(trkAngle*trkAngle + 1.);
        if (cosTkAng > 0.8 & cosTkAng <= 1) {
            MPCorr = cosTkAng;
        }

        this.set_X(x + this.get_LeftRightAmb() * (this.get_TimeToDistance() / MPCorr) );/// FastMath.cos(Math.toRadians(6.)));
        
    }
    public double corrForMidPlaneProjection(double trkAngle, Line3D wireLine, FittedHit hit) {
        double tilt = 90-Math.toDegrees(wireLine.direction().asUnit().angle(new Vector3D(1,0,0)));
        double MPCorr = 1;
        double cosTkAng = 1./Math.sqrt(trkAngle*trkAngle + 1.);
        if (cosTkAng > 0.8 & cosTkAng <= 1) {
            MPCorr = cosTkAng;
        }
        return MPCorr ;
    }
    //public double XatY(DCGeant4Factory DcDetector, double y) {
    //    double x = this.calc_GeomCorr(DcDetector, y);
    //    return x + this.get_LeftRightAmb() * (this.get_TimeToDistance()) ;
    //}
        
    private double _WireLength;

    public double get_WireLength() {
        return _WireLength;
    }

    public void set_WireLength(double _WireLength) {
        this._WireLength = _WireLength;
    }

    private double _WireMaxSag;
    
    public double get_WireMaxSag() {
        return _WireMaxSag;
    }

    public void set_WireMaxSag(double _WireMaxSag) {
        this._WireMaxSag = _WireMaxSag;
    }
    
    private Line3D _WireLine;
    
    public Line3D get_WireLine() {
        return _WireLine;
    }

    public void set_WireLine(Line3D _WireLine) {
        this._WireLine = _WireLine;
    }
    
    private double _TrkResid=999;
    
    public double get_TrkResid() {
        return _TrkResid;
    }

    public void set_TrkResid(double _TrkResid) {
        this._TrkResid = _TrkResid;
    }
    
    public void calc_GeomCorr(DCGeant4Factory DcDetector, double y) {
        //corrects for wire sag only
        double xL = DcDetector.getWireLeftend(this.get_Sector()-1, this.get_Superlayer()-1, this.get_Layer()-1, this.get_Wire()-1).x;
        double xR = DcDetector.getWireRightend(this.get_Sector()-1, this.get_Superlayer()-1, this.get_Layer()-1, this.get_Wire()-1).x;
        double yL = DcDetector.getWireLeftend(this.get_Sector()-1, this.get_Superlayer()-1, this.get_Layer()-1, this.get_Wire()-1).y;
        double yR = DcDetector.getWireRightend(this.get_Sector()-1, this.get_Superlayer()-1, this.get_Layer()-1, this.get_Wire()-1).y;
        double x  = DcDetector.getWireMidpoint(this.get_Sector()-1, this.get_Superlayer()-1, this.get_Layer()-1, this.get_Wire()-1).x;
        double z  = DcDetector.getWireMidpoint(this.get_Sector()-1, this.get_Superlayer()-1, this.get_Layer()-1, this.get_Wire()-1).z;
        double wire = this.get_Wire();
        double wireLen = Math.sqrt((xL-xR)*(xL-xR)+(yL-yR)*(yL-yR));
        int sector = this.get_Sector();
        int A = 0;
        double C = 0;
        double ConvFac = 1000000;
        switch (sector) {
            case (1):
                A=0;
                break;
            case (2):
                A=-1;
                break;
             case (3):
                A=-1;
                break;
            case (4):
                A=0;
                break;
            case (5):
                A=1;
                break;
            case (6):
                A=1;
                break;
            default:
                throw new RuntimeException("invalid sector");
        }
        int region = this.get_Region();
        switch (region) {
            case (1):
                C=2.0/ConvFac;
                break;
            case (2):
                C=4.95/ConvFac;
                break;
             case (3):
                if(wire<69)
                    C=12.5/ConvFac;
                if(wire>68 && wire<92)
                    C=7.49/ConvFac;
                if(wire>91)
                    C=5.98/ConvFac;
                break;
            default:
                throw new RuntimeException("invalid region");
        }    
        
        double MaxSag = Constants.getInstance().getWIREDIST()*A*C*wire*wire*FastMath.cos(Math.toRadians(25.))*FastMath.cos(Math.toRadians(30.));
        
        double delta_x = MaxSag*(1.-Math.abs(y)/(0.5*wireLen))*(1.-Math.abs(y)/(0.5*wireLen));
        
        //x+=delta_x;
        //Line3D wireLine = new Line3D(new Point3D(xL, yL, z), new Point3D(xR, yR, z));
        //wireLine.setOrigin(x, y, z);
        Line3D wireLine = new Line3D(new Point3D(x, 0, z), new Point3D(xR, yR, z));
        
        this.set_WireLength(wireLen);
        this.set_WireMaxSag(MaxSag);
        this.set_WireLine(wireLine);
        
        this.set_XWire(x);
        this.set_Z(z);
    }
    
    /**
     *
     * @param otherHit
     * @return a boolean comparing 2 hits based on basic descriptors; returns
     * true if the hits are the same
     */
    public boolean isSameAs(FittedHit otherHit) {
        FittedHit thisHit = this;
        boolean cmp = false;
        if (thisHit.get_Time() == otherHit.get_Time()
                && thisHit.get_Sector() == otherHit.get_Sector()
                && thisHit.get_Superlayer() == otherHit.get_Superlayer()
                && thisHit.get_Layer() == otherHit.get_Layer()
                && thisHit.get_Wire() == otherHit.get_Wire()) {
            cmp = true;
        }
        return cmp;
    }

    /**
     *
     * @param arg0 the other hit
     * @return an int used to sort a collection of hits by layer number
     */
    public int compareTo(FittedHit arg0) {
        if (this.get_Layer() > arg0.get_Layer()) {
            return 1;
        } else {
            return -1;
        }
    }

    
    /**
     * 
     * @return string with hit output 
     */
    @Override
    public String printInfo() {
        //double xr = this._X*FastMath.cos(Math.toRadians(25.))+this._Z*FastMath.sin(Math.toRadians(25.));		
        //double zr = this._Z*FastMath.cos(Math.toRadians(25.))-this._X*FastMath.sin(Math.toRadians(25.));
        String s = String.format("DC Fitted Hit: ID %d  Sector %d  Superlayer %d  Layer %d  Wire %d  TDC %d  Time %.4f  LR %d  Doca %.4f +/- %.4f\n",
                   this.get_Id(),this.get_Sector(),this.get_Superlayer(),this.get_Layer(),this.get_Wire(),this.get_TDC(),
                   this.get_Time(),this.get_LeftRightAmb(),this.get_TimeToDistance(),this.get_DocaErr());
        s = s +    String.format("               clusID %d  trkID %d  Tflight %.4f  Tprop %.4f  T0 %.4f  TStart %.4f  Beta %.4f  B %.4f\n",
                   this.get_AssociatedClusterID(), this.get_AssociatedHBTrackID(), this.getTFlight(), this.getTProp(), this.getT0(), this.getTStart(), this.get_Beta(), this.getB());
        s = s +    String.format("               clusFitDoca %.4f  X %.4f  Z %.4f", this.get_ClusFitDoca(), this.get_X(), this.get_Z());
        if(this.getCrossDirIntersWire()!=null) s = s + " " +  this.getCrossDirIntersWire().toString();
        return s;
    }

    /**
     * 
     * @return  cluster ID associated with the hit
     */
    public int get_AssociatedClusterID() {
        return _AssociatedClusterID;
    }
    /**
     * 
     * @param _AssociatedClusterID associated cluster ID
     */
    public void set_AssociatedClusterID(int _AssociatedClusterID) {
        this._AssociatedClusterID = _AssociatedClusterID;
    }
 
    /**
     * 
     * @param _id associated track id for Hit-Based tracking
     */
    public void set_AssociatedHBTrackID(int _id) {
        _AssociatedHBTrackID = _id;
    }
    /**
     * 
     * @return track id associated with the hit for Hit-Based tracking
     */
    public int get_AssociatedHBTrackID() {
        return _AssociatedHBTrackID;
    }
    /**
     * 
     * @param _id associated track id for Time-Based tracking
     */
    public void set_AssociatedTBTrackID(int _id) {
        _AssociatedTBTrackID = _id;
    }
    /**
     * 
     * @return track id associated with the hit for Time-Based tracking
     */
    public int get_AssociatedTBTrackID() {
        return _AssociatedTBTrackID;
    }
    /**
     * 
     * @return 
     */
    public Point3D getCrossDirIntersWire() {
        return CrossDirIntersWire;
    }

    public void setCrossDirIntersWire(Point3D CrossDirIntersWire) {
        this.CrossDirIntersWire = CrossDirIntersWire;
    }
    /**
     * 
     * @return beta of track at the hit location 
     */
    public double get_Beta() {
        return _Beta;
    }
    /**
     * 
     * @param beta beta of the track at the hit location (position of the track closest to the wire)
     */
    public void set_Beta(double beta) {
        _Beta = beta;
    }
    /**
     * 
     * @param DcDetector detector geometry
     * @return signal propagation time along the wire in ns
     */
    public double calc_SignalPropagAlongWire(DCGeant4Factory DcDetector) {
        
        Vector3d WireEnd;
        int end = Constants.getInstance().STBLOC[this.get_Sector()-1][this.get_Superlayer()-1];
        if(end>0) {
            WireEnd = DcDetector.getWireRightend(this.get_Sector()-1, this.get_Superlayer() - 1, this.get_Layer() - 1, this.get_Wire() - 1);
        } else {
            WireEnd = DcDetector.getWireLeftend(this.get_Sector()-1, this.get_Superlayer() - 1, this.get_Layer() - 1, this.get_Wire() - 1);
        }
        
        double X = this.getCrossDirIntersWire().x();
        double Y = this.getCrossDirIntersWire().y();
        
        double r2 = (X-WireEnd.x)*(X-WireEnd.x) + (Y-WireEnd.y)*(Y-WireEnd.y);
        
        return Math.sqrt(r2);
    }
    
    public double calc_SignalPropagAlongWire(double X, double Y, DCGeant4Factory DcDetector) {
        
        Vector3d WireEnd;
        int end = Constants.getInstance().STBLOC[this.get_Sector()-1][this.get_Superlayer()-1];
        if(end>0) {
            WireEnd = DcDetector.getWireRightend(this.get_Sector()-1, this.get_Superlayer() - 1, this.get_Layer() - 1, this.get_Wire() - 1);
        } else {
            WireEnd = DcDetector.getWireLeftend(this.get_Sector()-1, this.get_Superlayer() - 1, this.get_Layer() - 1, this.get_Wire() - 1);
        }
        
        double r2 = (X-WireEnd.x)*(X-WireEnd.x) + (Y-WireEnd.y)*(Y-WireEnd.y);
        
        return Math.sqrt(r2);
    }
    /**
     * 
     * @return signal propagation time along the wire in ns
     */
    public double getSignalPropagAlongWire() {
        return _SignalPropagAlongWire;
    }
    /**
     * 
     * @param DcDetector DC detector geometry
     */
    public void setSignalPropagAlongWire(DCGeant4Factory DcDetector) {
        this._SignalPropagAlongWire = this.calc_SignalPropagAlongWire( DcDetector);
    }
    
    /**
     * 
     * @return signal propagation time along the wire in ns
     */
    public double getSignalPropagTimeAlongWire() {
        return _SignalPropagTimeAlongWire;
    }
    /**
     * 
     * @param DcDetector DC detector geometry
     */
    public void setSignalPropagTimeAlongWire(DCGeant4Factory DcDetector) {
        this.setSignalPropagAlongWire( DcDetector);
        this._SignalPropagTimeAlongWire = this._SignalPropagAlongWire/(Constants.SPEEDLIGHT*0.7);
        this._tProp= this._SignalPropagTimeAlongWire;
    }

    /**
     * 
     * @param X
     * @param Y
     * @param DcDetector DC detector geometry
     */
    public void setSignalPropagTimeAlongWire(double X, double Y, DCGeant4Factory DcDetector) {
        this._SignalPropagAlongWire = this.calc_SignalPropagAlongWire(X,Y, DcDetector);
        this._SignalPropagTimeAlongWire = this._SignalPropagAlongWire/(Constants.SPEEDLIGHT*0.7);
        this._tProp= this._SignalPropagTimeAlongWire;
    }

    /**
     * 
     * @return signal time of flight to the track doca to the hit wire in ns
     */
    public double getSignalTimeOfFlight() {
        return _SignalTimeOfFlight;
    }
    /**
     * sets signal time of flight to the track doca to the hit wire in ns
     */
    public void setSignalTimeOfFlight() {
        if(this.get_Beta()>0 && this.getAssociatedStateVec()!=null)
            this._SignalTimeOfFlight = (this.getAssociatedStateVec().getPathLength())/(Constants.SPEEDLIGHT*this.get_Beta0to1());
        this._tFlight = this._SignalTimeOfFlight;
    }
    
    
    /**
     * 
     * @return start time from EB bank (ns)
     */
    public double getTStart() {
        return _tStart;
    }
    /**
     * 
     * @param tStart start time in ns
     */
    public void setTStart(double tStart) {
        this._tStart = tStart;
    }
    
    /**
     * 
     * @return T0 calibration constant in ns
     */
    public double getT0() {
        return _T0;
    }
    /**
     * 
     * @param T0 calibration constant in ns
     */
    public void setT0(double T0) {
        this._T0 = T0;
    }
    
    /**
     * 
     * @return Flight time to the track's closest point to the hit wire in ns
     */
    public double getTFlight() {
        return _tFlight;
    }
    /**
     * 
     * @param tFlight Flight time to the track's closest point to the hit wire in ns
     */
    public void setTFlight(double tFlight) {
        this._tFlight = tFlight;
    }
    /**
     * 
     * @return propagation time along the wire in ns
     */
    public double getTProp() {
        return _tProp;
    }
    /**
     * 
     * @param tProp propagation time along the wire in ns
     */
    public void setTProp(double tProp) {
        this._tProp = tProp;
    }
 
    /**
     *
     * @return the time in ns
     */
    public double get_Time() {
        return _Time;
    }

    /**
     * Sets the time
     *
     * @param _Time
     */
    public void set_Time(double _Time) {
        this._Time = _Time;
    }
  
    
    /**
     * 
     * @param b boolean to flag out-of-time hits
     */
    public void set_OutOfTimeFlag(boolean b) {
        _OutOfTimeFlag = b;
    }
    /**
     * 
     * @return boolean to flag out-of-time hits
     */
    public boolean get_OutOfTimeFlag() {
        return _OutOfTimeFlag;
    }

    private double _deltatime_beta;
    public void set_DeltaTimeBeta(double deltatime_beta) {
        _deltatime_beta = deltatime_beta;
    }

    public double get_DeltaTimeBeta() {
        return _deltatime_beta ;
    }
    
    // local angle 
    private double _alpha;
    
    /**
     * @return the _alpha
     */
    public double getAlpha() {
        return _alpha;
    }

    /**
     * @param _alpha the _alpha to set
     */
    public void setAlpha(double _alpha) {
        this._alpha = _alpha;
    }

    /**
     * 
     * @return a value <=1 resetting beta to 1 for overflows
     */
    public double get_Beta0to1() {
        double beta = this.get_Beta();
        if(beta>1.0)
            beta=1.0;
        return beta;
    }
    
    public void updateHitfromSV(StateVec st, DCGeant4Factory DcDetector) {
//        this.set_Id(this.get_Id());
//        this.set_TDC(this.get_TDC());
//        this.set_AssociatedHBTrackID(trk.get_Id());
//        this.set_AssociatedClusterID(this.get_AssociatedClusterID());
        this.setAssociatedStateVec(st);
        this.set_TrkResid(this.get_Doca() * Math.signum(st.getProjectorDoca()) - st.getProjectorDoca());
        this.setB(st.getB());
        this.setSignalPropagTimeAlongWire(st.x(), st.y(), DcDetector);
        this.setSignalTimeOfFlight();
        this.set_Doca(this.get_Doca());
        this.set_ClusFitDoca(this.get_ClusFitDoca());
//        this.set_DocaErr(this.get_DocaErr());
//        this.setT0(this.getT0());
//        this.set_Beta(this.get_Beta());
//        this.set_DeltaTimeBeta(this.get_DeltaTimeBeta());
//        this.setTStart(this.getTStart());
        this.set_Time(this.get_Time()
                + this.getSignalPropagTimeAlongWire() - this.getSignalPropagTimeAlongWire()
                + this.getTProp() - this.getTProp());
//        this.set_Id(this.get_Id());
//        this.set_TrkgStatus(this.get_TrkgStatus());
//        this.calc_CellSize(DcDetector);
//        this.set_LeftRightAmb(this.get_LeftRightAmb());

    }
    
    //make a  copy
    @Override
    public FittedHit clone() throws CloneNotSupportedException {
        FittedHit hitClone = new FittedHit(this.get_Sector(), this.get_Superlayer(), this.get_Layer(), this.get_Wire(),
                    this.get_TDC(), this.get_Id());
            hitClone.set_Doca(this.get_Doca());
            hitClone.set_DocaErr(this.get_DocaErr());
            hitClone.setT0(this.getT0()); 
            hitClone.set_Beta(this.get_Beta());  
            hitClone.setB(this.getB());  
            hitClone.set_DeltaTimeBeta(this.get_DeltaTimeBeta());
            hitClone.setTStart(this.getTStart());
            hitClone.setTProp(this.getTProp());
            hitClone.setTFlight(this.getTFlight());
            hitClone._SignalPropagAlongWire = this._SignalPropagAlongWire;
            hitClone._SignalPropagTimeAlongWire = this._SignalPropagTimeAlongWire;
            hitClone._SignalTimeOfFlight = this._SignalTimeOfFlight;
            hitClone.set_Time(this.get_Time());
            hitClone.set_Id(this.get_Id());
            hitClone.set_ClusFitDoca(this.get_ClusFitDoca());
            hitClone.set_LeftRightAmb(this.get_LeftRightAmb());
            hitClone.set_X(this.get_X());
            hitClone.set_Z(this.get_Z());
            hitClone.setAlpha(this.getAlpha());
            hitClone.set_CellSize(this.get_CellSize());
            hitClone.set_AssociatedClusterID(this.get_AssociatedClusterID());
            hitClone.set_AssociatedHBTrackID(this.get_AssociatedHBTrackID());
            hitClone.betaFlag = this.betaFlag;
            
        return hitClone;
    }

    
}
