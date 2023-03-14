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
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
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

    private final String detectorName;
    private final ConstantsManager ccdb = new ConstantsManager();
    private final ArrayList<String> detectorTabNames = new ArrayList();
    private final IndexedList<DataGroup> detectorData = new IndexedList<>(3);
    private DataGroup detectorSummary = null;
    private JPanel detectorPanel = null;
    private EmbeddedCanvasTabbed detectorCanvas = null;
    private DetectorPane2D detectorView = null;
    private ButtonGroup buttonGroup = null;
    private int numberOfEvents;
    private boolean sectorButtons = false;
    private int detectorActiveSector = 1;
    private boolean detectorLogZ = true;
    private boolean detectorLogY = false;
    private boolean active = true;
    private final JRadioButton[] bS = new JRadioButton[6];

    public IndexedList<List<Float>> ttdcs = new IndexedList<>(4);
    public IndexedList<List<Float>> fadcs = new IndexedList<>(4);
    public IndexedList<List<Float>> ftdcs = new IndexedList<>(4);
    public IndexedList<List<Integer>> fapmt = new IndexedList<>(3);
    public IndexedList<List<Integer>> ftpmt = new IndexedList<>(3);

    public int bitsec = 0;
    public long trigger = 0;
    public double max_occ = 10.0;
    public int trigFD = 0;
    public int trigCD = 0;

    private boolean testTrigger = true;
    private int UITriggerMask = 0;     // trigger mask from UI

    public double tdcconv = 0.023456;
    public double period = 4;
    public double phase = 1;
    public int ncycles = 6;
    public double rfbucket = 4.008;

    private static PrintStream outStream = null;
    private static PrintStream errStream = null;

    public DetectorMonitor(String name) {
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
        this.detectorPanel = new JPanel();
        this.detectorCanvas = new EmbeddedCanvasTabbed();
        this.detectorView = new DetectorPane2D();
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

        if (!testTriggerMask() && this.testTrigger) {
            return;
        }
        this.setNumberOfEvents(this.getNumberOfEvents() + 1);

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

    public void setTestTrigger(boolean test) {
        this.testTrigger = test;
    }

    public long getTriggerMask() {
        return (this.UITriggerMask & 0x00000000ffffffffL);
    }

    public int getL1Trigger() {
        return (int) (this.trigger) & 0x000000000ffffffff;
    }

    public int getTSTrigger() {
        return (int) (this.trigger >> 32) & 0x00000000ffffffff;
    }

    public boolean isTrigBitSet(int bit) {
        long mask = 0L;
        mask = 1L << bit;
        return isTrigMaskSet(mask);
    }

    public boolean isTrigMaskSet(long mask) {
        return (this.trigger & mask) != 0L;
    }

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

    public boolean isGoodECALTrigger(int is) {
        return is == getECALTriggerSector();
    }

    @Deprecated
    public int getECALTriggerSector() {
        return (int) (this.getL1Trigger() != 0 ? Math.log10(getL1Trigger() >> 19) / 0.301 + 1 : 0);
    }

    @Deprecated
    public int getPCALTriggerSector() {
        return (int) (this.getL1Trigger() != 0 ? Math.log10(getL1Trigger() >> 13) / 0.301 + 1 : 0);
    }

    @Deprecated
    public int getHTCCTriggerSector() {
        return (int) (this.getL1Trigger() != 0 ? Math.log10(getL1Trigger() >> 7) / 0.301 + 1 : 0);
    }

    public void setUITriggerMask(int bit) {
        this.UITriggerMask |= (1 << bit);
    }

    public void clearUITriggerMask(int bit) {
        this.UITriggerMask &= ~(1 << bit);
    }

    public boolean testTriggerMask() {
        return this.UITriggerMask != 0 ? isTrigMaskSet(this.getTriggerMask()) : true;
    }

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

    public IndexedList<DataGroup> getDataGroup() {
        return detectorData;
    }

    public String getDetectorName() {
        return detectorName;
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

    public void setActiveSector(int sector) {
        detectorActiveSector = sector;
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
        JSplitPane splitPane = new JSplitPane();
        splitPane.setLeftComponent(getDetectorView());
        splitPane.setRightComponent(getDetectorCanvas());
        if (flagDetectorView) {
            getDetectorPanel().add(splitPane, BorderLayout.CENTER);
        } else {
            getDetectorPanel().add(getDetectorCanvas(), BorderLayout.CENTER);
            if (sectorButtons) {
                getDetectorPanel().add(getButtonPane(), BorderLayout.PAGE_END);
            }
        }
        createHistos();
        plotHistos();
        if (sectorButtons) {
            bS[1].doClick();
        }
    }

    public JPanel getButtonPane() {
        buttonGroup = new ButtonGroup();
        JPanel buttonPane = new JPanel();
        for (int i = 0; i < 6; i++) {
            bS[i] = new JRadioButton("Sector " + (i + 1));
            buttonPane.add(bS[i]);
            bS[i].setActionCommand(String.valueOf(i + 1));
            bS[i].addActionListener(this);
            buttonGroup.add(bS[i]);
        }
        return buttonPane;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        this.detectorActiveSector = Integer.parseInt(buttonGroup.getSelection().getActionCommand());
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

    public LinkedHashMap<String, String> printCanvas(String dir, String timestamp) {
        // print canvas to files
        LinkedHashMap<String, String> prints = new LinkedHashMap<>();
        int nsec = 2;
        if(this.sectorButtons) nsec = 7;
        for(int sec=1; sec<nsec; sec++) {
            this.setActiveSector(sec);
            this.plotHistos();
            for (int tab = 0; tab < this.detectorTabNames.size(); tab++) {
                String name = this.detectorTabNames.get(tab);
                if(name.endsWith("_s")) name += sec;
                String fileName = dir + "/" + this.detectorName + "_" + name + ".png";
                if(!prints.containsKey(fileName)) {
                    System.out.println(fileName);
                    this.detectorCanvas.getCanvas(this.getCanvasTabName(this.detectorTabNames.get(tab))).save(fileName);
                    prints.put(fileName, name);
                }
            }
        }
        if(this.sectorButtons) bS[1].doClick();
        return prints;
    }

    @Override
    public void resetEventListener() {
        System.out.println("Resetting " + this.getDetectorName() + " histogram");
        this.createHistos();
        this.plotHistos();
    }

    public void setCanvasUpdate(int time) {
        for (int tab = 0; tab < this.detectorTabNames.size(); tab++) {
            String name = this.getCanvasTabName(this.detectorTabNames.get(tab));
            this.detectorCanvas.getCanvas(name).initTimer(time);
        }
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setDetectorCanvas(EmbeddedCanvasTabbed canvas) {
        this.detectorCanvas = canvas;
    }

    private String getCanvasTabName(String name) {
        if(name.endsWith("_s")) name = name.substring(0, name.length()-2);
        return name;
    }
    
    public void setDetectorTabNames(String... names) {
        EmbeddedCanvasTabbed canvas = null;
        for (String name : names) {
            this.detectorTabNames.add(name);
            String canvasName = this.getCanvasTabName(name);
            if(canvas == null) 
                canvas = new EmbeddedCanvasTabbed(canvasName);
            else
                canvas.addCanvas(canvasName);
        }
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
        DetectorMonitor.resetStreams();
    }

    public void analysisUpdate() {

    }

    public void readDataGroup(TDirectory dir) {
        String folder = this.getDetectorName() + "/";
        System.out.println("Reading from: " + folder);
        DataGroup sum = this.getDetectorSummary();
        if (sum != null) {
            int nrows = sum.getRows();
            int ncols = sum.getColumns();
            int nds = nrows * ncols;
            DataGroup newSum = new DataGroup(ncols, nrows);
            for (int i = 0; i < nds; i++) {
                List<IDataSet> dsList = sum.getData(i);
                for (IDataSet ds : dsList) {
                    if (dir.getObject(folder, ds.getName()) != null) {
                        newSum.addDataSet(dir.getObject(folder, ds.getName()), i);
                    }
                }
            }
            this.setDetectorSummary(newSum);

        }

        Map<Long, DataGroup> map = this.getDataGroup().getMap();
        for (Map.Entry<Long, DataGroup> entry : map.entrySet()) {
            Long key = entry.getKey();
            DataGroup group = entry.getValue();
            int nrows = group.getRows();
            int ncols = group.getColumns();
            int nds = nrows * ncols;
            DataGroup newGroup = new DataGroup(ncols, nrows);
            for (int i = 0; i < nds; i++) {
                List<IDataSet> dsList = group.getData(i);
                for (IDataSet ds : dsList) {
                    if (dir.getObject(folder, ds.getName()) != null) {
                        newGroup.addDataSet(dir.getObject(folder, ds.getName()), i);
                    }
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
        int nds = nrows * ncols;
        for (int i = 0; i < nds; i++) {
            List<IDataSet> dsList = sum.getData(i);
            for (IDataSet ds : dsList) {
                dir.addDataSet(ds);
            }
        }
        Map<Long, DataGroup> map = this.getDataGroup().getMap();
        for (Map.Entry<Long, DataGroup> entry : map.entrySet()) {
            DataGroup group = entry.getValue();
            nrows = group.getRows();
            ncols = group.getColumns();
            nds = nrows * ncols;
            for (int i = 0; i < nds; i++) {
                List<IDataSet> dsList = group.getData(i);
                for (IDataSet ds : dsList) {
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
            c.cd(i);
            String opt = " ";
            c.getPad().getAxisZ().setLog(getLogZ());
            c.getPad().getAxisY().setLog(getLogY());
            for (IDataSet ds : dsList) {
                c.draw(ds, opt);
                opt = "same";
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
