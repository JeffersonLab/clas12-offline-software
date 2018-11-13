package cnuphys.fastMCed.consumers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jlab.clas.physics.PhysicsEvent;

import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.PluginLoader;
import cnuphys.fastMCed.eventgen.AEventGenerator;
import cnuphys.fastMCed.eventio.IPhysicsEventListener;
import cnuphys.fastMCed.eventio.PhysicsEventManager;
import cnuphys.fastMCed.fastmc.ParticleHits;
import cnuphys.fastMCed.frame.FastMCed;
import cnuphys.fastMCed.streaming.IStreamProcessor;
import cnuphys.fastMCed.streaming.StreamManager;
import cnuphys.fastMCed.streaming.StreamProcessStatus;
import cnuphys.fastMCed.streaming.StreamReason;

/**
 * Managers consumers
 * @author heddle
 *
 */
public class ConsumerManager extends Vector<PhysicsEventConsumer> implements IPhysicsEventListener, IStreamProcessor, ActionListener  {
	
	// optional full path to consumers folder
	private String _consumerPath = sysPropOrEnvVar("CONSUMERDIR");
	
	/** Last selected data file */
	private static String dataFilePath = Environment.getInstance().getHomeDirectory();


	/** possible class file extensions */
	private static String extensions[] = { "class"};

	// filter to look for lund files
	private static FileNameExtensionFilter _classFileFilter = new FileNameExtensionFilter("Consumer class Files",
			extensions);

	//map consumers to menu tems
	//private Hashtable<PhysicsEventConsumer, JCheckBoxMenuItem> hash =  new Hashtable<>();
	
	//where the PhysicsEventConsumer plugins are found
	private File _consumerDir;

	//singleton
	private static ConsumerManager instance;
	
	// the base class for consumer plugins
	protected Class<PhysicsEventConsumer> _consumerClaz;
	
	//load a consumer (a .class file) directly
	private JMenuItem _loadItem;

	//the menu
	private JMenu _menu;
	
	//why an event was flagged
	private String _flagExplanation;

	//private singleton constructor
	private ConsumerManager() {
		String cwd = Environment.getInstance().getCurrentWorkingDirectory();
		
		//always add a test consumer 
//		AcceptanceMapperConsumer accConsumer = new AcceptanceMapperConsumer();
//		accConsumer.setActive(true);
//		add(accConsumer);
		
		SNRSector1TestConsumer testConsumer = new SNRSector1TestConsumer();
		testConsumer.setActive(false);
		add(testConsumer);
		
		SNRShiftTestConsumer shiftConsumer = new SNRShiftTestConsumer();
		shiftConsumer.setActive(false);
		add(shiftConsumer);
		
		
//		SNRSector1TestConsumerV2 testConsumer2 = new SNRSector1TestConsumerV2();
//		testConsumer2.setActive(false);
//		add(testConsumer2);
//		
//		SNRResolutionConsumer resConsumer = new SNRResolutionConsumer();
//		resConsumer.setActive(true);
//		add(resConsumer);

		
		//now the plugins
		_consumerDir = new File(cwd, "consumers");
		String classPath = _consumerDir.getPath();
		
		//TODO prepend other dirs
		if (_consumerPath != null) {
			classPath = _consumerPath + File.pathSeparator + classPath;
		}
		if (FastMCed.getUserConsumerDir() != null) {
			classPath = FastMCed.getUserConsumerDir() + File.pathSeparator + classPath;
		}
		
		System.err.println("Consumer plugin path: [" + classPath + "]");
		Log.getInstance().info("Consumer plugin path: [" + classPath + "]");
				
		try {
			_consumerClaz = (Class<PhysicsEventConsumer>) Class.forName("cnuphys.fastMCed.consumers.PhysicsEventConsumer");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		//let's try to load

		
		PluginLoader loader = new PluginLoader(classPath, _consumerClaz, null);
		
		List<Object> objs = loader.load();
		
		if (objs != null) {
			System.err.println("Found: " + objs.size() + " consumers.");
			for (Object obj : objs) {
				add((PhysicsEventConsumer)obj);
			}
		}
		
		PhysicsEventManager.getInstance().addPhysicsListener(this, 1);
		StreamManager.getInstance().addStreamListener(this);
	}
	
	/**
	 * Access for the singleton
	 * @return the singleton ConsumerManager
	 */
	public static ConsumerManager getInstance() {
		if (instance == null) {
			instance = new ConsumerManager();
		}
		return instance;
	}
	
	//create the menu item
	
	private boolean firstItem = true;
	private JCheckBoxMenuItem createMenuItem(final PhysicsEventConsumer consumer, boolean selected) {
		
		ItemListener il = new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
				consumer.setActive(item.isSelected());
			}
			
		};
		
		
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(consumer.getConsumerName(), selected);
		item.addItemListener(il);
		
