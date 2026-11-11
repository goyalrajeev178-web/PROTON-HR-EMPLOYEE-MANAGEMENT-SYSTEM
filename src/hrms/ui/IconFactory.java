package hrms.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * High-quality vector icon and avatar generator for crisp, scalable Swing UI icons.
 */
public class IconFactory {

    public enum IconType {
        DASHBOARD,
        EMPLOYEES,
        LEAVES,
        SETTINGS,
        BELL,
        HELP,
        PROFILE,
        SEARCH,
        PLUS,
        FILTER,
        EYE,
        EDIT,
        GRID,
        LIST,
        MAIL,
        PHONE,
        LOCATION,
        THEME,
        PAYMENT,
        TERMS,
        SECURITY,
        SUN,
        MOON,
        CHECK,
        LOCK
    }

    public static Icon createIcon(IconType type, int size, Color color) {
        return new VectorIcon(type, size, color);
    }

    public static ImageIcon createAvatarIcon(String name, int size, int cornerRadius, Color bgGradientStart, Color bgGradientEnd) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        GradientPaint gp = new GradientPaint(0, 0, bgGradientStart, size, size, bgGradientEnd);
        g2.setPaint(gp);
        g2.fill(new RoundRectangle2D.Float(0, 0, size, size, cornerRadius, cornerRadius));

