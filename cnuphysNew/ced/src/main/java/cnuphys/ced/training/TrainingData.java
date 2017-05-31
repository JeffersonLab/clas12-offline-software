package cnuphys.ced.training;

public class TrainingData {

	/** the 1-based sector containing the data */
	public int sector;
	
	/** the actual data */
	public long[] data;
	
	//the data 
	
	public TrainingData(int sector, long data[]) {
		this.sector = sector;
		this.data = data;
	}
	
	
}
