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
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;


public class RICHPMTReconstruction {


    private RICHTool         tool;
    private RICHEvent        richevent;
    private RICHio           richio;
      
    // ----------------
    public RICHPMTReconstruction(RICHEvent richeve, RICHTool richtool,RICHio io) {
    // ----------------

        tool = richtool;
        richevent = richeve;
        richio = io;

    }

    // ----------------
    public void processRawData(DataEvent event) {
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
        allEdges = readRawBank(event);

        // select good edges and order them
        Leads   = selectLeadEdges(allEdges);
        Trails  = selectTrailEdges(allEdges);

        // build hits
        Hits     = reconstructHits(Leads, Trails);
        AllClusters = findClusters(Hits);
        Clusters = selectGoodClusters(AllClusters);

        findXTalk(Hits, AllClusters);

        richevent.add_Hits(Hits);
        richevent.add_Clusters(Clusters);

        richio.write_PMTBanks(event, richevent);

    }

    // ----------------
    public ArrayList<RICHEdge> readRawBank(DataEvent event) {
    // ----------------

        int debugMode = 0;

        ArrayList<RICHEdge> allEdges = null;
        
        if(event instanceof EvioDataEvent) {
            if(debugMode>=2)System.out.print("EVIO event found\t");
            //allEdges = this.readRawEdgesEVIO(event);
        }
        
        if(event instanceof HipoDataEvent) {
            if(debugMode>=2)System.out.print("HIPO event found\t");
            allEdges = this.readRawEdgesHIPO(event);
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
                if(edge.passEdgeSelection()) {
                        if(edge.get_polarity()==tool.get_Constants().LEADING_EDGE_POLARITY)Leads.add(edge);      
                }
        }      

        if(debugMode>=2)System.out.println("Sorting leads "+Leads.size());
        Collections.sort(Leads);

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
                if(edge.passEdgeSelection()) {
                        if(edge.get_polarity()==tool.get_Constants().TRAILING_EDGE_POLARITY)Trails.add(edge);      
                }
        }      


        if(debugMode>=2)System.out.println("Sorting trail "+Trails.size());
        Collections.sort(Trails);

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
    public ArrayList<RICHEdge> readRawEdgesHIPO(DataEvent event) {
    // ----------------
        // getting raw data bank

        int debugMode = 0;
        if(debugMode>=2) System.out.println("Getting raw edges from RICH:tdc bank");

        ArrayList<RICHEdge>  edges = new ArrayList<RICHEdge>();
        if(event.hasBank("RICH::tdc")==true) {
            DataBank bankDGTZ = event.getBank("RICH::tdc");
            int nrows = bankDGTZ.rows();
            for(int row = 0; row < nrows; row++){
                int isector     = bankDGTZ.getByte("sector",row);
                int ilayer      = bankDGTZ.getByte("layer",row);
                int icomponent  = bankDGTZ.getShort("component",row);
                int iorder      = bankDGTZ.getByte("order",row);
                int itdc        = bankDGTZ.getInt("TDC",row);
		if(debugMode>=2)System.out.print(" --> Edge "+row+" sec "+isector+" lay "+ilayer+" comp "+icomponent+" order "+iorder+" tdc "+itdc+"\n");
            if(ilayer<0)ilayer=ilayer+256;
                if(itdc!=-1){
                    RICHEdge edge = new RICHEdge(row, isector, ilayer, icomponent, iorder, itdc);
                    edges.add(edge); 
                }                
            }
        }
        return edges;
    }
    

