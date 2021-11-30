package org.clas.viewer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import org.jlab.detector.base.DetectorOccupancy;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.detector.view.DetectorPane2D;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.IDataSet;
import org.jlab.groot.data.TDirectory;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;
import org.jlab.io.task.IDataEventListener;
import org.jlab.utils.groups.IndexedList;

/**
 *
 * @author devita
 */


public class DetectorMonitor implements IDataEventListener, ActionListener {    
    
    private final String           detectorName;
    private ConstantsManager                    ccdb = new ConstantsManager(); 
    private ArrayList<String>      detectorTabNames  = new ArrayList();
    private IndexedList<DataGroup> detectorData      = new IndexedList<DataGroup>(3);
    private DataGroup              detectorSummary   = null;
    private DetectorOccupancy      detectorOccupancy = new DetectorOccupancy();
    private JPanel                 detectorPanel     = null;
    private EmbeddedCanvasTabbed   detectorCanvas    = null;
    private DetectorPane2D         detectorView      = null;
    private ButtonGroup            buttonGroup       = null;
    private int                    numberOfEvents;
    private Boolean                sectorButtons     = false;
    private int                 detectorActiveSector = 1;
    private Boolean                     detectorLogZ = true;
    private Boolean                     detectorLogY = false;
    private Boolean                             isTB = false;
    private boolean                           active = true;
    private JRadioButton[]   bS = new JRadioButton[6];
    private JCheckBox        tbBtn;
    
    public IndexedList<List<Float>>         ttdcs = new IndexedList<List<Float>>(4);
    public IndexedList<List<Float>>         fadcs = new IndexedList<List<Float>>(4);
    public IndexedList<List<Float>>         ftdcs = new IndexedList<List<Float>>(4);
    public IndexedList<List<Integer>>       fapmt = new IndexedList<List<Integer>>(3); 
    public IndexedList<List<Integer>>       ftpmt = new IndexedList<List<Integer>>(3); 
    
    public int bitsec = 0;
    public long trigger = 0;
    public double max_occ = 10.0;
    public int trigFD = 0;
    public int trigCD = 0;
    
//    public boolean testTrigger = false;
//    public boolean TriggerBeam[] = new boolean[32];
    public  int   UITriggerMask = 0;     // trigger mask from UI
    private long  triggerMask   = 0L;    // monitor specific mask
    
    public double tdcconv  = 0.023456;
    public double period  = 4;
    public double phase   = 1;
    public int    ncycles = 6;

    private static PrintStream outStream = null;
    private static PrintStream errStream = null;
    
    public DetectorMonitor(String name){
        GStyle.getAxisAttributesX().setTitleFontSize(18); //24
        GStyle.getAxisAttributesX().setLabelFontSize(18); //18
        GStyle.getAxisAttributesY().setTitleFontSize(18); //24
        GStyle.getAxisAttributesY().setLabelFontSize(18); //18
        GStyle.getAxisAttributesZ().setLabelFontSize(14); //14
        GStyle.setPalette("kDefault");
        GStyle.getAxisAttributesX().setLabelFontName("Avenir");
        GStyle.getAxisAttributesY().setLabelFontName("Avenir");
        GStyle.getAxisAttributesZ().setLabelFontName("Avenir");
        GStyle.getAxisAttributesX().setTitleFontName("Avenir");
        GStyle.getAxisAttributesY().setTitleFontName("Avenir");
        GStyle.getAxisAttributesZ().setTitleFontName("Avenir");
        GStyle.setGraphicsFrameLineWidth(1);
        GStyle.getH1FAttributes().setLineWidth(1);
//        GStyle.getH1FAttributes().setOptStat("1111111");

        this.detectorName = name;
        this.detectorPanel  = new JPanel();
        this.detectorCanvas = new EmbeddedCanvasTabbed();
        this.detectorView   = new DetectorPane2D();
        this.numberOfEvents = 0;   
        
        DetectorMonitor.outStream = System.out;
        DetectorMonitor.errStream = System.err;
    }

    
    public void analyze() {
        // analyze detector data at the end of data processing
    }

    public void createHistos() {
        // initialize canvas and create histograms
    }
    
