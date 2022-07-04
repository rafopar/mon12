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

public class RASTERmonitor extends DetectorMonitor {
    
    private static final int NLAYERS = 36;
    private static final int NWIRES  = 112;
    
    public RASTERmonitor(String name) {
        super(name);
        this.setDetectorTabNames("R1occupancy");
        this.init(false);
        this.getCcdb().setVariation("default");
        this.getCcdb().init(Arrays.asList(new String[]{"/calibration/raster/adc_to_position"}));
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
        
        for(int sector=1; sector <= 6; sector++) {
            
            H2F eve = new H2F("eve" + sector, "Sector " + sector, 50, -1, 1, 50, -1, 1);
            eve.setTitleX("raster x (cm)");
            eve.setTitleY("raster y (cm)");
            eve.setTitle("sector "+sector);
            
            H2F raw = new H2F("raw" + sector, "Sector " + sector, 50, -1, 1, 50, -1, 1);
            raw.setTitleX("raster x (cm)");
            raw.setTitleY("raster y (cm)");
            raw.setTitle("sector "+sector);
            
            H2F occ = new H2F("occ" + sector, "Sector " + sector + " Occupancy vs. raster xy", 50, -1, 1, 50, -1, 1);
            occ.setTitleX("raster x (cm)");
            occ.setTitleY("raster y (cm)");
            occ.setTitle("sector "+sector);
            
            DataGroup dg = new DataGroup(1,3);
            dg.addDataSet(eve, 1);
            dg.addDataSet(raw, 2);
            dg.addDataSet(occ, 3);
            this.getDataGroup().add(dg, sector,0,0);
        }
        
       
        
    }
        
    @Override
    public void plotHistos() {
        // initialize canvas and plot histograms
    	    
        this.getDetectorCanvas().getCanvas("R1occupancy").divide(3, 2);
        this.getDetectorCanvas().getCanvas("R1occupancy").setGridX(false);
        this.getDetectorCanvas().getCanvas("R1occupancy").setGridY(false);
       
        for(int sector=1; sector <=6; sector++) {
            this.getDetectorCanvas().getCanvas("R1occupancy").cd(sector-1);
            this.getDetectorCanvas().getCanvas("R1occupancy").draw(this.getDataGroup().getItem(sector,0,0).getH2F("occ"+ sector));
        }

        this.getDetectorCanvas().getCanvas("R1occupancy").update();
    }

    @Override
    public void processEvent(DataEvent event) {
                
        // process event info and save into data group
        IndexedTable adc2position = null;
        if(event.hasBank("RUN::config")) {
            DataBank bank = event.getBank("RUN::config");
            int runNumber = bank.getInt("run", 0);
            adc2position  = this.getCcdb().getConstants(runNumber, "/calibration/raster/adc_to_position");
        }
        else {
            return;
        }
        double rasterX = -999;
        double rasterY = -999;
        if(event.hasBank("RASTER::adc") && adc2position!=null) {
            DataBank raster = event.getBank("RASTER::adc");
            for(int i=0; i<raster.rows(); i++) {
                int component = raster.getShort("component", i);
                int adc       = raster.getInt("ped", i);
                if(component == 1) rasterX = this.convertADC(adc2position, component, adc);
                if(component == 2) rasterY = this.convertADC(adc2position, component, adc);
            }
            for(int sector=1; sector <=6; sector++) this.getDataGroup().getItem(sector,0,0).getH2F("eve"+ sector).fill(rasterX, rasterY);
        
            if(event.hasBank("DC::tdc")==true){
                DataBank  bank = event.getBank("DC::tdc");
                int rows = bank.rows();

                for(int i = 0; i < rows; i++){
                    int    sector = bank.getByte("sector",i);
                    int     layer = bank.getByte("layer",i);
                    int        sl = (int) (layer-1)/6+1;
                    if(sl<=2) this.getDataGroup().getItem(sector,0,0).getH2F("raw"+ sector).fill(rasterX, rasterY);
                }
            }
        }   
    }

    @Override
    public void analysisUpdate() {
//        System.out.println("Updating DC");
        if(this.getNumberOfEvents()>0) {
            for(int sector=1; sector <=6; sector++) {
                H2F eve = this.getDataGroup().getItem(sector,0,0).getH2F("eve"+sector);
                H2F raw = this.getDataGroup().getItem(sector,0,0).getH2F("raw"+sector);
                for(int loop = 0; loop < raw.getDataBufferSize(); loop++){
                    if(eve.getDataBufferBin(loop)>0) this.getDataGroup().getItem(sector,0,0).getH2F("occ"+sector).setDataBufferBin(loop,100.0*raw.getDataBufferBin(loop)/NWIRES/(NLAYERS/3)/eve.getDataBufferBin(loop));
                }
                this.getDetectorCanvas().getCanvas("R1occupancy").getPad(sector-1).getAxisZ().setRange(0.0, max_occ);
            }
        }
    }
    
    private double convertADC(IndexedTable adc2pos, int component, int ADC) {
        double pos = adc2pos.getDoubleValue("p0", 0, 0, component)+
                     adc2pos.getDoubleValue("p1", 0, 0, component)*ADC;
        return pos;
    }

}
