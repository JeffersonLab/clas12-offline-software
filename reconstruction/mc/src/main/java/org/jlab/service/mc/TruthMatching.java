package org.jlab.service.mc;

import org.jlab.clas.reco.ReconstructionEngine;

import org.jlab.detector.base.DetectorType;


import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;

import java.util.*;

// ----------------------------------------------------------------------------
// Truth matching:
// the purpose of this class is to check if a MC::Particle has crossed enough
// detector elements to be reconstructable, i.e. this should allow to study 
// detector acceptances and reconstruction algorithm efficiencies
//
// IMPORTANT: Gemc simulations need to contain MC::True information
//
// TODO: for the moment, the truth matching is performed *only* for charged
// particles. Neutrals matching, i.e. gamma and neutrons, still missing.
// ----------------------------------------------------------------------------

public class TruthMatching extends ReconstructionEngine {
    public int debug;

    public TruthMatching() {
        super("TruthMatching", "fbossu", "0.0");
        debug = 0;
    }

  public boolean init() { return true; } 

  @Override
  public boolean processDataEvent(DataEvent event) {

    //System.out.print( " ******* EVENT " + event.getBank("RUN::config").getInt("event",0));
    // check if the event contains the MC banks
    if( event.hasBank( "MC::True") == false ){
      System.err.print(" [ WARNING, TruthMatching ]: no MC::True bank found" );
      if( event.hasBank("RUN::config") ) 
        System.err.print( " ******* EVENT " + event.getBank("RUN::config").getInt("event",0));
      System.err.println();
      return false; 
    }

    if( event.hasBank( "MC::Particle") == false ){
      System.err.println(" [ WARNING, TruthMatching ]: no MC::Particle bank found" );
      return false; 
    }

    // load banks
    // ----------
    Map<Short, MCpart> mcp = getMCparticles( event.getBank( "MC::Particle") );
    if( mcp != null ) {
      //for( short key : mcp.keySet() ){
        //System.out.println( "mcp: " + key + " " + mcp.get(key).pid );
      //}
    }
    else { System.err.println( "[ ERROR, TruthMatching ]: problems loading MC::Particle bank "); }

    Map<Byte, Map<Integer, MChit>> mchits = getMCHits( event.getBank( "MC::True"), mcp );
    if( mchits == null ){
      System.err.println(" [ WARNING, TruthMatching ]: no MC hits found" );
      return false; 
    }


    // Central Detectors
    // --------------------------------------------------------

    // load BMT hits
    Map< Integer, List<RecHit> > bmthits = getRecHits( event, DetectorType.BMT );
    // load BMT clusters
    List<RecCluster> bmtcls = getRecClusters(event, DetectorType.BMT );
    // match BMT clusters to BMT generated hits
    matchClusters( bmtcls, bmthits, mchits.get( (byte)DetectorType.BMT.getDetectorId() ) );


    // same thing for BST
    Map< Integer, List<RecHit> > bsthits = getRecHits( event, DetectorType.BST );
    List<RecCluster> bstcls = getRecClusters(event, DetectorType.BST );
    matchClusters( bstcls, bsthits, mchits.get( (byte)DetectorType.BST.getDetectorId() ) );


    // Forward Detectors
    // --------------------------------------------------------

    // since DC clusters have not the same meaning as BST/BMT clusters,
    // the matching is performed only on hit base.
    // for this reason, we decided to use the HBhits as RecClusters
    List<RecCluster> dccls = getRecClusters(event, DetectorType.DC );
    // HBhits are matched to MC true hits
    matchClusters( dccls, null, mchits.get( (byte)DetectorType.DC.getDetectorId() ) );
    // since some HBhits may not be used in TB tracking, here we check if the HBhits has been used
    // in a TB track 
    checkIfHBhitIsUsedInTB( event, dccls );

    
    // put together all rec clusters
    // -----------------------------
    List<RecCluster> allcls = new ArrayList<RecCluster>();
    if( bmtcls != null ) allcls.addAll( bmtcls );
    if( bstcls != null ) allcls.addAll( bstcls );
    if( dccls  != null ) allcls.addAll( dccls );

    
    // map the RecClusters to the MC particle that generated the hits
    // --------------------------------------------------------------
    Map<Short,List<RecCluster>> clsPerMCp = mapClustersToMCParticles( mcp.keySet(), allcls );


    //check if the event contains the Rec Track bank
    // we need this to make the link to the Rec Particle
    // --------------------------------------------------------------
    Map<Integer, RecTrack> rectrk = null;
    if( event.hasBank( "REC::Track") != false ){
      rectrk = getRecTracks( event.getBank( "REC::Track") );
    }


    // count how many clusters/hits have been used in the reconstructed track
    // here, we implement the minimal requirements to check if the MC particle has been reconstructed or nor 
    // --------------------------------------------------------------
    List<MCRecMatch> mmm = findMCRecMatch( mcp, clsPerMCp, rectrk );


    // write to the bank
    // --------------------------------------------------------------
    bankWriter( event, mmm, allcls );

    return true;
  }

