package org.clas.detectors;

import org.clas.viewer.DetectorMonitor;
import org.jlab.groot.data.H2F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;


import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.util.Arrays;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.MouseInputAdapter;
import org.jlab.groot.data.H1F;
import java.util.Map;
import org.jlab.detector.view.DetectorShape2D;

import java.util.HashMap;
import org.jlab.detector.base.DetectorType;


import org.jlab.groot.data.DataLine;
import org.jlab.utils.groups.IndexedTable;

/* Edited by Nicholaus Trotta and Joshua GoodWill */


public class RICHmonitor  extends DetectorMonitor {

    IndexedTable richStatus = null;
    private static int NPMT   = 391;
    private static int NPIXEL = 64;
    
    public RICHmonitor(String name) {
        super(name);

        this.setDetectorTabNames("PMT Window","Occupancies and spectra","RICH TDC");
        this.init(false);
        
        this.getCcdb().setVariation("default");
        this.getCcdb().init(Arrays.asList(new String[]{"/calibration/rich/pixels"}));
    }



    @Override
    public void createHistos() {
        this.getDetectorCanvas().getCanvas("PMT Window").setGridX(false);
        this.getDetectorCanvas().getCanvas("PMT Window").setGridY(false);

        this.getDetectorCanvas().getCanvas("PMT Window").setGridX(false);//no grid on the PMT window x-axis
        this.getDetectorCanvas().getCanvas("PMT Window").setGridY(false);//no grid on the PMT window y-axis

        H2F RichScaler = new H2F("RichScaler", "RICH Occupancy",261, 0, 261, 207, 0, 207);//creates 2D histogram, X from (0 to 261) and Y from (0 to 207). 
        DataGroup dg = new DataGroup(4,4); //4x4 data group 
        dg.addDataSet(RichScaler, 0);//(ID of data set, order)
        this.getDataGroup().add(dg,0,0,0); //adds something to dg
        
        
        boarder();//call border drawing method

        int row = 23;//initialize total number of rows in RICH
        double col= 0;
        double count = 28; // # of pmts per row decreasing by row
        int rowTemp =0; //temps used to count to 9 then places a space between PMTs
        int colTemp =0;
        int pmt= 391;


        for(int  rowNum = 23*8 +23; rowNum> 0; rowNum--) { //# of rows * 8 + spaces in between rows

            int layer = rowNum;
            double  comp = 0;
            double colStart = col; //reset column


            if(rowTemp == 9) {
                rowTemp =0;
                //pmt--;
                //System.out.print("pmt="+pmt);
            } else {

                for(int colNum = 0; colNum != count*8.0+count+9; colNum++) { // count = number of column; count * 8 + count + 9


                    if(colTemp ==9) { // adds a space between PMTs
                        colTemp =0;
                    }  else {
                        comp = col  + colNum;
                        //this.getDataGroup().getItem(0,0,0).getH2F("RichScaler").fill(comp*1.0,layer*1.0);
                    }
                    colTemp++;
                }
            }
            pmt--;


            col = colStart;
            if((rowNum) %  9== 0 && rowNum != 25*8) {
                col +=4.5;
                count--;
                //pmt--;
                //System.out.print("pmt="+pmt);
            }

            rowTemp++;
            pmt--;
        }


        // initialize canvas and create histograms
        this.setNumberOfEvents(0);//initialize at zero
        this.getDetectorCanvas().getCanvas("Occupancies and spectra").divide(1, 3);//puts three plots on Tab 2
        this.getDetectorCanvas().getCanvas("Occupancies and spectra").setGridX(false);//no x-grid
        this.getDetectorCanvas().getCanvas("Occupancies and spectra").setGridY(false);//no y-grid
        H2F summary = new H2F("summary","summary",192, 0.5, 192.5, 138, 0.5, 138.5);//create summary plot, in the summary tab of clas12mon
        summary.setTitleX("MAPMT channel");//title X
        summary.setTitleY("tile");//title Y
        summary.setTitle("RICH");//plot title
        DataGroup sum = new DataGroup(1,1); //create a 1x1 datagroup for the summary plot
        sum.addDataSet(summary, 0);//(ID of data set, order)
        this.setDetectorSummary(sum);//set the data group for the summary plot

        H2F occTDC = new H2F("occTDC", "occTDC", 192, 0.5, 192.5, 138, 0.5, 138.5);//instantiate first plot on Occupany tab. X from (0 to 192)and Y from (0 to 138)
        occTDC.setTitleY("tile number");//y-axis title
        occTDC.setTitleX("channel number");//x-axis title
        occTDC.setTitle("TDC Occupancy");//plot title
        H2F tdc_leading_edge = new H2F("tdc_leading_edge", "tdc leading edge", 260, 0, 260, 417, 0.5, 417.5);//instantiate second plot. X from (0 to 260) and Y from (0 to 417)
        tdc_leading_edge.setTitleX("leading edge time [ns]");//x-axis title
        tdc_leading_edge.setTitleY("MAPMT (3 slots / tile)");//y-axis title
        tdc_leading_edge.setTitle("TDC timing");//plot title
        H2F tdc_trailing_edge = new H2F("tdc_trailing_edge", "tdc trailing edge", 260, 0, 260, 417, 0.5, 417.5);//instantiate third plot. X from (0 to 260) and Y from (0 to 417)
        tdc_trailing_edge.setTitleX("trailing edge time [ns]");//x-axis title
        tdc_trailing_edge.setTitleY("MAPMT (3 slots / tile)");//y-axis title
        tdc_trailing_edge.setTitle("TDC timing");//plot title
        //  DataGroup dg = new DataGroup(2,2);
        dg.addDataSet(occTDC, 1);//add data set for 1st plot, order 1
        dg.addDataSet(tdc_leading_edge, 2);//add data set for 2nd plot, order 2
        dg.addDataSet(tdc_trailing_edge, 2);//add data set for 3rd plot, order 2
        this.getDataGroup().add(dg,0,0,0);//add these new additions to the data group
    } 


