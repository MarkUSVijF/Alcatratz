/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.technikumwien.bic4b18_01.server.spread;

import net.technikumwien.bic4b18_01.server.applicationMW.Spread;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.technikumwien.bic4b18_01.common.assist.TraceHelper;
import net.technikumwien.bic4b18_01.common.exception.GameException;
import net.technikumwien.bic4b18_01.common.rmi.MoveInfo;
import net.technikumwien.bic4b18_01.server.common.Game;
import net.technikumwien.bic4b18_01.server.common.GameList;
import net.technikumwien.bic4b18_01.server.rmi.CallBackManager;
import spread.SpreadException;
import spread.SpreadMessage;

/**
 * Server collective Notifications
 * @author Florian
 */
public class AsynchronSpreadMessage {

    private static final Logger logger = Logger.getLogger(TraceHelper.getClassName());
    private static final String GROUPNAME = "asynchronSMGroup";

    private static void deliver(SpreadMessage sm) throws SpreadException {

        sm.setSafe();
        sm.addGroup(GROUPNAME);
        Spread.connection.multicast(sm);
    }

    public static String getGroupName() {
        return GROUPNAME;
    }

    public static void process(SpreadMessage sm) throws SpreadException {
        switch (sm.getType()) {
            case 1:
                processPlayerChangeMessage(sm);
                break;
            case 2:
                processGameStartMessage(sm);
                break;
            case 3:
                processGameEndMessage(sm);
                break;
            case 9:
                processForceLeaveGameMessage(sm);
                break;
            case Short.MAX_VALUE:
                processTakeOverMessage(sm);
                break;
            default:
                logger.log(Level.INFO, "{0} -> #{1} received unknown asynchron message from {2}.", new Object[]{Thread.currentThread().getName(), Spread.getServerID().toString(), sm.getSender().toString()});
        }
    }
    
    //##########################################################################
    public static void sendPlayerChange(String server, HashSet<String> recipients, Game game) {
        SpreadMessage message = new SpreadMessage();
        message.setType((short) 1);
        try {
            message.digest(server);
            message.digest(recipients);
            message.digest(game);
            logger.log(Level.INFO, "{0} -> sendPlayerChange", new Object[]{Thread.currentThread().getName()});
            deliver(message);
        } catch (SpreadException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public static void sendGameStart(String server, HashSet<String> recipients, Game game) {
        SpreadMessage message = new SpreadMessage();
        message.setType((short) 2);
        try {
            message.digest(server);
            message.digest(recipients);
            message.digest(game);
            logger.log(Level.INFO, "{0} -> sendGameStart", new Object[]{Thread.currentThread().getName()});
            deliver(message);
        } catch (SpreadException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public static void sendGameEnd(String server, HashSet<String> recipients, Game game, ArrayDeque<MoveInfo> moves) {
        SpreadMessage message = new SpreadMessage();
        message.setType((short) 3);
        try {
            message.digest(server);
            message.digest(recipients);
            message.digest(game);
            message.digest(moves);
            logger.log(Level.INFO, "{0} -> sendGameEnd", new Object[]{Thread.currentThread().getName()});
            deliver(message);
        } catch (SpreadException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public static void sendForceLeaveGame(String connection, int gameID) {
        SpreadMessage message = new SpreadMessage();
        message.setType((short) 9);
        try {
            message.digest(connection);
            message.digest(gameID);
            logger.log(Level.INFO, "{0} -> sendForceLeaveGame", new Object[]{Thread.currentThread().getName()});
            deliver(message);
        } catch (SpreadException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    //##########################################################################
    private static void processPlayerChangeMessage(SpreadMessage sm) throws SpreadException {
        List digest = sm.getDigest();
        String server = (String) digest.get(0);
        HashSet<String> recipients = (HashSet<String>) digest.get(1);
        Game game = (Game) digest.get(2);
        logger.log(Level.INFO, "{0} -> processPlayerChangeMessage", new Object[]{Thread.currentThread().getName()});
        
        CallBackManager.playerChange(server, recipients, game);
    }

    private static void processGameStartMessage(SpreadMessage sm) throws SpreadException {
        List digest = sm.getDigest();
        String server = (String) digest.get(0);
        HashSet<String> recipients = (HashSet<String>) digest.get(1);
        Game game = (Game) digest.get(2);
        logger.log(Level.INFO, "{0} -> processGameStartMessage", new Object[]{Thread.currentThread().getName()});
        
        CallBackManager.gameStart(server, recipients, game);
    }

    private static void processGameEndMessage(SpreadMessage sm) throws SpreadException {
        List digest = sm.getDigest();
        String server = (String) digest.get(0);
        HashSet<String> recipients = (HashSet<String>) digest.get(1);
        Game game = (Game) digest.get(2);
        ArrayDeque<MoveInfo> moves = (ArrayDeque<MoveInfo>) digest.get(3);
        logger.log(Level.INFO, "{0} -> processGameEndMessage", new Object[]{Thread.currentThread().getName()});
        
        CallBackManager.gameEnd(server, recipients, game, moves);
    }

    private static void processForceLeaveGameMessage(SpreadMessage sm) throws SpreadException {
        //only in closed games
        List digest = sm.getDigest();
        String clientConnection = (String) digest.get(0);
        int gameID = (int) digest.get(1);
        try {
            Game game = GameList.removePlayerFromGame(clientConnection,gameID);
            if(game!=null){
                logger.log(Level.INFO, "{0} -> force leave {1} from {2}.", new Object[]{Thread.currentThread().getName(), clientConnection, game.getGameID()});
            }
        } catch (GameException ex) {
            logger.log(Level.SEVERE, "{0} -> player in active game. [HOW?]", Thread.currentThread().getName());
        }
    }
    
    //##########################################################################
    public static void sendTakeOverMessage(String spreadMember) {
        logger.log(Level.INFO, "{0} -> taking over for {1}.", new Object[]{Thread.currentThread().getName(), spreadMember});
        SpreadMessage message = new SpreadMessage();
        message.setType((short) Short.MAX_VALUE);
        try {
            message.digest(spreadMember);
            deliver(message);
        } catch (SpreadException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private static void processTakeOverMessage(SpreadMessage sm) throws SpreadException {
        List digest = sm.getDigest();
        String spreadMember = (String) digest.get(0);
        logger.log(Level.INFO, "{0} -> {1} is taking over for {2}.", new Object[]{Thread.currentThread().getName(), sm.getSender().toString(), spreadMember});
        CallBackManager.takeOverFor(sm.getSender().toString(), spreadMember);
    }
}
