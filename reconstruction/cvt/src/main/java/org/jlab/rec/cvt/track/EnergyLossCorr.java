package org.jlab.rec.cvt.track;

import org.jlab.clas.pdg.PhysicsConstants;
import org.jlab.clas.swimtools.Swim;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.svt.SVTParameters;
import org.jlab.rec.cvt.trajectory.Helix;

/**
 * Stand alone energy loss correction for the SVT
 *
 * @author ziegler
 *
 */
public class EnergyLossCorr {

    public static final double C = 0.0002997924580;

    public int massHypo = 211;
    private double cosEntAnglesPlanes[];
    private double[][] Points;
    private Track _updatedTrack;
    private Helix OrigTrack;

   
    private static final double MUMASS = 0.105658369;
    
    /**
     * The constructor
     *
     * @param trkcand the track candidate
     * @param bstSwim
     */
    public EnergyLossCorr(Track trkcand, Swim bstSwim) {

        if (trkcand == null) {
            return;
        }

        massHypo = trkcand.getPID();

        OrigTrack = new Helix(trkcand.getHelix().getDCA(), trkcand.getHelix().getPhiAtDCA(), trkcand.getHelix().getCurvature(),
                trkcand.getHelix().getZ0(), trkcand.getHelix().getTanDip(), trkcand.getHelix().getXb(), trkcand.getHelix().getYb(), null);

        init(trkcand, bstSwim);
    }
    
    public void doCorrection(Track trkcand) {
        double B = trkcand.getHelix().B;
        double ELossMax = 600; //600Mev 
        double stepSize = 0.001; //1 MeV
        int nbins = (int) ELossMax;

        double pt0 = trkcand.getPt() + ELossMax * stepSize;// Assumes the max ELoss is 600 MeV

        double pt = pt0;
        double curv = (Constants.LIGHTVEL * Math.abs(B)) * Math.signum(this.OrigTrack.getCurvature()) / pt;

        for (int j = 0; j < nbins; j++) {
            if (Math.abs(this.OrigTrack.getCurvature()) < Math.abs(curv)) {
                double correctedCurv = (Constants.LIGHTVEL * Math.abs(B)) * Math.signum(this.OrigTrack.getCurvature()) / (pt + stepSize);
                trkcand.getHelix().setCurvature(correctedCurv);
                trkcand.setPXYZ();
                return;
            }
            pt = pt0 - j * stepSize;

            double aveCurv = 0;
            for (int k = 0; k < trkcand.size(); k++) {
                aveCurv += doEnergyLossCorrection(k, pt, B);
            }
            aveCurv /= trkcand.size();
            curv = aveCurv;

        }

    }

    private void init(Track trkcand, Swim bstSwim) {

        Points = new double[trkcand.size()][3];
        //CorrPoints 	= new double[trkcand.size()][3] ;

        cosEntAnglesPlanes = new double[trkcand.size()];

        Track trkcandcopy = new Track(trkcand.getHelix());
        trkcandcopy.addAll(trkcand);

        this.setUpdatedTrack(trkcandcopy);

        for (int m = 0; m < trkcand.size(); m++) {
            Points[m][0] = trkcand.get(m).getPoint().x();
            Points[m][1] = trkcand.get(m).getPoint().y();
            Points[m][2] = trkcand.get(m).getPoint().z();

            double x = trkcand.getHelix().getPointAtRadius(Math.sqrt(Points[m][0] * Points[m][0] + Points[m][1] * Points[m][1])).x();
            double ux = trkcand.getHelix().getTrackDirectionAtRadius(Math.sqrt(Points[m][0] * Points[m][0] + Points[m][1] * Points[m][1])).x();
            double y = trkcand.getHelix().getPointAtRadius(Math.sqrt(Points[m][0] * Points[m][0] + Points[m][1] * Points[m][1])).y();
            double uy = trkcand.getHelix().getTrackDirectionAtRadius(Math.sqrt(Points[m][0] * Points[m][0] + Points[m][1] * Points[m][1])).y();
            double z = trkcand.getHelix().getPointAtRadius(Math.sqrt(Points[m][0] * Points[m][0] + Points[m][1] * Points[m][1])).z();
            double uz = trkcand.getHelix().getTrackDirectionAtRadius(Math.sqrt(Points[m][0] * Points[m][0] + Points[m][1] * Points[m][1])).z();

            double cosEntranceAngle = Math.abs((x * ux + y * uy + z * uz) / Math.sqrt(x * x + y * y + z * z));
            cosEntAnglesPlanes[m] = cosEntranceAngle;

        }

    }

    //? Solve numerically stepping over pt until corr pt matches with fit omega... how much dedx corresponds to obs pt?
    private double doEnergyLossCorrection(int m, double pt, double B) {
        
        double tanL = this.OrigTrack.getTanDip();

        // pz = pt*tanL
        double pz = pt * tanL;
        double p = Math.sqrt(pt * pt + pz * pz);

        double mass = massHypothesis(massHypo); // assume given mass hypothesis 

        double beta = p / Math.sqrt(p * p + mass * mass); // use particle momentum
        double gamma = 1. / Math.sqrt(1 - beta * beta);

        double cosEntranceAngle = cosEntAnglesPlanes[m];

        double s = PhysicsConstants.massElectron() / mass;
        //double Wmax = 2.*mass*beta*beta*gamma*gamma/(1.+2.*s*Math.sqrt(1+beta*gamma*beta*gamma)+s*s);
        double Wmax = 2. * mass * beta * beta * gamma * gamma / (1. + 2. * s * gamma + s * s);
        double I = 0.000000172;

        double logterm = 2. * mass * beta * beta * gamma * gamma * Wmax / (I * I);

        double delta = 0.;

        //double dEdx = 0.0001535*(Constants.detMatZ/Constants.detMatA)*(Math.log(logterm)-2*beta*beta-delta)/(beta*beta);
        double dEdx = 0.00001535 * SVTParameters.detMatZ_ov_A_timesThickn * (Math.log(logterm) - 2 * beta * beta - delta) / (beta * beta);

        double tmpPtot = p;

        double tmpEtot = Math.sqrt(massHypothesis(massHypo) * massHypothesis(massHypo) + tmpPtot * tmpPtot);
        //double tmpEtotCorrected = tmpEtot-dEdx*Constants.LAYRGAP/cosEntranceAngle;
        double tmpEtotCorrected = tmpEtot - dEdx / cosEntranceAngle;

        double tmpPtotCorrSq = tmpEtotCorrected * tmpEtotCorrected - massHypothesis(massHypo) * massHypothesis(massHypo);

        double newPt = Math.sqrt(tmpPtotCorrSq / (1 + tanL * tanL));

        double newCurv = (Constants.LIGHTVEL * Math.abs(B)) * Math.signum(this.OrigTrack.getCurvature()) / newPt;

        return newCurv;

    }

    public Track getUpdatedTrack() {
        return _updatedTrack;
    }

    public void setUpdatedTrack(Track updatedTrack) {
        this._updatedTrack = updatedTrack;
    }

    /**
     *
     * @param H a string corresponding to the mass hypothesis - the pion mass
     * hypothesis is the default value
     * @return the mass value for the given mass hypothesis in GeV/c^2
     */
    public double massHypothesis(int H) {
        double value = PhysicsConstants.massPionCharged(); //default
        if (H == 2212) {
            value = PhysicsConstants.massProton();
        }
        else if (H == 11) {
            value = PhysicsConstants.massElectron();
        }
        else if (H == 321) {
            value = PhysicsConstants.massKaonCharged();
        }
        else if (H == 13) {
            value = MUMASS;
        }
        return value;
    }

}
