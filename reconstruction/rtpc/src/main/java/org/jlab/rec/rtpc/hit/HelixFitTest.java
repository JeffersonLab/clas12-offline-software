/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.rtpc.hit;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import org.jlab.groot.data.*;
import org.jlab.groot.ui.*;
import org.jlab.groot.graphics.*;

/**
 *
 * @author davidpayette
 */
public class HelixFitTest {
    public HelixFitTest(List<Hit> rawHits, HitParameters params){
        HashMap<Integer, List<RecoHitVector>> recotrackmap = params.get_recotrackmap();
        
        
        double szpos[][] = new double[1000][3];
        int hit = 0;
        for(int TID : recotrackmap.keySet()){
            for(hit = 0; hit < recotrackmap.get(TID).size(); hit++){
                szpos[hit][0] = recotrackmap.get(TID).get(hit).x();
                szpos[hit][1] = recotrackmap.get(TID).get(hit).y();
                szpos[hit][2] = recotrackmap.get(TID).get(hit).z();
            }
            HelixFitJava h = new HelixFitJava();
            HelixFitObject ho = h.HelixFit(hit,szpos,0,0,0,0,0,0,1);
            double momfit =  0.3*50*ho.get_Rho()/10;
            System.out.println(momfit);
        }

        
        




        /*
	int entry = -1;
	int hitnum = 0;
	int tid = -1;
	int prevtid = 0;
	int num_chains = 0;
	int hh_num_hits = 0;
	int[] num_hits_this_chain = new int[300];
	int[][] chain_hits = new int[300][300];
	double[] tempx = new double[30];
	double[] tempy = new double[30];
	double[] tempz = new double[30];
	double[] tempr = new double[30];
	double[] num_hits = new double[5000];
	double[] mom_all = new double[5000];
	double[] z_all = new double[5000];
	double[] theta_all = new double[5000];
	int itemp = 0;
	int n_RTPC__rec_TID = 0;
	double R; double A; double B;
        double Phi_deg; double Theta_deg; double Z0; int fit_track_to_beamline=1;
	//HashMap <int,HashMap<int,double[][]>> hitHashMap;
	boolean flag = true; 
	boolean finish_track_flag = false;
	boolean trackidchange = false;
	boolean split_curve = false;
	boolean draw = false;
	double momx; double momy; double momz; double mom;
	TCanvas  c1 = new TCanvas("c1", 800, 800);
	H1F h1 = new H1F("h1", 100, 0,500);
       	
	//TCanvas  c2 = new TCanvas("c2", "Momentum Reconstructed Track (GeV)", 800, 800);
      	H1F h2 = new H1F("h2", "Momentum Reconstructed Track (MeV)", 75, 0, 500);
	H1F h3 = new H1F("h3", "Momentum Reconstructed Track (MeV)", 100, 0, 500);

	H2F hGenRecvsGenTrue = new H2F("hGenRecvsGenTrue","hGenRecvsGenTrue",50,0.,500.,50,0.,500.);
	hGenRecvsGenTrue.setTitleX("Momentum (lund)");
	hGenRecvsGenTrue.setTitleY("Momentum (reconstructed lund)");	
	H2F hGenRecvsGenTrue2 = new H2F("hGenRecvsGenTrue2","hGenRecvsGenTrue2",50,0.,500.,50,0.,500.);
	hGenRecvsGenTrue2.setTitleX("Momentum (lund)");
	hGenRecvsGenTrue2.setTitleY("Momentum (reconstructed lund)");
	H2F hRecvsGenTrue = new H2F("hRecvsGenTrue","hRecvsGenTrue",50,0.,500.,50,0.,500.);
	hRecvsGenTrue.setTitleX("Momentum (lund)");
	hRecvsGenTrue.setTitleY("Momentum (rec)");
	H2F hRecvsGenRec = new H2F("hRecvsGenRec","hRecvsGenRec",50,0.,500.,50,0.,500.);
 	hRecvsGenRec.setTitleX("Momentum (reconstructed lund)");
	hRecvsGenRec.setTitleY("Momentum (rec)");
	H2F hHitsvsMom = new H2F("hHitsvsMom","hHitsvsMom",50,0,500,50,0,250);
	hHitsvsMom.setTitleX("Momentum (lund)");
	hHitsvsMom.setTitleY("Number of Hits");
	
	H1F h4 = new H1F("h4", "num gen hits in track", 50, 0, 100);

	H2F hMomvsTheta = new H2F("hMomvsTheta","hMomvsTheta",50,0,180,50,0,500);
	H2F hZvsTheta = new H2F("hZvsTheta","hZvsTheta",50,0,180,50,-210,210);
	hZvsTheta.setTitleX("Theta");
	hZvsTheta.setTitleY("Z vertex (lund)");
	H2F hMomvsThetaCut = new H2F("hMomvsThetaCut","hMomvsThetaCut",50,0,180,50,0,500);
	H2F hZvsThetaCut = new H2F("hZvsThetaCut","hZvsThetaCut",50,0,180,50,-210,210);
	hZvsThetaCut.setTitleX("Theta");
	hZvsThetaCut.setTitleY("Z vertex (lund)");
	H2F hMomvsThetaelse = new H2F("hMomvsThetaelse","hMomvsThetaelse",50,0,180,50,0,500);
	H2F hZvsThetaelse = new H2F("hZvsThetaelse","hZvsThetaelse",50,0,180,50,-210,210);
	hZvsThetaelse.setTitleX("Theta");
	hZvsThetaelse.setTitleY("Z vertex (lund)");
	
	int cellID = 0;
       	double time = 0;
        double z = 0;
       	double x = 0;
       	double y = 0;
       	double r = 0;
       	double phi = 0;
     	double q = 0;
	boolean skipfirst = true;
	boolean skipfirsttrack = true; 
        HashMap<Integer,Double> zvertex_rec = new HashMap<Integer,Double>();
	HashMap<Integer,Double> tshift_rec = new HashMap<Integer,Double>();
	HashMap<Integer,Double> momentum_rec = new HashMap<Integer,Double>();
	//HashMap<int,double> momentum_gen;
	//HashMap<int,double> tshift_gen;
	double zvertex_gen;
	double tshift_gen;
	TCanvas  c6 = new TCanvas("c6", 800, 800);
	//myfile << "Generated Momentum" << "\t" << "Reconstructed Momentum " << "\t" << "Time Shift" << endl; 
	int failed_genrec = 0;
	for(Hit rawhit : rawHits){
	  if(skipfirst){ skipfirst = false; continue;}
	  entry++;
	  //myfile << "Event " << entry << endl;
	  //  if(entry > 1000) break;
       		//entry = -1;
         	hitnum = 0;
        	tid = -1;
        	prevtid = 0;
        	num_chains = 0;
        	hh_num_hits = 0;
        	itemp = 0;
		flag = true; 
        	finish_track_flag = false;
         	trackidchange = false;
	        split_curve = false;
	        draw = false;
		skipfirsttrack = true;
		//std::cout << "event # " << entry << std::endl;
		double thetarad = 0;
		double thetadeg = 0; 
		double vz = 0; 
                    if(rawhit.get_TID() == 2)
		    
                        momx = MC__Lund_px.getValue(c);
                        momy = MC__Lund_py.getValue(c);
                        momz = MC__Lund_pz.getValue(c);
                        vz = MC__Lund_vz.getValue(c);
                        vz*=10;
                        mom  = Math.Sqrt(momx*momx + momy*momy + momz*momz);
                        thetarad = Math.ACos(momz/mom);
                        thetadeg = thetarad*Math.RadToDeg();
		    //std::cout << MC__Lund_vz.getValue(c) << " vz" << endl;
		    //std::cout << momx << " " << momy << " " << momz << " " << mom << endl;
                    }
		}
		h1.Fill(mom*1000);
		//myfile << mom*1000 << "\t";
		/*int n_RTPC__pos_posx = RTPC__pos_posx.getLength();
		double genx[n_RTPC__pos_posx+1];
		double geny[n_RTPC__pos_posx+1];
		double genz[n_RTPC__pos_posx+1];
		for(int d = 0; d <= n_RTPC__pos_posx; d++){
		  genx[d] = RTPC__pos_posx.getValue(d);
		  geny[d] = RTPC__pos_posy.getValue(d);
		  genz[d] = RTPC__pos_posz.getValue(d);
		}
		
		TGraph *genxy = new TGraph(n_RTPC__pos_posx,genx,geny);
		genxy.SetMarkerStyle(31);
		genxy.SetMarkerColor(3);
		*/
		//		n_RTPC__rec_TID = RTPC__rec_TID.getLength();
		/*int n_gen_TID = RTPC__pos_tid.getLength();
		int tid_temp = -1;
		int num_tracks = 0;
		/*for(int i = 0; i < n_RTPC__rec_TID; i++){
		  if(RTPC__rec_TID.getValue(i) != tid_temp){
		    tid_temp = RTPC__rec_TID.getValue(i);
		    num_tracks++;
		  }
		}
		std::cout << "This event has " << num_tracks << " tracks" << endl;
		
		for(int b = 0; b <= n_RTPC__rec_TID; b++){
		  //if(!draw) break;
		  //std::cout << RTPC__rec_TID.getValue(b) << " " << RTPC__rec_time.getValue(b) << std::endl;
			if(b < n_RTPC__rec_TID){
				prevtid = RTPC__rec_TID.getValue(b);
			}
			else{
				prevtid = -111;
			}
			
			trackidchange = false;
			if(tid != prevtid){
			  	finish_track_flag = false;
			  	trackidchange = true; 
			        if(num_hits_this_chain[tid] > 0 && b != 0){
				  /*DEBUG
				  for(int i = 0; i < szpos.size(); i++){
				    std::cout << szpos[i][0] << endl;
				    }*/
	  /*HelixFit(num_hits_this_chain[tid]-1, szpos, R, A, B, Phi_deg, Theta_deg, Z0, fit_track_to_beamline);
					std::cout << "tid : "  << tid << std::endl;
					std::cout << "A : " << A << std::endl;
					std::cout << "B : " << B << std::endl;
					std::cout << "R : " << R << std::endl;
					std::cout << "p : " << 0.3*50*R/10 << std::endl;
					std::cout << "Z : " << Z0 << std::endl;
					std::cout << "Theta " << Theta_deg << std::endl;
					std::cout << "Phi " << Phi_deg << std::endl;
					zvertex_rec.insert(std::make_pair(tid,Z0));
					tshift_rec.insert(std::make_pair(tid,RTPC__rec_tdiff.getValue(b-1)));
					momentum_rec.insert(std::make_pair(tid,0.3*50*R/10));
					if(!skipfirsttrack) myfile << "" << "\t";
					skipfirsttrack = false;
					myfile << 0.3*50*R/10 << "\t" << RTPC__rec_tdiff.getValue(b-1) << endl;

					//if(tid == 1){
					 if(true){
					  //TCanvas *p = new TCanvas("p","p",800,600);
					  //TMultiGraph *mg = new TMultiGraph();
					  //TEllipse *e = new TEllipse(A, B, R, R);
					  //p.drawFrame(-70,-70,70,70);
						//e.draw();
						double xpos[300];
						double ypos[300];
						double zpos[300];
						double rpos[300];
						for(int i = 0; i < num_hits_this_chain[tid]-1; i++){
					  		xpos[i] = szpos[i][0];
					  		ypos[i] = szpos[i][1];
					  		zpos[i] = szpos[i][2];
					  		rpos[i] = Math.Sqrt(xpos[i]*xpos[i] + ypos[i]*ypos[i]);
						}
						//if(fit_track_to_beamline){
					  	xpos[num_hits_this_chain[tid]] = 0;
					  	ypos[num_hits_this_chain[tid]] = 0;
					  	zpos[num_hits_this_chain[tid]] = Z0;
					  	rpos[num_hits_this_chain[tid]] = 0;
					  	num_hits_this_chain[tid]++;
					  	//}
					  	double t0 = (Math.Pi()/2) + Phi_deg*Math.DegToRad();
						double tmin = t0; 
						double tmax = t0 - Math.TwoPi();
						int numpoints = 2000;
						double step = (tmax - tmin)/(numpoints-1);
						double xarrhelix[numpoints];
						double yarrhelix[numpoints];
						double zarrhelix[numpoints];
						double xarrinner[numpoints];
						double yarrinner[numpoints];
						double xarrouter[numpoints];
						double yarrouter[numpoints];
					
						int i2 = 0;
						double partxhelix[numpoints];
						double partyhelix[numpoints];
						double partzhelix[numpoints];
						double partrhelix[numpoints];
						//TNtuple *helixn = new TNtuple("helixn","helixn","x:y:z");
						for(int i = 0; i < numpoints;i++){
					 		if(i*step >= -Math.Pi()){
					    			xarrhelix[i] = A+xcirc(tmin+i*step,R,0);
					    			yarrhelix[i] = B+ycirc(tmin+i*step,R,0);
					    			zarrhelix[i] = Z0-zcirc(tmin+i*step-t0,R,Theta_deg);
					    			//zarrhelix[i] = Z0;
					    			//helixn.Fill(xarrhelix[i],yarrhelix[i],zarrhelix[i]);
					    			if(Math.Abs(xarrhelix[i])<70 && Math.Abs(yarrhelix[i])<70){
					      				partxhelix[i2] = xarrhelix[i];
					      				partyhelix[i2] = yarrhelix[i];
					      				partzhelix[i2] = zarrhelix[i];
					      				partrhelix[i2] = Math.Sqrt(xarrhelix[i] * xarrhelix[i] + yarrhelix[i] * yarrhelix[i]);
					      				i2++;
								}
					  		}
					  		xarrinner[i] = xcirc(tmin + i*step,30,0);
					  		yarrinner[i] = ycirc(tmin + i*step,30,0);
					  		xarrouter[i] = xcirc(tmin + i*step,70,0);
					  		yarrouter[i] = ycirc(tmin + i*step,70,0);
					  
						}
						//TGraph *circgr = new TGraph(numpoints,partxhelix,partyhelix);
						//circgr.SetMarkerColor(2);
						//circgr.GetXaxis().SetLimits(-70,70);
				        	//circgr.GetYaxis().SetRangeUser(-70,70);

						//mg.Add(circgr);

						//TGraph *innergr = new TGraph(numpoints,xarrinner,yarrinner);
						//mg.Add(innergr);
						//TGraph *outergr = new TGraph(numpoints,xarrouter,yarrouter);
						//mg.Add(outergr);
					
						//TGraph *gr = new TGraph(num_hits_this_chain[tid]-1,xpos,ypos);
						//gr.GetXaxis().SetLimits(-70,70);
				       		//gr.GetYaxis().SetRangeUser(-70,70);
						//gr.SetMarkerStyle(31);
						//mg.Add(gr);
						if(split_curve){
						  //TGraph *gr2 = new TGraph(itemp - 1, tempx, tempy);
						  //gr2.GetXaxis().SetLimits(-70,70);
						  //gr2.GetYaxis().SetRangeUser(-70,70);
						  //gr2.SetMarkerStyle(31);
						  //mg.Add(gr2);
						}
						//mg.Add(genxy);
						//mg.GetXaxis().SetLimits(-70,70);
				        	//mg.GetYaxis().SetRangeUser(-70,70);
						//mg.SetTitle("y vs x;x;y");
						//mg.draw("ap");
				       
						//p.Update();
						//p.SaveAs("gxy.png");
						//TCanvas *trz = new TCanvas("","",800,600);
						//TMultiGraph *mgrz = new TMultiGraph();
						//TGraph *grz = new TGraph(num_hits_this_chain[tid],rpos,zpos);
						//grz.SetMarkerStyle(31);
						//TGraph *hgrz = new TGraph(numpoints,partrhelix,partzhelix);
						//hgrz.SetMarkerColor(2);
						//mgrz.Add(grz);
						//mgrz.Add(hgrz);
						if(split_curve){
						  //TGraph *grz2 = new TGraph(itemp - 1, tempr, tempz);
						  //mgrz.Add(grz2);
						}
						//mgrz.SetTitle("z vs r;r;z");
						//mgrz.draw("ap");
						//trz.SaveAs("grz.png");
						//TApplication *tapp = new TApplication("tapp",0,0);
						//TApplication tapp("App", 0, 0);
						//TCanvas *tc = new TCanvas("","",800,600);
						//TView3D *view = (TView3D*) TView::CreateView(1);
						//view.SetRange(5,5,5,25,25,25);
						//TView *view = TView::CreateView(1);
				        	//view.SetRange(-70,-70,100,70,70,200);
						//TNtuple *n = new TNtuple("n","n","x:y:z");
						for(int i = 0; i <= num_hits_this_chain[tid] ; i++){
						  //n.Fill(xpos[i],ypos[i],zpos[i]);
					  		//std::cout << xpos[i] << " " << ypos[i] << " " << zpos[i] << " " << Math.ATan2(ypos[i],xpos[i])*Math.RadToDeg() <<  std::endl;
						}
						if(split_curve){
						  for(int i = 0; i <= itemp - 1; i++){
						    //n.Fill(tempx[i],tempy[i],tempz[i]);
						  }
						}

						//n.SetMarkerStyle(3);
						//n.draw("x:y:z");
						//helixn.draw("x:y:z","","same");
				        	//tc.cd();
						//tc.Modified(); 
						//tc.Update();
						//TApplication *tapp = new TApplication("tapp",&argc, argv);
						//tapp.Run();
				        	//tc.SaveAs("3d.png");
				       
						//std::cout << "\nNext Event?" << std::endl;
						//int key = std::cin.get();
						//if (key == EOF || key == 'n' || key == 'N') return 0;
						//if (key != '\n') std::cin.ignore(numeric_limits<streamsize>::max(), '\n');
						//std::cout << "OK" << std::endl;
					  
						flag = false;
					}
				}
				//double szpos[300][3] = new double[300][3];
				if(b < n_RTPC__rec_TID){
					tid = RTPC__rec_TID.getValue(b);
					//std::cout << tid << endl;
				}
				else
			  	{
				    	break;
			  	}
				//	if(tid = -111){break;}
				num_chains++;
				num_hits_this_chain[tid] = 0;
			}else if(finish_track_flag){
			  	tempx[itemp] = RTPC__rec_posX.getValue(b);
			  	tempy[itemp] = RTPC__rec_posY.getValue(b);
			  	tempz[itemp] = RTPC__rec_posZ.getValue(b);
			  	tempr[itemp] = Math.Sqrt(tempx[itemp]*tempx[itemp] + tempy[itemp]*tempy[itemp]);
				itemp++;
			   	continue;
			}
			
			cellID = RTPC__rec_cellID.getValue(b);
			time = RTPC__rec_time.getValue(b);
			z = RTPC__rec_posZ.getValue(b);//*10.0;
			x = RTPC__rec_posX.getValue(b);//*10.0;
			y = RTPC__rec_posY.getValue(b);//*10.0;
			r = sqrt(x*x+y*y);
			phi = atan2(y,x);
			q = 1;
			//std::cout << x << " " << y << " " << z << endl;
			if(split_curve && b > 0 && !trackidchange && !finish_track_flag && Math.Abs(time - RTPC__rec_time.getValue(b-1))>500){
			  	finish_track_flag = true;
			  	//std::cout << "finish track flag" << std::endl;
			  	continue;
			}
			itemp = 0; 
			num_hits_this_chain[tid]++;
			hh_num_hits++;
			chain_hits[tid][num_hits_this_chain[tid]] = hh_num_hits;
			/*			double z = RTPC__rec_posZ.getValue(b);//*10.0;
			double x = RTPC__rec_posX.getValue(b);//*10.0;
			double y = RTPC__rec_posY.getValue(b);//*10.0;
			double r = sqrt(x*x+y*y);
			double phi = atan2(y,x);
			double q = 1;*/
		/*szpos[num_hits_this_chain[tid]-1][0] = x;
			szpos[num_hits_this_chain[tid]-1][1] = y;
			szpos[num_hits_this_chain[tid]-1][2] = z;
			
			hh_hitlist[hh_num_hits] = new HitVector(cellID,time,1,z,r,phi,q);
			//std::cout << hh_num_hits << std::endl;
		}
		//std::cout << hh_num_hits << std::endl;
		*/
		/*double szpos_gen[300][3];
		int num_gen_hits = 0;
	        for(int b = 0; b < n_gen_TID; b++){
		  if(RTPC__pos_tid.getValue(b) == 2){
		    szpos_gen[num_gen_hits][0] = RTPC__pos_posx.getValue(b);
		    szpos_gen[num_gen_hits][1] = RTPC__pos_posy.getValue(b);
		    szpos_gen[num_gen_hits][2] = RTPC__pos_posz.getValue(b);
		    
		    num_gen_hits++;
		    
		  }
		}
		
		HelixFit(num_gen_hits, szpos_gen, R, A, B, Phi_deg, Theta_deg, Z0, fit_track_to_beamline);
//std::cout << "tid : "  << tid << std::endl;
	       	/*std::cout << "A gen: " << A << std::endl;
	        std::cout << "B gen: " << B << std::endl;
               	std::cout << "R gen: " << R << std::endl;
	        std::cout << "p gen: " << 0.3*50*R/10 << std::endl;
	       	std::cout << "Z gen: " << Z0 << std::endl;
	       	std::cout << "Theta gen " << Theta_deg << std::endl;
		std::cout << "Phi gen " << Phi_deg << std::endl;
		*/
		/*double genrec = 0.3*50*R/10;
		h3.Fill(genrec);
		hHitsvsMom.Fill(mom*1000,num_gen_hits);
		mom_all[entry] = mom*1000;
		num_hits[entry] = num_gen_hits;
		z_all[entry] = vz;
		hMomvsTheta.Fill(thetadeg,mom*1000);
		hZvsTheta.Fill(thetadeg,vz);
		theta_all[entry] = Math.Tan(thetarad);
		//		std::cout << vz << " " << Math.Tan(thetarad) << endl;
		if((((Math.Tan(thetarad) > 0) && ((200-vz)*Math.Tan(thetarad) > 50)) || ((Math.Tan(thetarad) < 0) && (-(vz+200)*Math.Tan(thetarad) > 50))) && num_gen_hits < 5) {
		//if(num_gen_hits < 5 && vz < (1.25*thetadeg + 75) && vz > (1.25*thetadeg - 300)){ //if(-(vz+200)*Math.Cos(thetarad) > 0 && (200 - vz)*Math.Cos(thetarad) > 0){//if(num_gen_hits < 5){
		  hMomvsThetaCut.Fill(thetadeg,mom*1000);
		  hZvsThetaCut.Fill(thetadeg,vz);
		  hGenRecvsGenTrue2.Fill(mom*1000,genrec);
		}else{
		  hMomvsThetaelse.Fill(thetadeg,mom*1000);
		  hZvsThetaelse.Fill(thetadeg,vz);
		}
		hGenRecvsGenTrue.Fill(mom*1000,genrec);
		double xtempgen[num_gen_hits-1];
		double ytempgen[num_gen_hits-1];
		double tmax = 2*Math.Pi();
	       	int numpoints2 = 2000;
		double step2 = (tmax)/(numpoints2-1);
	       	double xarrinner2[numpoints2];
	       	double yarrinner2[numpoints2];
	       	double xarrouter2[numpoints2];
	       	double yarrouter2[numpoints2];
	       	for(int i = 0; i < numpoints2; i++)
	        {
		  xarrinner2[i] = xcirc(i*step2,30,0);
		  yarrinner2[i] = ycirc(i*step2,30,0);
		  xarrouter2[i] = xcirc(i*step2,70,0);
		  yarrouter2[i] = ycirc(i*step2,70,0);
					  
		}
		TGraph ginner = new TGraph(numpoints2,xarrinner2,yarrinner2);
	       	TGraph gouter = new TGraph(numpoints2,xarrouter2,yarrouter2);

	          if(true){
		//if(genrec<1){
		  failed_genrec++;
		  for(int i = 0; i < num_gen_hits-1; i++){
		    xtempgen[i] = szpos_gen[i][0];
		    ytempgen[i] = szpos_gen[i][1];
		  }
		  gtest[entry] = new TGraph(num_gen_hits-1, xtempgen, ytempgen); 		
		  //		  std::cout << "DEBUG fail" << num_gen_hits << std::endl;
		}else{
		  double stuffx[1];
		  double stuffy[1];
		  stuffx[0] = 0;
		  stuffy[0] = 0;
		  gtest[entry] = new TGraph(1,stuffx,stuffy);
		  std::cout << "DEBUG succeed" << num_gen_hits << std::endl;
		}
		c6.cd();
		gtest[entry].SetTitle(Form("%f",mom*1000));
		gtest[entry].GetXaxis().SetLimits(-75,75);
		gtest[entry].GetYaxis().SetRangeUser(-75,75);
		
		gtest[entry].SetMarkerStyle(21);
		gtest[entry].SetMarkerColor(kBlack);      
		gtest[entry].draw("AP");
		ginner.draw("same");
		gouter.draw("same");
		//c6.SaveAs(Form("images_out/%u-mom=%f-theta=%f-vz=%f.png",entry,mom*1000,thetadeg,vz));
		c6.cd();
		h4.Fill(num_gen_hits);
		
		//After looping through all tracks
		
		/*HashMap<int,double>::iterator it1 = zvertex_rec.begin();
		HashMap<int,double>::iterator it2 = tshift_rec.begin();
		double zvertex_rec_comp = 100000;
		double tshift_rec_small = 100000;
		int tid_test = 0;
		int tid_test2 = 0;
		boolean found_track = false;
		while(it2!=tshift_rec.end()){
		  cout << it2.second << endl;
		  if(Math.Abs(it2.second) < tshift_rec_small){
		    tshift_rec_small = Math.Abs(it2.second);
		    tid_test = it2.first;
		  }
		  
		  if(Math.Abs(it2.second) < 1000 && genrec > 1){
		    double zdiff = Math.Abs(zvertex_rec[it2.first] - Z0);
		    if(zdiff < 20 && zdiff < zvertex_rec_comp){
		      zvertex_rec_comp = Math.Abs(zvertex_rec[it2.first]);
		      tid_test2 = it2.first;
		      found_track = true;
		    }
		  }
		 
		  it2++;
		}
		cout << tshift_rec_small << endl;
		cout << " " << endl;
		if(found_track){
		  cout << " " << endl;
		  cout << momentum_rec[tid_test] << " " << mom << endl;
		  cout << " " << endl;
		  h2.Fill(momentum_rec[tid_test2]);
		  hRecvsGenTrue.Fill(mom*1000,momentum_rec[tid_test2]);
	       	  hRecvsGenRec.Fill(genrec,momentum_rec[tid_test2]);	
		}
		zvertex_rec.clear();
		tshift_rec.clear();
		momentum_rec.clear();*/
	/*}
        h2.draw();
	h1.draw("same");
	h3.draw("same");	
	c1.SaveAs("c1.png");
	
	TCanvas * c2 = new TCanvas("c2", "Momentum genrec vs gen(MeV)", 800, 800);
	hGenRecvsGenTrue.draw("colz");
	c2.SaveAs("c2.png");	
	TCanvas * cGenRecvsGenTrue2 = new TCanvas("cGenRecvsGenTrue2", "Momentum genrec vs gen(MeV)", 800, 800);
	hGenRecvsGenTrue2.draw("colz");
	cGenRecvsGenTrue2.SaveAs("cGenRecvsGenTrue2.png");
	/*
	TCanvas * c3 = new TCanvas("c3", "Momentum rec    vs gen(MeV)", 800, 800);
      	hRecvsGenTrue.draw("colz");
	c3.SaveAs("c3.png");
	TCanvas * c4 = new TCanvas("c4", "Momentum rec vs genrec(MeV)", 800, 800);
	hRecvsGenRec.draw("colz");
	c4.SaveAs("c4.png");
	*/
	/*TCanvas *c5 = new TCanvas("c5","num gen hits per track", 800, 800);
	h4.draw();
	
	TCanvas *cZvsTan = new TCanvas("cZvsTan","Z vs Tan(theta)", 800, 800);
	TGraph *gZvsTan = new TGraph(4999,theta_all,z_all);
	gZvsTan.SetTitle("Z vs Tan(theta);Tan(theta);Z(mm)");
	gZvsTan.SetMarkerStyle(21);
	//	gZvsTan.GetXaxis().SetLimits(-75,75);
	//        gZvsTan.GetYaxis().SetRangeUser(-75,75);
	gZvsTan.draw("AP");
	cZvsTan.SaveAs("cZvsTan.png");

	TCanvas *cZvsTanrange = new TCanvas("cZvsTanrange","Z vs Tan(theta)", 800, 800);
	TGraph *gZvsTanrange = new TGraph(4999,theta_all,z_all);
	gZvsTanrange.SetTitle("Z vs Tan(theta);Tan(theta);Z(mm)");
	gZvsTanrange.SetMarkerStyle(1);
       	gZvsTanrange.GetXaxis().SetLimits(-100,100);
	//        gZvsTanrange.GetYaxis().SetRangeUser(-75,75);
	gZvsTanrange.draw("AP");
	cZvsTanrange.SaveAs("cZvsTanrange.png");
	
	TCanvas *cHitsvsMom = new TCanvas("cHitsvsMom","Num hits vs momentum", 800, 800);
	hHitsvsMom.draw("colz");
	cHitsvsMom.SaveAs("cHitsvsMom.png");

	TCanvas *cHitsvsMom2 = new TCanvas("cHitsvsMom2","Num hits vs momentum", 800, 800);
	TGraph *gHitsvsMom = new TGraph(4999,mom_all,num_hits);
	gHitsvsMom.SetTitle("Hits vs Mom;Momentum(lund);Number of Hits");
	gHitsvsMom.SetMarkerStyle(1);
	gHitsvsMom.SetMarkerColor(kBlack);      
	gHitsvsMom.draw("AP");
	cHitsvsMom2.SaveAs("cHitsvsMom2.png");

	TCanvas *cZvsTheta = new TCanvas("cZvsTheta","Z vertex(lund) vs Theta",800,800);
	hZvsTheta.draw("colz");
	cZvsTheta.SaveAs("cZvsTheta.png");

	TCanvas *cMomvsTheta = new TCanvas("cMomvsTheta","Mom vs Theta", 800, 800);
	hMomvsTheta.setTitleX("Theta");
	hMomvsTheta.setTitleY("Momentum");
	hMomvsTheta.draw("colz");

	
	TCanvas *cZvsThetaCut = new TCanvas("cZvsThetaCut","Z vertex(lund) vs Theta",800,800);
	hZvsThetaCut.draw("colz");


	TCanvas *cMomvsThetaCut = new TCanvas("cMomvsThetaCut","Mom vs Theta", 800, 800);
	hMomvsThetaCut.setTitleX("Theta");
	hMomvsThetaCut.setTitleY("Momentum");
	hMomvsThetaCut.draw("colz");


	TCanvas *cZvsThetaelse = new TCanvas("cZvsThetaelse","Z vertex(lund) vs Theta",800,800);
	hZvsThetaelse.draw("colz");


	TCanvas *cMomvsThetaelse = new TCanvas("cMomvsThetaelse","Mom vs Theta", 800, 800);
	hMomvsThetaelse.setTitleX("Theta");
	hMomvsThetaelse.setTitleY("Momentum");
	hMomvsThetaelse.draw("colz");




    }

    double xcirc(double t, double r, double ph0){
            
            return r*(Math.cos(ph0+t));
    }
    double ycirc(double t, double r, double ph0){
            
            return r*(Math.sin(ph0+t));
    }
    double zcirc(double t, double r, double th){
            
            return r*t/Math.tan(th*Math.PI/180);
    }
*/
}
}
class HitVector2{

	int pad; 
	double t; 
	int status; 
	double z; 
	double r; 
	double phi;
	double q;

	public HitVector2()//Default constructor
	{
		pad = status = -1;
		t = z = r = phi = q = -1.0; 
	}
	public HitVector2(int _pad, double _t, int _status, double _z, double _r, double _phi, double _q)
	{
		pad = _pad;
		t = _t; 
		status = _status;
		z = _z;
		r = _r;
		phi = _phi;
		q = _q;
	}
}