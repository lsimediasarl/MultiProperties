/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lsimedia.multiproperties.standalone;

import java.io.File;
import lsimedia.multiproperties.JMultiProperties;

/**
 * Container for the multiproperties file, contains the file reference and
 * the gui to handle it
 * 
 * @author sbodmer
 */
public class MultiProperties {
    JMultiProperties jm = null;
    File file = null;
    
    public MultiProperties(File file) {
        this.file = file;
    }
    //**************************************************************************
    //*** API
    //**************************************************************************
    @Override
    public String toString() {
        if (jm == null) return file.getPath();
        if (jm.isModified()) return "<html><b>"+file.getPath()+"</b></html>";
        return file.getPath();
    }
    
    public File getFile() {
        return file;
    }
    
    public JMultiProperties getVisual() {
        return jm;
    }
    
    public void setVisual(JMultiProperties jm) {
        this.jm = jm;
    }
}
