/**
 *
 */
package org.jlab.rec.tof.hit;

import java.text.DecimalFormat;
import org.jlab.geom.prim.Line3D;

import org.jlab.geom.prim.Point3D;
import org.jlab.utils.groups.IndexedTable;

/**
 * @author ziegler
 *
 */
public abstract class AHit implements Comparable<AHit> {

    /**
     * A hit class describing a hit in either the FTOF or the CTOF
     */
    private int _Id;
    private int _Panel;
    private int _Sector;
    private int _Paddle;
    private int _ADC1;
    private int _ADC2;
    private int _TDC1;
    private int _TDC2;

    private int _ADCbankHitIdx1 = -1;
    private int _ADCbankHitIdx2 = -1;
    private int _TDCbankHitIdx1 = -1;
    private int _TDCbankHitIdx2 = -1;

    private int _AssociatedTrkId = -1;
    
    public AHit(int id, int panel, int sector, int paddle, int aDC1, int tDC1,
            int aDC2, int tDC2) {
        _Id = id;
        _Panel = panel;
        _Sector = sector;
        _Paddle = paddle;
        _ADC1 = aDC1;
        _ADC2 = aDC2;
        _TDC1 = tDC1;
        _TDC2 = tDC2;
    }

    public int get_Id() {
        return _Id;
    }

    public void set_Id(int _Id) {
        this._Id = _Id;
    }

    public int get_Panel() {
        return _Panel;
    }

    public void set_Panel(int _Panel) {
        this._Panel = _Panel;
    }

    public int get_Sector() {
        return _Sector;
    }

    public void set_Sector(int _Sector) {
        this._Sector = _Sector;
    }

    public int get_Paddle() {
        return _Paddle;
    }

    public void set_Paddle(int _Paddle) {
        this._Paddle = _Paddle;
    }

    public int get_ADC1() {
        return _ADC1;
    }

    public void set_ADC1(int _ADC1) {
        this._ADC1 = _ADC1;
    }

    public int get_ADC2() {
        return _ADC2;
    }

    public void set_ADC2(int _ADC2) {
        this._ADC2 = _ADC2;
    }

    public int get_TDC1() {
        return _TDC1;
    }

    public void set_TDC1(int _TDC1) {
        this._TDC1 = _TDC1;
    }

    public int get_TDC2() {
        return _TDC2;
    }

    public void set_TDC2(int _TDC2) {
        this._TDC2 = _TDC2;
    }

    public Point3D get_Position() {
        return _Position;
    }

    public void set_Position(Point3D _Position) {
        this._Position = _Position;
    }

    public double get_Energy() {
        return _Energy;
    }

    public void set_Energy(double _Energy) {
        this._Energy = _Energy;
    }

    public double get_EnergyUnc() {
        return _EnergyUnc;
    }

    public void set_EnergyUnc(double _EnergyUnc) {
        this._EnergyUnc = _EnergyUnc;
    }

    public double get_Energy1() {
        return _Energy1;
    }

    public void set_Energy1(double _Energy1) {
        this._Energy1 = _Energy1;
    }

    public double get_Energy1Unc() {
        return _Energy1Unc;
    }

    public void set_Energy1Unc(double _Energy1Unc) {
        this._Energy1Unc = _Energy1Unc;
    }

    public double get_Energy2() {
        return _Energy2;
    }

    public void set_Energy2(double _Energy2) {
        this._Energy2 = _Energy2;
    }

    public double get_Energy2Unc() {
        return _Energy2Unc;
    }

    public void set_Energy2Unc(double _Energy2Unc) {
        this._Energy2Unc = _Energy2Unc;
    }

    public double get_t() {
        return _t;
    }

    public void set_t(double _t) {
        this._t = _t;
    }

    public double get_tUnc() {
        return _tUnc;
    }

    public void set_tUnc(double _tUnc) {
        this._tUnc = _tUnc;
    }

    public double get_t1() {
        return _t1;
    }

    public void set_t1(double _t1) {
        this._t1 = _t1;
    }

    public double get_t2() {
        return _t2;
    }

    public void set_t2(double _t2) {
        this._t2 = _t2;
    }

    public double get_t1Unc() {
        return _t1Unc;
    }

    public void set_t1Unc(double _t1Unc) {
        this._t1Unc = _t1Unc;
    }

    public double get_t2Unc() {
        return _t2Unc;
    }

    public void set_t2Unc(double _t2Unc) {
        this._t2Unc = _t2Unc;
    }

    public double get_timeWalk1() {
        return _timeWalk1;
    }

    public void set_timeWalk1(double _timeWalk1) {
        this._timeWalk1 = _timeWalk1;
    }

    public double get_timeWalk2() {
        return _timeWalk2;
    }

    public void set_timeWalk2(double _timeWalk2) {
        this._timeWalk2 = _timeWalk2;
    }

    public double get_lambda1() {
        return _lambda1;
    }

    public void set_lambda1(double _lambda1) {
        this._lambda1 = _lambda1;
    }

    public double get_lambda2() {
        return _lambda2;
    }

    public void set_lambda2(double _lambda2) {
        this._lambda2 = _lambda2;
    }

    public double get_lambda1Unc() {
        return _lambda1Unc;
    }

    public void set_lambda1Unc(double _lambda1Unc) {
        this._lambda1Unc = _lambda1Unc;
    }

    public double get_lambda2Unc() {
        return _lambda2Unc;
    }

    public void set_lambda2Unc(double _lambda2Unc) {
        this._lambda2Unc = _lambda2Unc;
    }

    public String get_StatusWord() {
        return _StatusWord;
    }

    public void set_StatusWord(String _StatusWord) {
        this._StatusWord = _StatusWord;
    }

    public int get_AssociatedClusterID() {
        return _AssociatedClusterID;
    }

    public void set_AssociatedClusterID(int _AssociatedClusterID) {
        this._AssociatedClusterID = _AssociatedClusterID;
    }

    public double getAdcToEConv() {
        return AdcToEConv;
    }

    public void setAdcToEConv(double adcToEConv) {
        AdcToEConv = adcToEConv;
    }

    public double get_y() {
        return _y;
    }

    public void set_y(double _y) {
        this._y = _y;
    }

    public double get_yUnc() {
        return _yUnc;
    }

    public void set_yUnc(double _yUnc) {
        this._yUnc = _yUnc;
    }

    public Line3D get_paddleLine() {
        return _paddleLine;
    }
    public void set_paddleLine(Line3D paddleLine) {
        this._paddleLine = paddleLine;
    }
    public void set_barthickness(double thickness) {
        this._barThickness = thickness;
    }
    public double get_barthickness() {
        return this._barThickness;
    }

    public int get_TrkId() {
        return _AssociatedTrkId;
    }

    public void set_TrkId(int id) {
        this._AssociatedTrkId=id;
    }

    public Point3D get_TrkPosition() {
        return _TrkPosition;
    }

