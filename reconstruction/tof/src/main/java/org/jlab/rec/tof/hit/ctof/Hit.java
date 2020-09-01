/**
 *
 */
package org.jlab.rec.tof.hit.ctof;

import java.io.FileNotFoundException;
import java.text.DecimalFormat;

import org.jlab.detector.geant4.v2.CTOFGeant4Factory;
import org.jlab.detector.hits.CTOFDetHit;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geometry.prim.Line3d;
import org.jlab.rec.ctof.Constants;
import org.jlab.rec.tof.hit.AHit;
import org.jlab.rec.tof.hit.IGetCalibrationParams;
import org.jlab.utils.groups.IndexedTable;

/**
 * @author ziegler
 *
 */
public class Hit extends AHit implements IGetCalibrationParams {

    public Hit(int id, int panel, int sector, int paddle, int aDCU, int tDCU,
            int aDCD, int tDCD) {
        super(id, panel, sector, paddle, aDCU, tDCU, aDCD, tDCD);
    }

    private CTOFDetHit _matchedTrackHit; // matched hit information from
    // tracking; this contains the
    // information of the entrance and
    // exit point of the track with the
    // FTOF hit counter
    private Line3d _matchedTrack;

    public int _AssociatedTrkId = -1;

    public CTOFDetHit get_matchedTrackHit() {
        return _matchedTrackHit;
    }

    public void set_matchedTrackHit(CTOFDetHit matchedTrackHit) {
        this._matchedTrackHit = matchedTrackHit;
    }

    public Line3d get_matchedTrack() {
        return _matchedTrack;
    }

    public void set_matchedTrack(Line3d _matchedTrack) {
        this._matchedTrack = _matchedTrack;
    }

    public void set_HitParameters(int superlayer,
            double triggerPhase,
            IndexedTable constants0, 
            IndexedTable constants1, 
            IndexedTable constants2, 
            IndexedTable constants3, 
            IndexedTable constants5, 
            IndexedTable constants8) {
        /*
        0: "/calibration/ctof/attenuation"),
        1: "/calibration/ctof/effective_velocity"),
        2: "/calibration/ctof/time_offsets"),
        3: "/calibration/ctof/tdc_conv"),
        4: "/calibration/ctof/status"),
        5: "/calibration/ctof/gain_balance"),
        5: "/calibration/ctof/hpos"));
        */
        double pl = this.get_paddleLine().length();

        // Get all the constants used in the hit parameters calculation
        double TW0U = this.TW01(null);
        double TW0D = this.TW02(null);
        double TW1U = this.TW11(null);
        double TW1D = this.TW12(null);
        double TW1P = this.TW1P(null); 
        double TW2P = this.TW2P(null);  
        double TW0E = this.TW0E(null); 
        double TW1E = this.TW1E(null); 
        double TW2E = this.TW2E(null); 
        double TW3E = this.TW3E(null); 
        double TW4E = this.TW4E(null);
        double HPOSa = this.HPOSa(constants8);
        double HPOSb = this.HPOSb(constants8);
        double HPOSc = this.HPOSc(constants8);
        double HPOSd = this.HPOSd(constants8);
        double HPOSe = this.HPOSe(constants8);
        double lambdaU = this.lambda1(constants0);
        this.set_lambda1(lambdaU);
        this.set_lambda1Unc(this.lambda1Unc(constants0));
        double lambdaD = this.lambda1(constants0);
        this.set_lambda2(lambdaD);
        this.set_lambda2Unc(this.lambda2Unc(constants0));
        double yOffset = this.yOffset(constants0);
        double vU = this.v1(constants1);
        double vD = this.v2(constants1);
        double vUUnc = this.v1Unc(constants1);
        double vDUnc = this.v2Unc(constants1);
        double PEDU = this.PED1();
        double PEDD = this.PED2();
        double PEDUUnc = this.PED1Unc();
        double PEDDUnc = this.PED2Unc();
        double paddle2paddle = this.PaddleToPaddle(constants2);
        double RFPad = this.RFPad(constants2);
        double timeOffset = this.TimeOffset(constants2);
        double[] LSBConv = this.LSBConversion(constants3);
        double LSBConvErr = this.LSBConversionUnc();
        double ADCUErr = this.ADC1Unc();
        double ADCDErr = this.ADC2Unc();
        double TDCUErr = this.TDC1Unc();
        double TDCDErr = this.TDC2Unc();
        double ADC_MIP = this.ADC_MIP(constants5);
        double ADC_MIPErr = this.ADC_MIPUnc(constants5);
        double DEDX_MIP = this.DEDX_MIP();
        double ScinBarThickn = this.get_barthickness();

        this.set_HitParams(superlayer, TW0U, TW0D, TW1U, TW1D, TW1P, TW2P, 
                TW0E, TW1E, TW2E, TW3E, TW4E, 
                HPOSa, HPOSb, HPOSc, HPOSd, HPOSe, lambdaU,lambdaD, 
                yOffset, vU, vD, vUUnc, vDUnc, PEDU, PEDD, PEDUUnc,
                PEDDUnc, paddle2paddle, RFPad, timeOffset, triggerPhase, LSBConv, LSBConvErr,
                ADCUErr, ADCDErr, TDCUErr, TDCDErr, ADC_MIP, ADC_MIPErr,
                DEDX_MIP, ScinBarThickn, pl);
        // Set the hit position in the local coordinate of the bar
        this.set_Position(this.calc_hitPosition());

    }

