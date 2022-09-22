package Sources;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;

public class Tag extends JButton {
    String m_tag;

    // Couleurs du feu
    private static final Color// R    V    B
            science_fiction = new Color( 32,  32,  32),
            horreur   = new Color(  0, 180,   0),
            Fantastique = new Color(255, 160,   0),
            Polar  = new Color(255,   0,   0);

    static enum color
    {
        Science,
        Horreur,
        Fantastique,
        Polar
    };

    public Tag(){
        m_tag="";
    }
    public Tag(String tag){
        setFocusable(false);
        setBackground(science_fiction);
        Dimension d = new Dimension(150,25);
        setBorderPainted(false);
        setFocusPainted(false);
        setFocusable(true);
        setMinimumSize(d);
        setPreferredSize(d);
        setText(tag);
        m_tag= tag;
    }

    public String getTextTag(){
        return m_tag;
    }

    public void setTag(String tag){
        this.m_tag=tag;
    }
}