    public void boarder() { // creates boarder around PMTs
        int pmt= 391;
        int[] twoBlock = {391,365,336,310,287,261,238,218,195,175,158,138,121,107,90,76,65,51,40,32,18,13,8}; //blocks that have two pmts instead of 3
        int[] firstTile = {363,336,310,285,261,238,216,195,175,156,138,121,105,90,76,63,51,40,30,21,13,6}; //start of each block

        double x0 =5, x = x0, y = 207.5, x1 =0, y1 =0;
        int j =0;

        DataLine dl = new DataLine(x,y,x1,y);
        for(int i =0; pmt >0;) {
            if(pmt == firstTile[j]) {
                y = y1;
                x = x0 + 4.5;
                x0 = x;
                if(j < 21)
                    j++;
            }
            if(pmt == twoBlock[i]) {
                x1= (x+8*2)+1;
                y1= y -9;
                dl =  new DataLine(x,y,x1,y);
                this.getDetectorCanvas().getCanvas("PMT Window").draw(dl);

                dl = new DataLine(x,y1,x1,y1);
                this.getDetectorCanvas().getCanvas("PMT Window").draw(dl);

                dl =  new DataLine(x,y,x,y1);
                this.getDetectorCanvas().getCanvas("PMT Window").draw(dl);

                dl =  new DataLine(x1,y,x1,y1);
                this.getDetectorCanvas().getCanvas("PMT Window").draw(dl);
                if(i <22)
                    i++;
                pmt-=2;

                x = x1 +1;
            } else {
                x1= (x+8*3)+2;
                y1= y -9;
                dl =  new DataLine(x,y,x1,y);
                this.getDetectorCanvas().getCanvas("PMT Window").draw(dl);

                dl =  new DataLine(x,y1,x1,y1);
                this.getDetectorCanvas().getCanvas("PMT Window").draw(dl);

                dl =  new DataLine(x,y,x,y1);
                this.getDetectorCanvas().getCanvas("PMT Window").draw(dl);

                dl =  new DataLine(x1,y,x1,y1);
                this.getDetectorCanvas().getCanvas("PMT Window").draw(dl);

                x = x1 +1;

                pmt-=3;
            }


        }


    }
 

    private int[] firstTile = {2,5,8,11,15,19,23,28,33,38,44,50,56,63,70,77,85,93,101,110,119,128,138};


