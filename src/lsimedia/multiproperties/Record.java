/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lsimedia.multiproperties;

import lsimedia.multiproperties.Column;
import java.util.ArrayList;
import javax.swing.JComponent;
import org.w3c.dom.Element;


/**
 *
 * @author sbodmer
 */
public abstract class Record implements Cloneable {
    
    public Record() {
        //---
    }
    
    public Record(Element record) {
        //---
    }

    /**
     * Returns the key of the record or null if none
     */
    public abstract String getKey();
    
    /**
     * Return the panel to changed the record
     * @return 
     */
    public abstract RecordGUI getGUI(MultiPropertiesTableModel model, int selectedColumn);
    
    /**
     * Dump the record
     * 
     * @param records 
     */
    public abstract void save(Element records);
    
    /**
     * Move a value from an index to a new one
     * 
     * @param fromIndex
     * @param toIndex 
     */
    public abstract void swapColumn(int fromIndex, int toIndex);
    
    /**
     * Add at the end
     */
    public abstract void addColumn();
    
    /**
     * Remove the column
     * 
     * @param index 
     */
    public abstract void removeColumn(int index);
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
