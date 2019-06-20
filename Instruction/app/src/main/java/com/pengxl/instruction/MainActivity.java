package com.pengxl.instruction;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.pengxl.instruction.StatusConstant.*;

/**
 * @author pengxianglong
 */
public class MainActivity extends AppCompatActivity {

    private Button connect, start, send;
    private TextView status, result;
    static ProgressBar progressBar;
    //socket与宿主机连接
    private Socket socket;
    //监听线程
    private RequestThread requestThread;
    private final String REQUEST = "-1_0_0";
    private String[] permissions = { Manifest.permission.WRITE_EXTERNAL_STORAGE };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(MainActivity.this, permissions, 1999);
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 权限请求回调
     * @param requestCode 请求码
     * @param permissions 权限
     * @param grantResults 结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1999) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
            else {
                Toast.makeText(MainActivity.this, "权限请求失败！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 初始化
     */
    private void init() {
        progressBar = (ProgressBar) findViewById(R.id.progress);
        status = (TextView) findViewById(R.id.status);
        result = (TextView) findViewById(R.id.result);
        connect = (Button) findViewById(R.id.connect);
        start = (Button) findViewById(R.id.start);
        send = (Button) findViewById(R.id.send);
        status.setText(DISCONNECTED);
        //start不可点击
        start.setEnabled(false);
        send.setEnabled(false);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!connect.isActivated()) {
                    new ConnectAsyncTask().execute();
                }
                else {
                    connect.setActivated(false);
                    connect.setText("连接");
                    try {
                        socket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start.setEnabled(false);
                requestThread.start();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Task task = new Task(0, new ArrayList<Integer>());
                final int[] selectedItem = new int[1];
                String[] choices = new String[]{"斐波那契", "高精度加法", "快速幂"};
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("请选择")
                        .setSingleChoiceItems(choices, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selectedItem[0] = which;
                            }
                        })
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                task.task = selectedItem[0];
                                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.parameters,null);
                                EditText par1 = (EditText) view.findViewById(R.id.par1);
                                EditText par2 = (EditText) view.findViewById(R.id.par2);
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("请选择")
                                        .setView(view)
                                        .setPositiveButton("确认", new MyOnClickListener(task, par1, par2))
                                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        });
                                builder.setCancelable(false);
                                builder.create();
                                builder.show();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                builder.setCancelable(false);
                builder.create();
                builder.show();
            }
        });
    }

    private class MyOnClickListener implements DialogInterface.OnClickListener {

        Task task;
        EditText par1, par2;

        MyOnClickListener(Task task, EditText par1, EditText par2) {
            this.task = task;
            this.par1 = par1;
            this.par2 = par2;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            String regex="^[1-9]+[0-9]*$";
            Pattern p = Pattern.compile(regex);
            Log.i("pengxl1999", "par1:" + par1.getText().toString());
            if(!p.matcher(par1.getText().toString()).matches()
                    || (!p.matcher(par2.getText().toString()).matches() && task.task != 0)) {
                Toast.makeText(MainActivity.this, "请输入正整数！", Toast.LENGTH_SHORT).show();
                return;
            }
            Integer p1 = Integer.valueOf(par1.getText().toString());
            task.parameters.add(p1);
            if(task.task != 0) {
                Integer p2 = Integer.valueOf(par2.getText().toString());
                task.parameters.add(p2);
            }
            new SendThread(task).start();
        }
    }

    private class ConnectAsyncTask extends AsyncTask<Void, Void, Void> {

        /**
         * 连接成功后回调函数
         * @param aVoid Void
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            status.setText(CONNECTED);
            //start可点击
            start.setEnabled(true);
            send.setEnabled(true);
            connect.setText("断开连接");
            connect.setActivated(true);
            //开启监听线程
            requestThread = new RequestThread();
        }

        /**
         * 连接服务器线程
         * @param voids Void...
         * @return null
         */
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                socket = new Socket("192.168.2.150", 8088);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
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
                requestTask();
            }
        }

        /**
         * 请求任务
         */
        private synchronized void requestTask() {
            try {
                //请求
                printWriter.println(REQUEST);
                printWriter.flush();
                String message = bufferedReader.readLine();
                if(message == null) {
                    return;
                }
                String[] s = message.split("_");
                if(s.length != 3) {
                    return;
                }
                Log.i("pengxl1999", message);
                ArrayList<Integer> parameters = new ArrayList<>();
                parameters.add(Integer.valueOf(s[1]));
                parameters.add(Integer.valueOf(s[2]));
                Task task = new Task(Integer.valueOf(s[0]), parameters);
                Integer res = 0;
                switch (task.task) {
                    case 0:
                        res = Fibonacci.fib(task.parameters.get(0));
                        break;
                    default:
                        break;
                }
                Log.i("pengxl1999", "res:" + res);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class SendThread extends Thread {

        private Task task;
        private PrintWriter printWriter;
        private BufferedReader bufferedReader;

        SendThread(Task task) {
            this.task = task;
            Log.i("pengxl1999", task.task + "");
            try {
                this.printWriter = new PrintWriter(socket.getOutputStream());
                this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            super.run();
            try {
                String message;
                if(task.task == 0) {
                    message = task.task + "_" + task.parameters.get(0) + "_0";
                }
                else {
                    message = task.task + "_" + task.parameters.get(0) + "_" + task.parameters.get(1);
                }
                printWriter.println(message);
                printWriter.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
