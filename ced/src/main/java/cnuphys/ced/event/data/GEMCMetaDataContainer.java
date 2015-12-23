package cnuphys.ced.event.data;

import java.util.List;
import java.util.Vector;

import org.jlab.coda.jevio.EvioNode;
import org.jlab.evio.clas12.EvioDataEvent;

import cnuphys.bCNU.util.FileUtilities;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.EvioNodeSupport;
import cnuphys.ced.frame.Ced;

public class GEMCMetaDataContainer extends ADataContainer {

    // if ((tag == 5) && (num == 1)) {

    public static final int GEMCMetaBankTag = 5;
    public static final int GEMCMetaBankNum = 1;

    // has this been use to set the fields?
    public boolean resetFields;

    // the raw strings
    public Vector<String> properties = new Vector<String>();

    public GEMCMetaDataContainer(ClasIoEventManager eventManager) {
	super(eventManager);
    }

    /**
     * Get the GCard, if any, that was in the meta data
     * 
     * @return the GCard, or null
     */
    public String getGCard() {
	return getProperty("GCARD");
    }

    /**
     * Check whether the torus is present
     * 
     * @return <code>true</code> if the torus was used to generate the gemc file
     */
    public boolean hasTorus() {
	return hasField("TORUS");
    }

    /**
     * Check whether the solenoid is present
     * 
     * @return <code>true</code> if the torus was used to generate the gemc file
     */
    public boolean hasSolenoid() {
	return hasField("SOLENOID");
    }

    private boolean hasField(String magnet) {
	String s = getProperty("ACTIVEFIELDS");
	if (s == null) {
	    return true;
	}

	s = s.toUpperCase();
	return s.contains(magnet.toUpperCase());
    }

    public double torusScaleFactor() {
	return scaleFactor("TORUS");
    }

    public double solenoidScaleFactor() {
	return scaleFactor("SOLENOID");
    }

    public double scaleFactor(String magnet) {
	double scale = 1.0;
	String valStr = scaleString(magnet);
	if (valStr != null) {
	    try {
		scale = Double.parseDouble(valStr);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	return scale;
    }

    private String scaleString(String magnet) {

	String valStr = null;
	magnet = magnet.toUpperCase();

	String s = getProperty("SCALE_FIELD").toUpperCase();
	if (s != null) {
	    if (s.contains(magnet)) {
		int index = s.indexOf(',');
		valStr = s.substring(index + 1);
	    }
	    else {
		s = getProperty("SCALE_FIELD_REPETITION_1");
		if (s != null) {
		    if (s.contains(magnet)) {
			int index = s.indexOf(',');
			valStr = s.substring(index + 1);
		    }
		}
	    }
	}
	return valStr;
    }

    /**
     * Get a property
     * 
     * @param key the key
     * @return the property, or null
     */

    private String getProperty(String key) {
	if (properties != null) {
	    for (int i = 0; i < properties.size() - 1; i = i + 2) {
		String tkey = properties.get(i);
		if (key.equalsIgnoreCase(tkey)) {
		    return properties.get(i + 1);
		}
	    }
	}
	return null;
    }

    @Override
    public void openedNewEventFile(String path) {
	clear();
    }

    @Override
    public void newClasIoEvent(EvioDataEvent event) {
	if (event != null) {
	    load(event);
	}
	finalEventPrep(event);
    }

    @Override
    public void clear() {
	properties.clear();
    }

    @Override
    public void load(EvioDataEvent event) {
	if (event == null) {
	    return;
	}

	EvioNode node = EvioNodeSupport.getNode(event, GEMCMetaBankTag,
		GEMCMetaBankNum);

	if (node != null) {

	    System.err.println("Found GEMC metadata bank.");
	    resetFields = true;

	    byte bytes[] = node.getStructureBuffer(true).array();

	    if (bytes != null) {
		String ss = new String(bytes);

		if (ss != null) {
		    String tokens[] = FileUtilities.tokens(ss, "\0");

		    if (tokens != null) {
			for (String s : tokens) {
			    s = s.trim();
			    if (s.startsWith("option ")) {
				s = s.substring(7);
				int index = s.indexOf(':');
				if (index > 0) {
				    String key = s.substring(0, index).trim()
					    .toUpperCase();
				    String val = s.substring(index + 1).trim();
				    // System.err.println("KEY: [" + key
				    // + "] VAL: [" + val + "]");

				    properties.addElement(key);
				    properties.addElement(val);
				}
			    }
			} // for s
		    } // tokens != null

		    Ced.getCed().getGEMCView().getGEMCTable().getGEMCMetaDataModel()
			    .setData(properties);
		} // ss not null
	    } // bytes not null
	} // node not null

    }

    @Override
    public int getHitCount(int option) {
	return (properties == null) ? 0 : properties.size();
    }

    @Override
    public void finalEventPrep(EvioDataEvent event) {
    }

    @Override
    public void addPreliminaryFeedback(int hitIndex, int option,
	    List<String> feedbackStrings) {
    }

    @Override
    public void addTrueFeedback(int hitIndex, int option,
	    List<String> feedbackStrings) {
    }

    @Override
    public void addDgtzFeedback(int hitIndex, int option,
	    List<String> feedbackStrings) {
    }

    @Override
    public void addFinalFeedback(int option, List<String> feedbackStrings) {
    }

    @Override
    public void addReconstructedFeedback(int option,
	    List<String> feedbackStrings) {
    }

}
