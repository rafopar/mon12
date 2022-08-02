package org.clas.detectors;

import org.clas.viewer.DetectorMonitor;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

import java.util.*;
import java.util.Arrays;
import org.jlab.utils.groups.IndexedTable;
//import org.clas.detectors.JLabTDCTools;
import org.clas.detectors.JLabTDCTools.SLCO;

public class TDCmonitor extends DetectorMonitor {

    private final int[] npaddles = new int[]{23, 62, 5};
    private final String[] names = {"left", "right"};
    private final double[] store = new double[npaddles[1] + npaddles[0]];
    private final double vfTdcConv = 0.016;
    private final String SLCO_listFile = "/u/home/rafopar/work/git/vfTDC_Calibration/codes/SLCO_List.dat";
    private final int paddles2Pot = 5;

    public JLabTDCTools tdcTools;

    public TDCmonitor(String name) {
        super(name);

        this.setDetectorTabNames("TDC Histograms", "JLab TDC", "TimeDiff2D", "Time Differences");
        this.useSectorButtons(false);
        this.init(false);   // set to true for picture on left side
        this.getCcdb().setVariation("default");
        this.getCcdb().init(Arrays.asList(new String[]{"/calibration/ftof/time_jitter"}));

        tdcTools = new JLabTDCTools();
        tdcTools.InitLUTs(SLCO_listFile);
        JLabTDCTools.SLCO t = tdcTools.new SLCO(3, 4, 5, 5);

    }

