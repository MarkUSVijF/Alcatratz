/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.technikumwien.bic4b18_01.spread.shared;

import java.io.IOException;
import java.nio.file.Files;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.technikumwien.bic4b18_01.common.assist.TraceHelper;
import static net.technikumwien.bic4b18_01.common.assist.TraceHelper.getTrunk;

/**
 *
 * @author Florian
 */
public class Daemon {
    static private final Set<String> daemonIPs;
     private static final Logger logger = Logger.getLogger(TraceHelper.getClassName());
    static {
        daemonIPs = new HashSet();
        Path path = Paths.get(getTrunk().getPath() + "\\spread\\src\\main\\java\\net\\technikumwien\\bic4b18_01\\spread\\local\\daemon.ip");
        try {
            for(String line:Files.readAllLines(path)){
                daemonIPs.add(line);
            }
            daemonIPs.remove("");
        } catch (IOException ex) {
            if(Files.exists(path, NOFOLLOW_LINKS)){
                logger.log(Level.SEVERE, "{0} -> could not read daemon.ip", Thread.currentThread().getName());
            } else {
                try {
                    Files.createFile(path);
                } catch (IOException ex1) {
                    logger.log(Level.SEVERE, "{0} -> could not create daemon.ip", Thread.currentThread().getName());
                }
            }
        }
    }
    public static Set<String> ips(){
        return daemonIPs;
    }
}
