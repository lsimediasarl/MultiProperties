package lsimedia.multiproperties.util;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.TransferHandler;
import lsimedia.multiproperties.Record;

/**
 *
 * @author sbodmer
 */
public class RecordDnDVector implements Transferable {

    public static DataFlavor recordDnDVectorFlavor = new DataFlavor(RecordDnDVector.class, DataFlavor.javaJVMLocalObjectMimeType);

    ArrayList<Record> v = new ArrayList<Record>();
    int info = TransferHandler.COPY;
    /**
     * The list of column contained in the record
     */
    String columnNames[] = new String[0];
    
    public RecordDnDVector() {
        super();
    }

    //**************************************************************************
    //*** API
    //**************************************************************************
    /**
     * Return the index of a string in an array (Arrays.binarySearch needs to
     * be ordered, so not possible to use in current context)<p>
     * 
     * @param s
     * @param target
     * @return 
     */
    public static int indexOf(String s[], String target) {
        for (int i=0;i<s.length;i++) if (s[i].equals(target)) return i;
        return -1;
    }
    
    public void add(Record rec) {
        v.add(rec);
    }

    public boolean remove(Record rec) {
        return v.remove(rec);
    }

    public Record remove(int i) {
        return v.remove(i);
    }

    /**
     * Some additional info (like "cut" or "copy" for clipboard operation)
     */
    public void setInfo(int info) {
        this.info = info;
    }

    public void setColumnNames(String columnNames[]) {
        this.columnNames = columnNames;
    }
    
    public String[] getColumnNames() {
        return columnNames;
    }
    
    public int getInfo() {
        return info;
    }

    public int size() {
        return v.size();
    }

    public void clear() {
        v.clear();
    }

    public Record get(int i) {
        return v.get(i);
    }

    //**************************************************************************
    //*** Transferrable
    //**************************************************************************
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        DataFlavor dataflavors[] = new DataFlavor[1];
        dataflavors[0] = recordDnDVectorFlavor;
        // dataflavors[1] = DataFlavor.stringFlavor;
        return dataflavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        if (flavor.isMimeTypeEqual(recordDnDVectorFlavor.getMimeType())) {
            return true;

        } else {
            return false;
        }
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.isMimeTypeEqual(recordDnDVectorFlavor.getMimeType())) {
            return this;

        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }
}
