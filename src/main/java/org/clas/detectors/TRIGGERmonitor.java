package org.clas.detectors;


import org.clas.viewer.DetectorMonitor;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.math.F1D;
import org.jlab.io.base.DataEvent;
import org.jlab.groot.graphics.EmbeddedCanvas;

import java.util.ArrayList;
import java.util.List;

import org.jlab.utils.groups.IndexedList;
import org.jlab.utils.groups.IndexedList.IndexGenerator;
import java.util.Map;

public class TRIGGERmonitor extends DetectorMonitor {
	
	H1F h1;
	public IndexedList<F1D>  trigfits = new IndexedList<F1D>(2); 
	
	
//	String tbit = "Trigger Bits: EC.PC.HTCC(0)    EC.PC.HTCC(1-6)    EC.PC OR noDC>300(7)  EC.PC>10(8)    FTOF.PC.EC(19-21)    FT.* (24-27)    1K Pulser(31)";
	String tbit = "Trigger Bits: EC.PC.HTCC(0)    EC.PC.HTCC(1-6)    FTOFxPCALxECALxDC14(7)   FTOFxPCALxECALxDC25(8)   FTOFxPCALxECALxDC36(9)   Electron OR no DC(10)  1K Pulser(31)";
    int bit[][] = {{1,2,3,4,5,6},{7,8,9,10,11,12}};
	double[] refm = new double[2];
	double[] refs = new double[2];
	boolean datataking = false;
	
    public TRIGGERmonitor(String name) {
        super(name);
        this.setDetectorTabNames("Trigger Bits","FTOF1A Fits","FTOF1A Summary");
        this.useSectorButtons(false);
        this.getDetectorCanvas().setActiveCanvas("Trigger Bits");
        this.init(false);
        this.testTrigger = true;
    }
   
    @Override
    public void createHistos() {
        this.setNumberOfEvents(0);
         createTriggerBits(0);
         createFTOFHistos(1,50,50,200,"tdc","MTIME (ns)" );   
    }
    
    @Override
    public void plotHistos() {
    	setLogY(true); plot1DSummary(0); setLogY(false);
        plot1DSummary(1); 
        if (datataking) {
          fillFTOFGraphs();
          plotFTOFGraphs(2);
        }
    }    
       
    public void createTriggerBits(int k) {
        H1F summary = new H1F("summary","Trigger Bits", 32,-0.5,31.5);
        summary.setFillColor(4);
        summary.setTitleX("Trigger Bits");
        summary.setTitleY("Counts");
        DataGroup sum = new DataGroup(1,1);
        sum.addDataSet(summary, 0);
        createFTOFGraphs(sum); 
        this.setDetectorSummary(sum);
        initFTOFGraphs(); updateFTOFGraphTitles();
       
        H1F trig = new H1F(tbit,tbit, 32,-0.5,31.5);
        trig.setFillColor(4);      
        trig.setTitleX("Trigger Bits");
        trig.setTitleY("Counts");
        
        DataGroup dg = new DataGroup(1,1);        
        dg.addDataSet(trig, 0);
        this.getDataGroup().add(dg, 0,0,k);   	
    }
    
    public void createFTOFHistos(int k, int nch, double x1, double x2, String var, String txt) {
 	
        DataGroup dg = new DataGroup(6,4);
        String rdo[] = {"MEAN TIME","FADC TIME"};
        
        for (int il=0; il<2; il++) {
        for (int id=0; id<2; id++) {
        for (int is=1; is<7; is++) {
            String tag = var+"_"+is+"_"+id+"_"+il+"_"+k;
            h1 = new H1F("dat_"+tag,"dat_"+tag, nch, x1, x2);
            h1.setTitle("Trig Bit "+bit[il][is-1]);
            h1.setTitleX("Sector 1 "+" "+rdo[id]); 
            h1.setOptStat("1000000");
            dg.addDataSet(h1,is-1+id*6+il*12);   
            F1D fit = new F1D("fit_"+tag,"[amp]*gaus(x,[mean],[sigma])+[p0]", 60, 160);
            fit.getAttributes().setOptStat("1100"); fit.setLineColor(2); fit.setLineWidth(2);
            dg.addDataSet(fit,is-1+id*6+il*12); 
        }
        }
        }
        
        this.getDataGroup().add(dg,0,0,k);
    }
    
