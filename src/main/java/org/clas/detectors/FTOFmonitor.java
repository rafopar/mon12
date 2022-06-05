package org.clas.detectors;

import org.clas.viewer.DetectorMonitor;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.view.DetectorShape2D;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jlab.utils.groups.IndexedList.IndexGenerator;
import java.util.Map;
import org.jlab.utils.groups.IndexedTable;

public class FTOFmonitor  extends DetectorMonitor {

    private final int[] npaddles = new int[]{23,62,5};
    

    FTOFHits          ftofHits[] = new FTOFHits[3];    
    
    public FTOFmonitor(String name) {
        super(name);
        
        this.setDetectorTabNames("adcOccupancy", "tdcOccupancy","adcEnergySector", "adcTimeSector", "tdcSector","miscSector");
        this.useSectorButtons(true);
        this.init(false);   // set to true for picture on left side
        ftofHits[0] = new FTOFHits("PANEL1A");
        ftofHits[1] = new FTOFHits("PANEL1B");
        ftofHits[2] = new FTOFHits("PANEL2");
        this.getCcdb().setVariation("default");
        this.getCcdb().init(Arrays.asList(new String[]{"/calibration/ftof/time_jitter"}));
    }

    @Override
    public void createHistos() {
        // initialize canvas and create histograms
        this.setNumberOfEvents(0);
        this.getDetectorCanvas().getCanvas("adcOccupancy").divide(2, 3);
        this.getDetectorCanvas().getCanvas("adcOccupancy").setGridX(false);
        this.getDetectorCanvas().getCanvas("adcOccupancy").setGridY(false);
        this.getDetectorCanvas().getCanvas("tdcOccupancy").divide(2, 3);
        this.getDetectorCanvas().getCanvas("tdcOccupancy").setGridX(false);
        this.getDetectorCanvas().getCanvas("tdcOccupancy").setGridY(false);
        this.getDetectorCanvas().getCanvas("adcEnergy").divide(2, 3);
        this.getDetectorCanvas().getCanvas("adcEnergy").setGridX(false);
        this.getDetectorCanvas().getCanvas("adcEnergy").setGridY(false);
        this.getDetectorCanvas().getCanvas("adcTime").divide(2, 3);
        this.getDetectorCanvas().getCanvas("adcTime").setGridX(false);
        this.getDetectorCanvas().getCanvas("adcTime").setGridY(false);
        this.getDetectorCanvas().getCanvas("tdc").divide(2, 3);
        this.getDetectorCanvas().getCanvas("tdc").setGridX(false);
        this.getDetectorCanvas().getCanvas("tdc").setGridY(false);
        this.getDetectorCanvas().getCanvas("misc").divide(2, 3);
        this.getDetectorCanvas().getCanvas("misc").setGridX(false);
        this.getDetectorCanvas().getCanvas("misc").setGridY(false);
        
        String[] stacks = new String[]{"P1A","P1B","P2"};
        String[] views = new String[]{"Left","Right"};   
        
        H1F sumStackp1 = new H1F("sum_p1","sum_p1",6,0.5,6.5);
        sumStackp1.setTitleX("sector");
        sumStackp1.setTitleY("Counts");
        sumStackp1.setTitle("FTOF panel 1b");
        sumStackp1.setFillColor(34);
        H1F sumStackp2 = new H1F("sum_p2","sum_p2",6,0.5,6.5);
        sumStackp2.setTitleX("sector");
        sumStackp2.setTitleY("Counts");
        sumStackp2.setTitle("FTOF panel 1A");
        sumStackp2.setFillColor(39);
        H1F sumStackp3 = new H1F("sum_p3","sum_p3",6,0.5,6.5);
        sumStackp3.setTitleX("sector");
        sumStackp3.setTitleY("Counts");
        sumStackp3.setTitle("FTOF panel 2");
        sumStackp3.setFillColor(38);
            
        DataGroup sum = new DataGroup(3,1); 
        sum.addDataSet(sumStackp1, 0);
        sum.addDataSet(sumStackp2, 1);
        sum.addDataSet(sumStackp3, 2);
        this.setDetectorSummary(sum);
        
        for(int lay=0; lay < 3; lay++) {
            DataGroup dg = new DataGroup(1,2);
        	for(int ord=0; ord < 2; ord++) {
            H2F occADC = new H2F("occADC"+lay+ord, "lay/ord " + lay + ord + " Occupancy", 6, 0.5, 6.5, npaddles[lay], 1, npaddles[lay]+1);
            occADC.setTitle(stacks[lay]+" "+views[ord]+" PMTS");
            occADC.setTitleY("paddle");
            occADC.setTitleX("sector");
            H2F occTDC = new H2F("occTDC"+lay+ord, "lay/ord " + lay + ord + " Occupancy", 6, 0.5, 6.5, npaddles[lay], 1, npaddles[lay]+1);
            occTDC.setTitle(stacks[lay]+" "+views[ord]+" PMTS");
            occTDC.setTitleY("paddle");
            occTDC.setTitleX("sector");           
            dg.addDataSet(occADC, 0);
            dg.addDataSet(occTDC, 1);
            this.getDataGroup().add(dg,0,lay,0);
        }
        }
        
        for(int sec=1; sec < 7; sec++) {
        for(int lay=0; lay < 3; lay++) {
            DataGroup dg = new DataGroup(2,3);
        	for(int ord=0; ord < 2; ord++) {
            H2F datADC = new H2F("datADC"+sec+lay+ord, "sec/lay/ord "+sec+lay+ord+" ADC", 100, 0., 4000., npaddles[lay], 1, npaddles[lay]+1);
            datADC.setTitleY(stacks[lay] + " " + views[ord] + " PMTS");
            datADC.setTitleX("ADC Channel");
            datADC.setTitle("Sector "+sec);
            H2F timeFADC = new H2F("timeFADC"+sec+lay+ord, "sec/lay/ord "+sec+lay+ord+" FADC", 80, 0., 400., npaddles[lay], 1, npaddles[lay]+1);
            timeFADC.setTitleY(stacks[lay] + " " + views[ord] + " PMTS");
            timeFADC.setTitleX("adcTime");
            timeFADC.setTitle("Sector "+sec);
            H2F datTDC = new H2F("datTDC"+sec+lay+ord, "sec/lay/ord "+sec+lay+ord+" TDC", 100, 0., 600., npaddles[lay], 1, npaddles[lay]+1);
            datTDC.setTitleY(stacks[lay] + " " + views[ord] + " PMTS");
            datTDC.setTitleX("TDC Channel");
            datTDC.setTitle("Sector "+sec);
            dg.addDataSet(datADC, 0);
            dg.addDataSet(timeFADC, 1);
            dg.addDataSet(datTDC, 2);
        }
            H2F misc = new H2F("misc"+sec+lay, "sec/lay"+sec+lay+" misc", 100, 0., 6000.,this.npaddles[lay], 1, npaddles[lay]+1 );
            misc.setTitleY(stacks[lay] +" PMTS");
            misc.setTitleX("misc");
            misc.setTitle("Sector "+sec);
            dg.addDataSet(misc, 3);
            H2F TDIF = new H2F("TDIF"+sec+lay, "sec/lay"+sec+lay+" TDIF", 100, -40., 40.,this.npaddles[lay], 1, npaddles[lay]+1 );
            TDIF.setTitleY(stacks[lay] +" PMTS");
            TDIF.setTitleX("TLeft-TRight");
            TDIF.setTitle("Sector "+sec);
            dg.addDataSet(TDIF, 4);
            this.getDataGroup().add(dg,sec,lay,0);
        }
        }
    }

