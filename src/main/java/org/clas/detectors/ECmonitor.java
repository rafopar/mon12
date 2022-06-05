package org.clas.detectors;


import java.util.Arrays;
import org.clas.viewer.DetectorMonitor;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.math.F1D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.utils.groups.IndexedTable;


public class ECmonitor  extends DetectorMonitor {

    private final int[] npaddles = new int[]{68,62,62,36,36,36,36,36,36};
    String[] stacks = new String[]{"PCAL","ECin","ECout"};
    String[] views = new String[]{"u","v","w"};
    
    public ECmonitor(String name) {
        super(name);

        this.setDetectorTabNames("adcOccupancy","tdcOccupancy", "adcEnergy_s", "adcTime_s", "tdc_s", "adcSum_s");
        this.useSectorButtons(true);
        this.init(false);
        this.getCcdb().setVariation("default");
        this.getCcdb().init(Arrays.asList(new String[]{"/calibration/ec/time_jitter"}));
    }

    @Override
    public void createHistos() {
        // initialize canvas and create histograms
        this.setNumberOfEvents(0);
        this.getDetectorCanvas().getCanvas("adcOccupancy").divide(3, 3);
        this.getDetectorCanvas().getCanvas("adcOccupancy").setGridX(false);
        this.getDetectorCanvas().getCanvas("adcOccupancy").setGridY(false);
        this.getDetectorCanvas().getCanvas("tdcOccupancy").divide(3, 3);
        this.getDetectorCanvas().getCanvas("tdcOccupancy").setGridX(false);
        this.getDetectorCanvas().getCanvas("tdcOccupancy").setGridY(false);
        this.getDetectorCanvas().getCanvas("adcEnergy").divide(3, 3);
        this.getDetectorCanvas().getCanvas("adcEnergy").setGridX(false);
        this.getDetectorCanvas().getCanvas("adcEnergy").setGridY(false);
        this.getDetectorCanvas().getCanvas("adcTime").divide(3, 3);
        this.getDetectorCanvas().getCanvas("adcTime").setGridX(false);
        this.getDetectorCanvas().getCanvas("adcTime").setGridY(false);
        this.getDetectorCanvas().getCanvas("tdc").divide(3, 3);
        this.getDetectorCanvas().getCanvas("tdc").setGridX(false);
        this.getDetectorCanvas().getCanvas("tdc").setGridY(false);
        this.getDetectorCanvas().getCanvas("adcSum").divide(3, 2);
        this.getDetectorCanvas().getCanvas("adcSum").setGridX(false);
        this.getDetectorCanvas().getCanvas("adcSum").setGridY(false);
        
        DataGroup sum = new DataGroup(1,1);
       
        int col[] = {1,2,4};
        String gtit[] = {"PCAL U (black) V (red) W (blue)","ECIN (left) ECOUT (right)", ""};
        
        F1D f1 = new F1D("p0","[a]",0.5,6.5); f1.setParameter(0,1); f1.setLineColor(3); f1.setLineWidth(3);
        sum.addDataSet(f1, 0);
        
        for(int i=0; i<3; i++) {
            String name = "sum"+stacks[i];
            H2F sumStack = new H2F(name,name,6,0.5,6.5,3,0.5,3.5);            
            sumStack.setTitleX("sector");
            sumStack.setTitleY("Sector Sum Norm");
            sumStack.setTitle(stacks[i]);
            
            sum.addDataSet(sumStack, 0); 
            
            for (int il=0; il<3; il++) {
            	GraphErrors g = new GraphErrors(stacks[i]+views[il]);
            	if(il==0) {g.setTitle(gtit[i]); g.setTitleX("sector"); g.setTitleY("Sector Normalized Hits");}
                g.setMarkerStyle(1); g.setMarkerColor(col[il]); g.setLineColor(col[il]); g.setMarkerSize(3); g.setLineThickness(1);
            	sum.addDataSet(g, 0);
            }
        }

        this.setDetectorSummary(sum);
        
        for(int layer=1; layer <= 9; layer++) {
            int stack = (int) ((layer-1)/3) + 1;
            int view  = layer - (stack-1)*3;
            H2F occADC = new H2F("occADC"+layer, "layer " + layer + " Occupancy", 6, 0.5, 6.5, this.npaddles[layer-1], 1, npaddles[layer-1]+1);
            occADC.setTitleY(stacks[stack-1] + " " + views[view-1] + " strip");
            occADC.setTitleX("sector");
            H2F occTDC = new H2F("occTDC"+layer, "layer " + layer + " Occupancy",6, 0.5, 6.5,  this.npaddles[layer-1], 1, npaddles[layer-1]+1);
            occTDC.setTitleY(stacks[stack-1] + " " + views[view-1] + " strip");
            occTDC.setTitleX("sector");
            DataGroup dg = new DataGroup(2,2);
            dg.addDataSet(occADC, 0);
            dg.addDataSet(occTDC, 0);
            this.getDataGroup().add(dg,0,layer,0);
        }
        
        for(int sector=1; sector<7; sector++) {
        for(int layer=1; layer <= 9; layer++) {
            int stack = (int) ((layer-1)/3) + 1;
            int view  = layer - (stack-1)*3;
            H2F datADC = new H2F("datADC"+layer+sector, "lay/sec " + layer + sector+" ADC", 100, 0., 2000., this.npaddles[layer-1], 1, npaddles[layer-1]+1);
            datADC.setTitleY(stacks[stack-1] + " " + views[view-1] + " strip");
            datADC.setTitleX("ADC Channel");
            datADC.setTitle("Sector "+sector);
            H2F timeFADC = new H2F("timeFADC"+layer+sector, "lay/sec " + layer + sector+" FADC", 80, 0., 400., this.npaddles[layer-1], 1, npaddles[layer-1]+1);
            timeFADC.setTitleY(stacks[stack-1] + " " + views[view-1] + " strip");
            timeFADC.setTitleX("adcTime");
            datADC.setTitle("Sector "+sector);
            H2F datTDC = new H2F("datTDC"+layer+sector, "lay/sec " + layer + sector+" TDC", 100, 0., 600., this.npaddles[layer-1], 1, npaddles[layer-1]+1);
            datTDC.setTitleY(stacks[stack-1] + " " + views[view-1] + " strip");
            datTDC.setTitleX("TDC Channel");
            datTDC.setTitle("Sector "+sector);
            DataGroup dg = new DataGroup(2,3);
            dg.addDataSet(datADC, 0);
            dg.addDataSet(timeFADC, 1);
            dg.addDataSet(datTDC, 2);
            this.getDataGroup().add(dg,sector,layer,0);
        }
            DataGroup dg = new DataGroup(1,1);      
            H2F mipADC = new H2F("mipADC"+sector, "sec" +sector+" MIP", 60, 1., 90000., 60, 1., 120000.);
            mipADC.setTitleY("ECin+ECout ADC SUM");
            mipADC.setTitleX("PCAL ADC SUM");
            mipADC.setTitle("Sector "+sector);
            dg.addDataSet(mipADC,0);
            this.getDataGroup().add(dg,sector,0,0);
        }
             
    }
    
