package org.clas.viewer;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import javax.imageio.ImageIO;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.clas.detectors.*;
import org.jlab.detector.decode.CLASDecoder4;
import org.jlab.detector.view.DetectorListener;
import org.jlab.detector.view.DetectorPane2D;
import org.jlab.detector.view.DetectorShape2D;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.TDirectory;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.io.task.DataSourceProcessorPane;
import org.jlab.io.task.IDataEventListener;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.SchemaFactory;
import org.jlab.utils.system.ClasUtilsFile;
import org.jlab.elog.LogEntry; 
import org.jlab.utils.options.OptionParser;

        
/**
 *
 * @author ziegler
 * @author devita
 */

public class EventViewer implements IDataEventListener, DetectorListener, ActionListener, ChangeListener {
    
    List<DetectorPane2D> DetectorPanels     = new ArrayList<DetectorPane2D>();
    JTabbedPane tabbedpane           	    = null;
    JPanel mainPanel 			            = null;
    JMenuBar menuBar                        = null;
    JTextPane clas12Textinfo                = new JTextPane();
    DataSourceProcessorPane processorPane   = null;
    EmbeddedCanvasTabbed CLAS12Canvas       = null;
    private SchemaFactory     schemaFactory = new SchemaFactory();
    
    CLASDecoder4                clasDecoder = new CLASDecoder4(); 
           
    private int canvasUpdateTime   = 2000;
    private int analysisUpdateTime = 100;
    private int runNumber     = 2284;
    private int ccdbRunNumber = 0;
    private int eventNumber = 0;
    
    double PERIOD = 0;
    int     PHASE = 0;
    int    CYCLES = 0;    
    
    public String outPath = null; 
    
    // detector monitors
    public LinkedHashMap<String, DetectorMonitor> monitors = new LinkedHashMap<>();
    
    
    public void initMonitors() {
        monitors.put("BAND",        new BANDmonitor("BAND"));
        monitors.put("BMT",         new BMTmonitor("BMT"));       // 1
        monitors.put("BST",         new BSTmonitor("BST"));        // 2
        monitors.put("CND",         new CNDmonitor("CND"));       // 3
        monitors.put("CTOF",        new CTOFmonitor("CTOF"));      // 4
        monitors.put("DC",          new DCmonitor("DC"));          // 5
        monitors.put("ECAL",        new ECmonitor("ECAL"));        // 6
        monitors.put("FMT",         new FMTmonitor("FMT"));       // 7
        monitors.put("FTCAL",       new FTCALmonitor("FTCAL"));    // 8
        monitors.put("FTHODO",      new FTHODOmonitor("FTHODO"));  // 9
        monitors.put("FTOF",        new FTOFmonitor("FTOF"));      // 10        
        monitors.put("FTTRK",       new FTTRKmonitor("FTTRK"));    // 11
        monitors.put("HTCC",        new HTCCmonitor("HTCC"));      // 12
        monitors.put("LTCC",        new LTCCmonitor("LTCC"));      // 13
        monitors.put("RICH",        new RICHmonitor("RICH"));      // 14
        monitors.put("RTPC",        new RTPCmonitor("RTPC"));     // 15
        monitors.put("RECON",       new RECmonitor("RECON"));      // 16
//        monitors.put("TRK",         new TRKmonitor("TRK"));        // 15
        monitors.put("RF",          new RFmonitor("RF"));          // 17
        monitors.put("HEL",         new HELmonitor("HEL"));        // 18
        monitors.put("FCUP",        new FCUPmonitor("FCUP"));  // 19
        monitors.put("Trigger",     new TRIGGERmonitor("Trigger"));   // 20
        monitors.put("TimeJitter",  new TJITTERmonitor("TimeJitter")); // 21
        monitors.get("FTCAL").setActive(false);
        monitors.get("FTHODO").setActive(false);
        monitors.get("FTTRK").setActive(false);
        monitors.get("RTPC").setActive(false);
        monitors.get("RECON").setActive(false);
    }
                    
    public EventViewer() {    
    	
        String dir = ClasUtilsFile.getResourceDir("CLAS12DIR", "etc/bankdefs/hipo4");
        schemaFactory.initFromDirectory(dir);
        
        outPath = System.getProperty("user.home") + "/CLAS12MON/output";
        System.out.println("OutPath set to :" + outPath);
        
        this.initMonitors();
        
        // create menu bar
        menuBar = new JMenuBar();
        JMenuItem menuItem;
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_A);
        file.getAccessibleContext().setAccessibleDescription("File options");
        menuItem = new JMenuItem("Open histograms file", KeyEvent.VK_O);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Open histograms file");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuItem = new JMenuItem("Save histograms to file", KeyEvent.VK_S);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Save histograms to file");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuItem = new JMenuItem("Print histograms as png", KeyEvent.VK_B);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Print histograms as png");
        menuItem.addActionListener(this);
        file.add(menuItem);
        //menuItem = new JMenuItem("Create histogram PDF", KeyEvent.VK_P);
        //menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        //menuItem.getAccessibleContext().setAccessibleDescription("Create historgram PDF");
        //menuItem.addActionListener(this);
        //file.add(menuItem);
        
        menuBar.add(file);
        JMenu settings = new JMenu("Settings");
        settings.setMnemonic(KeyEvent.VK_A);
        settings.getAccessibleContext().setAccessibleDescription("Choose monitoring parameters");
        menuItem = new JMenuItem("Set GUI update interval", KeyEvent.VK_T);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Set GUI update interval");
        menuItem.addActionListener(this);
        settings.add(menuItem);
        menuItem = new JMenuItem("Set global z-axis log scale", KeyEvent.VK_L);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Set global z-axis log scale");
        menuItem.addActionListener(this);
        settings.add(menuItem);
        menuItem = new JMenuItem("Set global z-axis lin scale", KeyEvent.VK_R);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Set global z-axis lin scale");
        menuItem.addActionListener(this);
        settings.add(menuItem);
        menuItem = new JMenuItem("Set DC occupancy scale max");
        menuItem.getAccessibleContext().setAccessibleDescription("Set DC occupancy scale max");
        menuItem.addActionListener(this);
        settings.add(menuItem);
        menuItem = new JMenuItem("Set run number");
        menuItem.getAccessibleContext().setAccessibleDescription("Set run number");
        menuItem.addActionListener(this);
        settings.add(menuItem);
        menuBar.add(settings);
         
        JMenu upload = new JMenu("Upload");
        upload.setMnemonic(KeyEvent.VK_A);
        upload.getAccessibleContext().setAccessibleDescription("Upload histograms to the Logbook");
        menuItem = new JMenuItem("Upload all histos to the logbook");
        menuItem.getAccessibleContext().setAccessibleDescription("Upload all histos to the logbook");
        menuItem.addActionListener(this);
        upload.add(menuItem);
