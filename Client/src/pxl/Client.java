package pxl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class Client extends Thread{

    private Socket socket;
    private final String REQUEST = "-1_0_0";

    @Override
    public void run() {
        try {
            socket = new Socket("192.168.2.150", 8088);
            new RequestThread().start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class RequestThread extends Thread {

        private PrintWriter printWriter;
        private BufferedReader bufferedReader;

        RequestThread() {
            try {
                this.printWriter = new PrintWriter(socket.getOutputStream());
                this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while(true) {
                try {
                    requestTask();
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        /**
         * 请求任务
         */
        private synchronized void requestTask() throws IOException {
            try {
                //请求
                printWriter.println(REQUEST);
                printWriter.flush();
                String message = bufferedReader.readLine();
                if(message == null) {
                    return;
                }
                String[] s = message.split("_");
                if(s.length != 4) {
                    return;
                }
                ArrayList<String> parameters = new ArrayList<>();
                parameters.add(s[2]);
                parameters.add(s[3]);
                Task task = new Task(Integer.valueOf(s[1]), parameters);
                task.id = Integer.valueOf(s[0]);
                String res = "";
                switch (task.task) {
                    case 0:
                        //斐波那契
                        res = Fibonacci.fib(Integer.valueOf(s[2])).toString();
                        break;
                    case 1:
                        //高精度加法
                        res = Sum.sum(s[2], s[3]);
                        break;
                    case 2:
                        res = Power.power(Integer.valueOf(s[2]), Integer.valueOf(s[3]));
                    default:
                        break;
                }
                message = (task.task + 10) + "_" + task.id + "_" + res;
                System.out.println(message);
                printWriter.println(message);
                printWriter.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
