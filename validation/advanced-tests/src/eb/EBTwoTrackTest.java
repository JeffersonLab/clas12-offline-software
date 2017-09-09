package eb;

import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;

import org.jlab.detector.base.DetectorType;

import org.jlab.analysis.math.ClasMath;

/**
 *
 * Analyze EB efficiencies based on Joseph's test events.
 *  - Two-particle (e-hadron) FD events
 *  - 1-electron FT events
 *  - etc.
 *
 * Note, hadron is assumed to have positive charge.
 *
 * Need to write a more general purpose one based on MC::Particle bank.
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
    boolean udfFileType=true;

    int nNegTrackEvents = 0;
    int nTwoTrackEvents = 0;
    int nEvents = 0;
    int epCount = 0;
    int eCount = 0;
    int eposCount = 0;
    int epiCount = 0;
    int ekCount = 0;
    int nMisid = 0;
    int nMissing = 0;
    int nElectronsSector[]={0,0,0,0,0,0};
    int nHadronsSector[]={0,0,0,0,0,0};
    int hadronPDG = 0;

    DataBank mcBank=null,ctrkBank=null,calBank=null;
    DataBank trkBank=null,tofBank=null,htccBank=null,ltccBank=null;
    DataBank recPartBank=null,recTrkBank=null,recFtBank=null;
    DataBank recCalBank=null,recSciBank=null,recCheBank=null;
    DataBank ftcBank=null,fthBank=null,ftpartBank=null;

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
        if (ss.equals("electronproton"))  {
            hadronPDG=2212;
            udfFileType=false;
        }
        else if (ss.equals("electronpion")) {
            hadronPDG=211;
            udfFileType=false;
        }
        else if (ss.equals("electronkaon")) {
            hadronPDG=321;
            udfFileType=false;
        }
        else if (ss.equals("electronFTgamma")) {
            isForwardTagger=true;
            hadronPDG=22;
            udfFileType=false;
        }
        else if (ss.equals("electronFTproton")) {
            isForwardTagger=true;
            hadronPDG=2212;
        }
        else if (ss.equals("electronFTkaon")) {
            isForwardTagger=true;
            hadronPDG=321;
        }
        else if (ss.equals("electronFTpion")) {
            isForwardTagger=true;
            hadronPDG=211;
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
        else if (ss.equals("electrongammaFT")) {
        }

        HipoDataSource reader = new HipoDataSource();
        reader.open(fileName);

        if (!udfFileType) {
            if (isForwardTagger) {
                processEventsFT(reader);
                checkResultsFT();
            }
            else if (isCentral) {
                processEvents(reader);
            }
            else {
                processEventsFD(reader);
                checkResultsFD();
            }
        }
        else processEvents(reader);
    }

    /*
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

    private void checkResultsFD() {

        final double twoTrackFrac = (double)nTwoTrackEvents / nEvents;
       
        final double eEff = (double)eCount / nNegTrackEvents;
        final double pEff = (double)epCount / eposCount;
        final double piEff = (double)epiCount / eposCount;
        final double kEff  = (double)ekCount / eposCount;
        
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
    }
   
    private void checkResultsFT() {

        final double eEff = eCount / nEvents;
        System.out.println(String.format("\n\neEff = %.3f",eEff));
        assertEquals(eEff>0.90,true);
    }

    // This only tries to check pointers between banks:
    private void processEvents(HipoDataSource reader) {

        while (reader.hasEvent()) {
            DataEvent event = reader.getNextEvent();
            getBanks(event);
            checkAllRefs(event);
        }
        System.out.println("");
        System.out.println("***********************************************************");
        System.out.println("EBTwoTrackTest does not know about this file.");
        System.out.println("  So we only did some bank index referencing tests.");
        System.out.println("***********************************************************");
        System.out.println("");
    }

    // This is for Forward Tagger;
    private void processEventsFT(HipoDataSource reader) {

        while (reader.hasEvent()) {
            
            DataEvent event = reader.getNextEvent();
            getBanks(event);
            checkAllRefs(event);

            if (ftcBank!=null) {

                nEvents++;
           
                if (recPartBank!=null && recFtBank!=null) {

                    // TODO:  add check on pid
                    eCount++;
                }
            }
        }
    }

    // This is for Forward Detectors (DC/EC/FTOF/HTCC/LTCC):
    private void processEventsFD(HipoDataSource reader) {

        while (reader.hasEvent()) {
            
            nEvents++;
            
            DataEvent event = reader.getNextEvent();
            getBanks(event);
            checkAllRefs(event);

            //if (mcBank!=null) mcBank.show();

            // no tracking bank, discard event:
            if (trkBank==null) continue;

            //if (recTrkBank!=null) recTrkBank.show(); 

/*
            if (recCheBank!=null) {
                for (int ii=0; ii<recCheBank.rows(); ii++) {
                    if (recCheBank.getInt("detector",ii)!=6) {
                        recCheBank.show();
                        break;
                    }
                }
            }
*/
/*
            if (ltccBank!=null) {
                System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                event.getBank("LTCC::clusters").show();
                if (htccBank!=null) htccBank.show();
                if (recCheBank!=null) recCheBank.show();
                if (recPartBank!=null) recPartBank.show();
                System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
            }
*/
            // count number of TBTracks:
            int nPosTracks=0,nNegTracks=0;
            for (int ii=0; ii<trkBank.rows(); ii++) {

                // count negative tracks in electronSector:
                if (trkBank.getInt("sector",ii)==electronSector) {

                    if (trkBank.getInt("q",ii)<0) nNegTracks++;
                
                }

                // count positive tracks in hadronSector
                else if (trkBank.getInt("sector",ii)==hadronSector) {

                    if (trkBank.getInt("q",ii)>0) nPosTracks++;

                }
            }

            // no possible electron tracks, discard event:
            if (nNegTracks==0) continue;
            
            nNegTrackEvents++;
            if (nPosTracks>0) nTwoTrackEvents++;

            boolean foundElectron = false;
            boolean foundHadron = false;
            boolean foundProton = false;
            boolean foundKaon = false;
            boolean foundPion = false;

            // check particle bank:
            if (recPartBank!=null) {

                /*
                // check references:
                if (recCheBank!=null)
                    assertEquals(true,hasValidRefs(event,recCheBank,recPartBank,"pindex"));
                if (recCalBank!=null)
                    assertEquals(true,hasValidRefs(event,recCalBank,recPartBank,"pindex"));
                if (recSciBank!=null)
                    assertEquals(true,hasValidRefs(event,recSciBank,recPartBank,"pindex"));
                if (recTrkBank!=null) {
                    assertEquals(true,hasValidRefs(event,recTrkBank,recPartBank,"pindex"));
                    assertEquals(true,hasValidRefs(event,recTrkBank,trkBank,"index"));
                }
                */

                for(int ii = 0; ii < recPartBank.rows(); ii++) {

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
                    }

                }
            }

            // pid counting:
            if (foundElectron) {

                eCount++;

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
        reader.close();
    }

}
