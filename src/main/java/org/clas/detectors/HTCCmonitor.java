package org.clas.detectors;


import java.util.Arrays;
import org.clas.viewer.DetectorMonitor;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author devita
 */

public class HTCCmonitor  extends DetectorMonitor {
    
    private final int rfpmt = 1;
    
    public HTCCmonitor(String name) {
        super(name);
        
        this.setDetectorTabNames("occupancy", "adcEnergy", "adcTime", "deltaRF");
        this.init(false);
        this.getCcdb().setVariation("default");
        this.getCcdb().init(Arrays.asList(new String[]{"/calibration/eb/rf/config","/calibration/eb/rf/jitter"}));
    }

    @Override
    public void createHistos() {
        // initialize canvas and create histograms
        this.setNumberOfEvents(0);
        this.getDetectorCanvas().getCanvas("occupancy").divide(1, 2);
        this.getDetectorCanvas().getCanvas("occupancy").setGridX(false);
        this.getDetectorCanvas().getCanvas("occupancy").setGridY(false);
        this.getDetectorCanvas().getCanvas("adcEnergy").divide(3, 2);
        this.getDetectorCanvas().getCanvas("adcEnergy").setGridX(false);
        this.getDetectorCanvas().getCanvas("adcEnergy").setGridY(false);
        this.getDetectorCanvas().getCanvas("adcTime").divide(3, 2);
        this.getDetectorCanvas().getCanvas("adcTime").setGridX(false);
        this.getDetectorCanvas().getCanvas("adcTime").setGridY(false);
        this.getDetectorCanvas().getCanvas("deltaRF").divide(2, 2);
        this.getDetectorCanvas().getCanvas("deltaRF").setGridX(false);
        this.getDetectorCanvas().getCanvas("deltaRF").setGridY(false);
        H1F summary = new H1F("summary","summary",6,1,7);
        summary.setTitleX("sector");
        summary.setTitleY("HTCC hits");
        summary.setTitle("HTCC");
        summary.setFillColor(36);
        DataGroup sum = new DataGroup(1,1);
        sum.addDataSet(summary, 0);
        this.setDetectorSummary(sum);
        H2F occADC = new H2F("occADC", "occADC", 6, 0.5, 6.5, 8, 0.5, 8.5);
        occADC.setTitleY("ring-PMT");
        occADC.setTitleX("sector");
        occADC.setTitle("ADC Occupancy");
        H1F occADC1D = new H1F("occADC1D", "occADC1D", 48, 0.5, 48.5);
        occADC1D.setTitleX("PMT (PMT/ring/sector");
        occADC1D.setTitleY("Counts");
        occADC1D.setTitle("ADC Occupancy");
        occADC1D.setFillColor(3);
        H2F occTDC = new H2F("occTDC", "occTDC", 6, 0.5, 6.5, 8, 0.5, 8.5);
        occTDC.setTitleY("ring-PMT");
        occTDC.setTitleX("sector");
        occTDC.setTitle("TDC Occupancy");
        
        H2F adc1 = new H2F("adc_s1", "adc_s1", 150, 0, 15000, 8, 0.5, 8.5);
        adc1.setTitleX("ADC - value");
        adc1.setTitleY("PMT");
        adc1.setTitle("sector 1");
        H2F adc2 = new H2F("adc_s2", "adc_s2", 150, 0, 15000, 8, 0.5, 8.5);
        adc2.setTitleX("ADC - value");
        adc2.setTitleY("PMT");
        adc2.setTitle("sector 2");
        H2F adc3 = new H2F("adc_s3", "adc_s3", 150, 0, 15000, 8, 0.5, 8.5);
        adc3.setTitleX("ADC - value");
        adc3.setTitleY("PMT");
        adc3.setTitle("sector 3");
        H2F adc4 = new H2F("adc_s4", "adc_s4", 150, 0, 15000, 8, 0.5, 8.5);
        adc4.setTitleX("ADC - value");
        adc4.setTitleY("PMT");
        adc4.setTitle("sector 4");
        H2F adc5 = new H2F("adc_s5", "adc_s5", 150, 0, 15000, 8, 0.5, 8.5);
        adc5.setTitleX("ADC - value");
        adc5.setTitleY("PMT");
        adc5.setTitle("sector 5");
        H2F adc6 = new H2F("adc_s6", "adc_s6", 150, 0, 15000, 8, 0.5, 8.5);
        adc6.setTitleX("ADC - value");
        adc6.setTitleY("PMT");
        adc6.setTitle("sector 6");
        
        H2F fadc_time1 = new H2F("fadc_time_s1", "fadc_time_s1", 80, 0, 400, 8, 0.5, 8.5);
        fadc_time1.setTitleX("FADC timing");
        fadc_time1.setTitleY("PMT");
        fadc_time1.setTitle("sector 1");
        H2F fadc_time2 = new H2F("fadc_time_s2", "fadc_time_s2", 80, 0, 400, 8, 0.5, 8.5);
        fadc_time2.setTitleX("FADC timing");
        fadc_time2.setTitleY("PMT");
        fadc_time2.setTitle("sector 2");
        H2F fadc_time3 = new H2F("fadc_time_s3", "fadc_time_s3", 80, 0, 400, 8, 0.5, 8.5);
        fadc_time3.setTitleX("FADC timing");
        fadc_time3.setTitleY("PMT");
        fadc_time3.setTitle("sector 3");
        H2F fadc_time4 = new H2F("fadc_time_s4", "fadc_time_s4", 80, 0, 400, 8, 0.5, 8.5);
        fadc_time4.setTitleX("FADC timing");
        fadc_time4.setTitleY("PMT");
        fadc_time4.setTitle("sector 4");
        H2F fadc_time5 = new H2F("fadc_time_s5", "fadc_time_s5", 80, 0, 400, 8, 0.5, 8.5);
        fadc_time5.setTitleX("FADC timing");
        fadc_time5.setTitleY("PMT");
        fadc_time5.setTitle("sector 5");
        H2F fadc_time6 = new H2F("fadc_time_s6", "fadc_time_s6", 80, 0, 400, 8, 0.5, 8.5);
        fadc_time6.setTitleX("FADC timing");
        fadc_time6.setTitleY("PMT");
        fadc_time6.setTitle("sector 6");
        
        H2F tdc1 = new H2F("2dHTCC-RF1", "", 48, 0.5, 48.5, 100, -rfbucket/2, rfbucket/2);
        tdc1.setTitleX("PMT");
        tdc1.setTitleY("HTCC-RF1");
        H2F tdc2 = new H2F("2dHTCC-RF2", "", 48, 0.5, 48.5, 100, -rfbucket/2, rfbucket/2);
        tdc2.setTitleX("PMT");
        tdc2.setTitleY("HTCC-RF2");
        H1F tdc3 = new H1F("1dHTCC-RF1", "", 100, -rfbucket/2, rfbucket/2);
        tdc3.setTitleX("HTCCpmt1-RF1");
        tdc3.setTitleY("Counts");
        tdc3.setFillColor(5);
        H1F tdc4 = new H1F("1dHTCC-RF2", "", 100, -rfbucket/2, rfbucket/2);
        tdc4.setTitleX("HTCCpmt1-RF2");
        tdc4.setTitleY("Counts");
        tdc4.setFillColor(5);
        
        DataGroup dg = new DataGroup(5,4);
        dg.addDataSet(occADC, 0);
        dg.addDataSet(occADC1D, 1);
        dg.addDataSet(occTDC, 1);
        dg.addDataSet(adc1, 2);
        dg.addDataSet(adc2, 3);
        dg.addDataSet(adc3, 4);
        dg.addDataSet(adc4, 5);
        dg.addDataSet(adc5, 6);
        dg.addDataSet(adc6, 7);
        dg.addDataSet(fadc_time1, 8);
        dg.addDataSet(fadc_time2, 9);
        dg.addDataSet(fadc_time3, 10);
        dg.addDataSet(fadc_time4, 11);
        dg.addDataSet(fadc_time5, 12);
        dg.addDataSet(fadc_time6, 13);
        dg.addDataSet(tdc1, 14);
        dg.addDataSet(tdc2, 15);
        dg.addDataSet(tdc3, 16);
        dg.addDataSet(tdc4, 17);
        this.getDataGroup().add(dg,0,0,0);
    }
        
