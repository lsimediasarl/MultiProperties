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
public interface HandlerGUI {
    /**
     * Return the visual panel to display
     * 
     * @return 
     */
    public JComponent getVisual();
 
    /**
     * Apply the changes
     */
    public void apply();
    
    /**
     * Cancel the changes
     */
    public void cancel();
    
}
