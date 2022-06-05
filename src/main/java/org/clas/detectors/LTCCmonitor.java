package org.clas.detectors;

import org.clas.viewer.DetectorMonitor;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author devita
 */

public class LTCCmonitor  extends DetectorMonitor {        
    
    public LTCCmonitor(String name) {
        super(name);

        this.setDetectorTabNames("adc", "adcTime", "tdc");
        this.init(false);
    }

    @Override
    public void createHistos() {
        // initialize canvas and create histograms
        this.setNumberOfEvents(0);
        this.getDetectorCanvas().getCanvas("adc").divide(2, 2);
        this.getDetectorCanvas().getCanvas("adc").setGridX(false);
        this.getDetectorCanvas().getCanvas("adc").setGridY(false);
        this.getDetectorCanvas().getCanvas("adcTime").divide(2, 1);
        this.getDetectorCanvas().getCanvas("adcTime").setGridX(false);
        this.getDetectorCanvas().getCanvas("adcTime").setGridY(false);
        this.getDetectorCanvas().getCanvas("tdc").divide(2, 2);
        this.getDetectorCanvas().getCanvas("tdc").setGridX(false);
        this.getDetectorCanvas().getCanvas("tdc").setGridY(false);
        
        H1F summary = new H1F("summary","summary",6,0.5,6.5);
        summary.setTitleX("sector");
        summary.setTitleY("LTCC hits");
        summary.setTitle("LTCC");
        summary.setFillColor(37);
        DataGroup sum = new DataGroup(1,1);
        sum.addDataSet(summary, 0);
        this.setDetectorSummary(sum);
        
        H2F occADC = new H2F("occADC", "occADC", 12, 0.5, 6.5, 18, 0.5, 18.5);
        occADC.setTitleY("PMT");
        occADC.setTitleX("sector (left-right combined)");
        occADC.setTitle("Raw ADC Occupancy");
        H2F occTDC = new H2F("occTDC", "occTDC", 12, 0.5, 6.5, 18, 0.5, 18.5);
        occTDC.setTitleY("PMT left");
        occTDC.setTitleX("sector (left-right combined)");
        H2F occADCref = new H2F("occADCref", "occADCref", 12, 0.5, 6.5, 18, 0.5, 18.5);
        occADCref.setTitleY("PMT");
        occADCref.setTitleX("sector (left-right combined)");
        
        for(int ibin=0; ibin<occADCref.getDataBufferSize(); ibin++) occADCref.setDataBufferBin(ibin, (float) 1.0);
        H2F occADCnorm = new H2F("occADCnorm", "occADCnorm", 12, 0.5, 6.5, 18, 0.5, 18.5);
        occADCnorm.setTitleY("PMT");
        occADCnorm.setTitleX("sector (left-right combined)");
        occADCnorm.setTitle("Normalized ADC Occupancy");
        
        H2F occTDCref = new H2F("occTDCref", "occTDCref", 12, 0.5, 6.5, 18, 0.5, 18.5);
        occTDCref.setTitleY("PMT");
        occTDCref.setTitleX("sector (left-right combined)");
        for(int ibin=0; ibin<occTDCref.getDataBufferSize(); ibin++) occTDCref.setDataBufferBin(ibin, (float) 1.0);
        H2F occTDCnorm = new H2F("occTDCnorm", "occTDCnorm", 12, 0.5, 6.5, 18, 0.5, 18.5);
        occTDCnorm.setTitleY("PMT");
        occTDCnorm.setTitleX("sector (left-right combined)");
        occTDCnorm.setTitle("Normalized TDC Occupancy");
        
        H2F adcL = new H2F("adcL", "adcL", 100, 0, 5000, 108, 0.5, 108.5);
        adcL.setTitleX("ADC left");
        adcL.setTitleY("PMT (all sectors combined)");
        H2F fadcL_time = new H2F("fadcL_time", "fadcL_time", 80, 0, 400, 108, 0.5, 108.5);
        fadcL_time.setTitleX("FADC left timing");
        fadcL_time.setTitleY("PMT (all sectors combined)");
        H2F tdcL = new H2F("tdcL", "tdcL", 400, 0, 40000, 108, 0.5, 108.5);
        tdcL.setTitleX("TDC left time");
        tdcL.setTitleY("PMT (all sectors combined)");

        H2F adcR = new H2F("adcR", "adcR", 100, 0, 5000, 108, 0.5, 108.5);
        adcR.setTitleX("ADC right");
        adcR.setTitleY("PMT (all sectors combined)");
        H2F fadcR_time = new H2F("fadcR_time", "fadcR_time", 80, 0, 400, 108, 0.5, 108.5);
        fadcR_time.setTitleX("FADC right timing");
        fadcR_time.setTitleY("PMT (all sectors combined)");  
        H2F tdcR = new H2F("tdcR", "tdcR", 400, 0, 40000, 108, 0.5, 108.5);
        tdcR.setTitleX("TDC right time");
        tdcR.setTitleY("PMT (all sectors combined)");
        
        
        DataGroup dg = new DataGroup(1,12);
        dg.addDataSet(occADC, 0);
        dg.addDataSet(occADCnorm, 1);
        dg.addDataSet(occADCref, 2);
        dg.addDataSet(adcL, 3);
        dg.addDataSet(adcR, 4);
        dg.addDataSet(fadcL_time, 5);
        dg.addDataSet(fadcR_time, 6);
        dg.addDataSet(occTDC, 7);
        dg.addDataSet(occTDCnorm, 8);
        dg.addDataSet(occTDCref, 9);
        dg.addDataSet(tdcL, 10);
        dg.addDataSet(tdcR, 11);
        this.getDataGroup().add(dg,0,0,0);
    }
 
