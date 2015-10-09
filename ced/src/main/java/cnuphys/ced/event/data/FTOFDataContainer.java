package cnuphys.ced.event.data;

import java.util.List;

import org.jlab.evio.clas12.EvioDataEvent;

import cnuphys.bCNU.log.Log;
import cnuphys.ced.clasio.ClasIoEventManager;

public class FTOFDataContainer extends ADataContainer {

	public static final int PANEL_1A = 0;
	public static final int PANEL_1B = 1;
	public static final int PANEL_2B = 2;

	public static final String panelNames[] = { "Panel 1A", "Panel 1B",
			"Panel 2" };

	/** ADC Left */
	public int[] ftof1a_dgtz_ADCL;

	/** ADC Right */
	public int[] ftof1a_dgtz_ADCR;

	/** hit number */
	public int[] ftof1a_dgtz_hitn;

	/** paddle number */
	public int[] ftof1a_dgtz_paddle;

	/** sector number */
	public int[] ftof1a_dgtz_sector;

	/** TDC Left */
	public int[] ftof1a_dgtz_TDCL;

	/** TDC Right */
	public int[] ftof1a_dgtz_TDCR;

	/** Average X position in local reference system */
	public double[] ftof1a_true_avgLx;

	/** Average Y position in local reference system */
	public double[] ftof1a_true_avgLy;

	/** Average Z position in local reference system */
	public double[] ftof1a_true_avgLz;

	/** Average time */
	public double[] ftof1a_true_avgT;

	/** Average X position in global reference system */
	public double[] ftof1a_true_avgX;

	/** Average Y position in global reference system */
	public double[] ftof1a_true_avgY;

	/** Average Z position in global reference system */
	public double[] ftof1a_true_avgZ;

	/** Hit1 Number */
	public int[] ftof1a_true_hitn;

	/** ID of the mother of the first particle entering the sensitive volume */
	public int[] ftof1a_true_mpid;

	/**
	 * Track ID of the mother of the first particle entering the sensitive
	 * volume
	 */
	public int[] ftof1a_true_mtid;

	/**
	 * x component of primary vertex of the mother of the particle entering the
	 * sensitive volume
	 */
	public double[] ftof1a_true_mvx;

	/**
	 * y component of primary vertex of the mother of the particle entering the
	 * sensitive volume
	 */
	public double[] ftof1a_true_mvy;

	/**
	 * z component of primary vertex of the mother of the particle entering the
	 * sensitive volume
	 */
	public double[] ftof1a_true_mvz;

	/**
	 * Track ID of the original track that generated the first particle entering
	 * the sensitive volume
	 */
	public int[] ftof1a_true_otid;

	/** ID of the first particle entering the sensitive volume */
	public int[] ftof1a_true_pid;

	/** x component of momentum of the particle entering the sensitive volume */
	public double[] ftof1a_true_px;

	/** y component of momentum of the particle entering the sensitive volume */
	public double[] ftof1a_true_py;

	/** z component of momentum of the particle entering the sensitive volume */
	public double[] ftof1a_true_pz;

	/** Track ID of the first particle entering the sensitive volume */
	public int[] ftof1a_true_tid;

	/** Total Energy Deposited */
	public double[] ftof1a_true_totEdep;

	/** Energy of the track */
	public double[] ftof1a_true_trackE;

	/**
	 * x component of primary vertex of the particle entering the sensitive
	 * volume
	 */
	public double[] ftof1a_true_vx;

	/**
	 * y component of primary vertex of the particle entering the sensitive
	 * volume
	 */
	public double[] ftof1a_true_vy;

	/**
	 * z component of primary vertex of the particle entering the sensitive
	 * volume
	 */
	public double[] ftof1a_true_vz;

	/** ADC Left */
	public int[] ftof1b_dgtz_ADCL;

	/** ADC Right */
	public int[] ftof1b_dgtz_ADCR;

	/** hit number */
	public int[] ftof1b_dgtz_hitn;

	/** paddle number */
	public int[] ftof1b_dgtz_paddle;

	/** sector number */
	public int[] ftof1b_dgtz_sector;

	/** TDC Left */
	public int[] ftof1b_dgtz_TDCL;

	/** TDC Right */
	public int[] ftof1b_dgtz_TDCR;

	/** Average X position in local reference system */
	public double[] ftof1b_true_avgLx;

	/** Average Y position in local reference system */
	public double[] ftof1b_true_avgLy;

	/** Average Z position in local reference system */
	public double[] ftof1b_true_avgLz;

	/** Average time */
	public double[] ftof1b_true_avgT;

	/** Average X position in global reference system */
	public double[] ftof1b_true_avgX;

	/** Average Y position in global reference system */
	public double[] ftof1b_true_avgY;

	/** Average Z position in global reference system */
	public double[] ftof1b_true_avgZ;

	/** Hit1 Number */
	public int[] ftof1b_true_hitn;

	/** ID of the mother of the first particle entering the sensitive volume */
	public int[] ftof1b_true_mpid;

