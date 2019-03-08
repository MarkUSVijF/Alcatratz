/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.technikumwien.bic4b18_01.server.applicationMW;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.technikumwien.bic4b18_01.common.assist.TraceHelper;
import static net.technikumwien.bic4b18_01.server.applicationMW.Middleware.state;
import net.technikumwien.bic4b18_01.server.rmi.CallBackManager;
import spread.SpreadException;

/**
 *
 * @author Florian
 * to setup all OUTGOING traffic
 */
public class Middleware {

    //state value:
    //0 - PreConstruction
    //1 - Partial Update
    //2 - Partial Update
    //3 - Update "Downloaded"
    //4 - Constructed
    public static final State state = new State();
    private static final Logger logger = Logger.getLogger(TraceHelper.getClassName());
    // read/write lock as a priority lock low/high (only one high!)
    // to prevent server changes while generating server update
    // read == local change
    // write == generating update
    public static final ReentrantReadWriteLock serverStateLock = new ReentrantReadWriteLock(true);

    //##########################################################################
    public static void construct() throws RemoteException, MalformedURLException, SpreadException {
        synchronized (state) {
            CallBackManager.construct();
            Spread.construct();
            ToClient.construct();
        }
    }

    public static void destruct() throws SpreadException {
        synchronized (state) {
            ToClient.destruct();
            Spread.destruct();
            state.setInt(0);
        }
    }

    public static class State {

        Integer value;

        State() {
            this.value = 0;
        }

        public void up() {
            synchronized (this) {
                this.value = this.value + 1;
            }
        }

        public void setInt(int value) {
            synchronized (this) {
                this.value = value;
            }
        }

        public int toInt() {
            synchronized (this) {
                return this.value;
            }
        }
    }
}
