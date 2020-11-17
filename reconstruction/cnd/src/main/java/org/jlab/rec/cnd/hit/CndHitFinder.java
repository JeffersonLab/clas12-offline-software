package org.jlab.rec.cnd.hit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jlab.geom.prim.Arc3D;
import org.jlab.geom.prim.Cylindrical3D;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.rec.cnd.constants.CalibrationConstantsLoader;
import org.jlab.rec.cnd.constants.Parameters;
import org.jlab.rec.cnd.hit.CvtGetHTrack.CVTTrack;

public class CndHitFinder {

	public CndHitFinder(){
		// empty constructor
	}

	// This class contains the core of the code. The findhits method reconstruct good cnd hits from raw halfhits using various cuts and matching.
	// The following method are used to calculate the length the particle is travelling in the paddle while depositing energy.

	// flag to distinguish between calibration mode 0 (loose cuts) and reconstruction mode 1 (tighter cuts)

	public ArrayList<CndHit> findHits(ArrayList<HalfHit> halfhits,int flag, CalibrationConstantsLoader ccdb) 
	{

		Parameters.SetParameters();

		ArrayList<CndHit> HitArray = new ArrayList<CndHit>();      // array list of all "good" reconstructed hits in CND
		ArrayList<CndHit> goodCndHits = new ArrayList<CndHit>();   // array list of unambiguous reconstructed hits in CND

		if(halfhits.size() > 0) {

			// Loop through the half-hits array to find possible physical combinations with neighbours.


			double E1=0;//used to check that the two component of the deposited energy are roughtly the same
			double E2=0;

			int neigh = 0;       // index of the coupled neighbour paddle to the one under consideration
			int pad = 0;         // index of paddle (component) under consideration in the half-hit list
			int lay = 0;         // index of layer under consideration in the half-hit list
			int block = 0;       // index of block (sector) under consideration in the half-hit list 
			int pad_d = 0;       // index of the paddle with the direct signal
			int pad_n = 0;       // index of the paddle with the indirect (neighbour) signal

			int indexadcR=0;	 // index of the adcR in the row  bank
			int indexadcL=0;	 // index of the adcL in the row  bank
			int indextdcR=0;	 // index of the tdcR in the row  bank
			int indextdcL=0;	 // index of the tdcL in the row  bank

			double Tup = 0.;      // Time at upstream end of hit paddle (taken from direct signal)
			double Tdown = 0.;    // Time at downstream end of hit paddle (taken from the neighbour signal)
			double Eup = 0.;      // Energy at upstream end of hit paddle
			double Edown = 0.;    // Energy at downstream end of hit paddle

			double Z_av = 0.;    // Z of the hit position (local co-ordinates, wrt the centre of the paddle)
			double T_hit = 0.;   // Reconstructed time of particle hit in the paddle
			double z_hit = 0.;   // Reconstructed position of the particle hit in the paddle (global co-ordinates, wrt to centre of the Central Detector)
			double x_hit = 0.;   // Reconstructed position of the particle hit in the paddle (global co-ordinates, wrt to centre of the Central Detector)
			double y_hit = 0.;   // Reconstructed position of the particle hit in the paddle (global co-ordinates, wrt to centre of the Central Detector)
			double E_hit = 0;	 // Reconstructed energy deposit of the particle in the paddle 
			double r_hit = 0.;   // Perpendicular distance of the hit position (assuming center of paddle) from the beam-axis  
			double path = 0.;	 // path length travelled by particle (assuming a straight line)
			double phi_hit = 0.; // Phi angle of the hit position (assuming center of paddle) from the x-axis(9o'clock looking downstream)
			double theta_hit = 0.; // Theta angle of the hit position from the z-axis;

			int totrec = 0;      // counter for "good" reconstructions

			for(int i = 0; i < (halfhits.size()); i++) 
			{	
				HalfHit hit1 = halfhits.get(i);   // first, get the half-hit			

				// for each half-hit (signal), work out the coupled paddle:
				block = hit1.Sector();    // the sector (block) of the hit
				pad = hit1.Component();   // the paddle associated with the hit
				lay = hit1.Layer();
				if (pad == 1) neigh = 2;  // the neighbouring paddle
				else neigh = 1;

				// Now loop through the half-hits again and match any which can give a physical reconstruction,
				// but off-set the start of the list to make sure no repeats:

				for (int j = i+1; j < halfhits.size(); j++) 
				{	
					HalfHit hit2 = halfhits.get(j);   // get the second half-hit	

					if (block != hit2.Sector()) continue;             // half-hits must be in the same sector
					if (lay != hit2.Layer()) continue;                // half-hits must be in the same layer					
					if (hit2.Component() != neigh) continue;             // half-hits must come from coupled paddles

					// Decide which one of the two signals is the direct and which one is indirect on the basis of timing.
					// Works if effective velocities in the coupled paddles don't differ much.

					HalfHit hit_d;
					HalfHit hit_n;

					double delta = 	(ccdb.LENGTH[lay-1]/10.)*((1./ccdb.EFFVEL[block-1][lay-1][0])-(1./ccdb.EFFVEL[block-1][lay-1][1]));
					double deltaR;

					if(hit1.Component()==1) deltaR = hit1.Tprop()-hit2.Tprop();
					else deltaR = hit2.Tprop()-hit1.Tprop();

					//System.out.println("sector "+block+" layer "+lay+" v L "+CalibrationConstantsLoader.EFFVEL[block-1][lay-1][0]+ " v R "+CalibrationConstantsLoader.EFFVEL[block-1][lay-1][1]+ " delta "+delta + " deltaR "+deltaR );

					if (deltaR<delta) 
					{

						if(hit1.Component()==1){
							hit_d = hit1;
							hit_n = hit2;
							pad_d = i;
							pad_n = j;
						}
						else{
							hit_d = hit2;
							hit_n = hit1;
							pad_d = j;
							pad_n = i;
						}
					}
					else if (deltaR>delta) 
					{                                                                         

						if(hit1.Component()==1){
							hit_d = hit2;
							hit_n = hit1;
							pad_d = j;
							pad_n = i;
						}
						else{
							hit_d = hit1;
							hit_n = hit2;
							pad_d = i;
							pad_n = j;
						}			
					}
					else continue; 		

					// Now calculate the time and energy at the upstream and downstream ends of the paddle the hit happened in:	
					// attlen is in cm. need to convert to mm -> *10
					Tup = hit_d.Tprop();
					Tdown = hit_n.Tprop() - ccdb.LENGTH[lay-1]/(10.*ccdb.EFFVEL[block-1][lay-1][hit_n.Component()-1]) - ccdb.UTURNTLOSS[block-1][lay-1];
					//Eup = hit_d.Eatt()/CalibrationConstantsLoader.MIPDIRECT[block-1][lay-1][hit_d.Component()-1];
					//Edown = hit_n.Eatt()/(Math.exp(-1.*CalibrationConstantsLoader.LENGTH[lay-1]/(10.*CalibrationConstantsLoader.ATNLEN[block-1][lay-1][hit_n.Component()-1]))*CalibrationConstantsLoader.UTURNELOSS[block-1][lay-1]*CalibrationConstantsLoader.MIPDIRECT[block-1][lay-1][hit_n.Component()-1]);

					//The next two lines have to be used if want to use MIP Indirect for reconstruction
					Eup = hit_d.Eatt()/ccdb.MIPDIRECT[hit_d.Sector()-1][hit_d.Layer()-1][hit_d.Component()-1];
					Edown = hit_n.Eatt()/ccdb.MIPINDIRECT[hit_d.Sector()-1][hit_d.Layer()-1][hit_d.Component()-1];

					// For this particular combination, check whether this gives a z within the paddle length (+/- z resolution).
					// "local" position of hit on the paddle (wrt paddle center):
					Z_av = ((Tup-Tdown) * 10. * ccdb.EFFVEL[block-1][lay-1][hit_d.Component()-1]) / 2.;                                                      

					//removed for calibration
					if(flag==1){
						if ( (Z_av < ((ccdb.LENGTH[lay-1] / (-2.)) - 10.*Parameters.Zres[lay-1])) || (Z_av > ((ccdb.LENGTH[lay-1] / 2.) + 10.*Parameters.Zres[lay-1])) ) continue;                                       
					}

					// Calculate time of hit in paddle and check that it's in a physical window for the event:
					T_hit = (Tup + Tdown - (ccdb.LENGTH[lay-1] / (10.*ccdb.EFFVEL[block-1][lay-1][hit_d.Component()-1]))) / 2.;  // time of hit in the paddle

					//test (check time of hit)
					//System.out.println(T_hit);

					//First cut : the time of hit has to be in a physical time window 
					// window set to 0-250ns for calibration	
					if(flag==1){
						if ((T_hit) < Parameters.MinTime[lay-1] || (T_hit) > Parameters.MaxTime[lay-1]) continue;
					}

					// Calculate the deposited energy and check whether it's over the imposed threshold.			        
					E_hit = (Eup / Math.exp(-1.*(ccdb.LENGTH[lay-1]/2. + Z_av) / (10.*ccdb.ATNLEN[block-1][lay-1][hit_d.Component()-1]))) +  (Edown / Math.exp(-1.*(ccdb.LENGTH[lay-1]/2. - Z_av) / (10.*ccdb.ATNLEN[block-1][lay-1][hit_d.Component()-1])));

					//test (check if the two component of the energy are roughtly the same)
					E1=(Eup / Math.exp(-1.*(ccdb.LENGTH[lay-1]/2. + Z_av) / (10.*ccdb.ATNLEN[block-1][lay-1][hit_d.Component()-1])));
					E2=(Edown / Math.exp(-1.*(ccdb.LENGTH[lay-1]/2. - Z_av) / (10.*ccdb.ATNLEN[block-1][lay-1][hit_d.Component()-1])));
					//					System.out.println(E1);
					//					System.out.println(E2);

					//second cut : the energy of the hit have to be higher than the threshold
					// the threshold is currently 0.1Mev for calibration
					if(flag==1){
						if (E_hit < Parameters.EThresh) continue;
					}

					// third cut (added by Pierre) : the energy of both component of the energy have to be of the same order of magnitude
					//	if (Math.abs(E1-E2) > ((E1+E2)/2.)) continue;

					// If you get a "good" reconstruction, calculate the rest of the details:                                                                                                                   

					if (hit_d.Component() == 1) phi_hit = (block-1) * Parameters.BlockSlice + 0.25*Parameters.BlockSlice;
					else if (hit_d.Component() == 2) phi_hit = (block-1) * Parameters.BlockSlice + 0.75*Parameters.BlockSlice;

					//in mm
					z_hit = (((-1.*ccdb.ZOFFSET[lay-1]) + (ccdb.LENGTH[lay-1]/2.)) + Z_av)+(ccdb.ZTARGET[0]*10);    // z co-ordinate of hit in the paddle wrt Central Detector centre                                                                   
					r_hit = ccdb.INNERRADIUS[0] + (lay - 0.5)*ccdb.THICKNESS[0] + (lay-1)*Parameters.LayerGap;
					path = Math.sqrt(r_hit*r_hit + z_hit*z_hit); 

					//in mm
					x_hit = (r_hit * Math.cos(phi_hit*Math.PI/180.));
					//in mm
					y_hit = (r_hit * Math.sin(phi_hit*Math.PI/180.));

					theta_hit = Math.acos(z_hit/path) * 180./Math.PI;

					totrec++;  // count number of "good" reconstructions

					// Create a new CndHit and fill it with the relevant info:

					CndHit GoodHit = new CndHit(pad_d,pad_n);  // Takes as index the halfhits array indices of the two half-hits involved.

					GoodHit.set_Time(T_hit);
					GoodHit.set_X(x_hit);
					GoodHit.set_Y(y_hit);
					GoodHit.set_Z(z_hit);
					GoodHit.set_Edep(E_hit);
					GoodHit.set_Theta(theta_hit);
					GoodHit.set_Phi(phi_hit);
					GoodHit.set_Sector(block);
					GoodHit.set_Layer(lay);
					//					GoodHit.set_Component(hit_d.Component());			        
					GoodHit.set_Component(hit_d.Component());  // set component to 1 for hits			        

					// set the index of the right and left signal for the reconstucted hit
					if(hit1.Component()==2){
						indexadcR=hit1.Indexadc();
						indexadcL=hit2.Indexadc();
						indextdcR=hit1.Indextdc();
						indextdcL=hit2.Indextdc();
					}
					else{
						indexadcR=hit2.Indexadc();
						indexadcL=hit1.Indexadc();
						indextdcR=hit2.Indextdc();
						indextdcL=hit1.Indextdc();
					}
					GoodHit.set_indexLadc(indexadcL);
					GoodHit.set_indexLtdc(indextdcL);
					GoodHit.set_indexRadc(indexadcR);
					GoodHit.set_indexRtdc(indextdcR);

					HitArray.add(GoodHit);

				}  // close loop over j
			} // close loop over i  		

			// At this stage an array of possible reconstructed hits, type CndHit and called HitArray, has been created. 
			// There may be cases of ambiguous reconstruction: where two signals from one paddle can be matched up with a single one from the neighbour.
			// Remove those reconstructions:

			int ambig_rec[] = new int[totrec];  // to keep track of ambiguous reconstructions in the next loops

			for (int i=0; i<totrec; i++)
			{
				ambig_rec[i] = 0;  // set all to zero before you start.
			}

			for(int i = 0; i < (HitArray.size()); i++)
			{		
				CndHit cndhit1 = HitArray.get(i);

				for(int j = i+1; j < HitArray.size(); j++) 
				{
					CndHit cndhit2 = HitArray.get(j);   

					if (i!=j){
						if ((cndhit1.index_d() == cndhit2.index_d() || cndhit1.index_d() == cndhit2.index_n() || cndhit1.index_n() == cndhit2.index_d() || cndhit1.index_n() == cndhit2.index_n()))	
						{
							ambig_rec[i] = 1;  // set the flags for ambiguous reconstructions
							ambig_rec[j] = 1;
						}
					}
				}
			}

			// Now loop through and create a new array in which there are no ambiguous reconstructions:
			for(int i = 0; i < HitArray.size(); i++)
			{	
				CndHit goodhit = HitArray.get(i);
				if (ambig_rec[i] == 0)
				{
					goodCndHits.add(goodhit);
				}
			}

			// Sort the hits in order of ascending time, so the first one has the shortest time:
			Collections.sort(goodCndHits);  

		}  // closes if halfhit array has non-zero entries...

		return goodCndHits;

	} // findHits function		