	/**
	 * Track ID of the mother of the first particle entering the sensitive
	 * volume
	 */
	public int[] ftof1b_true_mtid;

	/**
	 * x component of primary vertex of the mother of the particle entering the
	 * sensitive volume
	 */
	public double[] ftof1b_true_mvx;

	/**
	 * y component of primary vertex of the mother of the particle entering the
	 * sensitive volume
	 */
	public double[] ftof1b_true_mvy;

	/**
	 * z component of primary vertex of the mother of the particle entering the
	 * sensitive volume
	 */
	public double[] ftof1b_true_mvz;

	/**
	 * Track ID of the original track that generated the first particle entering
	 * the sensitive volume
	 */
	public int[] ftof1b_true_otid;

	/** ID of the first particle entering the sensitive volume */
	public int[] ftof1b_true_pid;

	/** x component of momentum of the particle entering the sensitive volume */
	public double[] ftof1b_true_px;

	/** y component of momentum of the particle entering the sensitive volume */
	public double[] ftof1b_true_py;

	/** z component of momentum of the particle entering the sensitive volume */
	public double[] ftof1b_true_pz;

	/** Track ID of the first particle entering the sensitive volume */
	public int[] ftof1b_true_tid;

	/** Total Energy Deposited */
	public double[] ftof1b_true_totEdep;

	/** Energy of the track */
	public double[] ftof1b_true_trackE;

	/**
	 * x component of primary vertex of the particle entering the sensitive
	 * volume
	 */
	public double[] ftof1b_true_vx;

	/**
	 * y component of primary vertex of the particle entering the sensitive
	 * volume
	 */
	public double[] ftof1b_true_vy;

	/**
	 * z component of primary vertex of the particle entering the sensitive
	 * volume
	 */
	public double[] ftof1b_true_vz;

	/** ADC Left */
	public int[] ftof2b_dgtz_ADCL;

	/** ADC Right */
	public int[] ftof2b_dgtz_ADCR;

	/** hit number */
	public int[] ftof2b_dgtz_hitn;

	/** paddle number */
	public int[] ftof2b_dgtz_paddle;

	/** sector number */
	public int[] ftof2b_dgtz_sector;

	/** TDC Left */
	public int[] ftof2b_dgtz_TDCL;

	/** TDC Right */
	public int[] ftof2b_dgtz_TDCR;

	/** Average X position in local reference system */
	public double[] ftof2b_true_avgLx;

	/** Average Y position in local reference system */
	public double[] ftof2b_true_avgLy;

	/** Average Z position in local reference system */
	public double[] ftof2b_true_avgLz;

	/** Average time */
	public double[] ftof2b_true_avgT;

	/** Average X position in global reference system */
	public double[] ftof2b_true_avgX;

	/** Average Y position in global reference system */
	public double[] ftof2b_true_avgY;

	/** Average Z position in global reference system */
	public double[] ftof2b_true_avgZ;

	/** Hit1 Number */
	public int[] ftof2b_true_hitn;

	/** ID of the mother of the first particle entering the sensitive volume */
	public int[] ftof2b_true_mpid;

	/**
	 * Track ID of the mother of the first particle entering the sensitive
	 * volume
	 */
	public int[] ftof2b_true_mtid;

	/**
	 * x component of primary vertex of the mother of the particle entering the
	 * sensitive volume
	 */
	public double[] ftof2b_true_mvx;

	/**
	 * y component of primary vertex of the mother of the particle entering the
	 * sensitive volume
	 */
	public double[] ftof2b_true_mvy;

	/**
	 * z component of primary vertex of the mother of the particle entering the
	 * sensitive volume
	 */
	public double[] ftof2b_true_mvz;

	/**
	 * Track ID of the original track that generated the first particle entering
	 * the sensitive volume
	 */
	public int[] ftof2b_true_otid;

	/** ID of the first particle entering the sensitive volume */
	public int[] ftof2b_true_pid;

	/** x component of momentum of the particle entering the sensitive volume */
	public double[] ftof2b_true_px;

	/** y component of momentum of the particle entering the sensitive volume */
	public double[] ftof2b_true_py;

	/** z component of momentum of the particle entering the sensitive volume */
	public double[] ftof2b_true_pz;

	/** Track ID of the first particle entering the sensitive volume */
	public int[] ftof2b_true_tid;

	/** Total Energy Deposited */
	public double[] ftof2b_true_totEdep;

	/** Energy of the track */
	public double[] ftof2b_true_trackE;

	/**
	 * x component of primary vertex of the particle entering the sensitive
	 * volume
	 */
	public double[] ftof2b_true_vx;

	/**
	 * y component of primary vertex of the particle entering the sensitive
	 * volume
	 */
	public double[] ftof2b_true_vy;

	/**
	 * z component of primary vertex of the particle entering the sensitive
	 * volume
	 */
	public double[] ftof2b_true_vz;

