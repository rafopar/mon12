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

public class FMTmonitor extends DetectorMonitor {

    int sparseSample;
    int numberOfSamples;
    int samplingTime;

    int maxNumberLayers;
    int maxNumberSectors;
    int maxNumberStrips;
    int numberStrips[];

    boolean mask[][][];

    int numberOfHitsPerDetector[][];

    int runNumber;
    int defaultRunNumber = 2284;

    public FMTmonitor(String name) {
        super(name);

        this.loadConstantsFromCCDB(defaultRunNumber);

        this.setDetectorTabNames("Occupancy", "TimeMax", "Amplitude", "Multiplicity");
        this.init(false);
    }

    public void loadConstantsFromCCDB(int runNumber) {
        List<String> tablesFitter = null;
        List<String> keysFitter = null;

        keysFitter = Arrays.asList(new String[]{"FMTconfig"});
        tablesFitter = Arrays.asList(new String[]{"/daq/config/fmt"});
        this.getCcdb().init(keysFitter, tablesFitter);

        IndexedTable bmtConfig = this.getCcdb().getConstants(runNumber, "FMTconfig");

        this.sparseSample = bmtConfig.getIntValue("sparse", 0, 0, 0);
        this.numberOfSamples = (bmtConfig.getIntValue("number_sample", 0, 0, 0) - 1) * (this.sparseSample + 1) + 1;
        this.samplingTime = (byte) bmtConfig.getDoubleValue("sampling_time", 0, 0, 0);

        DatabaseConstantProvider dbprovider = new DatabaseConstantProvider(runNumber, "default");
        dbprovider.loadTable("/geometry/fmt/fmt_layer");

        this.maxNumberLayers = dbprovider.length("/geometry/fmt/fmt_layer/Layer");

        dbprovider.loadTable("/geometry/fmt/fmt_global");

        this.maxNumberSectors = dbprovider.length("/geometry/fmt/fmt_global/N_strip");
        this.maxNumberStrips = dbprovider.getInteger("/geometry/fmt/fmt_global/N_strip", 0);
        this.numberStrips = new int[maxNumberLayers + 1];
        for (int layer = 1; layer < maxNumberLayers + 1; layer++) {
            this.numberStrips[layer] = dbprovider.getInteger("/geometry/fmt/fmt_global/N_strip", 0);
        }

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
        summary.setTitle("FMT");
        summary.setFillColor(38);
        DataGroup sum = new DataGroup(1, 1);
        sum.addDataSet(summary, 0);
        this.setDetectorSummary(sum);

        H1F histmulti = new H1F("multi", "multi", 200, -0.5, 199.5);
        histmulti.setTitleX("hit multiplicity");
        histmulti.setTitleY("counts");
        histmulti.setTitle("Multiplicity of FMT channels");
        DataGroup occupancyGroup = new DataGroup("");
        occupancyGroup.addDataSet(histmulti, 0);
        this.getDataGroup().add(occupancyGroup, 0, 0, 0);

        for (int sector = 1; sector <= maxNumberSectors; sector++) {
            for (int layer = 1; layer <= maxNumberLayers; layer++) {
                H1F hitmapHisto = new H1F("Occupancy Layer " + layer + " Sector " + sector, "Occupancy Layer " + layer + " Sector " + sector,
                        (numberStrips[layer]) + 1, 0., (double) (numberStrips[layer]) + 1);
                hitmapHisto.setTitleX("strips (Layer " + layer + " Sector " + sector + ")");
                hitmapHisto.setTitleY("Nb of hits");
                hitmapHisto.setFillColor(4);
                DataGroup hitmapGroup = new DataGroup("");
                hitmapGroup.addDataSet(hitmapHisto, 0);
                this.getDataGroup().add(hitmapGroup, sector, layer, 2);

                H1F timeMaxHisto = new H1F("TimeOfMax : Layer " + layer + " Sector " + sector, "TimeOfMax : Layer " + layer + " Sector " + sector,
                        samplingTime * numberOfSamples, 1., samplingTime * numberOfSamples);
                timeMaxHisto.setTitleX("Time of max (Layer " + layer + " Sector " + sector + ")");
                timeMaxHisto.setTitleY("Nb hits");
                timeMaxHisto.setFillColor(4);
                DataGroup timeOfMaxGroup = new DataGroup("");
                timeOfMaxGroup.addDataSet(timeMaxHisto, 0);
                this.getDataGroup().add(timeOfMaxGroup, sector, layer, 1);

                H1F amplitudeHisto = new H1F("Amplitude : Layer " + layer + " Sector " + sector, "Amplitude : Layer " + layer + " Sector " + sector,
                        500, 0., 5000.);
                amplitudeHisto.setTitleX("Amplitude (Layer " + layer + " Sector " + sector + ")");
                amplitudeHisto.setTitleY("Nb hits");
                amplitudeHisto.setFillColor(4);
                DataGroup amplitudeGroup = new DataGroup("");
                amplitudeGroup.addDataSet(amplitudeHisto, 0);
                this.getDataGroup().add(amplitudeGroup, sector, layer, 3);

            }
        }
    }