    public void set_TrkPosition(Point3D _TrkPosition) {
        this._TrkPosition = _TrkPosition;
    }

    public double get_yTrk() {
        return _yTrk;
    }

    public void set_yTrk(double _yTrk) {
        this._yTrk = _yTrk;
    }

    public double get_yTrkUnc() {
        return _yTrkUnc;
    }

    public void set_yTrkUnc(double _yTrkUnc) {
        this._yTrkUnc = _yTrkUnc;
    }

    public double get_TrkPathLen() {
        return _TrkPathLen;
    }

    public void set_TrkPathLen(double _TrkPathLen) {
        this._TrkPathLen = _TrkPathLen;
    }

    public double get_TrkPathLenThruBar() {
        return _PathLenThruBar;
    }

    public void set_TrkPathLenThruBar(double _PathLen) {
        this._PathLenThruBar = _PathLen;
    }

    private double _barThickness;   // bar thickness
    private Point3D _Position; // Hit position
    private Line3D _paddleLine; // paddle line
    private double _Energy; // Deposited energy in the bar
    private double _EnergyUnc; // Uncertainty in the deposited energy in the bar
    private double _Energy1; // (FTOF) L (CTOF) U deposited energy
    private double _Energy1Unc; // (FTOF) L (CTOF) U deposited energy
    // uncertainty
    private double _Energy2; // (FTOF) R (CTOF) D deposited energy
    private double _Energy2Unc; // (FTOF) R (CTOF) D deposited energy
    // uncertainty

    private double _t; // hit time (average)
    private double _tUnc; // hit time (average) uncertainty
    private double _t1; // hit time ((FTOF) L (CTOF) U)
    private double _t2; // hit time ((FTOF) R (CTOF) D)
    private double _t1Unc; // hit time uncertainty ((FTOF) L (CTOF) U)
    private double _t2Unc; // hit time uncertainty ((FTOF) R (CTOF) D)
    private double _timeWalk1; // hit time-walk ((FTOF) L (CTOF) U)
    private double _timeWalk2; // hit time-walk ((FTOF) R (CTOF) D)
    private double _lambda1; // attenuation length ((FTOF) L (CTOF) U)
    private double _lambda2; // attenuation length ((FTOF) R (CTOF) D)
    private double _lambda1Unc; // uncertainty in attenuation length ((FTOF) L
    // (CTOF) U)
    private double _lambda2Unc; // uncertainty in attenuation length ((FTOF) R
    // (CTOF) D)
    private String _StatusWord; // a status word: 1111 - fully functioning,
    // 0111-noADC1, 1011-noTDC1, 1101-noADC2,
    // 1110-noTDC2((FTOF) L=1,R=2 (CTOF) U=1,D=2)
    private int _AssociatedClusterID = -1;
    private double AdcToEConv; // conversion factor
    private double _y = Double.NaN; // hit coordinate measured with respect to
    // the center of the scintillator bar; init
    // as undefined
    private double _yUnc; // hit coordinate uncertainty [with respect to the
    // center of the scintillator bar]
    private Point3D _TrkPosition; // track hit position measured from the
    // reconstructed track info in lab frame
    private double _yTrk; // hit coordinate measured with respect to the center
    // of the scintillator bar from the reconstructed
    // track
    private double _yTrkUnc = 0.0500; // hit coordinate uncertainty [with
    // respect to the center of the
    // scintillator bar from track] -->
    // Guess of 500 microns ... error needs
    // to be propagated... to do
    private double _TrkPathLen; // pathlength of the track matched to the hit
    private double _PathLenThruBar;  // pathlength of the track from the entrance 
                                    // to the exit thru the bar

    public int get_ADCbankHitIdx1() {
        return _ADCbankHitIdx1;
    }

    public void set_ADCbankHitIdx1(int _ADCbankHitIdx1) {
        this._ADCbankHitIdx1 = _ADCbankHitIdx1;
    }

    public int get_ADCbankHitIdx2() {
        return _ADCbankHitIdx2;
    }

    public void set_ADCbankHitIdx2(int _ADCbankHitIdx2) {
        this._ADCbankHitIdx2 = _ADCbankHitIdx2;
    }

    public int get_TDCbankHitIdx1() {
        return _TDCbankHitIdx1;
    }

    public void set_TDCbankHitIdx1(int _TDCbankHitIdx1) {
        this._TDCbankHitIdx1 = _TDCbankHitIdx1;
    }

    public int get_TDCbankHitIdx2() {
        return _TDCbankHitIdx2;
    }

    public void set_TDCbankHitIdx2(int _TDCbankHitIdx2) {
        this._TDCbankHitIdx2 = _TDCbankHitIdx2;
    }

