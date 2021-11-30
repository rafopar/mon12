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

public class BANDmonitor  extends DetectorMonitor {        
    
    public BANDmonitor(String name) {
        super(name);
        
        this.setDetectorTabNames("ADC Occupancies", "TDC Occupancies");
        this.init(false);
        this.setTriggerMask("0x10000000000");
    }

    @Override
    public void createHistos() {
        // initialize canvas and create histograms
        this.setNumberOfEvents(0);
        H1F summary = new H1F("summary","summary",256,0.5,256.5);
        summary.setTitleX("PMT");
        summary.setTitleY("CTOF hits");
        summary.setTitle("CTOF");
        summary.setFillColor(38);
        DataGroup sum = new DataGroup(1,1);
        sum.addDataSet(summary, 0);
        this.setDetectorSummary(sum);
        
        H1F occADC = new H1F("occADC", "occADC", 256, 0.5, 256.5);
        occADC.setTitleX("PMT");
        occADC.setTitleY("Counts");
        occADC.setFillColor(38);
        H2F adc = new H2F("adc", "adc", 20, 0, 20000, 256, 0.5, 256.5);
        adc.setTitleX("ADC - amplitude");
        adc.setTitleY("PMT");
        H2F fadc_time = new H2F("fadc_time", "fadc_time", 30, 0, 400, 256, 0.5, 256.5);
        fadc_time.setTitleX("FADC - timing");
        fadc_time.setTitleY("PMT");
        H1F occTDC = new H1F("occTDC", "occTDC", 256, 0.5, 256.5);
        occTDC.setTitleX("PMT");
        occTDC.setTitleY("Counts");
        occTDC.setFillColor(38);
        H2F tdc = new H2F("tdc", "tdc", 30, 0, 1000, 256, 0.5, 256.5);
        tdc.setTitleX("TDC - (ns)");
        tdc.setTitleY("PMT"); 
        
        DataGroup dg = new DataGroup(1,5);
        dg.addDataSet(occADC, 0);
        dg.addDataSet(adc, 1);
        dg.addDataSet(fadc_time, 2);
        dg.addDataSet(occTDC, 3);
        dg.addDataSet(tdc, 4);
        this.getDataGroup().add(dg,0,0,0);
    }
        
    @Override
    public void plotHistos() {        
        // plotting histos
    	if (this.getDataGroup().getItem(0,0,0).getH1F("occADC")==null) return; //lcs To prevent exceptions if tab/histo missing from archive
        this.getDetectorCanvas().getCanvas("ADC Occupancies").divide(1, 3);
        this.getDetectorCanvas().getCanvas("ADC Occupancies").setGridX(false);
        this.getDetectorCanvas().getCanvas("ADC Occupancies").setGridY(false);
        this.getDetectorCanvas().getCanvas("TDC Occupancies").divide(2, 1);
        this.getDetectorCanvas().getCanvas("TDC Occupancies").setGridX(false);
        this.getDetectorCanvas().getCanvas("TDC Occupancies").setGridY(false);
        this.getDetectorCanvas().getCanvas("ADC Occupancies").cd(0);
        this.getDetectorCanvas().getCanvas("ADC Occupancies").draw(this.getDataGroup().getItem(0,0,0).getH1F("occADC"));
        this.getDetectorCanvas().getCanvas("ADC Occupancies").cd(1);
        this.getDetectorCanvas().getCanvas("ADC Occupancies").getPad(1).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("ADC Occupancies").draw(this.getDataGroup().getItem(0,0,0).getH2F("adc"));
        this.getDetectorCanvas().getCanvas("ADC Occupancies").cd(2);
        this.getDetectorCanvas().getCanvas("ADC Occupancies").getPad(2).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("ADC Occupancies").draw(this.getDataGroup().getItem(0,0,0).getH2F("fadc_time"));
        this.getDetectorCanvas().getCanvas("ADC Occupancies").update();
        this.getDetectorCanvas().getCanvas("TDC Occupancies").cd(0);
        this.getDetectorCanvas().getCanvas("TDC Occupancies").draw(this.getDataGroup().getItem(0,0,0).getH1F("occTDC"));
        this.getDetectorCanvas().getCanvas("TDC Occupancies").cd(1);
        this.getDetectorCanvas().getCanvas("TDC Occupancies").getPad(1).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("TDC Occupancies").draw(this.getDataGroup().getItem(0,0,0).getH2F("tdc"));
        this.getDetectorCanvas().getCanvas("TDC Occupancies").update();
    }

