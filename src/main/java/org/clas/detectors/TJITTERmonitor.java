package org.clas.detectors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.clas.viewer.DetectorMonitor;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.utils.groups.IndexedList;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author devita
 */

public class TJITTERmonitor  extends DetectorMonitor {        
    
    
    private int ctofPaddles = 48;
    private int ftofPaddles = 62;
    IndexedTable jitterConfig = null;

    
    public TJITTERmonitor(String name) {
        super(name);
        
        this.setDetectorTabNames("phase");
        this.init(false);
        this.getCcdb().setVariation("default");
        this.getCcdb().init(Arrays.asList(new String[]{"/calibration/ftof/time_jitter"}));
    }

    @Override
    public void createHistos() {
        // initialize canvas and create histograms
        this.setNumberOfEvents(0);
        H1F summary = new H1F("summary","summary",200,-40,60);
        summary.setTitleX("TDC phase (ns)");
        summary.setTitleY("Counts");
        summary.setTitle("TDC phase");
        summary.setFillColor(2);
        DataGroup sum = new DataGroup(1,1);
        sum.addDataSet(summary, 0);
        this.setDetectorSummary(sum);
        
        H2F hi_ctof_tlphase = new H2F("hi_ctof_tlphase", "hi_ctof_tlphase", 6, 0, 6, 100, 0.0, 100.0); 
        hi_ctof_tlphase.setTitleX("Trigger Phase"); 
        hi_ctof_tlphase.setTitleY("#DeltaT (ns)");
        hi_ctof_tlphase.setTitle("CTOF Upstream PMTs");
        H2F hi_ctof_trphase = new H2F("hi_ctof_trphase", "hi_ctof_trphase", 6, 0, 6, 100, 0.0, 100.0); 
        hi_ctof_trphase.setTitleX("Trigger Phase"); 
        hi_ctof_trphase.setTitleY("#DeltaT (ns)");
        hi_ctof_trphase.setTitle("CTOF Downstream PMTs");

        H2F hi_ftof_tlphase = new H2F("hi_ftof_tlphase", "hi_ftof_tlphase", 6, 0, 6, 100, -20.0, 80.0); 
        hi_ftof_tlphase.setTitleX("Trigger Phase"); 
        hi_ftof_tlphase.setTitleY("#DeltaT (ns)");
        hi_ftof_tlphase.setTitle("FTOF-1B Left PMTs");
        H2F hi_ftof_trphase = new H2F("hi_ftof_trphase", "hi_ftof_trphase", 6, 0, 6, 100, -20.0, 80.0); 
        hi_ftof_trphase.setTitleX("Trigger Phase"); 
        hi_ftof_trphase.setTitleY("#DeltaT (ns)");
        hi_ftof_trphase.setTitle("FTOF-1B Right PMTs");

        DataGroup dg = new DataGroup(2,2);
        dg.addDataSet(hi_ctof_tlphase, 0);
        dg.addDataSet(hi_ctof_trphase, 2);
        dg.addDataSet(hi_ftof_tlphase, 1);
        dg.addDataSet(hi_ftof_trphase, 3);
        this.getDataGroup().add(dg, 0,0,0);

    }
        
    @Override
    public void plotHistos() {        
        // plotting histos
        this.getDetectorCanvas().getCanvas("phase").divide(2, 2);
        this.getDetectorCanvas().getCanvas("phase").setGridX(false);
        this.getDetectorCanvas().getCanvas("phase").setGridY(false);
        this.getDetectorCanvas().getCanvas("phase").cd(0);
        this.getDetectorCanvas().getCanvas("phase").getPad(0).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("phase").draw(this.getDataGroup().getItem(0,0,0).getH2F("hi_ctof_tlphase"));
        this.getDetectorCanvas().getCanvas("phase").cd(1);
        this.getDetectorCanvas().getCanvas("phase").getPad(1).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("phase").draw(this.getDataGroup().getItem(0,0,0).getH2F("hi_ftof_tlphase"));
        this.getDetectorCanvas().getCanvas("phase").cd(2);
        this.getDetectorCanvas().getCanvas("phase").getPad(2).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("phase").draw(this.getDataGroup().getItem(0,0,0).getH2F("hi_ctof_trphase"));
        this.getDetectorCanvas().getCanvas("phase").cd(3);
        this.getDetectorCanvas().getCanvas("phase").getPad(3).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("phase").draw(this.getDataGroup().getItem(0,0,0).getH2F("hi_ftof_trphase"));
        this.getDetectorCanvas().getCanvas("phase").update();
    }