    @Override
    
    public void drawDetector() {
        
        double FTOFSize = 500.0;
        int[]     widths   = new int[]{6,15,25};
        int[]     lengths  = new int[]{6,15,25};

        String[]  names    = new String[]{"FTOF 1A","FTOF 1B","FTOF 2"};
        for(int sector = 1; sector <= 6; sector++){
            double rotation = Math.toRadians((sector-1)*(360.0/6)+90.0);
            for(int layer = 1; layer <=3; layer++){
                int width  = widths[layer-1];
                int length = lengths[layer-1];
                for(int paddle = 1; paddle <= npaddles[layer-1]; paddle++){
                    DetectorShape2D shape = new DetectorShape2D();
                    shape.getDescriptor().setType(DetectorType.FTOF);
                    shape.getDescriptor().setSectorLayerComponent(sector, layer, paddle);
                    shape.createBarXY(20 + length*paddle, width);
                    shape.getShapePath().translateXYZ(0.0, 40 + width*paddle , 0.0);
                    shape.getShapePath().rotateZ(rotation);
                    this.getDetectorView().getView().addShape(names[layer-1], shape);
                }
            }
        }
        this.getDetectorView().setName("FTOF");
        this.getDetectorView().updateBox();
                
    }
            
    @Override
    public void plotHistos() {
    	
        for (int lay = 0; lay < 3; lay++) {
            for (int ord = 0; ord < 2; ord++) {
                this.getDetectorCanvas().getCanvas("adcOccupancy").cd(lay * 2 + ord);
                this.getDetectorCanvas().getCanvas("adcOccupancy").getPad(lay * 2 + ord).getAxisZ().setLog(getLogZ());
                this.getDetectorCanvas().getCanvas("adcOccupancy").draw(this.getDataGroup().getItem(0, lay, 0).getH2F("occADC" + lay + ord));
                this.getDetectorCanvas().getCanvas("tdcOccupancy").cd(lay * 2 + ord);
                this.getDetectorCanvas().getCanvas("tdcOccupancy").getPad(lay * 2 + ord).getAxisZ().setLog(getLogZ());
                this.getDetectorCanvas().getCanvas("tdcOccupancy").draw(this.getDataGroup().getItem(0, lay, 0).getH2F("occTDC" + lay + ord));
            }
        }

        for (int sec = 1; sec < 7; sec++) {
            if (getActiveSector() == sec) {
                for (int lay = 0; lay < 3; lay++) {
                    for (int ord = 0; ord < 2; ord++) {
                        this.getDetectorCanvas().getCanvas("adcEnergy").cd(lay * 2 + ord);
                        this.getDetectorCanvas().getCanvas("adcEnergy").getPad(lay * 2 + ord).getAxisZ().setLog(getLogZ());
                        this.getDetectorCanvas().getCanvas("adcEnergy").draw(this.getDataGroup().getItem(sec, lay, 0).getH2F("datADC" + sec + lay + ord));
                        this.getDetectorCanvas().getCanvas("adcTime").cd(lay * 2 + ord);
                        this.getDetectorCanvas().getCanvas("adcTime").getPad(lay * 2 + ord).getAxisZ().setLog(getLogZ());
                        this.getDetectorCanvas().getCanvas("adcTime").draw(this.getDataGroup().getItem(sec, lay, 0).getH2F("timeFADC" + sec + lay + ord));
                        this.getDetectorCanvas().getCanvas("tdc").cd(lay * 2 + ord);
                        this.getDetectorCanvas().getCanvas("tdc").getPad(lay * 2 + ord).getAxisZ().setLog(getLogZ());
                        this.getDetectorCanvas().getCanvas("tdc").draw(this.getDataGroup().getItem(sec, lay, 0).getH2F("datTDC" + sec + lay + ord));
                    }
                    this.getDetectorCanvas().getCanvas("misc").cd(lay * 2 + 0);
                    this.getDetectorCanvas().getCanvas("misc").getPad(lay * 2 + 0).getAxisZ().setLog(getLogZ());
                    this.getDetectorCanvas().getCanvas("misc").draw(this.getDataGroup().getItem(sec, lay, 0).getH2F("misc" + sec + lay));
                    this.getDetectorCanvas().getCanvas("misc").cd(lay * 2 + 1);
                    this.getDetectorCanvas().getCanvas("misc").getPad(lay * 2 + 1).getAxisZ().setLog(getLogZ());
                    this.getDetectorCanvas().getCanvas("misc").draw(this.getDataGroup().getItem(sec, lay, 0).getH2F("TDIF" + sec + lay));
                }
            }
        }
        
        this.getDetectorCanvas().getCanvas("adcOccupancy").update();
        this.getDetectorCanvas().getCanvas("tdcOccupancy").update();
        this.getDetectorCanvas().getCanvas("adcEnergy").update();
        this.getDetectorCanvas().getCanvas("tdc").update();
        this.getDetectorCanvas().getCanvas("misc").update();
    }           

