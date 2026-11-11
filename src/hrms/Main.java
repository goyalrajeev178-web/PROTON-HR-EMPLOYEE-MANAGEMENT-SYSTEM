package hrms;

import hrms.ui.MainFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Try to set FlatLaf Light Look and Feel, fallback to system if not found
                UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
            } catch (Exception ex) {
                System.err.println("Failed to initialize FlatLaf");
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Initialize global theme fonts and colors
            hrms.ui.Theme.initializeUIDefaults();

            MainFrame frame = new MainFrame();
            hrms.ui.Theme.applyTypographyRecursively(frame);
            frame.setVisible(true);
        });
    }
}