//        menuItem = new JMenuItem("Upload occupancy histos to the logbook", KeyEvent.VK_U);
//        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.CTRL_MASK));
//        menuItem.getAccessibleContext().setAccessibleDescription("Upload occupancy histos to the logbook");
//        menuItem.addActionListener(this);
        upload.add(menuItem);
        menuBar.add(upload);
        
        JMenu reset = new JMenu("Reset");
        reset.getAccessibleContext().setAccessibleDescription("Reset histograms");
        
        JMenuItem menuItemdefault = new JMenuItem("Default for all");
        menuItemdefault.getAccessibleContext().setAccessibleDescription("Default for all");
        menuItemdefault.addActionListener(this);
        reset.add(menuItemdefault);
        
        JMenuItem menuItemdisable = new JMenuItem("Disable histogram reset");
        menuItemdisable.getAccessibleContext().setAccessibleDescription("Disable histogram reset");
        menuItemdisable.addActionListener(this);
        reset.add(menuItemdisable);
        
        JMenuItem menuItemBMT = new JMenuItem("Reset BMT histograms");
        menuItemBMT.getAccessibleContext().setAccessibleDescription("Reset BMT histograms");
        menuItemBMT.addActionListener(this);
        reset.add(menuItemBMT);
        
        JMenuItem menuItemBST = new JMenuItem("Reset BST histograms");
        menuItemBST.getAccessibleContext().setAccessibleDescription("Reset BST histograms");
        menuItemBST.addActionListener(this);
        reset.add(menuItemBST);
        
        JMenuItem menuItemCND = new JMenuItem("Reset CND histograms");
        menuItemCND.getAccessibleContext().setAccessibleDescription("Reset CND histograms");
        menuItemCND.addActionListener(this);
        reset.add(menuItemCND);
        
        JMenuItem menuItemCTOF = new JMenuItem("Reset CTOF histograms");
        menuItemCTOF.getAccessibleContext().setAccessibleDescription("Reset CTOF histograms");
        menuItemCTOF.addActionListener(this);
        reset.add(menuItemCTOF);
        
        JMenuItem menuItemDC = new JMenuItem("Reset DC histograms");
        menuItemDC.getAccessibleContext().setAccessibleDescription("Reset DC histograms");
        menuItemDC.addActionListener(this);
        reset.add(menuItemDC);
        
        JMenuItem menuItemECAL = new JMenuItem("Reset ECAL histograms");
        menuItemECAL.getAccessibleContext().setAccessibleDescription("Reset ECAL histograms");
        menuItemECAL.addActionListener(this);
        reset.add(menuItemECAL);
        
        JMenuItem menuItemFMT = new JMenuItem("Reset FMT histograms");
        menuItemFMT.getAccessibleContext().setAccessibleDescription("Reset FMT histograms");
        menuItemFMT.addActionListener(this);
        reset.add(menuItemFMT);
        
        JMenuItem menuItemFT = new JMenuItem("Reset FT histograms");
        menuItemFT.getAccessibleContext().setAccessibleDescription("Reset FT histograms");
        menuItemFT.addActionListener(this);
        reset.add(menuItemFT);
        
        JMenuItem menuItemFTOF = new JMenuItem("Reset FTOF histograms");
        menuItemFTOF.getAccessibleContext().setAccessibleDescription("Reset FTOF histograms");
        menuItemFTOF.addActionListener(this);
        reset.add(menuItemFTOF);
  
        JMenuItem menuItemHTTC = new JMenuItem("Reset HTTC histograms");
        menuItemHTTC.getAccessibleContext().setAccessibleDescription("Reset HTTC histograms");
        menuItemHTTC.addActionListener(this);
        reset.add(menuItemHTTC);
        
        JMenuItem menuItemLTTC = new JMenuItem("Reset LTTC histograms");
        menuItemLTTC.getAccessibleContext().setAccessibleDescription("Reset LTTC histograms");
        menuItemLTTC.addActionListener(this);
        reset.add(menuItemLTTC);
        
        JMenuItem menuItemRICH = new JMenuItem("Reset RICH histograms");
        menuItemRICH.getAccessibleContext().setAccessibleDescription("Reset RICH histograms");
        menuItemRICH.addActionListener(this);
        reset.add(menuItemRICH);        
        
        JMenuItem menuItemRTPC = new JMenuItem("Reset RTPC histograms");
        menuItemRTPC.getAccessibleContext().setAccessibleDescription("Reset RTPC histograms");
        menuItemRTPC.addActionListener(this);
        reset.add(menuItemRTPC);
        
        JMenuItem menuItemTRIG = new JMenuItem("Reset TRIGGER histograms");
        menuItemTRIG.getAccessibleContext().setAccessibleDescription("Reset TRIGGER histograms");
        menuItemTRIG.addActionListener(this);
        reset.add(menuItemTRIG);        
        
        menuBar.add(reset);
        
        JMenu trigBits = new JMenu("DetectorBits");
        trigBits.getAccessibleContext().setAccessibleDescription("Select Detectors for Testing Trigger Bits (not yet implmented)");
        
        JCheckBoxMenuItem cb1 = new JCheckBoxMenuItem("EC");    
        cb1.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                  	monitors.get("ECAL").setTestTrigger(true);
                } else {
                 	monitors.get("ECAL").setTestTrigger(false);
                };
            }
        });         
        trigBits.add(cb1); 
        
        JCheckBoxMenuItem cb2 = new JCheckBoxMenuItem("HTCC");
        cb2.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                  	monitors.get("HTCC").setTestTrigger(true);
                } else {
                 	monitors.get("HTCC").setTestTrigger(false);
                };
            }
        });         
        trigBits.add(cb2); 
        
        JCheckBoxMenuItem cb3 = new JCheckBoxMenuItem("BST");
        cb3.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                  	monitors.get("BST").setTestTrigger(true);
                } else {
                 	monitors.get("BST").setTestTrigger(false);
                };
            }
        });         
        trigBits.add(cb3); 
        
        JCheckBoxMenuItem cb4 = new JCheckBoxMenuItem("CTOF");
        cb4.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                  	monitors.get("CTOF").setTestTrigger(true);
                } else {
                 	monitors.get("CTOF").setTestTrigger(false);
                };
            }
        });         
        trigBits.add(cb4); 
        
        JCheckBoxMenuItem cb5 = new JCheckBoxMenuItem("BMT");
        cb5.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                  	monitors.get("BMT").setTestTrigger(true);
                } else {
                 	monitors.get("BMT").setTestTrigger(false);
                };
            }
        });         
        trigBits.add(cb5); 
        
        JCheckBoxMenuItem cb6 = new JCheckBoxMenuItem("FTOF");
        cb6.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                  	monitors.get("FTOF").setTestTrigger(true);
                } else {
                 	monitors.get("FTOF").setTestTrigger(false);
                };
            }
        });         
        trigBits.add(cb6); 
               
        menuBar.add(trigBits);
        
        //RGA
        String TriggerDefRGAFall[] = { "Electron",
		        "Electron S1","Electron S2","Electron S3","Electron S4","Electron S5","Electron S6",
		        "ElectronOR noDC>300","PCALxECAL>10","","","","","","","","","","",
		        "FTOFxPCALxECAL(1-4)","FTOFxPCALxECAL(2-5)","FTOFxPCALxECAL(3-6)","","",
		        "FTxHDxFTOFxPCALxCTOF",
		        "FTxHDx(FTOFxPCAL)^2","FTxHD>100","FT>100","","","",
		        "1K Pulser"};
        
        String TriggerDefRGASpring[] = { "Electron",
		        "Electron S1","Electron S2","Electron S3","Electron S4","Electron S5","Electron S6",
		        "HTCC(>1pe)","HTCCxPCAL(>300MeV)","FTOFxPCAL^3","FTOFxPCALxECAL^3",
		        "FTOFxPCALxCTOF","FTOFxPCALxCND","FTOFxPCALxCNDxCTOF","FTOFxPCAL^2",
		        "FTOFxPCALxECAL^2","FTOFxPCAL(1-4)","FTOFxPCAL(2-5)","FTOFxPCAL(3-6)",
		        "FTOFxPCALxECAL(1-4)","FTOFxPCALxECAL(2-5)","FTOFxPCALxECAL(3-6)",
		        "FTxFTOFxPCALxCTOF","FTxFTOFxPCALxCND","FTxFTOFxPCALxCTOFxCND",
		        "FTx(FTOFxPCAL)^2","FTx(FTOFxPCAL)^3","FT(>300)xHODO","FT(>500)xHODO","FT>300","FT>500",
		        "1K Pulser"};  
           
      //RGB
        String TriggerDefRGB[] = { "Electron OR",
		        "Electron S1","Electron S2","Electron S3","Electron S4","Electron S5","Electron S6",
		        "FTOFxPCALxECALxDC(1-4)","FTOFxPCALxECALxDC(2-5)","FTOFxPCALxECALxDC(3-6)",
		        "Electron OR no DC","","","","","","","","","","","","","","","","","","","","",
		        "1K Pulser"};   
        
        String TriggerDef[] = { "Electron OR",
		        "e Sector 1","e Sector 2","e Sector 3","e Sector 4","e Sector 5","e Sector 6",
		        "Muons S1+ S4-","Muons S2+ S5-","Muons S3+ S6-",
		        "Muons S4+ S1-","Muons S5+ S2-","Muons S6+ S3-","","","","","","","","","","","","","","","","","","",
		        "1K Pulser"};    
        
        JMenu trigBitsBeam = new JMenu("TriggerBits");
        trigBitsBeam.getAccessibleContext().setAccessibleDescription("Test Trigger Bits");
        
        for (int i=0; i<32; i++) {
        	
            JCheckBoxMenuItem bb = new JCheckBoxMenuItem(TriggerDef[i]);  
            final Integer bit = new Integer(i);
            bb.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                	
                    if(e.getStateChange() == ItemEvent.SELECTED) {
                        for(String key : monitors.keySet()) {
                      	monitors.get(key).setTriggerMask(bit);
                        }
                    } else {
                        for(String key : monitors.keySet()) {
                      	monitors.get(key).clearTriggerMask(bit);
                        }
                    };
                }
            });         
            trigBitsBeam.add(bb); 
        	        	
        }

        menuBar.add(trigBitsBeam);
        

        // create main panel
        mainPanel = new JPanel();	
        mainPanel.setLayout(new BorderLayout());
        
      	tabbedpane 	= new JTabbedPane();

        processorPane = new DataSourceProcessorPane();
        processorPane.setUpdateRate(analysisUpdateTime);

        mainPanel.add(tabbedpane);
        mainPanel.add(processorPane,BorderLayout.PAGE_END);
        
    
        GStyle.getAxisAttributesX().setTitleFontSize(18);
        GStyle.getAxisAttributesX().setLabelFontSize(14);
        GStyle.getAxisAttributesY().setTitleFontSize(18);
        GStyle.getAxisAttributesY().setLabelFontSize(14);
        
        CLAS12Canvas    = new EmbeddedCanvasTabbed("FD");
        CLAS12Canvas.getCanvas("FD").divide(3,3);
        CLAS12Canvas.getCanvas("FD").setGridX(false);
        CLAS12Canvas.getCanvas("FD").setGridY(false); 
        CLAS12Canvas.addCanvas("CD");
        CLAS12Canvas.getCanvas("CD").divide(2,2);
        CLAS12Canvas.getCanvas("CD").setGridX(false);
        CLAS12Canvas.getCanvas("CD").setGridY(false);
        CLAS12Canvas.addCanvas("FT");
        CLAS12Canvas.getCanvas("FT").divide(1,3);
        CLAS12Canvas.getCanvas("FT").setGridX(false);
        CLAS12Canvas.getCanvas("FT").setGridY(false);
        CLAS12Canvas.addCanvas("RF/HEL/JITTER/TRIGGER");
        CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER").divide(2,2);
        CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER").setGridX(false);
        CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER").setGridY(false);

        
        JPanel    CLAS12View = new JPanel(new BorderLayout());
        JSplitPane splitPanel = new JSplitPane();
        splitPanel.setLeftComponent(CLAS12View);
        splitPanel.setRightComponent(CLAS12Canvas);
        JTextPane clas12Text   = new JTextPane();
        clas12Text.setText("CLAS12\n monitoring plots\n V5.0\n");
        clas12Text.setEditable(false);       
        this.clas12Textinfo.setEditable(false);
        this.clas12Textinfo.setFont(new Font("Avenir",Font.PLAIN,16));
        this.clas12Textinfo.setBackground(CLAS12View.getBackground());
        StyledDocument styledDoc = clas12Text.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        styledDoc.setParagraphAttributes(0, styledDoc.getLength(), center, false);
        clas12Text.setBackground(CLAS12View.getBackground());
        clas12Text.setFont(new Font("Avenir",Font.PLAIN,20));
        JLabel clas12Design = this.getImage("https://www.jlab.org/Hall-B/clas12-web/sidebar/clas12-design.jpg",0.08);
        JLabel clas12Logo   = this.getImage("https://www.jlab.org/Hall-B/pubs-web/logo/CLAS-frame-low.jpg", 0.3);
