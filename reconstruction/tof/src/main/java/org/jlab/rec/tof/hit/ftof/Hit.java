/**
 *
 */
package org.jlab.rec.tof.hit.ftof;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.detector.geant4.v2.FTOFGeant4Factory;
import org.jlab.detector.hits.DetHit;
import org.jlab.detector.hits.FTOFDetHit;
import org.jlab.detector.volume.G4Box;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.geometry.prim.Line3d;
import org.jlab.rec.ftof.Constants;
import org.jlab.rec.tof.banks.ftof.HitReader;
import org.jlab.rec.tof.hit.AHit;
import org.jlab.rec.tof.hit.IGetCalibrationParams;
import eu.mihosoft.vrl.v3d.Vector3d;
import org.jlab.service.ftof.FTOFHBEngine;
import org.jlab.utils.groups.IndexedTable;

/**
 * @author ziegler
 *
 */
public class Hit extends AHit implements IGetCalibrationParams {

    /**
     * IMPORTANT: A possible mismatch between the evaluation at the face of the
     * counter from tracking and in the middle of the counter from clustering
     * needs to be checked in the geometry package !!!
     */
    public Hit(int id, int panel, int sector, int paddle, int aDCL, int tDCL,
            int aDCR, int tDCR) {
        super(id, panel, sector, paddle, aDCL, tDCL, aDCR, tDCR);

    }

    private Line3D _paddleLine; // paddle line
    private FTOFDetHit _matchedTrackHit; // matched hit information from
    // tracking; this contains the
    // information of the entrance and
    // exit point of the track with the
    // FTOF hit counter
    private Line3d _matchedTrack;
    
    public int trkAssociated_Paddle;

    public Line3D get_paddleLine() {
        return _paddleLine;
    }

    public void set_paddleLine(Line3D paddleLine) {
        this._paddleLine = paddleLine;
    }

    public FTOFDetHit get_matchedTrackHit() {
        return _matchedTrackHit;
    }

    public void set_matchedTrackHit(FTOFDetHit matchedTrackHit) {
        this._matchedTrackHit = matchedTrackHit;
    }

    public Line3d get_matchedTrack() {
        return _matchedTrack;
    }

    public void set_matchedTrack(Line3d _matchedTrack) {
        this._matchedTrack = _matchedTrack;
    }

