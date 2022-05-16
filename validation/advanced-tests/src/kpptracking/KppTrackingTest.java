package kpptracking;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;

/**
 *
 * @author naharrison
 */
public class KppTrackingTest {

    @Test
    public void testKppTracking() {

        HipoDataSource reader = new HipoDataSource();
        reader.open("out_twoTrackEvents_809.hipo");

        int totalCount = 0;
        int twoTrackCountCV = 0;
        int twoTrackCountAI = 0;
        while (reader.hasEvent()) {
            totalCount++;
            DataEvent event = reader.getNextEvent();
            if(this.checkTracks(event, "REC::Particle"))   twoTrackCountCV++;
            if(this.checkTracks(event, "RECAI::Particle")) twoTrackCountAI++;
        }
        reader.close();

        System.out.println("\nConventional tracking results:");
        System.out.println("\tTotal count: " + totalCount + "\n\t2-track count: " + twoTrackCountCV);
        System.out.println("\nAI tracking results:");
        System.out.println("\tTotal count: " + totalCount + "\n\t2-track count: " + twoTrackCountAI);

        assertEquals(twoTrackCountCV > 30, true); // this should be stricter!
        assertEquals(twoTrackCountAI > 30, true); // this should be stricter!

    }

    private boolean checkTracks(DataEvent event, String recBank) {
        boolean value = false;
        if (event.hasBank(recBank)) {
            DataBank bank = event.getBank(recBank);
            boolean foundElectron = false;
            boolean foundCharged = false;
            for (int k = 0; k < bank.rows(); k++) {
                byte charge = bank.getByte("charge", k);
                int pid = bank.getInt("pid", k);
                if (foundElectron == false && pid == 11) {
                    foundElectron = true;
                } else if (charge != 0) {
                    foundCharged = true;
                }
            }
            if (foundElectron && foundCharged) {
                value = true;
            }
        }
        return value;
    }

}