    public void createFTOFGraphs(DataGroup dg) {
    	F1D f1 = new F1D("A","[a]",0,13); f1.setParameter(0,0); f1.setLineColor(3); f1.setLineWidth(3);   	
    	F1D f2 = new F1D("R","[a]",0,13); f2.setParameter(0,1); f2.setLineColor(3); f2.setLineWidth(3);   	
    	dg.addDataSet(new GraphErrors("TAT"),0); dg.addDataSet(f1,0); 
     	dg.addDataSet(new GraphErrors("TRT"),0); dg.addDataSet(f2,0);
    	dg.addDataSet(new GraphErrors("TAF"),0);  
    	dg.addDataSet(new GraphErrors("TRF"),0);    	
    }
    
    public void initFTOFGraphs() {
    	getDetectorSummary().getGraph("TAT").setTitleX("Trigger Bit");
    	getDetectorSummary().getGraph("TRT").setTitleX("Trigger Bit");
    	getDetectorSummary().getGraph("TAF").setTitleX("Trigger Bit");
    	getDetectorSummary().getGraph("TRF").setTitleX("Trigger Bit");
    	getDetectorSummary().getGraph("TAT").setTitleY("TDC Mean - REF (ns)");
    	getDetectorSummary().getGraph("TRT").setTitleY("TDC Sigma/REF");
    	getDetectorSummary().getGraph("TAF").setTitleY("FADC Mean - REF (ns)");
    	getDetectorSummary().getGraph("TRF").setTitleY("FADC Sigma/REF");
    }
    
    public void updateFTOFGraphTitles() {
    	getDetectorSummary().getGraph("TAT").setTitle("Ref Time(Bit 1) = "+String.format("%5.1f",refm[0]));
    	getDetectorSummary().getGraph("TRT").setTitle("Ref Sigm(Bit 1) = "+String.format("%5.2f",refs[0])); 
    	getDetectorSummary().getGraph("TAF").setTitle("Ref Time(Bit 1) = "+String.format("%5.1f",refm[1]));   
    	getDetectorSummary().getGraph("TRF").setTitle("Ref Sigm(Bit 1) = "+String.format("%5.2f",refs[1])); 
    }
    
    public void plotFTOFGraphs(int k) {
      	EmbeddedCanvas c = getDetectorCanvas().getCanvas(getDetectorTabNames().get(k));   
        c.divide(2,2);
   	    c.cd(0); c.getPad(0).setAxisRange(0,13,-10, 10); c.draw(this.getDetectorSummary().getGraph("TAT"));
   	                                                     c.draw(this.getDetectorSummary().getF1D("A"),"same");
   	    c.cd(1); c.getPad(1).setAxisRange(0,13,  0,  5); c.draw(this.getDetectorSummary().getGraph("TRT"));
                                                         c.draw(this.getDetectorSummary().getF1D("R"),"same");
  	   	c.cd(2); c.getPad(2).setAxisRange(0,13,-10, 10); c.draw(this.getDetectorSummary().getGraph("TAF"));
                                                         c.draw(this.getDetectorSummary().getF1D("A"),"same");
  	   	c.cd(3); c.getPad(3).setAxisRange(0,13,  0,  5); c.draw(this.getDetectorSummary().getGraph("TRF")); 
                                                         c.draw(this.getDetectorSummary().getF1D("R"),"same");
  	   	updateFTOFGraphTitles();
  	   	c.repaint();
  	   
  	}

