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

public class DCmonitor extends DetectorMonitor {
    

    public DCmonitor(String name) {
        super(name);
        this.setDetectorTabNames("occupancy", "occupancyNorm", "occupancyPercent", "multiplicity", "tdc2d", "tdc1d_s");
        this.useSectorButtons(true);
        this.init(false);
    }

    
    @Override
    public void createHistos() {
        // create histograms
        this.setNumberOfEvents(0);
        H1F summary = new H1F("summary","summary",6,0.5,6.5);
        summary.setTitleX("sector");
        summary.setTitleY("DC occupancy");
        summary.setTitle("DC");
        summary.setFillColor(33);
        DataGroup sum = new DataGroup(1,1);
        sum.addDataSet(summary, 0);
        this.setDetectorSummary(sum);
        
        H1F raw_summary = new H1F("raw_summary","raw_summary",6,0.5,6.5);
        
        
        for(int sector=1; sector <= 6; sector++) {
            H2F raw = new H2F("raw_sec" + sector, "Sector " + sector + " Occupancy", 112, 0.5, 112.5, 36, 0.5, 36.5);
            raw.setTitleX("wire");
            raw.setTitleY("layer");
            raw.setTitle("sector "+sector);
            
            H2F occ = new H2F("occ_sec" + sector, "Sector " + sector + " Occupancy", 112, 0.5, 112.5, 36, 0.5, 36.5);
            occ.setTitleX("wire");
            occ.setTitleY("layer");
            occ.setTitle("sector "+sector);
            
            H1F reg_occ = new H1F("reg_occ_sec" + sector, "Sector " + sector + " region Occupancy", 3, 0.5, 3.5);
            reg_occ.setTitleX("region");
            reg_occ.setTitleY("occupancy %");
            reg_occ.setTitle("sector "+sector);
            reg_occ.setFillColor(3);
            
            H1F raw_reg_occ = new H1F("raw_reg_occ_sec" + sector, "Sector " + sector + " region Occupancy", 3, 0.5, 3.5);
            raw_reg_occ.setTitleX("region");
            raw_reg_occ.setTitleY("counts");
            raw_reg_occ.setTitle("sector "+sector);
            
            H2F tdc_raw = new H2F("tdc_raw" + sector, "Sector " + sector + " TDC raw distribution", 404, 0, 2020, 36, 0.5, 36.5);
            tdc_raw.setTitleX("tdc raw");
            tdc_raw.setTitleY("layer");
            tdc_raw.setTitle("sector "+sector);
            
            for(int sl=1; sl<=6; sl++) {
                H1F tdc_sl_raw = new H1F("tdc_sl_raw" + sector+ sl, "Sector " + sector + " Superlayer " + sl + " TDC spectrum", 404, 0, 2020);
                tdc_sl_raw.setFillColor(3);
                DataGroup dg_sl = new DataGroup(1,1);
                dg_sl.addDataSet(tdc_sl_raw, 0);
                this.getDataGroup().add(dg_sl, sector, sl, 0);
            }
            
            H1F mult = new H1F("multiplicity_sec"+ sector, "Multiplicity sector "+ sector, 200, 0., 1000);
            mult.setTitleX("hit multiplicity");
            mult.setTitleY("counts");
            mult.setTitle("multiplicity sector " + sector);
            mult.setFillColor(3);
            
            DataGroup dg = new DataGroup(6,1);
            dg.addDataSet(raw, 0);
            dg.addDataSet(occ, 1);
            dg.addDataSet(reg_occ, 2);
            dg.addDataSet(raw_reg_occ, 3);
            dg.addDataSet(tdc_raw, 4);
            dg.addDataSet(mult, 5);
            dg.addDataSet(raw_summary, 6);
            this.getDataGroup().add(dg, sector,0,0);
        }
        
       
        
    }
        
