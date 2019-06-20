package com.pengxl.instruction;

import java.io.Serializable;
import java.util.ArrayList;

public class Task implements Serializable {
    private static final long serialVersionUID = 1L;
    Integer task;
    ArrayList<Integer> parameters;

    public Task(int task, ArrayList<Integer> parameters) {
        this.task = task;
        this.parameters = parameters;
    }
}
