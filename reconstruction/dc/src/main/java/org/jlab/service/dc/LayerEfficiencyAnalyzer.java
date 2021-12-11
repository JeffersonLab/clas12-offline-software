package org.jlab.service.dc;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jlab.clas.swimtools.MagFieldsEngine;

import org.jlab.io.hipo.HipoDataSource;
import org.jlab.rec.dc.Constants;

import org.jlab.utils.options.OptionParser;

import org.jlab.groot.data.H1F;
import org.jlab.groot.data.TDirectory;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.task.DataSourceProcessorPane;
import org.jlab.io.task.IDataEventListener;
import org.jlab.rec.dc.banks.HitReader;
import org.jlab.rec.dc.cluster.ClusterCleanerUtilities;
import org.jlab.rec.dc.cluster.ClusterFinder;
import org.jlab.rec.dc.cluster.ClusterFitter;
import org.jlab.rec.dc.cluster.FittedCluster;
import org.jlab.rec.dc.hit.FittedHit;
import org.jlab.rec.dc.segment.Segment;
import org.jlab.rec.dc.segment.SegmentFinder;
import org.jlab.rec.dc.timetodistance.TimeToDistanceEstimator;
import org.jlab.rec.dc.trajectory.SegmentTrajectory;

public class LayerEfficiencyAnalyzer extends DCEngine implements IDataEventListener{
    

    public LayerEfficiencyAnalyzer(){
        super("LE");
        tde = new TimeToDistanceEstimator();
        //plotting stuff
        mainPanel = new JPanel();	
        mainPanel.setLayout(new BorderLayout());

        tabbedPane 	= new JTabbedPane();

        //processorPane = new DataSourceProcessorPane();
        //processorPane.setUpdateRate(10);

        mainPanel.add(tabbedPane);
       // mainPanel.add(processorPane,BorderLayout.PAGE_END);

        //this.processorPane.addEventListener(this);
    }
    private TimeToDistanceEstimator tde;
    //plotting stuff
    JPanel                  mainPanel 	= null;
    DataSourceProcessorPane processorPane 	= null;
    private JTabbedPane     tabbedPane      = null;


    private EmbeddedCanvas can1 = null;
    private EmbeddedCanvas can2 = null;
    private EmbeddedCanvas can3 = null;
    private EmbeddedCanvas can4 = null;
    private EmbeddedCanvas can5 = null;
    private EmbeddedCanvas can6 = null;
    private EmbeddedCanvas can7 = null;
    private EmbeddedCanvas can8 = null;
    private EmbeddedCanvas can9 = null;
    private EmbeddedCanvas can10 = null;
    private EmbeddedCanvas can11 = null;
    private EmbeddedCanvas can12 = null;
    
    public double[] maxDoca = new double[6];
    
