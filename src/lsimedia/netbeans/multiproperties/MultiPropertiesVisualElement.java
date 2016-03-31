/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lsimedia.netbeans.multiproperties;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lsimedia.multiproperties.JMultiProperties;
import lsimedia.multiproperties.Logit;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.spi.actions.AbstractSavable;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@MultiViewElement.Registration(
        displayName = "#LBL_MultiProperties_VISUAL",
        iconBase = "lsimedia/netbeans/multiproperties/multi.png",
        mimeType = "text/multiproperties+xml",
        persistenceType = TopComponent.PERSISTENCE_NEVER,
        preferredID = "MultiPropertiesVisual",
        position = 2000
)
@Messages("LBL_MultiProperties_VISUAL=Visual")
public final class MultiPropertiesVisualElement extends JPanel implements MultiViewElement, PropertyChangeListener, ActionListener {

    
    private MultiPropertiesDataObject obj;
    private JToolBar toolbar = new JToolBar();
    private transient MultiViewElementCallback callback;
        
    JMultiProperties jm = null;
    Logit logit = new Logit();
    
    public MultiPropertiesVisualElement(Lookup lkp) {
        obj = lkp.lookup(MultiPropertiesDataObject.class);
        assert obj != null;

        initComponents();

        //--- Parse the file
        FileObject fo = obj.getPrimaryFile();
        
        jm = new JMultiProperties(logit);
        jm.setFile(new File(fo.getPath()));
        jm.setActionListener(this);
        add(jm, BorderLayout.CENTER);
        
        //--- Add the save action
        obj.addPropertyChangeListener(this);

        //--- Register save button
        
    }

    @Override
    public String getName() {
        return "MultiPropertiesVisualElement";
    }

    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        return toolbar;
    }

    @Override
    public Action[] getActions() {
        return new Action[0];
    }

    @Override
    public Lookup getLookup() {
        return obj.getLookup();
    }

    @Override
    public void componentOpened() {
        //---
    }

    @Override
    public void componentClosed() {
        //---
    }

    @Override
    public void componentShowing() {
        // System.out.println("SHOWED");
    }

    @Override
    public void componentHidden() {
        // System.out.println("HIDDEN");
    }

    @Override
    public void componentActivated() {
        // System.out.println("ACTIVATED");
    }

    @Override
    public void componentDeactivated() {
        // System.out.println("DEACTIVATED");
        
    }

    @Override
    public UndoRedo getUndoRedo() {
        return UndoRedo.NONE;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
        
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    
    //**************************************************************************
    //*** PropertyChangeEvent
    //**************************************************************************
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(DataObject.PROP_MODIFIED)) {
            //--- Register saveable ?
            //--- I'm a bit confused how the saveable works, please help here ;^)
            MultiSavable sav = new MultiSavable();
            
            
        }
    }

    //**************************************************************************
    //*** ActionListener
    //**************************************************************************
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == jm) {
            //--- The panel has modification, set the current object as modifiable
            //--- so the save button becomes active
            if (e.getActionCommand().equals(JMultiProperties.ACTION_COMMAND_MODIFIED)) obj.setModified(true);
            
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    
    //**************************************************************************
    //*** Private
    //**************************************************************************
    private class MultiSavable extends AbstractSavable {
        int hash = obj.hashCode();
        
        private MultiSavable() {
            register();
        }

        @Override
        protected String findDisplayName() {
            return obj.getName();
        }

        @Override
        protected void handleSave() throws IOException {
            //---
            jm.save(true);
            obj.setModified(false);
            unregister();
            
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MultiSavable) {
                MultiSavable m = (MultiSavable) obj;
                return m.hashCode() == hashCode();
            }
            return false;
        }

        @Override
        public int hashCode() {
            return hash;
        }

    }
}