//        CLAS12View.add(clas12Name,BorderLayout.PAGE_START);
        CLAS12View.add(clas12Textinfo,BorderLayout.BEFORE_FIRST_LINE );
        CLAS12View.add(clas12Design);
        CLAS12View.add(clas12Text,BorderLayout.PAGE_END);

        tabbedpane.add(splitPanel,"Summary");
        tabbedpane.addChangeListener(this);
        
        this.setCanvasUpdate(canvasUpdateTime);
        this.plotSummaries();

        this.processorPane.addEventListener(this);
        
        
    }
      
    public void init() {
        for(String key : monitors.keySet()) {
            if(monitors.get(key).isActive()) this.tabbedpane.add(this.monitors.get(key).getDetectorPanel(), monitors.get(key).getDetectorName()); //don't show FMT tab
            monitors.get(key).getDetectorView().getView().addDetectorListener(this);                       
        }
        
        this.tabbedpane.add(new Contact(),"Contacts");        
        this.tabbedpane.add(new Acronyms(),"Acronyms");
    }
    
    
    public void actionPerformed(ActionEvent e) {
        System.out.println(e.getActionCommand());
        if(e.getActionCommand()=="Set GUI update interval") {
            this.chooseUpdateInterval();
        }
        if(e.getActionCommand()=="Set global z-axis log scale") {
        	   for(String key : monitors.keySet()) {this.monitors.get(key).setLogZ(true);this.monitors.get(key).plotHistos();}
        }
        if(e.getActionCommand()=="Set global z-axis lin scale") {
           for(String key : monitors.keySet()) {this.monitors.get(key).setLogZ(false);this.monitors.get(key).plotHistos();}
        }
        if(e.getActionCommand()=="Set DC occupancy scale max") {
           setDCRange(e.getActionCommand());
        }
        if(e.getActionCommand()=="Set run number") {
           setRunNumber(e.getActionCommand());
        }

        if(e.getActionCommand()=="Open histograms file") {
            String fileName = null;
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            File workingDirectory = new File(System.getProperty("user.dir"));  
            fc.setCurrentDirectory(workingDirectory);
            int option = fc.showOpenDialog(null);
            if (option == JFileChooser.APPROVE_OPTION) {
                fileName = fc.getSelectedFile().getAbsolutePath();            
            }
            if(fileName != null) this.loadHistosFromFile(fileName);
        }        
        if(e.getActionCommand()=="Print histograms as png") {
            this.printHistosToFile();
        }
        if(e.getActionCommand()=="Save histograms to file") {
            DateFormat df = new SimpleDateFormat("MM-dd-yyyy_hh.mm.ss_aa");
            String fileName = "CLAS12Mon_run_" + this.runNumber + "_" + df.format(new Date()) + ".hipo";
            JFileChooser fc = new JFileChooser();
            File workingDirectory = new File(System.getProperty("user.dir"));   
            fc.setCurrentDirectory(workingDirectory);
            File file = new File(fileName);
            fc.setSelectedFile(file);
            int returnValue = fc.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
               fileName = fc.getSelectedFile().getAbsolutePath();            
            }
            this.saveHistosToFile(fileName);
        }
        
        if(e.getActionCommand()=="Upload all histos to the logbook") {   
            
            DateFormat df = new SimpleDateFormat("MM-dd-yyyy_hh.mm.ss_aa");
            String tstamp = df.format(new Date());
            String data = outPath + "/clas12mon_" + this.runNumber + "_" + tstamp;        
            File theDir = new File(data);
            // if the directory does not exist, create it
            if (!theDir.exists()) {
                boolean result = false;
                try{theDir.mkdir();result = true;} 
                catch(SecurityException se){}        
                if(result){ System.out.println("Created directory: " + data);}
            }
            
            String fileName = data + "/clas12mon_histos_" + this.runNumber + "_" + tstamp + ".hipo"; 
            try{
                this.saveHistosToFile(fileName);
            }
            catch(IndexOutOfBoundsException exc){
                exc.printStackTrace(); 
                System.out.println( exc.getMessage());
            }
            
            String fileName1 = data + "/summary_FD_"+tstamp+".png";
            System.out.println(fileName1);
            CLAS12Canvas.getCanvas("FD").save(fileName1);
            String fileName2 = data + "/summary_CD_"+tstamp+".png";
            System.out.println(fileName2);
            CLAS12Canvas.getCanvas("CD").save(fileName2);
            String fileName3 = data + "/summary_FT_"+tstamp+".png";
            System.out.println(fileName3);
            CLAS12Canvas.getCanvas("FT").save(fileName3);
            String fileName4 = data + "/summary_RHJT_"+tstamp+".png";
            System.out.println(fileName4);
            CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER").save(fileName4);
            
            LogEntry entry = new LogEntry("All online monitoring histograms for run number " + this.runNumber, "HBLOG");
            
            System.out.println("Starting to upload all monitoring plots");
            
            try{
                entry.addAttachment(data + "/summary_FD_" + tstamp + ".png", "Summary plots for the forward detector");
                entry.addAttachment(data + "/summary_CD_" + tstamp + ".png", "Summary plots for the central detector");
                entry.addAttachment(data + "/summary_FT_" + tstamp + ".png", "Summary plots for the forward tagger");
                entry.addAttachment(data + "/summary_RHJT_" + tstamp + ".png", "Summary plots RF/HEL/JITTER/TRIGGER");
                System.out.println("Summary plots uploaded");
                for(String key : monitors.keySet()) {
                    if (monitors.get(key).isActive()) {
                        LinkedHashMap<String, String> prints = this.monitors.get(key).printCanvas(data, tstamp);
                        for(String print: prints.keySet()) {
                            entry.addAttachment(print, key + " " + prints.get(print));
                        }
                        System.out.println(this.monitors.get(key).getDetectorName() + " plots uploaded");
                    }
                }
            
              long lognumber = entry.submitNow();
              System.out.println("Successfully submitted log entry number: " + lognumber); 
            } catch(Exception exc){
                exc.printStackTrace(); 
                System.out.println( exc.getMessage());
            }
              
        }
                 
         
         if (e.getActionCommand()=="Default for all"){
            for(String key : monitors.keySet()) {
                this.monitors.get(key).eventResetTime_current = this.monitors.get(key).eventResetTime_default;
            }
        }
         
         if (e.getActionCommand()=="Disable histogram reset"){
            for(String key : monitors.keySet()) {
                this.monitors.get(key).eventResetTime_current = 0;
            }
        }
        
        if ( e.getActionCommand().substring(0, 5).equals("Reset")){
            resetHistograms(e.getActionCommand());
        }
        
        
    }

    public void chooseUpdateInterval() {
        String s = (String)JOptionPane.showInputDialog(
                    null,
                    "GUI update interval (ms)",
                    " ",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "1000");
        if(s!=null){
            int time = 1000;
            try { 
                time= Integer.parseInt(s);
            } catch(NumberFormatException e) { 
                JOptionPane.showMessageDialog(null, "Value must be a positive integer!");
            }
            if(time>0) {
                this.setCanvasUpdate(time);
            }
            else {
                JOptionPane.showMessageDialog(null, "Value must be a positive integer!");
            }
        }
    }
        
    private JLabel getImage(String path,double scale) {
        JLabel label = null;
        Image image = null;
        try {
            URL url = new URL(path);
            image = ImageIO.read(url);
        } catch (IOException e) {
        	e.printStackTrace();
                System.out.println("Picture upload from " + path + " failed");
        }
        ImageIcon imageIcon = new ImageIcon(image);
        double width  = imageIcon.getIconWidth()*scale;
        double height = imageIcon.getIconHeight()*scale;
        imageIcon = new ImageIcon(image.getScaledInstance((int) width,(int) height, Image.SCALE_SMOOTH));
        label = new JLabel(imageIcon);
        return label;
    }
    
    public JPanel  getPanel(){
        return mainPanel;
    }
    
    public long getTriggerWord(DataEvent event) {    	
 	    DataBank bank = event.getBank("RUN::config");	        
        return bank.getLong("trigger", 0);
    } 
    
    public long getTriggerPhase(DataEvent event) {    	
 	    DataBank bank = event.getBank("RUN::config");	        
        long timestamp = bank.getLong("timestamp",0);    
        int phase_offset = 3;
        return ((timestamp%6)+phase_offset)%6; // TI derived phase correction due to TDC and FADC clock differences 
    }  
    
    private int getRunNumber(DataEvent event) {
        int rNum = this.runNumber;
        DataBank bank = event.getBank("RUN::config");
        if(bank!=null) {
            rNum      = bank.getInt("run", 0);
        }
        return rNum;
    }
    
    private int getEventNumber(DataEvent event) {
        DataBank bank = event.getBank("RUN::config");
        return (bank!=null) ? bank.getInt("event", 0): this.eventNumber;
    }
    
    private void copyHitList(String k, String mon1, String mon2) {
    	if (k!=mon1) return;
    	monitors.get(mon1).ttdcs = monitors.get(mon2).ttdcs;
    	monitors.get(mon1).ftdcs = monitors.get(mon2).ftdcs;
    	monitors.get(mon1).fadcs = monitors.get(mon2).fadcs;
   	monitors.get(mon1).fapmt = monitors.get(mon2).fapmt;
    	monitors.get(mon1).ftpmt = monitors.get(mon2).ftpmt;
    }
    
    @Override
    public void dataEventAction(DataEvent event) {
    	
    	DataEvent hipo = null;
   	
	if(event!=null ){
            if(event instanceof EvioDataEvent){
                Event    dump = clasDecoder.getDataEvent(event);    
                Bank   header = clasDecoder.createHeaderBank(this.ccdbRunNumber, getEventNumber(event), (float) 0, (float) 0);
                Bank  trigger = clasDecoder.createTriggerBank();
                if(header!=null)  dump.write(header);
                if(trigger!=null) dump.write(trigger);
                hipo = new HipoDataEvent(dump,schemaFactory);
            }   
            else {            	
            	hipo = event; 
            }
        
            if(this.runNumber != this.getRunNumber(hipo)) {
                this.runNumber = this.getRunNumber(hipo);
                System.out.println("Setting run number to: " +this.runNumber);
                resetEventListener();
                this.clas12Textinfo.setText("\nrun number: "+this.runNumber + "\n");
            }     
            
            for(String key : monitors.keySet()) {
                if(this.monitors.get(key).isActive()) {
                    this.monitors.get(key).setTriggerPhase(getTriggerPhase(hipo));
                    this.monitors.get(key).setTriggerWord(getTriggerWord(hipo));
                    copyHitList(key,"Trigger","FTOF");
                    this.monitors.get(key).dataEventAction(hipo);
                }
            }      
        }
    }

    public void loadHistosFromFile(String fileName) {
        // TXT table summary FILE //
        System.out.println("Opening file: " + fileName);
        TDirectory dir = new TDirectory();
        dir.readFile(fileName);
        System.out.println(dir.getDirectoryList());
        dir.cd();
        dir.pwd();
        
        for(String key : monitors.keySet()) {
            this.monitors.get(key).readDataGroup(dir);
        }
        this.plotSummaries();
    }

    public void plotSummaries() {
        
        /////////////////////////////////////////////////
        /// FD:
        
        // DC
        this.CLAS12Canvas.getCanvas("FD").cd(0);
        if(this.monitors.get("DC").getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("DC").getDetectorSummary().getH1F("summary")); 
        // HTTC
        this.CLAS12Canvas.getCanvas("FD").cd(1);
        if(this.monitors.get("HTCC").getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("HTCC").getDetectorSummary().getH1F("summary"));
        // LTTC
        this.CLAS12Canvas.getCanvas("FD").cd(2);
        if(this.monitors.get("LTCC").getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("LTCC").getDetectorSummary().getH1F("summary"));
        // RICH
        this.CLAS12Canvas.getCanvas("FD").cd(3);
        this.CLAS12Canvas.getCanvas("FD").getPad(3).getAxisZ().setLog(true);
        if(this.monitors.get("RICH").getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("RICH").getDetectorSummary().getH2F("summary"));
        
        // ECAL 
        this.CLAS12Canvas.getCanvas("FD").cd(4); this.CLAS12Canvas.getCanvas("FD").getPad(4).setAxisRange(0.5,6.5,0.5,1.5);
        if(this.monitors.get("ECAL").getDetectorSummary()!=null) { 
        	this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("ECAL").getDetectorSummary().getGraph("PCALu"));
        	this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("ECAL").getDetectorSummary().getGraph("PCALv"),"same");
        	this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("ECAL").getDetectorSummary().getGraph("PCALw"),"same");
        	this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("ECAL").getDetectorSummary().getF1D("p0"),"same");
        }
        this.CLAS12Canvas.getCanvas("FD").cd(5); this.CLAS12Canvas.getCanvas("FD").getPad(5).setAxisRange(0.5,6.5,0.5,1.5);
        if(this.monitors.get("ECAL").getDetectorSummary()!=null) {
        	this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("ECAL").getDetectorSummary().getGraph("ECinu"));
        	this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("ECAL").getDetectorSummary().getGraph("ECinv"),"same");
        	this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("ECAL").getDetectorSummary().getGraph("ECinw"),"same");
        	this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("ECAL").getDetectorSummary().getGraph("ECoutu"),"same");
        	this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("ECAL").getDetectorSummary().getGraph("ECoutv"),"same");
        	this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("ECAL").getDetectorSummary().getGraph("ECoutw"),"same");
        	this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("ECAL").getDetectorSummary().getF1D("p0"),"same");
        }
        
        // FMT:
