package org.jlab.analysis.physics;

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
		// updated to use non-linear t2d
		// updated to include mini-stagger

		config.setInt("run", 0, (int) 11);
		config.setInt("event", 0, (int) 1);
		config.setInt("unixtime", 0, (int) 0);
		config.setInt("trigger", 0, (int) 0);
		config.setLong("timestamp", 0, (long) 0);
		config.setByte("type", 0, (byte) 0);
		config.setByte("mode", 0, (byte) 0);
		config.setFloat("torus", 0, (float) -1.0);
		config.setFloat("solenoid", 0, (float) 0.0);
		
		for(int i = 0; i < 37; i++) {
			DCtdc.setByte("sector", i, (byte) 1);
			DCtdc.setByte("order", i, (byte) 2);
			if(i >= 32) DCtdc.setByte("layer", i, (byte) i);
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

		DCtdc.setInt("TDC", 0, (int) 54);
		DCtdc.setInt("TDC", 1, (int) 88);
		DCtdc.setInt("TDC", 2, (int) 58);
		DCtdc.setInt("TDC", 3, (int) 83);
		DCtdc.setInt("TDC", 4, (int) 53);
		DCtdc.setInt("TDC", 5, (int) 88);
		DCtdc.setInt("TDC", 6, (int) 42);
		DCtdc.setInt("TDC", 7, (int) 112);
		DCtdc.setInt("TDC", 8, (int) 41);
		DCtdc.setInt("TDC", 9, (int) 110);
		DCtdc.setInt("TDC", 10, (int) 46);
		DCtdc.setInt("TDC", 11, (int) 104);
		DCtdc.setInt("TDC", 12, (int) 32);
		DCtdc.setInt("TDC", 13, (int) 204);
		DCtdc.setInt("TDC", 14, (int) 83);
		DCtdc.setInt("TDC", 15, (int) 137);
		DCtdc.setInt("TDC", 16, (int) 151);
		DCtdc.setInt("TDC", 17, (int) 81);
		DCtdc.setInt("TDC", 18, (int) 276);
		DCtdc.setInt("TDC", 19, (int) 57);
		DCtdc.setInt("TDC", 20, (int) 167);
		DCtdc.setInt("TDC", 21, (int) 136);
		DCtdc.setInt("TDC", 22, (int) 94);
		DCtdc.setInt("TDC", 23, (int) 225);
		DCtdc.setInt("TDC", 24, (int) 110);
		DCtdc.setInt("TDC", 25, (int) 190);
		DCtdc.setInt("TDC", 26, (int) 334);
		DCtdc.setInt("TDC", 27, (int) 2);
		DCtdc.setInt("TDC", 28, (int) 354);
		DCtdc.setInt("TDC", 29, (int) 188);
		DCtdc.setInt("TDC", 30, (int) 158);
		DCtdc.setInt("TDC", 31, (int) 424);
		DCtdc.setInt("TDC", 32, (int) 679);
		DCtdc.setInt("TDC", 33, (int) 37);
		DCtdc.setInt("TDC", 34, (int) 312);
		DCtdc.setInt("TDC", 35, (int) 260);
		DCtdc.setInt("TDC", 36, (int) 68);
		
		testEvent.appendBank(config);
		testEvent.appendBank(DCtdc);

		return testEvent;
	}


	public static HipoDataEvent getCVTTestEvent() {
		HipoDataSync writer = new HipoDataSync();
		HipoDataEvent testEvent = (HipoDataEvent) writer.createEvent();
		DataBank config = testEvent.createBank("RUN::config", 1);
		DataBank BSTadc = testEvent.createBank("BST::adc", 8);
		DataBank BMTadc = testEvent.createBank("BMT::adc", 38);
		
		// this event is based on a gemc that takes into account the new "no-shim" geometry for BMT
		// torus = -1.0 , solenoid = -1.0
    // positive muon

		config.setInt("run", 0, (int) 11);
		config.setInt("event", 0, (int) 1);
		config.setInt("unixtime", 0, (int) 0);
		config.setInt("trigger", 0, (int) 0);
		config.setLong("timestamp", 0, (long) 0);
		config.setByte("type", 0, (byte) 0);
		config.setByte("mode", 0, (byte) 0);
		config.setFloat("torus", 0, (float) -1.0);
		config.setFloat("solenoid", 0, (float) -1.0);
		
		for(int i = 0; i < 8; i++) {
			BSTadc.setByte("order", i, (byte) 0);
			BSTadc.setShort("ped", i, (short) 0);
			BSTadc.setLong("timestamp", i, (long) 0);
		}
		BSTadc.setByte("sector", 0, (byte)8 );
		BSTadc.setByte("sector", 1, (byte)8 );
		BSTadc.setByte("sector", 2, (byte)11);
		BSTadc.setByte("sector", 3, (byte)11);
		BSTadc.setByte("sector", 4, (byte)11);
		BSTadc.setByte("sector", 5, (byte)14);
		BSTadc.setByte("sector", 6, (byte)14);
		BSTadc.setByte("sector", 7, (byte)14);
	
		BSTadc.setByte("layer", 0, (byte) 1);
		BSTadc.setByte("layer", 1, (byte) 2);
		BSTadc.setByte("layer", 2, (byte) 3);
		BSTadc.setByte("layer", 3, (byte) 3);
		BSTadc.setByte("layer", 4, (byte) 4);
		BSTadc.setByte("layer", 5, (byte) 5);
		BSTadc.setByte("layer", 6, (byte) 5);
		BSTadc.setByte("layer", 7, (byte) 6);
	
		BSTadc.setShort("component", 0, (short) 150);
		BSTadc.setShort("component", 1, (short) 49 );
		BSTadc.setShort("component", 2, (short) 128);
		BSTadc.setShort("component", 3, (short) 129);
		BSTadc.setShort("component", 4, (short) 80 );
		BSTadc.setShort("component", 5, (short) 102);
		BSTadc.setShort("component", 6, (short) 101);
		BSTadc.setShort("component", 7, (short) 116);
	
		BSTadc.setInt("ADC", 0, (int) 5);
		BSTadc.setInt("ADC", 1, (int) 5);
		BSTadc.setInt("ADC", 2, (int) 3);
		BSTadc.setInt("ADC", 3, (int) 3);
		BSTadc.setInt("ADC", 4, (int) 7);
		BSTadc.setInt("ADC", 5, (int) 1);
		BSTadc.setInt("ADC", 6, (int) 1);
		BSTadc.setInt("ADC", 7, (int) 4);
	
		BSTadc.setFloat("time", 0, (float) 35.0 );
		BSTadc.setFloat("time", 1, (float) 238.0);
		BSTadc.setFloat("time", 2, (float) 56.0 );
		BSTadc.setFloat("time", 3, (float) 248.0);
		BSTadc.setFloat("time", 4, (float) 221.0);
		BSTadc.setFloat("time", 5, (float) 66.0 );
		BSTadc.setFloat("time", 6, (float) 144.0);
		BSTadc.setFloat("time", 7, (float) 86.0 );

		for(int i = 0; i < 38; i++) {
			BMTadc.setByte("sector", i, (byte) 3);
			BMTadc.setByte("order", i, (byte) 0);
			BMTadc.setFloat("time", i, (float) 0.0);
			BMTadc.setShort("ped", i, (short) 0);
			BMTadc.setInt("integral", i, (int) 0);
			BMTadc.setLong("timestamp", i, (long) 0);
		}

		BMTadc.setByte("layer",  0, (byte) 1);
		BMTadc.setByte("layer",  1, (byte) 1);
		BMTadc.setByte("layer",  2, (byte) 1);
		BMTadc.setByte("layer",  3, (byte) 1);
		BMTadc.setByte("layer",  4, (byte) 1);
		BMTadc.setByte("layer",  5, (byte) 1);
		BMTadc.setByte("layer",  6, (byte) 2);
		BMTadc.setByte("layer",  7, (byte) 2);
		BMTadc.setByte("layer",  8, (byte) 2);
		BMTadc.setByte("layer",  9, (byte) 2);
		BMTadc.setByte("layer", 10, (byte) 2);
		BMTadc.setByte("layer", 11, (byte) 2);
		BMTadc.setByte("layer", 12, (byte) 2);
		BMTadc.setByte("layer", 13, (byte) 3);
		BMTadc.setByte("layer", 14, (byte) 3);
		BMTadc.setByte("layer", 15, (byte) 3);
		BMTadc.setByte("layer", 16, (byte) 3);
		BMTadc.setByte("layer", 17, (byte) 3);
		BMTadc.setByte("layer", 18, (byte) 3);
		BMTadc.setByte("layer", 19, (byte) 4);
		BMTadc.setByte("layer", 20, (byte) 4);
		BMTadc.setByte("layer", 21, (byte) 4);
		BMTadc.setByte("layer", 22, (byte) 4);
		BMTadc.setByte("layer", 23, (byte) 4);
		BMTadc.setByte("layer", 24, (byte) 4);
		BMTadc.setByte("layer", 25, (byte) 4);
		BMTadc.setByte("layer", 26, (byte) 5);
		BMTadc.setByte("layer", 27, (byte) 5);
		BMTadc.setByte("layer", 28, (byte) 5);
		BMTadc.setByte("layer", 29, (byte) 5);
		BMTadc.setByte("layer", 30, (byte) 5);
		BMTadc.setByte("layer", 31, (byte) 5);
		BMTadc.setByte("layer", 32, (byte) 6);
		BMTadc.setByte("layer", 33, (byte) 6);
		BMTadc.setByte("layer", 34, (byte) 6);
		BMTadc.setByte("layer", 35, (byte) 6);
		BMTadc.setByte("layer", 36, (byte) 6);
		BMTadc.setByte("layer", 37, (byte) 6);

		BMTadc.setShort("component",  0, (short) 392);
		BMTadc.setShort("component",  1, (short) 393);
		BMTadc.setShort("component",  2, (short) 394);
		BMTadc.setShort("component",  3, (short) 395);
		BMTadc.setShort("component",  4, (short) 391);
		BMTadc.setShort("component",  5, (short) 396);
		BMTadc.setShort("component",  6, (short) 560);
		BMTadc.setShort("component",  7, (short) 561);
		BMTadc.setShort("component",  8, (short)  -1);
		BMTadc.setShort("component",  9, (short) 562);
		BMTadc.setShort("component", 10, (short) 563);
		BMTadc.setShort("component", 11, (short) 564);
		BMTadc.setShort("component", 12, (short) 565);
		BMTadc.setShort("component", 13, (short)  -1);
		BMTadc.setShort("component", 14, (short) 561);
		BMTadc.setShort("component", 15, (short) 560);
		BMTadc.setShort("component", 16, (short) 562);
		BMTadc.setShort("component", 17, (short) 563);
		BMTadc.setShort("component", 18, (short) 564);
		BMTadc.setShort("component", 19, (short)  -1);
		BMTadc.setShort("component", 20, (short) 462);
		BMTadc.setShort("component", 21, (short) 463);
		BMTadc.setShort("component", 22, (short) 461);
		BMTadc.setShort("component", 23, (short) 464);
		BMTadc.setShort("component", 24, (short) 460);
		BMTadc.setShort("component", 25, (short) 465);
		BMTadc.setShort("component", 26, (short)  -1);
		BMTadc.setShort("component", 27, (short) 674);
		BMTadc.setShort("component", 28, (short) 673);
		BMTadc.setShort("component", 29, (short) 675);
		BMTadc.setShort("component", 30, (short) 676);
		BMTadc.setShort("component", 31, (short) 677);
		BMTadc.setShort("component", 32, (short) 547);
		BMTadc.setShort("component", 33, (short) 546);
		BMTadc.setShort("component", 34, (short)  -1);
		BMTadc.setShort("component", 35, (short) 548);
		BMTadc.setShort("component", 36, (short) 549);
		BMTadc.setShort("component", 37, (short) 550);

		BMTadc.setInt("ADC",  0, (int)9 );
		BMTadc.setInt("ADC",  1, (int)13);
		BMTadc.setInt("ADC",  2, (int)10);
		BMTadc.setInt("ADC",  3, (int)0 );
		BMTadc.setInt("ADC",  4, (int)0 );
		BMTadc.setInt("ADC",  5, (int)0 );
		BMTadc.setInt("ADC",  6, (int)1 );
		BMTadc.setInt("ADC",  7, (int)25);
		BMTadc.setInt("ADC",  8, (int)0 );
		BMTadc.setInt("ADC",  9, (int)3 );
		BMTadc.setInt("ADC", 10, (int)1 );
		BMTadc.setInt("ADC", 11, (int)1 );
		BMTadc.setInt("ADC", 12, (int)0 );
		BMTadc.setInt("ADC", 13, (int)0 );
		BMTadc.setInt("ADC", 14, (int)14);
		BMTadc.setInt("ADC", 15, (int)0 );
		BMTadc.setInt("ADC", 16, (int)8 );
		BMTadc.setInt("ADC", 17, (int)3 );
		BMTadc.setInt("ADC", 18, (int)1 );
		BMTadc.setInt("ADC", 19, (int)0 );
		BMTadc.setInt("ADC", 20, (int)19);
		BMTadc.setInt("ADC", 21, (int)3 );
		BMTadc.setInt("ADC", 22, (int)0 );
		BMTadc.setInt("ADC", 23, (int)0 );
		BMTadc.setInt("ADC", 24, (int)0 );
		BMTadc.setInt("ADC", 25, (int)0 );
		BMTadc.setInt("ADC", 26, (int)0 );
		BMTadc.setInt("ADC", 27, (int)1 );
		BMTadc.setInt("ADC", 28, (int)0 );
		BMTadc.setInt("ADC", 29, (int)19);
		BMTadc.setInt("ADC", 30, (int)9 );
		BMTadc.setInt("ADC", 31, (int)0 );
		BMTadc.setInt("ADC", 32, (int)11);
		BMTadc.setInt("ADC", 33, (int)0 );
		BMTadc.setInt("ADC", 34, (int)0 );
		BMTadc.setInt("ADC", 35, (int)14);
		BMTadc.setInt("ADC", 36, (int)2 );
		BMTadc.setInt("ADC", 37, (int)0 );

		testEvent.appendBank(config);
		testEvent.appendBank(BSTadc);
		testEvent.appendBank(BMTadc);

		return testEvent;
	}


	public static HipoDataEvent getECSector1PhotonEvent() {
		HipoDataSync writer = new HipoDataSync();
		HipoDataEvent testEvent = (HipoDataEvent) writer.createEvent();
		DataBank config = testEvent.createBank("RUN::config", 1);
		DataBank ECALadc = testEvent.createBank("ECAL::adc", 48);
		DataBank ECALtdc = testEvent.createBank("ECAL::tdc", 48);
		
		// this event is based on a gemc event with
		// one generated photon with p=2.5, th=25, ph=0
		// (i.e. px=1.057, py=0, pz=2.266)
		// torus = -1.0 , solenoid = 0.0

		config.setInt("run", 0, (int) 11);
		config.setInt("event", 0, (int) 1);
		config.setInt("unixtime", 0, (int) 0);
                config.setInt("trigger", 0, (int) 0);
		config.setLong("timestamp", 0, (long) 0);
		config.setByte("type", 0, (byte) 0);
		config.setByte("mode", 0, (byte) 0);
		config.setFloat("torus", 0, (float) -1.0);
		config.setFloat("solenoid", 0, (float) 0.0);

		for(int i = 0; i < 48; i++) {
			ECALadc.setByte("sector", i, (byte) 1);
			ECALadc.setByte("order", i, (byte) 0);
			ECALadc.setFloat("time", i, (float) 0.0);
			ECALadc.setShort("ped", i, (short) 0);
			ECALtdc.setByte("sector", i, (byte) 1);
		}

		ECALadc.setByte("layer", 0, (byte) 1);
		ECALadc.setByte("layer", 1, (byte) 2);
		ECALadc.setByte("layer", 2, (byte) 3);
		ECALadc.setByte("layer", 3, (byte) 2);
		ECALadc.setByte("layer", 4, (byte) 3);
		ECALadc.setByte("layer", 5, (byte) 3);
		ECALadc.setByte("layer", 6, (byte) 3);
		ECALadc.setByte("layer", 7, (byte) 2);
		ECALadc.setByte("layer", 8, (byte) 2);
		ECALadc.setByte("layer", 9, (byte) 1);
		ECALadc.setByte("layer", 10, (byte) 1);
		ECALadc.setByte("layer", 11, (byte) 2);
		ECALadc.setByte("layer", 12, (byte) 1);
		ECALadc.setByte("layer", 13, (byte) 2);
		ECALadc.setByte("layer", 14, (byte) 3);
		ECALadc.setByte("layer", 15, (byte) 1);
		ECALadc.setByte("layer", 16, (byte) 2);
		ECALadc.setByte("layer", 17, (byte) 2);
		ECALadc.setByte("layer", 18, (byte) 2);
		ECALadc.setByte("layer", 19, (byte) 5);
		ECALadc.setByte("layer", 20, (byte) 5);
		ECALadc.setByte("layer", 21, (byte) 4);
		ECALadc.setByte("layer", 22, (byte) 7);
		ECALadc.setByte("layer", 23, (byte) 4);
		ECALadc.setByte("layer", 24, (byte) 6);
		ECALadc.setByte("layer", 25, (byte) 5);
		ECALadc.setByte("layer", 26, (byte) 6);
		ECALadc.setByte("layer", 27, (byte) 7);
		ECALadc.setByte("layer", 28, (byte) 5);
		ECALadc.setByte("layer", 29, (byte) 4);
		ECALadc.setByte("layer", 30, (byte) 4);
		ECALadc.setByte("layer", 31, (byte) 9);
		ECALadc.setByte("layer", 32, (byte) 8);
		ECALadc.setByte("layer", 33, (byte) 7);
		ECALadc.setByte("layer", 34, (byte) 8);
		ECALadc.setByte("layer", 35, (byte) 4);
		ECALadc.setByte("layer", 36, (byte) 4);
		ECALadc.setByte("layer", 37, (byte) 6);
		ECALadc.setByte("layer", 38, (byte) 4);
		ECALadc.setByte("layer", 39, (byte) 8);
		ECALadc.setByte("layer", 40, (byte) 4);
		ECALadc.setByte("layer", 41, (byte) 5);
		ECALadc.setByte("layer", 42, (byte) 6);
		ECALadc.setByte("layer", 43, (byte) 9);
		ECALadc.setByte("layer", 44, (byte) 7);
		ECALadc.setByte("layer", 45, (byte) 6);
		ECALadc.setByte("layer", 46, (byte) 5);
		ECALadc.setByte("layer", 47, (byte) 5);

		ECALadc.setShort("component", 0, (short) 58);
		ECALadc.setShort("component", 1, (short) 33);
		ECALadc.setShort("component", 2, (short) 33);
		ECALadc.setShort("component", 3, (short) 34);
		ECALadc.setShort("component", 4, (short) 32);
		ECALadc.setShort("component", 5, (short) 24);
		ECALadc.setShort("component", 6, (short) 34);
		ECALadc.setShort("component", 7, (short) 35);
		ECALadc.setShort("component", 8, (short) 32);
		ECALadc.setShort("component", 9, (short) 61);
		ECALadc.setShort("component", 10, (short) 59);
		ECALadc.setShort("component", 11, (short) 39);
		ECALadc.setShort("component", 12, (short) 57);
		ECALadc.setShort("component", 13, (short) 31);
		ECALadc.setShort("component", 14, (short) 38);
		ECALadc.setShort("component", 15, (short) 57);
		ECALadc.setShort("component", 16, (short) 34);
		ECALadc.setShort("component", 17, (short) 35);
		ECALadc.setShort("component", 18, (short) 35);
		ECALadc.setShort("component", 19, (short) 23);
		ECALadc.setShort("component", 20, (short) 24);
		ECALadc.setShort("component", 21, (short) 28);
		ECALadc.setShort("component", 22, (short) 27);
		ECALadc.setShort("component", 23, (short) 27);
		ECALadc.setShort("component", 24, (short) 23);
		ECALadc.setShort("component", 25, (short) 26);
		ECALadc.setShort("component", 26, (short) 22);
		ECALadc.setShort("component", 27, (short) 28);
		ECALadc.setShort("component", 28, (short) 21);
		ECALadc.setShort("component", 29, (short) 24);
		ECALadc.setShort("component", 30, (short) 22);
		ECALadc.setShort("component", 31, (short) 23);
		ECALadc.setShort("component", 32, (short) 22);
		ECALadc.setShort("component", 33, (short) 29);
		ECALadc.setShort("component", 34, (short) 24);
		ECALadc.setShort("component", 35, (short) 30);
		ECALadc.setShort("component", 36, (short) 26);
		ECALadc.setShort("component", 37, (short) 25);
		ECALadc.setShort("component", 38, (short) 25);
		ECALadc.setShort("component", 39, (short) 23);
		ECALadc.setShort("component", 40, (short) 29);
		ECALadc.setShort("component", 41, (short) 25);
		ECALadc.setShort("component", 42, (short) 24);
		ECALadc.setShort("component", 43, (short) 24);
		ECALadc.setShort("component", 44, (short) 26);
		ECALadc.setShort("component", 45, (short) 20);
		ECALadc.setShort("component", 46, (short) 27);
		ECALadc.setShort("component", 47, (short) 22);

		ECALadc.setInt("ADC", 0, (int) 14543);
		ECALadc.setInt("ADC", 1, (int) 10421);
		ECALadc.setInt("ADC", 2, (int) 13017);
		ECALadc.setInt("ADC", 3, (int) 758);
		ECALadc.setInt("ADC", 4, (int) 910);
		ECALadc.setInt("ADC", 5, (int) 0);
		ECALadc.setInt("ADC", 6, (int) 783);
		ECALadc.setInt("ADC", 7, (int) 23);
		ECALadc.setInt("ADC", 8, (int) 81);
		ECALadc.setInt("ADC", 9, (int) 0);
		ECALadc.setInt("ADC", 10, (int) 190);
		ECALadc.setInt("ADC", 11, (int) 0);
		ECALadc.setInt("ADC", 12, (int) 74);
		ECALadc.setInt("ADC", 13, (int) 225);
		ECALadc.setInt("ADC", 14, (int) 14);
		ECALadc.setInt("ADC", 15, (int) 0);
		ECALadc.setInt("ADC", 16, (int) 0);
		ECALadc.setInt("ADC", 17, (int) 0);
		ECALadc.setInt("ADC", 18, (int) 0);
		ECALadc.setInt("ADC", 19, (int) 3599);
		ECALadc.setInt("ADC", 20, (int) 353);
		ECALadc.setInt("ADC", 21, (int) 526);
		ECALadc.setInt("ADC", 22, (int) 1120);
		ECALadc.setInt("ADC", 23, (int) 3027);
		ECALadc.setInt("ADC", 24, (int) 2571);
		ECALadc.setInt("ADC", 25, (int) 367);
		ECALadc.setInt("ADC", 26, (int) 248);
		ECALadc.setInt("ADC", 27, (int) 0);
		ECALadc.setInt("ADC", 28, (int) 296);
		ECALadc.setInt("ADC", 29, (int) 73);
		ECALadc.setInt("ADC", 30, (int) 192);
		ECALadc.setInt("ADC", 31, (int) 361);
		ECALadc.setInt("ADC", 32, (int) 0);
		ECALadc.setInt("ADC", 33, (int) 51);
		ECALadc.setInt("ADC", 34, (int) 435);
		ECALadc.setInt("ADC", 35, (int) 0);
		ECALadc.setInt("ADC", 36, (int) 0);
		ECALadc.setInt("ADC", 37, (int) 252);
		ECALadc.setInt("ADC", 38, (int) 0);
		ECALadc.setInt("ADC", 39, (int) 872);
		ECALadc.setInt("ADC", 40, (int) 544);
		ECALadc.setInt("ADC", 41, (int) 0);
		ECALadc.setInt("ADC", 42, (int) 666);
		ECALadc.setInt("ADC", 43, (int) 0);
		ECALadc.setInt("ADC", 44, (int) 156);
		ECALadc.setInt("ADC", 45, (int) 0);
		ECALadc.setInt("ADC", 46, (int) 0);
		ECALadc.setInt("ADC", 47, (int) 391);

		ECALtdc.setByte("layer", 0, (byte) 1);
		ECALtdc.setByte("layer", 1, (byte) 2);
		ECALtdc.setByte("layer", 2, (byte) 3);
		ECALtdc.setByte("layer", 3, (byte) 2);
		ECALtdc.setByte("layer", 4, (byte) 3);
		ECALtdc.setByte("layer", 5, (byte) 3);
		ECALtdc.setByte("layer", 6, (byte) 3);
		ECALtdc.setByte("layer", 7, (byte) 2);
		ECALtdc.setByte("layer", 8, (byte) 2);
		ECALtdc.setByte("layer", 9, (byte) 1);
		ECALtdc.setByte("layer", 10, (byte) 1);
		ECALtdc.setByte("layer", 11, (byte) 2);
		ECALtdc.setByte("layer", 12, (byte) 1);
		ECALtdc.setByte("layer", 13, (byte) 2);
		ECALtdc.setByte("layer", 14, (byte) 3);
		ECALtdc.setByte("layer", 15, (byte) 1);
		ECALtdc.setByte("layer", 16, (byte) 2);
		ECALtdc.setByte("layer", 17, (byte) 2);
		ECALtdc.setByte("layer", 18, (byte) 2);
		ECALtdc.setByte("layer", 19, (byte) 5);
		ECALtdc.setByte("layer", 20, (byte) 5);
		ECALtdc.setByte("layer", 21, (byte) 4);
		ECALtdc.setByte("layer", 22, (byte) 7);
		ECALtdc.setByte("layer", 23, (byte) 4);
		ECALtdc.setByte("layer", 24, (byte) 6);
		ECALtdc.setByte("layer", 25, (byte) 5);
		ECALtdc.setByte("layer", 26, (byte) 6);
		ECALtdc.setByte("layer", 27, (byte) 7);
		ECALtdc.setByte("layer", 28, (byte) 5);
		ECALtdc.setByte("layer", 29, (byte) 4);
		ECALtdc.setByte("layer", 30, (byte) 4);
		ECALtdc.setByte("layer", 31, (byte) 9);
		ECALtdc.setByte("layer", 32, (byte) 8);
		ECALtdc.setByte("layer", 33, (byte) 7);
		ECALtdc.setByte("layer", 34, (byte) 8);
		ECALtdc.setByte("layer", 35, (byte) 4);
		ECALtdc.setByte("layer", 36, (byte) 4);
		ECALtdc.setByte("layer", 37, (byte) 6);
		ECALtdc.setByte("layer", 38, (byte) 4);
		ECALtdc.setByte("layer", 39, (byte) 8);
		ECALtdc.setByte("layer", 40, (byte) 4);
		ECALtdc.setByte("layer", 41, (byte) 5);
		ECALtdc.setByte("layer", 42, (byte) 6);
		ECALtdc.setByte("layer", 43, (byte) 9);
		ECALtdc.setByte("layer", 44, (byte) 7);
		ECALtdc.setByte("layer", 45, (byte) 6);
		ECALtdc.setByte("layer", 46, (byte) 5);
		ECALtdc.setByte("layer", 47, (byte) 5);

		ECALtdc.setShort("component", 0, (short) 58);
		ECALtdc.setShort("component", 1, (short) 33);
		ECALtdc.setShort("component", 2, (short) 33);
		ECALtdc.setShort("component", 3, (short) 34);
		ECALtdc.setShort("component", 4, (short) 32);
		ECALtdc.setShort("component", 5, (short) 24);
		ECALtdc.setShort("component", 6, (short) 34);
		ECALtdc.setShort("component", 7, (short) 35);
		ECALtdc.setShort("component", 8, (short) 32);
		ECALtdc.setShort("component", 9, (short) 61);
		ECALtdc.setShort("component", 10, (short) 59);
		ECALtdc.setShort("component", 11, (short) 39);
		ECALtdc.setShort("component", 12, (short) 57);
		ECALtdc.setShort("component", 13, (short) 31);
		ECALtdc.setShort("component", 14, (short) 38);
		ECALtdc.setShort("component", 15, (short) 57);
		ECALtdc.setShort("component", 16, (short) 34);
		ECALtdc.setShort("component", 17, (short) 35);
		ECALtdc.setShort("component", 18, (short) 35);
		ECALtdc.setShort("component", 19, (short) 23);
		ECALtdc.setShort("component", 20, (short) 24);
		ECALtdc.setShort("component", 21, (short) 28);
		ECALtdc.setShort("component", 22, (short) 27);
		ECALtdc.setShort("component", 23, (short) 27);
		ECALtdc.setShort("component", 24, (short) 23);
		ECALtdc.setShort("component", 25, (short) 26);
		ECALtdc.setShort("component", 26, (short) 22);
		ECALtdc.setShort("component", 27, (short) 28);
		ECALtdc.setShort("component", 28, (short) 21);
		ECALtdc.setShort("component", 29, (short) 24);
		ECALtdc.setShort("component", 30, (short) 22);
		ECALtdc.setShort("component", 31, (short) 23);
		ECALtdc.setShort("component", 32, (short) 22);
		ECALtdc.setShort("component", 33, (short) 29);
		ECALtdc.setShort("component", 34, (short) 24);
		ECALtdc.setShort("component", 35, (short) 30);
		ECALtdc.setShort("component", 36, (short) 26);
		ECALtdc.setShort("component", 37, (short) 25);
		ECALtdc.setShort("component", 38, (short) 25);
		ECALtdc.setShort("component", 39, (short) 23);
		ECALtdc.setShort("component", 40, (short) 29);
		ECALtdc.setShort("component", 41, (short) 25);
		ECALtdc.setShort("component", 42, (short) 24);
		ECALtdc.setShort("component", 43, (short) 24);
		ECALtdc.setShort("component", 44, (short) 26);
		ECALtdc.setShort("component", 45, (short) 20);
		ECALtdc.setShort("component", 46, (short) 27);
		ECALtdc.setShort("component", 47, (short) 22);

		ECALtdc.setByte("order", 0, (byte) 2);
		ECALtdc.setByte("order", 1, (byte) 2);
		ECALtdc.setByte("order", 2, (byte) 2);
		ECALtdc.setByte("order", 3, (byte) 2);
		ECALtdc.setByte("order", 4, (byte) 2);
		ECALtdc.setByte("order", 5, (byte) 2);
		ECALtdc.setByte("order", 6, (byte) 2);
		ECALtdc.setByte("order", 7, (byte) 2);
		ECALtdc.setByte("order", 8, (byte) 2);
		ECALtdc.setByte("order", 9, (byte) 2);
		ECALtdc.setByte("order", 10, (byte) 2);
		ECALtdc.setByte("order", 11, (byte) 2);
		ECALtdc.setByte("order", 12, (byte) 2);
		ECALtdc.setByte("order", 13, (byte) 2);
		ECALtdc.setByte("order", 14, (byte) 2);
		ECALtdc.setByte("order", 15, (byte) 2);
		ECALtdc.setByte("order", 16, (byte) 2);
		ECALtdc.setByte("order", 17, (byte) 2);
		ECALtdc.setByte("order", 18, (byte) 2);
		ECALtdc.setByte("order", 19, (byte) 1);
		ECALtdc.setByte("order", 20, (byte) 1);
		ECALtdc.setByte("order", 21, (byte) 1);
		ECALtdc.setByte("order", 22, (byte) 1);
		ECALtdc.setByte("order", 23, (byte) 1);
		ECALtdc.setByte("order", 24, (byte) 1);
		ECALtdc.setByte("order", 25, (byte) 1);
		ECALtdc.setByte("order", 26, (byte) 1);
		ECALtdc.setByte("order", 27, (byte) 1);
		ECALtdc.setByte("order", 28, (byte) 1);
		ECALtdc.setByte("order", 29, (byte) 1);
		ECALtdc.setByte("order", 30, (byte) 1);
		ECALtdc.setByte("order", 31, (byte) 1);
		ECALtdc.setByte("order", 32, (byte) 1);
		ECALtdc.setByte("order", 33, (byte) 1);
		ECALtdc.setByte("order", 34, (byte) 1);
		ECALtdc.setByte("order", 35, (byte) 1);
		ECALtdc.setByte("order", 36, (byte) 1);
		ECALtdc.setByte("order", 37, (byte) 1);
		ECALtdc.setByte("order", 38, (byte) 1);
		ECALtdc.setByte("order", 39, (byte) 1);
		ECALtdc.setByte("order", 40, (byte) 1);
		ECALtdc.setByte("order", 41, (byte) 1);
		ECALtdc.setByte("order", 42, (byte) 1);
		ECALtdc.setByte("order", 43, (byte) 1);
		ECALtdc.setByte("order", 44, (byte) 1);
		ECALtdc.setByte("order", 45, (byte) 1);
		ECALtdc.setByte("order", 46, (byte) 1);
		ECALtdc.setByte("order", 47, (byte) 1);

		ECALtdc.setInt("TDC", 0, (int) 157379);
		ECALtdc.setInt("TDC", 1, (int) 154505);
		ECALtdc.setInt("TDC", 2, (int) 154481);
		ECALtdc.setInt("TDC", 3, (int) 154733);
		ECALtdc.setInt("TDC", 4, (int) 154469);
		ECALtdc.setInt("TDC", 5, (int) 155018);
		ECALtdc.setInt("TDC", 6, (int) 155045);
		ECALtdc.setInt("TDC", 7, (int) 154860);
		ECALtdc.setInt("TDC", 8, (int) 154772);
		ECALtdc.setInt("TDC", 9, (int) 0);
		ECALtdc.setInt("TDC", 10, (int) 158643);
		ECALtdc.setInt("TDC", 11, (int) 0);
		ECALtdc.setInt("TDC", 12, (int) 160655);
		ECALtdc.setInt("TDC", 13, (int) 154097);
		ECALtdc.setInt("TDC", 14, (int) 155831);
		ECALtdc.setInt("TDC", 15, (int) 0);
		ECALtdc.setInt("TDC", 16, (int) 0);
		ECALtdc.setInt("TDC", 17, (int) 0);
		ECALtdc.setInt("TDC", 18, (int) 0);
		ECALtdc.setInt("TDC", 19, (int) 155544);
		ECALtdc.setInt("TDC", 20, (int) 155273);
		ECALtdc.setInt("TDC", 21, (int) 158296);
		ECALtdc.setInt("TDC", 22, (int) 159179);
		ECALtdc.setInt("TDC", 23, (int) 158011);
		ECALtdc.setInt("TDC", 24, (int) 158690);
		ECALtdc.setInt("TDC", 25, (int) 156844);
		ECALtdc.setInt("TDC", 26, (int) 158816);
		ECALtdc.setInt("TDC", 27, (int) 0);
		ECALtdc.setInt("TDC", 28, (int) 154777);
		ECALtdc.setInt("TDC", 29, (int) 158491);
		ECALtdc.setInt("TDC", 30, (int) 157782);
		ECALtdc.setInt("TDC", 31, (int) 159192);
		ECALtdc.setInt("TDC", 32, (int) 0);
		ECALtdc.setInt("TDC", 33, (int) 160551);
		ECALtdc.setInt("TDC", 34, (int) 157200);
		ECALtdc.setInt("TDC", 35, (int) 0);
		ECALtdc.setInt("TDC", 36, (int) 0);
		ECALtdc.setInt("TDC", 37, (int) 159032);
		ECALtdc.setInt("TDC", 38, (int) 0);
		ECALtdc.setInt("TDC", 39, (int) 156611);
		ECALtdc.setInt("TDC", 40, (int) 158961);
		ECALtdc.setInt("TDC", 41, (int) 0);
		ECALtdc.setInt("TDC", 42, (int) 159290);
		ECALtdc.setInt("TDC", 43, (int) 0);
		ECALtdc.setInt("TDC", 44, (int) 158795);
		ECALtdc.setInt("TDC", 45, (int) 0);
		ECALtdc.setInt("TDC", 46, (int) 0);
		ECALtdc.setInt("TDC", 47, (int) 154313);

		testEvent.appendBank(config);
		testEvent.appendBank(ECALadc);
		testEvent.appendBank(ECALtdc);

		return testEvent;
	}

}
