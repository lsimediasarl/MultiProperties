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
    public boolean save(MultiPropertiesTableModel model, String name, String description, File source);
    
    /**
     * Load the file and import into table model, return the added column, or
     * null if an error occured
     * 
     * @param model
     * @param f
     * @return 
     */
    public Column load(MultiPropertiesTableModel model, File f);
    
    /**
     * Return the handlers name (used to identify the class)
     * @return 
     */
    public String getName();
    
    /**
     * Return the handler gui panel already filled
     * 
     * @return 
     */
    public HandlerGUI getGUI(Column column);
}
