/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lsimedia.multiproperties;

import java.awt.Font;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author sbodmer
 */
public class Column {

    String name = "";
    String description = "";
    int width = 100;
    String handlerConfiguration = "";
    
    /**
     * The menu item to use for column selection
     */
    JMenu menu = null;
    
    public Column(String name) {
        this.name = name;
        
        prepare();
    }
    
    public Column(Element e) {
        NodeList nl = e.getChildNodes();
        for (int i = 0;i < nl.getLength();i++) {
            try {
                Node n = nl.item(i);
                if (n.getNodeName().equals("Name")) {
                    name = n.getFirstChild().getNodeValue();

                } else if (n.getNodeName().equals("HandlerConfiguration")) {
                    handlerConfiguration = n.getFirstChild().getNodeValue();
                    
                } else if (n.getNodeName().equals("Description")) {
                    description = n.getFirstChild().getNodeValue();

                } else if (n.getNodeName().equals("Width")) {
                    width = Integer.parseInt(n.getFirstChild().getNodeValue());
                }

            } catch (Exception ex) {
                //--- Number format exception or child node is null

            }
        }

        prepare();
    }

    @Override
    public String toString() {
        return name;
    }
    
    private void prepare() {
        menu = new JMenu(name);
        menu.setFont(new Font("Arial", Font.PLAIN, 11));
        JMenuItem item = new JMenuItem("Set final");
        item.setFont(new Font("Arial", Font.PLAIN, 11));
        item.setActionCommand("columnSetFinal");
        item.putClientProperty("column", this);
        menu.add(item);
        
        item = new JMenuItem("Set to unfinal");
        item.setFont(new Font("Arial", Font.PLAIN, 11));
        item.setActionCommand("columnSetUnFinal");
        item.putClientProperty("column", this);
        menu.add(item);
        
        item = new JMenuItem("Set to default value");
        item.setFont(new Font("Arial", Font.PLAIN, 11));
        item.setActionCommand("columnSetToDefault");
        item.putClientProperty("column", this);
        menu.add(item);
        
    }
    //**************************************************************************
    //*** API
    //**************************************************************************
    public String getName() {
        return name;

    }

    public JMenu getMenu() {
        return menu;
    }
    
    public void setName(String name) {
        this.name = name;
        menu.setText(name);
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getWidth() {
        return width;
    }
    
    public void setWidth(int width) {
        this.width = width;
    }
    
    public String getHandlerConfiguration() {
        return handlerConfiguration;
        
    }
    
    public void setHandlerConfiguration(String handlerConfiguration) {
        this.handlerConfiguration = handlerConfiguration;
    }
    
    /**
     * Dump the Column tag
     * 
     * @param e 
     */
    public void save(Element columns) {
        if (name.equals("Key")) {
            Element e = columns.getOwnerDocument().createElement("Key");
            Element w = columns.getOwnerDocument().createElement("Width");
            w.appendChild(columns.getOwnerDocument().createTextNode(""+width));
            e.appendChild(w);
            columns.appendChild(e);
            
        } else {
            Element e = columns.getOwnerDocument().createElement("Column");
            Element t = columns.getOwnerDocument().createElement("Width");
            t.appendChild(columns.getOwnerDocument().createTextNode(""+width));
            e.appendChild(t);
            
            t = columns.getOwnerDocument().createElement("Name");
            t.appendChild(columns.getOwnerDocument().createTextNode(name));
            e.appendChild(t);
            
            t = columns.getOwnerDocument().createElement("Description");
            t.appendChild(columns.getOwnerDocument().createTextNode(description));
            e.appendChild(t);
            
            t = columns.getOwnerDocument().createElement("HandlerConfiguration");
            t.appendChild(columns.getOwnerDocument().createTextNode(handlerConfiguration));
            e.appendChild(t);
            
            columns.appendChild(e);
        }
    }
}