    @Override
    public void processEvent(DataEvent event) {
        
	    clear(0); clear(1); clear(2); ttdcs.clear(); fadcs.clear(); ftdcs.clear(); ftpmt.clear() ; fapmt.clear();    	

	            
        int triggerPhase=0;
        if(event.hasBank("RUN::config")) {
            DataBank bank = event.getBank("RUN::config");
            int runNumber  = bank.getInt("run", 0);
            long timestamp = bank.getLong("timestamp",0);    
            IndexedTable jitter = this.getCcdb().getConstants(runNumber, "/calibration/ftof/time_jitter");
            this.period  = jitter.getDoubleValue("period",0,0,0);
            this.phase   = jitter.getIntValue("phase",0,0,0);
            this.ncycles = jitter.getIntValue("cycles",0,0,0);           
//            System.out.println(period + phase + ncycles + " " + timestamp + " " + triggerPhase0);
            if(ncycles>0){
                triggerPhase  = (int) ((timestamp+phase)%ncycles); // TI derived phase correction due to TDC and FADC clock differences
            }
        }
        
        if(event.hasBank("FTOF::tdc")==true){
            DataBank  bank = event.getBank("FTOF::tdc");
            for(int i = 0; i < bank.rows(); i++){
                int  is = bank.getByte("sector",i);
                int  il = bank.getByte("layer",i);
                int  lr = bank.getByte("order",i);                       
                int  ip = bank.getShort("component",i);
                
                float    tdcd = (float) (bank.getInt("TDC",i)*this.tdcconv-triggerPhase*this.period);
                
                int lay=il-1; int ord=lr-2;
                if(tdcd>0) {               	
                    if(!ttdcs.hasItem(is,il,ord,ip)) ttdcs.add(new ArrayList<Float>(),is,il,ord,ip);
                        ttdcs.getItem(is,il,ord,ip).add(tdcd); 
                    if(!ftpmt.hasItem(is,il,ip)) ftpmt.add(new ArrayList<Integer>(),is,il,ip);
                 	    ftpmt.getItem(is,il,ip).add(ip);                                	
                   this.getDataGroup().getItem(0,lay,0).getH2F("occTDC"+lay+ord).fill(is,ip);
                   this.getDataGroup().getItem(is,lay,0).getH2F("datTDC"+is+lay+ord).fill(tdcd,ip);
                   storeTDCHits(lay,is-1,ord,ip,(float)(tdcd));
                }
            }
        }	    
	    	    
        if(event.hasBank("FTOF::adc")==true){
            DataBank  bank = event.getBank("FTOF::adc");            
            for(int i = 0; i < bank.rows(); i++){
                int  is = bank.getByte("sector",i);
                int  il = bank.getByte("layer",i);
                int  lr = bank.getByte("order",i);
                int  ip = bank.getShort("component",i);
                int ADC = bank.getInt("ADC",i);
                float t = bank.getFloat("time",i);               
                int ped = bank.getShort("ped", i);
                
                if(ADC>0) {
                if(!fadcs.hasItem(is,il,lr,ip)) fadcs.add(new ArrayList<Float>(),is,il,lr,ip);
                    fadcs.getItem(is,il,lr,ip).add((float) ADC); 
                if(!ftdcs.hasItem(is,il,lr,ip)) ftdcs.add(new ArrayList<Float>(),is,il,lr,ip);
                    ftdcs.getItem(is,il,lr,ip).add((float) t); 
                if(!fapmt.hasItem(is,il,ip)) fapmt.add(new ArrayList<Integer>(),is,il,ip);
                    fapmt.getItem(is,il,ip).add(ip);              
           
                int lay=il-1; int ord=lr-0;
                
                this.getDataGroup().getItem(0,lay,0).getH2F("occADC"+lay+ord).fill(is,ip);
                this.getDataGroup().getItem(is,lay,0).getH2F("datADC"+is+lay+ord).fill(ADC,ip);
                if(t > 1)   this.getDataGroup().getItem(is,lay,0).getH2F("timeFADC"+is+lay+ord).fill(t,ip);
                if(il == 2) this.getDetectorSummary().getH1F("sum_p1").fill(is);
                if(il == 1) this.getDetectorSummary().getH1F("sum_p2").fill(is); 
                if(il == 3) this.getDetectorSummary().getH1F("sum_p3").fill(is); 
                storeADCHits(lay,is-1,ord,ip,ADC,t);
                }
            }
        }

        getGMM(); 
        getTDD();

        /*
        for(int sec=1; sec<7; sec++) {
           for(int il=0; il<3; il++) {
		        getGM(il,sec-1,this.getDataGroup().getItem(sec,il,0).getH2F("misc"+sec+il));
		        getTD(il,sec-1,this.getDataGroup().getItem(sec,il,0).getH2F("TDIF"+sec+il));

           }
        }
*/        

    }
    