	/** cluster energy deposited (sum of hit energies) */
	public float[] ftofrec_ftofclusters_energy;

	/** uncertainty cluster energy deposited */
	public float[] ftofrec_ftofclusters_energy_unc;

	/** paddle id of hit with lowest paddle id in cluster */
	public int[] ftofrec_ftofclusters_paddle_id;

	/**
	 * status of paddle (1-15 for single hit cluster up to 0101010101-1515151515
	 * for five hit cluster 15 is best status 1 worst see
	 * ftof.reconstruction.PaddleConvertor
	 */
	public int[] ftofrec_ftofclusters_paddle_status;

	/** panel id (1 2 3) for (1a 1b 2) */
	public int[] ftofrec_ftofclusters_panel_id;

	/** sector number (1-6) */
	public int[] ftofrec_ftofclusters_sector;

	/**
	 * cluster time (which is either an energy-weighted average of hits times or
	 * the time of the earliest hit in the cluster depending on configuration of
	 * ftof software)
	 */
	public float[] ftofrec_ftofclusters_time;

	/** uncertainty cluster time */
	public float[] ftofrec_ftofclusters_time_unc;

	/**
	 * cluster x position (sector coords this is either a simple average of hit
	 * x positions or the x position of the earliest hit in the cluster
	 * depending on configuration of ftof software)
	 */
	public float[] ftofrec_ftofclusters_x;

	/** uncertainty cluster x position (sector coords) */
	public float[] ftofrec_ftofclusters_x_unc;

	/**
	 * cluster y position (sector coords this is either an energy weighted
	 * average of hit y positions or the y position of the earliest hit in the
	 * cluster depending on configuration of ftof software)
	 */
	public float[] ftofrec_ftofclusters_y;

	/** uncertainty cluster y position (sector coords) */
	public float[] ftofrec_ftofclusters_y_unc;

	/**
	 * cluster z position (sector coords this is either a simple average of hit
	 * z positions or the z position of the earliest hit in the cluster
	 * depending on configuration of ftof software)
	 */
	public float[] ftofrec_ftofclusters_z;

	/** uncertainty cluster z position (sector coords) */
	public float[] ftofrec_ftofclusters_z_unc;

	/** hit energy deposited */
	public float[] ftofrec_ftofhits_energy;

	/** uncertainty hit energy deposited */
	public float[] ftofrec_ftofhits_energy_unc;

	/** paddle id */
	public int[] ftofrec_ftofhits_paddle_id;

	/**
	 * status of paddle (1 to 15) 15 is the best status 1 the worst see FTOF
	 * class ftof.reconstruction.PaddleConvertor
	 */
	public int[] ftofrec_ftofhits_paddle_status;

	/** panel id (1 2 3) for (1a 1b 2) */
	public int[] ftofrec_ftofhits_panel_id;

	/** sector number (1-6) */
	public int[] ftofrec_ftofhits_sector;

	/** hit time */
	public float[] ftofrec_ftofhits_time;

	/** uncertainty hit time */
	public float[] ftofrec_ftofhits_time_unc;

	/** hit x position (sector coords) */
	public float[] ftofrec_ftofhits_x;

	/** uncertainty hit x position (sector coords) */
	public float[] ftofrec_ftofhits_x_unc;

	/** hit y position (sector coords) */
	public float[] ftofrec_ftofhits_y;

	/** uncertainty hit y position (sector coords) */
	public float[] ftofrec_ftofhits_y_unc;

	/** hit z position (sector coords) */
	public float[] ftofrec_ftofhits_z;

	/** uncertainty hit z position (sector coords) */
	public float[] ftofrec_ftofhits_z_unc;

	/** left energy deposited */
	public float[] ftofrec_rawhits_energy_left;

	/** left energy deposited uncertainty */
	public float[] ftofrec_rawhits_energy_left_unc;

	/** right energy deposited */
	public float[] ftofrec_rawhits_energy_right;

	/** right energy deposited uncertainty */
	public float[] ftofrec_rawhits_energy_right_unc;

	/** paddle id */
	public int[] ftofrec_rawhits_paddle_id;

	/**
	 * status of paddle (1 to 15) 15 is the best status 1 the worst see FTOF
	 * class ftof.reconstruction.PaddleConvertor
	 */
	public int[] ftofrec_rawhits_paddle_status;

	/** panel id (1 2 3) for (1a 1b 2) */
	public int[] ftofrec_rawhits_panel_id;

	/** sector number (1-6) */
	public int[] ftofrec_rawhits_sector;

	/** left time */
	public float[] ftofrec_rawhits_time_left;

	/** left time uncertainty */
	public float[] ftofrec_rawhits_time_left_unc;

	/** right time */
	public float[] ftofrec_rawhits_time_right;

	/** right time uncertainty */
	public float[] ftofrec_rawhits_time_right_unc;

	/** view (1=u, 2=v, 3=w) */
	public int[] pcal_dgtz_view;

