package org.jlab.rec.rich;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.hipo.HipoDataEvent;

import javax.swing.JFrame;
import org.jlab.detector.banks.RawDataBank;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;

import org.jlab.detector.geom.RICH.RICHGeoFactory;

public class RICHPMTReconstruction {


    private RICHEvent          richevent;
    private RICHGeoFactory     richgeo;
    private RICHio             richio;
      
    // ----------------
    public RICHPMTReconstruction(RICHEvent richeve, RICHGeoFactory richgeo, RICHio richio) {
    // ----------------

        this.richevent = richeve;
        this.richgeo = richgeo;
        this.richio = richio;

    }

    // ----------------
    public void process_RawData(DataEvent event, RICHParameters richpar, RICHCalibration richcal) {
    // ----------------

        int debugMode = 0;
        if(debugMode>=1){
            System.out.println("---------------------------------");
            System.out.println("PMT Event: Process raw data ");
            System.out.println("---------------------------------");
        }

        ArrayList<RICHEdge>     allEdges      =    new ArrayList();
        ArrayList<RICHEdge>     Leads         =    new ArrayList();
        ArrayList<RICHEdge>     Trails        =    new ArrayList();

        ArrayList<RICHHit>      Hits          =    new ArrayList();
        ArrayList<RICHCluster>  AllClusters   =    new ArrayList();
        ArrayList<RICHCluster>  Clusters      =    new ArrayList();

        // get edges fron banks
        allEdges = read_RawBank(event);

        // select good edges and order them
        Leads   = selectLeadEdges(allEdges);
        Trails  = selectTrailEdges(allEdges);

        // build hits
        Hits     = reco_PMTHits(Leads, Trails, richcal);
        AllClusters = findClusters(Hits);
        Clusters = selectGoodClusters(AllClusters);

        find_XTalk(Hits, AllClusters, richpar);

        richevent.add_Hits(Hits);
        richevent.add_Clusters(Clusters);
        richevent.select_Signals();

        richio.write_PMTBanks(event, richevent);

    }

    // ----------------
    public ArrayList<RICHEdge> read_RawBank(DataEvent event) {
    // ----------------

        int debugMode = 0;

        ArrayList<RICHEdge> allEdges = null;
        
        if(event instanceof EvioDataEvent) {
            if(debugMode>=2)System.out.print("EVIO event found\t");
            //allEdges = this.read_RawEdgesEVIO(event);
        }
        
        if(event instanceof HipoDataEvent) {
            if(debugMode>=2)System.out.print("HIPO event found\t");
            allEdges = this.read_RawEdgesHIPO(event);
        }

        if(debugMode>=2) {
            System.out.println("Found " + allEdges.size() + " edges");
            for(int i = 0; i < allEdges.size(); i++) {
                System.out.print(i + "\t");
                allEdges.get(i).showEdge();
            }
        }
        return allEdges;
    }
    
    
    // ----------------
    public ArrayList<RICHEdge> selectLeadEdges(ArrayList<RICHEdge> allEdges) {
    // ----------------

        int debugMode = 0;
        ArrayList<RICHEdge> Leads = new ArrayList<RICHEdge>();
        
        for(int i = 0; i < allEdges.size(); i++) {
            RICHEdge edge = allEdges.get(i);
                if(edge.pass_EdgeSelection()) {
                        if(edge.get_polarity()==RICHConstants.LEADING_EDGE_POLARITY)Leads.add(edge);      
                }
        }      

        if(debugMode>=2)System.out.println("Sorting leads "+Leads.size());
        Collections.sort(Leads);
        // redefine IDs accroding to the sorting
        for(int il=0; il<Leads.size(); il++) Leads.get(il).set_id(il);

        if(debugMode>=2) {
            if(debugMode>=1)System.out.println("List of selected Leading edges");
            for(int i = 0; i < Leads.size(); i++) 
            {      
                System.out.print(i + "\t");
                Leads.get(i).showEdge();
            }
        }
        return Leads;
    }


