package com.pengxl.assignment;

import javax.swing.*;

/**
 * @author pengxianglong
 */
public class Main extends JFrame {

    Main() {

    }

    public static void main(String[] args) {
        Main main = new Main();
        Server server = new Server();
        server.start();
    }
}