    public void set_HitParameters(int superlayer, double triggerPhase, IndexedTable constants0, 
            IndexedTable constants1, 
            IndexedTable constants2, 
            IndexedTable constants3, 
            IndexedTable constants5, 
            IndexedTable constants6, 
            IndexedTable constants8, 
            IndexedTable constants9) {/*
        0: "/calibration/ftof/attenuation"),
        1: "/calibration/ftof/effective_velocity"),
        2: "/calibration/ftof/time_offsets"),
        3: "/calibration/ftof/time_walk"),
        4: "/calibration/ftof/status"),
        5: "/calibration/ftof/gain_balance"),
        6: "/calibration/ftof/tdc_conv"),
        8: "/calibration/ftof/time_walk_pos"),
        9: "/calibration/ftof/time_walk_exp"));
        */
        double pl = this.get_paddleLine().length();

        // Get all the constants used in the hit parameters calculation
        double TW0L = this.TW01(constants3);
        double TW0R = this.TW02(constants3);
        double TW1L = this.TW11(constants3);
        double TW1R = this.TW12(constants3); 
        double TW1P = this.TW1P(constants8); 
        double TW2P = this.TW2P(constants8); 
        double TW0E = this.TW0E(constants9); 
        double TW1E = this.TW1E(constants9); 
        double TW2E = this.TW2E(constants9); 
        double TW3E = this.TW3E(constants9); 
        double TW4E = this.TW4E(constants9); 
        double HPOSa = this.HPOSa(null);
        double HPOSb = this.HPOSb(null);
        double HPOSc = this.HPOSc(null);
        double HPOSd = this.HPOSd(null);
        double HPOSe = this.HPOSe(null);
        double lambdaL = this.lambda1(constants0);
        this.set_lambda1(lambdaL);
        this.set_lambda1Unc(this.lambda1Unc(constants0));
        double lambdaR = this.lambda1(constants0);
        this.set_lambda2(lambdaR);
        this.set_lambda2Unc(this.lambda2Unc(constants0));
        double yOffset = this.yOffset(constants0);
        double vL = this.v1(constants1);
        double vR = this.v2(constants1);
        double vLUnc = this.v1Unc(constants1);
        double vRUnc = this.v2Unc(constants1);
        double PEDL = this.PED1();
        double PEDR = this.PED2();
        double PEDLUnc = this.PED1Unc();
        double PEDRUnc = this.PED2Unc();
        double paddle2paddle = this.PaddleToPaddle(constants2);
        double RFPad = this.RFPad(constants2);
        double timeOffset = this.TimeOffset(constants2);
        double[] LSBConv = this.LSBConversion(constants6);
        double LSBConvErr = this.LSBConversionUnc();
        double ADCLErr = this.ADC1Unc();
        double ADCRErr = this.ADC2Unc();
        double TDCLErr = this.TDC1Unc();
        double TDCRErr = this.TDC2Unc();
        double ADC_MIP = this.ADC_MIP(constants5);
        double ADC_MIPErr = this.ADC_MIPUnc(constants5);
        double DEDX_MIP = this.DEDX_MIP();
        double ScinBarThickn = this.get_barthickness();

        this.set_HitParams(superlayer, TW0L, TW0R, TW1L, TW1R, TW1P, TW2P, 
                TW0E, TW1E, TW2E, TW3E, TW4E,
                HPOSa, HPOSb, HPOSc, HPOSd, HPOSe, lambdaL,lambdaR, 
                yOffset, vL, vR, vLUnc, vRUnc, PEDL, PEDR, PEDLUnc,
                PEDRUnc, paddle2paddle, RFPad, timeOffset, triggerPhase, LSBConv, LSBConvErr,
                ADCLErr, ADCRErr, TDCLErr, TDCRErr, ADC_MIP, ADC_MIPErr,
                DEDX_MIP, ScinBarThickn, pl);
        // Set the hit position in the local coordinate of the bar
        this.set_Position(this.calc_hitPosition());

    }

    public void setPaddleLine(FTOFGeant4Factory geometry) {
        // get the line in the middle of the paddle
        G4Box comp = (G4Box) geometry.getComponent(this.get_Sector(),
                this.get_Panel(), this.get_Paddle());
        Line3D paddleLine = new Line3D();
        // The scintilator paddles are constructed with the length of the paddle
        // as X dimention in the lab frame, so getLineX will return a line going
        // through the center of the paddle and length() equal to the paddle
        // length
        paddleLine.set(comp.getLineX().origin().x, comp.getLineX().origin().y,
                comp.getLineX().origin().z, comp.getLineX().end().x, comp
                .getLineX().end().y, comp.getLineX().end().z);
        this.set_paddleLine(paddleLine);
        this.set_barthickness(geometry.getThickness(this.get_Sector(),this.get_Panel(), this.get_Paddle()));
    }

    public Point3D calc_hitPosition() {
        Point3D hitPosition = new Point3D();
        Vector3D dir = new Vector3D(this.get_paddleLine().end().x()
                - this.get_paddleLine().origin().x(), this.get_paddleLine()
                .end().y()
                - this.get_paddleLine().origin().y(), this.get_paddleLine()
                .end().z()
                - this.get_paddleLine().origin().z());
        dir.unit();
        Point3D startpoint = this.get_paddleLine().origin();
        double L_2 = this.get_paddleLine().length() / 2;
        hitPosition.setX(startpoint.x() + (L_2 + this.get_y()) * dir.x());
        hitPosition.setY(startpoint.y() + (L_2 + this.get_y()) * dir.y());
        hitPosition.setZ(startpoint.z() + (L_2 + this.get_y()) * dir.z());

        return hitPosition;

    }