    /**
     * Sets the hit parameters based on the set calibration constants ( for FTOF
     * 1=L, 2=R; for CTOF 1=U, 2=D )
     *
     * @param superlayer
     * @param TrkPosInf
     * @param TW01
     * @param TW02
     * @param TW11
     * @param TW12
     * @param TW1P
     * @param TW2P
     * @param TW0E
     * @param TW1E
     * @param TW2E
     * @param TW3E
     * @param TW4E
     * @param HPOSa
     * @param HPOSb
     * @param HPOSc
     * @param HPOSd
     * @param HPOSe
     * @param lambda1
     * @param lambda2
     * @param yOffset
     * @param v1
     * @param v2
     * @param v1Unc
     * @param v2Unc
     * @param PED1
     * @param PED2
     * @param PED1Unc
     * @param PED2Unc
     * @param RFPad
     * @param paddle2paddle
     * @param timeOffset
     * @param LSBConv
     * @param LSBConvErr
     * @param ADC1Err
     * @param ADC2Err
     * @param TDC1Err
     * @param TDC2Err
     * @param ADC_MIP
     * @param ADC_MIPErr
     * @param DEDX_MIP
     * @param pl
     * @param ScinBarThickn
     */
    public void set_HitParams(int superlayer, double TW01, double TW02,
            double TW11, double TW12, double TW1P, double TW2P, 
            double TW0E, double TW1E, double TW2E, double TW3E, double TW4E, 
            double HPOSa, double HPOSb, double HPOSc, double HPOSd, double HPOSe,
            double lambda1, double lambda2,
            double yOffset, double v1, double v2, double v1Unc, double v2Unc,
            double PED1, double PED2, double PED1Unc, double PED2Unc,
            double paddle2paddle, double RFPad, double timeOffset, double triggerPhase, double[] LSBConv,
            double LSBConvErr, double ADC1Err, double ADC2Err, double TDC1Err,
            double TDC2Err, double ADC_MIP, double ADC_MIPErr, double DEDX_MIP,
            double ScinBarThickn, double pl) {
        // the order of calculation matters depending on the status
        String status = this.get_StatusWord();
        // initializing the values:
        double y = Double.NaN; // y = hit coordinate measured with respect to
        // the center of the scintillation bar (cm)
        double[] t12 = new double[2];
        double[] tErr12 = new double[2];
        double[] eDep12 = new double[2];
        double[] ePMTErr12 = new double[2];
        double tErr = 0;
        double eErr = 0;
        double expFac1 = 0;
        double expFac2 = 0;
        switch (status) {

            case "1111": // Good: TDC1, TDC2, ADC1, ADC2
                // 1. Compute time-walk corrections:
                this.set_timeWalk1(this.calc_timeWalk(TW0E, TW01, TW11,
                        (int) (this.get_ADC1() - PED1)));
                this.set_timeWalk2(this.calc_timeWalk(TW0E, TW02, TW12,
                        (int) (this.get_ADC2() - PED2)));
                // 2. Compute corrected hit times & errors:
                t12 = this.calc_t12(paddle2paddle, timeOffset, triggerPhase, LSBConv, RFPad);
                this.set_t1(t12[0]);
                this.set_t2(t12[1]);
                tErr12 = this.calc_tErr12(TDC1Err, TDC2Err, LSBConv, LSBConvErr);
                this.set_t1Unc(tErr12[0]);
                this.set_t2Unc(tErr12[1]);
                // 3. Compute hit coordinate from hit times t1, t2:
                y = v1 * v2 / (v1 + v2) * (this.get_t1() - this.get_t2()) - yOffset;
                this.set_y(y);
                // 3.1 Compute the uncertainty in y
                this.set_yUnc(this.calc_yUnc(status, v1, v2, v1Unc, v2Unc, ADC1Err,
                        ADC2Err));
                // 4. Compute average hit time:
                // // average of times
                this.set_t(0.5 * (this.get_t1() - (pl / 2 + y) / v1 + this.get_t2() - (pl / 2 - y) / v2)
                          + this.calc_TWpos(y, TW1P, TW2P) 
                          + this.calc_Hpos(y, HPOSa, HPOSb, HPOSc, HPOSd, HPOSe));
                // 4.1 Both TDCs --> Getting the uncertainty contribution from each
                tErr = this.calc_tErr(this.get_y(), this.get_yUnc(), this.get_t1(),
                        this.get_t1Unc(), v1, v1Unc, this.get_t2(),
                        this.get_t2Unc(), v2, v2Unc, pl);
                this.set_tUnc(tErr);
                // 5. Compute energy 1 and 2 & associated uncertainties:
                eDep12 = this.calc_Edep12(ADC_MIP, DEDX_MIP, ScinBarThickn, PED1,
                        PED2);
                this.set_Energy1(eDep12[0]);
                this.set_Energy2(eDep12[1]);
                ePMTErr12 = this.calc_PMTErr12(ADC_MIP, ADC_MIPErr, DEDX_MIP,
                        ScinBarThickn, PED1Unc, PED2Unc, ADC1Err, ADC2Err);
                this.set_Energy1Unc(ePMTErr12[0]); // 1 PMT energy uncertainty
                this.set_Energy2Unc(ePMTErr12[1]); // 2 PMT energy uncertainty
                // 6. Compute average energy deposited & associated uncertainty:
                expFac1 = Math.exp(this.get_y() / this.get_lambda1());
                expFac2 = Math.exp(-this.get_y() / this.get_lambda2());
                this.set_Energy(Math
                        .sqrt(eDep12[0] * expFac1 * eDep12[1] * expFac2));
                // 6.1 Both ADCs
                eErr = this.calc_EdepErr(this.get_Energy(), this.get_y(),
                        this.get_yUnc(), this.get_Energy1(), this.get_Energy1Unc(),
                        this.get_lambda1(), this.get_lambda1Unc(),
                        this.get_Energy2(), this.get_Energy2Unc(),
                        this.get_lambda2(), this.get_lambda2Unc());
                this.set_EnergyUnc(eErr);
                // 7. add new TW exponential correction
                this.set_t(this.get_t()-this.calc_TWexp(this.get_Energy(), TW0E, TW1E, TW2E, TW3E, TW4E));

                break;

            case "0111":
            case "1101": // Good: TDC1, TDC2 Missing: ADC1 or ADC2
                // 1. Compute hit coordinate from hit times t1, t2:
                if (!Double.isNaN(this.get_yTrk())) // use tracking info
                {
                    y = this.get_yTrk();
                }
                if (Double.isNaN(y)) // if there is no valid value reject the hit
                {
                    return;
                }
                this.set_y(y);
                // 1.1 Get the uncertainty in y from tracking
                this.set_yUnc(this.get_yTrkUnc());
                // 2. Compute the missing ADC
                this.setMissingADC(y, PED1, PED2);
                // 3. Compute time-walk corrections (with the estimated ADC1,2
                // value):
                this.set_timeWalk1(this.calc_timeWalk(TW0E, TW01, TW11,
                        (int) (this.get_ADC1() - PED1)));
                this.set_timeWalk2(this.calc_timeWalk(TW0E, TW02, TW12,
                        (int) (this.get_ADC2() - PED2)));
                // 4. Compute corrected hit times & uncertainties:
                t12 = this.calc_t12(paddle2paddle, timeOffset, triggerPhase, LSBConv, RFPad);
                this.set_t1(t12[0]);
                this.set_t2(t12[1]);
                // // average of times
                this.set_t(0.5 * (this.get_t1() - (pl / 2 + y) / v1 + this.get_t2() - (pl / 2 - y) / v2) 
                          + this.calc_TWpos(y, TW1P, TW2P) 
                          + this.calc_Hpos(y, HPOSa, HPOSb, HPOSc, HPOSd, HPOSe));
                tErr12 = this.calc_tErr12(TDC1Err, TDC2Err, LSBConv, LSBConvErr);
                this.set_t1Unc(tErr12[0]);
                this.set_t2Unc(tErr12[1]);
                // 4.1 Both TDCs --> Getting the error contribution from each
                tErr = this.calc_tErr(this.get_y(), this.get_yUnc(), this.get_t1(),
                        this.get_t1Unc(), v1, v1Unc, this.get_t2(),
                        this.get_t2Unc(), v2, v2Unc, pl);
                this.set_tUnc(tErr);
                // 5. Compute energy 1 and 2 and associated uncertainties:
                eDep12 = this.calc_Edep12(ADC_MIP, DEDX_MIP, ScinBarThickn, PED1,
                        PED2);
                this.set_Energy1(eDep12[0]);
                this.set_Energy2(eDep12[1]);
                ePMTErr12 = this.calc_PMTErr12(ADC_MIP, ADC_MIPErr, DEDX_MIP,
                        ScinBarThickn, PED1Unc, PED2Unc, ADC1Err, ADC2Err);
                // 6. Compute the energy deposited using the valid ADC only
                // 6.1 And compute the uncertainty in the deposited energy
                if (Character.toString(this.get_StatusWord().charAt(0)).equals("1")) { // ADC1
                    // is
                    // valid
                    this.set_Energy1Unc(ePMTErr12[0]); // PMT1 energy uncertainty
                    expFac1 = Math.exp(this.get_y() / this.get_lambda1());
                    this.set_Energy(eDep12[0] * expFac1);
                    eErr = this.calc_EdepErr(this.get_Energy(), this.get_y(),
                            this.get_yUnc(), this.get_Energy1(),
                            this.get_Energy1Unc(), this.get_lambda1(),
                            this.get_lambda1Unc(), 0, 0, 0, 0);
                    this.set_EnergyUnc(eErr);
                }
                if (Character.toString(this.get_StatusWord().charAt(2)).equals("1")) { // ADC2
                    // is
                    // valid
                    this.set_Energy2Unc(ePMTErr12[1]); // PMT2 energy uncertainty
                    expFac2 = Math.exp(-this.get_y() / this.get_lambda2());
                    this.set_Energy(eDep12[1] * expFac2);
                    eErr = this.calc_EdepErr(this.get_Energy(), this.get_y(),
                            this.get_yUnc(), 0, 0, 0, 0, this.get_Energy2(),
                            this.get_Energy2Unc(), this.get_lambda2(),
                            this.get_lambda2Unc());
                    this.set_EnergyUnc(eErr);
                }
                // 7. Re-calculate hit coordinate from hit times t1, t2:
                y = v1 * v2 / (v1 + v2) * (this.get_t1() - this.get_t2()) - yOffset;
                this.set_y(y);
                // 7.1. Compute the uncertainty in y
                this.set_yUnc(this.calc_yUnc(status, v1, v2, v1Unc, v2Unc, ADC1Err,
                        ADC2Err));
                // 8. add new TW exponential correction
                this.set_t(this.get_t()-this.calc_TWexp(this.get_Energy(), TW0E, TW1E, TW2E, TW3E, TW4E));

                break;

            case "1011":
            case "1110": // Good: ADC1, ADC2 Missing: TDC1 or TDC2
                // 1. Compute time-walk corrections:
                this.set_timeWalk1(this.calc_timeWalk(TW0E, TW01, TW11,
                        (int) (this.get_ADC1() - PED1)));
                this.set_timeWalk2(this.calc_timeWalk(TW0E, TW02, TW12,
                        (int) (this.get_ADC2() - PED2)));

                // 2. Determine hit coordinate from tracking or from ADC log ratio
                // if tracking is missing
                if (!Double.isNaN(this.get_yTrk())) { // use tracking info
                    y = this.get_yTrk();
                    this.set_y(y);
                    // set the uncertainty in y
                    this.set_yUnc(this.get_yTrkUnc());
                } else {
                    y = -(this.get_lambda1() * this.get_lambda2() / (this
                            .get_lambda1() + this.get_lambda2()))
                            * Math.log((double) (this.get_ADC1() - PED1)
                                    / (double) (this.get_ADC2() - PED2));
                    this.set_y(y);
                    // 2.1. Compute the uncertainty in y
                    this.set_yUnc(this.calc_yUnc(status, v1, v2, v1Unc, v2Unc,
                            ADC1Err, ADC2Err));
                }
                if (Double.isNaN(y)) // if there is no valid value reject the hit
                {
                    return;
                }

                // 3. Compute corrected hit times & uncertainties:
                t12 = this.calc_t12(paddle2paddle, timeOffset, triggerPhase, LSBConv, RFPad);
                this.set_t1(t12[0]);
                this.set_t2(t12[1]);
                if (Character.toString(this.get_StatusWord().charAt(1)).equals("1")) // TDC1
                // is
                // valid
                {
                    this.set_t(this.get_t1() - (pl / 2 + y) / v1 - yOffset / 2.
                              + this.calc_TWpos(y, TW1P, TW2P) 
                              + this.calc_Hpos(y, HPOSa, HPOSb, HPOSc, HPOSd, HPOSe));
                }
                if (Character.toString(this.get_StatusWord().charAt(3)).equals("1")) // TDC2
                // is
                // valid
                {
                    this.set_t(this.get_t2() + (y - pl / 2) / v2 + yOffset / 2.
                              + this.calc_TWpos(y, TW1P, TW2P) 
                              + this.calc_Hpos(y, HPOSa, HPOSb, HPOSc, HPOSd, HPOSe));
                }
                tErr12 = this.calc_tErr12(TDC1Err, TDC2Err, LSBConv, LSBConvErr);
                this.set_t1Unc(tErr12[0]);
                this.set_t2Unc(tErr12[1]);
                if (Character.toString(this.get_StatusWord().charAt(1)).equals("1")) // TDC1
                // is
                // valid
                {
                    this.set_tUnc(this.calc_tErr(this.get_y(), this.get_yUnc(),
                            this.get_t1(), this.get_t1Unc(), v1, v1Unc, 0, 0, 0, 0,
                            pl));
                }
                if (Character.toString(this.get_StatusWord().charAt(3)).equals("1")) // TDC2
                // is
                // valid
                {
                    this.set_tUnc(this.calc_tErr(this.get_y(), this.get_yUnc(), 0,
                            0, 0, 0, this.get_t2(), this.get_t2Unc(), v2, v2Unc, pl));
                }
                // 4. Compute energy 1 and 2 and associated uncertainties:
                eDep12 = this.calc_Edep12(ADC_MIP, DEDX_MIP, ScinBarThickn, PED1,
                        PED2);
                this.set_Energy1(eDep12[0]);
                this.set_Energy2(eDep12[1]);
                ePMTErr12 = this.calc_PMTErr12(ADC_MIP, ADC_MIPErr, DEDX_MIP,
                        ScinBarThickn, PED1Unc, PED2Unc, ADC1Err, ADC2Err);
                this.set_Energy1Unc(ePMTErr12[0]);
                this.set_Energy2Unc(ePMTErr12[1]);
                // 5. Compute energy deposited and associated uncertainties:
                expFac1 = Math.exp(this.get_y() / this.get_lambda1());
                expFac2 = Math.exp(-this.get_y() / this.get_lambda2());
                this.set_Energy(Math
                        .sqrt(eDep12[0] * expFac1 * eDep12[1] * expFac2));
                // 5.1 Both ADCs
                eErr = this.calc_EdepErr(this.get_Energy(), this.get_y(),
                        this.get_yUnc(), this.get_Energy1(), this.get_Energy1Unc(),
                        this.get_lambda1(), this.get_lambda1Unc(),
                        this.get_Energy2(), this.get_Energy2Unc(),
                        this.get_lambda2(), this.get_lambda2Unc());
                this.set_EnergyUnc(eErr);
                // 6. add new TW exponential correction
                this.set_t(this.get_t()-this.calc_TWexp(this.get_Energy(), TW0E, TW1E, TW2E, TW3E, TW4E));

                break;

            case "1100":
            case "0011":
            case "0110":
            case "1001": // Good: TDC1, ADC1 Missing: TDC2, ADC2 or Good: TDC2, ADC2
                // Missing: TDC1, ADC1 or Good: TDC1, ADC2 Missing:
                // TDC2, ADC1 or Good: TDC2, ADC1 Missing: TDC1, ADC2
                // 1. get the hit coordinate from tracking:
                if (!Double.isNaN(this.get_yTrk())) {
                    y = this.get_yTrk();
                }
                if (Double.isNaN(y)) // if there is no valid value reject the hit
                {
                    return;
                }
                this.set_y(y);
                // 1.1 Get the uncertainty in y from tracking
                this.set_yUnc(this.get_yTrkUnc());
                // 2. Compute the missing ADC
                this.setMissingADC(y, PED1, PED2);
                // 3. Compute time-walk corrections (with the estimated ADC value):
                this.set_timeWalk1(this.calc_timeWalk(TW0E, TW01, TW11,
                        (int) (this.get_ADC1() - PED1)));
                this.set_timeWalk2(this.calc_timeWalk(TW0E, TW02, TW12,
                        (int) (this.get_ADC2() - PED2)));
                // 4. Compute corrected hit times & uncertainties:
                t12 = this.calc_t12(paddle2paddle, timeOffset, triggerPhase, LSBConv, RFPad);
                this.set_t1(t12[0]);
                this.set_t2(t12[1]);
                if (Character.toString(this.get_StatusWord().charAt(1)).equals("1")) // TDC1
                // is
                // valid
                {
                    this.set_t(this.get_t1() - (pl / 2 + y) / v1 - yOffset / 2.
                              + this.calc_TWpos(y, TW1P, TW2P) 
                              + this.calc_Hpos(y, HPOSa, HPOSb, HPOSc, HPOSd, HPOSe));
                }
                if (Character.toString(this.get_StatusWord().charAt(3)).equals("1")) // TDC2
                // is
                // valid
                {
                    this.set_t(this.get_t2() + (y - pl / 2) / v2 + yOffset / 2.
                              + this.calc_TWpos(y, TW1P, TW2P) 
                              + this.calc_Hpos(y, HPOSa, HPOSb, HPOSc, HPOSd, HPOSe));
                }
                tErr12 = this.calc_tErr12(TDC1Err, TDC2Err, LSBConv, LSBConvErr);
                this.set_t1Unc(tErr12[0]);
                this.set_t2Unc(tErr12[1]);
                if (Character.toString(this.get_StatusWord().charAt(1)).equals("1")) // TDC1
                // is
                // valid
                {
                    this.set_tUnc(this.calc_tErr(this.get_y(), this.get_yUnc(),
                            this.get_t1(), this.get_t1Unc(), v1, v1Unc, 0, 0, 0, 0,
                            pl));
                }
                if (Character.toString(this.get_StatusWord().charAt(3)).equals("1")) // TDC2
                // is
                // valid
                {
                    this.set_tUnc(this.calc_tErr(this.get_y(), this.get_yUnc(), 0,
                            0, 0, 0, this.get_t2(), this.get_t2Unc(), v2, v2Unc, pl));
                }
                // 5. Compute energy 1 and 2 and associated uncertainties:
                eDep12 = this.calc_Edep12(ADC_MIP, DEDX_MIP, ScinBarThickn, PED1,
                        PED2);
                this.set_Energy1(eDep12[0]);
                this.set_Energy2(eDep12[1]);
                ePMTErr12 = this.calc_PMTErr12(ADC_MIP, ADC_MIPErr, DEDX_MIP,
                        ScinBarThickn, PED1Unc, PED2Unc, ADC1Err, ADC2Err);
                // 6. Compute the energy deposited using the valid ADC only
                // 6.1 And compute the uncertainty in the deposited energy
                if (Character.toString(this.get_StatusWord().charAt(0)).equals("1")) { // ADC1
                    // is
                    // valid
                    this.set_Energy1Unc(ePMTErr12[0]); // PMT1 energy uncertainty
                    expFac1 = Math.exp(this.get_y() / this.get_lambda1());
                    this.set_Energy(eDep12[0] * expFac1);
                    eErr = this.calc_EdepErr(this.get_Energy(), this.get_y(),
                            this.get_yUnc(), this.get_Energy1(),
                            this.get_Energy1Unc(), this.get_lambda1(),
                            this.get_lambda1Unc(), 0, 0, 0, 0);
                    this.set_EnergyUnc(eErr);
                }
                if (Character.toString(this.get_StatusWord().charAt(2)).equals("1")) { // ADC2
                    // is
                    // valid
                    this.set_Energy2Unc(ePMTErr12[1]); // PMT2 energy uncertainty
                    expFac2 = Math.exp(-this.get_y() / this.get_lambda2());
                    this.set_Energy(eDep12[1] * expFac2);
                    eErr = this.calc_EdepErr(this.get_Energy(), this.get_y(),
                            this.get_yUnc(), 0, 0, 0, 0, this.get_Energy2(),
                            this.get_Energy2Unc(), this.get_lambda2(),
                            this.get_lambda2Unc());
                    this.set_EnergyUnc(eErr);
                }
                // 7. add new TW exponential correction
                this.set_t(this.get_t()-this.calc_TWexp(this.get_Energy(), TW0E, TW1E, TW2E, TW3E, TW4E));

                break;
        }

    }
 
