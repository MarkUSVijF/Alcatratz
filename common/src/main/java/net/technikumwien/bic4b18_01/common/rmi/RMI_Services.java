/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.technikumwien.bic4b18_01.common.rmi;

/**
 *
 * @author Florian
 */
public enum RMI_Services {
    server2client("AlcatrazServer2Client"),
    client2server("AlcatrazLobbyService"),
    client2client("AlcatrazGameService");

    private final String name;

    RMI_Services() {
        this.name = this.name();
    }

    RMI_Services(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
