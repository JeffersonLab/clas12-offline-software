package org.jlab.rec.eb;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import org.jlab.clas.detector.DetectorParticle;
import org.jlab.detector.base.DetectorType;

public class EBUtil {

     /**
     * Get the mean expected ampling fraction.
     */
    public static double getExpectedSamplingFraction(final double measuredEnergy) {
        final Double[] t = EBCCDBConstants.getArray(EBCCDBEnum.ELEC_SF);
        final double sfMean = t[0]*(t[1] + t[2]/measuredEnergy + t[3]*pow(measuredEnergy,-2));
        return sfMean;
    }

     /**
     * Get the sigma of the expected sampling fraction.
     */
    public static double getExpectedSamplingFractionSigma(final double measuredEnergy) {
        final Double[] s = EBCCDBConstants.getArray(EBCCDBEnum.ELEC_SFS);
        final double sfSigma = s[0];
        return sfSigma;
    }

     /**
     * Calculate the signed number of sigma from the expected sampling fraction.
     */
    public static double getSamplingFractionNSigma(DetectorParticle p) {
        final double ener = p.getEnergy(DetectorType.ECAL);
        final double sf = p.getEnergyFraction(DetectorType.ECAL);
        final double sfMean = getExpectedSamplingFraction(ener);
        final double sfSigma = getExpectedSamplingFractionSigma(ener);
        return (sf-sfMean) / sfSigma;
    }

     /**
     * Perform a basic true/false identification for electrons.
     */
    public static boolean isSimpleElectron(DetectorParticle p) {
        
        final double pcalEnergy = p.getEnergy(DetectorType.ECAL,1);
        final double nphe = p.getNphe(DetectorType.HTCC);
        final double sfNSigma = getSamplingFractionNSigma(p);

        boolean isElectron=true;

        if (nphe < EBConstants.HTCC_NPHE_CUT)
            isElectron=false;
        
        else if (abs(sfNSigma) > EBConstants.ECAL_SF_NSIGMA)
            isElectron=false;
        
        else if (pcalEnergy < EBConstants.PCAL_ELEC_MINENERGY)
            isElectron=false;
        
        return isElectron;
    }

}

