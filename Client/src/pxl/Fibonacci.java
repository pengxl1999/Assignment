package pxl;

public class Fibonacci {
    public static Integer fib(Integer lastIndex) {
        int[] f = new int[1000];
        f[1] = 1;
        f[2] = 1;
        for(int i = 3; i <= lastIndex; i++) {
            Main.progressBar.setValue((int)((double)i / (double)lastIndex * 100));
            f[i%1000] = f[(i+999)%1000] + f[(i+998)%1000];
            f[i%1000] %= 99991;
        }
        Main.progressBar.setValue(0);
        return f[lastIndex%1000];
    }
}
