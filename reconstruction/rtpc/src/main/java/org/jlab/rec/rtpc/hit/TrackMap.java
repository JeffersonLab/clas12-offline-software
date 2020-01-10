package org.jlab.rec.rtpc.hit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;

public class TrackMap {
	
	private HashMap<Integer,Track> _map;
	private int _trackID = 0; 

	public TrackMap() {
		_map = new HashMap<Integer,Track>();
	}
	
	public void addTrack(Track t) {
		_trackID++;
		_map.put(_trackID, t);
	}
	
	public Track getTrack(int trackID) {
		if(!_map.containsKey(trackID)) {
			addTrack(new Track());
		}
		return _map.get(trackID);
	}
	
	public void updateTrack(int trackID, Track t) {
		_map.put(trackID, t);
	}
	
	public void removeTrack(int trackID) {
		_map.remove(trackID);
	}
	
	public List<Integer> getAllTrackIDs() {
		List<Integer> l = new ArrayList<Integer>();
		for(int i : _map.keySet()) {
			if(!l.contains(i)) { l.add(i);}
		}
		return l; 
	}
	
	public int getLastTrackID() {
		return _trackID;
	}
	
	public void mergeTracks(int trackIDparent, int trackID) {
		Track child = getTrack(trackID);
		List<Integer> l = child.getAllTimeSlices();
		Track parent = getTrack(trackIDparent);
		for(int time : l) {
			parent.addTimeSlice(time,child.getTimeSlice(time));
		}
		//updateTrack(trackIDparent, parent);
		removeTrack(trackID);
	}
	
}
