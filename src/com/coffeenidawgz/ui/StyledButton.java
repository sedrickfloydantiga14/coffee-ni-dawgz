package com.coffeenidawgz.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class StyledButton extends JButton {
    private Color normalBg;
    private Color hoverBg;
    private Color pressedBg;
    private int cornerRadius;

    public StyledButton(String text, Color bg, Color fg) {
        this(text, bg, fg, 8);
    }

    public StyledButton(String text, Color bg, Color fg, int cornerRadius) {
        super(text);
        this.normalBg = bg;
        this.hoverBg = brighten(bg, 1.18f);
        this.pressedBg = darken(bg, 0.85f);
        this.cornerRadius = cornerRadius;

        setForeground(fg);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(6, 12, 6, 12));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Color bg = normalBg;
        if (!isEnabled()) {
            bg = new Color(0xBD, 0xB7, 0xAB);
        } else if (getModel().isPressed()) {
            bg = pressedBg;
        } else if (getModel().isRollover()) {
            bg = hoverBg;
        }

        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        g2.dispose();
        super.paintComponent(g);
    }

    public void setNormalBg(Color bg) {
        this.normalBg = bg;
        this.hoverBg = brighten(bg, 1.18f);
        this.pressedBg = darken(bg, 0.85f);
        repaint();
    }

    private Color brighten(Color color, float factor) {
        int r = Math.min(255, (int) (color.getRed() * factor));
        int g = Math.min(255, (int) (color.getGreen() * factor));
        int b = Math.min(255, (int) (color.getBlue() * factor));
        return new Color(r, g, b, color.getAlpha());
    }

    private Color darken(Color color, float factor) {
        int r = Math.max(0, (int) (color.getRed() * factor));
        int g = Math.max(0, (int) (color.getGreen() * factor));
        int b = Math.max(0, (int) (color.getBlue() * factor));
        return new Color(r, g, b, color.getAlpha());
    }
}
