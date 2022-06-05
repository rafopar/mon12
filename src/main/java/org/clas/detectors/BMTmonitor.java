package org.clas.detectors;

import java.util.Arrays;
import java.util.List;

import org.clas.viewer.DetectorMonitor;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.groot.data.H1F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.utils.groups.IndexedTable;

public class BMTmonitor extends DetectorMonitor {

    int sparseSample;
    int numberOfSamples;
    int samplingTime;

    int maxNumberLayers;
    int maxNumberSectors;
    int maxNumberStrips;
    int numberStrips[];

    boolean isZ[];
    boolean mask[][][];

    int numberOfHitsPerDetector[][];

    int runNumber = 0;
    int defaultRunNumber = 2284;

    public BMTmonitor(String name) {
        super(name);

        this.loadConstantsFromCCDB(defaultRunNumber);

        this.setDetectorTabNames("cOccupancy", "zOccupancy", "tmax", "multiplicity");
        this.init(false);
    }

    public void loadConstantsFromCCDB(int runNumber) {
        List<String> tablesFitter = null;
        List<String> keysFitter = null;

        keysFitter   = Arrays.asList(new String[]{"BMTconfig"});
        tablesFitter = Arrays.asList(new String[]{"/daq/config/bmt"});
        this.getCcdb().init(keysFitter, tablesFitter);

        IndexedTable bmtConfig = this.getCcdb().getConstants(runNumber, "BMTconfig");

        this.sparseSample = bmtConfig.getIntValue("sparse", 0, 0, 0);
        this.numberOfSamples = (bmtConfig.getIntValue("number_sample", 0, 0, 0) - 1) * (this.sparseSample + 1) + 1;
        this.samplingTime = (byte) bmtConfig.getDoubleValue("sampling_time", 0, 0, 0);

        DatabaseConstantProvider dbprovider = new DatabaseConstantProvider(runNumber, "default");
        dbprovider.loadTable("/geometry/cvt/mvt/bmt_layer");

        this.maxNumberLayers = dbprovider.length("/geometry/cvt/mvt/bmt_layer/Layer");
        this.maxNumberSectors = dbprovider.getInteger("/geometry/cvt/mvt/bmt_layer/Nsector", 0);
        this.maxNumberStrips = 0;
        this.numberStrips = new int[maxNumberLayers + 1];
        this.isZ = new boolean[maxNumberLayers + 1];
        for (int layer = 1; layer < maxNumberLayers + 1; layer++) {
            this.numberStrips[layer] = dbprovider.getInteger("/geometry/cvt/mvt/bmt_layer/Nstrip", (layer - 1));
            if (this.numberStrips[layer] > this.maxNumberStrips) {
                this.maxNumberStrips = this.numberStrips[layer];
            }
            if (dbprovider.getInteger("/geometry/cvt/mvt/bmt_layer/Axis", (layer - 1)) == 1) {
                this.isZ[layer] = true;
            } else {
                this.isZ[layer] = false;
            }
        }
        dbprovider.disconnect();
        
        this.mask = new boolean[maxNumberSectors + 1][maxNumberLayers + 1][maxNumberStrips + 1];

        for (int sector = 1; sector <= maxNumberSectors; sector++) {
            for (int layer = 1; layer <= maxNumberLayers; layer++) {
                for (int component = 1; component <= numberStrips[layer]; component++) {
                    this.mask[sector][layer][component] = true;
                }
            }
        }

        this.numberOfHitsPerDetector = new int[maxNumberSectors + 1][maxNumberLayers + 1];
    }

