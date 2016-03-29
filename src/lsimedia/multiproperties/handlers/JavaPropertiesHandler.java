/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lsimedia.multiproperties.handlers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Properties;
import javax.swing.JComponent;
import lsimedia.multiproperties.Column;
import lsimedia.multiproperties.CommentRecord;
import lsimedia.multiproperties.HandlerGUI;
import lsimedia.multiproperties.MultiPropertiesTableModel;
import lsimedia.multiproperties.PropertiesHandler;
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
    @Override
    public boolean save(MultiPropertiesTableModel model, String name, String description, File source) {
        //--- For each column, store the Java properties file
        //--- The column at index 0 is the key, do not handle it
        for (int i = 1;i < model.getColumnCount();i++) {
            try {
                Column c = model.getColumn(i);

                String tokens[] = c.getHandlerConfiguration().split("\\|");
                String fn = tokens[0];
                if (fn.equals("")) continue;
                        
                File file = new File(fn);
                if (tokens.length >= 6) {
                    if (tokens[5].equals("true")) {
                        file = new File(source.getParent(), file.getName());
                    }
                }

                // System.out.println("JAVA PROPERTIES FILE IS:" + file.getPath());

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
                        PropertyRecord.Value v = pr.getValueAt(i - 1);
                        String key = pr.getName();
                        if (tokens[3].equals("true") && pr.isDisabled()) {
                            String val = v.isDisabled() ? pr.getDefaultValue() : v.getValue();
                            fw.write("#" + key + "=" + unicodeEscape(val) + "\n");

                        } else if (v.isDisabled()) {
                            if (!tokens[4].equals("true")) fw.write("" + key + "=" + unicodeEscape(pr.getDefaultValue()) + "\n");

                        } else {
                            fw.write("" + key + "=" + unicodeEscape(v.getValue()) + "\n");
                        }

                    }

                }
                fw.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return false;
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
            
            //--- Fill the the added column
            Enumeration<String> en = (Enumeration<String>) prop.propertyNames();
            while (en.hasMoreElements()) {
                String property = en.nextElement();
                String value = prop.getProperty(property);
                
                Record rec = model.find(property);
                if (rec == null) continue;
                
                if (rec instanceof PropertyRecord) {
                    PropertyRecord pr = (PropertyRecord) rec;
                    PropertyRecord.Value v = pr.getValueAt(pr.getValueCount()-1);
                    v.setValue(value);
                    v.setDisable(false);
                }
            }
            
            //--- Add column at the end
            model.addColumn(c);
            
            return c;
            
        } catch (Exception ex) {
            ex.printStackTrace();
            
        }   
        return null;
        
    }
    
    @Override
    public HandlerGUI getGUI(Column column) {
        //---
        return new JJavaPropertiesHandler(column);
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
