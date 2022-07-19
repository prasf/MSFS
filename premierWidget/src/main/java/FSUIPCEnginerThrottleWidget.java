package com.prasf.msfs.premierWidget;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.AbstractQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.mouseviator.fsuipc.FSUIPC;
import com.mouseviator.fsuipc.FSUIPCWrapper;
import com.mouseviator.fsuipc.IFSUIPCListener;
import com.mouseviator.fsuipc.datarequest.IDataRequest;
import com.mouseviator.fsuipc.datarequest.primitives.DoubleRequest;

public class FSUIPCEnginerThrottleWidget {

	/**
	 * FSUIPC Instance
	 */
	private final FSUIPC fsuipc = FSUIPC.getInstance();
	/**
	 * FSUIPC listener
	 */
	private IFSUIPCListener fsuipcListener;
	/**
	 * logger
	 */
	private static final Logger logger = Logger.getLogger(FSUIPCEnginerThrottleWidget.class.getName());

	private static DoubleRequest eng1_rpm_cont;

	int MAX_VALUE = 3500;
	int value = 0;

	//////////////////////////////
	// RPM = 3000
	// centerX : 191
	// centerY : 180
	// zeroAngle : 226.42142857142878
	// maxAngle : -42.94999999999999
	// angleToUse : 187.93979591836754
	boolean afficherBoutonReglage = false;
	int centerX = 191;
	int centerY = 180;
	double zeroAngle = 226.42142857142878;
	double maxAngle = -42.94999999999999;
	double range = zeroAngle - maxAngle;

	public static void main(String[] args) {
		new FSUIPCEnginerThrottleWidget();

	}