	public FTOFDataContainer(ClasIoEventManager eventManager) {
		super(eventManager);
	}

	@Override
	public int getHitCount(int option) {
		int hitCount = 0;

		switch (option) {
		case PANEL_1A:
			hitCount = (ftof1a_dgtz_sector == null) ? 0
					: ftof1a_dgtz_sector.length;
			break;
		case PANEL_1B:
			hitCount = (ftof1b_dgtz_sector == null) ? 0
					: ftof1b_dgtz_sector.length;
			break;
		case PANEL_2B:
			hitCount = (ftof2b_dgtz_sector == null) ? 0
					: ftof2b_dgtz_sector.length;
			break;
		}

		return hitCount;
	}

	@Override
	public void load(EvioDataEvent event) {
		if (event == null) {
			return;
		}

		if (event.hasBank("FTOF1A::dgtz")) {
			ftof1a_dgtz_ADCL = event.getInt("FTOF1A::dgtz.ADCL");
			ftof1a_dgtz_ADCR = event.getInt("FTOF1A::dgtz.ADCR");
			ftof1a_dgtz_hitn = event.getInt("FTOF1A::dgtz.hitn");
			ftof1a_dgtz_paddle = event.getInt("FTOF1A::dgtz.paddle");
			ftof1a_dgtz_sector = event.getInt("FTOF1A::dgtz.sector");
			ftof1a_dgtz_TDCL = event.getInt("FTOF1A::dgtz.TDCL");
			ftof1a_dgtz_TDCR = event.getInt("FTOF1A::dgtz.TDCR");
		} // FTOF1A::dgtz

		if (event.hasBank("FTOF1A::true")) {
			ftof1a_true_avgLx = event.getDouble("FTOF1A::true.avgLx");
			ftof1a_true_avgLy = event.getDouble("FTOF1A::true.avgLy");
			ftof1a_true_avgLz = event.getDouble("FTOF1A::true.avgLz");
			ftof1a_true_avgT = event.getDouble("FTOF1A::true.avgT");
			ftof1a_true_avgX = event.getDouble("FTOF1A::true.avgX");
			ftof1a_true_avgY = event.getDouble("FTOF1A::true.avgY");
			ftof1a_true_avgZ = event.getDouble("FTOF1A::true.avgZ");
			ftof1a_true_hitn = event.getInt("FTOF1A::true.hitn");
			ftof1a_true_mpid = event.getInt("FTOF1A::true.mpid");
			ftof1a_true_mtid = event.getInt("FTOF1A::true.mtid");
			ftof1a_true_mvx = event.getDouble("FTOF1A::true.mvx");
			ftof1a_true_mvy = event.getDouble("FTOF1A::true.mvy");
			ftof1a_true_mvz = event.getDouble("FTOF1A::true.mvz");
			ftof1a_true_otid = event.getInt("FTOF1A::true.otid");
			ftof1a_true_pid = event.getInt("FTOF1A::true.pid");
			ftof1a_true_px = event.getDouble("FTOF1A::true.px");
			ftof1a_true_py = event.getDouble("FTOF1A::true.py");
			ftof1a_true_pz = event.getDouble("FTOF1A::true.pz");
			ftof1a_true_tid = event.getInt("FTOF1A::true.tid");
			ftof1a_true_totEdep = event.getDouble("FTOF1A::true.totEdep");
			ftof1a_true_trackE = event.getDouble("FTOF1A::true.trackE");
			ftof1a_true_vx = event.getDouble("FTOF1A::true.vx");
			ftof1a_true_vy = event.getDouble("FTOF1A::true.vy");
			ftof1a_true_vz = event.getDouble("FTOF1A::true.vz");
		} // FTOF1A::true

		if (event.hasBank("FTOF1B::dgtz")) {
			ftof1b_dgtz_ADCL = event.getInt("FTOF1B::dgtz.ADCL");
			ftof1b_dgtz_ADCR = event.getInt("FTOF1B::dgtz.ADCR");
			ftof1b_dgtz_hitn = event.getInt("FTOF1B::dgtz.hitn");
			ftof1b_dgtz_paddle = event.getInt("FTOF1B::dgtz.paddle");
			ftof1b_dgtz_sector = event.getInt("FTOF1B::dgtz.sector");
			ftof1b_dgtz_TDCL = event.getInt("FTOF1B::dgtz.TDCL");
			ftof1b_dgtz_TDCR = event.getInt("FTOF1B::dgtz.TDCR");
		} // FTOF1B::dgtz

		if (event.hasBank("FTOF1B::true")) {
			ftof1b_true_avgLx = event.getDouble("FTOF1B::true.avgLx");
			ftof1b_true_avgLy = event.getDouble("FTOF1B::true.avgLy");
			ftof1b_true_avgLz = event.getDouble("FTOF1B::true.avgLz");
			ftof1b_true_avgT = event.getDouble("FTOF1B::true.avgT");
			ftof1b_true_avgX = event.getDouble("FTOF1B::true.avgX");
			ftof1b_true_avgY = event.getDouble("FTOF1B::true.avgY");
			ftof1b_true_avgZ = event.getDouble("FTOF1B::true.avgZ");
			ftof1b_true_hitn = event.getInt("FTOF1B::true.hitn");
			ftof1b_true_mpid = event.getInt("FTOF1B::true.mpid");
			ftof1b_true_mtid = event.getInt("FTOF1B::true.mtid");
			ftof1b_true_mvx = event.getDouble("FTOF1B::true.mvx");
			ftof1b_true_mvy = event.getDouble("FTOF1B::true.mvy");
			ftof1b_true_mvz = event.getDouble("FTOF1B::true.mvz");
			ftof1b_true_otid = event.getInt("FTOF1B::true.otid");
			ftof1b_true_pid = event.getInt("FTOF1B::true.pid");
			ftof1b_true_px = event.getDouble("FTOF1B::true.px");
			ftof1b_true_py = event.getDouble("FTOF1B::true.py");
			ftof1b_true_pz = event.getDouble("FTOF1B::true.pz");
			ftof1b_true_tid = event.getInt("FTOF1B::true.tid");
			ftof1b_true_totEdep = event.getDouble("FTOF1B::true.totEdep");
			ftof1b_true_trackE = event.getDouble("FTOF1B::true.trackE");
			ftof1b_true_vx = event.getDouble("FTOF1B::true.vx");
			ftof1b_true_vy = event.getDouble("FTOF1B::true.vy");
			ftof1b_true_vz = event.getDouble("FTOF1B::true.vz");
		} // FTOF1B::true

		if (event.hasBank("FTOF2B::dgtz")) {
			ftof2b_dgtz_ADCL = event.getInt("FTOF2B::dgtz.ADCL");
			ftof2b_dgtz_ADCR = event.getInt("FTOF2B::dgtz.ADCR");
			ftof2b_dgtz_hitn = event.getInt("FTOF2B::dgtz.hitn");
			ftof2b_dgtz_paddle = event.getInt("FTOF2B::dgtz.paddle");
			ftof2b_dgtz_sector = event.getInt("FTOF2B::dgtz.sector");
			ftof2b_dgtz_TDCL = event.getInt("FTOF2B::dgtz.TDCL");
			ftof2b_dgtz_TDCR = event.getInt("FTOF2B::dgtz.TDCR");
		} // FTOF2B::dgtz

		if (event.hasBank("FTOF2B::true")) {
			ftof2b_true_avgLx = event.getDouble("FTOF2B::true.avgLx");
			ftof2b_true_avgLy = event.getDouble("FTOF2B::true.avgLy");
			ftof2b_true_avgLz = event.getDouble("FTOF2B::true.avgLz");
			ftof2b_true_avgT = event.getDouble("FTOF2B::true.avgT");
			ftof2b_true_avgX = event.getDouble("FTOF2B::true.avgX");
			ftof2b_true_avgY = event.getDouble("FTOF2B::true.avgY");
			ftof2b_true_avgZ = event.getDouble("FTOF2B::true.avgZ");
			ftof2b_true_hitn = event.getInt("FTOF2B::true.hitn");
			ftof2b_true_mpid = event.getInt("FTOF2B::true.mpid");
			ftof2b_true_mtid = event.getInt("FTOF2B::true.mtid");
			ftof2b_true_mvx = event.getDouble("FTOF2B::true.mvx");
			ftof2b_true_mvy = event.getDouble("FTOF2B::true.mvy");
			ftof2b_true_mvz = event.getDouble("FTOF2B::true.mvz");
			ftof2b_true_otid = event.getInt("FTOF2B::true.otid");
			ftof2b_true_pid = event.getInt("FTOF2B::true.pid");
			ftof2b_true_px = event.getDouble("FTOF2B::true.px");
			ftof2b_true_py = event.getDouble("FTOF2B::true.py");
			ftof2b_true_pz = event.getDouble("FTOF2B::true.pz");
			ftof2b_true_tid = event.getInt("FTOF2B::true.tid");
			ftof2b_true_totEdep = event.getDouble("FTOF2B::true.totEdep");
			ftof2b_true_trackE = event.getDouble("FTOF2B::true.trackE");
			ftof2b_true_vx = event.getDouble("FTOF2B::true.vx");
			ftof2b_true_vy = event.getDouble("FTOF2B::true.vy");
			ftof2b_true_vz = event.getDouble("FTOF2B::true.vz");
		} // FTOF2B::true

		if (event.hasBank("FTOFRec::ftofclusters")) {
			ftofrec_ftofclusters_energy = event
					.getFloat("FTOFRec::ftofclusters.energy");
			ftofrec_ftofclusters_energy_unc = event
					.getFloat("FTOFRec::ftofclusters.energy_unc");
			ftofrec_ftofclusters_paddle_id = event
					.getInt("FTOFRec::ftofclusters.paddle_id");
			ftofrec_ftofclusters_paddle_status = event
					.getInt("FTOFRec::ftofclusters.paddle_status");
			ftofrec_ftofclusters_panel_id = event
					.getInt("FTOFRec::ftofclusters.panel_id");
			ftofrec_ftofclusters_sector = event
					.getInt("FTOFRec::ftofclusters.sector");
			ftofrec_ftofclusters_time = event
					.getFloat("FTOFRec::ftofclusters.time");
			ftofrec_ftofclusters_time_unc = event
					.getFloat("FTOFRec::ftofclusters.time_unc");
			ftofrec_ftofclusters_x = event.getFloat("FTOFRec::ftofclusters.x");
			ftofrec_ftofclusters_x_unc = event
					.getFloat("FTOFRec::ftofclusters.x_unc");
			ftofrec_ftofclusters_y = event.getFloat("FTOFRec::ftofclusters.y");
			ftofrec_ftofclusters_y_unc = event
					.getFloat("FTOFRec::ftofclusters.y_unc");
			ftofrec_ftofclusters_z = event.getFloat("FTOFRec::ftofclusters.z");
			ftofrec_ftofclusters_z_unc = event
					.getFloat("FTOFRec::ftofclusters.z_unc");
		} // FTOFRec::ftofclusters

		if (event.hasBank("FTOFRec::ftofhits")) {
			ftofrec_ftofhits_energy = event
					.getFloat("FTOFRec::ftofhits.energy");
			ftofrec_ftofhits_energy_unc = event
					.getFloat("FTOFRec::ftofhits.energy_unc");
			ftofrec_ftofhits_paddle_id = event
					.getInt("FTOFRec::ftofhits.paddle_id");
			ftofrec_ftofhits_paddle_status = event
					.getInt("FTOFRec::ftofhits.paddle_status");
			ftofrec_ftofhits_panel_id = event
					.getInt("FTOFRec::ftofhits.panel_id");
			ftofrec_ftofhits_sector = event.getInt("FTOFRec::ftofhits.sector");
			ftofrec_ftofhits_time = event.getFloat("FTOFRec::ftofhits.time");
			ftofrec_ftofhits_time_unc = event
					.getFloat("FTOFRec::ftofhits.time_unc");
			ftofrec_ftofhits_x = event.getFloat("FTOFRec::ftofhits.x");
			ftofrec_ftofhits_x_unc = event.getFloat("FTOFRec::ftofhits.x_unc");
			ftofrec_ftofhits_y = event.getFloat("FTOFRec::ftofhits.y");
			ftofrec_ftofhits_y_unc = event.getFloat("FTOFRec::ftofhits.y_unc");
			ftofrec_ftofhits_z = event.getFloat("FTOFRec::ftofhits.z");
			ftofrec_ftofhits_z_unc = event.getFloat("FTOFRec::ftofhits.z_unc");
		} // FTOFRec::ftofhits

		if (event.hasBank("FTOFRec::rawhits")) {
			ftofrec_rawhits_energy_left = event
					.getFloat("FTOFRec::rawhits.energy_left");
			ftofrec_rawhits_energy_left_unc = event
					.getFloat("FTOFRec::rawhits.energy_left_unc");
			ftofrec_rawhits_energy_right = event
					.getFloat("FTOFRec::rawhits.energy_right");
			ftofrec_rawhits_energy_right_unc = event
					.getFloat("FTOFRec::rawhits.energy_right_unc");
			ftofrec_rawhits_paddle_id = event
					.getInt("FTOFRec::rawhits.paddle_id");
			ftofrec_rawhits_paddle_status = event
					.getInt("FTOFRec::rawhits.paddle_status");
			ftofrec_rawhits_panel_id = event
					.getInt("FTOFRec::rawhits.panel_id");
			ftofrec_rawhits_sector = event.getInt("FTOFRec::rawhits.sector");
			ftofrec_rawhits_time_left = event
					.getFloat("FTOFRec::rawhits.time_left");
			ftofrec_rawhits_time_left_unc = event
					.getFloat("FTOFRec::rawhits.time_left_unc");
			ftofrec_rawhits_time_right = event
					.getFloat("FTOFRec::rawhits.time_right");
			ftofrec_rawhits_time_right_unc = event
					.getFloat("FTOFRec::rawhits.time_right_unc");
		} // FTOFRec::rawhits

	} // load

