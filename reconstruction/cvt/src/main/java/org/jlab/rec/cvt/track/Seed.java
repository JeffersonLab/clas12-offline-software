package org.jlab.rec.cvt.track;

import java.util.List;

import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.trajectory.Helix;
/**
 * 
 * @author ziegler
 *
 */
public class Seed {

	public Seed() {
		// TODO Auto-generated constructor stub
	}

	private Helix _Helix;
	private List<Cluster> _Clusters;
	private List<Cross> _Crosses;
	
	public Helix get_Helix() {
		return _Helix;
	}

	public  void set_Helix(Helix _Helix) {
		this._Helix = _Helix;
	}

	public List<Cluster> get_Clusters() {
		return _Clusters;
	}

	public void set_Clusters(List<Cluster> _Clusters) {
		this._Clusters = _Clusters;
	}

	public List<Cross> get_Crosses() {
		return _Crosses;
	}

	public void set_Crosses(List<Cross> _Crosses) {
		this._Crosses = _Crosses;
	}

	

	
}