    public void getGMM() {
 	   
        IndexGenerator ig = new IndexGenerator();
        
        for (Map.Entry<Long,List<Integer>>  entry : fapmt.getMap().entrySet()){
            long hash = entry.getKey();
            int is = ig.getIndex(hash, 0);
            int il = ig.getIndex(hash, 1);
            int ip = ig.getIndex(hash, 2);
         	   if(fadcs.hasItem(is,il,0,ip)&&fadcs.hasItem(is,il,1,ip)) {
                 float gm = (float) Math.sqrt(fadcs.getItem(is,il,0,ip).get(0)*
                                              fadcs.getItem(is,il,1,ip).get(0));
                 this.getDataGroup().getItem(is,il-1,0).getH2F("misc"+is+(il-1)).fill(gm,ip);
         	   }
        }        
    }
    
    public void getTDD() {

        IndexGenerator ig = new IndexGenerator();
    	
    	for (Map.Entry<Long,List<Integer>>  entry : ftpmt.getMap().entrySet()){
            long hash = entry.getKey();
            int is = ig.getIndex(hash, 0);
            int il = ig.getIndex(hash, 1);
            int ip = ig.getIndex(hash, 2);
             	   
         	   if(ttdcs.hasItem(is,il,0,ip)&&ttdcs.hasItem(is,il,1,ip)) {
         		  float td = ttdcs.getItem(is,il,0,ip).get(0)-ttdcs.getItem(is,il,1,ip).get(0);
         		  this.getDataGroup().getItem(is,il-1,0).getH2F("TDIF"+is+(il-1)).fill(td, ip);
         	   }
        }       
    }     
    
