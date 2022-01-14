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

public class CTOFmonitor  extends DetectorMonitor {        
    
    public CTOFmonitor(String name) {
        super(name);
        
        this.setDetectorTabNames("ADC Occupancies", "TDC Occupancies");
        this.init(false);
    }

    @Override
    public void createHistos() {
        // initialize canvas and create histograms
        this.setNumberOfEvents(0);
        H1F summary = new H1F("summary","summary",96,0.5,96.5);
        summary.setTitleX("PMT");
        summary.setTitleY("CTOF hits");
        summary.setTitle("CTOF");
        summary.setFillColor(38);
        DataGroup sum = new DataGroup(1,1);
        sum.addDataSet(summary, 0);
        this.setDetectorSummary(sum);
        
        H1F occADCU = new H1F("occADCU", "occADCU", 48, 0.5, 48.5);
        occADCU.setTitleX("PMT Upstream");
        occADCU.setTitleY("Counts");
        occADCU.setFillColor(38);
        H1F occADCD = new H1F("occADCD", "occADCD", 48, 0.5, 48.5);
        occADCD.setTitleX("PMT Downstream");
        occADCD.setTitleY("Counts");
        occADCD.setFillColor(38);
        H2F adcU = new H2F("adcU", "adcU", 50, 0, 5000, 48, 0.5, 48.5);
        adcU.setTitleX("ADC Upstream - amplitude");
        adcU.setTitleY("PMT Upstream");
        H2F adcD = new H2F("adcD", "adcD", 50, 0, 5000, 48, 0.5, 48.5);
        adcD.setTitleX("ADC Downstream - amplitude");
        adcD.setTitleY("PMT Downstream");   
        H2F fadcU_time = new H2F("fadcU_time", "fadcU_time", 80, 0, 400, 48, 0.5, 48.5);
        fadcU_time.setTitleX("FADC Upstream - timing");
        fadcU_time.setTitleY("PMT Upstream");
        H2F fadcD_time = new H2F("fadcD_time", "fadcD_time", 80, 0, 400, 48, 0.5, 48.5);
        fadcD_time.setTitleX("FADC Downstream - timing");
        fadcD_time.setTitleY("PMT Downstream");  
        H1F occTDCU = new H1F("occTDCU", "occTDCU", 48, 0.5, 48.5);
        occTDCU.setTitleX("PMT Upstream");
        occTDCU.setTitleY("Counts");
        occTDCU.setFillColor(38);
        H1F occTDCD = new H1F("occTDCD", "occTDCD", 48, 0.5, 48.5);
        occTDCD.setTitleX("PMT Downstream");
        occTDCD.setTitleY("Counts");
        occTDCD.setFillColor(38);
        H2F tdcU = new H2F("tdcU", "tdcU", 50, 0, 15000, 48, 0.5, 48.5);
        tdcU.setTitleX("TDC Upstream - amplitude");
        tdcU.setTitleY("PMT Upstream");
        H2F tdcD = new H2F("tdcD", "tdcD", 50, 0, 15000, 48, 0.5, 48.5);
        tdcD.setTitleX("TDC Downstream - amplitude");
        tdcD.setTitleY("PMT Downstream"); 
        
        DataGroup dg = new DataGroup(2,5);
        dg.addDataSet(occADCU, 0);
        dg.addDataSet(occADCD, 1);
        dg.addDataSet(adcU, 2);
        dg.addDataSet(adcD, 3);
        dg.addDataSet(fadcU_time, 4);
        dg.addDataSet(fadcD_time, 5);
        dg.addDataSet(occTDCU, 6);
        dg.addDataSet(occTDCD, 7);
        dg.addDataSet(tdcU, 8);
        dg.addDataSet(tdcD, 9);
        this.getDataGroup().add(dg,0,0,0);
    }
        
