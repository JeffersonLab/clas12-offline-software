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
import org.jlab.geom.prim.Vector3D;
import org.jlab.geometry.prim.Line3d;
import org.jlab.rec.ctof.CCDBConstants;
import org.jlab.rec.ctof.Constants;
import org.jlab.rec.tof.hit.AHit;
import org.jlab.rec.tof.hit.IGetCalibrationParams;

/**
 * @author ziegler
 *
 */
public class Hit extends AHit implements IGetCalibrationParams {

    public Hit(int id, int panel, int sector, int paddle, int aDCU, int tDCU,
            int aDCD, int tDCD) {
        super(id, panel, sector, paddle, aDCU, tDCU, aDCD, tDCD);
    }

    private Line3D _paddleLine; // paddle line

    private CTOFDetHit _matchedTrackHit; // matched hit information from
    // tracking; this contains the
    // information of the entrance and
    // exit point of the track with the
    // FTOF hit counter
    private Line3d _matchedTrack;

    public short _AssociatedTrkId;

    public Line3D get_paddleLine() {
        return _paddleLine;
    }

    public void set_paddleLine(Line3D paddleLine) {
        this._paddleLine = paddleLine;
    }

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

    public void set_HitParameters(int superlayer) {

        double pl = this.get_paddleLine().length();

        // Get all the constants used in the hit parameters calculation
        double TW0U = this.TW01();
        double TW0D = this.TW02();
        double TW1U = this.TW11();
        double TW1D = this.TW12();
        double lambdaU = this.lambda1();
        this.set_lambda1(lambdaU);
        this.set_lambda1Unc(this.lambda1Unc());
        double lambdaD = this.lambda1();
        this.set_lambda2(lambdaD);
        this.set_lambda2Unc(this.lambda2Unc());
        double yOffset = this.yOffset();
        double vU = this.v1();
        double vD = this.v2();
        double vUUnc = this.v1Unc();
        double vDUnc = this.v2Unc();
        double PEDU = this.PED1();
        double PEDD = this.PED2();
        double PEDUUnc = this.PED1Unc();
        double PEDDUnc = this.PED2Unc();
        double paddle2paddle = this.PaddleToPaddle();
        double RFPad = this.RFPad();
        double timeOffset = this.TimeOffset();
        double[] LSBConv = this.LSBConversion();
        double LSBConvErr = this.LSBConversionUnc();
        double ADCUErr = this.ADC1Unc();
        double ADCDErr = this.ADC2Unc();
        double TDCUErr = this.TDC1Unc();
        double TDCDErr = this.TDC2Unc();
        double ADC_MIP = this.ADC_MIP();
        double ADC_MIPErr = this.ADC_MIPUnc();
        double DEDX_MIP = this.DEDX_MIP();
        double ScinBarThickn = this.ScinBarThickn();

        this.set_HitParams(superlayer, TW0U, TW0D, TW1U, TW1D, lambdaU,
                lambdaD, yOffset, vU, vD, vUUnc, vDUnc, PEDU, PEDD, PEDUUnc,
                PEDDUnc, paddle2paddle, RFPad, timeOffset, LSBConv, LSBConvErr,
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

        this.set_paddleLine(paddleLine);
    }

    private Point3D calc_hitPosition() {
        Point3D hitPosition = new Point3D();
        Vector3D dir = new Vector3D(this.get_paddleLine().end().x()
                - this.get_paddleLine().origin().x(), this.get_paddleLine()
                .end().y()
                - this.get_paddleLine().origin().y(), this.get_paddleLine()
                .end().z()
                - this.get_paddleLine().origin().z());
        dir.unit();
        Point3D startpoint = this.get_paddleLine().origin();
        // double L_2 = this.get_paddleLine().length()/2;
        // hitPosition.setX(startpoint.x() + (L_2+this.get_y())*dir.x());
        // hitPosition.setY(startpoint.y() + (L_2+this.get_y())*dir.y());
        // hitPosition.setZ(startpoint.z() + (L_2+this.get_y())*dir.z());
        hitPosition.setX(startpoint.x() + Constants.SCBARTHICKN[0] / 2
                * dir.x());
        hitPosition.setY(startpoint.y() + Constants.SCBARTHICKN[0] / 2
                * dir.y());
        hitPosition.setZ(this.get_y());

        return hitPosition;
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

    @Override
    public double TW01() {
        double TW0U = CCDBConstants.getTW0U()[this.get_Sector() - 1][this
                .get_Panel() - 1][this.get_Paddle() - 1];

        return TW0U;
    }

    @Override
    public double TW02() {
        double TW0D = CCDBConstants.getTW0D()[this.get_Sector() - 1][this
                .get_Panel() - 1][this.get_Paddle() - 1];

        return TW0D;
    }

    @Override
    public double TW11() {
        double TW1U = CCDBConstants.getTW1U()[this.get_Sector() - 1][this
                .get_Panel() - 1][this.get_Paddle() - 1];

        return TW1U;
    }

    @Override
    public double TW12() {
        double TW1D = CCDBConstants.getTW1D()[this.get_Sector() - 1][this
                .get_Panel() - 1][this.get_Paddle() - 1];

        return TW1D;
    }

    @Override
    public double lambda1() {
        return CCDBConstants.getLAMBDAU()[this.get_Sector() - 1][this
                .get_Panel() - 1][this.get_Paddle() - 1];
    }

    @Override
    public double lambda2() {
        return CCDBConstants.getLAMBDAD()[this.get_Sector() - 1][this
                .get_Panel() - 1][this.get_Paddle() - 1];
    }

    @Override
    public double lambda1Unc() {
        return CCDBConstants.getLAMBDAUU()[this.get_Sector() - 1][this
                .get_Panel() - 1][this.get_Paddle() - 1];
    }

    @Override
    public double lambda2Unc() {
        return CCDBConstants.getLAMBDADU()[this.get_Sector() - 1][this
                .get_Panel() - 1][this.get_Paddle() - 1];
    }

    @Override
    public double yOffset() {
        double ccdbOffset = CCDBConstants.getYOFF()[this.get_Sector() - 1][this
                .get_Panel() - 1][this.get_Paddle() - 1];
        double shift = Constants.DYHL;
        if (this.get_Paddle() % 2 == 1) {
            shift = 0;
        }
        double paddleCenteringOffset = Constants.PCO;
        return ccdbOffset - shift + paddleCenteringOffset;
    }

    @Override
    public double v1() {
        return CCDBConstants.getEFFVELU()[this.get_Sector() - 1][this
                .get_Panel() - 1][this.get_Paddle() - 1];
    }

    @Override
    public double v2() {
        return CCDBConstants.getEFFVELD()[this.get_Sector() - 1][this
                .get_Panel() - 1][this.get_Paddle() - 1];
    }

    @Override
    public double v1Unc() {
        return CCDBConstants.getEFFVELUU()[this.get_Sector() - 1][this
                .get_Panel() - 1][this.get_Paddle() - 1];
    }

    @Override
    public double v2Unc() {
        return CCDBConstants.getEFFVELDU()[this.get_Sector() - 1][this
                .get_Panel() - 1][this.get_Paddle() - 1];
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
    public double PaddleToPaddle() {
        return CCDBConstants.getPADDLE2PADDLE()[this.get_Sector() - 1][this
                .get_Panel() - 1][this.get_Paddle() - 1];
    }

    private double RFPad() {
        // TODO Auto-generated method stub
        return CCDBConstants.getRFPAD()[this.get_Sector() - 1][this
                .get_Panel() - 1][this.get_Paddle() - 1];
    }

    @Override
    public double TimeOffset() {
        return CCDBConstants.getUD()[this.get_Sector() - 1][this
                .get_Panel() - 1][this.get_Paddle() - 1];
    }

    @Override
    public double[] LSBConversion() {
        return CCDBConstants.getLSBCONVFAC()[this.get_Sector() - 1][this.get_Panel() - 1][this
                .get_Paddle() - 1];
    }

    @Override
    public double LSBConversionUnc() {
        return Constants.LSBCONVFACERROR;
    }

    @Override
    public double ADC_MIP() {
        return Constants.ADC_MIP[this.get_Panel() - 1];
    }

    @Override
    public double ADC_MIPUnc() {
        return Constants.ADC_MIP_UNC[this.get_Panel() - 1];
    }

    @Override
    public double DEDX_MIP() {
        return Constants.DEDX_MIP;
    }

    @Override
    public double ScinBarThickn() {
        return Constants.SCBARTHICKN[this.get_Panel() - 1];
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
