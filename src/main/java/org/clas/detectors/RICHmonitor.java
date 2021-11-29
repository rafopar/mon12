package org.clas.detectors;

import org.clas.viewer.DetectorMonitor;
import org.jlab.geom.prim.Point3D;
import org.jlab.groot.data.DataLine;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.math.Axis;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.utils.groups.IndexedList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.List;



public class RICHmonitor  extends DetectorMonitor {

    private static final int NPMT        = 391;
    private static final int NANODE      = 64;
    private static final int NTILE       = 138;
    private static final double MAXMAP   = 5;
    private static final double MAXPMT   = 64*4;
    private static final double MAXPIXEL = 20;
    private static final double MAXTIME  = 4;
    private static final int NPMTROWS        = 23;
    private static final int NPIXELROWS      = 8;
    private static final int NPIXELCOLUMNS   = 8;
    private static final double PIXELSIZE    = 1;
    private static final double PMTCLEARANCE = 1;
  
    private static final double LE  = 100;
    private static final double TOT = 60;
    private static final double XT  = 25;
    private final int[] CHAN2PIX = {60, 58, 59, 57, 52, 50, 51, 49, 44, 42, 43, 41, 36, 34, 35, 33, 28, 26, 27, 25, 20, 18, 19, 17, 12, 10, 
                                    11, 9, 4, 2, 3, 1, 5, 7, 6, 8, 13, 15, 14, 16, 21, 23, 22, 24, 29, 31, 30, 32, 37, 39, 38, 40, 45, 47, 
                                    46, 48, 53, 55, 54, 56, 61, 63, 62, 64};
    private final Integer[] TWOTILERS = {3, 5, 7, 12, 15, 19, 24, 28, 33, 39, 44, 50, 57, 63, 70, 78, 85, 93, 102, 110, 119, 129, 138};
    private final int[] FIRSTPMTS = {1, 7, 14, 22, 31, 41, 52, 64, 77, 91, 106, 122, 139, 157, 176, 196, 217, 239, 262, 286, 311, 337, 364};   
            
    private IndexedList<Integer> tileToPMT = null;
    
    public RICHmonitor(String name) {
        super(name);

        this.setDetectorTabNames("Occupancy and time","Occupancy Map");
        this.tileToPMT = this.setTiletoPMTMap();
        this.init(false);
    }
    
    private IndexedList setTiletoPMTMap() {
        IndexedList<Integer> list = new IndexedList<>(2);
        List<Integer> twoTilers = Arrays.asList(TWOTILERS);
        int pmt=0;
        for(int i=0; i<NTILE; i++) {
            int tile = i+1;
            for(int j=0; j<3; j++) {
                if(j==1 && twoTilers.contains(tile)) continue;
                pmt++;
                list.add(pmt, tile, j);
            }
        }
        return list;
    }


    @Override
    public void createHistos() {
        H2F hi_pmt_leading_edge = new H2F("hi_pmt_leading_edge", "TDC Hit Leading Edge Time", NPMT, +0.5, NPMT+0.5, 100, 0, 300);
        hi_pmt_leading_edge.setTitleX("PMT");
        hi_pmt_leading_edge.setTitleY("Time (ns)");
        H2F hi_pmt_duration     = new H2F("hi_pmt_duration", "TDC Hit Time Over Threshold",   NPMT, +0.5, NPMT+0.5, 100, 0,  100);
        hi_pmt_duration.setTitleX("PMT");
        hi_pmt_duration.setTitleY("Time (ns)");
        H1F hi_pmt_occupancy    = new H1F("hi_pmt_occupancy", "PMT",   "Counts", NPMT, +0.5, NPMT+0.5);
        hi_pmt_occupancy.setTitle("PMT Hit Occupancy");
        hi_pmt_occupancy.setFillColor(25);
        hi_pmt_occupancy.setOptStat("1111");
        H1F hi_pmt_max          = new H1F("hi_pmt_max", " ", 1, 0.5, NPMT+0.5);
        hi_pmt_max.setLineWidth(2);
        hi_pmt_max.setLineColor(2);        
        H1F hi_pix_occupancy    = new H1F("hi_pix_occupancy", "Pixel", "Counts", NPMT*NANODE, -0.5, NPMT*NANODE-0.5);
        hi_pix_occupancy.setTitle("Pixel Hit Occupancy");
        hi_pix_occupancy.setFillColor(25);
        hi_pix_occupancy.setOptStat("1111");
        H1F hi_pix_max          = new H1F("hi_pix_max", " ", 1, 0.5, NPMT*NANODE+0.5);        
        hi_pix_max.setLineWidth(2);
        hi_pix_max.setLineColor(2);        
        H2F hi_scaler           = new H2F("hi_scaler", "TDC Hit Map",260, -130, 130, 207, 0, 207); 
        H1F hi_summary          = new H1F("summary", "PMT",   "Counts", NPMT, +0.5, NPMT+0.5);
        hi_summary.setTitle("RICH");
        hi_summary.setFillColor(25);
        DataGroup dg = new DataGroup(1,5); 
        dg.addDataSet(hi_pmt_leading_edge, 0);
        dg.addDataSet(hi_pmt_duration,     1);
        dg.addDataSet(hi_pmt_occupancy,    2);
        dg.addDataSet(hi_pmt_max,          2);
        dg.addDataSet(hi_pix_occupancy,    3);
        dg.addDataSet(hi_pix_max,          3);
        dg.addDataSet(hi_scaler,           4);
        this.getDataGroup().add(dg,0,0,0); 
        DataGroup sum = new DataGroup(1,1); 
        sum.addDataSet(hi_summary, 0);
        this.setDetectorSummary(sum);  
    }


