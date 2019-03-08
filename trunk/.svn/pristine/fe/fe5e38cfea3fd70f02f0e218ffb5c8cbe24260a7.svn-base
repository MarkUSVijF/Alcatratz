/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.technikumwien.bic4b18_01.common.settings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

/**
 *
 * @author INTERNET
 */
public class LoggerSettings {

    /**
     * Based on code by MR Head
     *
     * @author J. Betancourt
     * @throws java.io.IOException
     */
    public static void initLogging() throws IOException {
        String consoleLevel = "CONFIG";// FINER already shows system elements like entry
        String config = "\n"
                + "handlers = java.util.logging.ConsoleHandler" + "\n"
                + ".level = ALL" + "\n"
                + "java.util.logging.ConsoleHandler.level = " + consoleLevel + "\n"
                + "com.sun.level = INFO" + "\n"
                + "javax.level = INFO" + "\n"
                + "sun.level = INFO" + "\n"
                + "";

        InputStream ins = new ByteArrayInputStream(config.getBytes());

        LogManager.getLogManager().readConfiguration(ins);

    }
}
