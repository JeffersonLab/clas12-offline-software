package org.jlab.rec.eb;

import org.jlab.clas.detector.DetectorParticle;
import org.jlab.detector.base.DetectorLayer;
import org.jlab.detector.base.DetectorType;

/**
 *
 * @author baltzell
 */
public class SamplingFractionsPoly {

    public static final DetectorType DET_TYPE = DetectorType.ECAL;

	public enum Type {
		TOTAL,
		PARTIAL;
	}
	
    public static double calc(double var, Double[] par) {
        double ret = 0;
        for (int i=0; i<par.length; ++i) {
            ret += par[i] * Math.pow(var,i);
        }
        return ret;
    }

    /**
     * @param pid
     * @param part
     * @param ccdb
	 * @param type
     * @return mean of sampling fraction
     */
    public static double getMean(
            final int pid,
            final DetectorParticle part,
            final EBCCDBConstants ccdb,
			Type type) {
        final int sector = part.getSector(DET_TYPE);
		if (Math.abs(pid) != 11) {
			throw new RuntimeException("Unknown sampling fraction for pid="+pid);
		}
		Double[] p;
		switch (type) {
			case TOTAL:
				p = ccdb.getSectorArray(EBCCDBEnum.ELEC_SF_POLY,sector);
				break;
			case PARTIAL:
				p = ccdb.getSectorArray(EBCCDBEnum.ELEC_SUBSF_POLY,sector);
				break;
			default:
				throw new RuntimeException("Unknown sampling fraction for type="+type);
		}
		return calc(part.vector().mag(), p);
    }

    /**
     * @param pid
     * @param part
     * @param ccdb
	 * @param type
     * @return sigma of sampling fraction
     */
    public static double getSigma(
            final int pid,
            final DetectorParticle part,
            final EBCCDBConstants ccdb,
			final Type type) {
        final int sector = part.getSector(DET_TYPE);
		if (Math.abs(pid) != 11) {
			throw new RuntimeException("Unknown sampling fraction for pid="+pid);
		}
        Double[] p;
		switch (type) {
			case TOTAL:
				p = ccdb.getSectorArray(EBCCDBEnum.ELEC_SFS_POLY,sector);
				break;
			case PARTIAL:
				p = ccdb.getSectorArray(EBCCDBEnum.ELEC_SUBSFS_POLY,sector);
				break;
			default:
				throw new RuntimeException("Unknown sampling fraction for type="+type);
		}
        return calc(part.vector().mag(), p);
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
            final EBCCDBConstants ccdb,
			final Type type) {
		int[] layers;
		switch (type) {
			case TOTAL:
				layers = new int[]{DetectorLayer.PCAL,DetectorLayer.EC_INNER,DetectorLayer.EC_OUTER};
				break;
			case PARTIAL:
				layers = new int[]{DetectorLayer.PCAL,DetectorLayer.EC_INNER};
				break;
			default:
				throw new RuntimeException("Unknown sampling fraction for type="+type);
		};
		final double samplingFraction = part.getEnergyFraction(DET_TYPE, layers);
        final double mean  = getMean( pid,part,ccdb,type);
        final double sigma = getSigma(pid,part,ccdb,type);
        return (samplingFraction-mean)/sigma;
    }
    
	/**
     * @param pid
     * @param part
     * @param ccdb
     * @return 
     */
    public static double getNSigma(
            final int pid,
            final DetectorParticle part,
            final EBCCDBConstants ccdb) {
		double ns1 = getNSigma(pid,part,ccdb,Type.TOTAL);
		double ns2 = getNSigma(pid,part,ccdb,Type.PARTIAL);
		return Math.sqrt(ns1*ns1+ns2*ns2);
    }
}