    public void fillTile(int Ham_ID,long layer) {//this function fills tiles based on the XY position of the pixels in the histograms
        
        int row = 0;
        int NumofTiles = 5;
        int tempLayer = 0;
        int count = 2;
             
        while(tempLayer != layer) {
            row++;

            NumofTiles++;


            if(tempLayer == 2 || tempLayer == 11 || tempLayer == 23 || tempLayer == 38 || tempLayer == 56 || tempLayer == 77|| tempLayer == 101 || tempLayer == 128 ) {
                count++;//These templayers are left most tiles, going every 2 rows. Not sure why it counts every two rows
            }

            for(int i = 0; i < count; i++) {
                tempLayer++;
                if(tempLayer == layer) {
                    break;
                }

            }

            //System.out.println("in this row " + row + " NumofTiles: " + NumofTiles + " count:  " + count + " tempLayer: " + tempLayer );
        }//after all this, tempLayer==layer, and the number of tiles in row is set

        if(layer == 3 || layer ==5 || layer ==7|| layer ==12 || layer ==15 || layer ==19 || layer ==24|| layer ==28 || layer ==33 ||layer ==39|| layer ==44 || layer ==50|| layer ==57|| layer ==63 || layer ==70 || layer ==78|| layer ==85 || layer ==93 || layer == 102|| layer == 110 || layer == 119 || layer ==129|| layer ==138) {//Layers = two PMT tiles
            if(Ham_ID >64 && Ham_ID < 129) { //Two PMT tiles have channel values ranging from right to left: 0-64, 129-192, based on the ASIC construction in the PMT map
                Ham_ID+=64;//add 64 to skip over the "empty middle tile". See MAPMT map on RICH wiki for visual explanation of the skipping
            }
        }


        //  System.out.println(row + " " + layer);
        int  firstRowTile = firstTile[row-1]; //first tile in a row?


        // System.out.print("First tile = " + firstRowTile + " ");


        double x = 0;//This X is the x-coordinate on the histogram

        if(row % 2 == 0) {//Aka, if its an even row
            x  =  5 + (4.5*(23-row)) + 1; //5 + 1. If row=22, x=10.5. X-position of the start of a given row
        } else {

            x =  5 + (4.5*(23-row));//5 is the distance from window boundary. If row=23, this is the top row. if row=1, x=14
        }

        for(int tempTile = firstRowTile; tempTile > layer; tempTile--) {//increases x position for new tiles
            if(tempTile ==3 || tempTile == 5 || tempTile ==7 || tempTile == 12 || tempTile ==15 || tempTile ==19 || tempTile ==24|| tempTile ==28 || tempTile ==33 || tempTile ==39|| tempTile ==44 || tempTile ==50 || tempTile ==57|| tempTile ==63 || tempTile ==70 || tempTile ==78|| tempTile ==85) {//Two PMT tiles
                x += 2*9;
            } else  if( tempTile ==93 || tempTile == 102|| tempTile == 110 || tempTile == 119 || tempTile ==129|| tempTile ==138) {//Also two PMT tiles, why was this split up?
                x += 2*9;
            } else {//All other tiles; Triplet tiles
                x += 3*9;
            }
        }



        int y = row *  8 + row - 1;//Initialize the Y-position based on the row

        if(layer == 3 || layer ==5 || layer ==7|| layer ==12 || layer ==15 || layer ==19 || layer ==24|| layer ==28 || layer ==33 ||layer ==39|| layer ==44 || layer ==50|| layer ==57|| layer ==63 || layer ==70 || layer ==78|| layer ==85 || layer ==93 || layer == 102|| layer == 110 || layer == 119 || layer ==129|| layer ==138) {//if two tile PMT then

            if (Ham_ID > 0 && Ham_ID < 9) {//row 1
                x+= (Ham_ID - 1 + 9);
            } else if (Ham_ID  > 8 && Ham_ID < 17 ) {//row 2
                y--;
                x+= (Ham_ID - 9 + 9);
            } else if (Ham_ID  > 16 && Ham_ID < 25) {//row 3
                x+= (Ham_ID - 17 + 9);
                y-=2;
            } else if (Ham_ID  > 24 && Ham_ID < 33) {//row 4
                x+= (Ham_ID - 25 + 9);
                y-=3;
            } else if (Ham_ID  > 32 && Ham_ID < 41) {//row 5
                x+= (Ham_ID - 33 + 9);
                y-=4;
            } else if (Ham_ID > 40  && Ham_ID < 49) {//row 6
                x+= (Ham_ID - 41 + 9);
                y-=5;
            } else if (Ham_ID  > 48  && Ham_ID < 57) {//row 7
                x+= (Ham_ID - 49 + 9);
                y-=6;
            } else if (Ham_ID  > 56  && Ham_ID < 65) {//row 8
                x+= (Ham_ID - 57 + 9);
                y-=7;
            } else if (Ham_ID  > 128  && Ham_ID < 137) {//Start 2nd PMT. Starts from pixel 129. Row 1
                x+= (Ham_ID - 120 - 9);
            } else if (Ham_ID  > 136  && Ham_ID < 145) {//row 2
                x+= (Ham_ID - 128 - 9);
                y--;
            } else if (Ham_ID  > 144  && Ham_ID < 153) {//row 3
                x+= (Ham_ID - 136 - 9);
                y-=2;
            } else if (Ham_ID  > 152  && Ham_ID < 161) {//row 4
                x+= (Ham_ID - 144 - 9);
                y-=3;
            } else if (Ham_ID  > 160  && Ham_ID < 169) {//row 5
                x+= (Ham_ID - 152 - 9);
                y-=4;
            } else if (Ham_ID  > 168  && Ham_ID < 177) {//row 6
                x+= (Ham_ID - 160 - 9);
                y-=5;
            } else if (Ham_ID  > 176  && Ham_ID < 185) {//row 7
                x+= (Ham_ID - 168 - 9);
                y-=6;
            } else if (Ham_ID  > 184  && Ham_ID < 193) {//row 8
                x+= (Ham_ID - 176 - 9);
                y-=7;
            }
        } else {//Start 1st PMT in triplet tile (refer to map & tile diagram)
            if(Ham_ID > 0 && Ham_ID < 9) {//row 1
                x+= (Ham_ID - 1 + 18);
            } else if (Ham_ID  > 8 && Ham_ID < 17 ) {//row 2
                y--;
                x+= (Ham_ID - 9 + 18);
            } else if (Ham_ID  > 16 && Ham_ID < 25) { //row 3
                x+= (Ham_ID - 17 + 18);
                y-=2;
            } else if (Ham_ID  > 24 && Ham_ID < 33) { //row 4
                x+= (Ham_ID - 25 + 18);
                y-=3;
            } else if (Ham_ID  > 32 && Ham_ID < 41) { //row 5
                x+= (Ham_ID - 33 + 18);
                y-=4;
            } else if (Ham_ID > 40  && Ham_ID < 49) { //row 6
                x+= (Ham_ID - 41 + 18);
                y-=5;
            } else if (Ham_ID  > 48  && Ham_ID < 57) { //row 7
                x+= (Ham_ID - 49 + 18);
                y-=6;
            } else if (Ham_ID  > 56  && Ham_ID < 65) { //row 8
                x+= (Ham_ID - 57 + 18);
                y-=7;
            //Start 2nd PMT in triplet tile   
            } else if (Ham_ID > 64 && Ham_ID <73) { //row 1
                x+= (Ham_ID - 65 + 9);
            } else if (Ham_ID > 72 && Ham_ID < 81) {//row 2
                x+= (Ham_ID - 73 + 9);
                y--;
            } else if (Ham_ID > 80 && Ham_ID < 89) { //row 3
                x+= (Ham_ID - 81 + 9);
                y-=2;
            } else if (Ham_ID > 88 && Ham_ID < 97) { //row 4
                x+= (Ham_ID - 89 + 9);
                y-=3;
            } else if (Ham_ID > 96 && Ham_ID < 105) { //row 5
                x+= (Ham_ID - 97 + 9);
                y-=4;
            } else if (Ham_ID > 104 && Ham_ID < 113) { //row 6
                x+= (Ham_ID - 105 + 9);
                y-=5;
            } else if (Ham_ID > 112 && Ham_ID < 121) { //row 7
                x+= (Ham_ID - 113 + 9);
                y-=6;
            } else if (Ham_ID > 120 && Ham_ID < 129) { //row 8
                x+= (Ham_ID - 121 + 9);
                y-=7;
            //Start 3rd PMT in triplet tile
            } else if (Ham_ID > 128 && Ham_ID < 137) {//row 1
                x+= (Ham_ID - 129);
            } else if (Ham_ID > 136 && Ham_ID < 145) {//row 2
                x+= (Ham_ID - 137);
                y--;
            } else if (Ham_ID > 144 && Ham_ID < 153) {//row 3
                x+= (Ham_ID - 145);
                y-=2;
            } else if (Ham_ID > 152 && Ham_ID < 161) {//row 4
                x+= (Ham_ID - 153);
                y-=3;
            } else if (Ham_ID > 160 && Ham_ID < 169) {//row 5
                x+= (Ham_ID - 161);
                y-=4;
            } else if (Ham_ID > 168 && Ham_ID < 177) {//row 6
                x+= (Ham_ID - 169);
                y-=5;
            } else if (Ham_ID > 176 && Ham_ID < 185) {//row 7
                x+= (Ham_ID - 177);
                y-=6;
            } else if (Ham_ID > 184 && Ham_ID < 193) {//row 8
                x+= (Ham_ID - 185);
                y-=7;
            }
        }//end positioning loop

        //remove some of these hot channels.
        
    //Check to see if layer & Ham_ID match a hot channel, if so, exclude the hot channel. Otherwise, layer plots as intended
    if (layer==21) {
        if (Ham_ID!=126) {
            this.getDataGroup().getItem(0,0,0).getH2F("RichScaler").fill(x*1.0,y*1.0);
//            System.out.println("Excluded Cross-talk channel for PMT: 56 with Pixel: 64");
        }    
    }

    else if (layer==26) {
        if (Ham_ID!=34) {
            this.getDataGroup().getItem(0,0,0).getH2F("RichScaler").fill(x*1.0,y*1.0);
 //           System.out.println("Excluded Cross-talk channel for PMT: 69 with Pixel: 34");
        }    
    }

    else if (layer==43) {
        if (Ham_ID!=114 && Ham_ID!=115 && Ham_ID!=116 && Ham_ID!=123 && Ham_ID!=124) {
            this.getDataGroup().getItem(0,0,0).getH2F("RichScaler").fill(x*1.0,y*1.0);
//            System.out.println("Excluded Cross-talk channel for PMT: 118 with Pixels: 50, 51, 52, 59, and 60");
        }    
    }

    else if (layer==53) {
        if (Ham_ID!=169 && Ham_ID!=177) {
            this.getDataGroup().getItem(0,0,0).getH2F("RichScaler").fill(x*1.0,y*1.0);
//            System.out.println("Excluded Cross-talk channel for PMT: 147 with Pixels: 41 and 49");
        }    
    }

    else if (layer==56) {
        if (Ham_ID!=76) {
            this.getDataGroup().getItem(0,0,0).getH2F("RichScaler").fill(x*1.0,y*1.0);
//            System.out.println("Excluded Cross-talk channel for PMT: 155 with Pixel: 12");
        }    
    }

    else if (layer==58) {
        if (Ham_ID!=70 && Ham_ID!=72) {
            this.getDataGroup().getItem(0,0,0).getH2F("RichScaler").fill(x*1.0,y*1.0);
//            System.out.println("Excluded Cross-talk channel for PMT: 160 with Pixels: 6 and 8");
        }    
    }

    else if (layer==59) {
        if (Ham_ID!=89 && Ham_ID!=90) {
            this.getDataGroup().getItem(0,0,0).getH2F("RichScaler").fill(x*1.0,y*1.0);
//            System.out.println("Excluded Cross-talk channel for PMT: 163 with Pixels: 25 and 26");
        }    
    }

    else if (layer==62) {
        if (Ham_ID!=162 && Ham_ID!=169 && Ham_ID!=172 && Ham_ID!=177) {
            this.getDataGroup().getItem(0,0,0).getH2F("RichScaler").fill(x*1.0,y*1.0);
//            System.out.println("Excluded Cross-talk channel for PMT: 173 with Pixels: 34, 41, 44, and 49");
        }    
    }

    else if (layer==66) {
        if (Ham_ID!=191) {
            this.getDataGroup().getItem(0,0,0).getH2F("RichScaler").fill(x*1.0,y*1.0);
//            System.out.println("Excluded Cross-talk channel for PMT: 184 with Pixel: 63");
        }    
    }

    else if (layer==67) {
        if (Ham_ID!=106) {
            this.getDataGroup().getItem(0,0,0).getH2F("RichScaler").fill(x*1.0,y*1.0);
//            System.out.println("Excluded Cross-talk channel for PMT: 186 with Pixel: 42");
        }    
    }

    else if (layer==111) {
        if (Ham_ID!=128) {
            this.getDataGroup().getItem(0,0,0).getH2F("RichScaler").fill(x*1.0,y*1.0);
//            System.out.println("Excluded Cross-talk channel for PMT: 312 with Pixel: 64");
        }    
    }

    else if (layer==112) {
        if (Ham_ID!=15 && Ham_ID!=16) {
            this.getDataGroup().getItem(0,0,0).getH2F("RichScaler").fill(x*1.0,y*1.0);
//            System.out.println("Excluded Cross-talk channel for PMT: 314 with Pixels: 15 and 16");
        }    
    }

    else if (layer==117) {
        if (Ham_ID!=155 && Ham_ID!=162 && Ham_ID!=164) {
            this.getDataGroup().getItem(0,0,0).getH2F("RichScaler").fill(x*1.0,y*1.0);
//            System.out.println("Excluded Cross-talk channel for PMT: 331 with Pixels: 27, 34, and 36");
        }    
    }

    else if (layer==122) {
        if (Ham_ID!=122) {
            this.getDataGroup().getItem(0,0,0).getH2F("RichScaler").fill(x*1.0,y*1.0);
//            System.out.println("Excluded Cross-talk channel for PMT: 334 with Pixel: 58");
        }    
    }

    else if (layer==128) {
        if (Ham_ID!=14) {
            this.getDataGroup().getItem(0,0,0).getH2F("RichScaler").fill(x*1.0,y*1.0);
//            System.out.println("Excluded Cross-talk channel for PMT: 361 with Pixel: 14");
        }    
    }

    else if (layer==132) {
        if (Ham_ID!=121) {
            this.getDataGroup().getItem(0,0,0).getH2F("RichScaler").fill(x*1.0,y*1.0);
//            System.out.println("Excluded Cross-talk channel for PMT: 373 with Pixel: 57");
        }    
    }

    else if (layer==134) {
        if (Ham_ID!=42 && Ham_ID!=50 && Ham_ID!=93 && Ham_ID!=94) {
            this.getDataGroup().getItem(0,0,0).getH2F("RichScaler").fill(x*1.0,y*1.0);
//            System.out.println("Excluded Cross-talk channel for PMT: 378 with Pixels: 42, 50 and PMT: 379 with Pixels: 29 and 30");
        }    
    }

    else {
        this.getDataGroup().getItem(0,0,0).getH2F("RichScaler").fill(x*1.0,y*1.0);
    } 
    
    }//end fillTile Method


