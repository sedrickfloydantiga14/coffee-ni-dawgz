package com.coffeenidawgz;

import com.coffeenidawgz.database.DatabaseManager;
import com.coffeenidawgz.ui.MainFrame;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        // Initialize SQLite Database & Tables
        DatabaseManager.initializeDatabase();

        // Launch Swing GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Set System Look & Feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                UIManager.put("Button.foreground", new Color(0x21, 0x21, 0x21));
                UIManager.put("OptionPane.messageForeground", new Color(0x3E, 0x27, 0x23));
                UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 12));
            } catch (Exception e) {
                e.printStackTrace();
            }

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
