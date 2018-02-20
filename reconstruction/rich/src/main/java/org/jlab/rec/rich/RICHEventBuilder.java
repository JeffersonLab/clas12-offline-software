package org.jlab.rec.rich;

import java.util.ArrayList;
import java.util.List;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
//import org.jlab.rec.ft.cal.RICHConstantsLoader;

public class RICHEventBuilder{

    public int Neve = 0;
    // ----------------
    public RICHEventBuilder() {
    // ----------------

    }
 
    private RICHPMTReconstruction PMTreco;

    public int debugMode = 0;

    // ----------------
    public void init() {
    // ----------------
        if(debugMode>=1)  System.out.println("RICH Event Builder Initialization AA");
        PMTreco = new RICHPMTReconstruction();
    }
    
    // ----------------
    public void ProcessRawPMTData(DataEvent event) {
    // ----------------

        if(debugMode>=1){  
            System.out.println("---------------------------------\n");
            System.out.println("RICH Event Builder: Event Process "+Neve);
            System.out.println("---------------------------------\n");
        }

        List<RICHEdge>     allEdges      =    new ArrayList();
        List<RICHEdge>     Leads         =    new ArrayList();
        List<RICHEdge>     Trails        =    new ArrayList();

        List<RICHHit>      Hits          =    new ArrayList();
        List<RICHCluster>  AllClusters   =    new ArrayList();
        List<RICHCluster>  Clusters      =    new ArrayList();

        // get edges fron banks
        allEdges = PMTreco.initRICHPMT(event);

        // select good edges and order them
        Leads   = PMTreco.selectLeadEdges(allEdges);
        Trails  = PMTreco.selectTrailEdges(allEdges);

        // build hits
        Hits     = PMTreco.reconstructHits(Leads, Trails);
        AllClusters = PMTreco.findClusters(Hits);
        Clusters = PMTreco.selectGoodClusters(AllClusters);

        PMTreco.findXTalk(Hits, AllClusters);

        PMTreco.writeBanks(event, Hits, Clusters);
	
	Neve++;

    }


    // ----------------
    public void CosmicEvent(List<RICHHit> Hits, List<RICHCluster> Clusters) {
    // ----------------

      if(debugMode>=1)  System.out.println("RICH Event Builder: Event Process ");
  

    }

}
