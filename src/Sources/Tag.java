package Sources;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;

public class Tag extends JLabel {
    String m_tag;

    public Tag(){
        m_tag="";
    }
    public Tag(String tag){
        initComponent(tag);
        setBackground(new Color(255, 45, 227, 102));
        setText(tag);
        m_tag= tag;
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

    public void setTextTag(String tag){
        this.m_tag=tag;
    }
    public void initComponent(String tag){
        Dimension d;
        if(getSizeText(tag)<10)
            d = new Dimension(getSizeText(tag)*10,20);
        else
            d = new Dimension(getSizeText(tag)*7,20);
        setHorizontalAlignment(JLabel.CENTER);
        setOpaque(true);
        setMinimumSize(d);
        setPreferredSize(d);
    }
}
