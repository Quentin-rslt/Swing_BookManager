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
        setColor(stringToHex(tag));setBackground(new Color(getColor()));
        setText(tag);
        m_tag= tag;
        AbstractBorder roundBrd = new RoundBorderCp(new Color(getColor()),3,13,0,0);
        setBorder(roundBrd);
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
    public int stringToHex(String tag){
        byte[] getBytesFromString = tag.getBytes(StandardCharsets.UTF_8);
        BigInteger bigInteger = new BigInteger(1, getBytesFromString);

        return bigInteger.pow(2).intValue();
    }
    public int getColor(){
        return this.m_color;
    }

    public void setColor(int color){
        this.m_color = color;
    }
    public void setTextTag(String tag){
        this.m_tag=tag;
    }
    public void initComponent(String tag){
        Dimension d;
        if(getSizeText(tag)<6)
            d = new Dimension(getSizeText(tag)*13,21);
        else if(getSizeText(tag)<10)
            d = new Dimension(getSizeText(tag)*12,21);
        else
            d = new Dimension(getSizeText(tag)*9,21);
        setHorizontalAlignment(JLabel.CENTER);
        setOpaque(true);
        setMinimumSize(d);
        setPreferredSize(d);
    }
}
