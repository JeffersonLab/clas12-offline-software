package org.jlab.rec.rtpc.hit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;

public class Track {

	private HashMap<Integer,ArrayList<Integer>> _track = new HashMap<Integer,ArrayList<Integer>>(84,1.0f);
	private List<Integer> _l = new ArrayList<Integer>();
	//private int _padcount; 
	private boolean _flagTrack = false;
	
	public Track() {
		//default constructor
	}
	public Track(int time, int padnum) {
		if(!_track.containsKey(time)) {			
			addTimeSlice(time);
		}
		addPad(time,padnum);
	}
	
	public void addTimeSlice(int time) {
		_track.put(time, new ArrayList<Integer>());
	}
	
	public void addTimeSlice(int time, List<Integer> l) {
		List<Integer> templ = getTimeSlice(time);
		for(int i : l) {
			if(!templ.contains(i)) {
				templ.add(i);
			}
		}
	}
	
	public void addPad(int time, int padnum) {
		_track.get(time).add(padnum);
	}
	
	public List<Integer> getTimeSlice(int time) {
		if(!_track.containsKey(time)) {
			addTimeSlice(time);
		}
		return _track.get(time);
	}
	
	public List<Integer> getAllTimeSlices(){
		List<Integer> l = new ArrayList<Integer>();
		for(int i : _track.keySet()) {
			l.add(i);
		}
		return l;
	}
	
	public boolean padExists(int time, int padnum) {
		_l = getTimeSlice(time);
		return _l.contains(padnum);
	}
	
	public int padCountbyTime(int time) {
		_l = getTimeSlice(time);
		return _l.size();
	}
	
	public int padCountTotal() {
		int _padcount = 0;
		for(int time : _track.keySet()) {
			_padcount += padCountbyTime(time);
		}
		return _padcount;
	}
	
	public void flagTrack() {
		_flagTrack = true;
	}
	
	public boolean isTrackFlagged() {
		return _flagTrack;
	}
	
	public Set<Integer> uniquePadList() {
		List<Integer> slice = new ArrayList<Integer>();
		Set<Integer> pads = new HashSet<Integer>();
		for(int time : _track.keySet()) {
			slice = getTimeSlice(time);
			for(int pad : slice) {
				
				pads.add(pad);
				
			}
		}
		return pads;
	}
	
	public Set<Integer> PadTimeList(int pad) {
		Set<Integer> times = new HashSet<Integer>();		
		List<Integer> allslices = getAllTimeSlices(); 
		List<Integer> t = new ArrayList<Integer>();
		for(int slice : allslices) {
			t = getTimeSlice(slice);
			if(t.contains(pad)) {
				times.add(slice);
			}
		}
		return times;
	}
	
	public int uniquePadCountTotal() { 
		
		return uniquePadList().size();
		
	}
	
}
