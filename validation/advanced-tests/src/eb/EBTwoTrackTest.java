package eb;

import java.io.File;
import org.junit.Test;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import static org.junit.Assert.*;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;

import org.jlab.detector.base.DetectorType;
import org.jlab.clas.pdg.PDGDatabase;

import org.jlab.analysis.math.ClasMath;

/**
 *
 * Analyze EB efficiencies based on Joseph's 2-particle test events.
 *
 * TODO:  Inherit process/checkResults to subclasses for FD/CD/FT
 * TODO:  Write a more general purpose test based on MC::Particle bank.
 * TODO:  Rewrite this from scratch.  Meanwhile, can't live without it.
 * 
 * @author baltzell
 */
public class EBTwoTrackTest {

    static final boolean debug=false;
    
    // these correspond to Joseph's two-particle event generater:
    static final int electronSector=1;
    static final int hadronSector=3;

    boolean isForwardTagger=false;
    boolean isCentral=false;

    int fdCharge = 0;

    int nNegTrackEvents = 0;
    int nTwoTrackEvents = 0;
    
    int nEvents = 0;
    
    int eCount = 0;
    
    int eposCount = 0;
    int epiCount = 0;
    int ekCount = 0;
    int epCount = 0;
    int egCount = 0;
    int enCount = 0;

    int nFtPhotons = 0;
    int nFtElectrons = 0;
    int nFtFd = 0;
    
    int nMisid = 0;
    int nMissing = 0;

    int nElectronsSector[]={0,0,0,0,0,0};
    int nHadronsSector[]={0,0,0,0,0,0};
    int hadronPDG = 0;
    int ftPDG = 0;

    DataBank mcBank=null,ctrkBank=null,calBank=null,ctofBank=null;
    DataBank trkBank=null,tofBank=null,htccBank=null,ltccBank=null;
    DataBank recPartBank=null,recFtPartBank=null,recTrkBank=null,recFtBank=null;
    DataBank recCalBank=null,recSciBank=null,recCheBank=null;
    DataBank ftcBank=null,fthBank=null,ftpartBank=null,recBank=null,runBank=null;;

    Map <Integer,List<Integer>> recCalMap=new HashMap<Integer,List<Integer>>();
    Map <Integer,List<Integer>> recCheMap=new HashMap<Integer,List<Integer>>();
    Map <Integer,List<Integer>> recSciMap=new HashMap<Integer,List<Integer>>();
    Map <Integer,List<Integer>> recTrkMap=new HashMap<Integer,List<Integer>>();