    @Override
    public void dataEventAction(DataEvent event) {
    	
        if (!testTriggerMask()) return;        
        this.setNumberOfEvents(this.getNumberOfEvents()+1);
        
        if (event.getType() == DataEventType.EVENT_START) {
            processEvent(event);
	} else if (event.getType() == DataEventType.EVENT_SINGLE) {		   
            processEvent(event);
            plotEvent(event);
	} else if (event.getType() == DataEventType.EVENT_ACCUMULATE) {
            processEvent(event);
	} else if (event.getType() == DataEventType.EVENT_STOP) {
            analyze();
	}
    }

    public void drawDetector() {
    
    }
    
    public void setTriggerWord(long trig) {
    	   this.trigger = trig;
    }
    
//    public void setTestTrigger(boolean test) {
//    	   this.testTrigger = test;
//    }
/*    
    public boolean isGoodCDTrigger()       {return (testTrigger)? isGoodCD():true;}  
    public boolean isGoodHTCCTrigger()     {return (testTrigger)? isGoodHTCC():true;}
    public boolean isGoodFTOFTrigger()     {return (testTrigger)? isGoodFTOF():true;}
    public boolean isGoodBSTTrigger()      {return (testTrigger)? isGoodBST():true;}
    public boolean isGoodCTOFTrigger()     {return (testTrigger)? isGoodCTOF():true;}
    public boolean isGoodCNDTrigger()      {return (testTrigger)? isGoodCND():true;}
    public boolean isGoodBMTTrigger()      {return (testTrigger)? isGoodBMT():true;}
    public boolean isGoodFD()              {return  this.trigFD>=256&&this.trigFD<=8196;}    
    public boolean isGoodCD()              {return  isGoodBST()||isGoodCTOF()||isGoodCND()||isGoodBMT();}
    public boolean isGoodHTCC()            {return  this.trigFD==1;}    
    public boolean isGoodFTOF()            {return  isGoodFD();}   
    public boolean isGoodBST()             {return  this.trigCD==256;}    
    public boolean isGoodCTOF()            {return  this.trigCD==512;}
    public boolean isGoodCND()             {return  this.trigCD==1024;}   
    public boolean isGoodBMT()             {return  this.trigCD==2048;}       
    public int     getL1Trigger()          {return (this.trigger>>16)&0x0000ffff;}    
    public int     getTSTrigger()          {return this.trigger&0x00000fff;} 
*/
    public void setTriggerMask(long word) {
        this.triggerMask = word;
        System.out.println("["+this.getDetectorName()+"]: trigger mask set to " + Long.toHexString(this.triggerMask));
    }
    
    public void setTriggerMask(String hex) {
        if(hex.startsWith("0x")==true){
            this.setTriggerMask(Long.parseLong(hex.substring(2), 16));
        }
    }
    
    public long getTriggerMask() {
        return this.triggerMask | (this.UITriggerMask & 0x00000000ffffffff);
    }
    
    public int     getL1Trigger()            {return (int)(this.trigger)&0x000000000ffffffff;}
    public int     getTSTrigger()            {return (int)(this.trigger>>32)&0x00000000ffffffff;}

    public boolean isTrigBitSet(int bit)     {int mask=0; mask |= 1<<bit; return isTrigMaskSet(mask);}
    public boolean isTrigMaskSet(long mask)  {return (this.trigger&mask)!=0L;}

    public boolean isGoodECALTrigger(int is) {return (this.UITriggerMask!=0) ? is==getECALTriggerSector():true;}    
    
    
    public int getElecTrigger() {
        return (int) this.trigger & 0x1;
    }

    public int getElecTriggerSector() {
        int sector = 0;
        for (int i = 1; i <= 6; i++) {
            if (this.isTrigBitSet(i)) {
                sector = i;
                break;
            }
        }
        return sector;
    }
    public int     getECALTriggerSector()    {return (int) (this.getL1Trigger()!=0 ? Math.log10(getL1Trigger()>>19)/0.301+1:0);}       
    public int     getPCALTriggerSector()    {return (int) (this.getL1Trigger()!=0 ? Math.log10(getL1Trigger()>>13)/0.301+1:0);}       
    public int     getHTCCTriggerSector()    {return (int) (this.getL1Trigger()!=0 ? Math.log10(getL1Trigger()>>7)/0.301+1:0);} 
    
//    public int    getUITriggerMask()        {return this.UITriggerMask;}
    public void setUITriggerMask(int bit) {
        this.UITriggerMask |= (1 << bit);
    }

