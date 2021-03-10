package cnuphys.bCNU.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * A algorithm for auto layouts using forces between items
 * @author heddle
 *
 */
public class Layout extends JFrame {
	
	//a drawing canvas
	private Canvas _canvas;
	
	private JButton _startButton;
	
	//the bounds of where objects can live
	private Rectangle _bounds;
	
	/** the data model */
	public Model model;
	
	public static Random _rand = new Random(53197711);
	
	private static int _size = 48;
	public static int _gap = 48;
		
	int updateCount;
	
	private Timer _timer;
	
	//workspace
	private static double[] _p0 = new double[2];
	private static double[] _p1 = new double[2];
	
	/**
	 * Create the main frame with the given fractional size of the screen
	 * 
	 * @param fractionalSize
	 */
	private Layout(double fractionalSize) {
		super("Layout Tester");
		setLayout(new BorderLayout(4, 4));
		
		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setupTimer();
			}
			
		};
		
		_startButton = new JButton(" start ");
		_startButton.addActionListener(al);
		add(_startButton, BorderLayout.NORTH);
		
		
		_canvas = new Canvas();
		_bounds = new Rectangle(0, 0, 1000, 800);
		add(_canvas, BorderLayout.CENTER);
		

		// set up what to do if the window is closed
		WindowAdapter windowAdapter = new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent event) {
				System.exit(1);
			}
		};
		addWindowListener(windowAdapter);

		// set the fractional size
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

		d.width = (int) (fractionalSize * d.width);
		d.height = (int) (fractionalSize * d.height);
		setSize(d);
	}

	
	/**
	 * Get the number of rows and columns given the number of
	 * square "things" that go into a bigger "thing". Make it 
	 * as square as possible with the width longer than the 
	 * height (if necessary).
	 * @param n
	 * @param rowCol
	 */
	public void getRowCol(int n, int[] rowCol) {
		int nr = (int)(Math.max(1, Math.round(Math.sqrt(n))));
		int nc = 1;
		while ((nr * nc) < n) {
			nc++;
		}
		
		rowCol[0] = nr;
		rowCol[1] = nc;
	}
	
	// sets up the drawing time
	private void setupTimer() {
		TimerTask task = new TimerTask() {

			
			@Override
			public void run() {
				
				if (updateCount > 500) {
					return;
				}
				
				updateCount++;
				if ((updateCount % 100) == 0) {
					System.out.println("Update count: " + updateCount);
				}
				model.update();
				_canvas.repaint();
			}

		};
		_timer = new Timer();
		_timer.scheduleAtFixedRate(task, 500, 30);

	}
	
	/**
	 * main program for testing
	 * @param arg
	 */
	public static void main(String[] arg) {
		Layout layout = new Layout(0.85f);
		
		//create a model
		Box boxes[] = new Box[4];
		boxes[0] = layout.new Box(6);
		boxes[1] = layout.new Box(8);
		boxes[2] = layout.new Box(7);
		boxes[3] = layout.new Box(2);

		Singleton singletons[] = new Singleton[7];
		for (int i = 0; i < singletons.length; i++) {
			singletons[i] = layout.new Singleton();
		}
		
		Connection[] connections = new Connection[10];
		connections[0] = layout.new Connection(singletons[0], singletons[1]);
		connections[1] = layout.new Connection(singletons[1], singletons[2]);
		connections[2] = layout.new Connection(singletons[2], singletons[3]);
		connections[3] = layout.new Connection(singletons[2], singletons[4]);
		connections[4] = layout.new Connection(singletons[2], singletons[5]);
		connections[5] = layout.new Connection(singletons[2], singletons[6]);
		connections[6] = layout.new Connection(singletons[3], boxes[0]);
		connections[7] = layout.new Connection(singletons[4], boxes[1]);
		connections[8] = layout.new Connection(singletons[5], boxes[2]);
		connections[9] = layout.new Connection(singletons[6], boxes[3]);
		
		layout.model = layout.new Model(boxes, singletons, connections);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				layout.setVisible(true);
				layout.setLocationRelativeTo(null);

//				layout.setupTimer();
			}
						
		});
		
