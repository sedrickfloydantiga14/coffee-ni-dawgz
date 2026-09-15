package com.coffeenidawgz.services;

import com.coffeenidawgz.database.DatabaseManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class BackupService {

    public boolean backupDatabase(File destFile) {
        File srcFile = new File("coffee_ni_dawgz.db");
        if (!srcFile.exists()) return false;

        try (FileInputStream in = new FileInputStream(srcFile);
             FileOutputStream out = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean restoreDatabase(File backupFile) {
        if (!backupFile.exists()) return false;

        File targetFile = new File("coffee_ni_dawgz.db");

        // Simple file replace
        try (FileInputStream in = new FileInputStream(backupFile);
             FileOutputStream out = new FileOutputStream(targetFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            // Re-init database to ensure schema integrity
            DatabaseManager.initializeDatabase();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