    // ----------------
    public ArrayList<RICHEdge> selectTrailEdges(ArrayList<RICHEdge> allEdges) {
    // ----------------

        int debugMode = 0;
        ArrayList<RICHEdge> Trails = new ArrayList<RICHEdge>();
        
        for(int i = 0; i < allEdges.size(); i++) 
        {
            RICHEdge edge = allEdges.get(i);
                if(edge.pass_EdgeSelection()) {
                        if(edge.get_polarity()==RICHConstants.TRAILING_EDGE_POLARITY)Trails.add(edge);      
                }
        }      


        if(debugMode>=2)System.out.println("Sorting trail "+Trails.size());
        Collections.sort(Trails);
        // redefine IDs accroding to the sorting
        for(int it=0; it<Trails.size(); it++) Trails.get(it).set_id(it);

        if(debugMode>=2) {
            if(debugMode>=1)System.out.println("List of selected Trailing edges");
            for(int i = 0; i < Trails.size(); i++) 
            {      
                System.out.print(i + "\t");
                Trails.get(i).showEdge();
            }
        }
        return Trails;
    }


    // ----------------
    public ArrayList<RICHEdge> read_RawEdgesHIPO(DataEvent event) {
    // ----------------
        // getting raw data bank

        int debugMode = 0;
        if(debugMode>=2) System.out.println("Getting raw edges from RICH:tdc bank");

        ArrayList<RICHEdge>  edges = new ArrayList<RICHEdge>();
        if(event.hasBank("RICH::tdc")==true) {
            RawDataBank bankDGTZ = new RawDataBank("RICH::tdc");
            bankDGTZ.read(event);
            int nrows = bankDGTZ.rows();
            for(int row = 0; row < nrows; row++){
                int isector     = bankDGTZ.getByte("sector",row);
                int ilayer      = bankDGTZ.getByte("layer",row);
                int icomponent  = bankDGTZ.getShort("component",row);
                int iorder      = bankDGTZ.trueOrder(row);
                int itdc        = bankDGTZ.getInt("TDC",row);
                if(ilayer<0)ilayer=ilayer+256;
		if(debugMode>=2)System.out.print(" --> Edge "+row+" sec "+isector+" lay "+ilayer+" comp "+icomponent+" order "+iorder+" tdc "+itdc+"\n");
                if(itdc!=-1){
                    RICHEdge edge = new RICHEdge(bankDGTZ.trueIndex(row), isector, ilayer, icomponent, iorder, itdc);
                    edges.add(edge); 
                }                
            }
        }
        return edges;
    }
    

    // ----------------
    public ArrayList<RICHHit> reco_PMTHits(ArrayList<RICHEdge> Leads, ArrayList<RICHEdge >Trails, RICHCalibration richcal) {
    // ----------------

        int debugMode = 0;
        int nhit=0;
        ArrayList<RICHHit> hits = new ArrayList();

        if(debugMode>=1)System.out.println("Entering hit reconstruction");
        for(int iled=0; iled<Leads.size(); iled++) {
            RICHEdge lead = Leads.get(iled);

          if(lead.get_hit()>0)continue;
            //if(debugMode>=1) System.out.println("Working on lead "+iled+" ch "+lead.get_channel()+" tdc "+lead.get_tdc());
            if(debugMode>=1) System.out.format("Working on lead %4d  sec %3d  tile %4d  ch %4d  tdc %6d \n",iled,lead.get_sector(),
                lead.get_tile(), lead.get_channel(),lead.get_tdc());

            for(int itra=0; itra<Trails.size(); itra++) {
                RICHEdge trail = Trails.get(itra);

                if(trail.get_hit()>0)continue;
                if(trail.get_sector() == lead.get_sector() && 
                   trail.get_tile()*192+trail.get_channel() ==  lead.get_tile()*192+lead.get_channel()){
                    //if(debugMode>=1) System.out.println("Candidate trail found "+itra+" ch "+trail.get_channel()+" tdc " +trail.get_tdc());
                    if(debugMode>=1) System.out.format(" --> Candidate trail %4d  sec %3d  tile %4d ch %4d  tdc %6d \n",itra,trail.get_sector(),
                                     trail.get_tile(),trail.get_channel(),trail.get_tdc());
                    if(trail.get_tdc() > lead.get_tdc()){
                        if(debugMode>=1) System.out.println("     --> Candidate hit found from lead "+iled+" trail "+itra);

                  nhit++;
                  lead.set_hit(nhit);
                  trail.set_hit(nhit);
                  RICHHit hit = new RICHHit(nhit, richevent.getFTOFphase(), lead, trail, richgeo, richcal);
                  hits.add(hit);      
                  break;

                    }
                }
            }
        }

        if(debugMode>=2)System.out.println("Sorting hits "+hits.size());
        Collections.sort(hits);
        // redefine the IDs following the sorting
        for(int ih=0; ih<hits.size(); ih++) {
            hits.get(ih).set_id(ih);
            // likely unnecessary 
            int ilead = hits.get(ih).get_lead();
            Leads.get(ilead).set_hit(ih);
            int itrail = hits.get(ih).get_trail();
            Trails.get(itrail).set_hit(ih);
        }

        if(debugMode>=1) {
            System.out.println("-------------------------");
            System.out.println("List of selected Hits");
            System.out.println("-------------------------");
            for(int i = 0; i < hits.size(); i++) 
            {      
                hits.get(i).showHit();
            }
        }
        return hits;
    }