    /**
     *
     * @param status the status.
     * @param v1 effective velocity (L for FTOF, U for CTOF)
     * @param v2 effective velocity (R for FTOF, D for CTOF)
     * @param v1Unc effective velocity uncertainty (L for FTOF, U for CTOF)
     * @param v2Unc effective velocity uncertainty (R for FTOF, D for CTOF)
     * @param ADC1Err ADC (L for FTOF, U for CTOF) Jitter
     * @param ADC2Err ADC (R for FTOF, D for CTOF) Jitter
     * @return uncertainty in y for cases 1) TDC1, TDC2, ADC1, ADC2 are all
     * good, 2) Good: ADC1, ADC2 Missing: TDC1 or TDC2
     */
    private double calc_yUnc(String status, double v1, double v2, double v1Unc,
            double v2Unc, double ADC1Err, double ADC2Err) {
        double err = 0;

        switch (status) {

            case "1111": // Good: TDC1, TDC2, ADC1, ADC2
                double iterm1 = v2 * this.get_y() * v1Unc / (v1 * (v1 + v2));
                double iterm1Sq = iterm1 * iterm1;
                double iterm2 = v1 * this.get_y() * v2Unc / (v2 * (v1 + v2));
                double iterm2Sq = iterm2 * iterm2;
                double iterm3 = v1 * v2 * this.get_t1Unc() / (v1 + v2);
                double iterm3Sq = iterm3 * iterm3;
                double iterm4 = -v1 * v2 * this.get_t2Unc() / (v1 + v2);
                double iterm4Sq = iterm4 * iterm4;

                err = Math.sqrt(iterm1Sq + iterm2Sq + iterm3Sq + iterm4Sq);
                break;

            case "1011":
            case "1110": // Good: ADC1, ADC2 Missing: TDC1 or TDC2
                double l1 = this.get_lambda1();
                double l2 = this.get_lambda2();
                double l1Err = this.get_lambda1Unc();
                double l2Err = this.get_lambda2Unc();
                double ADC1 = (double) this.get_ADC1();
                double ADC2 = (double) this.get_ADC2();

                double jterm1 = ((-l2 / (l1 + l2)) * (-l2 / (l1 + l2)) * Math
                        .log(ADC1 / ADC2)) * l1Err;
                double jterm1Sq = jterm1 * jterm1;
                double jterm2 = ((-l1 / (l1 + l2)) * (-l1 / (l1 + l2)) * Math
                        .log(ADC1 / ADC2)) * l2Err;
                double jterm2Sq = jterm2 * jterm2;
                double jterm3 = (-l1 * l2 / ((l1 + l2) * ADC1)) * ADC1Err;
                double jterm3Sq = jterm3 * jterm3;
                double jterm4 = (l1 * l2 / ((l1 + l2) * ADC2)) * ADC2Err;
                double jterm4Sq = jterm4 * jterm4;

                err = Math.sqrt(jterm1Sq + jterm2Sq + jterm3Sq + jterm4Sq);
                break;
        }

        return err;
    }

