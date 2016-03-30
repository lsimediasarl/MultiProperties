/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lsimedia.multiproperties;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import lsimedia.multiproperties.handlers.JavaPropertiesHandler;
import lsimedia.netbeans.multiproperties.*;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
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

public final class JMultiProperties extends JPanel implements ActionListener, MouseListener, ListSelectionListener {

    public static final String ACTION_COMMAND_MODIFIED = "dataModified";

    File file = null;

    DefaultListModel columns = new DefaultListModel();
    MultiPropertiesTableModel model = new MultiPropertiesTableModel();

    DocumentBuilder builder = null;

    /**
     * The current selected column
     */
    Column selected = null;

    /**
     * Unique listener for data changes, the event fired is ACTION_PERFORMED
     * with "dataModified" as action name
     *
     */
    ActionListener listener = null;

    public JMultiProperties(File file) {
        this.file = file;

        initComponents();

        CMB_Handlers.addItem(new JavaPropertiesHandler());

        TB_Table.setModel(model);
        TB_Table.getTableHeader().setFont(new Font("Arial", 0, 11));
        TB_Table.addMouseListener(this);
        TB_Table.setDefaultRenderer(Record.class, new JRecordCellRenderer());

        LI_Columns.setModel(columns);
        LI_Columns.addListSelectionListener(this);

        BT_Import.addActionListener(this);
        BT_ColumnUp.addActionListener(this);
        BT_ColumnDown.addActionListener(this);

        BT_Add.addActionListener(this);
        BT_Remove.addActionListener(this);

        BT_NewComment.addActionListener(this);
        BT_NewEmpty.addActionListener(this);
        BT_NewProperty.addActionListener(this);
        BT_Up.addActionListener(this);
        BT_Down.addActionListener(this);
        BT_Copy.addActionListener(this);
        BT_Delete.addActionListener(this);

        BT_ConfigureHandler.addActionListener(this);

        MN_Copy.addActionListener(this);
        MN_Delete.addActionListener(this);

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();

            parseMultiproperties(file);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //--- Parse the file

    }

    //**************************************************************************
    //*** API
    //**************************************************************************
    /**
     * Unique listener for data changes
     *
     * @param listener
     */
    public void setActionListener(ActionListener listener) {
        this.listener = listener;
    }

    /**
     * Save the files to filesystem, two steps, first dave the .multiproperties
     * xml file, than for each column, save the properties via the handler<p>
     *
     */
    public void save() {
        //--- Save the multiproperties files and each properties
        try {
            PropertiesHandler handler = (PropertiesHandler) CMB_Handlers.getSelectedItem();

            if (selected != null) {
                //--- Save the current state of the selected column
                selected.setName(TF_ColumnName.getText().trim());
                selected.setDescription(TA_ColumnDescription.getText().trim());
            }

            //--- Save the column width in object
            TableColumnModel cm = TB_Table.getColumnModel();
            for (int i = 0;i < cm.getColumnCount();i++) {
                Column c = model.getColumn(i);
                TableColumn tc = cm.getColumn(i);
                c.setWidth(tc.getWidth());
            }

            Document doc = builder.newDocument();
            doc.setXmlStandalone(true);
            Element root = doc.createElementNS("hu.skzs.multiproperties", "MultiProperties");
            doc.appendChild(root);

            //--- Main data
            Element e = doc.createElement("Version");
            e.appendChild(doc.createTextNode(LB_Version.getText().trim()));
            root.appendChild(e);

            e = doc.createElement("Name");
            e.appendChild(doc.createTextNode(TF_Name.getText().trim()));
            root.appendChild(e);

            e = doc.createElement("Description");
            e.appendChild(doc.createTextNode(TA_Description.getText().trim()));
            root.appendChild(e);

            e = doc.createElement("Handler");
            e.appendChild(doc.createTextNode(handler.getName().trim()));
            root.appendChild(e);

            //--- Columns
            e = doc.createElement("Columns");
            for (int i = 0;i < model.getColumnCount();i++) {
                Column c = model.getColumn(i);
                c.save(e);
            }
            root.appendChild(e);

            //--- Records
            e = doc.createElement("Records");
            for (int i = 0;i < model.getRowCount();i++) {
                Record r = model.getRecord(i);
                r.save(e);
            }
            root.appendChild(e);

            //--- 
            TransformerFactory tfactory = TransformerFactory.newInstance();
            tfactory.setAttribute("indent-number", new Integer(4));
            Transformer transformer = tfactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource source = new DOMSource(doc);
            // File tmp = new File("/tmp/ml.multiproperties");
            StreamResult result = new StreamResult(file.toString());
            transformer.transform(source, result);

            System.out.println("SAVED TO :" + file.getPath());

            //--- Pass the model to the properties handler to save the specific file
            handler.save(model, TF_Name.getText(), TA_Description.getText(), file);

        } catch (Exception ex) {
            ex.printStackTrace();

        }

    }