    @Test
    public void main() {
        String fileName=System.getProperty("INPUTFILE");
        File file = new File(fileName);
        if (!file.exists() || file.isDirectory()) {
            System.err.println("Cannot find input file.");
            assertEquals(false, true);
        }

        // Strip off the "out_" prefix:
        String ss=fileName.replace("out_","");
        
        // Strip off the hipo suffix:
        ss=ss.replace(".hipo","");
        
        // test filename to determine data type:
        boolean udfFileType=false;
        if (ss.equals("electronproton"))  {
            hadronPDG=2212;
        }
        else if (ss.equals("electronpion")) {
            hadronPDG=211;
        }
        else if (ss.equals("electronkaon")) {
            hadronPDG=321;
        }
        else if (ss.equals("electrongamma")) {
            hadronPDG=22;
        }
        else if (ss.equals("electronneutron")) {
            hadronPDG=2112;
        }
        else if (ss.equals("electronFTgamma")) {
            isForwardTagger=true;
            hadronPDG=22;
            ftPDG=11;
        }
        else if (ss.equals("electronFTproton")) {
            isForwardTagger=true;
            hadronPDG=2212;
            ftPDG=11;
        }
        else if (ss.equals("electronFTkaon")) {
            isForwardTagger=true;
            hadronPDG=321;
            ftPDG=11;
        }
        else if (ss.equals("electronFTpion")) {
            isForwardTagger=true;
            hadronPDG=211;
            ftPDG=11;
        }
        else if (ss.equals("electronprotonC")) {
            isCentral=true;
            hadronPDG=2212;
        }
        else if (ss.equals("electronkaonC")) {
            isCentral=true;
            hadronPDG=321;
        }
        else if (ss.equals("electronpionC")) {
            isCentral=true;
            hadronPDG=211;
        }
        else if (ss.equals("electrongammaC")) {
            isCentral=true;
            hadronPDG=22;
        }
        else if (ss.equals("electronneutronC")) {
            isCentral=true;
            hadronPDG=2112;
        }
        else if (ss.equals("electrongammaFT")) {
            isForwardTagger=true;
            ftPDG=22;
            hadronPDG=11;
        }
        else udfFileType=true;

        fdCharge = PDGDatabase.getParticleById(hadronPDG).charge();

        HipoDataSource reader = new HipoDataSource();
        reader.open(fileName);

        while (reader.hasEvent()) {
            DataEvent event = reader.getNextEvent();
            getBanks(event);
            checkAllRefs(event);
            checkParticleStatus(event);
            if (!udfFileType) {
                if (isForwardTagger) processEventFT(event);
                else processEvent(event);
            }
        }
        reader.close();

        if (udfFileType) {
            System.out.println("");
            System.out.println("***********************************************************");
            System.out.println("EBTwoTrackTest does not know about this file.");
            System.out.println("  So we only did some bank index referencing tests.");
            System.out.println("***********************************************************");
            System.out.println("");
        }
        else {
            if (isForwardTagger) checkResultsFT();
            else checkResults();
        }
    }

    private DataBank getBank(DataEvent de,String bankName) {
        DataBank bank=null;
        if (de.hasBank(bankName))
            bank=de.getBank(bankName);
        return bank;
    }

    /**
     *
     * We're keeping all banks global to this class for now.
     *
     */
    private void getBanks(DataEvent de) {
        ctrkBank    = getBank(de,"CVTRec::Tracks");
        tofBank     = getBank(de,"FTOF::clusters");
        trkBank     = getBank(de,"TimeBasedTrkg::TBTracks");
        recPartBank = getBank(de,"REC::Particle");
        recFtPartBank = getBank(de,"RECFT::Particle");
        mcBank      = getBank(de,"MC::Particle");
        recCheBank  = getBank(de,"REC::Cherenkov");
        recCalBank  = getBank(de,"REC::Calorimeter");
        recSciBank  = getBank(de,"REC::Scintillator");
        ltccBank    = getBank(de,"LTCC::clusters");
        htccBank    = getBank(de,"HTCC::rec");
        recTrkBank  = getBank(de,"REC::Track");
        recFtBank   = getBank(de,"REC::ForwardTagger");
        ftcBank     = getBank(de,"FTCAL::clusters");
        fthBank     = getBank(de,"FTHODO::clusters");
        ftpartBank  = getBank(de,"FT::particles");
        calBank     = getBank(de,"ECAL::clusters");
        ctofBank    = getBank(de,"CTOF::hits");
        recBank     = getBank(de,"REC::Event");
        runBank     = getBank(de,"RUN::config");
        loadMaps();
    }
   
    /**
     *
     * Load mapping between banks.
     *
     */
    public void loadMap(Map<Integer,List<Integer>> map, 
                        DataBank fromBank, 
                        DataBank toBank, 
                        String idxVarName) {
        map.clear();
        if (fromBank==null) return;
        if (toBank==null) return;
        for (int ii=0; ii<fromBank.rows(); ii++) {
            final int iTo=fromBank.getInt(idxVarName,ii);
            if (map.containsKey(iTo)) {
                map.get(iTo).add(ii);
            }
            else {
                List<Integer> iFrom=new ArrayList<Integer>();
                map.put(iTo,iFrom);
                map.get(iTo).add(ii);
            }
        }
    }