    @Override
    public void plotHistos() {
        // plotting histos
        this.getDetectorCanvas().getCanvas("occupancy").cd(0);
        this.getDetectorCanvas().getCanvas("occupancy").getPad(0).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("occupancy").draw(this.getDataGroup().getItem(0,0,0).getH2F("occADC"));
        this.getDetectorCanvas().getCanvas("occupancy").cd(1);
        this.getDetectorCanvas().getCanvas("occupancy").getPad(1).getAxisY().setLog(true);
        this.getDetectorCanvas().getCanvas("occupancy").draw(this.getDataGroup().getItem(0,0,0).getH1F("occADC1D"));
        this.getDetectorCanvas().getCanvas("occupancy").update();
        
        this.getDetectorCanvas().getCanvas("adcEnergy").cd(0);
        this.getDetectorCanvas().getCanvas("adcEnergy").getPad(0).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("adcEnergy").draw(this.getDataGroup().getItem(0,0,0).getH2F("adc_s1"));
        this.getDetectorCanvas().getCanvas("adcEnergy").cd(1);
        this.getDetectorCanvas().getCanvas("adcEnergy").getPad(1).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("adcEnergy").draw(this.getDataGroup().getItem(0,0,0).getH2F("adc_s2"));
        this.getDetectorCanvas().getCanvas("adcEnergy").cd(2);
        this.getDetectorCanvas().getCanvas("adcEnergy").getPad(2).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("adcEnergy").draw(this.getDataGroup().getItem(0,0,0).getH2F("adc_s3"));
        this.getDetectorCanvas().getCanvas("adcEnergy").cd(3);
        this.getDetectorCanvas().getCanvas("adcEnergy").getPad(3).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("adcEnergy").draw(this.getDataGroup().getItem(0,0,0).getH2F("adc_s4"));
        this.getDetectorCanvas().getCanvas("adcEnergy").cd(4);
        this.getDetectorCanvas().getCanvas("adcEnergy").getPad(4).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("adcEnergy").draw(this.getDataGroup().getItem(0,0,0).getH2F("adc_s5"));
        this.getDetectorCanvas().getCanvas("adcEnergy").cd(5);
        this.getDetectorCanvas().getCanvas("adcEnergy").getPad(5).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("adcEnergy").draw(this.getDataGroup().getItem(0,0,0).getH2F("adc_s6"));
        this.getDetectorCanvas().getCanvas("adcEnergy").update();
        
        this.getDetectorCanvas().getCanvas("adcTime").cd(0);
        this.getDetectorCanvas().getCanvas("adcTime").getPad(0).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("adcTime").draw(this.getDataGroup().getItem(0,0,0).getH2F("fadc_time_s1"));
        this.getDetectorCanvas().getCanvas("adcTime").cd(1);
        this.getDetectorCanvas().getCanvas("adcTime").getPad(1).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("adcTime").draw(this.getDataGroup().getItem(0,0,0).getH2F("fadc_time_s2"));
        this.getDetectorCanvas().getCanvas("adcTime").cd(2);
        this.getDetectorCanvas().getCanvas("adcTime").getPad(2).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("adcTime").draw(this.getDataGroup().getItem(0,0,0).getH2F("fadc_time_s3"));
        this.getDetectorCanvas().getCanvas("adcTime").cd(3);
        this.getDetectorCanvas().getCanvas("adcTime").getPad(3).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("adcTime").draw(this.getDataGroup().getItem(0,0,0).getH2F("fadc_time_s4"));
        this.getDetectorCanvas().getCanvas("adcTime").cd(4);
        this.getDetectorCanvas().getCanvas("adcTime").getPad(4).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("adcTime").draw(this.getDataGroup().getItem(0,0,0).getH2F("fadc_time_s5"));
        this.getDetectorCanvas().getCanvas("adcTime").cd(5);
        this.getDetectorCanvas().getCanvas("adcTime").getPad(5).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("adcTime").draw(this.getDataGroup().getItem(0,0,0).getH2F("fadc_time_s6"));
        this.getDetectorCanvas().getCanvas("adcTime").update();

        this.getDetectorCanvas().getCanvas("deltaRF").cd(0);
        this.getDetectorCanvas().getCanvas("deltaRF").getPad(0).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("deltaRF").draw(this.getDataGroup().getItem(0,0,0).getH2F("2dHTCC-RF1"));
        this.getDetectorCanvas().getCanvas("deltaRF").cd(1);
        this.getDetectorCanvas().getCanvas("deltaRF").getPad(1).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("deltaRF").draw(this.getDataGroup().getItem(0,0,0).getH2F("2dHTCC-RF2"));
        this.getDetectorCanvas().getCanvas("deltaRF").cd(2);
        this.getDetectorCanvas().getCanvas("deltaRF").draw(this.getDataGroup().getItem(0,0,0).getH1F("1dHTCC-RF1"));
        this.getDetectorCanvas().getCanvas("deltaRF").cd(3);
        this.getDetectorCanvas().getCanvas("deltaRF").draw(this.getDataGroup().getItem(0,0,0).getH1F("1dHTCC-RF2"));
        this.getDetectorCanvas().getCanvas("deltaRF").update();
        
        
        
        this.getDetectorView().getView().repaint();
        this.getDetectorView().update();
    }

