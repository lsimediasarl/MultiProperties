/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lsimedia.multiproperties;

import javax.swing.JComponent;

/**
 *
 * @author sbodmer
 */
public interface RecordGUI {
    /**
     * Apply the modifiaction to the current record
     */
    public void apply();
    
    /**
     * When the modification should be canceled
     */
    public void cancel();
    
    /**
     * Return the visual panel to display
     * 
     * @return 
     */
    public JComponent getVisual();
    
}
