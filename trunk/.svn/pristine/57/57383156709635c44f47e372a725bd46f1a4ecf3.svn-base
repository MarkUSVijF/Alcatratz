/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.technikumwien.bic4b18_01.common.assist;

/**
 *
 * @author INTERNET
 */
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TraceHelper {

    // save it static to have it available on every call
    private static Method m = null;

    static {
        try {
            m = Throwable.class.getDeclaredMethod("getStackTraceElement",
                    int.class);
        } catch (NoSuchMethodException | SecurityException e) {
            System.out.println("TraceHelper could not be initialized!!! - [ " + e.toString()+" ]");
            try {
                m = TraceHelper.class.getDeclaredMethod("notInitialised", Throwable.class ,int.class);
            } catch (NoSuchMethodException | SecurityException ex) {}
        }
        m.setAccessible(true);
    }

    public static String getMethodName(final int depth) {
        try {
            StackTraceElement element = (StackTraceElement) m.invoke(
                    new Throwable(), depth + 1);
            if(element==null) return "Method";
            return element.getMethodName();
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            return "Method";
        }
    }

    public static String getMethodName() {
        try {
            StackTraceElement element = (StackTraceElement) m.invoke(
                    new Throwable(), 1);
            if(element==null) return "Method";
            return element.getMethodName();
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            return "Method";
        }
    }

    public static String getClassName(final int depth) {
        try {
            StackTraceElement element = (StackTraceElement) m.invoke(
                    new Throwable(), depth + 1);
            if(element==null) return "Class";
            return element.getClassName().substring(element.getClassName().lastIndexOf('.') + 1);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            return "Class";
        }
    }

    public static String getClassName() {
        try {
            StackTraceElement element = (StackTraceElement) m.invoke(
                    new Throwable(), 1);
            if(element==null) return "Class";
            return element.getClassName().substring(element.getClassName().lastIndexOf('.') + 1);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            return "Class";
        }
    }

    public static File getTrunk() {
        return (new File(TraceHelper.class.getProtectionDomain().getCodeSource().getLocation().getPath())).getParentFile().getParentFile().getParentFile();
    }
    
    private static StackTraceElement notInitialised(Throwable ta, int d){
        return null;
    }
}