  // ======================================
  // Definition of objects
  // ====================================== 

  class MCpart {
    // object to load the MC::Particle bank
    public int   id;      // index of the MC particle (it should correspond of tid in MC::True)
    public int   pid;     // PDG ID code
  }

  class MChit {
    // object to load the MC::Ture bank
    public int  pid;      // MC particle id (pdg code)
    public int  tid;      // MC track id
    public int  hitn;     // Hit id: it corresponds to the position of rec hits.
    public byte detector; // Detector code descriptor
  }


  class MCRecMatch {
    public MCRecMatch() {
      id = -1;
      nclusters = -1;
      nClsInRec = -1;
      frac = (float)-1.0;
      tid  = -1;
    }
    public short id;        // MC particle id
    public short nclusters; // number of matched clusters
    public short nClsInRec; // number of clusters used in reconstruction
    public float frac;      // fraction of clusters used
    public short tid;       // reconstructed track id
    public byte  reconstructable;       // reconstructed track id

  }

  class RecHit {
    public int id;    // ID
    public int tid;   // trkID
    public int cid;   // clusterID
  }

  class RecTrack {
    public int id;        // position in the bank
    public int detector;
    public int index;    // row number in the detector track bank. not the ID of the track,
    public int pindex;
  }

  class RecCluster {
    public RecCluster() {
      nHitMatched = 0;
      mctid = -1;
    }
    public short id;            // cluster id
    public short mctid;         // mc track id
    public short rectid;        // rec track id
    public short nHitMatched;   // number of hits MC matched
    public short size;          // number of hits
    public byte  detector;
    public byte  layer;
    public byte  superlayer;
    public byte  sector;
  }

  // ======================================
  // ====================================== 

