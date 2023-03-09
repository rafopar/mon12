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
 * @author rafopar
 */
public class uRWellMonitor extends DetectorMonitor {

    public uRWellMonitor(String name) {
        super(name);
        this.setDetectorTabNames("Hits");

        EmbeddedCanvas canvas = this.getDetectorCanvas().getCanvas("Hits");
        this.init(false);
    }

    @Override
    public void createHistos() {
        
    }

}