    public void updateNormGraph(H1F h, String name, double off) {
    	double norm = h.getIntegral()/6;
    	this.getDetectorSummary().getGraph(name).reset();
    	GraphErrors g = h.getGraph();
    	for (int i=0; i<g.getDataSize(0); i++) {
    		double    x = g.getDataX(i)+off;
    		double    y = (norm>0) ? g.getDataY(i)/norm:0.;
    		double yerr = (norm>0&&g.getDataY(i)>0) ? g.getDataY(i)/norm*Math.sqrt(1/g.getDataY(i)+1/norm):0.;    		
    		this.getDetectorSummary().getGraph(name).addPoint(x,y,0.,yerr);  
    	}
    }
      
    public void fillDetectorSummary() {
    	for (int i=0; i<3; i++) {
    		for (int il=0; il<3; il++) {
    			updateNormGraph(getDetectorSummary().getH2F("sum"+stacks[i]).getSlicesY().get(il),stacks[i]+views[il], (i==2)?0.2:0.0);
    		}
    	}    	
    }
    
    @Override
    public void plotHistos() {    
        
    	fillDetectorSummary();
    	
        for(int layer=1; layer <=9; layer++) {
            this.getDetectorCanvas().getCanvas("adcOccupancy").cd((layer-1)+0);
            this.getDetectorCanvas().getCanvas("adcOccupancy").getPad((layer-1)).getAxisZ().setLog(getLogZ());
            this.getDetectorCanvas().getCanvas("adcOccupancy").draw(this.getDataGroup().getItem(0,layer,0).getH2F("occADC"+layer));
            this.getDetectorCanvas().getCanvas("tdcOccupancy").cd((layer-1)+0);
            this.getDetectorCanvas().getCanvas("tdcOccupancy").getPad((layer-1)).getAxisZ().setLog(getLogZ());
            this.getDetectorCanvas().getCanvas("tdcOccupancy").draw(this.getDataGroup().getItem(0,layer,0).getH2F("occTDC"+layer));
        }
        
        for(int sector=1; sector<7; sector++) {
            this.getDetectorCanvas().getCanvas("adcSum").cd(sector-1);
            this.getDetectorCanvas().getCanvas("adcSum").getPad(sector-1).getAxisZ().setLog(getLogZ());
            this.getDetectorCanvas().getCanvas("adcSum").draw(this.getDataGroup().getItem(sector,0,0).getH2F("mipADC"+sector));
            if(getActiveSector()==sector) {
               for(int layer=1; layer <= 9; layer++) {
                   this.getDetectorCanvas().getCanvas("adcEnergy").cd((layer-1)+0);
                   this.getDetectorCanvas().getCanvas("adcEnergy").getPad((layer-1)).getAxisZ().setLog(getLogZ());
                   this.getDetectorCanvas().getCanvas("adcEnergy").draw(this.getDataGroup().getItem(sector,layer,0).getH2F("datADC"+layer+sector));
                   this.getDetectorCanvas().getCanvas("adcTime").cd((layer-1)+0);
                   this.getDetectorCanvas().getCanvas("adcTime").getPad((layer-1)).getAxisZ().setLog(getLogZ());
                   this.getDetectorCanvas().getCanvas("adcTime").draw(this.getDataGroup().getItem(sector,layer,0).getH2F("timeFADC"+layer+sector));
                   this.getDetectorCanvas().getCanvas("tdc").cd((layer-1)+0);
                   this.getDetectorCanvas().getCanvas("tdc").getPad((layer-1)).getAxisZ().setLog(getLogZ());
                   this.getDetectorCanvas().getCanvas("tdc").draw(this.getDataGroup().getItem(sector,layer,0).getH2F("datTDC"+layer+sector));
               }
        	   }
        }   
        
        this.getDetectorCanvas().getCanvas("adcOccupancy").update();
                
    }