	@Override
	public void clear() {
		ftof1a_dgtz_ADCL = null;
		ftof1a_dgtz_ADCR = null;
		ftof1a_dgtz_hitn = null;
		ftof1a_dgtz_paddle = null;
		ftof1a_dgtz_sector = null;
		ftof1a_dgtz_TDCL = null;
		ftof1a_dgtz_TDCR = null;
		ftof1a_true_avgLx = null;
		ftof1a_true_avgLy = null;
		ftof1a_true_avgLz = null;
		ftof1a_true_avgT = null;
		ftof1a_true_avgX = null;
		ftof1a_true_avgY = null;
		ftof1a_true_avgZ = null;
		ftof1a_true_hitn = null;
		ftof1a_true_mpid = null;
		ftof1a_true_mtid = null;
		ftof1a_true_mvx = null;
		ftof1a_true_mvy = null;
		ftof1a_true_mvz = null;
		ftof1a_true_otid = null;
		ftof1a_true_pid = null;
		ftof1a_true_px = null;
		ftof1a_true_py = null;
		ftof1a_true_pz = null;
		ftof1a_true_tid = null;
		ftof1a_true_totEdep = null;
		ftof1a_true_trackE = null;
		ftof1a_true_vx = null;
		ftof1a_true_vy = null;
		ftof1a_true_vz = null;
		ftof1b_dgtz_ADCL = null;
		ftof1b_dgtz_ADCR = null;
		ftof1b_dgtz_hitn = null;
		ftof1b_dgtz_paddle = null;
		ftof1b_dgtz_sector = null;
		ftof1b_dgtz_TDCL = null;
		ftof1b_dgtz_TDCR = null;
		ftof1b_true_avgLx = null;
		ftof1b_true_avgLy = null;
		ftof1b_true_avgLz = null;
		ftof1b_true_avgT = null;
		ftof1b_true_avgX = null;
		ftof1b_true_avgY = null;
		ftof1b_true_avgZ = null;
		ftof1b_true_hitn = null;
		ftof1b_true_mpid = null;
		ftof1b_true_mtid = null;
		ftof1b_true_mvx = null;
		ftof1b_true_mvy = null;
		ftof1b_true_mvz = null;
		ftof1b_true_otid = null;
		ftof1b_true_pid = null;
		ftof1b_true_px = null;
		ftof1b_true_py = null;
		ftof1b_true_pz = null;
		ftof1b_true_tid = null;
		ftof1b_true_totEdep = null;
		ftof1b_true_trackE = null;
		ftof1b_true_vx = null;
		ftof1b_true_vy = null;
		ftof1b_true_vz = null;
		ftof2b_dgtz_ADCL = null;
		ftof2b_dgtz_ADCR = null;
		ftof2b_dgtz_hitn = null;
		ftof2b_dgtz_paddle = null;
		ftof2b_dgtz_sector = null;
		ftof2b_dgtz_TDCL = null;
		ftof2b_dgtz_TDCR = null;
		ftof2b_true_avgLx = null;
		ftof2b_true_avgLy = null;
		ftof2b_true_avgLz = null;
		ftof2b_true_avgT = null;
		ftof2b_true_avgX = null;
		ftof2b_true_avgY = null;
		ftof2b_true_avgZ = null;
		ftof2b_true_hitn = null;
		ftof2b_true_mpid = null;
		ftof2b_true_mtid = null;
		ftof2b_true_mvx = null;
		ftof2b_true_mvy = null;
		ftof2b_true_mvz = null;
		ftof2b_true_otid = null;
		ftof2b_true_pid = null;
		ftof2b_true_px = null;
		ftof2b_true_py = null;
		ftof2b_true_pz = null;
		ftof2b_true_tid = null;
		ftof2b_true_totEdep = null;
		ftof2b_true_trackE = null;
		ftof2b_true_vx = null;
		ftof2b_true_vy = null;
		ftof2b_true_vz = null;
		ftofrec_ftofclusters_energy = null;
		ftofrec_ftofclusters_energy_unc = null;
		ftofrec_ftofclusters_paddle_id = null;
		ftofrec_ftofclusters_paddle_status = null;
		ftofrec_ftofclusters_panel_id = null;
		ftofrec_ftofclusters_sector = null;
		ftofrec_ftofclusters_time = null;
		ftofrec_ftofclusters_time_unc = null;
		ftofrec_ftofclusters_x = null;
		ftofrec_ftofclusters_x_unc = null;
		ftofrec_ftofclusters_y = null;
		ftofrec_ftofclusters_y_unc = null;
		ftofrec_ftofclusters_z = null;
		ftofrec_ftofclusters_z_unc = null;
		ftofrec_ftofhits_energy = null;
		ftofrec_ftofhits_energy_unc = null;
		ftofrec_ftofhits_paddle_id = null;
		ftofrec_ftofhits_paddle_status = null;
		ftofrec_ftofhits_panel_id = null;
		ftofrec_ftofhits_sector = null;
		ftofrec_ftofhits_time = null;
		ftofrec_ftofhits_time_unc = null;
		ftofrec_ftofhits_x = null;
		ftofrec_ftofhits_x_unc = null;
		ftofrec_ftofhits_y = null;
		ftofrec_ftofhits_y_unc = null;
		ftofrec_ftofhits_z = null;
		ftofrec_ftofhits_z_unc = null;
		ftofrec_rawhits_energy_left = null;
		ftofrec_rawhits_energy_left_unc = null;
		ftofrec_rawhits_energy_right = null;
		ftofrec_rawhits_energy_right_unc = null;
		ftofrec_rawhits_paddle_id = null;
		ftofrec_rawhits_paddle_status = null;
		ftofrec_rawhits_panel_id = null;
		ftofrec_rawhits_sector = null;
		ftofrec_rawhits_time_left = null;
		ftofrec_rawhits_time_left_unc = null;
		ftofrec_rawhits_time_right = null;
		ftofrec_rawhits_time_right_unc = null;
	} // clear

