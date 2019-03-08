/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.technikumwien.bic4b18_01.common.assist;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * used as marker
 *
 * @author Florian
 */
public enum TODO {
    EXCEPTION,
    METHOD,
    MORE;
    private static final Logger logger = Logger.getLogger(TraceHelper.getClassName());

    public void todo(String className) {
        logger.log(Level.INFO, "{0} -> [TODO] {1} to do in {2}", new Object[]{Thread.currentThread().getName(), this.name(), className});
    }

    public static void todo(String className, String msg) {
        logger.log(Level.INFO, "{0} -> [TODO] {1}", new Object[]{Thread.currentThread().getName(), msg});
    }
}
