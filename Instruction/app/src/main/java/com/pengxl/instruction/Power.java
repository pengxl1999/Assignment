package com.pengxl.instruction;

import static com.pengxl.instruction.MainActivity.progressBar;

public class Power {

    public static String power(Integer a, Integer b) {
        Integer d = b;
        Integer sumProgress = 0;
        while(d > 0) {
            d >>= 1;
            sumProgress++;
        }
        d = 0;
        Integer result = 1;
        while(b > 0){
            d++;
            progressBar.setProgress((int)((double)d / (double)sumProgress * 100));
            if(b % 2 == 1){
                result *= a;
                result %= 99991;
            }
            a *= a;
            a %= 99991;
            b >>= 1;
        }
        progressBar.setProgress(0);
        return String.valueOf(result);
    }
}
