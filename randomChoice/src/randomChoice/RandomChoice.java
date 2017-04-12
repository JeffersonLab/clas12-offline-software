package randomChoice;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


public class RandomChoice extends JFrame implements ActionListener, ISoundDone {

	// buttons
	private JButton _nextButton;
	private JButton _resetButton;
	private JButton _quitButton;
	
	private Team _currentTeam;

	
	//all the students
    private ArrayList<Student> _students = new ArrayList<Student>();
    
    //all the teams
    private ArrayList<Team> _teams = new ArrayList<Team>();
	
	public RandomChoice() {
		super("Random Choice");
		setLayout(new BorderLayout(8, 8));

		initialize();
		addContent();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(640, 410);
		fixTitle();
	}
	
	private void fixTitle() {
		int numTeams = _teams.size() - 1;
		setTitle("Number of teams remaining: " + numTeams);
	}
	
	
	private void initialize() {
		File cwd = new File(Environment.getInstance().getCurrentWorkingDirectory());
		
		File imageDir = new File(cwd, "images");
		if (imageDir.exists() && imageDir.isDirectory()) {
			System.err.println("Image dir looks good");
		}
		
		File files[] = imageDir.listFiles();
		if (files == null) {
			System.err.println("No image files found.");
			return;
		}
		else {
			System.err.println(files.length + " image files found.");
		}
		
		for (File file : files) {
			_students.add(new Student(file));
		}
		
		reset();
	}
	
	private Student findStudent(String name) {
		for (Student student : _students) {
			if (student.name.toLowerCase().contains(name.toLowerCase())) {
				return student;
			}
		}
		return null;
	}

	private void addContent() {
		addSouth();
		add(Box.createHorizontalStrut(25), BorderLayout.WEST);
		add(Box.createVerticalStrut(25), BorderLayout.NORTH);
	}

	private void addSouth() {

		JPanel bp = new JPanel();
		bp.setLayout(new FlowLayout(FlowLayout.CENTER, 60, 6));
		_nextButton = makeButton("  Next ", bp);
		_resetButton = makeButton(" Reset ", bp);
		_quitButton = makeButton("  Quit ", bp);
		add(bp, BorderLayout.SOUTH);
	}

	private JButton makeButton(String label, JPanel bp) {
		JButton button = new JButton(label);
		bp.add(button);
		button.addActionListener(this);
		return button;
	}

	/**
	 * Center a component.
	 * 
	 * @param component
	 *            The Component to center.
	 */
	public static void centerComponent(Component component) {

		if (component == null)
			return;

		try {

			Dimension screenSize = null;
			try {
				GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
				int width = gd.getDisplayMode().getWidth();
				int height = gd.getDisplayMode().getHeight();
				if ((width > 100) && (height > 100)) {
					screenSize = new Dimension(width, height);
				}
			} catch (Exception e) {
			}

			if (screenSize == null) {
				screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			}
			Dimension componentSize = component.getSize();
			if (componentSize.height > screenSize.height) {
				componentSize.height = screenSize.height;
			}
			if (componentSize.width > screenSize.width) {
				componentSize.width = screenSize.width;
			}

			int x = ((screenSize.width - componentSize.width) / 2);
			int y = ((screenSize.height - componentSize.height) / 2);

			component.setLocation(x, y);

		} catch (Exception e) {
			component.setLocation(200, 200);
			e.printStackTrace();
		}
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == _nextButton) {
			next();
		}
		else if (source == _resetButton) {
			reset();
		}
		else if (source == _quitButton) {
			System.exit(0);
		}

	}

	private boolean _done = false;

	private void next() {
		_done = false;
		_nextButton.setEnabled(false);
		
		if (_teams.size() <=2) {
			loadTeam(_teams.get(_teams.size()-1));
			soundDone();
			return;
		}
		
		
		SoundUtils.randomize(this, 5);

		Runnable update = new Runnable() {

			@Override
			public void run() {

				while (!_done) {

					Runnable update = new Runnable() {

						@Override
						public void run() {
							Team t = randomTeam();
							loadTeam(t);
						}
					};

					try {
						SwingUtilities.invokeAndWait(update);
						try {
							Thread.sleep(400);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					} catch (InvocationTargetException | InterruptedException e) {
						e.printStackTrace();
					}
				}
				
			}

		};

		new Thread(update).start();
		
		
//		while (!_done) {
//			loadTeam(randomTeam());
//			try {
//				Thread.sleep(20);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
			
//			Runnable update = new Runnable() {
//
//				@Override
//				public void run() {
//					loadTeam(randomTeam());
//				}
//				
//			};
//			
//			SwingUtilities.invokeLater(update);			
//		}
	}
	

	@Override
	public void soundDone() {
		_done = true;
		_nextButton.setEnabled(true);

		if (_teams.size() > 1) {
			_teams.remove(_currentTeam);
			fixTitle();
		}
	}
	
	
	private void reset() {
		//make the teams
		_teams.clear();
		_teams.add(new Team(findStudent("hulk"), findStudent("zeta")));
		_teams.add(new Team(findStudent("bostic"), findStudent("carly")));
		_teams.add(new Team(findStudent("boles"), findStudent("hennis")));
		_teams.add(new Team(findStudent("culver"), findStudent("pokorny")));
		_teams.add(new Team(findStudent("hawk"), findStudent("wilson")));
		_teams.add(new Team(findStudent("anstett"), findStudent("robinson")));
		_teams.add(new Team(findStudent("leigh"), findStudent("farrington")));
		_teams.add(new Team(findStudent("cunningham"), findStudent("gendell")));
		_teams.add(new Team(findStudent("webb"), findStudent("woolard")));
		_teams.add(new Team(findStudent("ellis"), findStudent("zippy")));
		_teams.add(new Team(findStudent("ferguson"), findStudent("keverline")));
		
		//load the 0th team, hulk and Catherine
		loadTeam(_teams.get(0));
		fixTitle();
	}
	
	private Team randomTeam() {
		if ((_teams == null) || (_teams.isEmpty())) {
			return null;
		}

		int size = _teams.size();

		if (size == 1) {
			return _teams.get(0);
		} else if (size == 2) {
			return _teams.get(1);
		} else {

			Team newTeam = _currentTeam;

			while (newTeam == _currentTeam) {
				int index = ThreadLocalRandom.current().nextInt(1, size);
//				System.err.println("size: " + size + " index: " + index);

				int goodIndex = Math.max(1, Math.min(size - 1, index));

				if (index != goodIndex) {
					System.err.println("ERROR size: " + size + " index: " + index + "  goodIndex: " + goodIndex);
					System.exit(1);
				}

				newTeam = _teams.get(goodIndex);
			}

			return newTeam;
		}

	}
	
	private void loadTeam(Team team) {
		if (_currentTeam != null) {
			this.remove(_currentTeam);
		}
		
		if (team != null) {
	//		System.err.println("adding team");
			add(team, BorderLayout.CENTER);
			revalidate();
			repaint(0);
		}
		
		_currentTeam = team;
		
	}
	
	public static void main(String args[]) {
		final RandomChoice app = new RandomChoice();
		centerComponent(app);
		System.err.println("Size: " + app.getSize());

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				app.setVisible(true);
			}
		});

	}


}
