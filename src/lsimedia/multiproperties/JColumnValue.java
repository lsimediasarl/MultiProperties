/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lsimedia.multiproperties;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author sbodmer
 */
public class JColumnValue extends javax.swing.JPanel implements ActionListener {
    
    String def = "";
    boolean fi = false;
    
    /**
     * Creates new form JColumnValue
     */
    public JColumnValue(String label, boolean disabled, boolean fi, String value, String def, boolean lockdown) {
        this.def = def;
        
        initComponents();
        
        CB_Column.setText(label);
        CB_Column.setSelected(!disabled);
        CB_Column.addActionListener(this);
        
        CB_Final.setSelected(fi);
        CB_Final.addActionListener(this);
        CB_Final.setEnabled(!lockdown);
        TA_Value.setEditable(!fi);
        CB_Column.setEnabled(!fi);
        
        TA_Value.setText(disabled?def:value);
        TA_Value.setEnabled(!disabled);
    }

    //**************************************************************************
    //*** API
    //**************************************************************************
    public boolean isDisabled() {
        return !CB_Column.isSelected();
    }
    public String getValue() {
        return TA_Value.getText();
    }
    
    public boolean isFinal() {
        return CB_Final.isSelected();
    }
    
    public void setValue(String value) {
        TA_Value.setText(value);
    }
    
    /**
     * Set the focus
     */ 
    public void focus() {
        TA_Value.requestFocusInWindow();
    }
    
    //**************************************************************************
    //*** ActionListener
    //**************************************************************************
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("enable")) {
            TA_Value.setEnabled(CB_Column.isSelected());
            TA_Value.setText(CB_Column.isSelected()?"":def);
            
        } else if (e.getActionCommand().equals("final")) {
            TA_Value.setEditable(!CB_Final.isSelected());
            
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        CB_Column = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        TA_Value = new javax.swing.JTextArea();
        CB_Final = new javax.swing.JCheckBox();

        setMaximumSize(new java.awt.Dimension(32767, 100));
        setPreferredSize(new java.awt.Dimension(680, 100));

        CB_Column.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        CB_Column.setText("...");
        CB_Column.setActionCommand("enable");
        CB_Column.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        CB_Column.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        CB_Column.setMaximumSize(new java.awt.Dimension(200, 22));
        CB_Column.setMinimumSize(new java.awt.Dimension(200, 22));
        CB_Column.setPreferredSize(new java.awt.Dimension(200, 22));

        TA_Value.setColumns(20);
        TA_Value.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        TA_Value.setRows(5);
        jScrollPane1.setViewportView(TA_Value);

        CB_Final.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        CB_Final.setText("Final (cannot be modified)");
        CB_Final.setActionCommand("final");
        CB_Final.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        CB_Final.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(CB_Column, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(CB_Final, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(CB_Column, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(CB_Final)
                        .addGap(0, 41, Short.MAX_VALUE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox CB_Column;
    private javax.swing.JCheckBox CB_Final;
    private javax.swing.JTextArea TA_Value;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    
}
