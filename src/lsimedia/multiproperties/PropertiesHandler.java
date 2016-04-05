/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lsimedia.multiproperties;

import java.io.File;

/**
 * Multiproperties saver
 * 
 * @author sbodmer
 */
public interface PropertiesHandler {
    /**
     * Store the data
     * 
     * The source is the .multiproperties file
     * @param model
     * @return 
     */
    public boolean save(final MultiPropertiesTableModel model, final String name, final String description, final File source, final Logit logit);
    
    /**
     * Load the file and import into table model, return the added column, or
     * null if an error occured
     * 
     * @param model
     * @param f
     * @return 
     */
    public Column load(final MultiPropertiesTableModel model, final File source);
    
    /**
     * Return the handlers name (used to identify the class)
     * @return 
     */
    public String getName();
    
    /**
     * Return the handler gui panel already filled<p>
     * 
     * The passed source is for information only
     * 
     * @return 
     */
    public HandlerGUI getGUI(final Column column, final File source);
    
    /**
     * Return the last known error or null if none
     * @return 
     */
    public String getLastError();
    
}
