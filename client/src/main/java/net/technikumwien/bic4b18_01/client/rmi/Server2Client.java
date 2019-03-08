/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.technikumwien.bic4b18_01.client.rmi;

import net.technikumwien.bic4b18_01.client.appllicationMW.GameConnection;
import java.rmi.RemoteException;
import java.util.ArrayDeque;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.technikumwien.bic4b18_01.client.local.GUI;
import net.technikumwien.bic4b18_01.common.assist.TraceHelper;
import net.technikumwien.bic4b18_01.common.rmi.GameInfo;
import net.technikumwien.bic4b18_01.common.rmi.MoveInfo;
import net.technikumwien.bic4b18_01.common.rmi.RMI_Server2Client;

/**
 *
 * @author Florian
 */
public class Server2Client implements RMI_Server2Client {

    private static final Logger logger = Logger.getLogger(TraceHelper.getClassName());
    private final GUI gui;

    public Server2Client(GUI gui){
        this.gui=gui;
    }
    public Server2Client(){
        this.gui=null;
    }
    @Override
    public void playerChange(GameInfo game) throws RemoteException {
        logger.log(Level.INFO, "{0} -> {1}.{2} - enter", new Object[]{Thread.currentThread().getName(), TraceHelper.getClassName(), TraceHelper.getMethodName()});
        if(gui!=null){
            gui.refreshGame(game);
        } else {
            logger.log(Level.SEVERE, "{0} -> {1}.{2} - Not supported yet.", new Object[]{Thread.currentThread().getName(), TraceHelper.getClassName(), TraceHelper.getMethodName()});
        }
        logger.log(Level.INFO, "{0} -> {1}.{2} - leave", new Object[]{Thread.currentThread().getName(), TraceHelper.getClassName(), TraceHelper.getMethodName()});
    }

    @Override
    public void gameStart(GameInfo game, Set<String> playerConnections, int playerID) throws RemoteException {
        logger.log(Level.INFO, "{0} -> {1}.{2} - enter", new Object[]{Thread.currentThread().getName(), TraceHelper.getClassName(), TraceHelper.getMethodName()});
        
        GameConnection.startGame(game, playerConnections, playerID);
        if(gui!=null){
            gui.startGame(game);
        } else {
            logger.log(Level.SEVERE, "{0} -> {1}.{2} - Not supported yet.", new Object[]{Thread.currentThread().getName(), TraceHelper.getClassName(), TraceHelper.getMethodName()});
        }
        logger.log(Level.INFO, "{0} -> {1}.{2} - leave", new Object[]{Thread.currentThread().getName(), TraceHelper.getClassName(), TraceHelper.getMethodName()});
    }

    @Override
    public void gameEnd(ArrayDeque<MoveInfo> lastMoves) throws RemoteException {
        logger.log(Level.INFO, "{0} -> {1}.{2} - enter", new Object[]{Thread.currentThread().getName(), TraceHelper.getClassName(), TraceHelper.getMethodName()});
        GameConnection.doFinalMoves(lastMoves);
        if(gui!=null){
            logger.log(Level.INFO, "{0} -> gui change", new Object[]{Thread.currentThread().getName()});
            gui.finishGame();
        } else {
            logger.log(Level.SEVERE, "{0} -> {1}.{2} - Not supported yet.", new Object[]{Thread.currentThread().getName(), TraceHelper.getClassName(), TraceHelper.getMethodName()});
        }
        logger.log(Level.INFO, "{0} -> {1}.{2} - leave", new Object[]{Thread.currentThread().getName(), TraceHelper.getClassName(), TraceHelper.getMethodName()});
    }
}
