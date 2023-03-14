package org.clas.detectors;

import java.util.ArrayList;
import java.util.Arrays;
import org.clas.viewer.DetectorMonitor;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.math.F1D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author devita
 */

public class RFmonitor extends DetectorMonitor {
    
    IndexedTable rfConfig = null;
    private int    ncycles  = 40;
    private int    eventNumber = 0;
    
    public RFmonitor(String name) {
        super(name);
        
        this.getCcdb().setVariation("default");
        this.getCcdb().init(Arrays.asList(new String[]{"/calibration/eb/rf/config"}));

        this.setDetectorTabNames("adc","tdc","time");
        this.init(false);
    }

    
    @Override
    public void createHistos() {
        // create histograms
        int tdcMin = (int) (ncycles*rfbucket/tdcconv)/-100;
        int tdcMax = (int) (ncycles*3*rfbucket/tdcconv)+100;
        this.setNumberOfEvents(0);
        H1F summary = new H1F("summary","RF Difference",500, 0, rfbucket);
        summary.setTitleX("RF diff (ns)");
        summary.setTitleY("Counts");
        summary.setFillColor(5);
        DataGroup sum = new DataGroup(1,1);
        sum.addDataSet(summary, 0);
        this.setDetectorSummary(sum);
        H1F rf1 = new H1F("rf1","rf1", 400,0.,120000);
        rf1.setTitleX("RF1 tdc");
        rf1.setTitleY("Counts");
        rf1.setFillColor(33);
        H1F rf2 = new H1F("rf2","rf2", 400,0.,120000);
        rf2.setTitleX("RF2 tdc");
        rf2.setTitleY("Counts");
        rf2.setFillColor(36);
        H1F rfdiff = new H1F("rfdiff","rfdiff", (int)(rfbucket/tdcconv), 0, rfbucket);
        rfdiff.setTitleX("RF diff");
        rfdiff.setTitleY("Counts");
        F1D fdiff = new F1D("fdiff","[amp]*gaus(x,[mean],[sigma])", 0, rfbucket);
        fdiff.setParameter(0, 0);
        fdiff.setParameter(1, 0);
        fdiff.setParameter(2, 1.0);
        fdiff.setLineWidth(2);
        fdiff.setLineColor(2);
        fdiff.setOptStat("1111");
        H1F rfdiffAve = new H1F("rfdiffAve","rfdiffAve", 500, 0, rfbucket);
        rfdiffAve.setTitleX("RF diff");
        rfdiffAve.setTitleY("Counts");
        F1D fdiffAve = new F1D("fdiffAve","[amp]*gaus(x,[mean],[sigma])", 0, rfbucket);
        fdiffAve.setParameter(0, 0);
        fdiffAve.setParameter(1, 0);
        fdiffAve.setParameter(2, 1.0);
        fdiffAve.setLineWidth(2);
        fdiffAve.setLineColor(2);
        fdiffAve.setOptStat("1111");
        H1F rf1rawdiff = new H1F("rf1rawdiff","rf1rawdiff", 100, tdcMin, tdcMax);
        rf1rawdiff.setTitleX("RF1 diff");
        rf1rawdiff.setTitleY("Counts");
        F1D f1rawdiff = new F1D("f1rawdiff","[amp]*gaus(x,[mean],[sigma])", tdcMin, tdcMax);
        f1rawdiff.setParameter(0, 0);
        f1rawdiff.setParameter(1, 0);
        f1rawdiff.setParameter(2, 1.0);
        f1rawdiff.setLineWidth(2);
        f1rawdiff.setLineColor(2);
        f1rawdiff.setOptStat("1111");
        H1F rf2rawdiff = new H1F("rf2rawdiff","rf2rawdiff", 100, tdcMin, tdcMax);
        rf2rawdiff.setTitleX("RF2 diff");
        rf2rawdiff.setTitleY("Counts");
        F1D f2rawdiff = new F1D("f2rawdiff","[amp]*gaus(x,[mean],[sigma])", tdcMin, tdcMax);
        f2rawdiff.setParameter(0, 0);
        f2rawdiff.setParameter(1, 0);
        f2rawdiff.setParameter(2, 1.0);
        f2rawdiff.setLineWidth(2);
        f2rawdiff.setLineColor(2);
        f2rawdiff.setOptStat("1111");
        H2F rf1rawdiffrf1 = new H2F("rf1rawdiffrf1","rf1rawdiffrf1", 100,0.,120000, 25, tdcMin, tdcMax);
        rf1rawdiffrf1.setTitleX("RF1 tdc");
        rf1rawdiffrf1.setTitleY("RF1 diff");
        H2F rf2rawdiffrf2 = new H2F("rf2rawdiffrf2","rf2rawdiffrf2", 100,0.,120000, 25, tdcMin, tdcMax);
        rf2rawdiffrf2.setTitleX("RF2 tdc");
        rf2rawdiffrf2.setTitleY("RF2 diff");
        H1F rf1diff = new H1F("rf1diff","rf1diff", (int) ((int) ncycles*rfbucket), ncycles*rfbucket-2, ncycles*rfbucket+2);
        rf1diff.setTitleX("RF1 diff (ns)");
        rf1diff.setTitleY("Counts");
        F1D f1diff = new F1D("f1diff","[amp]*gaus(x,[mean],[sigma])", ncycles*rfbucket-2, ncycles*rfbucket+2);
        f1diff.setParameter(0, 0);
        f1diff.setParameter(1, 0);
        f1diff.setParameter(2, 1.0);
        f1diff.setLineWidth(2);
        f1diff.setLineColor(2);
        f1diff.setOptStat("1111");
        H1F rf2diff = new H1F("rf2diff","rf2diff", (int) ((int) ncycles*rfbucket), ncycles*rfbucket-2, ncycles*rfbucket+2);
        rf2diff.setTitleX("RF2 diff (ns)");
        rf2diff.setTitleY("Counts");
        F1D f2diff = new F1D("f2diff","[amp]*gaus(x,[mean],[sigma])", ncycles*rfbucket-2, ncycles*rfbucket+2);
        f2diff.setParameter(0, 0);
        f2diff.setParameter(1, 0);
        f2diff.setParameter(2, 1.0);
        f2diff.setLineWidth(2);
        f2diff.setLineColor(2);
        f2diff.setOptStat("1111");
        H2F timeRF1 = new H2F("timeRF1","timeRF1",100,0.,ncycles*rfbucket*2, 200, 0, rfbucket);
        timeRF1.setTitleX("RF1 (ns)");
        timeRF1.setTitleY("RF diff (ns)");
        H2F timeRF2 = new H2F("timeRF2","timeRF2",100,0.,ncycles*rfbucket*2, 200, 0, rfbucket);
        timeRF2.setTitleX("RF2 (ns)");
        timeRF2.setTitleY("RF diff (ns)");
        GraphErrors  rf1Timeline = new GraphErrors("rf1Timeline");
        rf1Timeline.setTitle("RF1 Timeline"); //  title
        rf1Timeline.setTitleX("Event Number"); // X axis title
        rf1Timeline.setTitleY("RF1");   // Y axis title
        rf1Timeline.setMarkerColor(44); // color from 0-9 for given palette
        rf1Timeline.setMarkerSize(5);  // size in points on the screen
        GraphErrors  rf2Timeline = new GraphErrors("rf2Timeline");
        rf2Timeline.setTitle("RF2 Timeline"); //  title
        rf2Timeline.setTitleX("Event Number"); // X axis title
        rf2Timeline.setTitleY("RF2");   // Y axis title
        rf2Timeline.setMarkerColor(44); // color from 0-9 for given palette
        rf2Timeline.setMarkerSize(5);  // size in points on the screen
        GraphErrors  rfTimeline = new GraphErrors("rfTimeline");
        rfTimeline.setTitle("timeline"); //  title
        rfTimeline.setTitleX("Event Number"); // X axis title
        rfTimeline.setTitleY("RF");   // Y axis title
        rfTimeline.setMarkerColor(44); // color from 0-9 for given palette
        rfTimeline.setMarkerSize(5);  // size in points on the screen
        GraphErrors  rfAveTimeline = new GraphErrors("rfAveTimeline");
        rfAveTimeline.setTitle("<RF> Timeline"); //  title
        rfAveTimeline.setTitleX("Event Number"); // X axis title
        rfAveTimeline.setTitleY("<RF>");   // Y axis title
        rfAveTimeline.setMarkerColor(44); // color from 0-9 for given palette
        rfAveTimeline.setMarkerSize(5);  // size in points on the screen
        H1F rf1difftmp = new H1F("rf1difftmp","rf1difftmp", (int) ((int) ncycles*rfbucket), ncycles*rfbucket-2, ncycles*rfbucket+2);
        H1F rf2difftmp = new H1F("rf2difftmp","rf2difftmp", (int) ((int) ncycles*rfbucket), ncycles*rfbucket-2, ncycles*rfbucket+2);
        H1F rfdifftmp = new H1F("rfdifftmp","rfdifftmp", 250, 0, rfbucket);
        H1F rfdiffAvetmp = new H1F("rfdiffAvetmp","rfdiffAvetmp", 500, 0, rfbucket);
        H1F rf1fADC = new H1F("rf1fADC","rf1fADC", 100,0.,400);
        rf1fADC.setTitleX("RF1 tdc");
        rf1fADC.setTitleY("Counts");
        rf1fADC.setFillColor(33);
        H1F rf2fADC = new H1F("rf2fADC","rf2fADC", 100,0.,400);
        rf2fADC.setTitleX("RF2 tdc");
        rf2fADC.setTitleY("Counts");
        rf2fADC.setFillColor(36);
        H1F rf1fADCadc = new H1F("rf1fADCadc","rf1fADCadc", 100,0.,60000);
        rf1fADCadc.setTitleX("RF1 adc");
        rf1fADCadc.setTitleY("Counts");
        rf1fADCadc.setFillColor(33);
        H1F rf2fADCadc = new H1F("rf2fADCadc","rf2fADCadc", 100,0.,60000);
        rf2fADCadc.setTitleX("RF2 adc");
        rf2fADCadc.setTitleY("Counts");
        rf2fADCadc.setFillColor(36);
        H1F rffADCdiff = new H1F("rffADCdiff","rffADCdiff", 400, 0, rfbucket);
        rffADCdiff.setTitleX("RF diff");
        rffADCdiff.setTitleY("Counts");
        H1F rffADCdifftmp = new H1F("rffADCdifftmp","rffADCdifftmp", 400, 0, rfbucket);
        rffADCdifftmp.setTitleX("RF diff");
        rffADCdifftmp.setTitleY("Counts");
        F1D ffADCdiff = new F1D("ffADCdiff","[amp]*gaus(x,[mean],[sigma])", 0, rfbucket);
        ffADCdiff.setParameter(0, 0);
        ffADCdiff.setParameter(1, 0);
        ffADCdiff.setParameter(2, 1.0);
        ffADCdiff.setLineWidth(2);
        ffADCdiff.setLineColor(2);
        ffADCdiff.setOptStat("1111");
        GraphErrors  rffADCTimeline = new GraphErrors("rffADCTimeline");
        rffADCTimeline.setTitle("RF fADC Timeline"); //  title
        rffADCTimeline.setTitleX("Event Number"); // X axis title
        rffADCTimeline.setTitleY("RF");   // Y axis title
        rffADCTimeline.setMarkerColor(22); // color from 0-9 for given palette
        rffADCTimeline.setMarkerSize(5);  // size in points on the screen

 
        DataGroup dg = new DataGroup(1,26);
        dg.addDataSet(rf1, 0);
        dg.addDataSet(rf1rawdiff,1);
        dg.addDataSet(f1rawdiff, 1);
        dg.addDataSet(rf1rawdiffrf1,2);
        dg.addDataSet(rf2, 3);
        dg.addDataSet(rf2rawdiff,4);
        dg.addDataSet(f2rawdiff, 4);
        dg.addDataSet(rf2rawdiffrf2,5);        
        dg.addDataSet(rfdiff, 6);
        dg.addDataSet(fdiff,  6);
        dg.addDataSet(rf1diff,7);
        dg.addDataSet(f1diff, 7);
        dg.addDataSet(rf2diff,8);
        dg.addDataSet(f2diff, 8);
        dg.addDataSet(rfdiffAve, 9);
        dg.addDataSet(fdiffAve,  9);
        dg.addDataSet(timeRF1, 10);
        dg.addDataSet(timeRF2, 11);
        dg.addDataSet(rf1Timeline, 12);
        dg.addDataSet(rf2Timeline, 13);
        dg.addDataSet(rfTimeline, 14);
        dg.addDataSet(rfAveTimeline, 15);
        dg.addDataSet(rf1difftmp,16);
        dg.addDataSet(rf2difftmp,17);
        dg.addDataSet(rfdifftmp,18);
        dg.addDataSet(rfdiffAvetmp,19);
        dg.addDataSet(rf1fADC,20);
        dg.addDataSet(rf2fADC,21);
        dg.addDataSet(rf1fADCadc,22);
        dg.addDataSet(rf2fADCadc,23);
        dg.addDataSet(rffADCdiff,24);
        dg.addDataSet(rffADCdifftmp,24);
        dg.addDataSet(ffADCdiff,24);
        dg.addDataSet(rffADCTimeline, 25);
        this.getDataGroup().add(dg, 0,0,0);
    }
        