    public void setPaddleLine(CTOFGeant4Factory geometry) {
        // get the line in the middle of the paddle
        org.jlab.detector.volume.Geant4Basic pad = geometry
                .getPaddle(get_Paddle());
        Line3d l = pad.getLineZ();
        Line3D paddleLine = new Line3D();
        // The scintilator paddles are constructed with the length of the paddle
        // as X dimention in the lab frame, so getLineX will return a line going
        // through the center of the paddle and length() equal to the paddle
        // length
        // paddleLine.set(comp.getLineX().origin().x,
        // comp.getLineX().origin().y, comp.getLineX().origin().z,
        // comp.getLineX().end().x, comp.getLineX().end().y,
        // comp.getLineX().end().z);
        // paddleLine.set(comp.getLineX().origin().x,
        // comp.getLineX().origin().y, comp.getLineX().origin().z,
        // comp.getLineX().end().x, comp.getLineX().end().y,
        // comp.getLineX().end().z);

        paddleLine.set(l.origin().x, l.origin().y, l.origin().z, l.end().x,
                l.end().y, l.end().z);
       // this.printInfo();System.out.println(" ");
        this.set_paddleLine(paddleLine);
        
        this.set_barthickness(geometry.getThickness(get_Paddle()));
    }

    private Point3D calc_hitPosition() {
        Point3D hitPosition = new Point3D();
//        Vector3D dir = new Vector3D(this.get_paddleLine().end().x()
//                - this.get_paddleLine().origin().x(), this.get_paddleLine()
//                .end().y()
//                - this.get_paddleLine().origin().y(), this.get_paddleLine()
//                .end().z()
//                - this.get_paddleLine().origin().z());
//        dir.unit();
        Point3D startpoint = this.get_paddleLine().origin();
        // double L_2 = this.get_paddleLine().length()/2;
        // hitPosition.setX(startpoint.x() + (L_2+this.get_y())*dir.x());
        // hitPosition.setY(startpoint.y() + (L_2+this.get_y())*dir.y());
        // hitPosition.setZ(startpoint.z() + (L_2+this.get_y())*dir.z());
        hitPosition.setX(startpoint.x());
        hitPosition.setY(startpoint.y());
        hitPosition.setZ(this.get_y());
//        System.out.println(hitPosition.x() + " " + hitPosition.y() + " " +hitPosition.z() + " " + Constants.SCBARTHICKN[0]);

        return hitPosition;
    }

    @Override
    public boolean isAdjacent(AHit arg0) {
        boolean isClose = false;
        if(this.get_Paddle()==arg0.get_Paddle()-1 || this.get_Paddle()==arg0.get_Paddle()+1) isClose=true;
        if((this.get_Paddle()==1 && arg0.get_Paddle()==Constants.NPAD[0]) ||
           (arg0.get_Paddle()==1 && this.get_Paddle()==Constants.NPAD[0])) isClose=true;    
        return isClose;
    }

