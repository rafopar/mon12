package org.clas.detectors;

import org.clas.viewer.DetectorMonitor;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 *
 * @author rafopar
 */
public class uRWellMonitor extends DetectorMonitor {

    final int nCutLvls = 5;
    final ArrayList<Double> ADC_Thresholds = new ArrayList<>(Arrays.asList(5., 5.5, 6., 6.5, 7.));
    final double strip_alpha = Math.toRadians(10.); // The strip angle in radians
    final double Y_0 = 250 + 723 * Math.tan(strip_alpha);
    final double pitch = 1.; // mm

    Map<Integer, Double> m_ped_mean = new HashMap<>();
    Map<Integer, Double> m_ped_rms = new HashMap<>();

    public uRWellMonitor(String name) {
        super(name);
        this.setDetectorTabNames("1D Hits", "Crosses");

        //EmbeddedCanvas canvas = this.getDetectorCanvas().getCanvas("Hits");
        this.init(false);

        /*
         * Let's read the pedestal file and fill and fill the pedestald and RMS arrays
         */
        String fname = "PedFiles/CosmicPeds.dat";
        //String fname = "PedFiles/HallPeds.dat";
        try {
            FileInputStream fstream = new FileInputStream(fname);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            System.out.println("***************** Reading Pedestals for uRWELL *****************");
            while ((strLine = br.readLine()) != null) {
                String[] splited = strLine.split("\\s+");
                //System.out.println("Ch is " + splited[1] + " Ped is " + splited[2] + " PedErr is " + splited[3]);
                int ch = Integer.parseInt(splited[1]);
                double ped = Double.parseDouble(splited[2]);
                double pedErr = Double.parseDouble(splited[3]);
                m_ped_mean.put(ch, ped);
                m_ped_rms.put(ch, pedErr);
            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("Error: the file " + fname + " is not found.");
        }

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


        this.getDetectorCanvas().getCanvas("Crosses").divide(3, 2);
        this.getDetectorCanvas().getCanvas("Crosses").setGridX(false);
        this.getDetectorCanvas().getCanvas("Crosses").setGridY(false);

        ArrayList<H2F> arr_Crosses = new ArrayList<>();
        
        DataGroup dg_Crosses = new DataGroup(3, 2);
        for (int iCut = 0; iCut < nCutLvls; iCut++) {
            arr_Crosses.add(new H2F(String.format("Crosses_%d", iCut), String.format("Crosses_%d", iCut), 200, -900., 900., 200, -500., 500.));
            dg_Crosses.addDataSet(arr_Crosses.get(iCut), iCut);
        }

        this.getDataGroup().add(dg_1DHits, 0, 0, 0);
        this.getDataGroup().add(dg_Crosses, 0, 0, 2);
    }

    @Override
    public void plotHistos() {

        for (int iCut = 0; iCut < nCutLvls; iCut++) {
            this.getDetectorCanvas().getCanvas("1D Hits").cd(iCut);
            System.out.println("Datagroup.show() is");
            this.getDataGroup().show();
            this.getDetectorCanvas().getCanvas("1D Hits").draw(this.getDataGroup().getItem(0, 0, 0).getH1F("1dHits_" + iCut));
        }
        
        for (int iCut1 = 0; iCut1 < nCutLvls; iCut1++) {
            this.getDetectorCanvas().getCanvas("Crosses").cd(iCut1);
            this.getDetectorCanvas().getCanvas("Crosses").cd(iCut1).draw(this.getDataGroup().getItem(0, 0, 2).getH2F("Crosses_" + iCut1));
        }

    }

    @Override
    public void processEvent(DataEvent event) {

        System.out.println("Inside uRWELL PRocessing");
        int n_Banks = event.getBankList().length;
        System.out.println("The number of banks is " + n_Banks);
        
        if (event.hasBank("URWELL::adc") == true) {

            //System.out.println("The uRWELL bank exists");
            DataBank buRWellADC = event.getBank("URWELL::adc");

            int strip_U = -1;
            int strip_V = -1;

            double Max_U_ADCRel = 0; // Maxmim relative adc among U strips
            double Max_V_ADCRel = 0; // Maxmim relative adc among V strips

            for (int i = 0; i < buRWellADC.rows(); i++) {
                int sector = buRWellADC.getInt("sector", i);
                int layer = buRWellADC.getInt("layer", i);
                int channel = buRWellADC.getInt("component", i);
                int ADC = buRWellADC.getInt("ADC", i);
                int uniqueChan = (int) (buRWellADC.getFloat("time", i));
                int ts = buRWellADC.getInt("ped", i);

                int slot = layer;

                if (sector != 6) {
                    // Those are unphysical channels, are not connected to any strip.
                    continue;
                }

                double ADC_Rel = (m_ped_mean.get(uniqueChan) - ADC) / m_ped_rms.get(uniqueChan);

                for (int iCut = 0; iCut < nCutLvls; iCut++) {
                    if (ADC_Rel > ADC_Thresholds.get(iCut)) {
                        this.getDataGroup().getItem(0, 0, 0).getH1F("1dHits_" + iCut).fill(uniqueChan);
                    }
                }

                if (layer == 1 /* U layer */) {
                    if (ADC_Rel > Max_U_ADCRel) {
                        Max_U_ADCRel = ADC_Rel;
                        strip_U = channel;
                    }
                } else if (layer == 2 /* V layer */) {
                    if (ADC_Rel > Max_V_ADCRel) {
                        Max_V_ADCRel = ADC_Rel;
                        strip_V = channel;
                    }
                }

            }

            if (strip_U >= 0 && strip_V >= 0) {
                double crs_x = pitch * (strip_U - strip_V) / (2 * Math.sin(strip_alpha));
                double crs_y = Math.tan(strip_alpha) * crs_x + Y_0 - (strip_U * pitch) / Math.cos(strip_alpha);

                for (int iCut = 0; iCut < nCutLvls; iCut++) {
                    if (Max_U_ADCRel > ADC_Thresholds.get(iCut) && Max_V_ADCRel > ADC_Thresholds.get(iCut) ) {
                        this.getDataGroup().getItem(0, 0, 2).getH2F("Crosses_" + iCut).fill(crs_x, crs_y);
                    }
                }

            }

        }
    }

}
