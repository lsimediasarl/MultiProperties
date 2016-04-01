/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lsimedia.multiproperties.standalone;

import javax.swing.DefaultListModel;

/**
 * Container for a list of multiproperties files
 *
 * @author sbodmer
 */
public class MultiPropertiesSession {
    String identifier = null;
    String title = "";

    DefaultListModel<MultiProperties> model = new DefaultListModel<>();
    
    /**
     * Create a session, if identifier is null, then a default identifier will be
     * created (System.currenTimeMillis)
     * @param title
     * @param identifier 
     */
    public MultiPropertiesSession(final String title, final String identifier) {
        this.title = title;
        this.identifier = identifier;
        if (this.identifier == null) this.identifier = ""+System.currentTimeMillis();
    }
    
    //**************************************************************************
    //*** API
    //**************************************************************************
    @Override
    public String toString() {
        return title;
    }
    
    public String getIdentifier() {
        return identifier;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(final String title) {
        this.title = title;
    }
    
    /**
     * Return the list of the files for this session
     * @return 
     */
    public DefaultListModel<MultiProperties> getModel() {
        return model;
    }
    
}