    @Override
    public void plotHistos() {        
        // plotting histos
        this.getDetectorCanvas().getCanvas("adc").cd(0);
        this.getDetectorCanvas().getCanvas("adc").getPad(0).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("adc").draw(this.getDataGroup().getItem(0,0,0).getH2F("occADC"));
        this.getDetectorCanvas().getCanvas("adc").cd(1);
        this.getDetectorCanvas().getCanvas("adc").getPad(1).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("adc").draw(this.getDataGroup().getItem(0,0,0).getH2F("occADCnorm"));
        this.getDetectorCanvas().getCanvas("adc").cd(2);
        this.getDetectorCanvas().getCanvas("adc").getPad(2).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("adc").draw(this.getDataGroup().getItem(0,0,0).getH2F("adcL"));
        this.getDetectorCanvas().getCanvas("adc").cd(3);
        this.getDetectorCanvas().getCanvas("adc").getPad(3).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("adc").draw(this.getDataGroup().getItem(0,0,0).getH2F("adcR"));
        this.getDetectorCanvas().getCanvas("adc").update();
        
        this.getDetectorCanvas().getCanvas("adcTime").cd(0);
        this.getDetectorCanvas().getCanvas("adcTime").getPad(0).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("adcTime").draw(this.getDataGroup().getItem(0,0,0).getH2F("fadcL_time"));
        this.getDetectorCanvas().getCanvas("adcTime").cd(1);
        this.getDetectorCanvas().getCanvas("adcTime").getPad(1).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("adcTime").draw(this.getDataGroup().getItem(0,0,0).getH2F("fadcR_time"));
        this.getDetectorCanvas().getCanvas("adcTime").update();
        
        this.getDetectorCanvas().getCanvas("tdc").cd(0);
        this.getDetectorCanvas().getCanvas("tdc").getPad(0).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("tdc").draw(this.getDataGroup().getItem(0,0,0).getH2F("occTDC"));
        this.getDetectorCanvas().getCanvas("tdc").cd(1);
        this.getDetectorCanvas().getCanvas("tdc").getPad(1).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("tdc").draw(this.getDataGroup().getItem(0,0,0).getH2F("occTDCnorm"));
        this.getDetectorCanvas().getCanvas("tdc").cd(2);
        this.getDetectorCanvas().getCanvas("tdc").getPad(2).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("tdc").draw(this.getDataGroup().getItem(0,0,0).getH2F("tdcL"));
        this.getDetectorCanvas().getCanvas("tdc").cd(3);
        this.getDetectorCanvas().getCanvas("tdc").getPad(3).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("tdc").draw(this.getDataGroup().getItem(0,0,0).getH2F("tdcR"));
        this.getDetectorCanvas().getCanvas("tdc").update();
        this.getDetectorView().getView().repaint();
        this.getDetectorView().update();
 
    }
    
