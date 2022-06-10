package org.jlab.rec.rtpc.hit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;

public class ReducedTrackMap {
	
	private HashMap<Integer,ReducedTrack> _map;
	private int _trackID = 0; 

	public ReducedTrackMap() {
		_map = new HashMap<Integer,ReducedTrack>();
	}
	
	public void addTrack(ReducedTrack t) {
		_trackID++;
		_map.put(_trackID, t);
	}
	
	public ReducedTrack getTrack(int trackID) {
		if(!_map.containsKey(trackID)) {
			addTrack(new ReducedTrack());
		}
		return _map.get(trackID);
	}
	
	public void updateTrack(int trackID, ReducedTrack t) {
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
		ReducedTrack child = getTrack(trackID);
		List<HitVector> l = child.getAllHits();
		ReducedTrack parent = getTrack(trackIDparent);
		for(HitVector v : l) {
                        v.flagHit(0);
			parent.addHit(v);
		}
		removeTrack(trackID);
	}

	public void mergeTracksBackbend(int trackIDparent, int trackID) {
		ReducedTrack child = getTrack(trackID);
		List<HitVector> l = child.getAllHits();
		ReducedTrack parent = getTrack(trackIDparent);
		for(HitVector v : parent.getAllHits()){
			v.flagHit(1);
		}
		for(HitVector v : l) {
		        v.flagHit(2);
			parent.addHit(v);			
		}
		removeTrack(trackID);
	}
	
}
