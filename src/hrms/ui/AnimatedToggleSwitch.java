package hrms.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Animated, smooth modern toggle switch for Light/Dark mode.
 */
public class AnimatedToggleSwitch extends JComponent {

    private boolean selected;
    private float animationProgress = 0.0f; // 0.0 = Light (Left), 1.0 = Dark (Right)
    private Timer timer;
    private final int width = 56;
    private final int height = 30;
    private Runnable onToggleChanged;

    public AnimatedToggleSwitch(boolean initialState, Runnable onToggleChanged) {
        this.selected = initialState;
        this.animationProgress = initialState ? 1.0f : 0.0f;
        this.onToggleChanged = onToggleChanged;

        setPreferredSize(new Dimension(width, height));
        setMinimumSize(new Dimension(width, height));
        setMaximumSize(new Dimension(width, height));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggle();
            }
        });
    }

    public void toggle() {
        setSelected(!selected);
        if (onToggleChanged != null) {
            onToggleChanged.run();
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        if (this.selected != selected) {
            this.selected = selected;
            startAnimation();
        }
    }

    private void startAnimation() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }

        final float target = selected ? 1.0f : 0.0f;
        final float step = selected ? 0.12f : -0.12f;

        timer = new Timer(15, e -> {
            animationProgress += step;
            if ((selected && animationProgress >= target) || (!selected && animationProgress <= target)) {
                animationProgress = target;
                timer.stop();
            }
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 1. Background Pill Color Interpolation (Light Slate to Deep Slate/Navy)
        Color lightBg = new Color(0xCB, 0xD5, 0xE1); // #cbd5e1
        Color darkBg = new Color(0x3B, 0x82, 0xF6);  // #3b82f6 (Primary Blue when active)
        
        int r = (int) (lightBg.getRed() + (darkBg.getRed() - lightBg.getRed()) * animationProgress);
        int gr = (int) (lightBg.getGreen() + (darkBg.getGreen() - lightBg.getGreen()) * animationProgress);
        int b = (int) (lightBg.getBlue() + (darkBg.getBlue() - lightBg.getBlue()) * animationProgress);
        Color currentBg = new Color(r, gr, b);

        g2.setColor(currentBg);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));

        // Pill border
        g2.setColor(new Color(0, 0, 0, 20));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, h, h));

        // 2. Knob Dimensions & Sliding Position
        int knobPadding = 3;
        int knobSize = h - (knobPadding * 2);
        float minX = knobPadding;
        float maxX = w - knobSize - knobPadding;
        float currentX = minX + (maxX - minX) * animationProgress;

        // Knob Shadow
        g2.setColor(new Color(0, 0, 0, 30));
        g2.fillOval((int) currentX + 1, knobPadding + 1, knobSize, knobSize);

        // Knob Body (White Circle)
        g2.setColor(Color.WHITE);
        g2.fillOval((int) currentX, knobPadding, knobSize, knobSize);

        // 3. Mini Icon inside Knob (Sun when light, Moon when dark)
        float s = knobSize / 24.0f;
        int iconOffset = 3;
        if (animationProgress < 0.5f) {
            // Draw Sun rays & center
            g2.setColor(new Color(0xF5, 0x9E, 0x0B)); // Amber Sun
            int cx = (int) currentX + knobSize / 2;
            int cy = knobPadding + knobSize / 2;
            g2.fillOval(cx - 4, cy - 4, 8, 8);
        } else {
            // Draw Moon crescent
            g2.setColor(new Color(0x1D, 0x4E, 0xD8)); // Blue Moon
            int cx = (int) currentX + knobSize / 2;
            int cy = knobPadding + knobSize / 2;
            g2.fillOval(cx - 4, cy - 4, 8, 8);
            g2.setColor(Color.WHITE);
            g2.fillOval(cx - 2, cy - 6, 8, 8);
        }

        g2.dispose();
    }
}
