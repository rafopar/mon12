package org.clas.detectors;

import org.clas.viewer.DetectorMonitor;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author gotra
 */

public class BSTmonitor extends DetectorMonitor {

    private final int NREGIONS = 3;
    private final int NLAYERS = 6;
    private final int[] sectors = new int[]{10, 10, 14, 14, 18, 18};
    private final double[] occupNorm = new double[]{215.04, 25.6, 25.6, 35.84, 35.84, 46.08, 46.08}; // #strips / 100 (to convert to %)

    public BSTmonitor(String name) {
        super(name);
        this.setDetectorTabNames("Hit Maps", "Layer Hits", "Hit Multiplicity");
        addCanvas("Hit Maps", NREGIONS, 2, false, true);
        addCanvas("Layer Hits", NREGIONS, 2, true, false);
        addCanvas("Hit Multiplicity", NREGIONS + 1, 2, false, false);
        this.init(false);
    }

    @Override
    public void createHistos() {
        // create histograms
        this.setNumberOfEvents(0);
        H2F summary = new H2F("summary", "summary", 256, 0.5, 256.5, 84, 0.5, 84.5);
        summary.setTitleX("Strip");
        summary.setTitleY("Module");
        summary.setTitle("BST");
        DataGroup sum = new DataGroup(1, 1);
        sum.addDataSet(summary, 0);
        this.setDetectorSummary(sum);

        double hitallhigh = 999.5;
        double hithigh = 199.5;
        double hitlow = -0.5;
        int nbinshit = 100;
        int nbinshitall = 100;
        
        for (int i = 0; i < NLAYERS; ++i) {
            DataGroup dgLayer = new DataGroup(1,3);
            int nBins = sectors[i];
            H2F hhitmap = new H2F("hitmap_l" + (i + 1), "BST Layer " + (i + 1), 256, 0.5, 256.5, nBins, 0.5, nBins + 0.5);
            hhitmap.setTitleX("Strip");
            hhitmap.setTitleY("Sector");
            nBins = sectors[i] * 256;
            H1F hhitl = new H1F("hits_l" + (i + 1), "BST Layer " + (i + 1), nBins, 0.5, nBins + 0.5);
            hhitl.setTitleX("Channel");
            hhitl.setTitleY("Counts");
            H1F hmulti = new H1F("bstmulti_l" + (i + 1), nbinshit, hitlow, hithigh);
            hmulti.setTitleX("BST Layer " + (i + 1) + " Multiplicity");
            hmulti.setTitleY("Counts");
            hmulti.setLineWidth(2);
            hmulti.setFillColor(34);
            hmulti.setOptStat("111110");
            dgLayer.addDataSet(hhitmap, 0);
            dgLayer.addDataSet(hhitl,   1);
            dgLayer.addDataSet(hmulti,  2);
            this.getDataGroup().add(dgLayer, 0, i+1, 0);
        }

        DataGroup dgAll = new DataGroup(1,2);
        H1F hoccup = new H1F("occup", "", 7, -0.5, 6.5);
        hoccup.setTitleX("BST All Layers");
        hoccup.setTitleY("BST Occupancy (%)");
        hoccup.setLineWidth(2);
        hoccup.setFillColor(33);
        H1F hmulti = new H1F("bstmulti", "", nbinshitall, hitlow, hitallhigh);
        hmulti.setTitleX("BST All Layers Multiplicity");
        hmulti.setTitleY("Counts");
        hmulti.setLineWidth(2);
        hmulti.setFillColor(33);
        hmulti.setOptStat("111110");
        dgAll.addDataSet(hoccup, 0);
        dgAll.addDataSet(hmulti, 1);
        this.getDataGroup().add(dgAll, 0, 0, 0);
    }

