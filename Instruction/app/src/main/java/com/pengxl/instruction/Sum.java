package com.pengxl.instruction;

import android.util.Log;

import static com.pengxl.instruction.MainActivity.progressBar;

public class Sum {

    public static int max(int a, int b) {
        return a > b ? a : b;
    }

    public static String sum(String str1, String str2) {
        int length = max(str1.length(), str2.length());
        int[] num1 = new int[length];
        int[] num2 = new int[length];
        int[] result = new int[length+1];
        //Log.i("pengxl1999", str1 + " " + str2);
        StringBuilder resultStr = new StringBuilder();
        for(int i = 0; i < str1.length(); i++) {
            num1[i] = str1.charAt(str1.length() - i - 1) - '0';
        }
        for(int i = 0; i < str2.length(); i++) {
            num2[i] = str2.charAt(str2.length() - i - 1) - '0';
        }
        progressBar.setProgress(20);
        for(int i = 0; i < length; i++) {
            result[i] = num1[i] + num2[i];
        }
        progressBar.setProgress(60);
        for(int i = 0;i < length; i++) {
            if(result[i] >= 10) {
                result[i+1]++;
                result[i] %= 10;
            }
        }
        progressBar.setProgress(100);
        if(result[length] != 0) {
            length++;
        }
        for(int i = 0; i < length; i++) {
            resultStr.append(result[length-i-1]);
        }
        progressBar.setProgress(0);
        return resultStr.toString();
    }
}
