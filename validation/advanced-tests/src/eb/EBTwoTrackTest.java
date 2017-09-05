package eb;

import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;

import org.jlab.analysis.math.ClasMath;

/**
 *
 * Analyze EB efficiencies based on Joseph's test events.
 *  - Two-particle (e-X) FD events
 *  - 1-electron FT events
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
    int hadronPDG;

    DataBank mcBank=null;
    DataBank trkBank=null,tofBank=null,htccBank=null,ltccBank=null,cheBank=null;
    DataBank recpartBank=null,rectrkBank=null,recftBank=null;
    DataBank ftcBank=null,fthBank=null,ftpartBank=null;

    @Test
    public void main() {
        String fileName=System.getProperty("INPUTFILE");
        File file = new File(fileName);
        if (!file.exists() || file.isDirectory()) {
            System.err.println("Cannot find input file.");
            assertEquals(false, true);
        }

        if      (fileName.indexOf("proton")>=0) hadronPDG=2212;
        else if (fileName.indexOf("pion")>0)    hadronPDG=211;
        else if (fileName.indexOf("kaon")>0)    hadronPDG=321;
        else if (fileName.indexOf("forwardtagger")>0) isForwardTagger=true;
        else {
            System.err.println("Cannot find particle type in filename.");
            assertEquals(false, true);
        }

        HipoDataSource reader = new HipoDataSource();
        reader.open(fileName);

        if (isForwardTagger) {
            processEventsFT(reader);
            checkResultsFT();
        }
        else {
            processEvents(reader);
            checkResults();
        }
    }

    private void getBanks(DataEvent de) {
        mcBank=null;
        trkBank=null;tofBank=null;htccBank=null;ltccBank=null;cheBank=null;
        recpartBank=null;mcBank=null;rectrkBank=null;recftBank=null;
        ftcBank=null;fthBank=null;ftpartBank=null;
        if (de.hasBank("FTOF::clusters"))          tofBank=de.getBank("FTOF::clusters");
        if (de.hasBank("TimeBasedTrkg::TBTracks")) trkBank = de.getBank("TimeBasedTrkg::TBTracks");
        if(de.hasBank("REC::Particle"))            recpartBank = de.getBank("REC::Particle");
        if(de.hasBank("MC::Particle"))             mcBank = de.getBank("MC::Particle");
        if(de.hasBank("REC::Cherenkov"))           cheBank = de.getBank("REC::Cherenkov");
        if (de.hasBank("LTCC::clusters"))          ltccBank = de.getBank("LTCC::clusters");
        if (de.hasBank("HTCC::rec"))               htccBank = de.getBank("HTCC::rec");
        if (de.hasBank("REC::Track"))              rectrkBank = de.getBank("REC::Track");
        if (de.hasBank("REC::ForwardTagger"))      recftBank = de.getBank("REC::ForwardTagger");
        if (de.hasBank("FTCAL::clusters"))         ftcBank = de.getBank("FTCAL::clusters");
        if (de.hasBank("FTHODO::clusters"))        fthBank = de.getBank("FTHODO::clusters");
        if (de.hasBank("FT::particles"))           ftpartBank = de.getBank("FT::particles");
    }

    private void checkResults() {

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
        else if (hadronPDG==321)  assertEquals(kEff>0.42,true);
        else if (hadronPDG==211)  assertEquals(piEff>0.75,true);
    }
    
    private void checkResultsFT() {

        assertEquals(false,true);
    }

    private void processEventsFT(HipoDataSource reader) {

        while (reader.hasEvent()) {

            nEvents++;
            
            DataEvent event = reader.getNextEvent();

            getBanks(event);

            if (mcBank!=null) mcBank.show();
            if (recpartBank!=null) recpartBank.show();
            if (recftBank!=null) recftBank.show();
            if (ftpartBank!=null) ftpartBank.show();
            if (ftcBank!=null) ftcBank.show();
        }
    }

    private void processEvents(HipoDataSource reader) {

        while (reader.hasEvent()) {
            
            nEvents++;
            
            DataEvent event = reader.getNextEvent();

            getBanks(event);

            //if (mcBank!=null) mcBank.show();

            // no tracking bank, discard event:
            if (trkBank==null) continue;

            //if (rectrkBank!=null) rectrkBank.show(); 

/*
            if (cheBank!=null) {
                for (int ii=0; ii<cheBank.rows(); ii++) {
                    if (cheBank.getInt("detector",ii)!=6) {
                        cheBank.show();
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
                if (cheBank!=null) cheBank.show();
                if (recpartBank!=null) recpartBank.show();
                System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
            }
*/
            // count number of TBTracks:
            int nPosTracks=0,nNegTracks=0;
            for (int ii=0; ii<trkBank.rows(); ii++) {
                if (trkBank.getInt("q",ii)<0 &&
                    trkBank.getInt("sector",ii)==electronSector) nNegTracks++;

                else if (trkBank.getInt("sector",ii)==hadronSector) {
                    if (hadronPDG==2212 && trkBank.getInt("q",ii)>0) nPosTracks++;
                    else if (trkBank.getInt("q",ii)<0) nPosTracks++;
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
            if (recpartBank!=null) {
                for(int ii = 0; ii < recpartBank.rows(); ii++) {

                    final byte charge = recpartBank.getByte("charge", ii);
                    final int pid = recpartBank.getInt("pid", ii);
                    final double px=recpartBank.getFloat("px",ii);
                    final double py=recpartBank.getFloat("py",ii);
                    final int sector = ClasMath.getSectorFromPhi(Math.atan2(py,px));

                    if (pid==11 && sector==electronSector) {
                        if (!foundElectron) nElectronsSector[sector-1]++;
                        foundElectron=true;
                    }
                    else if (sector==hadronSector) {
                        if (Math.abs(pid)==hadronPDG) {
                            if (!foundHadron) nHadronsSector[sector-1]++;
                            foundHadron=true;
                        }
                        if      (pid==2212)           foundProton=true;
                        else if (Math.abs(pid)==211)  foundPion=true;
                        else if (Math.abs(pid)==321)  foundKaon=true;
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
                                recpartBank.show();
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
