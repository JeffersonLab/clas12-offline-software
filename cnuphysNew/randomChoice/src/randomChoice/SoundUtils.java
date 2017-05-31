package randomChoice;

import java.util.Random;

import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class SoundUtils {
	public static float SAMPLE_RATE = 8000f;

	/**
	 * Play a tone
	 * 
	 * @param hz
	 *            the frequency in Hz
	 * @param msecs
	 *            the duration in msec
	 * @throws LineUnavailableException
	 */
	public static void tone(int hz, int msecs) throws LineUnavailableException {
		tone(hz, msecs, 1.0);
	}

	/**
	 * Play a tone
	 * 
	 * @param hz
	 *            the frequency in Hz
	 * @param msecs
	 *            the duration in msec
	 * @param vol
	 *            a volume scale (1 = "default")
	 * @throws LineUnavailableException
	 */
	public static void tone(int hz, int msecs, double vol) throws LineUnavailableException {
		byte[] buf = new byte[1];
		AudioFormat af = new AudioFormat(SAMPLE_RATE, // sampleRate
				8, // sampleSizeInBits
				1, // channels
				true, // signed
				false); // bigEndian
		SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
		sdl.open(af);
		sdl.start();
		for (int i = 0; i < msecs * 8; i++) {
			double angle = i / (SAMPLE_RATE / hz) * 2.0 * Math.PI;
			buf[0] = (byte) (Math.sin(angle) * 127.0 * vol);
			sdl.write(buf, 0, 1);
		}
		sdl.drain();
		sdl.stop();
		sdl.close();
	}

	/**
	 * Play a tone based on a data value
	 * 
	 * @param minHz
	 *            the minimum frequency
	 * @param maxHz
	 *            the maximum frequency
	 * @param msecs
	 *            the duration in ms
	 * @param minVal
	 *            the minimum data value
	 * @param maxVal
	 *            the maximum data value
	 * @param val
	 *            the data value
	 * @param underHz
	 *            the frequency if the value is below the minimum
	 * @param overHz
	 *            the frequency if the value is above the maximum
	 * @param volume
	 */
	public static void playData(int minHz, int maxHz, int msecs, double minVal, double maxVal, double val, int underHz,
			int overHz, double volume) {
		int freq;

		if (val < minVal) {
			freq = minHz;
			msecs *= 2;
		} else if (val > maxVal) {
			freq = maxHz;
			msecs *= 2;
		} else {
			double fract = (val - minVal) / (maxVal - minVal);
			freq = minHz + (int) (fract * (maxHz - minHz));
		}
		try {
			tone(freq, msecs, volume);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	public static void randomize(ISoundDone callback, int seconds) {
		Runnable runnable = new Runnable() {

			final int msec = 100;
			@Override
			public void run() {
				boolean done = false;
				long start = System.currentTimeMillis();
				while (!done) {
					
					int hz = 600 + (int)(2000*Math.random());
					
					try {
						tone(hz, msec);
					} catch (LineUnavailableException e1) {
						e1.printStackTrace();
					}
					
					try {
						Thread.sleep(60);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					long playtime = (System.currentTimeMillis() - start)/1000;
					done = (playtime > seconds);
				}
				
				if (callback != null) {
					callback.soundDone();
				}
				
			}
			
		};
		
		Thread thread = new Thread(runnable);
		thread.start();
	}

	public static void main(String[] args) {

		ISoundDone callback = new ISoundDone() {

			@Override
			public void soundDone() {
				System.err.println("Sound Done");
			}
			
		};
		
		randomize(callback, 10);
//		System.err.println("Done");
//		System.exit(1);
	}
}
