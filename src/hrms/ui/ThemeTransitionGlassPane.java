package hrms.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * High-performance cross-fade transition overlay for smooth visual theme switching.
 */
public class ThemeTransitionGlassPane extends JComponent {

    private BufferedImage snapshotOld;
    private BufferedImage snapshotNew;
    private float alpha = 0.0f; // 0.0 = snapshotOld fully visible, 1.0 = snapshotNew fully visible
    private Timer fadeTimer;
    private Runnable onTransitionCompleted;

    public ThemeTransitionGlassPane() {
        setOpaque(false);
        setVisible(false);
    }

    /**
     * Captures current frame into a high-res image.
     */
    public BufferedImage capture(Component target) {
        int w = Math.max(1, target.getWidth());
        int h = Math.max(1, target.getHeight());
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        target.paint(g2);
        g2.dispose();
        return img;
    }

    /**
     * Executes a silky-smooth cross-fade animation between two state snapshots.
     */
    public void startTransition(BufferedImage before, BufferedImage after, Runnable onCompleted) {
        this.snapshotOld = before;
        this.snapshotNew = after;
        this.onTransitionCompleted = onCompleted;
        this.alpha = 0.0f;

        setVisible(true);

        if (fadeTimer != null && fadeTimer.isRunning()) {
            fadeTimer.stop();
        }

        // 60 FPS animation over ~220ms
        fadeTimer = new Timer(15, e -> {
            alpha += 0.08f;
            if (alpha >= 1.0f) {
                alpha = 1.0f;
                fadeTimer.stop();
                setVisible(false);
                snapshotOld = null;
                snapshotNew = null;
                if (onTransitionCompleted != null) {
                    onTransitionCompleted.run();
                }
            }
            repaint();
        });
        fadeTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!isVisible()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();

        // 1. Draw previous snapshot
        if (snapshotOld != null) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f - alpha));
            g2.drawImage(snapshotOld, 0, 0, w, h, null);
        }

        // 2. Cross-fade blend with new snapshot
        if (snapshotNew != null) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.drawImage(snapshotNew, 0, 0, w, h, null);
        }

        g2.dispose();
    }
}