	@Override
	public void addPreliminaryFeedback(int hitIndex, int option,
			List<String> feedbackStrings) {
	}

	@Override
	public void addTrueFeedback(int hitIndex, int option,
			List<String> feedbackStrings) {
	}

	@Override
	public void addDgtzFeedback(int hitIndex, int option,
			List<String> feedbackStrings) {

		switch (option) {
		case FTOFDataContainer.PANEL_1A:
			feedbackStrings.add(dgtxColor + "panel_1A  sector "
					+ ftof1a_dgtz_sector[hitIndex] + "  paddle "
					+ ftof1a_dgtz_paddle[hitIndex]);

			feedbackStrings.add(dgtxColor + "adc_left "
					+ ftof1a_dgtz_ADCL[hitIndex] + "  adc_right "
					+ ftof1a_dgtz_ADCR[hitIndex]);
			feedbackStrings.add(dgtxColor + "tdc_left "
					+ ftof1a_dgtz_TDCL[hitIndex] + "  tdc_right "
					+ ftof1a_dgtz_TDCR[hitIndex]);
			break;
		case FTOFDataContainer.PANEL_1B:
			feedbackStrings.add(dgtxColor + "panel_1B  sector "
					+ ftof1b_dgtz_sector[hitIndex] + "  paddle "
					+ ftof1b_dgtz_paddle[hitIndex]);

			feedbackStrings.add(dgtxColor + "adc_left "
					+ ftof1b_dgtz_ADCL[hitIndex] + "  adc_right "
					+ ftof1b_dgtz_ADCR[hitIndex]);
			feedbackStrings.add(dgtxColor + "tdc_left "
					+ ftof1b_dgtz_TDCL[hitIndex] + "  tdc_right "
					+ ftof1b_dgtz_TDCR[hitIndex]);
			break;
		case FTOFDataContainer.PANEL_2B:
			feedbackStrings.add(dgtxColor + "panel_2  sector "
					+ ftof2b_dgtz_sector[hitIndex] + "  paddle "
					+ ftof2b_dgtz_paddle[hitIndex]);

			feedbackStrings.add(dgtxColor + "adc_left "
					+ ftof2b_dgtz_ADCL[hitIndex] + "  adc_right "
					+ ftof2b_dgtz_ADCR[hitIndex]);
			feedbackStrings.add(dgtxColor + "tdc_left "
					+ ftof2b_dgtz_TDCL[hitIndex] + "  tdc_right "
					+ ftof2b_dgtz_TDCR[hitIndex]);
			break;
		}

	}