    public class FTOFHits {
    	    public String  detName = null;
        int        nha[][] = new    int[6][2];
        int        nht[][] = new    int[6][2];
        int    strra[][][] = new    int[6][2][62]; 
        int    strrt[][][] = new    int[6][2][62]; 
        int     adcr[][][] = new    int[6][2][62];      
        float   tdcr[][][] = new  float[6][2][62]; 
        float     tf[][][] = new  float[6][2][62]; 
        float     ph[][][] = new  float[6][2][62]; 
        
        public FTOFHits(String det) {
        	   detName = det;
        }
    }  
    
    public void clear(int idet) {
        
        for (int is=0 ; is<6 ; is++) {
            for (int il=0 ; il<2 ; il++) {
                ftofHits[idet].nha[is][il] = 0;
                ftofHits[idet].nht[is][il] = 0;
                for (int ip=0 ; ip<62 ; ip++) {
                    ftofHits[idet].strra[is][il][ip] = 0;
                    ftofHits[idet].strrt[is][il][ip] = 0;
                    ftofHits[idet].adcr[is][il][ip]  = 0;
                    ftofHits[idet].tdcr[is][il][ip]  = 0;
                    ftofHits[idet].tf[is][il][ip]    = 0;
                    ftofHits[idet].ph[is][il][ip]    = 0;
                }
            }
        } 
    }
        
    public void storeTDCHits(int idet, int is, int il, int ip, float tdc) {

        if(tdc>0&&tdc<600){
              ftofHits[idet].nht[is][il]++; int inh = ftofHits[idet].nht[is][il];
              if (inh>npaddles[idet]) inh=npaddles[idet];            
              ftofHits[idet].tdcr[is][il][inh-1]  = tdc;
              ftofHits[idet].strrt[is][il][inh-1] = ip;                                 
        }
    }
    
    public void storeADCHits(int idet, int is, int il, int ip, int adc, float time) {
        
        if(adc>50){
              ftofHits[idet].nha[is][il]++; int inh = ftofHits[idet].nha[is][il];
              if (inh>npaddles[idet]) inh=npaddles[idet];
              ftofHits[idet].adcr[is][il][inh-1]  = adc;
              ftofHits[idet].tf[is][il][inh-1]    = time;
              ftofHits[idet].strra[is][il][inh-1] = ip;
        } 
    }
    
    public void getGM(int idet, int is, H2F h2) {
        int     iL = ftofHits[idet].nha[is][0];
        int     iR = ftofHits[idet].nha[is][1];
        int    ipL = ftofHits[idet].strra[is][0][0];
        int    ipR = ftofHits[idet].strra[is][1][0];
        double adL = ftofHits[idet].adcr[is][0][0];
        double adR = ftofHits[idet].adcr[is][1][0];
    	if ((iL==1&&iR==1)&&(ipL==ipR)) h2.fill(Math.sqrt(adL*adR), ipL*1.0);
    }
    
    public void getTD(int idet, int is, H2F h2) {
        int     iL = ftofHits[idet].nht[is][0];
        int     iR = ftofHits[idet].nht[is][1];
        int    ipL = ftofHits[idet].strrt[is][0][0];
        int    ipR = ftofHits[idet].strrt[is][1][0];
        double tdL = ftofHits[idet].tdcr[is][0][0];
        double tdR = ftofHits[idet].tdcr[is][1][0];
    	if ((iL==1&&iR==1)&&(ipL==ipR)) h2.fill(tdL-tdR, ipL*1.0);
    }    
    
    @Override
    public void analysisUpdate() {

    }


}
