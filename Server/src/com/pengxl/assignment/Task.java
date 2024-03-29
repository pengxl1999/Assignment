package com.pengxl.assignment;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author pengxianglong
 */
public class Task {
    Integer id;
    Integer task;
    ArrayList<String> parameters;
    //开始时间，用于判断超时
    long startTime;
    String result;

    public Task(int task, ArrayList<String> parameters) {
        id = maxId++;
        this.task = task;
        this.parameters = parameters;
    }

    private static Integer maxId = 0;
}
