package com.coffeenidawgz.utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MascotIcon {

    private static BufferedImage cachedLogo = null;
    private static final Map<String, BufferedImage> productImageCache = new HashMap<>();
    private static boolean imageCacheInitialized = false;

    private static synchronized BufferedImage getLogoImage() {
        if (cachedLogo != null)
            return cachedLogo;

        try {
            // 1. Check ClassLoader resource
            InputStream is = MascotIcon.class.getResourceAsStream("/resources/logo.jpg");
            if (is == null)
                is = MascotIcon.class.getResourceAsStream("/logo.jpg");
            if (is != null) {
                cachedLogo = ImageIO.read(is);
                return cachedLogo;
            }

            // 2. Check local file paths
            String[] filePaths = new String[] { "logo.jpg", "assets/logo.jpg",
                    "src/resources/logo.jpg" };
            for (String fp : filePaths) {
                File f = new File(fp);
                if (f.exists()) {
                    cachedLogo = ImageIO.read(f);
                    return cachedLogo;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static synchronized void initializeProductImageCache() {
        if (imageCacheInitialized)
            return;
        imageCacheInitialized = true;

        // Known image files in images directory with exact case & variations
        String[] knownFiles = new String[] {
                "Americano.jpg", "Americano.png",
                "Latte.jpg", "Latte.png",
                "Mocha.jpg", "Mocha.png",
                "IcedMocha.jpg", "IcedMocha.png",
                "Hotchocolate.jpg", "Hotchocolate.png",
                "Matchalatte.jpg", "Matchalatte.png",
                "Fudgebrownies.jpg", "Fudgebrownies.png"
        };

        for (String kf : knownFiles) {
            BufferedImage img = loadSingleImage(kf);
            if (img != null) {
                String key = kf.substring(0, kf.lastIndexOf('.')).toLowerCase();
                productImageCache.put(key, img);
            }
        }

        // Scan images folder on disk if accessible
        File imagesDir = new File("images");
        if (!imagesDir.exists()) {
            try {
                File codeLocation = new File(
                        MascotIcon.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                imagesDir = new File(codeLocation.getParentFile(), "images");
            } catch (Exception ignored) {
            }
        }
        if (imagesDir.exists() && imagesDir.isDirectory()) {
            File[] files = imagesDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile()) {
                        try {
                            BufferedImage img = ImageIO.read(f);
                            if (img != null) {
                                String name = f.getName();
                                int dotIndex = name.lastIndexOf('.');
                                String key = (dotIndex > 0 ? name.substring(0, dotIndex) : name).toLowerCase();
                                productImageCache.put(key, img);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
    }

    private static BufferedImage loadSingleImage(String filename) {
        if (filename == null || filename.trim().isEmpty())
            return null;
        String nameOnly = filename.contains("/") ? filename.substring(filename.lastIndexOf('/') + 1)
                : (filename.contains("\\") ? filename.substring(filename.lastIndexOf('\\') + 1)
                        : filename);

        // 1. Try disk paths
        String[] possiblePaths = new String[] {
                filename,
                "images/" + nameOnly,
                "NewProject/images/" + nameOnly,
                "src/images/" + nameOnly,
                "bin/images/" + nameOnly
        };
        for (String path : possiblePaths) {
            try {
                File f = new File(path);
                if (f.exists() && f.isFile()) {
                    BufferedImage img = ImageIO.read(f);
                    if (img != null)
                        return img;
                }
            } catch (Exception ignored) {
            }
        }

        // 2. Try classpath resources
        String[] resourcePaths = new String[] {
                "/" + filename,
                "/images/" + nameOnly,
                "/resources/images/" + nameOnly
        };
        for (String res : resourcePaths) {
            try {
                InputStream is = MascotIcon.class.getResourceAsStream(res);
                if (is != null) {
                    BufferedImage img = ImageIO.read(is);
                    if (img != null)
                        return img;
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    public static ImageIcon getMascotIcon(int width, int height) {
        BufferedImage logo = getLogoImage();
        if (logo != null) {
            BufferedImage scaled = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = scaled.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);

            g2.drawImage(logo, 0, 0, width, height, null);
            g2.dispose();
            return new ImageIcon(scaled);
        }

        return drawFallbackLogo(width, height);
    }

    public static ImageIcon getProductIcon(com.coffeenidawgz.models.Product product, int width, int height) {
        if (product == null)
            return getMascotIcon(width, height);
        return getProductIcon(product.getName(), product.getImagePath(), width,
                height);
    }

    public static ImageIcon getProductIcon(String productName, int width, int height) {
        return getProductIcon(productName, null, width, height);
    }

    public static ImageIcon getProductIcon(String productName, String imagePath,
            int width, int height) {
        initializeProductImageCache();

        BufferedImage img = null;

        // 1. Try specified imagePath directly
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            img = loadSingleImage(imagePath);
        }

        // 2. If no direct image found, try matching by product name / image keys
        if (img == null && productName != null && !productName.trim().isEmpty()) {
            String clean = productName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();

            String imageKey = null;
            if (clean.contains("americano"))
                imageKey = "americano";
            else if (clean.contains("icedmocha"))
                imageKey = "icedmocha";
            else if (clean.contains("mocha"))
                imageKey = "mocha";
            else if (clean.contains("matcha") || clean.contains("tea"))
                imageKey = "matchalatte";
            else if (clean.contains("chocolate") || clean.contains("cocoa"))
                imageKey = "hotchocolate";
            else if (clean.contains("latte") || clean.contains("cappuccino"))
                imageKey = "latte";
            else if (clean.contains("brownie") || clean.contains("cookie") ||
                    clean.contains("cake") || clean.contains("sandwich"))
                imageKey = "fudgebrownies";

            if (imageKey != null && productImageCache.containsKey(imageKey)) {
                img = productImageCache.get(imageKey);
            }

            if (img == null && productImageCache.containsKey(clean)) {
                img = productImageCache.get(clean);
            }

            if (img == null) {
                for (Map.Entry<String, BufferedImage> entry : productImageCache.entrySet()) {
                    if (clean.contains(entry.getKey()) || entry.getKey().contains(clean)) {
                        img = entry.getValue();
                        break;
                    }
                }
            }
        }

        if (img != null) {
            BufferedImage scaled = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = scaled.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);

            g2.setClip(new java.awt.geom.RoundRectangle2D.Double(0, 0, width, height, 10,
                    10));
            g2.drawImage(img, 0, 0, width, height, null);
            g2.dispose();
            return new ImageIcon(scaled);
        }

        return getMascotIcon(width, height);
    }

    private static ImageIcon drawFallbackLogo(int width, int height) {
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        double cx = width / 2.0;
        double cy = height / 2.0;
        double scale = Math.min(width, height) / 100.0;

        GradientPaint bgGrad = new GradientPaint(
                0, 0, new Color(0xFF, 0xEE, 0xDD),
                width, height, new Color(0xE0, 0xA8, 0x68));
        g2.setPaint(bgGrad);
        g2.fill(new Ellipse2D.Double(2 * scale, 2 * scale, (width - 4 * scale),
                (height - 4 * scale)));

        g2.setColor(new Color(0x3E, 0x27, 0x23));
        g2.fill(new RoundRectangle2D.Double(cx - 8 * scale, cy - 2 * scale, 16 *
                scale, 11 * scale, 6 * scale, 6 * scale));

        g2.dispose();
        return new ImageIcon(image);
    }

    public static ImageIcon getPawIcon(int size, Color color) {
        return getMascotIcon(size, size);
    }
}
