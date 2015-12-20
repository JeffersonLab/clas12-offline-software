package cnuphys.ced.event.data;

public class DetectorComponent {

	public short sector;
	public short superlayer;
	public short layer;
	public short component;
	
	public DetectorComponent(short sect, short supl, short lay, short cid) {
		sector = sect;
		superlayer = supl;
		layer = lay;
		component = cid;
	}
	
	public String hashKey() {
		return sector + "$" + superlayer + "$" + layer + "$" + component;
	}
}
