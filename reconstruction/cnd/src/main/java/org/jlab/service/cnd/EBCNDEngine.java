package org.jlab.service.cnd;

import java.lang.String;
import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import java.io.IOException;
import java.util.ArrayList;

//import org.jlab.service.cnd.CNDEngine*;
//import org.jlab.rec.cnd.hit*;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.clas.physics.LorentzVector;


/*
 *
 * get momentum inforamtion of neutron from CND
 *
 * 
 * @author wangrong (R. Wang)
 */

public class EBCNDEngine extends ReconstructionEngine {

    ArrayList<Integer> neutron_id;
    ArrayList<Double> neutron_px;
    ArrayList<Double> neutron_py;
    ArrayList<Double> neutron_pz;

    ArrayList<Double> RECNeutron_px;
    ArrayList<Double> RECNeutron_py;
    ArrayList<Double> RECNeutron_pz;
    ArrayList<Double> RECNeutron_E;

    ArrayList<Double> trk_at_cnd_x;
    ArrayList<Double> trk_at_cnd_y;
    ArrayList<Double> trk_at_cnd_z;

    int size = 0;

    double p_mass = 0.938272;
    double n_mass = 0.93957;
    double light_speed = 29.9792458;

    double tstart = 124.25;
    int clustering_finish_flag = 0;
    int cluster_subA = 0;
    int cluster_subB = 0;
    double clustering_closest_distance = 0;

    double vertex_x = 0;
    double vertex_y = 0;
    double vertex_z = 0;

    ArrayList<Double> cnd_hits_theta;
    ArrayList<Double> cnd_hits_phi;
    ArrayList<Double> cnd_hits_beta;

    LorentzVector pp; 


    //// unit : deg.
    private double sigmaTheta(double theta){ return 2.5; }
    //// unit : deg.
    private double sigmaPhi(double phi){ return 2.8; }
    //// beta = v/c.
    private double sigmaBeta(double beta){ return 0.065*beta; }

    public EBCNDEngine(){
        super("EBCND","wangrong","1.0");
        cnd_hits_theta = new ArrayList<Double>();
        cnd_hits_phi = new ArrayList<Double>();
        cnd_hits_beta = new ArrayList<Double>();
        neutron_id = new ArrayList<Integer>();
        neutron_px = new ArrayList<Double>();
        neutron_py = new ArrayList<Double>();
        neutron_pz = new ArrayList<Double>();
        RECNeutron_px = new ArrayList<Double>();
        RECNeutron_py = new ArrayList<Double>();
        RECNeutron_pz = new ArrayList<Double>();
        RECNeutron_E = new ArrayList<Double>();
	trk_at_cnd_x = new ArrayList<Double>(); 
	trk_at_cnd_y = new ArrayList<Double>(); 
	trk_at_cnd_z = new ArrayList<Double>(); 
        pp = new LorentzVector();
        initBankNames();
    }

    public void initBankNames() {
        //Initialize bank names
    }


    private void find_closest(int begin, ArrayList<Double> theta, ArrayList<Double> phi, ArrayList<Double> beta){
    	if((begin+1)>=theta.size())return;
        for(int i=begin+1;i<theta.size();i++){
      		double distance = sqrt((theta.get(begin)-theta.get(i))*(theta.get(begin)-theta.get(i))/sigmaTheta(theta.get(begin))/sigmaTheta(theta.get(i))
                     +(phi.get(begin)-phi.get(i))*(phi.get(begin)-phi.get(i))/sigmaPhi(phi.get(begin))/sigmaPhi(phi.get(i))
                     +(beta.get(begin)-beta.get(i))*(beta.get(begin)-beta.get(i))/sigmaBeta(beta.get(begin))/sigmaBeta(beta.get(i)));
      		if(distance>5)continue;
      		else{
         		if(distance<clustering_closest_distance){
            			cluster_subA = begin;
            			cluster_subB = i;
         		}
      		}
   	}
        find_closest(begin+1, theta, phi, beta);
    }


