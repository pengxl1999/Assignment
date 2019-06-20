package pxl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Socket;

public class Main extends JFrame {

    private JLabel label;
    private JPanel panel;
    static JProgressBar progressBar;
    private JButton start;
    private Client client;

    public Main() {
        super("多机系统");
        panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 400, 20));
        panel.setPreferredSize(new Dimension(400, 200));
        label = new JLabel("Ready!");
        start = new JButton("开始");
        client = new Client();
        progressBar = new JProgressBar(SwingConstants.HORIZONTAL);
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setStringPainted(true);

        panel.add(label);
        panel.add(progressBar);

        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                label.setText("Running!");
                client.start();
            }
        });

        setLayout(new FlowLayout());
        add(panel);
        add(start);
        setSize(400, 300);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        Main main = new Main();
    }

}