    @Override
    public void plotHistos() {

        this.getDetectorCanvas().getCanvas("Occupancies and spectra").cd(0);
        this.getDetectorCanvas().getCanvas("Occupancies and spectra").getPad(0).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("Occupancies and spectra").draw(this.getDataGroup().getItem(0,0,0).getH2F("occTDC"));
        this.getDetectorCanvas().getCanvas("Occupancies and spectra").cd(1);
        this.getDetectorCanvas().getCanvas("Occupancies and spectra").getPad(1).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("Occupancies and spectra").draw(this.getDataGroup().getItem(0,0,0).getH2F("tdc_leading_edge"));
        this.getDetectorCanvas().getCanvas("Occupancies and spectra").cd(2);
        this.getDetectorCanvas().getCanvas("Occupancies and spectra").getPad(2).getAxisZ().setLog(getLogZ());
        this.getDetectorCanvas().getCanvas("Occupancies and spectra").draw(this.getDataGroup().getItem(0,0,0).getH2F("tdc_trailing_edge"));
        this.getDetectorCanvas().getCanvas("Occupancies and spectra").update();
        
        this.getDetectorCanvas().getCanvas("PMT Window").getPad(0).setPalette("kRainBow");
        this.getDetectorCanvas().getCanvas("PMT Window").draw(this.getDataGroup().getItem(0,0,0).getH2F("RichScaler"));
        //this.getDetectorCanvas().getCanvas("PMT Window").getPad(0).getAxisZ().setLog(getLogZ());//set it to start from 10 to 1000 maybe
        this.getDetectorCanvas().getCanvas("PMT Window").update();


        this.getDetectorCanvas().getCanvas("RICH TDC").setStatBoxFontSize(14);
        //this.getDetectorCanvas().getCanvas("RICH TDC").draw(hdet.htdc0);
        //this.getDetectorCanvas().getCanvas("RICH TDC").draw(hdet.htdc1, "same");
        hdet.hdeltaT.setOptStat("1111");
        this.getDetectorCanvas().getCanvas("RICH TDC").draw(hdet.hdeltaT);
        this.getDetectorCanvas().getCanvas("RICH TDC").draw(hdet.dl0);
        this.getDetectorCanvas().getCanvas("RICH TDC").draw(hdet.dl1);
 

        this.getDetectorView().getView().repaint();
        this.getDetectorView().update();

    }