    @Override
    public boolean processDataEvent(DataEvent event) {

    	int veto_flag;

        /// reset the arrayLists for the a new event
	neutron_id.clear();
	neutron_px.clear();
	neutron_py.clear();
	neutron_pz.clear();
	RECNeutron_px.clear();
	RECNeutron_py.clear();
	RECNeutron_pz.clear();
	RECNeutron_E.clear();
	cnd_hits_theta.clear();
	cnd_hits_phi.clear();
	cnd_hits_beta.clear();
        trk_at_cnd_x.clear();
        trk_at_cnd_y.clear();
        trk_at_cnd_z.clear();


        /// get the start time of the event form RECHB_Event
        if(event.hasBank("RECHB::Event") == true) {
		DataBank bank = event.getBank("RECHB::Event");
		tstart = bank.getFloat("STTime", 0);
        }
        else return false;

        /// get the tracks of charged particles at cnd from CND_ bank
        if(event.hasBank("CND::hits") == true) {
            DataBank bank = event.getBank("CND::hits");
            size = bank.rows();   // number of hits in the CND event
            for (int i = 0; i < size; i++) {
                double cnd_hits_tx = bank.getFloat("tx", i);
                double cnd_hits_ty = bank.getFloat("ty", i);
                double cnd_hits_tz = bank.getFloat("tz", i);
                if((cnd_hits_tx*cnd_hits_tx+cnd_hits_ty*cnd_hits_ty)>400){
                        trk_at_cnd_x.add(cnd_hits_tx);
                        trk_at_cnd_y.add(cnd_hits_ty);
                        trk_at_cnd_z.add(cnd_hits_tz);
                }
            }

        }
        else return false;
    	veto_flag = 0;

        /// get the cnd hits information from CND_ bank
	if(event.hasBank("CND::hits") == true) {

            DataBank bank = event.getBank("CND::hits");

	    double beta_temp;
            size = bank.rows();   // number of hits in the CND event
            for (int i = 0; i < size; i++) {
                double cnd_hits_energy = bank.getFloat("energy", i);
                double cnd_hits_x = bank.getFloat("x", i);
                double cnd_hits_y = bank.getFloat("y", i);
                double cnd_hits_z = bank.getFloat("z", i);
                double cnd_hits_time = bank.getFloat("time", i);
		if(cnd_hits_energy>3){
			if((cnd_hits_time-tstart)>8)continue;
			pp.setPxPyPzE(cnd_hits_x, cnd_hits_y, cnd_hits_z, 0);
			beta_temp = pp.p()/(cnd_hits_time-tstart)/light_speed;
			if(beta_temp>0.8)continue;

                        veto_flag = 0;
                        for(int k=0; k<trk_at_cnd_x.size(); k++)
                        if(  abs(cnd_hits_x-trk_at_cnd_x.get(k))<3.45
                                && abs(cnd_hits_y-trk_at_cnd_y.get(k))<3.45
                                && abs(cnd_hits_z-trk_at_cnd_z.get(k))<5.52 )veto_flag = 1;
                        if(veto_flag==1)continue; 

                        cnd_hits_theta.add(pp.theta()*180/3.14159);
                        cnd_hits_phi.add(pp.phi()*180/3.14159);
                        cnd_hits_beta.add(beta_temp);
		}

            }
	}
        else return false;


      	///// building neutron from good cnd_hits
      	//the case for only one good cnd hit
     	if(cnd_hits_theta.size()==1){
        	double Pn = cnd_hits_beta.get(0)*n_mass/sqrt(1-cnd_hits_beta.get(0)*cnd_hits_beta.get(0));
         	RECNeutron_px.add(Pn*sin(cnd_hits_theta.get(0)*3.14159/180.0)*cos(cnd_hits_phi.get(0)*3.14159/180.0));
         	RECNeutron_py.add(Pn*sin(cnd_hits_theta.get(0)*3.14159/180.0)*sin(cnd_hits_phi.get(0)*3.14159/180.0));
         	RECNeutron_pz.add(Pn*cos(cnd_hits_theta.get(0)*3.14159/180.0));
         	RECNeutron_E.add( sqrt(n_mass*n_mass+Pn*Pn ) );
      	}
      	///the case for two good cnd hits
      	else if(cnd_hits_theta.size()==2){
         	double distance = sqrt((cnd_hits_theta.get(0)-cnd_hits_theta.get(1))*(cnd_hits_theta.get(0)-cnd_hits_theta.get(1))/sigmaTheta(cnd_hits_theta.get(0))/sigmaTheta(cnd_hits_theta.get(1))
                           +(cnd_hits_phi.get(0)-cnd_hits_phi.get(1))*(cnd_hits_phi.get(0)-cnd_hits_phi.get(1))/sigmaPhi(cnd_hits_phi.get(0))/sigmaPhi(cnd_hits_phi.get(1))
                           +(cnd_hits_beta.get(0)-cnd_hits_beta.get(1))*(cnd_hits_beta.get(0)-cnd_hits_beta.get(1))/sigmaBeta(cnd_hits_beta.get(0))/sigmaBeta(cnd_hits_beta.get(1)));
         	if(distance<5){
            		double beta_ave = (cnd_hits_beta.get(0)+cnd_hits_beta.get(1))/2.0;
            		double theta_ave = (cnd_hits_theta.get(0)+cnd_hits_theta.get(1))/2.0*3.14159/180.0;
            		double phi_ave = (cnd_hits_phi.get(0)+cnd_hits_phi.get(1))/2.0*3.14159/180.0;
            		double Pn = beta_ave*n_mass/sqrt(1-beta_ave*beta_ave);
            		RECNeutron_px.add(Pn*sin(theta_ave)*cos(phi_ave));
            		RECNeutron_py.add(Pn*sin(theta_ave)*sin(phi_ave));
            		RECNeutron_pz.add(Pn*cos(theta_ave));
           	 	RECNeutron_E.add( sqrt(n_mass*n_mass+Pn*Pn ) );
         	}
         	else {
            		for(int j=0;j<cnd_hits_beta.size();j++){
               			double Pn = cnd_hits_beta.get(j)*n_mass/sqrt(1-cnd_hits_beta.get(j)*cnd_hits_beta.get(j));
               			RECNeutron_px.add(Pn*sin(cnd_hits_theta.get(j)*3.14159/180.0)*cos(cnd_hits_phi.get(j)*3.14159/180.0));
               			RECNeutron_py.add(Pn*sin(cnd_hits_theta.get(j)*3.14159/180.0)*sin(cnd_hits_phi.get(j)*3.14159/180.0));
               			RECNeutron_pz.add(Pn*cos(cnd_hits_theta.get(j)*3.14159/180.0));
               			RECNeutron_E.add( sqrt(n_mass*n_mass+Pn*Pn ) );
            		}
         	}
      	}
      	//// more than two cnd hits
      	//// hierarchiral clustering
      	else if(cnd_hits_theta.size()>2){
         	int cnd_hits_number_good = cnd_hits_theta.size();
         	while(true){
            		clustering_closest_distance = 1e15;
            		cluster_subA = -1;
            		cluster_subB = -1;
            		find_closest(0, cnd_hits_theta, cnd_hits_phi, cnd_hits_beta);
            		if(cluster_subA==-1||cluster_subB==-1)break;
            		else{
               			int cnd_hits_number_good_now = cnd_hits_theta.size();
               			cnd_hits_theta.set(cluster_subA, (cnd_hits_theta.get(cluster_subA)*(1+cnd_hits_number_good-cnd_hits_number_good_now)+cnd_hits_theta.get(cluster_subB))
                                               /(2.0+cnd_hits_number_good-cnd_hits_number_good_now) );
               			cnd_hits_phi.set(cluster_subA, (cnd_hits_phi.get(cluster_subA)*(1+cnd_hits_number_good-cnd_hits_number_good_now)+cnd_hits_phi.get(cluster_subB))
                                               /(2.0+cnd_hits_number_good-cnd_hits_number_good_now) );
               			cnd_hits_beta.set(cluster_subA, (cnd_hits_beta.get(cluster_subA)*(1+cnd_hits_number_good-cnd_hits_number_good_now)+cnd_hits_beta.get(cluster_subB))
                                               /(2.0+cnd_hits_number_good-cnd_hits_number_good_now) );
              			cnd_hits_theta.remove(cluster_subB);
              			cnd_hits_phi.remove(cluster_subB);
              			cnd_hits_beta.remove(cluster_subB);
            		}
         	}
         	for(int j=0;j<cnd_hits_beta.size();j++){
            		double Pn = cnd_hits_beta.get(j)*n_mass/sqrt(1-cnd_hits_beta.get(j)*cnd_hits_beta.get(j));
            		RECNeutron_px.add(Pn*sin(cnd_hits_theta.get(j)*3.14159/180.0)*cos(cnd_hits_phi.get(j)*3.14159/180.0));
            		RECNeutron_py.add(Pn*sin(cnd_hits_theta.get(j)*3.14159/180.0)*sin(cnd_hits_phi.get(j)*3.14159/180.0));
            		RECNeutron_pz.add(Pn*cos(cnd_hits_theta.get(j)*3.14159/180.0));
            		RECNeutron_E.add( sqrt(n_mass*n_mass+Pn*Pn ) );
         	}
      	}


	/// Filling the banks
	size = RECNeutron_px.size();
	if(size>0){
		DataBank bank2 =  event.createBank("EBCND::Neutron", size);
		if (bank2 == null) {
			System.err.println("COULD NOT CREATE A EBCND::Neutron BANK!!!!!!");
			return false;
		}
		for(int i =0; i< size; i++) {
			bank2.setInt("id",i, (i+1) );
			bank2.setFloat("time",i,  (float)0.0);
			bank2.setFloat("x",i,  (float)0.0);
			bank2.setFloat("y",i,  (float)0.0);
			bank2.setFloat("z",i,  (float)0.0);
			bank2.setFloat("px",i,  (float)(1.0*RECNeutron_px.get(i)) );
			bank2.setFloat("py",i,  (float)(1.0*RECNeutron_py.get(i)) );
			bank2.setFloat("pz",i,  (float)(1.0*RECNeutron_pz.get(i)) );
		}
		event.appendBanks(bank2);
	}


	return true;
    }


    @Override
    public boolean init() {
 	// TODO Auto-generated method stub
	System.out.println("EBCNDEngine in init ");
	return true;
    }


}
