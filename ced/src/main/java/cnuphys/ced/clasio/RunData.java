package cnuphys.ced.clasio;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.log.Log;
import cnuphys.ced.alldata.DataManager;
import cnuphys.magfield.MagneticFields;

/**
 * Information in the run bank
 * @author heddle
 *
 */

public class RunData {

	public int run = -1;
	public int event;
	public int trigger;
	public byte type;
	public byte mode;
	public float solenoid;
	public float torus;
	public float rf;
	public float startTime;
	
	public void reset() {
		run = -1;
	}
	
	/**
	 * Change the fields if the event contains the run bank
	 * @param event the data event
	 * @return true if a run config bank was found and successfully parsed
	 */
	public boolean set(DataEvent dataEvent) {
		if (dataEvent == null) {
			return false;
		}

		boolean hasRunBank = dataEvent.hasBank("RUN::config");
		if (!hasRunBank) {
			return false;
		}

		int oldRun = run;
		
		try {
			System.out.println("-- GETTING Run:Config...");
			
			run = safeInt(dataEvent, "run");
			if (run < 0) {

				return false;
			}
			
			event = safeInt(dataEvent, "event");
			
//			System.err.println("In Set Data event num: " + event + "    event: " + dataEvent);
			
			if (event < 0) {
				return false;
			}
			
			trigger = safeInt(dataEvent, "trigger");
			if (trigger < 0) {
				return false;
			}

			type = safeByte(dataEvent, "type");
			if (type < 0) {
				return false;
			}

			System.out.println("---- GETTING Run:Config.mode...");
			mode = safeByte(dataEvent, "mode");
			if (mode < 0) {
				return false;
			}
			System.out.println("---- Done");
			
			solenoid = safeFloat(dataEvent, "solenoid");
			if (Float.isNaN(solenoid)) {
				return false;
			}

			torus = safeFloat(dataEvent, "torus");
			if (Float.isNaN(torus)) {
				return false;
			}
			
			rf = safeFloat(dataEvent, "rf");
			if (Float.isNaN(rf)) {
				return false;
			}
			
			startTime = safeFloat(dataEvent, "startTime");
			if (Float.isNaN(startTime)) {
				return false;
			}
			
			
//			run = dm.getIntArray(dataEvent, "RUN::config.run")[0];
//			event = dm.getIntArray(dataEvent, "RUN::config.event")[0];
//			trigger = dm.getIntArray(dataEvent, "RUN::config.trigger")[0];
//			type = dm.getByteArray(dataEvent, "RUN::config.type")[0];
//			mode = dm.getByteArray(dataEvent, "RUN::config.mode")[0];
//			solenoid = dm.getFloatArray(dataEvent, "RUN::config.solenoid")[0];
//			torus = dm.getFloatArray(dataEvent, "RUN::config.torus")[0];
//			rf = dm.getFloatArray(dataEvent, "RUN::config.rf")[0];
//			startTime = dm.getFloatArray(dataEvent, "RUN::config.startTime")[0];
			

			
			if (oldRun != run) {
				//set the mag field and menus
				boolean changed = MagneticFields.getInstance().changeFieldsAndMenus(torus, solenoid);
			}
			return true;
		} catch (Exception e) {
			Log.getInstance().warning("Exception in RunData det method");
		}

		return false;
	}
	
	private int safeInt(DataEvent event, String colName) {
		DataManager dm = DataManager.getInstance();
	    int[] data = dm.getIntArray(event, "RUN::config." + colName);	
	    if ((data == null) || (data.length < 1)) {
	    	return -1;
	    }
	    return data[0];
	}
	
	private byte safeByte(DataEvent event, String colName) {
		DataManager dm = DataManager.getInstance();
	    byte[] data = dm.getByteArray(event, "RUN::config." + colName);	
	    if ((data == null) || (data.length < 1)) {
	    	return -1;
	    }
	    return data[0];
	}
	
	private float safeFloat(DataEvent event, String colName) {
		DataManager dm = DataManager.getInstance();
	    float[] data = dm.getFloatArray(event, "RUN::config." + colName);	
	    if ((data == null) || (data.length < 1)) {
	    	return Float.NaN;
	    }
	    return data[0];
	}


		
	@Override
	public String toString() {
		String s = "run: " + run;
		s += "\nevent: " + event;
		s += "\ntrigger: " + trigger;
		s += "\ntype: " + type;
		s += "\nmode: " + mode;
		s += "\nsolenoid: " + solenoid;
		s += "\ntorus: " + torus;
		s += "\nrf: " + rf;
		s += "\nstartTime: " + startTime;
		return s;
	}
}
