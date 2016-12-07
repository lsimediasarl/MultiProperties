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
import java.util.Properties;
import lsimedia.multiproperties.Column;
import lsimedia.multiproperties.CommentRecord;
import lsimedia.multiproperties.HandlerGUI;
import lsimedia.multiproperties.Logit;
import lsimedia.multiproperties.MultiPropertiesTableModel;
import lsimedia.multiproperties.PropertiesHandler;
import lsimedia.multiproperties.PropertyRecord;
import lsimedia.multiproperties.Record;

/**
 * The Java properties file
 *
 * @author sbodmer
 */
public class JavaPropertiesHandler implements PropertiesHandler {
    private static final char[] hexChar = {
        '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };
    
    /**
     * Last error
     */
    String error = null;
    
    public JavaPropertiesHandler() {
        //---
    }

    //**************************************************************************
    //*** API
    //**************************************************************************
    public String toString() {
        return getName();
    }

    public String getName() {
        return "Java Properties Handler";
    }

    //**************************************************************************
    //*** PropertiesHandler
    //**************************************************************************
    /**
     * There is some specific stuff to do
     * <PRE>
     * escape ':' and '\n'
     * </PRE>
     * @param model
     * @param name
     * @param description
     * @param source
     * @param logit
     * @return 
     */
    @Override
    public boolean save(final MultiPropertiesTableModel model, final String name, final String description, final File source, final Logit logit) {
        //--- For each column, store the Java properties file
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

                // System.out.println("JAVA PROPERTIES FILE IS:" + file.getCanonicalPath());

                //--- Headers
                // FileWriter fw = new FileWriter(file);
                PrintWriter fw = new PrintWriter(file, "us-ascii");
                if (tokens[1].equals("true")) {
                    String parts[] = description.split("\\n");
                    for (int j = 0;j < parts.length;j++) fw.write("#" + unicodeEscape(parts[j]) + "\n");
                }
                if (tokens[2].equals("true")) {
                    String parts[] = c.getDescription().split("\\n");
                    for (int j = 0;j < parts.length;j++) fw.write("#" + unicodeEscape(parts[j]) + "\n");
                }

                //--- Records
                for (int j = 0;j < model.getRowCount();j++) {
                    Record rec = model.getRecord(j);
                    if (rec instanceof CommentRecord) {
                        CommentRecord cr = (CommentRecord) rec;
                        fw.write("#" + unicodeEscape(cr.getValue()) + "\n");

                    } else if (rec instanceof PropertyRecord) {
                        PropertyRecord pr = (PropertyRecord) rec;
                        String key = pr.getName();
                        key = key.replaceAll(":", "\\\\:");
                        
                        PropertyRecord.Value v = pr.getValueAt(i - 1);
                        String val = v.isDisabled() ? pr.getDefaultValue() : v.getValue();
                        val = val.replaceAll("\n","\\\\n");
                            
                        if (tokens[3].equals("true") && pr.isDisabled()) {
                            fw.write("#" + key + "=" + unicodeEscape(val) + "\n");

                        } else if (v.isDisabled()) {
                            if (!tokens[4].equals("true")) fw.write("" + key + "=" + unicodeEscape(val) + "\n");

                        } else {
                            fw.write("" + key + "=" + unicodeEscape(val) + "\n");
                            
                        }

                    }

                }
                fw.close();

                logit.log("I", "Store java properties at "+file.getCanonicalPath(), null);
                
            } catch (Exception ex) {
                error = ex.getMessage();
                ex.printStackTrace();
                logit.log("E", "Error storing java properties :"+ex.getMessage(), null);
                result = false;
            }
            
        }

        return result;
    }

    @Override
    public Column load(MultiPropertiesTableModel model, File f) {
        //--- Load the java properties file
        Properties prop = new Properties();
        try {
            FileInputStream fin = new FileInputStream(f);
            prop.load(fin);
            
            Column c = new Column(f.getName());
            c.setHandlerConfiguration(f.getPath()+"|false|true|true|false|false");
            
            //--- Add the new column to all property records
            for (int i=0;i<model.getRowCount();i++) {
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
            Enumeration<String> en = (Enumeration<String>) prop.propertyNames();
            while (en.hasMoreElements()) {
                String property = en.nextElement();
                String value = prop.getProperty(property);
                
                Record rec = model.getRecord(property);
                if (rec == null) {
                    //--- Not found, add the entry with the default value
                    PropertyRecord pr = new PropertyRecord(property);
                    pr.setDefaultValue(value);
                    
                    for (int i=0;i<model.getColumnCount()-1;i++) pr.addColumn();
                    //--- Last column contains the value
                    PropertyRecord.Value v = pr.getValueAt(pr.getValueCount()-1);
                    v.setValue(value);
                    v.setDisable(false);
                    newRecords.add(pr);
                    
                } else if (rec instanceof PropertyRecord) {
                    PropertyRecord pr = (PropertyRecord) rec;
                    PropertyRecord.Value v = pr.getValueAt(pr.getValueCount()-1);
                    v.setValue(value);
                    v.setDisable(false);
                }
            }
            
            //--- Add the new records
            for (int i=0;i<newRecords.size();i++) model.addRecord(newRecords.get(i));
            
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
        return new JJavaPropertiesHandler(column, source);
    }
    
    @Override
    public String getLastError() {
        return error;
    }
    
    //**************************************************************************
    //*** Private
    //**************************************************************************
    
    /**
     * Convert utf-8 to unicode escaped sequence
     * @param s
     * @return 
     */
    private static String unicodeEscape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0;i < s.length();i++) {
            char c = s.charAt(i);
            if ((c >> 7) > 0) {
                sb.append("\\u");
                sb.append(hexChar[(c >> 12) & 0xF]); // append the hex character for the left-most 4-bits
                sb.append(hexChar[(c >> 8) & 0xF]);  // hex for the second group of 4-bits from the left
                sb.append(hexChar[(c >> 4) & 0xF]);  // hex for the third group
                sb.append(hexChar[c & 0xF]);         // hex for the last group, e.g., the right most 4-bits
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
