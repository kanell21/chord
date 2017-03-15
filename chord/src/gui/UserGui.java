package gui;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import chord.ChordNode;
import chord.Master;

import java.awt.Canvas;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ButtonGroup;
import javax.swing.JTextArea;

public class UserGui {

	private JFrame frame;
	private JButton btnJoin;
	private JButton btnDepart;
	private JButton btnInsert;
	private JButton btnDelete;
	private JButton btnQuery;
	private JButton btnFile;
	private JTextField textFieldJoin;
	private JTextField textFieldDepart;
	private JTextField textFieldInsertKey;
	private JTextField textFieldInsertValue;
	private JTextField textFieldDelete;
	private JTextField textFieldQuery;
	private JTextField textFieldFile;
	private JLabel lblActiveNodes;
	private Canvas canvas;
	private int R = 125;
	private Master master;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JTextArea textArea;
	private JScrollPane scrollPane;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UserGui window = new UserGui();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * @throws NoSuchAlgorithmException 
	 */
	public UserGui() throws NoSuchAlgorithmException {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 * @throws NoSuchAlgorithmException 
	 */
	private void initialize() throws NoSuchAlgorithmException {
		redirectSystemStreams();
		frame = new JFrame();
		frame.getContentPane().setBackground(new Color(211, 211, 211));
		frame.setBounds(280, 70, 800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		btnJoin = new JButton("Join");
		btnJoin.setBackground(new Color(230, 230, 250));
		btnJoin.setBounds(32, 44, 103, 25);
		frame.getContentPane().add(btnJoin);
		btnJoin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					if (textFieldJoin.getText().isEmpty())
						return;
					int num = Integer.parseInt(textFieldJoin.getText());
					if (num > 1023) return;
					master.NodeJoin(num);
					master.CheckAlive(num);
					textFieldJoin.setText(Integer.toString(num + 1));
					textFieldDepart.setText(Integer.toString(num));
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
			}
		});
		
