/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lsimedia.multiproperties;

/**
 *
 * @author sbodmer
 */
public interface LogitListener {
    public void logitLineLogged(final String kind, final String message, final Object arg);
}