    public void clearUITriggerMask(int bit) {
        this.UITriggerMask &= ~(1 << bit);
    }

    public boolean testTriggerMask() {
        return this.UITriggerMask != 0 ? isTrigMaskSet(this.getTriggerMask()) : true;
    }
//    public boolean isGoodTrigger(int bit) {return TriggerBeam[bit] ? isTrigBitSet(bit):true;}

    public boolean isActive() {
        return active;
    }

    public ConstantsManager getCcdb() {
        return ccdb;
    }
   
    public EmbeddedCanvasTabbed getDetectorCanvas() {
        return detectorCanvas;
    }
    
    public ArrayList<String> getDetectorTabNames() {
        return detectorTabNames;
    }
    
    public IndexedList<DataGroup>  getDataGroup(){
        return detectorData;
    }

    public String getDetectorName() {
        return detectorName;
    }
    
    public DetectorOccupancy getDetectorOccupancy() {
        return detectorOccupancy;
    }
    
    public JPanel getDetectorPanel() {
        return detectorPanel;
    }
    
    public DataGroup getDetectorSummary() {
        return detectorSummary;
    }
    
    public DetectorPane2D getDetectorView() {
        return detectorView;
    }
    
    public void useSectorButtons(boolean flag) {
    	    this.sectorButtons = flag;
    }
    
    public int getActiveSector() {
    	    return detectorActiveSector;
    }
    
    public int getNumberOfEvents() {
        return numberOfEvents;
    }
    
    public void setLogZ(boolean flag) {
	    this.detectorLogZ = flag;
    }
    
    public Boolean getLogZ() {
	    return this.detectorLogZ;
    }
    
    public void setLogY(boolean flag) {
	    this.detectorLogY = flag;
    }
    
    public Boolean getLogY() {
	    return this.detectorLogY;
    }   

    public void init(boolean flagDetectorView) {
        // initialize monitoring application
        // detector view is shown if flag is true
        getDetectorPanel().setLayout(new BorderLayout());
        drawDetector();
        JSplitPane   splitPane = new JSplitPane();
        splitPane.setLeftComponent(getDetectorView());
        splitPane.setRightComponent(getDetectorCanvas());
        if(flagDetectorView) {
            getDetectorPanel().add(splitPane,BorderLayout.CENTER);  
        }
        else {
            getDetectorPanel().add(getDetectorCanvas(),BorderLayout.CENTER); 
            if (sectorButtons) getDetectorPanel().add(getButtonPane(),BorderLayout.PAGE_END);  
        }
        createHistos();
        plotHistos(); 
        if (sectorButtons) bS[1].doClick();
    }
    