    @Override
    public boolean init() {
        
        this.setOptions();
        Constants.getInstance().initialize("LayerEfficiency");
        Constants.getInstance().setT2D(1);
        this.LoadTables();
        this.getBanks().init("TimBasedTrkg", "HB", "TB");
        this.setDropBanks();
        
        maxDoca[0]=0.8;maxDoca[1]=0.9;maxDoca[2]=1.3;maxDoca[3]=1.4;maxDoca[4]=1.9;maxDoca[5]=2.0;
        //plots
        can1 = new EmbeddedCanvas(); 
        can1.divide(2, 3);
        can2 = new EmbeddedCanvas(); 
        can2.divide(2, 3);
        can3 = new EmbeddedCanvas(); 
        can3.divide(2, 3);
        can4 = new EmbeddedCanvas(); 
        can4.divide(2, 3);
        can5 = new EmbeddedCanvas(); 
        can5.divide(2, 3);
        can6 = new EmbeddedCanvas(); 
        can6.divide(2, 3);
        can7 = new EmbeddedCanvas(); 
        can7.divide(2, 3);
        can8 = new EmbeddedCanvas(); 
        can8.divide(2, 3);
        can9 = new EmbeddedCanvas(); 
        can9.divide(2, 3);
        can10 = new EmbeddedCanvas(); 
        can10.divide(2, 3);
        can11 = new EmbeddedCanvas(); 
        can11.divide(2, 3);
        can12 = new EmbeddedCanvas(); 
        can12.divide(2, 3);
        // create histograms
        for(int si =0; si<6; si++) {
            LayerEffs.add(new HashMap<>());
            LayerEffsTrkD.add(new HashMap<>());
            for(int i =0; i<6; i++) {
                LayerEffs.get(si).put(new Coordinate(i),
                                new H1F("Sector-"+si+" layer efficiencies" + (i + 1), "superlayer" + (i + 1), 6, 0.5, 6.5));
                LayerEffs.get(si).get(new Coordinate(i)).setTitleX("Layer" );
                LayerEffs.get(si).get(new Coordinate(i)).setTitleY("Efficiency for superlayer "+(i+1) );


                LayerEffsTrkD.get(si).put(new Coordinate(i),
                                new H1F("Sector-"+si+" layer inefficiencies vs trkDoca" + (i + 1), "superlayer" + (i + 1), 40, 0.0, 4.0));
                LayerEffsTrkD.get(si).get(new Coordinate(i)).setTitleX("Track Doca (cm)" );
                LayerEffsTrkD.get(si).get(new Coordinate(i)).setTitleY("Inefficiency for superlayer "+(i+1) );

                this.setHistosAtt(LayerEffs.get(si).get(new Coordinate(i)), 4);
                this.setHistosAtt(LayerEffsTrkD.get(si).get(new Coordinate(i)), 2);
            }
        }
        tabbedPane.add("Sector-1 Layer Efficiencies", can1);
        tabbedPane.add("Sector-2 Layer Efficiencies", can2);
        tabbedPane.add("Sector-3 Layer Efficiencies", can3);
        tabbedPane.add("Sector-4 Layer Efficiencies", can4);
        tabbedPane.add("Sector-5 Layer Efficiencies", can5);
        tabbedPane.add("Sector-6 Layer Efficiencies", can6);

        tabbedPane.add("Sector-1 Inefficiencies vs Track Doca", can7);
        tabbedPane.add("Sector-2 Inefficiencies vs Track Doca", can8);
        tabbedPane.add("Sector-3 Inefficiencies vs Track Doca", can9);
        tabbedPane.add("Sector-4 Inefficiencies vs Track Doca", can10);
        tabbedPane.add("Sector-5 Inefficiencies vs Track Doca", can11);
        tabbedPane.add("Sector-6 Inefficiencies vs Track Doca", can12);
        
        return true;
    }

    @Override
    public void setDropBanks() {
        super.registerOutputBank("TimeBasedTrkg::TBSegmentTrajectory");
    }
    
    @Override
    public void dataEventAction(DataEvent event) {
        ProcessLayerEffs(event);
    }

    @Override
    public void timerUpdate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void resetEventListener() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void setHistosAtt(H1F h, int colorCode) {
        h.getAttributes().setLineWidth(2);
        h.getAttributes().setMarkerStyle(8);
        h.getAttributes().setMarkerColor(colorCode);
        h.getAttributes().setLineColor(colorCode);
    }
    
    

    public class Coordinate {
        private Integer[] size;

	public Coordinate(Integer... size) {
		this.size = size;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(size);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
            if (this == obj)
                    return true;
            if (obj == null)
                    return false;
            if (getClass() != obj.getClass())
                    return false;
            Coordinate other = (Coordinate) obj;
            return Arrays.equals(size, other.size);
	}
    }

    private final ArrayList<HashMap<Coordinate, H1F>> LayerEffs = new ArrayList<HashMap<Coordinate, H1F>>();
    private final ArrayList<HashMap<Coordinate, H1F>> LayerEffsTrkD = new ArrayList<HashMap<Coordinate, H1F>>();
    
