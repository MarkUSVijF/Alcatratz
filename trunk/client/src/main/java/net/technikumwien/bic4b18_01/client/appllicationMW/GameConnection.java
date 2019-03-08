/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.technikumwien.bic4b18_01.client.appllicationMW;

import at.falb.games.alcatraz.api.Alcatraz;
import at.falb.games.alcatraz.api.Player;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.technikumwien.bic4b18_01.client.rmi.Client2Client;
import net.technikumwien.bic4b18_01.common.assist.TraceHelper;
import net.technikumwien.bic4b18_01.common.rmi.GameInfo;
import net.technikumwien.bic4b18_01.common.rmi.MoveInfo;
import net.technikumwien.bic4b18_01.common.rmi.RMI_Client2Client;
import static net.technikumwien.bic4b18_01.common.rmi.RMI_Services.client2client;

/**
 * package access only
 *
 * @author Florian
 */
public class GameConnection {

    /**
     *
     */
    private static final Logger logger = Logger.getLogger(TraceHelper.getClassName());

    public static Alcatraz gameInstance = new Alcatraz();
    public static int myPlayerID = -1;
    private static final Set<String> clientConnections = new HashSet();
    private static final Thread heart;
    private static boolean gameStarted = false;
    private static final ArrayDeque<MoveInfo> lastMoves = new ArrayDeque();
    private static int lastMoveID = 0;
    private static final ReadWriteLock lock = new ReentrantReadWriteLock();