    public JPanel getButtonPane() {
        buttonGroup = new ButtonGroup();
        JPanel buttonPane = new JPanel();
        for(int i=0; i<6; i++) {
            bS[i] = new JRadioButton("Sector " + (i+1)); 
            buttonPane.add(bS[i]); 
            bS[i].setActionCommand(String.valueOf(i+1)); 
            bS[i].addActionListener(this);
            buttonGroup.add(bS[i]);
        }
        return buttonPane;
    } 
    
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        this.detectorActiveSector   = Integer.parseInt(buttonGroup.getSelection().getActionCommand());
        plotHistos();
    } 
    
    public void processEvent(DataEvent event) {
        // process event
    }
    
    public void plotEvent(DataEvent event) {
        // process event
    }
    
    public void plotDetectorSummary(EmbeddedCanvas c, String hname) {
    }  
    
    public void plotHistos() {
    }
    
    public LinkedHashMap<String,String> printCanvas(String dir, String timestamp) {
        // print canvas to files
        LinkedHashMap<String, String> prints = new LinkedHashMap<>();
        for(int tab=0; tab<this.detectorTabNames.size(); tab++) {
            String fileName = dir + "/" + this.detectorName + "_canvas" + tab + "_" + timestamp + ".png";
            System.out.println(fileName);
            this.detectorCanvas.getCanvas(this.detectorTabNames.get(tab)).save(fileName);
            prints.put(fileName, this.detectorTabNames.get(tab));
        }
        return prints;
    }
    
    @Override
    public void resetEventListener() {
        System.out.println("Resetting " + this.getDetectorName() + " histogram");
        this.createHistos();
        this.plotHistos();
    }
    
    public void setCanvasUpdate(int time) {
        for(int tab=0; tab<this.detectorTabNames.size(); tab++) {
            this.detectorCanvas.getCanvas(this.detectorTabNames.get(tab)).initTimer(time);
        }
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }

    public void setDetectorCanvas(EmbeddedCanvasTabbed canvas) {
        this.detectorCanvas = canvas;
    }
    
    public void setDetectorTabNames(String... names) {
        for(String name : names) {
            this.detectorTabNames.add(name);
        }
        EmbeddedCanvasTabbed canvas = new EmbeddedCanvasTabbed(names);
        this.setDetectorCanvas(canvas);
    }
 
    public void setDetectorSummary(DataGroup group) {
        this.detectorSummary = group;
    }
    
    public void setNumberOfEvents(int numberOfEvents) {
        this.numberOfEvents = numberOfEvents;
    }

    @Override
    public final void timerUpdate() {
        this.analysisUpdate();
        this.resetStreams();
    }

    public void analysisUpdate() {
        
    }

    public void readDataGroup(TDirectory dir) {
        String folder = this.getDetectorName() + "/";
        System.out.println("Reading from: " + folder);
        DataGroup sum = this.getDetectorSummary();
        if (sum!=null) {
        int nrows = sum.getRows();
        int ncols = sum.getColumns();
        int nds   = nrows*ncols;
        DataGroup newSum = new DataGroup(ncols,nrows);
        for(int i = 0; i < nds; i++){
            List<IDataSet> dsList = sum.getData(i);
            for(IDataSet ds : dsList){
                if(dir.getObject(folder, ds.getName())!=null) newSum.addDataSet(dir.getObject(folder, ds.getName()),i);
            }
        }            
        this.setDetectorSummary(newSum);
        
        }
        
        Map<Long, DataGroup> map = this.getDataGroup().getMap();
        for( Map.Entry<Long, DataGroup> entry : map.entrySet()) {
            Long key = entry.getKey();
            DataGroup group = entry.getValue();
            int nrows = group.getRows();
            int ncols = group.getColumns();
            int nds   = nrows*ncols;
            DataGroup newGroup = new DataGroup(ncols,nrows);
            for(int i = 0; i < nds; i++){
                List<IDataSet> dsList = group.getData(i);
                for(IDataSet ds : dsList){
                    if(dir.getObject(folder, ds.getName())!=null) newGroup.addDataSet(dir.getObject(folder, ds.getName()),i);
                }
            }
            map.replace(key, newGroup);
        }
        this.plotHistos();
        
    }
    
    public void writeDataGroup(TDirectory dir) {
        System.out.println(this.getDetectorName());
        String folder = "/" + this.getDetectorName();
        dir.mkdir(folder);
        dir.cd(folder);
        DataGroup sum = this.getDetectorSummary();
        int nrows = sum.getRows();
        int ncols = sum.getColumns();
        int nds   = nrows*ncols;
        for(int i = 0; i < nds; i++){
            List<IDataSet> dsList = sum.getData(i);
            for(IDataSet ds : dsList){
                dir.addDataSet(ds);
            }
        }            
        Map<Long, DataGroup> map = this.getDataGroup().getMap();
        for( Map.Entry<Long, DataGroup> entry : map.entrySet()) {
            DataGroup group = entry.getValue();
            nrows = group.getRows();
            ncols = group.getColumns();
            nds   = nrows*ncols;
            for(int i = 0; i < nds; i++){
                List<IDataSet> dsList = group.getData(i);
                for(IDataSet ds : dsList){
                    dir.addDataSet(ds);
                }
            }
        }
    }
    
    public void drawGroup(EmbeddedCanvas c, DataGroup group) {
        int nrows = group.getRows();
        int ncols = group.getColumns();
        c.divide(ncols, nrows); 	    
        int nds = nrows * ncols;
        for (int i = 0; i < nds; i++) {
            List<IDataSet> dsList = group.getData(i);
            c.cd(i);  String opt = " ";
            c.getPad().getAxisZ().setLog(getLogZ());
            c.getPad().getAxisY().setLog(getLogY());
            for (IDataSet ds : dsList) {
               c.draw(ds,opt); opt="same";
            }
        } 	
        c.update();
    }     
    
    public static void resetStreams() {
        System.setOut(outStream);
        System.setErr(errStream);
        System.out.flush();
        System.err.flush();
    }
        
}
