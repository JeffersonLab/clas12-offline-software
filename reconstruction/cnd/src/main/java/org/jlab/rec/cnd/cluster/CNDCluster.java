package org.jlab.rec.cnd.cluster;

import java.io.IOException;
import java.util.ArrayList;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cnd.hit.CndHit;

public class CNDCluster extends ArrayList<CndHit> implements Comparable<CNDCluster> {

	private int _id;
	private int _sector;
	private int _layer;
	private int _component;
	private double _x;
	private double _y;
	private double _z;
	private double _time;
	private int _nhits;
	private double _energysum;
	private int _status;
    //define layer multiplicity
    private int _layermultip;
    
    private int _layer1=0;
    private int _layer2=0;
    private int _layer3=0;
    private double _pathLengthThruBar;
    
	private ArrayList<CndHit> _cndhits;

	public CNDCluster(int id, int sector, int layer){
		_id = id;
		_sector = sector;
		_layer = layer;
		_cndhits = new ArrayList<CndHit>();
	}

    //get_veto and set_veto methods
    
    public double get_pathLengthThruBar(){
        return _pathLengthThruBar;
    }
    public void set_pathLengthThruBar(double pathLengthThruBar){
        _pathLengthThruBar = pathLengthThruBar;
    }
    

    //get_layermultip and set_layermultip methods
    
    public int get_layermultip(){
        return _layermultip;
    }
    public void set_layermultip(int layermultip){
        _layermultip = layermultip;
    }
    
    public int get_layer1(){
        return _layer1;
    }
    public void set_layer1(int layer1){
        _layer1 = layer1;
    }
    
    public int get_layer2(){
        return _layer2;
    }
    public void set_layer2(int layer2){
        _layer2 = layer2;
    }
    public int get_layer3(){
        return _layer3;
    }
    public void set_layer3(int layer3){
        _layer3 = layer3;
    }
    
    
	public int get_id(){
		return _id;
	}
	public void set_id(int id){
		_id = id;
	}
	public int get_sector(){
		return _sector;
	}
	public void set_sector(int sector){
		_sector = sector;
	}
	public int get_layer(){
		return _layer;
	}
	public void set_layer(int layer){
		_layer = layer;
	}
	public int get_component(){
		return _component;
	}
	public void set_component(int component){
		_component = component;
	}
	public double get_x(){
		return _x;
	}
	public void set_x(double x){
		_x = x;
	}
	public double get_y(){
		return _y;
	}
	public void set_y(double y){
		_y = y;
	}
	public double get_z(){
		return _z;
	}
	public void set_z(double z){
		_z = z;
	}
	public double get_time(){
		return _time;
	}
	public void set_time(double time){
		_time = time;
	}
	public int get_nhits(){
		return _nhits;
	}
	public void set_nhits(int nhits){
		_nhits = nhits;
	}
	public double get_energysum(){
		return _energysum;
	}
	public void set_energysum(double energysum){
		_energysum = energysum;
	}
	public int get_status(){
		return _status;
	}
	public void set_status(int status){
		_status = status;
	}

	public void add_cndhit(CndHit ahit){
		_cndhits.add(ahit);
	}

	@Override
	public int compareTo(CNDCluster arg) {
	/// sorted by sector, layer
		if(this.get_sector() < arg.get_sector())return 1;
		else if(this.get_sector() == arg.get_sector()){
			if(this.get_layer() < arg.get_layer())return 1;
			else return 0;
		}
		else return 0;
	}

}