    @Override
    public void plotHistos() {
        // initialize canvas and plot histograms
        this.getDetectorCanvas().getCanvas("tdc").divide(3, 2);
        this.getDetectorCanvas().getCanvas("tdc").setGridX(false);
        this.getDetectorCanvas().getCanvas("tdc").setGridY(false);
        this.getDetectorCanvas().getCanvas("tdc").cd(0);
        this.getDetectorCanvas().getCanvas("tdc").draw(this.getDataGroup().getItem(0,0,0).getH1F("rf1"));
        this.getDetectorCanvas().getCanvas("tdc").cd(1);
        this.getDetectorCanvas().getCanvas("tdc").draw(this.getDataGroup().getItem(0,0,0).getH1F("rf1rawdiff"));
        this.getDetectorCanvas().getCanvas("tdc").draw(this.getDataGroup().getItem(0,0,0).getF1D("f1rawdiff"),"same");
        this.getDetectorCanvas().getCanvas("tdc").cd(2);
        this.getDetectorCanvas().getCanvas("tdc").getPad(2).getAxisZ().setLog(true);
        this.getDetectorCanvas().getCanvas("tdc").draw(this.getDataGroup().getItem(0,0,0).getH2F("rf1rawdiffrf1"));
        this.getDetectorCanvas().getCanvas("tdc").cd(3);
        this.getDetectorCanvas().getCanvas("tdc").draw(this.getDataGroup().getItem(0,0,0).getH1F("rf2"));
        this.getDetectorCanvas().getCanvas("tdc").cd(4);
        this.getDetectorCanvas().getCanvas("tdc").draw(this.getDataGroup().getItem(0,0,0).getH1F("rf2rawdiff"));
        this.getDetectorCanvas().getCanvas("tdc").draw(this.getDataGroup().getItem(0,0,0).getF1D("f2rawdiff"),"same");
        this.getDetectorCanvas().getCanvas("tdc").cd(5);
        this.getDetectorCanvas().getCanvas("tdc").getPad(5).getAxisZ().setLog(true);
        this.getDetectorCanvas().getCanvas("tdc").draw(this.getDataGroup().getItem(0,0,0).getH2F("rf2rawdiffrf2"));
        this.getDetectorCanvas().getCanvas("tdc").update();
        this.getDetectorCanvas().getCanvas("time").divide(3, 2);
        this.getDetectorCanvas().getCanvas("time").setGridX(false);
        this.getDetectorCanvas().getCanvas("time").setGridY(false);
        this.getDetectorCanvas().getCanvas("time").cd(0);
        this.getDetectorCanvas().getCanvas("time").draw(this.getDataGroup().getItem(0,0,0).getH1F("rfdiff"));
        this.getDetectorCanvas().getCanvas("time").draw(this.getDataGroup().getItem(0,0,0).getF1D("fdiff"),"same");
        this.getDetectorCanvas().getCanvas("time").cd(1);
        this.getDetectorCanvas().getCanvas("time").draw(this.getDataGroup().getItem(0,0,0).getH1F("rf1diff"));
        this.getDetectorCanvas().getCanvas("time").draw(this.getDataGroup().getItem(0,0,0).getF1D("f1diff"),"same");
        this.getDetectorCanvas().getCanvas("time").cd(2);
        this.getDetectorCanvas().getCanvas("time").draw(this.getDataGroup().getItem(0,0,0).getH1F("rf2diff"));
        this.getDetectorCanvas().getCanvas("time").draw(this.getDataGroup().getItem(0,0,0).getF1D("f2diff"),"same");
        this.getDetectorCanvas().getCanvas("time").cd(3);
        this.getDetectorCanvas().getCanvas("time").draw(this.getDataGroup().getItem(0,0,0).getH1F("rfdiffAve"));
        this.getDetectorCanvas().getCanvas("time").draw(this.getDataGroup().getItem(0,0,0).getF1D("fdiffAve"),"same");
        this.getDetectorCanvas().getCanvas("time").cd(4);
        this.getDetectorCanvas().getCanvas("time").draw(this.getDataGroup().getItem(0,0,0).getH2F("timeRF1"));
        this.getDetectorCanvas().getCanvas("time").cd(5);
        this.getDetectorCanvas().getCanvas("time").draw(this.getDataGroup().getItem(0,0,0).getH2F("timeRF2"));
//        this.getDetectorCanvas().getCanvas("timeline").divide(2, 2);
//        this.getDetectorCanvas().getCanvas("timeline").setGridX(false);
//        this.getDetectorCanvas().getCanvas("timeline").setGridY(false);
        this.getDetectorCanvas().getCanvas("adc").divide(3, 2);
        this.getDetectorCanvas().getCanvas("adc").setGridX(false);
        this.getDetectorCanvas().getCanvas("adc").setGridY(false);
        this.getDetectorCanvas().getCanvas("adc").cd(0);
        this.getDetectorCanvas().getCanvas("adc").draw(this.getDataGroup().getItem(0,0,0).getH1F("rf1fADC"));
        this.getDetectorCanvas().getCanvas("adc").cd(1);
        this.getDetectorCanvas().getCanvas("adc").draw(this.getDataGroup().getItem(0,0,0).getH1F("rf1fADCadc"));
        this.getDetectorCanvas().getCanvas("adc").cd(2);
        this.getDetectorCanvas().getCanvas("adc").draw(this.getDataGroup().getItem(0,0,0).getH1F("rffADCdiff"));
        this.getDetectorCanvas().getCanvas("adc").draw(this.getDataGroup().getItem(0,0,0).getF1D("ffADCdiff"),"same");
        this.getDetectorCanvas().getCanvas("adc").cd(3);
        this.getDetectorCanvas().getCanvas("adc").draw(this.getDataGroup().getItem(0,0,0).getH1F("rf2fADC"));
        this.getDetectorCanvas().getCanvas("adc").cd(4);
        this.getDetectorCanvas().getCanvas("adc").draw(this.getDataGroup().getItem(0,0,0).getH1F("rf2fADCadc"));
    }