    private final int nleftTile[] = {2, 5, 8, 11, 15, 19, 23, 28, 33, 38, 44, 50, 56, 63, 70, 77, 85, 93, 101, 110, 119, 128, 138};//left-most tile on each row
    private static int PMT1[]={1,4,7,9,12,14,17,19,22,25,28,31,33,36,39,41,44,47,50,52,55,58,61,64,66,69,72,75,77,80,83,86,89,91,94,97,100,
                        103,106,108,111,114,117,120,122,125,128,131,134,137,139,142,145,148,151,154,157,159,162,165,168,171,174,176,
                        179,182,185,188,191,194,196,199,202,205,208,211,214,217,219,222,225,228,231,234,237,239,242,245,248,251,254,
                        257,260,262,265,268,271,274,277,280,283,286,288,291,294,297,300,303,306,309,311,314,317,320,323,326,329,332,
                        335,337,340,343,346,349,352,355,358,361,364,366,369,372,375,378,381,384,387,390};                        
    private static int PMT2[]={2,5,0,10,0,15,0,20,23,26,29,0,34,37,0,42,45,48,0,53,56,59,62,0,67,70,73,0,78,81,84,87,0,92,95,98,101,104,0,
                        109,112,115,118,0,123,126,129,132,135,0,140,143,146,149,152,155,0,160,163,166,169,172,0,177,180,183,186,189,
                        192,0,197,200,203,206,209,212,215,0,220,223,226,229,232,235,0,240,243,246,249,252,255,258,0,263,266,269,272,
                        275,278,281,284,0,289,292,295,298,301,304,307,0,312,315,318,321,324,327,330,333,0,338,341,344,347,350,353,
                        356,359,362,0,367,370,373,376,379,382,385,388,0};
    private static int PMT3[]={3,6,8,11,13,16,18,21,24,27,30,32,35,38,40,43,46,49,51,54,57,60,63,65,68,71,74,76,79,82,85,88,90,93,96,99,102,
                       105,107,110,113,116,119,121,124,127,130,133,136,138,141,144,147,150,153,156,158,161,164,167,170,173,175,178,181,
                       184,187,190,193,195,198,201,204,207,210,213,216,218,221,224,227,230,233,236,238,241,244,247,250,253,256,259,261,
                       264,267,270,273,276,279,282,285,287,290,293,296,299,302,305,308,310,313,316,319,322,325,328,331,334,336,339,342,
                       345,348,351,354,357,360,363,365,368,371,374,377,380,383,386,389,391};
    private static int chan2pix[] = {60, 58, 59, 57, 52, 50, 51, 49, 44, 42, 43, 41, 36, 34, 35, 33, 28, 26, 27, 25, 20, 18, 19, 17, 12, 10, 
                              11, 9, 4, 2, 3, 1, 5, 7, 6, 8, 13, 15, 14, 16, 21, 23, 22, 24, 29, 31, 30, 32, 37, 39, 38, 40, 45, 47, 
                              46, 48, 53, 55, 54, 56, 61, 63, 62, 64};
    private final double pmtW = 8; //number of pixels in a new
    private RICHtile[] rtile = new RICHtile[nleftTile[nleftTile.length - 1]];

