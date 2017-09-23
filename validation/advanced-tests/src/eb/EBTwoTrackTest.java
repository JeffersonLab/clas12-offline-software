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

import org.jlab.analysis.math.ClasMath;

/**
 *
 * Analyze EB efficiencies based on Joseph's 2-particle test events.
 *
 * Need to write a more general purpose test based on MC::Particle bank.
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
    
    int nMisid = 0;
    int nMissing = 0;

    int nElectronsSector[]={0,0,0,0,0,0};
    int nHadronsSector[]={0,0,0,0,0,0};
    int hadronPDG = 0;
    int ftPDG = 0;

    DataBank mcBank=null,ctrkBank=null,calBank=null;
    DataBank trkBank=null,tofBank=null,htccBank=null,ltccBank=null;
    DataBank recPartBank=null,recTrkBank=null,recFtBank=null;
    DataBank recCalBank=null,recSciBank=null,recCheBank=null;
    DataBank ftcBank=null,fthBank=null,ftpartBank=null;

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
        }
        else udfFileType=true;

        HipoDataSource reader = new HipoDataSource();
        reader.open(fileName);

        while (reader.hasEvent()) {
            DataEvent event = reader.getNextEvent();
            getBanks(event);
            checkAllRefs(event);
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

    /**
     *
     * We're keeping all banks global to this class for now.
     *
     */
    private void getBanks(DataEvent de) {
        mcBank=null;ctrkBank=null;calBank=null;
        trkBank=null;tofBank=null;htccBank=null;ltccBank=null;
        recPartBank=null;mcBank=null;recTrkBank=null;recFtBank=null;
        recCalBank=null;recSciBank=null;recCheBank=null;
        ftcBank=null;fthBank=null;ftpartBank=null;
        if (de.hasBank("CVTRec::Tracks"))          ctrkBank=de.getBank("CVTRec::Tracks");
        if (de.hasBank("FTOF::clusters"))          tofBank=de.getBank("FTOF::clusters");
        if (de.hasBank("TimeBasedTrkg::TBTracks")) trkBank = de.getBank("TimeBasedTrkg::TBTracks");
        if (de.hasBank("REC::Particle"))           recPartBank = de.getBank("REC::Particle");
        if (de.hasBank("MC::Particle"))            mcBank = de.getBank("MC::Particle");
        if (de.hasBank("REC::Cherenkov"))          recCheBank = de.getBank("REC::Cherenkov");
        if (de.hasBank("REC::Calorimeter"))        recCalBank = de.getBank("REC::Calorimeter");
        if (de.hasBank("REC::Scintillator"))       recSciBank = de.getBank("REC::Scintillator");
        if (de.hasBank("LTCC::clusters"))          ltccBank = de.getBank("LTCC::clusters");
        if (de.hasBank("HTCC::rec"))               htccBank = de.getBank("HTCC::rec");
        if (de.hasBank("REC::Track"))              recTrkBank = de.getBank("REC::Track");
        if (de.hasBank("REC::ForwardTagger"))      recFtBank = de.getBank("REC::ForwardTagger");
        if (de.hasBank("FTCAL::clusters"))         ftcBank = de.getBank("FTCAL::clusters");
        if (de.hasBank("FTHODO::clusters"))        fthBank = de.getBank("FTHODO::clusters");
        if (de.hasBank("FT::particles"))           ftpartBank = de.getBank("FT::particles");
        if (de.hasBank("ECAL::clusters"))          calBank = de.getBank("ECAL::clusters");
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
        assertEquals(eEff>0.9,true);
        if      (hadronPDG==2212) assertEquals(pEff>0.77,true);
        else if (hadronPDG==321)  assertEquals(kEff>0.60,true);
        else if (hadronPDG==211)  assertEquals(piEff>0.75,true);
        else if (hadronPDG==22)   assertEquals(gEff>0.75,true);
    }
   
    private void checkResultsFT() {

        final double eEff = (double)nFtElectrons / nEvents;
        final double gEff = (double)nFtPhotons / nEvents;
        System.out.println(String.format("\n\neEff = %.3f",eEff));
        System.out.println(String.format("\n\ngEff = %.3f",gEff));
        if      (ftPDG==11) assertEquals(eEff>0.90,true);
        else if (ftPDG==22) assertEquals(gEff>0.90,true);
    }

    // This is for Forward Tagger;
    private void processEventFT(DataEvent event) {

        if (ftcBank!=null) {

            nEvents++;

            if (recPartBank!=null && recFtBank!=null) {
                for (int ii=0; ii<recFtBank.rows(); ii++) {
                    int irp = recFtBank.getInt("pindex",ii);
                    int pid = recPartBank.getInt("pid",irp);
                    if      (pid==22) nFtPhotons++;
                    else if (pid==11) nFtElectrons++;
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
            if (ctrkBank==null) return;
            if (ctrkBank.rows()==0) return;
            for (int ii=0; ii<ctrkBank.rows(); ii++) {
                if (ctrkBank.getInt("q",ii)>0) {
                    final double phi0 = ctrkBank.getFloat("phi0",ii);
                    final int sector = ClasMath.getSectorFromPhi(phi0);
                    if (sector == hadronSector)
                        nPosTracks++;
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
                
                final byte charge = recPartBank.getByte("charge", ii);
                final int pid = recPartBank.getInt("pid", ii);
                final double px=recPartBank.getFloat("px",ii);
                final double py=recPartBank.getFloat("py",ii);
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
