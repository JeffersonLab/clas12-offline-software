package org.jlab.rec.eb;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import org.jlab.clas.detector.DetectorResponse;
import org.jlab.clas.detector.DetectorParticle;
import org.jlab.detector.base.DetectorType;
import org.jlab.clas.pdg.PhysicsConstants;

public class EBUtil {

     /**
     * Perform a basic true/false identification for electrons.
     */
    public static boolean isSimpleElectron(DetectorParticle p,EBCCDBConstants ccdb) {
       
        // require ECAL:
        final int sector = p.getSector(DetectorType.ECAL);
        if (sector<1) return false;
       
        // requre HTCC photoelectrons:
        final double nphe = p.getNphe(DetectorType.HTCC);
        if (nphe < ccdb.getDouble(EBCCDBEnum.HTCC_NPHE_CUT)) return false;
        
        // require ECAL sampling fraction:
        final double sfNSigma = SamplingFractions.getNSigma(11,p,ccdb);
        final double nSigmaCut = ccdb.getSectorDouble(EBCCDBEnum.ELEC_SF_nsigma,sector);
        if (abs(sfNSigma) > nSigmaCut) return false;
       
        // require PCAL minimum energy:
        final double minPcalEnergy = ccdb.getSectorDouble(EBCCDBEnum.ELEC_PCAL_min_energy,sector);
        final double pcalEnergy = p.getEnergy(DetectorType.ECAL,1);
        if (pcalEnergy < minPcalEnergy) return false;
       
        return true;
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
    public static double getDetTimingResolution(DetectorResponse resp,EBCCDBConstants ccdb) {
        final int sector = resp.getDescriptor().getSector();
        final int layer = resp.getDescriptor().getLayer();
        final int component = resp.getDescriptor().getComponent();
        String tableName=null;
        if (resp.getDescriptor().getType()==DetectorType.FTOF) {
            tableName="/calibration/ftof/tres";
        }
        else if (resp.getDescriptor().getType()==DetectorType.CTOF) {
            // CTOF doesn't currently have time resolution available in ccdb.
            // This is the design value:
            return 0.065;
        }
        else {
            throw new RuntimeException("not ready for non-TOF");
        }
        return ccdb.getTable(tableName).
            getDoubleValue("tres",sector,layer,component);
    }

    /**
     * Calculate beta for given detector type/layer, prioritized by layer:
     */
    public static double getNeutralBeta(DetectorParticle p, DetectorType type, int[] layers,double startTime) {
        double beta=-9999;
        for (int layer : layers) {
            DetectorResponse resp = p.getHit(type,layer);
            if (resp!=null) {
                beta = resp.getPosition().mag() /
                    (resp.getTime()-startTime) /
                    PhysicsConstants.speedOfLight();
                break;
            }
        }
        return beta;
    }

    /**
     * Calculate beta for given detector type:
     */
    public static double getNeutralBeta(DetectorParticle p, DetectorType type, int layer,double startTime) {
        return getNeutralBeta(p,type,new int[]{layer},startTime);
    }

}