    public void printInfo() {
        DecimalFormat form = new DecimalFormat("#.##");
        String s = " FTOF Hit in " + " Sector " + this.get_Sector() + " Panel "
                + this.get_Panel() + " Paddle " + this.get_Paddle()
                + " with Status " + this.get_StatusWord() + " in Cluster "
                + this.get_AssociatedClusterID() + " : \n" + "  ADCL =  "
                + this.get_ADC1() + "  ADCR =  " + this.get_ADC2()
                + "  TDCL =  " + this.get_TDC1() + "  TDCR =  "
                + this.get_TDC2() + "\n  tL =  " + form.format(this.get_t1())
                + "  tR =  " + form.format(this.get_t2()) + "  t =  "
                + form.format(this.get_t()) + "  timeWalkL =  "
                + form.format(this.get_timeWalk1()) + "  timeWalkR =  "
                + form.format(this.get_timeWalk2()) + "  lambdaL =  "
                + form.format(this.get_lambda1()) + "  lambdaR =  "
                + form.format(this.get_lambda2()) + "  Energy =  "
                + form.format(this.get_Energy()) + "  EnergyL =  "
                + form.format(this.get_Energy1()) + "  EnergyR =  "
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
        //double TW0L = CCDBConstants.getTW0L()[this.get_Sector() - 1][this
        //        .get_Panel() - 1][this.get_Paddle() - 1];
        return tab.getDoubleValue("tw0_left", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double TW02(IndexedTable tab) {
        //double TW0R = CCDBConstants.getTW0R()[this.get_Sector() - 1][this
        //        .get_Panel() - 1][this.get_Paddle() - 1];
        return tab.getDoubleValue("tw0_right", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double TW11(IndexedTable tab) {
        //double TW1L = CCDBConstants.getTW1L()[this.get_Sector() - 1][this
        //        .get_Panel() - 1][this.get_Paddle() - 1];
        return tab.getDoubleValue("tw1_left", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double TW12(IndexedTable tab) {
        //double TW1R = CCDBConstants.getTW1R()[this.get_Sector() - 1][this
        //        .get_Panel() - 1][this.get_Paddle() - 1];
        return tab.getDoubleValue("tw1_right", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double TW1P(IndexedTable tab) {
        return tab.getDoubleValue("tw1pos", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double TW2P(IndexedTable tab) {
        return tab.getDoubleValue("tw2pos", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double TW0E(IndexedTable tab) {
        return tab.getDoubleValue("tw0", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double TW1E(IndexedTable tab) {
        return tab.getDoubleValue("tw1", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double TW2E(IndexedTable tab) {
        return tab.getDoubleValue("tw2", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double TW3E(IndexedTable tab) {
        return tab.getDoubleValue("tw3", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double TW4E(IndexedTable tab) {
        return tab.getDoubleValue("tw4", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double HPOSa(IndexedTable tab) {
        return 0;
    }

    @Override
    public double HPOSb(IndexedTable tab) {
        return 0;
    }

    @Override
    public double HPOSc(IndexedTable tab) {
        return 0;
    }

    @Override
    public double HPOSd(IndexedTable tab) {
        return 0;
    }

    @Override
    public double HPOSe(IndexedTable tab) {
        return 0;
    }

    @Override
    public double lambda1(IndexedTable tab) {
        //return CCDBConstants.getLAMBDAL()[this.get_Sector() - 1][this
        //        .get_Panel() - 1][this.get_Paddle() - 1];
        return tab.getDoubleValue("attlen_left", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double lambda2(IndexedTable tab) {
        //return CCDBConstants.getLAMBDAR()[this.get_Sector() - 1][this
        //        .get_Panel() - 1][this.get_Paddle() - 1];
        return tab.getDoubleValue("attlen_right", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double lambda1Unc(IndexedTable tab) {
        //return CCDBConstants.getLAMBDALU()[this.get_Sector() - 1][this
        //        .get_Panel() - 1][this.get_Paddle() - 1];
        return tab.getDoubleValue("attlen_left_err", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double lambda2Unc(IndexedTable tab) {
        //return CCDBConstants.getLAMBDARU()[this.get_Sector() - 1][this
        //        .get_Panel() - 1][this.get_Paddle() - 1];
        return tab.getDoubleValue("attlen_right_err", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double yOffset(IndexedTable tab) {
        return 0.0;
    }

    @Override
    public double v1(IndexedTable tab) {
        //return CCDBConstants.getEFFVELL()[this.get_Sector() - 1][this
        //        .get_Panel() - 1][this.get_Paddle() - 1];
        return tab.getDoubleValue("veff_left", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double v2(IndexedTable tab) {
        //return CCDBConstants.getEFFVELR()[this.get_Sector() - 1][this
        //        .get_Panel() - 1][this.get_Paddle() - 1];
        return tab.getDoubleValue("veff_right", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double v1Unc(IndexedTable tab) {
        //return CCDBConstants.getEFFVELLU()[this.get_Sector() - 1][this
        //        .get_Panel() - 1][this.get_Paddle() - 1];
        return tab.getDoubleValue("veff_left_err", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double v2Unc(IndexedTable tab) {
        //return CCDBConstants.getEFFVELRU()[this.get_Sector() - 1][this
        //        .get_Panel() - 1][this.get_Paddle() - 1];
        return tab.getDoubleValue("veff_right_err", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double PED1() {
        return Constants.PEDL[this.get_Panel() - 1];
    }

    @Override
    public double PED2() {
        return Constants.PEDR[this.get_Panel() - 1];
    }

    @Override
    public double PED1Unc() {
        return Constants.PEDLUNC[this.get_Panel() - 1];
    }

    @Override
    public double PED2Unc() {
        return Constants.PEDRUNC[this.get_Panel() - 1];
    }

    @Override
    public double ADC1Unc() {
        return Constants.ADCJITTERL;
    }

    @Override
    public double TDC2Unc() {
        return Constants.TDCJITTERR;
    }

    @Override
    public double TDC1Unc() {
        return Constants.TDCJITTERL;
    }

    @Override
    public double ADC2Unc() {
        return Constants.ADCJITTERR;
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
        //return CCDBConstants.getLR()[this.get_Sector() - 1][this.get_Panel() - 1][this
        //        .get_Paddle() - 1];
        return tab.getDoubleValue("left_right", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double[] LSBConversion(IndexedTable tab) {
        //return CCDBConstants.getLSBCONVFAC()[this.get_Sector() - 1][this.get_Panel() - 1][this
        //        .get_Paddle() - 1];
        return new double[] {tab.getDoubleValue("left", this.get_Sector(),this.get_Panel(),this.get_Paddle()), 
            tab.getDoubleValue("right", this.get_Sector(),this.get_Panel(),this.get_Paddle())
        };
    }

    @Override
    public double LSBConversionUnc() {
        return Constants.LSBCONVFACERROR;
    }

    @Override
    public double ADC_MIP(IndexedTable tab) {
        // return Constants.ADC_MIP[this.get_Panel()-1];
        //return CCDBConstants.getMIPL()[this.get_Sector() - 1][this.get_Panel() - 1][this
        //        .get_Paddle() - 1];
        return tab.getDoubleValue("mipa_left", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double ADC_MIPUnc(IndexedTable tab) {
        // return Constants.ADC_MIP_UNC[this.get_Panel()-1];
        //return CCDBConstants.getMIPLU()[this.get_Sector() - 1][this.get_Panel() - 1][this
        //        .get_Paddle() - 1];
        return tab.getDoubleValue("mipa_left_err", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public double DEDX_MIP() {
        return Constants.DEDX_MIP;
    }

    @Override
    public int Status1(IndexedTable tab) {
        //return CCDBConstants.getSTATUSL()[this.get_Sector() - 1][this
        //        .get_Panel() - 1][this.get_Paddle() - 1];
        return tab.getIntValue("stat_left", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }

    @Override
    public int Status2(IndexedTable tab) {
        //return CCDBConstants.getSTATUSR()[this.get_Sector() - 1][this
        //        .get_Panel() - 1][this.get_Paddle() - 1];
        return tab.getIntValue("stat_right", this.get_Sector(),this.get_Panel(),this.get_Paddle());
    }
    
    public static void main(String arg[]) throws IOException {

        FTOFHBEngine rec = new FTOFHBEngine();
        rec.init();
        HitReader hrd = new HitReader();

        // get the status
        int id = 1;
        int sector = 4;
        int paddle = 21;
        // set the superlayer to get the paddle position from the geometry
        // package
        int superlayer = 1;
        List<ArrayList<Path3D>> trks = null;
        List<double[]> paths = null;
        // Detector geometry = rec.getGeometry("FTOF");
        DatabaseConstantProvider provider = new DatabaseConstantProvider(10,
                "default");

        provider.loadTable("/geometry/ftof/panel1a/paddles");
        provider.loadTable("/geometry/ftof/panel1a/panel");
        provider.loadTable("/geometry/ftof/panel1b/paddles");
        provider.loadTable("/geometry/ftof/panel1b/panel");
        provider.loadTable("/geometry/ftof/panel2/paddles");
        provider.loadTable("/geometry/ftof/panel2/panel");
        // disconncect from database. Important to do this after loading tables.
        provider.disconnect();

        FTOFGeant4Factory factory = new FTOFGeant4Factory(provider);

       // int statusL = CCDBConstants.getSTATUSL()[sector - 1][0][paddle - 1];
       // int statusR = CCDBConstants.getSTATUSR()[sector - 1][0][paddle - 1];

        Random rnd = new Random();

        for (int itrack = 0; itrack < 1000; itrack++) {
            Line3d line = new Line3d(new Vector3d(
                    rnd.nextDouble() * 10000 - 5000,
                    rnd.nextDouble() * 10000 - 5000, 3000), new Vector3d(
                    rnd.nextDouble() * 10000 - 5000,
                    rnd.nextDouble() * 10000 - 5000, 9000));

            List<DetHit> hits = factory.getIntersections(line);

            for (DetHit hit : hits) {
                FTOFDetHit fhit = new FTOFDetHit(hit);
                System.out.println("\t" + fhit.length());
                System.out.println("\t\t" + fhit.getSector());
                System.out.println("\t\t" + fhit.getLayer());
                System.out.println("\t\t" + fhit.getPaddle());
                System.out.println("\t\tentry: " + fhit.origin());
                System.out.println("\t\texit:  " + fhit.end());
                System.out.println("\t\tmid:   " + fhit.mid());
                System.out.println("\t\tlength: " + fhit.length());
            }
        }
        // create the hit object
        // Hit hit = new Hit(id, 1, sector, paddle, 900, 900, 800, 1000) ;
        // String statusWord = hrd.set_StatusWord(statusL, statusR,
        // hit.get_ADC1(), hit.get_TDC1(), hit.get_ADC2(), hit.get_TDC2());

        // System.out.println(statusWord);
        // hit.set_StatusWord(statusWord);
        // hit.set_HitParameters(superlayer, geometry, trks, paths);
        // read the hit object
        // System.out.println(" hit "); hit.printInfo();
    }

    public boolean isAssociatedWTrk(FTOFDetHit[][][][] hitArray, int i) {
        boolean isAssoc = false;

        int jIdxUp = this.get_Paddle() - 1;
        int jIdxDown = this.get_Paddle() - 1;
        int rIdx = 1; // how many counters from the track that hits a particular
        // counter : 2 for finer granularity of panel 1b
        if (this.get_Panel() == 2) {
            rIdx = 2; // : 1 for coarser granularity of panel 1a
        }
        for (int r = rIdx; r >= 0; r--) {
            jIdxUp = this.get_Paddle() - 1 + r;
            jIdxDown = this.get_Paddle() - 1 - r;
            if (jIdxDown < 0) {
                jIdxDown = 0;
            }
            if (jIdxUp >= 61) {
                jIdxUp = 61;
            }
            if (hitArray[this.get_Sector() - 1][this.get_Panel() - 1][jIdxUp][i] != null) {
                isAssoc = true;
                this.trkAssociated_Paddle = jIdxUp + 1;

                // System.out.println(r+") sector "+this.get_Sector()+" panel "+this.get_Panel()+" track associated index "+jIdxUp+" hit panel idx "+(this.get_Paddle()-1));
            }
            if (hitArray[this.get_Sector() - 1][this.get_Panel() - 1][jIdxDown][i] != null) {
                isAssoc = true;
                this.trkAssociated_Paddle = jIdxDown + 1;

                // System.out.println(r+") sector "+this.get_Sector()+" panel "+this.get_Panel()+" track associated index "+jIdxDown+" hit panel idx "+(this.get_Paddle()-1));
            }
        }
        return isAssoc;
    }

}
