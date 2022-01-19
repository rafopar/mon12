package org.clas.detectors;

import org.clas.viewer.DetectorMonitor;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

import java.util.Arrays;
import org.jlab.utils.groups.IndexedTable;

public class TDCmonitor  extends DetectorMonitor {

    private final int[] npaddles = new int[]{23,62,5};
    private final String[] names = {"left", "right"};
    private final double[] store = new double[npaddles[1]+npaddles[0]];
    private final double vfTdcConv = 0.016;
    
    public TDCmonitor(String name) {
        super(name);
        
        this.setDetectorTabNames("TDC Histograms");
        this.useSectorButtons(false);
        this.init(false);   // set to true for picture on left side
        this.getCcdb().setVariation("default");
        this.getCcdb().init(Arrays.asList(new String[]{"/calibration/ftof/time_jitter"}));
    }

    @Override
    public void createHistos() {
        // initialize canvas and create histograms
        this.setNumberOfEvents(0);
        this.getDetectorCanvas().getCanvas("TDC Histograms").divide(4, 2);
        this.getDetectorCanvas().getCanvas("TDC Histograms").setGridX(false);
        this.getDetectorCanvas().getCanvas("TDC Histograms").setGridY(false);

        H1F sumStackp2 = new H1F("sum_p2","sum_p2",6,0.5,6.5);
        sumStackp2.setTitleX("sector");
        sumStackp2.setTitleY("Counts");
        sumStackp2.setTitle("FTOF panel 1A");
        sumStackp2.setFillColor(39);
        DataGroup sum = new DataGroup(1,1); 
        sum.addDataSet(sumStackp2, 1);
        this.setDetectorSummary(sum);
        
        DataGroup dg = new DataGroup(3,2);
        for(int i=0; i<names.length; i++) {
            H1F occVF = new H1F("occVF"+names[i],  "PMT "+names[i],  "Counts",   npaddles[1]+npaddles[0]+2, -npaddles[0]-1, npaddles[1]+1);
            occVF.setLineColor(2);
            H1F occCA = new H1F("occCA"+names[i],  "PMT "+names[i],  "Counts",   npaddles[1]+npaddles[0]+2, -npaddles[0]-1, npaddles[1]+1);
            H2F tdc   = new H2F("tdc"+names[i],    "TDC spectrum", 100, 0, 1100, npaddles[1]+npaddles[0]+2, -npaddles[0]-1, npaddles[1]+1);
            tdc.setTitleX("vfTDC (ns)");
            tdc.setTitleY("PMT "+names[i]);
            H2F cor   = new H2F("cor"+names[i],    "Correlation",  100, 0, 400, 100, 500, 900);
            cor.setTitleX("caenTDC (ns)");
            cor.setTitleY("vfTDC (ns)");
            H2F dif   = new H2F("dif"+names[i],    "Difference", 100, -100, 900, npaddles[1]+npaddles[0]+2, -npaddles[0]-1, npaddles[1]+1);
            dif.setTitleX("vfTDC-caenTDC (ns)");
            dif.setTitleY("paddle");
            
            dg.addDataSet(occVF, i*4+0);
            dg.addDataSet(occCA, i*4+0);
            dg.addDataSet(tdc, i*4+1);
            dg.addDataSet(cor, i*4+2);
            dg.addDataSet(dif, i*4+3);
        }
        this.getDataGroup().add(dg,0,0,0);
    }

    @Override
    public void plotHistos() {
    	
        for(int i=0; i<names.length; i++) {
            this.getDetectorCanvas().getCanvas("TDC Histograms").cd(i*4+0);
            this.getDetectorCanvas().getCanvas("TDC Histograms").draw(this.getDataGroup().getItem(0, 0, 0).getH1F("occCA" + names[i]));
            this.getDetectorCanvas().getCanvas("TDC Histograms").draw(this.getDataGroup().getItem(0, 0, 0).getH1F("occVF" + names[i]),"same");
            this.getDetectorCanvas().getCanvas("TDC Histograms").cd(i*4+1);
            this.getDetectorCanvas().getCanvas("TDC Histograms").draw(this.getDataGroup().getItem(0, 0, 0).getH2F("tdc" + names[i]));
            this.getDetectorCanvas().getCanvas("TDC Histograms").getPad().getAxisZ().setLog(true);
            this.getDetectorCanvas().getCanvas("TDC Histograms").cd(i*4+2);
            this.getDetectorCanvas().getCanvas("TDC Histograms").draw(this.getDataGroup().getItem(0, 0, 0).getH2F("cor" + names[i]));
            this.getDetectorCanvas().getCanvas("TDC Histograms").getPad().getAxisZ().setLog(true);
            this.getDetectorCanvas().getCanvas("TDC Histograms").cd(i*4+3);
            this.getDetectorCanvas().getCanvas("TDC Histograms").draw(this.getDataGroup().getItem(0, 0, 0).getH2F("dif" + names[i]));
            this.getDetectorCanvas().getCanvas("TDC Histograms").getPad().getAxisZ().setLog(true);
        }
    }           

