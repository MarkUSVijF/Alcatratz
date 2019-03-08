/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.technikumwien.bic4b18_01.common.assist;

import java.util.Random;
import java.util.stream.IntStream;

/**
 *
 * @author Florian
 */
public class RUID {

    private String ruid;

    private RUID() {
    }

    @Override
    public String toString() {
        return this.ruid;
    }

    public static RUID new_9() {

        RUID that = new RUID();
        that.ruid = ""; //0 < serverRUID.length <= 9
        Random random = new Random();
        IntStream ints;
        ints = random.ints(3, 0, 26);
        ints.forEach(i -> {
            that.ruid += Ascii.encode(i); // A-Z
        }
        );
        ints = random.ints(6, 0, 26 + 10);
        ints.forEach(i -> {
            that.ruid += Ascii.encode(26 + i); // 0-9,a-z
        }
        );
        // still allows for (26)^(3)*(26+10)^(6)*(1)^(1)
        // = 38.259.126.337.536 unique IDS
        return that;
    }
}
