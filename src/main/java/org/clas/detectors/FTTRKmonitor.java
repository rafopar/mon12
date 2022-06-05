package org.clas.detectors;

import java.util.Arrays;
import java.util.List;

import org.clas.viewer.DetectorMonitor;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.utils.groups.IndexedTable;

public class FTTRKmonitor extends DetectorMonitor {

    int sparseSample;
    int numberOfSamples;
    int samplingTime;

    private int nstrip = 768;
    private int nlayer = 4;
    private int numberOfStripsPerChip = 64;
    private int numberOfChips = 12;

    int runNumber;
    int defaultRunNumber = 2284;

    public FTTRKmonitor(String name) {
        super(name);

        this.loadConstantsFromCCDB(defaultRunNumber);

        this.setDetectorTabNames("occupancy2d", "occupancy1d", "tmax", "adc");
        this.init(false);
    }

    public void loadConstantsFromCCDB(int runNumber) {

        List<String> tablesFitter = null;
        List<String> keysFitter = null;

        keysFitter = Arrays.asList(new String[]{"FTTconfig"});
        tablesFitter = Arrays.asList(new String[]{"/daq/config/fttrk"});
        this.getCcdb().init(keysFitter, tablesFitter);

        IndexedTable fttConfig = this.getCcdb().getConstants(runNumber, "FTTconfig");

        this.sparseSample = fttConfig.getIntValue("sparse", 0, 0, 0);
        this.numberOfSamples = (fttConfig.getIntValue("number_sample", 0, 0, 0) - 1) * (this.sparseSample + 1) + 1;
        this.samplingTime = (byte) fttConfig.getDoubleValue("sampling_time", 0, 0, 0);

    }

    @Override
    public void createHistos() {
        // initialize canvas and create histograms
        this.setNumberOfEvents(0);

        H1F summary = new H1F("summary", "summary", nstrip * nlayer, 0.5, nstrip * nlayer + 0.5);
        summary.setTitleX("channel");
        summary.setTitleY("FTTRK hits");
        summary.setTitle("FTTRK");
        summary.setFillColor(38);
        DataGroup sum = new DataGroup(1, 1);
        sum.addDataSet(summary, 0);
        this.setDetectorSummary(sum);

        H2F occADC2D = new H2F("occADC_2D", "occADC_2D", nstrip, 0.5, nstrip + 0.5, nlayer, 0.5, nlayer + 0.5);
        occADC2D.setTitleX("channel");
        occADC2D.setTitleY("layer");

        for (int ilayer = 1; ilayer <= nlayer; ilayer++) {
            H1F occADCl = new H1F("occADC_layer" + ilayer, "occADC_layer" + ilayer, nstrip, 0.5, nstrip + 0.5);
            occADCl.setTitleX("channel");
            occADCl.setTitleY("Counts");
            occADCl.setFillColor(38);
            occADCl.setTitle("layer " + ilayer);

            int sector = 1;
            H1F timeMaxHisto = new H1F("TimeOfMax : Layer " + ilayer + " Sector " + sector, "TimeOfMax : Layer " + ilayer + " Sector " + sector,
                    samplingTime * numberOfSamples, 0., samplingTime * numberOfSamples);
            timeMaxHisto.setTitleX("Time of max (Layer " + ilayer + " Sector " + sector + ")");
            timeMaxHisto.setTitleY("Nb hits");
            timeMaxHisto.setFillColor(4);

            DataGroup dg = new DataGroup(1, 2);
            dg.addDataSet(occADCl, 0);
            dg.addDataSet(timeMaxHisto, 1);
            this.getDataGroup().add(dg, 0, ilayer, 0);
        }

        H2F adc = new H2F("adc", "adc", 50, 0, 1000, nstrip * nlayer, 0.5, nstrip * nlayer + 0.5);
        adc.setTitleX("ADC - amplitude");
        adc.setTitleY("channel");
        H2F tdc = new H2F("tdc", "tdc", 50, 0, 500, nstrip * nlayer, 0.5, nstrip * nlayer + 0.5);
        tdc.setTitleX("time");
        tdc.setTitleY("channel");

        DataGroup dg = new DataGroup(1, 3);
        dg.addDataSet(occADC2D, 0);
        dg.addDataSet(adc, 1);
        dg.addDataSet(tdc, 1);
        this.getDataGroup().add(dg, 0, 0, 0);
    }