    @Override
    public void processEvent(DataEvent event) {
    	
        if(event.hasBank("BAND::adc")==true){
	    DataBank bank = event.getBank("BAND::adc");
	    int rows = bank.rows();
	    for(int loop = 0; loop < rows; loop++){
                int sector  = bank.getByte("sector", loop);
                int layer   = bank.getByte("layer", loop);
                int comp    = bank.getShort("component", loop);
                int order   = bank.getByte("order", loop);
                int adc     = bank.getInt("ADC", loop);
                float time  = bank.getFloat("time", loop);
                int PMT = this.getPMT(sector, layer, comp, order);
//                System.out.println("ROW " + loop + " SECTOR = " + sector + " LAYER = " + layer + " COMPONENT = " + comp + " ORDER + " + order +
//                      " ADC = " + adc + " TIME = " + time); 
                if(adc>0) {
                    this.getDataGroup().getItem(0,0,0).getH1F("occADC").fill(PMT*1.0);
                    this.getDataGroup().getItem(0,0,0).getH2F("adc").fill(adc*1.0,PMT*1.0);
                    if(time > 1) this.getDataGroup().getItem(0,0,0).getH2F("fadc_time").fill(time*1.0,PMT*1.0);
                    
                    
                    this.getDetectorSummary().getH1F("summary").fill((PMT)*1.0); 
                }
                
	    }
    	}
        if(event.hasBank("BAND::tdc")==true){
            DataBank  bank = event.getBank("BAND::tdc");
            int rows = bank.rows();
            for(int i = 0; i < rows; i++){
                int    sector = bank.getByte("sector",i);
                int     layer = bank.getByte("layer",i);
                int      comp = bank.getShort("component",i);
                int       tdc = bank.getInt("TDC",i);
                int     order = bank.getByte("order",i); // order specifies left-right for ADC
                int PMT = this.getPMT(sector, layer, comp, order);
//                           System.out.println("ROW " + i + " SECTOR = " + sector
//                                 + " LAYER = " + layer + " PADDLE = "
//                                 + paddle + " TDC = " + TDC);    
                if(tdc>0) {
                    this.getDataGroup().getItem(0,0,0).getH1F("occTDC").fill(PMT*1.0);
                    this.getDataGroup().getItem(0,0,0).getH2F("tdc").fill(tdc*0.0245,PMT*1.0);
                }
            }
        }        
    }

    private int getPMT(int sector,int layer,int component,int order) {
        // Shift all values down by one because some people want to start indexing at 1 instead of at 0
        sector = sector - 1;
        layer = layer - 1;
        component = component - 1;
        order = order - 1;

        int component_mod = component;
        if(sector == 2 || sector == 3) component_mod = component*2;

        int PMT = layer*48 + sect(sector) + component_mod*2+order;
        if(layer == 5) PMT = 232+veto(sector) + component_mod;
        
        return PMT+1;
    }
   
    private int sect(int sector) {
        switch (sector) {
            case 0:  return 0;
            case 1:  return 6;
            case 2:  return 20;
            case 3:  return 22;
            case 4:  return 40;
            default: return -11;
        }
    }
    
    private int veto(int sector) {
        switch (sector) {
            case 0:  return 0;
            case 1:  return 3;
            case 2:  return 10;
            case 3:  return 11;
            case 4:  return 22;
            default: return -13;
        }
    }

    @Override
    public void analysisUpdate() {

    }


}