    // ----------------
    public ArrayList<RICHCluster> findClusters(ArrayList<RICHHit> hits) {
    // ----------------

        int debugMode = 0;
        ArrayList<RICHCluster> allclusters = new ArrayList();
        if(debugMode>=2) {
            System.out.println("--------------------\n");
            System.out.println("Building allclusters\n");
            System.out.println("--------------------\n");
        }

        for(int ihit=0; ihit<hits.size(); ihit++) {
            RICHHit hit = hits.get(ihit);
                if(hit.get_cluster()==0)  {                       // this hit is not yet associated with a cluster
                if(debugMode>=2)System.out.println("  Check hit "+hit.get_id()+" "+hit.get_pmt()+" "+hit.get_anode()+" "+hit.get_Time());

                for(int jclus=0; jclus<allclusters.size(); jclus++) {
                    RICHCluster cluster = allclusters.get(jclus);
                    if(cluster.containsHit(hit)) {
                        hit.set_cluster(cluster.get_id());     // attaching hit to previous cluster
                        cluster.add(hit);
                        if(debugMode>=2) System.out.println("Attaching hit " + ihit + " to cluster " + cluster.get_id() + " label " + hit.get_cluster());
                    }
                }
            }

            if(hit.get_cluster()==0)  {                       // new cluster found
                RICHCluster cluster = new RICHCluster(allclusters.size()+1);
                hit.set_cluster(cluster.get_id());
                cluster.add(hit);
                allclusters.add(cluster);
                if(debugMode>=2) System.out.println("Creating new cluster with id " + cluster.get_id());
            }
        }

      if(debugMode>=2){
            System.out.println("List of all Clusters");
            for(int i=0; i<allclusters.size(); i++) {
                allclusters.get(i).showCluster();
            }
      }

        return allclusters;
    }


