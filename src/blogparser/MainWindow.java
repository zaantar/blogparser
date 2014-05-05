package blogparser;

import java.awt.EventQueue;

import javax.swing.JFrame;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.DefaultCaret;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JCheckBox;
import java.awt.Font;

public class MainWindow {

	private JFrame frame;
	private JTextField blogNameField;
	private JTextField outputPathField;
	private JTextArea outputArea;
	
	private Thread parseThread;
	private JProgressBar globalProgressBar;
	private JProgressBar currentStepProgressBar;
	private JButton btnBrowse;
	private JButton btnStart;
	private JCheckBox chckbxExtendedLogging;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			System.out.println("cannot set system look and feel");
		}
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if(parseThread == null || !parseThread.isAlive()) {
					frame.dispose();
				}
			}
		});
		frame.setBounds(100, 100, 600, 600);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.getContentPane().setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,}));
		
		JLabel lblBlogName = new JLabel("Blog name");
		frame.getContentPane().add(lblBlogName, "2, 2, right, default");
		
		blogNameField = new JTextField();
		lblBlogName.setLabelFor(blogNameField);
		frame.getContentPane().add(blogNameField, "4, 2, fill, default");
		blogNameField.setColumns(10);
		
		JLabel label = new JLabel("<html>Enter blog's name<br />\n(e.g. for some.blog.cz<br />\nenter \"blog.cz\")</html>");
		frame.getContentPane().add(label, "6, 2");
		
		JLabel lblOutputPath = new JLabel("Output path");
		frame.getContentPane().add(lblOutputPath, "2, 4, right, default");
		
		outputPathField = new JTextField();
		outputPathField.setText("/tmp/blogparser");
		lblOutputPath.setLabelFor(outputPathField);
		frame.getContentPane().add(outputPathField, "4, 4, fill, default");
		outputPathField.setColumns(10);
		
		btnBrowse = new JButton("Browse");
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(frame);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					outputPathField.setText(fc.getSelectedFile().getAbsolutePath());
				}
			}
		});
		frame.getContentPane().add(btnBrowse, "6, 4");
		
		JLabel lblProgress = new JLabel("Progress");
		frame.getContentPane().add(lblProgress, "2, 6, right, default");
		
		globalProgressBar = new JProgressBar();
		lblProgress.setLabelFor(globalProgressBar);
		frame.getContentPane().add(globalProgressBar, "4, 6");
		
		btnStart = new JButton("Start");
		btnStart.setFont(btnStart.getFont().deriveFont(btnStart.getFont().getStyle() | Font.BOLD));
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(outputPathField.getText().isEmpty()) {
					JOptionPane.showMessageDialog(frame, "You have to enter output path.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if(blogNameField.getText().isEmpty()) {
					JOptionPane.showMessageDialog(frame, "You have to enter blog name.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				parseThread = new Thread(new Runnable() {
					@Override
					public void run() {
						outputPathField.setEnabled(false);
						blogNameField.setEnabled(false);
						btnBrowse.setEnabled(false);
						btnStart.setEnabled(false);
						chckbxExtendedLogging.setEnabled(false);

						Logger logger = new TextAreaLogger(getOutputArea(), getGlobalProgressBar(), getCurrentStepProgressBar());
						int verbosity = chckbxExtendedLogging.isSelected() ? 0 : 1;
						logger.setVerbosity(verbosity);
						
						Main.parse(blogNameField.getText(), outputPathField.getText(), logger);

						chckbxExtendedLogging.setEnabled(true);
						outputPathField.setEnabled(true);
						blogNameField.setEnabled(true);
						btnBrowse.setEnabled(true);
						btnStart.setEnabled(true);
					}
				});
				parseThread.start();
			}
		});
		frame.getContentPane().add(btnStart, "6, 6");
		
		JLabel lblCurrentStep = new JLabel("Current step");
		frame.getContentPane().add(lblCurrentStep, "2, 8, right, default");
		
		currentStepProgressBar = new JProgressBar();
		lblCurrentStep.setLabelFor(currentStepProgressBar);
		frame.getContentPane().add(currentStepProgressBar, "4, 8");
		
		chckbxExtendedLogging = new JCheckBox("extended logging");
		frame.getContentPane().add(chckbxExtendedLogging, "6, 8");
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		frame.getContentPane().add(scrollPane, "2, 10, 5, 1, fill, fill");
		
		outputArea = new JTextArea();
		scrollPane.setViewportView(outputArea);
		outputArea.setLineWrap(true);
		outputArea.setEditable(false);
		DefaultCaret caret = (DefaultCaret)outputArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	}
	

	protected JTextArea getOutputArea() {
		return outputArea;
	}
	protected JProgressBar getGlobalProgressBar() {
		return globalProgressBar;
	}
	protected JProgressBar getCurrentStepProgressBar() {
		return currentStepProgressBar;
	}
	protected JButton getBtnBrowse() {
		return btnBrowse;
	}
	protected JButton getBtnStart() {
		return btnStart;
	}
}
