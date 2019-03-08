/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.technikumwien.bic4b18_01.common.assist;

/**
 *
 * @author Florian
 */
public class Ascii {

    public static char encode26(int ascii) {// 0 <= ascii < 26
        while (true) {
            ascii += 65;
            if (ascii > 90) {
                ascii += 0 - 91;
            } else {
                break; //A-Z ==> encode26
            }
        }
        return (char) (ascii); //ascii conversion
    }

    public static char encode36(int ascii) {// 0 <= ascii < 36
        while (true) {
            ascii += 65;
            if (ascii > 90) {
                ascii += 48 - 91;
            } else {
                break; //A-Z ==> encode26
            }
            if (ascii > 57) {
                ascii += 0 - 58;
            } else {
                break; //0-9 ==> encode36
            }
        }
        return (char) (ascii); //ascii conversion
    }

    public static char encode62(int ascii) {// 0 <= ascii < 62
        while (true) {
            ascii += 65;
            if (ascii > 90) {
                ascii += 48 - 91;
            } else {
                break; //A-Z ==> encode26
            }
            if (ascii > 57) {
                ascii += 97 - 58;
            } else {
                break; //0-9 ==> encode36
            }
            if (ascii > 122) {
                ascii += 0 - 123;
            } else {
                break; //a-z ==> encode62
            }
        }
        return (char) ascii; //ascii conversion
    }

    /**
     * sorted
     *
     * @param ascii
     * @return
     */
    public static char encode(int ascii) {// 0 <= ascii < 95 
        ascii %= 95;// added after the thought
        while (true) {
            ascii += 65;
            if (ascii > 90) {
                ascii += 48 - 91;
            } else {
                break; //A-Z ==> encode26
            }
            if (ascii > 57) {
                ascii += 97 - 58;
            } else {
                break; //0-9 ==> encode36
            }
            if (ascii > 122) {
                ascii += 32 - 123;
            } else {
                break; //a-z ==> encode62
            }
            if (ascii > 47) {
                ascii += 58 - 48;
            } else {
                break;
            }
            if (ascii > 64) {
                ascii += 91 - 65;
            } else {
                break;
            }
            if (ascii > 96) {
                ascii += 123 - 97;
            } else {
                break;
            }
            if (ascii > 126) {
                ascii += 0 - 127;
            } else {
                break;
            }
        }
        return (char) ascii; //ascii conversion
    }
}
