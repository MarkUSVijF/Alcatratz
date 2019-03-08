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
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import static net.technikumwien.bic4b18_01.common.assist.TraceHelper.getTrunk;
import net.technikumwien.bic4b18_01.common.settings.LoggerSettings;
import net.technikumwien.bic4b18_01.spread.shared.Daemon;
import static net.technikumwien.bic4b18_01.common.assist.LocalNetwork.getIPv4;

/**
 *
 * @author Florian
 */
public class DaemonApplication {

    private static final Logger logger = Logger.getLogger(DaemonApplication.class.getName());

    static {
        try {
            LoggerSettings.initLogging();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "{0} -> logger could not be started", Thread.currentThread().getName());
        }
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        File trunk = getTrunk();
        Set<String> myIPs = getIPv4();
        Set<String> daemonIPs = Daemon.ips(); // where you can contact a deamon
        Set<String> daemonNames = new HashSet(); // where you can contact a deamon

        //getting my spread-name from spread.conf
        //read
        try {
            for (String line : Files.readAllLines(Paths.get(trunk.getPath() + "\\spread\\src\\main\\java\\net\\technikumwien\\bic4b18_01\\spread\\local\\spread.conf"))) {
                if (!line.contains("\t")) {
                    continue;
                }
                String ip = line.substring(12);
                if (myIPs.contains(ip)) {
                    daemonIPs.add(ip);
                    daemonNames.add(line.substring(1, 10));
                }
            }
        } catch (IOException ex) {
            logger.log(Level.WARNING, "{0} -> !!! spread.conf UNREADABLE -> standart values used !!!", Thread.currentThread().getName()); // shouldn't happen
            daemonIPs.clear();
            daemonIPs.add("127.0.0.1");
            daemonNames.clear();
            daemonNames.add("localhost");
        }
        if (daemonNames.isEmpty()) {
            logger.log(Level.SEVERE, "{0} -> could not asign daemon to any IP", Thread.currentThread().getName());
            return;
        }

        // appending to daemon.ip
        String content="";
        content = daemonIPs.stream().map((ip) -> ip + "\n").reduce(content, String::concat);
        try {
            Path path = Paths.get(trunk.getPath() + "\\spread\\src\\main\\java\\net\\technikumwien\\bic4b18_01\\spread\\local\\daemon.ip");
            byte[] strToBytes = content.getBytes();
            Files.write(path, strToBytes, CREATE, TRUNCATE_EXISTING, WRITE);
            logger.log(Level.INFO, "{0} -> spread.config successfully created", Thread.currentThread().getName());

            // generating content of spread.bat
            content = "CD /D %~dp0\n";
            for (String name : daemonNames) {
                content += "start ..\\..\\..\\..\\..\\..\\..\\..\\spread-binary\\spread-bin-4.0.0\\bin\\win32\\spread.exe -n "
                        + name
                        + " -c spread.conf\n";
            }
            content += "exit\n";
            // creating spread.bat
            path = Paths.get(trunk.getPath() + "\\spread\\src\\main\\java\\net\\technikumwien\\bic4b18_01\\spread\\local\\spread.bat");
            strToBytes = content.getBytes();
            Files.write(path, strToBytes, CREATE, TRUNCATE_EXISTING, WRITE);
            logger.log(Level.INFO, "{0} -> spread.bat successfully created", Thread.currentThread().getName());

            //starting spread daemon
            try {
                Runtime.getRuntime().exec("cmd /c start " + trunk.getPath() + "\\spread\\src\\main\\java\\net\\technikumwien\\bic4b18_01\\spread\\local\\spread.bat");
            } catch (IOException ex) {
                logger.log(Level.SEVERE, Thread.currentThread().getName(), ex);
                System.exit(1);
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, Thread.currentThread().getName(), ex);
                System.exit(1);
        }
    }

}
