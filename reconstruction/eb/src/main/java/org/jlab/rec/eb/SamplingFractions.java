package org.jlab.rec.eb;

import org.jlab.clas.detector.DetectorParticle;
import org.jlab.detector.base.DetectorType;

/**
 * Utility class for calorimeter sampling fractions.
 * @author baltzell
 */

public class SamplingFractions {

    public static final DetectorType DET_TYPE = DetectorType.ECAL;

    public static double calc(double measuredEnergy, Double[] par) {
        return par[0]*(par[1] + par[2]/measuredEnergy + par[3]*Math.pow(measuredEnergy,-2));
	}

    /**
     * @param pid
     * @param part
     * @param ccdb
     * @return mean of sampling fraction
     */
    public static double getMean(
            final int pid,
            final DetectorParticle part,
            final EBCCDBConstants ccdb) {
        final int sector = part.getSector(DET_TYPE);
        final double measuredEnergy = part.getEnergy(DET_TYPE);
        Double[] p;
        switch (Math.abs(pid)) {
            case 11:
                p = ccdb.getSectorArray(EBCCDBEnum.ELEC_SF,sector);
                break;
            case 22:
                p = ccdb.getSectorArray(EBCCDBEnum.PHOT_SF,sector);
                break;
            default:
                throw new RuntimeException("Unknown sampling fraction for pid="+pid);
        }
        return SamplingFractions.calc(measuredEnergy, p);
    }
    
    /**
     * @param pid
     * @param part
     * @param ccdb
     * @return sigma of sampling fraction
     */
    public static double getSigma(
            final int pid,
            final DetectorParticle part,
            final EBCCDBConstants ccdb) {
        final int sector = part.getSector(DET_TYPE);
        final double measuredEnergy = part.getEnergy(DET_TYPE);
        Double[] p;
        switch (Math.abs(pid)) {
            case 11:
                p = ccdb.getSectorArray(EBCCDBEnum.ELEC_SFS,sector);
                break;
            case 22:
                p = ccdb.getSectorArray(EBCCDBEnum.PHOT_SFS,sector);
                break;
            default:
                throw new RuntimeException("Unknown sampling fraction for pid="+pid);
        }
        return SamplingFractions.calc(measuredEnergy, p);
    }
   
    /**
     * @param pid
     * @param part
     * @param ccdb
     * @return number of sigma from mean
     */
    public static double getNSigma(
            final int pid,
            final DetectorParticle part,
            final EBCCDBConstants ccdb) {
        final double samplingFraction = part.getEnergyFraction(DET_TYPE);
        final double mean  = getMean( pid,part,ccdb);
        final double sigma = getSigma(pid,part,ccdb);
        return (samplingFraction-mean)/sigma;
    }

}