        g2.setColor(new Color(255, 255, 255, 80));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Float(1, 1, size - 2, size - 2, cornerRadius, cornerRadius));

        String initials = "";
        if (name != null && !name.trim().isEmpty()) {
            String[] parts = name.trim().split("\\s+");
            if (parts.length >= 2) {
                initials = ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
            } else if (parts.length == 1) {
                initials = parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
            }
        }

        g2.setColor(Color.WHITE);
        int fontSize = Math.max(12, size / 2 - 2);
        g2.setFont(new Font(Theme.FONT_FAMILY, Font.BOLD, fontSize));
        FontMetrics fm = g2.getFontMetrics();
        int strWidth = fm.stringWidth(initials);
        int strHeight = fm.getAscent();
        int x = (size - strWidth) / 2;
        int y = (size + strHeight) / 2 - 3;
        g2.drawString(initials, x, y);

        g2.dispose();
        return new ImageIcon(image);
    }

    private static class VectorIcon implements Icon {
        private final IconType type;
        private final int size;
        private final Color color;

        public VectorIcon(IconType type, int size, Color color) {
            this.type = type;
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            g2.setColor(color);

            float s = size / 24.0f;

            switch (type) {
                case DASHBOARD:
                    paintDashboard(g2, s);
                    break;
                case EMPLOYEES:
                    paintEmployees(g2, s);
                    break;
                case LEAVES:
                    paintLeaves(g2, s);
                    break;
                case SETTINGS:
                    paintSettings(g2, s);
                    break;
                case BELL:
                    paintBell(g2, s);
                    break;
                case HELP:
                    paintHelp(g2, s);
                    break;
                case PROFILE:
                    paintProfile(g2, s);
                    break;
                case SEARCH:
                    paintSearch(g2, s);
                    break;
                case PLUS:
                    paintPlus(g2, s);
                    break;
                case FILTER:
                    paintFilter(g2, s);
                    break;
                case EYE:
                    paintEye(g2, s);
                    break;
                case EDIT:
                    paintEdit(g2, s);
                    break;
                case GRID:
                    paintGrid(g2, s);
                    break;
                case LIST:
                    paintList(g2, s);
                    break;
                case MAIL:
                    paintMail(g2, s);
                    break;
                case PHONE:
                    paintPhone(g2, s);
                    break;
                case LOCATION:
                    paintLocation(g2, s);
                    break;
                case THEME:
                    paintTheme(g2, s);
                    break;
                case PAYMENT:
                    paintPayment(g2, s);
                    break;
                case TERMS:
                    paintTerms(g2, s);
                    break;
                case SECURITY:
                case LOCK:
                    paintSecurity(g2, s);
                    break;
                case SUN:
                    paintSun(g2, s);
                    break;
                case MOON:
                    paintMoon(g2, s);
                    break;
                case CHECK:
                    paintCheck(g2, s);
                    break;
            }

            g2.dispose();
        }

        private void paintDashboard(Graphics2D g2, float s) {
            g2.setStroke(new BasicStroke(1.8f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new RoundRectangle2D.Float(3.5f * s, 3.5f * s, 7f * s, 7f * s, 2f * s, 2f * s));
            g2.draw(new RoundRectangle2D.Float(13.5f * s, 3.5f * s, 7f * s, 7f * s, 2f * s, 2f * s));
            g2.draw(new RoundRectangle2D.Float(3.5f * s, 13.5f * s, 7f * s, 7f * s, 2f * s, 2f * s));
            g2.draw(new RoundRectangle2D.Float(13.5f * s, 13.5f * s, 7f * s, 7f * s, 2f * s, 2f * s));
        }

        private void paintEmployees(Graphics2D g2, float s) {
            g2.setStroke(new BasicStroke(1.8f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Ellipse2D.Float(8.5f * s, 3.5f * s, 7f * s, 7f * s));
            Path2D.Float body = new Path2D.Float();
            body.moveTo(4.5f * s, 20.5f * s);
            body.curveTo(4.5f * s, 15.5f * s, 8f * s, 14f * s, 12f * s, 14f * s);
            body.curveTo(16f * s, 14f * s, 19.5f * s, 15.5f * s, 19.5f * s, 20.5f * s);
            g2.draw(body);
            g2.draw(new Ellipse2D.Float(2.5f * s, 6f * s, 4f * s, 4f * s));
            g2.draw(new Ellipse2D.Float(17.5f * s, 6f * s, 4f * s, 4f * s));
        }

        private void paintLeaves(Graphics2D g2, float s) {
            g2.setStroke(new BasicStroke(1.8f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new RoundRectangle2D.Float(3.5f * s, 4.5f * s, 17f * s, 16f * s, 3f * s, 3f * s));
            g2.drawLine((int)(3.5f * s), (int)(9.5f * s), (int)(20.5f * s), (int)(9.5f * s));
            g2.drawLine((int)(7.5f * s), (int)(2.5f * s), (int)(7.5f * s), (int)(5.5f * s));
            g2.drawLine((int)(16.5f * s), (int)(2.5f * s), (int)(16.5f * s), (int)(5.5f * s));
            Path2D.Float check = new Path2D.Float();
            check.moveTo(8.5f * s, 15f * s);
            check.lineTo(11f * s, 17.5f * s);
            check.lineTo(15.5f * s, 13f * s);
            g2.draw(check);
        }

        private void paintSettings(Graphics2D g2, float s) {
            g2.setStroke(new BasicStroke(1.8f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Ellipse2D.Float(8.5f * s, 8.5f * s, 7f * s, 7f * s));
            g2.drawLine((int)(12 * s), (int)(2.5f * s), (int)(12 * s), (int)(5.5f * s));
            g2.drawLine((int)(12 * s), (int)(18.5f * s), (int)(12 * s), (int)(21.5f * s));
            g2.drawLine((int)(2.5f * s), (int)(12 * s), (int)(5.5f * s), (int)(12 * s));
            g2.drawLine((int)(18.5f * s), (int)(12 * s), (int)(21.5f * s), (int)(12 * s));
            g2.drawLine((int)(5.3f * s), (int)(5.3f * s), (int)(7.5f * s), (int)(7.5f * s));
            g2.drawLine((int)(16.5f * s), (int)(16.5f * s), (int)(18.7f * s), (int)(18.7f * s));
            g2.drawLine((int)(5.3f * s), (int)(18.7f * s), (int)(7.5f * s), (int)(16.5f * s));
            g2.drawLine((int)(16.5f * s), (int)(7.5f * s), (int)(18.7f * s), (int)(5.3f * s));
        }

        private void paintBell(Graphics2D g2, float s) {
            g2.setStroke(new BasicStroke(1.8f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D.Float bell = new Path2D.Float();
            bell.moveTo(12 * s, 3.5f * s);
            bell.curveTo(8.5f * s, 3.5f * s, 6.5f * s, 6.5f * s, 6.5f * s, 11 * s);
            bell.curveTo(6.5f * s, 14.5f * s, 5 * s, 16.5f * s, 4 * s, 17.5f * s);
            bell.lineTo(20 * s, 17.5f * s);
            bell.curveTo(19 * s, 16.5f * s, 17.5f * s, 14.5f * s, 17.5f * s, 11 * s);
            bell.curveTo(17.5f * s, 6.5f * s, 15.5f * s, 3.5f * s, 12 * s, 3.5f * s);
            bell.closePath();
            g2.draw(bell);

            Path2D.Float clapper = new Path2D.Float();
            clapper.moveTo(10 * s, 18.5f * s);
            clapper.curveTo(10 * s, 19.8f * s, 10.9f * s, 21 * s, 12 * s, 21 * s);
            clapper.curveTo(13.1f * s, 21 * s, 14 * s, 19.8f * s, 14 * s, 18.5f * s);
            g2.draw(clapper);

            g2.setColor(new Color(0xEF, 0x44, 0x44));
            g2.fill(new Ellipse2D.Float(16 * s, 3 * s, 5.5f * s, 5.5f * s));
        }

        private void paintHelp(Graphics2D g2, float s) {
            g2.setStroke(new BasicStroke(1.8f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Ellipse2D.Float(2.5f * s, 2.5f * s, 19 * s, 19 * s));
            Path2D.Float qm = new Path2D.Float();
            qm.moveTo(9.5f * s, 9 * s);
            qm.curveTo(9.5f * s, 7 * s, 11 * s, 6 * s, 12 * s, 6 * s);
            qm.curveTo(13.8f * s, 6 * s, 15 * s, 7.2f * s, 15 * s, 8.8f * s);
            qm.curveTo(15 * s, 10.5f * s, 13 * s, 11.5f * s, 12 * s, 13f * s);
            qm.lineTo(12 * s, 14.5f * s);
            g2.draw(qm);
            g2.fill(new Ellipse2D.Float(11.2f * s, 16.5f * s, 1.8f * s, 1.8f * s));
        }

        private void paintProfile(Graphics2D g2, float s) {
            g2.setStroke(new BasicStroke(1.8f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Ellipse2D.Float(7.5f * s, 3.5f * s, 9 * s, 9 * s));
            Path2D.Float body = new Path2D.Float();
            body.moveTo(4f * s, 20.5f * s);
            body.curveTo(4.5f * s, 15.5f * s, 8.5f * s, 14.5f * s, 12f * s, 14.5f * s);
            body.curveTo(15.5f * s, 14.5f * s, 19.5f * s, 15.5f * s, 20f * s, 20.5f * s);
            g2.draw(body);
        }

        private void paintSearch(Graphics2D g2, float s) {
            g2.setStroke(new BasicStroke(1.8f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Ellipse2D.Float(3.5f * s, 3.5f * s, 11.5f * s, 11.5f * s));
            g2.drawLine((int)(12.5f * s), (int)(12.5f * s), (int)(20.5f * s), (int)(20.5f * s));
        }

        private void paintPlus(Graphics2D g2, float s) {
            g2.setStroke(new BasicStroke(2.2f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine((int)(12 * s), (int)(5 * s), (int)(12 * s), (int)(19 * s));
            g2.drawLine((int)(5 * s), (int)(12 * s), (int)(19 * s), (int)(12 * s));
        }

        private void paintFilter(Graphics2D g2, float s) {
            g2.setStroke(new BasicStroke(1.8f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D.Float f = new Path2D.Float();
            f.moveTo(3.5f * s, 4.5f * s);
            f.lineTo(20.5f * s, 4.5f * s);
            f.lineTo(14f * s, 12.5f * s);
            f.lineTo(14f * s, 18.5f * s);
            f.lineTo(10f * s, 20.5f * s);
            f.lineTo(10f * s, 12.5f * s);
            f.closePath();
            g2.draw(f);
        }

        private void paintEye(Graphics2D g2, float s) {
            g2.setStroke(new BasicStroke(1.8f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D.Float eye = new Path2D.Float();
            eye.moveTo(2.5f * s, 12f * s);
            eye.curveTo(6f * s, 5.5f * s, 18f * s, 5.5f * s, 21.5f * s, 12f * s);
            eye.curveTo(18f * s, 18.5f * s, 6f * s, 18.5f * s, 2.5f * s, 12f * s);
            eye.closePath();
            g2.draw(eye);
            g2.draw(new Ellipse2D.Float(9f * s, 9f * s, 6f * s, 6f * s));
        }

        private void paintEdit(Graphics2D g2, float s) {
            g2.setStroke(new BasicStroke(1.8f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D.Float pen = new Path2D.Float();
            pen.moveTo(14f * s, 4.5f * s);
            pen.lineTo(19.5f * s, 10f * s);
            pen.lineTo(8.5f * s, 21f * s);
            pen.lineTo(3.5f * s, 21f * s);
            pen.lineTo(3.5f * s, 16f * s);
            pen.closePath();
            g2.draw(pen);
        }

        private void paintGrid(Graphics2D g2, float s) {
            g2.setStroke(new BasicStroke(1.8f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new RoundRectangle2D.Float(3.5f * s, 3.5f * s, 7f * s, 7f * s, 2f * s, 2f * s));
            g2.draw(new RoundRectangle2D.Float(13.5f * s, 3.5f * s, 7f * s, 7f * s, 2f * s, 2f * s));
            g2.draw(new RoundRectangle2D.Float(3.5f * s, 13.5f * s, 7f * s, 7f * s, 2f * s, 2f * s));
            g2.draw(new RoundRectangle2D.Float(13.5f * s, 13.5f * s, 7f * s, 7f * s, 2f * s, 2f * s));
        }

        private void paintList(Graphics2D g2, float s) {
            g2.setStroke(new BasicStroke(2.0f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine((int)(4 * s), (int)(6 * s), (int)(6 * s), (int)(6 * s));
            g2.drawLine((int)(9 * s), (int)(6 * s), (int)(20 * s), (int)(6 * s));
            g2.drawLine((int)(4 * s), (int)(12 * s), (int)(6 * s), (int)(12 * s));
            g2.drawLine((int)(9 * s), (int)(12 * s), (int)(20 * s), (int)(12 * s));
            g2.drawLine((int)(4 * s), (int)(18 * s), (int)(6 * s), (int)(18 * s));
            g2.drawLine((int)(9 * s), (int)(18 * s), (int)(20 * s), (int)(18 * s));
        }

        private void paintMail(Graphics2D g2, float s) {
            g2.setStroke(new BasicStroke(1.6f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new RoundRectangle2D.Float(3.5f * s, 5.5f * s, 17f * s, 13f * s, 2f * s, 2f * s));
            Path2D.Float flap = new Path2D.Float();
            flap.moveTo(3.5f * s, 5.5f * s);
            flap.lineTo(12f * s, 12.5f * s);
            flap.lineTo(20.5f * s, 5.5f * s);
            g2.draw(flap);
        }

        private void paintPhone(Graphics2D g2, float s) {
            g2.setStroke(new BasicStroke(1.6f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new RoundRectangle2D.Float(6.5f * s, 3.5f * s, 11f * s, 17f * s, 3f * s, 3f * s));
            g2.drawLine((int)(10 * s), (int)(18 * s), (int)(14 * s), (int)(18 * s));
        }

        private void paintLocation(Graphics2D g2, float s) {
            g2.setStroke(new BasicStroke(1.6f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D.Float pin = new Path2D.Float();
            pin.moveTo(12 * s, 2.5f * s);
            pin.curveTo(8f * s, 2.5f * s, 5.5f * s, 5.5f * s, 5.5f * s, 9.5f * s);
            pin.curveTo(5.5f * s, 14.5f * s, 12f * s, 21.5f * s, 12f * s, 21.5f * s);
            pin.curveTo(12f * s, 21.5f * s, 18.5f * s, 14.5f * s, 18.5f * s, 9.5f * s);
            pin.curveTo(18.5f * s, 5.5f * s, 16f * s, 2.5f * s, 12f * s, 2.5f * s);
            pin.closePath();
            g2.draw(pin);
            g2.draw(new Ellipse2D.Float(9.5f * s, 7.5f * s, 5f * s, 5f * s));
        }

        // ==========================================
        // NEW SETTINGS ICONS
        // ==========================================
        private void paintTheme(Graphics2D g2, float s) {
            // Paintbrush / Palette Icon
            g2.setStroke(new BasicStroke(1.8f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Ellipse2D.Float(3.5f * s, 3.5f * s, 17f * s, 17f * s));
            g2.fill(new Ellipse2D.Float(7.5f * s, 7.5f * s, 2.5f * s, 2.5f * s));
            g2.fill(new Ellipse2D.Float(13.5f * s, 7.5f * s, 2.5f * s, 2.5f * s));
            g2.fill(new Ellipse2D.Float(7.5f * s, 13.5f * s, 2.5f * s, 2.5f * s));
        }

        private void paintPayment(Graphics2D g2, float s) {
            // Credit Card Icon
            g2.setStroke(new BasicStroke(1.8f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new RoundRectangle2D.Float(3.5f * s, 5.5f * s, 17f * s, 13f * s, 2.5f * s, 2.5f * s));
            g2.drawLine((int)(3.5f * s), (int)(9.5f * s), (int)(20.5f * s), (int)(9.5f * s));
            g2.fillRect((int)(6 * s), (int)(13 * s), (int)(4 * s), (int)(2.5f * s));
        }

        private void paintTerms(Graphics2D g2, float s) {
            // Document / Contract with Lines
            g2.setStroke(new BasicStroke(1.8f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new RoundRectangle2D.Float(4.5f * s, 3.5f * s, 15f * s, 17f * s, 2f * s, 2f * s));
            g2.drawLine((int)(8 * s), (int)(8 * s), (int)(16 * s), (int)(8 * s));
            g2.drawLine((int)(8 * s), (int)(12 * s), (int)(16 * s), (int)(12 * s));
            g2.drawLine((int)(8 * s), (int)(16 * s), (int)(13 * s), (int)(16 * s));
        }

        private void paintSecurity(Graphics2D g2, float s) {
            // Padlock Icon
            g2.setStroke(new BasicStroke(1.8f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Shackle
            Path2D.Float shackle = new Path2D.Float();
            shackle.moveTo(7.5f * s, 10f * s);
            shackle.curveTo(7.5f * s, 5f * s, 16.5f * s, 5f * s, 16.5f * s, 10f * s);
            g2.draw(shackle);
            // Body
            g2.draw(new RoundRectangle2D.Float(5.5f * s, 9.5f * s, 13f * s, 11f * s, 2.5f * s, 2.5f * s));
            // Keyhole
            g2.fill(new Ellipse2D.Float(11f * s, 13f * s, 2f * s, 2f * s));
            g2.drawLine((int)(12 * s), (int)(15 * s), (int)(12 * s), (int)(17.5f * s));
        }

        private void paintSun(Graphics2D g2, float s) {
            g2.setStroke(new BasicStroke(1.8f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Ellipse2D.Float(8f * s, 8f * s, 8f * s, 8f * s));
            // Rays
            g2.drawLine((int)(12 * s), (int)(2.5f * s), (int)(12 * s), (int)(5f * s));
            g2.drawLine((int)(12 * s), (int)(19f * s), (int)(12 * s), (int)(21.5f * s));
            g2.drawLine((int)(2.5f * s), (int)(12 * s), (int)(5f * s), (int)(12 * s));
            g2.drawLine((int)(19f * s), (int)(12 * s), (int)(21.5f * s), (int)(12 * s));
        }

        private void paintMoon(Graphics2D g2, float s) {
            g2.setStroke(new BasicStroke(1.8f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D.Float moon = new Path2D.Float();
            moon.moveTo(14f * s, 3.5f * s);
            moon.curveTo(8f * s, 4f * s, 4.5f * s, 9.5f * s, 5.5f * s, 16f * s);
            moon.curveTo(6.5f * s, 19.5f * s, 9.5f * s, 21f * s, 13f * s, 20.5f * s);
            moon.curveTo(10f * s, 18f * s, 9f * s, 12f * s, 13.5f * s, 8f * s);
            moon.curveTo(15.5f * s, 6.5f * s, 17f * s, 6.5f * s, 19f * s, 7f * s);
            moon.curveTo(18f * s, 4.5f * s, 16f * s, 3.5f * s, 14f * s, 3.5f * s);
            moon.closePath();
            g2.draw(moon);
        }

        private void paintCheck(Graphics2D g2, float s) {
            g2.setStroke(new BasicStroke(2.2f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D.Float check = new Path2D.Float();
            check.moveTo(5f * s, 12f * s);
            check.lineTo(10f * s, 17f * s);
            check.lineTo(19f * s, 7f * s);
            g2.draw(check);
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }
}