    @Override
    public void createHistos() {
        // initialize canvas and create histograms
        this.setNumberOfEvents(0);
        this.getDetectorCanvas().getCanvas("TDC Histograms").divide(4, 2);
        this.getDetectorCanvas().getCanvas("TDC Histograms").setGridX(false);
        this.getDetectorCanvas().getCanvas("TDC Histograms").setGridY(false);

        H1F sumStackp2 = new H1F("sum_p2", "sum_p2", 6, 0.5, 6.5);
        sumStackp2.setTitleX("sector");
        sumStackp2.setTitleY("Counts");
        sumStackp2.setTitle("FTOF panel 1A");
        sumStackp2.setFillColor(39);
        DataGroup sum = new DataGroup(1, 1);
        sum.addDataSet(sumStackp2, 1);
        this.setDetectorSummary(sum);

        DataGroup dg = new DataGroup(3, 2);
        for (int i = 0; i < names.length; i++) {
            H1F occVF = new H1F("occVF" + names[i], "PMT " + names[i], "Counts", npaddles[1] + npaddles[0] + 2, -npaddles[0] - 1, npaddles[1] + 1);
            occVF.setLineColor(2);
            H1F occCA = new H1F("occCA" + names[i], "PMT " + names[i], "Counts", npaddles[1] + npaddles[0] + 2, -npaddles[0] - 1, npaddles[1] + 1);
            H2F tdc = new H2F("tdc" + names[i], "TDC spectrum", 100, 0, 1100, npaddles[1] + npaddles[0] + 2, -npaddles[0] - 1, npaddles[1] + 1);
            tdc.setTitleX("vfTDC (ns)");
            tdc.setTitleY("PMT " + names[i]);
            H2F cor = new H2F("cor" + names[i], "Correlation", 100, 0, 400, 100, 500, 900);
            cor.setTitleX("caenTDC (ns)");
            cor.setTitleY("vfTDC (ns)");
            H2F dif = new H2F("dif" + names[i], "Difference", 100, -100, 900, npaddles[1] + npaddles[0] + 2, -npaddles[0] - 1, npaddles[1] + 1);
            dif.setTitleX("vfTDC-caenTDC (ns)");
            dif.setTitleY("paddle");

            H2F CAEN_VF_TimeDiff = new H2F("tDiff_" + names[i], "CAEN and JLab time diff", 200, -1000, 0., npaddles[1] + npaddles[0] + 2, -npaddles[0] - 1, npaddles[1] + 1);

            dg.addDataSet(occVF, i * 4 + 0);
            dg.addDataSet(occCA, i * 4 + 0);
            dg.addDataSet(tdc, i * 4 + 1);
            dg.addDataSet(cor, i * 4 + 2);
            dg.addDataSet(dif, i * 4 + 3);
        }
        this.getDataGroup().add(dg, 0, 0, 0);

        this.getDetectorCanvas().getCanvas("TDC Histograms").divide(4, 2);
        this.getDetectorCanvas().getCanvas("TDC Histograms").setGridX(false);
        this.getDetectorCanvas().getCanvas("TDC Histograms").setGridY(false);

        this.getDetectorCanvas().getCanvas("JLab TDC").divide(4, 2);
        this.getDetectorCanvas().getCanvas("JLab TDC").setGridX(false);
        this.getDetectorCanvas().getCanvas("JLab TDC").setGridY(false);

        DataGroup dgJLab = new DataGroup(4, 4);
        for (int i = 0; i < names.length; i++) {
            H1F test1 = new H1F("test_" + names[i], "test2", 400, -560., -550.);
            dgJLab.addDataSet(test1, i);
            H2F paddle_VS_CAEN_JLAB_Diff = new H2F("paddle_VS_CAEN_JLAB_Diff_" + names[i], "paddle_VS_CAEN_JLAB_Diff_" + names[i], 400, -560., -550., npaddles[1] + npaddles[0] + 2, -npaddles[0] - 1, npaddles[1] + 1);
            dgJLab.addDataSet(paddle_VS_CAEN_JLAB_Diff, i);
        }
        H1F LR_DIFF_VF = new H1F("LR_Diff_VF", "VF LR Difference", 200, -25., 25.);
        H1F LR_DIFF_CAEN = new H1F("LR_Diff_CAEN", "CAEN LR Differebce", 200, -25., 25.);
        H2F LD_dif_vsPaddle_VF = new H2F("LR_dif_vsPaddle_VF", "VF LR Difference vs paddle", 400, -25, 25, npaddles[1] + npaddles[0] + 2, -npaddles[0] - 1, npaddles[1] + 1);
        H2F LD_dif_vsPaddle_CAEN = new H2F("LR_dif_vsPaddle_CAEN", "CAEN LR Difference vs paddle", 400, -25, 25, npaddles[1] + npaddles[0] + 2, -npaddles[0] - 1, npaddles[1] + 1);
        dgJLab.addDataSet(LR_DIFF_VF, 2);
        dgJLab.addDataSet(LR_DIFF_CAEN, 3);
        dgJLab.addDataSet(LD_dif_vsPaddle_VF, 4);
        dgJLab.addDataSet(LD_dif_vsPaddle_CAEN, 5);

        this.getDataGroup().add(dgJLab, 0, 0, 1);

        this.getDetectorCanvas().getCanvas("Time Differences").divide(5, 2);
        this.getDetectorCanvas().getCanvas("Time Differences").setGridX(false);
        this.getDetectorCanvas().getCanvas("Time Differences").setGridY(false);

        DataGroup dgTDiff = new DataGroup(4, 4);

        for (int i = 0; i < names.length; i++) {
            for (int paddle = 0; paddle < paddles2Pot; paddle++) {
                H1F tDiffCaenJLab = new H1F("tDiffCaenJLab_" + names[i] + "_paddle_" + paddle, "tDiffCaenJLab_" + names[i] + "_paddle_" + paddle, 400, -560., -550.);
                dgTDiff.addDataSet(tDiffCaenJLab, paddles2Pot * i + paddle);
            }
        }

        this.getDataGroup().add(dgTDiff, 0, 0, 2);

        this.getDetectorCanvas().getCanvas("TimeDiff2D").divide(2, 2);
        this.getDetectorCanvas().getCanvas("TimeDiff2D").setGridX(false);
        this.getDetectorCanvas().getCanvas("TimeDiff2D").setGridY(false);

        DataGroup dgTDiff2D = new DataGroup(4, 4);
        for (int i = 0; i < names.length; i++) {
            H2F tDiffVsTCaen = new H2F("tDiffVsTCaen_paddle_" + names[i], "tDiffVsTCaen_paddle_" + names[i], 200, 120., 140., 100, -559.5, -558.5 );
            H2F tDiffVsTJLab = new H2F("tDiffVsTJLab_paddle_" + names[i], "tDiffVsTJLab_paddle_" + names[i], 200, 675., 695., 100, -559.5, -558.5 );
            
            dgTDiff2D.addDataSet(tDiffVsTCaen, i*2 + 0 );
            dgTDiff2D.addDataSet(tDiffVsTJLab, i*2 + 1 );
        }
        this.getDataGroup().add(dgTDiff2D, 0, 0, 3);
    }

