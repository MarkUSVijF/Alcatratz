/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.technikumwien.bic4b18_01.client.appllicationMW;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.MessageFormat;
import java.util.ArrayDeque;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.technikumwien.bic4b18_01.client.rmi.Server2Client;
import net.technikumwien.bic4b18_01.common.assist.TraceHelper;
import net.technikumwien.bic4b18_01.common.exception.ConnectionException;
import net.technikumwien.bic4b18_01.common.exception.GameException;
import net.technikumwien.bic4b18_01.common.rmi.GameInfo;
import net.technikumwien.bic4b18_01.common.rmi.MoveInfo;
import net.technikumwien.bic4b18_01.common.rmi.RMI_Client2Server;
import net.technikumwien.bic4b18_01.common.rmi.RMI_Server2Client;
import net.technikumwien.bic4b18_01.common.rmi.RMI_Services;
import net.technikumwien.bic4b18_01.common.rmi.Server;

/**
 * package access only
 *
 * @author Florian
 */
public class ToServer {

    /**
     *
     */
    private static final Logger logger = Logger.getLogger(TraceHelper.getClassName());

    static String construct(RMI_Server2Client server2clientSkeleton) {

        logger.log(Level.INFO, "{0} -> creating client-constructtion UUID", Thread.currentThread().getName());
        UUID clientCUID = UUID.randomUUID(); //

        Registry localRegistry;
        String connection;
        int startPort = (new Random()).nextInt(Server.maxPort() - Server.minPort() + 1) + Server.minPort();
        int port=startPort;
        int requestNr = 0;
        //get a stub
        while (true) {
            if(port>Server.maxPort()){
                port=Server.minPort();
            }
            try {
                //register local registry
                localRegistry = LocateRegistry.createRegistry(port);
                logger.log(Level.INFO, "{0} -> localRegistry created on {1}", new Object[]{Thread.currentThread().getName(), MessageFormat.format("{0,number,#}", port)});
            } catch (RemoteException ex1) {
                //could not create registry on port
                logger.log(Level.SEVERE, "{0} -> could not create local registry on port: {1}", new Object[]{Thread.currentThread().getName(), MessageFormat.format("{0,number,#}", port)});
                port++;
                if(port==startPort) noFreePort();
                continue;
            }
            try {
                connection = registerClient(clientCUID,port);
                logger.log(Level.INFO, "{0} -> global portID is {1}", new Object[]{Thread.currentThread().getName(), MessageFormat.format("{0,number,#}", port)});
            } catch (ConnectionException ex) {
                
                try {
                    UnicastRemoteObject.unexportObject(localRegistry, true);
                } catch (NoSuchObjectException ex1) {
                    logger.log(Level.INFO, "{0} -> localRegistry already unexported", Thread.currentThread().getName());
                }
                port++;
                if(port==startPort) noFreePort();
                continue;
            }
            try {
                //bind RMI
                System.setProperty("java.rmi.server.hostname", connection.substring(0,connection.indexOf(':')));
                UnicastRemoteObject.exportObject(server2clientSkeleton, port);
                Naming.rebind("rmi://" + connection + "/" + RMI_Services.server2client.toString(), server2clientSkeleton);
                System.clearProperty("java.rmi.server.hostname");
                logger.log(Level.INFO, "{0} bound to {1}", new Object[]{RMI_Services.server2client, "" + connection});
            } catch (RemoteException | MalformedURLException ex1) {
                //shouldn't reach
                logger.log(Level.SEVERE, "{0} -> local registry error [HOW?]", Thread.currentThread().getName());
                disconnect();
                port++;
                if(port==startPort) noFreePort();
                try {
                    UnicastRemoteObject.unexportObject(localRegistry, true);
                } catch (NoSuchObjectException ex) {
                    logger.log(Level.INFO, "{0} -> localRegistry already unexported", Thread.currentThread().getName());
                }
                //-> need to free port too. no idea how tho
                continue;
            }
            requestNr++;
            break;
        }

        //save everything in DATA
        Data.setPort(port);
        Data.setRequestNr(requestNr);

        return connection;
    }

    static void destruct() {

        // server connection
        disconnect();
        try {
            Naming.unbind("rmi://" + Middleware.myConnection + "/" + RMI_Services.server2client.toString());
        } catch (RemoteException | NotBoundException | MalformedURLException ex) {
            logger.log(Level.WARNING, "{0} -> could not unbind rmi service {1}", new Object[]{Thread.currentThread().getName(), RMI_Services.server2client.toString()});
        }
    }

    //######################################################################

