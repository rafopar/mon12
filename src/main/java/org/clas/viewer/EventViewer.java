package org.clas.viewer;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
    private int histoResetEvents = 0;
        
    private String defaultEtHost = null;
    private String defaultEtIp   = null;
    
    public String outPath = null; 
    public String elog = null;
    private  long triggerMask;
            
    // detector monitors
    public LinkedHashMap<String, DetectorMonitor> monitors = new LinkedHashMap<>();
    
    
    public final void initMonitors() {
        monitors.put("BAND",        new BANDmonitor("BAND"));
        monitors.put("BMT",         new BMTmonitor("BMT"));
        monitors.put("BST",         new BSTmonitor("BST"));
        monitors.put("CND",         new CNDmonitor("CND")); 
        monitors.put("CTOF",        new CTOFmonitor("CTOF")); 
        monitors.put("DC",          new DCmonitor("DC"));     
        monitors.put("ECAL",        new ECmonitor("ECAL"));       
        monitors.put("FMT",         new FMTmonitor("FMT"));      
        monitors.put("FTCAL",       new FTCALmonitor("FTCAL"));   
        monitors.put("FTHODO",      new FTHODOmonitor("FTHODO")); 
        monitors.put("FTOF",        new FTOFmonitor("FTOF"));             
        monitors.put("FTTRK",       new FTTRKmonitor("FTTRK"));   
        monitors.put("HTCC",        new HTCCmonitor("HTCC"));     
        monitors.put("LTCC",        new LTCCmonitor("LTCC")); 
        monitors.put("RICH",        new RICHmonitor("RICH"));    
        monitors.put("RTPC",        new RTPCmonitor("RTPC"));    
        monitors.put("RF",          new RFmonitor("RF"));       
        monitors.put("HEL",         new HELmonitor("HEL"));      
        monitors.put("FCUP",        new FCUPmonitor("FCUP")); 
        monitors.put("Trigger",     new TRIGGERmonitor("Trigger"));
        monitors.put("TimeJitter",  new TJITTERmonitor("TimeJitter"));
    }
                    
    public EventViewer(String host, String ip) {  
        // create main panel
        mainPanel = new JPanel();	
        mainPanel.setLayout(new BorderLayout());
        
        tabbedpane = new JTabbedPane();
        tabbedpane.addChangeListener(this);
        
        this.defaultEtHost = host;
        this.defaultEtIp   = ip;
        processorPane = new DataSourceProcessorPane(defaultEtHost, defaultEtIp);
        processorPane.setUpdateRate(analysisUpdateTime);
        processorPane.addEventListener(this);

        mainPanel.add(tabbedpane);
        mainPanel.add(processorPane,BorderLayout.PAGE_END);
        
        this.initMonitors();
        
    }
    
    public void init() {
        this.initsPaths();
        this.initSummary();
        this.initTabs();
        this.initMenus();
    }
    
    public void initMenus() {   
        // create menu bar
        menuBar = new JMenuBar();
        JMenuItem menuItem;
        JMenu file = new JMenu("File");
        file.getAccessibleContext().setAccessibleDescription("File options");
        menuItem = new JMenuItem("Open histograms file");
        menuItem.getAccessibleContext().setAccessibleDescription("Open histograms file");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuItem = new JMenuItem("Save histograms to file");
        menuItem.getAccessibleContext().setAccessibleDescription("Save histograms to file");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuItem = new JMenuItem("Print histograms as png");
        menuItem.getAccessibleContext().setAccessibleDescription("Print histograms as png");
        menuItem.addActionListener(this);
        file.add(menuItem);
        
        menuBar.add(file);
        JMenu settings = new JMenu("Settings");
        settings.getAccessibleContext().setAccessibleDescription("Choose monitoring parameters");
        menuItem = new JMenuItem("Set GUI update interval");
        menuItem.getAccessibleContext().setAccessibleDescription("Set GUI update interval");
        menuItem.addActionListener(this);
        settings.add(menuItem);
        menuItem = new JMenuItem("Set global z-axis log scale");
        menuItem.getAccessibleContext().setAccessibleDescription("Set global z-axis log scale");
        menuItem.addActionListener(this);
        settings.add(menuItem);
        menuItem = new JMenuItem("Set global z-axis lin scale");
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
        upload.getAccessibleContext().setAccessibleDescription("Upload histograms to the Logbook");
        menuItem = new JMenuItem("Upload all histos to the logbook");
        menuItem.getAccessibleContext().setAccessibleDescription("Upload all histos to the logbook");
        menuItem.addActionListener(this);
        upload.add(menuItem);
        menuBar.add(upload);
        
        JMenu reset = new JMenu("Reset");
        reset.getAccessibleContext().setAccessibleDescription("Reset histograms");        
        JMenuItem menuItemdisable = new JMenuItem("Set periodic reset");
        menuItemdisable.getAccessibleContext().setAccessibleDescription("Set periodic reset");
        menuItemdisable.addActionListener(this);
        reset.add(menuItemdisable);        
        for(String key : this.monitors.keySet()) {
            if(this.monitors.get(key).isActive()) {
                JMenuItem menuItemDet = new JMenuItem("Reset " + key + " histograms");
                menuItemDet.getAccessibleContext().setAccessibleDescription("Reset " + key + " histograms");
                menuItemDet.addActionListener(this);
                reset.add(menuItemDet);
            }
        }        
        JMenuItem menuItemStream = new JMenuItem("Reset stdout/stderr");
        menuItemStream.getAccessibleContext().setAccessibleDescription("Reset stdout/stderr");
        menuItemStream.addActionListener(this);
        reset.add(menuItemStream);        
        menuBar.add(reset);
        
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
        
        String TriggerDef[] = { "Electron OR", "Electron Sec 1","Electron Sec 2","Electron Sec 3",
                        "Electron Sec 4","Electron Sec 5","Electron Sec 6",
		        "","","","","","","","","","","","","","","","","","","","","","","","",
		        "Random Pulser"};    
        
        JMenu trigBitsBeam = new JMenu("TriggerBits");
        trigBitsBeam.getAccessibleContext().setAccessibleDescription("Select Trigger Bits");        
        for (int i=0; i<TriggerDef.length; i++) {
            
            
            JCheckBoxMenuItem bb = new JCheckBoxMenuItem(i + " " + TriggerDef[i]);
            final Integer bit = new Integer(i);
            bb.addItemListener(new ItemListener() {
                @Override
                @SuppressWarnings("empty-statement")
                public void itemStateChanged(ItemEvent e) {
                	
                    if(e.getStateChange() == ItemEvent.SELECTED) {
                        for(String key : monitors.keySet()) {
                            monitors.get(key).setUITriggerMask(bit);
                        }
                    } else {
                        for(String key : monitors.keySet()) {
                            monitors.get(key).clearUITriggerMask(bit);
                        }
                    };
                }
            });
            boolean bstate = ((triggerMask >> i) & 1) == 1;
            bb.setState(bstate);
            trigBitsBeam.add(bb); 
        	        	
        }
        menuBar.add(trigBitsBeam);
        
    }
        
    public void initsPaths() {
        String dir = ClasUtilsFile.getResourceDir("CLAS12DIR", "etc/bankdefs/hipo4");
        schemaFactory.initFromDirectory(dir);
        
        outPath = System.getProperty("user.home") + "/CLAS12MON/output";
        System.out.println("OutPath set to: " + outPath);
    }
        
    public void initSummary() {
        GStyle.getAxisAttributesX().setTitleFontSize(18);
        GStyle.getAxisAttributesX().setLabelFontSize(14);
        GStyle.getAxisAttributesY().setTitleFontSize(18);
        GStyle.getAxisAttributesY().setLabelFontSize(14);
        
        if(this.monitors.get("DC").isActive() ||
           this.monitors.get("HTCC").isActive() ||
           this.monitors.get("LTCC").isActive() ||
           this.monitors.get("FTOF").isActive() ||
           this.monitors.get("ECAL").isActive() ||
           this.monitors.get("RICH").isActive()) {
            this.createSummary("FD",3,3);
        }
        if(this.monitors.get("BST").isActive() ||
           this.monitors.get("BMT").isActive() ||
           this.monitors.get("CTOF").isActive() ||
           this.monitors.get("CND").isActive()) {
            this.createSummary("CD",2,2);
        }
        if(this.monitors.get("FTCAL").isActive() ||
           this.monitors.get("FTHODO").isActive() ||
           this.monitors.get("FTTRK").isActive()) {
            this.createSummary("FT",1,3);
        }
        if(this.monitors.get("RF").isActive() ||
           this.monitors.get("HEL").isActive() ||
           this.monitors.get("Trigger").isActive() ||
           this.monitors.get("TimeJitter").isActive()) {
            this.createSummary("RF/HEL/JITTER/TRIGGER",2,2);
        }
        
        JPanel    CLAS12View = new JPanel(new BorderLayout());
        JSplitPane splitPanel = new JSplitPane();
        splitPanel.setLeftComponent(CLAS12View);
        splitPanel.setRightComponent(CLAS12Canvas);
        JTextPane clas12Text   = new JTextPane();
        clas12Text.setText("CLAS12\n monitoring plots\n V6.0\n");
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
        CLAS12View.add(clas12Textinfo,BorderLayout.BEFORE_FIRST_LINE );
        CLAS12View.add(clas12Design);
        CLAS12View.add(clas12Text,BorderLayout.PAGE_END);

        tabbedpane.add(splitPanel,"Summary");
        
    }
    
    public void createSummary(String name, int nx, int ny) {
        if(CLAS12Canvas==null)
            CLAS12Canvas = new EmbeddedCanvasTabbed(name);
        else
            CLAS12Canvas.addCanvas(name);
        CLAS12Canvas.getCanvas(name).divide(nx,ny);
        CLAS12Canvas.getCanvas(name).setGridX(false);
        CLAS12Canvas.getCanvas(name).setGridY(false); 
    }
    public void initTabs() {

        this.plotSummaries();
        
        for(String key : monitors.keySet()) {
            if(monitors.get(key).isActive()) this.tabbedpane.add(this.monitors.get(key).getDetectorPanel(), monitors.get(key).getDetectorName()); //don't show FMT tab
            monitors.get(key).getDetectorView().getView().addDetectorListener(this);                       
        }
        this.tabbedpane.add(new Acronyms(),"Acronyms");
       
        this.setCanvasUpdate(canvasUpdateTime);
     }
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println(e.getActionCommand());
        if("Set GUI update interval".equals(e.getActionCommand())) {
            this.chooseUpdateInterval();
        }
        if("Set global z-axis log scale".equals(e.getActionCommand())) {
        	   for(String key : monitors.keySet()) {this.monitors.get(key).setLogZ(true);this.monitors.get(key).plotHistos();}
        }
        if("Set global z-axis lin scale".equals(e.getActionCommand())) {
           for(String key : monitors.keySet()) {this.monitors.get(key).setLogZ(false);this.monitors.get(key).plotHistos();}
        }
        if("Set DC occupancy scale max".equals(e.getActionCommand())) {
           setDCRange(e.getActionCommand());
        }
        if("Set run number".equals(e.getActionCommand())) {
           setRunNumber(e.getActionCommand());
        }

        if("Open histograms file".equals(e.getActionCommand())) {
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
        if("Print histograms as png".equals(e.getActionCommand())) {
            this.printHistosToFile();
        }
        if("Save histograms to file".equals(e.getActionCommand())) {
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
        
        if("Upload all histos to the logbook".equals(e.getActionCommand())) {   
            
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
            
            if(this.CLAS12Canvas.getCanvas("FD")!=null) {
                String fileName1 = data + "/summary_FD_"+tstamp+".png";
                System.out.println(fileName1);
                CLAS12Canvas.getCanvas("FD").save(fileName1);
            }
            if(this.CLAS12Canvas.getCanvas("CD")!=null) {
                String fileName2 = data + "/summary_CD_"+tstamp+".png";
                System.out.println(fileName2);
                CLAS12Canvas.getCanvas("CD").save(fileName2);
            }
            if(this.CLAS12Canvas.getCanvas("FT")!=null) {
                String fileName3 = data + "/summary_FT_"+tstamp+".png";
                System.out.println(fileName3);
                CLAS12Canvas.getCanvas("FT").save(fileName3);
            }
            if(this.CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER")!=null) {
                String fileName4 = data + "/summary_RHJT_"+tstamp+".png";
                System.out.println(fileName4);
                CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER").save(fileName4);
            }
            
            LogEntry entry = new LogEntry("All online monitoring histograms for run number " + this.runNumber, this.elog);
            
            System.out.println("Starting to upload all monitoring plots");
            
            try{
                if(this.CLAS12Canvas.getCanvas("FD")!=null)
                    entry.addAttachment(data + "/summary_FD_" + tstamp + ".png", "Summary plots for the forward detector");
                if(this.CLAS12Canvas.getCanvas("CD")!=null)
                    entry.addAttachment(data + "/summary_CD_" + tstamp + ".png", "Summary plots for the central detector");
                if(this.CLAS12Canvas.getCanvas("FT")!=null) 
                    entry.addAttachment(data + "/summary_FT_" + tstamp + ".png", "Summary plots for the forward tagger");
                if(this.CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER")!=null)
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
                 
        if ("Set periodic reset".equals(e.getActionCommand())){
            this.choosePeriodicReset();
        }
        
        if ( e.getActionCommand().substring(0, 5).equals("Reset") && e.getActionCommand().split(" ").length==3){
            String key = e.getActionCommand().split(" ")[1];
            if(monitors.containsKey(key)) monitors.get(key).resetEventListener();
        }
        
        if ("Reset stdout/stderr".equals(e.getActionCommand())){
            DetectorMonitor.resetStreams();
        }
        
        
    }

    public void choosePeriodicReset() {
        String s = (String)JOptionPane.showInputDialog(
                    null,
                    "Set periodic histogram reset (#events), 0-disabled ",
                    " ",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "0");
        if(s!=null){
            int nev = 1000;
            try { 
                nev= Integer.parseInt(s);
            } catch(NumberFormatException e) { 
                JOptionPane.showMessageDialog(null, "Value must be a >=0!");
            }
            if(nev>=0) {
                this.histoResetEvents = nev;
                System.out.println("Resetting histograms every " + this.histoResetEvents + " events");
            }
            else {
                JOptionPane.showMessageDialog(null, "Value must be a >=0!");
            }
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
        
    private JLabel getImage(String path, double scale) {
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
        double width = imageIcon.getIconWidth() * scale;
        double height = imageIcon.getIconHeight() * scale;
        imageIcon = new ImageIcon(image.getScaledInstance((int) width, (int) height, Image.SCALE_SMOOTH));
        label = new JLabel(imageIcon);
        return label;
    }
    
    private int getEventNumber(DataEvent event) {
        DataBank bank = event.getBank("RUN::config");
        return (bank!=null) ? bank.getInt("event", 0): this.eventNumber;
    }
    
    private int getRunNumber(DataEvent event) {
        int rNum = this.runNumber;
        DataBank bank = event.getBank("RUN::config");
        if(bank!=null) {
            rNum      = bank.getInt("run", 0);
        }
        return rNum;
    }
    
    public long getTriggerWord(DataEvent event) {    	
        DataBank bank = event.getBank("RUN::config");	        
        return bank.getLong("trigger", 0);
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
        
    	this.eventNumber++;
        
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
        
            if(this.histoResetEvents>0 && (this.eventNumber % this.histoResetEvents) == 0)
                this.resetEventListener();
            
            if(this.runNumber != this.getRunNumber(hipo)) {
                this.runNumber = this.getRunNumber(hipo);
                System.out.println("Setting run number to: " +this.runNumber);
                resetEventListener();
                this.clas12Textinfo.setText("\nrun number: "+this.runNumber + "\n");
            }     
            
            for(String key : monitors.keySet()) {
                if(this.monitors.get(key).isActive()) {
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
        if(this.CLAS12Canvas.getCanvas("FD")!=null) {
            // DC
            this.CLAS12Canvas.getCanvas("FD").cd(0);
            if(this.monitors.get("DC").isActive() && this.monitors.get("DC").getDetectorSummary()!=null) 
                this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("DC").getDetectorSummary().getH1F("summary")); 
            // HTTC
            this.CLAS12Canvas.getCanvas("FD").cd(1);
            if(this.monitors.get("HTCC").isActive() && this.monitors.get("HTCC").getDetectorSummary()!=null) 
                this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("HTCC").getDetectorSummary().getH1F("summary"));
            // LTTC
            this.CLAS12Canvas.getCanvas("FD").cd(2);
            if(this.monitors.get("LTCC").isActive() && this.monitors.get("LTCC").getDetectorSummary()!=null) 
                this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("LTCC").getDetectorSummary().getH1F("summary"));
            // RICH
            this.CLAS12Canvas.getCanvas("FD").cd(3);
            this.CLAS12Canvas.getCanvas("FD").getPad(3).getAxisY().setLog(true);
            if(this.monitors.get("RICH").isActive() && this.monitors.get("RICH").getDetectorSummary()!=null) {
                this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("RICH").getDetectorSummary().getH1F("summary_1"));
                this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("RICH").getDetectorSummary().getH1F("summary_4"), "same");
            }

            // ECAL 
            this.CLAS12Canvas.getCanvas("FD").cd(4); this.CLAS12Canvas.getCanvas("FD").getPad(4).setAxisRange(0.5,6.5,0.5,1.5);
            if(this.monitors.get("ECAL").isActive() && this.monitors.get("ECAL").getDetectorSummary()!=null) { 
                    this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("ECAL").getDetectorSummary().getGraph("PCALu"));
                    this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("ECAL").getDetectorSummary().getGraph("PCALv"),"same");
                    this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("ECAL").getDetectorSummary().getGraph("PCALw"),"same");
                    this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("ECAL").getDetectorSummary().getF1D("p0"),"same");
            }
            this.CLAS12Canvas.getCanvas("FD").cd(5); this.CLAS12Canvas.getCanvas("FD").getPad(5).setAxisRange(0.5,6.5,0.5,1.5);
            if(this.monitors.get("ECAL").isActive() && this.monitors.get("ECAL").getDetectorSummary()!=null) {
                    this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("ECAL").getDetectorSummary().getGraph("ECinu"));
                    this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("ECAL").getDetectorSummary().getGraph("ECinv"),"same");
                    this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("ECAL").getDetectorSummary().getGraph("ECinw"),"same");
                    this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("ECAL").getDetectorSummary().getGraph("ECoutu"),"same");
                    this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("ECAL").getDetectorSummary().getGraph("ECoutv"),"same");
                    this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("ECAL").getDetectorSummary().getGraph("ECoutw"),"same");
                    this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("ECAL").getDetectorSummary().getF1D("p0"),"same");
            }
            // FTOF:
            this.CLAS12Canvas.getCanvas("FD").cd(6);
            this.CLAS12Canvas.getCanvas("FD").getPad(6).getAxisZ().setLog(true);
            if(this.monitors.get("FTOF").isActive() && this.monitors.get("FTOF").getDetectorSummary()!=null) 
                this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("FTOF").getDetectorSummary().getH1F("sum_p1"));
            this.CLAS12Canvas.getCanvas("FD").cd(7);
            this.CLAS12Canvas.getCanvas("FD").getPad(7).getAxisZ().setLog(true);
            if(this.monitors.get("FTOF").isActive() && this.monitors.get("FTOF").getDetectorSummary()!=null) 
                this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("FTOF").getDetectorSummary().getH1F("sum_p2"));
            this.CLAS12Canvas.getCanvas("FD").cd(8);
            this.CLAS12Canvas.getCanvas("FD").getPad(8).getAxisZ().setLog(true);
            if(this.monitors.get("FTOF").isActive() && this.monitors.get("FTOF").getDetectorSummary()!=null) 
                this.CLAS12Canvas.getCanvas("FD").draw(this.monitors.get("FTOF").getDetectorSummary().getH1F("sum_p3"));
        }
        
        //////////////////////////////////////////////////
            ///  CD:
            if(this.CLAS12Canvas.getCanvas("CD")!=null) {
            // CND
            this.CLAS12Canvas.getCanvas("CD").cd(0);
            if(this.monitors.get("CND").isActive() && this.monitors.get("CND").getDetectorSummary()!=null) 
                this.CLAS12Canvas.getCanvas("CD").draw(this.monitors.get("CND").getDetectorSummary().getH1F("summary"));
            // CTOF
            this.CLAS12Canvas.getCanvas("CD").cd(1);
            if(this.monitors.get("CTOF").isActive() && this.monitors.get("CTOF").getDetectorSummary()!=null) 
                this.CLAS12Canvas.getCanvas("CD").draw(this.monitors.get("CTOF").getDetectorSummary().getH1F("summary"));
    //        // RTPC
    //        this.CLAS12Canvas.getCanvas("CD").cd(2);
    //        if(this.monitors.get("RTPC").getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("CD").draw(this.monitors.get("RTPC").getDetectorSummary().getH1F("summary"));
    //        // FMT
    //        this.CLAS12Canvas.getCanvas("CD").cd(3);
    //        if(this.monitors.get("FMT").getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("CD").draw(this.monitors.get("FMT").getDetectorSummary().getH1F("summary"));
            // BMT
            this.CLAS12Canvas.getCanvas("CD").cd(2);
            this.CLAS12Canvas.getCanvas("CD").getPad(2).getAxisZ().setLog(true);
            if(this.monitors.get("BMT").isActive() && this.monitors.get("BMT").getDetectorSummary()!=null) 
                this.CLAS12Canvas.getCanvas("CD").draw(this.monitors.get("BMT").getDetectorSummary().getH1F("summary"));
            // BST
            this.CLAS12Canvas.getCanvas("CD").cd(3);
            this.CLAS12Canvas.getCanvas("CD").getPad(3).getAxisZ().setLog(true);
            if(this.monitors.get("BST").isActive() && this.monitors.get("BST").getDetectorSummary()!=null) 
                this.CLAS12Canvas.getCanvas("CD").draw(this.monitors.get("BST").getDetectorSummary().getH2F("summary"));
        }
        
        
        ///////////////////////////////////////////////////
        // FT:
        if(this.CLAS12Canvas.getCanvas("FT")!=null) {
            // FTCAL
            this.CLAS12Canvas.getCanvas("FT").cd(0);
            if(this.monitors.get("FTCAL").isActive() && this.monitors.get("FTCAL").getDetectorSummary()!=null) 
                this.CLAS12Canvas.getCanvas("FT").draw(this.monitors.get("FTCAL").getDetectorSummary().getH1F("summary"));
            // FTHODO
            this.CLAS12Canvas.getCanvas("FT").cd(1);
            if(this.monitors.get("FTHODO").isActive() && this.monitors.get("FTHODO").getDetectorSummary()!=null) 
                this.CLAS12Canvas.getCanvas("FT").draw(this.monitors.get("FTHODO").getDetectorSummary().getH1F("summary"));
            // FTTRK
            this.CLAS12Canvas.getCanvas("FT").cd(2);
            if(this.monitors.get("FTTRK").isActive() && this.monitors.get("FTTRK").getDetectorSummary()!=null) 
                this.CLAS12Canvas.getCanvas("FT").draw(this.monitors.get("FTTRK").getDetectorSummary().getH1F("summary"));
        }
        ////////////////////////////////////////////////////
      
        ///////////////////////////////////////////////////
        // RF/HEL/JITTER/TRIGGER:
        if(this.CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER")!=null) {
            // RF
            this.CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER").cd(0);
            if(this.monitors.get("RF").isActive() && this.monitors.get("RF").getDetectorSummary()!=null) 
                this.CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER").draw(this.monitors.get("RF").getDetectorSummary().getH1F("summary"));
            // HEL
            this.CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER").cd(1);
            if(this.monitors.get("HEL").isActive() && this.monitors.get("HEL").getDetectorSummary()!=null) 
                this.CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER").draw(this.monitors.get("HEL").getDetectorSummary().getH1F("summary"));
            // FCUP
            this.CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER").cd(2);
            if(this.monitors.get("TimeJitter").isActive() && this.monitors.get("TimeJitter").getDetectorSummary()!=null) 
                this.CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER").draw(this.monitors.get("TimeJitter").getDetectorSummary().getH1F("summary"));
            // TRIGGER
            this.CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER").cd(3);
            this.CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER").getPad(3).getAxisY().setLog(true);
            if(this.monitors.get("Trigger").isActive() && this.monitors.get("Trigger").getDetectorSummary()!=null) 
                this.CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER").draw(this.monitors.get("Trigger").getDetectorSummary().getH1F("summary"));
        }
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
        
        if(this.CLAS12Canvas.getCanvas("FD")!=null) {
            String fileName1 = data + "/summary_FD_"+tstamp+".png";
            System.out.println(fileName1);
            CLAS12Canvas.getCanvas("FD").save(fileName1);
        }
        if(this.CLAS12Canvas.getCanvas("CD")!=null) {
            String fileName2 = data + "/summary_CD_"+tstamp+".png";
            System.out.println(fileName2);
            CLAS12Canvas.getCanvas("CD").save(fileName2);
        }
        if(this.CLAS12Canvas.getCanvas("FT")!=null) {
            String fileName3 = data + "/summary_FT_"+tstamp+".png";
            System.out.println(fileName3);
            CLAS12Canvas.getCanvas("FT").save(fileName3); 
        }
        if(this.CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER")!=null) {
            String fileName4 = data + "/summary_RHJT_"+tstamp+".png";
            System.out.println(fileName4);
            CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER").save(fileName4);
        }     
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
        if(this.CLAS12Canvas.getCanvas("FD")!=null) {
            this.CLAS12Canvas.getCanvas("FD").initTimer(time);
            this.CLAS12Canvas.getCanvas("FD").update();
        }
        if (this.CLAS12Canvas.getCanvas("CD") != null) {
            this.CLAS12Canvas.getCanvas("CD").initTimer(time);
            this.CLAS12Canvas.getCanvas("CD").update();
        }
        if (this.CLAS12Canvas.getCanvas("FT") != null) {
            this.CLAS12Canvas.getCanvas("FT").initTimer(time);
            this.CLAS12Canvas.getCanvas("FT").update();
        }
        if(this.CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER")!=null) {
            this.CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER").initTimer(time);
            this.CLAS12Canvas.getCanvas("RF/HEL/JITTER/TRIGGER").update();
        }
        for(String key : monitors.keySet()) {
            this.monitors.get(key).setCanvasUpdate(time);
        }
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
        
        OptionParser parser = new OptionParser("mon12");
        parser.setRequiresInputList(false);
        parser.setDescription("CLAS12 monitoring app");
        parser.addOption("-geometry", "1600x1000",      "Select window size, e.g. 1600x1200");
        parser.addOption("-tabs",     "All",            "Select active tabs, e.g. BST:FTOF");
        parser.addOption("-logbook",  "HBLOG",          "Select electronic logbook");
        parser.addOption("-trigger",  "0x0",            "Select trigger bits");
        parser.addOption("-ethost",   "clondaq6",       "Select ET host name");
        parser.addOption("-etip",     "129.57.167.60",  "Select ET host name");
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
        
        String ethost = parser.getOption("-ethost").stringValue();
        String etip   = parser.getOption("-etip").stringValue();        
        EventViewer viewer = new EventViewer(ethost, etip);
        
        String tabs     = parser.getOption("-tabs").stringValue();
        if(!tabs.equals("All")) {           
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

        String trigger = parser.getOption("-trigger").stringValue();
        if(trigger.startsWith("0x")==true){
            trigger = trigger.substring(2);
        }
        else 
            trigger = "";
        viewer.triggerMask = Long.parseLong(trigger,16);
        System.out.println("Trigger mask set to 0x" + trigger);

        viewer.elog = parser.getOption("-logbook").stringValue();
        System.out.println("Logbook set to " + viewer.elog);
       
        viewer.init();
        
        JFrame frame = new JFrame("MON12");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.add(viewer.getPanel());
        frame.add(viewer.mainPanel);
        frame.setJMenuBar(viewer.menuBar);
        frame.setSize(xSize, ySize);
        frame.setVisible(true);
    }
       
}