    @Override
    public void processEvent(DataEvent event) {
        
        // process event info and save into data group
        if(event.hasBank("LTCC::adc")==true){
	    DataBank bank = event.getBank("LTCC::adc");
	    int rows = bank.rows();
	    for(int loop = 0; loop < rows; loop++){
                int sector  = bank.getByte("sector", loop);
                int layer   = bank.getByte("layer", loop);
                int comp    = bank.getShort("component", loop);
                int order   = bank.getByte("order", loop);
                int adc     = bank.getInt("ADC", loop);
                float time  = bank.getFloat("time", loop);
                int iPMT    = (sector-1)*18+comp;
                if(adc>0 && time> 0) {
//                System.out.println("ROW " + loop + " SECTOR = " + sector + " LAYER = " + layer + " COMPONENT = " + comp + " ORDER + " + order +
//                      " ADC = " + adc + " TIME = " + time); 
                    this.getDataGroup().getItem(0,0,0).getH2F("occADC").fill((sector+order*0.5), comp*1.0);
                    if(order==0) {
                        this.getDataGroup().getItem(0,0,0).getH2F("adcL").fill(adc*1.0,iPMT*1.0);
                        this.getDataGroup().getItem(0,0,0).getH2F("fadcL_time").fill(time,iPMT*1.0);
                    } 
                    else if (order==1) {
                        this.getDataGroup().getItem(0,0,0).getH2F("adcR").fill(adc*1.0,iPMT*1.0);
                        this.getDataGroup().getItem(0,0,0).getH2F("fadcR_time").fill(time,iPMT*1.0);                    
                    }
                    this.getDetectorSummary().getH1F("summary").fill(sector*1.0);
                }
	    }
    	}
        
        if(event.hasBank("LTCC::tdc")==true){
	    DataBank bank = event.getBank("LTCC::tdc");
	    int rows = bank.rows();
            
            for(int i = 0; i < rows; i++){
	    for(int loop = 0; loop < rows; loop++){
                int    sector = bank.getByte("sector",i);
                int     layer = bank.getByte("layer",i);
                int      comp = bank.getShort("component",i);
                int       tdc = bank.getInt("TDC",i);
                int     order = bank.getByte("order",i); // order specifies left-right for ADC
                int iPMT    = (sector-1)*18+comp;
//                System.out.println(sector + " " + layer + " " + comp + " " + order + " " + tdc);
                if(tdc>0) {
                    this.getDataGroup().getItem(0,0,0).getH2F("occTDC").fill((sector-1)*2.0+layer, comp*1.0);
                    if(order==2) {
                        this.getDataGroup().getItem(0,0,0).getH2F("tdcL").fill(tdc*1.0,iPMT*1.0);
                    } 
                    else if (order==3) {
                        this.getDataGroup().getItem(0,0,0).getH2F("tdcR").fill(tdc*1.0,iPMT*1.0);                
                    }
                }
	    }
    	    }
        }
        
        
        
    }

    @Override
    public void analysisUpdate() {
        for(int ibin=0; ibin<this.getDataGroup().getItem(0,0,0).getH2F("occADC").getDataBufferSize(); ibin++) {
            float ref = this.getDataGroup().getItem(0,0,0).getH2F("occADCref").getDataBufferBin(ibin);
            float con = this.getDataGroup().getItem(0,0,0).getH2F("occADC").getDataBufferBin(ibin);
            if(ref>0) this.getDataGroup().getItem(0,0,0).getH2F("occADCnorm").setDataBufferBin(ibin, con/ref); 
        }
        for(int ibin=0; ibin<this.getDataGroup().getItem(0,0,0).getH2F("occTDC").getDataBufferSize(); ibin++) {
            float ref = this.getDataGroup().getItem(0,0,0).getH2F("occTDCref").getDataBufferBin(ibin);
            float con = this.getDataGroup().getItem(0,0,0).getH2F("occTDC").getDataBufferBin(ibin);
            if(ref>0) this.getDataGroup().getItem(0,0,0).getH2F("occTDCnorm").setDataBufferBin(ibin, con/ref); 
        }
    }


}
