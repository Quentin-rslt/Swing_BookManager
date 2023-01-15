package Sources.Components;

import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;

public class MyManagerRoundBorderComponents extends AbstractBorder {
    final Color color;
    final int thickness;
    final int radii;
    final Insets insets;
    final BasicStroke stroke;
    final int strokePad;
    final int h;
    final int gap;
    final int pad;
    RenderingHints hints;

    public MyManagerRoundBorderComponents(Color color, int thickness, int radii, int height, int gap, int pad) {
        this.thickness = thickness;
        this.radii = radii;
        this.color = color;
        this.h = height;
        this.gap=gap;
        this.pad = pad;
        stroke = new BasicStroke(thickness);
        strokePad = thickness / 2;

        hints = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int bottomPad = pad + strokePad;
        insets = new Insets(pad, pad, bottomPad+1, pad+1);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return insets;
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        return getBorderInsets(c);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {

        Graphics2D g2 = (Graphics2D) g;

        int bottomLineY = height - thickness;

        RoundRectangle2D.Double bubble = new RoundRectangle2D.Double(strokePad+gap, strokePad, width - (thickness+(gap*2)), bottomLineY+h, radii, radii);

        Area area = new Area(bubble);
        g2.setRenderingHints(hints);

        // Paint the BG color of the parent, everywhere outside the clip
        // of the text bubble.
        Component parent  = c.getParent();
        if (parent!=null) {
            Color bg = parent.getBackground();
            Rectangle rect = new Rectangle(0,0,width, height+h);
            Area borderRegion = new Area(rect);
            borderRegion.subtract(area);
            g2.setClip(borderRegion);
            g2.setColor(bg);
            g2.fillRect(0, 0, width, height+h);
            g2.setClip(null);
        }

        g2.setColor(color);
        g2.setStroke(stroke);
        g2.draw(area);
    }
}