    @Override
    public boolean processDataEvent(DataEvent event) {
        
        int run = this.getRun(event);
        if(run==0) return true;
        
        //LOGGER.log(Level.FINE, " RUNNING TIME BASED....................................");
        ClusterFitter cf = new ClusterFitter();
        ClusterCleanerUtilities ct = new ClusterCleanerUtilities();

        List<FittedCluster> clusters = new ArrayList<>();
        List<Segment> segments = new ArrayList<>();
        
        //instantiate bank writer
        HitReader hitRead = new HitReader(this.getBanks());

            hitRead.read_HBHits(event, 
            super.getConstantsManager().getConstants(run, Constants.DOCARES),
            super.getConstantsManager().getConstants(run, Constants.TIME2DIST),
            super.getConstantsManager().getConstants(run, Constants.T0CORRECTION),
            Constants.getInstance().dcDetector, tde);
        //hitRead.read_TBHits(event, 
        //    super.getConstantsManager().getConstants(newRun, Constants.DOCARES),
        //    super.getConstantsManager().getConstants(newRun, Constants.TIME2DIST), tde, Constants.getT0(), Constants.getT0Err());
        List<FittedHit> hits = new ArrayList<>();
        //I) get the hits
        if(hitRead.get_HBHits()==null)
            return true;
        if(!hitRead.get_HBHits().isEmpty()) {
            hits = hitRead.get_HBHits();
        } else {
            return true;
        }

        //II) process the hits
        //1) exit if hit list is empty
        if(hits.isEmpty() ) {
                return true;
        }

        //2) find the clusters from these hits
        ClusterFinder clusFinder = new ClusterFinder();

        clusters = clusFinder.FindTimeBasedClusters(event, hits, cf, ct, 
                super.getConstantsManager().getConstants(run, Constants.TIME2DIST), Constants.getInstance().dcDetector, tde);

        if(clusters.isEmpty()) {
            return true;
        }

        //3) find the segments from the fitted clusters
        SegmentFinder segFinder = new SegmentFinder();

        List<FittedCluster> pclusters = segFinder.selectTimeBasedSegments(clusters);

        segments =  segFinder.get_Segments(pclusters, event, Constants.getInstance().dcDetector, true);
        
        if(segments!=null && segments.size()>0) {
            DataBank bankE = event.createBank("TimeBasedTrkg::TBSegmentTrajectory", segments.size() * 6);
            int index = 0;
            for (Segment aSeglist : segments) {
                if (aSeglist.get_Id() == -1) {
                    continue;
                }
                SegmentTrajectory trj = aSeglist.get_Trajectory();
                for (int l = 0; l < 6; l++) {
                    bankE.setShort("segmentID", index, (short) trj.get_SegmentId());
                    bankE.setByte("sector", index, (byte) trj.get_Sector());
                    bankE.setByte("superlayer", index, (byte) trj.get_Superlayer());
                    bankE.setByte("layer", index, (byte) (l + 1));
                    bankE.setShort("matchedHitID", index, (short) trj.getMatchedHitId()[l]);
                    bankE.setFloat("trkDoca", index, (float) trj.getTrkDoca()[l]);
                    index++;
                }
            }
            
            event.appendBank(bankE);
        }
        return true;
    }

	int[][][] totLay = new int[6][6][6];
	int[][][] effLay = new int[6][6][6];
	
	int[][][] totLayA = new int[6][6][40];
	int[][][] effLayA = new int[6][6][40];
	