    //**************************************************************************
    //*** ActionListener
    //**************************************************************************
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("add")) {
            String columnName = JOptionPane.showInputDialog(this, "New column name (fr, de, it, ...)");
            if (columnName != null) {
                Column c = new Column(columnName);
                columns.addElement(c);

                //--- Add a new value to each column
                for (int i = 0;i < model.getRowCount();i++) model.getRecord(i).addColumn();
                model.addColumn(c);

                if (listener != null) listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ACTION_COMMAND_MODIFIED));
            }

        } else if (e.getActionCommand().equals("remove")) {
            //--- Remove the selected column
            int index = LI_Columns.getSelectedIndex();
            if (index != -1) {
                Column c = LI_Columns.getSelectedValue();
                columns.removeElement(c);

                //--- Add a new value to each column
                for (int i = 0;i < model.getRowCount();i++) model.getRecord(i).removeColumn(index);
                model.removeColumn(c);

                CardLayout layout = (CardLayout) PN_ColumnConfig.getLayout();
                layout.show(PN_ColumnConfig, "empty");

                if (listener != null) listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ACTION_COMMAND_MODIFIED));
            }

        } else if (e.getActionCommand().equals("handler")) {
            //--- Configure the handler for the selected column
            PropertiesHandler h = (PropertiesHandler) CMB_Handlers.getSelectedItem();

            JHandlerDialog dlg = new JHandlerDialog(null, true, h, selected);
            dlg.pack();
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);

            if (listener != null) listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ACTION_COMMAND_MODIFIED));

        } else if (e.getActionCommand().equals("import")) {
            //--- Configure the handler for the selected column
            PropertiesHandler h = (PropertiesHandler) CMB_Handlers.getSelectedItem();
            JFileChooser jf = new JFileChooser();
            jf.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jf.setMultiSelectionEnabled(true);
            int rep = jf.showOpenDialog(this);
            if (rep == JFileChooser.APPROVE_OPTION) {
                File f[] = jf.getSelectedFiles();
                for (int i = 0;i < f.length;i++) {
                    Column c = h.load(model, f[i]);
                    if (c != null) columns.addElement(c);
                }
            }

        } else if (e.getActionCommand().equals("columnUp")) {
            int index = LI_Columns.getSelectedIndex();
            if (index > 0) {
                Column from = (Column) columns.get(index);
                Column to = (Column) columns.get(index - 1);
                columns.setElementAt(from, index - 1);
                columns.setElementAt(to, index);

                //--- Add +1 to avoid the key column
                model.swapColumn(index + 1 - 1, index + 1);

                LI_Columns.setSelectedIndex(index - 1);
            }

        } else if (e.getActionCommand().equals("columnDown")) {
            int index = LI_Columns.getSelectedIndex();
            if (index < columns.getSize() - 1) {
                Column from = (Column) columns.get(index);
                Column to = (Column) columns.get(index + 1);
                columns.setElementAt(from, index + 1);
                columns.setElementAt(to, index);

                //--- Add +1 to avoid the key column
                model.swapColumn(index + 1, index + 1 + 1);

                LI_Columns.setSelectedIndex(index + 1);
            }

        } else if (e.getActionCommand().equals("newComment")) {
            String comment = JOptionPane.showInputDialog(this,"Comment");
            if (comment != null) {
                int index = TB_Table.getSelectedRow();
                model.insertRecord(index < 0 ? 0 : index, new CommentRecord(comment));

            }

        } else if (e.getActionCommand().equals("newProperty")) {
            String name = JOptionPane.showInputDialog(this, "Key");
            if (name != null) {
                int index = TB_Table.getSelectedRow();
                PropertyRecord rec = new PropertyRecord(name);
                for (int i = 0;i < columns.size();i++) rec.addColumn();
                model.insertRecord(index < 0 ? 0 : index, rec);

            }

        } else if (e.getActionCommand().equals("newEmpty")) {
            int index = TB_Table.getSelectedRow();
            EmptyRecord rec = new EmptyRecord();
            for (int i = 0;i < columns.size();i++) rec.addColumn();
            model.insertRecord(index < 0 ? 0 : index, rec);

        } else if (e.getActionCommand().equals("delete")) {
            int indices[] = TB_Table.getSelectedRows();
            ArrayList<Record> list = new ArrayList<>();
            for (int i = 0;i < indices.length;i++) list.add(model.getRecord(indices[i]));
            for (int i = 0;i < list.size();i++) model.removeRecord(list.get(i));

        } else if (e.getActionCommand().equals("copy")) {
            int index = TB_Table.getSelectedRow();
            if (index >= 0) {
                try {
                    Record rec = model.getRecord(index);
                    model.insertRecord(index, (Record) rec.clone());

                } catch (CloneNotSupportedException ex) {
                    ex.printStackTrace();
                }
            }

        } else if (e.getActionCommand().equals("up")) {
            int row = TB_Table.getSelectedRow();
            if (row > 0) {
                model.swapValue(row - 1, row);
                TB_Table.getSelectionModel().setSelectionInterval(row - 1, row - 1);
            }

        } else if (e.getActionCommand().equals("down")) {
            int row = TB_Table.getSelectedRow();
            if (row < TB_Table.getRowCount() - 1) {
                model.swapValue(row, row + 1);
                TB_Table.getSelectionModel().setSelectionInterval(row + 1, row + 1);
            }
        }
    }

    //**************************************************************************
    //*** MouseListener
    //**************************************************************************
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == TB_Table) {
            if (e.getClickCount() >= 2) {
                //--- Popup the modification frame
                Record rec = model.getRecord(TB_Table.getSelectedRow());
                ArrayList<Column> cols = new ArrayList<>();
                for (int i = 0;i < columns.size();i++) cols.add((Column) columns.get(i));
                JWriteDialog dlg = new JWriteDialog(null, true, rec, cols, model);
                dlg.pack();
                dlg.setLocationRelativeTo(this);
                dlg.setVisible(true);

                TB_Table.repaint();

                if (listener != null) listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ACTION_COMMAND_MODIFIED));
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getSource() == TB_Table) {
            //--- Popup menu
            if (e.isPopupTrigger()) {
                PU_Actions.show(TB_Table, e.getX(), e.getY());
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getSource() == TB_Table) {
            if (e.isPopupTrigger()) {
                //--- PopupMenu
                PU_Actions.show(TB_Table, e.getX(), e.getY());

            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        //---
    }

    @Override
    public void mouseExited(MouseEvent e) {
        //---
    }

    //**************************************************************************
    //*** ListSelectionListener
    //**************************************************************************
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() == false) {
            if (selected != null) {
                //--- Save the current state
                selected.setName(TF_ColumnName.getText().trim());
                selected.setDescription(TA_ColumnDescription.getText().trim());
            }

            CardLayout layout = (CardLayout) PN_ColumnConfig.getLayout();
            layout.show(PN_ColumnConfig, "column");

            //--- Display the selected value
            Column c = LI_Columns.getSelectedValue();
            if (c != null) {
                TF_ColumnName.setText(c.getName());
                TA_ColumnDescription.setText(c.getDescription());

                selected = c;

                if (listener != null) listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ACTION_COMMAND_MODIFIED));

            } else {
                layout.show(PN_ColumnConfig, "empty");

            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        PU_Actions = new javax.swing.JPopupMenu();
        MN_Copy = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        MN_Delete = new javax.swing.JMenuItem();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        PN_Overview = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        TF_Name = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        TA_Description = new javax.swing.JTextArea();
        CMB_Handlers = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        LB_Version = new javax.swing.JLabel();
        PN_Columns = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        LI_Columns = new javax.swing.JList<>();
        BT_Add = new javax.swing.JButton();
        BT_Remove = new javax.swing.JButton();
        PN_ColumnConfig = new javax.swing.JPanel();
        LB_Empty = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        LB_ColumnName = new javax.swing.JLabel();
        TF_ColumnName = new javax.swing.JTextField();
        LB_ColumnDescription = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        TA_ColumnDescription = new javax.swing.JTextArea();
        BT_ConfigureHandler = new javax.swing.JButton();
        BT_ColumnUp = new javax.swing.JButton();
        BT_ColumnDown = new javax.swing.JButton();
        BT_Import = new javax.swing.JButton();
        PN_Table = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        TB_Table = new JTable() {
            public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int colIndex) {
                Component c = super.prepareRenderer(renderer, rowIndex, colIndex);
                boolean selected = isCellSelected(rowIndex, colIndex);
                if (selected) {
                    c.setBackground(getSelectionBackground());
                    // c.setBackground(UIManager.getColor("Table.selectionBackground"));
                    // c.setForeground(UIManager.getColor("Table.selectionForeground"));
                } else {
                    if (rowIndex%2 == 0) {
                        c.setBackground(new Color(241,245,250));

                    } else {
                        // c.setBackground(getBackground());
                        c.setBackground(Color.WHITE);

                    }
                }
                return c;
            }
        };
        jToolBar1 = new javax.swing.JToolBar();
        BT_NewComment = new javax.swing.JButton();
        BT_NewEmpty = new javax.swing.JButton();
        BT_NewProperty = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        BT_Up = new javax.swing.JButton();
        BT_Down = new javax.swing.JButton();
        BT_Copy = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        BT_Delete = new javax.swing.JButton();

        PU_Actions.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N

        MN_Copy.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(MN_Copy, org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.MN_Copy.text")); // NOI18N
        MN_Copy.setActionCommand(org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.MN_Copy.actionCommand")); // NOI18N
        PU_Actions.add(MN_Copy);
        PU_Actions.add(jSeparator3);

        MN_Delete.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(MN_Delete, org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.MN_Delete.text")); // NOI18N
        MN_Delete.setActionCommand(org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.MN_Delete.actionCommand")); // NOI18N
        PU_Actions.add(MN_Delete);

        setLayout(new java.awt.BorderLayout());

        jTabbedPane1.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N

        jLabel1.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.jLabel1.text")); // NOI18N

        TF_Name.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        TF_Name.setText(org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.TF_Name.text")); // NOI18N

        jLabel2.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.jLabel2.text")); // NOI18N

        TA_Description.setColumns(20);
        TA_Description.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        TA_Description.setRows(5);
        jScrollPane3.setViewportView(TA_Description);

        CMB_Handlers.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N

        jLabel4.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.jLabel4.text")); // NOI18N

        LB_Version.setFont(new java.awt.Font("Monospaced", 0, 9)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(LB_Version, org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.LB_Version.text")); // NOI18N

        javax.swing.GroupLayout PN_OverviewLayout = new javax.swing.GroupLayout(PN_Overview);
        PN_Overview.setLayout(PN_OverviewLayout);
        PN_OverviewLayout.setHorizontalGroup(
            PN_OverviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PN_OverviewLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PN_OverviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PN_OverviewLayout.createSequentialGroup()
                        .addGroup(PN_OverviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(PN_OverviewLayout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(TF_Name, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(LB_Version))
                        .addContainerGap(493, Short.MAX_VALUE))
                    .addGroup(PN_OverviewLayout.createSequentialGroup()
                        .addGroup(PN_OverviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(PN_OverviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(PN_OverviewLayout.createSequentialGroup()
                                .addComponent(CMB_Handlers, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(PN_OverviewLayout.createSequentialGroup()
                                .addComponent(jScrollPane3)
                                .addContainerGap())))))
        );
        PN_OverviewLayout.setVerticalGroup(
            PN_OverviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PN_OverviewLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PN_OverviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(TF_Name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PN_OverviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PN_OverviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(CMB_Handlers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 302, Short.MAX_VALUE)
                .addComponent(LB_Version)
                .addContainerGap())
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.PN_Overview.TabConstraints.tabTitle"), PN_Overview); // NOI18N

        LI_Columns.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        LI_Columns.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(LI_Columns);

        BT_Add.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(BT_Add, org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.BT_Add.text")); // NOI18N
        BT_Add.setActionCommand(org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.BT_Add.actionCommand")); // NOI18N

        BT_Remove.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(BT_Remove, org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.BT_Remove.text")); // NOI18N
        BT_Remove.setActionCommand(org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.BT_Remove.actionCommand")); // NOI18N

        PN_ColumnConfig.setLayout(new java.awt.CardLayout());

        org.openide.awt.Mnemonics.setLocalizedText(LB_Empty, org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.LB_Empty.text")); // NOI18N
        PN_ColumnConfig.add(LB_Empty, "empty");

        LB_ColumnName.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(LB_ColumnName, org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.LB_ColumnName.text")); // NOI18N

        TF_ColumnName.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        TF_ColumnName.setText(org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.TF_ColumnName.text")); // NOI18N

        LB_ColumnDescription.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(LB_ColumnDescription, org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.LB_ColumnDescription.text")); // NOI18N

        jScrollPane4.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N

        TA_ColumnDescription.setColumns(20);
        TA_ColumnDescription.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        TA_ColumnDescription.setRows(5);
        jScrollPane4.setViewportView(TA_ColumnDescription);

        BT_ConfigureHandler.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(BT_ConfigureHandler, org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.BT_ConfigureHandler.text")); // NOI18N
        BT_ConfigureHandler.setActionCommand(org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.BT_ConfigureHandler.actionCommand")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(LB_ColumnName, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(TF_ColumnName))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(LB_ColumnDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 372, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(BT_ConfigureHandler)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LB_ColumnName)
                    .addComponent(TF_ColumnName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(LB_ColumnDescription)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(BT_ConfigureHandler)
                .addContainerGap(289, Short.MAX_VALUE))
        );

        PN_ColumnConfig.add(jPanel1, "column");

        BT_ColumnUp.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(BT_ColumnUp, org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.BT_ColumnUp.text")); // NOI18N
        BT_ColumnUp.setActionCommand(org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.BT_ColumnUp.actionCommand")); // NOI18N

        BT_ColumnDown.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(BT_ColumnDown, org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.BT_ColumnDown.text")); // NOI18N
        BT_ColumnDown.setActionCommand(org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.BT_ColumnDown.actionCommand")); // NOI18N

        BT_Import.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(BT_Import, org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.BT_Import.text")); // NOI18N
        BT_Import.setActionCommand(org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.BT_Import.actionCommand")); // NOI18N

        javax.swing.GroupLayout PN_ColumnsLayout = new javax.swing.GroupLayout(PN_Columns);
        PN_Columns.setLayout(PN_ColumnsLayout);
        PN_ColumnsLayout.setHorizontalGroup(
            PN_ColumnsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PN_ColumnsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PN_ColumnsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(BT_Import, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PN_ColumnsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(BT_Remove, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(BT_Add, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(BT_ColumnUp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(BT_ColumnDown, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(PN_ColumnConfig, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        PN_ColumnsLayout.setVerticalGroup(
            PN_ColumnsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PN_ColumnsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PN_ColumnsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(PN_ColumnConfig, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(PN_ColumnsLayout.createSequentialGroup()
                        .addGroup(PN_ColumnsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(PN_ColumnsLayout.createSequentialGroup()
                                .addComponent(BT_Add)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(BT_Remove)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(BT_ColumnUp)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(BT_ColumnDown))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(BT_Import)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.PN_Columns.TabConstraints.tabTitle"), PN_Columns); // NOI18N

        PN_Table.setLayout(new java.awt.BorderLayout());

        TB_Table.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        TB_Table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        TB_Table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        TB_Table.setRowHeight(22);
        TB_Table.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TB_TableKeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(TB_Table);

        PN_Table.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        BT_NewComment.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(BT_NewComment, org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.BT_NewComment.text")); // NOI18N
        BT_NewComment.setActionCommand(org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.BT_NewComment.actionCommand")); // NOI18N
        BT_NewComment.setFocusable(false);
        BT_NewComment.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_NewComment.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(BT_NewComment);

        BT_NewEmpty.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(BT_NewEmpty, org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.BT_NewEmpty.text")); // NOI18N
        BT_NewEmpty.setActionCommand(org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.BT_NewEmpty.actionCommand")); // NOI18N
        BT_NewEmpty.setFocusable(false);
        BT_NewEmpty.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_NewEmpty.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(BT_NewEmpty);

        BT_NewProperty.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(BT_NewProperty, org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.BT_NewProperty.text")); // NOI18N
        BT_NewProperty.setActionCommand(org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.BT_NewProperty.actionCommand")); // NOI18N
        BT_NewProperty.setFocusable(false);
        BT_NewProperty.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_NewProperty.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(BT_NewProperty);
        jToolBar1.add(jSeparator1);

        BT_Up.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(BT_Up, org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.BT_Up.text")); // NOI18N
        BT_Up.setActionCommand(org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.BT_Up.actionCommand")); // NOI18N
        BT_Up.setFocusable(false);
        BT_Up.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_Up.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(BT_Up);

        BT_Down.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(BT_Down, org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.BT_Down.text")); // NOI18N
        BT_Down.setActionCommand(org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.BT_Down.actionCommand")); // NOI18N
        BT_Down.setFocusable(false);
        BT_Down.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_Down.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(BT_Down);

        BT_Copy.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(BT_Copy, org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.BT_Copy.text")); // NOI18N
        BT_Copy.setActionCommand(org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.BT_Copy.actionCommand")); // NOI18N
        BT_Copy.setFocusable(false);
        BT_Copy.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_Copy.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(BT_Copy);
        jToolBar1.add(jSeparator2);

        BT_Delete.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(BT_Delete, org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.BT_Delete.text")); // NOI18N
        BT_Delete.setActionCommand(org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.BT_Delete.actionCommand")); // NOI18N
        BT_Delete.setFocusable(false);
        BT_Delete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_Delete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(BT_Delete);

        PN_Table.add(jToolBar1, java.awt.BorderLayout.PAGE_START);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(JMultiProperties.class, "JMultiProperties.PN_Table.TabConstraints.tabTitle"), PN_Table); // NOI18N

        add(jTabbedPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void TB_TableKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TB_TableKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_UP) {
            if (evt.isAltDown()) {
                int row = TB_Table.getSelectedRow();
                if (row > 0) {
                    model.swapValue(row - 1, row);
                    TB_Table.getSelectionModel().setSelectionInterval(row - 1, row - 1);
                }
            }

        } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            if (evt.isAltDown()) {
                int row = TB_Table.getSelectedRow();
                if (row < TB_Table.getRowCount() - 1) {
                    model.swapValue(row, row + 1);
                    TB_Table.getSelectionModel().setSelectionInterval(row + 1, row + 1);
                }

            }

        } else if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
            int row = TB_Table.getSelectedRow();
            if (row > 0) {
                Record rec = model.getRecord(row);
                int rep = JOptionPane.showConfirmDialog(this, "Do you really want to delete the property " + rec.getKey(), "Delete", JOptionPane.YES_NO_OPTION);
                if (rep == JOptionPane.OK_OPTION) {
                    model.removeRecord(rec);
                }

            }

        }
    }//GEN-LAST:event_TB_TableKeyPressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BT_Add;
    private javax.swing.JButton BT_ColumnDown;
    private javax.swing.JButton BT_ColumnUp;
    private javax.swing.JButton BT_ConfigureHandler;
    private javax.swing.JButton BT_Copy;
    private javax.swing.JButton BT_Delete;
    private javax.swing.JButton BT_Down;
    private javax.swing.JButton BT_Import;
    private javax.swing.JButton BT_NewComment;
    private javax.swing.JButton BT_NewEmpty;
    private javax.swing.JButton BT_NewProperty;
    private javax.swing.JButton BT_Remove;
    private javax.swing.JButton BT_Up;
    private javax.swing.JComboBox<PropertiesHandler> CMB_Handlers;
    private javax.swing.JLabel LB_ColumnDescription;
    private javax.swing.JLabel LB_ColumnName;
    private javax.swing.JLabel LB_Empty;
    private javax.swing.JLabel LB_Version;
    private javax.swing.JList<Column> LI_Columns;
    private javax.swing.JMenuItem MN_Copy;
    private javax.swing.JMenuItem MN_Delete;
    private javax.swing.JPanel PN_ColumnConfig;
    private javax.swing.JPanel PN_Columns;
    private javax.swing.JPanel PN_Overview;
    private javax.swing.JPanel PN_Table;
    private javax.swing.JPopupMenu PU_Actions;
    private javax.swing.JTextArea TA_ColumnDescription;
    private javax.swing.JTextArea TA_Description;
    private javax.swing.JTable TB_Table;
    private javax.swing.JTextField TF_ColumnName;
    private javax.swing.JTextField TF_Name;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables

    private void parseMultiproperties(File f) {
        try {
            InputSource source = new InputSource(new FileInputStream(f));
            source.setEncoding("UTF-8");
            Document xml = builder.parse(source);
            Element root = (Element) xml.getChildNodes().item(0);

            model.removeAllElements();
            columns.removeAllElements();

            //--- The handler used
            PropertiesHandler handler = null;

            NodeList nl = root.getChildNodes();
            for (int i = 0;i < nl.getLength();i++) {
                Node n = nl.item(i);
                if (n.getNodeName().equals("Version")) {
                    Element e = (Element) n;
                    LB_Version.setText(e.getFirstChild().getNodeValue());

                } else if (n.getNodeName().equals("Name")) {
                    Element e = (Element) n;
                    if (e.getFirstChild() != null) TF_Name.setText(e.getFirstChild().getNodeValue());

                } else if (n.getNodeName().equals("Description")) {
                    Element e = (Element) n;
                    if (e.getFirstChild() != null) TA_Description.setText(e.getFirstChild().getNodeValue());

                } else if (n.getNodeName().equals("Handler")) {
                    Element e = (Element) n;
                    if (e.getFirstChild() != null) {
                        String hn = e.getFirstChild().getNodeValue();
                        for (int j = 0;j < CMB_Handlers.getItemCount();j++) {
                            PropertiesHandler ph = CMB_Handlers.getItemAt(j);
                            if (ph.getName().equals(hn)) CMB_Handlers.setSelectedIndex(j);
                        }
                    }

                } else if (n.getNodeName().equals("Columns")) {
                    NodeList cols = n.getChildNodes();
                    for (int j = 0;j < cols.getLength();j++) {
                        Node n2 = cols.item(j);
                        if (n2.getNodeName().equals("Key")) {
                            try {
                                Element col = (Element) n2;
                                Column c = model.getColumn(0);
                                int width = Integer.parseInt(col.getElementsByTagName("Width").item(0).getFirstChild().getNodeValue());
                                c.setWidth(width);
                                
                            } catch (Exception ex) {
                                //---
                            }
                            
                        } else if (n2.getNodeName().equals("Column")) {
                            Element col = (Element) n2;
                            Column c = new Column(col);
                            columns.addElement(c);
                            model.addColumn(c);

                        }

                    }

                } else if (n.getNodeName().equals("Records")) {
                    NodeList recs = n.getChildNodes();
                    for (int j = 0;j < recs.getLength();j++) {
                        Node n2 = recs.item(j);
                        if (n2.getNodeName().equals("Comment")) {
                            CommentRecord cr = new CommentRecord((Element) n2);
                            model.addRecord(cr);

                        } else if (n2.getNodeName().equals("Property")) {
                            PropertyRecord pr = new PropertyRecord((Element) n2);
                            model.addRecord(pr);

                        } else if (n2.getNodeName().equals("Empty")) {
                            EmptyRecord er = new EmptyRecord((Element) n2);
                            model.addRecord(er);

                        }

                    }
                }
            }

            //--- Set column size
            for (int i = 0;i < columns.size();i++) {
                Column c = (Column) columns.get(i);
                TB_Table.getColumnModel().getColumn(i).setPreferredWidth(c.width);
                TB_Table.getColumnModel().getColumn(i).setWidth(c.width);
            }

        } catch (SAXException ex) {
            ex.printStackTrace();

        } catch (IOException ex) {
            ex.printStackTrace();

        }
    }

}
