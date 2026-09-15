package com.coffeenidawgz.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class IconGenerator {
    public static void generateIcoFile(File outputFile, int size) throws Exception {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        var g = img.createGraphics();
        MascotIcon.getMascotIcon(size, size).paintIcon(null, g, 0, 0);
        g.dispose();

        ByteArrayOutputStream pngBos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", pngBos);
        byte[] pngData = pngBos.toByteArray();

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputFile))) {
            // ICO Header
            dos.writeShort(Short.reverseBytes((short) 0)); // Reserved
            dos.writeShort(Short.reverseBytes((short) 1)); // Type ICO = 1
            dos.writeShort(Short.reverseBytes((short) 1)); // 1 image

            // Directory Entry
            dos.writeByte(size >= 256 ? 0 : size); // Width
            dos.writeByte(size >= 256 ? 0 : size); // Height
            dos.writeByte(0); // Color count
            dos.writeByte(0); // Reserved
            dos.writeShort(Short.reverseBytes((short) 1)); // Planes = 1
            dos.writeShort(Short.reverseBytes((short) 32)); // BitCount = 32
            dos.writeInt(Integer.reverseBytes(pngData.length)); // PNG size
            dos.writeInt(Integer.reverseBytes(22)); // Offset = 6 + 16 = 22

            // PNG payload
            dos.write(pngData);
        }
    }

    public static void main(String[] args) {
        try {
            File outFile = new File(args.length > 0 ? args[0] : "app.ico");
            generateIcoFile(outFile, 256);
            System.out.println("Generated ICO icon successfully: " + outFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
