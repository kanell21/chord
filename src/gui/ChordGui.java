package gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.NoSuchAlgorithmException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import chord.Master;

public class ChordGui extends JDialog {

	private static final long serialVersionUID = 1L;
	private JFrame mainFrame;
	private JButton joinButton;
	private JButton departButton;
	private Master master;
	private int incNumber;
	
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
		incNumber = 1;
		mainFrame = new JFrame();
		mainFrame.setAlwaysOnTop(true);
		mainFrame.setTitle("Chord GUI");
		mainFrame.setBounds(100, 100, 300, 200);
		mainFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		panel.setToolTipText("");
		mainFrame.getContentPane().add(panel, BorderLayout.CENTER);
		
		
		joinButton = new JButton("join");
		joinButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					master.InsertNode(incNumber++);
					master.CheckAlive(incNumber-1);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
			}
		});
		
		
		
		departButton = new JButton("depart");
		departButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					master.InsertNode(incNumber++);
					master.CheckAlive(incNumber-1);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
			}
		});
		
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewButton.gridx = 2;
		gbc_btnNewButton.gridy = 8;
		panel.add(joinButton, gbc_btnNewButton);
		panel.add(departButton, gbc_btnNewButton);
		
		mainFrame.setVisible(true);

	}
	
	public static void main(String[] args) {
		
		ChordGui chordGUI = new ChordGui();
		chordGUI.setAlwaysOnTop(true);
	}

}