	public double findLength(CndHit hit, List<CVTTrack> helices, int flag, CalibrationConstantsLoader ccdb) 
	{
		// this method is used to find the length of the path followed by the detected charged particle in the cnd
		// first we need to know if the particle is charged by matching the hit to the cvt helical tracks
		// then we can calculate the length of the track

		// flag is used to distinguish calibration mode 0 and reconstruction mode 1

		double length = 0;
		double xi=hit.X(); // retrieve cnd hit coordinates
		double yi=hit.Y();
		double zi=hit.Z();
		int lay=hit.Layer();
		// Constants SHOULD NOT be in the code methods BUT in a parameters file or in CCDB!!!

		double Rres= ccdb.THICKNESS[0]/2.; // Resolution in radius (half the thickness of the paddles)
		double Phires= Parameters.BlockSlice/4.; // resolution in Phi (half the angle covered by a paddle)
		double radius = ccdb.INNERRADIUS[0] + (lay - 0.5)*ccdb.THICKNESS[0] + (lay-1)*Parameters.LayerGap;

		//		double incx = Math.sqrt(((xi*xi*15.*15.)/(radius*radius))+((yi*yi)*(3.75*3.75*(Math.PI/180.)*(Math.PI/180.)))); //15 is half the paddle thickness
		//double incy = Math.sqrt(((yi*yi*15.*15.)/(radius*radius))+((xi*xi)*(3.75*3.75*(Math.PI/180.)*(Math.PI/180.))));	// 3.75 is the incertainty in phi		
		//double incz = 38.4/Math.sqrt(hit.Edep());	//evaluated with the non-corrected energy

		double incx = Math.sqrt(((xi*xi*Rres*Rres)/(radius*radius))+((yi*yi)*(Phires*Phires*(Math.PI/180.)*(Math.PI/180.)))); 
		double incy = Math.sqrt(((yi*yi*Rres*Rres)/(radius*radius))+((xi*xi)*(Phires*Phires*(Math.PI/180.)*(Math.PI/180.))));
		double incz = (10.*ccdb.EFFVEL[hit.Sector()-1][lay-1][hit.Component()-1]*Parameters.Tres)/Math.sqrt(hit.Edep());

		//uncertainty in z is estimated using uncertainty in T_d multiplied by veff
		hit.set_uX(incx);
		hit.set_uY(incy);
		hit.set_uZ(incz);

		for(int i =0 ; i<helices.size() ; i++){//loop through helical tracks extracted from the cvt

			// the following line give the point at which the cnd hit would occur according to the cvt track
			//Point3D hitCndfromCvt = helices.get(i).get_Helix().getPointAtRadius(radius); 
			
			//System.out.print("helices size "+helices.get(i).get_TrkInters().size());			
			//System.out.println("layer "+lay+ " helice  "+i);
			ArrayList<Point3D> hitCndfromCvtList = helices.get(i).get_TrkInters().get(lay-1); // middle of the counter
			
			if(hitCndfromCvtList.isEmpty())continue; //Check if a layer as a swimmer intersection. If not go to the next track
			

			Point3D hitCndfromCvt = helices.get(i).get_TrkInters().get(lay-1).get(1); // middle of the counter

			double xj=hitCndfromCvt.x(); // retrieve CndFromCvt hit coordinates
			double yj=hitCndfromCvt.y();
			double zj=hitCndfromCvt.z();

			double rj=(Math.sqrt(xj*xj+yj*yj+zj*zj));
			double phij = Math.signum(yj)*Math.acos(xj/rj)*(180./Math.PI);
			double thetaj = Math.acos(zj/rj)*(180./Math.PI);

			//	if(Math.abs(xi-xj)<(3.*incx)  && Math.abs(yi-yj)<(3.*incy)  && Math.abs(zi-zj)<(3.*incz)) { // CUT are set to x,y,z incertainty
			double zjAv = zj - ((-1.*ccdb.ZOFFSET[lay-1]) + (ccdb.LENGTH[lay-1]/2.))-(ccdb.ZTARGET[0]*10);
			if((flag==0 && Math.abs(xi-xj)<(5.*incx)  && Math.abs(yi-yj)<(5.*incy)  && zjAv>(ccdb.LENGTH[lay-1]/-2.)-10*Parameters.Zres[0] && zjAv<(ccdb.LENGTH[lay-1]/2.)+10*Parameters.Zres[0]) || 
					(flag==1 && Math.abs(xi-xj)<(5.*incx)  && Math.abs(yi-yj)<(5.*incy)  && Math.abs(zi-zj)<(5.*incz))){ // CUT are set to x,y incertainty and zj in the paddle length

				hit.set_AssociatedTrkId(helices.get(i).get_Id());
				hit.set_pathlength(helices.get(i).get_TrkLengths().get(lay-1));
				hit.set_tX(xj);
				hit.set_tY(yj);
				hit.set_tZ(zj);

				// get the length travelled by the particule in the paddle. If the entry point is outside the cnd skip the event, if the escape point is outside the paddle, the escape point is the intersection of the particule path and the plane defined at the edge of the paddle, otherwise get the distance between entrypoint and escape point.

				if(helices.get(i).get_TrkInters().get(lay-1).get(0).z()>((-1.*ccdb.ZOFFSET[lay-1]) + (ccdb.LENGTH[lay-1]))) continue;
				if(helices.get(i).get_TrkInters().get(lay-1).get(0).z()<(-1.*ccdb.ZOFFSET[lay-1])) continue;

				Plane3D zmax = new Plane3D(0.0,0.0,((-1.*ccdb.ZOFFSET[lay-1]) + (ccdb.LENGTH[lay-1])),0.0,0.0,1.0);
				Plane3D zmin = new Plane3D(0.0,0.0,(-1.*ccdb.ZOFFSET[lay-1]),0.0,0.0,1.0);

				if(helices.get(i).get_TrkInters().get(lay-1).get(2).z()>((-1.*ccdb.ZOFFSET[lay-1]) + (ccdb.LENGTH[lay-1]))) {
					Line3D ray = new Line3D(helices.get(i).get_TrkInters().get(lay-1).get(0),helices.get(i).get_TrkInters().get(lay-1).get(2));
					Point3D inter = new Point3D();
					if(zmax.intersection(ray,inter)!=1) continue;
					length = helices.get(i).get_TrkInters().get(lay-1).get(0).distance(inter);
					//System.out.print("use inter + "+inter.z());			
				} 				

				if(helices.get(i).get_TrkInters().get(lay-1).get(2).z()<(-1.*ccdb.ZOFFSET[lay-1])) {
					Line3D ray = new Line3D(helices.get(i).get_TrkInters().get(lay-1).get(0),helices.get(i).get_TrkInters().get(lay-1).get(2));
					Point3D inter = new Point3D();
					if(zmin.intersection(ray,inter)!=1) continue;
					length = helices.get(i).get_TrkInters().get(lay-1).get(0).distance(inter);
					//System.out.print("use inter - "+inter.z());
				}

				else {length = helices.get(i).get_TrkInters().get(lay-1).get(0).distance(helices.get(i).get_TrkInters().get(lay-1).get(2));
				}
				
				//System.out.println(length);
			}
		}
		return length; 

		} //findLength function for charged particles



