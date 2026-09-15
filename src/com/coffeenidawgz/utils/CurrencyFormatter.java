package com.coffeenidawgz.utils;

import java.text.DecimalFormat;

public class CurrencyFormatter {
    private static final DecimalFormat df = new DecimalFormat("₱#,##0.00");

    public static String format(double amount) {
        return df.format(amount);
    }
}