    @Override
    public void processEvent(DataEvent event) {
	            
        int triggerPhase=0;
        if(event.hasBank("RUN::config")) {
            DataBank bank = event.getBank("RUN::config");
            int runNumber  = bank.getInt("run", 0);
            long timestamp = bank.getLong("timestamp",0);    
            IndexedTable jitter = this.getCcdb().getConstants(runNumber, "/calibration/ftof/time_jitter");
            this.period  = jitter.getDoubleValue("period",0,0,0);
            this.phase   = jitter.getIntValue("phase",0,0,0);
            this.ncycles = jitter.getIntValue("cycles",0,0,0);           
            if(ncycles>0){
                triggerPhase  = (int) ((timestamp+phase)%ncycles); // TI derived phase correction due to TDC and FADC clock differences
            }
        }
        
        for(int i=0; i<store.length; i++) store[i]=0;
        int nCaen = 0;
        if(event.hasBank("FTOF::tdc")==true){
            DataBank  bank = event.getBank("FTOF::tdc");
            for(int i = 0; i < bank.rows(); i++){
                int  sector = bank.getByte("sector",i);
                int  layer  = bank.getByte("layer",i);
                int  pmt    = bank.getByte("order",i);                       
                int  paddle = bank.getShort("component",i);
                
                double tdc =  bank.getInt("TDC",i)*this.tdcconv-triggerPhase*this.period;
                
                if(tdc>0 && sector==6 && layer<=3) {               	
                    this.getDataGroup().getItem(0,0,0).getH1F("occCA"+names[pmt-2]).fill(paddle*(layer*2-3));
                    if(tdc<store[paddle-1+npaddles[0]*(layer-1)] || store[paddle-1+npaddles[0]*(layer-1)]==0) store[paddle-1+npaddles[0]*(layer-1)]=tdc;
//                    System.out.println("caen " + sector + " " + layer + " " + paddle + " " + pmt);
//                    nCaen++;
                }
            }
        }	    
        if(event.hasBank("FTOF::vftdc")==true){
            DataBank  bank = event.getBank("FTOF::vftdc");
            for(int i = 0; i < bank.rows(); i++){
                int  sector = bank.getByte("sector",i);
                int  layer  = bank.getByte("layer",i);
                int  pmt    = bank.getByte("order",i);                       
                int  paddle = bank.getShort("component",i);
                int  edge   = bank.getByte("edge",i);
                
                double tdc =  bank.getInt("TDC",i)*this.vfTdcConv;
                
                if(tdc>0 && layer<=2) {  
                    this.getDataGroup().getItem(0,0,0).getH2F("tdc"+names[pmt-2]).fill(tdc, paddle*(layer*2-3));
                    if(edge==0) {
                        this.getDataGroup().getItem(0,0,0).getH1F("occVF"+names[pmt-2]).fill(paddle*(layer*2-3));
                        if(store[paddle-1+npaddles[0]*(layer-1)]>0) {
                            this.getDataGroup().getItem(0,0,0).getH2F("cor"+names[pmt-2]).fill(store[paddle-1+npaddles[0]*(layer-1)], tdc);
                            this.getDataGroup().getItem(0,0,0).getH2F("dif"+names[pmt-2]).fill(tdc-store[paddle-1+npaddles[0]*(layer-1)], paddle*(layer*2-3));
                        }
//                        if(nCaen>0) {
//                            System.out.println("vf   " + sector + " " + layer + " " + paddle + " " + pmt);                            
//                        }
                    }
                }
            }
        }
    }
        
    
    @Override
    public void analysisUpdate() {

    }


}