//        this.CLAS12Canvas.getCanvas("FD").cd(6);
//        this.CLAS12Canvas.getCanvas("FD").getPad(6).getAxisZ().setLog(true);
//        if(this.monitors.get("ECAL").getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("ECAL").getDetectorSummary().getH2F("summary"));
       
        // FTOF:
        this.CLAS12Canvas.getCanvas("FD").cd(6);
        this.CLAS12Canvas.getCanvas("FD").getPad(6).getAxisZ().setLog(true);
        if(this.monitors.get("FTOF").getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("FTOF").getDetectorSummary().getH1F("sum_p1"));
        this.CLAS12Canvas.getCanvas("FD").cd(7);
        this.CLAS12Canvas.getCanvas("FD").getPad(7).getAxisZ().setLog(true);
        if(this.monitors.get("FTOF").getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("FTOF").getDetectorSummary().getH1F("sum_p2"));
        this.CLAS12Canvas.getCanvas("FD").cd(8);
        this.CLAS12Canvas.getCanvas("FD").getPad(8).getAxisZ().setLog(true);
        if(this.monitors.get("FTOF").getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("FTOF").getDetectorSummary().getH1F("sum_p3"));
        
        //////////////////////////////////////////////////
        ///  CD:
        
        // CND
        this.CLAS12Canvas.getCanvas("CD").cd(0);
        if(this.monitors.get("CND").getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("CD").draw(this.monitors.get("CND").getDetectorSummary().getH1F("summary"));
        // CTOF
        this.CLAS12Canvas.getCanvas("CD").cd(1);
        if(this.monitors.get("CTOF").getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("CD").draw(this.monitors.get("CTOF").getDetectorSummary().getH1F("summary"));
//        // RTPC
//        this.CLAS12Canvas.getCanvas("CD").cd(2);
//        if(this.monitors.get("RTPC").getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("CD").draw(this.monitors.get("RTPC").getDetectorSummary().getH1F("summary"));
//        // FMT
//        this.CLAS12Canvas.getCanvas("CD").cd(3);
//        if(this.monitors.get("FMT").getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("CD").draw(this.monitors.get("FMT").getDetectorSummary().getH1F("summary"));
        // BMT
        this.CLAS12Canvas.getCanvas("CD").cd(2);
        this.CLAS12Canvas.getCanvas("CD").getPad(2).getAxisZ().setLog(true);
        if(this.monitors.get("BMT").getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("CD").draw(this.monitors.get("BMT").getDetectorSummary().getH1F("summary"));
        // BST
        this.CLAS12Canvas.getCanvas("CD").cd(3);
        this.CLAS12Canvas.getCanvas("CD").getPad(3).getAxisZ().setLog(true);
        if(this.monitors.get("BST").getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("CD").draw(this.monitors.get("BST").getDetectorSummary().getH2F("summary"));
        
        
        
        ///////////////////////////////////////////////////
        // FT:
        
        // FTCAL
        this.CLAS12Canvas.getCanvas("FT").cd(0);
        if(this.monitors.get("FTCAL").getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("FT").draw(this.monitors.get("FTCAL").getDetectorSummary().getH1F("summary"));
        // FTHODO
        this.CLAS12Canvas.getCanvas("FT").cd(1);
        if(this.monitors.get("FTHODO").getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("FT").draw(this.monitors.get("FTHODO").getDetectorSummary().getH1F("summary"));
        // FTTRK
        this.CLAS12Canvas.getCanvas("FT").cd(2);
        if(this.monitors.get("FTTRK").getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("FT").draw(this.monitors.get("FTTRK").getDetectorSummary().getH1F("summary"));
        
        ////////////////////////////////////////////////////
      
        ///////////////////////////////////////////////////
        // RF/HEL/JITTER/TRIGGER:
        
        // RF
        this.CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER").cd(0);
        if(this.monitors.get("RF").getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER").draw(this.monitors.get("RF").getDetectorSummary().getH1F("summary"));
        // HEL
        this.CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER").cd(1);
        if(this.monitors.get("HEL").getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER").draw(this.monitors.get("HEL").getDetectorSummary().getH1F("summary"));
        // FCUP
        this.CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER").cd(2);
        if(this.monitors.get("TimeJitter").getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER").draw(this.monitors.get("TimeJitter").getDetectorSummary().getH1F("summary"));
        // TRIGGER
        this.CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER").cd(3);
        this.CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER").getPad(3).getAxisY().setLog(true);
        if(this.monitors.get("Trigger").getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER").draw(this.monitors.get("Trigger").getDetectorSummary().getH1F("summary"));
         
        ////////////////////////////////////////////////////
      
        
    }
    
    public void printHistosToFile() {
        DateFormat df = new SimpleDateFormat("MM-dd-yyyy_hh.mm.ss_aa");
        String tstamp = df.format(new Date());
        String data = outPath + "/clas12mon_" + this.runNumber + "_" + tstamp;        
        File theDir = new File(data);
        // if the directory does not exist, create it
        if (!theDir.exists()) {
            boolean result = false;
            try{
                theDir.mkdir();
                result = true;
            } 
            catch(SecurityException se){
                //handle it
            }        
            if(result) {    
            System.out.println("Created directory: " + data);
            }
        }
        
        String fileName1 = data + "/summary_FD_"+tstamp+".png";
        System.out.println(fileName1);
        CLAS12Canvas.getCanvas("FD").save(fileName1);
        String fileName2 = data + "/summary_CD_"+tstamp+".png";
        System.out.println(fileName2);
        CLAS12Canvas.getCanvas("CD").save(fileName2);
        String fileName3 = data + "/summary_FT_"+tstamp+".png";
        System.out.println(fileName3);
        CLAS12Canvas.getCanvas("FT").save(fileName3);
        String fileName4 = data + "/summary_RHJT_"+tstamp+".png";
        System.out.println(fileName4);
        CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER").save(fileName4);
        
        for(String key : monitors.keySet()) {
            if(this.monitors.get(key).isActive()) this.monitors.get(key).printCanvas(data,tstamp);
        }
        
        System.out.println("Histogram pngs succesfully saved in: " + data);
    }
       

    @Override
    public void processShape(DetectorShape2D shape) {
        System.out.println("SHAPE SELECTED = " + shape.getDescriptor());
    }
    
    @Override
    public void resetEventListener() {
        for(String key : monitors.keySet()) {
            this.monitors.get(key).resetEventListener();
            this.monitors.get(key).timerUpdate();
        }      
        this.plotSummaries();
    }
    
    public void saveHistosToFile(String fileName) {
        // TXT table summary FILE //
        TDirectory dir = new TDirectory();
        for(String key : monitors.keySet()) {
            this.monitors.get(key).writeDataGroup(dir);
        }
        System.out.println("Saving histograms to file " + fileName);
        dir.writeFile(fileName);
    }
        
    public void setCanvasUpdate(int time) {
        System.out.println("Setting " + time + " ms update interval");
        this.canvasUpdateTime = time;
        this.CLAS12Canvas.getCanvas("FD").initTimer(time);
        this.CLAS12Canvas.getCanvas("FD").update();
        this.CLAS12Canvas.getCanvas("CD").initTimer(time);
        this.CLAS12Canvas.getCanvas("CD").update();
        this.CLAS12Canvas.getCanvas("FT").initTimer(time);
        this.CLAS12Canvas.getCanvas("FT").update();
        this.CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER").initTimer(time);
        this.CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER").update();
        for(String key : monitors.keySet()) {
            this.monitors.get(key).setCanvasUpdate(time);
        }
    }

    public void stateChanged(ChangeEvent e) {
        this.timerUpdate();
    }
    
    @Override
    public void timerUpdate() {
//        System.out.println("Time to update ...");
        for(String key : monitors.keySet()) {
            this.monitors.get(key).timerUpdate();
        }
        this.plotSummaries();
   }

    public static void main(String[] args){
        
        OptionParser parser = new OptionParser();
        
        parser.addOption("-geometry", "1600x1000", "Select window size, e.g. 1600x1200");
        parser.addOption("-tabs",     "",          "Select active tabs, e.g. Summary:BST:FTOF");
        parser.parse(args);

        int xSize = 1600;
        int ySize = 1000;        
        String geometry = parser.getOption("-geometry").stringValue();
        if(!geometry.isEmpty()) {
            if(geometry.split("x").length==2){
                xSize = Integer.parseInt(geometry.split("x")[0]);
                ySize = Integer.parseInt(geometry.split("x")[1]);
            }
        }
        System.out.println("Setting windows size to " + xSize + "x" + ySize);
        
        EventViewer viewer = new EventViewer();
        String tabs     = parser.getOption("-tabs").stringValue();
        if(!tabs.isEmpty()) {           
            if(tabs.split(":").length>0) {
                for(String tab : viewer.monitors.keySet()) viewer.monitors.get(tab).setActive(false);
                for(String tab: tabs.split(":")) {
                    if(viewer.monitors.containsKey(tab.trim())) viewer.monitors.get(tab.trim()).setActive(true);
                    System.out.println(tab + " monitor set to active");
                }
            }
        }
        else {
            System.out.println("All monitors set to active");
        }       
        viewer.init();
        
        JFrame frame = new JFrame("CLAS12Mon");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.add(viewer.getPanel());
        frame.add(viewer.mainPanel);
        frame.setJMenuBar(viewer.menuBar);
        frame.setSize(xSize, ySize);
        frame.setVisible(true);
    }
    
    
    private void setDCRange(String actionCommand) {
    
        System.out.println("Set normalized DC occuopancy range maximum");
        String  DC_scale = (String) JOptionPane.showInputDialog(null, "Set normalized DC occuopancy range maximum to ", " ", JOptionPane.PLAIN_MESSAGE, null, null, "15");
        
        if (DC_scale != null) { 
            double DC_scale_max= 0;
            try {DC_scale_max = Double.parseDouble(DC_scale);} 
            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
            if (DC_scale_max > 0){ this.monitors.get("DC").max_occ = DC_scale_max;} 
            else {JOptionPane.showMessageDialog(null, "Value must be a positive number!");}   
        }
        
    }
    
    private void setRunNumber(String actionCommand) {
    
        System.out.println("Set run number for CCDB access");
        String  RUN_number = (String) JOptionPane.showInputDialog(null, "Set run number to ", " ", JOptionPane.PLAIN_MESSAGE, null, null, "2284");
        
        if (RUN_number != null) { 
            int cur_runNumber= this.runNumber;
            try {
                cur_runNumber = Integer.parseInt(RUN_number);
            } 
            catch (
                NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");
            }
            if (cur_runNumber > 0){ 
                this.ccdbRunNumber = cur_runNumber;               
                clasDecoder.setRunNumber(cur_runNumber,true);
            } 
            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
        }
        
    }

    private void resetHistograms(String actionCommand) {
       
        if (actionCommand=="Reset BMT histograms"){
            System.out.println("Reset BMT histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset BMT plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors.get("BMT").eventResetTime_default;
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors.get("BMT").eventResetTime_current = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors.get("BMT").eventResetTime_current = 0;
                    }	
         }
        
        if (actionCommand=="Reset BST histograms"){
            System.out.println("Reset BST histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset BST plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors.get("BST").eventResetTime_default;
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors.get("BST").eventResetTime_current = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors.get("BST").eventResetTime_current = 0;
                    }	
         }
        
        if (actionCommand=="Reset CND histograms"){
            System.out.println("Reset CND histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset CND plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors.get("CND").eventResetTime_default;
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors.get("CND").eventResetTime_current = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors.get("CND").eventResetTime_current = 0;
                    }	
         }
        
        if (actionCommand=="Reset CTOF histograms"){
            System.out.println("Reset CTOF histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset CTOF plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors.get("CTOF").eventResetTime_default;
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors.get("CTOF").eventResetTime_current = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors.get("CTOF").eventResetTime_current = 0;
                    }	
         }
        
        if (actionCommand=="Reset DC histograms"){
            System.out.println("Reset DC histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset DC plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors.get("DC").eventResetTime_default;
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors.get("DC").eventResetTime_current = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors.get("DC").eventResetTime_current = 0;
                    }	
         }
        
        if (actionCommand=="Reset ECAL histograms"){
            System.out.println("Reset ECAL histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset ECAL plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors.get("ECAL").eventResetTime_default;
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors.get("ECAL").eventResetTime_current = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors.get("ECAL").eventResetTime_current = 0;
                    }	
         }
        
        if (actionCommand=="Reset FMT histograms"){
            System.out.println("Reset FMT histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset FMT plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors.get("FMT").eventResetTime_default;
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors.get("FMT").eventResetTime_current = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors.get("FMT").eventResetTime_current = 0;
                    }	
         }
        
        if (actionCommand=="Reset FT histograms"){
            System.out.println("Reset FT histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset FT plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors.get("FTCAL").eventResetTime_default;
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {
                                this.monitors.get("FTCAL").eventResetTime_current = time;
                                this.monitors.get("FTHODO").eventResetTime_current = time;
                                this.monitors.get("FTTRK").eventResetTime_current = time;
                            } 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 		     this.monitors.get("FTCAL").eventResetTime_current = 0;
                        this.monitors.get("FTHODO").eventResetTime_current = 0;
                        this.monitors.get("FTTRK").eventResetTime_current = 0;
                    }	
         }
        
        if (actionCommand=="Reset FTOF histograms"){
            System.out.println("Reset FTOF histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset FTOF plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors.get("FTOF").eventResetTime_default;
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors.get("FTOF").eventResetTime_current = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors.get("FTOF").eventResetTime_current = 0;
                    }	
         }
        
        if (actionCommand=="Reset HTTC histograms"){
            System.out.println("Reset HTTC histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset HTTC plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors.get("HTCC").eventResetTime_default;
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors.get("HTCC").eventResetTime_current = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors.get("HTCC").eventResetTime_current = 0;
                    }	
         }
        
        if (actionCommand=="Reset LTTC histograms"){
            System.out.println("Reset LTTC histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset LTTC plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors.get("LTCC").eventResetTime_default;
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors.get("LTCC").eventResetTime_current = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors.get("LTCC").eventResetTime_current = 0;
                    }	
         }
        
        if (actionCommand=="Reset RICH histograms"){
            System.out.println("Reset RICH histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset RICH plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors.get("RICH").eventResetTime_default;
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors.get("RICH").eventResetTime_current = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors.get("RICH").eventResetTime_current = 0;
                    }	
         }
        
        if (actionCommand=="Reset RTPC histograms"){
            System.out.println("Reset RTPC histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset RTPC plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors.get("RTPC").eventResetTime_default;
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors.get("RTPC").eventResetTime_current = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors.get("RTPC").eventResetTime_current = 0;
                    }	
         }

        if (actionCommand=="Reset RECON histograms"){
            System.out.println("Reset RECON histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset RECON plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors.get("RF").eventResetTime_default;
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors.get("RF").eventResetTime_current = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors.get("RF").eventResetTime_current = 0;
                    }	
         }
        
        if (actionCommand=="Reset RF histograms"){
            System.out.println("Reset RF histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset RF plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors.get("HEL").eventResetTime_default;
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors.get("HEL").eventResetTime_current = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors.get("HEL").eventResetTime_current = 0;
                    }	
         }
        
        if (actionCommand=="Reset HEL histograms"){
            System.out.println("Reset HEL histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset HEL plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors.get("HEL").eventResetTime_default;
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors.get("HEL").eventResetTime_current = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors.get("HEL").eventResetTime_current = 0;
                    }	
         }
        
        if (actionCommand=="Reset Faraday Cup histograms"){
            System.out.println("Reset Faraday Cup histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset Faraday Cup plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors.get("FCUP").eventResetTime_default;
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors.get("FCUP").eventResetTime_current = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors.get("FCUP").eventResetTime_current = 0;
                    }	
         }
        
        if (actionCommand=="Reset TRIGGER histograms"){
            System.out.println("Reset TRIGGER histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset Trigger plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors.get("Trigger").eventResetTime_default;
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors.get("Trigger").eventResetTime_current = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors.get("Trigger").eventResetTime_current = 0;
                    }	
         }
        
        
    }

   
}
