/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lsimedia.multiproperties;

/**
 * Simple container for the logs service
 * 
 * @author sbodmer
 */
public class Logit {

    
    LogitListener listener = null;
    
    public Logit(LogitListener listener) {
        this.listener = listener;
    }

    public void log(final String kind, final String message, final Object arg) {
        if (listener != null) listener.logitLineLogged(kind, message, arg);
        
    }

}