    @Override
    public void plotHistos() {
        this.getDetectorCanvas().getCanvas("Occupancy and time").divide(2, 2);
        for(String tab: this.getDetectorTabNames()) {
            this.getDetectorCanvas().getCanvas(tab).setGridX(false);
            this.getDetectorCanvas().getCanvas(tab).setGridY(false);
        }

        Axis pmtAxis = this.getDataGroup().getItem(0,0,0).getH2F("hi_pmt_duration").getXAxis();
        DataLine lineLE  = new DataLine(pmtAxis.min(),LE,  pmtAxis.max(), LE);
        DataLine lineTOT = new DataLine(pmtAxis.min(),TOT, pmtAxis.max(), TOT);
        DataLine lineXT  = new DataLine(pmtAxis.min(),XT,  pmtAxis.max(), XT);
        this.getDetectorCanvas().getCanvas("Occupancy and time").cd(0);
        this.getDetectorCanvas().getCanvas("Occupancy and time").getPad(0).getAxisY().setLog(true);
        this.getDetectorCanvas().getCanvas("Occupancy and time").draw(this.getDataGroup().getItem(0,0,0).getH1F("hi_pmt_occupancy"));
        this.getDetectorCanvas().getCanvas("Occupancy and time").draw(this.getDataGroup().getItem(0,0,0).getH1F("hi_pmt_max"),"same");
        this.getDetectorCanvas().getCanvas("Occupancy and time").cd(1);
        this.getDetectorCanvas().getCanvas("Occupancy and time").getPad(1).getAxisY().setLog(true);
        this.getDetectorCanvas().getCanvas("Occupancy and time").draw(this.getDataGroup().getItem(0,0,0).getH1F("hi_pix_occupancy"));
        this.getDetectorCanvas().getCanvas("Occupancy and time").draw(this.getDataGroup().getItem(0,0,0).getH1F("hi_pix_max"),"same");
        this.getDetectorCanvas().getCanvas("Occupancy and time").cd(2);
        this.getDetectorCanvas().getCanvas("Occupancy and time").getPad(2).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("Occupancy and time").draw(this.getDataGroup().getItem(0,0,0).getH2F("hi_pmt_leading_edge"));
        this.getDetectorCanvas().getCanvas("Occupancy and time").draw(lineLE);
        this.getDetectorCanvas().getCanvas("Occupancy and time").cd(3);
        this.getDetectorCanvas().getCanvas("Occupancy and time").getPad(3).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("Occupancy and time").draw(this.getDataGroup().getItem(0,0,0).getH2F("hi_pmt_duration"));
        this.getDetectorCanvas().getCanvas("Occupancy and time").draw(lineXT);
        this.getDetectorCanvas().getCanvas("Occupancy and time").draw(lineTOT);
        this.getDetectorCanvas().getCanvas("Occupancy and time").update();
        this.getDetectorCanvas().getCanvas("Occupancy Map").getPad(0).setPalette("kRainBow");
        this.getDetectorCanvas().getCanvas("Occupancy Map").draw(this.getDataGroup().getItem(0,0,0).getH2F("hi_scaler"));
        this.getDetectorCanvas().getCanvas("Occupancy Map").getPad(0).getAxisZ().setLog(true);
        this.getDetectorCanvas().getCanvas("Occupancy Map").update();
        this.DrawTiles();
    }


