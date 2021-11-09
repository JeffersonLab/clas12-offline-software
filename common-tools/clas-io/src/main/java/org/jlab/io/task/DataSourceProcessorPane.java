package org.jlab.io.task;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TimerTask;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;
import org.jlab.io.evio.EvioETSource;
import org.jlab.io.evio.EvioSource;
import org.jlab.io.hipo3.Hipo3DataSource;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoRingSource;
import org.jlab.io.ui.ConnectionDialog;
import org.jlab.io.ui.DialogUtilities;

/**
 *
 * @author gavalian
 */
public class DataSourceProcessorPane extends JPanel implements ActionListener {
    
    public static int  TOOLBAR = 1;
    public static int  SQUARE  = 1;
    
    private DataSourceProcessor  dataProcessor = new DataSourceProcessor();
    private String               dataFile      = null;
    private int                  dataPaneStyle = DataSourceProcessorPane.TOOLBAR;
    private JLabel               statusLabel   = null;    
    private java.util.Timer      processTimer  = null;
    private JButton              mediaPause    = null;
    private JButton              mediaPlay     = null;
    private JButton              mediaNext     = null;
    private JButton              mediaPrev     = null;
    private JButton              mediaEject    = null;
    private JButton              sourceFile    = null;
    private JButton              sourceEt      = null;
    private int                  eventDelay    = 0;
    private Color paneBackground               = Color.GRAY;
    public boolean               isHipo3Event  = false;          
    private String               defaultHost   = null;
    private String               defaultIp     = null;    
    
    public DataSourceProcessorPane(){
        super();
        initUI();
    }
    
    public DataSourceProcessorPane(String host, String ip){
        super();
        initUI();
        this.defaultHost = host;
        this.defaultIp   = ip;
    }
    
    private void initUI(){
        
        ImageIcon playIcon  = new ImageIcon(DataSourceProcessorPane.class.getClassLoader().getResource("icons/media/themes/default/play-20x20.png"));
        ImageIcon pauseIcon = new ImageIcon(DataSourceProcessorPane.class.getClassLoader().getResource("icons/media/pause_24px.png"));
        ImageIcon fileIcon  = new ImageIcon(DataSourceProcessorPane.class.getClassLoader().getResource("icons/media/themes/default/etring-20x20.png"));

        //setLayout(new FlowLayout());
        setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createSoftBevelBorder(SoftBevelBorder.RAISED));
        
        sourceFile = new JButton("E");
        //sourceFile.setIcon(fileIcon);
        //sourceFile.setPreferredSize(new Dimension(35, 35));
        sourceFile.setActionCommand("OpenFile"); 
        sourceFile.setToolTipText("Open EVIO file.");        
        sourceFile.addActionListener(this);
        
        JButton sourceFileEt = new JButton("Et");
        //sourceFile.setIcon(fileIcon);
        //sourceFileEt.setPreferredSize(new Dimension(35, 35));
        sourceFileEt.setActionCommand("OpenFileET");
        sourceFileEt.setToolTipText("Connect to ET ring");        
        sourceFileEt.addActionListener(this);
        
        JButton sourceFileHipo3 = new JButton("H3");
        //sourceFileHipo.setPreferredSize(new Dimension(35, 35));
        sourceFileHipo3.setActionCommand("OpenFileHipo3");
        sourceFileHipo3.setToolTipText("Open Hipo3 file");
        sourceFileHipo3.addActionListener(this);
        
        JButton sourceFileHipo4 = new JButton("H4");
        //sourceFileHipo.setPreferredSize(new Dimension(35, 35));
        sourceFileHipo4.setActionCommand("OpenFileHipo4");
        sourceFileHipo4.setToolTipText("Open Hipo4 file");
        sourceFileHipo4.addActionListener(this);        
        
