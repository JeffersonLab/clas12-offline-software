package cnuphys.ced.clasio;

import org.jlab.io.base.DataEvent;

import cnuphys.ced.alldata.DataManager;

/**
 * Information in the run bank
 * @author heddle
 *
 */

//bank name: [RUN::config] column name: [run] full name: [RUN::config.run] data type: int
//bank name: [RUN::config] column name: [event] full name: [RUN::config.event] data type: int
//bank name: [RUN::config] column name: [trigger] full name: [RUN::config.trigger] data type: int
//bank name: [RUN::config] column name: [type] full name: [RUN::config.type] data type: byte
//bank name: [RUN::config] column name: [mode] full name: [RUN::config.mode] data type: byte
//bank name: [RUN::config] column name: [torus] full name: [RUN::config.torus] data type: float
//bank name: [RUN::config] column name: [solenoid] full name: [RUN::config.solenoid] data type: float
//bank name: [RUN::config] column name: [rf] full name: [RUN::config.rf] data type: float
//bank name: [RUN::config] column name: [startTime] full name: [RUN::config.startTime] data type: float
//bank name: [RUN::rf] column name: [id] full name: [RUN::rf.id] data type: short
//bank name: [RUN::rf] column name: [time] full name: [RUN::rf.time] data type: float
public class RunData {

	public int run;
	public int event;
	public int trigger;
	public byte type;
	public byte mode;
	public float solenoid;
	public float torus;
	public float rf;
	public float startTime;
	
	/**
	 * Change the fields if the event contains the run bank
	 * @param event the data event
	 * @return true if a run config bank was found and successfully parsed
	 */
	public boolean set(DataEvent dataEvent) {
		if ((dataEvent != null) && dataEvent.hasBank("RUN::config")) {
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
				return true;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
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
