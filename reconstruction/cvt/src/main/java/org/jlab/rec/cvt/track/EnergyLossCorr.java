package org.jlab.rec.cvt.track;

import org.jlab.clas.swimtools.Swim;
import org.jlab.rec.cvt.svt.Constants;
import org.jlab.rec.cvt.svt.Geometry;
import org.jlab.rec.cvt.trajectory.Helix;

/**
 * Stand alone energy loss correction for the SVT
 *
 * @author ziegler
 *
 */
public class EnergyLossCorr {

    public static final double C = 0.0002997924580;

    public String massHypo = "pion";
    /**
     * Field instantiated using the torus and the solenoid
     */
    
    double cosEntAnglesPlanes[];

    private double[][] Points;
    //private double[][] CorrPoints;
    private Track _updatedTrack;

    public Track get_UpdatedTrack() {
        return _updatedTrack;
    }

    public void set_UpdatedTrack(Track updatedTrack) {
        this._updatedTrack = updatedTrack;
    }

    private Helix OrigTrack;

    /**
     * The constructor
     *
     * @param trkcand the track candidate
     */
    public EnergyLossCorr(Track trkcand, Swim bstSwim) {

        if (trkcand == null) {
            return;
        }

        massHypo = trkcand.get_PID();

        OrigTrack = new Helix(trkcand.get_helix().get_dca(), trkcand.get_helix().get_phi_at_dca(), trkcand.get_helix().get_curvature(),
                trkcand.get_helix().get_Z0(), trkcand.get_helix().get_tandip(), null);

        init(trkcand, bstSwim);
    }
    
    public void doCorrection(Track trkcand) {
        double B = trkcand.get_helix().B;
        double ELossMax = 600; //600Mev 
        double stepSize = 0.001; //1 MeV
        int nbins = (int) ELossMax;

        double pt0 = trkcand.get_Pt() + ELossMax * stepSize;// Assumes the max ELoss is 600 MeV

        double pt = pt0;
        double curv = (Constants.LIGHTVEL * Math.abs(B)) * Math.signum(this.OrigTrack.get_curvature()) / pt;

        for (int j = 0; j < nbins; j++) {
            if (Math.abs(this.OrigTrack.get_curvature()) < Math.abs(curv)) {
                double correctedCurv = (Constants.LIGHTVEL * Math.abs(B)) * Math.signum(this.OrigTrack.get_curvature()) / (pt + stepSize);
                trkcand.get_helix().set_curvature(correctedCurv);
                trkcand.set_HelicalTrack(trkcand.get_helix());
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

        Track trkcandcopy = new Track(trkcand.get_helix());
        trkcandcopy.addAll(trkcand);

        this.set_UpdatedTrack(trkcandcopy);

        for (int m = 0; m < trkcand.size(); m++) {
            Points[m][0] = trkcand.get(m).get_Point().x();
            Points[m][1] = trkcand.get(m).get_Point().y();
            Points[m][2] = trkcand.get(m).get_Point().z();

            double x = trkcand.get_helix().getPointAtRadius(Math.sqrt(Points[m][0] * Points[m][0] + Points[m][1] * Points[m][1])).x();
            double ux = trkcand.get_helix().getTrackDirectionAtRadius(Math.sqrt(Points[m][0] * Points[m][0] + Points[m][1] * Points[m][1])).x();
            double y = trkcand.get_helix().getPointAtRadius(Math.sqrt(Points[m][0] * Points[m][0] + Points[m][1] * Points[m][1])).y();
            double uy = trkcand.get_helix().getTrackDirectionAtRadius(Math.sqrt(Points[m][0] * Points[m][0] + Points[m][1] * Points[m][1])).y();
            double z = trkcand.get_helix().getPointAtRadius(Math.sqrt(Points[m][0] * Points[m][0] + Points[m][1] * Points[m][1])).z();
            double uz = trkcand.get_helix().getTrackDirectionAtRadius(Math.sqrt(Points[m][0] * Points[m][0] + Points[m][1] * Points[m][1])).z();

            double cosEntranceAngle = Math.abs((x * ux + y * uy + z * uz) / Math.sqrt(x * x + y * y + z * z));
            cosEntAnglesPlanes[m] = cosEntranceAngle;

        }

    }

    //? Solve numerically stepping over pt until corr pt matches with fit omega... how much dedx corresponds to obs pt?
    private double doEnergyLossCorrection(int m, double pt, double B) {
        
        double tanL = this.OrigTrack.get_tandip();

        // pz = pt*tanL
        double pz = pt * tanL;
        double p = Math.sqrt(pt * pt + pz * pz);

        double mass = MassHypothesis(massHypo); // assume given mass hypothesis 

        double beta = p / Math.sqrt(p * p + mass * mass); // use particle momentum
        double gamma = 1. / Math.sqrt(1 - beta * beta);

        double cosEntranceAngle = cosEntAnglesPlanes[m];

        double s = eMass / mass;
        //double Wmax = 2.*mass*beta*beta*gamma*gamma/(1.+2.*s*Math.sqrt(1+beta*gamma*beta*gamma)+s*s);
        double Wmax = 2. * mass * beta * beta * gamma * gamma / (1. + 2. * s * gamma + s * s);
        double I = 0.000000172;

        double logterm = 2. * mass * beta * beta * gamma * gamma * Wmax / (I * I);

        double delta = 0.;

        //double dEdx = 0.0001535*(Constants.detMatZ/Constants.detMatA)*(Math.log(logterm)-2*beta*beta-delta)/(beta*beta);
        double dEdx = 0.00001535 * Constants.detMatZ_ov_A_timesThickn * (Math.log(logterm) - 2 * beta * beta - delta) / (beta * beta);

        double tmpPtot = p;

        double tmpEtot = Math.sqrt(MassHypothesis(massHypo) * MassHypothesis(massHypo) + tmpPtot * tmpPtot);
        //double tmpEtotCorrected = tmpEtot-dEdx*Constants.LAYRGAP/cosEntranceAngle;
        double tmpEtotCorrected = tmpEtot - dEdx / cosEntranceAngle;

        double tmpPtotCorrSq = tmpEtotCorrected * tmpEtotCorrected - MassHypothesis(massHypo) * MassHypothesis(massHypo);

        double newPt = Math.sqrt(tmpPtotCorrSq / (1 + tanL * tanL));

        double newCurv = (Constants.LIGHTVEL * Math.abs(B)) * Math.signum(this.OrigTrack.get_curvature()) / newPt;

        return newCurv;

    }

    /**
     *
     * @param H a string corresponding to the mass hypothesis - the pion mass
     * hypothesis is the default value
     * @return the mass value for the given mass hypothesis in GeV/c^2
     */
    public double MassHypothesis(String H) {
        double value = piMass; //default
        if (H.equals("proton")) {
            value = pMass;
        }
        if (H.equals("electron")) {
            value = eMass;
        }
        if (H.equals("pion")) {
            value = piMass;
        }
        if (H.equals("kaon")) {
            value = KMass;
        }
        if (H.equals("muon")) {
            value = muMass;
        }
        return value;
    }

    static double piMass = 0.13957018;
    static double KMass = 0.493677;
    static double muMass = 0.105658369;
    static double eMass = 0.000510998;
    static double pMass = 0.938272029;

}