        JButton sourceFileRing = new JButton("HR");
        sourceFileRing.setActionCommand("OpenFileRing");
        sourceFileRing.setToolTipText("Connect to xMsg Hipo ring");
        //sourceFileRing.setPreferredSize(new Dimension(35, 35));
        sourceFileRing.addActionListener(this);
        //sourceFile.setBackground(this.paneBackground);
        
        JButton resetListeners = new JButton("Reset");
        resetListeners.setActionCommand("ResetListeners");
        //resetListeners.setPreferredSize(new Dimension(55, 35));
        resetListeners.setToolTipText("Reset data listeners");
        resetListeners.addActionListener(this);
        
        JPanel mediaPane = this.createMediaPane();
        JPanel sourcePane = new JPanel();
                        
        
        sourcePane.setBorder(BorderFactory.createSoftBevelBorder(SoftBevelBorder.LOWERED));
        //sourcePane.setBackground(Color.LIGHT_GRAY);
        sourcePane.add(sourceFile);
        sourcePane.add(sourceFileEt);
        sourcePane.add(sourceFileHipo3);
        sourcePane.add(sourceFileHipo4);
//        sourcePane.add(sourceFileRing);
        sourcePane.add(resetListeners);
        //this.add(openFile);
        //this.add(Box.createHorizontalStrut(30));
        //this.add(mediaPane);
        
        //this.add(Box.createHorizontalStrut(30));
        
        statusLabel = new JLabel(dataProcessor.getStatusString());
        statusLabel.setFont(new Font("Avenir",Font.PLAIN,14));
        JPanel  statusLabelPane = new JPanel();
        //statusLabelPane.setBackground(Color.LIGHT_GRAY);
        statusLabelPane.setLayout(new BorderLayout());
        statusLabelPane.setBorder(BorderFactory.createSoftBevelBorder(SoftBevelBorder.LOWERED));
        statusLabelPane.add(statusLabel,BorderLayout.CENTER);
        
