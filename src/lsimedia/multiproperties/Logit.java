/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lsimedia.multiproperties;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author sbodmer
 */
public class Logit {

    SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");
    ArrayList<String[]> logs = new ArrayList<>();

    public Logit() {

    }

    public void log(String kind, String message, Object arg) {
        logs.add(new String[]{tf.format(new Date()), kind, message});
    }

    /**
     * Return the log line (date, kind, message) or null if none
     *
     * @return
     */
    public String[] fetchLog() {
        if (logs.size() > 0) return logs.remove(0);
        return null;
    }
}