    // ----------------
    public ArrayList<RICHHit> reconstructHits(ArrayList<RICHEdge> Leads, ArrayList<RICHEdge >Trails) {
    // ----------------

        int debugMode = 0;
        int nhit=0;
        ArrayList<RICHHit> hits = new ArrayList();

        if(debugMode>=2)System.out.println("Entering hit reconstruction");
        for(int iled=0; iled<Leads.size(); iled++) {
            RICHEdge lead = Leads.get(iled);

          if(lead.get_hit()>0)continue;
            if(debugMode>=2) System.out.println("Working on lead "+iled+" ch "+lead.get_channel()+" tdc "+lead.get_tdc());

            for(int itra=0; itra<Trails.size(); itra++) {
                RICHEdge trail = Trails.get(itra);

                if(trail.get_hit()>0)continue;
                if(trail.get_tile()*192+trail.get_channel() ==  lead.get_tile()*192+lead.get_channel()){
                    if(debugMode>=2) System.out.println("Candidate trail found "+itra+" ch "+trail.get_channel()+" tdc " +trail.get_tdc());
                    if(trail.get_tdc() > lead.get_tdc()){
                        if(debugMode>=2) System.out.println("Candidate hit found from lead "+iled+" trail "+itra);

                  nhit++;
                  lead.set_hit(nhit);
                  trail.set_hit(nhit);
                  RICHHit hit = new RICHHit(nhit, tool, richevent.getFTOFphase(), lead, trail);
                  hits.add(hit);      
                  break;

                    }
                }
            }
        }

        if(debugMode>=2)System.out.println("Sorting hits "+hits.size());
        Collections.sort(hits);

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
                if(debugMode>=2)System.out.println("  Check hit "+hit.get_id()+" "+hit.get_pmt()+" "+hit.get_anode()+" "+hit.get_time());

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
            System.out.println("-------------------------");
            System.out.println("List of selected Clusters");
            System.out.println("-------------------------");
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
    public void findXTalk(ArrayList<RICHHit> hits, ArrayList<RICHCluster> allclusters) {
    // ----------------

        int debugMode = 0;
        if(debugMode==6){
            System.out.println("----------------");
            System.out.println("Search for Xtalk");
            System.out.println("----------------");
        }

        for(int ih=0; ih<hits.size(); ih++) {
            RICHHit hiti = hits.get(ih);
            if(hiti.get_cluster()!=0)  continue; // this hit is not yet associated with a cluster

            for(int jh=ih+1; jh<hits.size(); jh++) {
                RICHHit hitj = hits.get(jh);
                if(hiti.get_cluster()!=0)  continue; // this hit is not yet associated with a cluster
                if(debugMode==6)System.out.println("Hit pair "+ih+" "+hiti.get_id()+" "+hiti.get_pmt()+" "+hiti.get_channel()+" "+hiti.get_duration()+" "+hiti.get_cluster()+" | " +jh+" "+hitj.get_id()+" "+hitj.get_pmt()+" "+hitj.get_channel()+" "+hitj.get_duration()+" "+hitj.get_cluster());

                if(hiti.get_pmt()==hitj.get_pmt() && hitj.get_duration()*100 < hiti.get_duration()*tool.get_Constants().GOODHIT_FRAC){
                    for(int k=-1; k<=1; k+=2 ) {
                        if(hiti.get_channel() == (k+hitj.get_channel())) {hitj.set_xtalk(1000+hiti.get_id()); if(debugMode==6)System.out.println(" E Xtalk "+hitj.get_xtalk());}
                    }
                }
            }
        }

        for(int iclu=0; iclu<allclusters.size(); iclu++) {
            if(allclusters.get(iclu).get_size()< tool.get_Constants().CLUSTER_MIN_SIZE) {
                RICHCluster clu = allclusters.get(iclu);
                if(debugMode==6)System.out.println("  Cluster "+ iclu +" ID "+clu.get_id());
                for(int ih = 0; ih< clu.size(); ih++) {
                    RICHHit hiti = clu.get(ih);

                    for(int jh = ih+1; jh< clu.size(); jh++) {
                        RICHHit hitj = clu.get(jh);
                        if(debugMode==6)System.out.println("Hit pair "+ih+" "+hiti.get_id()+" "+hiti.get_pmt()+" "+hiti.get_channel()+" "+hiti.get_duration()+" | " +jh+" "+hitj.get_id()+" "+hitj.get_pmt()+" "+hitj.get_channel()+" "+hitj.get_duration());

                        if(hitj.get_duration()*100 < hiti.get_duration()*tool.get_Constants().GOODHIT_FRAC) {hitj.set_xtalk(hiti.get_id()); if(debugMode==6)System.out.println(" O Xtalk "+hitj.get_xtalk());}

                    }
                }
            }
        }

    }

}