//		for (int i = 0; i < 10000; i++) {
//			layout.model.update();
//		}
//		
//		layout._canvas.repaint();
		
		
	}
	
	/**
	 * The class that holds all the data
	 * @author heddle
	 *
	 */
	class Model {
		
		/** weak long range upper-left bias force constant */
        public double springConstantULB = 0.002;
        
		/** weak long range upper-left bias force constant */
        public double springConstantULS = 0.001;

		
		/** overlap repulsion */
		public double overlapStrength = 60;
		
		/** repulsion from out of bounds */
		//public double oobStrength = 2;
		
		/** box-box repulsion */
		public double bbRepulsionStrength = 80;
		
		/** unconnected singleton singleton  repulsion */
		public double ssRepulsionStrength = 100;
		
		/** unconnected singleton box  repulsion */
		public double sbRepulsionStrength = 80;

		
		//spring force attraction singleton box
		public double springConstantSB = .008;
		
		//spring force attraction singleton singleton
		public double springConstantSS = .02;

		
		/** drag */
		public double dragStrength = 0.015;
		
		public Box[] boxes;
		public Singleton[] singletons;
		public Connection[] connections;
		public double timeStep = 0.5;
		public double halfTSquare = 0.5*timeStep*timeStep;
		
		public Model(Box[] boxes, Singleton[] singletons, Connection[] connections) {
			this.boxes = boxes;
			this.singletons = singletons;
			this.connections = connections;
		}
		
		public void draw(Graphics g) {
			
			for (Connection cnx : connections) {
				cnx.draw(g);
			}
			
			
			for (Box box : boxes) {
				box.draw(g);
			}
			
			for (Singleton singleton : singletons) {
				singleton.draw(g);
			}
			
			debugDrawGaps(g);

		}
		
		private void debugDrawGaps(Graphics g) {
			for (Connection cnx : connections) {
				
				g.setColor(Color.green);
				cnx.pr1.gapDistance(cnx.pr2, _p0, _p1);
				g.drawLine((int)_p0[0], (int)_p0[1], (int)_p1[0], (int)_p1[1]);
				
				g.setColor(Color.magenta);

				g.fillOval((int)_p0[0]-3, (int)_p0[1]-3, 6, 6);
				
			}

		}
		
		/**
		 * Are the two positioned rects part of a connection?
		 * @param pr1 one rectangle
		 * @param pr2 the other rectangle
		 * @return <code>true</code> if the two have a connection
		 */
		public boolean isConnected(PositionedRectangle pr1, PositionedRectangle pr2) {
			if (connections == null) {
				return false;
			}
			
			for(Connection cnx : connections) {
				if ((cnx.pr1 == pr1) && (cnx.pr2 == pr2)) {
					return true;
				}
				if ((cnx.pr1 == pr2) && (cnx.pr2 == pr1)) {
					return true;
				}

			}
				
			return false;
				
		}
		
		/**
		 * Get the boxes and singletons combined
		 * @return all the rectangles
		 */
		public PositionedRectangle[] GetEverything() {
			int len1 = (boxes == null) ? 0 : boxes.length;
			int len2 = (singletons == null) ? 0 : singletons.length;
			int len = len1 + len2;
			if (len == 0) {
				return null;
			}
			PositionedRectangle[] prs = new PositionedRectangle[len];
			
			int index = 0;
			
			if (len1 != 0) {
				for (int i = 0; i < len1; i++) {
					prs[index] = boxes[i];
					index++;
				}
			}
			
			if (len2 != 0) {
				for (int i = 0; i < len2; i++) {
					prs[index] = singletons[i];
					index++;
				}
			}
			
			return prs;

		}

		/**
		 * Update the model for the next time step. Compute the forces
		 * and change the velocity and position.
		 */
		public void update() {
			
			Del del = new Del();
			double f[] = new double[2];

			// compute forces
			PositionedRectangle[] prs = GetEverything();
			if (prs == null) {
				return;
			}
			
			//initialize and drag force to slow things down
			for (int k = 0; k < prs.length; k++) {
				PositionedRectangle pr = prs[k];
				pr.getForce().zero();

				//upper left bias force
				double kul = pr.isBox() ? springConstantULB : springConstantULS;
				pr.upperLeftBiasForce(kul, f);
				pr.getForce().add(f[0], f[1]);
				
				//drag force
				pr.getVelocity().dragForce(dragStrength, f);
				pr.getForce().add(f[0], f[1]);
			}

			//the necessary double loop
			int len = prs.length;
			for (int i = 0; i < (len - 1); i++) {
				boolean pr1IsBox = prs[i].isBox();
				boolean pr1IsSingleton = !pr1IsBox;

				for (int j = i + 1; j < len; j++) {

					
					if (prs[i].overlaps(prs[j])) {
						prs[i].getDel(prs[j], del);
						
						double rand = 0.5 + 0.75*_rand.nextDouble();
						
						double olfx = overlapStrength * del.getUx();
						double olfy = overlapStrength * del.getUy();
						prs[i].getForce().add(-olfx, -olfy*rand);
						prs[j].getForce().add(olfx*rand, olfy);
						continue;
					}

					boolean pr2IsBox = prs[j].isBox();

					boolean pr2IsSingleton = !pr2IsBox;

					//likes repel
					if (pr1IsBox && pr2IsBox) {
						prs[i].inverseLengthForce(bbRepulsionStrength, prs[j], del, f);
						prs[i].getForce().add(-f[0], -f[1]);
						prs[j].getForce().add(f[0], f[1]);

					} 
					else if ((pr1IsBox && pr2IsSingleton) || (pr2IsBox && pr1IsSingleton)) {
						if (isConnected(prs[i], prs[j])) {		
								prs[i].springForce(springConstantSB, prs[j], del, f);
								prs[i].getForce().add(f[0], f[1]);
								prs[j].getForce().add(-f[0], -f[1]);					
						}
						prs[i].inverseLengthForce(sbRepulsionStrength, prs[j], del, f);
						prs[i].getForce().add(-f[0], -f[1]);
						prs[j].getForce().add(f[0], f[1]);

					} 
					else if (pr1IsSingleton && pr2IsSingleton) {
						if (isConnected(prs[i], prs[j])) {
								prs[i].springForce(springConstantSS, prs[j], del, f);
								prs[i].getForce().add(f[0], f[1]);
								prs[j].getForce().add(-f[0], -f[1]);
						}
						prs[i].inverseLengthForce(ssRepulsionStrength, prs[j], del, f);
						prs[i].getForce().add(-f[0], -f[1]);
						prs[j].getForce().add(f[0], f[1]);
					}

				} // j loop
			} // i loop
						
			//update positions and velocities
			for (int k = 0; k < prs.length; k++) {
				PositionedRectangle pr = prs[k];
				Force force = pr.getForce();
				Velocity velocity = pr.getVelocity();
				
				double dvx = force.fx * timeStep;
				double dvy = force.fy * timeStep;
				double dx = velocity.vx * timeStep + force.fx * halfTSquare;
				double dy = velocity.vy * timeStep + force.fy * halfTSquare;
				
				velocity.add(dvx, dvy);
				pr.offset(dx, dy);
								
				//billiard ball bounce if out of bounds
				
                if (pr.x <= _bounds.x) {
                	pr.getVelocity().vx = Math.abs(1.2*pr.getVelocity().vx);
                }
                else if ((pr.x + pr.width) >= (_bounds.x + _bounds.width)) {
                	pr.getVelocity().vx = -Math.abs(1.2 * pr.getVelocity().vx);
                }
                if (pr.y <= _bounds.y) {
                	pr.getVelocity().vy  = Math.abs(1.2 * pr.getVelocity().vy);
                } 
                else if ((pr.y + pr.height) >= (_bounds.y + _bounds.height)) {
                	pr.getVelocity().vy  = -Math.abs(1.2 * pr.getVelocity().vy );

                }

			}
			

		} //update
		

	}
	
	/**
	 * Holds a connection
	 * @author heddle
	 *
	 */
	class Connection {
		public PositionedRectangle pr1;
		public PositionedRectangle pr2;
		
		public Connection(PositionedRectangle pr1, PositionedRectangle pr2) {
			this.pr1 = pr1;
			this.pr2 = pr2;
		
		}
		
		public void draw(Graphics g) {
			

			if (pr1.isSingleton() && pr2.isSingleton()) {
				g.setColor(Color.red);
				
			}
			else {
				g.setColor(Color.black);
		}
			
			
			Position p1 = pr1.getPosition();
			Position p2 = pr2.getPosition();
			
			int x1 = _bounds.x + (int)(p1.x);
			int y1 = _bounds.y + (int)(p1.y);
			
			int x2 = _bounds.x + (int)(p2.x);
			int y2 = _bounds.y + (int)(p2.y);
			
			g.drawLine(x1, y1, x2, y2);
		}
	}
	
	//a positioned rectangle
	class PositionedRectangle extends Rectangle {
		
		public UUID guid = UUID.randomUUID();
		private Position _position = new Position();
		private Velocity _velocity = new Velocity();
		private Force _force = new Force();
		
		public PositionedRectangle() {
			x = _bounds.x + _rand.nextInt(_bounds.width/2);
			y = _bounds.y + _rand.nextInt(_bounds.height/2);
			
//			y = _bounds.y - 200;
			
		}

		public Position getPosition() {
			_position.x = getCenterX();
			_position.y = getCenterY();
			
			return _position;
		}
		
		public void offset(double dx, double dy) {
			x += (int)dx;
			y += (int)dy;
		}
		
		/**
		 * Get the force
		 * @return the force
		 */
		public Force getForce() {
			return _force;
		}
		
		/**
		 * Get the velocity
		 * @return the velocity
		 */
		public Velocity getVelocity() {
			return _velocity;
		}
		
		public void setPosition(double x, double y) {
			double w2 = width/2.0;
			double h2 = height/2.0;
			setFrame(x-w2, y-h2, width, height);
		}
		
		public boolean isBox() {
			return this instanceof Box;
		}
		
		public boolean isSingleton() {
			return this instanceof Singleton;
		}
		
		public boolean overlaps(PositionedRectangle opr) {
			return intersects(opr);
		}
		
		public boolean outOfBounds() {
			boolean inside =  _bounds.contains(this);
			return !inside;
		}
				
		/**
		 * Get the gap distance which is the "air" between the boxes
		 * @param opr
		 * @return
		 */
		public double gapDistance(PositionedRectangle opr, double[] p0, double[] p1) {
			if (overlaps(opr)) {
				return 0;
			}
			
			final double x0 = getCenterX();
			final double y0 = getCenterY();
			final double x1 = opr.getCenterX();
			final double y1 = opr.getCenterY();
			
			double dx = x1 - x0;
			double dy = y1 - y0;
			
			double dt = 0.05;
			double t0 = dt;
			
			while (t0 < 0.999) {
				double x = x0 + dx*t0;
				double y = y0 + dy*t0;
				
				if (!contains(x, y)) {
					break;
				}
				t0 += dt;
			}
			
			double t1 = 1-dt;
			
			double tmax = t0 + dt;
			while (t1 > tmax) {
				double x = x0 + dx*t1;
				double y = y0 + dy*t1;
				
				if (!opr.contains(x, y)) {
					break;
				}
				t1 -= dt;
			}
			
			
			p0[0] = x0 + dx*t0;
			p0[1] = y0 + dy*t0;
			p1[0] = x0 + dx*t1;
			p1[1] = y0 + dy*t1;


			dx = p1[0] - p0[0];
			dy = p1[1] - p0[1];
			
			double d = Math.sqrt(dx*dx + dy*dy);
			
			if (d < 1) {
				System.out.println("");
			}
			
			return d;
		}
		
		public void getDel(PositionedRectangle opr, Del del) {			
			double dx = opr.getCenterX() - getCenterX();
			double dy = opr.getCenterY() - getCenterY();
			del.set(dx, dy);
		}
		
		public double distance(PositionedRectangle opr) {
			
			double dx = opr.getCenterX() - getCenterX();
			double dy = opr.getCenterY() - getCenterY();
			return Math.sqrt(dx*dx + dy*dy);
		}
		
		public void toCenter(Del del) {
			double dx = _bounds.getCenterX() - getCenterX();
			double dy = _bounds.getCenterY() - getCenterY();

			del.set(dx, dy);
		}
		
		/**
		 * A force pulling to the upper left
		 * @param k the force constant
		 * @param f will hold the force
		 */
		public void upperLeftBiasForce(double k, double f[]) {
			f[0] = -k * getCenterX();
			f[1] = -k * getCenterY() ;
		}
		
		/**
		 * Spring force
		 * @param k the spring constant
		 * @param opr the other positioned rectangle
		 * @param del workspace for a delta object
		 * @param f will hold the force [fx, fy]
		 */
		public void springForce(double k, PositionedRectangle opr, Del del, double f[]) {
			
			if (overlaps(opr)) {
				f[0] = 0;
				f[1] = 0;
			}
			else {
				double d = gapDistance(opr, _p0, _p1);
								
				getDel(opr, del);
				double fact = k * d;
				f[0] = fact*del.getUx();
				f[1] = fact*del.getUy();
			}
		}

		
		/**
		 * Force with inverse distance
		 * @param k
		 * @param opr
		 * @param del
		 * @param f
		 */
		public void inverseLengthForce(double k, PositionedRectangle opr, Del del, double f[]) {
			
			if (overlaps(opr)) {
				f[0] = 0;
				f[1] = 0;
			}
			else {
				double d = gapDistance(opr, _p0, _p1);
				getDel(opr, del);
				double fact = k/(d + 1);
				f[0] = fact*del.getUx();
				f[1] = fact*del.getUy();
			}
			
		}
		

	}
	
	/**
	 * A subnet box
	 * @author heddle
	 *
	 */
	class Box extends PositionedRectangle {
		
		public int count;
		public int numRow;
		public int numCol;
				
		public Box(int count) {
			this.count = count;
			
			int rowCol[] = new int[2];
			getRowCol(count, rowCol);
			
			numRow = rowCol[0];
			numCol = rowCol[1];
			
			width = numCol * (_size + _gap);
			height = numRow * (_size + _gap);
			
		}
		
		public void draw(Graphics g) {
			g.setColor(Color.lightGray);
			g.fillRect(_bounds.x + x, _bounds.y + y, width, height);
			g.setColor(Color.gray);
			g.drawRect(_bounds.x + x, _bounds.y + y, width, height);
			
			int gs = _gap + _size;

			int index = 0;
			for (int row = 0; row < numRow; row++) {
				int yy = _bounds.y + y + _gap/2 + row*gs;
				

				for (int col = 0; col < numCol; col++) {
					index++;
					
					int xx = _bounds.x + x + _gap/2 + col*gs;
					
					g.setColor(Color.yellow);
					g.fillRect(xx, yy, _size, _size);
					g.setColor(Color.red);
					g.drawRect(xx, yy, _size, _size);
					
					if (index == count) {
						break;
					}
				}
			
			}
		}
		
		
	}
	
	class Singleton extends PositionedRectangle {
		public Singleton() {
			width  = _size;
			height = _size;
		}
		
		public void draw(Graphics g) {
			g.setColor(Color.cyan);
			g.fillOval(_bounds.x + x, _bounds.y + y, width, height);
			g.setColor(Color.blue);
			g.drawOval(_bounds.x + x, _bounds.y + y, width, height);

		}
		

	}
	
	class Position {
		public double x;
		public double y;
	}
	
	//Delta class
	class Del {
		private double dx;
		private double dy;
		double lengthSq;
		private double length;
		private double ux;
		private double uy;
		
		public void set(double dx, double dy) {
			this.dx = dx;
			this.dy = dy;
			lengthSq = dx*dx + dy*dy;
			length = Math.sqrt(lengthSq);
			ux = (length > 1.0e-20) ? dx/length : 0;
			uy = (length > 1.0e-20) ? dy/length : 0;
		}
		
		public double getDx() {
			return dx;
		}
		public double getDy() {
			return dy;
		}
		public double getLength() {
			return length;
		}
		public double getLengthSq() {
			return lengthSq;
		}
		public double getUx() {
			return ux;
		}
		public double getUy() {
			return uy;
		}
		
	}
	
	class Velocity {
		public double vx;
		public double vy;
		
		public void add(double dvx, double dvy) {
			vx += dvx;
			vy += dvy;
		}
		
		public void dragForce(double k, double[] f) {
			double vsq = vx*vx + vy*vy;
			if (vsq < 1.0e-20) {
				f[0] = 0;
				f[1] = 0;
			}
			else {
				double v = Math.sqrt(vsq);

				double ux = vx/v;
				double uy = vy/v;
				
				double fact = -k*vsq;
				
				f[0] = fact*ux;
				f[1] = fact*uy;
			}
		}
		
	}
	
	/**
	 * Holds a force
	 * @author heddle
	 *
	 */
	class Force {
		public double fx;
		public double fy;
		
		public void zero() {
			fx = 0;
			fy = 0;
		}
		
		public void set(double fx, double fy) {
			this.fx = fx;
			this.fy = fy;
		}
		
		public void add(double dfx, double dfy) {
			fx += dfx;
			fy += dfy;
		}
	}
	
	
	class Canvas extends JComponent {
		@Override
		public void paintComponent(Graphics g) {
			
			g.setColor(Color.white);
			g.fillRect(_bounds.x, _bounds.y, _bounds.width, _bounds.height);

			g.setColor(Color.black);
			g.drawRect(_bounds.x, _bounds.y, _bounds.width, _bounds.height);
			
			if (model != null) {
				model.draw(g);
			}
		}
	}

}
