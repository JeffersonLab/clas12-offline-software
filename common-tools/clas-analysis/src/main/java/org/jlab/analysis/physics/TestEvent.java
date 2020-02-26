package org.jlab.analysis.physics;

import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.SchemaFactory;

/**
 *
 * @author naharrison
 */
public class TestEvent {


	public static HipoDataEvent getDCSector1ElectronEvent(SchemaFactory schemaFactory) {
                Event testEvent = new Event();
		
		// this event is based on a gemc event with
		// one generated electron with p=2.5, th=25, ph=0
		// (i.e. px=1.057, py=0, pz=2.266)
		// torus = -1.0 , solenoid = 0.0
		// updated to use non-linear t2d
		// updated to include mini-stagger

                Bank config = new Bank(schemaFactory.getSchema("RUN::config"), 1);
		config.putInt("run", 0, (int) 11);
		config.putInt("event", 0, (int) 1);
		config.putInt("unixtime", 0, (int) 0);
		config.putLong("trigger", 0, (long) 0);
		config.putLong("timestamp", 0, (long) 0);
		config.putByte("type", 0, (byte) 0);
		config.putByte("mode", 0, (byte) 0);
		config.putFloat("torus", 0, (float) -1.0);
		config.putFloat("solenoid", 0, (float) 0.0);
		
                Bank event = new Bank(schemaFactory.getSchema("RECHB::Event"), 1);
		event.putFloat("startTime", 0, (float) 124.25);


                int[] layer = {    1,   2,   3,   4,   5,   6,   7,   8,   9,  10
                              ,   11,  12,  13,  14,  15,  16,  17,  18,  19,  20
                              ,   21,  22,  23,  24,  25,  26,  26,  27,  28,  29
                              ,   30,  31,  31,  32,  33,  34,  35,  36};
                int[] component = {    63,  64,  63,  64,  63,  64,  64,  64,  64,  64
                                  ,   64,  64,  59,  60,  59,  60,  59,  60,  59,  59
                                  ,   59,  59,  59,  59,  52,  53,  52,  52,  52,  52
                                  ,   52,  52,  51,  52,  51,  52,  51,  51};
                int[]  TDC = {   73,  74,  79,  72,  77,  75,  21, 145,  27, 136
                             ,   29, 136, 149, 124,  99, 192,  43, 260,  89, 142
                             ,  159,  69, 243,   3,  88, 456, 547,  67, 256, 280
                             ,   56, 537, 484,  96, 220, 312,  18, 423};
      
                Bank DCtdc  = new Bank(schemaFactory.getSchema("DC::tdc"), layer.length);
		for(int i = 0; i < layer.length; i++) {
			DCtdc.putByte("sector",     i, (byte) 1);
			DCtdc.putByte("order",      i, (byte) 2);
			DCtdc.putByte("layer",      i, (byte) layer[i]);
		        DCtdc.putShort("component", i, (short) component[i]);
		        DCtdc.putInt("TDC",         i, TDC[i]);
                }


		
		testEvent.write(config);
		testEvent.write(DCtdc);
		testEvent.write(event);
                testEvent.show();
                HipoDataEvent hipoEvent = new HipoDataEvent(testEvent,schemaFactory);
                hipoEvent.show();
		return hipoEvent;
	}


