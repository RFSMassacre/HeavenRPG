package com.github.rfsmassacre.heavenrpg.utils;

import java.util.TreeMap;

public final class RomanNumeralUtil
{
    private static final TreeMap<Integer, String> TREE_MAP = new TreeMap<>();

    static
    {
        TREE_MAP.put(1000, "M");
        TREE_MAP.put(900, "CM");
        TREE_MAP.put(500, "D");
        TREE_MAP.put(400, "CD");
        TREE_MAP.put(100, "C");
        TREE_MAP.put(90, "XC");
        TREE_MAP.put(50, "L");
        TREE_MAP.put(40, "XL");
        TREE_MAP.put(10, "X");
        TREE_MAP.put(9, "IX");
        TREE_MAP.put(5, "V");
        TREE_MAP.put(4, "IV");
        TREE_MAP.put(1, "I");

    }

    public static String toRomanNumeral(int number)
    {
        int floor = TREE_MAP.floorKey(number);
        if (number == floor)
        {
            return TREE_MAP.get(number);
        }

        return TREE_MAP.get(floor) + toRomanNumeral(number - floor);
    }
}