    @Override
    public void plotHistos() {

        for (int i = 0; i < names.length; i++) {
            this.getDetectorCanvas().getCanvas("TDC Histograms").cd(i * 4 + 0);
            this.getDetectorCanvas().getCanvas("TDC Histograms").draw(this.getDataGroup().getItem(0, 0, 0).getH1F("occCA" + names[i]));
            this.getDetectorCanvas().getCanvas("TDC Histograms").draw(this.getDataGroup().getItem(0, 0, 0).getH1F("occVF" + names[i]), "same");
            this.getDetectorCanvas().getCanvas("TDC Histograms").cd(i * 4 + 1);
            this.getDetectorCanvas().getCanvas("TDC Histograms").draw(this.getDataGroup().getItem(0, 0, 0).getH2F("tdc" + names[i]));
            this.getDetectorCanvas().getCanvas("TDC Histograms").getPad().getAxisZ().setLog(true);
            this.getDetectorCanvas().getCanvas("TDC Histograms").cd(i * 4 + 2);
            this.getDetectorCanvas().getCanvas("TDC Histograms").draw(this.getDataGroup().getItem(0, 0, 0).getH2F("cor" + names[i]));
            this.getDetectorCanvas().getCanvas("TDC Histograms").getPad().getAxisZ().setLog(true);
            this.getDetectorCanvas().getCanvas("TDC Histograms").cd(i * 4 + 3);
            this.getDetectorCanvas().getCanvas("TDC Histograms").draw(this.getDataGroup().getItem(0, 0, 0).getH2F("dif" + names[i]));
            this.getDetectorCanvas().getCanvas("TDC Histograms").getPad().getAxisZ().setLog(true);

            //System.out.println( "====================== Kuku kuku" + this.getDataGroup().getItem(0, 0, 1). );
            this.getDetectorCanvas().getCanvas("JLab TDC").cd(i * 4 + 0);
            this.getDetectorCanvas().getCanvas("JLab TDC").draw(this.getDataGroup().getItem(0, 0, 1).getH1F("test_" + names[i]));

            this.getDetectorCanvas().getCanvas("JLab TDC").cd(i * 4 + 3);
            this.getDetectorCanvas().getCanvas("JLab TDC").draw(this.getDataGroup().getItem(0, 0, 1).getH2F("paddle_VS_CAEN_JLAB_Diff_" + names[i]));
        }

        this.getDetectorCanvas().getCanvas("JLab TDC").cd(1);
        this.getDetectorCanvas().getCanvas("JLab TDC").cd(1).draw(this.getDataGroup().getItem(0, 0, 1).getH1F("LR_Diff_VF"));
        this.getDetectorCanvas().getCanvas("JLab TDC").cd(5);
        this.getDetectorCanvas().getCanvas("JLab TDC").cd(5).draw(this.getDataGroup().getItem(0, 0, 1).getH1F("LR_Diff_CAEN"));
        this.getDetectorCanvas().getCanvas("JLab TDC").cd(2);
        this.getDetectorCanvas().getCanvas("JLab TDC").cd(2).draw(this.getDataGroup().getItem(0, 0, 1).getH2F("LR_dif_vsPaddle_VF"));
        this.getDetectorCanvas().getCanvas("JLab TDC").cd(6);
        this.getDetectorCanvas().getCanvas("JLab TDC").cd(6).draw(this.getDataGroup().getItem(0, 0, 1).getH2F("LR_dif_vsPaddle_CAEN"));

        for (int i = 0; i < names.length; i++) {
            for (int paddle = 0; paddle < paddles2Pot; paddle++) {
                this.getDetectorCanvas().getCanvas("Time Differences").cd(paddles2Pot * i + paddle);
                String histName = "tDiffCaenJLab_" + names[i] + "_paddle_" + paddle;
                this.getDetectorCanvas().getCanvas("Time Differences").cd(paddles2Pot * i + paddle).draw(this.getDataGroup().getItem(0, 0, 2).getH1F(histName));
            }
        }
        
        for (int i = 0; i < names.length; i++) {
            this.getDetectorCanvas().getCanvas("TimeDiff2D").cd( 2*i + 0 );
            String histName = "tDiffVsTCaen_paddle_" + names[i];
            this.getDetectorCanvas().getCanvas("TimeDiff2D").cd( 2*i + 0 ).draw( this.getDataGroup().getItem( 0, 0, 3 ).getH2F(histName) );
            
            this.getDetectorCanvas().getCanvas("TimeDiff2D").cd( 2*i + 1 );
            histName = "tDiffVsTJLab_paddle_" + names[i];
            this.getDetectorCanvas().getCanvas("TimeDiff2D").cd( 2*i + 1 ).draw( this.getDataGroup().getItem( 0, 0, 3 ).getH2F(histName) );
        }

    }