    public void printInfo() {
        DecimalFormat form = new DecimalFormat("#.##");
        String s = " CTOF Hit in  Paddle " + this.get_Paddle()
                + " with Status " + this.get_StatusWord() + " in Cluster "
                + this.get_AssociatedClusterID() + " : \n" + "  ADCU =  "
                + this.get_ADC1() + "  ADCD =  " + this.get_ADC2()
                + "  TDCU =  " + this.get_TDC1() + "  TDCD =  "
                + this.get_TDC2() + "\n  tU =  " + form.format(this.get_t1())
                + "  tD =  " + form.format(this.get_t2()) + "  t =  "
                + form.format(this.get_t()) + "  timeWalkU =  "
                + form.format(this.get_timeWalk1()) + "  timeWalkD =  "
                + form.format(this.get_timeWalk2()) + "  lambdaU =  "
                + form.format(this.get_lambda1()) + "  lambdaD =  "
                + form.format(this.get_lambda2()) + "  Energy =  "
                + form.format(this.get_Energy()) + "  EnergyU =  "
                + form.format(this.get_Energy1()) + "  EnergyD =  "
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
    
    @Override
    public double TW01(IndexedTable tab) {
        //double TW0U = CCDBConstants.getTW0U()[this.get_Sector() - 1][this
        //        .get_Panel() - 1][this.get_Paddle() - 1];
        return 0;
    }

    @Override
    public double TW02(IndexedTable tab) {
        //double TW0D = CCDBConstants.getTW0D()[this.get_Sector() - 1][this
        //        .get_Panel() - 1][this.get_Paddle() - 1];
        return 0;
    }

    @Override
    public double TW11(IndexedTable tab) {
        //double TW1U = CCDBConstants.getTW1U()[this.get_Sector() - 1][this
        //        .get_Panel() - 1][this.get_Paddle() - 1];
        return 0;
    }

    @Override
    public double TW12(IndexedTable tab) {
        //double TW1D = CCDBConstants.getTW1D()[this.get_Sector() - 1][this
         //       .get_Panel() - 1][this.get_Paddle() - 1];
         return 0;
    }

    @Override
    public double TW0E(IndexedTable tab) {
         return 0;
    }

    @Override
    public double TW1E(IndexedTable tab) {
         return 0;
    }

    @Override
    public double TW2E(IndexedTable tab) {
         return 0;
    }

    @Override
    public double TW3E(IndexedTable tab) {
         return 0;
    }

    @Override
    public double TW4E(IndexedTable tab) {
         return 0;
    }

    @Override
    public double HPOSa(IndexedTable tab) {
         return tab.getDoubleValue("hposa", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double HPOSb(IndexedTable tab) {
         return tab.getDoubleValue("hposb", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double HPOSc(IndexedTable tab) {
         return tab.getDoubleValue("hposc", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double HPOSd(IndexedTable tab) {
         return tab.getDoubleValue("hposd", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double HPOSe(IndexedTable tab) {
         return tab.getDoubleValue("hpose", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double TW1P(IndexedTable tab) {
        return 0;
    }

    @Override
    public double TW2P(IndexedTable tab) {
        return 0;
    }

    @Override
    public double lambda1(IndexedTable tab) {
        //return CCDBConstants.getLAMBDAU()[this.get_Sector() - 1][this
        //        .get_Panel() - 1][this.get_Paddle() - 1];
        return tab.getDoubleValue("attlen_upstream", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double lambda2(IndexedTable tab) {
        //return CCDBConstants.getLAMBDAD()[this.get_Sector() - 1][this
        //        .get_Panel() - 1][this.get_Paddle() - 1];
        return tab.getDoubleValue("attlen_downstream", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double lambda1Unc(IndexedTable tab) {
        //return CCDBConstants.getLAMBDAUU()[this.get_Sector() - 1][this
        //        .get_Panel() - 1][this.get_Paddle() - 1];
        return tab.getDoubleValue("attlen_upstream_err", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double lambda2Unc(IndexedTable tab) {
        //return CCDBConstants.getLAMBDADU()[this.get_Sector() - 1][this
        //        .get_Panel() - 1][this.get_Paddle() - 1];
        return tab.getDoubleValue("attlen_downstream_err", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double yOffset(IndexedTable tab) {
        return -(this.get_paddleLine().origin().z()+this.get_paddleLine().end().z())/2;
    }

    @Override
    public double v1(IndexedTable tab) {
        //return CCDBConstants.getEFFVELU()[this.get_Sector() - 1][this
        //        .get_Panel() - 1][this.get_Paddle() - 1];
        return tab.getDoubleValue("veff_upstream", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double v2(IndexedTable tab) {
        //return CCDBConstants.getEFFVELD()[this.get_Sector() - 1][this
        //        .get_Panel() - 1][this.get_Paddle() - 1];
        return tab.getDoubleValue("veff_downstream", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double v1Unc(IndexedTable tab) {
        //return CCDBConstants.getEFFVELUU()[this.get_Sector() - 1][this
        //        .get_Panel() - 1][this.get_Paddle() - 1];
        return tab.getDoubleValue("veff_upstream_err", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double v2Unc(IndexedTable tab) {
        //return CCDBConstants.getEFFVELDU()[this.get_Sector() - 1][this
        //        .get_Panel() - 1][this.get_Paddle() - 1];
        return tab.getDoubleValue("veff_downstream_err", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double PED1() {
        return Constants.PEDU[this.get_Panel() - 1];
    }

    @Override
    public double PED2() {
        return Constants.PEDD[this.get_Panel() - 1];
    }

    @Override
    public double PED1Unc() {
        return Constants.PEDUUNC[this.get_Panel() - 1];
    }

    @Override
    public double PED2Unc() {
        return Constants.PEDDUNC[this.get_Panel() - 1];
    }

    @Override
    public double ADC1Unc() {
        return Constants.ADCJITTERU;
    }

    @Override
    public double TDC2Unc() {
        return Constants.TDCJITTERD;
    }

    @Override
    public double TDC1Unc() {
        return Constants.TDCJITTERU;
    }

    @Override
    public double ADC2Unc() {
        return Constants.ADCJITTERD;
    }

    @Override
    public double PaddleToPaddle(IndexedTable tab) {
        //return CCDBConstants.getPADDLE2PADDLE()[this.get_Sector() - 1][this
        //        .get_Panel() - 1][this.get_Paddle() - 1];
        return tab.getDoubleValue("paddle2paddle", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    private double RFPad(IndexedTable tab) {
        // TODO Auto-generated method stub
        //return CCDBConstants.getRFPAD()[this.get_Sector() - 1][this
        //        .get_Panel() - 1][this.get_Paddle() - 1];
        return tab.getDoubleValue("rfpad", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double TimeOffset(IndexedTable tab) {
        //return CCDBConstants.getUD()[this.get_Sector() - 1][this
        //        .get_Panel() - 1][this.get_Paddle() - 1];
        return tab.getDoubleValue("upstream_downstream", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double[] LSBConversion(IndexedTable tab) {
        //return CCDBConstants.getLSBCONVFAC()[this.get_Sector() - 1][this.get_Panel() - 1][this
        //        .get_Paddle() - 1];
        return new double[] {tab.getDoubleValue("upstream", this.get_Sector(),this.get_Panel(),this.get_Paddle()),
                 tab.getDoubleValue("downstream", this.get_Sector(),this.get_Panel(),this.get_Paddle())};
    }

    @Override
    public double LSBConversionUnc() {
        return Constants.LSBCONVFACERROR;
    }

    @Override
    public double ADC_MIP(IndexedTable tab) {
//        return Constants.ADC_MIP[this.get_Panel() - 1];
        return tab.getDoubleValue("mipa_upstream", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double ADC_MIPUnc(IndexedTable tab) {
//        return Constants.ADC_MIP_UNC[this.get_Panel() - 1];
        return tab.getDoubleValue("mipa_upstream_err", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double DEDX_MIP() {
        return Constants.DEDX_MIP;
    }

    @Override
    public int Status1(IndexedTable tab) {
        //return CCDBConstants.getSTATUSL()[this.get_Sector() - 1][this
        //        .get_Panel() - 1][this.get_Paddle() - 1];
        return tab.getIntValue("stat_upstream", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public int Status2(IndexedTable tab) {
        //return CCDBConstants.getSTATUSR()[this.get_Sector() - 1][this
        //        .get_Panel() - 1][this.get_Paddle() - 1];
        return tab.getIntValue("stat_downstream", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }
    public static void main(String arg[]) throws FileNotFoundException {
        /*
		 * CTOFEngine rec = new CTOFEngine() ; rec.init(); HitReader hrd = new
		 * HitReader();
		 * 
		 * // get the status int id = 1; int sector = 1; int paddle = 21; // set
		 * the superlayer to get the paddle position from the geometry package
		 * int superlayer = 1; List<ArrayList<Path3D>> trks = null;
		 * List<double[]> paths = null; CTOFGeometry geometry = new
		 * CTOFGeometry(); //Detector geometry = rec.getGeometry("CTOF"); int
		 * statusL = CalibrationConstantsLoader.STATUSU[sector-1][0][paddle-1];
		 * int statusR =
		 * CalibrationConstantsLoader.STATUSD[sector-1][0][paddle-1]; // create
		 * the hit object Hit hit = new Hit(id, 1, sector, paddle, 900, 900,
		 * 800, 1000) ; String statusWord = hrd.set_StatusWord(statusL, statusR,
		 * hit.get_ADC1(), hit.get_TDC1(), hit.get_ADC2(), hit.get_TDC2()); //
		 * get the line in the middle of the paddle
		 * //hit.set_paddleLine(hit.calc_PaddleLine(geometry));
		 * hit.set_StatusWord(statusWord); hit.set_HitParameters(superlayer); //
		 * read the hit object System.out.println(" hit "); hit.printInfo();
         */
    }

}