		if (firstItem) {
			_menu.addSeparator();
			firstItem = false;
		}
		return item;
	}
	
	/**
	 * Get the consumer menu
	 * @return the consumer menu
	 */
	public JMenu getMenu() {
		if (_menu == null) {
			_menu = new JMenu("Consumers");
			
			_loadItem = new JMenuItem("Load a consumer .class file...");
			_loadItem.addActionListener(instance);
			_menu.add(_loadItem);
			
			for (PhysicsEventConsumer consumer : this) {
				_menu.add(createMenuItem(consumer, consumer.isActive()));
			}
		}
		
		return _menu;
	}

	@Override
	public void streamingChange(StreamReason reason) {
		for (PhysicsEventConsumer consumer : this) {
			if (consumer.isActive()) {
				consumer.streamingChange(reason);
			}
		}
	}

	@Override
	public StreamProcessStatus streamingPhysicsEvent(PhysicsEvent event, List<ParticleHits> particleHits) {
		for (PhysicsEventConsumer consumer : this) {
			if (consumer.isActive()) {
				StreamProcessStatus status = consumer.streamingPhysicsEvent(event, particleHits);
				if (status == StreamProcessStatus.FLAG) {
					System.err.println("FLAGGED");
					_flagExplanation = consumer.flagExplanation() + 
							"\nConsumer: " + consumer.getConsumerName();
					return StreamProcessStatus.FLAG;
				}
			}
		}
		
		return StreamProcessStatus.CONTINUE;
	}
	
	@Override
	public String flagExplanation() {
		return _flagExplanation;
	}

	/**
	 * A new event generator is active
	 * 
	 * @param generator
	 *            the now active generator
	 */
	public void newEventGenerator(final AEventGenerator generator) {
		for (PhysicsEventConsumer consumer : this) {
			if (consumer.isActive()) {
				consumer.newEventGenerator(generator);
			}
		}
	}

	@Override
	public void newPhysicsEvent(PhysicsEvent event, List<ParticleHits> particleHits) {
		for (PhysicsEventConsumer consumer : this) {
			if (consumer.isActive()) {
				consumer.newPhysicsEvent(event, particleHits);
			}
		}
	}
	
	// get a property or environment variable
	// the property takes precedence
	private String sysPropOrEnvVar(String key) {
		String s = System.getProperty(key);
		if (s == null) {
			s = System.getenv(key);
		}
		return s;
	}
	
	//direct load of a class
	private void handleLoad() {
		//open the file
		
		JFileChooser chooser = new JFileChooser(dataFilePath);
		chooser.setSelectedFile(null);
		chooser.setFileFilter(_classFileFilter);
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			
			try {
				String className = PluginLoader.getFullClassName(file);
				Object object = PluginLoader.instantiateFromClassFile(file, className);
				if (object instanceof PhysicsEventConsumer) {
					PhysicsEventConsumer consumer = (PhysicsEventConsumer)object;
					add(consumer);
					_menu.add(createMenuItem(consumer, true));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			
		}

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == _loadItem) {
			handleLoad();
		}
	}


}