    @Override
    public void plotHistos() {

        this.getDetectorCanvas().getCanvas("Occupancy").divide(maxNumberSectors * 2, maxNumberLayers / 2);
        this.getDetectorCanvas().getCanvas("Occupancy").setGridX(false);
        this.getDetectorCanvas().getCanvas("Occupancy").setGridY(false);
        this.getDetectorCanvas().getCanvas("Occupancy").setAxisTitleSize(12);
        this.getDetectorCanvas().getCanvas("Occupancy").setAxisLabelSize(12);

        this.getDetectorCanvas().getCanvas("TimeMax").divide(maxNumberSectors, maxNumberLayers);
        this.getDetectorCanvas().getCanvas("TimeMax").setGridX(false);
        this.getDetectorCanvas().getCanvas("TimeMax").setGridY(false);
        this.getDetectorCanvas().getCanvas("TimeMax").setAxisTitleSize(12);
        this.getDetectorCanvas().getCanvas("TimeMax").setAxisLabelSize(12);

        this.getDetectorCanvas().getCanvas("Amplitude").divide(maxNumberSectors, maxNumberLayers);
        this.getDetectorCanvas().getCanvas("Amplitude").setGridX(false);
        this.getDetectorCanvas().getCanvas("Amplitude").setGridY(false);
        this.getDetectorCanvas().getCanvas("Amplitude").setAxisTitleSize(12);
        this.getDetectorCanvas().getCanvas("Amplitude").setAxisLabelSize(12);

        for (int sector = 1; sector <= maxNumberSectors; sector++) {
            for (int layer = 1; layer <= maxNumberLayers; layer++) {
                int column = maxNumberSectors - sector;
                int row;
                int numberOfColumns = maxNumberSectors;
                switch (layer) {
                    case 1:
                        row = 5;
                        break;
                    case 2:
                        row = 4;
                        break;
                    case 3:
                        row = 3;
                        break;
                    case 4:
                        row = 2;
                        break;
                    case 5:
                        row = 1;
                        break;
                    case 6:
                        row = 0;
                        break;
                    default:
                        row = -1;
                        break;
                }
                this.getDetectorCanvas().getCanvas("Occupancy").cd(column + numberOfColumns * row);
                this.getDetectorCanvas().getCanvas("Occupancy").draw(
                        this.getDataGroup().getItem(sector, layer, 2).getH1F("Occupancy Layer " + layer + " Sector " + sector));

                this.getDetectorCanvas().getCanvas("TimeMax").cd(column + numberOfColumns * row);
                this.getDetectorCanvas().getCanvas("TimeMax").draw(
                        this.getDataGroup().getItem(sector, layer, 1).getH1F("TimeOfMax : Layer " + layer + " Sector " + sector));

                this.getDetectorCanvas().getCanvas("Amplitude").cd(column + numberOfColumns * row);
                this.getDetectorCanvas().getCanvas("Amplitude").getPad(column + numberOfColumns * row).getAxisY().setLog(true);
                this.getDetectorCanvas().getCanvas("Amplitude").draw(
                        this.getDataGroup().getItem(sector, layer, 3).getH1F("Amplitude : Layer " + layer + " Sector " + sector));
            }
        }
        this.getDetectorCanvas().getCanvas("Occupancy").update();
        this.getDetectorCanvas().getCanvas("TimeMax").update();
        this.getDetectorCanvas().getCanvas("Amplitude").update();

        this.getDetectorCanvas().getCanvas("Multiplicity").divide(1, 1);
        this.getDetectorCanvas().getCanvas("Multiplicity").setGridX(false);
        this.getDetectorCanvas().getCanvas("Multiplicity").setGridY(false);
        this.getDetectorCanvas().getCanvas("Multiplicity").cd(0);
        this.getDetectorCanvas().getCanvas("Multiplicity").draw(this.getDataGroup().getItem(0, 0, 0).getH1F("multi"));
        this.getDetectorCanvas().getCanvas("Multiplicity").update();
    }

    public void processEvent(DataEvent event) {

        if (this.getNumberOfEvents() >= super.eventResetTime_current && super.eventResetTime_current > 0) {
            resetEventListener();
        }

        if (this.runNumber == 0) {
            int numberOfEvents = this.getNumberOfEvents();
            if (event.hasBank("RUN::config")) {
                DataBank head = event.getBank("RUN::config");
                runNumber = head.getInt("run", 0);
            } else {
                runNumber = 2284;
            }
            this.loadConstantsFromCCDB(runNumber);
            this.createHistos();
            this.plotHistos();
            this.setNumberOfEvents(numberOfEvents); //Cause number of events got reset
        }

        //if (!testTriggerMask()) return;
        if (event.hasBank("FMT::adc") == true) {
            DataBank bank = event.getBank("FMT::adc");

            this.getDataGroup().getItem(0, 0, 0).getH1F("multi").fill(bank.rows());

            for (int i = 0; i < bank.rows(); i++) {
                int sector = bank.getByte("sector", i);
                int layer = bank.getByte("layer", i);
                int strip = bank.getShort("component", i);
                int adc   = bank.getInt("ADC", i);
                float timeOfMax = bank.getFloat("time", i);

                if (strip < 0 || !mask[sector][layer][strip]) {
                    continue;
                }

                this.getDataGroup().getItem(sector, layer, 2).getH1F("Occupancy Layer " + layer + " Sector " + sector).fill(strip);
                if ((samplingTime < timeOfMax) && (timeOfMax < samplingTime * (numberOfSamples - 1))) {
                    this.getDataGroup().getItem(sector, layer, 1).getH1F("TimeOfMax : Layer " + layer + " Sector " + sector).fill(timeOfMax);
                }
                this.getDataGroup().getItem(sector, layer, 3).getH1F("Amplitude : Layer " + layer + " Sector " + sector).fill(adc);
                this.numberOfHitsPerDetector[sector][layer]++;
                this.getDetectorSummary().getH1F("summary").setBinContent(maxNumberSectors * (layer - 1) + (sector - 1), (double) this.numberOfHitsPerDetector[sector][layer] / ((double) this.getNumberOfEvents()));
            }
        }
    }
}
