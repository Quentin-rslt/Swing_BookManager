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
        initComponent(tag);
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
        setBackground(new Color(m_color));

        if(color<(-16777216/2)){
            setForeground(new Color(232,208,208));
        }else
            setForeground(new Color(38,34,34));

        AbstractBorder roundBrd = new RoundBorderCp(new Color(m_color),3,13,0,0);
        setBorder(roundBrd);
    }
    public void setTextTag(String tag){
        this.m_tag=tag;
    }
    public void initComponent(String tag){
        Dimension d;
        if(getSizeText(tag)<2)
            d = new Dimension(getSizeText(tag)+40,22);
        else if(getSizeText(tag)<3)
            d = new Dimension(getSizeText(tag)+50,22);
        else if(getSizeText(tag)<4)
            d = new Dimension(getSizeText(tag)+60,22);
        else if(getSizeText(tag)<6)
            d = new Dimension(getSizeText(tag)+70,22);
        else if(getSizeText(tag)<10)
            d = new Dimension(getSizeText(tag)+80,22);
        else
            d = new Dimension(getSizeText(tag)*9,22);
        setHorizontalAlignment(JLabel.CENTER);
        setOpaque(true);
        setMinimumSize(d);
        setPreferredSize(d);
    }
}
