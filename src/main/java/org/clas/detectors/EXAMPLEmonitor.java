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

public class EXAMPLEmonitor  extends DetectorMonitor {
        
    
    public EXAMPLEmonitor(String name) {
        super(name);
        
        this.setDetectorTabNames("Example");
        this.init(false);
    }

    @Override
    public void createHistos() {
        // initialize canvas and create histograms
        this.setNumberOfEvents(0);
        this.getDetectorCanvas().getCanvas("Example").divide(1, 2);
        this.getDetectorCanvas().getCanvas("Example").setGridX(false);
        this.getDetectorCanvas().getCanvas("Example").setGridY(false);
        H1F summary = new H1F("summary","summary",6,1,7);
        summary.setTitleX("sector");
        summary.setTitleY("Example hits");
        summary.setTitle("Example");
        summary.setFillColor(36);
        DataGroup sum = new DataGroup(1,1);
        sum.addDataSet(summary, 0);
        this.setDetectorSummary(sum);
        H2F occADC = new H2F("occADC", "occADC", 6, 0.5, 6.5, 8, 0.5, 8.5);
        occADC.setTitleY("ring-PMT");
        occADC.setTitleX("sector");
        occADC.setTitle("ADC Occupancy");
        H2F occTDC = new H2F("occTDC", "occTDC", 6, 0.5, 6.5, 8, 0.5, 8.5);
        occTDC.setTitleY("ring-PMT");
        occTDC.setTitleX("sector");
        occTDC.setTitle("TDC Occupancy");
        DataGroup dg = new DataGroup(1,2);
        dg.addDataSet(occADC, 0);
        dg.addDataSet(occTDC, 1);
        this.getDataGroup().add(dg,0,0,0);
    }
        
    @Override
    public void plotHistos() {
        // plotting histos
        this.getDetectorCanvas().getCanvas("Occupancies").cd(0);
        this.getDetectorCanvas().getCanvas("Occupancies").draw(this.getDataGroup().getItem(0,0,0).getH2F("occADC"));
        this.getDetectorCanvas().getCanvas("Occupancies").cd(1);
        this.getDetectorCanvas().getCanvas("Occupancies").draw(this.getDataGroup().getItem(0,0,0).getH1F("occTDC"));
        this.getDetectorCanvas().getCanvas("Occupancies").update();
    }

    @Override
    public void processEvent(DataEvent event) {
        // process event info and fill the histograms
        if(event.hasBank("HTCC::adc")==true){
	    DataBank bank = event.getBank("HTCC::adc");
	    int rows = bank.rows();
	    for(int loop = 0; loop < rows; loop++){
                int sector  = bank.getByte("sector", loop);
                int layer   = bank.getByte("layer", loop);
                int comp    = bank.getShort("component", loop);
                int order   = bank.getByte("order", loop);
                int adc     = bank.getInt("ADC", loop);
                float time  = bank.getFloat("time", loop);

                if(adc>0 && time>0) {
                    this.getDataGroup().getItem(0,0,0).getH2F("occADC").fill(sector*1.0,((comp-1)*2+layer)*1.0);
                    this.getDetectorSummary().getH1F("summary").fill(sector*1.0);
                }
	    }
    	}
    }

    @Override
    public void analysisUpdate() {
        // use to manipulate the histograms (fit, normalize, ...)
    }


}