		btnDepart = new JButton("Depart");
		btnDepart.setBackground(new Color(230, 230, 250));
		btnDepart.setBounds(32, 82, 103, 25);
		frame.getContentPane().add(btnDepart);
		btnDepart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					if (textFieldDepart.getText().isEmpty())
						return;
					int num = Integer.parseInt(textFieldDepart.getText());
					if (num > 1023) return;
					if (master.NodeDepart(num) > 0) {
						master.CheckAlive(num);
						textFieldDepart.setText(Integer.toString(num - 1));
					}
				} catch (NoSuchAlgorithmException | InterruptedException | IOException e) {
					e.printStackTrace();
				}
			}
		});
				
		btnInsert = new JButton("Insert");
		btnInsert.setBackground(new Color(230, 230, 250));
		btnInsert.setBounds(32, 120, 103, 25);
		frame.getContentPane().add(btnInsert);
		btnInsert.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (textFieldInsertKey.getText().isEmpty())
					return;
				if (textFieldInsertValue.getText().isEmpty())
					return;
				String key = textFieldInsertKey.getText();
				String value = textFieldInsertValue.getText();
				if (key != null && value != null)
					try {
						master.InsertKeyValue(key, value, false);
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}				
			}
		});
		
		btnDelete = new JButton("Delete");
		btnDelete.setBackground(new Color(230, 230, 250));
		btnDelete.setBounds(32, 158, 103, 25);
		frame.getContentPane().add(btnDelete);
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (textFieldDelete.getText().isEmpty())
					return;
				String key = textFieldDelete.getText();
				try {
					master.DeleteKey(key, false);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}				
			}
		});
		
		btnQuery = new JButton("Query");
		btnQuery.setBackground(new Color(230, 230, 250));
		btnQuery.setBounds(32, 196, 103, 25);
		frame.getContentPane().add(btnQuery);
		btnQuery.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (textFieldQuery.getText().isEmpty())
					return;
				String key = textFieldQuery.getText();
				if (key != null)
					try {
						master.Query(key, false);
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}				
			}
		});
		
		btnFile = new JButton("File");
		btnFile.setBackground(new Color(230, 230, 250));
		btnFile.setBounds(32, 234, 103, 25);
		frame.getContentPane().add(btnFile);
		btnFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (textFieldFile.getText().isEmpty())
					return;
				try {
					master.Fileread(textFieldFile.getText());
				} catch (NoSuchAlgorithmException | IOException e) {
					e.printStackTrace();
				}				
			}
		});
		
		textFieldJoin = new JTextField();
		textFieldJoin.setBounds(172, 46, 81, 19);
		frame.getContentPane().add(textFieldJoin);
		textFieldJoin.setColumns(10);
		textFieldJoin.setText("1");
		
		textFieldDepart = new JTextField();
		textFieldDepart.setBounds(172, 84, 81, 19);
		frame.getContentPane().add(textFieldDepart);
		textFieldDepart.setColumns(10);
		
		textFieldInsertKey = new JTextField();
		textFieldInsertKey.setBounds(172, 122, 191, 19);
		frame.getContentPane().add(textFieldInsertKey);
		textFieldInsertKey.setColumns(10);
		
		textFieldInsertValue = new JTextField();
		textFieldInsertValue.setBounds(370, 122, 81, 19);
		frame.getContentPane().add(textFieldInsertValue);
		textFieldInsertValue.setColumns(10);
		
		textFieldDelete = new JTextField();
		textFieldDelete.setBounds(172, 160, 280, 19);
		frame.getContentPane().add(textFieldDelete);
		textFieldDelete.setColumns(10);
		
		textFieldQuery = new JTextField();
		textFieldQuery.setBounds(172, 198, 280, 19);
		frame.getContentPane().add(textFieldQuery);
		textFieldQuery.setColumns(10);
		
		textFieldFile = new JTextField();
		textFieldFile.setBounds(172, 236, 280, 19);
		frame.getContentPane().add(textFieldFile);
		textFieldFile.setColumns(10);
		
		lblActiveNodes = new JLabel("active nodes");
		lblActiveNodes.setBounds(570, 323, 170, 19);
		frame.getContentPane().add(lblActiveNodes);
		
		JRadioButton rdbtnLinearizability = new JRadioButton("Linearizability");
		rdbtnLinearizability.setBackground(new Color(211, 211, 211));
		buttonGroup.add(rdbtnLinearizability);
		rdbtnLinearizability.setBounds(280, 45, 179, 23);
		frame.getContentPane().add(rdbtnLinearizability);
		rdbtnLinearizability.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Master.MODE = "lin";		
				int key;
				ChordNode tmpNode;
				for (Iterator<Integer> iter = Master.chord.keySet().iterator(); iter.hasNext(); ) {
					key = iter.next();
					tmpNode = Master.chord.get(key);
					tmpNode.MODE = "lin";
				}
				System.out.println("MODE set to 'lin'");
			}
		});
		
		JRadioButton rdbtnEventualConsistency = new JRadioButton("Eventual Consistency");
		rdbtnEventualConsistency.setBackground(new Color(211, 211, 211));
		buttonGroup.add(rdbtnEventualConsistency);
		rdbtnEventualConsistency.setBounds(280, 82, 179, 23);
		frame.getContentPane().add(rdbtnEventualConsistency);
		rdbtnEventualConsistency.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Master.MODE = "ev_con";
				int key;
				ChordNode tmpNode;
				for (Iterator<Integer> iter = Master.chord.keySet().iterator(); iter.hasNext(); ) {
					key = iter.next();
					tmpNode = Master.chord.get(key);
					tmpNode.MODE = "ev_con";
				}
				System.out.println("MODE set to 'ev_con'");
			}
		});
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(32, 354, 540, 220);
		frame.getContentPane().add(scrollPane);
		
		textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		textArea.setBackground(Color.BLACK);
		textArea.setFont(new Font(Font.SANS_SERIF, 1, 12));
		textArea.setForeground(Color.GREEN);
		textArea.setEditable(false);
		
		rdbtnLinearizability.setSelected(true);
		textFieldQuery.setText("*");
		textFieldFile.setText("/home/dimosthenis/insert.txt");
		canvas = new Canvas() {

			private static final long serialVersionUID = 1L;

			@Override
		    public void paint(Graphics g) {
				
				g.setColor(Color.BLACK);
				g.drawArc(2, 2, 2*R, 2*R, 0, 360);
				
				g.setColor(Color.RED);
				double x, y, angle;
				int key;
				
				for (Iterator<Integer> iter = Master.chord.keySet().iterator(); iter.hasNext(); ) {
					key = iter.next();
					angle = key * 360 / 1024 - 90;
					angle = Math.toRadians(angle);
					x = 2 + R * (1 + Math.cos(angle));
					y = 2 + R * (1 + Math.sin(angle));				
					
					g.fillOval((int)(x - 3.5), (int)(y - 3.5), 7, 7);
				}
				g.setColor(Color.BLUE);
				angle = -90;
				angle = Math.toRadians(angle);
				x = 2 + R * (1 + Math.cos(angle));
				y = 2 + R * (1 + Math.sin(angle));	
				g.fillOval((int)(x - 3.5), (int)(y - 3.5), 7, 7);
				
				lblActiveNodes.setText(Master.chord.size() + " active nodes, k = " + Master.K);
		    }
		};
		canvas.setBounds(520, 63, 270, 270);
		frame.getContentPane().add(canvas);
		
		master = new Master(this.canvas);
	}
	
	private void updateTextArea(final String text) {
		  SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		      textArea.append(text);
		    }
		  });
	}
		 
	private void redirectSystemStreams() {
		 OutputStream out = new OutputStream() {
			 @Override
			 public void write(int b) throws IOException {
				 updateTextArea(String.valueOf((char) b));
			 }
			 
			 @Override
			 public void write(byte[] b, int off, int len) throws IOException {
			     updateTextArea(new String(b, off, len));
			 }
			 
			 @Override
			 public void write(byte[] b) throws IOException {
				 write(b, 0, b.length);
			 }
		 };
		 
		 System.setOut(new PrintStream(out, true));
		 System.setErr(new PrintStream(out, true));
	}
}