    @Override
    public void createHistos() {

        // create histograms
        this.setNumberOfEvents(0);

        H1F summary = new H1F("summary", "summary", maxNumberSectors * maxNumberLayers, 0.5, maxNumberSectors * maxNumberLayers + 0.5);
        summary.setTitleX("detector");
        summary.setTitleY("occupancy");
        summary.setTitle("BMT");
        summary.setFillColor(38);
        DataGroup sum = new DataGroup(1, 1);
        sum.addDataSet(summary, 0);
        this.setDetectorSummary(sum);
        
        H1F histmulti = new H1F("multi", "multi", 100, -0.5, 999.5);
        histmulti.setTitleX("hit multiplicity");
        histmulti.setTitleY("counts");
        histmulti.setTitle("multiplicity of BMT channels");
        histmulti.setFillColor(34);
        histmulti.setOptStat("111110");
        DataGroup occupancyGroup = new DataGroup("");
        occupancyGroup.addDataSet(histmulti, 0);
        this.getDataGroup().add(occupancyGroup, 0, 0, 0);

        for (int sector = 1; sector <= maxNumberSectors; sector++) {
            for (int layer = 1; layer <= maxNumberLayers; layer++) {
                H1F hitmapHisto = new H1F("Occupancy Layer " + layer + " Sector " + sector, "Occupancy Layer " + layer + " Sector " + sector + " (detector " + (3 * (layer - 1) + sector) + ")",
                        (numberStrips[layer]) + 1, 0., (double) (numberStrips[layer]) + 1);
                hitmapHisto.setTitleX("Strips (Layer " + layer + " Sector " + sector + ")");
                hitmapHisto.setTitleY("Nb of hits");
                if (isZ[layer]) {
                    hitmapHisto.setFillColor(4);
                } else {
                    hitmapHisto.setFillColor(8);
                }
                DataGroup hitmapGroup = new DataGroup("");
                hitmapGroup.addDataSet(hitmapHisto, 0);
                this.getDataGroup().add(hitmapGroup, sector, layer, 2);

                H1F tmaxHisto = new H1F("TimeOfMax : Layer " + layer + " Sector " + sector, "TimeOfMax : Layer " + layer + " Sector " + sector,
                        samplingTime * numberOfSamples, 1., samplingTime * numberOfSamples);
                tmaxHisto.setTitleX("Time of max (Layer " + layer + " Sector " + sector + ")");
                tmaxHisto.setTitleY("Nb hits");
                if (isZ[layer]) {
                    tmaxHisto.setFillColor(4);
                } else {
                    tmaxHisto.setFillColor(8);
                }
                DataGroup timeOfMaxGroup = new DataGroup("");
                timeOfMaxGroup.addDataSet(tmaxHisto, 0);
                this.getDataGroup().add(timeOfMaxGroup, sector, layer, 1);
            }
        }

    }