        mediaPane.setBackground(Color.LIGHT_GRAY);
        this.add(statusLabelPane,BorderLayout.CENTER);
        this.add(mediaPane,BorderLayout.LINE_START);
        this.add(sourcePane,BorderLayout.LINE_END);
    }
    
    public void setDelay(int delay){
        this.dataProcessor.setDelay(delay);
        this.eventDelay = delay;
    }
    
    public void addEventListener(IDataEventListener del){
        this.dataProcessor.addEventListener(del);
    }
    
    
    public void setUpdateRate(int nevents){
        this.dataProcessor.setUpdateRate(nevents);
    }
    
    private JPanel  createMediaPane(){
        
        ImageIcon playIcon  = new ImageIcon(DataSourceProcessorPane.class.getClassLoader().getResource("icons/media/themes/thin/play-24x24.png"));
        ImageIcon pauseIcon = new ImageIcon(DataSourceProcessorPane.class.getClassLoader().getResource("icons/media/themes/thin/pause-24x24.png"));
        ImageIcon nextIcon  = new ImageIcon(DataSourceProcessorPane.class.getClassLoader().getResource("icons/media/themes/thin/next-24x24.png"));
        ImageIcon prevIcon  = new ImageIcon(DataSourceProcessorPane.class.getClassLoader().getResource("icons/media/themes/thin/previous-24x24.png"));
        ImageIcon ejectIcon  = new ImageIcon(DataSourceProcessorPane.class.getClassLoader().getResource("icons/media/themes/thin/eject-24x24.png"));
        
        JPanel mediaPane = new JPanel();
        mediaPane.setBorder(BorderFactory.createSoftBevelBorder(SoftBevelBorder.LOWERED));
        mediaPane.setLayout(new FlowLayout());
        
        mediaPause = new JButton();
        
        mediaPause.setIcon(pauseIcon);        
        //mediaPause.setContentAreaFilled(false);
        //mediaPause.setOpaque(true);
        mediaPause.setActionCommand("PauseFile");
        mediaPause.addActionListener(this);
        mediaPause.setEnabled(false);
        
        mediaPlay = new JButton();
        mediaPlay.setBackground(new Color(80,105,220));
        mediaPlay.setIcon(playIcon);
        mediaPlay.setActionCommand("PlayFile");
        mediaPlay.addActionListener(this);
        mediaPlay.setEnabled(false);
        
        //mediaPlay.setBorderPainted(false); 
        mediaPlay.setContentAreaFilled(false); 
        mediaPlay.setFocusPainted(false); 
        //mediaPlay.setOpaque(false);
        
        mediaNext = new JButton();
        mediaNext.setIcon(nextIcon);
        mediaNext.setActionCommand("PlayNext");
        mediaNext.addActionListener(this);
        mediaNext.setEnabled(false);
        
        mediaPrev = new JButton();
        mediaPrev.setIcon(prevIcon);
        mediaPrev.setActionCommand("PlayPrev");
        mediaPrev.addActionListener(this);
        mediaPrev.setEnabled(false);
        
        mediaEject = new JButton();
        mediaEject.setIcon(ejectIcon);
        mediaEject.setActionCommand("PlayEject");
        mediaEject.addActionListener(this);
        mediaEject.setEnabled(false);
        
        mediaPane.add(mediaEject);
        mediaPane.add(mediaPrev);
        mediaPane.add(mediaNext);
        mediaPane.add(mediaPause);
        mediaPane.add(mediaPlay);

        return mediaPane;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("[action] --> " + e.getActionCommand());
        
        if(e.getActionCommand().compareTo("PlayFile")==0){
            mediaPlay.setEnabled(false);
            mediaPause.setEnabled(true);
            mediaNext.setEnabled(true);
            mediaPrev.setEnabled(true);
            this.startProcessorTimer();
        }

        if(e.getActionCommand().compareTo("PauseFile")==0){
            mediaPlay.setEnabled(true);
            mediaPause.setEnabled(false);
            this.processTimer.cancel();
            this.processTimer = null;
        }
        
        if(e.getActionCommand().compareTo("PlayNext")==0){
            this.dataProcessor.processNextEvent();
        }
        if(e.getActionCommand().compareTo("OpenFileET")==0){
            ConnectionDialog dialog = new ConnectionDialog(this.defaultHost, this.defaultIp);
            dialog.setVisible(true);
            if(dialog.reason()==DialogUtilities.OK_RESPONSE){
                String ip   = dialog.getIpAddress();
                String file = dialog.getFileName();
                Integer port = dialog.getPort();
                
                EvioETSource source = new EvioETSource(ip,port);
                source.open(file);
                this.dataProcessor.setSource(source);
                mediaNext.setEnabled(true);
                mediaPrev.setEnabled(true);
                mediaPlay.setEnabled(true);
                this.setDataFile(null);
            }
        }
        
        if(e.getActionCommand().compareTo("OpenFile")==0){

            if(this.processTimer!=null){
                this.processTimer.cancel();
                this.processTimer = null;
            }
            
            JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(null);
            int returnVal = fc.showOpenDialog(this);
            
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                String fileName = fc.getSelectedFile().getAbsolutePath();
                System.out.println("file -> " + fileName);
                EvioSource source = new EvioSource();
                source.open(fileName);
                //This is where a real application would open the file.
                this.dataProcessor.setSource(source);
                mediaNext.setEnabled(true);
                mediaPrev.setEnabled(true);
                mediaPlay.setEnabled(true);
                this.setDataFile(fileName);
            } else {
                
            }
        }
        
        
        if(e.getActionCommand().compareTo("ResetListeners")==0){
            System.out.println("\n   >>>> resetting all listeners");
            for(IDataEventListener listener : this.dataProcessor.getEventListeners()){
                try {
                    listener.resetEventListener();
                } catch (Exception exc){
                    System.out.println("\n   >>>> error resetting listener : " + listener.getClass().getName());
                }
            }
        }
        if(e.getActionCommand().compareTo("OpenFileRing")==0){
            HipoRingSource source = HipoRingSource.createSource();
            
            this.dataProcessor.setSource(source);
            statusLabel.setText(dataProcessor.getStatusString());
            mediaNext.setEnabled(true);
            mediaPrev.setEnabled(true);
            mediaPlay.setEnabled(true);
            this.setDataFile(null);
        }
        
        if(e.getActionCommand().compareTo("OpenFileHipo3")==0){
            if(this.processTimer!=null){
                this.processTimer.cancel();
                this.processTimer = null;
            }
            isHipo3Event = true;
            JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(null);
            int returnVal = fc.showOpenDialog(this);
            
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                String fileName = fc.getSelectedFile().getAbsolutePath();
                System.out.println("file -> " + fileName);
                Hipo3DataSource source = new Hipo3DataSource();
                source.open(fileName);

                //This is where a real application would open the file.
                this.dataProcessor.setSource(source);
                statusLabel.setText(dataProcessor.getStatusString());
                mediaNext.setEnabled(true);
                mediaPrev.setEnabled(true);
                mediaPlay.setEnabled(true);
                this.setDataFile(fileName);
            }
        }
        
        if(e.getActionCommand().compareTo("OpenFileHipo4")==0){
            if(this.processTimer!=null){
                this.processTimer.cancel();
                this.processTimer = null;
            }
            isHipo3Event = false;
            JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(null);
            int returnVal = fc.showOpenDialog(this);
            
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                String fileName = fc.getSelectedFile().getAbsolutePath();
                System.out.println("file -> " + fileName);
                HipoDataSource source = new HipoDataSource();
                source.open(fileName);

                //This is where a real application would open the file.
                this.dataProcessor.setSource(source);
                statusLabel.setText(dataProcessor.getStatusString());
                mediaNext.setEnabled(true);
                mediaPrev.setEnabled(true);
                mediaPlay.setEnabled(true);
                this.setDataFile(fileName);
            }
        }
        
    }
    
    private void startProcessorTimer(){
        //System.out.println(" starting timer ");
        class CrunchifyReminder extends TimerTask {
            boolean hasFinished = false;
            public void run() {
                //dataProcessor.processNextEvent(0, DataEventType.EVENT_START);
                /*if(hasFinished==true){
                    dataProcessor.processNextEvent(0, DataEventType.EVENT_STOP);
                    return;
                }*/
                //System.out.println("running");
                for (int i=1 ; i<=50 ; i++) {
                    boolean status = dataProcessor.processNextEvent(eventDelay,DataEventType.EVENT_ACCUMULATE);
                    if(status==false&&hasFinished==false){
                        hasFinished = true;
                        System.out.println("[DataProcessingPane] ----> task is done...");
                    }
                }
                statusLabel.setText(dataProcessor.getStatusString());
            }
        }
        processTimer = new java.util.Timer();
        processTimer.schedule(new CrunchifyReminder(),1,1);        
    }
    
    public String getDataFile() {
        return dataFile;
    }

    public void setDataFile(String dataFile) {
        this.dataFile = dataFile;
    }

    public static void main(String[] args){
        JFrame frame = new JFrame();
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        DataSourceProcessorPane pane = new DataSourceProcessorPane();
        
        pane.addEventListener(new IDataEventListener(){
            int ncount = 0;
            public void dataEventAction(DataEvent event) {
                ncount++;
                if(event.getType() == DataEventType.EVENT_START){
                    System.out.println(" ---> start event " + ncount);
                }
                
                if(event.getType() == DataEventType.EVENT_STOP){
                    System.out.println(" ---> stop event " + ncount);
                }
            }

            public void timerUpdate() {
                System.out.println("update is called");
            }

            public void resetEventListener() {
                System.out.println("reset is called");
                ncount = 0;
            }
            
        });
        panel.add(pane,BorderLayout.PAGE_END);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }
    
}