    static {
        heart = new Thread(() -> {
            while (true) {
                lock.readLock().lock();
                try {
                    if (gameStarted) {
                        for (String connection : clientConnections) {
                            if (connection.equals(Middleware.myConnection)) {
                                continue;
                            }
                            try {

                                System.setProperty("sun.rmi.transport.connectionTimeout", 5000 + "");
                                RMI_Client2Client outgoing = (RMI_Client2Client) Naming.lookup("rmi://" + connection + "/"+client2client.toString());
                                outgoing.sendHeartbeat(Middleware.myConnection, GameConnection.getLastMoveId());
                                System.setProperty("sun.rmi.transport.connectionTimeout", 15000 + "");
                                
                            } catch (MalformedURLException ex) {
                                logger.log(Level.SEVERE, "CRITICAL", ex);
                            } catch (NotBoundException | RemoteException ex) {
                                logger.log(Level.INFO, "{0} -> {1} currently not reachable.", new Object[]{Thread.currentThread().getName(), connection});
                            }
                        }
                    }
                } finally {
                    lock.readLock().unlock();
                }
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(GameConnection.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        heart.setName("HeartBeat " + Middleware.myConnection);
        heart.setDaemon(true);
        heart.start();
    }

    public static void startGame(GameInfo gameInfo, Set<String> playerConnections, int playerID) {
        lock.writeLock().lock();
        try {
            gameInstance.disposeWindow();
            gameInstance = new Alcatraz();
            clientConnections.clear();
            lastMoves.clear();
            lastMoveID = 0;

            gameInstance.init(gameInfo.getGameSize(), playerID);
            for (Player p : gameInfo.getPlayers()) {
                gameInstance.getPlayer(p.getId()).setName(p.getName());
            }
            gameInstance.addMoveListener(new GameListener());
            //gameInstance.showWindow();
            gameInstance.start();
            clientConnections.addAll(playerConnections);
            myPlayerID = playerID;
            gameStarted = true;// == heart.isAlive();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static void sendMove(MoveInfo mv) {
        Thread sender = new Thread(() -> {
            lock.writeLock().lock();
            try {
                for (String connection : clientConnections) {
                    if (connection.equals(Middleware.myConnection)) {
                        continue;
                    }
                    try {
                        RMI_Client2Client outgoing = (RMI_Client2Client) Naming.lookup("rmi://" + connection + "/" + client2client.toString());
                        outgoing.sendMove(Middleware.myConnection, mv);
                    } catch (MalformedURLException ex) {
                        logger.log(Level.SEVERE, "CRITICAL", ex);
                    } catch (NotBoundException | RemoteException ex) {
                        logger.log(Level.INFO, "{0} -> {1} currently not reachable.", new Object[]{Thread.currentThread().getName(), connection});
                    }
                }
                lastMoves.add(mv);
                if (lastMoves.size() > clientConnections.size()) {
                    lastMoves.poll();
                }
                lastMoveID = mv.moveID;
            } finally {
                lock.writeLock().unlock();
            }
        });
        sender.setDaemon(true);
        sender.setName("Movement sender");
        sender.start();
    }

    public static void addMove(String sender, MoveInfo move) {
        int i = getLastMoveId();
        if (move.moveID <= i) {
            return;
        }
        if (move.moveID - i > 1) {
            getUpdateFrom(sender);
            return;
        }
        doMove(move);
    }

    public static void getUpdateFrom(String client) {
        logger.log(Level.INFO, "{0} -> getting update from {1}", new Object[]{Thread.currentThread().getName(), client});
        ArrayDeque<MoveInfo> update = new ArrayDeque();
        try {
            RMI_Client2Client outgoing = (RMI_Client2Client) Naming.lookup("rmi://" + client + "/"+client2client.toString());
            update = outgoing.getUpdate();
        } catch (MalformedURLException ex) {
            logger.log(Level.SEVERE, "CRITICAL", ex);
        } catch (NotBoundException | RemoteException ex) {
            logger.log(Level.INFO, "{0} -> {1} currently not reachable.", new Object[]{Thread.currentThread().getName(), client});
        }
        lock.writeLock().lock();
        logger.log(Level.INFO, "{0} -> recieved update from {1}", new Object[]{Thread.currentThread().getName(), client});
        logger.log(Level.INFO, "{0} -> update size = {1}", new Object[]{Thread.currentThread().getName(), update.size()});
        try {
            while (!update.isEmpty()) {
                MoveInfo move = update.poll();
                 logger.log(Level.INFO, "{0} -> moveID = {1} {2}", new Object[]{Thread.currentThread().getName(), move.moveID, lastMoveID});
                if (move.moveID <= lastMoveID) {
                    continue;
                }
                doMove(move);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private static void doMove(MoveInfo move) {
        logger.log(Level.INFO, "{0} -> do move {1}", new Object[]{Thread.currentThread().getName(), move.moveID});
        lock.writeLock().lock();
        if (!gameStarted | move.moveID <= getLastMoveId()) {
            return;
        }
        try {
            gameInstance.doMove(move.player, move.prisoner, move.rowOrCol, move.row, move.col);
            lastMoveID = move.moveID;
            lastMoves.add(move);
            if (lastMoves.size() > clientConnections.size()) {
                lastMoves.poll();
            }
            lastMoveID = move.moveID;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static void doFinalMoves(ArrayDeque<MoveInfo> lastMoves) {

        int i = getLastMoveId();
        while (!lastMoves.isEmpty()) {
            MoveInfo move = lastMoves.poll();
            if (move.moveID <= i) {
                continue;
            }
            doMove(move);
        }
        stopGame();
    }

    public static ArrayDeque<MoveInfo> getLastMoves() {
        ArrayDeque<MoveInfo> result = new ArrayDeque();
        lock.readLock().lock();
        try {
            result=lastMoves.clone();
        } finally {
            lock.readLock().unlock();
        }
        return result;
    }

    public static boolean stopGame() {

        lock.writeLock().lock();
        try {
            gameInstance = new Alcatraz();
            gameStarted = false;
            myPlayerID = -1;
        } finally {
            lock.writeLock().unlock();
        }
        return true;
    }

    public static int getLastMoveId() {
        lock.readLock().lock();
        try {
            if (!gameStarted) {
                return Integer.MAX_VALUE;
            }
            return lastMoveID;
        } finally {
            lock.readLock().unlock();
        }
    }

    //##########################################################################
    static void construct(String connection) throws RemoteException, MalformedURLException {

        // ### ToClient.Data.createInstance(); at game start
        //bind RMI
        String ip = connection.substring(0, connection.indexOf(':'));
        int port = Integer.valueOf(connection.substring(connection.indexOf(':') + 1));
        System.setProperty("java.rmi.server.hostname", ip);
        RMI_Client2Client client2clientSkeleton = new Client2Client();
        UnicastRemoteObject.exportObject(client2clientSkeleton, port);
        Naming.rebind("rmi://" + connection + "/" + client2client.toString(), client2clientSkeleton);
        System.clearProperty("java.rmi.server.hostname");
        logger.log(Level.INFO, "{0} -> {1} bound to {2}", new Object[]{Thread.currentThread().getName(), client2client, "" + port});

    }

    static void destruct(String connection) {

        try {
            Naming.unbind("rmi://" + connection + "/" + client2client.toString());
        } catch (RemoteException | NotBoundException | MalformedURLException ex) {
            logger.log(Level.WARNING, "{0} -> could not unbind rmi service {1}", new Object[]{Thread.currentThread().getName(), client2client.toString()});
        }
    }
}