  // decide if a MC particle has been reconstructed or not
  // inputs: 
  //   - dictionary of MC particles
  //   - dictionary of cluster matched to mc particles
  //   - dictionary of rec tracks. keys are 100*DetID + trID + 1
  // -----------------------------------------------------
  List<MCRecMatch> findMCRecMatch( Map<Short,MCpart> mcp, Map<Short,List<RecCluster>> clsPerMCp , Map<Integer, RecTrack> trk ){
    //if( clsPerMCp == null ) return null;

    List<MCRecMatch> matchs = new ArrayList<MCRecMatch>();

    // loop over the id of the mc particle
    for( short i : mcp.keySet() ){

      MCRecMatch m = new MCRecMatch();

      // get the list of clusters associated to this mc particle
      m.id = (short)mcp.get(i).id ;
      if( clsPerMCp.get(i) != null ){
        m.nclusters = (short)clsPerMCp.get(i).size();

        short rectid = -1;
        int nmatch = 0;
        short nUsedInRec = 0;
        byte detector = -1;
        
        // counters for bmt superlayers, bst, bmtz, bmtc
        Map<Byte,Integer> countSL = new HashMap<Byte,Integer>();
        for( byte l = 1; l <= 6; l++ ) countSL.put( l, 0 );
        
        int countBST = 0;
        int countBMTC = 0;
        int countBMTZ = 0;
        byte BMTsector = 0;
        
        // check if the cluster has been used in the reconstructed track
        for( RecCluster c : clsPerMCp.get(i) ){
          if( c.nHitMatched > 0 ) {
            nmatch++;
            if( c.detector == (byte) DetectorType.DC.getDetectorId() ){
              countSL.put( ((byte)c.superlayer),  countSL.get( ((byte)c.superlayer) ) + 1 );
            }

            if( c.detector == (byte) DetectorType.BMT.getDetectorId() ){ 
              if( BMTsector == 0 ) BMTsector = c.sector; // assign the sector of the first cluster
              if( c.sector == BMTsector ){ // check if they are in the same sector
                if( c.layer == 1 || c.layer == 4 || c.layer == 6 ) countBMTC++;
                else countBMTZ++;
              }
            }
            if( c.detector == (byte) DetectorType.BST.getDetectorId() ){ countBST++; }
            
          }
          rectid = c.rectid;
          detector = c.detector;
          if( c.rectid >=0 ) {
             nUsedInRec++;      // count +1 if the cluster is used in rec
          }
        }

        // unfortunately the rec id in the cluster banks do not correspond to the REC::Track id
        // so, here we make the matching to the rec track
        if( trk != null && rectid >= 0 ){
          if( detector == (byte) DetectorType.DC.getDetectorId() ){
            rectid = (short)trk.get( 100*detector + rectid ).id; 
          }
          else if( detector == (byte) DetectorType.BMT.getDetectorId() || detector == (byte) DetectorType.BST.getDetectorId() ){
            rectid = (short)trk.get( 100*5 + rectid ).id;
          }
          else {
            rectid = -1;
          }
          m.tid = rectid;
        }

        // compute the fraction of clusters used in reconstruction
        m.frac = ((float)nUsedInRec) / m.nclusters;
    
        m.nClsInRec = nUsedInRec;

        // Implement minimal tracking requirements to decide if the track could have been reconstructed or not

        if( detector == (byte) DetectorType.DC.getDetectorId() ){
          int testSL = 0;
          for( byte l = 1; l <= 6; l++ ){ 
            if( countSL.get( l ) >= 4 ) testSL++;  // per SuperLayer at least 5 out of 6 layers need to be fired
          }
          if( testSL >= 5 ){  // at least 5 out of 6 SuperLayers need to be fired
            m.reconstructable = 1; 
            //System.out.println( " Yeah! DC track found! " );
          }
        }
        if( detector == (byte) DetectorType.BMT.getDetectorId() || detector == (byte) DetectorType.BST.getDetectorId() ){
          // a track in CVT can be reconstructed if it has:
          if( 
              countBST >= 2 &&                   // at least a cross in SVT
              countBMTZ >= 1 &&                  // at least a cluster in BMTZ
              (countBST/2 + countBMTZ) >= 3  &&  // at least 3 (x,y) crosses, i.e. 2 SVT and 1 BMT or 1 SVT and 2 BMT
              countBMTC >= 2                     // at least 2 clusters in BMTC
            ){
 
            m.reconstructable = 1; 
            //System.out.println( " Yeah! CVT track found! " );
          }
        }

      }
      matchs.add( m );
    }
    return matchs;
  }

 
  // load MC particle
  // -----------------------------------------------------------
  Map<Short, MCpart> getMCparticles( DataBank mcpart ){
    Map<Short, MCpart> mcp = new HashMap<Short,MCpart>();
    for( short i = 0; i < mcpart.rows(); i++ ) {
      MCpart p = new MCpart();
      p.id = i+1;
      p.pid = mcpart.getInt( "pid", i );
      
      mcp.put( (short)(i+1), p );
    }
    return mcp;
  }

  // load MC:True hits that correspond to MC::Particle
  // it returns a dictionary of hits per detector
  // -----------------------------------------------------------
  Map<Byte,Map<Integer, MChit>> getMCHits( DataBank mctrue, Map<Short,MCpart> mcp  ){

    Map<Byte,Map<Integer, MChit >> dmchits = new HashMap<Byte,Map<Integer, MChit>>();
    

    for( int i = 0; i < mctrue.rows(); i++ ){
      MChit hit = new MChit();
      hit.pid      = mctrue.getInt( "pid", i );
      hit.tid      = mctrue.getInt( "tid", i );
      hit.hitn     = mctrue.getInt( "hitn", i );
      hit.detector = mctrue.getByte( "detector", i );

      if( mcp.get( (short) hit.tid ) == null ) continue;

      if( dmchits.get( hit.detector ) == null ){
        dmchits.put( hit.detector , new HashMap<Integer,MChit>() );
      }
      dmchits.get( hit.detector ).put( hit.hitn ,  hit );

    }
    return dmchits;
  } 

