package cnuphys.fastMCed.snr;

import java.io.Serializable;

import cnuphys.snr.ExtendedWord;

public class SegmentSet implements Comparable<SegmentSet>, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9013901298231511907L;
	private long _words[] = new long[12];
	public int length = 12;
	
	private ReducedParticleRecord _particleRecord;
	
	/**
	 * 
	 * @param particleRecord
	 * @param words
	 */
	public SegmentSet(ExtendedWord... ewords) {
		_words[0] = ewords[0].getWords()[0];
		_words[1] = ewords[0].getWords()[1];
		_words[2] = ewords[1].getWords()[0];
		_words[3] = ewords[1].getWords()[1];
		_words[4] = ewords[2].getWords()[0];
		_words[5] = ewords[2].getWords()[1];
		_words[6] = ewords[3].getWords()[0];
		_words[7] = ewords[3].getWords()[1];
		_words[8] = ewords[4].getWords()[0];
		_words[9] = ewords[4].getWords()[1];
		_words[10] = ewords[5].getWords()[0];
		_words[11] = ewords[5].getWords()[1];
	}

	/**
	 * Get the associated general particle record
	 * @return the associated general particle record
	 */
	public ReducedParticleRecord getReducedParticleRecord() {
		return _particleRecord;
	}
	
	public void setReducedParticleRecord(ReducedParticleRecord pr) {
		_particleRecord = pr; 
	}
	
	@Override
	public int compareTo(SegmentSet o) {
		for (int i = 0; i < length; i++) {
			if (_words[i] > o._words[i]) {
				return 1;
			}
			if (_words[i] < o._words[i]) {
				return -1;
			}
		}
		return 0;
	}

	

}
