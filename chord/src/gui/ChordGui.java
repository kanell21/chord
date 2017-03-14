package gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import chord.Master;

public class ChordGui extends JDialog {

	private static final long serialVersionUID = 1L;
	private JFrame mainFrame;
	private JButton joinButton;
	private JButton departButton;
	private JButton insertButton;
	private JButton deleteButton;
	private JButton queryButton;
	private JButton tkanelButton;
	private JButton fileButton;
	private JTextField keyText;
	private JTextField valueText;
	private Master master;
	private int incJoinNumber;
	private int incDepartNumber;
	
	private ChordGui() {
		
		try {
			initialize();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		pack();
		this.setModal(true);
		this.toFront();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() throws NoSuchAlgorithmException {
		
		master = new Master();
		incJoinNumber = 1;
		incDepartNumber = 1;
		mainFrame = new JFrame();
		mainFrame.setAlwaysOnTop(true);
		mainFrame.setTitle("Chord GUI");
		mainFrame.setBounds(100, 100, 600, 100);
		mainFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		panel.setToolTipText("");
		mainFrame.getContentPane().add(panel, BorderLayout.CENTER);
		
		
		joinButton = new JButton("join");
		joinButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					master.InsertNode(incJoinNumber++);
					master.CheckAlive(incJoinNumber-1);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
			}
		});
		
		
		departButton = new JButton("depart");
		departButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					if (master.RemoveNode(incDepartNumber) > 0) {
						master.CheckAlive(incDepartNumber++);
					}
				} catch (NoSuchAlgorithmException | InterruptedException | IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewButton.gridx = 2;
		gbc_btnNewButton.gridy = 8;
		
		
		insertButton = new JButton("insert");
		insertButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String key = keyText.getText();
				String value = valueText.getText();
				if (key != null && value != null)
					try {
						master.InsertKeyValue(key, value);
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}				
			}
		});
		
		
		deleteButton = new JButton("delete");
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String key = keyText.getText();
				if (key != null)
					try {
						master.DeleteKey(key);
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}				
			}
		});
		
		
		queryButton = new JButton("query");
		queryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String key = keyText.getText();
				if (key != null)
					try {
						master.Query(key);
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}				
			}
		});
		
		
		tkanelButton = new JButton("tkanel");
		tkanelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				master.TKanel();				
			}
		});
		
		
		fileButton = new JButton("file");
		fileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					master.Fileread();
				} catch (NoSuchAlgorithmException | IOException e) {
					e.printStackTrace();
				}				
			}
		});
		
		
		keyText = new JTextField(15);
		valueText = new JTextField(15);
		
		
		panel.add(joinButton, gbc_btnNewButton);
		panel.add(departButton, gbc_btnNewButton);
		panel.add(insertButton);
		panel.add(deleteButton);
		panel.add(queryButton);
		panel.add(tkanelButton);
		panel.add(fileButton);
		panel.add(keyText);
		panel.add(valueText);
		mainFrame.setVisible(true);
	}
	
	public static void main(String[] args) {

		ChordGui chordGUI = new ChordGui();
		chordGUI.setAlwaysOnTop(true);
	}

}