    @Override
    public void processEvent(DataEvent event) {
        
        DataBank recRun    = null;
        DataBank ctofADC = null;
        DataBank ctofTDC = null;
        DataBank ftofADC = null;
        DataBank ftofTDC = null;
        if(event.hasBank("RUN::config"))            recRun      = event.getBank("RUN::config");
        if(event.hasBank("CTOF::adc"))              ctofADC     = event.getBank("CTOF::adc");
        if(event.hasBank("CTOF::tdc"))              ctofTDC     = event.getBank("CTOF::tdc");
        if(event.hasBank("FTOF::adc"))              ftofADC     = event.getBank("FTOF::adc");
        if(event.hasBank("FTOF::tdc"))              ftofTDC     = event.getBank("FTOF::tdc");
       
	int triggerPhase0=0;
	int triggerPhase=0;
        if(recRun!=null) {
            int runNumber  = recRun.getInt("run", 0);
            long timestamp = recRun.getLong("timestamp",0);    
            this.jitterConfig = this.getCcdb().getConstants(runNumber, "/calibration/ftof/time_jitter");
            this.period  = jitterConfig.getDoubleValue("period",0,0,0);
            this.phase   = jitterConfig.getIntValue("phase",0,0,0);
            this.ncycles = jitterConfig.getIntValue("cycles",0,0,0);           
//            System.out.println(period + phase + ncycles + " " + timestamp + " " + triggerPhase0);
            if(this.ncycles>0){
                triggerPhase0 = (int) ((timestamp)%this.ncycles); // TI derived phase correction due to TDC and FADC clock differences
                triggerPhase  = (int) ((timestamp+this.phase)%this.ncycles); // TI derived phase correction due to TDC and FADC clock differences
            }
        }
        if(ctofADC!=null && ctofTDC!=null) {
	    IndexedList<ArrayList<Integer>> tdcs = new IndexedList<>(3);
	    IndexedList<ArrayList<Double>>  adcs = new IndexedList<>(3);
	    for(int i=0; i<ctofADC.rows(); i++) {
	        int paddle = ctofADC.getShort("component",i);
                int order  = ctofADC.getByte("order",i);
	        int icomp  = (paddle-1)*2+order+1;
	        double adct = (double) ctofADC.getFloat("time",i);
	        if(adct>0) {
                    if(!adcs.hasItem(1,1,icomp)) adcs.add(new ArrayList<>(),1,1,icomp);
                    adcs.getItem(1,1,icomp).add(adct);
		 }
	    }
           for (int i = 0; i < ctofTDC.rows(); i++) {
                int paddle = ctofTDC.getShort("component", i);
                int order = ctofTDC.getByte("order", i) - 2;
                int icomp = (paddle - 1) * 2 + order + 1;
                int tdc = ctofTDC.getInt("TDC", i);
                if (tdc > 0) {
                    if (!tdcs.hasItem(1, 1, icomp)) {
                        tdcs.add(new ArrayList<>(), 1, 1, icomp);
                    }
                    tdcs.getItem(1, 1, icomp).add(tdc);
                }
            }
            for (int icomp = 1; icomp < this.ctofPaddles * 2 + 1; icomp++) {
                if (tdcs.hasItem(1, 1, icomp) && adcs.hasItem(1, 1, icomp)) {
                    List<Integer> listTDC = new ArrayList<>();
                    List<Double> listADC = new ArrayList<>();
                    listTDC = tdcs.getItem(1, 1, icomp);
                    listADC = adcs.getItem(1, 1, icomp);
                    for (int iadc = 0; iadc < listADC.size(); iadc++) {
                        for (int itdc = 0; itdc < listTDC.size(); itdc++) {
                            double adc = listADC.get(iadc);
                            int tdc = listTDC.get(itdc);
			    if(icomp%2 ==0) this.getDataGroup().getItem(0,0,0).getH2F("hi_ctof_tlphase").fill(triggerPhase0,tdc*tdcconv-adc);
                            else            this.getDataGroup().getItem(0,0,0).getH2F("hi_ctof_trphase").fill(triggerPhase0,tdc*tdcconv-adc);
			}
                    }
		}
	    }
        }
        if (ftofADC != null && ftofTDC != null) {
            IndexedList<ArrayList<Integer>> tdcs = new IndexedList<ArrayList<Integer>>(3);
            IndexedList<ArrayList<Double>>  adcs = new IndexedList<ArrayList<Double>>(3);
            for (int i = 0; i < ftofADC.rows(); i++) {
                int sector = ftofADC.getByte("sector", i);
                int layer  = ftofADC.getByte("layer", i);
                int paddle = ftofADC.getShort("component", i);
                int order = ftofADC.getByte("order", i);
                int icomp = (paddle - 1) * 2 + order + 1;
                double adct = (double) ftofADC.getFloat("time", i);
                if (adct > 0) {
                    if (!adcs.hasItem(sector,layer, icomp)) adcs.add(new ArrayList<Double>(), sector,layer, icomp);
                    adcs.getItem(sector,layer, icomp).add(adct);
                }
            }
            for (int i = 0; i < ftofTDC.rows(); i++) {
                int sector = ftofTDC.getByte("sector", i);
                int layer = ftofTDC.getByte("layer", i);
                int paddle = ftofTDC.getShort("component", i);
                int order = ftofTDC.getByte("order", i) - 2;
                int icomp = (paddle - 1) * 2 + order + 1;
                int tdc = ftofTDC.getInt("TDC", i);
                if (tdc > 0 ) {
	           if(!tdcs.hasItem(sector,layer,icomp)) tdcs.add(new ArrayList<Integer>(),sector,layer,icomp);
                   tdcs.getItem(sector,layer,icomp).add(tdc);
                }
            }
            for(int isect=1; isect<=6; isect++) {
                for(int icomp=1; icomp<this.ftofPaddles*2+1; icomp++) {
                    if(tdcs.hasItem(isect,2,icomp) && adcs.hasItem(isect,2,icomp) && icomp<30 && isect!=2) {
                       List<Integer> listTDC = new ArrayList<Integer>();
                       List<Double>  listADC = new ArrayList<Double>();
                       listTDC = tdcs.getItem(isect,2,icomp);
                       listADC = adcs.getItem(isect,2,icomp);
                       for(int iadc=0; iadc<listADC.size(); iadc++) {
                            for(int itdc=0; itdc<listTDC.size(); itdc++) {
                                double adc = listADC.get(iadc);
                                int tdc = listTDC.get(itdc);
                                if(icomp%2 ==0) this.getDataGroup().getItem(0,0,0).getH2F("hi_ftof_tlphase").fill(triggerPhase0,tdc*tdcconv-adc);
                                else            this.getDataGroup().getItem(0,0,0).getH2F("hi_ftof_trphase").fill(triggerPhase0,tdc*tdcconv-adc);
                                this.getDetectorSummary().getH1F("summary").fill(tdc*tdcconv-adc-triggerPhase*period);
                            }
                        }
                    }
                }
	    }
        }
    }

    @Override
    public void analysisUpdate() {

    }


}