    // ----------------
    public ArrayList<RICHCluster> selectGoodClusters(ArrayList<RICHCluster> allclusters) {
    // ----------------

        int debugMode = 0;
        ArrayList<RICHCluster> clusters = new ArrayList();
        if(debugMode>=2) System.out.println("\nSelecting good clusters");

        int nclu = 0;
        for(int i=0; i<allclusters.size(); i++) {
            if(allclusters.get(i).isgoodCluster()) {
                RICHCluster goodclu = allclusters.get(i);
                int merge = 0 ;
                for (int j=0; j<clusters.size(); j++){
                    if(clusters.get(j).get(0).get_pmt() == goodclu.get(0).get_pmt()){
                        clusters.get(j).merge(goodclu);
                        merge = 1;
                        if(debugMode>=1)System.out.format(" merge clu %d %d  xyz %7.2f %7.2f %7.2f  to %d %d \n",i,goodclu.get_id(),goodclu.get_x(), goodclu.get_y(), goodclu.get_z(), j,clusters.get(j).get_id());
                    }
                }
                if(merge==0){
                    nclu++;
                    goodclu.set_id(nclu);
                    clusters.add(goodclu);
                }
          }else{
                // cancel hit to cluster link
                RICHCluster badclu = allclusters.get(i);
                for(int j = 0; j< badclu.size(); j++) {
                    badclu.get(j).set_cluster(0);
                }
            }
        }

        if(debugMode>=1){
            System.out.format("-------------------------\n");
            System.out.format("List of selected Clusters %4d \n",clusters.size());
            System.out.format("-------------------------\n");
            for(int i=0; i<clusters.size(); i++) {
                clusters.get(i).showCluster();
            }
        }

        /*Collections.sort(clusters);

        if(debugMode>=1){
            System.out.println("List of sorted Clusters");
            for(int i=0; i<clusters.size(); i++) {
                clusters.get(i).showCluster();
            }
        }*/

        return clusters;
    }


    // ----------------
    public void find_XTalk(ArrayList<RICHHit> hits, ArrayList<RICHCluster> allclusters, RICHParameters richpar) {
    // ----------------

        int debugMode = 0;
        if(debugMode==1){
            System.out.println("----------------");
            System.out.println("Search for Xtalk");
            System.out.println("----------------");
            System.out.format(" %7.3f \n",richpar.GOODHIT_FRAC);
        }

        for(int ih=0; ih<hits.size(); ih++) {
            RICHHit hiti = hits.get(ih);
            if(hiti.get_cluster()!=0)  continue; // this hit is not yet associated with a cluster

            for(int jh=ih+1; jh<hits.size(); jh++) {
                RICHHit hitj = hits.get(jh);
                if(hiti.get_cluster()!=0)  continue; // this hit is not yet associated with a cluster
                if(debugMode==1)System.out.println("Hit pair "+ih+" "+hiti.get_id()+" "+hiti.get_pmt()+" "+hiti.get_channel()+" "+hiti.get_duration()+" "+hiti.get_cluster()+" | " +jh+" "+hitj.get_id()+" "+hitj.get_pmt()+" "+hitj.get_channel()+" "+hitj.get_duration()+" "+hitj.get_cluster());

                if(hiti.get_pmt()==hitj.get_pmt() && hitj.get_duration()*100 < hiti.get_duration()*richpar.GOODHIT_FRAC){
                    for(int k=-1; k<=1; k+=2 ) {
                        if(hiti.get_channel() == (k+hitj.get_channel())) {hitj.set_xtalk(1000+hiti.get_id()+1); if(debugMode==1)System.out.println(" E Xtalk "+hitj.get_xtalk());}
                    }
                }
            }
        }

        for(int iclu=0; iclu<allclusters.size(); iclu++) {
            if(allclusters.get(iclu).get_size()< RICHConstants.CLUSTER_MIN_SIZE) {
                RICHCluster clu = allclusters.get(iclu);
                if(debugMode==1)System.out.println("  Cluster "+ iclu +" ID "+clu.get_id());
                for(int ih = 0; ih< clu.size(); ih++) {
                    RICHHit hiti = clu.get(ih);

                    for(int jh = ih+1; jh< clu.size(); jh++) {
                        RICHHit hitj = clu.get(jh);
                        if(debugMode==1)System.out.println("Hit pair "+ih+" "+hiti.get_id()+" "+hiti.get_pmt()+" "+hiti.get_channel()+" "+hiti.get_duration()+" | " +jh+" "+hitj.get_id()+" "+hitj.get_pmt()+" "+hitj.get_channel()+" "+hitj.get_duration());

                        if(hitj.get_duration()*100 < hiti.get_duration()*richpar.GOODHIT_FRAC) {hitj.set_xtalk(hiti.get_id()+1); if(debugMode==1)System.out.println(" O Xtalk "+hitj.get_xtalk());}

                    }
                }
            }
        }

    }

}