    /**
     *
     * Load mapping from REC::Particle to REC::"Detector".
     *
     */
    public void loadMaps() {
        loadMap(recCalMap,recCalBank,recPartBank,"pindex");
        loadMap(recCheMap,recCheBank,recPartBank,"pindex");
        loadMap(recSciMap,recSciBank,recPartBank,"pindex");
        loadMap(recTrkMap,recTrkBank,recPartBank,"pindex");
    }

    /**
     *
     * Choose a referenced bank, based on "detector" from from a REC:: detector bank
     *
     * detId - a DetectorType id
     *
     */
    public DataBank getDetectorBank(int detId) {
        DataBank bankTo=null;
        if (detId == DetectorType.DC.getDetectorId())
            bankTo = trkBank;
        else if (detId == DetectorType.CVT.getDetectorId())
            bankTo = ctrkBank;
        else if (detId == DetectorType.ECAL.getDetectorId())
            bankTo = calBank;
        else if (detId == DetectorType.FTOF.getDetectorId())
            bankTo = tofBank;
        else if (detId == DetectorType.HTCC.getDetectorId())
            bankTo = htccBank;
        else if (detId == DetectorType.LTCC.getDetectorId())
            bankTo = ltccBank;
        else if (detId == DetectorType.FTCAL.getDetectorId())
            bankTo = ftcBank;
        else if (detId == DetectorType.FTHODO.getDetectorId())
            bankTo = fthBank;
        else
            throw new RuntimeException("Unkown detector Id:  "+detId);
        return bankTo;
    }
    
    /**
     *
     * Check that index references are within valid range
     *
     * bankFrom - bank containing the index
     * bankTo - bank to which the index refers
     * idxVarName - name of the index variable
     *
     */
    public boolean hasValidRefsToREC(DataEvent ev,
                        DataBank bankFrom,
                        DataBank bankTo,
                        String idxVarName) {
        for (int ii=0; ii<bankFrom.rows(); ii++) {
            int ref=bankFrom.getInt(idxVarName,ii);
            if (ref>=bankTo.rows() || ref<0) {
                System.err.println(String.format(
                        "\bnhasValidRefs: failed on (%s0>%s) %d->%d\n",
                        bankFrom.getDescriptor().getName(),
                        bankTo.getDescriptor().getName(),ii,ref));
                ev.show();
                bankFrom.show();
                bankTo.show();
                return false;
            }
        }
        return true;
    }

    /**
     *
     * Check that index references are within valid range
     *
     * bankFrom - bank containing the index
     * idxVarName - name of the index variable
     *
     * determine bankTo based on "detector" variable, row by row, in bankFrom
     *
     */
    public boolean hasValidRefsToDET(DataEvent ev,
                        DataBank bankFrom,
                        String idxVarName) {

        for (int ii=0; ii<bankFrom.rows(); ii++) {
            int ref=bankFrom.getInt(idxVarName,ii);
            int det=bankFrom.getInt("detector",ii);
            DataBank bankTo = getDetectorBank(det);
            if (ref>=bankTo.rows() || ref<0) {
                System.err.println(String.format(
                        "\bnhasValidRefs: failed on det=%d/%s (%s0>%s) %d->%d\n",
                        det,DetectorType.getType(det).getName(),
                        bankFrom.getDescriptor().getName(),
                        bankTo.getDescriptor().getName(),ii,ref));
                ev.show();
                bankFrom.show();
                bankTo.show();
                return false;
            }
        }
        return true;
    }

    /*
     *
     * Check that all from->to indices are within valid range
     *
     */
    public void checkAllRefs(DataEvent ev) {
        if (recPartBank!=null) {
            if (recCheBank!=null)
                assertEquals(true,hasValidRefsToREC(ev,recCheBank,recPartBank,"pindex"));
            if (recCalBank!=null)
                assertEquals(true,hasValidRefsToREC(ev,recCalBank,recPartBank,"pindex"));
            if (recSciBank!=null)
                assertEquals(true,hasValidRefsToREC(ev,recSciBank,recPartBank,"pindex"));
            if (recTrkBank!=null)
                assertEquals(true,hasValidRefsToREC(ev,recTrkBank,recPartBank,"pindex"));
            if (recFtBank!=null)
                assertEquals(true,hasValidRefsToREC(ev,recFtBank,recPartBank,"pindex"));
        }
        if (recTrkBank!=null) {
            if (trkBank!=null) {
                assertEquals(true,hasValidRefsToDET(ev,recTrkBank,"index"));
            }
        }
    }

