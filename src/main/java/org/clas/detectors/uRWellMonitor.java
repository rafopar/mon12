package org.clas.detectors;

import org.clas.viewer.DetectorMonitor;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import java.util.*;

/**
 *
 * @author rafopar
 */
public class uRWellMonitor extends DetectorMonitor {

    final int nCutLvls = 5;
    final ArrayList<Double> ADC_Thresholds = new ArrayList<>(Arrays.asList(5., 5.5, 6., 6.5, 7.));

    public uRWellMonitor(String name) {
        super(name);
        this.setDetectorTabNames("1D Hits", "2D Hits");

        EmbeddedCanvas canvas = this.getDetectorCanvas().getCanvas("Hits");
        this.init(false);
        
        /*
         * Let's read the pedestal file and fill and fill the pedestald and RMS arrays
         */
        
        
    }

    @Override
    public void createHistos() {
        this.setNumberOfEvents(0);
        this.getDetectorCanvas().getCanvas("1D Hits").divide(3, 2);
        this.getDetectorCanvas().getCanvas("1D Hits").setGridX(false);
        this.getDetectorCanvas().getCanvas("1D Hits").setGridY(false);

        ArrayList<H1F> arr_1dHits = new ArrayList<>();

        DataGroup dg_1DHits = new DataGroup(3, 2);
        for (int iCut = 0; iCut < nCutLvls; iCut++) {
            arr_1dHits.add(new H1F(String.format("1dHits_%d", iCut), String.format("1dHits_%d", iCut), 1711, -0.5, 1710.5));

            dg_1DHits.addDataSet(arr_1dHits.get(iCut), iCut);
        }

    }

    @Override
    public void plotHistos() {

        for (int iCut = 0; iCut < nCutLvls; iCut++) {
            this.getDetectorCanvas().getCanvas("1D Hits").cd(iCut);
            this.getDetectorCanvas().getCanvas("1D Hits").draw(this.getDataGroup().getItem(0, 0, 0).getH1F("1dHits_" + iCut));

        }

    }

    @Override
    public void processEvent(DataEvent event) {
        if (event.hasBank("URWELL::adc") == true) {
            DataBank buRWellADC = event.getBank("URWELL::adc");

            for (int i = 0; i < buRWellADC.rows(); i++) {
                int sector = buRWellADC.getInt("sector", i);
                int layer = buRWellADC.getInt("layer", i);
                int channel = buRWellADC.getInt("component", i);
                int ADC = buRWellADC.getInt("ADC", i);
                int uniqueChan = (int)(buRWellADC.getFloat("time", i));
                int ts = buRWellADC.getInt("ped", i);

                int slot = layer;

                if (sector != 6) {
                    // Those are unphysical channels, are not connected to any strip.
                    continue;
                }

            }
        }
    }

}