    @Override
    public void plotHistos() {
        // initialize canvas and plot histograms
    	    
        this.getDetectorCanvas().getCanvas("occupancy").divide(2, 3);
        this.getDetectorCanvas().getCanvas("occupancy").setGridX(false);
        this.getDetectorCanvas().getCanvas("occupancy").setGridY(false);
        this.getDetectorCanvas().getCanvas("occupancyNorm").divide(2, 3);
        this.getDetectorCanvas().getCanvas("occupancyNorm").setGridX(false);
        this.getDetectorCanvas().getCanvas("occupancyNorm").setGridY(false);
//        this.getDetectorCanvas().getCanvas("Raw Occupancies").divide(2, 3);
//        this.getDetectorCanvas().getCanvas("Raw Occupancies").setGridX(false);
//        this.getDetectorCanvas().getCanvas("Raw Occupancies").setGridY(false);
        this.getDetectorCanvas().getCanvas("occupancyPercent").divide(2, 3);
        this.getDetectorCanvas().getCanvas("occupancyPercent").setGridX(false);
        this.getDetectorCanvas().getCanvas("occupancyPercent").setGridY(false);
        this.getDetectorCanvas().getCanvas("tdc2d").divide(2, 3);
        this.getDetectorCanvas().getCanvas("tdc2d").setGridX(false);
        this.getDetectorCanvas().getCanvas("tdc2d").setGridY(false);
        this.getDetectorCanvas().getCanvas("tdc1d").divide(2, 3);
        this.getDetectorCanvas().getCanvas("tdc1d").setGridX(false);
        this.getDetectorCanvas().getCanvas("tdc1d").setGridY(false);
        this.getDetectorCanvas().getCanvas("multiplicity").divide(2, 3);
        this.getDetectorCanvas().getCanvas("multiplicity").setGridX(false);
        this.getDetectorCanvas().getCanvas("multiplicity").setGridY(false);
        
        for(int sector=1; sector <=6; sector++) {
            this.getDetectorCanvas().getCanvas("occupancy").getPad(sector-1).getAxisZ().setRange(0.01, max_occ);
            this.getDetectorCanvas().getCanvas("occupancy").getPad(sector-1).getAxisZ().setLog(getLogZ());
            this.getDetectorCanvas().getCanvas("occupancy").cd(sector-1);
            this.getDetectorCanvas().getCanvas("occupancy").draw(this.getDataGroup().getItem(sector,0,0).getH2F("occ_sec"+sector));
            this.getDetectorCanvas().getCanvas("occupancyNorm").getPad(sector-1).getAxisZ().setRange(0.01, max_occ);
            this.getDetectorCanvas().getCanvas("occupancyNorm").getPad(sector-1).getAxisZ().setLog(!getLogZ());
            this.getDetectorCanvas().getCanvas("occupancyNorm").cd(sector-1);
            this.getDetectorCanvas().getCanvas("occupancyNorm").draw(this.getDataGroup().getItem(sector,0,0).getH2F("occ_sec"+sector));
//            this.getDetectorCanvas().getCanvas("Raw Occupancies").getPad(sector-1).getAxisZ().setLog(getLogZ());
//            this.getDetectorCanvas().getCanvas("Raw Occupancies").cd(sector-1);
//            this.getDetectorCanvas().getCanvas("Raw Occupancies").draw(this.getDataGroup().getItem(sector,0,0).getH2F("raw_sec"+sector));
            this.getDetectorCanvas().getCanvas("occupancyPercent").cd(sector-1);
            this.getDetectorCanvas().getCanvas("occupancyPercent").draw(this.getDataGroup().getItem(sector,0,0).getH1F("reg_occ_sec"+sector));
            this.getDetectorCanvas().getCanvas("tdc2d").getPad(sector-1).getAxisZ().setLog(getLogZ());
            this.getDetectorCanvas().getCanvas("tdc2d").cd(sector-1);
            this.getDetectorCanvas().getCanvas("tdc2d").draw(this.getDataGroup().getItem(sector,0,0).getH2F("tdc_raw" + sector));
            this.getDetectorCanvas().getCanvas("multiplicity").cd(sector-1);
            this.getDetectorCanvas().getCanvas("multiplicity").draw(this.getDataGroup().getItem(sector,0,0).getH1F("multiplicity_sec"+ sector));
            if(getActiveSector()==sector) {
               for(int sl=1; sl <=6; sl++) {
                   this.getDetectorCanvas().getCanvas("tdc1d").cd(sl-1);
                   this.getDetectorCanvas().getCanvas("tdc1d").draw(this.getDataGroup().getItem(sector,sl,0).getH1F("tdc_sl_raw" + sector+ sl));
               }
            }
        }

        this.getDetectorCanvas().getCanvas("occupancy").update();
        this.getDetectorCanvas().getCanvas("occupancyNorm").update();
//        this.getDetectorCanvas().getCanvas("Raw Occupancies").update();
        this.getDetectorCanvas().getCanvas("occupancyPercent").update();
        this.getDetectorCanvas().getCanvas("tdc2d").update();
        this.getDetectorCanvas().getCanvas("multiplicity").update();
        
    }

