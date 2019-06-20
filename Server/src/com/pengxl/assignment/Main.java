package com.pengxl.assignment;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;

/**
 * @author pengxianglong
 */
public class Main extends JFrame{

    private JTable table;
    private JScrollPane scrollPane;
    private JButton display;
    private Object[][] tableData;
    private String[] title = {"任务ID", "运行内容", "参数1", "参数2", "运行结果"};

    Main() {
        super("服务器端");
        tableData = new Object[100000][5];
        table = new JTable(tableData, title);
        scrollPane = new JScrollPane(table);
        display = new JButton("显示结果");
        scrollPane.setPreferredSize(new Dimension(800, 500));

        display.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int size = Server.finishedTask.size();
                for(int i = 0; i < size; i++) {
                    table.setValueAt(Server.finishedTask.get(i).id, i, 0);
                    switch (Server.finishedTask.get(i).task) {
                        case 0:
                            table.setValueAt("斐波那契", i, 1);
                            break;
                        case 1:
                            table.setValueAt("高精度加法", i, 1);
                            break;
                        case 2:
                            table.setValueAt("快速幂", i, 1);
                            break;
                        default:
                            table.setValueAt("null", i, 1);
                            break;
                    }
                    table.setValueAt(Server.finishedTask.get(i).parameters.get(0), i, 2);
                    table.setValueAt(Server.finishedTask.get(i).parameters.get(1), i, 3);
                    table.setValueAt(Server.finishedTask.get(i).result, i, 4);
                }

            }
        });

        setLayout(new FlowLayout());
        add(scrollPane);
        //add(display);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 500);
        setVisible(true);
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.loadData();
    }

    /**
     * 加载初始数据，1000条指令，通过随机数生成。
     */
    private void loadData() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("/Users/pengxianglong/Desktop/Assignment/tasks.txt")));
            String message;
            while((message = bufferedReader.readLine()) != null) {
                System.out.println(message);
                String[] s = message.split("_");
                ArrayList<String> parameters = new ArrayList<>();
                parameters.add(s[1]);
                parameters.add(s[2]);
                Task task = new Task(Integer.valueOf(s[0]), parameters);
                Server.taskQueue.add(task);
            }
            Server server = new Server();
            server.start();
            new UpdateThread().start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //更新结果线程
    private class UpdateThread extends Thread {

        @Override
        public void run() {
            while (true) {
                int size = Server.finishedTask.size();
                for(int i = 0; i < size; i++) {
                    table.setValueAt(Server.finishedTask.get(i).id, i, 0);
                    switch (Server.finishedTask.get(i).task) {
                        case 0:
                            table.setValueAt("斐波那契", i, 1);
                            break;
                        case 1:
                            table.setValueAt("高精度加法", i, 1);
                            break;
                        case 2:
                            table.setValueAt("快速幂", i, 1);
                            break;
                        default:
                            table.setValueAt("null", i, 1);
                            break;
                    }
                    table.setValueAt(Server.finishedTask.get(i).parameters.get(0), i, 2);
                    table.setValueAt(Server.finishedTask.get(i).parameters.get(1), i, 3);
                    table.setValueAt(Server.finishedTask.get(i).result, i, 4);
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
