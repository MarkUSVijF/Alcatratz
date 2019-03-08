/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.technikumwien.bic4b18_01.server.rmi;

import net.technikumwien.bic4b18_01.server.applicationMW.Spread;
import java.io.Serializable;
import java.lang.invoke.WrongMethodTypeException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.technikumwien.bic4b18_01.common.exception.ConnectionException;
import net.technikumwien.bic4b18_01.common.exception.GameException;
import net.technikumwien.bic4b18_01.server.applicationMW.Middleware;
import net.technikumwien.bic4b18_01.server.common.Response;
import spread.SpreadException;
import spread.SpreadMessage;

/**
 * collection of server data
 *
 * @author Florian
 */
public class ClientRequests {

    private static final Logger logger = Logger.getLogger(ClientRequests.class.getName());
    private static final Map<String, Response> client_responses = new HashMap(); //clientConnection -> response
    private static final Map<Long, Thread> sleeping_threads = new HashMap();//RMI_Client2Server Threads
    
    public static SpreadMessage generateUpdate(SpreadMessage sm) {
        Middleware.serverStateLock.writeLock().lock();
        try {
            sm.digest(new HashMap(client_responses));
        } catch (SpreadException ex) {
            logger.log(Level.SEVERE, null, ex);
        } finally {
            Middleware.serverStateLock.writeLock().unlock();
        }
        return sm;
    }

    public static void update(Map<String, Response> client_responsesUpdate) {
        logger.log(Level.INFO, "{0} -> update", new Object[]{Thread.currentThread().getName()});
        Middleware.serverStateLock.writeLock().lock();
        try {
            client_responses.clear();
            client_responses.putAll(client_responsesUpdate);
        } finally {
            Middleware.serverStateLock.writeLock().unlock();
        }
    }
    //##########################################################################
    //client requests:
    private static void addResponse(String clientConnection, Response response) {

        Middleware.serverStateLock.readLock().lock();
        try {
            synchronized (client_responses) {
                client_responses.put(clientConnection, response);
            }
        } finally {
            Middleware.serverStateLock.readLock().unlock();
        }
    }

    //##########################################################################
    //thread management
    private static void selfSleep() throws InterruptedException {

        //Die ID des aktuellen Threads wird gespeichert
        //Dies wird benötigt damit der Thread des Spread-Listeners uns wieder aufwecken kann (Scheduling)
        synchronized (ClientRequests.sleeping_threads) {
            ClientRequests.sleeping_threads.put(Thread.currentThread().getId(), Thread.currentThread());
        }
        //Der Thread wird für 1 Minute angehalten
        Thread.sleep(60000);// timeout 1min
    }

    private static void selfWakeup() {

        synchronized (ClientRequests.sleeping_threads) {
            ClientRequests.sleeping_threads.remove(Thread.currentThread().getId());
        }
    }

    public static void wakeupThread(long threadID) {

        Thread sleepingThread;
        synchronized (ClientRequests.sleeping_threads) {
            sleepingThread = ClientRequests.sleeping_threads.remove(threadID);
        }
        if (sleepingThread != null && sleepingThread.isAlive()) {
            sleepingThread.interrupt();
        }
    }
    //##########################################################################

    /**
     * RMI side
     *
     * @param clientConnection
     * @param requestNr
     * @param requestMethod
     * @return
     * @throws GameException
     * @throws ConnectionException
     * @throws NoSuchElementException
     * @throws WrongMethodTypeException
     */
    public static Serializable getResponse(final String clientConnection, final int requestNr, final String requestMethod) throws GameException, ConnectionException, NoSuchElementException, WrongMethodTypeException {
        Response response;
        Middleware.serverStateLock.readLock().lock();
        try {
            synchronized (client_responses) {
                response = client_responses.get(clientConnection);
                if (response != null && requestMethod != null) {
                    if (response.reqID == requestNr) {
                        // add more throw clauses
                        if (!requestMethod.equals(response.request)) {
                            throw new WrongMethodTypeException("request method is not as expected.");
                        }
                        if ("GameException".equals(response.type)) {
                            throw (GameException) response.value;
                        }
                        if ("ConnectionException".equals(response.type)) {
                            throw (ConnectionException) response.value;
                        }
                        return response.value; // may be null!!!!
                    }
                    if (requestMethod.equals("registerClient") && response.reqID != requestNr) {
                        throw new ConnectionException("Port already in construction by another Client.");
                    }
                }
                throw new NoSuchElementException();
            }
        } finally {
            Middleware.serverStateLock.readLock().unlock();
        }
    }

    /**
     * RMI side
     *
     * @param clientConnection
     * @param requestNr
     * @param request
     * @return
     * @throws GameException
     * @throws ConnectionException
     * @throws RemoteException
     */
    public static Serializable awaitResponse(String clientConnection, int requestNr, String request) throws GameException, ConnectionException, RemoteException {

        //Die Anfrage wird an Spread weitergeleitet
        try {
            //Der Thread wird für 5 Minuten angehalten
            //wir wollen geweckt werden
            selfSleep();
        } catch (InterruptedException wakeup) {
            try {
                // throws GameException, ConnectionException others are cought
                return ClientRequests.getResponse(clientConnection, requestNr, request);
            } catch (NoSuchElementException ex) {
                logger.log(Level.WARNING, "{0} -> Request for {1} with NR {2} could not be finished.", new Object[]{Thread.currentThread().getName(), clientConnection, requestNr});
            } catch (WrongMethodTypeException ex) {
                logger.log(Level.WARNING, "{0} -> Request for {1} with NR {2} has unexpected Type", new Object[]{Thread.currentThread().getName(), clientConnection, requestNr});
            }
            throw new RemoteException("request could not be finished.");
        }
        selfWakeup();
        throw new RemoteException("timed out after 5 minutes");
    }

    /**
     * Spread side
     *
     * @param threadID
     * @param connection
     * @param sender
     * @param response
     */
    public static void returnResponse(long threadID, String connection, String sender, Response response) {

        ClientRequests.addResponse(connection, response);
        if (sender.contains(Spread.getServerID().toString())) {
            ClientRequests.wakeupThread(threadID);
        }
    }

    public static boolean existsClient(String connection) {
        Middleware.serverStateLock.readLock().lock();
        try {
            synchronized (client_responses) {
                return client_responses.keySet().contains(connection) && client_responses.get(connection).reqID != -1;
            }
        } finally {
            Middleware.serverStateLock.readLock().unlock();
        }
    }

    public static void unregisterClient(final String connection) {
        Middleware.serverStateLock.readLock().lock();
        try {
            synchronized (client_responses) {
                client_responses.remove(connection);
            }
        } finally {
            Middleware.serverStateLock.readLock().unlock();
        }
    }
}