    /*
     *
     * Check that REC::Particle.status agrees with REC::Detector banks.
     *
     */
    public void checkParticleStatus(DataEvent event) {
        if (recPartBank==null) return;
        for (int ipart=0; ipart<recPartBank.rows(); ipart++) {
            final int status = Math.abs(recPartBank.getShort("status",ipart));
            final boolean isFD = ((int)status/1000)==2;
            final int ncher = status%10;
            final int ncalo = (status%100  - ncher)/10;
            final int nscin = (status%1000 - ncalo*10 - ncher)/100;
            int mcher=0;
            int mcalo=0;
            int mscin=0;
            if (!isFD) continue;
            if (recCalMap.containsKey(ipart)) mcalo = recCalMap.get(ipart).size();
            if (recSciMap.containsKey(ipart)) mscin = recSciMap.get(ipart).size();
            // cherenkov is special case, requires 2 photoelectrons to be in status:
            if (recCheMap.containsKey(ipart)) {
                for (int iche : recCheMap.get(ipart)) {
                    if (recCheBank.getFloat("nphe",iche)>2) mcher++;
                }
            }
            if (mcalo!=ncalo || mscin!=nscin || mcher!=ncher) { recPartBank.show(); recCalBank.show(); recSciBank.show(); recCheBank.show(); }
            assertEquals(String.format("Cherenkov Count, Event >%d<",runBank.getInt("event",0)),ncher,mcher);
            assertEquals(String.format("Calorimeter Count, Event >%d<",runBank.getInt("event",0)),ncalo,mcalo);
            assertEquals(String.format("Scintillator Count, Event >%d<",runBank.getInt("event",0)),nscin,mscin);
        }
    }

    private void checkResults() {

        final double twoTrackFrac = (double)nTwoTrackEvents / nEvents;
       
        final double eEff = (double)eCount / nNegTrackEvents;
        final double pEff = (double)epCount / eposCount;
        final double piEff = (double)epiCount / eposCount;
        final double kEff  = (double)ekCount / eposCount;
        final double gEff  = (double)egCount / nNegTrackEvents;
        final double nEff  = (double)enCount / nNegTrackEvents;
        
        final double epEff = (double)epCount / nTwoTrackEvents;
        final double epiEff = (double)epiCount / nTwoTrackEvents;
        final double ekEff = (double)ekCount / nTwoTrackEvents;

        System.out.println("\n#############################################################");
        System.out.println(String.format("\n# Events = %d",nEvents));
        System.out.print("\nElectrons Sectors: ");
        for (int k=0; k<6; k++) System.out.print(String.format(" %4d",nElectronsSector[k]));
        System.out.print("\nHadrons   Sectors: ");
        for (int k=0; k<6; k++) System.out.print(String.format(" %4d",nHadronsSector[k]));
        System.out.println("\n");
        System.out.println(String.format("2-Track Frac = %.3f\n",twoTrackFrac));
        System.out.println(String.format("eEff         = %.3f",eEff));
        System.out.println(String.format("pEff         = %.3f",pEff));
        System.out.println(String.format("piEff        = %.3f",piEff));
        System.out.println(String.format("kEff         = %.3f\n",kEff));
        System.out.println(String.format("gEff         = %.3f",gEff));
        System.out.println(String.format("nEff         = %.3f\n",nEff));
        System.out.println(String.format("epEff        = %.3f",epEff));
        System.out.println(String.format("epiEff       = %.3f",epiEff));
        System.out.println(String.format("ekEff        = %.3f\n",ekEff));
        System.out.println(String.format("misid        = %.3f",(float)nMisid/eposCount));
        System.out.println(String.format("missing      = %.3f",(float)nMissing/eposCount));
        System.out.println("\n#############################################################");

        // some global efficiency tests:
        assertEquals(eEff>0.88,true);
        switch (hadronPDG) {
            case 2212:
                if (isCentral) assertEquals(pEff>0.77,true);
                else           assertEquals(pEff>0.77,true);
                break;
            case 321:
                if (isCentral) assertEquals(kEff>0.55,true);
                else           assertEquals(kEff>0.60,true);
                break;
            case 211:
                if (isCentral) assertEquals(piEff>0.72,true);
                else           assertEquals(piEff>0.75,true);
                break;
            case 22:
                if (isCentral) assertEquals(gEff>0.20,true);
                else           assertEquals(gEff>0.84,true);
                break;
            case 2112:
                if (isCentral) assertEquals(nEff>0.095,true);
                else           assertEquals(nEff>0.55,true);
                break;
            default:
                throw new RuntimeException("Not ready for pid="+hadronPDG);
        }
    }
   
