package events;

import org.jlab.io.base.DataBank;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.io.hipo.HipoDataSync;

/**
 *
 * @author naharrison
 */
public class TestEvent {
	
	public static HipoDataEvent getDCSector1ElectronEvent() {
		HipoDataSync writer = new HipoDataSync();
		HipoDataEvent testEvent = (HipoDataEvent) writer.createEvent();
		DataBank config = testEvent.createBank("RUN::config", 1);
		DataBank DCtdc = testEvent.createBank("DC::tdc", 37);
		
		// this event is based on a gemc event with
		// one generated electron with p=2.5, th=25, ph=0
		// (i.e. px=1.057, py=0, pz=2.266)
		// torus = -1.0 , solenoid = 0.0

		config.setInt("run", 0, (int) 11);
		config.setInt("event", 0, (int) 1);
		config.setInt("trigger", 0, (int) 0);
		config.setLong("timestamp", 0, (long) 0);
		config.setByte("type", 0, (byte) 0);
		config.setByte("mode", 0, (byte) 0);
		config.setFloat("torus", 0, (float) -1.0);
		config.setFloat("solenoid", 0, (float) 0.0);
		config.setFloat("rf", 0, (float) 0.0);
		config.setFloat("startTime", 0, (float) 0.0);
		
		for(int i = 0; i < 37; i++) {
			DCtdc.setByte("sector", i, (byte) 1);
			DCtdc.setByte("order", i, (byte) 2);
			if(i > 31) DCtdc.setByte("layer", i, (byte) i);
			else DCtdc.setByte("layer", i, (byte) (i+1));
		}
		
		DCtdc.setShort("component", 0, (short) 62);
		DCtdc.setShort("component", 1, (short) 63);
		DCtdc.setShort("component", 2, (short) 62);
		DCtdc.setShort("component", 3, (short) 63);
		DCtdc.setShort("component", 4, (short) 62);
		DCtdc.setShort("component", 5, (short) 63);
		DCtdc.setShort("component", 6, (short) 63);
		DCtdc.setShort("component", 7, (short) 63);
		DCtdc.setShort("component", 8, (short) 63);
		DCtdc.setShort("component", 9, (short) 63);
		DCtdc.setShort("component", 10, (short) 63);
		DCtdc.setShort("component", 11, (short) 63);
		DCtdc.setShort("component", 12, (short) 58);
		DCtdc.setShort("component", 13, (short) 58);
		DCtdc.setShort("component", 14, (short) 58);
		DCtdc.setShort("component", 15, (short) 58);
		DCtdc.setShort("component", 16, (short) 58);
		DCtdc.setShort("component", 17, (short) 58);
		DCtdc.setShort("component", 18, (short) 57);
		DCtdc.setShort("component", 19, (short) 58);
		DCtdc.setShort("component", 20, (short) 57);
		DCtdc.setShort("component", 21, (short) 58);
		DCtdc.setShort("component", 22, (short) 57);
		DCtdc.setShort("component", 23, (short) 58);
		DCtdc.setShort("component", 24, (short) 51);
		DCtdc.setShort("component", 25, (short) 51);
		DCtdc.setShort("component", 26, (short) 51);
		DCtdc.setShort("component", 27, (short) 51);
		DCtdc.setShort("component", 28, (short) 50);
		DCtdc.setShort("component", 29, (short) 51);
		DCtdc.setShort("component", 30, (short) 50);
		DCtdc.setShort("component", 31, (short) 51);
		DCtdc.setShort("component", 32, (short) 50);
		DCtdc.setShort("component", 33, (short) 50);
		DCtdc.setShort("component", 34, (short) 50);
		DCtdc.setShort("component", 35, (short) 50);
		DCtdc.setShort("component", 36, (short) 50);
		
		DCtdc.setInt("TDC", 0, (int) 50);
		DCtdc.setInt("TDC", 1, (int) 81);
		DCtdc.setInt("TDC", 2, (int) 48);
		DCtdc.setInt("TDC", 3, (int) 74);
		DCtdc.setInt("TDC", 4, (int) 44);
		DCtdc.setInt("TDC", 5, (int) 80);
		DCtdc.setInt("TDC", 6, (int) 40);
		DCtdc.setInt("TDC", 7, (int) 91);
		DCtdc.setInt("TDC", 8, (int) 40);
		DCtdc.setInt("TDC", 9, (int) 97);
		DCtdc.setInt("TDC", 10, (int) 38);
		DCtdc.setInt("TDC", 11, (int) 88);
		DCtdc.setInt("TDC", 12, (int) 96);
		DCtdc.setInt("TDC", 13, (int) 290);
		DCtdc.setInt("TDC", 14, (int) 156);
		DCtdc.setInt("TDC", 15, (int) 207);
		DCtdc.setInt("TDC", 16, (int) 240);
		DCtdc.setInt("TDC", 17, (int) 111);
		DCtdc.setInt("TDC", 18, (int) 393);
		DCtdc.setInt("TDC", 19, (int) 113);
		DCtdc.setInt("TDC", 20, (int) 264);
		DCtdc.setInt("TDC", 21, (int) 227);
		DCtdc.setInt("TDC", 22, (int) 144);
		DCtdc.setInt("TDC", 23, (int) 332);
		DCtdc.setInt("TDC", 24, (int) 148);
		DCtdc.setInt("TDC", 25, (int) 187);
		DCtdc.setInt("TDC", 26, (int) 360);
		DCtdc.setInt("TDC", 27, (int) 34);
		DCtdc.setInt("TDC", 28, (int) 322);
		DCtdc.setInt("TDC", 29, (int) 233);
		DCtdc.setInt("TDC", 30, (int) 145);
		DCtdc.setInt("TDC", 31, (int) 431);
		DCtdc.setInt("TDC", 32, (int) 547);
		DCtdc.setInt("TDC", 33, (int) 80);
		DCtdc.setInt("TDC", 34, (int) 278);
		DCtdc.setInt("TDC", 35, (int) 296);
		DCtdc.setInt("TDC", 36, (int) 59);
		
		testEvent.appendBank(config);
		testEvent.appendBank(DCtdc);

		return testEvent;
	}

}