    /**
     *
     * @param y the hit position in bar coordinate system
     * @param PED1 for FTOF L pedestal, for CTOF U pedestal
     * @param PED2 for FTOF R pedestal, for CTOF D pedestal
     */
    private void setMissingADC(double y, double PED1, double PED2) {
        // if 1 ADC is missing
        if (Character.toString(this.get_StatusWord().charAt(0)).equals("0")) { // ADC1
            // is
            // missing
            // ==>
            // ADC1
            // =
            // ADC2
            // *
            // exp(-y/Lambda1
            // -y/Lambda2)
            int ADC1 = (int) ((this.get_ADC2() - PED2) * Math.exp(-y
                    / this.get_lambda1() - y / this.get_lambda2()));
            this.set_ADC1(ADC1); // now that the left adc is set the timewalk
            // will be calculated using this value
        }
        if (Character.toString(this.get_StatusWord().charAt(2)).equals("0")) { // ADC2
            // is
            // missing
            // ==>
            // ADC2
            // =
            // ADC1
            // *
            // exp(y/Lambda1
            // +
            // y/Lambda2)
            int ADC2 = (int) ((this.get_ADC1() - PED1) * Math.exp(y
                    / this.get_lambda1() + y / this.get_lambda2()));
            this.set_ADC2(ADC2); // now that the right adc is set the timewalk
            // will be calculated using this value
        }
    }