    public void plotTriggerBits(int index) {
      	EmbeddedCanvas c = getDetectorCanvas().getCanvas(getDetectorTabNames().get(index));    	
    	if(this.getDataGroup().getItem(0,0,0).getH1F(tbit)==null) return; //lcs To prevent exceptions if histo missing from archive
        c.divide(1, 1);
        c.setGridX(false);
        c.setGridY(false);
        c.cd(0);
        c.getPad().getAxisY().setLog(true);
        c.draw(this.getDataGroup().getItem(0,0,0).getH1F(tbit));
        c.update(); 	
    }
    
    public void plot1DSummary(int index) {
      	drawGroup(getDetectorCanvas().getCanvas(getDetectorTabNames().get(index)),getDataGroup().getItem(0,0,index));
    }
    
    @Override
    public void processEvent(DataEvent event) {

        for (int i=0; i<32; i++) if(isTrigBitSet(i)) ((H1F) this.getDataGroup().getItem(0,0,0).getData(0).get(0)).fill(i);
        for (int i=0; i<32; i++) if(isTrigBitSet(i)) this.getDetectorSummary().getH1F("summary").fill(i);
        
        getFTOFMeanTime();
        getFTOFFADCTime();  
        
        datataking = true;

    } 
    
    public void fillFTOFGraphs() {
    
       
  	   this.getDetectorSummary().getGraph("TAT").reset();
	   this.getDetectorSummary().getGraph("TRT").reset();
	   this.getDetectorSummary().getGraph("TAF").reset();
	   this.getDetectorSummary().getGraph("TRF").reset();
	      	
        for (int t=1; t<13; t++) {
           boolean good = trigfits.hasItem(1,0)&&trigfits.hasItem(t,0);
           List<Double> dif0 = getFitDiff(t,0,good);   
           this.getDetectorSummary().getGraph("TAT").addPoint(t, dif0.get(0), 0, dif0.get(1));
           List<Double> rat0 = getFitRatio(t,0,good);  
           this.getDetectorSummary().getGraph("TRT").addPoint(t, rat0.get(0), 0, rat0.get(1));
           List<Double> dif1 = getFitDiff(t,1,good);   
           this.getDetectorSummary().getGraph("TAF").addPoint(t, dif1.get(0), 0, dif1.get(1));
           List<Double> rat1 = getFitRatio(t,1,good);  
           this.getDetectorSummary().getGraph("TRF").addPoint(t, rat1.get(0), 0, rat1.get(1));
        }
   	
    }
    
    public List<Double> getFitDiff(int t, int k, boolean good) {
        List<Double> result = new ArrayList<Double>();      
        if(!good) {result.add(0.);result.add(0.); return result;}
        double d1=trigfits.getItem(t,k).parameter(1).value(); 
        double d2=trigfits.getItem(1,k).parameter(1).value(); 
        double e1=trigfits.getItem(t,k).parameter(1).error(); 
        double e2=trigfits.getItem(1,k).parameter(1).error(); 
        result.add(d1-d2);       	
        result.add(Math.sqrt(e1*e1+e2*e2));
        refm[k]=d2;
        return result;   	
    }
    
    public List<Double> getFitRatio(int t, int k, boolean good) {
        List<Double> result = new ArrayList<Double>();
        if(!good) {result.add(1.);result.add(0.); return result;}
        double d1=Math.abs(trigfits.getItem(t,k).parameter(2).value()); 
        double d2=Math.abs(trigfits.getItem(1,k).parameter(2).value()); 
        double e1=trigfits.getItem(t,k).parameter(2).error(); 
        double e2=trigfits.getItem(1,k).parameter(2).error(); 
        result.add(d1/d2);       	
        result.add((d1/d2)*Math.sqrt(e1*e1/(d1*d1)+e2*e2/(d2*d2)));
        refs[k]=d2;

        return result;   	
    } 
    
