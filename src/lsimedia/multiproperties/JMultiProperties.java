/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lsimedia.multiproperties;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import lsimedia.multiproperties.handlers.JavaPropertiesHandler;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.AbstractAction;
import static javax.swing.Action.ACCELERATOR_KEY;
import static javax.swing.Action.ACTION_COMMAND_KEY;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import lsimedia.multiproperties.handlers.AndroidValuesHandler;
import lsimedia.multiproperties.handlers.EmptyPropertiesHandler;
import lsimedia.multiproperties.util.RecordDnDVector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class JMultiProperties extends JPanel implements ActionListener, MouseListener, ListSelectionListener, ClipboardOwner, KeyListener, ItemListener {

    public static final Color COLOR_KEY = new Color(230, 230, 230);
    public static final String ACTION_COMMAND_MODIFIED = "dataModified";

    /**
     * Shared clipbard for multiproperties key cop/paste
     */
    public static final Clipboard CLIPBOARD = new Clipboard("JMultiProperties");

    SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    File file = null;
    boolean lockdown = false;

    DefaultListModel<Column> columns = new DefaultListModel<>();
    MultiPropertiesTableModel model = new MultiPropertiesTableModel();

    DocumentBuilder builder = null;

    /**
     * The current selected column
     */
    Column selected = null;
    int selectedIndexes[] = new int[0];

    /**
     * The current selected key text field
     */
    JTextField keySelected = null;

    /**
     * Unique listener for data changes, the event fired is ACTION_PERFORMED
     * with "dataModified" as action name
     *
     */
    ActionListener listener = null;

    /**
     * Logging instance
     */
    Logit logit = null;

    /**
     * Last imported/opened file
     */
    File last = null;

    /**
     * If the panel was modified and not yet saved, the flag would be true
     */
    boolean modified = false;

    /**
     * Default write dialog dimension
     */
    Dimension writeDialogSize = new Dimension(800, 600);

    public JMultiProperties(Logit logit) {
        this.logit = logit;

        initComponents();

        CMB_Handlers.addItem(new EmptyPropertiesHandler());
        CMB_Handlers.addItem(new JavaPropertiesHandler());
        CMB_Handlers.addItem(new AndroidValuesHandler());
        CMB_Handlers.addItemListener(this);
        
        TF_Name.addKeyListener(this);
        TA_Description.addKeyListener(this);
        
        TB_Table.setModel(model);
        TB_Table.getTableHeader().setFont(new Font("Arial", 0, 11));
        TB_Table.addMouseListener(this);
        TB_Table.getSelectionModel().addListSelectionListener(this);
        TB_Table.setDefaultRenderer(Record.class, new JRecordCellRenderer());
        TB_Table.getTableHeader().setReorderingAllowed(false);
        SP_Table.setRowHeaderView(PN_Rows);
        // SP_Table.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, BT_Collapse);
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
        BT_Delete.addActionListener(this);
        BT_Merge.addActionListener(this);
        BT_Sort.addActionListener(this);

        BT_Save.addActionListener(this);
        BT_SaveProcess.addActionListener(this);

        BT_ConfigureHandler.addActionListener(this);

        MN_Clone.addActionListener(this);
        MN_Delete.addActionListener(this);
        MN_NewComment.addActionListener(this);
        MN_NewProperty.addActionListener(this);
        MN_NewEmpty.addActionListener(this);
        MN_MoveUp.addActionListener(this);
        MN_MoveDown.addActionListener(this);
        MN_SetFinalAll.addActionListener(this);
        MN_SetFinalTranslatedOnly.addActionListener(this);
        MN_UnFinal.addActionListener(this);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        CopyAction ca = new CopyAction(this);
        MN_Copy.setAction(ca);
        TB_Table.getInputMap().put(MN_Copy.getAccelerator(), "copy");
        TB_Table.getActionMap().put("copy", ca);

        CutAction ba = new CutAction(this);
        MN_Cut.setAction(ba);
        TB_Table.getInputMap().put(MN_Cut.getAccelerator(), "cut");
        TB_Table.getActionMap().put("cut", ba);

        PasteAction da = new PasteAction(this);
        MN_Paste.setAction(da);
        TB_Table.getInputMap().put(MN_Paste.getAccelerator(), "paste");
        TB_Table.getActionMap().put("paste", da);
    }

    //**************************************************************************
    //*** API
    //**************************************************************************
    /**
     * Fill the passed element with the configuration for this panel
     *
     * @param config
     */
    public void getConfig(Element config) {
        if (config == null) return;

        config.setAttribute("writeDialogWidth", "" + writeDialogSize.width);
        config.setAttribute("writeDialogHeight", "" + writeDialogSize.height);

    }

    /**
     * Set the wanted configuration values for this panel (like the write dialog
     * size)
     * <p>
     *
     * @param config
     */
    public void setConfig(Element config) {
        if (config == null) return;

        try {
            int w = Integer.parseInt(config.getAttribute("writeDialogWidth"));
            int h = Integer.parseInt(config.getAttribute("writeDialogHeight"));
            writeDialogSize.setSize(w, h);

        } catch (NumberFormatException ex) {
            //---
        }
    }

    /**
     * Set the file to be handled
     *
     * @param file
     */
    public void setFile(File file) {
        this.file = file;
        this.last = file;
        boolean found = parseMultiproperties(file);
        if (!found) JOptionPane.showMessageDialog(this, "The handler was not found !\n\nDo not save this file if you want to keep the unknown handler...", "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Pass all non translated field to final state
     *
     * @param fi
     */
    public void setFinal(boolean fi) {
        for (int i = 0;i < model.getRowCount();i++) {
            Record rec = model.getRecord(i);
            if (rec instanceof PropertyRecord) {
                PropertyRecord pr = (PropertyRecord) rec;
                pr.setFinal(fi);
            }
        }
        fireModifiedEvent();
        TB_Table.repaint();
    }

    /**
     * Force to hide the process button (only save is available in any case)
     *
     * @param hide
     */
    public void hideProcessAction(boolean hide) {
        BT_SaveProcess.setVisible(!hide);

    }

    public void lockdown() {
        lockdown = true;

        BT_SaveProcess.setVisible(false);

        TAB_Main.setEnabledAt(0, false);
        TAB_Main.setEnabledAt(1, false);
        TAB_Main.setSelectedIndex(1);

        BT_NewComment.setVisible(false);
        BT_NewEmpty.setVisible(false);
        BT_NewProperty.setVisible(false);

        BT_Delete.setVisible(false);
        BT_Sort.setVisible(false);
        BT_Merge.setVisible(false);

        MN_Columns.setEnabled(false);
    }

    /**
     * Hide the save button
     *
     * @param hide
     */
    public void hideSaveAction(boolean hide) {
        BT_Save.setVisible(!hide);

    }

    public void selectTab(int index) {
        TAB_Main.setSelectedIndex(index);;
    }

    public File getFile() {
        return file;
    }

    /**
     * Returns the data model
     *
     * @return
     */
    public MultiPropertiesTableModel getModel() {
        return model;
    }

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
     * xml file<p>
     *
     * If the process argument is true, than for each column, save the
     * properties via the handler<p>
     *
     */
    public void save(boolean process) {
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
                c.setWidth(tc.getWidth() == 0 ? 150 : tc.getWidth());
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

            logit.log("M", "Multiproperties file stored at " + file.getPath(), null);
            // System.out.println("SAVED TO :" + file.getPath());

            //--- Pass the model to the properties handler to save the specific file
            if (process) {
                boolean rep = handler.save(model, TF_Name.getText(), TA_Description.getText(), file, logit);
                if (rep == false) JOptionPane.showMessageDialog(this, "An error occured during the saving of the property\n\n" + handler.getLastError());
            }

            BT_Save.setEnabled(false);
            BT_SaveProcess.setEnabled(false);
            modified = false;

        } catch (Exception ex) {
            // ex.printStackTrace();
            logit.log("E", "Multiproperties save error " + ex.getMessage(), null);
        }

    }

    public boolean isModified() {
        return modified;
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

                JMenu menu = c.getMenu();
                MN_Columns.add(menu);
                for (int i = 0;i < menu.getItemCount();i++) menu.getItem(i).addActionListener(this);

                fireModifiedEvent();

                // resizeColumns();
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
                JMenu menu = c.getMenu();
                MN_Columns.remove(menu);
                for (int i = 0;i < menu.getItemCount();i++) menu.getItem(i).removeActionListener(this);

                CardLayout layout = (CardLayout) PN_ColumnConfig.getLayout();
                layout.show(PN_ColumnConfig, "empty");

                fireModifiedEvent();
            }

        } else if (e.getActionCommand().equals("handler")) {
            //--- Configure the handler for the selected column
            PropertiesHandler h = (PropertiesHandler) CMB_Handlers.getSelectedItem();

            JHandlerDialog dlg = new JHandlerDialog(null, true, h, selected, file);
            dlg.pack();
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);

            fireModifiedEvent();

        } else if (e.getActionCommand().equals("import")) {
            //--- Configure the handler for the selected column
            PropertiesHandler h = (PropertiesHandler) CMB_Handlers.getSelectedItem();
            JFileChooser jf = new JFileChooser(last);
            jf.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jf.setMultiSelectionEnabled(true);
            int rep = jf.showOpenDialog(this);
            if (rep == JFileChooser.APPROVE_OPTION) {
                File f[] = jf.getSelectedFiles();
                for (int i = 0;i < f.length;i++) {
                    Column c = h.load(model, f[i]);
                    if (c != null) {
                        columns.addElement(c);
                        JMenu menu = c.getMenu();;
                        MN_Columns.add(menu);
                        for (int j = 0;j < menu.getItemCount();j++) menu.getItem(j).addActionListener(this);
                    }

                    last = f[i];
                }

                fireModifiedEvent();

            }

        } else if (e.getActionCommand().equals("columnUp")) {
            int index = LI_Columns.getSelectedIndex();
            if (index > 0) {
                Column from = columns.get(index);
                Column to = columns.get(index - 1);
                columns.setElementAt(from, index - 1);
                columns.setElementAt(to, index);

                //--- Add +1 to avoid the key column
                model.swapColumn(index + 1 - 1, index + 1);

                LI_Columns.setSelectedIndex(index - 1);

                fireModifiedEvent();
            }

        } else if (e.getActionCommand().equals("columnDown")) {
            int index = LI_Columns.getSelectedIndex();
            if (index < columns.getSize() - 1) {
                Column from = columns.get(index);
                Column to = columns.get(index + 1);
                columns.setElementAt(from, index + 1);
                columns.setElementAt(to, index);

                //--- Add +1 to avoid the key column
                model.swapColumn(index + 1, index + 1 + 1);

                LI_Columns.setSelectedIndex(index + 1);

                fireModifiedEvent();
            }

        } else if (e.getActionCommand().equals("columnSetFinal")) {
            //--- Finalize a column
            JMenuItem item = (JMenuItem) e.getSource();
            Column c = (Column) item.getClientProperty("column");
            int index = model.getColumnIndex(c.getName());

            for (int i = 0;i < model.getRowCount();i++) {
                Record rec = model.getRecord(i);
                if (rec instanceof PropertyRecord) {
                    PropertyRecord pr = (PropertyRecord) rec;
                    pr.getValueAt(index - 1).setFinal(true);

                }
            }
            fireModifiedEvent();
            TB_Table.repaint();

        } else if (e.getActionCommand().equals("columnSetUnFinal")) {
            //--- Finalize a column
            JMenuItem item = (JMenuItem) e.getSource();
            Column c = (Column) item.getClientProperty("column");
            int index = model.getColumnIndex(c.getName());

            for (int i = 0;i < model.getRowCount();i++) {
                Record rec = model.getRecord(i);
                if (rec instanceof PropertyRecord) {
                    PropertyRecord pr = (PropertyRecord) rec;
                    pr.getValueAt(index - 1).setFinal(false);

                }
            }
            fireModifiedEvent();
            TB_Table.repaint();

        } else if (e.getActionCommand().equals("columnSetToDefault")) {
            //--- Finalize a column
            JMenuItem item = (JMenuItem) e.getSource();
            Column c = (Column) item.getClientProperty("column");
            int index = model.getColumnIndex(c.getName());

            for (int i = 0;i < model.getRowCount();i++) {
                Record rec = model.getRecord(i);
                if (rec instanceof PropertyRecord) {
                    PropertyRecord pr = (PropertyRecord) rec;
                    pr.getValueAt(index - 1).setDisable(true);

                }
            }
            fireModifiedEvent();
            TB_Table.repaint();

        } else if (e.getActionCommand().equals("columnMoveToDefault")) {
            //--- Move the columns value to default
            JMenuItem item = (JMenuItem) e.getSource();
            Column c = (Column) item.getClientProperty("column");
            int index = model.getColumnIndex(c.getName());

            for (int i = 0;i < model.getRowCount();i++) {
                Record rec = model.getRecord(i);
                if (rec instanceof PropertyRecord) {
                    PropertyRecord pr = (PropertyRecord) rec;
                    PropertyRecord.Value v = pr.getValueAt(index - 1);
                    if (!v.isDisabled()) {
                        pr.setDefaultValue(v.getValue());
                        v.setDisable(true);
                    }

                }
            }
            fireModifiedEvent();
            TB_Table.repaint();

        } else if (e.getActionCommand().equals("newComment")) {
            String comment = JOptionPane.showInputDialog(this, "Comment");
            if (comment != null) {
                int index = TB_Table.getSelectedRow();
                CommentRecord cr = new CommentRecord(comment);
                for (int i = 0;i < columns.size();i++) cr.addColumn();
                if ((index == -1) || (index == model.getRowCount() - 1)) {
                    model.addRecord(cr);
                    index = model.getRowCount() - 1;

                } else {
                    model.insertRecord(index, cr);
                }
                //--- Order is important, first fire event then select it
                fireModifiedEvent();
                TB_Table.getSelectionModel().setSelectionInterval(index, index);

            }

        } else if (e.getActionCommand().equals("newProperty")) {
            String name = JOptionPane.showInputDialog(this, "Key");
            if (name != null) {
                int index = TB_Table.getSelectedRow();
                PropertyRecord rec = new PropertyRecord(name);
                for (int i = 0;i < columns.size();i++) rec.addColumn();
                if ((index == -1) || (index == model.getRowCount() - 1)) {
                    model.addRecord(rec);
                    index = model.getRowCount() - 1;

                } else {
                    model.insertRecord(index, rec);
                }
                //--- Order is important, first fire event then select it
                fireModifiedEvent();
                TB_Table.getSelectionModel().setSelectionInterval(index, index);

                //--- Open it
                int sc = TB_Table.getSelectedColumn();
                JWriteDialog dlg = new JWriteDialog(null, true, rec, model, sc, lockdown);
                dlg.pack();
                dlg.setSize(writeDialogSize);
                dlg.setLocationRelativeTo(this);
                dlg.setVisible(true);

                writeDialogSize = dlg.getSize();

                TB_Table.repaint();

            }

        } else if (e.getActionCommand().equals("newEmpty")) {
            int index = TB_Table.getSelectedRow();
            EmptyRecord rec = new EmptyRecord();
            for (int i = 0;i < columns.size();i++) rec.addColumn();
            if ((index == -1) || (index == model.getRowCount() - 1)) {
                model.addRecord(rec);
                index = model.getRowCount() - 1;

            } else {
                model.insertRecord(index, rec);
            }
            fireModifiedEvent();
            TB_Table.getSelectionModel().setSelectionInterval(index, index);

        } else if (e.getActionCommand().equals("delete")) {
            int indices[] = TB_Table.getSelectedRows();
            if (indices.length > 0) {
                int rep = JOptionPane.showConfirmDialog(this, "Do you really want to delete the entry ?", "Delete", JOptionPane.YES_NO_OPTION);
                if (rep == JOptionPane.YES_OPTION) {
                    ArrayList<Record> list = new ArrayList<>();
                    for (int i = 0;i < indices.length;i++) list.add(model.getRecord(indices[i]));
                    for (int i = 0;i < list.size();i++) model.removeRecord(list.get(i));

                    fireModifiedEvent();
                }
            }

        } else if (e.getActionCommand().equals("clone")) {
            int index = TB_Table.getSelectedRow();
            if (index >= 0) {
                Record rec = model.getRecord(index);
                Record nrec = (Record) rec.copy();
                model.insertRecord(index, nrec);

                fireModifiedEvent();

            }

        } else if (e.getActionCommand().equals("up")) {
            int row = TB_Table.getSelectedRow();
            if (row > 0) {
                model.swapValue(row - 1, row);
                TB_Table.getSelectionModel().setSelectionInterval(row - 1, row - 1);
                TB_Table.scrollRectToVisible(new Rectangle(TB_Table.getCellRect(row - 1, 0, true)));
                fireModifiedEvent();
            }

        } else if (e.getActionCommand().equals("down")) {
            int row = TB_Table.getSelectedRow();
            if (row < TB_Table.getRowCount() - 1) {
                model.swapValue(row, row + 1);
                TB_Table.getSelectionModel().setSelectionInterval(row + 1, row + 1);
                TB_Table.scrollRectToVisible(new Rectangle(TB_Table.getCellRect(row + 1, 0, true)));
                fireModifiedEvent();
            }

        } else if (e.getActionCommand().equals("merge")) {
            //--- Merge other file to the current one
            JFileChooser jf = new JFileChooser(file);
            jf.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jf.setMultiSelectionEnabled(true);
            jf.setFileFilter(new FileNameExtensionFilter("Multiproperties", "multiproperties"));
            int rep = jf.showOpenDialog(this);
            if (rep == JFileChooser.APPROVE_OPTION) {
                File source = jf.getSelectedFile();
                JMultiProperties jm = new JMultiProperties(logit);
                jm.lockdown();
                jm.setFile(source);
                MultiPropertiesTableModel smodel = jm.getModel();
                Container comp = getTopLevelAncestor();
                JMergeDialog jdialog = null;
                if (comp instanceof Frame) {
                    jdialog = new JMergeDialog((Frame) comp, true, model, smodel);

                } else {
                    jdialog = new JMergeDialog((Dialog) comp, true, model, smodel);
                }
                jdialog.setLocationRelativeTo(this);
                jdialog.setVisible(true);

                //--- Check if new column was added (at the end)
                for (int i = columns.getSize() + 1;i < model.getColumnCount();i++) {
                    Column c = model.getColumn(i);
                    columns.addElement(c);
                }

                fireModifiedEvent();

                // resizeColumns();
            }

        } else if (e.getActionCommand().equals("setFinalAll")) {
            //--- Set the final flag to all columns
            int indexes[] = TB_Table.getSelectedRows();
            for (int i = 0;i < indexes.length;i++) {
                Record rec = model.getRecord(indexes[i]);
                if (rec instanceof PropertyRecord) {
                    PropertyRecord pr = (PropertyRecord) rec;
                    for (int j = 0;j < pr.getValueCount();j++) {
                        PropertyRecord.Value v = pr.getValueAt(j);
                        v.setFinal(true);
                    }
                }

            }
            fireModifiedEvent();

        } else if (e.getActionCommand().equals("setFinalTranslatedOnly")) {
            //--- Set the final flag to translated column only
            int indexes[] = TB_Table.getSelectedRows();
            for (int i = 0;i < indexes.length;i++) {
                Record rec = model.getRecord(indexes[i]);
                if (rec instanceof PropertyRecord) {
                    PropertyRecord pr = (PropertyRecord) rec;
                    for (int j = 0;j < pr.getValueCount();j++) {
                        PropertyRecord.Value v = pr.getValueAt(j);
                        if (!v.disabled) v.setFinal(true);
                    }
                }

            }
            fireModifiedEvent();

        } else if (e.getActionCommand().equals("unfinal")) {
            //--- Remove final flag
            int indexes[] = TB_Table.getSelectedRows();
            for (int i = 0;i < indexes.length;i++) {
                Record rec = model.getRecord(indexes[i]);
                if (rec instanceof PropertyRecord) {
                    PropertyRecord pr = (PropertyRecord) rec;
                    for (int j = 0;j < pr.getValueCount();j++) {
                        PropertyRecord.Value v = pr.getValueAt(j);
                        v.setFinal(false);
                    }
                }

            }
            fireModifiedEvent();

        } else if (e.getActionCommand().equals("save")) {
            save(false);
            BT_Save.setEnabled(false);
            BT_SaveProcess.setEnabled(false);

            modified = false;

        } else if (e.getActionCommand().equals("saveProcess")) {
            save(true);
            BT_Save.setEnabled(false);
            BT_SaveProcess.setEnabled(false);

            modified = false;

        } else if (e.getActionCommand().equals("sort")) {
            PN_Keys.removeAll();
            model.sort();
            fillRowHeader();
            PN_Keys.revalidate();
            fireModifiedEvent();
        }
    }

    //**************************************************************************
    //*** MouseListener
    //**************************************************************************
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == TB_Table) {
            if (e.getClickCount() >= 2) {
                int sc = TB_Table.getSelectedColumn();

                //--- Popup the modification frame
                Record rec = model.getRecord(TB_Table.getSelectedRow());
                ArrayList<Column> cols = new ArrayList<>();
                for (int i = 0;i < columns.size();i++) cols.add(columns.get(i));
                JWriteDialog dlg = new JWriteDialog(null, true, rec, model, sc, lockdown);
                dlg.pack();
                dlg.setSize(writeDialogSize);
                dlg.setLocationRelativeTo(this);
                dlg.setVisible(true);

                writeDialogSize = dlg.getSize();

                TB_Table.repaint();

                fireModifiedEvent();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getSource() == TB_Table) {
            //--- Popup menu
            if (e.isPopupTrigger() && !lockdown) {
                MN_Paste.setEnabled(CLIPBOARD.getContents(null) != null);
                PU_Actions.show(TB_Table, e.getX(), e.getY());
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getSource() == TB_Table) {
            if (e.isPopupTrigger() && !lockdown) {
                MN_Paste.setEnabled(CLIPBOARD.getContents(null) != null);
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
        if (e.getSource() == TB_Table.getSelectionModel()) {
            //if (e.getValueIsAdjusting() == false) return;
            //--- Clear old selection
            for (int i = 0;i < selectedIndexes.length;i++) {
                ((JTextField) PN_Keys.getComponent(selectedIndexes[i])).setForeground(TB_Table.getForeground());
                ((JTextField) PN_Keys.getComponent(selectedIndexes[i])).setBackground(COLOR_KEY);
            }
            selectedIndexes = TB_Table.getSelectedRows();
            for (int i = 0;i < selectedIndexes.length;i++) {
                ((JTextField) PN_Keys.getComponent(selectedIndexes[i])).setBackground(TB_Table.getSelectionBackground());
                ((JTextField) PN_Keys.getComponent(selectedIndexes[i])).setForeground(TB_Table.getSelectionForeground());
            }

        } else if (e.getSource() == LI_Columns) {
            if (e.getValueIsAdjusting() == false) {
                if (selected != null) {
                    //--- Save the current state
                    for (int i = 0;i < model.getColumnCount();i++) model.getColumn(i).setWidth(TB_Table.getColumnModel().getColumn(i).getWidth());

                    selected.setName(TF_ColumnName.getText().trim());
                    selected.setDescription(TA_ColumnDescription.getText().trim());
                    model.fireTableChanged();
                    resizeColumns();

                }

                CardLayout layout = (CardLayout) PN_ColumnConfig.getLayout();
                layout.show(PN_ColumnConfig, "column");

                //--- Display the selected value
                Column c = LI_Columns.getSelectedValue();
                if (c != null) {
                    TF_ColumnName.setText(c.getName());
                    TA_ColumnDescription.setText(c.getDescription());
                    selected = c;

                    fireModifiedEvent();

                } else {
                    layout.show(PN_ColumnConfig, "empty");

                }
            }
        }
    }

    //**************************************************************************
    //*** ClipboardListener
    //**************************************************************************
    @Override
    public void lostOwnership(Clipboard clip, Transferable t) {
        try {
            if (t != null) {
                RecordDnDVector dnd = (RecordDnDVector) t.getTransferData(RecordDnDVector.recordDnDVectorFlavor);
                if (dnd.getInfo() == TransferHandler.MOVE) {
                    //--- Record where moved, so remove it from current file
                    while (dnd.size() > 0) {
                        Record rec = dnd.remove(0);
                        model.removeRecord(rec);

                    }
                    fireModifiedEvent();

                }

            }

        } catch (UnsupportedFlavorException ex) {
            ex.printStackTrace();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    //**************************************************************************
    //*** KeyListener
    //**************************************************************************
    @Override
    public void keyTyped(KeyEvent e) {
        fireModifiedEvent();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        //---
    }

    @Override
    public void keyReleased(KeyEvent e) {
        //---
    }
    
    //**************************************************************************
    //*** ItemListener
    //**************************************************************************
    @Override
    public void itemStateChanged(ItemEvent e) {
        fireModifiedEvent();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        PU_Actions = new javax.swing.JPopupMenu();
        MN_NewProperty = new javax.swing.JMenuItem();
        MN_NewComment = new javax.swing.JMenuItem();
        MN_NewEmpty = new javax.swing.JMenuItem();
        MN_Clone = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        MN_SetFinalAll = new javax.swing.JMenuItem();
        MN_SetFinalTranslatedOnly = new javax.swing.JMenuItem();
        MN_UnFinal = new javax.swing.JMenuItem();
        MN_Columns = new javax.swing.JMenu();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        MN_Delete = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        MN_Copy = new javax.swing.JMenuItem();
        MN_Cut = new javax.swing.JMenuItem();
        MN_Paste = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        MN_MoveUp = new javax.swing.JMenuItem();
        MN_MoveDown = new javax.swing.JMenuItem();
        PN_Rows = new javax.swing.JPanel();
        PN_Keys = new javax.swing.JPanel();
        TAB_Main = new javax.swing.JTabbedPane();
        PN_File = new javax.swing.JPanel();
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
        SP_Table = new javax.swing.JScrollPane();
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
        PN_Actions = new javax.swing.JPanel();
        BT_Save = new javax.swing.JButton();
        BT_SaveProcess = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        BT_NewProperty = new javax.swing.JButton();
        BT_NewComment = new javax.swing.JButton();
        BT_NewEmpty = new javax.swing.JButton();
        BT_Merge = new javax.swing.JButton();
        BT_Sort = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        BT_Delete = new javax.swing.JButton();

        PU_Actions.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N

        MN_NewProperty.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        MN_NewProperty.setText("New property");
        MN_NewProperty.setActionCommand("newProperty");
        PU_Actions.add(MN_NewProperty);

        MN_NewComment.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        MN_NewComment.setText("New comment");
        MN_NewComment.setActionCommand("newComment");
        PU_Actions.add(MN_NewComment);

        MN_NewEmpty.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        MN_NewEmpty.setText("New empty");
        MN_NewEmpty.setActionCommand("newEmpty");
        PU_Actions.add(MN_NewEmpty);

        MN_Clone.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        MN_Clone.setText("Clone");
        MN_Clone.setActionCommand("clone");
        PU_Actions.add(MN_Clone);
        PU_Actions.add(jSeparator4);

        MN_SetFinalAll.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        MN_SetFinalAll.setText("Set Final to selected rows");
        MN_SetFinalAll.setActionCommand("setFinalAll");
        PU_Actions.add(MN_SetFinalAll);

        MN_SetFinalTranslatedOnly.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        MN_SetFinalTranslatedOnly.setText("Set Final to selected translated fields only");
        MN_SetFinalTranslatedOnly.setActionCommand("setFinalTranslatedOnly");
        PU_Actions.add(MN_SetFinalTranslatedOnly);

        MN_UnFinal.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        MN_UnFinal.setText("Remove Final to selected rows");
        MN_UnFinal.setActionCommand("unfinal");
        PU_Actions.add(MN_UnFinal);

        MN_Columns.setText("Columns");
        MN_Columns.setActionCommand("columns");
        MN_Columns.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        PU_Actions.add(MN_Columns);
        PU_Actions.add(jSeparator3);

        MN_Delete.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
        MN_Delete.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        MN_Delete.setText("Delete");
        MN_Delete.setActionCommand("delete");
        PU_Actions.add(MN_Delete);
        PU_Actions.add(jSeparator5);

        MN_Copy.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        MN_Copy.setText("Copy");
        MN_Copy.setActionCommand("copy");
        PU_Actions.add(MN_Copy);

        MN_Cut.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        MN_Cut.setText("Cut");
        MN_Cut.setActionCommand("cut");
        PU_Actions.add(MN_Cut);

        MN_Paste.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        MN_Paste.setText("Paste");
        MN_Paste.setActionCommand("paste");
        PU_Actions.add(MN_Paste);
        PU_Actions.add(jSeparator6);

        MN_MoveUp.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, java.awt.event.InputEvent.ALT_MASK));
        MN_MoveUp.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        MN_MoveUp.setText("Move up");
        MN_MoveUp.setActionCommand("up");
        PU_Actions.add(MN_MoveUp);

        MN_MoveDown.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, java.awt.event.InputEvent.ALT_MASK));
        MN_MoveDown.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        MN_MoveDown.setText("Move down");
        MN_MoveDown.setActionCommand("down");
        PU_Actions.add(MN_MoveDown);

        PN_Rows.setLayout(new java.awt.BorderLayout());

        PN_Keys.setLayout(new javax.swing.BoxLayout(PN_Keys, javax.swing.BoxLayout.Y_AXIS));
        PN_Rows.add(PN_Keys, java.awt.BorderLayout.NORTH);

        setLayout(new java.awt.BorderLayout());

        TAB_Main.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N

        PN_File.setLayout(new java.awt.BorderLayout());

        PN_Overview.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Overview", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 11))); // NOI18N

        jLabel1.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Name");
        jLabel1.setPreferredSize(new java.awt.Dimension(100, 26));

        TF_Name.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N

        jLabel2.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Description");
        jLabel2.setPreferredSize(new java.awt.Dimension(100, 26));

        TA_Description.setColumns(20);
        TA_Description.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        TA_Description.setRows(5);
        jScrollPane3.setViewportView(TA_Description);

        CMB_Handlers.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        CMB_Handlers.setPreferredSize(new java.awt.Dimension(33, 26));

        jLabel4.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("Handler");
        jLabel4.setPreferredSize(new java.awt.Dimension(100, 26));

        LB_Version.setFont(new java.awt.Font("Monospaced", 0, 9)); // NOI18N
        LB_Version.setText("1.2");

        javax.swing.GroupLayout PN_OverviewLayout = new javax.swing.GroupLayout(PN_Overview);
        PN_Overview.setLayout(PN_OverviewLayout);
        PN_OverviewLayout.setHorizontalGroup(
            PN_OverviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PN_OverviewLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PN_OverviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PN_OverviewLayout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(TF_Name, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(PN_OverviewLayout.createSequentialGroup()
                        .addGroup(PN_OverviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(LB_Version, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TF_Name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PN_OverviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PN_OverviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(CMB_Handlers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(LB_Version, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        PN_File.add(PN_Overview, java.awt.BorderLayout.NORTH);

        PN_Columns.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Colums", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 11))); // NOI18N

        LI_Columns.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        LI_Columns.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(LI_Columns);

        BT_Add.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Add.setText("Add");
        BT_Add.setActionCommand("add");

        BT_Remove.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Remove.setText("Remove");
        BT_Remove.setActionCommand("remove");

        PN_ColumnConfig.setLayout(new java.awt.CardLayout());
        PN_ColumnConfig.add(LB_Empty, "empty");

        LB_ColumnName.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        LB_ColumnName.setText("Name");

        TF_ColumnName.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N

        LB_ColumnDescription.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        LB_ColumnDescription.setText("Description");

        jScrollPane4.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N

        TA_ColumnDescription.setColumns(20);
        TA_ColumnDescription.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        TA_ColumnDescription.setRows(5);
        TA_ColumnDescription.setToolTipText("<html>The eclipse plugin does not support saving in the same folder has the multiproperties.<br>\n</html>");
        jScrollPane4.setViewportView(TA_ColumnDescription);

        BT_ConfigureHandler.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_ConfigureHandler.setText("Configure handler");
        BT_ConfigureHandler.setActionCommand("handler");

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
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
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
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(LB_ColumnDescription)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(BT_ConfigureHandler)
                .addContainerGap())
        );

        PN_ColumnConfig.add(jPanel1, "column");

        BT_ColumnUp.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_ColumnUp.setText("Up");
        BT_ColumnUp.setActionCommand("columnUp");

        BT_ColumnDown.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_ColumnDown.setText("Down");
        BT_ColumnDown.setActionCommand("columnDown");

        BT_Import.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Import.setText("Import");
        BT_Import.setActionCommand("import");

        javax.swing.GroupLayout PN_ColumnsLayout = new javax.swing.GroupLayout(PN_Columns);
        PN_Columns.setLayout(PN_ColumnsLayout);
        PN_ColumnsLayout.setHorizontalGroup(
            PN_ColumnsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PN_ColumnsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PN_ColumnsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PN_ColumnsLayout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PN_ColumnsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(BT_Remove, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(BT_Add, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(BT_ColumnUp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(BT_ColumnDown, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(BT_Import, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(PN_ColumnConfig, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        PN_ColumnsLayout.setVerticalGroup(
            PN_ColumnsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PN_ColumnsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PN_ColumnsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(PN_ColumnConfig, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(PN_ColumnsLayout.createSequentialGroup()
                        .addComponent(BT_Add)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(BT_Remove)
                        .addGap(23, 23, 23)
                        .addComponent(BT_ColumnUp)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(BT_ColumnDown)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(PN_ColumnsLayout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(BT_Import)))
                .addContainerGap())
        );

        PN_File.add(PN_Columns, java.awt.BorderLayout.CENTER);

        TAB_Main.addTab("File", PN_File);

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
        SP_Table.setViewportView(TB_Table);

        PN_Table.add(SP_Table, java.awt.BorderLayout.CENTER);

        PN_Actions.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        BT_Save.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Save.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lsimedia/multiproperties/Resources/Icons/16x16/Save.png"))); // NOI18N
        BT_Save.setToolTipText("Save");
        BT_Save.setActionCommand("save");
        BT_Save.setEnabled(false);
        BT_Save.setFocusable(false);
        BT_Save.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        BT_Save.setPreferredSize(new java.awt.Dimension(28, 28));
        PN_Actions.add(BT_Save);

        BT_SaveProcess.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_SaveProcess.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lsimedia/multiproperties/Resources/Icons/16x16/Process.png"))); // NOI18N
        BT_SaveProcess.setToolTipText("Save and process");
        BT_SaveProcess.setActionCommand("saveProcess");
        BT_SaveProcess.setEnabled(false);
        BT_SaveProcess.setFocusable(false);
        BT_SaveProcess.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        BT_SaveProcess.setPreferredSize(new java.awt.Dimension(28, 28));
        PN_Actions.add(BT_SaveProcess);
        PN_Actions.add(jSeparator1);

        BT_NewProperty.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_NewProperty.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lsimedia/multiproperties/Resources/Icons/16x16/Add.png"))); // NOI18N
        BT_NewProperty.setText("New property");
        BT_NewProperty.setActionCommand("newProperty");
        BT_NewProperty.setFocusable(false);
        PN_Actions.add(BT_NewProperty);

        BT_NewComment.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_NewComment.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lsimedia/multiproperties/Resources/Icons/16x16/Add.png"))); // NOI18N
        BT_NewComment.setText("New comment");
        BT_NewComment.setActionCommand("newComment");
        BT_NewComment.setFocusable(false);
        PN_Actions.add(BT_NewComment);

        BT_NewEmpty.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_NewEmpty.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lsimedia/multiproperties/Resources/Icons/16x16/Add.png"))); // NOI18N
        BT_NewEmpty.setText("New empty line");
        BT_NewEmpty.setActionCommand("newEmpty");
        BT_NewEmpty.setFocusable(false);
        PN_Actions.add(BT_NewEmpty);

        BT_Merge.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Merge.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lsimedia/multiproperties/Resources/Icons/16x16/merge.png"))); // NOI18N
        BT_Merge.setText("Merge");
        BT_Merge.setActionCommand("merge");
        BT_Merge.setFocusable(false);
        PN_Actions.add(BT_Merge);

        BT_Sort.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Sort.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lsimedia/multiproperties/Resources/Icons/16x16/sort.png"))); // NOI18N
        BT_Sort.setText("Sort keys");
        BT_Sort.setActionCommand("sort");
        BT_Sort.setFocusable(false);
        PN_Actions.add(BT_Sort);
        PN_Actions.add(jSeparator2);

        BT_Delete.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Delete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lsimedia/multiproperties/Resources/Icons/16x16/Delete.png"))); // NOI18N
        BT_Delete.setText("Delete");
        BT_Delete.setActionCommand("delete");
        BT_Delete.setFocusable(false);
        PN_Actions.add(BT_Delete);

        PN_Table.add(PN_Actions, java.awt.BorderLayout.NORTH);

        TAB_Main.addTab("Table", PN_Table);

        add(TAB_Main, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void TB_TableKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TB_TableKeyPressed
        if (lockdown) return;

        if (evt.getKeyCode() == KeyEvent.VK_UP) {
            if (evt.isAltDown()) {
                ActionEvent e = new ActionEvent(TB_Table, ActionEvent.ACTION_PERFORMED, MN_MoveUp.getActionCommand());
                actionPerformed(e);

            }

        } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            if (evt.isAltDown()) {
                ActionEvent e = new ActionEvent(TB_Table, ActionEvent.ACTION_PERFORMED, MN_MoveDown.getActionCommand());
                actionPerformed(e);

            }

        } else if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
            ActionEvent e = new ActionEvent(TB_Table, ActionEvent.ACTION_PERFORMED, BT_Delete.getActionCommand());
            actionPerformed(e);

        }
    }//GEN-LAST:event_TB_TableKeyPressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BT_Add;
    private javax.swing.JButton BT_ColumnDown;
    private javax.swing.JButton BT_ColumnUp;
    private javax.swing.JButton BT_ConfigureHandler;
    private javax.swing.JButton BT_Delete;
    private javax.swing.JButton BT_Import;
    private javax.swing.JButton BT_Merge;
    private javax.swing.JButton BT_NewComment;
    private javax.swing.JButton BT_NewEmpty;
    private javax.swing.JButton BT_NewProperty;
    private javax.swing.JButton BT_Remove;
    private javax.swing.JButton BT_Save;
    private javax.swing.JButton BT_SaveProcess;
    private javax.swing.JButton BT_Sort;
    private javax.swing.JComboBox<PropertiesHandler> CMB_Handlers;
    private javax.swing.JLabel LB_ColumnDescription;
    private javax.swing.JLabel LB_ColumnName;
    private javax.swing.JLabel LB_Empty;
    private javax.swing.JLabel LB_Version;
    private javax.swing.JList<Column> LI_Columns;
    private javax.swing.JMenuItem MN_Clone;
    private javax.swing.JMenu MN_Columns;
    private javax.swing.JMenuItem MN_Copy;
    private javax.swing.JMenuItem MN_Cut;
    private javax.swing.JMenuItem MN_Delete;
    private javax.swing.JMenuItem MN_MoveDown;
    private javax.swing.JMenuItem MN_MoveUp;
    private javax.swing.JMenuItem MN_NewComment;
    private javax.swing.JMenuItem MN_NewEmpty;
    private javax.swing.JMenuItem MN_NewProperty;
    private javax.swing.JMenuItem MN_Paste;
    private javax.swing.JMenuItem MN_SetFinalAll;
    private javax.swing.JMenuItem MN_SetFinalTranslatedOnly;
    private javax.swing.JMenuItem MN_UnFinal;
    private javax.swing.JPanel PN_Actions;
    private javax.swing.JPanel PN_ColumnConfig;
    private javax.swing.JPanel PN_Columns;
    private javax.swing.JPanel PN_File;
    private javax.swing.JPanel PN_Keys;
    private javax.swing.JPanel PN_Overview;
    private javax.swing.JPanel PN_Rows;
    private javax.swing.JPanel PN_Table;
    private javax.swing.JPopupMenu PU_Actions;
    private javax.swing.JScrollPane SP_Table;
    private javax.swing.JTabbedPane TAB_Main;
    private javax.swing.JTextArea TA_ColumnDescription;
    private javax.swing.JTextArea TA_Description;
    private javax.swing.JTable TB_Table;
    private javax.swing.JTextField TF_ColumnName;
    private javax.swing.JTextField TF_Name;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    // End of variables declaration//GEN-END:variables

    /**
     * Parse the file, if the handler was found, return true, false otherwise
     *
     * @param f
     */
    private boolean parseMultiproperties(File f) {
        boolean found = false;
        try {
            InputSource source = new InputSource(new FileInputStream(f));
            source.setEncoding("UTF-8");
            Document xml = builder.parse(source);
            Element root = (Element) xml.getChildNodes().item(0);

            model.removeAllElements();
            columns.removeAllElements();

            NodeList nl = root.getChildNodes();
            for (int i = 0;i < nl.getLength();i++) {
                Node n = nl.item(i);
                if (n.getNodeName().equals("Version")) {
                    Element e = (Element) n;
                    if (e.getFirstChild() != null) LB_Version.setText(e.getFirstChild().getNodeValue());
                    
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
                            if (ph.getName().equals(hn)) {
                                CMB_Handlers.setSelectedIndex(j);
                                found = true;
                                break;
                            }
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
                            JMenu menu = c.getMenu();
                            model.addColumn(c);
                            MN_Columns.add(menu);
                            for (int k = 0;k < menu.getItemCount();k++) menu.getItem(k).addActionListener(this);

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
            fillRowHeader();
            resizeColumns();

        } catch (SAXException ex) {
            logit.log("E", "Parsing error " + ex.getMessage(), null);
            ex.printStackTrace();

        } catch (IOException ex) {
            logit.log("E", "Parsing error " + ex.getMessage(), null);
            ex.printStackTrace();

        }
        return found;
    }

    /**
     * Resize the column with the width defined in the multiproperties file
     */
    private void resizeColumns() {
        //--- Set column size
        //--- First one is the key wich is hidden
        for (int i = 0;i < model.getColumnCount();i++) {
            Column c = model.getColumn(i);
            if (i == 0) {
                //--- Hide key column (do not remove it from column model)
                TB_Table.getColumnModel().getColumn(i).setMaxWidth(0);
                TB_Table.getColumnModel().getColumn(i).setMinWidth(0);
                TB_Table.getColumnModel().getColumn(i).setPreferredWidth(0);
                TB_Table.getColumnModel().getColumn(i).setWidth(0);

            } else {
                TB_Table.getColumnModel().getColumn(i).setPreferredWidth(c.width);
                TB_Table.getColumnModel().getColumn(i).setWidth(c.width);
            }

        }

    }

    private void fillRowHeader() {
        PN_Keys.removeAll();

        //--- Add flavor keys
        for (int i = 0;i < model.getRowCount();i++) {
            Record rec = (Record) model.getValueAt(i, 0);
            JTextField tf = new JTextField(rec.getKey());
            tf.setFont(new Font("Monospaced", Font.BOLD, 11));
            tf.setForeground(TB_Table.getForeground());
            tf.setBackground(COLOR_KEY);
            if (rec instanceof CommentRecord) {
                CommentRecord cr = (CommentRecord) rec;
                tf.setText(cr.getValue());
                tf.setForeground(Color.BLUE);

            } else if (rec instanceof PropertyRecord) {
                //--- Check if multiple same keys
                PropertyRecord pr = (PropertyRecord) rec;

                boolean same = false;
                for (int j = 0;j < model.getRowCount();j++) {
                    Record tmp = (Record) model.getValueAt(j, 0);
                    if (tmp.getKey() != null) {
                        if ((j != i) && pr.getKey().equals(tmp.getKey())) same = true;
                    }
                }
                if (same) {
                    tf.setText(pr.name);
                    tf.setForeground(Color.RED);

                } else if (pr.disabled) {
                    tf.setText(pr.name);
                    tf.setForeground(Color.LIGHT_GRAY);

                } else {
                    tf.setText(pr.name);

                }

            }
            Dimension dim = tf.getPreferredSize();
            tf.setPreferredSize(new Dimension(dim.width + 10, 22));
            tf.setEditable(false);
            // tf.setBackground(bg);
            PN_Keys.add(tf);
        }
        PN_Keys.revalidate();
    }

    /**
     * Fire the listeners that the data has changed
     */
    private void fireModifiedEvent() {
        BT_Save.setEnabled(true);
        BT_SaveProcess.setEnabled(true);

        //--- Fill row header first
        fillRowHeader();

        modified = true;
        if (listener != null) listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ACTION_COMMAND_MODIFIED));

    }
  

    private class CopyAction extends AbstractAction {

        ClipboardOwner owner = null;

        public CopyAction(ClipboardOwner owner) {
            super("Copy", null);
            this.owner = owner;
            putValue(ACTION_COMMAND_KEY, "copy");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("CTRL+C"));
            putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke("CTRL+C"));
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            RecordDnDVector dnd = new RecordDnDVector();
            for (int i = 0;i < selectedIndexes.length;i++) dnd.add(model.getRecord(selectedIndexes[i]));
            dnd.setInfo(TransferHandler.COPY);
            dnd.setColumnNames(model.getColumnNames());
            CLIPBOARD.setContents(dnd, owner);

        }

    }

    private class CutAction extends AbstractAction {

        ClipboardOwner owner = null;

        public CutAction(ClipboardOwner owner) {
            super("Cut", null);
            this.owner = owner;
            putValue(ACTION_COMMAND_KEY, "cut");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("CTRL-X"));
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            RecordDnDVector dnd = new RecordDnDVector();
            for (int i = 0;i < selectedIndexes.length;i++) dnd.add(model.getRecord(selectedIndexes[i]));
            dnd.setInfo(TransferHandler.MOVE);
            dnd.setColumnNames(model.getColumnNames());
            CLIPBOARD.setContents(dnd, owner);

        }

    }

    /**
     * Past just after the selected row
     */
    private class PasteAction extends AbstractAction {

        ClipboardOwner owner = null;

        public PasteAction(ClipboardOwner owner) {
            super("Paste", null);
            this.owner = owner;
            putValue(ACTION_COMMAND_KEY, "paste");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("CTRL-V"));
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            int index = TB_Table.getSelectedRow();
            if (index == -1) index = TB_Table.getRowCount();
            
            RecordDnDVector dnd = (RecordDnDVector) CLIPBOARD.getContents(null);
            if (dnd == null) return;

            int info = dnd.getInfo();
            String columnNames[] = dnd.getColumnNames();
            // System.out.println("COLUMN NAMES LENGTH:" + columnNames.length);
            // for (int i = 0;i < columnNames.length;i++) System.out.println("COLUMN[" + i + "] = " + columnNames[i]);
            for (int i = 0;i < dnd.size();i++) {
                Record rec = dnd.get(i);
                if (rec instanceof EmptyRecord) {
                    EmptyRecord ec = new EmptyRecord();
                    model.insertRecord(index, ec);

                } else if (rec instanceof CommentRecord) {
                    CommentRecord cr = (CommentRecord) rec.copy();
                    model.insertRecord(index, cr);

                } else if (rec instanceof PropertyRecord) {
                    PropertyRecord nrec = (PropertyRecord) rec.copy();
                    model.insertRecord(index, nrec);

                }
                index++;
            }
            //--- Clear the clipboard
            CLIPBOARD.setContents(null, null);

            fireModifiedEvent();

        }

    }
}