    /**
     *
     * @param ADC1Err for FTOF L Jitter, for CTOF U Jitter
     * @param ADC2Err for FTOF R Jitter, for CTOF D Jitter
     * @return the uncertainty of the missing ADC calculated using the track
     * information
     */
    @SuppressWarnings("unused")
    private double calc_missingADCUnc(double ADC1Err, double ADC2Err) {
        double err = 0;
        double y = this.get_yTrk();
        double yErr = this.get_yTrkUnc();

        if (Character.toString(this.get_StatusWord().charAt(0)).equals("0")) { // ADC1
            // is
            // missing
            // ==>
            // ADC1
            // =
            // ADC2
            // *
            // exp(-y/Lambda1
            // -y/Lambda2)
            double expVal = Math.exp(-y / this.get_lambda1() - y
                    / this.get_lambda2());
            double ADCErr = ADC2Err;
            double term1Sq = ADCErr * ADCErr;
            double term2Sq = (this.get_ADC2() * yErr / this.get_lambda1() + yErr
                    / this.get_lambda2())
                    * (this.get_ADC2() * yErr / this.get_lambda1() + yErr
                    / this.get_lambda2());
            double term3Sq = (this.get_ADC2() * y * this.get_lambda1Unc() / (this
                    .get_lambda1() * this.get_lambda1()))
                    * (this.get_ADC2() * y * this.get_lambda1Unc() / (this
                    .get_lambda1() * this.get_lambda1()))
                    + (this.get_ADC2() * y * this.get_lambda2Unc() / (this
                    .get_lambda2() * this.get_lambda2()))
                    * (this.get_ADC2() * y * this.get_lambda2Unc() / (this
                    .get_lambda2() * this.get_lambda2()));
            err = expVal * Math.sqrt(term1Sq + term2Sq + term3Sq);
        }
        if (Character.toString(this.get_StatusWord().charAt(2)).equals("0")) { // ADC2
            // is
            // missing
            // ==>
            // ADC1R
            // =
            // ADC1
            // *
            // exp(y/Lambda1
            // +
            // y/Lambda2)
            double expVal = (this.get_ADC1() * Math.exp(y / this.get_lambda1()
                    + y / this.get_lambda2()));
            double ADCErr = ADC1Err;
            double term1Sq = ADCErr * ADCErr;
            double term2Sq = (this.get_ADC1() * yErr / this.get_lambda1() + yErr
                    / this.get_lambda2())
                    * (this.get_ADC1() * yErr / this.get_lambda1() + yErr
                    / this.get_lambda2());
            double term3Sq = (this.get_ADC1() * y * this.get_lambda1Unc() / (this
                    .get_lambda1() * this.get_lambda1()))
                    * (this.get_ADC1() * y * this.get_lambda1Unc() / (this
                    .get_lambda1() * this.get_lambda1()))
                    + (this.get_ADC1() * y * this.get_lambda2Unc() / (this
                    .get_lambda2() * this.get_lambda2()))
                    * (this.get_ADC1() * y * this.get_lambda2Unc() / (this
                    .get_lambda2() * this.get_lambda2()));
            err = expVal * Math.sqrt(term1Sq + term2Sq + term3Sq);
        }
        return err;
    }

    private double calc_timeWalk(double E, double A, double B, int ADC) {
        // E: first constants from time_wal_exp table to enable/disable standard TW correction
        double tw =(1-E)*(A / Math.pow((double) ADC, B));
        return tw;
    }

    /**
     * @param paddle2paddle paddle2paddle calibration parameter
     * @param timeOffset timeOffset calibration parameter
     * @param LSBConv LSB conversion factor
     * @return double[] index 0 = t1, index 1 = t2, the reconstructed time using
     * 1,2 TDCs
     */
    private double[] calc_t12(double paddle2paddle, double timeOffset, double triggerPhase, 
            double[] LSBConv, double RFPad) {
        double[] t12 = new double[2];

        // check status of TDCs and then calc t12 --> if TDC is not valid, set t
        // to zero
        double t1 = 0;
        double t2 = 0;
        if (Character.toString(this.get_StatusWord().charAt(1)).equals("1")) // TDC1
        // is
        // valid
        {
            t1 = this.get_TDC1() * LSBConv[0] - triggerPhase - timeOffset / 2.
                    - this.get_timeWalk1() + paddle2paddle + RFPad;
        }
        if (Character.toString(this.get_StatusWord().charAt(3)).equals("1")) // TDC2
        // is
        // valid
        {
            t2 = this.get_TDC2() * LSBConv[1] - triggerPhase + timeOffset / 2.
                    - this.get_timeWalk2() + paddle2paddle + RFPad;
        }

        // for small values of TDCs the t12 can be negative
        if (t1 < 0) {
            t1 = 0;
        }
        if (t2 < 0) {
            t2 = 0;
        }

        t12[0] = t1;
        t12[1] = t2;

        return t12;
    }