    @Override
    public void processEvent(DataEvent event) {
        
        // process event info and save into data group
        if(event.hasBank("RICH::tdc")) {
            Map<Integer, ArrayList<TDCHit>> tdcMap = new HashMap<>();
 
            DataBank  bank = event.getBank("RICH::tdc");
            int rows = bank.rows();
            
            for(int i = 0; i < rows; i++) {
                int sector = bank.getByte("sector",i);     //4 by default (only 1 RICH at the time of writing)
                int  layer = bank.getByte("layer",i);      //byte variable, ranges from -127 to 127
                int   tile = layer & 0xFF;                 //conversion of byte to int variable, ranges from 1 to 138 (Tile Number)
                int   comp = bank.getShort("component",i); //short variable, comp is the MAROC ID shifted by 1 (ranges 1-192)
                int    tdc = bank.getInt("TDC",i);         //TDC value
                int  order = bank.getByte("order",i);      // order specifies leading or trailing edge

                int anode = CHAN2PIX[(comp-1) % NANODE];//from 1 to 64
                int asic  = (comp-1) / NANODE;
                int pmt   = this.tileToPMT.getItem(tile, asic);
                int pixel = (pmt-1)*NANODE + anode-1;//from 0 to 25023

                if(tdc>0) {
                    
                    if(!tdcMap.containsKey(pixel) && order==1) {
                        tdcMap.put(pixel, new ArrayList<>());
                        tdcMap.get(pixel).add(new TDCHit(tdc));
                    }
                    else {
                        if(order==1) {
                            tdcMap.get(pixel).add(new TDCHit(tdc));
                        }
                        else {
                            TDCHit last = tdcMap.get(pixel).get(tdcMap.get(pixel).size()-1);
                            if(last.getDuration()==0) last.setTrailingEdge(tdc);
                        }
                    }
                }

            }
            for(int pixel : tdcMap.keySet()) {
                for(TDCHit hit : tdcMap.get(pixel)) {
                    if(hit.getDuration()>0) {
                        int pmt   = pixel/NANODE + 1;
                        int anode = pixel%NANODE + 1;
                        this.getDataGroup().getItem(0,0,0).getH2F("hi_pmt_leading_edge").fill(pmt,hit.getLedingEdge());
                        this.getDataGroup().getItem(0,0,0).getH2F("hi_pmt_duration").fill(pixel,hit.getDuration());
                    
                        Point3D pxy = this.getCoordinates(pmt, anode);
                        this.getDataGroup().getItem(0,0,0).getH2F("hi_scaler").fill(pxy.x(), pxy.y());
                    
                        this.getDetectorSummary().getH1F("summary").fill(pmt);
                        this.getDataGroup().getItem(0,0,0).getH1F("hi_pmt_occupancy").fill(pmt);
                        this.getDataGroup().getItem(0,0,0).getH1F("hi_pix_occupancy").fill(pixel);
                    }
                }
            }
        }
    }





    @Override
    public void analysisUpdate() {
        double nentries = this.getDataGroup().getItem(0,0,0).getH2F("hi_scaler").getEntries();
        double average  = nentries/NPMT/NANODE;
        double max = average+0*Math.sqrt(average);
        this.getDataGroup().getItem(0,0,0).getH1F("hi_pmt_max").setBinContent(0, max*MAXPMT);
        this.getDataGroup().getItem(0,0,0).getH1F("hi_pix_max").setBinContent(0, max*MAXPIXEL);
        this.getDetectorCanvas().getCanvas("Occupancy and time").getPad(2).getAxisZ().setRange(0, max*MAXTIME);
        this.getDetectorCanvas().getCanvas("Occupancy and time").getPad(3).getAxisZ().setRange(0, max*MAXTIME);
        this.getDetectorCanvas().getCanvas("Occupancy Map").getPad().getAxisZ().setRange(0, max*MAXMAP);
    }

    private class TDCHit {
        private int leadingEdge;
        private int trailingEdge;
        
        TDCHit(int tdc) {
            this.leadingEdge = tdc;
        }
        
        public void setTrailingEdge(int tdc) {
            this.trailingEdge = tdc;
        }
        
