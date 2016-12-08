/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lsimedia.multiproperties.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import lsimedia.multiproperties.Column;
import lsimedia.multiproperties.CommentRecord;
import lsimedia.multiproperties.HandlerGUI;
import lsimedia.multiproperties.Logit;
import lsimedia.multiproperties.MultiPropertiesTableModel;
import lsimedia.multiproperties.PropertiesHandler;
import lsimedia.multiproperties.PropertyRecord;
import lsimedia.multiproperties.Record;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The Android value files (/res/values/string.xml)
 *
 * @author sbodmer
 */
public class AndroidValuesHandler implements PropertiesHandler {

    DocumentBuilder builder = null;
    
    private static final char[] hexChar = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };
    
    /**
     * Last error
     */
    String error = null;

    public AndroidValuesHandler() {
        try {
             builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            
        }

    }

    //**************************************************************************
    //*** API
    //**************************************************************************
    public String toString() {
        return getName();
    }

    public String getName() {
        return "Android Values Handler";
    }

    //**************************************************************************
    //*** PropertiesHandler
    //**************************************************************************
    /**
     * There is some specific stuff to do
     * <PRE>
     * escape : for the keys
     * escape " as "\"" for the value
     * escape '\n' as "\n" for the value
     * escape ' as "\'" for the value
     * </PRE>
     *
     * @param model
     * @param name
     * @param description
     * @param source
     * @param logit
     * @return
     */
    @Override
    public boolean save(final MultiPropertiesTableModel model, final String name, final String description, final File source, final Logit logit) {
        //--- For each column, store the xml file
        boolean result = true;

        //--- The column at index 0 is the key, do not handle it
        for (int i = 1;i < model.getColumnCount();i++) {
            try {
                Column c = model.getColumn(i);

                String tokens[] = c.getHandlerConfiguration().split("\\|");
                String fn = tokens[0];
                if (fn.equals("")) continue;
                fn = fn.replace('\\', '/');

                File file = new File(fn);
                if (fn.startsWith("/")) {
                    //--- Absolute path, do nothing

                } else if (fn.startsWith("./")) {
                    //--- Use same directory has source
                    file = new File(source.getParent(), file.getPath());

                } else if (fn.startsWith("../")) {
                    //--- Use relative path
                    file = new File(source.getParent(), file.getPath());

                }

                //--- Headers
                // FileWriter fw = new FileWriter(file);
                PrintWriter fw = new PrintWriter(file, "UTF-8");
                fw.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
                fw.write("<resources>\n");
                if (tokens[1].equals("true")) {
                    String parts[] = description.split("\\n");
                    for (int j = 0;j < parts.length;j++) {
                        fw.write("<!-- ");
                        fw.write(escape(parts[j]) + "\n");
                        fw.write("-->\n");
                    }
                }
                if (tokens[2].equals("true")) {
                    String parts[] = c.getDescription().split("\\n");
                    for (int j = 0;j < parts.length;j++) {
                        fw.write("<!-- ");
                        fw.write(escape(parts[j]) + "\n");
                        fw.write("-->\n");
                    }
                }

                //--- Records
                for (int j = 0;j < model.getRowCount();j++) {
                    Record rec = model.getRecord(j);
                    if (rec instanceof CommentRecord) {
                        CommentRecord cr = (CommentRecord) rec;
                        fw.write("<!-- ");
                        fw.write(escape(cr.getValue()));
                        fw.write("-->\n");

                    } else if (rec instanceof PropertyRecord) {
                        PropertyRecord pr = (PropertyRecord) rec;
                        String key = pr.getName();
                        key = key.replaceAll(":", "\\\\:");

                        PropertyRecord.Value v = pr.getValueAt(i - 1);
                        String val = v.isDisabled() ? pr.getDefaultValue() : v.getValue();
                        val = val.replaceAll("\n", "\\\\n");
                        val = val.replaceAll("\"","\\\\\"");
                        val = val.replaceAll("'","\\\'");
                        
                        if (tokens[3].equals("true") && pr.isDisabled()) {
                            fw.write("<!-- string name=\"" + key + "\">" + escape(val) + "</string -->\n");

                        } else if (v.isDisabled()) {
                            if (!tokens[4].equals("true")) {
                                fw.write("<string name=\"" + key + "\">" + escape(val) + "</string>\n");
                            }

                        } else {
                            fw.write("<string name=\"" + key + "\">" + escape(val) + "</string>\n");

                        }

                    }

                }
                fw.write("</resources>");
                fw.close();

                logit.log("I", "Storing android values xml at " + file.getCanonicalPath(), null);

            } catch (Exception ex) {
                error = ex.getMessage();
                ex.printStackTrace();
                logit.log("E", "Error storing android values xml :" + ex.getMessage(), null);
                result = false;
            }

        }

        return result;
    }

    /**
     * Load the xml file
     *
     * @param model
     * @param f
     * @return
     */
    @Override
    public Column load(MultiPropertiesTableModel model, File f) {
        //--- Load the java properties file
        try {
            Document document = builder.parse(f);
            
            //--- Store the keys in a map for later use
            HashMap<String, String> hm = new HashMap<>();
            NodeList nl = document.getElementsByTagName("string");
            for (int i=0;i<nl.getLength();i++) {
                Element e = (Element) nl.item(i);
                String key = e.getAttribute("name");
                if (e.getFirstChild() != null) {
                    String value = e.getFirstChild().getNodeValue();
                    value = value.replaceAll("\\\\'", "'");
                    hm.put(key, value);
                    
                } else {
                    //---
                }
                
            }
            
            Column c = new Column(f.getName());
            c.setHandlerConfiguration(f.getPath() + "|false|true|true|false|false");

            //--- Add the new column to all property records
            for (int i = 0;i < model.getRowCount();i++) {
                Record rec = model.getRecord(i);
                if (rec instanceof PropertyRecord) {
                    PropertyRecord pr = (PropertyRecord) rec;
                    pr.addColumn();
                }
            }
            //--- Add column
            model.addColumn(c);

            //--- Fill the the added column
            ArrayList<Record> newRecords = new ArrayList<>();
            Iterator<String> it = hm.keySet().iterator();
            while (it.hasNext()) {
                String property = it.next();
                String value = hm.get(property);

                Record rec = model.getRecord(property);
                if (rec == null) {
                    //--- Not found, add the entry with the default value
                    PropertyRecord pr = new PropertyRecord(property);
                    pr.setDefaultValue(value);

                    for (int i = 0;i < model.getColumnCount() - 1;i++) pr.addColumn();
                    //--- Last column contains the value
                    PropertyRecord.Value v = pr.getValueAt(pr.getValueCount() - 1);
                    v.setValue(value);
                    v.setDisable(false);
                    newRecords.add(pr);

                } else if (rec instanceof PropertyRecord) {
                    PropertyRecord pr = (PropertyRecord) rec;
                    PropertyRecord.Value v = pr.getValueAt(pr.getValueCount() - 1);
                    v.setValue(value);
                    v.setDisable(false);
                }
            }

            //--- Add the new records
            for (int i = 0;i < newRecords.size();i++) model.addRecord(newRecords.get(i));

            return c;

        } catch (Exception ex) {
            error = ex.getMessage();
            ex.printStackTrace();

        }
        return null;

    }

    @Override
    public HandlerGUI getGUI(Column column, File source) {
        //---
        return new JAndroidValuesHandler(column, source);
    }

    @Override
    public String getLastError() {
        return error;
    }

    //**************************************************************************
    //*** Private
    //**************************************************************************
    /**
     * Escape the some chars
     *
     * @param s
     * @return
     */
    private static String escape(final String s) {
        String sb = s.replace('<', ' ');
        sb = sb.replace('>', ' ');
        sb = sb.replaceAll("'", "\\\\'");
        sb = sb.replaceAll("\n", "\\\\\\n");
        return sb;
    }
    
    
}
