package cnuphys.bCNU.util;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class SoundUtils {
	 public static float SAMPLE_RATE = 8000f;

	 /**
	  * Play a tone
	  * @param hz the frequency in Hz
	  * @param msecs the duration in msec
	  * @throws LineUnavailableException
	  */
	  public static void tone(int hz, int msecs) 
	     throws LineUnavailableException  {
	     tone(hz, msecs, 1.0);
	  }

	  /**
	   * Play a tone
	   * @param hz the frequency in Hz
	   * @param msecs the duration in msec
	   * @param vol a volume scale (1 = "default")
	   * @throws LineUnavailableException
	   */
	  public static void tone(int hz, int msecs, double vol)
	      throws LineUnavailableException  {
	    byte[] buf = new byte[1];
	    AudioFormat af = 
	        new AudioFormat(
	            SAMPLE_RATE, // sampleRate
	            8,           // sampleSizeInBits
	            1,           // channels
	            true,        // signed
	            false);      // bigEndian
	    SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
	    sdl.open(af);
	    sdl.start();
	    for (int i=0; i < msecs*8; i++) {
	      double angle = i / (SAMPLE_RATE / hz) * 2.0 * Math.PI;
	      buf[0] = (byte)(Math.sin(angle) * 127.0 * vol);
	      sdl.write(buf,0,1);
	    }
	    sdl.drain();
	    sdl.stop();
	    sdl.close();
	  }
	  
	  /**
	   * Play a tone based on a data value
	   * @param minHz the minimum frequency
	   * @param maxHz the maximum frequency
	   * @param msecs the duration in ms
	   * @param minVal the minimum data value
	   * @param maxVal the maximum data value
	   * @param val the data value
	   * @param underHz the frequency if the value is below the minimum
	   * @param overHz the frequency if the value is above the maximum
	   * @param volume 
	   */
	  public static void playData(int minHz, int maxHz, int msecs, double minVal, double maxVal, double val, int underHz, int overHz, double volume) {
		  int freq;
		  
		  if (val < minVal) {
			  freq = minHz;
			  msecs *= 2;
		  }
		  else if (val > maxVal) {
			  freq = maxHz;
			  msecs *= 2;
		  }
		  else {
			  double fract = (val - minVal)/(maxVal - minVal);
			  freq = minHz + (int)(fract*(maxHz-minHz));
		  }
		  try {
			tone(freq, msecs, volume);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	  }
	  

	  public static void main(String[] args) {
//		  
//		  Synthesizer synthesizer = null;
//		  try {
//			synthesizer = MidiSystem.getSynthesizer();
//			Instrument[] instruments = synthesizer.getAvailableInstruments();
//			for (Instrument instrument : instruments) {
//				System.out.println(instrument.getName());
//			}
//			synthesizer.loadInstrument(instruments[0]);
//		    Receiver synthRcvr = synthesizer.getReceiver();
//		    
//			ShortMessage myMsg = new ShortMessage();
//		    // Play the note Middle C (60) moderately loud
//		    // (velocity = 93)on channel 4 (zero-based).
//		    try {
//				myMsg.setMessage(ShortMessage.NOTE_ON, 4, 60, 93);
//			} catch (InvalidMidiDataException e) {
//				e.printStackTrace();
//			} 
//		    synthRcvr.send(myMsg, -1); // -1 means no time stamp			
//
//
//		} catch (MidiUnavailableException e) {
//			e.printStackTrace();
//		}
//		  

		  
		  
//		  Random random = new Random();
//		  for (int i = 0; i < 100; i++) {
//			  double val = random.nextDouble();
//			  playData(2000, 3000, 200, 0.1, 0.9, val, 600, 5000, 1.);
//		  }
		  
		  
//		  try {
//		  System.err.println("1000 Hz .25 sec");
//	    SoundUtils.tone(1000, 250);
//	    Thread.sleep(1000);
//	    
//		  System.err.println("1000 Hz 1.0 sec");
//	    SoundUtils.tone(1000, 1000);
//	    Thread.sleep(1000);
//	    
//		  System.err.println("5000 Hz 0.2 sec");
//	    SoundUtils.tone(5000, 200);
//	    Thread.sleep(1000);
//	    
//		  System.err.println("400 Hz 0.5 sec");
//	    SoundUtils.tone(400,500);
//	    Thread.sleep(1000);
//	    
//		  System.err.println("400 Hz 0.5 sec louder");
//	    SoundUtils.tone(400, 500, 1.5);
//		  System.err.println("Done");
//		  }
//		  catch (Exception e) {
//			  e.printStackTrace();
//		  }

System.exit(1);
	  }
}