    @Override
    public void processEvent(DataEvent event) {
        
        // process event info and save into data group
        if(event.hasBank("RICH::tdc")) {
            Map<Integer, Integer> tdcMap0 = new HashMap<>();
            Map<Integer, Integer> tdcMap1 = new HashMap<>();
            
            if (event.hasBank("RUN::config")) {
                DataBank head = event.getBank("RUN::config");
                int runNumber = head.getInt("run", 0);
                richStatus = this.getCcdb().getConstants(runNumber,"/calibration/rich/pixels");
            }

 
            DataBank  bank = event.getBank("RICH::tdc");
            int rows = bank.rows();
            
            for(int i = 0; i < rows; i++) {
                int     sector = bank.getByte("sector",i);//4 by default (only 1 RICH at the time of writing)
                int  layerbyte = bank.getByte("layer",i);//byte variable, ranges from -127 to 127
                int      layer = layerbyte & 0xFF;//conversion of byte to long variable, ranges from 1 to 138 (Tile Number)
                int       comp = bank.getShort("component",i);//short variable, comp is the MAROC ID shifted by 1 (ranges 1-192)
                int        pmt = comp/64;//determines which PMT in tile
                int        tdc = bank.getInt("TDC",i);//TDC value
                int  orderbyte = bank.getByte("order",i); // order specifies leading or trailing edge

                if(tdc>0) {
                    int sector_table = 4;
                    int Ham_ID = 0;
                    int Compare_Ham_ID = 0;
                    int Layer_to_pmt = 0;

                    //initialize three arrays. The entries of the vector are
                    //the PMT located in a layer in that position (PMT1=PMT in position 1, see ASIC diagram
                    //on the RICH Front_End Electronics wiki
                    //The index of an array + 1 corresponds to the the layer for which that PMT is located in
                    //begin conversion from comp to pixel value. The commented print lines ahead are checks to make sure it works.
                    if (comp < 65) { //convert MAROC IDs 1-64 to Pixel 1-64
                        Ham_ID = chan2pix[comp - 1];
                        Compare_Ham_ID = chan2pix[comp - 1];
                        Layer_to_pmt = PMT1[(int) (layer) - 1];//convert layer to PMT to try to match hot channel position
                        //System.out.println("Hamamatsu ID: " + Ham_ID);
                    } else if (comp > 64 && comp < 129) {//convet MAROC IDs 65-128 to Pixel 65-128
                        Ham_ID = chan2pix[comp - 65] + 64;
                        Compare_Ham_ID = chan2pix[comp - 65];
                        Layer_to_pmt = PMT2[(int) (layer) - 1];
                        //System.out.println("Hamamatsu ID: " + Ham_ID);
                    } else if (comp > 128 && comp < 193) {//convert MAROC IDs 129-192 to Pixel 129-192
                        Ham_ID = chan2pix[comp - 129] + 128;
                        Compare_Ham_ID = chan2pix[comp - 129];
                        Layer_to_pmt = PMT3[(int) (layer) - 1];
                        //System.out.println("Hamamatsu ID: " + Ham_ID);
                    }
                    
                    int status = richStatus.getIntValue("status", sector_table, Layer_to_pmt, Compare_Ham_ID);
                        
                    this.getDataGroup().getItem(0,0,0).getH2F("occTDC").fill(comp,layer); //fill occupancies by comp & layer    
                    if(status!=5) {
                        fillTile(Ham_ID,layer);
                    }
                    //fillTile(Ham_ID,layer); //sends this info to the fill tile method

                    if(orderbyte == 1) this.getDataGroup().getItem(0,0,0).getH2F("tdc_leading_edge").fill(tdc, layer*3 + pmt); //checks if the tdc is leading or trailing, then plots
                    if(orderbyte == 0) this.getDataGroup().getItem(0,0,0).getH2F("tdc_trailing_edge").fill(tdc, layer*3 + pmt); //checks if the tdc is leading or trailing, then pltos

                    int id = sector * 200000+ layerbyte * 1000 + (int) comp; //??

                    if(orderbyte==0) tdcMap0.put(id, tdc); //??
                    else tdcMap1.put(id,tdc); //??

                    this.getDetectorSummary().getH2F("summary").fill(comp,layer); //fills the summary plot on the first tab of the monitor

                }

            }

            tdcMap0.forEach((k,v) -> { //what is k, what is v?
              hdet.htdc0.fill(v); //filling delta TDC with whatever v is
              //if (k>1 && k<1.5) {
              if(tdcMap1.containsKey(k)) hdet.hdeltaT.fill(v-tdcMap1.get(k)); //if tdcmap1 (leading edge TDC)
              //if(tdcMap0.containsKey(k)) hdet.hdeltaT.fill(v-tdcMap0.get(k)); //for tdcmap0 (trailing edge)
              //}
            });
            tdcMap1.forEach((k,v) -> hdet.htdc1.fill(v));
        }
    }