    @Override
    public void processEvent(DataEvent event) {

        // get rf period
        double tjitter = 0;
        if(event.hasBank("RUN::config")) {
            DataBank config = event.getBank("RUN::config");
            int  run       = config.getInt("run", 0);
            long timestamp = config.getLong("timestamp",0);    
            IndexedTable ctable = this.getCcdb().getConstants(run, "/calibration/eb/rf/config");
            IndexedTable jtable = this.getCcdb().getConstants(run, "/calibration/eb/rf/jitter");
            this.rfbucket = ctable.getDoubleValue("clock", 1, 1, 1);
            this.tdcconv = ctable.getDoubleValue("tdc2time", 1, 1, 1);
            this.period  = jtable.getDoubleValue("period",0,0,0);
            this.phase   = jtable.getIntValue("phase",0,0,0);
            this.ncycles = jtable.getIntValue("cycles",0,0,0);
            tjitter = ((timestamp + phase) % ncycles) * period; 
        }
        // process event info and save into data group
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
                               
//                System.out.println("ROW " + loop + " SECTOR = " + sector + " LAYER = " + layer + " COMPONENT = " + comp + " ORDER + " + order +
//                      " ADC = " + adc + " TIME = " + time); 
                if(adc>0 && time>0) {
                    this.getDataGroup().getItem(0,0,0).getH2F("occADC").fill(sector*1.0,((comp-1)*2+layer)*1.0);
                    this.getDataGroup().getItem(0,0,0).getH1F("occADC1D").fill(((comp-1)*2+layer-1)*6.0+sector);
                    if(sector == 1) this.getDataGroup().getItem(0,0,0).getH2F("adc_s1").fill(adc*1.0,((comp-1)*2+layer)*1.0);
                    if(sector == 1) this.getDataGroup().getItem(0,0,0).getH2F("fadc_time_s1").fill(time,((comp-1)*2+layer)*1.0);
                    if(sector == 2) this.getDataGroup().getItem(0,0,0).getH2F("adc_s2").fill(adc*1.0,((comp-1)*2+layer)*1.0);
                    if(sector == 2) this.getDataGroup().getItem(0,0,0).getH2F("fadc_time_s2").fill(time,((comp-1)*2+layer)*1.0);
                    if(sector == 3) this.getDataGroup().getItem(0,0,0).getH2F("adc_s3").fill(adc*1.0,((comp-1)*2+layer)*1.0);
                    if(sector == 3) this.getDataGroup().getItem(0,0,0).getH2F("fadc_time_s3").fill(time,((comp-1)*2+layer)*1.0);
                    if(sector == 4) this.getDataGroup().getItem(0,0,0).getH2F("adc_s4").fill(adc*1.0,((comp-1)*2+layer)*1.0);
                    if(sector == 4) this.getDataGroup().getItem(0,0,0).getH2F("fadc_time_s4").fill(time,((comp-1)*2+layer)*1.0);
                    if(sector == 5) this.getDataGroup().getItem(0,0,0).getH2F("adc_s5").fill(adc*1.0,((comp-1)*2+layer)*1.0);
                    if(sector == 5) this.getDataGroup().getItem(0,0,0).getH2F("fadc_time_s5").fill(time,((comp-1)*2+layer)*1.0);
                    if(sector == 6) this.getDataGroup().getItem(0,0,0).getH2F("adc_s6").fill(adc*1.0,((comp-1)*2+layer)*1.0);
                    if(sector == 6) this.getDataGroup().getItem(0,0,0).getH2F("fadc_time_s6").fill(time,((comp-1)*2+layer)*1.0);
                    this.getDetectorSummary().getH1F("summary").fill(sector*1.0);
                    
                    if(event.hasBank("RF::tdc")) {
                        int pmt = ((comp-1)*2+layer-1)*6+sector;
                        DataBank rf = event.getBank("RF::tdc");
                        for(int i = 0; i<rf.rows(); i++){
                            int id  = rf.getShort("component",i);
                            double tdc = rf.getInt("TDC",i);
                            if(tdc>0) {
                                double delta = (time - (tdc*tdcconv-tjitter)+rfbucket*10000)%rfbucket-rfbucket/2;
                                this.getDataGroup().getItem(0,0,0).getH2F("2dHTCC-RF"+id).fill(pmt, delta);
                                if(pmt==rfpmt) this.getDataGroup().getItem(0,0,0).getH1F("1dHTCC-RF"+id).fill(delta);
                            }
                        }
                    }
                }
	    }
    	}
        
