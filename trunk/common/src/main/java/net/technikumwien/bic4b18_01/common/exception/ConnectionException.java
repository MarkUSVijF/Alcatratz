/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.technikumwien.bic4b18_01.common.exception;

/**
 *
 * @author Florian
 */
public class ConnectionException extends Exception {

    /**
     * Creates a new instance of <code>ConnectionException</code> without detail
     * message.
     */
    public ConnectionException() {
    }

    /**
     * Constructs an instance of <code>ConnectionException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public ConnectionException(String msg) {
        super(msg);
    }
}