    @Override
    public void analysisUpdate() {
        double nentries = this.getDataGroup().getItem(0,0,0).getH2F("RichScaler").getEntries();
        double average  = nentries/NPMT/NPIXEL;
        double max = 4*average;
        this.getDetectorCanvas().getCanvas("PMT Window").getPad().getAxisZ().setRange(0, max);
    }


    public class PixelXY {

        public double x, y;

        PixelXY(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public class RICHtile {

        private int nmapmts;
        private DetectorShape2D pmts[];
        PixelXY[] pxy = new PixelXY[192];

        RICHtile(int id) {
            this(id, 3);
        }

        RICHtile(int id, int nmapmts) {
            this.nmapmts = nmapmts;
            pmts = new DetectorShape2D[nmapmts];
            
            for (int imaroc = 0; imaroc < nmapmts; imaroc++) {
                pmts[imaroc] = new DetectorShape2D();
                pmts[imaroc].getDescriptor().setType(DetectorType.RICH);
                pmts[imaroc].getDescriptor().setSectorLayerComponent(1, id, nmapmts == 2 && imaroc == 1 ? imaroc + 1 : imaroc);
                pmts[imaroc].createBarXY(pmtW, pmtW);
            }
        }

        double getWidth() {
            return pmtW * nmapmts;
        }

        void setPosition(double x0, double y0) {
            for (int imaroc = 0; imaroc < nmapmts; imaroc++) {
                double x1 = x0 + (nmapmts - imaroc - 1) * pmtW;
                pmts[imaroc].getShapePath().translateXYZ(x1, y0, 0.0);
                for (int irow = -4; irow < 4; irow++) {
                    for (int icol = -4; icol < 4; icol++) {
                        pxy[imaroc * 64 + (irow+4) * 8 + (icol+4)] = new PixelXY(x1 + icol, y0 + irow);
                    }
                }
            }
        }

        public DetectorShape2D getPMT(int imaroc) {
            return pmts[imaroc];
        }

        public DetectorShape2D[] getPMTs() {
            return pmts;
        }

        public PixelXY getPixel(int imaroc, int ipix) {
            if (nmapmts == imaroc) {
                imaroc--;
            }
            return pxy[imaroc * 64 + ipix];
        }
    }



    public PixelXY getPixel(int itile, int imaroc, int ipix) {
        return rtile[itile].getPixel(imaroc, ipix);

    }


    private class HistTDC {

        H1F htdc0 = new H1F("RICH TDC0", "TDC", 260, 0, 260);
        H1F htdc1 = new H1F("RICH TDC1", "TDC", 260, 0, 260);
        H1F hdeltaT = new H1F("RICH delta", "delta TDC", 150, 0, 150);
        DataLine dl0 = new DataLine(28,0,28,3e10);
        DataLine dl1 = new DataLine(60,0,60,3e10);


        public HistTDC() {
            htdc1.setLineColor(2);
            htdc0.setLineColor(4);
            dl0.setLineColor(4);
            dl1.setLineColor(4);
        }

        public void setTitle(String title) {

            htdc0.setTitle(title);
            htdc1.setTitle(title);
            hdeltaT.setTitle(title);

        }
    }
    
    public JPanel mainPanel = new JPanel(new BorderLayout());
    public JComboBox tdcBox, lvlBox;
    private int selectedITile = 0, selectedIMaroc = 0;
    private int ntiles = 138, nmarocs = 3, npixs = 64;

    public HistTDC hdet = new HistTDC();
    private HistTDC[][] hpmt = new HistTDC[ntiles][nmarocs];
    private HistTDC[][][] hpix = new HistTDC[ntiles][nmarocs][npixs];

    public void RichPlotTDC() {
        tdcBox = new JComboBox(new String[] {"TDC", "Tover"});
        lvlBox = new JComboBox(new String[] {"detector", "pmt", "pixel"});

        lvlBox.addActionListener(ev -> redraw());
        tdcBox.addActionListener(ev -> redraw());

        JToolBar toolBar = new JToolBar();
        toolBar.setLayout(new FlowLayout());
        toolBar.add(tdcBox);
        toolBar.add(lvlBox);


        this.getDetectorPanel().add(toolBar, BorderLayout.PAGE_START);

        reset();
    }



    public class ResizableLine extends JComponent {

        private double x1, y1, x2, y2;

        ResizableLine(double x1, double y1, double x2, double y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }


        public void paintComponent(Graphics g) {
            Line2D line = new Line2D.Double(x1, y1, x2, y2);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.GREEN);
            g2d.draw(line);
        }
    }