    @Override
    public void processEvent(DataEvent event) {
        
        if (event.hasBank("RUN::config")) {
            DataBank head = event.getBank("RUN::config");
            int runNumber = head.getInt("run", 0);
            this.eventNumber = head.getInt("event", 0);
            rfConfig = this.getCcdb().getConstants(runNumber, "/calibration/eb/rf/config");
            double run_tdc2Time = rfConfig.getDoubleValue("tdc2time", 1, 1, 1);
            double run_rfbucket = rfConfig.getDoubleValue("clock", 1, 1, 1);
            int run_ncycles = rfConfig.getIntValue("cycles", 1, 1, 1);
            if (run_tdc2Time != this.tdcconv || run_rfbucket != this.rfbucket || run_ncycles != this.ncycles) {
                this.tdcconv = run_tdc2Time;
                this.rfbucket = run_rfbucket;
                this.ncycles = run_ncycles;
                this.resetEventListener();
                System.out.println("RF config parameter changed to: \n\t tdc2time = " + this.tdcconv + "\n\t rf bucket = " + this.rfbucket + "\n\t n. of cycles = " + this.ncycles);
            }
//            System.out.println();
        }
        // process event info and save into data group
        ArrayList<Integer> rf1 = new ArrayList();
        ArrayList<Integer> rf2 = new ArrayList();
        if(event.hasBank("RF::tdc")==true){
            DataBank  bank = event.getBank("RF::tdc");
            int rows = bank.rows();
            for(int i = 0; i < rows; i++){
                int    sector = bank.getByte("sector",i);
                int     layer = bank.getByte("layer",i);
                int      comp = bank.getShort("component",i);
                int       TDC = bank.getInt("TDC",i);
                int     order = bank.getByte("order",i); 
//                if(TDC>29000) continue;
                if(order==2) {
                    if(comp==1) {
                        this.getDataGroup().getItem(0,0,0).getH1F("rf1").fill(TDC*1.0);
                        rf1.add(TDC);
                    }
                    else {
                        this.getDataGroup().getItem(0,0,0).getH1F("rf2").fill(TDC*1.0);
                        rf2.add(TDC);
                    }
                }
            }
        }
//        System.out.println(rf1.size() + " " +rf2.size());
        for(int i=0; i<rf1.size()-1; i++) {
            this.getDataGroup().getItem(0,0,0).getH1F("rf1rawdiff").fill((rf1.get(i+1)-rf1.get(i))*1.0);
            this.getDataGroup().getItem(0,0,0).getH2F("rf1rawdiffrf1").fill(rf1.get(i),(rf1.get(i+1)-rf1.get(i))*1.0);
            this.getDataGroup().getItem(0,0,0).getH1F("rf1diff").fill((rf1.get(i+1)-rf1.get(i))*tdcconv);
            this.getDataGroup().getItem(0,0,0).getH1F("rf1difftmp").fill((rf1.get(i+1)-rf1.get(i))*tdcconv);
        }
        for(int i=0; i<rf2.size()-1; i++) {
            this.getDataGroup().getItem(0,0,0).getH1F("rf2rawdiff").fill((rf2.get(i+1)-rf2.get(i))*1.0);
            this.getDataGroup().getItem(0,0,0).getH2F("rf2rawdiffrf2").fill(rf2.get(i),(rf2.get(i+1)-rf2.get(i))*1.0);
            this.getDataGroup().getItem(0,0,0).getH1F("rf2diff").fill((rf2.get(i+1)-rf2.get(i))*tdcconv);
            this.getDataGroup().getItem(0,0,0).getH1F("rf2difftmp").fill((rf2.get(i+1)-rf2.get(i))*tdcconv);
        }

        if(rf1.size()==rf2.size() || true) {
//            for(int i=0; i<rf1.size(); i++) System.out.println(rf1.get(i));
            int npairs = Math.min(rf1.size(),rf2.size());
            for(int i=0; i<npairs; i++) {
                double rfTimei = ((rf1.get(i)-rf2.get(i))*tdcconv + (100*rfbucket)) % rfbucket;
                this.getDataGroup().getItem(0,0,0).getH1F("rfdiff").fill(rfTimei);
                this.getDataGroup().getItem(0,0,0).getH1F("rfdifftmp").fill(rfTimei);
            }
            double rfTime1 = 0;
            double rfTime2 = 0;
            for(int i=0; i<rf1.size(); i++) {
                rfTime1 += ((rf1.get(i)*tdcconv) % (ncycles*rfbucket));
//                System.out.println(i + " " + rf1.get(i)*tdc2Time + " " + ((rf1.get(i)*tdc2Time) % (ncycles*rfbucket)));
            }
            for(int i=0; i<rf2.size(); i++) {
                rfTime2 += ((rf2.get(i)*tdcconv) % (ncycles*rfbucket));
            }
            rfTime1 /=rf1.size();
            rfTime2 /=rf2.size();            
//            System.out.println("aaa " + (rfTime1-rfTime2));
            double rfTime = (rfTime1-rfTime2 + 1000*rfbucket) % rfbucket;
            this.getDetectorSummary().getH1F("summary").fill(rfTime);
            this.getDataGroup().getItem(0,0,0).getH1F("rfdiffAve").fill(rfTime);
            this.getDataGroup().getItem(0,0,0).getH1F("rfdiffAvetmp").fill(rfTime);
            this.getDataGroup().getItem(0,0,0).getH2F("timeRF1").fill(rfTime1,rfTime);
            this.getDataGroup().getItem(0,0,0).getH2F("timeRF2").fill(rfTime2,rfTime);            
        }
//        if(this.getDataGroup().getItem(0,0,0).getH1F("rfdiffAvetmp").getEntries()>=500){
//            H1F rf1diff   = this.getDataGroup().getItem(0,0,0).getH1F("rf1difftmp");
//            H1F rf2diff   = this.getDataGroup().getItem(0,0,0).getH1F("rf2difftmp");
//            H1F rfdiff    = this.getDataGroup().getItem(0,0,0).getH1F("rfdifftmp");
//            H1F rfdiffAve = this.getDataGroup().getItem(0,0,0).getH1F("rfdiffAvetmp");
//            this.getDataGroup().getItem(0,0,0).getGraph("rf1Timeline").addPoint(this.eventNumber, rf1diff.getMean() , 0, rf1diff.getRMS()/Math.sqrt(rf1diff.getEntries()));
//            this.getDataGroup().getItem(0,0,0).getGraph("rf2Timeline").addPoint(this.eventNumber, rf2diff.getMean() , 0, rf2diff.getRMS()/Math.sqrt(rf2diff.getEntries()));
//            this.getDataGroup().getItem(0,0,0).getGraph("rfTimeline").addPoint(this.eventNumber, rfdiff.getMean() , 0, rfdiff.getRMS()/Math.sqrt(rfdiff.getEntries()));
//            this.getDataGroup().getItem(0,0,0).getGraph("rfAveTimeline").addPoint(this.eventNumber, rfdiffAve.getMean() , 0, rfdiffAve.getRMS()/Math.sqrt(rfdiffAve.getEntries()));
//            if(this.getDataGroup().getItem(0,0,0).getGraph("rf1Timeline").getVectorX().size()==2) {
//                this.getDetectorCanvas().getCanvas("timeline").cd(0);
//                this.getDetectorCanvas().getCanvas("timeline").draw(this.getDataGroup().getItem(0,0,0).getGraph("rf1Timeline"));
//                this.getDetectorCanvas().getCanvas("timeline").cd(1);
//                this.getDetectorCanvas().getCanvas("timeline").draw(this.getDataGroup().getItem(0,0,0).getGraph("rf2Timeline"));
//                this.getDetectorCanvas().getCanvas("timeline").cd(2);
//                this.getDetectorCanvas().getCanvas("timeline").draw(this.getDataGroup().getItem(0,0,0).getGraph("rfTimeline"));
//                this.getDetectorCanvas().getCanvas("timeline").cd(3);
//                this.getDetectorCanvas().getCanvas("timeline").draw(this.getDataGroup().getItem(0,0,0).getGraph("rfAveTimeline"));               
//            }
//            rf1diff.reset();
//            rf2diff.reset();
//            rfdiff.reset();
//            rfdiffAve.reset();
//        }
        if(event.hasBank("RF::adc")==true){
            DataBank  bank = event.getBank("RF::adc");
            int rows = bank.rows();
            double rf1time=0;
            double rf2time=0;
            double rf1adc=0;
            double rf2adc=0;
            for(int i = 0; i < rows; i++){
                int    sector = bank.getByte("sector",i);
                int     layer = bank.getByte("layer",i);
                int      comp = bank.getShort("component",i);
                int       adc = bank.getInt("ADC",i);
                double   time = bank.getFloat("time",i);
                int     order = bank.getByte("order",i); 
                if(order==0) {
                    if(comp==3) {
                        this.getDataGroup().getItem(0,0,0).getH1F("rf1fADC").fill(time);
                        if(time>0) this.getDataGroup().getItem(0,0,0).getH1F("rf1fADCadc").fill(adc);
                        rf1time = time;
                        rf1adc  = adc;
                    }
                    else {
                        this.getDataGroup().getItem(0,0,0).getH1F("rf2fADC").fill(time);
                        if(time>0) this.getDataGroup().getItem(0,0,0).getH1F("rf2fADCadc").fill(adc);
                        rf2time = time;
                        rf2adc  = adc;
                     }
                }
            }
            if(rf1time>0 && rf2time>0) {
//                System.out.println(rf1time + " " + rf2time);
                double rftime = (rf1time-rf2time + 100*rfbucket) % rfbucket;
                this.getDataGroup().getItem(0,0,0).getH1F("rffADCdiff").fill(rftime);
                this.getDataGroup().getItem(0,0,0).getH1F("rffADCdifftmp").fill(rftime);
            }
        }
//        if(this.getDataGroup().getItem(0,0,0).getH1F("rffADCdifftmp").getEntries()>=500){
//            H1F rfdiff    = this.getDataGroup().getItem(0,0,0).getH1F("rffADCdifftmp");
//            this.getDataGroup().getItem(0,0,0).getGraph("rffADCTimeline").addPoint(this.eventNumber, rfdiff.getMean() , 0, rfdiff.getRMS()/Math.sqrt(rfdiff.getEntries()));
//            if(this.getDataGroup().getItem(0,0,0).getGraph("rffADCTimeline").getVectorX().size()==2) {
//                this.getDetectorCanvas().getCanvas("RF fADC").cd(5);
//                this.getDetectorCanvas().getCanvas("RF fADC").draw(this.getDataGroup().getItem(0,0,0).getGraph("rffADCTimeline"));
//            }
//            rfdiff.reset();
//        }
    }

