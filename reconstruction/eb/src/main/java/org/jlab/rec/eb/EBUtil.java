package org.jlab.rec.eb;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import org.jlab.clas.detector.ScintillatorResponse;
import org.jlab.clas.detector.DetectorResponse;
import org.jlab.clas.detector.DetectorParticle;
import org.jlab.detector.base.DetectorType;
import org.jlab.clas.pdg.PhysicsConstants;

public class EBUtil {

     /**
     * Get the mean expected ampling fraction.
     */
    public static double getExpectedSamplingFraction(final double measuredEnergy,EBCCDBConstants ccdb) {
        final Double[] t = ccdb.getArray(EBCCDBEnum.ELEC_SF);
        final double sfMean = t[0]*(t[1] + t[2]/measuredEnergy + t[3]*pow(measuredEnergy,-2));
        return sfMean;
    }

     /**
     * Get the sigma of the expected sampling fraction.
     */
    public static double getExpectedSamplingFractionSigma(final double measuredEnergy,EBCCDBConstants ccdb) {
        final Double[] s = ccdb.getArray(EBCCDBEnum.ELEC_SFS);
        final double sfSigma = s[0];
        return sfSigma;
    }

     /**
     * Calculate the signed number of sigma from the expected sampling fraction.
     */
    public static double getSamplingFractionNSigma(DetectorParticle p,EBCCDBConstants ccdb) {
        final double ener = p.getEnergy(DetectorType.ECAL);
        final double sf = p.getEnergyFraction(DetectorType.ECAL);
        final double sfMean = getExpectedSamplingFraction(ener,ccdb);
        final double sfSigma = getExpectedSamplingFractionSigma(ener,ccdb);
        return (sf-sfMean) / sfSigma;
    }

     /**
     * Perform a basic true/false identification for electrons.
     */
    public static boolean isSimpleElectron(DetectorParticle p,EBCCDBConstants ccdb) {
        
        final double pcalEnergy = p.getEnergy(DetectorType.ECAL,1);
        final double nphe = p.getNphe(DetectorType.HTCC);
        final double sfNSigma = getSamplingFractionNSigma(p,ccdb);

        boolean isElectron=true;

        if (nphe < EBConstants.HTCC_NPHE_CUT)
            isElectron=false;
        
        else if (abs(sfNSigma) > EBConstants.ECAL_SF_NSIGMA)
            isElectron=false;
        
        else if (pcalEnergy < EBConstants.PCAL_ELEC_MINENERGY)
            isElectron=false;
        
        return isElectron;
    }

    /**
     * Calculate timing resolution from EventBuilder constants:
     */
    public static double getEBTimingResolution(DetectorParticle p, DetectorType type, int layer,EBCCDBConstants ccdb) {
        Double[] pars;
        if (type==DetectorType.FTOF) {
            if (layer==1) pars=ccdb.getArray(EBCCDBEnum.FTOF1A_TimingRes);
            if (layer==2) pars=ccdb.getArray(EBCCDBEnum.FTOF1B_TimingRes);
            else          pars=ccdb.getArray(EBCCDBEnum.FTOF2_TimingRes);
        }
        else if (type==DetectorType.CTOF) {
            pars=ccdb.getArray(EBCCDBEnum.CTOF_TimingRes);
        }
        else throw new RuntimeException("not ready for non-TOF");
        final double mom=p.vector().mag();
        double res=0;
        for (int ii=0; ii<pars.length; ii++) res += pars[ii]*pow(mom,ii);
        return res;
    }

    /**
     * Get timing resolution from detector calibration constants:
     */
    public static double getDetTimingResolution(ScintillatorResponse resp,int run,EBCCDBConstants ccdb) {
        final int sector = resp.getDescriptor().getSector();
        final int layer = resp.getDescriptor().getLayer();
        final int component = resp.getDescriptor().getComponent();
        String tableName=null;
        if (resp.getDescriptor().getType()==DetectorType.FTOF) {
            tableName="/calibration/ftof/tres";
        }
        else {
            throw new RuntimeException("not ready for non-FTOF");
        }
        return ccdb.getTable(tableName).
            getDoubleValue("tres",sector,layer,component);
    }

    /**
     * Calculate beta for given detector type:
     */
    public static double getNeutralBeta(DetectorParticle p, DetectorType type, int layer,double startTime) {
        double beta=-1;
        DetectorResponse resp = p.getResponse(type,layer);
        if (resp!=null) {
            beta = resp.getPosition().mag() / 
                (resp.getTime()-startTime) / 
                PhysicsConstants.speedOfLight(); 
        }
        return beta;
    }

    /**
     * Calculate beta for ECAL, prioritized by layer:
     */
    public static double getNeutralBetaECAL(DetectorParticle p, double startTime) {
        double      beta = getNeutralBeta(p,DetectorType.ECAL,1,startTime);
        if (beta<0) beta = getNeutralBeta(p,DetectorType.ECAL,4,startTime);
        if (beta<0) beta = getNeutralBeta(p,DetectorType.ECAL,7,startTime);
        return beta;
    }


}

