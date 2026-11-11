package hrms.ui;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Centralized theme management with dynamic Light & Dark mode support.
 */
public class Theme {

    public static boolean isDarkMode = false;

    // Dynamic Colors based on mode
    public static Color PRIMARY = new Color(0x25, 0x63, 0xEB);         // #2563eb
    public static Color PRIMARY_HOVER = new Color(0x1D, 0x4E, 0xD8);   // #1d4ed8
    public static Color PRIMARY_LIGHT = new Color(0xDB, 0xEA, 0xFE);   // #dbeafe

    // Surfaces
    public static Color BG_APP = new Color(0xF8, 0xFA, 0xFC);          // #f8fafc
    public static Color SURFACE_CARD = Color.WHITE;                     // #ffffff
    public static Color SIDEBAR_BG = new Color(0x0F, 0x17, 0x2A);       // #0f172a
    public static Color TOPBAR_BG = Color.WHITE;                        // #ffffff

    // Borders
    public static Color BORDER_LIGHT = new Color(0xE2, 0xE8, 0xF0);     // #e2e8f0

    // Text
    public static Color TEXT_PRIMARY = new Color(0x0F, 0x17, 0x2A);     // #0f172a
    public static Color TEXT_SECONDARY = new Color(0x47, 0x55, 0x69);   // #475569
    public static Color TEXT_MUTED = new Color(0x94, 0xA3, 0xB8);       // #94a3b8
    public static Color TEXT_ON_PRIMARY = Color.WHITE;
    public static Color TEXT_SIDEBAR = new Color(0x94, 0xA3, 0xB8);
    public static Color TEXT_SIDEBAR_ACTIVE = Color.WHITE;

    // Status Colors
    public static Color STATUS_APPROVED_BG = new Color(0xDC, 0xFC, 0xE7); // #dcfce7
    public static Color STATUS_APPROVED_FG = new Color(0x16, 0x65, 0x34); // #166534
    public static Color STATUS_PENDING_BG  = new Color(0xFE, 0xF3, 0xC7); // #fef3c7
    public static Color STATUS_PENDING_FG  = new Color(0x92, 0x40, 0x0E); // #92400e
    public static Color STATUS_REJECTED_BG = new Color(0xFE, 0xE2, 0xE2); // #fee2e2
    public static Color STATUS_REJECTED_FG = new Color(0x99, 0x1B, 0x1B); // #991b1b

    public static Color SUCCESS = new Color(0x16, 0xA3, 0x4A);

    // Font Family
    public static final String FONT_FAMILY = resolveFontFamily();

    private static String resolveFontFamily() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] availableFonts = ge.getAvailableFontFamilyNames();
        String[] preferredFonts = {"Segoe UI", "Inter", "Roboto", "Verdana", "SansSerif"};
        for (String preferred : preferredFonts) {
            for (String available : availableFonts) {
                if (available.equalsIgnoreCase(preferred)) {
                    return available;
                }
            }
        }
        return Font.SANS_SERIF;
    }

    public static final Font FONT_TITLE = new Font(FONT_FAMILY, Font.BOLD, 24);
    public static final Font FONT_SECTION = new Font(FONT_FAMILY, Font.BOLD, 18);
    public static final Font FONT_SUBTITLE = new Font(FONT_FAMILY, Font.BOLD, 16);
    public static final Font FONT_BODY = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font FONT_BODY_BOLD = new Font(FONT_FAMILY, Font.BOLD, 14);
    public static final Font FONT_SMALL = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font FONT_SMALL_BOLD = new Font(FONT_FAMILY, Font.BOLD, 12);

    /**
     * Toggles between Light and Dark mode.
     */
    public static void setDarkMode(boolean dark) {
        isDarkMode = dark;
        if (dark) {
            BG_APP = new Color(0x0F, 0x17, 0x2A);          // #0f172a
            SURFACE_CARD = new Color(0x1E, 0x29, 0x3B);    // #1e293b
            SIDEBAR_BG = new Color(0x02, 0x06, 0x17);      // #020617
            TOPBAR_BG = new Color(0x1E, 0x29, 0x3B);       // #1e293b
            BORDER_LIGHT = new Color(0x33, 0x41, 0x55);    // #334155
            TEXT_PRIMARY = new Color(0xF8, 0xFA, 0xFC);    // #f8fafc
            TEXT_SECONDARY = new Color(0x94, 0xA3, 0xB8);  // #94a3b8
            TEXT_MUTED = new Color(0x64, 0x74, 0x8B);      // #64748b
            PRIMARY_LIGHT = new Color(0x1E, 0x3A, 0x8A);   // #1e3a8a
        } else {
            BG_APP = new Color(0xF8, 0xFA, 0xFC);          // #f8fafc
            SURFACE_CARD = Color.WHITE;                     // #ffffff
            SIDEBAR_BG = new Color(0x0F, 0x17, 0x2A);       // #0f172a
            TOPBAR_BG = Color.WHITE;                        // #ffffff
            BORDER_LIGHT = new Color(0xE2, 0xE8, 0xF0);     // #e2e8f0
            TEXT_PRIMARY = new Color(0x0F, 0x17, 0x2A);     // #0f172a
            TEXT_SECONDARY = new Color(0x47, 0x55, 0x69);   // #475569
            TEXT_MUTED = new Color(0x94, 0xA3, 0xB8);       // #94a3b8
            PRIMARY_LIGHT = new Color(0xDB, 0xEA, 0xFE);   // #dbeafe
        }
        initializeUIDefaults();
    }

    public static void initializeUIDefaults() {
        Font defaultFont = FONT_BODY;
        UIManager.put("defaultFont", defaultFont);
        UIManager.put("Label.font", defaultFont);
        UIManager.put("Button.font", FONT_BODY_BOLD);
        UIManager.put("TextField.font", defaultFont);
        UIManager.put("Table.font", defaultFont);
        UIManager.put("TableHeader.font", FONT_SMALL_BOLD);
        UIManager.put("ProgressBar.font", FONT_SMALL);
        UIManager.put("Panel.background", BG_APP);
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("ProgressBar.arc", 6);
        UIManager.put("TextComponent.arc", 6);
    }

    public static void applyTypographyRecursively(Component comp) {
        if (comp == null) return;
        if (comp instanceof JLabel) {
            JLabel lbl = (JLabel) comp;
            if (lbl.getFont() == null || lbl.getFont().getSize() <= 14) {
                if (lbl.getFont() != null && lbl.getFont().isBold()) {
                    lbl.setFont(FONT_BODY_BOLD);
                } else {
                    lbl.setFont(FONT_BODY);
                }
            }
        } else if (comp instanceof JButton) {
            JButton btn = (JButton) comp;
            btn.setFont(FONT_BODY_BOLD);
        } else if (comp instanceof JTextField) {
            JTextField tf = (JTextField) comp;
            tf.setFont(FONT_BODY);
            tf.setForeground(TEXT_PRIMARY);
        } else if (comp instanceof JTable) {
            JTable tbl = (JTable) comp;
            tbl.setFont(FONT_BODY);
            JTableHeader header = tbl.getTableHeader();
            if (header != null) {
                header.setFont(FONT_SMALL_BOLD);
            }
        }

        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                applyTypographyRecursively(child);
            }
        }
    }

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setBackground(PRIMARY);
        btn.setForeground(TEXT_ON_PRIMARY);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 38));
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setBackground(SURFACE_CARD);
        btn.setForeground(TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 38));
        return btn;
    }
}
