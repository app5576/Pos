package com.posmix.mixtuvgag.utils;
public class TaxHelper {
    public static double calc(double sub, double rate) { return sub * (rate/100); }
    public static double total(double sub, double rate) { return sub + calc(sub, rate); }
}