    private void checkResultsFT() {

        final double eEff = (double)nFtElectrons / nEvents;
        final double gEff = (double)nFtPhotons / nEvents;
        final double hEff = (double)nFtFd / nEvents;
        System.out.println("\n#############################################################");
        System.out.print("\nFT Electrons:  "+nFtElectrons);
        System.out.print("\nHadrons   Sectors: ");
        for (int k=0; k<6; k++) System.out.print(String.format(" %4d",nHadronsSector[k]));
        System.out.println("\n");
        System.out.println(String.format("FT eEff = %.3f",eEff));
        System.out.println(String.format("FT gEff = %.3f",gEff));
        System.out.println(String.format("FD hEff = %.3f",hEff));
        System.out.println("#############################################################");
        if      (ftPDG==11) assertEquals(eEff>0.77,true);
        else if (ftPDG==22) assertEquals(gEff>0.88,true);
        assertEquals(hEff>0.62,true);
    }

    // This is for Forward Tagger;
    private void processEventFT(DataEvent event) {

        if (ftcBank==null) return;

        nEvents++;

        if (recBank==null || recPartBank==null || recFtBank==null || recFtPartBank==null) return;

        if (debug) {
            System.out.println("\n\n#############################################################\n");
            if (ftpartBank!=null) ftpartBank.show();
            recFtBank.show();
            recPartBank.show();
        }

        final float startTime=recBank.getFloat("startTime",0);

        for (int ii=0; ii<recFtPartBank.rows(); ii++) {
            if (Math.abs(recFtPartBank.getShort("status",ii))/1000 == 1) {
                switch (recFtPartBank.getInt("pid",ii)) {
                    case 11:
                        nFtElectrons++;
                        break;
                    case 22:
                        nFtPhotons++;
                        break;
                }
            }
        }

        for (int ii=0; ii<recFtPartBank.rows() && (startTime>0 || fdCharge==0); ii++) {
            final int pid = recFtPartBank.getInt("pid",ii);
            if (pid==hadronPDG) {
                final double px=recPartBank.getFloat("px",ii);
                final double py=recPartBank.getFloat("py",ii);
                final int sector = ClasMath.getSectorFromPhi(Math.atan2(py,px));
                nHadronsSector[sector-1]++;
                if (sector==hadronSector || (pid==11 && sector==electronSector)) {
                    nFtFd++;
                    break;
                }
            }
        }
    }