    /**
     *
     * @param TDC1Err for FTOF L,U Jitter, for CTOF U Jitter
     * @param TDC2Err for FTOF R,D Jitter, for CTOF D Jitter
     * @param LSBConv LSB conversion factor
     * @param LSBConvErr Uncertainty in LSB conversion factor
     * @return time uncertainty array [0]=L, [1]=R
     */
    private double[] calc_tErr12(double TDC1Err, double TDC2Err,
            double[] LSBConv, double LSBConvErr) {
        double[] tErr12 = new double[2];

        // check status of TDCs and then calc t12 --> if TDC is not valid, set t
        // to zero
        double tErr1 = 0;
        double tErr2 = 0;
        if (Character.toString(this.get_StatusWord().charAt(1)).equals("1")) // TDC1
        // is
        // valid
        {
            tErr1 = Math.sqrt((this.get_TDC1() * LSBConvErr)
                    * (this.get_TDC1() * LSBConvErr) + (TDC1Err * LSBConv[0])
                    * (TDC1Err * LSBConv[0]));
        }
        if (Character.toString(this.get_StatusWord().charAt(3)).equals("1")) // TDC2
        // is
        // valid
        {
            tErr2 = Math.sqrt((this.get_TDC2() * LSBConvErr)
                    * (this.get_TDC2() * LSBConvErr) + (TDC2Err * LSBConv[1])
                    * (TDC2Err * LSBConv[1]));
        }

        tErr12[0] = tErr1;
        tErr12[1] = tErr2;

        return tErr12;
    }

    /**
     *
     * @param y y
     * @param yUnc error in y
     * @param t1 for FTOF tL, for CTOF tU
     * @param t1Unc uncertainty in t1 [for FTOF tL, for CTOF tU]
     * @param v1 effective velocity [L for FTOF, U for CTOF]
     * @param v1Unc uncertainty in the effective velocity [for FTOF vL, for CTOF
     * vU]
     * @param t2 for FTOF tR, for CTOF tD
     * @param t2Unc uncertainty in t2 [for FTOF tR, for CTOF tD]
     * @param v2 effective velocity [R for FTOF, D for CTOF]
     * @param v2Unc uncertainty in the effective velocity [for FTOF vR, for CTOF
     * vD]
     * @param L the paddle length
     * @return the time uncertainty
     */
    private double calc_tErr(double y, double yUnc, double t1, double t1Unc,
            double v1, double v1Unc, double t2, double t2Unc, double v2,
            double v2Unc, double L) {

        double errSq = 0;

        if (t2 == 0) {
            errSq = t1Unc * t1Unc + (yUnc / v1) * (yUnc / v1)
                    + ((y + L / 2) * v1Unc / (v1 * v1))
                    * ((y + L / 2) * v1Unc / (v1 * v1));
        }
        if (t1 == 0) {
            errSq = t2Unc * t2Unc + (yUnc / v2) * (yUnc / v2)
                    + ((L / 2 - y) * v2Unc / (v2 * v2))
                    * ((L / 2 - y) * v2Unc / (v2 * v2));
        }
        if (t1 != 0 && t2 != 0) {
            errSq = (t1Unc * t1Unc + ((y + L / 2) * v1Unc / (v1 * v1))
                    * ((y + L / 2) * v1Unc / (v1 * v1)) + +t2Unc * t2Unc
                    + ((L / 2 - y) * v2Unc / (v2 * v2))
                    * ((L / 2 - y) * v2Unc / (v2 * v2)) + (yUnc * yUnc
                    * (1 / v2 - 1 / v1) * (1 / v2 - 1 / v1))) / 4.;
        }

        return Math.sqrt(errSq);

    }

    /**
     * @param ADC_MIP ADC MIP
     * @param DEDX_MIP dEdx MIP
     * @param ScinBarThickn scintillator bar thickness
     * @param PED1 for FTOF L pedestal, for CTOF U pedestal
     * @param PED2 for FTOF R pedestal, for CTOF D pedestal
     * @return double[] index 0 = for ADC1, index 1 = for ADC2 corresponding to
     * the (FTOF) L,R or (CTOF) U, D PMT energy
     */
    private double[] calc_Edep12(double ADC_MIP, double DEDX_MIP,
            double ScinBarThickn, double PED1, double PED2) {
        double[] eDep12 = new double[2];

        // the conversion factor from ADC to energy
        this.AdcToEConv = ADC_MIP / (DEDX_MIP * ScinBarThickn);
        // the inverse conversion factor is used in the Edep calculation
        double InvAdcToEConv = 1. / this.AdcToEConv;

        // subtracting the pedestals the 1 and 2 reconstructed energies are
        eDep12[0] = ((double) this.get_ADC1() - PED1) * InvAdcToEConv;
        eDep12[1] = ((double) this.get_ADC2() - PED2) * InvAdcToEConv;

        return eDep12;

    }

    /**
     *
     * @param ADCMIP ADC MIP
     * @param ADCMIPErr ADC MIP uncertainty
     * @param DEDX_MIP dEdx MIP
     * @param ScinBarThickn scintillator bar thickness
     * @param PEDErr1 uncertainty in pedestal [for FTOF L, for CTOF U]
     * @param PEDErr2 uncertainty in pedestal [for FTOF R, for CTOF D]
     * @param ADCErr1 for FTOF L Jitter, for CTOF U Jitter
     * @param ADCErr2 for FTOF R Jitter, for CTOF D Jitter
     * @return the 12 (FTOF LR, CTOF UD) PMT energy uncertainties
     */
    private double[] calc_PMTErr12(double ADCMIP, double ADCMIPErr,
            double DEDX_MIP, double ScinBarThickn, double PEDErr1,
            double PEDErr2, double ADCErr1, double ADCErr2) {
        double[] eDepErr12 = new double[2];

        double K = DEDX_MIP * ScinBarThickn;

        // PMT errors
        if (Character.toString(this.get_StatusWord().charAt(0)).equals("1")) // ADC1
        // is
        // OK
        {
            eDepErr12[0] = K
                    * Math.sqrt((this.get_Energy1() * this.get_Energy1() / (K * K))
                            * ADCMIPErr
                            * ADCMIPErr
                            + ADCErr1
                            * ADCErr1
                            + PEDErr1 * PEDErr1) / ADCMIP;
        }
        if (Character.toString(this.get_StatusWord().charAt(2)).equals("1")) // ADC2
        // is
        // OK
        {
            eDepErr12[1] = K
                    * Math.sqrt((this.get_Energy2() * this.get_Energy2() / (K * K))
                            * ADCMIPErr
                            * ADCMIPErr
                            + ADCErr2
                            * ADCErr2
                            + PEDErr2 * PEDErr2) / ADCMIP;
        }

        return eDepErr12;

    }

