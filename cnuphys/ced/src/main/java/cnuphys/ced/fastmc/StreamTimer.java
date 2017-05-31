package cnuphys.ced.fastmc;

public class StreamTimer {
	
	public static final int READ = 0;
	public static final int GET = 1;
	public static final int SWIM = 2;
	public static final int GETHITS = 3;
	public static final int STORE = 4;
	public static final int TRAJ2D = 5;
	public static final int SWIM2 = 6;
	

	private long startTime;
	private long totalTime;
	private long start[] = new long[7];
	private long duration[] = new long[7];
	private boolean timing;
	
	public void restart() {
		timing = true;
		for (int i = 0; i < duration.length; i++) {
			duration[i] = 0L;
		}
		totalTime = 0L;
		startTime = System.nanoTime();
	}
	
	public void done() {
		totalTime = System.nanoTime() - startTime;
		timing = false;
	}
	
	public void start(int index) {
		start[index] = System.nanoTime();
	}
	
	public void stop(int index) {
		duration[index] = duration[index] + (System.nanoTime() - start[index]);
	}
	
	
	public boolean isTiming() {
		return timing;
	}
	
	public void report() {
		totalTime = System.nanoTime() - startTime;
		System.err.println("-------------");
		System.err.println(" READ TIME: " + duration[READ]/1.0e9);
		System.err.println("  GET TIME: " + duration[GET]/1.0e9);
		System.err.println(" SWIM TIME: " + duration[SWIM]/1.0e9);
		System.err.println("SWIM2 TIME: " + duration[SWIM2]/1.0e9);
		System.err.println(" TRAJ TIME: " + duration[TRAJ2D]/1.0e9);
		System.err.println(" HITS TIME: " + duration[GETHITS]/1.0e9);
		System.err.println("STORE TIME: " + duration[STORE]/1.0e9);
		System.err.println("TOTAL TIME: " + totalTime/1.0e9);
		System.err.println("-------------");
	}
}