    @Override
    public void processEvent(DataEvent event) {
                
        // process event info and save into data group
        if(event.hasBank("DC::tdc")==true){
            DataBank  bank = event.getBank("DC::tdc");
            int rows = bank.rows();
            int[] nEventSector = {0,0,0,0,0,0};
            
            for(int i = 0; i < rows; i++){
                int    sector = bank.getByte("sector",i);
                int     layer = bank.getByte("layer",i);
                int      wire = bank.getShort("component",i);
                int       TDC = bank.getInt("TDC",i);
                int     order = bank.getByte("order",i); 
                int    region = (int) (layer-1)/12+1;
                int        sl = (int) (layer-1)/6+1;
                
                this.getDataGroup().getItem(sector,0,0).getH2F("raw_sec"+sector).fill(wire*1.0,layer*1.0);                
                this.getDataGroup().getItem(sector,0,0).getH1F("raw_reg_occ_sec"+sector).fill(region * 1.0);
                this.getDataGroup().getItem(sector,0,0).getH2F("tdc_raw"+sector).fill(TDC,layer*1.0);
                this.getDataGroup().getItem(sector,sl,0).getH1F("tdc_sl_raw" + sector+ sl).fill(TDC,layer*1.0);
                //if(TDC > 0) this.getDetectorSummary().getH1F("summary").fill(sector*1.0);
                if(TDC > 0) this.getDataGroup().getItem(sector,0,0).getH1F("raw_summary").fill(sector*1.0);
                
                if(this.getDataGroup().getItem(sector,0,0).getH1F("raw_summary").getEntries()>0) {
                    this.getDetectorSummary().getH1F("summary").setBinContent(sector-1, 100*this.getDataGroup().getItem(sector,0,0).getH1F("raw_summary").getBinContent(sector-1)/this.getNumberOfEvents()/112/12/3);
                }
                
                
                nEventSector[sector-1]++;
                
//                if(sector == 1 && sec1_check == 0){
//                    this.getDataGroup().getItem(sector,0,0).getH1F("multiplicity_sec"+ sector).fill(rows);
//                    sec1_check += 1;
//                }
//                if(sector == 2 && sec2_check == 0){
//                    this.getDataGroup().getItem(sector,0,0).getH1F("multiplicity_sec"+ sector).fill(rows);
//                    sec2_check += 1;
//                }
//                if(sector == 3 && sec3_check == 0){
//                    this.getDataGroup().getItem(sector,0,0).getH1F("multiplicity_sec"+ sector).fill(rows);
//                    sec3_check += 1;
//                }
//                if(sector == 4 && sec4_check == 0){
//                    this.getDataGroup().getItem(sector,0,0).getH1F("multiplicity_sec"+ sector).fill(rows);
//                    sec4_check += 1;
//                }
//                if(sector == 5 && sec5_check == 0){
//                    this.getDataGroup().getItem(sector,0,0).getH1F("multiplicity_sec"+ sector).fill(rows);
//                    sec5_check += 1;
//                }
//                if(sector == 6 && sec6_check == 0){
//                    this.getDataGroup().getItem(sector,0,0).getH1F("multiplicity_sec"+ sector).fill(rows);
//                    sec6_check += 1;
//                }              
            }
            for(int sec=1; sec<=6; sec++) this.getDataGroup().getItem(sec,0,0).getH1F("multiplicity_sec"+ sec).fill(nEventSector[sec-1]*1.0);
            
       }   
    }

    @Override
    public void analysisUpdate() {
//        System.out.println("Updating DC");
        if(this.getNumberOfEvents()>0) {
            for(int sector=1; sector <=6; sector++) {
                H2F raw = this.getDataGroup().getItem(sector,0,0).getH2F("raw_sec"+sector);
                for(int loop = 0; loop < raw.getDataBufferSize(); loop++){
                    this.getDataGroup().getItem(sector,0,0).getH2F("occ_sec"+sector).setDataBufferBin(loop,100*raw.getDataBufferBin(loop)/this.getNumberOfEvents());
                }
            }
        }
     
        
        if(this.getNumberOfEvents()>0) {
            int entries = 0;
            for(int sector=1; sector <=6; sector++) {
              H1F raw_check = this.getDataGroup().getItem(sector,0,0).getH1F("raw_reg_occ_sec"+sector);
              entries += raw_check.getEntries();
            }
 
            for(int sector=1; sector <=6; sector++) {
            	H1F raw = this.getDataGroup().getItem(sector,0,0).getH1F("raw_reg_occ_sec"+sector);
                H1F ave = this.getDataGroup().getItem(sector,0,0).getH1F("reg_occ_sec"+sector);
                if(entries>0) {
                for(int loop = 0; loop < 3; loop++){
                    ave.setBinContent(loop, 100*raw.getBinContent(loop)/this.getNumberOfEvents()/112/12);
                }
                }
                this.getDetectorCanvas().getCanvas("occupancy").getPad(sector-1).getAxisZ().setRange(0.01, max_occ);
                this.getDetectorCanvas().getCanvas("occupancyNorm").getPad(sector-1).getAxisZ().setRange(0.01, max_occ);
                
            }
        }   
    }

}