		public double findLengthNeutral(Point3D vertex, CndHit hit, CalibrationConstantsLoader ccdb){

			// not finished	

			// if the particle is not charged, it is not detected in the cvt and we can calculate its pathlength using reconstructed vertex				
			double length = 0.;

			if( vertex!=null){

				double xi = hit.X();
				double yi = hit.Y();
				double zi = hit.Z();
				Point3D hitpoint = new Point3D(xi,yi,zi);
				double energyNCorr=hit.Edep();
				int lay = hit.Layer();
				double entryradius = ccdb.INNERRADIUS[0] + (lay-1)*ccdb.THICKNESS[0] + (lay-1)*Parameters.LayerGap;
				double escaperadius = ccdb.INNERRADIUS[0] + (lay)*ccdb.THICKNESS[0] + (lay-1)*Parameters.LayerGap;	
				// get the length of the path as the distance between the two intersection points of 
				// the line between the hit point and the vertex and 2 cylinders corresponding to the hit paddle.		

				//set the cylinders
				Point3D center = new Point3D(0.,0.,-1.*ccdb.LENGTH[lay-1]);
				Point3D origin1 = new Point3D(entryradius,0.,0.);
				Point3D origin2 = new Point3D(escaperadius,0.,0.);
				Vector3D normal = new Vector3D(0.,0.,1.);
				Arc3D arc1 = new Arc3D(origin1,center,normal,Math.PI*2.);
				Arc3D arc2 = new Arc3D(origin2,center,normal,Math.PI*2.);
				Cylindrical3D cyl1 = new Cylindrical3D(arc1,2.*ccdb.LENGTH[lay-1]);
				Cylindrical3D cyl2 = new Cylindrical3D(arc2,2.*ccdb.LENGTH[lay-1]);
				// the cylinders are biggers than the actual cnd but it is just for convenience, as the hit point is in the cnd anyway

				//set the line between the vertex and the hit point
				Line3D line = new Line3D(vertex,hitpoint);

				//find intersection points
				List<Point3D> entrypoints = new ArrayList<Point3D>();
				List<Point3D> exitpoints = new ArrayList<Point3D>();
				cyl1.intersectionRay(line, entrypoints);
				cyl2.intersectionRay(line, exitpoints);
				if(entrypoints.size()==1 && exitpoints.size()==1){
					length=entrypoints.get(0).distance(exitpoints.get(0));
					System.err.println("length neutral " + length);
				}
				else {
					System.err.println("probleme intersection"+" entrypoints nb "+entrypoints.size()+" exitpoints nb "+exitpoints.size());}

				hit.set_Edep(energyNCorr*(Math.max(length, ccdb.THICKNESS[0])/ccdb.THICKNESS[0]));
				return length;
			}
			else return length;

		} // fingLengthNeutral



	} // CndHitFinder
