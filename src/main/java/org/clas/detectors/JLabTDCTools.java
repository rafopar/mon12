package org.clas.detectors;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

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
                
                while( (strLineLUT = brLUT.readLine() ) != null ){
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
    
    void MakeCalibratedBank( DataEvent event ){
        
        int nhits = 10;
        DataBank bank = event.createBank("FTOF::vfTime", nhits);
        
//        for( int i = 0; i < nhits; i++ ){
//            bank.setInt("sector", i, i);
//            bank.setInt("layer", i, 2*i);
//        }
        
        event.appendBank(bank);
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

            this.hashCode = this.hashCode();
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

    }

}
