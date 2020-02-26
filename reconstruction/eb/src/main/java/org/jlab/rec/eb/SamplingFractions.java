package org.jlab.rec.eb;

import static java.lang.Math.pow;

import org.jlab.clas.detector.DetectorParticle;
import org.jlab.detector.base.DetectorType;

/**
 * Utility class for calorimeter sampling fractions.
 * @author baltzell
 */

public class SamplingFractions {

    private static DetectorType detType = DetectorType.ECAL;

    /**
     * @return mean of sampling fraction
     */
    public static double getMean(
            final int pid,
            final DetectorParticle part,
            final EBCCDBConstants ccdb) {
        final int sector = part.getSector(detType);
        final double measuredEnergy = part.getEnergy(detType);
        Double[] p;
        switch (pid) {
            case -11:
                p = ccdb.getSectorArray(EBCCDBEnum.ELEC_SF,sector);
                break;
            case 11:
                p = ccdb.getSectorArray(EBCCDBEnum.ELEC_SF,sector);
                break;
            case 22:
                p = ccdb.getSectorArray(EBCCDBEnum.PHOT_SF,sector);
                break;
            default:
                throw new RuntimeException("Unknown sampling fraction for pid="+pid);
        }
        return p[0]*(p[1] + p[2]/measuredEnergy + p[3]*pow(measuredEnergy,-2));
    }
    
    /**
     * @return sigma of sampling fraction
     */
    public static double getSigma(
            final int pid,
            final DetectorParticle part,
            final EBCCDBConstants ccdb) {
        final int sector = part.getSector(detType);
        final double measuredEnergy = part.getEnergy(detType);
        Double[] p;
        switch (pid) {
            case -11:
                p = ccdb.getSectorArray(EBCCDBEnum.ELEC_SFS,sector);
                break;
            case 11:
                p = ccdb.getSectorArray(EBCCDBEnum.ELEC_SFS,sector);
                break;
            case 22:
                p = ccdb.getSectorArray(EBCCDBEnum.PHOT_SFS,sector);
                break;
            default:
                throw new RuntimeException("Unknown sampling fraction for pid="+pid);
        }
        return p[0]*(p[1] + p[2]/measuredEnergy + p[3]*pow(measuredEnergy,-2));
    }
   
    /**
     * @return number of sigma from mean
     */
    public static double getNSigma(
            final int pid,
            final DetectorParticle part,
            final EBCCDBConstants ccdb) {
        final double samplingFraction = part.getEnergyFraction(detType);
        final double mean  = getMean( pid,part,ccdb);
        final double sigma = getSigma(pid,part,ccdb);
        return (samplingFraction-mean)/sigma;
    }
}

