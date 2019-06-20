/*
 * 通过上位机模拟较难实现操作系统底层控制，以及编译代码过程，
 * 所以这里我们使用固定的三种任务类型：
 * （1）计算斐波那契数列第n项对99991取模的值
 * （2）计算200万位数字的和（高精度加法）
 * （3）使用快速幂计算m的n次方，结果对99991取模
 * 这四类代码分别在不同设备中编译完毕，在对并行计算结果进行评
 * 估时，所采用的时间值并不包括编译时间。该多机系统相当于一个
 * 执行特定功能的机器，并通过并行提高效率。
 * 通过用户的请求，将该四种类型的任务加入到队列中，并进行执行，
 * 运算结果存放在memory中，并可在多台计算机中查看，
 * 具体内容参考设计文档。
 */
package com.pengxl.assignment;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @author pengxianglong
 */
public class Server extends Thread {

    //服务器socket
    private ServerSocket serverSocket;
    //未执行任务队列
    static LinkedList<Task> taskQueue = new LinkedList<>();
    //正在执行的任务
    private LinkedList<Task> runningTask;
    //已完成的任务
    static LinkedList<Task> finishedTask = new LinkedList<>();
    //空闲的计算机个数
    private LinkedList<Socket> clients;

    Server() {
        try {
            //开启socket
            serverSocket = new ServerSocket(8088);
            clients = new LinkedList<>();
            runningTask = new LinkedList<>();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while(true) {
            try {
                Socket client = serverSocket.accept();
                new ListenThread(client).start();
                new AllocThread().start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //监听请求线程
    private class ListenThread extends Thread {

        private PrintWriter printWriter;
        private BufferedReader bufferedReader;
        private Socket client;

        ListenThread(Socket client) {
            this.client = client;
            try {
                printWriter = new PrintWriter(client.getOutputStream());
                bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while(client.isConnected()) {
                int n = getRequest();
                if(n == -2) {
                    try {
                        if(printWriter != null) {
                            printWriter.close();
                        }
                        if(bufferedReader != null) {
                            bufferedReader.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                if(n == -1) {
                    clients.addLast(client);
                }
            }
        }

        //获取请求
        private synchronized int getRequest() {
            try {
                String message = bufferedReader.readLine();
                if(message == null) {
                    return -3;
                }
                //System.out.println(message);
                String[] s = message.split("_");
                if(s.length != 3) {
                    return -2;
                }
                if(Integer.valueOf(s[0]) == -1) {
                    return -1;
                }
                if(Integer.valueOf(s[0]) / 10 == 0) {
                    loadTask(s);
                }
                if(Integer.valueOf(s[0]) / 10 == 1) {
                    loadResult(s);
                }
                return Integer.valueOf(s[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return -2;
        }
    }

    //分配任务线程
    private class AllocThread extends Thread {

        private PrintWriter printWriter;
        private Socket client;
        private Task task;

        @Override
        public void run() {
            while(true) {
                if(!taskQueue.isEmpty() && task == null) {
                    task = taskQueue.removeFirst();
                }
                //单个任务运行时间大于60s则认为超时
                if(!runningTask.isEmpty() && System.currentTimeMillis() - runningTask.getFirst().startTime > 60000) {
                    taskQueue.addLast(runningTask.removeFirst());
                }
                if(!clients.isEmpty() && task != null) {
                    client = clients.removeFirst();
                    try {
                        printWriter = new PrintWriter(client.getOutputStream());
                        String message = task.id + "_" + task.task + "_" + task.parameters.get(0) + "_" + task.parameters.get(1);
                        printWriter.println(message);
                        printWriter.flush();
                        task.startTime = System.currentTimeMillis();
                        runningTask.addLast(task);
                        task = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                }
                try {
                    //每隔1s分配一次任务
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private synchronized void loadTask(String[] s) {
        ArrayList<String> parameters = new ArrayList<>();
        parameters.add(s[1]);
        parameters.add(s[2]);
        Task task = new Task(Integer.valueOf(s[0]), parameters);
        //把任务加入队列
        taskQueue.addLast(task);
    }

    private synchronized void loadResult(String[] s) {
        Integer id = Integer.valueOf(s[1]);
        for(Task task : runningTask) {
            if(task.id.equals(id)) {
                task.result = s[2];
                System.out.println(task.result);
                finishedTask.add(task);
                runningTask.remove(task);
                break;
            }
        }
    }
}
