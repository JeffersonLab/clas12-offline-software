package eb;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;

/**
 *
 */
public class EBSimpleTest {

    @Test
    public void testKppTracking() {

        HipoDataSource reader = new HipoDataSource();
        reader.open("out_electronproton.hipo");
        //reader.open("out_electronpion.hipo");
        //reader.open("out_electronkaon.hipo");

        int nEvents = 0;
        int twoTrackCount = 0;
        int epCount = 0;
        int eCount = 0;
        while(reader.hasEvent()) {
            nEvents++;
            DataEvent event = reader.getNextEvent();
            if(event.hasBank("REC::Particle")) {
                DataBank recBank = event.getBank("REC::Particle");
                boolean foundElectron = false;
                boolean foundCharged = false;
                boolean foundProton = false;
                for(int k = 0; k < recBank.rows(); k++) {
                    byte charge = recBank.getByte("charge", k);
                    int pid = recBank.getInt("pid", k);
                    if(foundElectron == false && pid == 11) foundElectron = true;
                    else if(charge != 0) foundCharged = true;
                    if (pid==2212) foundProton=true;
                }
                if(foundElectron && foundCharged) twoTrackCount++;
                if(foundElectron && foundProton) epCount++;
                if(foundElectron) eCount++;

            }
        }

        reader.close();

        double protonEff = (double)epCount / eCount;
        double epEff = (double)epCount / nEvents;
        double eEff = (double)eCount / nEvents;

        System.out.println("# Events = "+nEvents);
        System.out.println("eEff = "+eEff);
        System.out.println("pEff = "+protonEff);
        System.out.println("epEff = "+epEff);

//assertEquals(twoTrackCount > 100, true); // this should be stricter!

    }

}