/*        
        if(event.hasBank("HTCC::tdc")==true){
            DataBank  bank = event.getBank("HTCC::tdc");
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
                if(tdc>0) this.getDataGroup().getItem(0,0,0).getH2F("occTDC").fill(sector*1.0,((comp-1)*2+layer)*1.0);
                
                if(sector == 1) this.getDataGroup().getItem(0,0,0).getH2F("tdc_s1").fill(tdc,((comp-1)*2+layer)*1.0);
                if(sector == 2) this.getDataGroup().getItem(0,0,0).getH2F("tdc_s2").fill(tdc,((comp-1)*2+layer)*1.0);
                if(sector == 3) this.getDataGroup().getItem(0,0,0).getH2F("tdc_s3").fill(tdc,((comp-1)*2+layer)*1.0);
                if(sector == 4) this.getDataGroup().getItem(0,0,0).getH2F("tdc_s4").fill(tdc,((comp-1)*2+layer)*1.0);
                if(sector == 5) this.getDataGroup().getItem(0,0,0).getH2F("tdc_s5").fill(tdc,((comp-1)*2+layer)*1.0);
                if(sector == 6) this.getDataGroup().getItem(0,0,0).getH2F("tdc_s6").fill(tdc,((comp-1)*2+layer)*1.0);
                
//              this.getDetectorSummary().getH1F("summary").fill(sector*1.0);                
            }
        }
*/        
    }

    @Override
    public void analysisUpdate() {

    }

}