	@Override
	public void addFinalFeedback(int option, List<String> feedbackStrings) {
	}

	@Override
	public void addReconstructedFeedback(int option,
			List<String> feedbackStrings) {
	}

	@Override
	public void finalEventPrep(EvioDataEvent event) {
		extractUniqueLundIds(ftof1a_true_pid);
		extractUniqueLundIds(ftof1b_true_pid);
		extractUniqueLundIds(ftof2b_true_pid);
	}

	public static String getName(int panelType) {
		if ((panelType < 0) || (panelType > 2)) {
			return "???";
		} else {
			return panelNames[panelType];
		}
	}

	/**
	 * Get the index of the ftof hit
	 * 
	 * @param sect
	 *            the 1-based sector
	 * @param paddle
	 *            the 1-based paddle
	 * @return the index of a hit with these parameters, or -1 if not found
	 */
	public int getHitIndex(int sect, int paddle, int panelType) {

		int sector[] = null;
		int paddles[] = null;

		switch (panelType) {
		case FTOFDataContainer.PANEL_1A:
			sector = ftof1a_dgtz_sector;
			paddles = ftof1a_dgtz_paddle;
			break;
		case FTOFDataContainer.PANEL_1B:
			sector = ftof1b_dgtz_sector;
			paddles = ftof1b_dgtz_paddle;
			break;
		case FTOFDataContainer.PANEL_2B:
			sector = ftof2b_dgtz_sector;
			paddles = ftof2b_dgtz_paddle;
			break;
		}

		if (sector == null) {
			return -1;
		}

		if (paddles == null) {
			Log.getInstance()
					.warning("null paddles array in FTOFDataContainer");
			return -1;
		}

		for (int i = 0; i < getHitCount(panelType); i++) {
			if ((sect == sector[i]) && (paddle == paddles[i])) {
				// System.err.println("Computed Hit Index: " + i + "  out of " +
				// getHitCount(panelType) + "  panelType: " + panelType);
				return i;
			}
		}
		return -1;
	}

}