    @Override
    public void plotHistos() {        
        // plotting histos
        this.getDetectorCanvas().getCanvas("ADC Occupancies").divide(2, 3);
        this.getDetectorCanvas().getCanvas("ADC Occupancies").setGridX(false);
        this.getDetectorCanvas().getCanvas("ADC Occupancies").setGridY(false);
        this.getDetectorCanvas().getCanvas("TDC Occupancies").divide(2, 2);
        this.getDetectorCanvas().getCanvas("TDC Occupancies").setGridX(false);
        this.getDetectorCanvas().getCanvas("TDC Occupancies").setGridY(false);
        this.getDetectorCanvas().getCanvas("ADC Occupancies").cd(0);
        this.getDetectorCanvas().getCanvas("ADC Occupancies").draw(this.getDataGroup().getItem(0,0,0).getH1F("occADCU"));
        this.getDetectorCanvas().getCanvas("ADC Occupancies").cd(1);
        this.getDetectorCanvas().getCanvas("ADC Occupancies").draw(this.getDataGroup().getItem(0,0,0).getH1F("occADCD"));
        this.getDetectorCanvas().getCanvas("ADC Occupancies").cd(2);
        this.getDetectorCanvas().getCanvas("ADC Occupancies").getPad(2).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("ADC Occupancies").draw(this.getDataGroup().getItem(0,0,0).getH2F("adcU"));
        this.getDetectorCanvas().getCanvas("ADC Occupancies").cd(3);
        this.getDetectorCanvas().getCanvas("ADC Occupancies").getPad(3).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("ADC Occupancies").draw(this.getDataGroup().getItem(0,0,0).getH2F("adcD"));
        this.getDetectorCanvas().getCanvas("ADC Occupancies").cd(4);
        this.getDetectorCanvas().getCanvas("ADC Occupancies").getPad(4).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("ADC Occupancies").draw(this.getDataGroup().getItem(0,0,0).getH2F("fadcU_time"));
        this.getDetectorCanvas().getCanvas("ADC Occupancies").cd(5);
        this.getDetectorCanvas().getCanvas("ADC Occupancies").getPad(5).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("ADC Occupancies").draw(this.getDataGroup().getItem(0,0,0).getH2F("fadcD_time"));
        this.getDetectorCanvas().getCanvas("ADC Occupancies").update();
        this.getDetectorCanvas().getCanvas("TDC Occupancies").cd(0);
        this.getDetectorCanvas().getCanvas("TDC Occupancies").draw(this.getDataGroup().getItem(0,0,0).getH1F("occTDCU"));
        this.getDetectorCanvas().getCanvas("TDC Occupancies").cd(1);
        this.getDetectorCanvas().getCanvas("TDC Occupancies").draw(this.getDataGroup().getItem(0,0,0).getH1F("occTDCD"));
        this.getDetectorCanvas().getCanvas("TDC Occupancies").cd(2);
        this.getDetectorCanvas().getCanvas("TDC Occupancies").getPad(2).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("TDC Occupancies").draw(this.getDataGroup().getItem(0,0,0).getH2F("tdcU"));
        this.getDetectorCanvas().getCanvas("TDC Occupancies").cd(3);
        this.getDetectorCanvas().getCanvas("TDC Occupancies").getPad(3).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("TDC Occupancies").draw(this.getDataGroup().getItem(0,0,0).getH2F("tdcD"));
        this.getDetectorCanvas().getCanvas("TDC Occupancies").update();
    }

    @Override
    public void processEvent(DataEvent event) {
        
        if(event.hasBank("CTOF::adc")==true){
	    DataBank bank = event.getBank("CTOF::adc");
	    int rows = bank.rows();
	    for(int loop = 0; loop < rows; loop++){
                int sector  = bank.getByte("sector", loop);
                int layer   = bank.getByte("layer", loop);
                int comp    = bank.getShort("component", loop);
                int order   = bank.getByte("order", loop);
                int adc     = bank.getInt("ADC", loop);
                float time  = bank.getFloat("time", loop);
//                System.out.println("ROW " + loop + " SECTOR = " + sector + " LAYER = " + layer + " COMPONENT = " + comp + " ORDER + " + order +
//                      " ADC = " + adc + " TIME = " + time); 
                if(adc>0) {
                    if(order==0) {
                        this.getDataGroup().getItem(0,0,0).getH1F("occADCU").fill(comp*1.0);
                        this.getDataGroup().getItem(0,0,0).getH2F("adcU").fill(adc*1.0,comp*1.0);
                        if(time > 1) this.getDataGroup().getItem(0,0,0).getH2F("fadcU_time").fill(time*1.0,comp*1.0);
                    }
                    else if(order==1) {
                        this.getDataGroup().getItem(0,0,0).getH1F("occADCD").fill(comp*1.0);
                        this.getDataGroup().getItem(0,0,0).getH2F("adcD").fill(adc*1.0,comp*1.0);
                        if(time > 1) this.getDataGroup().getItem(0,0,0).getH2F("fadcD_time").fill(time*1.0,comp*1.0);
                    }
                    
                    this.getDetectorSummary().getH1F("summary").fill((order*48+comp)*1.0); 
                }
                
	    }
    	}
        if(event.hasBank("CTOF::tdc")==true){
            DataBank  bank = event.getBank("CTOF::tdc");
            int rows = bank.rows();
            for(int i = 0; i < rows; i++){
                int    sector = bank.getByte("sector",i);
                int     layer = bank.getByte("layer",i);
                int      comp = bank.getShort("component",i);
                int       tdc = bank.getInt("TDC",i);
                int     order = bank.getByte("order",i); // order specifies left-right for ADC
//                           System.out.println("ROW " + i + " SECTOR = " + sector
//                                 + " LAYER = " + layer + " PADDLE = "
//                                 + paddle + " TDC = " + TDC);    
                if(tdc>0) {
                    if(order==2) {
                        this.getDataGroup().getItem(0,0,0).getH1F("occTDCU").fill(comp*1.0);
                        this.getDataGroup().getItem(0,0,0).getH2F("tdcU").fill(tdc*1.0,comp*1.0);
                    }
                    else if(order==3) {
                        this.getDataGroup().getItem(0,0,0).getH1F("occTDCD").fill(comp*1.0);
                        this.getDataGroup().getItem(0,0,0).getH2F("tdcD").fill(tdc*1.0,comp*1.0);
                    }
                }
            }
        }        
    }

    @Override
    public void analysisUpdate() {

    }


}
