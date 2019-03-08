/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.technikumwien.bic4b18_01.spread.local;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.technikumwien.bic4b18_01.common.assist.RUID;
import static net.technikumwien.bic4b18_01.common.assist.TraceHelper.getTrunk;
import net.technikumwien.bic4b18_01.common.rmi.Server;
import net.technikumwien.bic4b18_01.common.settings.LoggerSettings;
import static net.technikumwien.bic4b18_01.common.assist.LocalNetwork.getIPv4;

/**
 *
 * @author Florian
 */
public class ConfigSetup {

    private static final Logger logger = Logger.getLogger(ConfigSetup.class.getName());

    /**
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        create();
    }

    /**
     * only run after there is a change in either:
     * -) common.settings.server.ip
     * -) spread daemon localtions (ip)
     */
    public static void create() {
        try {
            LoggerSettings.initLogging();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "{0} -> logger could not be started", Thread.currentThread().getName());
        }
        File trunk = getTrunk();
        Map<String, Set<String>> spreadAddresses = Server.network();
        Set<String> myIPs = getIPv4();
        byte[] strToBytes;
        Path path;

        // generating content of spread.conf
        String content = "";
        for (Entry<String, Set<String>> subNetIPs : spreadAddresses.entrySet()) {
            content += "Spread_Segment  " + subNetIPs.getKey() + " {\n\n";
            for (String ip : subNetIPs.getValue()) {
                String name = RUID.new_9().toString();
                content += "	" + name + "		" + ip + "\n";
            }
            content += "}\n";
        }
        //read
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(trunk.getPath() + "\\spread\\src\\main\\java\\net\\technikumwien\\bic4b18_01\\spread\\settings\\spreadFlags.txt"));
        } catch (IOException ex) {
            logger.log(Level.WARNING, "{0} -> !!! spreadFlags.txt UNREADABLE -> standart values used !!!", Thread.currentThread().getName());
            lines = new ArrayList(Arrays.asList(Arrays.toString(new String[]{"DebugFlags = { PRINT EXIT SESSION GROUPS }", "EventPriority =  INFO", "EventTimeStamp = \"[%y.%m.%d %H:%M-%S]\""})));
        }

        content = lines.stream().map((s) -> s + "\n").reduce(content, String::concat);
        try {
            // creating spread.conf
            path = Paths.get(trunk.getPath() + "\\spread\\src\\main\\java\\net\\technikumwien\\bic4b18_01\\spread\\local\\spread.conf");
            strToBytes = content.getBytes();
            Files.write(path, strToBytes, CREATE, TRUNCATE_EXISTING, WRITE);
            logger.log(Level.INFO, "{0} -> spread.config successfully created", Thread.currentThread().getName());

            // cleansing daemon.ip
            path = Paths.get(trunk.getPath() + "\\spread\\src\\main\\java\\net\\technikumwien\\bic4b18_01\\spread\\local\\daemon.ip");
            strToBytes = "".getBytes();
            Files.write(path, strToBytes, CREATE, TRUNCATE_EXISTING, WRITE);
            logger.log(Level.INFO, "{0} -> daemon.ip successfully created", Thread.currentThread().getName());

        } catch (IOException ex) {
            logger.log(Level.SEVERE, "{0} -> something went very wrong", Thread.currentThread().getName());
        }
    }
}
