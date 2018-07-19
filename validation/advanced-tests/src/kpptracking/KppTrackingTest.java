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
		int twoTrackCount = 0;
		while(reader.hasEvent()) {
			totalCount++;
			DataEvent event = reader.getNextEvent();
			if(event.hasBank("REC::Particle")) {
				DataBank recBank = event.getBank("REC::Particle");
				boolean foundElectron = false;
				boolean foundCharged = false;
				for(int k = 0; k < recBank.rows(); k++) {
					byte charge = recBank.getByte("charge", k);
					int pid = recBank.getInt("pid", k);
					if(foundElectron == false && pid == 11) foundElectron = true;
					else if(charge != 0) foundCharged = true;
				}
				if(foundElectron && foundCharged) twoTrackCount++;
			}
		}

		reader.close();

		System.out.println("Total count: " + totalCount + ". 2 track count: " + twoTrackCount);

		assertEquals(twoTrackCount > 30, true); // this should be stricter!
		
	}

}
