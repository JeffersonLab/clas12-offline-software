package org.jlab.rec.eb;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import java.util.List;
import org.jlab.clas.detector.DetectorResponse;
import org.jlab.clas.detector.DetectorParticle;
import org.jlab.clas.detector.ScintillatorResponse;
import org.jlab.detector.base.DetectorType;
import org.jlab.clas.pdg.PhysicsConstants;

public class EBUtil {

     /**
     * Central neutral veto logic from Adam Hobart.
     *
     * @param p
     * @return whether to veto neutrality of p
     *
     * FIXME:  move float parameters to CCDB
     */
    public static boolean centralNeutralVeto(DetectorParticle p) {

        ScintillatorResponse cnd=(ScintillatorResponse)p.getHit(DetectorType.CND);
        ScintillatorResponse ctof=(ScintillatorResponse)p.getHit(DetectorType.CTOF);

        if (cnd!=null || ctof!=null) {

            // CTOF-only veto:
            if (cnd == null) {
                if (ctof.getEnergy() >= 18.0) {
                    return true;
                }
            }

            // CND-only veto:
            else if (ctof == null) {
                if (cnd.getClusterSize() > 3) {
                    return true;
                }
                else if (cnd.getClusterSize() > 2) {
                    if (cnd.getEnergy() > 10) {
                        return true;
                    }
                }
            }

            // CTOF/CND-veto:
            else if (ctof.getEnergy() >= 10.0) {
                return true;
            }
            else if (cnd.getLayerMultiplicity()==1) {
                if (cnd.getEnergy() >= 30.0) {
                    return true;
                }
            }
            else if (cnd.getLayerMultiplicity()==2) {
                if (cnd.getClusterSize() > 2) {
                    return true;
                }
                else if (cnd.getEnergy()+ctof.getEnergy() >= 10.0) {
                    return true;
                }
            }
            else {
                return true;
            }
        }
        return false;
    }
    
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
            return 0;//throw new RuntimeException("not ready for non-TOF");
        }
        return ccdb.getTable(tableName).
            getDoubleValue("tres",sector,layer,component);
    }

}