    private static void noFreePort() {
        logger.log(Level.SEVERE, "{0} -> no free port for your IP", Thread.currentThread().getName());
        try {
            Thread.sleep(60000);// timeout 1min
        } catch (InterruptedException ex2) {
            //wakeUP
        }
    }

    //######################################################################
    //player driven functionalities
    public static Set<GameInfo> getGames(){
        Set<GameInfo> games = null;
        int port = Data.getPort();
        int requestNr = Data.getRequestNr();
        while (true) {
            try {
                games = Data.getStub().getGames(port);
                break;
            } catch (RemoteException ex1) {
                //server gone offline (no connection to current server)
                logger.log(Level.WARNING, "{0} -> no connection to current server - looking for different one", Thread.currentThread().getName());
                generateStub();
            } catch (ConnectionException ex) {
                logger.log(Level.SEVERE, "{0} -> {1}", new Object[]{Thread.currentThread(), ex.getMessage()});
            }
        }
        Data.setRequestNr(requestNr+1);
        return games;
    }
    
    public static GameInfo getGameInfo(int gameID)
            throws GameException {
        GameInfo game = null;
        int port = Data.getPort();
        int requestNr = Data.getRequestNr();
        while (true) {
            try {
                game = Data.getStub().getGameInfo(port, gameID);
                break;
            } catch (RemoteException ex1) {
                //server gone offline (no connection to current server)
                logger.log(Level.WARNING, "{0} -> no connection to current server -> looking for different one", Thread.currentThread().getName());
                generateStub();
            } catch (ConnectionException ex) {
                logger.log(Level.SEVERE, "{0} -> {1}", new Object[]{Thread.currentThread(), ex.getMessage()});
            }
        }
        Data.setRequestNr(requestNr+1);
        return game;
    }

    public static GameInfo hostGame(int players, String playerName)
            throws GameException {//throws exception: to few/many players
        GameInfo gameInfo = null;
        int port = Data.getPort();
        int requestNr = Data.getRequestNr();
        while (true) {
            try {
                //Der tatsächliche RMI-Aufruf findet hier statt
                gameInfo = Data.getStub().hostGame(port, requestNr, players, playerName);
                break;
            } catch (RemoteException ex1) {
                //server gone offline (no connection to current server)
                logger.log(Level.WARNING, "{0} -> no connection to current server -> looking for different one", Thread.currentThread().getName());
                generateStub();
            } catch (ConnectionException ex) {
                logger.log(Level.SEVERE, "{0} -> {1}", new Object[]{Thread.currentThread(), ex.getMessage()});
                requestNr++;
            }
        }
        Data.setRequestNr(requestNr+1);
        return gameInfo;
    }

    public static GameInfo joinGame(int gameID, String playerName)
            throws GameException {//throws exception: already full, no such game, player name already used

        GameInfo gameInfo = null;
        int port = Data.getPort();
        int requestNr = Data.getRequestNr();
        while (true) {
            try {
                gameInfo = Data.getStub().joinGame(port, requestNr, gameID, playerName);
                break;
            } catch (RemoteException ex1) {
                //server gone offline (no connection to current server)
                logger.log(Level.WARNING, "{0} -> no connection to current server -> looking for different one", Thread.currentThread().getName());
                generateStub();
            } catch (ConnectionException ex) {
                logger.log(Level.SEVERE, "{0} -> {1}", new Object[]{Thread.currentThread(), ex.getMessage()});
                requestNr++;
            }
        }
        Data.setRequestNr(requestNr+1);
        return gameInfo;
    }

    public static void leaveGame()
            throws GameException {//throws exception: already full, no such game

        int port = Data.getPort();
        int requestNr = Data.getRequestNr();
        while (true) {
            try {
                Data.getStub().leaveGame(port, requestNr);
                break;
            } catch (RemoteException ex1) {
                //server gone offline (no connection to current server)
                logger.log(Level.WARNING, "{0} -> no connection to current server -> looking for different one", Thread.currentThread().getName());
                generateStub();
            } catch (ConnectionException ex) {
                logger.log(Level.SEVERE, "{0} -> {1}", new Object[]{Thread.currentThread(), ex.getMessage()});
                requestNr++;
            }
        }
        Data.setRequestNr(requestNr+1);
    }

    public static void closeApp() throws GameException {
        int port = Data.getPort();
        int requestNr = Data.getRequestNr();
        while (true) {
            try {
                Data.getStub().unregisterClient(port, requestNr);
                break;
            } catch (RemoteException ex1) {
                //server gone offline (no connection to current server)
                logger.log(Level.WARNING, "{0} -> no connection to current server -> looking for different one", Thread.currentThread().getName());
                generateStub();
            } catch (ConnectionException ex) {
                logger.log(Level.SEVERE, "{0} -> {1}", new Object[]{Thread.currentThread(), ex.getMessage()});
                requestNr++;
            }
        }
        Data.setRequestNr(requestNr+1);
    }

