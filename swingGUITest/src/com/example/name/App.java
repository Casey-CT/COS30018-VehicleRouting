package com.example.name;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import jade.*;
import oracle.jrockit.jfr.JFR;
import sun.management.Agent;


public class App {
    private JButton helloButton;
    private JPanel mainPanel;


    public App() {
        HelloWorldAgent ha = new HelloWorldAgent();

        helloButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ha.setup();
                paramStart();
            }
        });
    }
    protected void paramStart() {
        String[] param = new String[3];
        param[0] = "-gui";
        param[1] = "-agents";
        param[2] = "drFoo:HelloWorldAgent";
        Boot.main(param);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("App");
        HelloWorldAgent ha = new HelloWorldAgent();

        frame.setContentPane(new App().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);


    }


}
