package com.pengxl.instruction;

import java.util.ArrayList;

public class Task {
    Integer id;
    Integer task;
    ArrayList<String> parameters;

    public Task(int task, ArrayList<String> parameters) {
        this.task = task;
        this.parameters = parameters;
    }
}