    //######################################################################
    // automatic functionalities
    public static void endGame(ArrayDeque<MoveInfo> lastMoves)
            throws GameException {
        
        int port = Data.getPort();
        int requestNr = Data.getRequestNr();
        while (true) {
            try {
                Data.getStub().endGame(port, requestNr, lastMoves);
            } catch (RemoteException ex) {
                //server gone offline (no connection to current server)
                logger.log(Level.WARNING, "{0} -> no connection to current server -> looking for different one", Thread.currentThread().getName());
                generateStub();
            } catch (ConnectionException ex) {
                logger.log(Level.SEVERE, "{0} -> {1}", new Object[]{Thread.currentThread(), ex.getMessage()});
                requestNr++;
            }
            break;
        }
        Data.setRequestNr(requestNr+1);
    }

    //######################################################################
    private static RMI_Client2Server generateStub() {

        logger.log(Level.INFO, "{0} -> generating stub", Thread.currentThread().getName());
        RMI_Client2Server stub = null;
        while (true) {
            for (String serverIP : Server.adresses()) {
                logger.log(Level.INFO, "{0} -> trying stub for {1}", new Object[]{Thread.currentThread().getName(), serverIP});
                try {
                    stub = (RMI_Client2Server) Naming.lookup("rmi://" + serverIP + "/" + RMI_Services.client2server.toString()+"_"+serverIP);
                    logger.log(Level.INFO, "{0} -> stub to {1} generated", new Object[]{Thread.currentThread().getName(), serverIP});
                    break;
                } catch (NotBoundException | MalformedURLException | RemoteException ex) {
                    logger.log(Level.INFO, "{0} -> can not generate stub to {1} at this time", new Object[]{Thread.currentThread().getName(), serverIP});
                    logger.log(Level.FINEST, Thread.currentThread().getName()+" -> detailed:", ex);
                }
            }
            if (stub == null) {
                //no server online (client offline)
                logger.log(Level.SEVERE, "{0} -> no connection to any server -> retrying later", Thread.currentThread().getName());
                try {
                    Thread.sleep(60000);// timeout 1min
                } catch (InterruptedException ex3) {
                    //wakeUP
                }
                continue;
            }
            break;
        }
        Data.setStub(stub);
        return stub;
    }

    /**
     *
     * @param stub
     * @param port
     * @return
     * @throws RemoteException
     */
    private static String registerClient(UUID cUID, int port)
            throws ConnectionException {
        
        while (true) {
            try {
                logger.log(Level.INFO, "{0} -> trying to get port {1} in servercollective", new Object[]{Thread.currentThread().getName(), MessageFormat.format("{0,number,#}", port)});
                String connection = Data.getStub().registerClient(cUID, port); //throws exception: port taken
                logger.log(Level.INFO, "{0} -> got connection on port: {1}", new Object[]{Thread.currentThread().getName(), MessageFormat.format("{0,number,#}", port)});
                return connection;
            } catch (RemoteException ex) {
                logger.log(Level.INFO, "{0} -> stub is unreachable", Thread.currentThread().getName());
                generateStub();
            }
        }
    }

    private static String disconnect() {

        int port = Data.getPort();
        int requestNr = Data.getRequestNr();
        while (true) {
            try {
                String connection = Data.getStub().unregisterClient(port, requestNr);
                logger.log(Level.INFO, "{0} -> disconnected from sever", Thread.currentThread().getName());
                return connection;
            } catch (RemoteException | ConnectionException | GameException ex) {
                logger.log(Level.SEVERE, "{0} -> could not disconnect from server", Thread.currentThread().getName());
                generateStub();
            }
        }
    }

    //######################################################################
    /**
     * used for client 2 server connections outside a game
     */
    static class Data { //singleton

        private static RMI_Client2Server stub;
        private static int port;
        private static int requestNr;

        static{
            generateStub();
            port = Server.maxPort();
            requestNr = 0;
        }

        //##################################################################

        //##################################################################
        public static RMI_Client2Server getStub() {
            return Data.stub;
        }

        public static int getPort() {
            return Data.port;
        }

        public static int getRequestNr() {
            return Data.requestNr;
        }

        public static void setStub(RMI_Client2Server stub) {
            Data.stub = stub;
        }

        public static void setPort(int port) {
            Data.port = port;
        }

        public static void setRequestNr(int requestNr) {
            Data.requestNr = requestNr;
        }
    }
}