  // Specific for DC:
  // check if a HB hit has been used in the TB tracking
  // Reminder: for DC a RecCluster is actually a HB hit
  // -----------------------------------------------------
  void checkIfHBhitIsUsedInTB( DataEvent event, List<RecCluster> dccls){
   
    if( dccls == null || dccls.size()==0 ) return; 
    // load TBHits
    // map of hit id, TB rec track id
    Map<Short,Byte> tbhit = new HashMap<Short,Byte>();
    
    if( event.hasBank( "TimeBasedTrkg::TBHits" ) ){
      DataBank tbbank = event.getBank( "TimeBasedTrkg::TBHits" );

      for( int i=0;i<tbbank.rows();i++){
        tbhit.put( tbbank.getShort( "id", i ), tbbank.getByte( "trkID",i ) );
      }
    }

    // check if HB hit became a TB hit
    for( RecCluster c : dccls ){
      if( tbhit.get( c.id ) != null ){
        // yes, it has been used in TB tracking
        c.rectid = tbhit.get(c.id);
      }
      else c.rectid = -1;
    }

  }
  // map clusters to MC particle
  // this method returns a map of < mc particle ids, list of clusters >
  // -------------------------------------------------------------------
  Map<Short,List<RecCluster>> mapClustersToMCParticles( Set<Short> mcpKeys, List<RecCluster> cls ){
    Map<Short,List<RecCluster>> map = new HashMap<Short,List<RecCluster>>();
    for( short i : mcpKeys ){
      map.put( i, new ArrayList<RecCluster>() );
    }

    for( RecCluster c : cls ){
      if( map.get( c.mctid ) == null ) map.put( c.mctid,new ArrayList<RecCluster>() );
      map.get( c.mctid ).add( c );
    }
    return map;
  }

  // match clusters:
  // inputs are: list of clusters, map of <cluster,rec hits>, map of mc hits
  // this method checks if a cluster has some rec hits that match with mc hits
  // if so, it sets the mc track id to the one of the corresponding hits
  // -----------------------------------------------------------------------
  void matchClusters( List<RecCluster> cls, Map<Integer,List<RecHit>> rechits, Map<Integer,MChit> mchits ){
    if( cls == null ) return;
 
    for( RecCluster cluster : cls ){

      // get corresponding hits
      if( cluster.detector != DetectorType.DC.getDetectorId() ) {
        List<RecHit> hits = rechits.get( (int) cluster.id );
        if( hits == null ){
          if( this.debug > 2 ) System.out.println( "cluster " + cluster.id + " no rec hit found");
          continue;
        }
        if( mchits == null ){
          if( this.debug > 2 )System.out.println( "cluster " + cluster.id + " not matched");
          continue;
        }

        for( RecHit h : hits ){
          if( mchits.get(h.id) != null ){
            cluster.nHitMatched++;
            if( cluster.mctid > 0 ){
              if( cluster.mctid != mchits.get(h.id).tid )
                if( this.debug > 0 )System.out.println( " WARNING: detector " + cluster.detector + " cluster id " + cluster.id + 
                                    "; mc track changed from " + cluster.mctid + " to " + mchits.get(h.id).tid );
            }
            cluster.mctid = (short) mchits.get(h.id).tid;
          }
        }

      }
      else { // it is a DC hit
          if( mchits != null &&  mchits.get((int)cluster.id) != null ){
            cluster.nHitMatched = 1;
            cluster.mctid = (short) mchits.get((int)cluster.id).tid;
          }
          //else System.out.println(cluster.id + " AAAA" );
        
      }
    }
  }
   