        public int getLedingEdge() {
            return this.leadingEdge;
        }
        
        public int getDuration() {
            if(this.trailingEdge>this.leadingEdge)
                return this.trailingEdge-this.leadingEdge;
            else
                return 0;
        }
    }
    
    private void DrawTiles() {
        for(int i=0; i<NTILE; i++) {
            int tile = i+1;
            int pmt0 = this.tileToPMT.getItem(tile,2);
            int pmt2 = this.tileToPMT.getItem(tile,0);
            Point3D p1 = this.getCoordinates(pmt0, 1);
            Point3D p2 = this.getCoordinates(pmt0, 57);
            Point3D p3 = this.getCoordinates(pmt2, 64);
            Point3D p4 = this.getCoordinates(pmt2, 8);
            DataLine line1 = new DataLine(p1.x()-0.5, p1.y()+1.5, p2.x()-0.5, p2.y()-0.5);
            DataLine line2 = new DataLine(p2.x()-0.5, p2.y()-0.5, p3.x()+1.5, p3.y()-0.5);
            DataLine line3 = new DataLine(p3.x()+1.5, p3.y()-0.5, p4.x()+1.5, p4.y()+1.5);
            DataLine line4 = new DataLine(p4.x()+1.5, p4.y()+1.5, p1.x()-0.5, p1.y()+1.5);
            this.getDetectorCanvas().getCanvas("Occupancy Map").draw(line1);
            this.getDetectorCanvas().getCanvas("Occupancy Map").draw(line2);
            this.getDetectorCanvas().getCanvas("Occupancy Map").draw(line3);
            this.getDetectorCanvas().getCanvas("Occupancy Map").draw(line4);
        }
    }
        
    private Point3D getCoordinates(int pmt, int anode) {

        /* Finding the row and column of the pmt */
        int row = this.getPMTRow(pmt); 
        int col = this.getPMTColumn(pmt, row);

        /* Finding the position of the first anode of the row */
        Point3D anode1 = getRowAnode1(row);
        /* finding the local coordinates of the anode in the pmt */
        Point3D local = getLocalCoordinates(anode);

        double x = local.x() + anode1.x() + (col - 1) * (NPIXELCOLUMNS * PIXELSIZE + PMTCLEARANCE);
        double y = local.y() + anode1.y();
        //if (anode == 1) cout << "  localx=" << localx << "   localy=" << localy << "   x=" << *x << "  y=" << *y << endl;

        return new Point3D(x,y,0);
    }

    private Point3D getRowAnode1(int row) {
        /* Return the position of anode 1 of the leftmost pmt of the row 
        Anode 1 is top left pixel of the PMT 
        */
        double anode1x = - ((row-1)*0.5+3) * (NPIXELCOLUMNS * PIXELSIZE + PMTCLEARANCE);
        anode1x = Math.floor(anode1x);
        double anode1y =    row * (NPIXELCOLUMNS * PIXELSIZE + PMTCLEARANCE);

        return new Point3D(anode1x, anode1y, 0);
    }

    private Point3D getLocalCoordinates(int anode){
        /* anode start from 1 
        column is from 1 to 8
        row is from -1 to -8
        anode 1 is in column 1 and row -1
        anode 64 is in column 8 and row -8
        */

        /* row and column of the anode */
        int col =   1 + (anode - 1) % NPIXELROWS;
        int row = -(1 + (anode - 1) / NPIXELROWS);

        //cout << "anode=" << anode << "  row=" << row << "  col=" << column << endl;
        double localx = (col - 0) * PIXELSIZE;
        double localy = (row + 0) * PIXELSIZE;

        return new Point3D(localx, localy, 0);
    }

    private int getPMTRow(int pmt) {
        int row = NPMTROWS;
        for (int r = 0; r < NPMTROWS; r++) {
            if (pmt < this.FIRSTPMTS[r]) {
                row = r;
                break;
            }
        }
        return row;
    }

    private int getPMTColumn(int pmt, int row) {

        int col = 1 + pmt - this.FIRSTPMTS[row - 1];
        int nCols = getNColumns(row);
        col = 1 + nCols - col;

        return col;
    }

    private int getNColumns(int row) {
        if (row == NPMTROWS) {
            return NPMT - FIRSTPMTS[row - 1] + 1;
        }
        else {
            return FIRSTPMTS[row] - FIRSTPMTS[row - 1];
        }
    }

    
    
}
