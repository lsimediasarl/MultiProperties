/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lsimedia.multiproperties;

import lsimedia.multiproperties.Column;
import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author sbodmer
 */
public class PropertyRecord extends Record {

    String name = "";
    String description = "";
    boolean disabled = false;    //--- Write has comment
    boolean multiLine = false;
    String defaultValue = "";

    /**
     * The order is important => columns
     */
    ArrayList<Value> values = new ArrayList<>();

    public PropertyRecord(String name) {
        super();
        this.name = name;
    }

    public PropertyRecord(Element record) {
        super(record);

        NodeList nl = record.getChildNodes();
        for (int i = 0;i < nl.getLength();i++) {
            Node n = nl.item(i);
            try {
                if (n.getNodeName().equals("Name")) {
                    Element e = (Element) n;
                    name = e.getFirstChild().getNodeValue();

                } else if (n.getNodeName().equals("Disabled")) {
                    disabled = n.getFirstChild().getNodeValue().equals("true");

                } else if (n.getNodeName().equals("Description")) {
                    description = n.getFirstChild().getNodeValue();

                } else if (n.getNodeName().equals("MultiLine")) {
                    multiLine = n.getFirstChild().getNodeValue().equals("true");

                } else if (n.getNodeName().equals("DefaultValue")) {
                    defaultValue = n.getFirstChild().getNodeValue();

                } else if (n.getNodeName().equals("Value")) {
                    Element e = (Element) n;
                    boolean disabled = e.getAttribute("disabled").equals("true");
                    String v = (e.getFirstChild() != null ? e.getFirstChild().getNodeValue() : "");
                    values.add(new Value(disabled, v));
                }

            } catch (Exception ex) {
                //---
            }
        }

    }

    //**************************************************************************
    //*** API
    //**************************************************************************
    public Value getValueAt(int index) {
        return values.get(index);
    }

    public void setValueAt(int index, Value element) {
        values.set(index, element);
    }
    
    public int getValueCount() {
        return values.size();
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getName() {
        return name;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public boolean isMultiLine() {
        return multiLine;
    }

    //**************************************************************************
    //*** Record
    //***************************************************************************
    @Override
    public RecordGUI getGUI(ArrayList<Column> columns) {
        return new JPropertyRecord(this, columns);
    }

    @Override
    public String getKey() {
        return name;
    }

    @Override
    public void save(Element records) {
        Element e = records.getOwnerDocument().createElement("Property");
        Element w = records.getOwnerDocument().createElement("Name");
        w.appendChild(records.getOwnerDocument().createTextNode(name));
        e.appendChild(w);

        w = records.getOwnerDocument().createElement("Description");
        w.appendChild(records.getOwnerDocument().createTextNode(description));
        e.appendChild(w);

        w = records.getOwnerDocument().createElement("Disabled");
        w.appendChild(records.getOwnerDocument().createTextNode("" + disabled));
        e.appendChild(w);

        w = records.getOwnerDocument().createElement("MultiLine");
        w.appendChild(records.getOwnerDocument().createTextNode("" + multiLine));
        e.appendChild(w);

        w = records.getOwnerDocument().createElement("DefaultValue");
        w.appendChild(records.getOwnerDocument().createTextNode(defaultValue));
        e.appendChild(w);

        //--- The column values
        for (int i = 0;i < values.size();i++) {
            Value v = values.get(i);
            w = records.getOwnerDocument().createElement("Value");
            w.appendChild(records.getOwnerDocument().createTextNode(v.value));
            w.setAttribute("disabled", "" + v.disabled);
            e.appendChild(w);
        }

        records.appendChild(e);
    }

    @Override
    public void swapColumn(int fromIndex, int toIndex) {
        //--- Nonthing here
        Value to = values.get(toIndex);
        Value from = values.get(fromIndex);
        values.set(fromIndex, to);
        values.set(toIndex, from);
    }

    @Override
    public void addColumn() {
        //--- Nothing here
        values.add(new Value(true, ""));

    }

    @Override
    public void removeColumn(int index) {
        values.remove(index);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        PropertyRecord c = (PropertyRecord) super.clone();
        //--- Clone the values
        for (int i=0;i<values.size();i++) c.setValueAt(i, (PropertyRecord.Value) values.get(i).clone());
        return c;
    }

    //**************************************************************************
    //*** Helpful class
    //**************************************************************************
    public class Value implements Cloneable {

        String value = "";
        boolean disabled = true;    //--- Use default value

        public Value(boolean disabled, String value) {
            this.disabled = disabled;
            this.value = value;
        }

        public String toString() {
            return value;
        }

        public boolean isDisabled() {
            return disabled;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public void setDisable(boolean disabled) {
            this.disabled = disabled;

        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }
}