	public FSUIPCEnginerThrottleWidget() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| UnsupportedLookAndFeelException ex) {
				}

				JWindow frame = new JWindow();
				LeafPane myLeafPane = new LeafPane();
				frame.setBackground(new Color(0, 0, 0, 0));
				frame.setContentPane(myLeafPane);
				frame.pack();
				// frame.setLocationRelativeTo(null);
				frame.setVisible(true);
				FrameDragListener frameDragListener = new FrameDragListener(frame);
				frame.addMouseListener(frameDragListener);
				frame.addMouseMotionListener(frameDragListener);
				frame.setAlwaysOnTop(true);
				//frame.setShape(new Ellipse2D.Double(11, 10, 350, 350));

				byte result = FSUIPC.load();
				if (result != FSUIPC.LIB_LOAD_RESULT_OK) {
					System.out.println("Failed to load native library. Quiting...");
					return;
				} else

				{

					startFSUIPC(myLeafPane);

				}

			}
		});

	}

	public class LeafPane extends JPanel {

		private BufferedImage leaf;
		int t = 10;

		public LeafPane() {

			setBorder(new CompoundBorder(new LineBorder(Color.BLACK), new EmptyBorder(0, 0, 250, 0)));

			try {
				leaf = ImageIO.read(getClass().getResource("/img/RPM.png"));
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			setOpaque(false);
			setLayout(new FlowLayout(FlowLayout.CENTER,0,280));

			
			JButton button = new JButton("Fermer");
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					fsuipc.disconnect();
					System.exit(0);
				}
			});
			add("South",button);

			value = 0;

			// Cette méthode permet d'ajouter des boutons qui permette le réglage de la
			// gauge
			if (afficherBoutonReglage)
				addBoutonPourReglageGauge();

		}

		private void addBoutonPourReglageGauge() {
			// Crée un objet de contraintes
			GridBagConstraints c = new GridBagConstraints();

			///////////////////
			// Valeur JAUGE
			c.gridx = 1;
			c.gridy = 0;
			JButton button2 = new JButton("+");
			button2.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					value += 50;
					repaint();
				}
			});
			add(button2, c);

			c.gridx = 1;
			c.gridy = 1;
			JButton button3 = new JButton("-");
			button3.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					value -= 50;
					repaint();
				}
			});
			add(button3, c);

			///////////////////
			// Postion X

			c.gridx = 2;
			c.gridy = 0;
			JButton buttonReglagecentreXplus = new JButton("X+");
			buttonReglagecentreXplus.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					centerX += 1;
					repaint();
				}
			});
			add(buttonReglagecentreXplus, c);

			c.gridx = 2;
			c.gridy = 1;
			JButton buttonReglagecentreXmoins = new JButton("X-");
			buttonReglagecentreXmoins.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					centerX -= 1;
					repaint();
				}
			});
			add(buttonReglagecentreXmoins, c);

			///////////////////
			// Postion Y

			c.gridx = 3;
			c.gridy = 0;
			JButton buttonReglagecentreYplus = new JButton("Y+");
			buttonReglagecentreYplus.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					centerY -= 1;
					repaint();
				}
			});
			add(buttonReglagecentreYplus, c);

			c.gridx = 3;
			c.gridy = 1;
			JButton buttonReglagecentreYmoins = new JButton("Y-");
			buttonReglagecentreYmoins.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					centerY += 1;
					repaint();
				}
			});
			add(buttonReglagecentreYmoins, c);

			///////////////////
			// Angle

			c.gridx = 4;
			c.gridy = 0;
			JButton buttonzeroAngleplus = new JButton("A+");
			buttonzeroAngleplus.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					zeroAngle -= 1;
					repaint();
				}
			});
			add(buttonzeroAngleplus, c);

			c.gridx = 4;
			c.gridy = 1;
			JButton buttonzeroAngleFinplus = new JButton("AF+");
			buttonzeroAngleFinplus.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					zeroAngle -= 0.5;
					repaint();
				}
			});
			add(buttonzeroAngleFinplus, c);

			c.gridx = 4;
			c.gridy = 2;
			JButton buttonzeroAngleFinFinplus = new JButton("AFF+");
			buttonzeroAngleFinFinplus.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					zeroAngle -= 0.05;
					repaint();
				}
			});
			add(buttonzeroAngleFinFinplus, c);

			c.gridx = 5;
			c.gridy = 0;
			JButton buttonzeroAnglemoins = new JButton("A-");
			buttonzeroAnglemoins.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					zeroAngle += 1;
					repaint();
				}
			});
			add(buttonzeroAnglemoins, c);

			c.gridx = 5;
			c.gridy = 1;
			JButton buttonzeroAngleFinmoins = new JButton("AF-");
			buttonzeroAngleFinmoins.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					zeroAngle += 0.5;
					repaint();
				}
			});
			add(buttonzeroAngleFinmoins, c);

			c.gridx = 5;
			c.gridy = 2;
			JButton buttonzeroAngleFinFinmoins = new JButton("AFF-");
			buttonzeroAngleFinFinmoins.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					zeroAngle += 0.05;
					repaint();
				}
			});
			add(buttonzeroAngleFinFinmoins, c);

			///////////////////
			// Max Angle

			c.gridx = 0;
			c.gridy = 4;
			JButton buttonzeroMaxAngleplus = new JButton("MA+");
			buttonzeroMaxAngleplus.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					maxAngle -= 1;
					repaint();
				}
			});
			add(buttonzeroMaxAngleplus, c);

			c.gridx = 1;
			c.gridy = 4;
			JButton buttonzeroMaxAngleFinplus = new JButton("MAF+");
			buttonzeroMaxAngleFinplus.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					maxAngle -= 0.5;
					repaint();
				}
			});
			add(buttonzeroMaxAngleFinplus, c);

			c.gridx = 2;
			c.gridy = 4;
			JButton buttonzeroMaxAngleFinFinplus = new JButton("MAFF+");
			buttonzeroMaxAngleFinFinplus.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					maxAngle -= 0.05;
					repaint();
				}
			});
			add(buttonzeroMaxAngleFinFinplus, c);

			c.gridx = 0;
			c.gridy = 5;
			JButton buttonzeroMaxAnglemoins = new JButton("MA-");
			buttonzeroMaxAnglemoins.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					maxAngle += 1;
					repaint();
				}
			});
			add(buttonzeroMaxAnglemoins, c);

			c.gridx = 1;
			c.gridy = 5;
			JButton buttonzeroMaxAngleFinmoins = new JButton("MAF-");
			buttonzeroMaxAngleFinmoins.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					maxAngle += 0.5;
					repaint();
				}
			});
			add(buttonzeroMaxAngleFinmoins, c);

			c.gridx = 2;
			c.gridy = 5;
			JButton buttonzeroMaxAngleFinFinmoins = new JButton("MAFF-");
			buttonzeroMaxAngleFinFinmoins.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					maxAngle += 0.05;
					repaint();
				}
			});
			add(buttonzeroMaxAngleFinFinmoins, c);

			/////// FIN A SUPPRIMER ////////

		}

		@Override
		public Dimension getPreferredSize() {
			return leaf == null ? new Dimension(200, 200) : new Dimension(leaf.getWidth(), leaf.getHeight());
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (leaf != null) {
				Graphics2D g2d = (Graphics2D) g.create();
				g2d.drawImage(leaf, 0, 0, this);
				g2d.dispose();
				
				/////////////////////////////////////////////////////////////
				// jauge V1 (ligne)
				g.setColor(Color.RED);
				int x1 = centerX, x2 = x1, y1 = centerY, y2 = y1;

				range = zeroAngle - maxAngle;

				// TODO : Si la valeur est inférieur à 500 il faut gérer le fait que la jauge
				// n'est pas proportionné pareil dans la zone.
				// par exemple si on value=0 l'aiguille est en dessous.
				double angleToUse = zeroAngle - 1.0 * range * (value * 1.0 / MAX_VALUE * 1.0);
				x2 += (int) (Math.cos(Math.toRadians(angleToUse)) * centerX);
				y2 -= (int) (Math.sin(Math.toRadians(angleToUse)) * centerY);
				g.drawLine(x1, y1, x2, y2);
				g.setFont(new Font("TimesRoman", Font.PLAIN, 20)); 
				g.drawString("" + value, centerX-26, centerY + 59);				
				
				
				/*
				 * Double x,y; x=(double) 10; y=(double) 10; double[] pt = {x, y}; double
				 * angleToUse = zeroAngle - 1.0 * range * (value * 1.0 / MAX_VALUE * 1.0);
				 * AffineTransform.getRotateInstance(Math.toRadians(angleToUse), x, y)
				 * .transform(pt, 0, pt, 0, 1); //specifying to use this double[] to hold coords
				 * Double newX = pt[0]; Double newY = pt[1];
				 */

				if (afficherBoutonReglage) {
					System.out.println("//////////////////////////////");
					System.out.println("centerX : " + String.valueOf(centerX));
					System.out.println("centerY : " + String.valueOf(centerY));
					System.out.println("zeroAngle : " + String.valueOf(zeroAngle));
					System.out.println("maxAngle : " + String.valueOf(maxAngle));
					//System.out.println("angleToUse : " + String.valueOf(angleToUse));
				}

				/*
				 * System.out.println("x1 : " + String.valueOf(x1)); System.out.println("x2 : "
				 * + String.valueOf(x2)); System.out.println("y1 : " + String.valueOf(y1));
				 * System.out.println("y2 : " + String.valueOf(y2));
				 */

				/*
				 * g.drawLine(newX.intValue(), newY.intValue(),centerX, centerY);
				 * g.drawString("" + value, centerX - 10, centerY + 30);
				 */
				
				/////////////////////////////////////////////////////////////
				// jauge V2 (triangle)
				//g.fillPolygon(new int[] {186, 196, x2}, new int[] {180, 180, y2}, 3);
				
				
			}

			

		}

	}

	private void startFSUIPC(LeafPane myLeafPane) {
		// First of all, load the native library. The default load function will try to
		// determine if we are running under 32 or 64 bit JVM
		// and load 32/64 bit native library respectively

		fsuipcListener = new IFSUIPCListener() {
			@Override
			public void onConnected() {
				logger.info("FSUIPC connected!");

				fsuipc.processRequestsOnce();

				// clear all previous continual requests, it will also stop processing thread
				fsuipc.clearContinualRequests();

				// register continual requests
				eng1_rpm_cont = (DoubleRequest) fsuipc.addContinualRequest(new DoubleRequest(0x2400));

				fsuipc.processRequests(50, true);

			}

			@Override
			public void onDisconnected() {
				logger.info("FSUIPC disconnected!");

				// cancel continual request processing
				// not needed anymore, the fsuipc class will do it while it discovers that
				// FSUIPC disconnected, before the listener is called
				// fsuipc.cancelRequestsProcessing();

				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						value = 0;
					}
				});

			}

			@Override
			public void onProcess(AbstractQueue<IDataRequest> arRequests) {
				logger.fine("FSUIPC continual request processing callback!");

				// GUI updates on EDT thread
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						System.out.println("Engine 1 RPM: " + String.valueOf(eng1_rpm_cont.getValue()));
						value = eng1_rpm_cont.getValue().intValue();
						myLeafPane.repaint();

					}
				});
			}

			@Override
			public void onFail(int lastResult) {
				logger.log(Level.INFO, "Last FSUIPC function call ended with error code: {0}, message: {1}",
						new Object[] { lastResult,
								FSUIPC.FSUIPC_ERROR_MESSAGES.get(FSUIPCWrapper.FSUIPCResult.get(lastResult)) });
			}
		};

		// add the listener to fsuipc
		fsuipc.addListener(fsuipcListener);

		// start the thread that will wait for successful fsuipc connection, will try
		// every 5 seconds
		fsuipc.waitForConnection(FSUIPCWrapper.FSUIPCSimVersion.SIM_ANY, 5);
	}

	public static class FrameDragListener extends MouseAdapter {

		private final JWindow frame;
		private Point mouseDownCompCoords = null;

		public FrameDragListener(JWindow frame) {
			this.frame = frame;
		}

		public void mouseReleased(MouseEvent e) {
			mouseDownCompCoords = null;
		}

		public void mousePressed(MouseEvent e) {
			mouseDownCompCoords = e.getPoint();
		}

		public void mouseDragged(MouseEvent e) {
			Point currCoords = e.getLocationOnScreen();
			frame.setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
		}
	}

}