	float trkDBinning = (float) ((float) 4.0/40.0);
	private void ProcessLayerEffs(DataEvent event) {
		if(event.hasBank("TimeBasedTrkg::TBSegmentTrajectory")==false)
                    return;
		DataBank Bank = event.getBank("TimeBasedTrkg::TBSegmentTrajectory") ;
		int nrows =  Bank.rows();
		//Bank.show(); LOGGER.log(Level.FINE, " NUMBER OF ENTRIES IN BANK = "+nrows);
		for (int i = 0; i < nrows; i++) {
			totLay[Bank.getByte("sector", i)-1][Bank.getByte("superlayer", i)-1][Bank.getByte("layer", i)-1]++;
			
			//LOGGER.log(Level.FINE, " Layer eff denom for ["+Bank.getByte("sector", i)+"]["+ Bank.getByte("superlayer", i)+"]["+Bank.getByte("layer", i)+"] = "+totLay[Bank.getByte("sector", i)-1][Bank.getByte("superlayer", i)-1][Bank.getByte("layer", i)-1]);
			if(Bank.getShort("matchedHitID", i)!=-1) {
				effLay[Bank.getByte("sector", i)-1][Bank.getByte("superlayer", i)-1][Bank.getByte("layer", i)-1]++;
			}
			//LOGGER.log(Level.FINE, " Layer eff num for ["+Bank.getByte("sector", i)+"]["+ Bank.getByte("superlayer", i)+"]["+Bank.getByte("layer", i)+"] = "+effLay[Bank.getByte("sector", i)-1][Bank.getByte("superlayer", i)-1][Bank.getByte("layer", i)-1]);
			
			if(Math.abs(Bank.getFloat("trkDoca", i))<4.0) {
				totLayA[Bank.getByte("sector", i)-1][Bank.getByte("superlayer", i)-1][(int)((Math.floor(Math.abs(Bank.getFloat("trkDoca", i))/trkDBinning)))]++;
				if(Bank.getShort("matchedHitID", i)==-1) {
					effLayA[Bank.getByte("sector", i)-1][Bank.getByte("superlayer", i)-1][(int)((Math.floor(Math.abs(Bank.getFloat("trkDoca", i))/trkDBinning)))]++;
				}
			}
			
			if(totLay[Bank.getByte("sector", i)-1][Bank.getByte("superlayer", i)-1][Bank.getByte("layer", i)-1]>0) {
                            float d = effLay[Bank.getByte("sector", i)-1][Bank.getByte("superlayer", i)-1][Bank.getByte("layer", i)-1];
                            float n = totLay[Bank.getByte("sector", i)-1][Bank.getByte("superlayer", i)-1][Bank.getByte("layer", i)-1];
                            float err = (float) (Math.sqrt(d*(d/n+1))/n);
                            
                            LayerEffs.get(Bank.getByte("sector", i)-1).get(new Coordinate(Bank.getByte("superlayer", i) - 1))
						.setBinContent(Bank.getByte("layer", i)-1,(float)100*d/ (float)n);
			    LayerEffs.get(Bank.getByte("sector", i)-1).get(new Coordinate(Bank.getByte("superlayer", i) - 1))
					.setBinError(Bank.getByte("layer", i)-1,(float)100*err);
                            if(Math.abs(Bank.getFloat("trkDoca", i))<4.0) {
						float ddc = effLayA[Bank.getByte("sector", i)-1][Bank.getByte("superlayer", i)-1][(int)((Math.floor(Math.abs(Bank.getFloat("trkDoca", i))/trkDBinning)))];
						float ndc = totLayA[Bank.getByte("sector", i)-1][Bank.getByte("superlayer", i)-1][(int)((Math.floor(Math.abs(Bank.getFloat("trkDoca", i))/trkDBinning)))];
						float errdc = (float) (Math.sqrt(ddc*(ddc/ndc+1))/ndc);
						LayerEffsTrkD.get(Bank.getByte("sector", i)-1)
						.get(new Coordinate(Bank.getByte("superlayer", i) - 1))
						.setBinContent((int)((Math.floor(Math.abs(Bank.getFloat("trkDoca", i))/trkDBinning))),(float)100*ddc/ (float)ndc);
						LayerEffsTrkD.get(Bank.getByte("sector", i)-1)
						.get(new Coordinate(Bank.getByte("superlayer", i) - 1))
						.setBinError((int)((Math.floor(Math.abs(Bank.getFloat("trkDoca", i))/trkDBinning))),(float)100*errdc);
                            }
                        }
                }
        }

    
    private void drawPlots() {
        for (int i = 0; i < 6; i++) {

            can1.cd(i);
            can1.draw(LayerEffs.get(0).get(new Coordinate(i)), "E");
            can2.cd(i);
            can2.draw(LayerEffs.get(1).get(new Coordinate(i)), "E");
            can3.cd(i);
            can3.draw(LayerEffs.get(2).get(new Coordinate(i)), "E");
            can4.cd(i);
            can4.draw(LayerEffs.get(3).get(new Coordinate(i)), "E");
            can5.cd(i);
            can5.draw(LayerEffs.get(4).get(new Coordinate(i)), "E");
            can6.cd(i);
            can6.draw(LayerEffs.get(5).get(new Coordinate(i)), "E");
            
            can7.cd(i);
            can7.draw(LayerEffsTrkD.get(0).get(new Coordinate(i)), "E");
            can8.cd(i);
            can8.draw(LayerEffsTrkD.get(1).get(new Coordinate(i)), "E");
            can9.cd(i);
            can9.draw(LayerEffsTrkD.get(2).get(new Coordinate(i)), "E");
            can10.cd(i);
            can10.draw(LayerEffsTrkD.get(3).get(new Coordinate(i)), "E");
            can11.cd(i);
            can11.draw(LayerEffsTrkD.get(4).get(new Coordinate(i)), "E");
            can12.cd(i);
            can12.draw(LayerEffsTrkD.get(5).get(new Coordinate(i)), "E");
            
            can7.getPad(i).getAxisX().setRange(0, this.maxDoca[i]);
            can8.getPad(i).getAxisX().setRange(0, this.maxDoca[i]);
            can9.getPad(i).getAxisX().setRange(0, this.maxDoca[i]);
            can10.getPad(i).getAxisX().setRange(0, this.maxDoca[i]);
            can11.getPad(i).getAxisX().setRange(0, this.maxDoca[i]);
            can12.getPad(i).getAxisX().setRange(0, this.maxDoca[i]);
            can7.getPad(i).getAxisY().setRange(-2, 45);
            can8.getPad(i).getAxisY().setRange(-2, 45);
            can9.getPad(i).getAxisY().setRange(-2, 45);
            can10.getPad(i).getAxisY().setRange(-2, 45);
            can11.getPad(i).getAxisY().setRange(-2, 45);
            can12.getPad(i).getAxisY().setRange(-2, 45);
        }
        can1.update();
        can2.update();
        can3.update();
        can4.update();
        can5.update();
        can6.update();
        can7.update();
        can8.update();
        can9.update();
        can10.update();
        can11.update();
        can12.update();
	}
    
