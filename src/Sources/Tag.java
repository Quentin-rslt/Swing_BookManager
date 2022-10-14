package Sources;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class Tag extends JLabel {
    String m_tag;
    int m_color;

    public Tag(){
        m_tag="";
    }
    public Tag(String tag){
        JLabel sizelbl = new JLabel(tag);
        initComponent(tag,sizelbl);
        m_tag= tag;
        setText(tag);
    }

    public String getTextTag(){
        return m_tag;
    }
    public int getSizeText(String tag){
        int size=0;
        for(int i=0; i<tag.length();i++)
            size++;
        return size;
    }
    public int getColor(){
        return this.m_color;
    }
    public void setColor(int color){
        this.m_color = color;
        Color col = new Color(m_color);
        double darkness = 1-(0.299*col.getRed() + 0.587*col.getGreen() + 0.114*col.getBlue())/255;
        setBackground(new Color(m_color));

        if(darkness>0.5){
            setForeground(new Color(232,208,208));
        }else
            setForeground(new Color(38,34,34));
//        new Color(m_color)
        AbstractBorder roundBrd = new RoundBorderCp(new Color(38,34,34),3,23,0,0,0);
        setBorder(roundBrd);
    }
    public void setTextTag(String tag){
        this.m_tag=tag;
    }
    public void initComponent(String tag, JLabel lbl){
        Dimension d;

        d = new Dimension((int) lbl.getPreferredSize().getWidth()+50,27);
        setMinimumSize(d);
        setPreferredSize(d);

        setHorizontalAlignment(JLabel.CENTER);
        setOpaque(true);
    }
}