    @Override
    public void plotHistos() {

        this.getDetectorCanvas().getCanvas("cOccupancy").divide(maxNumberSectors, maxNumberLayers / 2);
        this.getDetectorCanvas().getCanvas("cOccupancy").setGridX(false);
        this.getDetectorCanvas().getCanvas("cOccupancy").setGridY(false);
        this.getDetectorCanvas().getCanvas("cOccupancy").setAxisTitleSize(12);
        this.getDetectorCanvas().getCanvas("cOccupancy").setAxisLabelSize(12);

        this.getDetectorCanvas().getCanvas("zOccupancy").divide(maxNumberSectors, maxNumberLayers / 2);
        this.getDetectorCanvas().getCanvas("zOccupancy").setGridX(false);
        this.getDetectorCanvas().getCanvas("zOccupancy").setGridY(false);
        this.getDetectorCanvas().getCanvas("zOccupancy").setAxisTitleSize(12);
        this.getDetectorCanvas().getCanvas("zOccupancy").setAxisLabelSize(12);

        this.getDetectorCanvas().getCanvas("tmax").divide(maxNumberSectors, maxNumberLayers);
        this.getDetectorCanvas().getCanvas("tmax").setGridX(false);
        this.getDetectorCanvas().getCanvas("tmax").setGridY(false);
        this.getDetectorCanvas().getCanvas("tmax").setAxisTitleSize(12);
        this.getDetectorCanvas().getCanvas("tmax").setAxisLabelSize(12);

        this.getDetectorCanvas().getCanvas("multiplicity").setGridX(false);
        this.getDetectorCanvas().getCanvas("multiplicity").setGridY(false);
        this.getDetectorCanvas().getCanvas("multiplicity").setAxisTitleSize(24);
        this.getDetectorCanvas().getCanvas("multiplicity").setAxisLabelSize(18);
        this.getDetectorCanvas().getCanvas("multiplicity").setTitleSize(18);
        this.getDetectorCanvas().getCanvas("multiplicity").setStatBoxFontSize(18);
        
        for (int sector = 1; sector <= maxNumberSectors; sector++) {
            for (int layer = 1; layer <= maxNumberLayers; layer++) {
                int column = maxNumberSectors - sector;
                int row;
                int numberOfColumns = maxNumberSectors;
                switch (layer) {
                    case 1:
                        row = 2;
                        break;
                    case 2:
                        row = 2;
                        break;
                    case 3:
                        row = 1;
                        break;
                    case 4:
                        row = 1;
                        break;
                    case 5:
                        row = 0;
                        break;
                    case 6:
                        row = 0;
                        break;
                    default:
                        row = -1;
                        break;
                }
                if (isZ[layer]) {
                    this.getDetectorCanvas().getCanvas("zOccupancy").cd(column + numberOfColumns * row);
                    this.getDetectorCanvas().getCanvas("zOccupancy").draw(
                            this.getDataGroup().getItem(sector, layer, 2).getH1F("Occupancy Layer " + layer + " Sector " + sector));
                } else {
                    this.getDetectorCanvas().getCanvas("cOccupancy").cd(column + numberOfColumns * row);
                    this.getDetectorCanvas().getCanvas("cOccupancy").draw(
                            this.getDataGroup().getItem(sector, layer, 2).getH1F("Occupancy Layer " + layer + " Sector " + sector));
                }
                switch (layer) {
                    case 1:
                        row = 5;
                        break;
                    case 2:
                        row = 2;
                        break;
                    case 3:
                        row = 1;
                        break;
                    case 4:
                        row = 4;
                        break;
                    case 5:
                        row = 0;
                        break;
                    case 6:
                        row = 3;
                        break;
                    default:
                        row = -1;
                        break;
                }

                this.getDetectorCanvas().getCanvas("tmax").cd(column + numberOfColumns * row);
                this.getDetectorCanvas().getCanvas("tmax").draw(
                        this.getDataGroup().getItem(sector, layer, 1).getH1F("TimeOfMax : Layer " + layer + " Sector " + sector));
            }
        }
        this.getDetectorCanvas().getCanvas("multiplicity").draw(this.getDataGroup().getItem(0, 0, 0).getH1F("multi"));
        
        this.getDetectorCanvas().getCanvas("zOccupancy").update();
        this.getDetectorCanvas().getCanvas("cOccupancy").update();
        this.getDetectorCanvas().getCanvas("tmax").update();
        this.getDetectorCanvas().getCanvas("multiplicity").update();
    }

    public void processEvent(DataEvent event) {

        if (event.hasBank("BMT::adc") == true) {
            DataBank bank = event.getBank("BMT::adc");

            this.getDataGroup().getItem(0, 0, 0).getH1F("multi").fill(bank.rows());

            for (int i = 0; i < bank.rows(); i++) {
                int sector = bank.getByte("sector", i);
                int layer = bank.getByte("layer", i);
                int strip = bank.getShort("component", i);
                float timeOfMax = bank.getFloat("time", i);

//				if (strip < 0 || !mask[sector][layer][strip]){
//					continue;
//				}
                this.getDataGroup().getItem(sector, layer, 2).getH1F("Occupancy Layer " + layer + " Sector " + sector).fill(strip);
                if ((samplingTime < timeOfMax) && (timeOfMax < samplingTime * (numberOfSamples - 1))) {
                    this.getDataGroup().getItem(sector, layer, 1).getH1F("TimeOfMax : Layer " + layer + " Sector " + sector).fill(timeOfMax);
                }
                this.numberOfHitsPerDetector[sector][layer]++;
                this.getDetectorSummary().getH1F("summary").setBinContent(maxNumberSectors * (layer - 1) + (sector - 1), (double) this.numberOfHitsPerDetector[sector][layer] / ((double) this.getNumberOfEvents()));
            }
        }
    }
}
