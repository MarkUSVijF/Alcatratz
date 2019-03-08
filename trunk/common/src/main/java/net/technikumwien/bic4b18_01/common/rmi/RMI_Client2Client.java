package net.technikumwien.bic4b18_01.common.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayDeque;

public interface RMI_Client2Client extends Remote {

    public void sendMove(String sender, MoveInfo move)
            throws RemoteException;

    public void sendHeartbeat(String sender, int moveID)
            throws RemoteException;

    public ArrayDeque<MoveInfo> getUpdate()
            throws RemoteException;
}