    public void saveHistosToFile(String fileName) {
        // TXT table summary FILE //
        TDirectory dir = new TDirectory();
        String folder = "/LayerEffs";
        dir.mkdir(folder);
        dir.cd(folder);
                
        for (int i = 0; i < 6; i++) {
            for (int si = 0; si < 6; si++) {
                dir.addDataSet(LayerEffs.get(si).get(new Coordinate(i)));
                dir.addDataSet(LayerEffsTrkD.get(si).get(new Coordinate(i)));
            }
        }
        LOGGER.log(Level.INFO, "Saving histograms to file " + fileName);
        dir.writeFile(fileName);
    }
   
   
    public static void main(String[] args) {
        JFrame frame = new JFrame("DC ANALYSIS");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension screensize = null;
        screensize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int) (screensize.getHeight() * .75 * 1.618), (int) (screensize.getHeight() * .75));
             
        
        MagFieldsEngine enf = new MagFieldsEngine();
        enf.init();
        LayerEfficiencyAnalyzer tm = new LayerEfficiencyAnalyzer();
        tm.init();
        
        frame.add(tm.mainPanel);
        frame.setVisible(true);   
        
        OptionParser parser = new OptionParser("dclayereffs-anal");
        parser.addOption("-i","");
        parser.parse(args);
        
        if(parser.hasOption("-i")==true){
            String inputFile    = parser.getOption("-i").stringValue();
            int counter =0;
            HipoDataSource reader = new HipoDataSource();
            reader.open(inputFile);
            while (reader.hasEvent()) {
                counter++;
                DataEvent event = reader.getNextEvent();
                tm.processDataEvent(event);
                tm.ProcessLayerEffs(event);
                //event.show();
                if(counter%1000==0) {
                    LOGGER.log(Level.INFO, "processed " + counter + " events");
                    tm.drawPlots();
                }
            }
            tm.drawPlots();
            tm.saveHistosToFile("dclayereffs.hipo");
        }
    }
   
}
