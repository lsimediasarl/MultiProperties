/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lsimedia.multiproperties;

import lsimedia.multiproperties.Record;
import lsimedia.multiproperties.RecordGUI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author sbodmer
 */
public class CommentRecord extends Record {

    String value = "";

    public CommentRecord(String comment) {
        super();
        
        this.value = comment;
    }
    
    public CommentRecord(Element record) {
        super(record);

        NodeList nl = record.getChildNodes();
        for (int i = 0;i < nl.getLength();i++) {
            Node n = nl.item(i);
            try {
                if (n.getNodeName().equals("Value")) {
                    Element e = (Element) n;
                    value = e.getFirstChild().getNodeValue();
                }

            } catch (Exception ex) {
                //---
            }
        }

    }

    //**************************************************************************
    //*** API
    //**************************************************************************
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    //**************************************************************************
    //*** Record
    //**************************************************************************
    @Override
    public RecordGUI getGUI(MultiPropertiesTableModel model, int selectedColumn, boolean lockdown) {
        return new JCommentRecord(this, lockdown);
    }

    @Override
    public String getKey() {
        return "";
    }
    
    @Override
    public void save(Element records) {
        Element e = records.getOwnerDocument().createElement("Comment");
        Element w = records.getOwnerDocument().createElement("Value");
        w.appendChild(records.getOwnerDocument().createTextNode(value));
        e.appendChild(w);
        records.appendChild(e);
    }

    @Override
    public void swapColumn(int fromIndex, int toIndex) {
        //--- Nonthing here
    }
    
    @Override
    public void addColumn() {
        //--- Nothing here
    }
    
    @Override
    public void removeColumn(int index) {
        //--- Nothing here
    }
    
    @Override
    public Object copy() {
        return new CommentRecord(value);
    }

    @Override
    public String getSortString() {
        return getValue()+""+hashCode();
    }
}
