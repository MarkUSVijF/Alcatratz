/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.technikumwien.bic4b18_01.common.rmi;

import at.falb.games.alcatraz.api.Player;
import at.falb.games.alcatraz.api.Prisoner;
import java.io.Serializable;

/**
 *
 * @author Florian
 */
public class MoveInfo implements Serializable {

    public final int moveID;
    public final Player player;
    public final Prisoner prisoner;
    public final int rowOrCol;
    public final int row;
    public final int col;
   
    public MoveInfo(int moveID, Player player, Prisoner prisoner, int rowOrCol, int row, int col){
        this.moveID = moveID;
        this.player = player;
        this.prisoner = prisoner;
        this.rowOrCol = rowOrCol;
        this.row = row;
        this.col = col;
    }
}
