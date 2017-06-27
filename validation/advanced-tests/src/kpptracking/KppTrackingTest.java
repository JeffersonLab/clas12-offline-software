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

		int recCount = 0;
		while(reader.hasEvent()) {
			DataEvent event = reader.getNextEvent();
			if(event.hasBank("REC::Particle")) recCount++;
		}

		reader.close();

		assertEquals(recCount > 100, true);
		
	}

}