    public class DragListener extends MouseInputAdapter {

        Point location;
        MouseEvent pressed;

        @Override
        public void mousePressed(MouseEvent me) {
            pressed = me;
        }

        @Override
        public void mouseDragged(MouseEvent me) {
            Component component = me.getComponent();
            location = component.getLocation(location);
            int x = location.x - pressed.getX() + me.getX();
            int y = location.y - pressed.getY() + me.getY();
            component.setLocation(x, y);
        }
    }

    private void redraw() {
/*
        this.getDetectorCanvas().getCanvas("RICH TDC").clear();
        this.getDetectorCanvas().getCanvas("RICH TDC").divide(1, 1);
        this.getDetectorCanvas().getCanvas("RICH TDC").setStatBoxFontSize(14);

        if (lvlBox.getSelectedIndex() == 0) {
            if (tdcBox.getSelectedIndex() == 0) {
                this.getDetectorCanvas().getCanvas("RICH TDC").draw(hdet.htdc0);
                this.getDetectorCanvas().getCanvas("RICH TDC").draw(hdet.htdc1, "same");
            } else {
                this.getDetectorCanvas().getCanvas("RICH TDC").cd(1);
                hdet.hdeltaT.setOptStat("1111");
                this.getDetectorCanvas().getCanvas("RICH TDC").draw(hdet.hdeltaT);
            }
        } else if (lvlBox.getSelectedIndex() == 1) {
            if (tdcBox.getSelectedIndex() == 0) {
                this.getDetectorCanvas().getCanvas("RICH TDC").draw(hpmt[selectedITile][selectedIMaroc].htdc0);
                this.getDetectorCanvas().getCanvas("RICH TDC").draw(hpmt[selectedITile][selectedIMaroc].htdc1, "same");
            } else {
                hpmt[selectedITile][selectedIMaroc].hdeltaT.setOptStat("1111");
                this.getDetectorCanvas().getCanvas("RICH TDC").draw(hpmt[selectedITile][selectedIMaroc].hdeltaT);
            }
        } else {
            this.getDetectorCanvas().getCanvas("RICH TDC").divide(8, 8);
            if (tdcBox.getSelectedIndex() == 0) {
                for (int ipix = 0; ipix < npixs; ipix++) {
                    this.getDetectorCanvas().getCanvas("RICH TDC").cd(ipix);
                    this.getDetectorCanvas().getCanvas("RICH TDC").draw(hpix[selectedITile][selectedIMaroc][ipix].htdc0);
                    this.getDetectorCanvas().getCanvas("RICH TDC").draw(hpix[selectedITile][selectedIMaroc][ipix].htdc1, "same");
                }
            } else {
                for (int ipix = 0; ipix < npixs; ipix++) {
                    this.getDetectorCanvas().getCanvas("RICH TDC").cd(ipix);
                    this.getDetectorCanvas().getCanvas("RICH TDC").draw(hpix[selectedITile][selectedIMaroc][ipix].hdeltaT);
                }
            }
        }
*/
    }


    public void reset() {
        hdet = new HistTDC();
        hpmt = new HistTDC[ntiles][nmarocs];
        hpix = new HistTDC[ntiles][nmarocs][npixs];

        hdet = new HistTDC();
        hdet.setTitle("Integrated over RICH");

        for (int itile = 0; itile < ntiles; itile++) {
            for (int imaroc = 0; imaroc < nmarocs; imaroc++) {
                hpmt[itile][imaroc] = new HistTDC();
                hpmt[itile][imaroc].setTitle("Integrated over PMT: tile " + (itile + 1) + " maroc " + imaroc);
            }
        }

        for (int itile = 0; itile < ntiles; itile++) {
            for (int imaroc = 0; imaroc < nmarocs; imaroc++) {
                for (int ipix = 0; ipix < npixs; ipix++) {
                    hpix[itile][imaroc][ipix] = new HistTDC();
                    hpix[itile][imaroc][ipix].setTitle("tile/maroc/pix: " + (itile + 1) + " / " + imaroc + " / " + (ipix + 1));
                }
            }
        }

        redraw();
    }


    public JPanel getPanel() {
        return mainPanel;
    }

    public void processShape(DetectorShape2D shape) {
        selectedITile = shape.getDescriptor().getLayer() - 1;
        selectedIMaroc = shape.getDescriptor().getComponent();
        redraw();
    }
}