    @Override
    public void analysisUpdate() {
//        System.out.println("Updating RF");
        this.fitRF(this.getDataGroup().getItem(0,0,0).getH1F("rf1rawdiff"),this.getDataGroup().getItem(0,0,0).getF1D("f1rawdiff"));
        this.fitRF(this.getDataGroup().getItem(0,0,0).getH1F("rf2rawdiff"),this.getDataGroup().getItem(0,0,0).getF1D("f2rawdiff"));
        this.fitRF(this.getDataGroup().getItem(0,0,0).getH1F("rf1diff"),   this.getDataGroup().getItem(0,0,0).getF1D("f1diff"));
        this.fitRF(this.getDataGroup().getItem(0,0,0).getH1F("rf2diff"),   this.getDataGroup().getItem(0,0,0).getF1D("f2diff"));
        this.fitRF(this.getDataGroup().getItem(0,0,0).getH1F("rfdiff"),    this.getDataGroup().getItem(0,0,0).getF1D("fdiff"));
        this.fitRF(this.getDataGroup().getItem(0,0,0).getH1F("rfdiffAve"), this.getDataGroup().getItem(0,0,0).getF1D("fdiffAve"));
        double rfMean = this.getDataGroup().getItem(0,0,0).getH1F("rfdiffAve").getMean();
        this.fitRF(this.getDataGroup().getItem(0,0,0).getH1F("rffADCdiff"), this.getDataGroup().getItem(0,0,0).getF1D("ffADCdiff"));
    }

    public void fitRF(H1F hirf, F1D f1rf) {
        double mean  = hirf.getDataX(hirf.getMaximumBin());
        double amp   = hirf.getBinContent(hirf.getMaximumBin());
        double sigma = hirf.getRMS();
        f1rf.setParameter(0, amp);
        f1rf.setParameter(1, mean);
        f1rf.setParameter(2, 0.02);
        f1rf.setRange(mean-3.*sigma,mean+3.*sigma);
        DataFitter.fit(f1rf, hirf, "Q"); //No options uses error for sigma        
    }
    
}