    @Override
    public void plotHistos() {
        // plotting histos
        this.getDetectorCanvas().getCanvas("occupancy2d").divide(1, 1);
        this.getDetectorCanvas().getCanvas("occupancy2d").setGridX(false);
        this.getDetectorCanvas().getCanvas("occupancy2d").setGridY(false);
        this.getDetectorCanvas().getCanvas("occupancy2d").cd(0);
        this.getDetectorCanvas().getCanvas("occupancy2d").getPad(0).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("occupancy2d").draw(this.getDataGroup().getItem(0, 0, 0).getH2F("occADC_2D"));
        this.getDetectorCanvas().getCanvas("occupancy1d").divide(2, 2);
        this.getDetectorCanvas().getCanvas("occupancy1d").setGridX(false);
        this.getDetectorCanvas().getCanvas("occupancy1d").setGridY(false);
        for (int ilayer = 1; ilayer <= nlayer; ilayer++) {
            this.getDetectorCanvas().getCanvas("occupancy1d").cd(0 + ilayer - 1);
            this.getDetectorCanvas().getCanvas("occupancy1d").draw(this.getDataGroup().getItem(0, ilayer, 0).getH1F("occADC_layer" + ilayer));
        }
        this.getDetectorCanvas().getCanvas("tmax").divide(nlayer / 2, nlayer / 2);
        this.getDetectorCanvas().getCanvas("tmax").setGridX(false);
        this.getDetectorCanvas().getCanvas("tmax").setGridY(false);
        for (int ilayer = 1; ilayer <= nlayer; ilayer++) {
            this.getDetectorCanvas().getCanvas("tmax").cd(0 + ilayer - 1);
            int sector = 1;
            this.getDetectorCanvas().getCanvas("tmax").draw(this.getDataGroup().getItem(0, ilayer, 0).getH1F("TimeOfMax : Layer " + ilayer + " Sector " + sector));
        }
        this.getDetectorCanvas().getCanvas("tmax").update();
        this.getDetectorCanvas().getCanvas("adc").divide(1, 2);
        this.getDetectorCanvas().getCanvas("adc").setGridX(false);
        this.getDetectorCanvas().getCanvas("adc").setGridY(false);
        this.getDetectorCanvas().getCanvas("adc").cd(0);
        this.getDetectorCanvas().getCanvas("adc").getPad(0).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("adc").draw(this.getDataGroup().getItem(0, 0, 0).getH2F("adc"));
        this.getDetectorCanvas().getCanvas("adc").cd(1);
        this.getDetectorCanvas().getCanvas("adc").getPad(1).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("adc").draw(this.getDataGroup().getItem(0, 0, 0).getH2F("tdc"));
        this.getDetectorCanvas().getCanvas("adc").update();
    }

    @Override
    public void processEvent(DataEvent event) {

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

        // process event info and save into data group
        if (event.hasBank("FTTRK::adc") == true) {
            DataBank bank = event.getBank("FTTRK::adc");
            int rows = bank.rows();
            for (int loop = 0; loop < rows; loop++) {
                int sector = bank.getByte("sector", loop);
                int layer = bank.getByte("layer", loop);
                int comp = bank.getInt("component", loop);
                int order = bank.getByte("order", loop);
                int adc = bank.getInt("ADC", loop);
                float timeOfMax = bank.getFloat("time", loop);
                int channel = comp + (layer - 1) * nstrip;

                if ((samplingTime < timeOfMax) && (timeOfMax < samplingTime * (numberOfSamples - 1))) {
                    this.getDataGroup().getItem(0, layer, 0).getH1F("TimeOfMax : Layer " + layer + " Sector " + sector).fill(timeOfMax);
                }

//                System.out.println("ROW " + loop + " SECTOR = " + sector + " LAYER = " + layer + " COMPONENT = " + comp +
//                      " ADC = " + adc); 
                if (adc > 0) {

                    this.getDataGroup().getItem(0, 0, 0).getH2F("occADC_2D").fill(comp * 1.0, layer * 1.0);

                    this.getDataGroup().getItem(0, layer, 0).getH1F("occADC_layer" + layer).fill(comp * 1.0);

                    this.getDataGroup().getItem(0, 0, 0).getH2F("adc").fill(adc * 1.0, channel * 1.0);
                    this.getDataGroup().getItem(0, 0, 0).getH2F("tdc").fill(timeOfMax * 1.0, channel * 1.0);
                }
                this.getDetectorSummary().getH1F("summary").fill(channel * 1.0);
            }
        }
    }

    @Override
    public void analysisUpdate() {
        if (this.getNumberOfEvents() > 0) {
            for (int layer = 1; layer <= nlayer; layer++) {
//                H1F raw1 = this.getDataGroup().getItem(0,layer,0).getH1F("timeMaxTmp1_layer"+layer);
//                H1F raw2 = this.getDataGroup().getItem(0,layer,0).getH1F("timeMaxTmp2_layer"+layer);
//                H1F ave = this.getDataGroup().getItem(0,layer,0).getH1F("timeMax_layer"+layer);
//                for(int loop = 0; loop < raw1.getDataSize(0); loop++){
//                    ave.setBinContent(loop, raw2.getBinContent(loop)/raw1.getBinContent(loop));
//                }
            }
        }

    }

}