    private void processEvent(DataEvent event) {

        nEvents++;

        // no tracking bank, discard event:
        if (trkBank==null) return;

        // count number of TBTracks:
        int nPosTracks=0,nNegTracks=0;
        for (int ii=0; ii<trkBank.rows(); ii++) {

            // count negative tracks in electronSector:
            if (trkBank.getInt("sector",ii)==electronSector) {

                if (trkBank.getInt("q",ii)<0) nNegTracks++;
            }

            // count positive tracks in hadronSector
            else if (!isCentral && trkBank.getInt("sector",ii)==hadronSector) {

                if (trkBank.getInt("q",ii)>0) nPosTracks++;
            }
        }
        
        if (isCentral) {
            if (ctrkBank!=null && ctrkBank.rows()!=0) {
                if (debug) {
                    System.out.println("\n\n#############################################################\n");
                    if (recBank!=null) recBank.show();
                    if (ctofBank!=null) ctofBank.show();
                    if (ctrkBank!=null) ctrkBank.show();
                    if (recPartBank!=null) recPartBank.show();
                    if (recSciBank!=null) recSciBank.show();
                }
                for (int ii=0; ii<ctrkBank.rows(); ii++) {
                    if (ctrkBank.getInt("q",ii)>0) {
                        final double phi0 = ctrkBank.getFloat("phi0",ii);
                        final int sector = ClasMath.getSectorFromPhi(phi0);
                        if (sector == hadronSector)
                            nPosTracks++;
                    }
                }
            }
        }


        // no possible electron tracks, discard event:
        if (nNegTracks==0) return;

        nNegTrackEvents++;
        if (nPosTracks>0) nTwoTrackEvents++;

        boolean foundElectron = false;
        boolean foundHadron = false;
        boolean foundProton = false;
        boolean foundKaon = false;
        boolean foundPion = false;
        boolean foundPhoton = false;
        boolean foundNeutron = false;

        // check particle bank:
        if (recPartBank!=null) {

            for (int ii = 0; ii < recPartBank.rows(); ii++) {
               
                final short status = (short)Math.abs(recPartBank.getShort("status", ii));
                final byte charge  = recPartBank.getByte("charge", ii);
                final int pid      = recPartBank.getInt("pid", ii);
                final boolean isFT = status/1000 == 1;
                final boolean isFD = status/1000 == 2;
                final boolean isCD = status/1000 == 4;
                
                // determine sector 1-6:
                double px = recPartBank.getFloat("px",ii);
                double py = recPartBank.getFloat("py",ii);
                // use hit position for photons from CD (with UDF momentum):
                if (isCD && pid==22 && recSciMap.containsKey(ii)) {
                    px = recSciBank.getFloat("x",recSciMap.get(ii).get(0));
                    py = recSciBank.getFloat("y",recSciMap.get(ii).get(0));
                }
                final int sector = ClasMath.getSectorFromPhi(Math.atan2(py,px));

                if (pid==11 && sector==electronSector) {
                    if (!foundElectron) nElectronsSector[sector-1]++;
                    foundElectron=true;
                }
                else if (sector==hadronSector) {
                    if (pid==hadronPDG) {
                        if (!foundHadron) nHadronsSector[sector-1]++;
                        foundHadron=true;
                    }
                    if      (pid==2212) foundProton=true;
                    else if (pid==211)  foundPion=true;
                    else if (pid==321)  foundKaon=true;
                    else if (pid==22)   foundPhoton=true;
                    else if (pid==2112) foundNeutron=true;
                }
            }
        }

        // pid counting:
        if (foundElectron) {

            eCount++;

            if (foundPhoton) egCount++;
            if (foundNeutron) enCount++;

            if (nPosTracks>0) {

                eposCount++;

                if (foundProton) epCount++;
                if (foundKaon)   ekCount++;
                if (foundPion)   epiCount++;

                // FIXME
                if ( (hadronPDG==2212 && !foundProton) ||
                     (hadronPDG==321  && !foundKaon) ||
                     (hadronPDG==211  && !foundPion) ) {

                    if      (hadronPDG==2212 && (foundPion || foundKaon)) nMisid++;
                    else if (hadronPDG==321 && (foundProton || foundPion)) nMisid++;
                    else if (hadronPDG==211 && (foundProton || foundKaon)) nMisid++;
                    else {
                        nMissing++;
                        if (debug) {
                            recPartBank.show();
                            tofBank.show();
                        }
                    }
                }
            }
        }

        // CVT tracks could make this happen:
        //if (foundProton && nPosTracks==0) {
        //    System.err.println("WHAT");
        //}
    }

}
