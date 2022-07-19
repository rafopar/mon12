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
        
        this.setDetectorTabNames("adc", "tdc");
        this.init(false);
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
        H2F adc = new H2F("adc", "adc", 256, 0.5, 256.5, 20, 0, 20000);
        adc.setTitleX("PMT");
        adc.setTitleY("ADC - amplitude");
        H2F fadc_time = new H2F("fadc_time", "fadc_time", 256, 0.5, 256.5, 20, 0, 400);
        fadc_time.setTitleX("PMT");
        fadc_time.setTitleY("FADC - timing");
        H1F occTDC = new H1F("occTDC", "occTDC", 256, 0.5, 256.5);
        occTDC.setTitleX("PMT");
        occTDC.setTitleY("Counts");
        occTDC.setFillColor(38);
        H1F spectrumTDC = new H1F("spectrumTDC", "tdc", 50, 0, 1000);
        spectrumTDC.setTitleX("TDC - (ns)"); 
        spectrumTDC.setTitleY("Counts");
        spectrumTDC.setFillColor(38);
        H2F tdc = new H2F("tdc", "tdc", 256, 0.5, 256.5, 20, 0, 1000);
        tdc.setTitleX("PMT"); 
        tdc.setTitleY("TDC - (ns)");
        
        DataGroup dg = new DataGroup(1,6);
        dg.addDataSet(occADC, 0);
        dg.addDataSet(adc, 1);
        dg.addDataSet(fadc_time, 2);
        dg.addDataSet(occTDC, 3);
        dg.addDataSet(tdc, 4);
        dg.addDataSet(spectrumTDC, 5);
        this.getDataGroup().add(dg,0,0,0);
    }
        
    @Override
    public void plotHistos() {        
        // plotting histos
    	if (this.getDataGroup().getItem(0,0,0).getH1F("occADC")==null) return; //lcs To prevent exceptions if tab/histo missing from archive
        this.getDetectorCanvas().getCanvas("adc").divide(1, 3);
        this.getDetectorCanvas().getCanvas("adc").setGridX(false);
        this.getDetectorCanvas().getCanvas("adc").setGridY(false);
        this.getDetectorCanvas().getCanvas("tdc").divide(1,3);
        this.getDetectorCanvas().getCanvas("tdc").setGridX(false);
        this.getDetectorCanvas().getCanvas("tdc").setGridY(false);
        this.getDetectorCanvas().getCanvas("adc").cd(0);
        this.getDetectorCanvas().getCanvas("adc").draw(this.getDataGroup().getItem(0,0,0).getH1F("occADC"));
        this.getDetectorCanvas().getCanvas("adc").cd(1);
        this.getDetectorCanvas().getCanvas("adc").getPad(1).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("adc").draw(this.getDataGroup().getItem(0,0,0).getH2F("adc"));
        this.getDetectorCanvas().getCanvas("adc").cd(2);
        this.getDetectorCanvas().getCanvas("adc").getPad(2).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("adc").draw(this.getDataGroup().getItem(0,0,0).getH2F("fadc_time"));
        this.getDetectorCanvas().getCanvas("adc").update();
        this.getDetectorCanvas().getCanvas("tdc").cd(0);
        this.getDetectorCanvas().getCanvas("tdc").draw(this.getDataGroup().getItem(0,0,0).getH1F("occTDC"));
        this.getDetectorCanvas().getCanvas("tdc").cd(1);
        this.getDetectorCanvas().getCanvas("tdc").getPad(1).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("tdc").draw(this.getDataGroup().getItem(0,0,0).getH2F("tdc"));
        this.getDetectorCanvas().getCanvas("tdc").cd(2);
        this.getDetectorCanvas().getCanvas("tdc").draw(this.getDataGroup().getItem(0,0,0).getH1F("spectrumTDC"));
        this.getDetectorCanvas().getCanvas("tdc").update();
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
                    this.getDataGroup().getItem(0,0,0).getH1F("occADC").fill(PMT);
                    this.getDataGroup().getItem(0,0,0).getH2F("adc").fill(PMT, adc);
                    if(time > 1) this.getDataGroup().getItem(0,0,0).getH2F("fadc_time").fill(PMT, time);
                    
                    
                    this.getDetectorSummary().getH1F("summary").fill((PMT)); 
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
                    this.getDataGroup().getItem(0,0,0).getH1F("occTDC").fill(PMT);
                    this.getDataGroup().getItem(0,0,0).getH2F("tdc").fill(PMT, tdc*this.tdcconv);
                    this.getDataGroup().getItem(0,0,0).getH1F("spectrumTDC").fill(tdc*this.tdcconv);
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
