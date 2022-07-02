package org.clas.detectors;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.lang.Math;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author rafopar
 */
public class JLabTDCTools {

    public Map<SLCO, ArrayList<Double>> mv_LUTs = new HashMap<>();
    public Map<SLCO, ArrayList<Double>> mv_LUTErrs = new HashMap<>();

    private final Integer runLUT = 15986;
    private final String LUTDIR = "/local/work/git/vfTDC_Calibration/codes/LUTs";
    private final int nBins = 128; // Effectively it is 64 as the 1st bit is always is 0

    public void InitLUTs(String fname) {
        try {
            FileInputStream fstream = new FileInputStream(fname);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                // Print the content on the console
                //System.out.println(strLine);

                String[] splited = strLine.split("\\s+");

                int sector = Integer.parseInt(splited[0]);
                int layer = Integer.parseInt(splited[1]);
                int component = Integer.parseInt(splited[2]);
                int order = Integer.parseInt(splited[3]);

                SLCO cur_SLCO = new SLCO(sector, layer, component, order);

                mv_LUTs.put(cur_SLCO, new ArrayList<Double>());
                mv_LUTErrs.put(cur_SLCO, new ArrayList<Double>());

                String LUTfName = LUTDIR + String.format("/LUTs_%d_%d_%d_%d_%d.dat", runLUT, sector, layer, component, order);
                FileInputStream fstream_LUT = new FileInputStream(LUTfName);
                DataInputStream inLUT = new DataInputStream(fstream_LUT);
                BufferedReader brLUT = new BufferedReader(new InputStreamReader(inLUT));
                String strLineLUT;

                while ((strLineLUT = brLUT.readLine()) != null) {
                    //System.out.println(strLineLUT);

                    String[] splitedValues = strLineLUT.split("\\s+");
                    int tdc = Integer.parseInt(splitedValues[1]);
                    double timeLUT = Double.parseDouble(splitedValues[2]);
                    double timeErrLUT = Double.parseDouble(splitedValues[3]);

                    mv_LUTs.get(cur_SLCO).add(tdc, timeLUT);
                    mv_LUTErrs.get(cur_SLCO).add(tdc, timeErrLUT);

                }
                inLUT.close();

            }
            //Close the input stream
            in.close();

        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("Error: the file " + fname + " is not found.");
        }

    }

    void MakeCalibratedBank(DataEvent event) {

        Map< SLCO, ArrayList<TDCHit>> m_v_leadHits = new HashMap<>();
        Map< SLCO, ArrayList< ArrayList<TDCHit>>> m_v_OrganizedLeadHits = new HashMap<>();

        DataBank bvftdc = event.getBank("FTOF::vftdc");

        for (int i = 0; i < bvftdc.rows(); i++) {
            int sector = bvftdc.getByte("sector", i);
            int layer = bvftdc.getByte("layer", i);
            int component = bvftdc.getShort("component", i);
            int order = bvftdc.getByte("order", i);
            int edge = bvftdc.getByte("edge", i);

            int tdc = bvftdc.getInt("TDC", i);

            int interval = tdc / nBins;
            int tdcBin = (tdc % nBins);

            Long timestamp = bvftdc.getLong("timestamp", i);

            if (edge != 0) {
                continue;
            }

            SLCO cur_SLCO = new SLCO(sector, layer, component, order);
            TDCHit curHit = new TDCHit(tdc, interval, tdcBin, timestamp);

            if (!m_v_leadHits.containsKey(cur_SLCO)) {
                m_v_leadHits.put(cur_SLCO, new ArrayList<TDCHit>());
            }
            m_v_leadHits.get(cur_SLCO).add(curHit);
        }

        int n_timeHits = 0; // counter for 'real hits' each signal is noe measured twice or triple, this will represent number of signals
        for (SLCO cur_SLCO : mv_LUTs.keySet()) {

            // checking if there is a hit with the given SLCO in the event
            if (!m_v_leadHits.containsKey(cur_SLCO)) {
                continue;
            }

            m_v_OrganizedLeadHits.put(cur_SLCO, GetOrganizedHits(m_v_leadHits.get(cur_SLCO)));
            n_timeHits = n_timeHits + m_v_OrganizedLeadHits.get(cur_SLCO).size();
        }

        DataBank bank = event.createBank("FTOF::vfTime", n_timeHits);

        int row = 0;
        for (SLCO curSLCO : m_v_OrganizedLeadHits.keySet()) {

            //  ========== Now let's calculate the time!!
            for (ArrayList<TDCHit> curHurHits : m_v_OrganizedLeadHits.get(curSLCO)) {

                double time;

                if (curHurHits.size() > 2) {

                    double t_1 = mv_LUTs.get(curSLCO).get(curHurHits.get(0).tdcBin);
                    double t_2 = mv_LUTs.get(curSLCO).get(curHurHits.get(1).tdcBin);

                    double Value_1_4ns = curHurHits.get(0).rawTDC / 256;
                    double Value_2_4ns = curHurHits.get(1).rawTDC / 256;

                    double Value_1_2ns = curHurHits.get(0).interval % 2;
                    double Value_2_2ns = curHurHits.get(1).interval % 2;

                    double time_1 = 4000 * Value_1_4ns + 2000 * Value_1_2ns + t_1;
                    double time_2 = 4000 * Value_2_4ns + 2000 * Value_2_2ns + 2_1;

                    double time_1_err = mv_LUTErrs.get(curSLCO).get( curHurHits.get(0).tdcBin );
                    double time_2_err = mv_LUTErrs.get(curSLCO).get( curHurHits.get(1).tdcBin );
                  
                    time = (time_1 * time_2_err * time_2_err + time_2 * time_1_err * time_1_err) / (time_1_err * time_1_err + time_2_err * time_2_err);
                    
                }else {
                    
                    /*
                     * These in principle can happen when times are at the beginning or at the end of the window,
                     * So we will take in this case the best estimate of the time
                     */
                    
                    double t_1 = mv_LUTs.get(curSLCO).get(curHurHits.get(0).tdcBin);
                    double Value_1_4ns = curHurHits.get(0).rawTDC / 256;
                    double Value_1_2ns = curHurHits.get(0).interval % 2;
                    double time_1 = 4000 * Value_1_4ns + 2000 * Value_1_2ns + t_1;
                    time = time_1;
                    
                    System.out.println("Oho The number of hits is less than2");
                    System.out.println("The tdc of this Single hit is " + curHurHits.get(0).rawTDC);

                    if (curHurHits.get(0).rawTDC > 1000 && curHurHits.get(0).rawTDC < 50000) {
                        System.out.println("The SLCO is " + curSLCO.toString());
                        bvftdc.show();
                    }
                }

                bank.setByte("sector", row, (byte) curSLCO.sector);
                bank.setByte("layer", row, (byte) curSLCO.layer);
                bank.setShort("component", row, (short) curSLCO.component);
                bank.setByte("order", row, (byte) curSLCO.order);
                bank.setFloat("time", row, (float) time);

                row = row + 1;
            }
        }
        event.appendBank(bank);
    }

    public ArrayList< ArrayList<TDCHit>> GetOrganizedHits(ArrayList<TDCHit> v_allHits) {
        ArrayList< ArrayList<TDCHit>> v_organizedHits = new ArrayList<>();

        Iterator<TDCHit> it1 = v_allHits.listIterator();

        while (it1.hasNext()) {
            ArrayList<TDCHit> v_curHits = new ArrayList<>();

            TDCHit curHit = it1.next();

            v_curHits.add(curHit);

            Iterator<TDCHit> it2 = it1;

            while (it2.hasNext()) {

                TDCHit nextHit = it2.next();

                if (Math.abs(curHit.interval - nextHit.interval) < 3) {
                    v_curHits.add(nextHit);
                    it2.remove();
                    it2 = it1;
                }

            }

            v_organizedHits.add(v_curHits);
        }

        return v_organizedHits;
    }

    public class SLCO {

        int sector;
        int layer;
        int component;
        int order;
        private int hashCode;

        public SLCO(int sector, int layer, int component, int order) {
            this.sector = sector;
            this.layer = layer;
            this.component = component;
            this.order = order;

            this.hashCode = Objects.hash(sector, layer, component, order);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            SLCO that = (SLCO) o;

            return sector == that.sector && layer == that.layer && component == that.component
                    && order == that.order;
        }

        @Override
        public int hashCode() {
            return this.hashCode;
        }

        @Override
        public String toString() {
            String str = "******* SLCO object ************ \n";
            str += "sector = " + String.valueOf(sector);
            str += "   layer = " + String.valueOf(layer);
            str += "   componnt = " + String.valueOf(component);
            str += "   order = " + String.valueOf(order);

            return str;
        }

    }

    public class TDCHit {

        public TDCHit(int rawTDC, int interval, int tdcBin, Long timestamp) {
            this.rawTDC = rawTDC;
            this.interval = interval;
            this.tdcBin = tdcBin;
            this.timestamp = timestamp;
        }

        int interval;
        int rawTDC;
        int tdcBin;
        Long timestamp;

    }

}
