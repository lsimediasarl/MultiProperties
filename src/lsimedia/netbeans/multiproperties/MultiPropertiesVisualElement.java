/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lsimedia.netbeans.multiproperties;

import java.awt.BorderLayout;
import java.awt.CardLayout;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lsimedia.multiproperties.JMultiProperties;
import lsimedia.multiproperties.Logit;
import lsimedia.multiproperties.LogitListener;
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
public final class MultiPropertiesVisualElement extends JPanel implements MultiViewElement, PropertyChangeListener, ActionListener, LogitListener  {

    
    private MultiPropertiesDataObject obj;
    private JToolBar toolbar = new JToolBar();
    private transient MultiViewElementCallback callback;
        
    /**
     * Logs related
     */
    DefaultListModel logs = new DefaultListModel<String>();
    SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");
    Logit logit = null;
    
    /**
     * Main panel
     */
    JMultiProperties jm = null;
    
    public MultiPropertiesVisualElement(Lookup lkp) {
        obj = lkp.lookup(MultiPropertiesDataObject.class);
        assert obj != null;
        logit = new Logit(this);
        
        initComponents();

        LI_Logs.setModel(logs);
        
        BT_Data.addActionListener(this);
        BT_Logs.addActionListener(this);
            
        //--- Parse the file
        FileObject fo = obj.getPrimaryFile();
        
        jm = new JMultiProperties(logit, false);
        jm.setFile(new File(fo.getPath()));
        jm.setActionListener(this);
        PN_Data.add(jm, BorderLayout.CENTER);
        
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
            
        } else if (e.getActionCommand().equals("data")) {
            CardLayout layout = (CardLayout) PN_Views.getLayout();
            layout.show(PN_Views, "data");
            
        } else if (e.getActionCommand().equals("logs")) {
            CardLayout layout = (CardLayout) PN_Views.getLayout();
            layout.show(PN_Views, "logs");
            
            
        }
    }
    
    //**************************************************************************
    //*** LogitListener
    //**************************************************************************
    @Override
    public void logitLineLogged(final String kind, final String message, final Object arg) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                String txt = "<html><i>" + tf.format(new Date()) + "</i>";
                if (kind.equals("E")) {
                    txt += " <font color=\"#ff0000\">(E)";
                    txt += " " + message;
                    txt += "</font>";

                } else if (kind.equals("I")) {
                    txt += " <font color=\"#aaaaaa\">(I)";
                    txt += " " +message;
                    txt += "</font>";

                } else {
                    txt += " (" + kind + ") " + message;
                }
                txt += "</html>";
                
                logs.insertElementAt(txt, 0);

                LB_Status.setText(txt);
            }
        });
        
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        BTG_Views = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        BT_Data = new javax.swing.JToggleButton();
        BT_Logs = new javax.swing.JToggleButton();
        jPanel3 = new javax.swing.JPanel();
        LB_Status = new javax.swing.JLabel();
        PN_Views = new javax.swing.JPanel();
        PN_Data = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        LI_Logs = new javax.swing.JList<>();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.BorderLayout());

        BTG_Views.add(BT_Data);
        BT_Data.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Data.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(BT_Data, org.openide.util.NbBundle.getMessage(MultiPropertiesVisualElement.class, "MultiPropertiesVisualElement.BT_Data.text")); // NOI18N
        BT_Data.setActionCommand(org.openide.util.NbBundle.getMessage(MultiPropertiesVisualElement.class, "MultiPropertiesVisualElement.BT_Data.actionCommand")); // NOI18N
        jPanel2.add(BT_Data);

        BTG_Views.add(BT_Logs);
        BT_Logs.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(BT_Logs, org.openide.util.NbBundle.getMessage(MultiPropertiesVisualElement.class, "MultiPropertiesVisualElement.BT_Logs.text")); // NOI18N
        BT_Logs.setActionCommand(org.openide.util.NbBundle.getMessage(MultiPropertiesVisualElement.class, "MultiPropertiesVisualElement.BT_Logs.actionCommand")); // NOI18N
        jPanel2.add(BT_Logs);

        jPanel1.add(jPanel2, java.awt.BorderLayout.WEST);

        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.LINE_AXIS));

        LB_Status.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(LB_Status, org.openide.util.NbBundle.getMessage(MultiPropertiesVisualElement.class, "MultiPropertiesVisualElement.LB_Status.text")); // NOI18N
        jPanel3.add(LB_Status);

        jPanel1.add(jPanel3, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.SOUTH);

        PN_Views.setLayout(new java.awt.CardLayout());

        PN_Data.setLayout(new java.awt.BorderLayout());
        PN_Views.add(PN_Data, "data");

        LI_Logs.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        LI_Logs.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        jScrollPane3.setViewportView(LI_Logs);

        PN_Views.add(jScrollPane3, "logs");

        add(PN_Views, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup BTG_Views;
    private javax.swing.JToggleButton BT_Data;
    private javax.swing.JToggleButton BT_Logs;
    private javax.swing.JLabel LB_Status;
    private javax.swing.JList<String> LI_Logs;
    private javax.swing.JPanel PN_Data;
    private javax.swing.JPanel PN_Views;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane3;
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