    @Override
    public void processEvent(DataEvent event) {

        Map<SLCO, ArrayList<Double>> mv_vfTime = new HashMap<>();
        Map<SLCO, ArrayList<Double>> mv_CAENTime = new HashMap<>();
        Map<SLCO, ArrayList<Long>> mv_vfTStamp = new HashMap<>();

        int triggerPhase = 0;
        long TI_TimeStamp = 0;

        if (event.hasBank("RUN::config")) {
            DataBank bank = event.getBank("RUN::config");
            int runNumber = bank.getInt("run", 0);
            long timestamp = bank.getLong("timestamp", 0);
            TI_TimeStamp = timestamp;
            IndexedTable jitter = this.getCcdb().getConstants(runNumber, "/calibration/ftof/time_jitter");
            this.period = jitter.getDoubleValue("period", 0, 0, 0);
            this.phase = jitter.getIntValue("phase", 0, 0, 0);
            this.ncycles = jitter.getIntValue("cycles", 0, 0, 0);
            if (ncycles > 0) {
                triggerPhase = (int) ((timestamp + phase) % ncycles); // TI derived phase correction due to TDC and FADC clock differences
            }
        }

        for (int i = 0; i < store.length; i++) {
            store[i] = 0;
        }
        int nCaen = 0;
        if (event.hasBank("FTOF::tdc") == true) {
            DataBank bank = event.getBank("FTOF::tdc");
            for (int i = 0; i < bank.rows(); i++) {
                int sector = bank.getByte("sector", i);
                int layer = bank.getByte("layer", i);
                int pmt = bank.getByte("order", i);
                int paddle = bank.getShort("component", i);

                double tdc = bank.getInt("TDC", i) * this.tdcconv - triggerPhase * this.period;

                if (tdc > 0 && sector == 6 && layer <= 3) {

                    SLCO cur_SLCO = tdcTools.new SLCO(sector, layer, paddle, pmt);
                    if (mv_CAENTime.containsKey(cur_SLCO) == false) {
                        mv_CAENTime.put(cur_SLCO, new ArrayList<Double>());
                    }

                    mv_CAENTime.get(cur_SLCO).add(tdc);

                    this.getDataGroup().getItem(0, 0, 0).getH1F("occCA" + names[pmt - 2]).fill(paddle * (layer * 2 - 3));
                    if (tdc < store[paddle - 1 + npaddles[0] * (layer - 1)] || store[paddle - 1 + npaddles[0] * (layer - 1)] == 0) {
                        store[paddle - 1 + npaddles[0] * (layer - 1)] = tdc;
                    }
//                    System.out.println("caen " + sector + " " + layer + " " + paddle + " " + pmt);
//                    nCaen++;
                }
            }
        }
        if (event.hasBank("FTOF::vftdc") == true) {
            DataBank bank = event.getBank("FTOF::vftdc");
            for (int i = 0; i < bank.rows(); i++) {
                int sector = bank.getByte("sector", i);
                int layer = bank.getByte("layer", i);
                int pmt = bank.getByte("order", i);
                int paddle = bank.getShort("component", i);
                int edge = bank.getByte("edge", i);

                double tdc = bank.getInt("TDC", i) * this.vfTdcConv;

                if (tdc > 0 && layer <= 2) {
                    this.getDataGroup().getItem(0, 0, 0).getH2F("tdc" + names[pmt - 2]).fill(tdc, paddle * (layer * 2 - 3));
                    if (edge == 0) {
                        this.getDataGroup().getItem(0, 0, 0).getH1F("occVF" + names[pmt - 2]).fill(paddle * (layer * 2 - 3));
                        if (store[paddle - 1 + npaddles[0] * (layer - 1)] > 0) {
                            this.getDataGroup().getItem(0, 0, 0).getH2F("cor" + names[pmt - 2]).fill(store[paddle - 1 + npaddles[0] * (layer - 1)], tdc);
                            this.getDataGroup().getItem(0, 0, 0).getH2F("dif" + names[pmt - 2]).fill(tdc - store[paddle - 1 + npaddles[0] * (layer - 1)], paddle * (layer * 2 - 3));
                        }
//                        if(nCaen>0) {
//                            System.out.println("vf   " + sector + " " + layer + " " + paddle + " " + pmt);                            
//                        }
                    }
                }
            }

            tdcTools.MakeCalibratedBank(event);
        }
        // Here after calling MakeCalibratedBank(event), we should have 
        if (event.hasBank("FTOF::vfTime") == true) {

            DataBank bank = event.getBank("FTOF::vfTime");

            for (int i = 0; i < bank.rows(); i++) {

                int sector = bank.getByte("sector", i);
                int layer = bank.getByte("layer", i);
                int pmt = bank.getByte("order", i);
                int paddle = bank.getShort("component", i);
                int edge = bank.getByte("edge", i);

                double time = (double) bank.getFloat("time", i) - triggerPhase * this.period;
                Long timestamp = bank.getLong("timestamp", i);

                if (edge != 0) {
                    continue;
                }

                SLCO cur_SLCO = tdcTools.new SLCO(sector, layer, paddle, pmt);

                if (mv_vfTime.containsKey(cur_SLCO) == false) {
                    mv_vfTime.put(cur_SLCO, new ArrayList<>());
                    mv_vfTStamp.put(cur_SLCO, new ArrayList<>());
                }

                mv_vfTime.get(cur_SLCO).add(time);
                mv_vfTStamp.get(cur_SLCO).add(timestamp);
            }
        }

        /**
         * All banks are read, now lets compare CAEN and vfTimes
         */
//        for (SLCO cur_SLCO : mv_CAENTime.keySet()) {
//
//            if (mv_vfTime.containsKey(cur_SLCO) == false) {
//                mv_vfTime.put(cur_SLCO, new ArrayList<>());
//            }
//
//            System.out.println("Cur_SLCO is " + cur_SLCO.toString() + "   The number of CAEN hits is " + mv_CAENTime.get(cur_SLCO).size() + 
//                    "  The number of vfHits is " + mv_vfTime.get(cur_SLCO).size() );
//        }
        for (SLCO cur_SLCO : mv_vfTime.keySet()) {

            if (mv_CAENTime.containsKey(cur_SLCO) == false) {
                mv_CAENTime.put(cur_SLCO, new ArrayList<>());
            }

//            System.out.println("Cur_SLCO is " + cur_SLCO.toString() + "   The number of CAEN hits is " + mv_CAENTime.get(cur_SLCO).size() + 
//                    "  The number of vfHits is " + mv_vfTime.get(cur_SLCO).size() );
            if (!mv_CAENTime.get(cur_SLCO).isEmpty()) {
                //System.out.println(mv_CAENTime.get(cur_SLCO).get(0) - mv_vfTime.get(cur_SLCO).get(0) / 1000.);
                // test_
                //this.getDataGroup().getItem(0, 0, 1).getH1F("test_1" ).fill( -545 );

                SLCO SLCO_otherside = tdcTools.new SLCO(cur_SLCO.sector, cur_SLCO.layer, cur_SLCO.component, 5 - cur_SLCO.order);

                this.getDataGroup().getItem(0, 0, 1).getH1F("test_" + names[cur_SLCO.order - 2]).fill(mv_CAENTime.get(cur_SLCO).get(0) - mv_vfTime.get(cur_SLCO).get(0) / 1000.);
                this.getDataGroup().getItem(0, 0, 1).getH2F("paddle_VS_CAEN_JLAB_Diff_" + names[cur_SLCO.order - 2]).fill(mv_CAENTime.get(cur_SLCO).get(0) - mv_vfTime.get(cur_SLCO).get(0) / 1000., cur_SLCO.component * (cur_SLCO.layer * 2 - 3));

                double caenJLab_tDiff = mv_CAENTime.get(cur_SLCO).get(0) - mv_vfTime.get(cur_SLCO).get(0) / 1000.;

                this.getDataGroup().getItem(0, 0, 1).getH1F("test_" + names[cur_SLCO.order - 2]).fill(mv_CAENTime.get(cur_SLCO).get(0) - mv_vfTime.get(cur_SLCO).get(0) / 1000.);
                int paddle = -1 - cur_SLCO.component * (cur_SLCO.layer * 2 - 3);
                if (paddle >= 0 && paddle < 5) {
                    this.getDataGroup().getItem(0, 0, 2).getH1F("tDiffCaenJLab_" + names[cur_SLCO.order - 2] + "_paddle_" + paddle).fill(caenJLab_tDiff);
                    
                    if( paddle == 2 ){
                        this.getDataGroup().getItem(0, 0, 3).getH2F( "tDiffVsTCaen_paddle_" + names[cur_SLCO.order - 2] ).fill(mv_CAENTime.get(cur_SLCO).get(0), caenJLab_tDiff);
                        this.getDataGroup().getItem(0, 0, 3).getH2F( "tDiffVsTJLab_paddle_" + names[cur_SLCO.order - 2] ).fill(mv_vfTime.get(cur_SLCO).get(0) / 1000., caenJLab_tDiff);
                    }
                    
                }
                //"tDiffCaenJLab_" + names[i] + "_paddle_" + paddle;
                if (mv_vfTime.containsKey(SLCO_otherside) && mv_CAENTime.containsKey(SLCO_otherside)) {

                    double tDiffCAEN = mv_CAENTime.get(cur_SLCO).get(0) - mv_CAENTime.get(SLCO_otherside).get(0);
                    double tDiffVF = (mv_vfTime.get(cur_SLCO).get(0) - mv_vfTime.get(SLCO_otherside).get(0)) / 1000.;

                    //System.out.println(mv_vfTime.get(cur_SLCO).get(0) + "     " + mv_vfTime.get(SLCO_otherside).get(0) );
                    //System.out.println(mv_vfTStamp.get(cur_SLCO).get(0) + "    " + TI_TimeStamp);
                    //if (mv_vfTStamp.get(cur_SLCO).get(0) - TI_TimeStamp == 5) {
                    this.getDataGroup().getItem(0, 0, 1).getH1F("LR_Diff_VF").fill(tDiffVF);
                    this.getDataGroup().getItem(0, 0, 1).getH1F("LR_Diff_CAEN").fill(tDiffCAEN);
                    this.getDataGroup().getItem(0, 0, 1).getH2F("LR_dif_vsPaddle_VF").fill(tDiffVF, cur_SLCO.component * (cur_SLCO.layer * 2 - 3));
                    this.getDataGroup().getItem(0, 0, 1).getH2F("LR_dif_vsPaddle_CAEN").fill(tDiffCAEN, cur_SLCO.component * (cur_SLCO.layer * 2 - 3));
                    //}
                }
            }

        }

    }

    @Override
    public void analysisUpdate() {

    }

}
