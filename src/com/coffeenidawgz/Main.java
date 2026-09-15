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
            } catch (Exception e) {
                e.printStackTrace();
            }

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
