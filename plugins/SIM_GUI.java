import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;

import java.util.Arrays;
import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.Panel;
import java.awt.Button;
import java.awt.TextField;
import java.awt.Label;
import java.awt.GridLayout;

import javax.swing.BoxLayout;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.JSeparator;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.JMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import java.awt.Choice;

import javax.swing.JLayeredPane;
import javax.swing.JInternalFrame;

import java.awt.FlowLayout;

import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import javax.swing.JSpinner;
import javax.swing.JTree;
import javax.swing.JProgressBar;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.border.LineBorder;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import javax.swing.JFormattedTextField;
import javax.swing.JList;
import javax.swing.SpinnerNumberModel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.ListSelectionModel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JPopupMenu;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTextPane;
import javax.swing.JTextArea;


public class SIM_GUI extends JFrame implements PlugIn {

	private JPanel contentPane;
	private JTextField OTFMeasuredOpenFilePath;
	private JTextField inputFilePath;
	private JTextField FringeOpenFilePath;
	private JTextField NumericalApetureText;
	private JTextField EmissionWavelengthText;
	private JTextField PixResText;
	
	final JFileChooser fc = new JFileChooser();
	
	ImagePlus input;

	private double fringeFrequency[];
	private double fringeAngle[];
	
	List<Double> listfringeFrequency = new ArrayList<Double>();
	List<Double> listfringeAngle = new ArrayList<Double>();
	
	double dx; // pixel size of the image [um] (field of view [um]/number of pixels in image)
	double n_immersion; // refractive index of immersion oil
	double lambda; // centre wavelength for fluorescence emission
	double NA; // numerical aperture of lens
	double r;
	float r0;
	