    @Override
    public void plotHistos() {
        EmbeddedCanvas canvas = this.getDetectorCanvas().getCanvas("Hit Maps");
        for (int i = 0; i < NREGIONS; ++i) {
            canvas.cd(i);
            int j = 2 * i + 1;
            canvas.draw(this.getDataGroup().getItem(0, j+1, 0).getH2F("hitmap_l" + (j + 1)));
            canvas.cd(i + NREGIONS);
            canvas.draw(this.getDataGroup().getItem(0, j, 0).getH2F("hitmap_l" + j));
        }
        canvas.update();

        canvas = this.getDetectorCanvas().getCanvas("Layer Hits");
        for (int i = 0; i < NREGIONS; ++i) {
            canvas.cd(i);
            int j = 2 * i + 1;
            canvas.draw(this.getDataGroup().getItem(0, j+1, 0).getH1F("hits_l" + (j + 1)));
            canvas.cd(i + NREGIONS);
            canvas.draw(this.getDataGroup().getItem(0, j, 0).getH1F("hits_l" + j));
        }
        canvas.update();

        canvas = this.getDetectorCanvas().getCanvas("Hit Multiplicity");
        for (int i = 0; i < NREGIONS; ++i) {
            canvas.cd(i);
            int j = 2 * i + 1;
            canvas.draw(this.getDataGroup().getItem(0, j+1, 0).getH1F("bstmulti_l" + (j + 1)));
            canvas.cd(i + NREGIONS + 1);
            canvas.draw(this.getDataGroup().getItem(0, j, 0).getH1F("bstmulti_l" + j));
        }
        canvas.cd(3);
        canvas.draw(this.getDataGroup().getItem(0, 0, 0).getH1F("bstmulti"));
        canvas.cd(7);
        canvas.draw(this.getDataGroup().getItem(0, 0, 0).getH1F("occup"));
        canvas.update();
    }

    @Override
    public void processEvent(DataEvent event) {

        boolean[] trigger_bits1 = new boolean[32];
        boolean[] trigger_bits2 = new boolean[32];
        if (event.hasBank("RUN::trigger")) {
            DataBank bank = event.getBank("RUN::trigger");
            int TriggerWord1 = bank.getInt("trigger", 0); // first bank
            int TriggerWord2 = bank.getInt("trigger", 1); // second bank
            for (int i = 31; i >= 0; i--) {
                trigger_bits1[i] = (TriggerWord1 & (1 << i)) != 0;
                trigger_bits2[i] = (TriggerWord2 & (1 << i)) != 0;
            }
        }

        if (trigger_bits2[7] || trigger_bits1[31]) {
            return; // random trigger, skip event
        }
        int bsthits = 0;
        int[] hits = {0, 0, 0, 0, 0, 0};

        if (event.hasBank("BST::adc") == true) {
            DataBank bank = event.getBank("BST::adc");
            this.getDetectorOccupancy().addTDCBank(bank);
            int rows = bank.rows();

            for (int i = 0; i < rows; i++) {
                int adc = bank.getInt("ADC", i);
                if (adc < 0) {
                    continue; // TDC hits not counted
                }
                int sector = bank.getByte("sector", i);
                int layer = bank.getByte("layer", i);
                int comp = bank.getShort("component", i);

                bsthits++;
                hits[layer - 1]++;
                this.getDataGroup().getItem(0, layer, 0).getH2F("hitmap_l" + layer).fill(comp, sector);
                this.getDataGroup().getItem(0, layer, 0).getH1F("hits_l" + layer)
                        .fill((sector - 1) * 256 + comp);
                int shift = 0;
                for (int l = 0; l < layer - 1; ++l) {
                    shift += sectors[l];
                }
                this.getDetectorSummary().getH2F("summary").fill(comp, sector + shift);
            } // adc loop
        } // BST::adc
        this.getDataGroup().getItem(0, 0, 0).getH1F("bstmulti").fill(bsthits);
        double occup = this.getDataGroup().getItem(0, 0, 0).getH1F("bstmulti").getMean();
        occup /= occupNorm[0];
        this.getDataGroup().getItem(0, 0, 0).getH1F("occup").setBinContent(0, occup);
        for (int l = 1; l <= 6; ++l) {
            this.getDataGroup().getItem(0, l, 0).getH1F("bstmulti_l" + l).fill(hits[l - 1]);
            occup = this.getDataGroup().getItem(0, l, 0).getH1F("bstmulti_l" + l).getMean();
            occup /= occupNorm[l];
            this.getDataGroup().getItem(0, 0, 0).getH1F("occup").setBinContent(l, occup);
        }
    }

    private void addCanvas(String title, int rows, int columns, boolean yLog, boolean zLog) {
        EmbeddedCanvas canvas = this.getDetectorCanvas().getCanvas(title);
        canvas.divide(rows, columns);
        canvas.setGridX(false);
        canvas.setGridY(false);
        canvas.setTitleSize(18);
        canvas.setAxisTitleSize(24);
        canvas.setAxisLabelSize(18);
        canvas.setStatBoxFontSize(18);
        if (yLog) {
            for (int i = 0; i < canvas.getCanvasPads().size(); ++i) {
                canvas.getPad(i).getAxisY().setLog(true);
            }
        }
        if (zLog) {
            for (int i = 0; i < canvas.getCanvasPads().size(); ++i) {
                canvas.getPad(i).getAxisZ().setLog(true);
            }
        }
    }

    @Override
    public void analysisUpdate() {
    }

}
