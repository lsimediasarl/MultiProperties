/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lsimedia.multiproperties.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * MultiPropertiesResourceBundle
 *
 * Store only the values for the current locale
 *
 * @author sbodmer
 */
public class MultiPropertiesResourceBundle extends ResourceBundle {

    String version = "";
    String name = "";
    String description = "";
    String handler = "";

    /**
     * The key is the resource key
     */
    HashMap<String, String> values = new HashMap<>();

    public MultiPropertiesResourceBundle(BufferedInputStream in, Locale locale) {
        //---
        try {
            //--- Parse config file
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource source = new InputSource(in);
            source.setEncoding("UTF-8");
            Document xml = builder.parse(source);

            int localeIndex = -1;
            Element root = (Element) xml.getChildNodes().item(0);
            NodeList nl = root.getChildNodes();
            for (int i = 0;i < nl.getLength();i++) {
                Node n = nl.item(i);
                if (n.getNodeName().equals("Version")) {
                    Element e = (Element) n;
                    if (e.getFirstChild() != null) version = e.getFirstChild().getNodeValue();

                } else if (n.getNodeName().equals("Name")) {
                    Element e = (Element) n;
                    if (e.getFirstChild() != null) name = e.getFirstChild().getNodeValue();

                } else if (n.getNodeName().equals("Description")) {
                    Element e = (Element) n;
                    if (e.getFirstChild() != null) description = e.getFirstChild().getNodeValue();

                } else if (n.getNodeName().equals("Handler")) {
                    Element e = (Element) n;
                    if (e.getFirstChild() != null) handler = e.getFirstChild().getNodeValue();

                } else if (n.getNodeName().equals("Columns")) {
                    Element e = (Element) n;
                    NodeList cols = e.getElementsByTagName("Column");
                    for (int j = 0;j < cols.getLength();j++) {
                        Node n2 = cols.item(j);
                        Element col = (Element) n2;
                        String clocale = col.getElementsByTagName("Name").item(0).getFirstChild().getNodeValue();
                        if (clocale.equals(locale.getLanguage())) localeIndex = j;

                    }

                } else if (n.getNodeName().equals("Records")) {
                    Element e = (Element) n;
                    NodeList recs = e.getElementsByTagName("Property");
                    for (int j = 0;j < recs.getLength();j++) {
                        Node n2 = recs.item(j);
                        Element pr = (Element) n2;

                        String key = "";
                        boolean disabled = true;
                        String defaultValue = "";

                        //--- The value index
                        int index = 0;
                        NodeList lines = pr.getChildNodes();
                        for (int k = 0;k < lines.getLength();k++) {
                            Node n3 = lines.item(k);
                            if (n3.getNodeName().equals("Name")) {
                                key = n3.getFirstChild().getNodeValue();

                            } else if (n3.getNodeName().equals("Disabled")) {
                                disabled = n3.getFirstChild().getNodeValue().equals("true");

                            } else if (n3.getNodeName().equals("Description")) {
                                //--- Not handled

                            } else if (n3.getNodeName().equals("MultiLine")) {
                                //--- Not handled

                            } else if (n3.getNodeName().equals("DefaultValue")) {
                                defaultValue = n3.getFirstChild().getNodeValue();

                            } else if (n3.getNodeName().equals("Value")) {
                                if (!disabled) {
                                    e = (Element) n3;
                                    boolean vdisabled = e.getAttribute("disabled").equals("true");
                                    String v = (e.getFirstChild() != null ? e.getFirstChild().getNodeValue() : "");
                                    if ((!vdisabled) && (index == localeIndex)) values.put(key, v);
                                    index++;
                                }

                            }
                        }

                        //--- Add default if no locale was found
                        if ((!disabled) && (localeIndex == -1)) values.put(key, defaultValue);

                    }
                }
            }
            in.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Check the values,
     *
     * @param key
     * @return
     */
    @Override
    protected Object handleGetObject(String key) {
        if (key == null) throw new NullPointerException();
        
        String s = values.get(key);
        if (s == null) return "<" + key + ">";
        return s;

    }

    @Override
    public Enumeration<String> getKeys() {

        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