	int Orientations = 3;
	int Phases = 3;
	private NumberFormat intformat ;
	private JTable FringeParametersTable;
	private JScrollPane scrollPane;
	private String[] FringeColumnHeadings = new String[] {"Frequency", "Angle"};
	JFrame frame;
	protected ImagePlus input_OTF2D;
	protected float[][] OTF2D;
	protected boolean Threaded = false;
	protected JTextComponent textPane;
	private JScrollPane scrollPane_1;
	protected int OTFpanel_state =0;
	static JProgressBar progressBar;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					
			        UIManager.setLookAndFeel(
			                UIManager.getSystemLookAndFeelClassName());
					SIM_GUI frame = new SIM_GUI();
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
	public SIM_GUI() {
		this.frame = this;
		intformat= NumberFormat.getNumberInstance();
		//intformat.setMinimumIntegerDigits(1);
		intformat.setParseIntegerOnly(true);
		
		
		setTitle("Structured Illumination Microscopy Image Processor");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 471, 726);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);
		
		JPanel FileInputPanel = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.NORTH, FileInputPanel, 10, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, FileInputPanel, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, FileInputPanel, 120, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, FileInputPanel, -10, SpringLayout.EAST, contentPane);
		FileInputPanel.setBorder(new TitledBorder(null, "File", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPane.add(FileInputPanel);
		SpringLayout sl_FileInputPanel = new SpringLayout();
		FileInputPanel.setLayout(sl_FileInputPanel);
		
		inputFilePath = new JTextField();
		inputFilePath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(new File(inputFilePath.getText().toString()).isFile())
				{
				input = IJ.openImage(inputFilePath.getText().toString());
				}
				else{
					JOptionPane.showMessageDialog(frame,"No such file exists ERR 3","Message",JOptionPane.PLAIN_MESSAGE);
					System.out.println(inputFilePath.toString());
				}
				
			}
		});
		sl_FileInputPanel.putConstraint(SpringLayout.NORTH, inputFilePath, 10, SpringLayout.NORTH, FileInputPanel);
		sl_FileInputPanel.putConstraint(SpringLayout.WEST, inputFilePath, 10, SpringLayout.WEST, FileInputPanel);
		sl_FileInputPanel.putConstraint(SpringLayout.EAST, inputFilePath, -40, SpringLayout.EAST, FileInputPanel);
		inputFilePath.setHorizontalAlignment(SwingConstants.LEFT);
		inputFilePath.setColumns(10);
		FileInputPanel.add(inputFilePath);
		
		JButton InputOpenButton = new JButton("");
		InputOpenButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
	            FileNameExtensionFilter tifffilter = new FileNameExtensionFilter("Tiff Stacks (*.tiff)", "tiff","tif");
	                fc.setFileFilter(tifffilter);
	                fc.setDialogTitle("Open Tiff Stack");

	                
				
				int returnVal = fc.showOpenDialog(SIM_GUI.this);
				
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
				     System.out.println("Opening: " + file.getName());				     
				     input = new ImagePlus(file.getAbsolutePath());	
				     inputFilePath.setText(file.getAbsolutePath());
				     input.show();
				     
				     
	            } else {
	            	System.out.println("Open command cancelled by user.");
	            }		
				
			}
		});
		InputOpenButton.setIcon(new ImageIcon(SIM_GUI.class.getResource("/javax/swing/plaf/metal/icons/ocean/directory.gif")));
		sl_FileInputPanel.putConstraint(SpringLayout.NORTH, InputOpenButton, 0, SpringLayout.NORTH, inputFilePath);
		sl_FileInputPanel.putConstraint(SpringLayout.WEST, InputOpenButton, 6, SpringLayout.EAST, inputFilePath);
		sl_FileInputPanel.putConstraint(SpringLayout.SOUTH, InputOpenButton, 20, SpringLayout.NORTH, inputFilePath);
		sl_FileInputPanel.putConstraint(SpringLayout.EAST, InputOpenButton, -10, SpringLayout.EAST, FileInputPanel);
		FileInputPanel.add(InputOpenButton);
		
		final JTabbedPane OTFPanel = new JTabbedPane(JTabbedPane.TOP);
		sl_contentPane.putConstraint(SpringLayout.WEST, OTFPanel, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, OTFPanel, -10, SpringLayout.EAST, contentPane);
		OTFPanel.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (OTFPanel.getTitleAt(OTFPanel.getSelectedIndex()).equals("Measured")){
					OTFpanel_state = 0;
				}
				if (OTFPanel.getTitleAt(OTFPanel.getSelectedIndex()).equals("Theoretical")){
					OTFpanel_state = 1;
					dx = 0.04d; // pixel size of the image [um] (field of view [um]/number of pixels in image)
					n_immersion = 1.515d; // refractive index of immersion oil
					lambda = 0.520d; // centre wavelength for fluorescence emission
					NA = 1.3d; // numerical aperture of lens
					//System.out.println("OTFPanel Selected");
				}
			}
		});
		OTFPanel.setBorder(new TitledBorder(null, "OTF", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPane.add(OTFPanel);
		
		JPanel OTFMeasuredPanel = new JPanel();
		OTFPanel.addTab("Measured", null, OTFMeasuredPanel, null);
		SpringLayout sl_OTFMeasuredPanel = new SpringLayout();
		OTFMeasuredPanel.setLayout(sl_OTFMeasuredPanel);
		

		
		OTFMeasuredOpenFilePath = new JTextField();
		sl_OTFMeasuredPanel.putConstraint(SpringLayout.NORTH, OTFMeasuredOpenFilePath, 10, SpringLayout.NORTH, OTFMeasuredPanel);
		sl_OTFMeasuredPanel.putConstraint(SpringLayout.WEST, OTFMeasuredOpenFilePath, 10, SpringLayout.WEST, OTFMeasuredPanel);
		sl_OTFMeasuredPanel.putConstraint(SpringLayout.EAST, OTFMeasuredOpenFilePath, -40, SpringLayout.EAST, OTFMeasuredPanel);
		OTFMeasuredOpenFilePath.setHorizontalAlignment(SwingConstants.LEFT);
		OTFMeasuredPanel.add(OTFMeasuredOpenFilePath);
		OTFMeasuredOpenFilePath.setColumns(10);
		
		JButton OTFMeasuredOpenButton = new JButton("");
		OTFMeasuredOpenButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				OTFpanel_state = 0;
				
	            FileNameExtensionFilter tifffilter = new FileNameExtensionFilter("Tiff Image(*.tiff)", "tiff","tif");
                fc.setFileFilter(tifffilter);
                fc.setDialogTitle("Open Tiff Image");

                
			
			int returnVal = fc.showOpenDialog(SIM_GUI.this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
			     System.out.println("Opening: " + file.getName());				     
			     input_OTF2D = new ImagePlus(file.getAbsolutePath());			
			     //input_OTF2D.show();
			     OTF2D = input_OTF2D.getProcessor().getFloatArray();
			     OTFMeasuredOpenFilePath.setText(file.getAbsolutePath());

		
			     OTF2D = Sim_2D.pad(new ImagePlus(null,new FloatProcessor(OTF2D)),
			    		 (int) Math.round((input.getWidth()*2-OTF2D.length)/2),
			    		 (int) Math.round((input.getWidth()*2-OTF2D.length)/2),
			    		 (int) Math.round((input.getWidth()*2-OTF2D[0].length)/2),
			    		 (int) Math.round((input.getWidth()*2-OTF2D[0].length)/2)
			    		 ).getProcessor().getFloatArray();
			     new ImagePlus(null,new FloatProcessor(OTF2D)).show();
			     
            } else {
            	System.out.println("Open command cancelled by user.");
            }		
			
			
				
				
			}
		});
		sl_OTFMeasuredPanel.putConstraint(SpringLayout.NORTH, OTFMeasuredOpenButton, 0, SpringLayout.NORTH, OTFMeasuredOpenFilePath);
		sl_OTFMeasuredPanel.putConstraint(SpringLayout.WEST, OTFMeasuredOpenButton, 6, SpringLayout.EAST, OTFMeasuredOpenFilePath);
		sl_OTFMeasuredPanel.putConstraint(SpringLayout.SOUTH, OTFMeasuredOpenButton, 20, SpringLayout.NORTH, OTFMeasuredOpenFilePath);
		sl_OTFMeasuredPanel.putConstraint(SpringLayout.EAST, OTFMeasuredOpenButton, -10, SpringLayout.EAST, OTFMeasuredPanel);
		OTFMeasuredOpenButton.setIcon(new ImageIcon(SIM_GUI.class.getResource("/javax/swing/plaf/metal/icons/ocean/directory.gif")));
		OTFMeasuredOpenButton.setSelectedIcon(new ImageIcon(SIM_GUI.class.getResource("/javax/swing/plaf/metal/icons/ocean/directory.gif")));
		OTFMeasuredPanel.add(OTFMeasuredOpenButton);
		
		JPanel OTTTheoryPanel = new JPanel();
		OTFPanel.addTab("Theoretical", null, OTTTheoryPanel, null);
		SpringLayout sl_OTTTheoryPanel = new SpringLayout();
		OTTTheoryPanel.setLayout(sl_OTTTheoryPanel);
		
		JLabel lblNewLabel = new JLabel("Numerical Apeture");
		sl_OTTTheoryPanel.putConstraint(SpringLayout.WEST, lblNewLabel, 25, SpringLayout.WEST, OTTTheoryPanel);
		OTTTheoryPanel.add(lblNewLabel);
		
		JLabel lblEmissionWavelength = new JLabel("Emission Wavelength");
		sl_OTTTheoryPanel.putConstraint(SpringLayout.NORTH, lblEmissionWavelength, 12, SpringLayout.SOUTH, lblNewLabel);
		sl_OTTTheoryPanel.putConstraint(SpringLayout.WEST, lblEmissionWavelength, 9, SpringLayout.WEST, OTTTheoryPanel);
		sl_OTTTheoryPanel.putConstraint(SpringLayout.EAST, lblNewLabel, 0, SpringLayout.EAST, lblEmissionWavelength);
		OTTTheoryPanel.add(lblEmissionWavelength);
		
		JLabel lblPixelResolution = new JLabel("Pixel Resolution");
		sl_OTTTheoryPanel.putConstraint(SpringLayout.SOUTH, lblEmissionWavelength, -11, SpringLayout.NORTH, lblPixelResolution);
		sl_OTTTheoryPanel.putConstraint(SpringLayout.WEST, lblPixelResolution, 40, SpringLayout.WEST, OTTTheoryPanel);
		sl_OTTTheoryPanel.putConstraint(SpringLayout.EAST, lblPixelResolution, 0, SpringLayout.EAST, lblNewLabel);
		OTTTheoryPanel.add(lblPixelResolution);
		
		NumericalApetureText = new JTextField();
		sl_OTTTheoryPanel.putConstraint(SpringLayout.NORTH, NumericalApetureText, 7, SpringLayout.NORTH, OTTTheoryPanel);
		sl_OTTTheoryPanel.putConstraint(SpringLayout.WEST, NumericalApetureText, 138, SpringLayout.WEST, OTTTheoryPanel);
		sl_OTTTheoryPanel.putConstraint(SpringLayout.NORTH, lblNewLabel, 2, SpringLayout.NORTH, NumericalApetureText);
		OTTTheoryPanel.add(NumericalApetureText);
		NumericalApetureText.setColumns(10);
		NumericalApetureText.setText("1.4");
		
		
		EmissionWavelengthText = new JTextField();
		sl_OTTTheoryPanel.putConstraint(SpringLayout.NORTH, EmissionWavelengthText, 33, SpringLayout.NORTH, OTTTheoryPanel);
		sl_OTTTheoryPanel.putConstraint(SpringLayout.WEST, EmissionWavelengthText, 138, SpringLayout.WEST, OTTTheoryPanel);
		sl_OTTTheoryPanel.putConstraint(SpringLayout.EAST, lblEmissionWavelength, -7, SpringLayout.WEST, EmissionWavelengthText);
		OTTTheoryPanel.add(EmissionWavelengthText);
		EmissionWavelengthText.setColumns(10);
		EmissionWavelengthText.setText("488");
		
		
		PixResText = new JTextField();
		sl_OTTTheoryPanel.putConstraint(SpringLayout.NORTH, lblPixelResolution, 3, SpringLayout.NORTH, PixResText);
		sl_OTTTheoryPanel.putConstraint(SpringLayout.NORTH, PixResText, 59, SpringLayout.NORTH, OTTTheoryPanel);
		sl_OTTTheoryPanel.putConstraint(SpringLayout.WEST, PixResText, 138, SpringLayout.WEST, OTTTheoryPanel);
		sl_OTTTheoryPanel.putConstraint(SpringLayout.SOUTH, EmissionWavelengthText, -6, SpringLayout.NORTH, PixResText);
		OTTTheoryPanel.add(PixResText);
		PixResText.setColumns(10);
		PixResText.setText("65");
		
		
		JLabel lblAu = new JLabel("A.U");
		sl_OTTTheoryPanel.putConstraint(SpringLayout.NORTH, lblAu, 0, SpringLayout.NORTH, lblNewLabel);
		sl_OTTTheoryPanel.putConstraint(SpringLayout.WEST, lblAu, 6, SpringLayout.EAST, NumericalApetureText);
		OTTTheoryPanel.add(lblAu);
		
		JLabel lblnm = new JLabel("nm");
		sl_OTTTheoryPanel.putConstraint(SpringLayout.NORTH, lblnm, 0, SpringLayout.NORTH, lblEmissionWavelength);
		sl_OTTTheoryPanel.putConstraint(SpringLayout.WEST, lblnm, 6, SpringLayout.EAST, EmissionWavelengthText);
		OTTTheoryPanel.add(lblnm);
		
		JLabel lblNm = new JLabel("nm / px");
		sl_OTTTheoryPanel.putConstraint(SpringLayout.NORTH, lblNm, 12, SpringLayout.SOUTH, lblnm);
		sl_OTTTheoryPanel.putConstraint(SpringLayout.WEST, lblNm, 6, SpringLayout.EAST, PixResText);
		sl_OTTTheoryPanel.putConstraint(SpringLayout.SOUTH, lblNm, -69, SpringLayout.SOUTH, OTTTheoryPanel);
		OTTTheoryPanel.add(lblNm);
		
		JButton SavePreferencesButton = new JButton("Save Preferences");
		SavePreferencesButton.setEnabled(false);
		sl_contentPane.putConstraint(SpringLayout.NORTH, SavePreferencesButton, 5, SpringLayout.SOUTH, OTFPanel);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, SavePreferencesButton, 30, SpringLayout.SOUTH, OTFPanel);
		sl_contentPane.putConstraint(SpringLayout.EAST, SavePreferencesButton, 0, SpringLayout.EAST, OTFPanel);
		
		SavePreferencesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		contentPane.add(SavePreferencesButton);
		
		JButton ProcessButton = new JButton("Process");
		sl_contentPane.putConstraint(SpringLayout.NORTH, ProcessButton, 0, SpringLayout.NORTH, SavePreferencesButton);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, ProcessButton, 0, SpringLayout.SOUTH, SavePreferencesButton);
		sl_contentPane.putConstraint(SpringLayout.EAST, ProcessButton, -5, SpringLayout.WEST, SavePreferencesButton);
		ProcessButton.addActionListener(new ActionListener() {
			

			public void actionPerformed(ActionEvent e) {
				redirectSystemStreams();
				new Thread("SIM_Plugin"){		

				
				public void run(){
					float[][] trianglemask = new float[input.getWidth()][input.getHeight()];
					for (float[] row: trianglemask)
						Arrays.fill(row,(float) 1);
					
				Sim_2D Sim_2D = new Sim_2D();
				
				if (OTFpanel_state == 1){
				dx = Double.parseDouble(PixResText.getText())*0.001d;
				lambda = Double.parseDouble(EmissionWavelengthText.getText())*0.001d;
				NA = Double.parseDouble(NumericalApetureText.getText());	
				
				r = (2d * NA)/(lambda*dx);
				System.out.println("OTF Radius /px"+" \t"+ r);
				r0 = (float) (2d*r)/(input.getWidth());				
				OTF2D = new OTF_2D(input.getWidth()*2,r0).get();
				trianglemask = Sim_2D.TriangleMask(input.getWidth()*2,(float) r0*input.getWidth(),0,0);
				//new ImagePlus(null, new FloatProcessor(OTF2D)).show();
				
				}
				
				//Sim_2D.setup(progressBar,input,OTF2D,fringeFrequency,fringeAngle,dx,n_immersion,lambda,NA,Orientations,Phases);
				Sim_2D.setup(progressBar,input,OTF2D,fringeFrequency,fringeAngle,Orientations,Phases,trianglemask);
				//TODO Add complex OTF support
				
				//Sim_2D.setup(progressBar,input,OTF2D,fringeFrequency,fringeAngle,dx,n_immersion,lambda,NA,Orientations,Phases);
				
			        	Sim_2D.run(null);
			        }
			      }.start();
				
				
				
			}
		});
		contentPane.add(ProcessButton);
		
		JTabbedPane FringeParamsTab = new JTabbedPane(JTabbedPane.TOP);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, OTFPanel, 150, SpringLayout.SOUTH, FringeParamsTab);
		sl_contentPane.putConstraint(SpringLayout.EAST, FringeParamsTab, -200, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.NORTH, OTFPanel, 5, SpringLayout.SOUTH, FringeParamsTab);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, FringeParamsTab, 200, SpringLayout.SOUTH, FileInputPanel);
		sl_contentPane.putConstraint(SpringLayout.NORTH, FringeParamsTab, 2, SpringLayout.SOUTH, FileInputPanel);
		sl_contentPane.putConstraint(SpringLayout.WEST, FringeParamsTab, 10, SpringLayout.WEST, contentPane);
		FringeParamsTab.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Fringe Parameters", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPane.add(FringeParamsTab);
		
		JPanel loadParamsPanel = new JPanel();
		FringeParamsTab.addTab("Load", null, loadParamsPanel, null);
		SpringLayout sl_loadParamsPanel = new SpringLayout();
		loadParamsPanel.setLayout(sl_loadParamsPanel);
		
		JButton FringeOpenButton = new JButton("");
		FringeOpenButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				FileNameExtensionFilter csvFilter = new FileNameExtensionFilter("CSV Fringe Parameters (*.csv)", "csv","txt");
                fc.setFileFilter(csvFilter);
                fc.setDialogTitle("Open Fringe Paramters File");

                
			
			int returnVal = fc.showOpenDialog(SIM_GUI.this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
			     System.out.println("Opening: " + file.getName());
			     
			     FringeOpenFilePath.setText(file.getAbsolutePath());
			     //input = new ImagePlus(file.getAbsolutePath());			
			     //input.show();
			     
			     try {
						CSVReader csvReader = new CSVReader(new FileReader(FringeOpenFilePath.getText().toString()));
						String[] row = null;
						listfringeFrequency.clear();
						listfringeAngle.clear();
						while((row = csvReader.readNext()) != null) {
							
							listfringeFrequency.add(Double.parseDouble((row[0]))) ;
							listfringeAngle.add(Double.parseDouble((row[1])));

						}
						csvReader.close();
						fringeFrequency = new double[listfringeFrequency.size()];
						fringeAngle = new double[listfringeAngle.size()];
						
						//System.out.println(listfringeFrequency.size());
						

						DefaultTableModel FringeParametersTablemodel = new DefaultTableModel(
								new Object[listfringeFrequency.size()][2],
								FringeColumnHeadings
							);
						
						for (int i =0; i <listfringeFrequency.size();i++)
						{
							fringeFrequency[i] = listfringeFrequency.get(i).doubleValue();							
							fringeAngle[i] = listfringeAngle.get(i).doubleValue();
								FringeParametersTablemodel.setValueAt(fringeFrequency[i], i, 0);
								FringeParametersTablemodel.setValueAt(fringeAngle[i], i, 1);
								
						}
						
						FringeParametersTable.setModel(FringeParametersTablemodel);
						scrollPane.setViewportView(FringeParametersTable);

						
					} catch (FileNotFoundException e1) {
						
						
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(frame,"No such file exists ERR4","Message",JOptionPane.PLAIN_MESSAGE);
					} catch (NumberFormatException e1) {
						JOptionPane.showMessageDialog(frame,"File Corrupt, foul characters","Message",JOptionPane.PLAIN_MESSAGE);
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			     
			     
			     
			     
			     
			     
            } else {
            	System.out.println("Open command cancelled by user.");
            }		
			
			
			}
		});
		FringeOpenButton.setIcon(new ImageIcon(SIM_GUI.class.getResource("/com/sun/java/swing/plaf/windows/icons/Directory.gif")));
		sl_loadParamsPanel.putConstraint(SpringLayout.NORTH, FringeOpenButton, 10, SpringLayout.NORTH, loadParamsPanel);
		loadParamsPanel.add(FringeOpenButton);
		
		FringeOpenFilePath = new JTextField();
		sl_loadParamsPanel.putConstraint(SpringLayout.EAST, FringeOpenButton, 30, SpringLayout.EAST, FringeOpenFilePath);
		FringeOpenFilePath.addActionListener(new ActionListener() {
			

			public void actionPerformed(ActionEvent e) {
				
				if(new File(FringeOpenFilePath.getText().toString()).isFile())
				{
				//TODO Add csv code
					
					try {
						CSVReader csvReader = new CSVReader(new FileReader(FringeOpenFilePath.getText().toString()));
						String[] row = null;
						listfringeFrequency.clear();
						listfringeAngle.clear();
						while((row = csvReader.readNext()) != null) {
							
							listfringeFrequency.add(Double.parseDouble((row[0]))) ;
							listfringeAngle.add(Double.parseDouble((row[1])));

						}
						csvReader.close();
						fringeFrequency = new double[listfringeFrequency.size()];
						fringeAngle = new double[listfringeAngle.size()];
						
						System.out.println(listfringeFrequency.size());
						

						DefaultTableModel FringeParametersTablemodel = new DefaultTableModel(
								new Object[listfringeFrequency.size()][2],
								FringeColumnHeadings 
							);
						
						for (int i =0; i <listfringeFrequency.size();i++)
						{
							fringeFrequency[i] = listfringeFrequency.get(i).doubleValue();							
							fringeAngle[i] = listfringeAngle.get(i).doubleValue();
								FringeParametersTablemodel.setValueAt(fringeFrequency[i], i, 0);
								FringeParametersTablemodel.setValueAt(fringeAngle[i], i, 1);
								
						}
						
						FringeParametersTable.setModel(FringeParametersTablemodel);
						scrollPane.setViewportView(FringeParametersTable);

						
					} catch (FileNotFoundException e1) {
						
						
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(frame,"No such file exists ERR4","Message",JOptionPane.PLAIN_MESSAGE);
					} catch (NumberFormatException e1) {
						JOptionPane.showMessageDialog(frame,"File Corrupt, foul characters","Message",JOptionPane.PLAIN_MESSAGE);
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
				}
				else{
					JOptionPane.showMessageDialog(frame,"No such file exists ERR2","Message",JOptionPane.PLAIN_MESSAGE);
				}
				
			}
				
				
			
		});
		sl_loadParamsPanel.putConstraint(SpringLayout.WEST, FringeOpenButton, 6, SpringLayout.EAST, FringeOpenFilePath);
		sl_loadParamsPanel.putConstraint(SpringLayout.SOUTH, FringeOpenButton, 20, SpringLayout.NORTH, FringeOpenFilePath);
		sl_loadParamsPanel.putConstraint(SpringLayout.EAST, FringeOpenFilePath, -40, SpringLayout.EAST, loadParamsPanel);
		sl_loadParamsPanel.putConstraint(SpringLayout.NORTH, FringeOpenFilePath, 10, SpringLayout.NORTH, loadParamsPanel);
		sl_loadParamsPanel.putConstraint(SpringLayout.WEST, FringeOpenFilePath, 10, SpringLayout.WEST, loadParamsPanel);
		FringeOpenFilePath.setHorizontalAlignment(SwingConstants.LEFT);
		FringeOpenFilePath.setColumns(10);
		loadParamsPanel.add(FringeOpenFilePath);
		
		JPanel computeParamsPanel = new JPanel();
		FringeParamsTab.addTab("Compute", null, computeParamsPanel, null);
		SpringLayout sl_computeParamsPanel = new SpringLayout();
		computeParamsPanel.setLayout(sl_computeParamsPanel);
		
		JCheckBox ComputeDuringProcessChkBx = new JCheckBox("Compute during process");
		sl_computeParamsPanel.putConstraint(SpringLayout.WEST, ComputeDuringProcessChkBx, 10, SpringLayout.WEST, computeParamsPanel);
		computeParamsPanel.add(ComputeDuringProcessChkBx);
		
		JButton ComputeFringeParamsButton = new JButton("Robust Fringe Registration");
		sl_computeParamsPanel.putConstraint(SpringLayout.EAST, ComputeFringeParamsButton, 171, SpringLayout.WEST, computeParamsPanel);
		ComputeFringeParamsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				redirectSystemStreams();
				new Thread("FringeParams_Plugin"){
				public void run(){
				Fringe_Params FringeParams = new Fringe_Params();
					try {
						;
						FringeParams.calcFringeParams(null,input,Orientations,Phases,Threaded);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				

				//fringeFrequency = new double[]{136.852546526160,135.741042289866,135.439705087354};
				//fringeAngle = new double[]{-0.513593210927861,0.526789085380402,-1.55759150441705};
				
				fringeFrequency = FringeParams.fringeFrequency();
				fringeAngle = FringeParams.fringeAngle();				

				
				System.out.println("Frequency Array " + Arrays.toString(fringeFrequency));				
				System.out.println("Angle Array " +Arrays.toString( fringeAngle));
				
				
				DefaultTableModel FringeParametersTablemodel = new DefaultTableModel(
						new Object[Orientations][2],
						FringeColumnHeadings
					);
				
				for (int i =0; i <Orientations;i++)
				{
						FringeParametersTablemodel.setValueAt(fringeFrequency[i], i, 0);
						FringeParametersTablemodel.setValueAt(fringeAngle[i], i, 1);
						
				}
				
				FringeParametersTable.setModel(FringeParametersTablemodel);
				scrollPane.setViewportView(FringeParametersTable);
		        }
		      }.start();
				
			}
		});
		sl_computeParamsPanel.putConstraint(SpringLayout.NORTH, ComputeFringeParamsButton, 10, SpringLayout.NORTH, computeParamsPanel);
		sl_computeParamsPanel.putConstraint(SpringLayout.WEST, ComputeFringeParamsButton, 10, SpringLayout.WEST, computeParamsPanel);
		computeParamsPanel.add(ComputeFringeParamsButton);
		
		
		intformat.setMinimumFractionDigits(2);
		
		JLabel lblOrientations = new JLabel("Orientations");
		sl_computeParamsPanel.putConstraint(SpringLayout.WEST, lblOrientations, 10, SpringLayout.WEST, computeParamsPanel);
		sl_computeParamsPanel.putConstraint(SpringLayout.SOUTH, lblOrientations, 100, SpringLayout.NORTH, computeParamsPanel);
		computeParamsPanel.add(lblOrientations);
		
		JLabel lblPhases = new JLabel("Phases");
		sl_computeParamsPanel.putConstraint(SpringLayout.WEST, lblPhases, 30, SpringLayout.EAST, lblOrientations);
		sl_computeParamsPanel.putConstraint(SpringLayout.SOUTH, lblPhases, 0, SpringLayout.SOUTH, lblOrientations);
		computeParamsPanel.add(lblPhases);
		
		
		final JSpinner PhasesSpinner = new JSpinner();
		PhasesSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Phases = (Integer) PhasesSpinner.getValue();	
				
				/*int rows = 0;
				if (Orientations < Phases)
					{
					rows = Phases;
					}
				else
					{
					rows = Orientations;
					}
				
				FringeParametersTable.setModel(new DefaultTableModel(
						new Object[rows][2],
						FringeColumnHeadings
					));
				scrollPane.setViewportView(FringeParametersTable);*/
			}
		});
		
		
		final JSpinner OrientationsSpinner = new JSpinner();
		OrientationsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Orientations = (Integer) OrientationsSpinner.getValue();
				
				int rows = 0;
				if (Orientations < Phases)
					{
					rows = Phases;
					}
				else
					{
					rows = Orientations;
					}
				
				FringeParametersTable.setModel(new DefaultTableModel(
						new Object[rows][2],
						FringeColumnHeadings
					));
				scrollPane.setViewportView(FringeParametersTable);
			}
		});
		
		sl_computeParamsPanel.putConstraint(SpringLayout.NORTH, OrientationsSpinner, 5, SpringLayout.SOUTH, lblOrientations);
		sl_computeParamsPanel.putConstraint(SpringLayout.WEST, OrientationsSpinner, -40, SpringLayout.EAST, lblOrientations);
		sl_computeParamsPanel.putConstraint(SpringLayout.SOUTH, OrientationsSpinner, 25, SpringLayout.SOUTH, lblOrientations);
		sl_computeParamsPanel.putConstraint(SpringLayout.EAST, OrientationsSpinner, 0, SpringLayout.EAST, lblOrientations);
		OrientationsSpinner.setModel(new SpinnerNumberModel(3, 0, 9, 1));
		computeParamsPanel.add(OrientationsSpinner);
		


		
		
		sl_computeParamsPanel.putConstraint(SpringLayout.NORTH, PhasesSpinner, 5, SpringLayout.SOUTH, lblPhases);
		sl_computeParamsPanel.putConstraint(SpringLayout.WEST, PhasesSpinner, -40, SpringLayout.EAST, lblPhases);
		sl_computeParamsPanel.putConstraint(SpringLayout.SOUTH, PhasesSpinner, 0, SpringLayout.SOUTH, OrientationsSpinner);
		PhasesSpinner.setModel(new SpinnerNumberModel(3, 0, 9, 1));
		sl_computeParamsPanel.putConstraint(SpringLayout.EAST, PhasesSpinner, 0, SpringLayout.EAST, lblPhases);
		computeParamsPanel.add(PhasesSpinner);
		
		final JCheckBox ThreadedComputationCHKBX = new JCheckBox("Threaded Registration (less accurate)");
		sl_computeParamsPanel.putConstraint(SpringLayout.NORTH, ThreadedComputationCHKBX, 5, SpringLayout.SOUTH, ComputeFringeParamsButton);
		sl_computeParamsPanel.putConstraint(SpringLayout.WEST, ThreadedComputationCHKBX, 10, SpringLayout.WEST, computeParamsPanel);
		ThreadedComputationCHKBX.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				Threaded = ThreadedComputationCHKBX.isSelected();
			}
		});
		sl_computeParamsPanel.putConstraint(SpringLayout.NORTH, ComputeDuringProcessChkBx, 1, SpringLayout.SOUTH, ThreadedComputationCHKBX);
		computeParamsPanel.add(ThreadedComputationCHKBX);
		
		JButton Save_Params = new JButton("");
		sl_computeParamsPanel.putConstraint(SpringLayout.EAST, Save_Params, 35, SpringLayout.EAST, ComputeFringeParamsButton);
		
		
		Save_Params.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
                JFileChooser saveFile = new JFileChooser();
                
	            FileNameExtensionFilter csvfilter = new FileNameExtensionFilter("Spreadsheet(*.csv)", "csv",".txt");
	            saveFile.setFileFilter(csvfilter);
	            saveFile.setDialogTitle("Save CSV");
                
                
                int returnVal = saveFile.showSaveDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File fileSave = saveFile.getSelectedFile();
				     System.out.println("Saving: " + fileSave.getName());	
				     
				     
		       
	                        // duplicate full set of settings of CSV file format
	                        CSVWriter writer;
	                        
				try {
					writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(fileSave), "UTF-8"),',');		
					System.out.println("Open");
	            try {
	            	for (int i =0; i < Orientations; i++){
	            		writer.writeNext(new String[] {String.valueOf(FringeParametersTable.getValueAt(i, 0)), 
	            				String.valueOf(FringeParametersTable.getValueAt(i, 1))});	
	            		
	            		System.out.println(String.valueOf(FringeParametersTable.getValueAt(i, 0)) + String.valueOf(FringeParametersTable.getValueAt(i, 1)));
	            		} 
	            	writer.close();
	            	} finally {
	            		// we have to close writer manually
	            		writer.close();
	            		System.out.println("Closing");
	            		}
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					System.out.println("Failed");
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					System.out.println("Failed");
				} catch (IOException e1){
					e1.printStackTrace();
					System.out.println("Failed");
				}

				     
	            } else {
	            	System.out.println("Open command cancelled by user.");
	            }
                /*if saveFile.showSaveDialog(modalToComponent) == JFileChooser.APPROVE_OPTION {
                    File xyz = saveFile.getSelectedFile();
                }*/
				
			}
		});
		Save_Params.setIcon(new ImageIcon(SIM_GUI.class.getResource("/com/sun/java/swing/plaf/windows/icons/FloppyDrive.gif")));
		sl_computeParamsPanel.putConstraint(SpringLayout.NORTH, Save_Params, 0, SpringLayout.NORTH, ComputeFringeParamsButton);
		sl_computeParamsPanel.putConstraint(SpringLayout.WEST, Save_Params, 6, SpringLayout.EAST, ComputeFringeParamsButton);
		sl_computeParamsPanel.putConstraint(SpringLayout.SOUTH, Save_Params, 0, SpringLayout.SOUTH, ComputeFringeParamsButton);
		computeParamsPanel.add(Save_Params);
		

		FringeParametersTable = new JTable();
		FringeParametersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		FringeParametersTable.setCellSelectionEnabled(true);
		FringeParametersTable.setRowSelectionAllowed(false);
		FringeParametersTable.setColumnSelectionAllowed(false);
		FringeParametersTable.setModel(new DefaultTableModel(
			new Object[Orientations][2],
			FringeColumnHeadings
		));
		
		
		InputMap inputMap = FringeParametersTable.getInputMap(JComponent.WHEN_FOCUSED);
		ActionMap actionMap = FringeParametersTable.getActionMap();

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
		actionMap.put("delete", new AbstractAction() {
		    public void actionPerformed(ActionEvent evt) {
		       // Note, you can use getSelectedRows() and/or getSelectedColumns
		       // to get all the rows/columns that have being selected
		       // and simply loop through them using the same method as
		       // described below.
		       // As is, it will only get the lead selection
		       int row = FringeParametersTable.getSelectedRow();
		       int col = FringeParametersTable.getSelectedColumn();
		       if (row >= 0 && col >= 0) {
		           row = FringeParametersTable.convertRowIndexToModel(row);
		           col = FringeParametersTable.convertColumnIndexToModel(col);
		           FringeParametersTable.getModel().setValueAt(null, row, col);
		       }
		    }
		});
		
		
		
		scrollPane = new JScrollPane();
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane, 8, SpringLayout.NORTH, FringeParamsTab);
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane, 0, SpringLayout.EAST, FringeParamsTab);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane, -2, SpringLayout.SOUTH, FringeParamsTab);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane, 0, SpringLayout.EAST, FileInputPanel);
		scrollPane.setViewportView(FringeParametersTable);
		contentPane.add(scrollPane);
		
		progressBar = new JProgressBar();		
		sl_contentPane.putConstraint(SpringLayout.NORTH, progressBar, 5, SpringLayout.NORTH, ProcessButton);
		sl_contentPane.putConstraint(SpringLayout.WEST, progressBar, 5, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, progressBar, -10, SpringLayout.WEST, ProcessButton);
		contentPane.add(progressBar);
		
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		
		scrollPane_1 = new JScrollPane();
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane_1, 10, SpringLayout.SOUTH, progressBar);
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane_1, 0, SpringLayout.WEST, progressBar);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane_1, -5, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane_1, 0, SpringLayout.EAST, SavePreferencesButton);
		contentPane.add(scrollPane_1);
		





		
		textPane = new JTextPane();
		scrollPane_1.setViewportView(textPane);
		sl_contentPane.putConstraint(SpringLayout.NORTH, textPane, 6, SpringLayout.SOUTH, SavePreferencesButton);
		sl_contentPane.putConstraint(SpringLayout.WEST, textPane, 0, SpringLayout.WEST, FileInputPanel);
		sl_contentPane.putConstraint(SpringLayout.EAST, textPane, 0, SpringLayout.EAST, FileInputPanel);
		
	}
	
	  private void redirectSystemStreams() {
		    OutputStream out = new OutputStream() {
		      @Override
		      public void write(final int b) throws IOException {
		        updateTextPane(String.valueOf((char) b));
		      }

		      @Override
		      public void write(byte[] b, int off, int len) throws IOException {
		        updateTextPane(new String(b, off, len));
		      }

		      @Override
		      public void write(byte[] b) throws IOException {
		        write(b, 0, b.length);
		      }
		    };

		    System.setOut(new PrintStream(out, true));
		    System.setErr(new PrintStream(out, true));
		  }
	  
	  
	  private void updateTextPane(final String text) {
		    SwingUtilities.invokeLater(new Runnable() {
		      public void run() {
		        Document doc = textPane.getDocument();
		        try {
		          doc.insertString(doc.getLength(), text, null);
		        } catch (BadLocationException e) {
		          throw new RuntimeException(e);
		        }
		        textPane.setCaretPosition(doc.getLength() - 1);
		        textPane.setCaretPosition(textPane.getDocument().getLength());
		        //scrollPane_1.set
		      }
		    });
		  }
	
	  
	  

	@Override
	public void run(String arg0) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					
			        UIManager.setLookAndFeel(
			                UIManager.getSystemLookAndFeelClassName());
					SIM_GUI frame = new SIM_GUI();
					frame.setVisible(true);
					
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
	}
	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
	
	/*public float[][] TriangleMask(int n,float r0,int x0,int y0)
	{
		float r[][]= new float[n][n];
		double jy = 0;
		double ix = 0;
		
		double half_n = n/2;
		double r_temp;
		
		
		for (int  x= 0; x < n ; x++)
		{
			ix = x - half_n;
			
			for (int y = 0; y < n ; y++)
			{
				jy = y - half_n;
				
				r_temp = Math.hypot(ix, jy);			

				r[x][y] = (float) (r_temp*(-1/r0) + 1);
						
				if (r[x][y] <= 0)
				{
					r[x][y] = 0;
				}
			}
		}
		
		return r;
	}*/
}