    @Override
    public void processEvent(DataEvent event) {
        
        int triggerPhase=0;
        if(event.hasBank("RUN::config")) {
            DataBank bank = event.getBank("RUN::config");
            int runNumber  = bank.getInt("run", 0);
            long timestamp = bank.getLong("timestamp",0);    
            IndexedTable jitter = this.getCcdb().getConstants(runNumber, "/calibration/ec/time_jitter");
            this.period  = jitter.getDoubleValue("period",0,0,0);
            this.phase   = jitter.getIntValue("phase",0,0,0);
            this.ncycles = jitter.getIntValue("cycles",0,0,0);           
//            System.out.println(period + phase + ncycles + " " + timestamp + " " + triggerPhase0);
            if(ncycles>0){
                triggerPhase  = (int) ((timestamp+phase)%ncycles); // TI derived phase correction due to TDC and FADC clock differences
            }
        }

        double[] pcsum = new double[6];
        double[] ecsum = new double[6];
    	        
        if(event.hasBank("ECAL::adc")==true){        	
        	DataBank bank = event.getBank("ECAL::adc");
	        int rows = bank.rows();
            for(int loop = 0; loop < rows; loop++){
                int sector = bank.getByte("sector", loop);
                int layer  = bank.getByte("layer", loop);
                int comp   = bank.getShort("component", loop);
                int adc    = bank.getInt("ADC", loop);
                float time = bank.getFloat("time",loop);
                if(adc>0 && time>=0) {
                  	this.getDataGroup().getItem(0,layer,0).getH2F("occADC"+layer).fill(sector*1.0, comp*1.0);
                  	this.getDataGroup().getItem(sector,layer,0).getH2F("datADC"+layer+sector).fill(adc,comp*1.0);
                    if(time > 1) this.getDataGroup().getItem(sector,layer,0).getH2F("timeFADC"+layer+sector).fill(time,comp*1.0);
                }
                if (layer<4) pcsum[sector-1]+=adc; //raw adcSum in PCAL
                if (layer>3) ecsum[sector-1]+=adc; //raw adcSum in EC
                if(adc>0 && time>=0) {
                    if(layer <= 3)               this.getDetectorSummary().getH2F("sumPCAL").fill(sector, layer);
                    if(layer >= 4 && layer <= 6) this.getDetectorSummary().getH2F("sumECin").fill(sector, layer-3);
                    if(layer >= 6 && layer <= 9) this.getDetectorSummary().getH2F("sumECout").fill(sector, layer-6);
                }
	    }
    	    }   
        
        fillDetectorSummary();
        
    	int bitsec = getElecTriggerSector(); 
        if(bitsec>0&&bitsec<7) this.getDataGroup().getItem(bitsec,0,0).getH2F("mipADC"+bitsec).fill(pcsum[bitsec-1], ecsum[bitsec-1]);
                
        if(event.hasBank("ECAL::tdc")==true){
            DataBank  bank = event.getBank("ECAL::tdc");
            int rows = bank.rows();
            for(int i = 0; i < rows; i++){
                int    sector = bank.getByte("sector",i);
                int     layer = bank.getByte("layer",i);
                int      comp = bank.getShort("component",i);
                int       TDC = bank.getInt("TDC",i);
                int     order = bank.getByte("order",i); 
                if(TDC>0) {
                    this.getDataGroup().getItem(0,layer,0).getH2F("occTDC"+layer).fill(sector*1.0, comp*1.0);
                    this.getDataGroup().getItem(sector,layer,0).getH2F("datTDC"+layer+sector).fill(TDC*this.tdcconv-triggerPhase*this.period,comp*1.0);
                }
//                if(layer==1)      this.getDetectorSummary().getH1F("sumPCAL").fill(sector*1.0);
//                else if (layer==2)this.getDetectorSummary().getH1F("sumECin").fill(sector*1.0);
//                else              this.getDetectorSummary().getH1F("sumECout").fill(sector*1.0);
            }
        }
    }

    @Override
    public void resetEventListener() {
        System.out.println("Resetting EC histogram");
        this.createHistos();
        this.plotHistos();
    }

    @Override
    public void analysisUpdate() {
 //       System.out.println("Updating FTOF canvas");
    }


}