    public void getFTOFMeanTime() {

        IndexGenerator ig = new IndexGenerator();
       
        int isref = 1, ilref=1, ipref=4;
        int off=-1, nt=0, isbit=-1; 
       
        for (int is=1; is<7; is++) {
    	  for (int ik=0; ik<2; ik++) {
    		if(isTrigBitSet(bit[ik][is-1])) {off=ik*12; nt++; isbit=is;} //trigger bit counter
          }
        }
    	
        if (nt!=1) return;
        
    	for (Map.Entry<Long,List<Integer>>  entry : ftpmt.getMap().entrySet()){
            long hash = entry.getKey();
            int is = ig.getIndex(hash, 0);
            int il = ig.getIndex(hash, 1);
            int ip = ig.getIndex(hash, 2);
            
            if (is==isref && il==ilref && ip==ipref) {            
          	if(ttdcs.hasItem(is,il,0,ip)&&ttdcs.hasItem(is,il,1,ip)) {
         	   float td = 0.5f*(ttdcs.getItem(is,il,0,ip).get(0)+ttdcs.getItem(is,il,1,ip).get(0));         	   
         	   ((H1F) this.getDataGroup().getItem(0,0,1).getData(isbit-1+off).get(0)).fill(td);         	           	   
         	}
            }
        } 
              
    } 
    
    public void getFTOFFADCTime() {

        IndexGenerator ig = new IndexGenerator();
        
        int isref = 1, ilref=1, ipref=4;
        int off=-1, nt=0, isbit=-1;
       
        for (int is=1; is<7; is++) {
    	  for (int ik=0; ik<2; ik++) {
    		if(isTrigBitSet(bit[ik][is-1])) {off=ik*12; nt++; isbit=is;} //trigger bit counter
          }
        }
       
        if (nt!=1) return; 
        
    	for (Map.Entry<Long,List<Integer>>  entry : fapmt.getMap().entrySet()){
            long hash = entry.getKey();
            int is = ig.getIndex(hash, 0);
            int il = ig.getIndex(hash, 1);
            int ip = ig.getIndex(hash, 2);
            
            if (is==isref && il==ilref && ip==ipref) {            
        	if(ftdcs.hasItem(is,il,0,ip)&&ftdcs.hasItem(is,il,1,ip)) {
         	   float td = 0.5f*(ftdcs.getItem(is,il,0,ip).get(0)+ftdcs.getItem(is,il,1,ip).get(0));         	   
         	   ((H1F) this.getDataGroup().getItem(0,0,1).getData(isbit-1+6+off).get(0)).fill(td);
         	}
            }
        }       
    } 
    
    public void getFits() {
    	trigfits.clear();
        for (int il=0; il<2; il++) { //0=electron bits 1=muon bits
        for (int id=0; id<2; id++) { //0=tdc 1=fadc
        for (int is=1; is<7; is++) { //sector
        String tag = "tdc"+"_"+is+"_"+id+"_"+il+"_"+1; 	          
    	trigfits.add(this.fit(this.getDataGroup().getItem(0,0,1).getH1F("dat_"+tag),
   			                  this.getDataGroup().getItem(0,0,1).getF1D("fit_"+tag)),is+il*6,id);
        }
        }
        }        
    }
    
    public F1D fit(H1F h, F1D f) {
        double mean  = h.getDataX(h.getMaximumBin());
        double amp   = h.getBinContent(h.getMaximumBin());
        double sigma = h.getRMS();
        f.setParameter(0, amp);
        f.setParameter(1, mean);
        f.setParameter(2, 3);
        f.setParameter(3, 2);
//        f.setRange(mean-3.*sigma,mean+3.*sigma);
        f.setRange(60,160);
        DataFitter.fit(f, h, "Q"); //No options uses error for sigma  
        return f;
    }
    
    @Override
    public void analysisUpdate() { 
    	if(!datataking) return;
        getFits();
        fillFTOFGraphs();
        plotFTOFGraphs(2);
    }

    
}