  // read the rec clusters per detector
  // retruns a list of clusters
  // -----------------------------------
  List<RecCluster> getRecClusters( DataEvent event, DetectorType det ){
    List<RecCluster> cls = new ArrayList<RecCluster>();

    String bankName = new String("Rec::Clusters");
    switch( det ){
      case BMT:
        bankName = "BMT" + bankName;
        break;
      case BST:
        bankName = "BST" + bankName;
        break;
      case DC:
        bankName = "HitBasedTrkg::HBHits";
        break;
      default:
        System.err.println(" detector name not correct ");
        break;
    }

    if( event.hasBank( bankName ) == false ){
      //System.err.println(" [ WARNING, TruthMatching ]: no " + bankName + " bank found, " + det.getName() );
      return null; 
    }
   
    DataBank bank = event.getBank( bankName );

    // reminder: CVT and DC banks do not have exactly the same structure nor the same row names
    for( int i = 0; i < bank.rows(); i++ ){
      RecCluster h = new RecCluster();
      h.id       = bank.getShort( (DetectorType.DC == det )? "id" : "ID" , i );
      h.rectid   = (DetectorType.DC == det ) ? bank.getByte( "trkID", i )     :  bank.getShort( "trkID", i );
      h.size     = (DetectorType.DC == det ) ?           1                    :  bank.getShort( "size",  i ); // size is meaningless for DC, set to 1
      h.detector = (byte) det.getDetectorId();
      h.layer    = bank.getByte( "layer", i );
      h.superlayer = (DetectorType.DC == det ) ? bank.getByte( "superlayer", i ) : -1;
      h.sector   = (byte) bank.getByte( "sector", i );
      cls.add( h );
    }
    return cls;
  }


  // read the rec hits per detector
  // retruns a map of hits per cluster
  // -----------------------------------
  Map< Integer, List<RecHit> > getRecHits( DataEvent event, DetectorType det ){
    
    Map< Integer, List<RecHit> > hits = new HashMap< Integer, List<RecHit> >();
    
    String bankName = new String("Rec::Hits");
    switch( det ){
      case BMT:
        bankName = "BMT" + bankName;
        break;
      case BST:
        bankName = "BST" + bankName;
        break;
      default:
        System.err.println(" detector name not correct ");
        break;
    }

    if( event.hasBank( bankName ) == false ){
      if( this.debug > 1 )System.err.println(" [ WARNING, TruthMatching ]: no " + bankName + " bank found, " + det.getName() );
      return null; 
    }
   
    DataBank bank = event.getBank( bankName );

    for( int i = 0; i < bank.rows(); i++ ){
      RecHit h = new RecHit();
      h.id   = bank.getShort( "ID" , i );
      h.tid  = bank.getShort( "trkID", i );
      h.cid  = bank.getShort( "clusterID", i );
      
      if( hits.get( h.cid ) == null ){
        hits.put( h.cid, new ArrayList<RecHit>() );
      }
      hits.get( h.cid ).add( h );
    }

    return hits;
  }

  //
  Map< Integer, RecTrack >  getRecTracks( DataBank bank ){
    Map< Integer, RecTrack > trks = new HashMap< Integer, RecTrack >();
    for( int i = 0; i < bank.rows(); i++ ){
      RecTrack t = new RecTrack();
      t.id = i;
      t.detector = bank.getInt( "detector", i );
      t.index    = bank.getInt( "index", i );
      t.pindex   = bank.getInt( "pindex", i );
      
      trks.put( t.detector * 100 + t.index + 1, t );
    }
    return trks;
  }


  // write to bank
  // -------------  
  void bankWriter( DataEvent event, List<MCRecMatch> mcp, List<RecCluster> cls ) {

    DataBank bank  = event.createBank("MC::IsParticleMatched", mcp.size()  );
    
    for(  int j=0; j < mcp.size(); j++ ){
      MCRecMatch p = mcp.get(j);
      bank.setShort( "mcTindex",       j, p.id );
      bank.setShort( "recTindex",      j, p.tid );
      bank.setShort( "nMCclusters",    j, (short)p.nclusters );
      bank.setByte(  "isInAcc",        j, (byte)p.reconstructable );
      bank.setFloat( "fraction",       j, p.frac );
    }

    event.appendBanks( bank );

    DataBank clsbank  = event.createBank("MC::MatchedClusters", cls.size()  );
    for( int j = 0; j < cls.size(); j++ ){
      RecCluster c = cls.get(j);
      clsbank.setShort( "index",      j ,  c.id );
      clsbank.setByte(  "detector",   j ,  c.detector );
      clsbank.setShort( "recTindex",  j ,  c.rectid );
      clsbank.setShort( "mcTindex",   j ,  c.mctid );
    }
    event.appendBanks( clsbank );
  }

}
