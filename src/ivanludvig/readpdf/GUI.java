package ivanludvig.readpdf;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class GUI extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	private JTextField textField_1;

	String input, output, filename;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI frame = new GUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 482, 333);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		JLabel lblReadpdf = new JLabel("readPDF");
		lblReadpdf.setBounds(158, 11, 149, 73);
		lblReadpdf.setFont(new Font("Consolas",Font.PLAIN, 36));
		
		textField = new JTextField();
		textField.setBounds(104, 106, 238, 20);
		textField.setColumns(10);
		
		JLabel lblInputPath = new JLabel("Input File:");
		lblInputPath.setBounds(20, 109, 79, 14);
		
		JLabel lblOutputPath = new JLabel("Output Folder:");
		lblOutputPath.setBounds(20, 152, 79, 14);
		
		textField_1 = new JTextField();
		textField_1.setBounds(106, 149, 238, 20);
		textField_1.setColumns(10);
		
		JLabel label = new JLabel("");
		label.setBounds(161, 263, 143, 14);
		label.setAlignmentX(CENTER_ALIGNMENT);
		contentPane.add(label);
		
		
		JButton btnNewButton = new JButton("Choose");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileNameExtensionFilter("PDF files", "pdf"));
	            int option = fileChooser.showOpenDialog(contentPane);
	            if(option == JFileChooser.APPROVE_OPTION){
	               File file = fileChooser.getSelectedFile();
	               textField.setText(file.getAbsolutePath());
	               filename = file.getName();
	               input = file.getAbsolutePath();
	            }
			}
		});
		
		JButton btnConvert = new JButton("Convert");
		btnConvert.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				label.setText("...");
				try {
					Main main = new Main(input, output, filename);
					label.setText("Done!");
				} catch (IOException | ParserConfigurationException | TransformerException | NullPointerException e1) {
					label.setText("Error");
				}
			}
		});
		btnConvert.setBounds(161, 197, 143, 55);
		
//		JLabel lblIcon = new JLabel(new ImageIcon("res/icon64.png"));
//		lblIcon.setBounds(64, 13, 64, 71);
		contentPane.setLayout(null);
		contentPane.add(btnConvert);
//		contentPane.add(lblIcon);
		contentPane.add(lblReadpdf);
		contentPane.add(lblOutputPath);
		contentPane.add(textField_1);
		contentPane.add(lblInputPath);
		contentPane.add(textField);
		

		btnNewButton.setBounds(352, 105, 79, 23);
		contentPane.add(btnNewButton);
		
		JButton btnNewButton_1 = new JButton("Choose");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
	            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	            int option = fileChooser.showOpenDialog(contentPane);
	            if(option == JFileChooser.APPROVE_OPTION){
	               File file = fileChooser.getSelectedFile();
	               textField_1.setText(file.getAbsolutePath());
	               output = file.getAbsolutePath();
	            }
			}
		});
		btnNewButton_1.setBounds(354, 148, 77, 23);
		contentPane.add(btnNewButton_1);
		
	}
}
