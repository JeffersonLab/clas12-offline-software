package org.jlab.service.ec;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jlab.io.base.DataEvent;

import org.jlab.analysis.physics.TestEvent;
import org.jlab.jnp.hipo4.data.SchemaFactory;
import org.jlab.logging.DefaultLogger;
import org.jlab.utils.system.ClasUtilsFile;

/**
 *
 * @author naharrison
 */
public class ECReconstructionTest {
	
  @Test
  public void testECReconstruction() {
    DefaultLogger.debug();

    System.setProperty("CLAS12DIR", "../../");

    String dir = ClasUtilsFile.getResourceDir("CLAS12DIR", "etc/bankdefs/hipo4");
    SchemaFactory schemaFactory = new SchemaFactory();
    schemaFactory.initFromDirectory(dir);
    
    DataEvent testEvent = TestEvent.getECSector1PhotonEvent(schemaFactory);
    
    ECEngine engineEC = new ECEngine();
    engineEC.init();
    engineEC.processDataEvent(testEvent);

    testEvent.show();
    testEvent.getBank("ECAL::hits").show();
    testEvent.getBank("ECAL::clusters").show();
    
    assertEquals(testEvent.hasBank("FAKE::Bank"), false);
    assertEquals(testEvent.hasBank("ECAL::clusters"), true);
    assertEquals(testEvent.getBank("ECAL::clusters").rows(), 3);    
  }

}
