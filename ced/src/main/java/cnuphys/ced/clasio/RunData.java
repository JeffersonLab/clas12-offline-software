package cnuphys.ced.clasio;

import org.jlab.io.base.DataEvent;

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
			DataManager dm = DataManager.getInstance();
			run = dm.getIntArray(dataEvent, "RUN::config.run")[0];
			event = dm.getIntArray(dataEvent, "RUN::config.event")[0];
			trigger = dm.getIntArray(dataEvent, "RUN::config.trigger")[0];
			type = dm.getByteArray(dataEvent, "RUN::config.type")[0];
			mode = dm.getByteArray(dataEvent, "RUN::config.mode")[0];
			solenoid = dm.getFloatArray(dataEvent, "RUN::config.solenoid")[0];
			torus = dm.getFloatArray(dataEvent, "RUN::config.torus")[0];
			rf = dm.getFloatArray(dataEvent, "RUN::config.rf")[0];
			startTime = dm.getFloatArray(dataEvent, "RUN::config.startTime")[0];
			
			
			if (oldRun != run) {
				//set the mag field and menus
				boolean changed = MagneticFields.getInstance().changeFieldsAndMenus(torus, solenoid);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
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
