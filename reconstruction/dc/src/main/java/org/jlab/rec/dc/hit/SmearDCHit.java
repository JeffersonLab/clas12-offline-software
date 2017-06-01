package org.jlab.rec.dc.hit;

import java.util.Random;

/**
 * A class to smear a Monte Carlo DC hit doca and time according to a function
 * derived from CLAS 6 data
 *
 * @author ziegler
 *
 */
public class SmearDCHit {

    private Random rn;

    public SmearDCHit() {
        rn = new Random();
    }

    public double smearedDocaSigma(double d, int superlayer) {

        int region = (int) ((superlayer + 1) / 2);

        double[] reg1Pars = {671.232,
            -8432.06,
            68630.7,
            -290897,
            657285,
            -753106,
            349788};
        double[] reg2Pars = {716.247,
            -5681.78,
            26387.2,
            -63076.8,
            80902.5,
            -52713.9,
            13724.1};
        double[] reg3Pars = {620.714,
            -1833.35,
            6018.07,
            -13483.4,
            16142.7,
            -9304.73,
            2052.39};

        double dSmear = 0;
        double[] polynPars = new double[7];
        if (region == 1) {
            polynPars = reg1Pars;
        }
        if (region == 2) {
            polynPars = reg2Pars;
        }
        if (region == 3) {
            polynPars = reg3Pars;
        }
        dSmear = polynPars[0] + polynPars[1] * d + polynPars[2] * d * d + polynPars[3] * d * d * d + polynPars[4] * d * d * d * d + polynPars[5] * d * d * d * d * d + polynPars[6] * d * d * d * d * d * d;

        return dSmear / 10000.;

    }

    public double smearedDoca(double d, int superlayer) {

        double sigma = this.smearedDocaSigma(d, superlayer);

        double smearing = sigma * rn.nextGaussian();

        if ((d + smearing) < 0) {
            smearing *= -1;
        }

        double smearedDoca = d + smearing;

        return smearedDoca;

    }

    public double smearedTime(double invTimeToDist, double d, int superlayer) {

        return smearedDoca(d, superlayer) / invTimeToDist;
    }

    public double smearedTimeSigma(double invTimeToDist, double d, int superlayer) {

        return smearedDocaSigma(d, superlayer) / invTimeToDist;
    }
}
