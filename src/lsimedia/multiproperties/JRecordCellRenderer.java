/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lsimedia.multiproperties;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author sbodmer
 */
public class JRecordCellRenderer extends javax.swing.JPanel implements TableCellRenderer {

    static public final String COLOR_DISABLED = "#ff00ff";  // "#888888"
    static public final String COLOR_COMMENT = "#0000ff";
    static public final String COLOR_ERROR = "#ff0000";
    static public final String COLOR_FINAL = "#ff00ff";
    /**
     * Creates new form JRecordCellRenderer
     */
    public JRecordCellRenderer() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        LB_Content = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        LB_Content.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        LB_Content.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        add(LB_Content, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel LB_Content;
    // End of variables declaration//GEN-END:variables

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        LB_Content.setOpaque(column == 0);
        String txt = "";
        if (value instanceof String) {
            txt = value.toString().replaceAll("\n", "\\\\n");
            LB_Content.setText(txt);

        } else if (value instanceof CommentRecord) {
            CommentRecord cr = (CommentRecord) value;
            txt = "<html><font color=\"" + COLOR_COMMENT + "\"><b>" + cr.value + "</b></font></html>";
            LB_Content.setText(txt);

        } else if (value instanceof PropertyRecord) {
            PropertyRecord pr = (PropertyRecord) value;
            if (column == 0) {
                boolean same = false;
                //--- Check if multiple same keys
                for (int i=0;i<table.getRowCount();i++) {
                    Record rec = (Record) table.getModel().getValueAt(i, 0);
                    if (rec.getKey() != null) {
                        if ((i != row) && rec.getKey().equals(pr.getKey())) same = true;
                    }
                    
                }
                
                if (same) {
                    txt = "<html><pre><b><font color=\"" + COLOR_ERROR + "\">" + pr.name + "</font></b></pre></html>";
                    
                } else if (pr.disabled) {
                    txt = "<html><pre><b><i><font color=\"" + COLOR_DISABLED + "\">" + pr.name + "</font></i></b></pre></html>";
                    
                } else {
                    txt = "<html><pre><b>" + pr.name + "</b></pre></html>";
                    
                }
                
            } else {
                PropertyRecord.Value v = pr.getValueAt(column - 1);
                txt = v.getValue().replaceAll("\n", "<br>");
                LB_Content.setOpaque(v.fi);
                if (v.disabled) {
                    txt = pr.defaultValue.replaceAll("\n", "<br>");
                    txt = "<html><i><font color=\"" + COLOR_DISABLED + "\">" + txt + "</font></i></html>";
                    
                } else {
                    txt = "<html>"+txt+"</html>";
                    
                }

            }
            LB_Content.setText(txt);

        } else if (value instanceof EmptyRecord) {
            LB_Content.setText("");

        }
        setToolTipText(txt);
        if (isSelected) {
            LB_Content.setOpaque(false);
            LB_Content.setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());

        } else {
            LB_Content.setForeground(table.getForeground());
            setBackground(table.getBackground());

        }
        return this;
    }
}
