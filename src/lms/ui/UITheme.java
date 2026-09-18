package lms.ui;

import java.awt.Color;
import java.awt.Font;

/**
 * Centralized light theme for the Swing desktop UI.
 * One restrained palette: cream background, white surfaces, thin borders,
 * dark text, muted labels and a single gold accent.
 */
public final class UITheme {

    private UITheme() {
    }

    // --- Colors -----------------------------------------------------------
    public static final Color BACKGROUND   = new Color(0xF6F3EB);
    public static final Color SURFACE      = new Color(0xFFFFFF);
    public static final Color SURFACE_ALT  = new Color(0xEFEEE2);
    public static final Color BORDER       = new Color(0xDAD4C7);
    public static final Color TEXT         = new Color(0x2A2A27);
    public static final Color MUTED        = new Color(0x6E6A60);
    public static final Color ACCENT       = new Color(0x9D7921);
    public static final Color ACCENT_LIGHT = new Color(0xEEE5CC);
    public static final Color SUCCESS      = new Color(0x30784D);
    public static final Color DANGER       = new Color(0xB23731);

    // --- Fonts ------------------------------------------------------------
    public static final Font TITLE   = new Font("Serif", Font.BOLD, 28);
    public static final Font SECTION = new Font("Serif", Font.BOLD, 18);
    public static final Font BODY    = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font SMALL   = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font LABEL   = new Font("SansSerif", Font.BOLD, 11);
    public static final Font VALUE   = new Font("SansSerif", Font.BOLD, 26);

    /** Applies the shared defaults to a JButton (flat, left-friendly). */
    public static void styleButton(javax.swing.AbstractButton button) {
        button.setFont(BODY);
        button.setForeground(TEXT);
        button.setBackground(SURFACE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(BORDER),
                javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16)));
    }

    /** Primary (accent-filled) button variant. */
    public static void stylePrimaryButton(javax.swing.AbstractButton button) {
        styleButton(button);
        button.setBackground(ACCENT);
        button.setForeground(Color.WHITE);
        button.setBorder(javax.swing.BorderFactory.createEmptyBorder(9, 18, 9, 18));
    }

    /** Danger (destructive) button variant. */
    public static void styleDangerButton(javax.swing.AbstractButton button) {
        styleButton(button);
        button.setBackground(SURFACE);
        button.setForeground(DANGER);
    }

    /** Styled JLabel showing a muted, uppercase field label. */
    public static javax.swing.JLabel fieldLabel(String text) {
        javax.swing.JLabel label = new javax.swing.JLabel(text.toUpperCase());
        label.setFont(LABEL);
        label.setForeground(MUTED);
        return label;
    }
}
