/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lsimedia.multiproperties;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author sbodmer
 */
public class JMergeDialog extends javax.swing.JDialog implements ActionListener {

    MultiPropertiesTableModel current = null;
    MultiPropertiesTableModel source = null;

    /**
     * The source is the model to merge into the current model
     *
     */
    public JMergeDialog(final java.awt.Frame parent, final boolean modal, final MultiPropertiesTableModel current, final MultiPropertiesTableModel source) {
        super(parent, modal);
        this.current = current;
        this.source = source;
        
        prepare();
    }

    public JMergeDialog(final java.awt.Dialog parent, final boolean modal, final MultiPropertiesTableModel current, final MultiPropertiesTableModel source) {
        super(parent, modal);
        this.current = current;
        this.source = source;
        
        prepare();
    }

    private void prepare() {

        initComponents();

        //--- First column is the Key, pass it
        DefaultTableModel tm = (DefaultTableModel) TB_Columns.getModel();
        for (int i = 1;i < source.getColumnCount();i++) {
            Column c = source.getColumn(i);
            String name = "<html><b>" + c.getName() + "</b></html>";
            for (int j = 1;j < current.getColumnCount();j++) {
                if (current.getColumnName(j).equals(c.getName())) {
                    name = "<html>" + c.getName() + "</html>";
                    break;
                }
            }
            tm.addRow(new Object[]{true, name, c.getName()});
        }
        TB_Columns.getTableHeader().setFont(new Font("Arial", 0, 11));
        TB_Columns.getColumnModel().getColumn(0).setMaxWidth(22);

        BT_Ok.addActionListener(this);
        BT_Cancel.addActionListener(this);
    }

    //**************************************************************************
    //*** ActionListener
    //**************************************************************************
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("ok")) {
            //--- Start merging
            for (int i = 0;i < TB_Columns.getRowCount();i++) {
                boolean selected = (boolean) TB_Columns.getValueAt(i, 0);
                if (!selected) continue;

                //--- Find the column to merge into
                int columnIndex = -1;
                String into = TB_Columns.getValueAt(i, 2).toString();
                for (int j = 1;j < current.getColumnCount();j++) {
                    if (current.getColumnName(j).equals(into)) {
                        columnIndex = j;
                        break;
                    }

                }

                //--- If column not found, create a new one
                if (columnIndex == -1) {
                    //--- To be merged column (add 1,because the index 0 is the key column)
                    Column mc = source.getColumn(i + 1);

                    //--- Create new column in current model and fill it
                    Column c = new Column(into);
                    c.description = mc.description;
                    c.width = mc.width;
                    c.handlerConfiguration = mc.handlerConfiguration;

                    //--- Add a new value to each row of the current model and add the column
                    for (int j = 0;j < current.getRowCount();j++) current.getRecord(j).addColumn();
                    current.addColumn(c);

                    columnIndex = current.getColumnCount() - 1;
                }

                //--- Merge it, only handle property record, the value index to
                //--- merge into is defined in columIndex
                for (int j = 0;j < source.getRowCount();j++) {
                    Record tmp = source.getRecord(j);
                    if (!(tmp instanceof PropertyRecord)) continue;

                    PropertyRecord toMerge = (PropertyRecord) tmp;

                    //--- Search key in current model
                    Record rec = current.getRecord(toMerge.getKey());
                    if (rec == null) {
                        //--- Create new record
                        PropertyRecord pr = new PropertyRecord(toMerge.getKey());
                        pr.setDefaultValue(toMerge.getDefaultValue());
                        pr.setDisabled(toMerge.isDisabled());
                        pr.setMultiLine(toMerge.isMultiLine());
                        //--- Fill all values
                        for (int k = 0;k < current.getColumnCount() - 1;k++) {
                            pr.addColumn();
                            pr.setValueAt(k, toMerge.getValueAt(i));
                        }
                        current.addRecord(pr);

                    } else if (rec instanceof PropertyRecord) {
                        PropertyRecord pr = (PropertyRecord) rec;

                        //--- Replace the value at columnIndex
                        pr.setDefaultValue(toMerge.getDefaultValue());
                        pr.setDisabled(toMerge.isDisabled());
                        pr.setMultiLine(toMerge.isMultiLine());
                        pr.setValueAt(columnIndex - 1, toMerge.getValueAt(i));

                    } else {
                        //--- Key was found, but not a property column, do nothing

                    }

                }

            }

            setVisible(false);
            dispose();

        } else if (e.getActionCommand().equals("cancel")) {

            setVisible(false);
            dispose();
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

        jPanel2 = new javax.swing.JPanel();
        BT_Cancel = new javax.swing.JButton();
        BT_Ok = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        TB_Columns = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        BT_Cancel.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Cancel.setText("Cancel");
        BT_Cancel.setActionCommand("cancel");
        jPanel2.add(BT_Cancel);

        BT_Ok.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Ok.setText("Ok");
        BT_Ok.setActionCommand("ok");
        jPanel2.add(BT_Ok);

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        TB_Columns.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        TB_Columns.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "", "Column", "Merge into"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Boolean.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                true, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(TB_Columns);

        jLabel1.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jLabel1.setText("<html>New columns will be added at the end (new columns are in <b>bold</b>).<br><br>\n The current same keys values and default value will be overwritten by the merged ones.<br><br>\n New values will be added to the current file.<br><br>\nOnly values and disabled state will be merged (not description, handler, etc.).<br><br>\n It's possible to choose which column to merge into by changing the \"Merge into\" columns values.<br> </html>");
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jLabel2.setText("Please select the columns to merge into current file");
        jPanel3.add(jLabel2);

        getContentPane().add(jPanel3, java.awt.BorderLayout.PAGE_START);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        setVisible(false);
        dispose();
    }//GEN-LAST:event_formWindowClosing


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BT_Cancel;
    private javax.swing.JButton BT_Ok;
    private javax.swing.JTable TB_Columns;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
