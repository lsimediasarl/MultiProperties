/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lsimedia.multiproperties.handlers;

import java.io.File;
import lsimedia.multiproperties.Column;
import lsimedia.multiproperties.HandlerGUI;
import lsimedia.multiproperties.Logit;
import lsimedia.multiproperties.MultiPropertiesTableModel;
import lsimedia.multiproperties.PropertiesHandler;

/**
 * Empty properties handler (simple container for multiproperties, no processing
 * here)<p>
 * 
 * @author sbodmer
 */
public class EmptyPropertiesHandler implements PropertiesHandler {
    public EmptyPropertiesHandler() {
        //---
    }
    
    public String toString() {
        return getName();
    }
    
    @Override
    public boolean save(MultiPropertiesTableModel model, String name, String description, File source, Logit logit) {
        return true;
    }

    @Override
    public Column load(MultiPropertiesTableModel model, File source) {
        return null;
        
    }

    @Override
    public String getName() {
        return "Empty";
    }

    @Override
    public HandlerGUI getGUI(Column column, File source) {
        return new JEmptyPropertiesHandler(column, source);
    }

    @Override
    public String getLastError() {
        return null;
    }
    
}