	public static HipoDataEvent getCVTTestEvent(SchemaFactory schemaFactory) {
		Event testEvent = new Event();
                Bank config = new Bank(schemaFactory.getSchema("RUN::config"), 1);
		Bank BSTadc = new Bank(schemaFactory.getSchema("BST::adc"), 8);
		Bank BMTadc = new Bank(schemaFactory.getSchema("BMT::adc"), 38);
		
		// this event is based on a gemc that takes into account the new "no-shim" geometry for BMT
		// torus = -1.0 , solenoid = -1.0
    // positive muon

		config.putInt("run", 0, (int) 11);
		config.putInt("event", 0, (int) 1);
		config.putInt("unixtime", 0, (int) 0);
		config.putLong("trigger", 0, (long) 0);
		config.putLong("timestamp", 0, (long) 0);
		config.putByte("type", 0, (byte) 0);
		config.putByte("mode", 0, (byte) 0);
		config.putFloat("torus", 0, (float) -1.0);
		config.putFloat("solenoid", 0, (float) -1.0);
		
		for(int i = 0; i < 8; i++) {
			BSTadc.putByte("order", i, (byte) 0);
			BSTadc.putShort("ped", i, (short) 0);
			BSTadc.putLong("timestamp", i, (long) 0);
		}
		BSTadc.putByte("sector", 0, (byte)8 );
		BSTadc.putByte("sector", 1, (byte)8 );
		BSTadc.putByte("sector", 2, (byte)11);
		BSTadc.putByte("sector", 3, (byte)11);
		BSTadc.putByte("sector", 4, (byte)11);
		BSTadc.putByte("sector", 5, (byte)14);
		BSTadc.putByte("sector", 6, (byte)14);
		BSTadc.putByte("sector", 7, (byte)14);
	
		BSTadc.putByte("layer", 0, (byte) 1);
		BSTadc.putByte("layer", 1, (byte) 2);
		BSTadc.putByte("layer", 2, (byte) 3);
		BSTadc.putByte("layer", 3, (byte) 3);
		BSTadc.putByte("layer", 4, (byte) 4);
		BSTadc.putByte("layer", 5, (byte) 5);
		BSTadc.putByte("layer", 6, (byte) 5);
		BSTadc.putByte("layer", 7, (byte) 6);
	
		BSTadc.putShort("component", 0, (short) 150);
		BSTadc.putShort("component", 1, (short) 49 );
		BSTadc.putShort("component", 2, (short) 128);
		BSTadc.putShort("component", 3, (short) 129);
		BSTadc.putShort("component", 4, (short) 80 );
		BSTadc.putShort("component", 5, (short) 102);
		BSTadc.putShort("component", 6, (short) 101);
		BSTadc.putShort("component", 7, (short) 116);
	
		BSTadc.putInt("ADC", 0, (int) 5);
		BSTadc.putInt("ADC", 1, (int) 5);
		BSTadc.putInt("ADC", 2, (int) 3);
		BSTadc.putInt("ADC", 3, (int) 3);
		BSTadc.putInt("ADC", 4, (int) 7);
		BSTadc.putInt("ADC", 5, (int) 1);
		BSTadc.putInt("ADC", 6, (int) 1);
		BSTadc.putInt("ADC", 7, (int) 4);
	
		BSTadc.putFloat("time", 0, (float) 35.0 );
		BSTadc.putFloat("time", 1, (float) 238.0);
		BSTadc.putFloat("time", 2, (float) 56.0 );
		BSTadc.putFloat("time", 3, (float) 248.0);
		BSTadc.putFloat("time", 4, (float) 221.0);
		BSTadc.putFloat("time", 5, (float) 66.0 );
		BSTadc.putFloat("time", 6, (float) 144.0);
		BSTadc.putFloat("time", 7, (float) 86.0 );

		for(int i = 0; i < 38; i++) {
			BMTadc.putByte("sector", i, (byte) 3);
			BMTadc.putByte("order", i, (byte) 0);
			BMTadc.putFloat("time", i, (float) 0.0);
			BMTadc.putShort("ped", i, (short) 0);
			BMTadc.putInt("integral", i, (int) 0);
			BMTadc.putLong("timestamp", i, (long) 0);
		}

		BMTadc.putByte("layer",  0, (byte) 1);
		BMTadc.putByte("layer",  1, (byte) 1);
		BMTadc.putByte("layer",  2, (byte) 1);
		BMTadc.putByte("layer",  3, (byte) 1);
		BMTadc.putByte("layer",  4, (byte) 1);
		BMTadc.putByte("layer",  5, (byte) 1);
		BMTadc.putByte("layer",  6, (byte) 2);
		BMTadc.putByte("layer",  7, (byte) 2);
		BMTadc.putByte("layer",  8, (byte) 2);
		BMTadc.putByte("layer",  9, (byte) 2);
		BMTadc.putByte("layer", 10, (byte) 2);
		BMTadc.putByte("layer", 11, (byte) 2);
		BMTadc.putByte("layer", 12, (byte) 2);
		BMTadc.putByte("layer", 13, (byte) 3);
		BMTadc.putByte("layer", 14, (byte) 3);
		BMTadc.putByte("layer", 15, (byte) 3);
		BMTadc.putByte("layer", 16, (byte) 3);
		BMTadc.putByte("layer", 17, (byte) 3);
		BMTadc.putByte("layer", 18, (byte) 3);
		BMTadc.putByte("layer", 19, (byte) 4);
		BMTadc.putByte("layer", 20, (byte) 4);
		BMTadc.putByte("layer", 21, (byte) 4);
		BMTadc.putByte("layer", 22, (byte) 4);
		BMTadc.putByte("layer", 23, (byte) 4);
		BMTadc.putByte("layer", 24, (byte) 4);
		BMTadc.putByte("layer", 25, (byte) 4);
		BMTadc.putByte("layer", 26, (byte) 5);
		BMTadc.putByte("layer", 27, (byte) 5);
		BMTadc.putByte("layer", 28, (byte) 5);
		BMTadc.putByte("layer", 29, (byte) 5);
		BMTadc.putByte("layer", 30, (byte) 5);
		BMTadc.putByte("layer", 31, (byte) 5);
		BMTadc.putByte("layer", 32, (byte) 6);
		BMTadc.putByte("layer", 33, (byte) 6);
		BMTadc.putByte("layer", 34, (byte) 6);
		BMTadc.putByte("layer", 35, (byte) 6);
		BMTadc.putByte("layer", 36, (byte) 6);
		BMTadc.putByte("layer", 37, (byte) 6);

		BMTadc.putShort("component",  0, (short) 392);
		BMTadc.putShort("component",  1, (short) 393);
		BMTadc.putShort("component",  2, (short) 394);
		BMTadc.putShort("component",  3, (short) 395);
		BMTadc.putShort("component",  4, (short) 391);
		BMTadc.putShort("component",  5, (short) 396);
		BMTadc.putShort("component",  6, (short) 560);
		BMTadc.putShort("component",  7, (short) 561);
		BMTadc.putShort("component",  8, (short)  -1);
		BMTadc.putShort("component",  9, (short) 562);
		BMTadc.putShort("component", 10, (short) 563);
		BMTadc.putShort("component", 11, (short) 564);
		BMTadc.putShort("component", 12, (short) 565);
		BMTadc.putShort("component", 13, (short)  -1);
		BMTadc.putShort("component", 14, (short) 561);
		BMTadc.putShort("component", 15, (short) 560);
		BMTadc.putShort("component", 16, (short) 562);
		BMTadc.putShort("component", 17, (short) 563);
		BMTadc.putShort("component", 18, (short) 564);
		BMTadc.putShort("component", 19, (short)  -1);
		BMTadc.putShort("component", 20, (short) 462);
		BMTadc.putShort("component", 21, (short) 463);
		BMTadc.putShort("component", 22, (short) 461);
		BMTadc.putShort("component", 23, (short) 464);
		BMTadc.putShort("component", 24, (short) 460);
		BMTadc.putShort("component", 25, (short) 465);
		BMTadc.putShort("component", 26, (short)  -1);
		BMTadc.putShort("component", 27, (short) 674);
		BMTadc.putShort("component", 28, (short) 673);
		BMTadc.putShort("component", 29, (short) 675);
		BMTadc.putShort("component", 30, (short) 676);
		BMTadc.putShort("component", 31, (short) 677);
		BMTadc.putShort("component", 32, (short) 547);
		BMTadc.putShort("component", 33, (short) 546);
		BMTadc.putShort("component", 34, (short)  -1);
		BMTadc.putShort("component", 35, (short) 548);
		BMTadc.putShort("component", 36, (short) 549);
		BMTadc.putShort("component", 37, (short) 550);

		BMTadc.putInt("ADC",  0, (int)9 );
		BMTadc.putInt("ADC",  1, (int)13);
		BMTadc.putInt("ADC",  2, (int)10);
		BMTadc.putInt("ADC",  3, (int)0 );
		BMTadc.putInt("ADC",  4, (int)0 );
		BMTadc.putInt("ADC",  5, (int)0 );
		BMTadc.putInt("ADC",  6, (int)1 );
		BMTadc.putInt("ADC",  7, (int)25);
		BMTadc.putInt("ADC",  8, (int)0 );
		BMTadc.putInt("ADC",  9, (int)3 );
		BMTadc.putInt("ADC", 10, (int)1 );
		BMTadc.putInt("ADC", 11, (int)1 );
		BMTadc.putInt("ADC", 12, (int)0 );
		BMTadc.putInt("ADC", 13, (int)0 );
		BMTadc.putInt("ADC", 14, (int)14);
		BMTadc.putInt("ADC", 15, (int)0 );
		BMTadc.putInt("ADC", 16, (int)8 );
		BMTadc.putInt("ADC", 17, (int)3 );
		BMTadc.putInt("ADC", 18, (int)1 );
		BMTadc.putInt("ADC", 19, (int)0 );
		BMTadc.putInt("ADC", 20, (int)19);
		BMTadc.putInt("ADC", 21, (int)3 );
		BMTadc.putInt("ADC", 22, (int)0 );
		BMTadc.putInt("ADC", 23, (int)0 );
		BMTadc.putInt("ADC", 24, (int)0 );
		BMTadc.putInt("ADC", 25, (int)0 );
		BMTadc.putInt("ADC", 26, (int)0 );
		BMTadc.putInt("ADC", 27, (int)1 );
		BMTadc.putInt("ADC", 28, (int)0 );
		BMTadc.putInt("ADC", 29, (int)19);
		BMTadc.putInt("ADC", 30, (int)9 );
		BMTadc.putInt("ADC", 31, (int)0 );
		BMTadc.putInt("ADC", 32, (int)11);
		BMTadc.putInt("ADC", 33, (int)0 );
		BMTadc.putInt("ADC", 34, (int)0 );
		BMTadc.putInt("ADC", 35, (int)14);
		BMTadc.putInt("ADC", 36, (int)2 );
		BMTadc.putInt("ADC", 37, (int)0 );

		testEvent.write(config);
		testEvent.write(BSTadc);
		testEvent.write(BMTadc);
                HipoDataEvent hipoEvent = new HipoDataEvent(testEvent,schemaFactory);
		return hipoEvent;
	}