    /**
     *
     * @param E
     * @param y
     * @param yUnc
     * @param E1 the PMT energy
     * @param E1Unc the uncertainty on the PMT energy
     * @param E2 the PMT energy
     * @param E2Unc the uncertainty on the PMT energy
     * @param lambda1 lambda (FTOF) L (CTOF) U
     * @param lambda1Unc uncertainty in lambda (FTOF) L (CTOF) U
     * @param lambda2 lambda (FTOF) R (CTOF) D
     * @param lambda2Unc uncertainty in lambda (FTOF) R (CTOF) D
     * @return the square of the uncertainty on the deposited energy for [FTOF
     * L, CTOF U] 1 or [FTOF R, CTOF D] 2
     */
    private double calc_EdepErr(double E, double y, double yUnc, double E1,
            double E1Unc, double lambda1, double lambda1Unc, double E2,
            double E2Unc, double lambda2, double lambda2Unc) {

        double Sq = 0;

        if (E2 == 0) {
            double term1Sq = (E1Unc / E1) * (E1Unc / E1);
            double term2Sq = (yUnc / lambda1) * (yUnc / lambda1);
            double term3Sq = (lambda1Unc / (lambda1 * lambda1))
                    * (lambda1Unc / (lambda1 * lambda1));
            Sq = term1Sq + term2Sq + term3Sq;
        }
        if (E1 == 0) {
            double term1Sq = (E2Unc / E2) * (E2Unc / E2);
            double term2Sq = (yUnc / lambda2) * (yUnc / lambda2);
            double term3Sq = (lambda2Unc / (lambda2 * lambda2))
                    * (lambda2Unc / (lambda2 * lambda2));
            Sq = term1Sq + term2Sq + term3Sq;
        }
        if (E1 != 0 && E2 != 0) {
            double term1Sq = ((E2Unc / E2) * (E2Unc / E2) + (E1Unc / E1)
                    * (E1Unc / E1)) / 4.;
            double term2Sq = (yUnc * yUnc * (1. / lambda1 - 1. / lambda2) * (1. / lambda1 - 1. / lambda2)) / 4.;
            double term3Sq = ((lambda1Unc / (lambda1 * lambda1))
                    * (lambda1Unc / (lambda1 * lambda1)) + (lambda2Unc / (lambda2 * lambda2))
                    * (lambda2Unc / (lambda2 * lambda2))) / 4.;
            Sq = term1Sq + term2Sq + term3Sq;
        }
        return E * Math.sqrt(Sq);
    }

    private double calc_TWpos(double y, double tw1pos, double tw2pos) {
        return tw1pos*Math.pow(y, 2)+tw2pos*y;
    }
    
    /**  
    * Calculate new TW correction for FTOF
    * @param energy hit energy deposition
    * @param tw0e activate new TW correction (0) or old (1)
    * @param tw1e first correction parameter
    * @param tw2e second correction parameter, currently not used
    * @param tw3e third correction parameter, currently not used
    * @param tw4e fourth correction parameter, currently not used
    * @return time offset in ns
    */     
    private double calc_TWexp(double energy, double tw0e, double tw1e, double tw2e, double tw3e, double tw4e) {
        double twexp = 0;
        if(energy>0) {
            twexp = tw0e*tw1e*Math.exp(tw2e*energy)+tw3e/energy;
        }
        return twexp;
    }
    
    /**  
    * Calculate position dependent correction to timing offsets  
    * Currently used for CTOF
    * @param y hit position along the paddle
    * @param hposa first correction parameter
    * @param hposb second correction parameter
    * @param hposc third correction parameter, currently not used
    * @param hposd fourth correction parameter, currently not used
    * @param hpose fifth correction parameter, currently not used
    * @return time offset in ns
    */     
    private double calc_Hpos(double y, double hposa, double hposb, double hposc, double hposd, double hpose) {
       return hposa*Math.exp(hposb*y);
    }
    
    @Override
    public int compareTo(AHit arg) {
        // Sort by sector, panel, paddle
        int return_val = 0;
        int CompSec = this.get_Sector() < arg.get_Sector() ? -1 : this
                .get_Sector() == arg.get_Sector() ? 0 : 1;
        int CompPan = this.get_Panel() < arg.get_Panel() ? -1 : this
                .get_Panel() == arg.get_Panel() ? 0 : 1;
        int CompPad = this.get_Paddle() < arg.get_Paddle() ? -1 : this
                .get_Paddle() == arg.get_Paddle() ? 0 : 1;

        int return_val1 = ((CompPan == 0) ? CompPad : CompPan);
        return_val = ((CompSec == 0) ? return_val1 : CompSec);

        return return_val;
    }
    
    public boolean isAdjacent(AHit arg0) {
        boolean isClose = false;
        if(this.get_Sector()==arg0.get_Sector() && this.get_Panel()==arg0.get_Panel()) {
            if(this.get_Paddle()==arg0.get_Paddle()-1 || this.get_Paddle()==arg0.get_Paddle()+1) isClose=true;
        }
        return isClose;
    }

    public void printInfo() {
        DecimalFormat form = new DecimalFormat("#.##");
        String s = " FTOF Hit in " + " Sector " + this.get_Sector() + " Panel "
                + this.get_Panel() + " Paddle " + this.get_Paddle()
                + " with Status " + this.get_StatusWord() + " in Cluster "
                + this.get_AssociatedClusterID() + " : \n" + "  ADC1 =  "
                + this.get_ADC1() + "  ADC2 =  " + this.get_ADC2()
                + "  TDC1 =  " + this.get_TDC1() + "  TDC2 =  "
                + this.get_TDC2() + "\n  t1 =  " + form.format(this.get_t1())
                + "  t2 =  " + form.format(this.get_t2()) + "  t =  "
                + form.format(this.get_t()) + "  timeWalk1 =  "
                + form.format(this.get_timeWalk1()) + "  timeWalk2 =  "
                + form.format(this.get_timeWalk2()) + "  lambda1 =  "
                + form.format(this.get_lambda1()) + "  lambda2 =  "
                + form.format(this.get_lambda2()) + "  Energy =  "
                + form.format(this.get_Energy()) + "  Energy1 =  "
                + form.format(this.get_Energy1()) + "  Energy2 =  "
                + form.format(this.get_Energy2()) + "  y =  "
                + form.format(this.get_y()) + "\n ";
        if (this.get_Position() != null) {
            s += "  xPos =  " + form.format(this.get_Position().x())
                    + "  yPos =  " + form.format(this.get_Position().y())
                    + "  zPos =  " + form.format(this.get_Position().z())
                    + "\n ";
        }
        System.out.println(s);
    }
}