	public static HipoDataEvent getECSector1PhotonEvent(SchemaFactory schemaFactory) {
		Event testEvent = new Event();
                Bank config  = new Bank(schemaFactory.getSchema("RUN::config"), 1);
		Bank ECALadc = new Bank(schemaFactory.getSchema("ECAL::adc"), 48);
		Bank ECALtdc = new Bank(schemaFactory.getSchema("ECAL::tdc"), 48);
		
		// this event is based on a gemc event with
		// one generated photon with p=2.5, th=25, ph=0
		// (i.e. px=1.057, py=0, pz=2.266)
		// torus = -1.0 , solenoid = 0.0

		config.putInt("run", 0, (int) 11);
		config.putInt("event", 0, (int) 1);
		config.putInt("unixtime", 0, (int) 0);
		config.putLong("trigger", 0, (long) 0);
		config.putLong("timestamp", 0, (long) 0);
		config.putByte("type", 0, (byte) 0);
		config.putByte("mode", 0, (byte) 0);
		config.putFloat("torus", 0, (float) -1.0);
		config.putFloat("solenoid", 0, (float) 0.0);

		for(int i = 0; i < 48; i++) {
			ECALadc.putByte("sector", i, (byte) 1);
			ECALadc.putByte("order", i, (byte) 0);
			ECALadc.putFloat("time", i, (float) 0.0);
			ECALadc.putShort("ped", i, (short) 0);
			ECALtdc.putByte("sector", i, (byte) 1);
		}

		ECALadc.putByte("layer", 0, (byte) 1);
		ECALadc.putByte("layer", 1, (byte) 2);
		ECALadc.putByte("layer", 2, (byte) 3);
		ECALadc.putByte("layer", 3, (byte) 2);
		ECALadc.putByte("layer", 4, (byte) 3);
		ECALadc.putByte("layer", 5, (byte) 3);
		ECALadc.putByte("layer", 6, (byte) 3);
		ECALadc.putByte("layer", 7, (byte) 2);
		ECALadc.putByte("layer", 8, (byte) 2);
		ECALadc.putByte("layer", 9, (byte) 1);
		ECALadc.putByte("layer", 10, (byte) 1);
		ECALadc.putByte("layer", 11, (byte) 2);
		ECALadc.putByte("layer", 12, (byte) 1);
		ECALadc.putByte("layer", 13, (byte) 2);
		ECALadc.putByte("layer", 14, (byte) 3);
		ECALadc.putByte("layer", 15, (byte) 1);
		ECALadc.putByte("layer", 16, (byte) 2);
		ECALadc.putByte("layer", 17, (byte) 2);
		ECALadc.putByte("layer", 18, (byte) 2);
		ECALadc.putByte("layer", 19, (byte) 5);
		ECALadc.putByte("layer", 20, (byte) 5);
		ECALadc.putByte("layer", 21, (byte) 4);
		ECALadc.putByte("layer", 22, (byte) 7);
		ECALadc.putByte("layer", 23, (byte) 4);
		ECALadc.putByte("layer", 24, (byte) 6);
		ECALadc.putByte("layer", 25, (byte) 5);
		ECALadc.putByte("layer", 26, (byte) 6);
		ECALadc.putByte("layer", 27, (byte) 7);
		ECALadc.putByte("layer", 28, (byte) 5);
		ECALadc.putByte("layer", 29, (byte) 4);
		ECALadc.putByte("layer", 30, (byte) 4);
		ECALadc.putByte("layer", 31, (byte) 9);
		ECALadc.putByte("layer", 32, (byte) 8);
		ECALadc.putByte("layer", 33, (byte) 7);
		ECALadc.putByte("layer", 34, (byte) 8);
		ECALadc.putByte("layer", 35, (byte) 4);
		ECALadc.putByte("layer", 36, (byte) 4);
		ECALadc.putByte("layer", 37, (byte) 6);
		ECALadc.putByte("layer", 38, (byte) 4);
		ECALadc.putByte("layer", 39, (byte) 8);
		ECALadc.putByte("layer", 40, (byte) 4);
		ECALadc.putByte("layer", 41, (byte) 5);
		ECALadc.putByte("layer", 42, (byte) 6);
		ECALadc.putByte("layer", 43, (byte) 9);
		ECALadc.putByte("layer", 44, (byte) 7);
		ECALadc.putByte("layer", 45, (byte) 6);
		ECALadc.putByte("layer", 46, (byte) 5);
		ECALadc.putByte("layer", 47, (byte) 5);

		ECALadc.putShort("component", 0, (short) 58);
		ECALadc.putShort("component", 1, (short) 33);
		ECALadc.putShort("component", 2, (short) 33);
		ECALadc.putShort("component", 3, (short) 34);
		ECALadc.putShort("component", 4, (short) 32);
		ECALadc.putShort("component", 5, (short) 24);
		ECALadc.putShort("component", 6, (short) 34);
		ECALadc.putShort("component", 7, (short) 35);
		ECALadc.putShort("component", 8, (short) 32);
		ECALadc.putShort("component", 9, (short) 61);
		ECALadc.putShort("component", 10, (short) 59);
		ECALadc.putShort("component", 11, (short) 39);
		ECALadc.putShort("component", 12, (short) 57);
		ECALadc.putShort("component", 13, (short) 31);
		ECALadc.putShort("component", 14, (short) 38);
		ECALadc.putShort("component", 15, (short) 57);
		ECALadc.putShort("component", 16, (short) 34);
		ECALadc.putShort("component", 17, (short) 35);
		ECALadc.putShort("component", 18, (short) 35);
		ECALadc.putShort("component", 19, (short) 23);
		ECALadc.putShort("component", 20, (short) 24);
		ECALadc.putShort("component", 21, (short) 28);
		ECALadc.putShort("component", 22, (short) 27);
		ECALadc.putShort("component", 23, (short) 27);
		ECALadc.putShort("component", 24, (short) 23);
		ECALadc.putShort("component", 25, (short) 26);
		ECALadc.putShort("component", 26, (short) 22);
		ECALadc.putShort("component", 27, (short) 28);
		ECALadc.putShort("component", 28, (short) 21);
		ECALadc.putShort("component", 29, (short) 24);
		ECALadc.putShort("component", 30, (short) 22);
		ECALadc.putShort("component", 31, (short) 23);
		ECALadc.putShort("component", 32, (short) 22);
		ECALadc.putShort("component", 33, (short) 29);
		ECALadc.putShort("component", 34, (short) 24);
		ECALadc.putShort("component", 35, (short) 30);
		ECALadc.putShort("component", 36, (short) 26);
		ECALadc.putShort("component", 37, (short) 25);
		ECALadc.putShort("component", 38, (short) 25);
		ECALadc.putShort("component", 39, (short) 23);
		ECALadc.putShort("component", 40, (short) 29);
		ECALadc.putShort("component", 41, (short) 25);
		ECALadc.putShort("component", 42, (short) 24);
		ECALadc.putShort("component", 43, (short) 24);
		ECALadc.putShort("component", 44, (short) 26);
		ECALadc.putShort("component", 45, (short) 20);
		ECALadc.putShort("component", 46, (short) 27);
		ECALadc.putShort("component", 47, (short) 22);

		ECALadc.putInt("ADC", 0, (int) 14543);
		ECALadc.putInt("ADC", 1, (int) 10421);
		ECALadc.putInt("ADC", 2, (int) 13017);
		ECALadc.putInt("ADC", 3, (int) 758);
		ECALadc.putInt("ADC", 4, (int) 910);
		ECALadc.putInt("ADC", 5, (int) 0);
		ECALadc.putInt("ADC", 6, (int) 783);
		ECALadc.putInt("ADC", 7, (int) 23);
		ECALadc.putInt("ADC", 8, (int) 81);
		ECALadc.putInt("ADC", 9, (int) 0);
		ECALadc.putInt("ADC", 10, (int) 190);
		ECALadc.putInt("ADC", 11, (int) 0);
		ECALadc.putInt("ADC", 12, (int) 74);
		ECALadc.putInt("ADC", 13, (int) 225);
		ECALadc.putInt("ADC", 14, (int) 14);
		ECALadc.putInt("ADC", 15, (int) 0);
		ECALadc.putInt("ADC", 16, (int) 0);
		ECALadc.putInt("ADC", 17, (int) 0);
		ECALadc.putInt("ADC", 18, (int) 0);
		ECALadc.putInt("ADC", 19, (int) 3599);
		ECALadc.putInt("ADC", 20, (int) 353);
		ECALadc.putInt("ADC", 21, (int) 526);
		ECALadc.putInt("ADC", 22, (int) 1120);
		ECALadc.putInt("ADC", 23, (int) 3027);
		ECALadc.putInt("ADC", 24, (int) 2571);
		ECALadc.putInt("ADC", 25, (int) 367);
		ECALadc.putInt("ADC", 26, (int) 248);
		ECALadc.putInt("ADC", 27, (int) 0);
		ECALadc.putInt("ADC", 28, (int) 296);
		ECALadc.putInt("ADC", 29, (int) 73);
		ECALadc.putInt("ADC", 30, (int) 192);
		ECALadc.putInt("ADC", 31, (int) 361);
		ECALadc.putInt("ADC", 32, (int) 0);
		ECALadc.putInt("ADC", 33, (int) 51);
		ECALadc.putInt("ADC", 34, (int) 435);
		ECALadc.putInt("ADC", 35, (int) 0);
		ECALadc.putInt("ADC", 36, (int) 0);
		ECALadc.putInt("ADC", 37, (int) 252);
		ECALadc.putInt("ADC", 38, (int) 0);
		ECALadc.putInt("ADC", 39, (int) 872);
		ECALadc.putInt("ADC", 40, (int) 544);
		ECALadc.putInt("ADC", 41, (int) 0);
		ECALadc.putInt("ADC", 42, (int) 666);
		ECALadc.putInt("ADC", 43, (int) 0);
		ECALadc.putInt("ADC", 44, (int) 156);
		ECALadc.putInt("ADC", 45, (int) 0);
		ECALadc.putInt("ADC", 46, (int) 0);
		ECALadc.putInt("ADC", 47, (int) 391);

		ECALtdc.putByte("layer", 0, (byte) 1);
		ECALtdc.putByte("layer", 1, (byte) 2);
		ECALtdc.putByte("layer", 2, (byte) 3);
		ECALtdc.putByte("layer", 3, (byte) 2);
		ECALtdc.putByte("layer", 4, (byte) 3);
		ECALtdc.putByte("layer", 5, (byte) 3);
		ECALtdc.putByte("layer", 6, (byte) 3);
		ECALtdc.putByte("layer", 7, (byte) 2);
		ECALtdc.putByte("layer", 8, (byte) 2);
		ECALtdc.putByte("layer", 9, (byte) 1);
		ECALtdc.putByte("layer", 10, (byte) 1);
		ECALtdc.putByte("layer", 11, (byte) 2);
		ECALtdc.putByte("layer", 12, (byte) 1);
		ECALtdc.putByte("layer", 13, (byte) 2);
		ECALtdc.putByte("layer", 14, (byte) 3);
		ECALtdc.putByte("layer", 15, (byte) 1);
		ECALtdc.putByte("layer", 16, (byte) 2);
		ECALtdc.putByte("layer", 17, (byte) 2);
		ECALtdc.putByte("layer", 18, (byte) 2);
		ECALtdc.putByte("layer", 19, (byte) 5);
		ECALtdc.putByte("layer", 20, (byte) 5);
		ECALtdc.putByte("layer", 21, (byte) 4);
		ECALtdc.putByte("layer", 22, (byte) 7);
		ECALtdc.putByte("layer", 23, (byte) 4);
		ECALtdc.putByte("layer", 24, (byte) 6);
		ECALtdc.putByte("layer", 25, (byte) 5);
		ECALtdc.putByte("layer", 26, (byte) 6);
		ECALtdc.putByte("layer", 27, (byte) 7);
		ECALtdc.putByte("layer", 28, (byte) 5);
		ECALtdc.putByte("layer", 29, (byte) 4);
		ECALtdc.putByte("layer", 30, (byte) 4);
		ECALtdc.putByte("layer", 31, (byte) 9);
		ECALtdc.putByte("layer", 32, (byte) 8);
		ECALtdc.putByte("layer", 33, (byte) 7);
		ECALtdc.putByte("layer", 34, (byte) 8);
		ECALtdc.putByte("layer", 35, (byte) 4);
		ECALtdc.putByte("layer", 36, (byte) 4);
		ECALtdc.putByte("layer", 37, (byte) 6);
		ECALtdc.putByte("layer", 38, (byte) 4);
		ECALtdc.putByte("layer", 39, (byte) 8);
		ECALtdc.putByte("layer", 40, (byte) 4);
		ECALtdc.putByte("layer", 41, (byte) 5);
		ECALtdc.putByte("layer", 42, (byte) 6);
		ECALtdc.putByte("layer", 43, (byte) 9);
		ECALtdc.putByte("layer", 44, (byte) 7);
		ECALtdc.putByte("layer", 45, (byte) 6);
		ECALtdc.putByte("layer", 46, (byte) 5);
		ECALtdc.putByte("layer", 47, (byte) 5);

		ECALtdc.putShort("component", 0, (short) 58);
		ECALtdc.putShort("component", 1, (short) 33);
		ECALtdc.putShort("component", 2, (short) 33);
		ECALtdc.putShort("component", 3, (short) 34);
		ECALtdc.putShort("component", 4, (short) 32);
		ECALtdc.putShort("component", 5, (short) 24);
		ECALtdc.putShort("component", 6, (short) 34);
		ECALtdc.putShort("component", 7, (short) 35);
		ECALtdc.putShort("component", 8, (short) 32);
		ECALtdc.putShort("component", 9, (short) 61);
		ECALtdc.putShort("component", 10, (short) 59);
		ECALtdc.putShort("component", 11, (short) 39);
		ECALtdc.putShort("component", 12, (short) 57);
		ECALtdc.putShort("component", 13, (short) 31);
		ECALtdc.putShort("component", 14, (short) 38);
		ECALtdc.putShort("component", 15, (short) 57);
		ECALtdc.putShort("component", 16, (short) 34);
		ECALtdc.putShort("component", 17, (short) 35);
		ECALtdc.putShort("component", 18, (short) 35);
		ECALtdc.putShort("component", 19, (short) 23);
		ECALtdc.putShort("component", 20, (short) 24);
		ECALtdc.putShort("component", 21, (short) 28);
		ECALtdc.putShort("component", 22, (short) 27);
		ECALtdc.putShort("component", 23, (short) 27);
		ECALtdc.putShort("component", 24, (short) 23);
		ECALtdc.putShort("component", 25, (short) 26);
		ECALtdc.putShort("component", 26, (short) 22);
		ECALtdc.putShort("component", 27, (short) 28);
		ECALtdc.putShort("component", 28, (short) 21);
		ECALtdc.putShort("component", 29, (short) 24);
		ECALtdc.putShort("component", 30, (short) 22);
		ECALtdc.putShort("component", 31, (short) 23);
		ECALtdc.putShort("component", 32, (short) 22);
		ECALtdc.putShort("component", 33, (short) 29);
		ECALtdc.putShort("component", 34, (short) 24);
		ECALtdc.putShort("component", 35, (short) 30);
		ECALtdc.putShort("component", 36, (short) 26);
		ECALtdc.putShort("component", 37, (short) 25);
		ECALtdc.putShort("component", 38, (short) 25);
		ECALtdc.putShort("component", 39, (short) 23);
		ECALtdc.putShort("component", 40, (short) 29);
		ECALtdc.putShort("component", 41, (short) 25);
		ECALtdc.putShort("component", 42, (short) 24);
		ECALtdc.putShort("component", 43, (short) 24);
		ECALtdc.putShort("component", 44, (short) 26);
		ECALtdc.putShort("component", 45, (short) 20);
		ECALtdc.putShort("component", 46, (short) 27);
		ECALtdc.putShort("component", 47, (short) 22);

		ECALtdc.putByte("order", 0, (byte) 2);
		ECALtdc.putByte("order", 1, (byte) 2);
		ECALtdc.putByte("order", 2, (byte) 2);
		ECALtdc.putByte("order", 3, (byte) 2);
		ECALtdc.putByte("order", 4, (byte) 2);
		ECALtdc.putByte("order", 5, (byte) 2);
		ECALtdc.putByte("order", 6, (byte) 2);
		ECALtdc.putByte("order", 7, (byte) 2);
		ECALtdc.putByte("order", 8, (byte) 2);
		ECALtdc.putByte("order", 9, (byte) 2);
		ECALtdc.putByte("order", 10, (byte) 2);
		ECALtdc.putByte("order", 11, (byte) 2);
		ECALtdc.putByte("order", 12, (byte) 2);
		ECALtdc.putByte("order", 13, (byte) 2);
		ECALtdc.putByte("order", 14, (byte) 2);
		ECALtdc.putByte("order", 15, (byte) 2);
		ECALtdc.putByte("order", 16, (byte) 2);
		ECALtdc.putByte("order", 17, (byte) 2);
		ECALtdc.putByte("order", 18, (byte) 2);
		ECALtdc.putByte("order", 19, (byte) 1);
		ECALtdc.putByte("order", 20, (byte) 1);
		ECALtdc.putByte("order", 21, (byte) 1);
		ECALtdc.putByte("order", 22, (byte) 1);
		ECALtdc.putByte("order", 23, (byte) 1);
		ECALtdc.putByte("order", 24, (byte) 1);
		ECALtdc.putByte("order", 25, (byte) 1);
		ECALtdc.putByte("order", 26, (byte) 1);
		ECALtdc.putByte("order", 27, (byte) 1);
		ECALtdc.putByte("order", 28, (byte) 1);
		ECALtdc.putByte("order", 29, (byte) 1);
		ECALtdc.putByte("order", 30, (byte) 1);
		ECALtdc.putByte("order", 31, (byte) 1);
		ECALtdc.putByte("order", 32, (byte) 1);
		ECALtdc.putByte("order", 33, (byte) 1);
		ECALtdc.putByte("order", 34, (byte) 1);
		ECALtdc.putByte("order", 35, (byte) 1);
		ECALtdc.putByte("order", 36, (byte) 1);
		ECALtdc.putByte("order", 37, (byte) 1);
		ECALtdc.putByte("order", 38, (byte) 1);
		ECALtdc.putByte("order", 39, (byte) 1);
		ECALtdc.putByte("order", 40, (byte) 1);
		ECALtdc.putByte("order", 41, (byte) 1);
		ECALtdc.putByte("order", 42, (byte) 1);
		ECALtdc.putByte("order", 43, (byte) 1);
		ECALtdc.putByte("order", 44, (byte) 1);
		ECALtdc.putByte("order", 45, (byte) 1);
		ECALtdc.putByte("order", 46, (byte) 1);
		ECALtdc.putByte("order", 47, (byte) 1);

		ECALtdc.putInt("TDC", 0, (int) 157379);
		ECALtdc.putInt("TDC", 1, (int) 154505);
		ECALtdc.putInt("TDC", 2, (int) 154481);
		ECALtdc.putInt("TDC", 3, (int) 154733);
		ECALtdc.putInt("TDC", 4, (int) 154469);
		ECALtdc.putInt("TDC", 5, (int) 155018);
		ECALtdc.putInt("TDC", 6, (int) 155045);
		ECALtdc.putInt("TDC", 7, (int) 154860);
		ECALtdc.putInt("TDC", 8, (int) 154772);
		ECALtdc.putInt("TDC", 9, (int) 0);
		ECALtdc.putInt("TDC", 10, (int) 158643);
		ECALtdc.putInt("TDC", 11, (int) 0);
		ECALtdc.putInt("TDC", 12, (int) 160655);
		ECALtdc.putInt("TDC", 13, (int) 154097);
		ECALtdc.putInt("TDC", 14, (int) 155831);
		ECALtdc.putInt("TDC", 15, (int) 0);
		ECALtdc.putInt("TDC", 16, (int) 0);
		ECALtdc.putInt("TDC", 17, (int) 0);
		ECALtdc.putInt("TDC", 18, (int) 0);
		ECALtdc.putInt("TDC", 19, (int) 155544);
		ECALtdc.putInt("TDC", 20, (int) 155273);
		ECALtdc.putInt("TDC", 21, (int) 158296);
		ECALtdc.putInt("TDC", 22, (int) 159179);
		ECALtdc.putInt("TDC", 23, (int) 158011);
		ECALtdc.putInt("TDC", 24, (int) 158690);
		ECALtdc.putInt("TDC", 25, (int) 156844);
		ECALtdc.putInt("TDC", 26, (int) 158816);
		ECALtdc.putInt("TDC", 27, (int) 0);
		ECALtdc.putInt("TDC", 28, (int) 154777);
		ECALtdc.putInt("TDC", 29, (int) 158491);
		ECALtdc.putInt("TDC", 30, (int) 157782);
		ECALtdc.putInt("TDC", 31, (int) 159192);
		ECALtdc.putInt("TDC", 32, (int) 0);
		ECALtdc.putInt("TDC", 33, (int) 160551);
		ECALtdc.putInt("TDC", 34, (int) 157200);
		ECALtdc.putInt("TDC", 35, (int) 0);
		ECALtdc.putInt("TDC", 36, (int) 0);
		ECALtdc.putInt("TDC", 37, (int) 159032);
		ECALtdc.putInt("TDC", 38, (int) 0);
		ECALtdc.putInt("TDC", 39, (int) 156611);
		ECALtdc.putInt("TDC", 40, (int) 158961);
		ECALtdc.putInt("TDC", 41, (int) 0);
		ECALtdc.putInt("TDC", 42, (int) 159290);
		ECALtdc.putInt("TDC", 43, (int) 0);
		ECALtdc.putInt("TDC", 44, (int) 158795);
		ECALtdc.putInt("TDC", 45, (int) 0);
		ECALtdc.putInt("TDC", 46, (int) 0);
		ECALtdc.putInt("TDC", 47, (int) 154313);

		testEvent.write(config);
		testEvent.write(ECALadc);
		testEvent.write(ECALtdc);
                HipoDataEvent hipoEvent = new HipoDataEvent(testEvent,schemaFactory);
		return hipoEvent;
	}

}
